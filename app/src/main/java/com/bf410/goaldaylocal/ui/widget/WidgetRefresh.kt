package com.bf410.goaldaylocal.ui.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context

object WidgetRefresh {
    fun refreshScheduleWidgets(context: Context) {
        val manager = AppWidgetManager.getInstance(context)
        refreshProvider(context, manager, ScheduleWidgetProvider::class.java) { id ->
            ScheduleWidgetProvider.buildRemoteViews(context, id)
        }
        refreshProvider(context, manager, LargeScheduleWidgetProvider::class.java) { id ->
            ScheduleWidgetProvider.buildLargeRemoteViews(context, id)
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
}
