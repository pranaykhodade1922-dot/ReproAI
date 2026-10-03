from enum import Enum
from typing import List, Optional
from pydantic import BaseModel, Field

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

class TestScenario(BaseModel):
    id: str
    name: str
    description: str = ""
    preconditions: List[TestPrecondition] = Field(default_factory=list)
    steps: List[TestActionItem] = Field(default_factory=list)
    assertions: List[TestAssertion] = Field(default_factory=list)
