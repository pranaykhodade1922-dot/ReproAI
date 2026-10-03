import time
import logging
import asyncio
from datetime import datetime, timezone
from typing import Optional, Callable, Awaitable
from app.models.scenario import TestScenario
from app.models.execution import ExecutionResult, ExecutionStepResult, ExecutionStatus, StepStatus
from app.runner.action_registry import ActionRegistry
from app.runner.assertion_engine import AssertionEngine
from app.adb.client import AdbClient
from app.adb.demo_bridge import DemoShopBridge
from app.runner.demo_assertions import DemoShopAssertionProvider
from app.config import settings

logger = logging.getLogger("ReproRunner.ScenarioRunner")

class ScenarioRunner:

    def __init__(
        self,
        adb_client: Optional[AdbClient] = None,
        action_registry: Optional[ActionRegistry] = None,
        assertion_engine: Optional[AssertionEngine] = None
    ):
        self.adb = adb_client or AdbClient()
        self.registry = action_registry or ActionRegistry(self.adb)
        self.assertion_engine = assertion_engine or AssertionEngine()

    async def run_scenario(self, scenario, execution_id, device_serial="mock-device", is_mock=True, on_step_update=None):
        rotation=not is_mock and any(s.action.value=='ROTATE_DEVICE' for s in scenario.steps)
        saved=None
        initial_orientation=None
        result=None
        try:
            if rotation:
                try:
                    saved=await asyncio.to_thread(self.adb.get_rotation_lock,device_serial)
                    initial_orientation=await asyncio.to_thread(self.adb.get_surface_orientation,device_serial)
                    await asyncio.to_thread(self.adb.set_orientation,device_serial,'PORTRAIT')
                except Exception:
                    stamp=datetime.now(timezone.utc).isoformat()
                    return ExecutionResult(execution_id=execution_id,scenario_id=scenario.id,scenario_name=scenario.name,
                        device_serial=device_serial,execution_mode='ADB',status=ExecutionStatus.FAILED,
                        started_at=stamp,finished_at=stamp,failure_reason='Device rotation is unsupported or unavailable.',
                        steps=[ExecutionStepResult(index=0,action='ROTATE_DEVICE',status=StepStatus.UNSUPPORTED,
                            started_at=stamp,finished_at=stamp,duration_ms=0,message='Actual device orientation could not be controlled.')])
            result=await self._run_scenario(scenario,execution_id,device_serial,is_mock,on_step_update)
        finally:
            if rotation and isinstance(saved,str):
                await asyncio.to_thread(self.adb.restore_rotation_lock,device_serial,saved)
                if result is not None:
                    result.setup_evidence['rotation_initial']={'lock':saved,'surface_orientation':initial_orientation}
                    result.observed_state['rotation_restore']={'lock':saved,'verified':True,
                        'surface_orientation':await asyncio.to_thread(self.adb.get_surface_orientation,device_serial)}
        return result

    async def _run_scenario(
        self,
        scenario: TestScenario,
        execution_id: str,
        device_serial: str = "mock-device",
        is_mock: bool = True,
        on_step_update: Optional[Callable[[ExecutionStepResult], Awaitable[None]]] = None
    ) -> ExecutionResult:
        started_at = datetime.now(timezone.utc).isoformat()
        start_time = time.time()

        step_results = []
        assertions_passed = 0
        assertions_failed = 0
        overall_status = ExecutionStatus.PASSED
        failure_reason = None
        bridge = None
        setup_evidence = {}
        observed_state = {}
        if not is_mock:
            try:
                if settings.NETWORK_STRATEGY != "DETERMINISTIC_DEMO":
                    raise RuntimeError("REAL_NETWORK is unsupported; no strategy fallback was performed.")
                bridge = DemoShopBridge(self.adb, device_serial, execution_id)
                setup_evidence = await asyncio.to_thread(bridge.call, "RESET_DEMO", True)
                setup_evidence["action"] = "RESET_DEMO"
            except Exception as exc:
                return ExecutionResult(execution_id=execution_id, scenario_id=scenario.id, scenario_name=scenario.name,
                    device_serial=device_serial, execution_mode="ADB", status=ExecutionStatus.ERROR,
                    started_at=started_at, finished_at=datetime.now(timezone.utc).isoformat(),
                    failure_reason="REAL_NETWORK is unsupported; no fallback performed." if settings.NETWORK_STRATEGY == "REAL_NETWORK"
                        else "DemoShop debug automation is unavailable. Check device connection and install the current debug APK.")

        logger.info("Starting scenario execution id=%s mode=%s", execution_id, 'MOCK' if is_mock else 'ADB')

        # Execute Test Actions
        for idx, step in enumerate(scenario.steps):
            step_start_str = datetime.now(timezone.utc).isoformat()
            step_start_time = time.time()

            if on_step_update:
                await on_step_update(ExecutionStepResult(index=idx, action=step.action.value,
                    target=step.target, status=StepStatus.RUNNING, started_at=step_start_str,
                    finished_at="", duration_ms=0))

            status, msg, evidence = await self.registry.execute_step(
                step=step,
                device_serial=device_serial,
                is_mock=is_mock,
                bridge=bridge
            )

            step_end_time = time.time()
            step_end_str = datetime.now(timezone.utc).isoformat()
            duration_ms = (step_end_time - step_start_time) * 1000.0

            step_res = ExecutionStepResult(
                index=idx,
                action=step.action.value,
                target=step.target,
                status=status,
                started_at=step_start_str,
                finished_at=step_end_str,
                duration_ms=round(duration_ms, 2),
                message=msg,
                evidence=evidence
            )
            step_results.append(step_res)

            if on_step_update:
                await on_step_update(step_res)

            if status in (StepStatus.FAILED, StepStatus.UNSUPPORTED):
                overall_status = ExecutionStatus.FAILED
                failure_reason = f"Step {idx + 1} ({step.action.value}) failed: {msg}"

        # Execute Assertions
        for idx, assertion in enumerate(scenario.assertions):
            step_idx = len(scenario.steps) + idx
            step_start_str = datetime.now(timezone.utc).isoformat()
            step_start_time = time.time()

            if on_step_update:
                await on_step_update(ExecutionStepResult(index=step_idx,
                    action=assertion.type if assertion.type.startswith("ASSERT_") else f"ASSERT_{assertion.type}",
                    target=assertion.target, status=StepStatus.RUNNING, started_at=step_start_str,
                    finished_at="", duration_ms=0))

            try:
                if is_mock:
                    status, msg, evidence = await self.assertion_engine.evaluate_assertion(assertion, device_serial, True)
                else:
                    status, msg, evidence = await DemoShopAssertionProvider(bridge).evaluate(assertion)
            except Exception:
                status, msg, evidence = StepStatus.FAILED, "Device assertion evidence could not be read.", {"verified": False}

            step_end_time = time.time()
            step_end_str = datetime.now(timezone.utc).isoformat()
            duration_ms = (step_end_time - step_start_time) * 1000.0

            if status == StepStatus.PASSED:
                assertions_passed += 1
            else:
                assertions_failed += 1
                if overall_status == ExecutionStatus.PASSED:
                    overall_status = ExecutionStatus.FAILED
                    failure_reason = f"Assertion {assertion.type} failed: {msg}"

            step_res = ExecutionStepResult(
                index=step_idx,
                action=assertion.type if assertion.type.startswith("ASSERT_") else f"ASSERT_{assertion.type}",
                target=assertion.target,
                status=status,
                started_at=step_start_str,
                finished_at=step_end_str,
                duration_ms=round(duration_ms, 2),
                message=msg,
                evidence=evidence
            )
            step_results.append(step_res)

            if on_step_update:
                await on_step_update(step_res)

        if bridge:
            try:
                observed_state = await asyncio.to_thread(bridge.state)
                if observed_state.get("paymentStatus") == "PROCESSING":
                    observed_state = await asyncio.to_thread(bridge.state, True)
                observed_state["verified"] = True
            except Exception:
                overall_status = ExecutionStatus.ERROR
                failure_reason = "Final execution-scoped DemoShop evidence could not be read."
        end_time = time.time()
        finished_at = datetime.now(timezone.utc).isoformat()
        total_duration_ms = (end_time - start_time) * 1000.0

        return ExecutionResult(
            execution_id=execution_id,
            scenario_id=scenario.id,
            scenario_name=scenario.name,
            device_serial=device_serial,
            status=overall_status,
            execution_mode="MOCK" if is_mock else "ADB",
            started_at=started_at,
            finished_at=finished_at,
            duration_ms=round(total_duration_ms, 2),
            steps=step_results,
            assertions_passed=assertions_passed,
            assertions_failed=assertions_failed,
            failure_reason=failure_reason,
            network_strategy=("ANDROID_CONFIGURATION" if any(s.action.value=='ROTATE_DEVICE' for s in scenario.steps) else "DEMOSHOP_DEMO_HOOK") if bridge else None,
            observed_state=observed_state, setup_evidence=setup_evidence
        )
