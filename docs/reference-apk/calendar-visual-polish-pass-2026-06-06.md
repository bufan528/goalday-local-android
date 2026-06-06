# Calendar visual polish pass - 2026-06-06

Scope: clean-room local visual polish for the calendar and execution-board surfaces. This pass does not reverse protected APK code, bypass packing, or add server/VIP behavior.

## Changes

- Added paper-like elevation to the calendar hero, month controls, month grid, and execution board cards.
- Improved month day cells with clearer selected/active states and compact todo/done count pills.
- Reworked time-slot drop rows with stronger slot badges, clearer empty/ready copy, and schedule metadata under assigned tasks.
- Reworked plan-pool rows into task cards with date pills, selected grab state, and clearer tap/drag affordance.

## Verification

- `./gradlew :app:testDebugUnitTest :app:assembleDebug`
- `./gradlew :app:lintDebug`
