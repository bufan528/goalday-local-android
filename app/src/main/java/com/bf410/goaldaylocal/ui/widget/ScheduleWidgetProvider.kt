package com.bf410.goaldaylocal.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.View
import android.widget.RemoteViews
import com.bf410.goaldaylocal.MainActivity
import com.bf410.goaldaylocal.R
import com.bf410.goaldaylocal.data.LocalStateStore
import com.bf410.goaldaylocal.data.ScheduleEntry
import com.tencent.mmkv.MMKV
import java.time.LocalDate

enum class ScheduleWidgetStyle(
    val raw: String,
    val label: String,
    val title: String,
    val backgroundColor: Int,
    val titleColor: Int,
    val subtitleColor: Int,
    val accentColor: Int,
    val doneColor: Int,
    val doneTextColor: Int,
) {
    SOFT(
        raw = "soft",
        label = "柔和手账",
        title = "Goalday 今日",
        backgroundColor = Color.rgb(255, 248, 239),
        titleColor = Color.rgb(47, 41, 34),
        subtitleColor = Color.rgb(139, 122, 104),
        accentColor = Color.rgb(180, 94, 122),
        doneColor = Color.rgb(57, 167, 109),
        doneTextColor = Color.rgb(102, 115, 103),
    ),
    CLEAN(
        raw = "clean",
        label = "清爽白底",
        title = "今日日程",
        backgroundColor = Color.rgb(255, 255, 255),
        titleColor = Color.rgb(34, 34, 34),
        subtitleColor = Color.rgb(112, 112, 112),
        accentColor = Color.rgb(65, 121, 184),
        doneColor = Color.rgb(72, 150, 102),
        doneTextColor = Color.rgb(96, 110, 100),
    ),
    CONTRAST(
        raw = "contrast",
        label = "高对比",
        title = "GOALDAY",
        backgroundColor = Color.rgb(47, 41, 34),
        titleColor = Color.rgb(255, 250, 243),
        subtitleColor = Color.rgb(224, 207, 184),
        accentColor = Color.rgb(255, 193, 111),
        doneColor = Color.rgb(131, 210, 155),
        doneTextColor = Color.rgb(205, 221, 204),
    ),
    ROSE(
        raw = "rose",
        label = "粉色计划",
        title = "今日计划",
        backgroundColor = Color.rgb(255, 240, 246),
        titleColor = Color.rgb(71, 45, 58),
        subtitleColor = Color.rgb(141, 91, 111),
        accentColor = Color.rgb(218, 112, 149),
        doneColor = Color.rgb(75, 160, 116),
        doneTextColor = Color.rgb(128, 103, 113),
    ),
    MINT(
        raw = "mint",
        label = "薄荷清单",
        title = "Goalday List",
        backgroundColor = Color.rgb(240, 248, 242),
        titleColor = Color.rgb(42, 64, 52),
        subtitleColor = Color.rgb(91, 126, 101),
        accentColor = Color.rgb(78, 146, 107),
        doneColor = Color.rgb(54, 132, 91),
        doneTextColor = Color.rgb(103, 125, 111),
    ),
    NOTEBOOK(
        raw = "notebook",
        label = "纸页手账",
        title = "手账日程",
        backgroundColor = Color.rgb(255, 252, 245),
        titleColor = Color.rgb(62, 49, 37),
        subtitleColor = Color.rgb(132, 109, 82),
        accentColor = Color.rgb(176, 122, 77),
        doneColor = Color.rgb(91, 145, 95),
        doneTextColor = Color.rgb(126, 112, 94),
    );

    companion object {
        fun fromRaw(raw: String?): ScheduleWidgetStyle =
            entries.firstOrNull { it.raw == raw } ?: SOFT
    }
}

class ScheduleWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        appWidgetIds.forEach { widgetId ->
            appWidgetManager.updateAppWidget(widgetId, buildRemoteViews(context, widgetId))
        }
    }

    companion object {
        const val KEY_WIDGET_STYLE_PREFIX = "schedule_widget_style_"

        fun buildRemoteViews(context: Context, widgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID): RemoteViews =
            buildScheduleViews(
                context = context,
                widgetId = widgetId,
                layoutId = R.layout.widget_schedule,
                sectionId = null,
                rowIds = listOf(
                    R.id.widget_row_1,
                    R.id.widget_row_2,
                    R.id.widget_row_3,
                ),
                dotIds = listOf(
                    R.id.widget_dot_1,
                    R.id.widget_dot_2,
                    R.id.widget_dot_3,
                ),
                taskIds = listOf(
                    R.id.widget_task_1,
                    R.id.widget_task_2,
                    R.id.widget_task_3,
                ),
            )

        fun buildLargeRemoteViews(context: Context, widgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID): RemoteViews =
            buildScheduleViews(
                context = context,
                widgetId = widgetId,
                layoutId = R.layout.widget_schedule_large,
                sectionId = R.id.widget_section_today,
                rowIds = listOf(
                    R.id.widget_row_1,
                    R.id.widget_row_2,
                    R.id.widget_row_3,
                    R.id.widget_row_4,
                    R.id.widget_row_5,
                ),
                dotIds = listOf(
                    R.id.widget_dot_1,
                    R.id.widget_dot_2,
                    R.id.widget_dot_3,
                    R.id.widget_dot_4,
                    R.id.widget_dot_5,
                ),
                taskIds = listOf(
                    R.id.widget_task_1,
                    R.id.widget_task_2,
                    R.id.widget_task_3,
                    R.id.widget_task_4,
                    R.id.widget_task_5,
                ),
            )

        private fun buildScheduleViews(
            context: Context,
            widgetId: Int,
            layoutId: Int,
            sectionId: Int?,
            rowIds: List<Int>,
            dotIds: List<Int>,
            taskIds: List<Int>,
        ): RemoteViews {
            val today = LocalDate.now()
            val entries = LocalStateStore(MMKV.defaultMMKV())
                .scheduleEntries()
                .filter { it.year == today.year && it.month == today.monthValue && it.day == today.dayOfMonth }
                .sortedWith(compareBy<ScheduleEntry>({ it.completed }, { it.timeText }, { it.title.lowercase() }))
            val todo = entries.filterNot { it.completed }
            val done = entries.count { it.completed }
            val views = RemoteViews(context.packageName, layoutId)
            val style = ScheduleWidgetStyle.fromRaw(MMKV.defaultMMKV().decodeString("$KEY_WIDGET_STYLE_PREFIX$widgetId", null))
            views.setInt(R.id.widget_root, "setBackgroundColor", style.backgroundColor)
            views.setTextViewText(R.id.widget_title, style.title)
            views.setTextColor(R.id.widget_title, style.titleColor)
            views.setTextColor(R.id.widget_subtitle, style.subtitleColor)
            views.setTextColor(R.id.widget_status_pill, style.accentColor)
            views.setTextColor(R.id.widget_footer, style.subtitleColor)
            sectionId?.let { id -> views.setTextColor(id, style.accentColor) }
            views.setTextViewText(R.id.widget_subtitle, "${today.monthValue}月${today.dayOfMonth}日 · 待办 ${todo.size} · 完成 $done")
            views.setTextViewText(R.id.widget_status_pill, if (todo.isEmpty()) "清爽" else "${todo.size} todo")
            val displayEntries = entries.take(taskIds.size)
            taskIds.forEachIndexed { index, id ->
                val entry = displayEntries.getOrNull(index)
                if (entry == null) {
                    views.setViewVisibility(rowIds[index], View.GONE)
                } else {
                    views.setViewVisibility(rowIds[index], View.VISIBLE)
                    val time = entry.timeText.takeIf { it.isNotBlank() }?.let { "$it " }.orEmpty()
                    val repeat = widgetRepeatLabel(entry).takeIf { it.isNotBlank() }?.let { " · $it" }.orEmpty()
                    views.setTextViewText(dotIds[index], "●")
                    views.setTextColor(dotIds[index], if (entry.completed) style.doneColor else style.accentColor)
                    views.setTextViewText(id, "$time${entry.title}$repeat")
                    views.setTextColor(id, if (entry.completed) style.doneTextColor else style.titleColor)
                }
            }
            if (entries.isEmpty()) {
                views.setViewVisibility(R.id.widget_empty, View.VISIBLE)
            } else {
                views.setViewVisibility(R.id.widget_empty, View.GONE)
            }
            sectionId?.let { id -> views.setViewVisibility(id, if (entries.isEmpty()) View.GONE else View.VISIBLE) }
            views.setOnClickPendingIntent(R.id.widget_root, openAppPendingIntent(context))
            return views
        }

        private fun openAppPendingIntent(context: Context): PendingIntent {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            return PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }
    }
}

private fun widgetRepeatLabel(entry: ScheduleEntry): String {
    if (entry.repeatRule.isBlank()) return ""
    val interval = entry.repeatInterval.coerceAtLeast(1)
    return when (entry.repeatRule) {
        "daily" -> if (interval == 1) "每天" else "每${interval}天"
        "weekly" -> if (interval == 1) "每周" else "每${interval}周"
        "monthly" -> if (interval == 1) "每月" else "每${interval}月"
        else -> ""
    }
}
