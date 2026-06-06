# Home and inspiration boundary fix - 2026-06-06

Scope: local stability fixes found during continued project review. This pass keeps the app offline and does not reverse protected APK code or add server/VIP behavior.

## Fixes

- Home planner now clears grabbed tasks, dragging state, stale day drop bounds, and done drop bounds whenever the visible month changes.
- Standalone inspiration center now shows an actionable empty state if the local template list is empty instead of indexing into an empty list.
- Embedded handbook inspiration center now shows a returnable empty state if no templates are available.

## Verification

- `./gradlew :app:testDebugUnitTest :app:assembleDebug`
- `./gradlew :app:lintDebug`
