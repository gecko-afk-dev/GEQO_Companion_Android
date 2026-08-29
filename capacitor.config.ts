import type { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'com.mygeqo.companion',
  appName: 'GEQO Companion',
  webDir: 'www',
  // Phase 0: remote-loaded shell. GEQO_Frontend (the Admin Dashboard) is
  // pure Vue 3 CDN ESM with no build step, so rather than bundling a copy
  // of it into www/, the WebView loads the live dashboard directly. This
  // also means auth (ADR 017) and WebSocket live updates (ADR 015) work
  // unchanged — Capacitor's native JS bridge is still injected into the
  // remote-loaded page. See claude/native-app-planning-tracker.md in the
  // project for the full architecture writeup.
  server: {
    url: 'https://app.mygeqo.com',
    cleartext: false
  }
};

export default config;
