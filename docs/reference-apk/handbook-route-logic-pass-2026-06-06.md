# Handbook Route Logic Pass - 2026-06-06

## Problem

- Handbook pages could be changed from inside `BookReader` without updating the outer handbook route section.
- This could leave the UI in a stale section, for example a diary route rendering a target or schedule page after a page flip.
- The diary entry mode used the diary segment visually, but its reader page-turn callbacks still walked through the whole book.
- Entry landing effects could reset the selected page more often than intended when entering dedicated handbook or diary modes.

## Local Changes

- Routed handbook reader page flips through the outer `goToPage` function so page and section stay synchronized.
- Limited segmented reader previous/next page context to the current filtered section.
- Limited diary-mode page chips and page turns to diary pages instead of the whole book.
- Made handbook/diary landing page selection run once per entry session, then preserve internal user navigation.

## Remaining Delta

- The reference APK likely has separate activities/fragments for each route; the local app still keeps routes in one Compose module.
- More route-level polish is still needed around target detail, long-image export, and page-specific option menus.
