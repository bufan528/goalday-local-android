# Project Notes

## Project

- Name: `goalday-local`
- Goal: reverse-engineer the reference APK and recreate an Android app that is as close as practical in local-only usage, interaction, and overall feel.

## User Requirements

- The app must be fully local-only.
- No server dependency is allowed.
- No login, VIP, payment, subscription, or ad flows are needed.
- The app is for personal local use, not commercial deployment.
- The recreated app should stay as close as possible to the reference APK in:
  - feature set
  - visual structure
  - interaction details
  - motion/effects
- The book metaphor is a hard requirement.
- The page-turn effect must feel like a real book, not just a basic horizontal swipe.
- UI can reference iOS polish, but should still follow the original APK's information architecture and core visual identity.
- Users must be able to create and edit their own content locally:
  - books
  - pages
  - schedules
  - diary content
  - checklist items
- When a task is marked completed, text must become gray and show a strikethrough. Tapping again must clear that state.
- Calendar/day interactions should support tap-to-add behavior similar to the original APK.

## Delivery Constraints

- Always place the latest installable APK in `D:\Downloads`.
- Prefer keeping heavy Android/Gradle caches and tooling on `D:` when possible because `C:` space is limited.

## Reference APK

- Original APK path:
  - `D:\电脑管家迁移文件\xwechat_files\wxid_dfb9b3ch4lju22_65fe\msg\file\2026-05\base.apk.1(1).1`
- Reverse-engineering notes already established:
  - package name is `com.first.goalday`
  - original app contains topic center, book module, diary module, calendar-related capability, backup, and more content-heavy assets
  - original diary uses local asset-based rich editor files
  - code appears protected/packed, so resource and asset analysis is more reliable than direct source recovery

## Current Product Direction

- Keep prioritizing parity for local core modules first:
  - book/library
  - page editing
  - diary editing
  - schedule/calendar
  - backup/restore
  - page-turn interaction
- Business/network features remain intentionally excluded unless the user later asks otherwise.

## Current Known Gaps

- Page turning is improved but still not at full reference parity.
- The app still needs more original-like visual detail and richer module completeness.
- Topic-center scale/content breadth from the original APK is not yet fully replicated.
- More reverse-engineered interaction details still need to be brought over.

## Working Rule

- Do not describe a feature as fully matched to the original APK unless it is actually verified to be close in behavior and feel.
- Prefer verified build evidence over assumptions.
