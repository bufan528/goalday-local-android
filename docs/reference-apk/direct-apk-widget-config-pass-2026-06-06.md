# Direct APK widget config pass - 2026-06-06

Reference APK: `docs/reference-apk/goalday-reference-base.apk`

This pass only used readable APK metadata and resource-table names from `aapt dump resources`.
It does not decompile protected code, bypass packing, or copy implementation.

Direct APK resource signals found:

- Layouts: `schedule_mid_widget`, `schedule_larger_widget`, `diary_add_widget`, `diary_add_widget_configure`.
- Unlock layouts: `unlock_schedule_mid_widget`, `unlock_schedule_larger_widget`, `unlock_diary_add_widget`.
- Color configuration layouts: `widget_schedule_add_color`, `widget_plan_add_color`.
- Widget dot drawables: `ic_widget_dot_000000`, `ic_widget_dot_334f46`, `ic_widget_dot_9eaadb`, `ic_widget_dot_bbd1ad`, `ic_widget_dot_f1a5b6`, `ic_widget_dot_f8d58a`, `ic_widget_dot_ffffff`.
- Widget shell drawables: `app_widget_background`, `app_widget_inner_view_background`, `app_widget_lock_background`.
- Widget descriptions/strings: `add_widget`, `app_widget_description`, `appwidget_today_want_write`, `configure`, `diary_add_widget_info_desc`, `schedule_mid_widget_desc`.

Local implementation changes:

- `ScheduleWidgetStyle` now includes APK-named dot palettes, so widget configuration can choose the same color families identified in the APK resource table.
- Widget configuration now displays the matched layout, color-layout, and unlock-layout resource signals per widget type.
- Because this local edition intentionally has no VIP or server dependency, the APK `unlock_*` evidence is represented as `本地全解锁 / 无 VIP 锁` instead of adding any paywall.
- Schedule and diary widget RemoteViews now surface the local-unlocked state after being placed on the launcher.

Follow-up gap:

- The protected APK implementation remains unavailable through safe static resource analysis. Further parity should continue from visible behavior, readable resources, and clean-room UI/function rebuilding.
