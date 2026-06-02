# Deep Static Delta Audit

Reference APK: `docs/reference-apk/goalday-reference-base.apk`

Date: 2026-06-02

## Boundary

This pass uses non-invasive static APK inspection only: zip inventory, readable assets, and strings. The reference APK is packed/protected (`assets/libjiagu*.so` and `.jgapp`), so business code cannot be treated as available source. The local app must stay a clean-room offline implementation: no copied protected code, no copied paid/proprietary assets, and no payment/VIP/protection bypass.

## Tooling Reality

Available locally:

- `unzip`
- `strings`
- `file`

Not available in this environment:

- `jadx`
- `apktool`
- `aapt`
- `adb`

That means this audit can identify product structure, assets, strings, and resource signals, but it cannot reliably reconstruct original layouts or runtime behavior.

## Confirmed Reference APK Signals

### Protection and runtime

- APK is a zip archive.
- Protected/packed assets are present:
  - `assets/.jgapp`
  - `assets/libjiagu.so`
  - `assets/libjiagu_a64.so`
  - `assets/libjiagu_x64.so`
  - `assets/libjiagu_x86.so`

Impact: full source-level reverse engineering is not realistic with the current tooling. Product parity should be built from observed UX and asset/resource evidence.

### Topic center depth

APK evidence:

- `assets/topic_center_config.json`
- `assets/cover/*.png`: 33 cover images
- `assets/topictarget/*.txt`: 28 target-list files

The local app currently has:

- `app/src/main/assets/topic_center_config.json`
- 4 local `topictarget` files
- Compose-generated cover art, not bitmap covers

Gap: very large. The current topic center is a light clean-room skeleton compared with the reference catalog. Even if the workflow exists, visual density and content depth are far behind.

### Guide/onboarding

APK evidence:

- `GuideActivity` signal from previous string passes
- `assets/lottie/goalday.json`
- `assets/lottie/coupon.json`
- 18 files under `assets/lottie/`, including book/card/star/images

The local app currently has:

- Compose guide overlay
- Settings re-entry for guide
- No Lottie-style animation asset system
- No route-aware animated spotlight sequence

Gap: medium-large visually. Functionally there is guidance; visually it is still much simpler than the reference.

### Diary/editor system

APK evidence:

- `assets/editor.html`
- `assets/rich_editor.js`
- `assets/style.css`
- `assets/normalize.css`
- Previous resource/string signals:
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

The local app currently has:

- Persistent diary blocks
- Text, image, target, and topic-target concepts
- Rich editor web assets added locally

Gap: large. The local app has the block idea, but not the same specialized item model, nested child target rows, exact editor chrome, or dedicated diary route behavior.

### Schedule/calendar system

APK evidence from static/resource strings:

- `fragment_monthly_schedule`
- `fragment_schedule_inbook`
- `item_schedule_item`
- `item_schedule_item_adaptive`
- `item_schedule_item_in_book`
- `item_schedule_move_target`
- `style_schedule_day_form_adaptive`
- `style_schedule_repeat_hook`
- `style_schedule_repeat_item`
- `style_schedule_repeat_line`
- `change_time_all_future_schedules`

The local app currently has:

- Calendar page
- In-book schedule representation
- Drag/drop and move-to-day behavior
- Repeat data support
- Schedule widgets

Gap: medium-large. Core scheduling works, but the reference appears to have a more specialized split between monthly schedule, in-book schedule, adaptive rows, repeat editing, and future-repeat editing.

### Long image/export/print

APK evidence:

- `activity_long_image_display`
- `iv_long_image`
- `long_image_content`
- `PrintPage`
- `ic_print`
- `print_export_*`
- `shortcut_print_export_*`
- `androidx.print_print.version`

The local app currently has:

- Export preview sheet
- Save/share/print actions
- Android PDF print path

Gap: medium. The essential export action exists, but the reference has a dedicated route and stronger print/export surface.

### Widget system

APK evidence:

- `Theme.GoalDay.AppWidgetContainer`
- `Widget.GoalDay.AppWidget.Container`
- `Widget.GoalDay.AppWidget.InnerView`
- `app_widget_inner_view_background`
- `diary_add_widget_configure`
- `widget_schedule_add_color`
- `schedule_mid_widget`
- `schedule_larger_widget`
- `unlock_schedule_mid_widget`
- `unlock_schedule_larger_widget`
- `unlock_diary_add_widget`

The local app currently has:

- Small schedule widget
- Large schedule widget
- Quick diary widget
- Some style persistence for schedule widgets

Gap: medium. The local widgets exist, but the exact configure flow, preset hierarchy, unlock-state visuals, and reference styling are still not there.

### Settings and backup

APK evidence:

- `activity_setting`
- `ll_backup`
- `bg_setting_item`
- `bg_setting_fontsize_menu`
- `fg_setting_left_menu`
- `fg_setting_mid_menu`
- `fg_setting_right_menu`
- MMKV backup strings

The local app currently has:

- Settings screen
- Local backup/restore
- Backup history
- Font-size preference

Gap: medium. Local functionality exists, but the settings surface is still simpler and font-size/style behavior is not yet as fully integrated as a mature app.

### Account, VIP, coupon, payment

APK evidence includes account/VIP/coupon/payment strings and Lottie coupon assets.

These remain intentionally out of scope for the offline local app. They should not be copied, bypassed, or reimplemented as paid-feature removal.

## Corrected Gap Assessment

The current app is not close enough to claim video-level parity. The gap is still large, especially in visual systems and specialized flows.

| Scope | Current Estimate | Reason |
| --- | ---: | --- |
| Useful offline planner functionality | 82-88% | The local app can already create/use books, targets, schedules, diary blocks, widgets, backup, and export. |
| Reference/video visual parity | 55-65% | The biggest visual signals are still missing or simplified: bitmap topic covers, Lottie guide, exact diary row models, route-specific schedule pages, widget configuration, and long-image route. |
| APK structure parity excluding online/account/VIP/pay | 68-76% | Many local equivalents exist, but several are simplified Compose surfaces rather than dedicated reference-like flows. |
| Full APK parity including online/account/VIP/pay | 28-35% | Account, payment, VIP, coupon, and server ecosystems are intentionally excluded. |

## Biggest Remaining Differences

1. Topic catalog is too small and too synthetic.
   The reference has 33 cover images and 28 target files. Local has 4 target files and generated cover visuals. This is one of the most obvious reasons the app still feels unlike the video.

2. The handbook/book surface still needs a stronger physical-book product model.
   Page-turning exists, but the surrounding page hierarchy, row density, book chrome, and in-book components still read as a custom planner rather than the reference app.

3. Diary blocks are not specialized enough.
   The reference has separate item types for text/image/target/topic-target/in-book/child rows. Local blocks exist, but need more exact row components and editor toolbar behavior.

4. Monthly schedule and in-book schedule need to be split more clearly.
   The reference appears to have dedicated monthly and in-book fragments plus adaptive item rows. Local scheduling works but still feels consolidated.

5. Widget setup is incomplete.
   Widgets exist, but configuration, style/color selection, compact/mid/large presets, and unlock-state-style visuals are still missing.

6. Guide animation is too static.
   The reference uses Lottie/onboarding assets. Local guide is useful but not visually comparable.

7. Export/print is functional but not reference-like.
   Local print/export exists; the reference has a dedicated long-image display and print/export route.

## Next Build Order

### P0: Stop chasing tiny parity patches

The next work should be a targeted visual/system rebuild, not scattered fixes. The core app already functions; the user-visible gap is now product depth.

### P1: Rebuild topic center as a real local catalog

- Expand local clean-room topic config to 25-30 topics.
- Add generated/original bitmap-style cover assets or a stronger asset-backed cover system.
- Add one target text file per topic.
- Load topic metadata from assets instead of relying mainly on hardcoded templates.

### P1: Rebuild handbook page components

- Make the opened handbook feel like a physical book first, not a dashboard.
- Reduce generic cards inside pages.
- Build distinct row components for target, schedule, diary, topic-target, and done items.
- Keep page-turn engine, but make the page content denser and more reference-like.

### P1: Rebuild diary editor interaction

- Treat text/image/target/topic-target as first-class block rows.
- Add nested child target rows.
- Wire rich editor chrome where text editing actually happens.
- Add image block controls instead of image-only strips.

### P2: Split schedule surfaces

- Dedicated monthly schedule board.
- Dedicated in-book schedule page.
- Adaptive item row styling.
- Repeat editor with clear "all future schedules" behavior.

### P2: Widget configuration

- Add style/color configuration.
- Add compact/mid/large preset behavior.
- Add empty and locked-looking visual states only as local non-paid style states, not paywall bypass.

### P3: Guide and export polish

- Add clean-room animated guide visuals or lightweight Compose motion.
- Add dedicated full-screen long-image preview route.
- Add export preset/history if needed.

## Immediate Practical Conclusion

Yes, the gap is still big. The current local APK can be useful, but it still does not visually and structurally match the reference Goalday-style app. The next high-impact work is a larger rebuild of topic assets, handbook page components, and diary/schedule row systems, not another small bugfix pass.
