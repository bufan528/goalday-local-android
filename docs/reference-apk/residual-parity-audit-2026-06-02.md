# Residual Parity Audit After Export Preview

Reference APK: `docs/reference-apk/goalday-reference-base.apk`

Date: 2026-06-02

## Boundary

This pass uses non-invasive static analysis only. The APK remains protected/packed, so this document treats it as product/UX evidence, not a source-code source. The local app should continue as a clean-room offline implementation.

## Static Evidence Rechecked

### Asset inventory

- `assets/topic_center_config.json` is a bilingual config object with `en` and `cn` roots.
- `assets/cover/*.png`: 33 cover images.
- `assets/topictarget/*.txt`: 28 target-list files.
- Rich editor bundle:
  - `assets/editor.html`
  - `assets/rich_editor.js`
  - `assets/style.css`
  - `assets/normalize.css`
- Lottie/onboarding-style assets: 18 files under `assets/lottie`, including `goalday.json`, `coupon.json`, `book.png`, `card.png`, `img_0.png` through `img_8.png`, and star images.

### Resource/string signals

Diary:

- `item_diary_img`
- `item_diary_text`
- `item_diary_target`
- `item_diary_target_in_book`
- `item_diary_target_child`
- `item_diary_target_child_inbook`
- `item_diary_topic_target`
- `item_diary_topic_target_inbook`
- `dialog_diary_date_picker`
- `diary_pic_mode`
- `diary_write_at`

Schedule/widgets:

- `schedule_mid_widget`
- `schedule_larger_widget`
- `diary_add_widget`
- `diary_add_widget_configure`
- `widget_schedule_add_color`
- `schedule_empty_hints`
- `unlock_schedule_mid_widget`
- `unlock_schedule_larger_widget`
- `unlock_diary_add_widget`

Settings/backup:

- `activity_setting`
- `bg_setting_item`
- `bg_setting_fontsize_menu`
- `fg_setting_left_menu`
- `fg_setting_mid_menu`
- `fg_setting_right_menu`
- `style_setting_pic`
- `ll_backup`

Export/print:

- `activity_long_image_display`
- `iv_long_image`
- `long_image_content`
- `PrintPage`
- `ic_print`
- `print_export`
- `print_export_1`
- `print_export_2`
- `shortcut_print_export_long`
- `shortcut_print_export_short`

Guide/onboarding:

- `activity_guide`
- `GuideActivity`
- `ic_guide_add`
- `ic_guide_finish`
- `ic_guide_next`
- `ic_guide_plan_scroll`
- `ic_guide_schedule`
- `ic_guide_schedule_scroll`
- `ic_guide_target`
- `ic_guide_tip`
- `view_guide_dairy`
- `view_guide_plan`
- `view_guide_schedule_scroll`

## Current Local App Position

Recent local changes improved the biggest visible gaps:

- Target detail route exists with completion, note, deadline, and schedule actions.
- Diary now has persistent typed blocks for text, target, and topic-target entries.
- Diary and handbook long-image export now have a preview sheet with save/share/print actions.
- Schedule widgets, large schedule widget, and quick diary widget exist and refresh from local data.

The remaining gap is now mostly product depth and polish, not missing core offline functionality.

## Remaining Gap Ranking

| Rank | Area | APK Evidence | Local App Status | Remaining Gap |
| --- | --- | --- | --- | --- |
| 1 | Widget configuration and polish | `diary_add_widget_configure`, `widget_schedule_add_color`, mid/large widget resources, unlock widget signals | Three widgets exist, but layouts are simple title/subtitle/text rows with no configuration screen | Need widget color/style configuration, richer empty states, compact row variants, and clearer diary-add widget behavior. |
| 2 | Settings/backup density | `activity_setting`, `bg_setting_item`, `bg_setting_fontsize_menu`, `ll_backup`, menu foreground assets | Settings page has offline note, backup, restore latest, and path display | Need backup history, restore confirmation, storage status, font-size menu, denser setting rows, and clearer destructive-action handling. |
| 3 | Topic center asset fidelity | 33 cover PNGs, 28 target text files, bilingual topic config | Local templates have ids/cover keys/target keys/categories and longer lists, but generated cover-style visuals | Need original asset-backed or newly generated cover illustrations and closer config-driven topic loading. |
| 4 | Guide/onboarding | `GuideActivity`, guide icons/views, Lottie assets | No dedicated guided first-run/tutorial layer | Need optional first-run guide for adding targets, schedule scrolling, diary writing, and handbook use. |
| 5 | Diary editor polish | Rich editor bundle and nested target child diary item layouts | Persistent typed blocks exist; rich editor wrapper exists but is not wired into block editing | Need inline formatting toolbar, child target block nesting, and closer image/text/target row chrome. |
| 6 | Export route completion | `LongImageDisplayActivity`, `PrintPage`, print shortcut resources | Preview sheet supports save/share/print | Need a full-screen preview route only if zoom, print presets, or export history are added. |
| 7 | Target detail completion | `target_detail_options`, popup/date resources | Target detail overlay has core local editing | Need richer overflow options and save-as-own flow. |

## Updated Parity Estimate

| Scope | Estimate | Reason |
| --- | ---: | --- |
| Useful offline planner functionality | 76-82% | Core schedule, book, topic, diary, target detail, widgets, import, backup, and export preview exist. |
| Reference/video visual parity | 53-61% | Main book/diary/export surfaces improved, but widgets/settings/topic covers/onboarding remain simpler. |
| APK structure parity excluding account/VIP/pay/server | 65-71% | Most local module equivalents exist; remaining gaps are configuration screens and asset-backed polish. |
| Full APK parity including account/VIP/pay/server | 28-35% | Account/VIP/pay/login/coupon/server flows remain intentionally excluded from this offline app. |

## Recommended Next Implementation

1. Widget parity pass:
   - Add richer widget backgrounds and row states.
   - Add colored schedule dots and better empty-state hints.
   - Add a widget configuration surface or in-app widget settings section for color/style.
   - Make quick diary widget route more explicit and closer to `diary_add_widget`.

2. Settings/backup parity pass:
   - Add backup history list.
   - Add restore confirmation dialog.
   - Add storage status and backup count.
   - Add font-size setting UI matching the APK `bg_setting_fontsize_menu` signal.

3. Topic/guide pass:
   - Replace generated topic cover cards with original generated bitmap cover assets or a stronger local asset system.
   - Add optional first-run guide using local illustrations/animations rather than APK-copied assets.

## Practical Conclusion

After the latest diary and export changes, the app is close enough in core offline workflow that the next high-impact work should not be another diary/export tweak. The remaining visible difference is that the reference APK feels like a finished product around the edges: configurable widgets, dense settings/backup, first-run guidance, and stronger topic cover assets.
