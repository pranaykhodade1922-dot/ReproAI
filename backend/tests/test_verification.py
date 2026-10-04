import base64
import json
from unittest.mock import Mock

import pytest
from app.models.scenario import TestScenario, TestAssertion, TestActionItem, TestAction, ExecutionPurpose
from app.models.execution import ExecutionStatus
from app.runner.scenario_runner import ScenarioRunner
from app.config import settings


def scenario(rotation=False):
    failure = ([TestAssertion(type="ASSERT_EVENT", target=e, expected="True") for e in
                ("CHECKOUT_STATE_LOST", "CHECKOUT_INVALID", "PAYMENT_BLOCKED")] if rotation else [
        TestAssertion(type="ASSERT_EVENT", target="TOKEN_EXPIRED", expected="True"),
        TestAssertion(type="ASSERT_API_STATUS", target="/payment", expected="401"),
        TestAssertion(type="ASSERT_EVENT", target="PAYMENT_FAILED", expected="True")])
    healthy = ([TestAssertion(type="ASSERT_EVENT", target=e, expected="True") for e in
                ("CHECKOUT_STATE_RESTORED", "CHECKOUT_VALID", "PAYMENT_AVAILABLE")] if rotation else [
        TestAssertion(type="ASSERT_EVENT", target="TOKEN_REFRESHED", expected="True"),
        TestAssertion(type="ASSERT_API_STATUS", target="/payment", expected="200"),
        TestAssertion(type="ASSERT_EVENT", target="PAYMENT_SUCCESS", expected="True")])
    return TestScenario(id="original", name="Original scenario", steps=[TestActionItem(
        action=TestAction.ROTATE_DEVICE if rotation else TestAction.TAP,
        target=None if rotation else "PAY", value="LANDSCAPE" if rotation else None)],
        assertions=failure, verification_assertions=healthy)


@pytest.mark.asyncio
@pytest.mark.parametrize("rotation,verify,events,status,expected", [
    (False, False, ["TOKEN_EXPIRED", "PAYMENT_FAILED"], 401, "BUG_REPRODUCED"),
    (False, True, ["TOKEN_REFRESHED", "PAYMENT_SUCCESS"], 200, "FIX_VERIFIED"),
    (False, True, [], None, "VERIFICATION_FAILED"),
    (False, True, ["TOKEN_REFRESHED", "PAYMENT_FAILED"], 401, "VERIFICATION_FAILED"),
    (False, True, ["PAYMENT_SUCCESS"], 200, "VERIFICATION_FAILED"),
    (True, False, ["CHECKOUT_STATE_LOST", "CHECKOUT_INVALID", "PAYMENT_BLOCKED"], None, "BUG_REPRODUCED"),
    (True, True, ["CHECKOUT_STATE_RESTORED", "CHECKOUT_VALID", "PAYMENT_AVAILABLE"], None, "FIX_VERIFIED"),
    (True, True, [], None, "VERIFICATION_FAILED"),
])
async def test_runner_evaluates_selected_positive_profile(monkeypatch, rotation, verify, events, status, expected):
    monkeypatch.setattr(settings, "NETWORK_STRATEGY", "DETERMINISTIC_DEMO")
    original = scenario(rotation)
    request = original.model_copy(update={"execution_purpose": ExecutionPurpose.VERIFY_FIX if verify else ExecutionPurpose.REPRODUCE})
    healthy = expected == "FIX_VERIFIED"
    state = dict(executionId="current", source="DEMOSHOP", simulated=True, visibleScreen="checkout", events=["PAY_BUTTON_CLICKED"] + events,
                 lastApiStatus=status, paymentStatus="SUCCESS" if status == 200 else "FAILED" if status == 401 else "IDLE")
    if rotation:
        state["events"] = ["PAYMENT_METHOD_SELECTED", "ORIENTATION_CHANGED", "CHECKOUT_RECREATED"] + events
        state["checkout"] = dict(orientationBefore=1, orientationAfter=2, isValid=healthy,
                                  selectedPaymentMethod="UPI" if healthy else None)
    adb = Mock()
    adb.shell.return_value = "Bundle[{data=" + base64.b64encode(json.dumps(state).encode()).decode() + "}]"
    adb.get_rotation_lock.return_value = "lock 0"
    adb.get_surface_orientation.return_value = 0
    result = await ScenarioRunner(adb_client=adb).run_scenario(request, "current", "device", False)
    assert result.product_outcome == expected
    assert result.execution_purpose == request.execution_purpose
    assert result.status == (ExecutionStatus.FAILED if expected == "VERIFICATION_FAILED" else ExecutionStatus.PASSED)
    assert result.scenario_id == original.id
    assert request.steps == original.steps and request.preconditions == original.preconditions
    assert request.assertions == original.assertions
    assert [s.target for s in result.steps[len(original.steps):]] == [a.target for a in request.execution_assertions()]
    assert all(s.evidence["execution_id"] == "current" for s in result.steps[len(original.steps):])


def test_missing_or_negative_verification_profile_is_rejected():
    original = scenario()
    for assertions in ([], [TestAssertion(type="ASSERT_EVENT", target="PAYMENT_FAILED", expected="False")],
                       [TestAssertion(type="ASSERT_API_STATUS", target="/payment", expected="200")]):
        request = original.model_copy(update={"execution_purpose": ExecutionPurpose.VERIFY_FIX, "verification_assertions": assertions})
        with pytest.raises(ValueError, match="requires"):
            request.execution_assertions()


def test_legacy_export_null_fields_default_to_reproduction():
    saved = scenario().model_dump(mode="json")
    saved.update(verification_assertions=None, execution_purpose=None)
    imported = TestScenario.model_validate(saved)
    assert imported.execution_purpose == ExecutionPurpose.REPRODUCE
    assert imported.verification_assertions == []
    assert imported.execution_assertions() == imported.assertions


@pytest.mark.asyncio
async def test_mock_cannot_prove_fix():
    request = scenario().model_copy(update={"execution_purpose": ExecutionPurpose.VERIFY_FIX})
    result = await ScenarioRunner().run_scenario(request, "mock", is_mock=True)
    assert result.product_outcome == "UNCONFIRMED"
