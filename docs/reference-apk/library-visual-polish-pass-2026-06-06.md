# Library visual polish pass - 2026-06-06

Scope: clean-room local visual polish for the handbook library and shelf entry surfaces. This pass does not reverse protected APK code, bypass packing, or add server/VIP behavior.

## Changes

- Added a subtle border to the library summary card so it reads as a paper note instead of loose text.
- Added page-type preview chips to the featured handbook cover.
- Added a shelf count pill to the bookshelf header.
- Added mini page-type tabs to shelf book covers so users can scan what each handbook contains.
- Added elevation to the "new handbook" shelf card.

## Verification

- `./gradlew :app:testDebugUnitTest :app:assembleDebug`
- `./gradlew :app:lintDebug`
