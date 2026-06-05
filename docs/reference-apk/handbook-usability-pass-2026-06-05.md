# Handbook usability pass - 2026-06-05

Scope: clean-room comparison against the bundled reference APK, focused on the local handbook and diary entry points.

## Findings

- The local handbook/detail route was rendering the root book header above the physical handbook UI. The APK-style handbook surface uses its own chrome, so the extra header reduced the usable page area and made the diary/editor surface feel broken.
- The handbook and diary shortcuts reused the last stored page index. Entering these shortcuts could land on a target or plan page before the UI corrected itself, which made the route look inconsistent.
- Forced diary/planner segments were updating page state during Compose composition. This could create visible jumps and made the detail route harder to reason about.

## Changes

- Keep the root book header on the home, inspiration, and planner/library surfaces, but hide it on the dedicated handbook and diary surfaces.
- Align the handbook shortcut to the first schedule/plan page and the diary shortcut to the first diary page when entering that route.
- Move forced segment page alignment into `LaunchedEffect`, so the route selection runs as a side effect instead of mutating state during composition.

## Verification

- `./gradlew :app:compileDebugKotlin`
- `./gradlew :app:testDebugUnitTest`
- `./gradlew :app:assembleDebug`
