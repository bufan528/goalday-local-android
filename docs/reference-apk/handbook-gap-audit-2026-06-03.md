# Handbook Gap Audit

Reference APK: `docs/reference-apk/goalday-reference-base.apk`

Date: 2026-06-03

## Boundary

This audit uses only static, readable evidence from the APK zip inventory, strings, assets, and current local source code. The reference APK is protected/packed with `assets/libjiagu*.so`, so this project must remain a clean-room implementation. This audit does not copy protected code or paid assets, and it does not bypass VIP/payment/account/server behavior.

## Short Conclusion

The user's criticism is correct for the handbook area: the current app is usable in several offline flows, but the handbook still does not match the reference APK/video closely enough.

The main issue is structural. The reference APK appears to treat handbook, diary, in-book schedule rows, target detail, long-image display, print/export, widgets, and guide/onboarding as dedicated product surfaces. The current local app still uses a Compose book shell with segment filtering and shared board components. That makes the app functional, but it does not yet feel like the original handbook product.

## Static APK Evidence

Readable reference assets include:

- 33 cover PNG files under `assets/cover/`.
- 28 topic target text files under `assets/topictarget/`.
- `assets/editor.html`, `assets/rich_editor.js`, `assets/style.css`, and `assets/normalize.css`.
- 18 guide/onboarding files under `assets/lottie/`, including `goalday.json`, `coupon.json`, `book.png`, `card.png`, `img_0.png` to `img_8.png`, and star images.

Readable reference layout/string signals found in earlier and current audits include:

- Handbook/schedule: `fragment_schedule_inbook`, `fragment_monthly_schedule`, `item_schedule_item`, `item_schedule_item_adaptive`, `item_schedule_item_in_book`, `item_schedule_move_target`.
- Diary: `item_diary_img`, `item_diary_text`, `item_diary_target`, `item_diary_target_in_book`, `item_diary_target_child`, `item_diary_target_child_inbook`, `item_diary_topic_target`, `item_diary_topic_target_inbook`.
- Detail/export: `TargetDetailActivity`, `DiaryActivity`, `BookActivity`, `LongImageDisplayActivity`, `PrintPage`.
- Repeat/widget/settings: `fl_repeat`, `iv_repeat`, schedule repeat resources, `diary_add_widget_configure`, `schedule_mid_widget`, `schedule_larger_widget`, widget color/config resources.

## Current Local Implementation

Relevant current local code:

- `BookHomeScreen.kt` still drives handbook entry through `BookDetailView`.
- `BookSegment` is `日程 / 日记 / 清单`; handbook behavior is selected by filtering pages and forcing segments.
- `PageSurface.kt` contains `HandbookReplicaPage`, `ScheduleBoardMode.SPREAD / MONTH`, `TargetDetailReplicaPage`, `DiarySection`, and long-image preview dialogs.
- `DiaryBlockType` currently supports only `TEXT`, `TARGET`, and `TOPIC_TARGET`.
- Local assets now include 27 clean-room cover PNG files and 27 topic target files, but still no local `assets/lottie/` bundle and no `normalize.css`.

## Biggest Handbook Differences

1. The handbook shell is not dedicated enough.

   The reference product looks like a real handbook/book-first surface. The current app still behaves like a generic book detail page with mode tabs. The physical book effect exists, but the page model and navigation structure are not yet the same.

2. Diary rows are not first-class enough.

   The reference APK has separate item signals for image, text, target, target child, topic target, and in-book variants. Local diary blocks are stored as a small generic block model, with image URIs handled separately from typed blocks. This is a major reason the diary page still feels wrong.

3. In-book schedule is still a board, not a native handbook row system.

   Local `HandbookReplicaPage` has a useful monthly/spread board, drag behavior, preview, and export. But the reference APK signals dedicated in-book schedule fragments and item layouts. Local needs a row-based book schedule system, not only a board projection.

4. Target detail is still integrated into local rows/overlays.

   Local target rows have completion, detail text, schedule chips, note chips, and deadline chips. The reference APK still has stronger dedicated target detail activity/options/date resources. The local detail flow needs a separate route with matching hierarchy.

5. Long-image and print/export are not product surfaces yet.

   Local has preview/export dialogs. The reference APK has `LongImageDisplayActivity`, `PrintPage`, and print/export shortcut signals. Local needs a dedicated full-screen long-image route with presets and history.

6. Guide/onboarding is visually far away.

   Local uses a Compose guide overlay. The reference APK ships a Lottie/onboarding asset bundle. Current motion is not equivalent.

7. Widget configuration is still simplified.

   Local widgets exist and are useful, but the reference APK has richer add/config/color/mid/large/unlock-state resources. Widget parity remains incomplete.

8. Topic/cover assets are closer, but not exact.

   Local has 27 clean-room covers. The APK has 33 covers and a different exact topic set, including `50films`, `autumn`, `january_happiness`, `review`, `summer_vacation`, and `wish_list_2025`. Some local topics intentionally differ.

## Revised Parity Estimate

| Scope | Estimate | Reason |
| --- | ---: | --- |
| Offline planner usefulness | 88-92% | Home, local tasks, calendar, topic import, diary, handbook, backup, widgets, and export are reachable. |
| Overall video/reference visual parity | 62-70% | Many surfaces exist, but asset fidelity, handbook structure, diary rows, guide motion, and widget polish are still off. |
| Handbook-specific parity | 45-58% | This is the weakest area. The book look exists, but row models, route hierarchy, and page behavior are still not reference-like enough. |
| APK structure parity excluding account/VIP/pay/server | 72-80% | Most module equivalents exist, but many are implemented as shared Compose surfaces instead of dedicated reference-like flows. |
| Full APK parity including account/VIP/pay/server | 28-35% | Online/account/VIP/payment behavior remains out of scope by design. |

## Required Rebuild Order

### P0: Rebuild the handbook shell

- Introduce a dedicated `GoaldayHandbookScreen` instead of treating handbook as a forced `BookDetailView` segment.
- Make the first viewport a true book surface: full-screen spread, page stack edges, persistent top tabs, bottom page indicator, and physical paper pages.
- Remove dashboard-like card composition from the primary handbook surface.
- Keep data local and reuse existing repositories/view models where possible.

### P1: Split handbook content into reference-like row systems

- Add first-class diary row types: image, text, target, target child, topic target, and in-book variants.
- Split schedule rendering into monthly schedule, in-book schedule, and schedule item row components.
- Add a dedicated target detail route with options/date/repeat-style panels.

### P2: Promote export, guide, and widget surfaces

- Replace long-image dialogs with a dedicated full-screen preview/export route.
- Add print/export presets and export history.
- Build a richer guide sequence with local clean-room motion assets.
- Expand widget configure screens for diary add, schedule medium/large, color presets, and locked/unlocked visual states.

## Next Code Step

The next code step should not be another small visual patch. It should start P0: create a dedicated handbook route/screen and move the current handbook content into a book-first layout. After that, diary and schedule rows can be replaced one by one without breaking the rest of the app.
