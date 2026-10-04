# Verify Fix workflow validation

Date: 2026-10-04. Changes are local and uncommitted.

## Cause and implementation

`ReproViewModel.startTestRun` selected VERIFY_FIX only as Android context. The repository
submitted the original TestScenario, and the runner evaluated its failure assertions.
The presentation layer could then label a FAILED execution FIX VERIFIED from its snapshot,
leaving contradictory status and assertion rows.

TestScenario now carries an explicit `verification_assertions` profile and
`execution_purpose` (REPRODUCE or VERIFY_FIX). The request is a copy of the original
scenario with the same ID, preconditions, steps and failure assertions. Only the selected
expectation profile changes. Inline action assertions, including valid checkout before
rotation, remain unchanged.

The backend selects the requested profile for pending rows, preflight and evaluation.
Missing, negative-only or incomplete known healthy profiles are rejected. Its typed
`product_outcome` confirms a product result only for passed ADB execution and scoped
measured evidence. MOCK never confirms a fix. Android requires PASSED, zero failed
assertions, the complete healthy profile, matching purpose and runner outcome, and the
measured positive snapshot before showing FIX VERIFIED.

Known saved payment and rotation scenarios receive deterministic profiles without
regenerating their ID or actions. Legacy omitted/null optional fields remain importable.
No LLM inference or security relaxation was introduced. ADB allow lists and shell=False
are unchanged. Existing execution-history persistence retains separate run records.

## Required checks

| Check | Result |
| --- | --- |
| Backend full pytest suite | 62 passed |
| Android app debug unit tests | 40 passed |
| Physical Android instrumentation/UI suite | 35 passed |
| `:app:assembleDebug` | Passed |
| `:demo-shop:assembleDebug` | Passed |
| `:app:assembleDebugAndroidTest` | Passed |
| APK installation | `adb install -r`; app data retained |

Regression coverage includes both bugs, missing healthy evidence, HTTP 401 in verification,
HTTP 200 without token refresh, failed execution despite a healthy snapshot, MOCK,
legacy saved scenarios, unchanged actions and report preservation.

## Physical Bug 1

A real ReproAI session captured DemoShop payment failure through Product, Cart and
Checkout. Capture Issue populated the expected authentication-retry description.
Analysis generated scenario `scen_c4d14590`. Run and Verify Fix were invoked in ReproAI.

| Run | Runner status | Product outcome | Assertions |
| --- | --- | --- | --- |
| BUGGY authentication | PASSED | BUG_REPRODUCED | 3/3 matched |
| FIXED authentication | PASSED | FIX_VERIFIED | 3/3 matched |

Original evidence: TOKEN_EXPIRED, HTTP 401, PAYMENT_FAILED.
Healthy evidence: TOKEN_REFRESHED, HTTP 200, PAYMENT_SUCCESS.
Both include measured payment request, network transition and retry events.
All four original controls matched across executions; scenario IDs were identical.

Reproduction execution: `exec-d36caf46e5234208a6375d040934e0ce`.
Successful verification execution: `exec-36ed1455892741a0a7c8b26bab184433`.

An earlier verification attempt timed out launching DemoShop and was not reported as
verified. Runtime connection recovery and a DemoShop restart without data clearing
allowed the same scenario to pass. Failed attempts remain in execution history.

The actual local developer package exported from incident RPA-42F8A45F was inspected:
report status FIX_VERIFIED, original outcome BUG REPRODUCED / PASSED, verification
outcome FIX VERIFIED / PASSED, distinct execution IDs, sameScenario=true, and separate
original/healthy signatures. The original HTTP 401 assertion profile was retained.
The validation debug session was then stopped. No recording was started.

## Physical Bug 2 regression

Scenario `scen_rotation_verify_regression` used the seven original rotation-rule controls:
Product, Add to cart, Proceed to checkout, Select UPI, Assert valid checkout,
actual landscape rotation, and Wait. The same ID, preconditions and controls were used
for both runs through the real ADB runner API.

| Run | Runner status | Product outcome | Assertions |
| --- | --- | --- | --- |
| BUGGY checkout restoration | PASSED | BUG_REPRODUCED | 5/5 matched |
| FIXED checkout restoration | PASSED | FIX_VERIFIED | 5/5 matched |

Buggy evidence: CHECKOUT_STATE_LOST, CHECKOUT_INVALID, PAYMENT_BLOCKED.
Healthy evidence: CHECKOUT_STATE_RESTORED, CHECKOUT_VALID, PAYMENT_AVAILABLE.
Both confirmed actual orientation change and Activity recreation. The successful fixed
run also confirmed restoration of the initial portrait rotation state. One earlier fixed
attempt encountered a device-evidence timeout and remained ERROR; it was retried after
restoring portrait and restarting DemoShop without clearing data.

## Recording preparation

DemoShop was reset to BUGGY with payment IDLE, Product start, and deterministic network
transition enabled. Reset produced no payment evidence. Existing ReproAI history and
the newly validated report were retained. The runner was restarted in ADB mode with
the final tested code and no active execution.

Final checks confirmed exactly one authorized phone and one runner device, healthy ADB
runner, installed ReproAI APK matching the successful build, ReproAI Home with READY,
readiness READY / ADB, no active debug session, portrait orientation, keyboard hidden,
and configuration controls closed. Home retained all 20 incidents (19 existing plus
the validation incident), including the new VERIFIED incident. Ready for manual recording.
No commit, push or release update was performed.

## Files changed

Android production:

- `android-app/app/src/main/java/com/pranay/reproai/ai/AnalysisModels.kt`
- `android-app/app/src/main/java/com/pranay/reproai/ai/PaymentNetworkFailureRule.kt`
- `android-app/app/src/main/java/com/pranay/reproai/ai/CheckoutRotationStateLossRule.kt`
- `android-app/app/src/main/java/com/pranay/reproai/ai/VerificationProfile.kt`
- `android-app/app/src/main/java/com/pranay/reproai/data/remote/dto/RunnerModels.kt`
- `android-app/app/src/main/java/com/pranay/reproai/data/repository/ReproRunnerRepository.kt`
- `android-app/app/src/main/java/com/pranay/reproai/ui/screens/running/RunnerScreens.kt`

Android tests:

- `android-app/app/src/test/java/com/pranay/reproai/DeveloperPackageTest.kt`
- `android-app/app/src/test/java/com/pranay/reproai/ExecutionInterpretationTest.kt`
- `android-app/app/src/test/java/com/pranay/reproai/IncidentReportTest.kt`
- `android-app/app/src/test/java/com/pranay/reproai/RotationBugTest.kt`
- `android-app/app/src/test/java/com/pranay/reproai/VerificationProfileTest.kt`
- `android-app/app/src/androidTest/java/com/pranay/reproai/VerificationPresentationTest.kt`

Backend production and tests:

- `backend/app/models/scenario.py`
- `backend/app/models/execution.py`
- `backend/app/api/routes.py`
- `backend/app/services/execution_service.py`
- `backend/app/services/preflight.py`
- `backend/app/runner/scenario_runner.py`
- `backend/app/runner/execution_outcome.py`
- `backend/tests/test_verification.py`

Documentation:

- `backend/docs/api-contract.md`
- `backend/docs/demoshop-automation.md`
- `backend/validate_contract.py`
- `docs/verify-fix-validation.md`
