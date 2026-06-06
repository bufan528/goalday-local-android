# Direct APK Long Image Shortcut Pass - 2026-06-06

## Direct APK Evidence

Commands used:

- `aapt dump xmltree docs/reference-apk/goalday-reference-base.apk AndroidManifest.xml`
- `aapt dump resources docs/reference-apk/goalday-reference-base.apk`

Visible evidence:

- Manifest contains `com.first.goalday.mainmodule.LongImageDisplayActivity`.
- Manifest contains `com.first.goalday.mainmodule.book.PrintPage`.
- Resource table contains `activity_long_image_display`, `iv_long_image`, `long_image_content`, `ic_print`, `ic_pdf`, and `ic_select_pdf`.
- Resource table contains shortcut strings:
  - `shortcut_print_export_disabled`
  - `shortcut_print_export_long`
  - `shortcut_print_export_short`
  - `shortcut_print_export_short_1`
  - `shortcut_print_export_short_2`

## Local Changes

- Added a persisted long-image shortcut export mode.
- Added shortcut modes matching the visible APK resource names: disabled, long, short, short 1, and short 2.
- Added a shortcut configuration panel to the local `LongImageDisplayActivity`-style preview route.
- Added a shortcut save action and made the bottom action bar horizontally scrollable for small screens.

## Boundary

No protected implementation was decompiled or copied. The change is a clean-room implementation based on readable manifest and resource-table evidence.
