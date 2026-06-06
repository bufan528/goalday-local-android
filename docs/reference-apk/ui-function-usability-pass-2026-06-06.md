# UI and function usability pass - 2026-06-06

Scope:

- Continued local clean-room UI/function repair based on direct code review.
- No server, VIP, login, or protected APK implementation was added.

Fixes:

- Page creation now supports a preset page type and title from the entry point.
- Empty-handbook "add page" now defaults to a schedule page.
- Missing-diary "add diary page" now defaults to a diary page.
- Create-page dialog changes its default title when the user switches page type.
- Create-page dialog disables the create action until a title is present.
- Newly created pages now open reliably after the book list refreshes.
- Embedded inspiration center "back" no longer uses an empty click handler.
- Top bars are less likely to overflow on narrow screens.
- Home floating add button now gives feedback when the task input is empty.

Verification:

- `./gradlew :app:testDebugUnitTest :app:assembleDebug`
