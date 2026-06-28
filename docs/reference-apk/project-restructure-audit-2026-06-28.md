# Project Restructure Audit - 2026-06-28

This audit records the next large changes needed after the handbook visual baseline work.

## Current Shape

- `PageSurface.kt` is the highest-risk file at roughly 5.8k lines. It mixes page rendering, diary block editing, long-image export, image picking, route dashboards, and MMKV-backed export preferences.
- `BookHomeScreen.kt` is roughly 3.2k lines. It owns library, inspiration, handbook routes, reader chrome, dialogs, destructive actions, and target detail overlays.
- `BookViewModel.kt` is roughly 1k lines. It coordinates custom books, page scoped data, diary syncing, target metadata, schedule entry mutations, and route state.
- `LocalStateStore.kt` still has many raw MMKV keys and inline JSON decoders. Page title hashing is used for several scoped keys, so rename/delete paths must stay carefully guarded.

## Highest-Risk Areas

- Diary/handbook coupling: completed tasks are mirrored into diary text, and diary text can generate schedule entries. This needs focused tests before major editor changes.
- Page-scoped persistence: rename and delete flows migrate or remove diary, custom items, today plan/done, check state, and target metadata. These paths should be extracted and tested.
- UI file size: handbook, diary, target, schedule, export, and dialogs are still intertwined in large Compose files.
- Visual consistency: the new paper/book tokens exist, but older screens still carry hard-coded near-duplicate colors.

## Recommended Refactor Order

1. Extract small pure helpers from `BookHomeScreen.kt` and `PageSurface.kt`, with tests.
2. Split handbook route UI from `BookHomeScreen.kt` into route-specific files.
3. Split diary editor/export code out of `PageSurface.kt`.
4. Consolidate MMKV key generation and page-scoped migration/removal into a dedicated store helper.
5. Replace remaining hard-coded paper/book/route colors with `GoaldayDesign` tokens.

## This Pass

- Added `BookPageMetrics.kt` as the first small pure helper for page and route counts.
- Updated the handbook reading desk header to consume the extracted metrics.
- Added unit coverage for page metrics so future route/header refactors have a stable behavior baseline.
