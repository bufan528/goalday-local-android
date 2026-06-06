# Empty entry action fix - 2026-06-06

Scope: local usability fixes found while continuing APK-parity work. This pass does not reverse protected APK code or add server/VIP behavior.

## Fixes

- Fixed empty-handbook and missing-diary entry states so the "add page" action actually renders `CreatePageDialog` before the screen returns from the empty-state branch.
- Fixed planner empty-book state with the same dialog rendering path.
- Made calendar plan-pool rows respond to normal taps by grabbing the task and explaining the next drop action, instead of exposing a no-op click target.

## Verification

- `./gradlew :app:testDebugUnitTest :app:assembleDebug`
