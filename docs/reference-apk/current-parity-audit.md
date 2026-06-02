# Current Reference APK Parity Audit

Reference APK: `docs/reference-apk/goalday-reference-base.apk`

Date: 2026-06-02

Latest delta: `docs/reference-apk/deep-parity-delta-2026-06-02.md`

Latest residual audit: `docs/reference-apk/residual-parity-audit-2026-06-02.md`

## Boundary

This audit treats the APK as a product and UX reference only. The APK contains `assets/libjiagu*.so`, so direct code recovery is not a reliable or appropriate implementation path. The local app should continue as a clean-room, offline implementation: no copied APK code, no copied protected/paid assets, and no payment/protection bypass.

## Static Evidence Rechecked

- APK package/version already identified in `analysis.md`: `com.first.goalday`, version `2.5.7`, versionCode `56`.
- Protected/packed signal remains present: `assets/libjiagu.so`, `assets/libjiagu_a64.so`, `assets/libjiagu_x64.so`, `assets/libjiagu_x86.so`.
- Topic center is data-backed:
  - `assets/topic_center_config.json`
  - 33 `assets/cover/*.png` cover files
  - 28 `assets/topictarget/*.txt` target-list files
- Topic target files are substantially richer than the current local templates. Examples found:
  - `life.txt` is a long experience list, not just a 5-6 item preview.
  - `topicweek.txt` contains many themed weekly plans.
  - `five_year_diary_target.txt` is a long one-question-per-day style prompt set.
  - `2026.txt` is a full annual wish list.
- Diary editor evidence remains stronger than the current Compose block editor:
  - `assets/editor.html`
  - `assets/rich_editor.js`
  - `assets/style.css`
  - `assets/normalize.css`
- Print/export evidence remains stronger than the current preview-sheet flow:
  - iText PDF resources are bundled.
  - `PrintPage` and `LongImageDisplayActivity` are visible in previous manifest/resource-name extraction.
- UI resource-name signals still point to dedicated screens and item models:
  - `activity_plan_idea_center`, `activity_topic_detail`, `activity_target_detail`
  - `fragment_schedule`, `fragment_monthly_schedule`, `fragment_schedule_inbook`
  - `fragment_diary`, `fragment_diary_inbook`
  - `item_diary_img`, `item_diary_text`, `item_diary_target`, `item_diary_topic_target`
  - `item_schedule_item`, `item_schedule_item_adaptive`, `item_schedule_item_in_book`, `item_schedule_move_target`
  - `schedule_mid_widget`, `schedule_larger_widget`, `diary_add_widget`

## Current App Position

The local app is now usable offline and has equivalents for most major surface names: three root tabs, book/library, inspiration/topic center, target pages, handbook schedule, diary blocks, calendar import, widgets, backup, and long-image share.

The main remaining issue is not whether a feature name exists. The gap is that the reference APK has deeper data models, dedicated flows, and more specific item-level layouts.

## Updated Parity Estimate

| Scope | Estimate | Reason |
| --- | ---: | --- |
| Useful offline planner functionality | 76-82% | Core local scheduling, handbook, typed diary blocks, export preview, config-style templates, target detail overlay, widgets, backup, and import exist. |
| Reference/video visual parity | 53-61% | Opened-book shell, topic-cover cards, target detail route, diary item rows, and export preview improved, but widgets/settings still differ. |
| APK structure parity excluding account/VIP/pay | 65-71% | Many module names have local equivalents; diary has item-level blocks and export has preview/print actions, but settings/widgets are still simplified. |
| Full APK parity including account/VIP/pay/server | 28-35% | Those online/business flows are intentionally out of scope for the local app. |

## Biggest Remaining Gaps

| Priority | Area | Reference APK Signal | Current App | Remaining Gap |
| --- | --- | --- | --- | --- |
| P1 | Topic center visual assets | Config JSON, 33 covers, 28 target files | `InspirationTemplates` now has id/cover key/target key/category and longer local lists, but visuals are generated cover-style cards | Need stronger original cover illustrations or asset-backed cover model. |
| P1 | Target detail flow | Dedicated `TargetDetailActivity`, target detail item names/options | Target cards now open a full-screen detail overlay with status, note, deadline, and schedule actions | Need save-as-own behavior and richer per-target sections. |
| P1 | Diary polish | Rich editor bundle and diary image/text/target/topic-target item layouts | Compose structured diary with images and persistent text/target/topic-target blocks | Need richer editor chrome and inline formatting; image blocks still use the existing image strip. |
| P1 | Book page content parity | `BookActivity`, in-book schedule/diary fragments | Better shell and handbook schedule page | Need page contents to stop looking like generic panels, especially target and diary pages. |
| P1 | Monthly schedule split | `fragment_monthly_schedule`, schedule item variants | Calendar page plus handbook spread | Need a clearer monthly board/detail split and closer schedule-item density. |
| P1 | Print/export route | `LongImageDisplayActivity`, `PrintPage`, iText | Diary/handbook now open a long-image preview sheet with save/share/print and quick-save fallback | Need a dedicated full-screen route only if more preview controls are required. |
| P2 | Widgets visual parity | diary add, mid schedule, large schedule widgets | Widgets exist | Need visual polish, empty states, and closer compact layout. |
| P2 | Settings/backup density | `BackupActivity`, `SettingActivity` | Basic local backup/settings | Need backup history, restore confirmation, storage path/status, and denser settings rows. |

## Recommended Next Implementation Order

1. Polish widget visual states and compact/mid/large widget layouts.
2. Add richer settings/backup rows, backup history, restore confirmation, and storage status.
3. Add richer diary editor chrome and optional inline formatting.
4. Strengthen original topic cover illustrations or add a local asset-backed cover model.
5. Add target save-as-own behavior and richer per-target sections.

## Practical Conclusion

The current app is no longer "almost completely different"; it now has a similar offline product skeleton. But compared with the APK/video, it is still missing the reference app's widget/settings density and finer editor chrome. The next high-impact change should be widget/settings polish, not another small page-shell tweak.
