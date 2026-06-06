# Target Detail Diary Link Pass - 2026-06-06

## APK Evidence Used

- The reference APK exposes `activity_target_detail` and `target_detail_options`.
- Diary item resources include target-specific rows such as `item_diary_target`, `item_diary_target_child`, and in-book variants.

## Local Changes

- Added a target-detail action that writes the current target into the first diary page as a structured target diary block.
- Reused the existing local diary block encoding so the new block participates in diary-to-schedule synchronization.
- Added the action to the target detail option panel beside save-as-own, review, and execution-note actions.
- Preserved the offline-only data model and did not add server or account dependencies.

## Remaining Delta

- The reference app likely has a native target-detail option popup; the local app uses a Compose option panel.
- Target detail still needs more exact visual treatment and route-level transitions if pixel-level matching is required.
