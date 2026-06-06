# Guide and handbook usability pass - 2026-06-06

Problem addressed:

- The guide was mostly explanatory and did not route users into the usable app areas.
- The default local handbook only had schedule pages, so the diary entry could land on a book with no diary page.
- Handbook/diary entry modes could render a blank surface when required pages were missing.
- Empty diary pages looked like read-only previews instead of an obvious place to start writing.

Local implementation changes:

- New guide pages now expose direct actions for inspiration, handbook, diary, and today/home.
- The guide body is scrollable and its illustration height is reduced to avoid small-screen clipping.
- The default `GOALDAY` book now contains both monthly schedule pages and monthly diary pages.
- Handbook and diary entry modes select a book that contains the required page type.
- Missing-book, missing-page, and missing-diary cases now show an actionable empty state.
- Blank diary pages now show a start panel for writing, adding an image, or adding a target block.

Scope note:

- This is a clean-room usability fix for the local app. It does not add VIP, login, server checks, or protected APK reverse-engineered implementation.
