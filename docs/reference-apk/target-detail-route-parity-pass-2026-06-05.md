# Target detail route parity pass - 2026-06-05

Reference signals:

- `TargetDetailActivity`
- target option/date panels
- in-book target rows that open a dedicated detail surface

Changes:

- Reworked the local target detail overlay from a centered modal card into a full-screen route-like surface.
- Added a persistent top bar with back action, route label, and target status.
- Added a stronger target hero block with page/title/index metadata, status badge, and compact metrics.
- Grouped actions, notes, deadline selection, and schedule history into dedicated detail panels.
- Added extra local-only detail actions for next step, review, and execution sentence generation.
- Increased visible schedule history from 8 to 10 rows.
- Clamped target deadline metadata to the current month length when reading and writing local state.

Validation:

- `./gradlew :app:compileDebugKotlin`
- `./gradlew :app:testDebugUnitTest`
