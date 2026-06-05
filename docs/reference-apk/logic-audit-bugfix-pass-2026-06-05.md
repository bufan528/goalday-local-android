# Logic audit bugfix pass - 2026-06-05

Scope: reviewed runtime logic around root navigation, fixed handbook/diary entry state alignment, hardened local backup operations, and normalized schedule dates across UI, ViewModel, repository, and persisted data reads.

Changes:

- Root back navigation no longer consumes Android back while the user is already on the home tab because a hidden book detail state exists.
- Book planner detail now returns to the book library first; non-planner book entry modes still return to home.
- Handbook and diary direct entries wait until the primary local book is selected before exposing interactive controls, preventing writes to a previously selected book.
- Backup restore/delete now only accepts direct children of the app backup directory.
- Backup delete and cleanup now fail loudly when file removal fails instead of returning a misleading success.
- Calendar "today" selection now uses the target date's day and is not clipped by the previously visible month.
- Schedule add, update, drag/move, repository insert, and persisted data reads now clamp days to the real length of their year/month.

Validation:

- `./gradlew :app:testDebugUnitTest`
- `./gradlew :app:compileDebugKotlin`
