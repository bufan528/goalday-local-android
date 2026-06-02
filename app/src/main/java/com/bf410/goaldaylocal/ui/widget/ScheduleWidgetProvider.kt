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

class ScheduleWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        appWidgetIds.forEach { widgetId ->
            appWidgetManager.updateAppWidget(widgetId, buildRemoteViews(context))
        }
    }

    companion object {
        fun buildRemoteViews(context: Context): RemoteViews =
            buildScheduleViews(
                context = context,
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

        fun buildLargeRemoteViews(context: Context): RemoteViews =
            buildScheduleViews(
                context = context,
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
            views.setTextViewText(R.id.widget_title, "Goalday 今日")
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
                    views.setTextViewText(dotIds[index], "●")
                    views.setTextColor(dotIds[index], if (entry.completed) Color.rgb(57, 167, 109) else Color.rgb(232, 143, 174))
                    views.setTextViewText(id, "$time${entry.title}")
                    views.setTextColor(id, if (entry.completed) Color.rgb(102, 115, 103) else Color.rgb(58, 52, 46))
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
