# Handbook book + diary schedule pass - 2026-06-05

Scope: clean-room handbook parity pass focused on making the handbook feel like a physical book while keeping the mobile page usable.

## Problems Addressed

- The last mobile pass made the page usable, but the handbook lost too much of the book-like visual shell.
- Diary content and schedule content were not connected strongly enough. Work tasks or target blocks entered in the diary could remain only in the diary draft and not appear in the schedule handbook.

## Changes

- Restored a physical book frame around the handbook route: layered cover, paper stack, spine, edge highlights, and page progress detail.
- Kept the actual content as a full-width single page inside the frame so controls and editors are still usable on phones.
- Synced diary sections into local schedule entries:
  - `工作任务`, `日程`, `计划`, and `待办` become planned schedule entries.
  - `今日完成` becomes completed schedule entries.
  - diary target blocks, topic target blocks, and child target blocks are parsed into schedule entries.
- Diary-sourced schedule rows are marked with a local `日记同步` note and are refreshed when diary content changes.

## Verification

- `./gradlew :app:compileDebugKotlin`
- `./gradlew :app:testDebugUnitTest :app:assembleDebug`
