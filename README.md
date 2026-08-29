# GEQO Companion (Android)

Native companion app for restaurant owners/cashiers on GEQO's Pro and Scale
tiers. It exists for one reason: **hardware access** — auto-printing kitchen
tickets to 80mm ESC/POS thermal printers (Xprinter, Rongta, Sunmi) over
LAN/TCP:9100 or Bluetooth SPP, which a browser cannot reach.

Full architecture, decisions, and phased roadmap live in the
`claude/native-app-planning-tracker.md` doc in the GEQO Claude Project — this
README stays a short pointer, not a duplicate of that document.

## What this is (and isn't)

This is a **thin Capacitor wrapper**, not a rebuilt frontend. The WebView
loads the live Admin Dashboard directly (`server.url` in
`capacitor.config.ts` points at `https://app.mygeqo.com`) instead of
bundling a copy of it, because the dashboard (`GEQO_Frontend`) is pure Vue 3
CDN ESM with no build step. Auth (ADR 017) and WebSocket live updates
(ADR 015) work unchanged inside the WebView — nothing about the dashboard's
business logic is duplicated here.

The only native code this repo will own is a small custom Capacitor plugin
(`GeqoPrinterPlugin`, added in Phase 1/2) wrapping
[DantSu/ESCPOS-ThermalPrinter-Android](https://github.com/DantSu/ESCPOS-ThermalPrinter-Android)
for the print bridge.

## Status: Phase 0 — wrapper only, no printing yet

- [x] `capacitor.config.ts` configured with `server.url` → `https://app.mygeqo.com`
- [x] Android platform scaffolded (`android/`)
- [ ] Verify the WebView loads the live dashboard and holds an authenticated
      session on a real sideloaded debug build
- [ ] Phase 1: `GeqoPrinterPlugin` — TCP:9100 only, manual print button
- [ ] Phase 2: auto-print on order acceptance, Bluetooth SPP pairing,
      offline print-job queue, server-side tier gate
- [ ] Phase 3: multi-printer-model hardening, Play Store rollout
      (Internal → Closed → Production)
- [ ] Phase 4: iOS — deferred until real market demand

## Local development

```bash
npm install
npx cap sync android
npx cap open android   # requires Android Studio / SDK
```

There is no local web build step — the app always talks to the live
`app.mygeqo.com` dashboard, so there's nothing to `npm run build` here.

## Decisions on record (see the planning tracker for full context)

- Print trigger: order **acceptance only** (single kitchen ticket), not a
  second print at dispatch/ready, for v1.
- Printer pairing is **per-device/station**, not a shared restaurant-wide
  pool, for v1.
- Sunmi hardware SKU (standalone printer vs. all-in-one POS terminal) is
  **not yet confirmed** — the Xprinter/Rongta path ships first; Sunmi
  support is a Phase 3 addition once the SKU is known.
