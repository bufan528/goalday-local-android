# Book root shell parity pass - 2026-06-05

Reference APK evidence used for this pass:

- Root navigation is organized around book, calendar, and settings.
- Book-related evidence includes `BookActivity`, `DiaryActivity`, `fragment_schedule`, topic center assets, diary item rows, and in-book schedule/diary fragments.

Implemented locally:

- Added a book-root shell above the book tab content so the three-tab root no longer drops directly into a loose standalone schedule page.
- Grouped the local-only book surfaces under one visible module switcher: today, library, handbook, diary, and inspiration.
- Added compact route labels such as `fragment_schedule`, `BookActivity`, `DiaryActivity`, and `TopicCenter` to keep the local surface aligned with the reference route map.
- Preserved existing offline behavior: local schedule editing, library opening, handbook mode, diary mode, and inspiration imports remain server-free.

Known remaining gaps:

- The shell is still a clean-room Compose approximation, not the reference APK's native activity/fragment stack.
- The exact transitions and bitmap-level chrome still need screenshot-by-screenshot tuning.
