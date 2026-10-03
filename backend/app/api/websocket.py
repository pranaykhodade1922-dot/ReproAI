import asyncio
from fastapi import APIRouter, WebSocket, WebSocketDisconnect
from app.api.routes import execution_service
from app.models.execution import ExecutionStatus
router = APIRouter()
@router.websocket("/ws/executions/{execution_id}")
async def websocket_execution_stream(websocket: WebSocket, execution_id: str):
    await websocket.accept()
    try:
        while True:
            result = execution_service.get_execution(execution_id)
            if result is None:
                await websocket.close(code=1008, reason="Execution not found")
                return
            await websocket.send_json(result.model_dump(mode="json"))
            if result.status not in (ExecutionStatus.PENDING, ExecutionStatus.RUNNING):
                await websocket.close()
                return
            await asyncio.sleep(0.5)
    except WebSocketDisconnect:
        pass
