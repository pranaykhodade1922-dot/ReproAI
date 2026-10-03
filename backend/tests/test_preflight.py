import base64
import json
from unittest.mock import Mock
import pytest
from fastapi.testclient import TestClient
from main import app
from app.config import settings
from app.models.scenario import TestScenario, TestActionItem, TestAction
from app.services.preflight import DemoPreflight


def scenario():
    return TestScenario(id="payment", name="Payment", steps=[TestActionItem(action=TestAction.TAP, target="PAY")])


def device_adb(missing=None, hooks=True):
    adb=Mock()
    adb.list_devices.return_value=[("phone", "device")]
    def shell(serial,args):
        if args[:2] == ["pm", "path"]:
            return "" if args[2] == missing else "package:/data/app/base.apk"
        if args[-1] == "STATE":
            state={"source":"DEMOSHOP","simulated":True,"events":[]}
            return "Bundle[{data="+base64.b64encode(json.dumps(state).encode()).decode()+"}]" if hooks else "no provider"
        raise AssertionError("Preflight must be read-only")
    adb.shell.side_effect=shell
    return adb


@pytest.fixture(autouse=True)
def config(monkeypatch):
    monkeypatch.setattr(settings,"RUNNER_MODE","adb")
    monkeypatch.setattr(settings,"DEFAULT_DEVICE_SERIAL","phone")
    monkeypatch.setattr(settings,"NETWORK_STRATEGY","DETERMINISTIC_DEMO")


def test_ready_reads_installs_and_hooks_without_mutation():
    adb=device_adb()
    r=DemoPreflight(adb).inspect(scenario())
    assert r["ready"] and r["executionMode"] == "ADB"
    assert set(r["checks"]) == {"Runner","Scenario","Device","DemoShop","ReproAI","Automation hooks"}
    assert all(call.args[1][-1] != "RESET_DEMO" for call in adb.shell.call_args_list)


def test_mock_requires_clear_warning_and_never_queries_device(monkeypatch):
    monkeypatch.setattr(settings,"RUNNER_MODE","mock")
    adb=Mock();r=DemoPreflight(adb).inspect(scenario())
    assert r["ready"] and "Mock runner enabled" in r["warnings"][0]
    adb.list_devices.assert_not_called()


def test_unavailable_device_is_actionable():
    adb=device_adb();adb.list_devices.return_value=[]
    r=DemoPreflight(adb).inspect(scenario())
    assert not r["ready"] and "authorized" in r["issues"][0]
    adb.shell.assert_not_called()


@pytest.mark.parametrize("package",["com.pranay.demoshop","com.pranay.reproai"])
def test_missing_apk_is_not_uninstalled_for_testing(package):
    assert not DemoPreflight(device_adb(missing=package)).inspect(scenario())["ready"]


def test_missing_hooks_and_real_network_are_not_ready(monkeypatch):
    assert not DemoPreflight(device_adb(hooks=False)).inspect(scenario())["ready"]
    monkeypatch.setattr(settings,"NETWORK_STRATEGY","REAL_NETWORK")
    assert not DemoPreflight(device_adb()).inspect(scenario())["ready"]


def test_invalid_wait_and_custom_are_not_ready():
    for action,value in [(TestAction.WAIT,"999999"),(TestAction.CUSTOM,None)]:
        s=scenario().model_copy(update={"steps":[TestActionItem(action=action,value=value)]})
        assert not DemoPreflight(device_adb()).inspect(s)["ready"]


def test_reset_disabled_by_default(monkeypatch):
    monkeypatch.setattr(settings,"ENABLE_DEMO_CONTROLS",False)
    assert TestClient(app).post("/demo/reset").status_code == 403


@pytest.mark.asyncio
async def test_preflight_disconnect_is_controlled():
    adb=Mock();adb.list_devices.side_effect=RuntimeError("disconnected")
    assert not (await DemoPreflight(adb).check(scenario()))["ready"]


@pytest.mark.parametrize("confirmed", [True, False])
def test_reset_requires_measured_cleared_state(monkeypatch, confirmed):
    from app.api import routes
    monkeypatch.setattr(settings, "ENABLE_DEMO_CONTROLS", True)
    monkeypatch.setattr(routes.execution_service, "has_active_executions", lambda: False)
    adb=device_adb()
    original=adb.shell.side_effect
    def shell(serial, args):
        if args[-1] != "RESET_PRESENTATION":
            return original(serial,args)
        state={"source":"DEMOSHOP", "useFixedAuth":not confirmed,
               "simulateNetworkTransition":False, "cartCount":0, "paymentStatus":"IDLE",
               "lastApiStatus":None, "events":[], 'checkout':{'selectedPaymentMethod':None,
               'isValid':False,'lastStateEvent':None,'preserveAfterRotation':False,
               'orientationBefore':None,'orientationAfter':None}}
        return "Bundle[{data="+base64.b64encode(json.dumps(state).encode()).decode()+"}]"
    adb.shell.side_effect=shell
    monkeypatch.setattr(routes, "preflight", DemoPreflight(adb))
    response=TestClient(app).post("/demo/reset")
    assert response.status_code == (200 if confirmed else 409)
    if confirmed:
        assert response.json()["historyPreserved"] is True


def test_reset_refuses_active_execution(monkeypatch):
    from app.api import routes
    monkeypatch.setattr(settings,"ENABLE_DEMO_CONTROLS",True)
    monkeypatch.setattr(routes.execution_service,"has_active_executions",lambda:True)
    assert TestClient(app).post("/demo/reset").status_code == 409


@pytest.mark.parametrize('stale', [{'selectedPaymentMethod':'UPI'}, {'isValid':True},
    {'lastStateEvent':'CHECKOUT_STATE_LOST'}, {'preserveAfterRotation':True}, {'orientationAfter':2}])
def test_reset_rejects_stale_checkout_state(monkeypatch, stale):
    from app.api import routes
    monkeypatch.setattr(settings,'ENABLE_DEMO_CONTROLS',True)
    monkeypatch.setattr(routes.execution_service,'has_active_executions',lambda:False)
    adb=device_adb();original=adb.shell.side_effect
    def shell(serial,args):
        if args[-1]!='RESET_PRESENTATION':return original(serial,args)
        checkout={'selectedPaymentMethod':None,'isValid':False,'lastStateEvent':None,
            'preserveAfterRotation':False,'orientationBefore':None,'orientationAfter':None}
        checkout.update(stale)
        state={'source':'DEMOSHOP','useFixedAuth':False,'simulateNetworkTransition':False,
            'cartCount':0,'paymentStatus':'IDLE','lastApiStatus':None,'events':[],'checkout':checkout}
        return 'Bundle[{data='+base64.b64encode(json.dumps(state).encode()).decode()+'}]'
    adb.shell.side_effect=shell
    monkeypatch.setattr(routes,'preflight',DemoPreflight(adb))
    assert TestClient(app).post('/demo/reset').status_code==409
