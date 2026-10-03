"""Allow-listed debug-only DemoShop bridge. No coordinates or shell interpolation."""
import base64
import json
import re
import time
from app.adb.client import AdbClient, AdbException

class DemoShopBridge:
    URI = "content://com.pranay.demoshop.automation"
    ACTIONS = {"STATE", "RESET_DEMO", "PRODUCT", "CART", "CHECKOUT", "ADD_TO_CART",
               "PROCEED_TO_CHECKOUT", "PAY", "ENABLE_DEMO_NETWORK_TRANSITION", "SELECT_UPI", "SELECT_CARD"}

    def __init__(self, adb: AdbClient, serial: str, execution_id: str):
        self.adb, self.serial, self.execution_id = adb, serial, execution_id

    def call(self, action, reset=False):
        if action not in self.ACTIONS:
            raise AdbException("Unknown DemoShop automation action")
        args = ["content", "call", "--uri", self.URI, "--method", action]
        if reset:
            if not re.fullmatch(r"[a-zA-Z0-9_-]{1,100}", self.execution_id):
                raise AdbException("Invalid execution ID")
            args += ["--arg", self.execution_id]
        out = self.adb.shell(self.serial, args)
        match = re.search(r"data=([A-Za-z0-9+/=]+)", out)
        if not match:
            raise AdbException("DemoShop debug bridge unavailable. Install the current DemoShop debug APK.")
        try:
            state = json.loads(base64.b64decode(match.group(1), validate=True))
        except (ValueError, TypeError) as exc:
            raise AdbException("Invalid DemoShop debug state") from exc
        if (not isinstance(state, dict) or state.get("executionId") != self.execution_id
                or state.get("source") != "DEMOSHOP" or state.get("simulated") is not True
                or not isinstance(state.get("events"), list)):
            raise AdbException("DemoShop evidence belongs to a different execution or is invalid")
        return state

    def state(self, wait_terminal=False):
        deadline = time.monotonic() + (8 if wait_terminal else 0)
        while True:
            state = self.call("STATE")
            if not wait_terminal or state.get("paymentStatus") != "PROCESSING":
                return state
            if time.monotonic() >= deadline:
                raise AdbException("Timed out waiting for on-device payment evidence")
            time.sleep(0.2)

    def open_screen(self, screen):
        routes = {"Home": "product", "Product": "product", "Cart": "cart", "Checkout": "checkout"}
        if screen not in routes:
            raise AdbException("Unsupported DemoShop screen")
        route = routes[screen]
        self.adb.shell(self.serial, ["am", "start", "-W", "-a", "android.intent.action.VIEW",
            "-d", f"reproai-demo://{route}", "-n", "com.pranay.demoshop/.DemoDeepLinkActivity"])
        deadline = time.monotonic() + 8
        while True:
            state = self.state()
            if state.get("visibleScreen") == route:
                return state
            if time.monotonic() >= deadline:
                raise AdbException("DemoShop destination did not become visible")
            time.sleep(.2)
