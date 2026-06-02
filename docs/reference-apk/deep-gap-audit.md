# Reference APK Deep Gap Audit

Reference APK: `docs/reference-apk/goalday-reference-base.apk`

Date: 2026-06-02

## Boundary

This audit uses the APK as a product reference only. The implementation in this repo should stay clean-room: no copied APK code, no copied paid/proprietary assets, and no payment/protection bypass.

## What The APK Actually Contains

The APK is not just a simple planner UI. Even without full smali/layout decoding, the readable asset layer shows a larger product system:

- 33 topic cover images in `assets/cover/`.
- 28 topic target text files in `assets/topictarget/`.
- `assets/topic_center_config.json` with 15 Chinese topic templates and 4 English templates.
- A rich editor bundle: `editor.html`, `rich_editor.js`, `style.css`, `normalize.css`.
- Lottie onboarding/empty-state assets under `assets/lottie/`.
- iText PDF resources and `PrintPage` signals, which point to a real print/export flow.
- Activity/module signals for `GuideActivity`, `PlanIdeaCenterActivity`, `TopicDetailActivity`, `TargetDetailActivity`, `BookActivity`, `DiaryActivity`, `CalendarImportActivity`, `LongImageDisplayActivity`, `PrintPage`, `BackupActivity`, and `SettingActivity`.
- Widget signals for diary add, mid schedule, and large schedule widgets.
- Account/VIP/coupon/payment/login flows, intentionally out of scope for the local version.

## Current App Reality

The current app has many local equivalents, but most are simplified:

- Topic center exists, but it is handwritten Compose data, not a config-driven catalog with visual cover assets and full target files.
- Book/handbook exists, including page turn, but the visual model is still a custom planner surface, not a close replica of the reference book pages.
- Schedule exists with drag/drop, todo/done, repeat, import, and widgets, but the reference has more specialized schedule item layouts and a more guided monthly/in-book schedule split.
- Diary exists with structured local blocks and image support, but the reference has a rich text editor asset bundle and distinct diary image/text/target/topic-target item models.
- Export/share exists, but reference signals suggest a dedicated long-image display plus print/PDF flow.
- Settings/backup exists, but reference has a broader settings/account ecosystem.

## Corrected Completion Estimate

The previous `93-95%` estimate was too optimistic because it counted feature names, not visual/interaction parity.

More realistic estimates:

- **Offline functional coverage**: about **60-70%**.
- **Exact video/reference visual parity**: about **35-45%**.
- **Full APK including account/VIP/payment/server flows**: about **25-35%**.

The app is usable as a local planner, but it is not yet visually or structurally close enough to say it matches the reference APK/video.

## Biggest Gaps

| Priority | Area | Reference Signal | Current State | Gap |
| --- | --- | --- | --- | --- |
| P0 | Book/handbook visual system | `BookActivity`, in-book schedule/diary fragments, dense book-like pages | Custom page-turn planner with simplified content blocks | Needs the biggest visual rebuild |
| P0 | Topic assets and catalog | 33 covers, 28 target files, config JSON | 15 simplified templates, no real cover-image system | Need config-driven templates and generated/original cover cards |
| P0 | Target detail | Dedicated target detail screen and item layouts | Target cards exist but simpler | Need full target-detail workflow: title/header, sections, notes, deadlines, save-as-own |
| P1 | Diary editor | Rich editor web bundle and diary item layouts | Structured blocks plus images | Need richer inline text/image/target blocks and edit toolbar |
| P1 | Schedule monthly/in-book split | `fragment_schedule`, `fragment_monthly_schedule`, `fragment_schedule_inbook` | Calendar page plus handbook preview | Need separate monthly board and book-embedded schedule page style |
| P1 | Export/print | Long image display and `PrintPage` | Share/export exists, preview is limited | Need dedicated preview/export/print surface |
| P2 | Widgets | diary add, mid schedule, large schedule | Widgets exist | Need visual/config polish |
| P2 | Settings/backup | Dedicated backup/settings/account screens | Local backup/settings | Need settings density and restore UX polish |

## Recommended Next Build Order

1. Rebuild the book/handbook first screen so it looks like a real book library and not a generic Compose dashboard.
2. Make topic center config-driven from local JSON-style models, with 25-30 templates and cover-like visuals.
3. Rebuild the in-book schedule page: left task pool, dated page area, done area, compact controls, fewer generic cards.
4. Rebuild target detail into a dedicated full-screen flow with editable sections and save-as-own behavior.
5. Replace the diary surface with block-based editor UI: text block, image block, target link block, topic-target block.
6. Add a dedicated long-image/print preview screen before exporting.

## Immediate Next Fix

The next implementation should not be another small schedule feature. The highest-value fix is **book/handbook visual parity**:

- make the library cards look like physical notebooks/books;
- make the opened handbook full-screen and page-like;
- reduce generic nested panels;
- move task pool/date assignment/done into a dense in-book layout;
- make page headers, tabs, and bottom actions match the Goalday-style product rhythm.

