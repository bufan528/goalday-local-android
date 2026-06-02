# Reference APK Gap Report

Reference APK: `docs/reference-apk/goalday-reference-base.apk`

Current app status after the calendar-preview pass: local/offline planner with three root tabs, topic-style book covers, template center, topic detail surface, save-as-book flow, standalone target-detail-style book pages, handbook page turn, task pool, schedule todo/done, schedule time/repeat fields, Android system calendar import preview, structured diary editor with image blocks, target-linked diary chips, explicit diary date picker, diary and handbook schedule long-image export/share, small/large schedule widgets, quick diary widget, immediate schedule widget refresh hooks, and backup settings.

## Overall Gap

Estimated completion against the reference APK's useful offline planner surface: **about 91-93%**.

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
| Schedule planner | Monthly schedule, day/week views, repeat/move target, calendar dialog | Local calendar, handbook drag, time fields, repeat labels, local repeat expansion, and system-calendar import exist | Medium |
| Handbook/book | Book activity plus in-book schedule/diary fragments | Page-turn book exists but visual density and page types still rough | Medium-high |
| Diary | Rich editor, text/image/target/topic-target blocks, date picker, media picker | Structured diary pages support image blocks, mood/work/done/improvement sections, explicit date picker, and target-linked chips from today's plans/completions | Low |
| Calendar import | Dedicated system calendar import activity and permissions | Current-month Android CalendarContract import with preview and source calendar name exists from the calendar tab | Low |
| Long image/print | Long image display and print/export page | Diary long-image export/share and handbook schedule export/share exist; full print preview still missing | Low |
| Widgets | Diary add widget, mid/large schedule widgets | Today schedule widget, large schedule widget, quick diary widget, and schedule refresh hooks exist | Low |
| Backup/settings | Backup/settings activities | Local backup exists | Medium-low |
| Account/VIP/pay | Login/account/VIP/coupon/pay screens | Intentionally absent | Not required for local version |

## Priority Plan

1. **Schedule polish**: add a richer repeat editor and per-occurrence edit behavior if needed.
2. **Calendar import polish**: add selectable calendars and broader range import if needed.
3. **Diary polish**: add richer topic-linked blocks if needed.
4. **Long image polish**: add dedicated preview screen before share/export if needed.
5. **Widget polish**: add visual previews/configuration if needed.
6. **Target detail polish**: add richer note/deadline fields if the local workflow needs them.

## Immediate Next Fix

The largest remaining visible mismatch is now **schedule repeat polish + optional import filters**. Core local schedule, diary, import preview, export/share, widget surfaces, diary target links, and diary date controls exist, but the app still lacks a richer repeat editor and selectable calendar filters.
