# Local Date Guard Hardening Pass - 2026-06-22

Scope: continue project-wide optimization by hardening local schedule and calendar date state.

Changes:
- Added shared schedule date guards for year/month/day normalization.
- Clamped calendar anchor reads and writes before UI code builds month models.
- Normalized schedule dates when reading legacy MMKV JSON and when saving entries.
- Reused the same guard in `ScheduleRepository.addEntry`.
- Added unit tests for leap days, invalid months, out-of-range years, and month-end clamping.

Verification target:
- `:app:testDebugUnitTest`
- `:app:assembleDebug`
- `:app:lintDebug`
