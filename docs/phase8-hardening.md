# Phase 8: final hackathon hardening

The audit found four concrete gaps: no complete reset, no device/hooks preflight, no transferable scenario/package, and no final submission materials. The smallest plan retained the existing architecture and added those boundaries plus bounded recovery and documentation.

## Implemented

- Debug Home developer controls check readiness and reset the demo. Reset is blocked during execution, requires backend opt-in, verifies actual cleared DemoShop state, restores BUGGY mode, ends an active ReproAI session and preserves historical incidents.
- DemoShop debug mode displays BUGGY/FIXED and exposes Authentication retry behavior. Its debug-only provider supports the finite reset action; the release manifest has no automation provider or deep-link activity.
- `/preflight` checks health/mode, authorized device, both APKs, scenario and deterministic automation hooks before Android/CLI submission. Missing devices/APKs/hooks fail clearly; physical radio requests are unsupported. MOCK requires explicit confirmation.
- Reports export a sanitized developer ZIP and plain TestScenario JSON. Native Save to Files and Share file make artifacts usable by supported file-transfer software. The optional CLI validates the existing Pydantic schema and calls the existing API.
- Completed reports restore from Room. Persisted nonterminal execution is marked interrupted. Monitoring tolerates two transient polling I/O failures and fails on the third within the existing overall timeout; it never submits a replacement execution during recovery.
- Error screens provide Retry/Reconnect/Reset. Error badges take priority over stale result status. Logging avoids raw SDK text and analysis exception contents.
- The report export menu scrolls in landscape. Existing portrait layouts retain sticky actions; large-text and narrow-screen tests exercise reachability. Status uses text and color, touch controls use standard Material targets, and navigation icons retain descriptions.

## Test results

Backend: **33 tests passed**, including unavailable device, both missing APK cases, absent hooks, unsupported network strategy, malformed scenario/import, reset disabled, active execution blocked and actual reset-state confirmation. A dependency deprecation warning remains; it does not fail tests.

Android: **25 unit tests passed**, covering outcome interpretation, report history, nested sanitization, developer ZIP/schema and formatted phone/credential redaction. Debug and unsigned release builds pass for both apps. Release manifests were inspected; production automation is absent. No release APK was installed over the debugging app.

Device UI suite: **33 tests passed** on the final APK in 68.491 seconds: 360/393/430dp at 100/130% text, persistence/provider boundaries, and scrollable landscape export. Compose test overrides exercise those dimensions without changing the phone's global display settings.

Physical capture validated the failure sequence and ADB reproduction with three matched failure assertions. Historical incidents survived reset and reinstall. An additional wireless run failed during a real device connection drop; it was not reported as reproduced. This led to bounded monitoring recovery and corrected error labeling. See the final real-device evidence recorded below.

## Matrix and evidence boundaries

| Case | Validation |
|---|---|
| MOCK | Existing API/CLI run succeeds only with explicit `--allow-mock`; no reproduction proof |
| ADB BUGGY | Real capture and scoped ADB run: TOKEN_EXPIRED / HTTP 401 / PAYMENT_FAILED |
| ADB FIXED | Same-scenario verification; positive success evidence required independently of failed original assertions |
| Backend unavailable | Real phone configured temporarily to closed port 8002; displayed a bounded timeout error, then reconnected successfully after restoring 8000 |
| Device unavailable | Injected no-device preflight test; actual wireless drop observed |
| DemoShop missing | Injected missing-install preflight; user's APK was not uninstalled |
| Restart | Reinstall/fresh launch restores all nine incidents and five verified reports; no stale running screen |
| Share/export | Provider tests and real phone native export validation |
| Reset | Actual phone returned BUGGY/idle/cleared hooks; incident history preserved |

Raw validation files stay in ignored `artifacts/phase8/`; only reviewed screenshots are submission assets. [Screenshots](screenshots/README.md), [live/video script](demo-script.md), [Office Kit workflow](office-kit-workflow.md), [iQOO checklist](iqoo-validation-checklist.md), [submission checklist](submission-checklist.md).

The native document picker saved the real sanitized ZIP to local Download. The six-file archive opened on the laptop, its plain TestScenario passed Pydantic, and its original/verification results referenced the identical scenario: `exec-8f388fd0996b43d78b2f9b9e86b29bd2` (401) and `exec-51cd8f7396ef4149af6de9f8e10fab1d` (200). Android's native ChooserActivity opened for Share report; no recipient was selected or message sent.

A fresh capture through normal UI used the exact description “Payment failed after switching networks.” Final incident **RPA-F1E6817D** reproduced in 33.6 seconds (`exec-727167b96307445ba6879765f9361dba`) and verified the same scenario in 31.7 seconds (`exec-a68e5cd281574ad5be9ef7a67f653a54`). Its developer ZIP and standalone scenario were generated using both actual export actions, read as sanitized files on the laptop, and validated to match each other and Pydantic. Screenshots 06–09 show this final APK/incident; earlier capture screenshots show the same verified flow from the preceding capture.

Final state: the app-driven Reset demo completed; provider state confirmed BUGGY authentication, empty cart, IDLE payment, disabled transition and cleared events. Home reports READY / ADB on port 8000 with all nine incidents and five verified reports preserved. The temporary mock test server was stopped. Report ID `c2cee87b-4e35-4a6b-92a3-3fc98aa7aec7` retains FIX_VERIFIED and matching, independently scoped original/verification evidence. Presenter pacing/recording remains a submission preparation step; screenshot collection was not a timed live-pitch rehearsal.

## Timeouts

| Boundary | Limit |
|---|---|
| HTTP connect | 5 seconds |
| Android HTTP read / total call | 25 / 30 seconds |
| Progress receive / polling interval | 750 ms; polling fallback |
| Polling I/O recovery | At most two retries, 1 second apart |
| Preflight | 18 seconds overall |
| ADB subprocess | 10 seconds |
| Demo bridge / main-thread hook | 8 / 5 seconds |
| Payment simulation / scenario wait | About 1.5 / 2.5 seconds |
| Backend execution / Android monitoring | 180 / 190 seconds |
| CLI HTTP / execution polling | 25 / 190 seconds |

Cancellation is cooperative; an in-flight ADB action can finish before cancellation takes effect. Process death can leave a backend execution running independently; Android restores only received completed results.

## Security and privacy review

Backend uses `shell=False` and finite device commands/targets, not eval/exec or arbitrary model/imported shell text. Preflight enforces known package names. Imported files are bounded to 1 MiB and Pydantic-validated. The development backend is unauthenticated and unencrypted on HTTP; use localhost/USB or a trusted LAN. Reset defaults off and is an explicit demo environment option, not access control.

DemoShop's debug automation provider checks caller UID (shell/root/self); it and the navigation activity are absent from release. Launcher activities are exported as required. ReproAI's SDK receiver remains exported for cross-app telemetry and has no sender authentication: another app can spoof capture events. Such broadcasts are not production-trusted evidence; final ADB outcomes require independently measured execution-scoped provider state. Debug deep links should likewise only be used on trusted development devices.

Reports use an unexported FileProvider limited to the private reports directory, with temporary read grants. No broad storage permission is introduced; document-picker export requires the user's destination choice. Nested report/scenario/execution evidence passes the shared sanitizer. Tests cover Bearer/Authorization, emails, plain/formatted phones, password, apiKey, secret and token metadata. Redaction is heuristic: inspect unfamiliar formats before sharing. Original captures remain sensitive local data; app backup is enabled in the existing manifest, so use a dedicated demo phone/profile and assess backup policy before production.

Root `.gitignore` excludes environment secrets, build files, raw artifacts, logs and signing keys. The workspace has no Git repository initialized; no repository, prototype or video was published.

## Honest limits and future work

Current target is DemoShop. DEMOSHOP_DEMO_HOOK simulates the transition and payment service; physical Wi-Fi/cellular switching and production payments are unsupported. Current analysis is rule-based local inference, not an active LLM. OPPO validation does not establish iQOO/Office Kit compatibility. Final compatible hardware pairing, transfer, desktop opening, recording and team submission URLs remain in their checklists.

Production API authentication, signed telemetry, additional app adapters, LLM runtime and physical radio controls are future work, not additions to this phase.
