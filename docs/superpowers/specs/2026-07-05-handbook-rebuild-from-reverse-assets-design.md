# Handbook Rebuild From Reverse Assets Design

## Goal

Rebuild the Goalday local handbook module around the newly reversed APK resources in `D:\Downloads\goalday_reverse`, prioritizing original-like operation, UI structure, and visual behavior for local personal use.

## Reference Inputs

- `res/layout/fragment_main_page.xml`: top tab bar plus ViewPager-style content area.
- `res/layout/fragment_schedule_inbook.xml`: in-book schedule page shell with date markers and list area.
- `res/layout/item_schedule_item_in_book.xml`: daily schedule row with a left date column and six task slots in two columns.
- `res/layout/fragment_diary_inbook.xml`: in-book diary page with date header, content list, and bottom picture/edit bar.
- `res/layout/fragment_plan.xml`: plan list with floating add and tip buttons.
- `res/layout/item_plan_item.xml`: swipe-reveal plan row with edit and delete actions.
- `res/layout/activity_plan_idea_center.xml`, `activity_topic_detail.xml`, `activity_target_detail.xml`: topic and target flows.
- `assets/topic_center_config.json`, `assets/cover`, `assets/topictarget`, `assets/rich_editor.js`, `assets/editor.html`, `assets/style.css`: local topic, cover, target, and editor content.

## Product Rules

- Keep all behavior local-only.
- Do not add login, VIP, payment, ads, or network dependency.
- Preserve existing local data where possible.
- The handbook tab is the main product surface, not a generic dashboard.
- Recreate reference information architecture before adding extra polish.

## Architecture

The app keeps the existing Android Compose stack and MMKV persistence, but replaces the current mixed handbook UI with a reference-shaped Compose handbook shell. A small adapter layer normalizes reversed topic config and local state into UI models. Existing schedule and diary storage remain the persistence source, while the new UI presents them in original-like pages.

## Handbook Surfaces

### Main Shell

The handbook entry shows a warm paper background, a compact top tab bar, and a pager-like body. Tabs map to:

- Plan: local plan list.
- Schedule: in-book schedule page.
- Diary: in-book diary page.
- Topics: topic center and target templates.

Back behavior returns from detail/editor surfaces to the handbook shell before leaving the handbook tab.

### Schedule

The schedule surface uses a date/weekday column and six task slots per day, arranged as two columns of three rows. Empty slots are tappable. Completed tasks render muted with strikethrough and can be toggled back.

### Diary

The diary surface uses an optional date header, a scrollable list of text/image/target blocks, and a bottom tool bar. The existing rich editor assets remain available for editing longer diary content, but the visible in-book shell follows the reversed layout.

### Plan

The plan surface uses a vertical list with right-side floating add and tip buttons. Plan rows support edit and delete actions through a right-reveal interaction or a stable fallback action area where Compose gesture conflicts make reveal unreliable.

### Topics And Targets

The topic center reads local reversed assets. Topic cards show cover, title, color, and link-to-schedule state. Topic details show target items and allow saving as a local handbook or importing selected targets into schedule/task slots.

## Testing

Unit tests cover:

- Topic config parsing from both reversed and local-clean-room shapes.
- In-book schedule row slot mapping.
- Plan swipe/action state decisions.
- Existing page-turn edge gestures.

Manual verification covers:

- App opens to the handbook shell.
- Tabs switch predictably.
- Schedule slot add/toggle works.
- Diary edit persists.
- Topic save/import uses local assets.
- Debug APK builds and is copied to `D:\Downloads`.
