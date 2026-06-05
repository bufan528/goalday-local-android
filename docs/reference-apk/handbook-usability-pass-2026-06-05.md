# Handbook Usability Pass - 2026-06-05

Reference APK signals used for this pass:

- `BookActivity`
- `fragment_schedule_inbook`
- `fragment_diary_inbook`
- `TargetDetailActivity`
- `item_diary_text`
- `item_diary_img`
- `item_diary_topic_target`

Implemented locally:

- Fixed a handbook/diary entry-mode blank-screen path by always opening the local primary handbook instead of returning when another book was previously selected.
- Added a persistent in-handbook page dock with previous/next actions, current page label, page count, and section switching.
- Synchronized the active handbook section when opening a page from the index, preventing mismatches such as a target page showing under the diary section.
- Clamped visible page numbering for entry-mode transitions so stale selected-page state cannot display out-of-range progress.
- Added diary browse-mode quick actions for edit, text block, image block, and topic target block.
- Preserved the local-only model: no server, account, VIP, payment, or remote sync was added.

Known remaining gaps:

- The book content pages still need deeper visual parity with the reference APK's dedicated schedule/diary/target fragments.
- Diary editing still needs drag ordering and richer inline block controls.
- Schedule and target pages still contain some generic Compose panel structure that should be replaced with denser in-book rows.
