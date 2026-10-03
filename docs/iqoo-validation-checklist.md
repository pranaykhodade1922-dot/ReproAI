# Final iQOO device validation

Current test hardware is OPPO CPH2477 / Android 12. No iQOO device has been claimed as validated. Device information is detected at capture; do not replace it with hardcoded branding.

- [ ] Record actual iQOO model, Android/OriginOS version, screen dp and font scale.
- [ ] Install ReproAI and DemoShop debug APKs without deleting existing incidents.
- [ ] Launch both; confirm safe system-bar insets and BUGGY/FIXED debug indicator.
- [ ] Start session; check timer, SDK event ingestion, network/orientation/app-state signals.
- [ ] Configure laptop LAN host; health reports ADB, then preflight reports READY.
- [ ] Confirm one selected authorized device; verify current wireless port or USB serial.
- [ ] Capture payment failure within the analysis window and enter the exact demo description.
- [ ] Show inferred diagnosis separately from actual 401/token-expiry evidence.
- [ ] Run reproduction; measure BUG REPRODUCED from the scoped current execution.
- [ ] Enable FIXED behavior; Verify Fix with the same scenario; confirm 200/refresh/success and unchanged failure assertions.
- [ ] Open report; confirm real iQOO environment, both execution IDs and before/after.
- [ ] Restart app; reopen saved incident/report and confirm stable report identity.
- [ ] Copy summary; open Share Sheet; export valid JSON, Markdown and Text.
- [ ] Export developer ZIP and standalone scenario; save locally through the document picker.
- [ ] Pair official Office Kit, locate the file in native File Manager, transfer to laptop and open/extract it.
- [ ] Validate exported scenario through `python -m app.cli validate`; confirm exact backend schema.
- [ ] Test Reset demo: BUGGY, zero cart, IDLE payment, cleared events/transition; incidents remain.
- [ ] Test portrait 100%/130% text and landscape: scrolls, actions reachable, no crash.
- [ ] Test disconnect/reconnect and clear recovery messages; never silently fall back to MOCK.
- [ ] Capture clean screenshots and a backup real-device recording, hiding private connection identifiers and unrelated notifications/files.

Record pass/fail evidence and date in [the hardening matrix](phase8-hardening.md). Office Kit availability and behavior must be checked for the actual model/OS/region.
