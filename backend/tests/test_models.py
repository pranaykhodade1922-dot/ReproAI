import pytest
from pydantic import ValidationError
from app.models.scenario import TestScenario, TestActionItem, TestAction, TestAssertion, TestPrecondition
from app.models.execution import ExecutionResult, ExecutionStatus, StepStatus

def test_scenario_serialization():
    scenario = TestScenario(
        id="scen_001",
        name="Test Payment",
        description="Demo test scenario",
        preconditions=[TestPrecondition(type="NETWORK", value="WIFI")],
        steps=[
            TestActionItem(action=TestAction.OPEN_SCREEN, target="Checkout"),
            TestActionItem(action=TestAction.TAP, target="PAY"),
            TestActionItem(action=TestAction.WAIT, value="1000")
        ],
        assertions=[
            TestAssertion(type="ASSERT_API_STATUS", target="/payment", expected="401")
        ]
    )
    data = scenario.model_dump()
    assert data["id"] == "scen_001"
    assert data["steps"][0]["action"] == "OPEN_SCREEN"
    assert len(data["steps"]) == 3

def test_invalid_action_rejected():
    with pytest.raises(ValidationError):
        TestActionItem(action="INVALID_ACTION")
