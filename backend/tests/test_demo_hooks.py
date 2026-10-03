import base64, json
from unittest.mock import Mock
import pytest
from app.adb.client import AdbException
from app.adb.demo_bridge import DemoShopBridge
from app.adb.device_manager import DeviceManager
from app.config import settings
from app.models.scenario import TestScenario, TestActionItem, TestAction, TestAssertion
from app.models.execution import StepStatus, ExecutionStatus
from app.runner.demo_assertions import DemoShopAssertionProvider
from app.runner.action_registry import ActionRegistry
from app.runner.scenario_runner import ScenarioRunner


def encoded_state(execution='current', events=None, status=401):
    state={'executionId':execution,'source':'DEMOSHOP','simulated':True,'events':events or [],'lastApiStatus':status,'paymentStatus':'FAILED'}
    return 'Bundle[{data='+base64.b64encode(json.dumps(state).encode()).decode()+'}]'


def test_bridge_rejects_stale_evidence_and_arbitrary_actions():
    adb=Mock(); adb.shell.return_value=encoded_state('previous', ['TOKEN_EXPIRED'])
    bridge=DemoShopBridge(adb,'device','current')
    with pytest.raises(AdbException, match='different execution'): bridge.state()
    with pytest.raises(AdbException, match='Unknown'): bridge.call('SHELL')
    assert adb.shell.call_count == 1


def test_disconnection_during_device_discovery_is_controlled():
    adb = Mock()
    adb.list_devices.return_value = [('device', 'device')]
    adb.get_prop.side_effect = AdbException('disconnected')
    assert DeviceManager(adb).get_connected_devices() == []


@pytest.mark.asyncio
async def test_assertions_compare_device_state_without_canned_success():
    adb=Mock(); adb.shell.return_value=encoded_state(events=['PAYMENT_FAILED'])
    provider=DemoShopAssertionProvider(DemoShopBridge(adb,'device','current'))
    status,_,ev=await provider.evaluate(TestAssertion(type='ASSERT_EVENT',target='TOKEN_EXPIRED',expected='True'))
    assert status == StepStatus.FAILED and ev['observed'] is False and ev['verified']
    status,_,ev=await provider.evaluate(TestAssertion(type='ASSERT_API_STATUS',target='/payment',expected='401'))
    assert status == StepStatus.PASSED and ev['observed'] == 401
    adb.shell.return_value=encoded_state(events=['TOKEN_REFRESHED','PAYMENT_SUCCESS'],status=200)
    status,_,ev=await provider.evaluate(TestAssertion(type='ASSERT_API_STATUS',target='/payment',expected='401'))
    assert status == StepStatus.FAILED and ev['observed'] == 200


@pytest.mark.asyncio
async def test_unknown_target_is_unsupported_without_command():
    adb=Mock()
    status,_,_=await ActionRegistry(adb).execute_step(TestActionItem(action=TestAction.TAP,target='arbitrary'),bridge=Mock())
    assert status == StepStatus.UNSUPPORTED
    adb.shell.assert_not_called()


@pytest.mark.asyncio
async def test_missing_debug_provider_is_controlled_error(monkeypatch):
    monkeypatch.setattr(settings,'NETWORK_STRATEGY','DETERMINISTIC_DEMO')
    adb=Mock(); adb.shell.return_value='Error: Could not find provider'
    result=await ScenarioRunner(adb).run_scenario(TestScenario(id='test',name='Missing APK'),'current','device',False)
    assert result.status == ExecutionStatus.ERROR and result.execution_mode == 'ADB'
    assert result.observed_state == {} and 'debug' in result.failure_reason


@pytest.mark.asyncio
async def test_real_network_never_silently_falls_back(monkeypatch):
    monkeypatch.setattr(settings,'NETWORK_STRATEGY','REAL_NETWORK')
    adb=Mock()
    result=await ScenarioRunner(adb).run_scenario(TestScenario(id='test',name='Radio test'),'current','device',False)
    assert result.status == ExecutionStatus.ERROR and 'unsupported' in result.failure_reason
    adb.shell.assert_not_called()
