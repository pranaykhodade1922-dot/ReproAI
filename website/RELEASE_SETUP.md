# ReproAI Hackathon Demo Release

This document outlines the procedure for publishing release assets to GitHub Releases for hackathon judges and evaluators. 

> **Important**: [Demo release `v1.0.0-demo`](https://github.com/pranaykhodade1922-dot/ReproAI/releases/tag/v1.0.0-demo) is published. Its ReproAI APK predates the latest typed Verify Fix validation; do not treat it as the current validated build. Release updates require a separate review. Do not commit `.apk` binaries into normal Git history.

---

## 1. Suggested Release Metadata

- **Published Release Tag**: `v1.0.0-demo`
- **Suggested Release Title**: `ReproAI Hackathon Demo v1.0`

### Suggested Release Description

```markdown
# ReproAI Hackathon Demo

Autonomous mobile failure reproduction and regression verification on physical Android devices.

### Includes:
- **ReproAI Android app** (`reproai-demo-v1.0.apk`)
- **DemoShop instrumented target app** (`demoshop-demo-v1.0.apk`)

### Requirements:
- Android device (USB or wireless debugging enabled)
- ADB authorization
- Repro Runner (Python 3.12 + FastAPI) running on a laptop

For the complete reproduction and fix-verification workflow, install both APKs and follow the setup guide:
https://github.com/pranaykhodade1922-dot/ReproAI/blob/main/backend/README.md

### Known Prototype Limitations:
- DemoShop payment/network behavior uses deterministic demo hooks
- Physical Wi-Fi-to-cellular switching is not automated
- Production payment systems are not integrated
- Actual iQOO hardware validation remains pending
```

---

## 2. Release Asset Preparation Notes

Conceptual debug build outputs from Gradle:

- **ReproAI Host App**:
  `android-app/app/build/outputs/apk/debug/app-debug.apk`
- **DemoShop Target App**:
  `android-app/demo-shop/build/outputs/apk/debug/demo-shop-debug.apk`

### Preparation Steps:
1. For GitHub Release upload, copy the APKs outside the build tree and rename them:
   - `app-debug.apk` → `reproai-demo-v1.0.apk`
   - `demo-shop-debug.apk` → `demoshop-demo-v1.0.apk`
2. **Do not** change Gradle build logic just to rename artifacts.
3. **Do not** add APK files into `website/public/` or commit them to Git.

---

## 3. APK Pre-Release Safety Checklist

Before attaching and publishing APKs to GitHub Releases, verify:

- [ ] No hardcoded personal IP addresses
- [ ] No ADB device serial strings
- [ ] No Wi-Fi pairing codes
- [ ] No secret API keys
- [ ] No authentication tokens
- [ ] No hardcoded passwords
- [ ] No private `.env` values
- [ ] No local machine absolute file paths
- [ ] No private signing credentials or keystores
- [ ] No sensitive test or user data

---

## 4. Git Policy Reminder

APK files are distributed exclusively through GitHub Releases:
- `*.apk` is strictly ignored by `.gitignore`.
- Keep the Git repository lean, auditable, and focused on source code.
