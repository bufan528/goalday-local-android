# Handbook in-book pages pass - 2026-06-05

Scope: clean-room reference APK parity pass for the handbook route.

## Reference Signals

Static APK evidence used for this pass:

- `fragment_schedule_inbook`
- `item_schedule_item_in_book`
- `item_diary_target_in_book`
- `item_diary_target_child_inbook`
- `item_diary_topic_target_inbook`

## Problems Addressed

- The handbook route only used the dedicated in-book renderer for schedule pages.
- Diary and target pages could still feel like generic pages inside a book shell instead of APK-style in-book rows.

## Changes

- Made the handbook route always use the book shell and handbook page-turn profile.
- Split `handbookMode` rendering by page type:
  - schedule/plan pages use the in-book schedule page;
  - diary pages use the in-book diary block layout with edit entry;
  - target pages use the in-book target dossier layout.
- Kept diary editing reachable from the in-book diary page instead of hiding it behind a separate route.

## Verification

- `./gradlew :app:compileDebugKotlin`
- `./gradlew :app:testDebugUnitTest :app:assembleDebug`
