# Home UI Parity Pass - 2026-06-05

Reference APK evidence used for this pass:

- `fragment_schedule`
- `fragment_monthly_schedule`
- `fragment_schedule_inbook`
- `assets/lottie/book.png`
- `schedule_mid_widget`
- `schedule_larger_widget`

Implemented locally:

- Reworked the home header into a visual Goalday hero using a bundled local guide/handbook image asset.
- Replaced the simple text title area with a date-aware local schedule hero.
- Added local status chips for offline, weekly plan, and drag/drop behavior.
- Restyled the planner header as `SCHEDULE ACTIVITY` with compact todo/done metrics.
- Replaced the four plain text navigation pills with a two-row action dock for calendar, inspiration, handbook, and diary.
- Kept the existing offline schedule interactions: quick add, day selection, drag to date, and drag to done.

Known remaining gaps:

- The main schedule board still needs deeper density and row-level styling to match the reference APK exactly.
- Bottom tab navigation is still Compose-native and should be visually tuned in a later pass.
- Exact widget/activity transitions are not replicated yet; this pass focuses on the first-screen visual gap.
