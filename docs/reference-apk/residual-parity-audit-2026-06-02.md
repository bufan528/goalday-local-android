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
- Schedule widgets, large schedule widget, and quick diary widget exist and refresh from local data. The latest widget pass adds richer gradient backgrounds, status pills, colored schedule dots, clearer empty states, and a stronger quick-diary add surface.
- Settings now has denser rows, backup storage status, backup history, restore confirmation, and a persisted font-size menu.
- Topic cover cards now share a local original cover-art system with category-aware palettes, book-spine treatment, cover stamps, and paper-line details across both inspiration entry points.

The remaining gap is now mostly product depth and polish, not missing core offline functionality.

## Remaining Gap Ranking

| Rank | Area | APK Evidence | Local App Status | Remaining Gap |
| --- | --- | --- | --- | --- |
| 1 | Guide/onboarding | `GuideActivity`, guide icons/views, Lottie assets | No dedicated guided first-run/tutorial layer | Need optional first-run guide for adding targets, schedule scrolling, diary writing, and handbook use. |
| 2 | Topic center asset completion | 33 cover PNGs, 28 target text files, bilingual topic config | Local templates now use a shared original category-aware cover-art system, but not bitmap cover assets or config parsing | Need closer config-driven topic loading and optional generated bitmap cover assets if visual delta remains obvious. |
| 3 | Widget configuration completion | `diary_add_widget_configure`, `widget_schedule_add_color`, mid/large widget resources, unlock widget signals | Widgets now have richer backgrounds, status pills, colored dots, clearer empty states, and quick-diary date/action chrome | Need an actual widget color/style configuration surface and more exact compact/mid/large presets. |
| 4 | Settings/backup completion | `activity_setting`, `bg_setting_item`, `bg_setting_fontsize_menu`, `ll_backup`, menu foreground assets | Settings now has denser rows, backup history, restore confirmation, storage status, and font-size menu | Need global application of font size and any exact setting menu art if required. |
| 5 | Diary editor polish | Rich editor bundle and nested target child diary item layouts | Persistent typed blocks exist; rich editor wrapper exists but is not wired into block editing | Need inline formatting toolbar, child target block nesting, and closer image/text/target row chrome. |
| 6 | Export route completion | `LongImageDisplayActivity`, `PrintPage`, print shortcut resources | Preview sheet supports save/share/print | Need a full-screen preview route only if zoom, print presets, or export history are added. |
| 7 | Target detail completion | `target_detail_options`, popup/date resources | Target detail overlay has core local editing | Need richer overflow options and save-as-own flow. |

## Updated Parity Estimate

| Scope | Estimate | Reason |
| --- | ---: | --- |
| Useful offline planner functionality | 78-84% | Core schedule, book, topic, diary, target detail, polished widgets, import, richer backup/settings, and export preview exist. |
| Reference/video visual parity | 59-67% | Main book/diary/export/widget/settings surfaces and topic cover visuals improved, but onboarding remains absent. |
| APK structure parity excluding account/VIP/pay/server | 69-75% | Most local module equivalents exist; remaining gaps are onboarding, config-driven topic loading, widget configuration, and finer editor polish. |
| Full APK parity including account/VIP/pay/server | 28-35% | Account/VIP/pay/login/coupon/server flows remain intentionally excluded from this offline app. |

## Recommended Next Implementation

1. Guide/onboarding pass:
   - Add optional first-run guide using local illustrations/animations rather than APK-copied assets.
   - Cover adding targets, schedule scrolling, diary writing, and handbook use.

2. Topic completion:
   - Move topic metadata closer to config-driven loading if needed.
   - Add generated bitmap cover assets only if the current code-native cover art still looks too generic.

3. Widget configuration completion:
   - Add a widget configuration surface or in-app widget settings section for color/style.
   - Add more exact compact/mid/large presets if needed.

4. Settings completion:
   - Apply persisted font-size preference globally if required.
   - Add exact setting menu art only if the visual delta remains obvious.

## Practical Conclusion

After the latest diary, export, widget, settings, and topic-cover changes, the app is close enough in core offline workflow that the next high-impact work should not be another diary/export tweak. The remaining visible difference is strongest in first-run guidance, with widget configuration and finer editor chrome behind that.
