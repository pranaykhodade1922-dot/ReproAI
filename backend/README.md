# Repro Runner — Laptop-Side Test Execution Engine

Repro Runner is a Python 3.12 + FastAPI execution engine for ReproAI that executes structured `TestScenario`s against Android devices via controlled `AdbClient` wrappers.

## Setup & Running

```bash
cd backend
python -m venv .venv

# On Windows PowerShell
.venv\Scripts\Activate.ps1
# or alternative
.venv\Scripts\python.exe -m pip install -r requirements.txt

# Run FastAPI server
python -m uvicorn main:app --reload --host 127.0.0.1 --port 8000
```

## Running Tests

```bash
python -m pytest
```

## Endpoints

- `GET /health`
- `GET /devices`
- `POST /preflight` — optional TestScenario; readiness, issues, mode and hook checks
- `POST /demo/reset` — ADB-only, opt-in demo reset; preserves incident history
- `POST /execute`
- `GET /executions/{id}`
- `POST /executions/{id}/cancel`
- `WS /ws/executions/{id}`

## Current Android integration

Phase 8 adds device/APK/scenario/hook preflight, verified demo reset and a small scenario-file CLI. `ENABLE_DEMO_CONTROLS=false` by default; enable it only for the trusted demo environment. Reset is refused during active execution and confirms cleared BUGGY DemoShop state. From this directory, use `.venv/Scripts/python.exe -m app.cli validate path/to/test-scenario.json` or `run` with the same file; MOCK requires explicit `--allow-mock`. See [final hardening](../docs/phase8-hardening.md) and [Office Kit file workflow](../docs/office-kit-workflow.md).

See [the root setup guide](../README.md), [API contract](docs/api-contract.md), and [Android integration](docs/android-integration.md). /execute now returns an accepted snapshot immediately; poll or subscribe for completion. Health reports runnerMode. ADB mode never falls back to mock and unsupported evidence hooks cannot return a canned pass. validate_contract.py exercises the running mock server and recreates exact examples.

DemoShop debug automation is now implemented: [setup, bridge contract, buggy/fixed evidence, and limitations](docs/demoshop-automation.md). Use NETWORK_STRATEGY=DETERMINISTIC_DEMO with RUNNER_MODE=adb. Physical radio switching remains unsupported.

Checkout rotation uses the same typed scenario API with stable `SELECT_UPI` / `SELECT_CARD` targets and measured checkout event assertions. `ROTATE_DEVICE` resumes Checkout, controls the actual display through the window manager, verifies display orientation and Activity recreation, and restores the initial rotation setting. Its execution strategy is `ANDROID_CONFIGURATION`; rotation is never simulated in ADB mode. Presenter reset clears checkout state and both debug modes; execution reset preserves the selected implementation for same-scenario fix verification. See [the two-bug validation record](../docs/multi-bug-validation.md).
