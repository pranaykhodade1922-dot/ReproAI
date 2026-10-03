import asyncio

SUPPORTED_EVENTS={"TOKEN_EXPIRED", "PAYMENT_FAILED", "PAYMENT_SUCCESS", "TOKEN_REFRESHED", "PAY_BUTTON_CLICKED", "PAYMENT_NETWORK_CHANGED", "PAYMENT_RETRY",
    "CHECKOUT_OPENED","PAYMENT_METHOD_SELECTED","ORIENTATION_CHANGED","CHECKOUT_RECREATED","CHECKOUT_STATE_LOST","CHECKOUT_INVALID",
    "PAYMENT_BLOCKED","CHECKOUT_STATE_RESTORED","CHECKOUT_VALID","PAYMENT_AVAILABLE"}
from app.models.execution import StepStatus

class DemoShopAssertionProvider:
    def __init__(self, bridge):
        self.bridge = bridge

    async def evaluate(self, assertion):
        state = await asyncio.to_thread(self.bridge.state, True)
        evidence = {"verified": True, "execution_id": self.bridge.execution_id,
                    "source": "DEMOSHOP", "simulated": True,
                    "network_strategy": state.get("network_strategy","DEMOSHOP_DEMO_HOOK")}
        if assertion.type == "ASSERT_EVENT":
            if assertion.target not in SUPPORTED_EVENTS:
                return StepStatus.UNSUPPORTED, "Unknown event target", {"verified": False}
            if assertion.expected.lower() not in ("true", "false"):
                return StepStatus.UNSUPPORTED, "Event expectation must be True or False", {"verified": False}
            observed = assertion.target in state["events"]
            expected = assertion.expected.lower() == "true"
            evidence.update(event=assertion.target, expected=expected, observed=observed)
        elif assertion.type == "ASSERT_API_STATUS":
            if assertion.target != "/payment" or not assertion.expected.isdigit():
                return StepStatus.UNSUPPORTED, "Unsupported API assertion", {"verified": False}
            observed, expected = state.get("lastApiStatus"), int(assertion.expected)
            evidence.update(endpoint="/payment", expected=expected, observed=observed)
        else:
            return StepStatus.UNSUPPORTED, "Only measured event/API assertions are supported", {"verified": False}
        passed = observed == expected
        return (StepStatus.PASSED if passed else StepStatus.FAILED,
                f"Measured {assertion.target}: expected {expected}, observed {observed}.", evidence)
