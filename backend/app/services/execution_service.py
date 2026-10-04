import asyncio
import uuid
from datetime import datetime, timezone
from typing import Dict, Optional
from app.models.scenario import TestScenario
from app.models.execution import ExecutionResult, ExecutionStatus, ExecutionStepResult, StepStatus
from app.runner.scenario_runner import ScenarioRunner
from app.adb.device_manager import DeviceManager
from app.config import settings

class ExecutionService:
    def __init__(self, runner: Optional[ScenarioRunner] = None, device_manager: Optional[DeviceManager] = None):
        self.runner = runner or ScenarioRunner()
        self.device_manager = device_manager or DeviceManager()
        self._executions: Dict[str, ExecutionResult] = {}
        self._tasks: Dict[str, asyncio.Task] = {}
        self._device_locks: Dict[str, asyncio.Lock] = {}

    async def execute_scenario(self, scenario: TestScenario) -> ExecutionResult:
        assertions = scenario.execution_assertions()
        execution_id = f"exec-{uuid.uuid4().hex}"
        is_mock = settings.RUNNER_MODE == "mock"
        serial = "mock-device"
        result = ExecutionResult(execution_id=execution_id, scenario_id=scenario.id, scenario_name=scenario.name,
            execution_purpose=scenario.execution_purpose,
            device_serial=serial, status=ExecutionStatus.PENDING, execution_mode="MOCK" if is_mock else "ADB",
            started_at=datetime.now(timezone.utc).isoformat())
        self._executions[execution_id] = result
        result.steps = [ExecutionStepResult(index=i, action=step.action.value, target=step.target,
            status=StepStatus.PENDING, started_at="", finished_at="", duration_ms=0)
            for i, step in enumerate(scenario.steps)]
        result.steps.extend(ExecutionStepResult(index=len(scenario.steps) + i,
            action=assertion.type if assertion.type.startswith("ASSERT_") else f"ASSERT_{assertion.type}",
            target=assertion.target, status=StepStatus.PENDING, started_at="", finished_at="", duration_ms=0)
            for i, assertion in enumerate(assertions))
        if not is_mock:
            devices = await asyncio.to_thread(self.device_manager.get_connected_devices)
            devices = [d for d in devices if not settings.DEFAULT_DEVICE_SERIAL or d.serial == settings.DEFAULT_DEVICE_SERIAL]
            if len(devices) != 1:
                result.status = ExecutionStatus.ERROR
                result.failure_reason = "Select one authorized online device using DEFAULT_DEVICE_SERIAL."
                result.finished_at = datetime.now(timezone.utc).isoformat()
                return result
            serial = devices[0].serial
            result.device_serial = serial
        self._tasks[execution_id] = asyncio.create_task(self._run(scenario, result, serial, is_mock))
        return result

    async def _run(self, scenario, result, serial, is_mock):
        async def update(step):
            result.steps[step.index] = step
        async def execute():
            result.status = ExecutionStatus.RUNNING
            return await self.runner.run_scenario(scenario, result.execution_id, serial, is_mock, update)
        async def serialized():
            if is_mock:
                return await execute()
            async with self._device_locks.setdefault(serial, asyncio.Lock()):
                return await execute()
        try:
            completed = await asyncio.wait_for(serialized(), timeout=180)
            self._executions[result.execution_id] = completed
        except asyncio.CancelledError:
            result.status = ExecutionStatus.CANCELLED
            result.failure_reason = "Execution cancelled by user."
            result.finished_at = datetime.now(timezone.utc).isoformat()
        except Exception:
            result.status = ExecutionStatus.ERROR
            result.failure_reason = "Runner failed or timed out. Check runner and device connection."
            result.finished_at = datetime.now(timezone.utc).isoformat()
        finally:
            self._tasks.pop(result.execution_id, None)

    def get_execution(self, execution_id):
        return self._executions.get(execution_id)

    def has_active_executions(self):
        return any(result.status in (ExecutionStatus.PENDING, ExecutionStatus.RUNNING)
                   for result in self._executions.values())

    def cancel_execution(self, execution_id):
        result = self.get_execution(execution_id)
        if result and result.status in (ExecutionStatus.PENDING, ExecutionStatus.RUNNING):
            result.status = ExecutionStatus.CANCELLED
            result.failure_reason = "Execution cancelled by user."
            result.finished_at = datetime.now(timezone.utc).isoformat()
            task = self._tasks.get(execution_id)
            if task:
                task.cancel()
        return result
