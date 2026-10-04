from enum import Enum
from typing import List, Optional
from pydantic import BaseModel, Field, field_validator

class TestAction(str, Enum):
    OPEN_SCREEN = "OPEN_SCREEN"
    TAP = "TAP"
    WAIT = "WAIT"
    CHANGE_NETWORK = "CHANGE_NETWORK"
    BACKGROUND_APP = "BACKGROUND_APP"
    FOREGROUND_APP = "FOREGROUND_APP"
    ROTATE_DEVICE = "ROTATE_DEVICE"
    ASSERT_VISIBLE = "ASSERT_VISIBLE"
    ASSERT_TEXT = "ASSERT_TEXT"
    ASSERT_API_STATUS = "ASSERT_API_STATUS"
    ASSERT_EVENT = "ASSERT_EVENT"
    CUSTOM = "CUSTOM"

class TestPrecondition(BaseModel):
    type: str
    value: str

class TestActionItem(BaseModel):
    action: TestAction
    target: Optional[str] = None
    value: Optional[str] = None
    description: Optional[str] = None

class TestAssertion(BaseModel):
    type: str
    target: Optional[str] = None
    expected: str

class ExecutionPurpose(str, Enum):
    REPRODUCE = "REPRODUCE"
    VERIFY_FIX = "VERIFY_FIX"

class TestScenario(BaseModel):
    id: str
    name: str
    description: str = ""
    preconditions: List[TestPrecondition] = Field(default_factory=list)
    steps: List[TestActionItem] = Field(default_factory=list)
    assertions: List[TestAssertion] = Field(default_factory=list)
    verification_assertions: List[TestAssertion] = Field(default_factory=list)
    execution_purpose: ExecutionPurpose = ExecutionPurpose.REPRODUCE

    @field_validator("verification_assertions", "execution_purpose", mode="before")
    @classmethod
    def legacy_optional_fields(cls, value, info):
        # Gson serializes absent fields on older saved incidents as null.
        if value is None:
            return [] if info.field_name == "verification_assertions" else ExecutionPurpose.REPRODUCE
        return value

    def execution_assertions(self) -> List[TestAssertion]:
        if self.execution_purpose == ExecutionPurpose.REPRODUCE:
            return self.assertions
        if not self.verification_assertions:
            raise ValueError("Fix verification requires an explicit healthy assertion profile.")
        if any(a.expected.lower() == "false" for a in self.verification_assertions):
            raise ValueError("Fix verification requires positive healthy evidence, not absence of failure.")
        positive = {(a.type, a.target, a.expected.lower()) for a in self.verification_assertions}
        if any(s.action == TestAction.ROTATE_DEVICE for s in self.steps):
            required = {("ASSERT_EVENT", e, "true") for e in
                        ("CHECKOUT_STATE_RESTORED", "CHECKOUT_VALID", "PAYMENT_AVAILABLE")}
        elif any(a.type == "ASSERT_API_STATUS" and a.target == "/payment" and a.expected == "401" for a in self.assertions):
            required = {("ASSERT_EVENT", "TOKEN_REFRESHED", "true"),
                        ("ASSERT_API_STATUS", "/payment", "200"),
                        ("ASSERT_EVENT", "PAYMENT_SUCCESS", "true")}
        else:
            required = set()
        if not required.issubset(positive):
            raise ValueError("Fix verification requires the complete positive healthy evidence profile.")
        return self.verification_assertions
