# Phase 7: incident reports, sharing and export

Reports are built deterministically from existing captured sessions, AnalysisResult/TestScenario and measured execution results. No additional AI request, GitHub/Jira API, Office Kit integration or PDF generation was added.

## Architecture and persistence

`report/IncidentReport.kt` defines typed report, environment, evidence, reproduction, execution and verification models. Status is DRAFT without analysis, READY with analysis, REPRODUCED only with the existing measured reproduction signature, and FIX_VERIFIED only with the existing fix verification signature. MOCK remains explicitly labelled and cannot establish a product outcome. A later reproduction supersedes an older fix status.

`IncidentReportRepository` loads by session ID and regenerates content. Room version 3 adds report metadata (ID, creation time, status, inferred category), separately persisted analysis for incidents that have not run yet, and execution history. History retains actual runner results required to show both original failure and verification; the existing latest-result table remains compatible. Execution history and latest-result saves are transactional. Migration preserves all existing sessions and events; no destructive migration is used.

Legacy incidents retain only their previously saved latest run. Reports import that real run; overwritten historical executions are not fabricated. If the original run is unavailable, the verification section says so and does not claim comparison with a recorded original scenario. Newly reproduced/verified incidents retain both results. Same-scenario comparison uses the recorded scenario IDs; runner verification continues to execute the existing generated scenario without rewriting failure assertions.

Likely category is an inference, not guaranteed ownership. DemoShop authentication/token evidence maps to APPLICATION; insufficient evidence maps to UNKNOWN. There are no invented ownership percentages or hardcoded device values.

## UI and outward data

The `report/{sessionId}` route has Summary, Environment, observed Evidence, inferred Diagnosis, Reproduction, Execution and Verification sections. It follows PhonePage styling, with dividers, wrapping technical values, expandable evidence descriptions and sticky Share report / Copy summary / Export controls. Partial reports display “Not available yet.” Access is available from Analysis, results including Fix Verified, and Home incident rows.

Copy uses ClipboardManager and a “Report summary copied” snackbar. Text sharing uses ACTION_SEND and Android's Share Sheet. Exports produce JSON, Markdown or Text in the app-private `files/reports/` directory, then offer the file through Android's Share Sheet. The user selects the receiving app. FileProvider exposes only that directory with temporary read permission; no storage permission or arbitrary filesystem path is exposed. Re-export replaces the same incident/format file with the current report.

All outward formats use `IncidentReportExporter` and recursively apply EventSanitizer, including diagnosis, scenario targets/values and nested execution evidence. Sanitization redacts bearer/basic credentials, authorization fields, emails, phone numbers, secret assignments and sensitive metadata keys. The displayed report is sanitized too. JSON is a deliberate report projection rather than a raw Room/session dump.

DemoShop's deterministic network transition and fake payment API are explicitly described as simulation. Physical ADB execution remains distinguished from MOCK. Fix reports preserve the original unmatched failure assertions and use actual scoped success evidence for FIX VERIFIED.

## Modified and added files

Paths below are relative to `android-app/app/src/main/java/com/pranay/reproai/` unless stated otherwise.

| File | Purpose |
|---|---|
| report/IncidentReport.kt | Typed model and deterministic builder/status rules |
| report/IncidentReportRepository.kt | Regenerate reports and persist minimal metadata |
| report/IncidentReportExporter.kt | Sanitized JSON, Markdown, Text and concise summary |
| report/ReportSharing.kt | App-private files and share intents |
| data/local/entity/ReportMetadataEntity.kt | Report metadata, analysis and execution-history entities |
| data/local/dao/ReportDao.kt | Persistence operations |
| data/local/ReproDatabase.kt | Non-destructive 2-to-3 migration |
| viewmodel/ReproViewModel.kt | Preserve analyzed data and execution history, restore saved analysis |
| tracking/DebugSessionManager.kt | Keep edited incident title/description in active state and persistence |
| tracking/EventSanitizer.kt | Redact secret-bearing keys and credential text |
| ui/screens/report/BugReportScreen.kt | Mobile-first report screen and actions |
| ui/screens/home/HomeScreen.kt | Incident report access |
| navigation/AppRoutes.kt, ReproNavHost.kt | Session-specific report route and entry points |
| app/src/main/AndroidManifest.xml, res/xml/report_paths.xml | Restricted FileProvider |
| app/src/test/.../IncidentReportTest.kt | Report/status/export/privacy unit tests |
| app/src/androidTest/.../PhoneLayoutTest.kt | Six report layout/action checks alongside existing 24 |
| app/src/androidTest/.../ReportStorageTest.kt | Persistence, sanitized files and provider boundaries |

Backend, SDK collection, scenario generation, runner transport and outcome interpretation rules were not changed. The running runner's default serial was updated to the newly discovered phone port without editing `.env`.

## Validation and real-phone evidence

Both `:app:assembleDebug` and `:demo-shop:assembleDebug` succeeded. Unit tests: 21 passed. Phone instrumentation covers 360/393/430dp at 100% and 130% text, sticky report actions, export menu, clipboard feedback, persistence and restricted FileProvider access, alongside existing core screen checks.

An initial complete instrumentation run passed all 32 tests. A later Phase 7 rerun was interrupted by wireless ADB disconnect after eight passing cases. Phase 8 installed the final safeguards and completed all 33 device UI tests (including the new landscape check), 25 unit tests, report restoration, measured reproduction/fix verification and native developer-package export. See [final hardening validation](phase8-hardening.md).

Physical phone: OPPO CPH2477, Android 12, wireless ADB `DEVICE_SERIAL`. The original six saved incidents survived migration. A new incident `RPA-BC305AAE` was captured/analyzed, reproduced, reported, then verified with the actual fixed-auth DemoShop branch.

- Reproduction: `exec-978e1f5ff0ca45e8a882fc44efd8d3a7`, ADB, 31.3s, all three failure assertions matched, HTTP401 / TOKEN_EXPIRED / PAYMENT_FAILED.
- Verification: `exec-21bde773f4f041c5b8e20be52fd78fb9`, same scenario ID, ADB, 31.9s, HTTP200 / TOKEN_REFRESHED / PAYMENT_SUCCESS. The three original failure assertions correctly remain unmatched.
- Report metadata persisted: report ID `94e698fe-619e-4165-9358-139ccb4a5e37`; status FIX_VERIFIED; category APPLICATION.
- Share text and each exported file opened Android ChooserActivity. No report was sent to an external recipient during validation.
- Reinstallation/restart restored report status and both recorded executions through Home's View report action.

Evidence: analysis (private local evidence: `../artifacts/phase7/analysis.xml`; excluded from GitHub), reproduction result (private local evidence: `../artifacts/phase7/reproduced.xml`; excluded from GitHub), reproduced report (private local evidence: `../artifacts/phase7/reproduced-report.json`; excluded from GitHub), verified result (private local evidence: `../artifacts/phase7/verified.xml`; excluded from GitHub), restored report UI (private local evidence: `../artifacts/phase7/restored-report.xml`; excluded from GitHub), verified JSON (private local evidence: `../artifacts/phase7/verified-report.json`; excluded from GitHub), Markdown (private local evidence: `../artifacts/phase7/verified-report.md`; excluded from GitHub), Text (private local evidence: `../artifacts/phase7/verified-report.txt`; excluded from GitHub), instrumentation output (private local evidence: `../artifacts/phase7/instrumentation.txt`; excluded from GitHub).

## Remaining scope

Landscape and text sizes above 130% remain unvalidated. Artifacts are omitted when no real attachment references exist. Files are kept in app-private storage; use the Share Sheet to save/send them to another app. Old overwritten executions cannot be recovered. GitHub/Jira, Office Kit and PDF are intentionally deferred.
