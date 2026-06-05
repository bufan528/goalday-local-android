# Handbook mobile UI pass - 2026-06-05

Scope: clean-room UI/useability pass for the local handbook route.

## Problems Addressed

- The handbook content was squeezed into a two-column spread with decorative book layers. On phone screens this left too little width for the actual editable schedule, diary, and target content.
- Page chips resolved pages by title, so duplicate or similar titles could route to the wrong page.
- The bottom dock duplicated the same section controls already shown at the top, increasing visual noise and reducing content height.
- The overview route did not behave like a useful entry point.

## Changes

- Rebuilt the handbook body as a single-column, full-width mobile surface.
- Added a real overview dashboard with metrics and direct entries for schedule, diary, and target sections.
- Switched page-chip navigation to real page indexes instead of title lookup.
- Simplified the bottom dock to previous/current/next controls only.
- Made any page-chip or bottom navigation action enter the matching content section.

## Verification

- `./gradlew :app:compileDebugKotlin :app:testDebugUnitTest :app:assembleDebug`
