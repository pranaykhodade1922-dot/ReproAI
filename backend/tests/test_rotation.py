import base64
import json
from unittest.mock import Mock
import pytest
from app.adb.client import AdbClient, AdbException
from app.adb.demo_bridge import DemoShopBridge
from app.models.scenario import TestAssertion, TestActionItem, TestAction
from app.models.execution import StepStatus
from app.runner.demo_assertions import DemoShopAssertionProvider
from app.runner.action_registry import ActionRegistry


def test_rotation_verifies_actual_surface(monkeypatch):
    adb=AdbClient();adb.shell=Mock(side_effect=['','SurfaceOrientation: 1'])
    monkeypatch.setattr('app.adb.client.time.sleep',lambda _:None)
    assert adb.set_orientation('device','LANDSCAPE')
    assert adb.shell.call_args_list[0].args[1]==['wm','user-rotation','lock','1']
    assert adb.shell.call_args_list[1].args[1]==['dumpsys','input']


def test_rotation_never_claims_success_without_measurement(monkeypatch):
    adb=AdbClient();adb.shell=Mock(return_value='')
    monkeypatch.setattr('app.adb.client.time.monotonic',Mock(side_effect=[0,6]))
    with pytest.raises(AdbException,match='not observed'):adb.set_orientation('device','LANDSCAPE')


def test_original_auto_rotation_mode_is_restored():
    adb=AdbClient();adb.shell=Mock(side_effect=['','','free 2'])
    adb.restore_rotation_lock('device','free 2')
    assert [c.args[1] for c in adb.shell.call_args_list]==[
        ['wm','user-rotation','lock','2'],['wm','user-rotation','free'],['wm','user-rotation']]


def test_locked_restore_waits_for_measured_original_display(monkeypatch):
    adb=AdbClient();adb.shell=Mock(side_effect=['','lock 0','SurfaceOrientation: 1','SurfaceOrientation: 0'])
    monkeypatch.setattr('app.adb.client.time.sleep',lambda _:None)
    adb.restore_rotation_lock('device','lock 0')
    assert [c.args[1] for c in adb.shell.call_args_list][-2:]==[['dumpsys','input'],['dumpsys','input']]


def test_locked_restore_never_claims_success_from_settings_alone(monkeypatch):
    adb=AdbClient();adb.shell=Mock(side_effect=['','lock 0'])
    monkeypatch.setattr('app.adb.client.time.monotonic',Mock(side_effect=[0,6]))
    with pytest.raises(AdbException,match='display orientation was not restored'):
        adb.restore_rotation_lock('device','lock 0')


@pytest.mark.parametrize('event',['CHECKOUT_STATE_LOST','CHECKOUT_INVALID','CHECKOUT_STATE_RESTORED','CHECKOUT_VALID','PAYMENT_METHOD_SELECTED'])
@pytest.mark.asyncio
async def test_checkout_assertions_use_actual_bridge_snapshot(event):
    adb=Mock()
    def encode(events):
        state={'executionId':'run','source':'DEMOSHOP','simulated':True,'events':events,'paymentStatus':'IDLE',
            'lastApiStatus':None,'checkout':{'selectedPaymentMethod':'UPI','isValid':True}}
        return 'Bundle[{data='+base64.b64encode(json.dumps(state).encode()).decode()+'}]'
    adb.shell.return_value=encode([event])
    provider=DemoShopAssertionProvider(DemoShopBridge(adb,'device','run'))
    assertion=TestAssertion(type='ASSERT_EVENT',target=event,expected='True')
    assert (await provider.evaluate(assertion))[0]==StepStatus.PASSED
    adb.shell.return_value=encode([])
    assert (await provider.evaluate(assertion))[0]==StepStatus.FAILED


@pytest.mark.asyncio
async def test_rotation_without_checkout_recreation_is_unsupported():
    adb=Mock();bridge=Mock();bridge.state.return_value={'events':[],'checkout':{'orientationBefore':1,'orientationAfter':1}}
    status,_,_=await ActionRegistry(adb).execute_step(TestActionItem(action=TestAction.ROTATE_DEVICE,value='LANDSCAPE'),
        device_serial='device',bridge=bridge)
    assert status==StepStatus.UNSUPPORTED


@pytest.mark.asyncio
async def test_rotation_execution_records_and_restores_initial_device_state():
    from unittest.mock import AsyncMock
    from app.runner.scenario_runner import ScenarioRunner
    from app.models.scenario import TestScenario
    from app.models.execution import ExecutionResult, ExecutionStatus
    scenario=TestScenario(id='rotation', name='Rotation', steps=[TestActionItem(action=TestAction.ROTATE_DEVICE,value='LANDSCAPE')])
    adb=Mock();adb.get_rotation_lock.return_value='free 0';adb.get_surface_orientation.return_value=0
    runner=ScenarioRunner(adb_client=adb)
    runner._run_scenario=AsyncMock(return_value=ExecutionResult(execution_id='run',scenario_id='rotation',scenario_name='Rotation',
        status=ExecutionStatus.PASSED,execution_mode='ADB',started_at='now',network_strategy='ANDROID_CONFIGURATION'))
    result=await runner.run_scenario(scenario,'run','device',False)
    adb.restore_rotation_lock.assert_called_once_with('device','free 0')
    assert result.setup_evidence['rotation_initial']=={'lock':'free 0','surface_orientation':0}
    assert result.observed_state['rotation_restore']['verified'] is True


@pytest.mark.asyncio
async def test_rotation_restores_device_even_when_execution_raises():
    from unittest.mock import AsyncMock
    from app.runner.scenario_runner import ScenarioRunner
    from app.models.scenario import TestScenario
    adb=Mock();adb.get_rotation_lock.return_value='lock 0'
    runner=ScenarioRunner(adb_client=adb);runner._run_scenario=AsyncMock(side_effect=RuntimeError('device disconnected'))
    scenario=TestScenario(id='rotation',name='Rotation',steps=[TestActionItem(action=TestAction.ROTATE_DEVICE,value='LANDSCAPE')])
    with pytest.raises(RuntimeError):await runner.run_scenario(scenario,'run','device',False)
    adb.restore_rotation_lock.assert_called_once_with('device','lock 0')
