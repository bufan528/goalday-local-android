# Reference APK Gap Report

Reference APK: `docs/reference-apk/goalday-reference-base.apk`

Current app status after the schedule-time pass: local/offline planner with three root tabs, topic-style book covers, template center, topic detail surface, save-as-book flow, standalone target-detail-style book pages, handbook page turn, task pool, schedule todo/done, schedule time/repeat fields, rich diary editor, and backup settings.

## Overall Gap

Estimated completion against the reference APK's useful offline planner surface: **about 50-55%**.

This excludes account, VIP, payment, coupon, third-party login, webview marketing, and online commerce flows because the local app should not need them.

If those online/business flows are included, current completion is closer to **35-40%**.

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
| Topic center | Config-driven topic catalog, covers, target lists, topic detail | Template catalog plus topic-detail-style surface and save-as-book flow | Medium-low |
| Target detail | Dedicated target detail screen, checkbox/edit fields, save-as-own | Topic detail and book target pages now use structured target cards with completion, editing, deletion for custom items, and add-to-today schedule action | Medium |
| Schedule planner | Monthly schedule, day/week views, repeat/move target, calendar dialog | Local calendar, handbook drag, time fields, repeat labels, and local repeat expansion exist; system-calendar import still missing | Medium-high |
| Handbook/book | Book activity plus in-book schedule/diary fragments | Page-turn book exists but visual density and page types still rough | Medium-high |
| Diary | Rich editor, text/image/target/topic-target blocks, date picker, media picker | Rich text draft exists, media blocks missing | High |
| Calendar import | Dedicated system calendar import activity and permissions | Not implemented | High |
| Long image/print | Long image display and print/export page | Not implemented | High |
| Widgets | Diary add widget, mid/large schedule widgets | Not implemented | High |
| Backup/settings | Backup/settings activities | Local backup exists | Medium-low |
| Account/VIP/pay | Login/account/VIP/coupon/pay screens | Intentionally absent | Not required for local version |

## Priority Plan

1. **Calendar import**: implement optional Android system calendar import into the local schedule repository.
2. **Diary media blocks**: add image block support and date picker to diary pages.
3. **Long image export**: export handbook/diary pages as an image file.
4. **Widgets**: local schedule widget and quick diary add widget.
5. **Schedule polish**: add a richer repeat editor and per-occurrence edit behavior if needed.
6. **Target detail polish**: add richer note/deadline fields if the local workflow needs them.

## Immediate Next Fix

The largest remaining visible mismatch is now **calendar import + diary media blocks**. Local schedules have first-pass time and repeat behavior, but they still do not import Android system calendar events or support reference-style rich diary media blocks.
