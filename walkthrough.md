# Current phase: Phase 8 hackathon hardening

Debug reset, BUGGY/FIXED controls, runner/device/hooks preflight, explicit mock confirmation, bounded recovery, sanitized developer ZIP and standalone scenario export are implemented. Backend: 33 tests passed. Android: 25 unit tests and 33 physical-device UI tests passed; debug and unsigned release builds succeeded. See [the final hardening review](docs/phase8-hardening.md), [demo script](docs/demo-script.md), [Office Kit workflow](docs/office-kit-workflow.md) and [submission checklist](docs/submission-checklist.md). Real ADB reproduction and same-scenario fix verification were measured; the sanitized phone ZIP was saved through the native local file picker and its scenario validated by Pydantic.

Device information is discovered dynamically. Actual iQOO/Office Kit pairing and transfer, recording and team submission URLs still require the final hardware/team artifacts. Historical sections below describe their respective earlier builds.

# Previous phase: Phase 7 incident reports

Professional incident reports, sanitized sharing/copy and JSON/Markdown/Text exports are implemented. See [Phase 7 changes and validation](docs/phase7-reports.md). Current discovered wireless serial: `DEVICE_SERIAL`.

# Previous phase: phone UI redesign

The mobile UI redesign is implemented. See [UI changes and validation](docs/ui-redesign.md) for screens, components, modified files, responsive checks and physical-phone evidence. Phase 7 report work remains outside this change.

# Previous phase: DemoShop ADB automation

The debug automation phase is implemented and verified on the phone: BUG REPRODUCED, followed by FIX VERIFIED for the same scenario. Backend: 18 tests passed. Android: 8 tests passed; both debug builds successful. See [DemoShop automation and measured device evidence](backend/docs/demoshop-automation.md) for setup, buggy/fixed runs, classifications, and limitations. The sections below record the earlier integration phase; its unsupported-hook statements describe that earlier build. Current wireless serial: `DEVICE_SERIAL`.

# ReproAI runner integration walkthrough

Validated on 2 October 2026 using Python 3.12.10, the local FastAPI runner, and an OPPO CPH2477 running Android 12. The phone was reconnected using the user-provided wireless-debugging address `DEVICE_SERIAL`.

## 1. Schema comparison and fixes

The active Android module is `android-app/app`; the older `android-app/src` tree is not the app module. The typed models in `ai/AnalysisModels.kt` match Pydantic field for field: scenario id/name/description/preconditions/steps/assertions; precondition type/value; step action/target/value/description; assertion type/target/expected. All 12 action enum strings match. Nullable optional fields may be omitted or null. `data/model/TestScenario` is an older presentation model and is never posted.

ExecutionResult already had execution_mode; it is now restricted to MOCK/ADB. Health now returns runnerMode. The prior backend accepted /execute only after completing work and its WebSocket merely echoed messages. Submission now returns an accepted snapshot, and GET/WebSocket expose PENDING/RUNNING/terminal execution and step snapshots. Assertion action names retain their existing ASSERT_ prefix rather than duplicating it.

Removed unsafe outcome assumptions: absent ADB devices no longer cause silent mock fallback; real assertions/network changes no longer produce canned passes; unknown taps no longer use fallback coordinates; Android no longer labels every non-reproduction as FIX VERIFIED. Unknown response modes and incomplete response shapes are rejected before UI rendering.

## 2. Backend files changed or added

- `backend/.env.example`, `backend/requirements.txt`, `backend/README.md`
- `backend/app/api/routes.py`, `backend/app/api/websocket.py`
- `backend/app/models/execution.py`
- `backend/app/services/execution_service.py`
- `backend/app/runner/scenario_runner.py`, `action_registry.py`, `assertion_engine.py`
- `backend/app/adb/client.py`
- `backend/tests/test_health.py`, `test_execution_endpoint.py`, `test_integration.py`, `test_adb_safety.py`
- `backend/validate_contract.py`
- `backend/docs/api-contract.md`, `android-integration.md`, and validation JSON/XML artifacts

The user's `backend/.env` retains localhost/mock defaults. No cloud infrastructure or Office Kit was added. Controlled ADB calls retain shell=False; actions and packages remain restricted; no user/model text is evaluated or executed as code.

## 3. Android files changed or added

Paths below are relative to `android-app/app/`:

- `build.gradle.kts`: Retrofit, Gson converter, OkHttp, JUnit
- `src/main/AndroidManifest.xml`: INTERNET permission
- `src/debug/AndroidManifest.xml`: debug-only cleartext HTTP
- `src/main/java/com/pranay/reproai/data/remote/ReproRunnerApi.kt`, `RunnerApiClient.kt`, `dto/RunnerModels.kt`
- `src/main/java/com/pranay/reproai/data/repository/ReproRunnerRepository.kt`
- `src/main/java/com/pranay/reproai/data/local/ReproDatabase.kt`
- `src/main/java/com/pranay/reproai/data/local/entity/IncidentExecutionEntity.kt`, `dao/IncidentExecutionDao.kt`
- `src/main/java/com/pranay/reproai/viewmodel/ReproViewModel.kt`
- `src/main/java/com/pranay/reproai/navigation/ReproNavHost.kt`
- `src/main/java/com/pranay/reproai/ui/screens/home/HomeScreen.kt`
- `src/main/java/com/pranay/reproai/ui/screens/reproduction/ReproductionScreen.kt`
- `src/main/java/com/pranay/reproai/ui/screens/running/RunnerScreens.kt`
- `src/test/java/com/pranay/reproai/ExecutionInterpretationTest.kt`

Existing session capture, SDK ingestion, analysis and Room tables were preserved. Room version 2 adds incident_executions through a 1→2 migration without deleting existing records. Settings use local preferences. Result actions retain timeline/report navigation. Home displays real recent sessions and the latest runner status instead of a hardcoded verified count.

## 4. Exact startup commands

From the repository root, PowerShell:

```powershell
cd backend
.\.venv\Scripts\python.exe -m pip install -r requirements.txt
.\.venv\Scripts\python.exe -m pytest
# Default localhost/mock server; reads backend/.env:
.\.venv\Scripts\python.exe main.py
```

For explicit LAN development, use a separate terminal and stop any server already using port 8000:

```powershell
cd backend
$env:HOST='0.0.0.0'
$env:RUNNER_MODE='mock'
.\.venv\Scripts\python.exe main.py
```

For ADB mode:

```powershell
cd backend
adb devices
$env:HOST='0.0.0.0'
$env:RUNNER_MODE='adb'
$env:DEFAULT_DEVICE_SERIAL='DEVICE_SERIAL'
.\.venv\Scripts\python.exe main.py
```

Wireless debugging ports can change; replace the serial with the currently authorized online device. With multiple ADB transports/devices, explicitly select the serial. The Private-network Windows firewall may need an application exception or scoped inbound TCP 8000 rule. No firewall setting was disabled or automatically changed.

Android validation:

```powershell
cd android-app
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat :app:assembleDebug :demo-shop:assembleDebug :app:testDebugUnitTest
```

## 5. Exact phone settings used

Host: `192.168.x.x` (the laptop's observed Wi-Fi address).

Port: `8000`.

These are local test settings, not hardcoded application defaults. The phone's `DEVICE_SERIAL` address is an ADB connection address, not the runner URL. TEST CONNECTION displayed Connected, service repro-runner, and the actual MOCK/ADB mode. Host/port survived application replacement and restart.

## 6. Mock result and actual phone journey

The laptop sample executed six steps with two passing simulated assertions. Exact example: mock-result.json (private local evidence: `backend/docs/mock-result.json`; excluded from GitHub).

The full physical-phone journey was performed:

1. ReproAI tested the laptop LAN connection and started recording.
2. DemoShop opened; Add to Cart and Proceed to Checkout were tapped.
3. Its demo transition switch was enabled; PAY displayed HTTP 401/token expiry.
4. ReproAI received real SDK events: PAY_BUTTON_CLICKED, PAYMENT_NETWORK_CHANGED, TOKEN_EXPIRED, API Response 401, PAYMENT_FAILED.
5. The issue was captured and analyzed; the generated scenario was Payment Network Transition Failure.
6. Android posted that typed scenario over Wi-Fi and subscribed to real WebSocket snapshots.
7. Android displayed MOCK EXECUTION — PASSED, six steps, and two simulated assertion passes.
8. Verify Fix posted the identical scenario ID with Android purpose VERIFY_FIX. The mock label remained, and FIX VERIFIED was not claimed.
9. ReproAI was stopped, the final APK reinstalled, and the saved incident selected. Room restored its analysis, scenario, final steps and VERIFY_FIX purpose.

The first capture attempt was outside the analyzer's existing 30-second evidence window and yielded a general scenario. The DemoShop failure was repeated and captured promptly; the final payment-specific run included its real SDK evidence. The analyzer window was preserved.

Evidence:

- Phone reproduction JSON (private local evidence: `backend/docs/phone-reproduction-result.json`; excluded from GitHub), UI snapshot (private local evidence: `backend/docs/phone-reproduction-result.xml`; excluded from GitHub)
- Phone Verify Fix JSON (private local evidence: `backend/docs/phone-verify-result.json`; excluded from GitHub), UI snapshot (private local evidence: `backend/docs/phone-verify-result.xml`; excluded from GitHub)
- Restored result UI (private local evidence: `backend/docs/phone-restored-result.xml`; excluded from GitHub)

## 7. ADB result

A real ADB smoke test launched the installed DemoShop Product activity and waited successfully. The phone UI confirmed Featured Product. An event assertion then returned UNSUPPORTED, producing FAILED rather than a false proof of reproduction. ADB smoke result (private local evidence: `backend/docs/adb-result.json`; excluded from GitHub), launch UI (private local evidence: `backend/docs/phone-adb-launch.xml`; excluded from GitHub).

The same payment scenario was also submitted from Android against the LAN runner in ADB mode. Health reported adb; Android received real WebSocket updates and displayed EXECUTION FAILED / Mode ADB / selected phone serial. Checkout navigation, network transition and both evidence assertions returned UNSUPPORTED. WAIT executed; TAP issued a registered coordinate command whose evidence explicitly says the target hit is unverified. Zero assertions passed; two were unsupported/failed. Phone ADB result (private local evidence: `backend/docs/phone-adb-result.json`; excluded from GitHub), UI snapshot (private local evidence: `backend/docs/phone-adb-result.xml`; excluded from GitHub).

This validates the ADB transport, device selection, controlled launch and honest error/result UI. It does not establish successful automated payment reproduction or fix verification.

The final installed APK also restored the latest ADB result from the saved incident and retained VIEW TIMELINE / CREATE REPORT actions. Final installed result UI (private local evidence: `backend/docs/phone-final-result.xml`; excluded from GitHub).

## 8. Remaining limitations

- Required DemoShop hooks: stable selectors for cart/checkout/pay/switch, deterministic reset/direct Checkout navigation, transition control, per-execution API/event/result evidence, and measured success assertions.
- Registered coordinate taps depend on screen size/orientation; PASSED means the input command completed, not that the intended UI target was verified.
- Preconditions are transported but must be arranged manually in ADB mode.
- The current payment scenario asserts failure evidence. Passing HTTP 401/token-expiry assertions cannot prove that payment was fixed; the same failure-oriented scenario remains unconfirmed for VERIFY_FIX.
- Backend execution storage is in memory. Android persists received terminal results. Process death during a run can leave an unreceived result on the backend.
- Cancellation is cooperative; an ADB command already in flight may finish before cancellation takes effect.
- Automated tests cover polling/terminal results, cancellation, WebSocket delivery, invalid actions, missing devices, package restrictions, and outcome interpretation. Network-disconnect/timeouts have bounded handling but were not all manually injected on the phone.
- Existing local analysis/provider behavior was preserved; no new hosted AI integration was introduced.

## 9. What is still simulated

MOCK runner actions/assertions; DemoShop's FakePaymentService and demo network transition; existing fallback/local analysis behavior. No bank/payment API or physical Wi-Fi-to-cellular transition was proven.

## 10. What is real

Physical phone interaction, debug capture, SDK broadcast ingestion, Room event storage, typed scenario generation/serialization, LAN HTTP submission, backend scheduling, WebSocket result snapshots, polling fallback code, cancellation API, Android StateFlow/result UI, persisted settings, non-destructive migration and saved-result restoration. ADB device discovery, explicit device selection, app launch and wait commands were exercised on the actual OPPO.

Validation: Python 3.12.10; pip check passed; 12 backend tests passed; four Android outcome/contract tests passed; app and demo-shop debug builds succeeded. A Starlette test-client deprecation warning and pre-existing Gradle deprecation notices remain non-blocking.

Temporary LAN/ADB validation servers were stopped afterward. Use the explicit LAN startup command above to resume phone testing; `.env` retains localhost/mock defaults.
