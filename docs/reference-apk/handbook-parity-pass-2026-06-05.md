# Handbook Parity Pass

Reference APK: `docs/reference-apk/goalday-reference-base.apk`

Date: 2026-06-05

## Boundary

This pass uses clean-room implementation work only. It does not bypass APK packing, copy protected implementation code, or re-enable online/account/VIP/payment/server behavior.

## APK Evidence Driving This Pass

- Reference signals include `BookActivity`, `fragment_schedule_inbook`, `fragment_diary_inbook`, `fragment_monthly_schedule`, and dedicated item rows such as `item_schedule_item_in_book` and `item_diary_*_inbook`.
- Previous local implementation already had `GoaldayHandbookScreen`, but the main content still felt like a generic page reader embedded in a book frame.
- The weakest visible area remained the handbook shell and page hierarchy.

## Change Made

- Reworked the handbook interior into an open-spread surface:
  - left paper page: `BOOK ACTIVITY` index, book title/subtitle, local metrics, section directory, page tabs
  - center crease/spine
  - right paper page: current editable handbook route content
- Section navigation now has an in-book directory equivalent for `总览 / 日程 / 日记 / 目标`.
- Page navigation now has persistent in-book tabs with page type labels (`PLAN`, `SCHEDULE`, `DIARY`, `TARGET`).
- The existing editable schedule/diary/target behavior is retained on the right page.
- Fixed route dispatch so only schedule/plan pages use the dedicated in-book schedule spread. Diary and target routes now render their own page content instead of being forced through the schedule replica page.

## Remaining Gaps

1. The right page still uses shared Compose `BookReader` content, so exact XML/View row hierarchy parity is incomplete.
2. In-book schedule rows need closer `item_schedule_item_adaptive` and `item_schedule_move_target` visual behavior.
3. Diary rows need more exact `item_diary_img/text/target/topic_target` in-book chrome and editor pipeline.
4. Target detail should eventually become a stronger dedicated in-handbook route rather than only an overlay.

## Verification

- `:app:compileDebugKotlin` passed.
- `:app:testDebugUnitTest` passed.
- `:app:assembleDebug` passed.
- Latest APK copied to `/home/ubuntu/Downloads/goalday-local-debug-20260605-handbook-routes.apk`.
