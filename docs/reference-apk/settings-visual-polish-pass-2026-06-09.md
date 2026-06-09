# Settings Visual Polish Pass - 2026-06-09

Scope: continue the local-only parity polish by tightening the Settings page visual hierarchy and small-screen action affordances.

Changes:
- Reworked the Settings hero into a stronger local data center card with paper elevation, border treatment, and a three-part local data metric strip.
- Matched Settings sections to the recent home/calendar/handbook paper-card language with consistent shadows, borders, and warmer surfaces.
- Converted backup action controls from raw text pills into fixed-height centered buttons to reduce cramped labels and uneven vertical alignment.
- Upgraded backup history and empty states from flat translucent rows to bordered paper rows.

Verification target:
- `:app:testDebugUnitTest`
- `:app:assembleDebug`
- `:app:lintDebug`
