package com.bf410.goaldaylocal.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.bf410.goaldaylocal.EXTRA_START_TARGET
import com.bf410.goaldaylocal.MainActivity
import com.bf410.goaldaylocal.R
import com.bf410.goaldaylocal.START_TARGET_DIARY
import java.time.LocalDate

class QuickDiaryWidgetProvider : AppWidgetProvider() {
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
        fun buildRemoteViews(context: Context): RemoteViews {
            val today = LocalDate.now()
            val views = RemoteViews(context.packageName, R.layout.widget_quick_diary)
            views.setTextViewText(R.id.quick_diary_title, "记录今天")
            views.setTextViewText(R.id.quick_diary_subtitle, "打开日记页 · 本地保存")
            views.setTextViewText(R.id.quick_diary_date, "${today.monthValue}/${today.dayOfMonth}")
            views.setTextViewText(R.id.quick_diary_hint, "补一条文字、目标或图片块")
            views.setOnClickPendingIntent(R.id.quick_diary_root, openDiaryPendingIntent(context))
            views.setOnClickPendingIntent(R.id.quick_diary_action, openDiaryPendingIntent(context))
            return views
        }

        private fun openDiaryPendingIntent(context: Context): PendingIntent {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(EXTRA_START_TARGET, START_TARGET_DIARY)
            }
            return PendingIntent.getActivity(
                context,
                12,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }
    }
}
