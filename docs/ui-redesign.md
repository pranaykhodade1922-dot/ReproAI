# ReproAI phone UI redesign

Applied on 2 October 2026. This phase changes Android presentation and reads existing data for display. Session capture, Room schema/records, SDK receiver, analysis provider, TestScenario generation, runner networking, WebSocket handling, verification rules, and backend implementation were not changed. Phase 7 report work was not started.

## Presentation

The app uses a neutral dark palette, sans-serif headings/body, monospace technical values, thin dividers and compact status labels. One inset owner in MainActivity keeps content clear of system bars. PhonePage supplies a lazy scrolling body and sticky action area; keyboard insets are handled there. Workflow buttons and interactive icons have at least 48dp targets, with text describing every status.

Home shows a small live-session group, two inline counts, expandable runner configuration and compact incident rows. Titles and outcomes come from a read-only join with existing saved execution data. Verified counts use the unchanged executionTitle evidence rules; raw PASSED or MOCK never becomes VERIFIED. This join does not write Room records, modify sessions, or submit executions.

Live session shows the supplied timer/environment and newest events as compact log rows. Export and demo simulation remain available in its actions menu. Capture uses the current session ID, actual event count and latest API/error signal; voice input is a small trailing action. Analysis emphasizes actual token/network/API failure evidence and gives a compact diagnosis, evidence-strength label, likely areas, existing report action and sticky reproduction action.

Timeline provides All/Device/App/Network/API/Errors filters and expandable, wrapping metadata. Reproduction presents real preconditions, numbered typed steps, expectations, assertions and selectable JSON. Execution uses compact status rows, a thin progress bar, elapsed time, expandable per-step evidence and sticky cancellation. Results use restrained outcome labels and measured evidence, with Verify Fix reachable at the bottom.

Fixed-path results still show the original failure assertions as unmatched. The unchanged verification logic separately confirms the current success signature; the redesign does not claim the original failure scenario passed after a fix.

## Files modified or created

All Kotlin paths below are relative to `android-app/app/src/main/java/com/pranay/reproai/`.

| File | Change |
|---|---|
| MainActivity.kt | Single safe-drawing inset owner. |
| navigation/ReproNavHost.kt | Pass existing session/orientation/event state; preserve workflow callbacks and routes. |
| ui/theme/Color.kt, Type.kt | Neutral colors, restrained status accents, phone typography. |
| ui/components/ConsoleComponents.kt | Compact header, badges, incident rows, accessible actions and compatibility helpers. |
| ui/components/PhoneComponents.kt | New PhonePage, BottomActions, SectionLabel, MetadataRow, TechnicalText, LogRow, NumberedRow. |
| ui/screens/home/HomeScreen.kt | Compact Home and expandable connection settings. |
| ui/screens/home/HomeIncidentPresentation.kt | Read-only saved-title/outcome display adapter. |
| ui/screens/session/ActiveSessionScreen.kt | Live logging and sticky capture/stop actions. |
| ui/screens/describe/DescribeBugScreen.kt | Real capture context, compact voice control, keyboard-safe action. |
| ui/screens/analysis/AiAnalysisScreen.kt | Incident evidence and diagnosis instead of an AI answer layout. |
| ui/screens/timeline/TimelineScreen.kt | Filterable log viewer with expandable details. |
| ui/screens/reproduction/ReproductionScreen.kt | Numbered actions, assertions and sticky run/JSON controls. |
| ui/screens/running/RunnerScreens.kt | Both active execution and result routes; executionTitle is preserved. |

Also updated `android-app/app/build.gradle.kts` with test-only Compose instrumentation dependencies. Added `android-app/app/src/androidTest/java/com/pranay/reproai/PhoneLayoutTest.kt`. Root README and walkthrough link this report. The unused legacy Android source tree was not modified.

## Validation

```powershell
cd android-app
.\gradlew.bat :app:assembleDebug :demo-shop:assembleDebug :app:assembleDebugAndroidTest :app:testDebugUnitTest
cd ..
adb -s <serial> install -r android-app/app/build/outputs/apk/debug/app-debug.apk
adb -s <serial> install -r android-app/app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb -s <serial> shell am instrument -w -r -e class com.pranay.reproai.PhoneLayoutTest com.pranay.reproai.test/androidx.test.runner.AndroidJUnitRunner
```

Both requested debug builds passed. Existing Android unit tests: 8 passed. Responsive instrumented UI tests: 24 passed. Instrumentation checks cover 360/393/430dp at 100% and 130% text, long incident titles, overflowing technical targets, scrolling reproduction steps, analysis failure evidence, sticky actions and callback invocation. Test-local density fits each dp viewport to the physical display without changing system settings. Test fixtures are isolated from Room and the production workflow.

Responsive instrumentation output (private local evidence: `../artifacts/ui-redesign/layout-tests.txt`; excluded from GitHub).

A physical OPPO CPH2477 at 360dp was used for the real workflow. Saved evidence:

- Home (private local evidence: `../artifacts/ui-redesign/home.xml`; excluded from GitHub), Home screenshot (private local evidence: `../artifacts/ui-redesign/home.png`; excluded from GitHub): real saved incident title, VERIFIED status and count; expandable runner connection succeeded.
- Live session (private local evidence: `../artifacts/ui-redesign/live.xml`; excluded from GitHub): actual session ID/environment and sticky capture/stop controls.
- Capture (private local evidence: `../artifacts/ui-redesign/capture.xml`; excluded from GitHub): 13 actual recorded events and API Response 401.
- Timeline API filter (private local evidence: `../artifacts/ui-redesign/timeline-api.xml`; excluded from GitHub), expanded details (private local evidence: `../artifacts/ui-redesign/timeline-details.xml`; excluded from GitHub): six actual API events from the 40-event incident; filtering and metadata expansion verified on the phone.
- Reproduction (private local evidence: `../artifacts/ui-redesign/reproduction.xml`; excluded from GitHub): generated typed actions and reachable Run reproduction / View JSON.
- Fresh reproduction (private local evidence: `../artifacts/ui-redesign/reproduced.xml`; excluded from GitHub), API result (private local evidence: `../artifacts/ui-redesign/reproduced-result.json`; excluded from GitHub): actual ADB execution, three failure assertions matched, HTTP401/TOKEN_EXPIRED/PAYMENT_FAILED, duration 31.8s.
- Verify Fix (private local evidence: `../artifacts/ui-redesign/verify.xml`; excluded from GitHub), API result (private local evidence: `../artifacts/ui-redesign/verified-result.json`; excluded from GitHub): identical scenario ID, HTTP200/TOKEN_REFRESHED/PAYMENT_SUCCESS, duration 30.9s. Original failure assertions correctly remain FAILED. Runner logs show real WebSocket connections for both executions.
- Previously saved result screenshot (private local evidence: `../artifacts/ui-redesign/result.png`; excluded from GitHub): persisted measured fix evidence restored after installation.

## Remaining scope

No clipping was found in the completed responsive cases. Landscape and text scaling above 130% have not been validated. Long technical values wrap or use scrolling in the JSON viewer; incident titles and secondary metadata may ellipsize intentionally.

The existing report destination remains available. Its separate Phase 7 redesign, share flow and regression-saving features are outside this change. No placeholder buttons for unimplemented actions were added.
