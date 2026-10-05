# ReproAI Hackathon Demo v1.0

ReproAI turns captured Android failures into executable reproduction scenarios and reruns the same scenario to verify the fix.

## Assets

- `reproai-demo-v1.0.apk`
  ReproAI Android developer tool

- `demoshop-demo-v1.0.apk`
  Instrumented DemoShop target application

## Requirements

- Android device
- ADB authorization
- Laptop running the Repro Runner
- Both APKs installed

## Validated Failure Classes

### Network / Authentication Retry

Failure:
TOKEN_EXPIRED → HTTP 401 → PAYMENT_FAILED

Fixed:
TOKEN_REFRESHED → HTTP 200 → PAYMENT_SUCCESS

### Rotation / State Restoration

Failure:
CHECKOUT_STATE_LOST → CHECKOUT_INVALID → PAYMENT_BLOCKED

Fixed:
CHECKOUT_STATE_RESTORED → CHECKOUT_VALID → PAYMENT_AVAILABLE

## Prototype Notes

- ADB reproduction and verification are real.
- Device rotation and Activity recreation are real.
- DemoShop payment/network behavior uses deterministic demo hooks.
- Physical Wi-Fi-to-cellular switching is not automated.
- Production payment systems are not integrated.
- Both currently supported bug classes have been physically reproduced and fix-verified on iQOO hardware running Android 16 / API 36.

## Source

https://github.com/pranaykhodade1922-dot/ReproAI
