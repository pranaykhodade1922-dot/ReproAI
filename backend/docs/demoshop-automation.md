# DemoShop automation and physical-device evidence

Validated on 2 October 2026 with OPPO CPH2477 / Android 12, wireless ADB `DEVICE_SERIAL`, Python 3.12.10, and both current debug APKs.

## What changed

Previously Checkout could not be opened reliably, taps used fixed coordinates, network changes were unavailable, and assertions had no measured source. DemoShop now shares one ShopController between Compose UI and a finite debug automation provider. The fake payment service itself emits SDK events and updates the state read by the runner. Python compares the returned state; it does not generate payment events or statuses.

Compose exports resource IDs for `product_add_to_cart`, `cart_checkout`, `checkout_pay`, `checkout_demo_network_toggle`, `payment_success`, `payment_failure`, and `payment_status`. `checkout_fixed_auth_toggle` controls the debug authentication implementation. These hooks invoke the same controller functions as the buttons, rather than synthesizing touchscreen input.

`src/debug` contains a shell/root/self-only ContentProvider and a temporary deep-link routing Activity. Release merged manifest contains neither component nor the deep-link filters. The fixed-auth UI and preference loading are gated by BuildConfig.DEBUG. Deep links accept only product/cart/checkout, optionally `simulateNetworkTransition=true`. Their separate task affinity and NEW_TASK/CLEAR_TOP/SINGLE_TOP launch prevent repeated launches from only foregrounding an old task.

## Start the runner

From the repository root, install both debug APKs after building:

```powershell
cd android-app
.\gradlew.bat :app:assembleDebug :demo-shop:assembleDebug :app:testDebugUnitTest
cd ..
adb connect DEVICE_SERIAL
adb -s DEVICE_SERIAL install -r android-app/app/build/outputs/apk/debug/app-debug.apk
adb -s DEVICE_SERIAL install -r android-app/demo-shop/build/outputs/apk/debug/demo-shop-debug.apk
cd backend
$env:HOST='0.0.0.0'
$env:RUNNER_MODE='adb'
$env:DEFAULT_DEVICE_SERIAL='DEVICE_SERIAL'
$env:NETWORK_STRATEGY='DETERMINISTIC_DEMO'
.\.venv\Scripts\python.exe -m uvicorn main:app --host 0.0.0.0 --port 8000
```

Wireless ports change; replace the serial with the current phone connection address. In ReproAI use laptop Wi-Fi host and port 8000, then TEST CONNECTION. Environment overrides above do not rewrite the user's .env. ADB mode never silently falls back to mock. REAL_NETWORK currently returns a controlled unsupported error; DETERMINISTIC_DEMO explicitly reports DEMOSHOP_DEMO_HOOK.

## Debug bridge

```powershell
adb -s <serial> shell content call --uri content://com.pranay.demoshop.automation --method RESET_DEMO --arg exec-example
adb -s <serial> shell am start -W -a android.intent.action.VIEW -d reproai-demo://checkout -n com.pranay.demoshop/.DemoDeepLinkActivity
adb -s <serial> shell content call --uri content://com.pranay.demoshop.automation --method ENABLE_DEMO_NETWORK_TRANSITION
adb -s <serial> shell content call --uri content://com.pranay.demoshop.automation --method PAY
adb -s <serial> shell content call --uri content://com.pranay.demoshop.automation --method STATE
```

Response is a Bundle containing base64 JSON in `data`. Known provider methods: STATE, RESET_DEMO, PRODUCT, CART, CHECKOUT, ADD_TO_CART, PROCEED_TO_CHECKOUT, PAY, ENABLE_DEMO_NETWORK_TRANSITION, FIXED_AUTH, BUGGY_AUTH. Runner permits the operational subset and never changes the authentication setting. Unknown actions fail. Other application UIDs cannot use the provider. There is no arbitrary shell/script interface.

RESET_DEMO cancels payment and clears cart/payment/API/events/transition state, assigns the new execution ID, and preserves the explicitly selected buggy/fixed authentication setting. Provider dispatches mutations on the app main thread. Payment callbacks reject obsolete execution IDs; backend rejects stale snapshots and serializes runs per selected device. Backend samples structured state until terminal payment status and verifies actual visible navigation.

Enable transition BEFORE Pay: the payment delay is shorter than some wireless ADB calls. Generated scenarios and samples use OPEN_SCREEN Checkout, CHANGE_NETWORK CELLULAR, TAP PAY, WAIT 2500, then the three failure assertions. A previously saved scenario retains its old order; regenerate its analysis to obtain the corrected scenario.

## Product outcome semantics

The unchanged scenario has failure assertions: TOKEN_EXPIRED=True, /payment=401, PAYMENT_FAILED=True. In buggy mode they pass, and Android requires the complete measured failure signature before showing BUG REPRODUCED. Successful navigation alone or one assertion cannot establish reproduction.

Enable **Use fixed authentication retry** in DemoShop Checkout, or the known shell FIXED_AUTH method. The on-device fixed branch refreshes the demo token before retry and emits TOKEN_REFRESHED, HTTP200, PAYMENT_SUCCESS. Verify Fix sends the same scenario ID, setup and actions with execution_purpose=VERIFY_FIX. The runner selects verification_assertions for TOKEN_REFRESHED=True, /payment=200, PAYMENT_SUCCESS=True and returns PASSED / FIX_VERIFIED only after measured healthy evidence. The original failure profile remains unchanged. Android requires the passed profile and scoped positive evidence before showing FIX VERIFIED. This proves the DemoShop retry fix, not a production authentication service. The validation records below describe the earlier workflow; the current rebuild uses passed healthy assertions.

## Evidence and coverage

Buggy phone result (private local evidence: `../artifacts/exec-phone-buggy-hooks/result.json`; excluded from GitHub) records ADB, DEMOSHOP_DEMO_HOOK, reset evidence, all seven passing steps, HTTP401, TOKEN_EXPIRED and PAYMENT_FAILED. Failure screenshot (private local evidence: `../artifacts/exec-phone-buggy-hooks/payment.png`; excluded from GitHub).

Fixed phone result (private local evidence: `../artifacts/exec-phone-fixed-hooks/result.json`; excluded from GitHub) uses the identical scenario (private local evidence: `../artifacts/payment-network-scenario.json`; excluded from GitHub), records HTTP200, TOKEN_REFRESHED and PAYMENT_SUCCESS, and correctly fails original failure assertions. Success screenshot (private local evidence: `../artifacts/exec-phone-fixed-hooks/payment.png`; excluded from GitHub).

| Category | Verified scope |
|---|---|
| FULLY REAL | Physical ADB transport, installed debug app, app navigation/controller invocation, on-device payment execution, querying its current state, backend comparisons, SDK capture and runner transport. |
| DETERMINISTIC DEMO HOOK | Network transition flag, fake payment API status, expired-token failure and fixed-auth success. Events contain source=DEMOSHOP and simulated=true. No radios are toggled. |
| MOCK | Isolated mock server contract run still passes all seven actions/assertions; execution_mode=MOCK, no observed device state. |
| UNSUPPORTED | Physical cellular/Wi-Fi switching, unknown targets, arbitrary commands, generic visibility/text assertions, production payment/API verification. |

Backend regression tests cover missing provider/APK, disconnected authorized-device selection, stale execution evidence, unknown targets, measured mismatches, and REAL_NETWORK without fallback. Missing APK coverage uses an injected ADB response; the user's installed app was not uninstalled. Android tests cover full signatures, stale/missing evidence, mock labels, fixed retry, and invalid execution modes. Release manifest was inspected for component exclusion.

Backend execution results are in memory and disappear on restart; Android saves its latest completed result in Room. Preconditions are transported, but physical network preconditions are not established by the runner. Screenshots are optional evidence manually captured here, not an automatic runner feature.


## Final ReproAI phone journey

The final APK generated the four-action / three-assertion scenario from captured DemoShop SDK events and submitted it over LAN to the ADB runner. WebSocket progress was observed in runner logs.

- Reproduce: execution `exec-be8606e646584fed911289f41ef3f851`, PASSED, three failure assertions passed, duration 35.5 seconds. API result (private local evidence: `../artifacts/exec-be8606e646584fed911289f41ef3f851/result.json`; excluded from GitHub), ReproAI BUG REPRODUCED UI (private local evidence: `../artifacts/phone-hooks-reproduction.xml`; excluded from GitHub). ReproAI was force-stopped and restarted; selecting the incident restored this complete outcome from Room.
- Enable the actual DemoShop FIXED_AUTH setting, then tap Verify Fix on that restored result.
- Verify Fix: execution `exec-fa1a5b6f9cfd44dab4d3ce31c318ea4f`, same scenario ID and action/assertion signatures, FAILED with three original failure assertions failed, duration 32.0 seconds. API result (private local evidence: `../artifacts/exec-fa1a5b6f9cfd44dab4d3ce31c318ea4f/result.json`; excluded from GitHub), ReproAI FIX VERIFIED UI (private local evidence: `../artifacts/phone-hooks-fixed.xml`; excluded from GitHub). All four control steps passed. Positive measured evidence confirms the fixed retry.

Final checks: backend pytest **18 passed**; Android unit tests **8 passed**; both assembleDebug builds successful; release manifest excludes the debug provider and deep-link activity. The isolated mock server still returned PASSED with seven simulated steps and three assertions; see mock result (private local evidence: `mock-result.json`; excluded from GitHub).

Use uvicorn without --reload during demos. A reload discards in-memory executions and Android correctly reports an execution-lost error. Earlier failed navigation/reload attempts were corrected and rerun before the final outcomes above. Screenshots containing unrelated phone UI were discarded; final app XML dumps are restricted to ReproAI/DemoShop.
