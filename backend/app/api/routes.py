from fastapi import APIRouter, HTTPException, status
from typing import List
from app.models.scenario import TestScenario
from app.models.execution import ExecutionResult
from app.models.device import DeviceInfo
from app.services.execution_service import ExecutionService
from app.adb.device_manager import DeviceManager
from app.config import settings
import asyncio
from app.services.preflight import DemoPreflight

router = APIRouter()
execution_service = ExecutionService()
device_manager = DeviceManager()
preflight = DemoPreflight()

@router.post("/preflight")
async def demo_preflight(scenario: TestScenario | None = None):
    return await preflight.check(scenario)

@router.post("/demo/reset")
async def reset_demo():
    if not settings.ENABLE_DEMO_CONTROLS:
        raise HTTPException(403, "Demo controls disabled. Enable ENABLE_DEMO_CONTROLS only on the trusted demo LAN.")
    if settings.RUNNER_MODE != "adb":
        raise HTTPException(409, "Reset Demo requires ADB mode; no mode is switched automatically.")
    if execution_service.has_active_executions():
        raise HTTPException(409, "Cancel or finish active executions before resetting the demo.")
    readiness = await preflight.check()
    if not readiness["ready"]:
        raise HTTPException(409, "; ".join(readiness["issues"]))
    serial = readiness["deviceSerial"]
    try:
        raw = await asyncio.to_thread(preflight.adb.shell, serial,
            ["content", "call", "--uri", "content://com.pranay.demoshop.automation", "--method", "RESET_PRESENTATION"])
        state=preflight.decode_state(raw)
        checkout=state.get('checkout', {})
        if not (checkout.get('selectedPaymentMethod') is None and checkout.get('isValid') is False and
                checkout.get('lastStateEvent') is None and checkout.get('preserveAfterRotation') is False and
                checkout.get('orientationBefore') is None and checkout.get('orientationAfter') is None and
                state.get("source") == "DEMOSHOP" and state.get("useFixedAuth") is False and
                state.get("simulateNetworkTransition") is False and state.get("cartCount") == 0 and
                state.get("paymentStatus") == "IDLE" and state.get("lastApiStatus") is None and state.get("events") == []):
            raise ValueError("Reset state not confirmed")
    except Exception:
        raise HTTPException(409, "Reset hook unavailable. Install the latest DemoShop debug APK.")
    return {"status": "reset", "authentication": "BUGGY", "checkoutRotation": "BUGGY", "historyPreserved": True}

@router.get("/health")
async def health_check():
    return {
        "status": "ok",
        "service": "repro-runner",
        "runnerMode": settings.RUNNER_MODE
    }

@router.get("/devices", response_model=List[DeviceInfo])
async def list_devices():
    return await asyncio.to_thread(device_manager.get_connected_devices)

@router.post("/execute", response_model=ExecutionResult, status_code=status.HTTP_200_OK)
async def execute_scenario(scenario: TestScenario):
    try:
        return await execution_service.execute_scenario(scenario)
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Execution could not be started. Check runner logs and device connection."
        )

@router.get("/executions/{execution_id}", response_model=ExecutionResult)
async def get_execution(execution_id: str):
    res = execution_service.get_execution(execution_id)
    if not res:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail=f"Execution {execution_id} not found.")
    return res

@router.post("/executions/{execution_id}/cancel", response_model=ExecutionResult)
async def cancel_execution(execution_id: str):
    res = execution_service.cancel_execution(execution_id)
    if not res:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail=f"Execution {execution_id} not found.")
    return res
