# Calendar And Navigation Parity Pass - 2026-06-05

Reference APK evidence used for this pass:

- `fragment_monthly_schedule`
- `fragment_schedule`
- `schedule_mid_widget`
- `schedule_larger_widget`
- Main app tab/navigation signals from visible activity structure

Implemented locally:

- Replaced the default Material bottom navigation with a custom Goalday-style local dock.
- Switched bottom tab markers from generic shapes to compact Chinese tab glyphs.
- Reworked the calendar top area into a monthly schedule hero with selected day, monthly todo count, done count, and progress.
- Merged month navigation and system calendar import into a denser month control row.
- Preserved existing local calendar features: system calendar import, day focus, slot assignment, drag/drop, completion, and edit dialogs.

Known remaining gaps:

- The detailed calendar board still needs a closer row-level visual pass.
- The custom dock is visually closer but still not an exact recreation of the reference APK's bottom navigation animations.
- Widget configuration and calendar import dialogs can still be visually tuned in later passes.
