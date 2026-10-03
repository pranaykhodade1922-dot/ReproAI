from fastapi.testclient import TestClient
from main import app

client = TestClient(app)

def test_execute_endpoint():
    payload = {
        "id": "scenario-payment-network",
        "name": "Payment network transition failure",
        "description": "Reproduce payment failure during connectivity transition",
        "preconditions": [
            {
                "type": "NETWORK",
                "value": "WIFI"
            }
        ],
        "steps": [
            {
                "action": "OPEN_SCREEN",
                "target": "Checkout"
            },
            {
                "action": "TAP",
                "target": "PAY"
            },
            {
                "action": "CHANGE_NETWORK",
                "value": "CELLULAR"
            },
            {
                "action": "WAIT",
                "value": "1000"
            }
        ],
        "assertions": [
            {
                "type": "ASSERT_EVENT",
                "target": "TOKEN_EXPIRED",
                "expected": "True"
            },
            {
                "type": "ASSERT_API_STATUS",
                "target": "/payment",
                "expected": "401"
            }
        ]
    }

    import time
    with TestClient(app) as running_client:
        response = running_client.post("/execute", json=payload)
        assert response.status_code == 200
        execution_id = response.json()["execution_id"]
        for _ in range(40):
            response = running_client.get(f"/executions/{execution_id}")
            if response.json()["status"] not in ("PENDING", "RUNNING"):
                break
            time.sleep(0.1)
    assert response.status_code == 200
    data = response.json()
    assert data["status"] == "PASSED"
    assert data["scenario_id"] == "scenario-payment-network"
    assert len(data["steps"]) == 6
