# Android runner integration

Base URL example: http://192.168.x.x:8000/ (Retrofit requires trailing slash). Enter host only and port separately in ReproAI Home. TEST CONNECTION saves settings locally and calls real GET /health. Connected means this request succeeded; each run checks again.

Use debug APKs for LAN HTTP: INTERNET permission is present, and cleartext traffic is enabled only in the debug manifest. Release builds require HTTPS configuration.

| Endpoint | Body / response |
|---|---|
| GET /health | RunnerHealth |
| GET /devices | array of RunnerDevice |
| POST /execute | TestScenario â†’ ExecutionResult snapshot |
| GET /executions/{id} | latest ExecutionResult |
| POST /executions/{id}/cancel | no body â†’ ExecutionResult |
| WS /ws/executions/{id} | complete ExecutionResult every 500 ms, closes after terminal result |

Exact examples for all nested models and enums are in [api-contract.md](api-contract.md).

Health response:
```json
{
  "status": "ok",
  "service": "repro-runner",
  "runnerMode": "mock"
}
```

Devices response:
```json
[
  {
    "serial": "DEVICE_SERIAL",
    "state": "device",
    "model": "CPH2477",
    "manufacturer": "OPPO",
    "android_version": "12"
  },
  {
    "serial": "adb-W8JF59AUDEROVO7P-saVYnd._adb-tls-connect._tcp",
    "state": "device",
    "model": "CPH2477",
    "manufacturer": "OPPO",
    "android_version": "12"
  }
]
```

POST request:
```json
{
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
      "action": "CHANGE_NETWORK",
      "value": "CELLULAR"
    },
    {
      "action": "TAP",
      "target": "PAY"
    },
    {
      "action": "WAIT",
      "value": "2500"
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
    },
    {
      "type": "ASSERT_EVENT",
      "target": "PAYMENT_FAILED",
      "expected": "True"
    }
  ]
}
```

POST initial response:
```json
{
  "execution_id": "exec-7d9d3bf089474e96bc13005af64fdeb4",
  "scenario_id": "scenario-payment-network",
  "scenario_name": "Payment network transition failure",
  "device_serial": "mock-device",
  "status": "PENDING",
  "execution_mode": "MOCK",
  "started_at": "2026-10-02T11:59:42.877017+00:00",
  "finished_at": "",
  "duration_ms": 0.0,
  "steps": [
    {
      "index": 0,
      "action": "OPEN_SCREEN",
      "target": "Checkout",
      "status": "PENDING",
      "started_at": "",
      "finished_at": "",
      "duration_ms": 0.0,
      "message": "",
      "evidence": {}
    },
    {
      "index": 1,
      "action": "CHANGE_NETWORK",
      "target": null,
      "status": "PENDING",
      "started_at": "",
      "finished_at": "",
      "duration_ms": 0.0,
      "message": "",
      "evidence": {}
    },
    {
      "index": 2,
      "action": "TAP",
      "target": "PAY",
      "status": "PENDING",
      "started_at": "",
      "finished_at": "",
      "duration_ms": 0.0,
      "message": "",
      "evidence": {}
    },
    {
      "index": 3,
      "action": "WAIT",
      "target": null,
      "status": "PENDING",
      "started_at": "",
      "finished_at": "",
      "duration_ms": 0.0,
      "message": "",
      "evidence": {}
    },
    {
      "index": 4,
      "action": "ASSERT_EVENT",
      "target": "TOKEN_EXPIRED",
      "status": "PENDING",
      "started_at": "",
      "finished_at": "",
      "duration_ms": 0.0,
      "message": "",
      "evidence": {}
    },
    {
      "index": 5,
      "action": "ASSERT_API_STATUS",
      "target": "/payment",
      "status": "PENDING",
      "started_at": "",
      "finished_at": "",
      "duration_ms": 0.0,
      "message": "",
      "evidence": {}
    },
    {
      "index": 6,
      "action": "ASSERT_EVENT",
      "target": "PAYMENT_FAILED",
      "status": "PENDING",
      "started_at": "",
      "finished_at": "",
      "duration_ms": 0.0,
      "message": "",
      "evidence": {}
    }
  ],
  "assertions_passed": 0,
  "assertions_failed": 0,
  "failure_reason": null,
  "network_strategy": null,
  "observed_state": {},
  "setup_evidence": {}
}
```

GET terminal / WebSocket terminal response:
```json
{
  "execution_id": "exec-7d9d3bf089474e96bc13005af64fdeb4",
  "scenario_id": "scenario-payment-network",
  "scenario_name": "Payment network transition failure",
  "device_serial": "mock-device",
  "status": "PASSED",
  "execution_mode": "MOCK",
  "started_at": "2026-10-02T11:59:42.877973+00:00",
  "finished_at": "2026-10-02T11:59:44.098136+00:00",
  "duration_ms": 1220.16,
  "steps": [
    {
      "index": 0,
      "action": "OPEN_SCREEN",
      "target": "Checkout",
      "status": "PASSED",
      "started_at": "2026-10-02T11:59:42.877973+00:00",
      "finished_at": "2026-10-02T11:59:43.167277+00:00",
      "duration_ms": 289.3,
      "message": "Simulated action; no device evidence.",
      "evidence": {
        "mock": true
      }
    },
    {
      "index": 1,
      "action": "CHANGE_NETWORK",
      "target": null,
      "status": "PASSED",
      "started_at": "2026-10-02T11:59:43.167277+00:00",
      "finished_at": "2026-10-02T11:59:43.476431+00:00",
      "duration_ms": 309.15,
      "message": "Simulated action; no device evidence.",
      "evidence": {
        "mock": true
      }
    },
    {
      "index": 2,
      "action": "TAP",
      "target": "PAY",
      "status": "PASSED",
      "started_at": "2026-10-02T11:59:43.476431+00:00",
      "finished_at": "2026-10-02T11:59:43.787430+00:00",
      "duration_ms": 311.0,
      "message": "Simulated action; no device evidence.",
      "evidence": {
        "mock": true
      }
    },
    {
      "index": 3,
      "action": "WAIT",
      "target": null,
      "status": "PASSED",
      "started_at": "2026-10-02T11:59:43.787430+00:00",
      "finished_at": "2026-10-02T11:59:44.098136+00:00",
      "duration_ms": 310.71,
      "message": "Simulated action; no device evidence.",
      "evidence": {
        "mock": true
      }
    },
    {
      "index": 4,
      "action": "ASSERT_EVENT",
      "target": "TOKEN_EXPIRED",
      "status": "PASSED",
      "started_at": "2026-10-02T11:59:44.098136+00:00",
      "finished_at": "2026-10-02T11:59:44.098136+00:00",
      "duration_ms": 0.0,
      "message": "Simulated assertion; no device evidence collected.",
      "evidence": {
        "mock": true,
        "expected": "True"
      }
    },
    {
      "index": 5,
      "action": "ASSERT_API_STATUS",
      "target": "/payment",
      "status": "PASSED",
      "started_at": "2026-10-02T11:59:44.098136+00:00",
      "finished_at": "2026-10-02T11:59:44.098136+00:00",
      "duration_ms": 0.0,
      "message": "Simulated assertion; no device evidence collected.",
      "evidence": {
        "mock": true,
        "expected": "401"
      }
    },
    {
      "index": 6,
      "action": "ASSERT_EVENT",
      "target": "PAYMENT_FAILED",
      "status": "PASSED",
      "started_at": "2026-10-02T11:59:44.098136+00:00",
      "finished_at": "2026-10-02T11:59:44.098136+00:00",
      "duration_ms": 0.0,
      "message": "Simulated assertion; no device evidence collected.",
      "evidence": {
        "mock": true,
        "expected": "True"
      }
    }
  ],
  "assertions_passed": 3,
  "assertions_failed": 0,
  "failure_reason": null,
  "network_strategy": null,
  "observed_state": {},
  "setup_evidence": {}
}
```

Cancellation response (same result fields; status changes):
```json
{
  "execution_id": "exec-7d9d3bf089474e96bc13005af64fdeb4",
  "scenario_id": "scenario-payment-network",
  "scenario_name": "Payment network transition failure",
  "device_serial": "mock-device",
  "status": "CANCELLED",
  "execution_mode": "MOCK",
  "started_at": "2026-10-02T11:59:42.877017+00:00",
  "finished_at": "",
  "duration_ms": 0.0,
  "steps": [
    {
      "index": 0,
      "action": "OPEN_SCREEN",
      "target": "Checkout",
      "status": "PENDING",
      "started_at": "",
      "finished_at": "",
      "duration_ms": 0.0,
      "message": "",
      "evidence": {}
    },
    {
      "index": 1,
      "action": "CHANGE_NETWORK",
      "target": null,
      "status": "PENDING",
      "started_at": "",
      "finished_at": "",
      "duration_ms": 0.0,
      "message": "",
      "evidence": {}
    },
    {
      "index": 2,
      "action": "TAP",
      "target": "PAY",
      "status": "PENDING",
      "started_at": "",
      "finished_at": "",
      "duration_ms": 0.0,
      "message": "",
      "evidence": {}
    },
    {
      "index": 3,
      "action": "WAIT",
      "target": null,
      "status": "PENDING",
      "started_at": "",
      "finished_at": "",
      "duration_ms": 0.0,
      "message": "",
      "evidence": {}
    },
    {
      "index": 4,
      "action": "ASSERT_EVENT",
      "target": "TOKEN_EXPIRED",
      "status": "PENDING",
      "started_at": "",
      "finished_at": "",
      "duration_ms": 0.0,
      "message": "",
      "evidence": {}
    },
    {
      "index": 5,
      "action": "ASSERT_API_STATUS",
      "target": "/payment",
      "status": "PENDING",
      "started_at": "",
      "finished_at": "",
      "duration_ms": 0.0,
      "message": "",
      "evidence": {}
    },
    {
      "index": 6,
      "action": "ASSERT_EVENT",
      "target": "PAYMENT_FAILED",
      "status": "PENDING",
      "started_at": "",
      "finished_at": "",
      "duration_ms": 0.0,
      "message": "",
      "evidence": {}
    }
  ],
  "assertions_passed": 0,
  "assertions_failed": 0,
  "failure_reason": "Execution cancelled by user.",
  "network_strategy": null,
  "observed_state": {},
  "setup_evidence": {}
}
```

StateFlow phases: Idle â†’ Connecting â†’ Submitting â†’ Running â†’ Completed, or Error. WebSocket failure or 750 ms without a message falls back to GET polling every roughly 750 ms. Execution is bounded to 190 seconds; backend work is bounded to 180 seconds. Timeout attempts cancellation. Errors have plain UI messages. Configuration is frozen during a run. Latest execution, purpose, scenario and analysis are saved per session in Room; selecting that incident restores its result. Database 1â†’2 adds a table without deleting sessions/events.

## Manual journey

1. Start runner; select mock mode first.
2. Open ReproAI Home; enter laptop host and port; TEST CONNECTION.
3. Start debug session, open DemoShop, add to cart, proceed to checkout.
4. Enable DemoShop simulated network transition; press PAY. This is a fake payment service, not a bank/API transaction.
5. Return to ReproAI, capture issue, describe and analyze.
6. Generate reproduction and execute; inspect MOCK label, progress and result.
7. Verify Fix reruns the same scenario; it retains the mock label and cannot claim a verified fix.
8. Return Home and select saved incident to inspect persisted result.

## ADB automation

Set RUNNER_MODE=adb and DEFAULT_DEVICE_SERIAL to an exact authorized serial. Zero/multiple eligible devices produce ERROR, never mock fallback. Install both current debug APKs. Use NETWORK_STRATEGY=DETERMINISTIC_DEMO; REAL_NETWORK returns a controlled unsupported error without fallback.

Stable Compose tags, debug-only deep links, reset, the finite shell-only provider, and measured event/API assertions are implemented. See [DemoShop automation](demoshop-automation.md) for the buggy/fixed journey, reset semantics, exact evidence and limitations. Backend results remain in memory; Android persists the latest completed result.
