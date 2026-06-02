package com.bf410.goaldaylocal.ui.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context

object WidgetRefresh {
    fun refreshScheduleWidgets(context: Context) {
        val manager = AppWidgetManager.getInstance(context)
        refreshProvider(context, manager, ScheduleWidgetProvider::class.java) {
            ScheduleWidgetProvider.buildRemoteViews(context)
        }
        refreshProvider(context, manager, LargeScheduleWidgetProvider::class.java) {
            ScheduleWidgetProvider.buildLargeRemoteViews(context)
        }
    }

    private fun refreshProvider(
        context: Context,
        manager: AppWidgetManager,
        providerClass: Class<*>,
        buildViews: () -> android.widget.RemoteViews,
    ) {
        val ids = manager.getAppWidgetIds(ComponentName(context, providerClass))
        ids.forEach { id -> manager.updateAppWidget(id, buildViews()) }
    }
}
