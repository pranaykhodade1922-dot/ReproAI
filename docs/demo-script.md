# ReproAI live demo: 3–5 minutes

## Before the pitch

Install both debug APKs, use trusted local Wi-Fi or USB debugging, start the laptop runner in ADB mode, and configure the phone's runner host/port. On Home, open **Configure → Developer demo → Check readiness**. Confirm READY / ADB and both apps/hooks. Use **Reset demo** to restore BUGGY authentication and cleared state without removing saved incidents. Keep the phone awake and use portrait orientation. Wireless ports can change; update DEFAULT_DEVICE_SERIAL and restart the runner if necessary.

Enable the deterministic network transition in DemoShop Checkout **before** tapping PAY. Capture within 30 seconds of the failure so the existing analysis window includes the relevant events. The transition and payment service are deliberately simulated; ADB controls the actual APK on the actual phone.

| Time | Action | Presenter line |
|---|---|---|
| 0:00–0:20 | Show ReproAI Home, start session | “Developers often know a bug happened but cannot reproduce the exact conditions.” |
| 0:20–0:50 | Open DemoShop, add product, checkout, confirm BUGGY MODE, enable transition, tap PAY | “This demo app fails during an authentication retry after a deterministic network transition.” |
| 0:50–1:15 | Return immediately, Capture issue, enter “Payment failed after switching networks.”, Analyze | “ReproAI records device and application events, then uses local rules to identify the relevant sequence.” |
| 1:15–1:40 | Show TOKEN_EXPIRED, API 401, PAYMENT_FAILED and inferred diagnosis | “These are observed events; the likely cause is an inference.” |
| 1:40–2:20 | Open reproduction, Run reproduction | “The phone sends this exact typed scenario to the laptop. ADB executes it on the device.” |
| 2:20–2:40 | Show BUG REPRODUCED, open report briefly | “The original failure signature was measured, not inferred from a generic green test status.” |
| 2:40–3:10 | Open DemoShop Checkout, enable FIXED authentication, return to result | “The fixed retry refreshes the demo token. The test scenario stays unchanged.” |
| 3:10–3:50 | Verify Fix, wait for the same scenario | “Original failure assertions should stop matching; positive success evidence verifies the fix.” |
| 3:50–4:30 | Show FIX VERIFIED and report before/after | “Before: 401 and token expiry. After: 200, refreshed token and payment success.” |
| 4:30–5:00 | Export developer package, Save to Files; show Office Kit transfer only when validated | “The phone packages sanitized reports, the scenario and both results for the laptop developer workflow.” |

During runner execution, DemoShop may foreground itself. Return to ReproAI without terminating the session/run. Do not change authentication behavior during payment processing.

## Recovery

For a runner error use Reconnect runner, then Retry. For preflight failure, follow the displayed device/APK/hooks message. Cancel/finish an active run before Reset demo. Reset restores BUGGY mode and ends an active ReproAI session; it keeps historical incidents. If wireless ADB drops, use USB or reconnect the current wireless port. Do not secretly switch the live pitch to MOCK; label mock footage clearly if used as a fallback.

## 60–90 second recording

0–10s: problem statement and start session. 10–25s: DemoShop failure, return and capture. 25–40s: analysis evidence and generated scenario. 40–55s: start ADB reproduction and cut to BUG REPRODUCED. 55–75s: enable fixed behavior, Verify Fix, cut to FIX VERIFIED. 75–90s: report's before/after and developer package. Label edits/time compression; do not imply the two real 30-second executions took seconds. Keep setup, account pairing and configuration outside the main recording.

Close: “ReproAI turns real mobile failures into reproducible tests, then verifies the fix using the same scenario.”
