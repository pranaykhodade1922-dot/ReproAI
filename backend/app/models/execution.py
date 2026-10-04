from enum import Enum
from typing import List, Optional, Dict, Any
from pydantic import BaseModel, Field
from typing import Literal
from app.models.scenario import ExecutionPurpose

class ExecutionStatus(str, Enum):
    PENDING = "PENDING"
    RUNNING = "RUNNING"
    PASSED = "PASSED"
    FAILED = "FAILED"
    ERROR = "ERROR"
    CANCELLED = "CANCELLED"

class StepStatus(str, Enum):
    PENDING = "PENDING"
    RUNNING = "RUNNING"
    PASSED = "PASSED"
    FAILED = "FAILED"
    SKIPPED = "SKIPPED"
    UNSUPPORTED = "UNSUPPORTED"

class ExecutionStepResult(BaseModel):
    index: int
    action: str
    target: Optional[str] = None
    status: StepStatus
    started_at: str
    finished_at: str
    duration_ms: float
    message: str = ""
    evidence: Dict[str, Any] = Field(default_factory=dict)

class ExecutionResult(BaseModel):
    execution_id: str
    scenario_id: str
    scenario_name: str
    device_serial: str = "mock-device"
    status: ExecutionStatus
    execution_mode: Literal["MOCK", "ADB"] = "MOCK"
    execution_purpose: ExecutionPurpose = ExecutionPurpose.REPRODUCE
    product_outcome: Literal["UNCONFIRMED", "BUG_REPRODUCED", "FIX_VERIFIED", "VERIFICATION_FAILED"] = "UNCONFIRMED"
    started_at: str
    finished_at: str = ""
    duration_ms: float = 0.0
    steps: List[ExecutionStepResult] = Field(default_factory=list)
    assertions_passed: int = 0
    assertions_failed: int = 0
    failure_reason: Optional[str] = None
    network_strategy: Optional[Literal["REAL_NETWORK", "DEMOSHOP_DEMO_HOOK", "ANDROID_CONFIGURATION"]] = None
    observed_state: Dict[str, Any] = Field(default_factory=dict)
    setup_evidence: Dict[str, Any] = Field(default_factory=dict)
