"""Recreate contract examples against a running local MOCK runner."""
import json
import os
import time
from pathlib import Path
import httpx
from app.models.scenario import TestScenario, TestPrecondition, TestActionItem, TestAssertion, TestAction
from app.models.execution import ExecutionResult, ExecutionStepResult, ExecutionStatus, StepStatus

def block(value):
    return '\n```json\n' + json.dumps(value, indent=2) + '\n```\n'

out = Path('docs')
out.mkdir(exist_ok=True)
scenario = json.loads(Path('samples/payment_network_failure.json').read_text())
with httpx.Client(base_url=os.environ.get('CONTRACT_BASE_URL', 'http://127.0.0.1:8000'), timeout=30) as client:
    health = client.get('/health').json()
    assert health['runnerMode'] == 'mock', 'Start a mock runner first'
    devices = client.get('/devices').json()
    response = client.post('/execute', json=scenario)
    response.raise_for_status()
    result = response.json()
    submitted = dict(result)
    for _ in range(100):
        if result['status'] not in ['PENDING', 'RUNNING']:
            break
        time.sleep(.1)
        result = client.get('/executions/' + result['execution_id']).json()
    assert result['status'] == 'PASSED', result
    (out / 'mock-result.json').write_text(json.dumps(result, indent=2))
    print(json.dumps({'health': health, 'mock_status': result['status'], 'steps': len(result['steps']), 'execution_mode': result['execution_mode']}))

text = '''# Repro Runner API contract

Compared with app/src/main/java/com/pranay/reproai/ai/AnalysisModels.kt: every request field, optional field, nested array and all 12 action enum strings match. Android data/model/TestScenario is a presentation model and MUST NOT be submitted. Serialize AnalysisResult.testScenario directly. Gson retains enum names and omits nullable optional fields; backend accepts omitted fields and null.

POST /execute returns HTTP 200 with an accepted PENDING/RUNNING snapshot, not guaranteed completion. HTTP 422 means invalid scenario. Follow GET /executions/{id} or WS /ws/executions/{id} until PASSED, FAILED, ERROR or CANCELLED. IDs are opaque. Cancellation returns the latest snapshot. HTTP 404 means unknown execution. Each WebSocket message is a complete ExecutionResult; no echo envelope. Unknown WebSocket IDs close with code 1008.

TestStep is the existing TestActionItem model; no duplicate request model is introduced. Preconditions are transported but are not established/validated by the runner. Configure them manually in ADB mode.
'''
for name, model, example in [
    ('TestScenario', TestScenario, scenario),
    ('TestPrecondition', TestPrecondition, scenario['preconditions'][0]),
    ('TestStep (TestActionItem)', TestActionItem, scenario['steps'][0]),
    ('TestAssertion', TestAssertion, scenario['assertions'][0]),
    ('ExecutionResult', ExecutionResult, result),
    ('ExecutionStepResult', ExecutionStepResult, result['steps'][0]),
]:
    text += '\n## ' + name + '\n\nExact example:' + block(model.model_validate(example).model_dump(mode='json'))
    text += '\nJSON schema:' + block(model.model_json_schema())
text += '\n## Enum values\n' + block({'TestAction': [x.value for x in TestAction], 'ExecutionStatus': [x.value for x in ExecutionStatus], 'StepStatus': [x.value for x in StepStatus], 'execution_mode': ['MOCK', 'ADB']})
text += '\n## Health and devices\n\nGET /health:' + block(health)
text += '\nGET /devices returns an array; only authorized online devices are listed:' + block(devices)
text += '\n## Submission snapshot\n' + block(submitted)
text += '\n## Errors\n\nValidation response structure (messages vary by Pydantic version):' + block({'detail': [{'type': 'enum', 'loc': ['body', 'steps', 0, 'action'], 'msg': 'Input should be an allow-listed TestAction', 'input': 'SHELL'}]})
text += '\nGET missing ID:' + block({'detail': 'Execution missing not found.'})
text += '''
## Evidence and product meaning

MOCK actions/assertions are simulated and never prove reproduction. ADB DemoShop assertions use execution-scoped debug state emitted by the on-device fake payment service. The result adds network_strategy, setup_evidence and observed_state. See [DemoShop automation](demoshop-automation.md) for exact physical-device evidence.

PASSED means the selected assertion profile succeeded. REPRODUCE requires the measured TOKEN_EXPIRED + HTTP401 + PAYMENT_FAILED signature. VERIFY_FIX reruns the original scenario ID, setup and actions with verification_assertions requiring TOKEN_REFRESHED + HTTP200 + PAYMENT_SUCCESS. Both successful runs are PASSED. The runner echoes execution_purpose and supplies the scoped product_outcome; the UI never upgrades a FAILED run to FIX VERIFIED. Verification requires an explicit complete positive profile; omitted execution_purpose defaults to REPRODUCE for legacy requests.

Known DemoShop taps invoke the shared UI controllers through an allow-listed debug bridge, without coordinate taps. Unknown targets and unsupported assertion kinds return UNSUPPORTED. REAL_NETWORK is explicitly unsupported; DETERMINISTIC_DEMO reports DEMOSHOP_DEMO_HOOK and never toggles radios. Preconditions remain manual.
'''
(out / 'api-contract.md').write_text(text, encoding='utf-8')

integration = '''# Android runner integration

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
'''
integration += '\nHealth response:' + block(health) + '\nDevices response:' + block(devices)
integration += '\nPOST request:' + block(scenario) + '\nPOST initial response:' + block(submitted)
integration += '\nGET terminal / WebSocket terminal response:' + block(result)
integration += '\nCancellation response (same result fields; status changes):' + block({**submitted, 'status': 'CANCELLED', 'failure_reason': 'Execution cancelled by user.'})
integration += '''
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
'''
(out / 'android-integration.md').write_text(integration, encoding='utf-8')
