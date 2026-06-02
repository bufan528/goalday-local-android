# Goalday Reference APK Analysis

Reference file: `docs/reference-apk/goalday-reference-base.apk`

## APK facts

- Package: `com.first.goalday`
- Version: `Version 2.5.7` / versionCode `56`
- Launcher activity: `com.first.goalday.guidemodule.GuideActivity`
- Minimum SDK: `26`
- Target SDK: `34`
- Notable permissions: system calendar read/write, media read, camera, microphone, vibration, network.
- Protection signal: `assets/libjiagu*.so` is present, and resource filenames are heavily shortened.

## Exposed product structure

The readable asset layer shows the reference app is built around:

- A topic center with localized topic config: `assets/topic_center_config.json`.
- Topic cover images under `assets/cover/`.
- Topic target lists under `assets/topictarget/`.
- A rich editor web asset bundle: `editor.html`, `rich_editor.js`, `style.css`.
- Lottie onboarding/empty-state assets under `assets/lottie/`.
- Schedule and target resource names such as `rv_schedule`, `rv_target`, `ll_import_calendar`, `target_detail_options`, `schedule_empty_hints`, `ic_double_schedule`, and `ic_unlimited_schedule`.

## UX implications for this app

- The app needs a real topic/template center, not just a small inspiration list.
- Templates should carry color, type, schedule-link behavior, and a multi-item target list.
- Imported template items should land in the handbook task pool so they can be assigned to dates.
- The planner should support the video workflow: collect tasks first, drag into dates, then drag completed items into done.
- Calendar integration and richer diary/media editing are reference-app gaps to address later.

## Current implementation response

- Expanded `InspirationTemplates` into a larger topic-center-like catalog.
- Updated `InspirationScreen` to show colored topic cards and schedule/record type labels.
- Kept implementation original; reference APK code/assets are not copied into app source.

## Clean-room boundary

This project uses the reference APK only to understand product structure and visible UX behavior.

- Do not copy third-party APK code into this repository.
- Do not copy third-party paid assets into this repository.
- Do not implement payment bypasses for the reference APK.
- Implement equivalent local/offline behavior from scratch in this app.

The intended product direction is an offline, no-server Goalday-style planner where templates, handbook pages, schedule assignment, todo/done tracking, diary editing, and backups are available locally.
