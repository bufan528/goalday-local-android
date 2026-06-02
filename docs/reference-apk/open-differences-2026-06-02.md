# Open Differences After Guide Overlay

Reference APK: `docs/reference-apk/goalday-reference-base.apk`

Date: 2026-06-02

## Method

This pass rechecked the protected APK and the current local repo after the recent diary, export, widget, settings, topic-cover, and guide passes.

The APK still remains a product/UX reference only. Protected code and paid/server behavior are not copied or bypassed.

## Current Local Status

Latest local commits now cover most offline-visible surfaces:

- Target detail overlay with completion, notes, deadline, and schedule actions.
- Persistent typed diary blocks for text, target, and topic-target entries.
- Diary/handbook long-image preview with save/share/print.
- Polished schedule widgets and quick diary widget.
- Denser settings/backup with backup history, storage summary, restore confirmation, and font-size menu.
- Shared original topic cover-art system across inspiration entry points.
- First-run guide overlay with settings re-entry.

## APK Evidence Still Not Fully Matched

### 1. Topic Center Config Depth

APK evidence:

- `assets/topic_center_config.json` with `en` and `cn` roots.
- `assets/cover/*.png`: 33 cover images.
- `assets/topictarget/*.txt`: 28 target-list files.

Current app:

- Uses code-defined `InspirationTemplates`.
- Has cover keys, target keys, categories, longer lists, and shared original cover art.

Remaining difference:

- The local app does not yet load a config-style topic catalog from assets/data.
- Local cover art is code-native Compose, not bitmap cover assets.
- Target text depth is improved, but not loaded from independent target files.

Impact: medium. The visible cover gap is much smaller now, but the APK still has a stronger data-driven catalog model.

### 2. Widget Configuration Flow

APK evidence:

- `diary_add_widget_configure`
- `widget_schedule_add_color`
- `schedule_mid_widget`
- `schedule_larger_widget`
- `unlock_schedule_mid_widget`
- `unlock_schedule_larger_widget`
- `unlock_diary_add_widget`

Current app:

- Has small schedule widget, large schedule widget, quick diary widget.
- Has richer backgrounds, status pills, colored dots, empty states, and refresh hooks.

Remaining difference:

- No widget configuration screen for color/style.
- No exact compact/mid/large preset selector.
- No per-widget customization state.

Impact: medium-low for offline use, medium for product polish.

### 3. Diary Editor Chrome And Nested Target Rows

APK evidence:

- `assets/editor.html`
- `assets/rich_editor.js`
- `item_diary_img`
- `item_diary_text`
- `item_diary_target`
- `item_diary_target_in_book`
- `item_diary_target_child`
- `item_diary_target_child_inbook`
- `item_diary_topic_target`
- `item_diary_topic_target_inbook`
- `diary_pic_mode`
- `diary_write_at`

Current app:

- Has persistent typed diary blocks.
- Supports text, target, topic-target blocks and images.
- Has a `RichDiaryEditor` wrapper, but it is not wired into block editing.

Remaining difference:

- No inline formatting toolbar for diary text blocks.
- No nested child target rows.
- Image blocks still use a separate image strip rather than the exact reference item model.

Impact: medium. The main diary workflow exists, but the editor still feels simpler than the APK.

### 4. Target Detail Option Depth

APK evidence:

- `activity_target_detail`
- `item_target_detail`
- `target_detail_options`
- `bg_pop_target_detail_option`
- `bg_target_detail_date`

Current app:

- Has a full-screen target detail overlay.
- Supports completion, note, deadline, scheduled entries, and quick schedule actions.

Remaining difference:

- No richer overflow option menu.
- No explicit save-as-own behavior from target detail.
- No exact popup/date visual treatment.

Impact: medium-low. Core local editing works; missing pieces are option depth.

### 5. Monthly Schedule Split

APK evidence:

- `fragment_monthly_schedule`
- `fragment_schedule_inbook`
- `item_schedule_item`
- `item_schedule_item_adaptive`
- `item_schedule_item_in_book`
- `item_schedule_move_target`
- `style_schedule_repeat_hook`
- `style_schedule_repeat_item`
- `style_schedule_repeat_line`

Current app:

- Calendar page, handbook spread, schedule drag/drop, repeat controls, import, and widgets exist.

Remaining difference:

- Monthly schedule is still less clearly split into a dedicated monthly board/detail module.
- In-book schedule is close but not an exact item-style match.

Impact: medium-low after recent handbook and schedule work.

### 6. Export Route Completion

APK evidence:

- `activity_long_image_display`
- `iv_long_image`
- `long_image_content`
- `PrintPage`
- `ic_print`
- `print_export_*`
- `shortcut_print_export_*`

Current app:

- Has a preview sheet with save/share/print.

Remaining difference:

- No dedicated full-screen long-image route.
- No export presets/history.
- Current print path is functional Android PDF output, not a dedicated print page UI.

Impact: low-medium. Core workflow exists; this is mostly route-level polish.

### 7. Guide Animation Polish

APK evidence:

- `GuideActivity`
- guide icons/views
- 18 Lottie/onboarding assets

Current app:

- Has a first-run guide overlay with local Compose illustrations and settings re-entry.

Remaining difference:

- No Lottie-like animation.
- No route-aware spotlight callouts.

Impact: low-medium. Guidance exists; animation polish remains.

## Updated Difference Ranking

| Rank | Area | Remaining Gap Type | Priority |
| --- | --- | --- | --- |
| 1 | Topic config/data loading | Structural data model | P1 |
| 2 | Diary editor chrome | User-facing workflow polish | P1 |
| 3 | Widget configuration | Product configuration polish | P2 |
| 4 | Target detail options | Option/menu depth | P2 |
| 5 | Monthly schedule split | Layout/module specificity | P2 |
| 6 | Export full route | Route/preset polish | P3 |
| 7 | Guide animation | Animation/spotlight polish | P3 |

## Updated Estimate

| Scope | Estimate | Reason |
| --- | ---: | --- |
| Useful offline planner functionality | 80-86% | The main local workflows are usable: topic, book, target, schedule, diary, widgets, backup, import, export, guide. |
| Reference/video visual parity | 61-70% | Cover art, book shell, diary rows, widgets, export preview, settings, and guide are improved, but exact assets/editor/config still differ. |
| APK structure parity excluding account/VIP/pay/server | 71-78% | Most local modules now have equivalents; remaining gaps are config loading, editor chrome, widget config, and detail menus. |
| Full APK parity including account/VIP/pay/server | 28-35% | Account/VIP/pay/login/coupon/server flows remain intentionally excluded. |

## Recommended Next Work

1. Make topic center more config-driven:
   - Move template metadata into a local JSON/data file.
   - Keep current clean-room text and cover art.
   - Add a loader that maps cover keys and target keys.

2. Polish diary editor chrome:
   - Add inline formatting controls for text blocks.
   - Add target child rows.
   - Treat image as a first-class block type rather than only an image strip.

3. Add widget configuration:
   - Add in-app widget style settings or Android widget configure activity.
   - Persist color/style choices.

4. Add target detail options:
   - Overflow menu.
   - Save-as-own action.
   - Richer date/options UI.

## Practical Conclusion

The app is now much closer to the reference in core offline behavior. The remaining differences are no longer "the app is totally different"; they are mostly data-driven depth, editor polish, configuration surfaces, and exact asset/animation fidelity.
