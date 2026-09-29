package com.bf410.goaldaylocal.ui.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log

object WidgetRefresh {
    fun refreshForSystemTimeChange(context: Context, intent: Intent?): Boolean {
        val action = intent?.action ?: return false
        if (action !in REFRESH_ACTIONS) return false
        refreshScheduleWidgets(context)
        return true
    }

    /**
     * 组件刷新多由 TIME_SET/TIMEZONE_CHANGED 广播触发，此时没有前台 Activity 接异常：
     * 任一 buildRemoteViews 抛出去就是整进程崩溃。这里逐个 provider、逐个组件隔离，
     * 一个组件数据异常只影响它自己。
     */
    fun refreshScheduleWidgets(context: Context) {
        val appContext = context.applicationContext
        val manager = runCatching { AppWidgetManager.getInstance(appContext) }.getOrNull() ?: return
        refreshProvider(appContext, manager, ScheduleWidgetProvider::class.java) { id ->
            ScheduleWidgetProvider.buildRemoteViews(appContext, id)
        }
        refreshProvider(appContext, manager, LargeScheduleWidgetProvider::class.java) { id ->
            ScheduleWidgetProvider.buildLargeRemoteViews(appContext, id)
        }
        refreshProvider(appContext, manager, QuickDiaryWidgetProvider::class.java) { id ->
            QuickDiaryWidgetProvider.buildRemoteViews(appContext, id)
        }
        refreshProvider(appContext, manager, DiaryAddWidgetProvider::class.java) { id ->
            DiaryAddWidgetProvider.buildRemoteViews(appContext, id)
        }
    }

    private fun refreshProvider(
        context: Context,
        manager: AppWidgetManager,
        providerClass: Class<*>,
        buildViews: (Int) -> android.widget.RemoteViews,
    ) {
        val ids = runCatching {
            manager.getAppWidgetIds(ComponentName(context, providerClass))
        }.getOrNull() ?: return
        ids.forEach { id ->
            runCatching { manager.updateAppWidget(id, buildViews(id)) }
                .onFailure { Log.w("WidgetRefresh", "刷新 ${providerClass.simpleName} #$id 失败", it) }
        }
    }

    private val REFRESH_ACTIONS = setOf(
        Intent.ACTION_DATE_CHANGED,
        Intent.ACTION_TIME_CHANGED,
        Intent.ACTION_TIMEZONE_CHANGED,
        Intent.ACTION_LOCALE_CHANGED,
    )
}
