# Hackathon submission checklist

- [x] Final debug APKs build and install; release APK builds reviewed (unsigned until a signing key is configured).
- [x] Backend pytest (62) and Android unit tests (40) pass in the final audit; the latest documented physical phone UI suite passed 35 tests. Repeat phone checks after the final UI fixes when the device is online.
- [x] Full capture/analyze/buggy/fixed/report ADB flow completed on the phone.
- [ ] Presenter rehearses 3–5 minute pacing and records the compressed video.
- [x] Reset/preflight/recovery verified; portrait and landscape checks complete.
- [x] [GitHub repository](https://github.com/pranaykhodade1922-dot/ReproAI) exists; the workspace is a Git repository.
- [ ] Final audited README, website copy and Android UI fixes reviewed and committed by the team.
- [x] README opens with the core product, architecture, setup and limitations.
- [x] [Prototype](https://repro-ai-mu.vercel.app/) and [demo release](https://github.com/pranaykhodade1922-dot/ReproAI/releases/tag/v1.0.0-demo) URLs supplied and reachable.
- [ ] Published ReproAI APK aligned with the current typed healthy-state Verify Fix implementation; the existing release APK predates that implementation. DemoShop matches the validated build.
- [ ] Live website updated with the audited 62/40/35 counts and healthy-profile wording; current deployed copy is stale.
- [x] [Shortened demo video](https://youtube.com/shorts/72w0FL3EjJw?feature=share) URL supplied and reachable.
- [ ] Backup recording available; presenter confirms final recording content and submission duration requirements.
- [x] Nine clean screenshots present in `docs/screenshots/` and reviewed for private identifiers.
- [ ] Recapture current Verify Fix assertion rows when the phone is online; the existing Phase 8 screenshot is retained and explicitly labelled historical.
- [ ] Office Kit workflow demonstrated on supported iQOO and recorded; workflow-level integration labelled honestly.
- [x] Actual iQOO validation checklist completed; OPPO checks are not presented as iQOO checks.
- [x] Developer ZIP/report JSON/Markdown and standalone scenario validated on laptop; Text export covered by exporter/provider tests.
- [x] Sanitizer tests cover credentials, emails, phones and nested secret metadata.
- [x] Tracked-file and APK scan found no credential, private-IP or personal-path candidates; `.env`, local SDK paths, APKs, keystores, databases and raw artifacts remain excluded from Git.
- [x] Debug automation absent from release manifests; demo reset control hidden/blocked in release.
- [x] Known limitations, rule-based analysis, MOCK and DEMO_HOOK boundaries documented.
- [ ] Presenter follows [demo-script.md](demo-script.md); setup/account pairing stays outside the pitch.

Repository, video and prototype URLs are team-provided submission fields. They are not fabricated or automatically published by this phase.
