# Long image and print route pass - 2026-06-05

Reference signals:

- `LongImageDisplayActivity`
- `PrintPage`
- long-image export/share/print resources

Changes:

- Reworked the long-image preview surface header into a route-style top bar with explicit back action.
- Added visible `LongImageDisplayActivity` and `PrintPage` labels to match the reference route structure.
- Added a print information panel showing the selected preset, paper target, and preview ratio.
- Kept the existing save/share/print implementation, but made the bottom actions equal-width and route-like.
- Added a stable `paperLabel` to export presets instead of relying on Android `MediaSize` labels.

Validation:

- `./gradlew :app:compileDebugKotlin`
- `./gradlew :app:testDebugUnitTest`
