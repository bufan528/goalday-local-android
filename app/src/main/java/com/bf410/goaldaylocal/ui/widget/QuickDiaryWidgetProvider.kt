package com.bf410.goaldaylocal.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.widget.RemoteViews
import com.bf410.goaldaylocal.EXTRA_START_TARGET
import com.bf410.goaldaylocal.MainActivity
import com.bf410.goaldaylocal.R
import com.bf410.goaldaylocal.START_TARGET_DIARY
import com.tencent.mmkv.MMKV
import java.time.LocalDate

class QuickDiaryWidgetProvider : AppWidgetProvider() {
    override fun onReceive(context: Context, intent: Intent) {
        runCatching {
            if (WidgetRefresh.refreshForSystemTimeChange(context, intent)) return
            super.onReceive(context, intent)
        }.onFailure { Log.w("QuickDiaryWidget", "组件广播处理失败：${intent.action}", it) }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        appWidgetIds.forEach { widgetId ->
            runCatching { appWidgetManager.updateAppWidget(widgetId, buildRemoteViews(context, widgetId)) }
                .onFailure { Log.w("QuickDiaryWidget", "更新组件 #$widgetId 失败", it) }
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        runCatching { appWidgetIds.forEach { ScheduleWidgetProvider.deleteConfig(it) } }
            .onFailure { Log.w("QuickDiaryWidget", "清理组件配置失败", it) }
    }

    companion object {
        fun buildRemoteViews(context: Context, widgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID): RemoteViews {
            val today = LocalDate.now()
            val views = RemoteViews(context.packageName, R.layout.widget_quick_diary)
            val style = ScheduleWidgetStyle.fromRaw(MMKV.defaultMMKV().decodeString("${ScheduleWidgetProvider.KEY_WIDGET_STYLE_PREFIX}$widgetId", null))
            views.setInt(R.id.quick_diary_root, "setBackgroundColor", style.backgroundColor)
            views.setTextViewText(R.id.quick_diary_title, "记录今天")
            views.setTextViewText(R.id.quick_diary_subtitle, "本地日记 · 无 VIP 锁")
            // 日期格式与日记组件统一（M月d日 周X），原来 M/d 两边对不上
            views.setTextViewText(R.id.quick_diary_date, DiaryAddWidgetProvider.diaryWidgetTitle(today))
            views.setTextViewText(R.id.quick_diary_hint, "补一条文字、目标或图片块，写完只保存在本机")
            views.setTextViewText(R.id.quick_diary_action, "打开手账")
            views.setTextColor(R.id.quick_diary_title, style.titleColor)
            views.setTextColor(R.id.quick_diary_subtitle, style.subtitleColor)
            views.setTextColor(R.id.quick_diary_date, style.accentColor)
            views.setTextColor(R.id.quick_diary_hint, style.doneTextColor)
            views.setTextColor(R.id.quick_diary_action, if (style == ScheduleWidgetStyle.APK_WHITE) style.backgroundColor else Color.WHITE)
            views.setOnClickPendingIntent(R.id.quick_diary_root, openDiaryPendingIntent(context, widgetId))
            views.setOnClickPendingIntent(R.id.quick_diary_action, openDiaryPendingIntent(context, widgetId))
            return views
        }

        private fun openDiaryPendingIntent(context: Context, widgetId: Int): PendingIntent {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(EXTRA_START_TARGET, START_TARGET_DIARY)
            }
            return PendingIntent.getActivity(
                context,
                12 + widgetId.coerceAtLeast(0),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }
    }
}
