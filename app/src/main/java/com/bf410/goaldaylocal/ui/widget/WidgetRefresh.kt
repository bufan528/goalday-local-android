package com.bf410.goaldaylocal.ui.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent

object WidgetRefresh {
    fun refreshForSystemTimeChange(context: Context, intent: Intent?): Boolean {
        val action = intent?.action ?: return false
        if (action !in REFRESH_ACTIONS) return false
        refreshScheduleWidgets(context)
        return true
    }

    fun refreshScheduleWidgets(context: Context) {
        val manager = AppWidgetManager.getInstance(context)
        refreshProvider(context, manager, ScheduleWidgetProvider::class.java) { id ->
            ScheduleWidgetProvider.buildRemoteViews(context, id)
        }
        refreshProvider(context, manager, LargeScheduleWidgetProvider::class.java) { id ->
            ScheduleWidgetProvider.buildLargeRemoteViews(context, id)
        }
        refreshProvider(context, manager, QuickDiaryWidgetProvider::class.java) { id ->
            QuickDiaryWidgetProvider.buildRemoteViews(context, id)
        }
    }

    private fun refreshProvider(
        context: Context,
        manager: AppWidgetManager,
        providerClass: Class<*>,
        buildViews: (Int) -> android.widget.RemoteViews,
    ) {
        val ids = manager.getAppWidgetIds(ComponentName(context, providerClass))
        ids.forEach { id -> manager.updateAppWidget(id, buildViews(id)) }
    }

    private val REFRESH_ACTIONS = setOf(
        Intent.ACTION_DATE_CHANGED,
        Intent.ACTION_TIME_CHANGED,
        Intent.ACTION_TIMEZONE_CHANGED,
        Intent.ACTION_LOCALE_CHANGED,
    )
}
