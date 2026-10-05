# Office Kit: phone-to-laptop developer workflow

ReproAI exports standard files; Office Kit transports and displays them. It is not part of the execution engine and no undocumented API, SDK or cloud service was added to ReproAI.

Official references: [vivo Office Kit download and features](https://pc.vivoglobal.com/) and [iQOO connectivity information](https://www.iqoo.com/en/originos). Office Kit's official file-transfer guidance describes dragging files from the phone's native File Manager or Albums inside Phone Mirroring to a PC folder. Support depends on the phone, OS version, region and Office Kit installation. Verify it on the actual iQOO before claiming integration is demonstrated.

## Supported product flow

1. Open the incident report in ReproAI.
2. Tap **Export → Export developer package**.
3. Choose **Save to Files** and save `reproai-RPA-…-developer-package.zip` in a local folder such as Downloads using Android's document picker.
4. On a supported iQOO, connect Office Kit to the laptop using its official pairing/account flow. Use the native File Manager in Phone Mirroring to locate the saved ZIP.
5. Drag/transfer the ZIP to the laptop using Office Kit's supported file-transfer UI. Extract it and open `incident-report.md` in VS Code or a text viewer.
6. Validate the exported `test-scenario.json` with the backend CLI. It is the same schema used by the existing API, not a second generated test.

Do not assume Office Kit appears as a Share Sheet target on every device. Saving through Android's document picker makes the files available to the native file manager independently of Share Sheet support. Alternatively choose **Share file** when the device offers a suitable installed transfer app. No recipient is chosen automatically.

## Package contents

```text
RPA-…/
  incident-report.md
  incident-report.json
  test-scenario.json          # when analysis/scenario exists
  execution-result.json       # when reproduction exists
  verification-result.json    # when verification exists
  README.txt
```

All files pass the shared sanitizer, including nested runner evidence. Missing artifacts are omitted. No screenshot is invented or automatically added. Standalone **Export TestScenario JSON** saves `reproai-RPA-…-test-scenario.json` through the same Save to Files / Share file flow.

## Laptop import

From `backend/`:

```powershell
.\.venv\Scripts\python.exe -m app.cli validate 'C:\path\RPA-…\test-scenario.json'
.\.venv\Scripts\python.exe -m app.cli run 'C:\path\RPA-…\test-scenario.json' --runner http://127.0.0.1:8000
```

`run` uses the existing runner API after preflight. MOCK requires explicit `--allow-mock`; it does not prove reproduction. Fixed-path runner status FAILED is expected when original failure assertions no longer match; inspect the measured success evidence and Android's verification result.

## Validation boundary

Current physical validation uses iQOO, but Office Kit validation remains pending. The final iQOO transfer, native File Manager visibility, desktop opening and pairing remain items in [the device checklist](iqoo-validation-checklist.md). A USB/ADB development transfer is a fallback, not evidence of Office Kit usage. Do not show vendor account credentials or unrelated personal files in the recording.
