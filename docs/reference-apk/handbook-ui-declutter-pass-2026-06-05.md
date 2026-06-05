# Handbook UI declutter pass - 2026-06-05

Scope: clean-room handbook UI pass focused on reducing visual clutter and restoring usable content space.

## Problems Addressed

- The handbook had two visible book shells: the outer handbook frame and the inner `BookShell` book chrome.
- The top area had separate title, section, and page-chip rows, which made the page feel like stacked navigation instead of a book.
- The content area lost too much usable space to nested padding and a large inner route header.
- The bottom page controls were visually heavier than necessary.

## Changes

- Kept the outer physical handbook shell, but changed the inner reader shell to a light page container while preserving handbook page-turn behavior.
- Reduced handbook reader page padding.
- Merged the title, page counter, and section controls into a compact top chrome.
- Slimmed the inner route header and bottom page dock.
- Tightened the content frame padding so schedule, diary, and target pages have more room.

## Verification

- `./gradlew :app:compileDebugKotlin :app:testDebugUnitTest :app:assembleDebug`
