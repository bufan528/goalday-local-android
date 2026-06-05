# Handbook diary row parity pass - 2026-06-05

Reference APK evidence used for this pass:

- `fragment_schedule_inbook`
- `item_schedule_item_adaptive`
- `item_schedule_item_in_book`
- `fragment_diary_inbook`
- `item_diary_text`
- `item_diary_img`
- `item_diary_target_child`
- `item_diary_topic_target_inbook`

Implemented locally:

- Added a default in-book metadata rail to handbook schedule rows so todo/done entries read more like dense in-book list items instead of plain editable cards.
- The metadata rail surfaces time, repeat, note, completion state, and move availability without requiring the detail row to be expanded.
- Added diary block reordering controls in the local `DiaryActivity`-style editor so text, image, target, target-child, and topic-target blocks can be moved up/down.
- Preserved existing local-only storage and export behavior; no server, account, VIP, or payment behavior was added.

Known remaining gaps:

- The in-book rows are still clean-room Compose components, not copied XML `item_*` resources.
- Drag reordering for diary blocks is not implemented yet; this pass adds explicit up/down controls first.
- Exact page-turn timing, row spacing, and native animation parity still require device-side screenshot comparison.
