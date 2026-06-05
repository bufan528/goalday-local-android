# Widget refresh and preview parity pass - 2026-06-05

Reference APK evidence used for this pass:

- `schedule_mid_widget`
- `schedule_larger_widget`
- `diary_add_widget_configure`
- `widget_schedule_add_color`

Implemented locally:

- The widget configuration screen now previews real local schedule entries instead of fixed sample rows.
- The preview respects the selected widget range and density: today, future 7 days, or current week.
- Empty preview states now mirror the actual widget empty messages.
- The schedule widget provider now listens for date, time, timezone, and locale changes and refreshes all local widgets, including large schedule and quick diary widgets.
- The quick diary widget date is refreshed through the same shared widget refresh path.

Known remaining gaps:

- The widget layout remains a clean-room Android RemoteViews implementation, not a copied reference XML/theme stack.
- Exact locked/unlocked resource states and bitmap-level widget chrome still need black-box screenshot comparison.
