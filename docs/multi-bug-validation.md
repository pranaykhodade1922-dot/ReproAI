# Two independent mobile failure classes

Latest validation: [Verify Fix workflow validation](verify-fix-validation.md) records 62 backend tests, 40 Android unit tests and 35 physical phone UI tests, with typed healthy-state verification using the same scenario identity/actions. Fresh Bug 1 validation on OPPO CPH2577 (Android 15) passed with 3/3 failure assertions and 3/3 healthy assertions (`scen_46e1a67c`); Bug 2 results below remain previously validated. Historical counts and execution IDs below are retained as evidence from that run.

ReproAI uses the same capture, analysis, typed scenario, ADB execution, report and same-scenario verification pipeline for two DemoShop failures. This validates these two instrumented patterns, not arbitrary Android bugs. Analysis is local rule based, including the normal `LocalAiProvider` path; no LLM inference is active.

## Architecture and changed files

DemoShop previously kept checkout/payment state in the singleton `ShopController`. Checkout now includes a UPI/Card selector, validity, a last state event and before/after configuration orientation. `MainActivity.onSaveInstanceState` records the selected method and orientation. On actual Activity recreation, BUGGY mode deliberately discards the method; FIXED mode restores it from the saved Bundle. The separate **Checkout rotation state: BUGGY/FIXED** control does not change authentication behavior. Both controls and the automation provider are debug-only.

Changed Android files: `demo-shop/.../ShopController.kt`, `MainActivity.kt`, debug `DemoAutomationProvider.kt`; app `ai/RuleBasedFallbackProvider.kt`, new `PaymentNetworkFailureRule.kt` and `CheckoutRotationStateLossRule.kt`; `tracking/OrientationMonitor.kt`, `DebugSessionManager.kt`, `receiver/SdkEventReceiver.kt`; `data/remote/RunnerApiClient.kt`, `navigation/ReproNavHost.kt`; `ui/screens/running/RunnerScreens.kt`, `ui/screens/report/BugReportScreen.kt`; `report/IncidentReport.kt`, `IncidentReportExporter.kt`, `DeveloperPackageExporter.kt`; new `RotationBugTest.kt` and extended `IncidentReportTest.kt`.

Changed backend files: `app/adb/client.py`, `demo_bridge.py`; `runner/action_registry.py`, `demo_assertions.py`, `scenario_runner.py`; `services/preflight.py`, `models/execution.py`, `api/routes.py`; `tests/test_rotation.py`, `tests/test_preflight.py`. Documentation: this document and the root/backend READMEs.

Each analysis rule implements `detect` and `analyze` and produces a `TestScenario`. The rotation rule requires a real DEVICE orientation event plus selected, recreated, lost and invalid DEMOSHOP evidence. Fixed or orientation-only evidence cannot diagnose state loss. Result interpretation uses the scenario's expected failure assertions, execution/scenario IDs and measured current app state. Mock, unsupported, incomplete and unrelated executions cannot prove reproduction or a fix.

SDK events retain their original occurrence timestamps. Session event writes are serialized to avoid dropped concurrent events. Lifecycle/display monitors register on Android's main thread. Display/configuration callbacks capture actual orientation; SDK events do not impersonate DEVICE events. Null state values survive result persistence and report export; old omitted-null checkout records remain readable when their scoped loss/invalid/blocked evidence is complete. Strictly formatted technical identifiers survive phone-number masking in identifier fields; actual phone numbers and secret fields remain redacted. Execution completion navigates through the current resumed composition so Activity recreation cannot call a destroyed navigation controller.

## Bug 1: network/authentication retry

Trigger: start a payment with the deterministic network-transition hook enabled. Failure: `PAY_BUTTON_CLICKED`, `PAYMENT_NETWORK_CHANGED`, `PAYMENT_RETRY`, `TOKEN_EXPIRED`, HTTP 401, `PAYMENT_FAILED`. Fixed authentication refreshes the token and reaches HTTP 200 / `PAYMENT_SUCCESS`.

The existing generated scenario opens Checkout, enables the network-transition condition, invokes PAY and waits 2500 ms, then asserts the payment failure signature. Verification reuses that exact scenario. The payment behavior and independent authentication control are retained.

What is real: phone app execution, SDK broadcasts, runner communication, scoped debug-state reads, report persistence and export. What is deterministic: DemoShop's network/authentication/payment state machine. Physical radio switching and a production payment API are not exercised.

## Bug 2: configuration/state restoration

Trigger: valid Checkout with UPI selected, followed by PORTRAIT → LANDSCAPE. The display actually rotates and Android recreates DemoShop's Activity.

Buggy evidence: `PAYMENT_METHOD_SELECTED`, DEVICE `ORIENTATION_CHANGED`, `CHECKOUT_RECREATED`, `CHECKOUT_STATE_LOST`, `CHECKOUT_INVALID`, `PAYMENT_BLOCKED`. Debug state has `selectedPaymentMethod=null`, `isValid=false`, `lastStateEvent=CHECKOUT_STATE_LOST`, configuration orientation 1 → 2.

Fixed evidence: selection remains UPI, `CHECKOUT_STATE_RESTORED`, `CHECKOUT_VALID`, `PAYMENT_AVAILABLE`, `isValid=true`, configuration orientation 1 → 2. No HTTP or network-transition events are required.

Actual captured incident **RPA-088203E8** (3 October 2026, device local time):

| Time | Source | Captured event |
| --- | --- | --- |
| 10:13:43.778 | DEMOSHOP | PAYMENT_METHOD_SELECTED |
| 10:13:45.282 | DEVICE | ORIENTATION_CHANGED, PORTRAIT → LANDSCAPE |
| 10:13:45.492 | DEMOSHOP | CHECKOUT_RECREATED |
| 10:13:45.534 | DEMOSHOP | CHECKOUT_STATE_LOST |
| 10:13:45.538 | DEMOSHOP | CHECKOUT_INVALID |
| 10:13:45.551 | DEMOSHOP | PAYMENT_BLOCKED |

The independently captured DEVICE event precedes actual checkout recreation. The original report identifier is `5222f8cc-f607-44ec-88ab-1aac44f19cdf`; report regeneration preserves it.

Generated scenario:

1. `OPEN_SCREEN Product`
2. `TAP ADD_TO_CART`
3. `TAP PROCEED_TO_CHECKOUT`
4. `TAP SELECT_UPI`
5. `ASSERT_EVENT CHECKOUT_VALID=True`
6. `ROTATE_DEVICE LANDSCAPE`
7. `WAIT 1000`

Final assertions: `ORIENTATION_CHANGED`, `CHECKOUT_RECREATED`, `CHECKOUT_STATE_LOST`, `CHECKOUT_INVALID`, `PAYMENT_BLOCKED`. Verification reuses this scenario unchanged: the failure assertions becoming false is expected, but **FIX VERIFIED** additionally requires positive restored/valid/available evidence and UPI retained.

Stable automation targets invoke the same controller operations as the UI. The selector and invalid state have Compose test tags. Rotation has no synthetic debug hook: ADB uses `wm user-rotation lock`, measures `dumpsys input` SurfaceOrientation and checks actual checkout recreation. It records initial rotation lock and surface orientation and restores the original lock/free setting in `finally`. Uncontrollable or unmeasured rotation returns UNSUPPORTED.

What is real: device display rotation, Android configuration change and Activity recreation, lost/restored app state, SDK/device observations, ADB assertions and reports. What is deterministic: the deliberately broken checkout restore path and its debug fix toggle. This is a demonstration of restoration behavior, not a discovered production defect.

## Reset and history

Presenter **Reset demo** clears cart, selected method, checkout validity, last event, orientation state and debug events, and returns both independent controls to BUGGY. Backend reset verifies those fields before confirming success. Per-execution `RESET_DEMO` clears the execution state and events while retaining the selected implementation, allowing the same scenario to verify a fix. Historical ReproAI incidents and earlier execution/report evidence are preserved.

## Validation and limitations

Backend tests pass **51/51**, Android unit tests **31/31**, and real-phone UI instrumentation **33/33** (80.027 seconds). Both debug APKs build successfully.

| Real-device case | Scenario | Result |
| --- | --- | --- |
| Bug 1 buggy authentication | `scen_78561729` | BUG REPRODUCED; all four controls pass, 3/3 failure assertions match, HTTP 401 / TOKEN_EXPIRED / PAYMENT_FAILED. |
| Bug 1 fixed authentication | Same scenario | FIX VERIFIED; all four controls pass, 0/3 original failure assertions match, HTTP 200 / TOKEN_REFRESHED / PAYMENT_SUCCESS. |
| Bug 2 buggy checkout | `scen_rotation_e1f0ba16` | BUG REPRODUCED; all seven controls pass, 5/5 failure assertions match, selection null and checkout invalid after actual recreation. |
| Bug 2 fixed checkout | Same scenario | FIX VERIFIED; all seven controls pass, 2/5 original failure assertions match (orientation and recreation), UPI retained with RESTORED / VALID / AVAILABLE. |

Bug 1 incident is `RPA-F1E6817D`, original execution `exec-974eb120335a4e458c558a15d5ea1921` (34.2 seconds), verification `exec-7685566e1694476d8cf1039272407a86` (32.4 seconds). Bug 2 incident is `RPA-088203E8`, original execution `exec-f399d0a92da74837a483fe4eeaec23aa` (62.7 seconds). An explicitly enabled isolated MOCK run also passes, but is not reproduction or fix evidence.

Sanitized developer packages and their typed scenario/original/verification JSON are retained locally under `artifacts/multi-bug/RPA-F1E6817D/` and `artifacts/multi-bug/RPA-088203E8/`. These private artifacts are excluded from version control. Runner JSON and UI XML are under `artifacts/multi-bug/`; final instrumentation output is `instrumentation-final.txt`.

Final Bug 2 verification is `exec-24eb3c70197d4eac91b7572758b7ec84` (55.6 seconds). Its measured restoration evidence is `rotation_restore={lock:"lock 0",verified:true,surface_orientation:0}`. Both sanitized packages were checked against their exact original/verification execution IDs and unchanged scenario IDs. After force-stop and normal launcher restart, Bug 1's report retained identifier `c2cee87b-4e35-4a6b-92a3-3fc98aa7aec7`; Bug 2's report retained `5222f8cc-f607-44ec-88ab-1aac44f19cdf`, FIX VERIFIED and both execution IDs. All 13 incidents remained available. Final presenter reset confirmed both controls BUGGY, cleared checkout/payment state and preserved history; physical portrait was measured again.

Device: OPPO CPH2477, Android 12, 720 × 1612 pixels. This device rejects direct Settings rotation writes, but supports the window-manager rotation command. Original rotation setting is `lock 0`. The corrected display listener captured the DEVICE landscape transition while DemoShop was foregrounded, before checkout recreation and before ReproAI returned to the foreground. The runner resumes Checkout before rotating because background Activities can defer configuration changes. Other devices may return UNSUPPORTED; no fallback fabricates rotation. Wireless debugging can disconnect when the phone sleeps.

Exactly two failure classes are implemented. Reports keep original and verification evidence separate; no third bug was added.
