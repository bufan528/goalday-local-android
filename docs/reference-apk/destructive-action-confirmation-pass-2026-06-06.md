# Destructive action confirmation pass - 2026-06-06

Scope:

- Continued local clean-room UI/function repair.
- No server, VIP, login, or protected APK implementation was added.

Fixes:

- Custom handbook "delete page" now asks for confirmation before removing page-scoped local data.
- Custom handbook "delete book" now asks for confirmation before removing the whole local custom book.
- Calendar task delete now asks for confirmation before removing the schedule entry.
- Calendar deletion now gives a visible "已删除任务" feedback message after confirming.

Verification:

- `./gradlew :app:testDebugUnitTest :app:assembleDebug`
