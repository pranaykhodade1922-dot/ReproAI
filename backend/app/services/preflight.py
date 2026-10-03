"""Read-only demo readiness. Never resets state or falls back to mock."""
import asyncio
import base64
import json
import re
from app.config import settings
from app.models.scenario import TestScenario
from app.adb.client import AdbClient
from app.runner.action_registry import TARGETS
from app.runner.demo_assertions import SUPPORTED_EVENTS


class DemoPreflight:
    def __init__(self, adb=None):
        self.adb = adb or AdbClient()

    @staticmethod
    def decode_state(raw):
        match=re.search(r"data=([A-Za-z0-9+/=]+)",raw)
        return json.loads(base64.b64decode(match.group(1),validate=True)) if match else {}

    def inspect(self, scenario: TestScenario | None = None):
        issues, warnings, checks = [], [], ["Runner"]
        mode = settings.RUNNER_MODE.upper()
        if scenario is not None:
            if not scenario.id.strip() or not scenario.name.strip() or not scenario.steps:
                issues.append("Scenario must have an ID, name and at least one action. Analyze the incident again.")
            for step in scenario.steps:
                action, target, value = step.action.value, step.target, step.value
                if action == "CUSTOM":
                    issues.append("CUSTOM actions are not supported.")
                if action == "WAIT":
                    try:
                        assert 0 <= int(value or "") <= 10000
                    except (ValueError, AssertionError):
                        issues.append("WAIT must be 0..10000 milliseconds.")
                if action == "ROTATE_DEVICE" and value not in ("PORTRAIT","LANDSCAPE"):
                    issues.append("Rotation must request PORTRAIT or LANDSCAPE.")
                if mode == "ADB":
                    if action == "OPEN_SCREEN" and target not in ("Home", "Product", "Cart", "Checkout"):
                        issues.append("Use a supported DemoShop screen.")
                    elif action == "TAP" and target not in TARGETS:
                        issues.append("Use a supported DemoShop automation target.")
                    elif action == "CHANGE_NETWORK" and value not in ("CELLULAR", "WIFI_TO_CELLULAR"):
                        issues.append("Use the supported deterministic demo transition.")
                    elif action in ("ASSERT_VISIBLE", "ASSERT_TEXT"):
                        issues.append("Only measured DemoShop event/API assertions are supported.")
            if mode == "ADB":
                for assertion in scenario.assertions:
                    if assertion.type not in ("ASSERT_API_STATUS", "ASSERT_EVENT"):
                        issues.append("Only measured DemoShop event/API assertions are supported.")
                    elif assertion.type == "ASSERT_API_STATUS" and (assertion.target != "/payment" or not assertion.expected.isdigit()):
                        issues.append("API assertions must use /payment and a numeric status.")
                    elif assertion.type == "ASSERT_EVENT" and (assertion.target not in SUPPORTED_EVENTS or assertion.expected.lower() not in ("true", "false")):
                        issues.append("Use a supported event and a True/False expectation.")
            if not issues:
                checks.append("Scenario")
        if mode == "MOCK":
            warnings.append("Mock runner enabled. No physical reproduction or fix is proven.")
            return dict(ready=not issues, executionMode=mode, deviceSerial=None,
                        checks=checks, issues=issues, warnings=warnings)
        serial = None
        if (settings.DEMOSHOP_PACKAGE, settings.REPROAI_PACKAGE) != ("com.pranay.demoshop", "com.pranay.reproai"):
            issues.append("This demo supports only the known DemoShop and ReproAI packages.")
            return dict(ready=False,executionMode=mode,deviceSerial=None,checks=checks,issues=issues,warnings=warnings)
        try:
            online = [s for s, state in self.adb.list_devices() if state == "device" and
                      (not settings.DEFAULT_DEVICE_SERIAL or s == settings.DEFAULT_DEVICE_SERIAL)]
            if len(online) != 1:
                issues.append("No single authorized Android device selected. Connect/unlock the phone and update DEFAULT_DEVICE_SERIAL to its current ADB serial.")
            else:
                serial = online[0]
                checks.append("Device")
                for package, label in ((settings.DEMOSHOP_PACKAGE, "DemoShop"), (settings.REPROAI_PACKAGE, "ReproAI")):
                    if self.adb.shell(serial, ["pm", "path", package]).strip().startswith("package:"):
                        checks.append(label)
                    else:
                        issues.append(f"{label} missing. Install the current debug APK.")
                if "DemoShop" in checks:
                    raw = self.adb.shell(serial, ["content", "call", "--uri", "content://com.pranay.demoshop.automation", "--method", "STATE"])
                    state = self.decode_state(raw)
                    if state.get("source") == "DEMOSHOP" and state.get("simulated") is True and isinstance(state.get("events"), list):
                        checks.append("Automation hooks")
                    else:
                        issues.append("DemoShop debug automation unavailable. Install the debug APK, not release.")
        except Exception:
            issues.append("Device/hook check failed. Reconnect ADB and install the current DemoShop debug APK.")
        if settings.NETWORK_STRATEGY != "DETERMINISTIC_DEMO":
            issues.append("Physical network automation is unsupported. Set NETWORK_STRATEGY=DETERMINISTIC_DEMO for this demo.")
        return dict(ready=not issues, executionMode=mode, deviceSerial=serial,
                    checks=checks, issues=list(dict.fromkeys(issues)), warnings=warnings)

    async def check(self, scenario=None):
        try:
            return await asyncio.wait_for(asyncio.to_thread(self.inspect, scenario), 18)
        except asyncio.TimeoutError:
            return dict(ready=False, executionMode=settings.RUNNER_MODE.upper(), deviceSerial=None,
                        checks=["Runner"], issues=["Device preflight timed out. Reconnect the phone and retry."], warnings=[])
