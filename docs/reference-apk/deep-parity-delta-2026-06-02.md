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
- Diary has structured Compose fields and image support, but `RichDiaryEditor` is only defined and is not currently used by the diary page.
- Export/share currently renders PNG long images directly; there is no dedicated preview screen and no PDF/print path.
- Backup/settings are functional but sparse: immediate backup, latest restore, and path display only.
- Widgets exist, but reference widget assets imply denser visual variants and more polished states.

## Updated Estimates

| Scope | Estimate | Why |
| --- | ---: | --- |
| Useful offline planner functionality | 72-78% | Local scheduling, templates, handbook, target detail, diary, widgets, import, and backup exist. |
| Reference/video visual parity | 46-54% | Book shell, topic cards, and target detail improved; diary/export/widget/settings still visibly simplified. |
| APK structure parity excluding account/VIP/pay/server | 60-66% | Major local modules exist, but diary, export/print, settings, and widget polish remain incomplete. |
| Full APK parity including account/VIP/pay/server | 28-35% | Account/VIP/pay/login/coupon flows are still intentionally excluded for a local offline app. |

## Remaining Gap Ranking

| Rank | Area | Current Gap | Impact |
| --- | --- | --- | --- |
| 1 | Diary editor/model | Current diary is structured fields; reference points to rich editor plus separate text/image/target/topic-target item models. | High: directly visible in daily-use workflow. |
| 2 | Export/print preview | Current app exports/shares immediately; reference has long-image display and print/PDF signals. | High: visible product polish and share flow. |
| 3 | Widget visual polish | Current widgets work; reference has mid/large/add widget assets and colored dot variants. | Medium: home-screen experience still simpler. |
| 4 | Settings/backup density | Current settings are sparse; reference has denser setting rows, backup menu, font size/menu assets. | Medium: less important than planner flow, but visible. |
| 5 | Target detail completion | Route now exists; still lacks save-as-own and richer section/menu options. | Medium-low: core target editing works, but reference has more options. |
| 6 | Topic cover visual assets | Local cards are generated cover-style visuals; reference has real cover PNGs. | Medium-low: improved, but still not exact. |

## Next Build Recommendation

The next implementation should focus on diary parity:

1. Add a persistent diary block model instead of relying on one raw structured string.
2. Add block types: text, image, target, topic-target.
3. Make the diary editor show block rows similar to the reference item model.
4. Wire the existing `RichDiaryEditor` into text blocks or replace it with a Compose toolbar if WebView assets are not bundled in this local app.
5. Keep existing image picker and target-link chips, but store them as real blocks.

This will likely move visual parity more than another small book-shell or topic-card pass.
