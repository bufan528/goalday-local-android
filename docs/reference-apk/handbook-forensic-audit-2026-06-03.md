# Handbook Forensic Audit

Reference APK: `docs/reference-apk/goalday-reference-base.apk`

Date: 2026-06-03

## Boundary

This audit uses only static evidence available from the APK package: zip inventory, readable assets, binary strings, and current local source. The APK contains `assets/libjiagu.so`, `assets/libjiagu_a64.so`, `assets/libjiagu_x64.so`, and `assets/libjiagu_x86.so`, and `res/` entries are obfuscated to short names. I did not bypass packing/protection, copy protected code, remove paid/VIP checks, or extract private implementation logic.

## Tooling Result

Available local tools:

- `unzip`
- `strings`
- `file`

Unavailable in this environment:

- `jadx`
- `apktool`
- `aapt`
- `apkanalyzer`

Because the APK is packed and resource names are obfuscated, source-level reverse engineering is not reliable here. The useful evidence is product structure, asset inventory, editor bundle, Lottie guide bundle, topic catalog, and surviving strings.

## Strong Static Evidence

### 1. The reference APK has a real asset-led handbook system

Reference APK:

- 33 cover PNG files in `assets/cover/`
- 28 topic target text files in `assets/topictarget/`
- `assets/topic_center_config.json`
- `assets/editor.html`
- `assets/normalize.css`
- `assets/rich_editor.js`
- `assets/style.css`
- 18 guide/onboarding files in `assets/lottie/`

Current local app:

- 27 cover PNG files
- 27 topic target text files
- `assets/topic_center_config.json`
- `assets/editor.html`
- `assets/rich_editor.js`
- `assets/style.css`
- no `normalize.css`
- no `assets/lottie/`

Missing reference cover files:

- `50films.png`
- `autumn.png`
- `january_happiness.png`
- `review.png`
- `summer_vacation.png`
- `wish_list_2025.png`

Missing or differently named reference topic files include:

- `2025goalfornexthalfyear.txt`
- `2026.txt`
- `50films.txt`
- `april.txt`
- `august.txt`
- `autumn.txt`
- `december.txt`
- `feb.txt`
- `jan.txt`
- `january_happiness_target.txt`
- `july.txt`
- `june.txt`
- `life.txt`
- `march.txt`
- `may.txt`
- `november.txt`
- `october.txt`
- `review.txt`
- `september.txt`
- `summer_vacation.txt`
- `topicweek.txt`
- `wish_list_2025_target.txt`

This matters because the reference handbook is not only layout. It is driven by a concrete catalog of visual covers, target sets, editor assets, and guide animation assets.

### 2. The reference editor is a WebView rich editor bundle

Reference `editor.html` explicitly loads:

- `normalize.css`
- `style.css`
- `rich_editor.js`

Current local app has a local editor route and `rich_editor.js`, but the editor bundle is not asset-identical because `normalize.css` is missing. The reference editor CSS is from the Wasabeef rich editor style lineage. This means the original diary editing surface likely used a WebView editor with normalized browser styling, not only native Compose fields.

### 3. The reference guide/onboarding is Lottie-backed

Reference APK includes:

- `assets/lottie/goalday.json`
- `assets/lottie/coupon.json`
- `assets/lottie/book.png`
- `assets/lottie/card.png`
- `assets/lottie/img_0.png` through `img_8.png`
- `assets/lottie/star_green.png`
- `assets/lottie/star_pink.png`
- `assets/lottie/star_purple.png`
- `assets/lottie/star_white.png`
- `assets/lottie/star_yellow.png`

`goalday.json` is a Lottie animation at 25fps, 100 frames, 393x300. Current local guide is Compose animation only. It is not visually equivalent.

### 4. Surviving strings point to dedicated views and row resources

Readable string evidence includes:

- `fl_repeat`
- `iv_repeat`
- `ll_backup`
- `style_schedule_day_form_adaptive`
- `Theme.GoalDay.AppWidgetContainer`
- `Widget.GoalDay.AppWidget.Container`
- `Widget.GoalDay.AppWidget.InnerView`
- `Add widget`
- `AppMidWidgetText`
- `AppWidgetText`

Earlier audits also found readable route/layout signals such as:

- `BookActivity`
- `DiaryActivity`
- `TargetDetailActivity`
- `LongImageDisplayActivity`
- `PrintPage`
- `fragment_schedule_inbook`
- `fragment_monthly_schedule`
- `item_schedule_item`
- `item_schedule_item_adaptive`
- `item_schedule_item_in_book`
- `item_schedule_move_target`
- `item_diary_img`
- `item_diary_text`
- `item_diary_target`
- `item_diary_target_in_book`
- `item_diary_target_child`
- `item_diary_target_child_inbook`
- `item_diary_topic_target`
- `item_diary_topic_target_inbook`

The important conclusion is architectural: the reference APK appears to have dedicated activities/fragments/item rows for handbook, diary, target detail, schedule, long image, print, guide, and widget flows. Current local implementation still has many of these surfaces inside shared Compose components.

## Why The Current Handbook Still Feels Unlike The APK

The local app has improved, but it is still not the same product shape.

Current local state:

- `GoaldayHandbookScreen` now exists.
- `HandbookReplicaPage` exists.
- Diary has first-class `IMAGE`, `TEXT`, `TARGET`, `TARGET_CHILD`, and `TOPIC_TARGET` blocks.
- In-book TODO and DONE schedule rows exist.
- Target detail overlay has a stronger route-like surface.
- Long-image preview/export exists as dialogs.

Remaining mismatch:

1. The reference APK is asset-led; local is mostly code-drawn.

   The original uses many bitmap covers and Lottie guide assets. Local still has fewer covers and no Lottie assets, so the first visual impression is different.

2. The reference handbook likely uses native XML/View row hierarchy.

   Static signals point to item layouts for diary and schedule rows. Local rows are custom Compose approximations. They can become close, but they are not the same implementation style.

3. The original diary/editor stack is WebView-rich-editor-first.

   Local has rich editor support, but diary still mixes native fields, typed blocks, and a WebView area. The reference likely uses a more integrated editor/page pipeline.

4. The original in-book schedule has repeat/adaptive/move-target resources.

   Local now has in-book rows and drag/move/done behavior, but repeat handling and adaptive row variants are still incomplete.

5. The original has dedicated long-image/print routes.

   Local still uses dialogs for long-image preview/export. It should become a full-screen route with export presets.

6. Widgets are still simplified.

   The reference has GoalDay widget themes and mid/add widget strings. Local widgets exist, but config and visual states do not match.

## Updated Handbook Gap Estimate

| Area | Current parity | Notes |
| --- | ---: | --- |
| Dedicated handbook shell | 55-65% | Better after `GoaldayHandbookScreen`, still not original page hierarchy. |
| Diary row model | 60-70% | First-class image/text/target/child/topic rows now exist, but editor pipeline differs. |
| In-book schedule rows | 55-65% | TODO/DONE rows exist, repeat/adaptive/move-target variants still missing. |
| Topic/catalog data | 70-80% | Many local topics exist, but exact cover/topic file set differs. |
| Visual asset fidelity | 35-45% | Missing 6 covers, all Lottie assets, and original bitmap treatment. |
| Long-image/print route | 45-55% | Export works, but no dedicated route/preset surface. |
| Widget parity | 45-55% | Widgets exist, config/style depth still incomplete. |
| Overall handbook similarity | 50-60% | Functional pieces exist, but original APK product feel is still not matched. |

## Required Next Work

### P0: Stop polishing around the wrong structure

The handbook should become a route family, not one large shared page:

- `GoaldayHandbookScreen`
- `HandbookScheduleRoute`
- `HandbookDiaryRoute`
- `HandbookTargetRoute`
- `HandbookLongImageRoute`
- `HandbookPrintRoute`

### P1: Match the reference page systems

- Replace generic handbook board internals with explicit schedule page components:
  - monthly schedule page
  - in-book schedule page
  - adaptive schedule row
  - repeat row/panel
  - move-target row/panel
- Replace mixed diary editor with a diary page pipeline:
  - image row
  - text row
  - target row
  - target child row
  - topic target row
  - in-book variants

### P2: Restore asset-driven feeling with clean-room assets

- Add clean-room equivalents for the 6 missing reference cover names.
- Add `normalize.css` to the local editor bundle.
- Add local Lottie-like or Compose-equivalent guide screens using the same roles:
  - goalday animation
  - book/card visuals
  - star accents
  - step-by-step onboarding

### P3: Promote export/widgets into first-class surfaces

- Replace long-image dialog with full-screen long-image display route.
- Add print/export presets.
- Expand widget config for add/mid/large schedule and diary widgets.

## Bottom Line

The user is right: the current handbook still does not look like the original APK. The biggest gap is no longer only missing features. It is product architecture and asset fidelity. The next effective move is to rebuild the handbook as route families and page systems, then layer clean-room assets and motion on top.
