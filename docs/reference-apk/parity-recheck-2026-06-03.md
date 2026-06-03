# APK Parity Recheck

Reference APK: `docs/reference-apk/goalday-reference-base.apk`

Date: 2026-06-03

## Boundary

This recheck uses static evidence only: APK zip inventory, readable assets, strings, and current local source/resource files. The reference APK remains protected/packed with `assets/libjiagu*.so`; this repo must stay a clean-room local implementation. No protected code, paid assets, VIP/payment bypass, or server/account behavior is copied.

## What Changed Since The Last Deep Audit

Recent local commits materially improved reachable offline behavior:

- `2081287` restored a real default home navigation and direct entries for today, handbook, diary, inspiration, calendar, and settings.
- `e8d5fe3` expanded local inspiration target assets from 4 files to 27 files.
- `d6a6dfa` added visible ledger summaries to handbook target and planner pages.
- `61137fa` changed target entries from cramped two-column cards into single target ledger rows.
- `9d8d192` added clearer diary block badges and typed block row treatment.

## Resource Delta

| Area | Reference APK | Current Local App | Current Gap |
| --- | ---: | ---: | --- |
| Topic target text files | 28 | 27 | Very small |
| Topic config topics | about 19 readable config entries in APK evidence, with broad CN/EN roots | 27 CN clean-room topics | Local is now deep enough for offline use |
| Topic cover images | 33 PNG files | 0 bitmap cover files; Compose cover art only | Large visual gap |
| Lottie/onboarding assets | 18 files | 0 Lottie files; Compose guide overlay only | Large animation gap |
| Rich editor bundle | `editor.html`, `rich_editor.js`, `style.css`, `normalize.css` | `editor.html`, `rich_editor.js`, `style.css` | Medium; no `normalize.css`, editor route is simpler |

## Module Gap Recheck

### 1. Home and core navigation

Current local app:

- Default home screen exists.
- Home can add tasks, drag/mark done, and jump to calendar, handbook, diary, and inspiration.
- Bottom navigation includes today, handbook, inspiration, calendar, settings.

Gap now: small-medium. The app is no longer hidden behind the book library. The remaining difference is visual fidelity, not basic reachability.

### 2. Inspiration/topic center

Current local app:

- 27 local target text files.
- 27 clean-room topic config entries.
- Inspiration screen can select topics, edit/select items, import into task pool, and open handbook.

Gap now: medium. Functional/data depth is much closer. The big remaining visual gap is the lack of 33 bitmap-style topic covers from the APK; local cover art is generated in Compose.

### 3. Target detail / target page

Current local app:

- Target ledger summary: pending, completed, scheduled, custom.
- Single-row target ledger cards with number, done toggle, detail, schedule actions, notes, quick schedule chips, deadline chips, delete for custom targets.

Gap now: medium. This is significantly closer than before. Remaining differences:

- The APK has dedicated `activity_target_detail`, `target_detail_options`, popup/date resources.
- Local target detail is still integrated into Compose overlays and handbook rows, not a fully separated route with the same option hierarchy.

### 4. Diary and rich editor

Current local app:

- Rich editor assets are present.
- Diary supports text, target, topic-target blocks, child lines, image URIs, long-image preview/export.
- Diary block rows now have typed badges and clearer browsing/editing treatment.

Gap now: medium-large. It is usable, but reference evidence still suggests more exact item layouts:

- `item_diary_img`
- `item_diary_text`
- `item_diary_target`
- `item_diary_target_in_book`
- `item_diary_target_child`
- `item_diary_target_child_inbook`
- `item_diary_topic_target`
- `item_diary_topic_target_inbook`

Local implementation is structurally similar but not visually exact.

### 5. Schedule/month/in-book planning

Current local app:

- Today board exists.
- Calendar supports add, edit, move, done, import from system calendar.
- Handbook schedule page has a replica board, monthly mode, long-image preview/export.
- Planner pages now show summary counts.

Gap now: medium. Core offline function is strong. Remaining reference-specific differences:

- APK signals dedicated `fragment_monthly_schedule`, `fragment_schedule_inbook`, adaptive schedule item rows, repeat hook/line/item resources.
- Local still uses Compose boards rather than the APK's exact fragment/item hierarchy.

### 6. Export/print

Current local app:

- Diary and handbook schedule long-image preview dialogs.
- Save/share/print helpers exist.

Gap now: medium-small functionally, medium visually. Reference APK still has stronger route signals:

- `activity_long_image_display`
- `PrintPage`
- `print_export_*`
- `shortcut_print_export_*`

Local has the workflow, but not the same dedicated route/preset/history surface.

### 7. Widgets

Current local app:

- Schedule widget.
- Large schedule widget.
- Quick diary widget.
- Schedule widget configure activity exists.

Gap now: medium. The APK has stronger widget resource signals:

- `diary_add_widget_configure`
- `widget_schedule_add_color`
- `schedule_mid_widget`
- `schedule_larger_widget`
- unlock-state resources

Local widgets are useful but still need richer configure presets and closer visuals.

### 8. Guide/onboarding

Current local app:

- Compose guide overlay exists and can be reopened.

Gap now: large visually. Reference APK has `GuideActivity` and 18 Lottie/onboarding assets. Local guide is static and simpler.

### 9. Settings/backup

Current local app:

- Backup manager.
- Settings backup/restore/history.
- Font-size/settings rows.

Gap now: medium-small for offline use. Reference has more settings/account ecosystem; online/account/VIP/payment remains intentionally out of scope.

## Updated Parity Estimate

| Scope | Estimate | Reason |
| --- | ---: | --- |
| Useful offline planner functionality | 88-92% | Home, topic import, handbook, target rows, diary blocks, calendar, widgets, backup, export are all reachable and build-tested. |
| Reference/video visual parity | 64-72% | Target and diary rows improved, topic data gap shrank, but bitmap covers, Lottie guide, exact item layouts, and route-specific polish remain. |
| APK structure parity excluding online/account/VIP/pay | 76-83% | Most local modules now have clear equivalents; remaining work is exact route/asset/layout fidelity. |
| Full APK parity including online/account/VIP/pay | 28-35% | Account, VIP, coupon, payment, and server behavior remain excluded by design. |

## Biggest Remaining Gaps

1. Cover art and visual assets.
   The data gap is nearly closed, but the visual cover gap remains large because reference APK has 33 cover PNGs and local uses Compose-generated cover art.

2. Guide animation.
   Reference has Lottie/onboarding assets. Local guide is static.

3. Exact diary item layout.
   Local has typed blocks, but not the exact `item_diary_*` hierarchy and image/text/target/in-book row chrome.

4. Dedicated route polish.
   Reference has dedicated long-image display, print page, target detail, guide activity, and likely more activity-level flows. Local often implements these as Compose overlays/sheets.

5. Widget configuration depth.
   Local widgets exist, but exact mid/large/config/color/unlock-state visual behavior is not fully matched.

## Next Practical Work

1. Add clean-room bitmap-style topic covers for the 27 topics, or generate them as local assets, so topic center looks closer to the APK/video.
2. Build a stronger guide sequence with motion/spotlight callouts.
3. Split diary rendering into explicit `image`, `text`, `target`, `target child`, and `topic target` row components.
4. Add a dedicated full-screen long-image preview route.
5. Expand widget configuration presets and visual styles.

## Bottom Line

The gap is no longer "almost everything is missing." The app is now much more usable as an offline Goalday-like planner. The remaining gap is mostly visual fidelity and exact product structure: cover assets, onboarding animation, dedicated routes, widget presets, and more precise diary/schedule item chrome.
