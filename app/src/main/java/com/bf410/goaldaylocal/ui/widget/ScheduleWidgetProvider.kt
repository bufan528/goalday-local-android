package com.bf410.goaldaylocal.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
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
                taskIds = listOf(
                    R.id.widget_task_1,
                    R.id.widget_task_2,
                    R.id.widget_task_3,
                    R.id.widget_task_4,
                ),
            )

        fun buildLargeRemoteViews(context: Context): RemoteViews =
            buildScheduleViews(
                context = context,
                layoutId = R.layout.widget_schedule_large,
                taskIds = listOf(
                    R.id.widget_task_1,
                    R.id.widget_task_2,
                    R.id.widget_task_3,
                    R.id.widget_task_4,
                    R.id.widget_task_5,
                    R.id.widget_task_6,
                    R.id.widget_task_7,
                    R.id.widget_task_8,
                ),
            )

        private fun buildScheduleViews(
            context: Context,
            layoutId: Int,
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
            val displayEntries = entries.take(taskIds.size)
            taskIds.forEachIndexed { index, id ->
                val entry = displayEntries.getOrNull(index)
                if (entry == null) {
                    views.setViewVisibility(id, View.GONE)
                } else {
                    views.setViewVisibility(id, View.VISIBLE)
                    val time = entry.timeText.takeIf { it.isNotBlank() }?.let { "$it " }.orEmpty()
                    val marker = if (entry.completed) "✓" else "○"
                    views.setTextViewText(id, "$marker $time${entry.title}")
                }
            }
            if (entries.isEmpty()) {
                views.setViewVisibility(R.id.widget_empty, View.VISIBLE)
            } else {
                views.setViewVisibility(R.id.widget_empty, View.GONE)
            }
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
