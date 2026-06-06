# Diary Focused Block Editor Pass - 2026-06-06

## APK Evidence Used

- The reference APK exposes a rich editor bundle: `assets/editor.html`, `assets/rich_editor.js`, `assets/style.css`, and `assets/normalize.css`.
- The reference resource names include first-class diary item variants: `item_diary_text`, `item_diary_img`, `item_diary_target`, `item_diary_target_child`, and `item_diary_topic_target`.

## Local Changes

- Added a focused block state to the local structured diary editor.
- Highlighted the selected diary block so editing has an explicit current item.
- Added a block-level toolbar for style changes and fast insertion after the selected block.
- Supported inserting text, target, child target, and topic-target blocks directly after the selected item.
- Kept the existing local raw diary serialization format and offline-only storage.

## Remaining Delta

- The WebView rich editor is still optional chrome around the native structured blocks, not the whole diary editing pipeline.
- Exact APK item-row visuals, stickers, and animation timing still need visual comparison on device.
