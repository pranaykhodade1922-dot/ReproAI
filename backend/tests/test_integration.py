import asyncio
import pytest
from unittest.mock import Mock
from fastapi.testclient import TestClient
from main import app
from app.config import settings
from app.models.scenario import TestScenario, TestActionItem, TestAction, TestAssertion
from app.models.execution import ExecutionStatus, StepStatus
from app.services.execution_service import ExecutionService
from app.runner.assertion_engine import AssertionEngine

@pytest.mark.asyncio
async def test_cancel_inflight_execution(monkeypatch):
    monkeypatch.setattr(settings, "RUNNER_MODE", "mock")
    service = ExecutionService()
    result = await service.execute_scenario(TestScenario(id="cancel", name="cancel", steps=[TestActionItem(action=TestAction.WAIT, value="10000")]))
    await asyncio.sleep(0)
    assert service.cancel_execution(result.execution_id).status == ExecutionStatus.CANCELLED
    await asyncio.sleep(0)
    assert service.get_execution(result.execution_id).status == ExecutionStatus.CANCELLED

@pytest.mark.asyncio
async def test_adb_missing_device_never_falls_back(monkeypatch):
    monkeypatch.setattr(settings, "RUNNER_MODE", "adb")
    manager = Mock()
    manager.get_connected_devices.return_value = []
    result = await ExecutionService(device_manager=manager).execute_scenario(TestScenario(id="missing", name="missing"))
    assert result.execution_mode == "ADB"
    assert result.status == ExecutionStatus.ERROR

@pytest.mark.asyncio
async def test_real_assertions_cannot_claim_canned_evidence():
    status, _, evidence = await AssertionEngine().evaluate_assertion(TestAssertion(type="ASSERT_API_STATUS", target="/payment", expected="401"), is_mock=False)
    assert status == StepStatus.UNSUPPORTED
    assert evidence["verified"] is False

def test_websocket_snapshot_and_validation(monkeypatch):
    monkeypatch.setattr(settings, "RUNNER_MODE", "mock")
    with TestClient(app) as client:
        assert client.post("/execute", json={"id": "invalid", "name": "invalid", "steps": [{"action": "SHELL"}]}).status_code == 422
        result = client.post("/execute", json={"id": "ws", "name": "ws", "steps": [{"action": "WAIT", "value": "500"}]}).json()
        with client.websocket_connect(f'/ws/executions/{result["execution_id"]}') as socket:
            snapshot = socket.receive_json()
            assert snapshot["execution_id"] == result["execution_id"]
            while snapshot["status"] in ("PENDING", "RUNNING"):
                snapshot = socket.receive_json()
            assert snapshot["status"] == "PASSED"
            assert snapshot["execution_mode"] == "MOCK"
