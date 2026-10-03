import pytest
from app.models.scenario import TestScenario, TestActionItem, TestAction, TestAssertion
from app.models.execution import ExecutionStatus, StepStatus
from app.runner.scenario_runner import ScenarioRunner

@pytest.mark.asyncio
async def test_mock_scenario_execution():
    runner = ScenarioRunner()
    scenario = TestScenario(
        id="scen_002",
        name="Mock Execution Test",
        steps=[
            TestActionItem(action=TestAction.OPEN_SCREEN, target="Home"),
            TestActionItem(action=TestAction.WAIT, value="500")
        ],
        assertions=[
            TestAssertion(type="ASSERT_VISIBLE", target="MainScreen", expected="True")
        ]
    )

    result = await runner.run_scenario(scenario, execution_id="exec-test-01", is_mock=True)

    assert result.status == ExecutionStatus.PASSED
    assert result.execution_mode == "MOCK"
    assert len(result.steps) == 3
    assert result.assertions_passed == 1
    assert result.assertions_failed == 0
