# APK Parity Recheck

Reference APK: `docs/reference-apk/goalday-reference-base.apk`

Date: 2026-06-04

## Static APK Evidence

- APK package remains `com.first.goalday`.
- APK is still protected/packed through `assets/libjiagu*.so`, so clean-room implementation remains the correct boundary.
- Readable reference asset groups:
  - 33 files under `assets/cover/`
  - 18 files under `assets/lottie/`
  - `editor.html`, `rich_editor.js`, `style.css`, `normalize.css`
  - `topic_center_config.json`
  - 28 files under `assets/topictarget/`

## Current Local Asset Position

- Local app now has all selected cover, lottie, editor, normalize, and topic-center assets present.
- The remaining static asset mismatch is naming shape in `topictarget`:
  - Several reference APK files use names like `life.txt`, `topicweek.txt`, and `2026.txt`.
  - Local clean-room files primarily use names like `life_target.txt`, `topicweek_target.txt`, and `2026_target.txt`.

## Change Made In This Pass

- Topic metadata now points to real bitmap cover assets through `assets/cover/<key>.png`, not the older `compose/cover` placeholder path.
- Target loading now accepts both local `_target.txt` names and reference APK `.txt` names.
- Added unit coverage for target asset candidate generation.
- Long-image preview/export now behaves more like a dedicated route: full-screen `LONG IMAGE DISPLAY · PRINT EXPORT`, export presets, scrollable long-image view, save/share/print actions, and local MMKV-backed recent export history.
- Widget configuration is now closer to the reference add/config flow: schedule mid, schedule large, and diary-add widgets share a richer configure screen with live preview, color swatches, display range presets (`今天` / `未来7天` / `本周`), density presets (`紧凑` / `标准` / `详细`), and per-widget MMKV persistence. Schedule widgets now render from the selected range and density instead of always showing today only.
- First-run guide is now closer to the reference `GuideActivity` shape: full-screen onboarding, local `assets/lottie` bitmap roles (`book`, `card`, `img_*`, `star_*`), animated hero stage, step dots, and route-style bottom content/actions. It remains a clean-room Compose implementation and does not render or bypass protected Lottie internals.

## Remaining Practical Gaps

1. Dedicated route parity remains incomplete: target detail, long-image display, print/export, and guide are still Compose surfaces instead of separate reference-style Android activities, though long-image/export and guide now have route-like full-screen surfaces.
2. Widget configuration is improved, but exact reference XML theme resources, locked/unlocked resource states, and bitmap-level widget visuals are still clean-room approximations.
3. Diary item rendering is structurally close but not an exact match for the reference `item_diary_*` row hierarchy.
4. Online/account/VIP/payment/server flows remain intentionally out of scope for this local-only project.

## Verification Notes

- `:app:compileDebugKotlin` passed.
- `:app:testDebugUnitTest` passed.
- `:app:assembleDebug` passed.
- The build host is arm64 while Android Gradle cached x86-64 `aapt2`; installed `qemu-user`, `binfmt-support`, and amd64 runtime libraries so local Android build-tools can execute.
- Latest APK copied to `/home/ubuntu/Downloads/goalday-local-debug-20260604-guide.apk`. The Windows `D:\Downloads` mount was not present in this Linux environment.
