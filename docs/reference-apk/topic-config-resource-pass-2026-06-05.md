# Topic Config Resource Pass - 2026-06-05

## APK Evidence Used

- `docs/reference-apk/goalday-reference-base.apk` exposes `assets/topic_center_config.json`.
- The same APK exposes `assets/cover/*.png` cover artwork and `assets/topictarget/*.txt` target copy.
- Target files use mixed names in the observable asset tree, including both plain names such as `life.txt` and `_target` names such as `weekly_review_target.txt`.

## Local Changes

- Added a shared `TopicCatalogSummary` loader for the local topic-center config.
- Counted cover and target asset directories at runtime so the UI can show whether the config/resource bundle is actually present.
- Reused the same summary in both the handbook embedded inspiration center and the standalone inspiration center screen.
- Switched topic cards and hero badges to show the loaded target-file count instead of only the hardcoded fallback item count.
- Kept fallback behavior local/offline when the config asset cannot be read.

## Remaining Delta

- The local implementation still uses clean-room Compose cover art over the copied local resource structure, not the APK implementation code.
- Exact APK animation timing and internal routing around the topic center still need visual-device comparison.
