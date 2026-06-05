# Parity gap recheck - 2026-06-05

Reference APK: `docs/reference-apk/goalday-reference-base.apk`

Boundary: static/readable APK evidence only. The local app remains a clean-room offline implementation. Account, VIP, coupon, payment, invite, server, and protection-bypass flows are intentionally excluded.

## Latest local passes included

- Calendar month grid and navigation dock.
- Settings backup/history cleanup.
- Home, topic center, handbook, diary in-book, target detail route, diary editor chrome, and long-image/print route passes.
- Logic fixes for root back handling, backup operations, and schedule date clamping.

## Remaining visible gaps

| Area | Reference signal | Current local status | Remaining gap |
| --- | --- | --- | --- |
| Widget add/config | `schedule_mid_widget`, `schedule_larger_widget`, `diary_add_widget_configure`, `widget_schedule_add_color` | Local has mid/large/diary widgets, color/style/scope/density configuration, and live preview | Need tighter config-route chrome and reliable preview/current-date refresh. |
| Diary editor | `DiaryActivity`, `item_diary_text/img/target/target_child/topic_target`, rich editor assets | Local has structured blocks, rich editor chrome, counters, child target insertion, image blocks | Exact row hierarchy and media picker behavior are still approximations. |
| Handbook pages | `BookActivity`, `fragment_schedule_inbook`, `fragment_diary_inbook` | Local has opened-book spread, page turn, in-book schedule, diary preview rows | Exact native row density and motion remain approximated in Compose. |
| Long image/print | `LongImageDisplayActivity`, `PrintPage` | Local has full-screen preview, presets, print panel, history, save/share/print | PDF/layout internals are local bitmap-based rather than reference iText pipeline. |
| Topic center | `topic_center_config.json`, 33 covers, target txt files | Local ships matching cover/target assets and config-style cards | Exact visual composition and animation still need black-box screenshot comparison. |
| Target detail | `TargetDetailActivity`, target option/date panels | Local has full-screen target detail route with notes/deadline/schedule/actions | Save-as-own and exact option rows are still approximations. |

## This pass

- Fixed the widget configuration preview date so diary-add widgets use the actual current day instead of a hard-coded sample date.
- Added route/config hints for the widget add flow so schedule mid, large, diary-add, and color configuration are clearly represented locally.
- Added quick diary widget refresh to the shared widget refresh path.

## Current estimate

- Useful offline planner functionality: 90-93%.
- APK structure parity excluding online/account/VIP/pay: 80-86%.
- Visual/video parity: 68-76%, pending real screenshot-by-screenshot comparison.
- Full APK parity including excluded business/server flows: still intentionally low, about 28-35%.
