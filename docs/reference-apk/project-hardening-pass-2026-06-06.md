# Project hardening pass - 2026-06-06

Scope: broad local stability review and targeted hardening. This pass keeps the app fully offline and does not reverse protected APK code, bypass packing, or add VIP/server behavior.

## Fixes

- Calendar month changes now clear grabbed/dragging task state and stale drop-zone bounds so tasks from the previous month cannot be accidentally dropped into the new view.
- Backup restore now clears existing safe MMKV files before copying the selected backup, avoiding stale local keys surviving a restore.
- Custom-book deletion no longer assumes the sample library is always non-empty; book selection now has a safe empty fallback.
- Rich diary editor no longer exposes `addJavascriptInterface`; editor changes now use a local custom URL bridge handled by `WebViewClient`.
- Moved `android:windowLightNavigationBar` into `values-v27` so minSdk 26 devices do not hit an API 27 style attribute.

## Verification

- `./gradlew :app:testDebugUnitTest :app:assembleDebug`
- `./gradlew :app:lintDebug`
