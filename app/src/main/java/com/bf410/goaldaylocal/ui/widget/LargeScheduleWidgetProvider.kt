package com.bf410.goaldaylocal.ui.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.Log

class LargeScheduleWidgetProvider : AppWidgetProvider() {
    override fun onReceive(context: Context, intent: Intent) {
        runCatching {
            if (WidgetRefresh.refreshForSystemTimeChange(context, intent)) return
            super.onReceive(context, intent)
        }.onFailure { Log.w("LargeScheduleWidget", "组件广播处理失败：${intent.action}", it) }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        appWidgetIds.forEach { widgetId ->
            runCatching {
                appWidgetManager.updateAppWidget(widgetId, ScheduleWidgetProvider.buildLargeRemoteViews(context, widgetId))
            }.onFailure { Log.w("LargeScheduleWidget", "更新组件 #$widgetId 失败", it) }
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        runCatching { appWidgetIds.forEach { ScheduleWidgetProvider.deleteConfig(it) } }
            .onFailure { Log.w("LargeScheduleWidget", "清理组件配置失败", it) }
    }
}
