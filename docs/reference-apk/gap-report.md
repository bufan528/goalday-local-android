# Reference APK Gap Report

Reference APK: `docs/reference-apk/goalday-reference-base.apk`

Current app status after the diary block-editor pass: local/offline planner with three root tabs, bookshelf-style library, featured physical handbook cover, expanded topic/template catalog, topic detail surface, save-as-book flow, standalone target-detail-style book pages, handbook page turn, book-spread schedule page with task pool/todo/done columns, schedule time/repeat fields, repeat interval and end-date controls, repeat group editing, Android system calendar range/source selection/import preview, structured diary editor with text/image/target block rail and image blocks, target-linked diary chips, explicit diary date picker, diary and handbook schedule long-image export/share, small/large schedule widgets, quick diary widget, immediate schedule widget refresh hooks, and backup settings.

Deep audit: `docs/reference-apk/deep-gap-audit.md`

## Overall Gap

Estimated completion against the reference APK's useful offline planner surface: **about 60-70%**.

Estimated exact visual/video parity: **about 35-45%**.

This excludes account, VIP, payment, coupon, third-party login, webview marketing, and online commerce flows because the local app should not need them.

If those online/business flows are included, current completion is closer to **25-35%**.

## Reference APK Structure Found

Manifest activities/modules:

- `GuideActivity`
- `MainActivity`
- `PlanIdeaCenterActivity`
- `TopicDetailActivity`
- `TargetDetailActivity`
- `BookActivity`
- `DiaryActivity`
- `RichTextDebugActivity`
- `CalendarImportActivity`
- `LongImageDisplayActivity`
- `PrintPage`
- `BackupActivity`
- `SettingActivity`
- Widget receivers/services for diary add, mid schedule, and large schedule
- Account/VIP/coupon/login/invite/payment related screens

Resource/layout signals:

- Main fragments: `fragment_main_page`, `fragment_main_tab`, `fragment_plan`, `fragment_plan_add`, `fragment_schedule`, `fragment_monthly_schedule`, `fragment_diary`.
- In-book variants: `fragment_schedule_inbook`, `fragment_diary_inbook`.
- Topic/target details: `activity_plan_idea_center`, `activity_topic_detail`, `activity_target_detail`, `item_topic_detail`, `item_target_detail`, many `target_*_*` fields.
- Schedule behavior: `item_schedule_item`, `item_schedule_item_adaptive`, `item_schedule_item_in_book`, `item_schedule_move_target`, `item_schedule_target`, `dialog_calendar`, `pop_repeat`.
- Diary/media: `item_diary_img`, `item_diary_text`, `item_diary_target`, `item_diary_topic_target`, rich editor assets, picture picker layouts.
- Export/widget: `activity_long_image_display`, `PrintPage`, `schedule_mid_widget`, `schedule_larger_widget`, `diary_add_widget`.
- Navigation: `ic_tab_book`, `ic_tab_calendar`, `ic_tab_setting`.

## Gap by Area

| Area | Reference APK | Current App | Gap |
| --- | --- | --- | --- |
| Root navigation | 3 tabs: book/calendar/setting | Now aligned to 3 tabs | Low |
| Topic center | Config-driven topic catalog, covers, target lists, topic detail | Expanded local template catalog plus topic-detail-style surface and save-as-book flow, but no full cover-image asset model | Medium |
| Target detail | Dedicated target detail screen, checkbox/edit fields, save-as-own | Topic detail and book target pages now use structured target cards with completion, editing, deletion for custom items, and add-to-today schedule action | Medium-high |
| Schedule planner | Monthly schedule, day/week views, repeat/move target, calendar dialog | Local calendar, handbook drag, book-spread schedule page, time fields, repeat labels, repeat interval/end-date expansion, repeat group editing, and system-calendar import exist | Medium-low |
| Handbook/book | Book activity plus in-book schedule/diary fragments | Bookshelf-style library, page-turn book, and book-spread schedule page exist, but diary/target pages still need parity work | Medium |
| Diary | Rich editor, text/image/target/topic-target blocks, date picker, media picker | Structured diary pages support text/image/target block rail, image blocks, mood/work/done/improvement sections, explicit date picker, and target-linked chips from today's plans/completions | Medium |
| Calendar import | Dedicated system calendar import activity and permissions | Android CalendarContract import supports current/3-month/6-month ranges, source selection, preview, and source calendar names from the calendar tab | Low |
| Long image/print | Long image display and print/export page | Diary long-image export/share and handbook schedule export/share exist; full print preview still missing | Medium |
| Widgets | Diary add widget, mid/large schedule widgets | Today schedule widget, large schedule widget, quick diary widget, and schedule refresh hooks exist | Medium-low |
| Backup/settings | Backup/settings activities | Local backup exists | Medium |
| Account/VIP/pay | Login/account/VIP/coupon/pay screens | Intentionally absent | Not required for local version |

## Priority Plan

1. **Target detail parity**: add richer note/deadline/section behavior and a dedicated full-screen target detail flow.
2. **Diary editor parity**: add true rich-text formatting and topic-target blocks.
3. **Book/handbook visual parity**: continue opened-book surface polish and reduce generic panel styling.
4. **Target detail parity**: add richer note/deadline/section behavior and a dedicated full-screen target detail flow.
5. **Diary editor parity**: add richer block editing for text/image/target/topic-target entries.
6. **Export/print parity**: add a dedicated preview screen before share/export/print.

## Immediate Next Fix

The largest remaining visible mismatch is now **book/handbook visual parity**. Core local functionality exists, but the current app still looks and feels too much like a generic Compose planner. The next implementation should rebuild the library and opened-handbook surfaces first.
