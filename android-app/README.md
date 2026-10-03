# ReproAI - Autonomous Mobile Bug Reproduction Engine

ReproAI turns mobile bug reports into deterministic, reproducible test scenarios.

## Project Workspace Architecture

```text
DemoShop (com.pranay.demoshop)
   │
   ▼
ReproAI SDK (:repro-sdk)
   │ (Explicit Local Broadcast IPC: com.pranay.reproai.TRACK_EVENT)
   ▼
ReproAI Event Receiver (SdkEventReceiver)
   │
   ▼
Debug Session Manager & System Monitors (Network, Lifecycle, Orientation)
   │
   ▼
Room Local Database (repro_ai.db)
   │
   ▼
Interactive Timeline / Structured JSON Exporter
```

## Modules

- **`:app`** (`com.pranay.reproai`): Main ReproAI application featuring the active debug session monitor, Room persistence layer, interactive event timeline, and structured JSON exporter.
- **`:repro-sdk`** (`com.pranay.reproai.sdk`): Lightweight Android library exposing a clean tracking API (`ReproAI.trackScreen`, `trackAction`, `trackApiRequest`, `trackApiResponse`, `trackError`, `trackEvent`).
- **`:demo-shop`** (`com.pranay.demoshop`): Demo e-commerce application integrating `:repro-sdk` to simulate realistic payment flow and network transition token expiration bug scenarios.

## Manual Demo Testing Steps

1. Launch **ReproAI** -> Tap **START DEBUG SESSION**.
2. Launch **DemoShop** -> View Product ("Pro Wireless Earbuds") -> Tap **ADD TO CART**.
3. Proceed to Cart -> Tap **PROCEED TO CHECKOUT**.
4. On Checkout screen -> Toggle **Simulate network transition during payment** to `ON`.
5. Tap **PAY ₹2,499** -> Payment fails with `HTTP 401 Unauthorized`.
6. Return to **ReproAI** -> Tap **CAPTURE BUG** -> Enter "Payment failed during network switch".
7. Open **Failure Timeline** to observe combined real-time trace events with source tags:
   - `[DEVICE]` Network & Orientation state events
   - `[REPROAI]` Session & Navigation events
   - `[DEMOSHOP]` User actions (`PAY_BUTTON_CLICKED`), API requests (`/payment`), Token expiration (`TOKEN_EXPIRED`), and HTTP 401 errors.
8. Tap **EXPORT SESSION JSON** to inspect the machine-readable, sanitized session payload.
