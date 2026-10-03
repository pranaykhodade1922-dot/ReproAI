# Repro Runner API contract

Compared with app/src/main/java/com/pranay/reproai/ai/AnalysisModels.kt: every request field, optional field, nested array and all 12 action enum strings match. Android data/model/TestScenario is a presentation model and MUST NOT be submitted. Serialize AnalysisResult.testScenario directly. Gson retains enum names and omits nullable optional fields; backend accepts omitted fields and null.

POST /execute returns HTTP 200 with an accepted PENDING/RUNNING snapshot, not guaranteed completion. HTTP 422 means invalid scenario. Follow GET /executions/{id} or WS /ws/executions/{id} until PASSED, FAILED, ERROR or CANCELLED. IDs are opaque. Cancellation returns the latest snapshot. HTTP 404 means unknown execution. Each WebSocket message is a complete ExecutionResult; no echo envelope. Unknown WebSocket IDs close with code 1008.

TestStep is the existing TestActionItem model; no duplicate request model is introduced. Preconditions are transported but are not established/validated by the runner. Configure them manually in ADB mode.

## TestScenario

Exact example:
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
      "target": "Checkout",
      "value": null,
      "description": null
    },
    {
      "action": "CHANGE_NETWORK",
      "target": null,
      "value": "CELLULAR",
      "description": null
    },
    {
      "action": "TAP",
      "target": "PAY",
      "value": null,
      "description": null
    },
    {
      "action": "WAIT",
      "target": null,
      "value": "2500",
      "description": null
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

JSON schema:
```json
{
  "$defs": {
    "TestAction": {
      "enum": [
        "OPEN_SCREEN",
        "TAP",
        "WAIT",
        "CHANGE_NETWORK",
        "BACKGROUND_APP",
        "FOREGROUND_APP",
        "ROTATE_DEVICE",
        "ASSERT_VISIBLE",
        "ASSERT_TEXT",
        "ASSERT_API_STATUS",
        "ASSERT_EVENT",
        "CUSTOM"
      ],
      "title": "TestAction",
      "type": "string"
    },
    "TestActionItem": {
      "properties": {
        "action": {
          "$ref": "#/$defs/TestAction"
        },
        "target": {
          "anyOf": [
            {
              "type": "string"
            },
            {
              "type": "null"
            }
          ],
          "default": null,
          "title": "Target"
        },
        "value": {
          "anyOf": [
            {
              "type": "string"
            },
            {
              "type": "null"
            }
          ],
          "default": null,
          "title": "Value"
        },
        "description": {
          "anyOf": [
            {
              "type": "string"
            },
            {
              "type": "null"
            }
          ],
          "default": null,
          "title": "Description"
        }
      },
      "required": [
        "action"
      ],
      "title": "TestActionItem",
      "type": "object"
    },
    "TestAssertion": {
      "properties": {
        "type": {
          "title": "Type",
          "type": "string"
        },
        "target": {
          "anyOf": [
            {
              "type": "string"
            },
            {
              "type": "null"
            }
          ],
          "default": null,
          "title": "Target"
        },
        "expected": {
          "title": "Expected",
          "type": "string"
        }
      },
      "required": [
        "type",
        "expected"
      ],
      "title": "TestAssertion",
      "type": "object"
    },
    "TestPrecondition": {
      "properties": {
        "type": {
          "title": "Type",
          "type": "string"
        },
        "value": {
          "title": "Value",
          "type": "string"
        }
      },
      "required": [
        "type",
        "value"
      ],
      "title": "TestPrecondition",
      "type": "object"
    }
  },
  "properties": {
    "id": {
      "title": "Id",
      "type": "string"
    },
    "name": {
      "title": "Name",
      "type": "string"
    },
    "description": {
      "default": "",
      "title": "Description",
      "type": "string"
    },
    "preconditions": {
      "items": {
        "$ref": "#/$defs/TestPrecondition"
      },
      "title": "Preconditions",
      "type": "array"
    },
    "steps": {
      "items": {
        "$ref": "#/$defs/TestActionItem"
      },
      "title": "Steps",
      "type": "array"
    },
    "assertions": {
      "items": {
        "$ref": "#/$defs/TestAssertion"
      },
      "title": "Assertions",
      "type": "array"
    }
  },
  "required": [
    "id",
    "name"
  ],
  "title": "TestScenario",
  "type": "object"
}
```

## TestPrecondition

Exact example:
```json
{
  "type": "NETWORK",
  "value": "WIFI"
}
```

JSON schema:
```json
{
  "properties": {
    "type": {
      "title": "Type",
      "type": "string"
    },
    "value": {
      "title": "Value",
      "type": "string"
    }
  },
  "required": [
    "type",
    "value"
  ],
  "title": "TestPrecondition",
  "type": "object"
}
```

## TestStep (TestActionItem)

Exact example:
```json
{
  "action": "OPEN_SCREEN",
  "target": "Checkout",
  "value": null,
  "description": null
}
```

JSON schema:
```json
{
  "$defs": {
    "TestAction": {
      "enum": [
        "OPEN_SCREEN",
        "TAP",
        "WAIT",
        "CHANGE_NETWORK",
        "BACKGROUND_APP",
        "FOREGROUND_APP",
        "ROTATE_DEVICE",
        "ASSERT_VISIBLE",
        "ASSERT_TEXT",
        "ASSERT_API_STATUS",
        "ASSERT_EVENT",
        "CUSTOM"
      ],
      "title": "TestAction",
      "type": "string"
    }
  },
  "properties": {
    "action": {
      "$ref": "#/$defs/TestAction"
    },
    "target": {
      "anyOf": [
        {
          "type": "string"
        },
        {
          "type": "null"
        }
      ],
      "default": null,
      "title": "Target"
    },
    "value": {
      "anyOf": [
        {
          "type": "string"
        },
        {
          "type": "null"
        }
      ],
      "default": null,
      "title": "Value"
    },
    "description": {
      "anyOf": [
        {
          "type": "string"
        },
        {
          "type": "null"
        }
      ],
      "default": null,
      "title": "Description"
    }
  },
  "required": [
    "action"
  ],
  "title": "TestActionItem",
  "type": "object"
}
```

## TestAssertion

Exact example:
```json
{
  "type": "ASSERT_EVENT",
  "target": "TOKEN_EXPIRED",
  "expected": "True"
}
```

JSON schema:
```json
{
  "properties": {
    "type": {
      "title": "Type",
      "type": "string"
    },
    "target": {
      "anyOf": [
        {
          "type": "string"
        },
        {
          "type": "null"
        }
      ],
      "default": null,
      "title": "Target"
    },
    "expected": {
      "title": "Expected",
      "type": "string"
    }
  },
  "required": [
    "type",
    "expected"
  ],
  "title": "TestAssertion",
  "type": "object"
}
```

## ExecutionResult

Exact example:
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

JSON schema:
```json
{
  "$defs": {
    "ExecutionStatus": {
      "enum": [
        "PENDING",
        "RUNNING",
        "PASSED",
        "FAILED",
        "ERROR",
        "CANCELLED"
      ],
      "title": "ExecutionStatus",
      "type": "string"
    },
    "ExecutionStepResult": {
      "properties": {
        "index": {
          "title": "Index",
          "type": "integer"
        },
        "action": {
          "title": "Action",
          "type": "string"
        },
        "target": {
          "anyOf": [
            {
              "type": "string"
            },
            {
              "type": "null"
            }
          ],
          "default": null,
          "title": "Target"
        },
        "status": {
          "$ref": "#/$defs/StepStatus"
        },
        "started_at": {
          "title": "Started At",
          "type": "string"
        },
        "finished_at": {
          "title": "Finished At",
          "type": "string"
        },
        "duration_ms": {
          "title": "Duration Ms",
          "type": "number"
        },
        "message": {
          "default": "",
          "title": "Message",
          "type": "string"
        },
        "evidence": {
          "additionalProperties": true,
          "title": "Evidence",
          "type": "object"
        }
      },
      "required": [
        "index",
        "action",
        "status",
        "started_at",
        "finished_at",
        "duration_ms"
      ],
      "title": "ExecutionStepResult",
      "type": "object"
    },
    "StepStatus": {
      "enum": [
        "PENDING",
        "RUNNING",
        "PASSED",
        "FAILED",
        "SKIPPED",
        "UNSUPPORTED"
      ],
      "title": "StepStatus",
      "type": "string"
    }
  },
  "properties": {
    "execution_id": {
      "title": "Execution Id",
      "type": "string"
    },
    "scenario_id": {
      "title": "Scenario Id",
      "type": "string"
    },
    "scenario_name": {
      "title": "Scenario Name",
      "type": "string"
    },
    "device_serial": {
      "default": "mock-device",
      "title": "Device Serial",
      "type": "string"
    },
    "status": {
      "$ref": "#/$defs/ExecutionStatus"
    },
    "execution_mode": {
      "default": "MOCK",
      "enum": [
        "MOCK",
        "ADB"
      ],
      "title": "Execution Mode",
      "type": "string"
    },
    "started_at": {
      "title": "Started At",
      "type": "string"
    },
    "finished_at": {
      "default": "",
      "title": "Finished At",
      "type": "string"
    },
    "duration_ms": {
      "default": 0.0,
      "title": "Duration Ms",
      "type": "number"
    },
    "steps": {
      "items": {
        "$ref": "#/$defs/ExecutionStepResult"
      },
      "title": "Steps",
      "type": "array"
    },
    "assertions_passed": {
      "default": 0,
      "title": "Assertions Passed",
      "type": "integer"
    },
    "assertions_failed": {
      "default": 0,
      "title": "Assertions Failed",
      "type": "integer"
    },
    "failure_reason": {
      "anyOf": [
        {
          "type": "string"
        },
        {
          "type": "null"
        }
      ],
      "default": null,
      "title": "Failure Reason"
    },
    "network_strategy": {
      "anyOf": [
        {
          "enum": [
            "REAL_NETWORK",
            "DEMOSHOP_DEMO_HOOK"
          ],
          "type": "string"
        },
        {
          "type": "null"
        }
      ],
      "default": null,
      "title": "Network Strategy"
    },
    "observed_state": {
      "additionalProperties": true,
      "title": "Observed State",
      "type": "object"
    },
    "setup_evidence": {
      "additionalProperties": true,
      "title": "Setup Evidence",
      "type": "object"
    }
  },
  "required": [
    "execution_id",
    "scenario_id",
    "scenario_name",
    "status",
    "started_at"
  ],
  "title": "ExecutionResult",
  "type": "object"
}
```

## ExecutionStepResult

Exact example:
```json
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
}
```

JSON schema:
```json
{
  "$defs": {
    "StepStatus": {
      "enum": [
        "PENDING",
        "RUNNING",
        "PASSED",
        "FAILED",
        "SKIPPED",
        "UNSUPPORTED"
      ],
      "title": "StepStatus",
      "type": "string"
    }
  },
  "properties": {
    "index": {
      "title": "Index",
      "type": "integer"
    },
    "action": {
      "title": "Action",
      "type": "string"
    },
    "target": {
      "anyOf": [
        {
          "type": "string"
        },
        {
          "type": "null"
        }
      ],
      "default": null,
      "title": "Target"
    },
    "status": {
      "$ref": "#/$defs/StepStatus"
    },
    "started_at": {
      "title": "Started At",
      "type": "string"
    },
    "finished_at": {
      "title": "Finished At",
      "type": "string"
    },
    "duration_ms": {
      "title": "Duration Ms",
      "type": "number"
    },
    "message": {
      "default": "",
      "title": "Message",
      "type": "string"
    },
    "evidence": {
      "additionalProperties": true,
      "title": "Evidence",
      "type": "object"
    }
  },
  "required": [
    "index",
    "action",
    "status",
    "started_at",
    "finished_at",
    "duration_ms"
  ],
  "title": "ExecutionStepResult",
  "type": "object"
}
```

## Enum values

```json
{
  "TestAction": [
    "OPEN_SCREEN",
    "TAP",
    "WAIT",
    "CHANGE_NETWORK",
    "BACKGROUND_APP",
    "FOREGROUND_APP",
    "ROTATE_DEVICE",
    "ASSERT_VISIBLE",
    "ASSERT_TEXT",
    "ASSERT_API_STATUS",
    "ASSERT_EVENT",
    "CUSTOM"
  ],
  "ExecutionStatus": [
    "PENDING",
    "RUNNING",
    "PASSED",
    "FAILED",
    "ERROR",
    "CANCELLED"
  ],
  "StepStatus": [
    "PENDING",
    "RUNNING",
    "PASSED",
    "FAILED",
    "SKIPPED",
    "UNSUPPORTED"
  ],
  "execution_mode": [
    "MOCK",
    "ADB"
  ]
}
```

## Health and devices

GET /health:
```json
{
  "status": "ok",
  "service": "repro-runner",
  "runnerMode": "mock"
}
```

GET /devices returns an array; only authorized online devices are listed:
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

## Submission snapshot

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

## Errors

Validation response structure (messages vary by Pydantic version):
```json
{
  "detail": [
    {
      "type": "enum",
      "loc": [
        "body",
        "steps",
        0,
        "action"
      ],
      "msg": "Input should be an allow-listed TestAction",
      "input": "SHELL"
    }
  ]
}
```

GET missing ID:
```json
{
  "detail": "Execution missing not found."
}
```

## Evidence and product meaning

MOCK actions/assertions are simulated and never prove reproduction. ADB DemoShop assertions use execution-scoped debug state emitted by the on-device fake payment service. The result adds network_strategy, setup_evidence and observed_state. See [DemoShop automation](demoshop-automation.md) for exact physical-device evidence.

PASSED means the submitted assertions succeeded. Android REPRODUCE requires the full measured TOKEN_EXPIRED + HTTP401 + PAYMENT_FAILED signature. VERIFY_FIX reruns the same failure scenario, whose assertions correctly FAIL after the fix; the UI requires TOKEN_REFRESHED + HTTP200 + PAYMENT_SUCCESS with no failure events. Purpose remains Android context, never a request field.

Known DemoShop taps invoke the shared UI controllers through an allow-listed debug bridge, without coordinate taps. Unknown targets and unsupported assertion kinds return UNSUPPORTED. REAL_NETWORK is explicitly unsupported; DETERMINISTIC_DEMO reports DEMOSHOP_DEMO_HOOK and never toggles radios. Preconditions remain manual.
