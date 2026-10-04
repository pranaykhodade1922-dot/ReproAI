"""Product outcomes require passed assertions and execution-scoped measured evidence."""
from app.models.scenario import ExecutionPurpose
from app.models.execution import ExecutionStatus


def execution_outcome(scenario, status, state, execution_id, is_mock):
    if is_mock:
        return "UNCONFIRMED"
    verifying = scenario.execution_purpose == ExecutionPurpose.VERIFY_FIX
    unsuccessful = "VERIFICATION_FAILED" if verifying else "UNCONFIRMED"
    if (status != ExecutionStatus.PASSED or state.get("verified") is not True or
            state.get("executionId") != execution_id or state.get("source") != "DEMOSHOP"):
        return unsuccessful
    events = set(state.get("events", []))
    if any(s.action.value == "ROTATE_DEVICE" for s in scenario.steps):
        checkout = state.get("checkout", {})
        required = {"CHECKOUT_STATE_RESTORED", "CHECKOUT_VALID", "PAYMENT_AVAILABLE"} if verifying else {
            "CHECKOUT_STATE_LOST", "CHECKOUT_INVALID", "PAYMENT_BLOCKED"}
        measured = (checkout.get("orientationBefore") == 1 and checkout.get("orientationAfter") == 2 and
                    {"PAYMENT_METHOD_SELECTED", "ORIENTATION_CHANGED", "CHECKOUT_RECREATED"} <= events)
        healthy = checkout.get("isValid") is True and checkout.get("selectedPaymentMethod") == "UPI" and "CHECKOUT_STATE_LOST" not in events
        failed = checkout.get("isValid") is False and checkout.get("selectedPaymentMethod") is None
    else:
        required = {"TOKEN_REFRESHED", "PAYMENT_SUCCESS"} if verifying else {"TOKEN_EXPIRED", "PAYMENT_FAILED"}
        measured = any(a.type == "ASSERT_API_STATUS" and a.target == "/payment" and a.expected == "401" for a in scenario.assertions)
        healthy = (state.get("lastApiStatus") == 200 and state.get("paymentStatus") == "SUCCESS" and
                   not events.intersection({"TOKEN_EXPIRED", "PAYMENT_FAILED"}))
        failed = state.get("lastApiStatus") == 401 and state.get("paymentStatus") == "FAILED"
    if measured and required <= events and (healthy if verifying else failed):
        return "FIX_VERIFIED" if verifying else "BUG_REPRODUCED"
    return unsuccessful
