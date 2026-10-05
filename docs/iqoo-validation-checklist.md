# iQOO Hardware Validation Record

Status: PASS
Device: iQOO / vivo I2407
Android: 16
API: 36

Bug 1:
BUG REPRODUCED 3/3
FIX VERIFIED 3/3

Bug 2:
BUG REPRODUCED 5/5
FIX VERIFIED 5/5

Historical test hardware included OPPO CPH2477 / Android 12.

- [x] Record actual iQOO model, Android/OriginOS version, screen dp and font scale.
- [x] Install ReproAI and DemoShop debug APKs without deleting existing incidents.
- [x] Launch both; confirm safe system-bar insets and BUGGY/FIXED debug indicator.
- [x] Start session; check timer, SDK event ingestion, network/orientation/app-state signals.
- [x] Configure laptop LAN host; health reports ADB, then preflight reports READY.
- [x] Confirm one selected authorized device; verify current wireless port or USB serial.
- [x] Capture payment failure within the analysis window and enter the exact demo description.
- [x] Show inferred diagnosis separately from actual 401/token-expiry evidence.
- [x] Run reproduction; measure BUG REPRODUCED from the scoped current execution.
- [x] Enable FIXED behavior; Verify Fix with the same scenario; confirm 200/refresh/success and unchanged failure assertions.
- [x] Open report; confirm real iQOO environment, both execution IDs and before/after.
- [x] Restart app; reopen saved incident/report and confirm stable report identity.
- [x] Copy summary; open Share Sheet; export valid JSON, Markdown and Text.
- [x] Export developer ZIP and standalone scenario; save locally through the document picker.
- [ ] Pair official Office Kit, locate the file in native File Manager, transfer to laptop and open/extract it.
- [x] Validate exported scenario through `python -m app.cli validate`; confirm exact backend schema.
- [x] Test Reset demo: BUGGY, zero cart, IDLE payment, cleared events/transition; incidents remain.
- [x] Test portrait 100%/130% text and landscape: scrolls, actions reachable, no crash.
- [x] Test disconnect/reconnect and clear recovery messages; never silently fall back to MOCK.
- [x] Capture clean screenshots and a backup real-device recording, hiding private connection identifiers and unrelated notifications/files.

Record pass/fail evidence and date in [the hardening matrix](phase8-hardening.md). Office Kit availability and behavior must be checked for the actual model/OS/region.
