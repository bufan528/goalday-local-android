# Home visual polish pass - 2026-06-06

Scope: clean-room local visual polish for the home planner surface. This pass does not reverse protected APK code, bypass packing, or add server/VIP behavior.

## Changes

- Added paper-like elevation to the home hero, planner board, and quick action dock.
- Strengthened the weekly theme input with a subtle border and paper fill.
- Reworked timeline day badges to read more like handbook page tabs.
- Reworked home task rows into cards with status pills and clearer completed state.
- Added light elevation to quick action cards so the entry grid feels less flat.

## Verification

- `./gradlew :app:testDebugUnitTest :app:assembleDebug`
- `./gradlew :app:lintDebug`
