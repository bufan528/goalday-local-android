# Deep Parity Delta After Target Detail Route

Reference APK: `docs/reference-apk/goalday-reference-base.apk`

Date: 2026-06-02

## Method

This pass rechecked the protected APK with non-invasive static analysis only:

- APK zip inventory
- `classes.dex` strings
- full APK strings for resource names
- asset inventory
- current repo code search

The APK still shows `assets/libjiagu*.so`; business class names are mostly not recoverable from strings. That means code-level copying is neither reliable nor appropriate. The usable evidence is resource/assets/module signals.

## New Evidence From This Pass

### APK-side signals

- The APK has strong print/export evidence:
  - `META-INF/androidx.print_print.version`
  - `ic_print`
  - `long_image_content`
  - bundled iText font/hyphenation resources
- The APK has richer diary item evidence:
  - `bg_diary_target`
  - `bg_diary_topic_target`
  - `diary_write_at`
  - previously identified `item_diary_img`, `item_diary_text`, `item_diary_target`, `item_diary_topic_target`
- The APK has richer schedule/widget visual evidence:
  - `ic_larger_widget_cur`
  - `ic_mid_widget_separator`
  - `ic_widget_add`
  - many colored `ic_widget_dot_*`
  - `schedule_empty_hints`
- The APK has settings/backup/menu density evidence:
  - `bg_setting_item`
  - `bg_setting_fontsize_menu`
  - `ll_backup`
  - setting menu foreground assets
- The APK has target-detail popup evidence beyond the route itself:
  - `target_detail_options`
  - `bg_pop_target_detail_option`
  - `bg_target_detail_date`

### Current app-side signals

- Target detail now has a full-screen overlay route with completion, note, deadline, and schedule actions.
- Topic catalog now has id/cover key/target key/category and longer local target lists.
- Diary now has structured Compose fields, image support, and persistent typed diary blocks for text/target/topic-target entries. `RichDiaryEditor` is still only defined and is not currently used by the diary page.
- Export/share now has an in-app long-image preview sheet for diary and handbook exports, plus save/share/print actions. It is still not a dedicated full route like the reference `LongImageDisplayActivity`.
- Backup/settings are functional but sparse: immediate backup, latest restore, and path display only.
- Widgets exist, but reference widget assets imply denser visual variants and more polished states.

## Updated Estimates

| Scope | Estimate | Why |
| --- | ---: | --- |
| Useful offline planner functionality | 76-82% | Local scheduling, templates, handbook, target detail, typed diary blocks, preview export, widgets, import, and backup exist. |
| Reference/video visual parity | 53-61% | Book shell, topic cards, target detail, diary item rows, and export preview improved; widget/settings and full export route still differ. |
| APK structure parity excluding account/VIP/pay/server | 65-71% | Major local modules exist, diary has item-level blocks, and export has preview/print actions; settings and widget polish remain incomplete. |
| Full APK parity including account/VIP/pay/server | 28-35% | Account/VIP/pay/login/coupon flows are still intentionally excluded for a local offline app. |

## Remaining Gap Ranking

| Rank | Area | Current Gap | Impact |
| --- | --- | --- | --- |
| 1 | Widget visual polish | Current widgets work; reference has mid/large/add widget assets and colored dot variants. | Medium-high: home-screen experience still simpler. |
| 2 | Settings/backup density | Current settings are sparse; reference has denser setting rows, backup menu, font size/menu assets. | Medium: less important than planner flow, but visible. |
| 3 | Diary editor polish | Diary now has persistent typed blocks; reference still points to richer editor chrome and text formatting. | Medium: directly visible in daily-use workflow. |
| 4 | Export route completion | Current app uses a preview sheet with save/share/print; reference has a dedicated display activity and stronger PDF evidence. | Medium: product polish gap remains, but core workflow exists. |
| 5 | Target detail completion | Route now exists; still lacks save-as-own and richer section/menu options. | Medium-low: core target editing works, but reference has more options. |
| 6 | Topic cover visual assets | Local cards are generated cover-style visuals; reference has real cover PNGs. | Medium-low: improved, but still not exact. |

## Next Build Recommendation

The diary model and export preview passes are now implemented. The next implementation should focus on widget/settings parity:

1. Polish compact/mid/large widget states and empty states.
2. Add richer backup/settings rows: backup history, restore confirmation, storage status, and font-size menu.
3. Add richer diary editor chrome and optional inline formatting.
4. Promote the current export preview sheet into a dedicated route if more preview controls are needed.

This should now move visual parity more than another small export tweak.
