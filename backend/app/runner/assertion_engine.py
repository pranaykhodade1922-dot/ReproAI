from app.models.execution import StepStatus
class AssertionEngine:
    async def evaluate_assertion(self, assertion, device_serial=None, is_mock=False):
        if assertion.type not in ("ASSERT_VISIBLE", "ASSERT_TEXT", "ASSERT_API_STATUS", "ASSERT_EVENT"):
            return StepStatus.UNSUPPORTED, "Assertion type is not allow-listed.", {"verified": False}
        if is_mock:
            return StepStatus.PASSED, "Simulated assertion; no device evidence collected.", {"mock": True, "expected": assertion.expected}
        return StepStatus.UNSUPPORTED, "Real assertion evidence adapter is not implemented.", {"expected": assertion.expected, "verified": False}
