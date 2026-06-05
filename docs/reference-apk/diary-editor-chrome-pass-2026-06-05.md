# Diary editor chrome pass - 2026-06-05

Reference signals:

- `DiaryActivity`
- `item_diary_text`
- `item_diary_img`
- `item_diary_target`
- `item_diary_target_child`
- `item_diary_topic_target`
- local rich editor bundle: `editor.html`, `style.css`, `normalize.css`, `rich_editor.js`

Changes:

- Reworked the diary editor toolbar into a route-like `DiaryActivity` header with date picker and done action.
- Added visible editor counters for text, image, and target rows.
- Split diary controls into block insertion and rich-text formatting controls.
- Added a direct target-child block insertion action.
- Added an explicit `StructuredDiary.withTargetChildBlock()` path, preserving the existing raw block format.
- Replaced the empty block editor hint with text/image/target placeholder rows, closer to the reference item-row model.

Validation:

- `./gradlew :app:compileDebugKotlin`
- `./gradlew :app:testDebugUnitTest`
