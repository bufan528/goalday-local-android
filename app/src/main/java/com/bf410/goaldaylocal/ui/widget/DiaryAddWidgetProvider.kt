package com.bf410.goaldaylocal.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.TypedValue
import android.widget.RemoteViews
import com.bf410.goaldaylocal.EXTRA_START_TARGET
import com.bf410.goaldaylocal.MainActivity
import com.bf410.goaldaylocal.R
import com.bf410.goaldaylocal.START_TARGET_DIARY
import com.bf410.goaldaylocal.data.LocalStateStore
import com.tencent.mmkv.MMKV
import java.time.LocalDate

/**
 * 日记小组件（原版映射）。
 *
 * 对照原版 `widgetmodule/widget/DiaryAddWidget` + `DiaryWidgetUpdateService`：
 * - 布局即 `R.layout.diary_add_widget`（日期 12sp #EE5C51 + 正文 12sp #4D000000 + 右下角 + 图标），
 *   锁定时换 `R.layout.unlock_diary_add_widget`（同布局 + 锁形遮罩，对照 `getUnlockLayout`）；
 * - 标题=当天日期 `M月d日 EEE`（对照 `TimeUtils "M月d日 ccc"`）；
 * - 正文=今天日记正文去 `<img>` 标签后截前 50 字（对照 `WidgetUtils.formatStr(content, 50)`），
 *   为空回落 `@string/appwidget_today_want_write`（对照布局默认值与刷新服务）；
 * - 点整块进日记（对照 `cur_pos=0` 跳主界面；开发版用记录 Tab 直达）；
 * - API 31+ 固定根高 155dp（对照 `setViewLayoutHeight(root, 155dp)`）。
 *
 * 锁定语义：原版未解锁（VIP）时显示锁形遮罩；本地版无 VIP 门槛，恒为解锁（对照 QuickDiary 的"无 VIP 锁"），
 * 锁分支仅保留结构对等（`isDiaryWidgetLocked()`）。
 */
class DiaryAddWidgetProvider : AppWidgetProvider() {
    override fun onReceive(context: Context, intent: Intent) {
        if (WidgetRefresh.refreshForSystemTimeChange(context, intent)) return
        super.onReceive(context, intent)
    }

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
        internal const val MAX_CONTENT_CHARS = 50
        internal const val WIDGET_HEIGHT_DP = 155f
        private const val DIARY_BOOK_ID = "diary"

        private val WEEKDAY_NAMES = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")

        /** 本地版恒解锁（对照原版 VIP 锁形遮罩；无门槛故恒 false，分支保留对等）。 */
        internal fun isDiaryWidgetLocked(): Boolean = false

        /** 标题：M月d日 EEE（对照原版 `"M月d日 ccc"`，如 9月16日 周三）。 */
        internal fun diaryWidgetTitle(date: LocalDate): String {
            val weekday = WEEKDAY_NAMES.getOrElse(date.dayOfWeek.value - 1) { "" }
            return "${date.monthValue}月${date.dayOfMonth}日 $weekday"
        }

        /**
         * 日记存储原文取用户正文（对照 `diaryUserText`：无 `# ` 结构即整段原文，
         * 否则取 `# 富文本` 分区到下一分区分界）。
         */
        internal fun diaryWidgetUserText(raw: String): String {
            if (raw.isBlank()) return ""
            if (!raw.contains("# ")) return raw
            val start = raw.indexOf("# 富文本")
            if (start < 0) return ""
            val bodyStart = raw.indexOf('\n', start).takeIf { it >= 0 }?.plus(1) ?: return ""
            val next = raw.indexOf("# ", bodyStart).takeIf { it >= 0 } ?: raw.length
            return raw.substring(bodyStart, next).trim()
        }

        /**
         * 正文成块：去 `<img>` 标签后截前 50 字（对照 `WidgetUtils.formatStr(content, 50)`）；
         * 为空回落占位文案（对照 `appwidget_today_want_write`）。
         */
        internal fun diaryWidgetContent(rawDiary: String, placeholder: String): String {
            val text = diaryWidgetUserText(rawDiary)
            if (text.isBlank()) return placeholder
            val stripped = text.replace(Regex("<img[^>]*>"), "")
            if (stripped.isBlank()) return placeholder
            return stripped.substring(0, minOf(MAX_CONTENT_CHARS, stripped.length))
        }

        fun buildRemoteViews(context: Context, widgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID): RemoteViews {
            val today = LocalDate.now()
            val placeholder = context.getString(R.string.appwidget_today_want_write)
            return if (isDiaryWidgetLocked()) {
                RemoteViews(context.packageName, R.layout.unlock_diary_add_widget).apply {
                    applyFixedHeight(context)
                    setTextViewText(R.id.tv_title, diaryWidgetTitle(today))
                    setTextViewText(R.id.tv_content, placeholder)
                }
            } else {
                val store = LocalStateStore(MMKV.defaultMMKV())
                val content = diaryWidgetContent(store.diaryText(DIARY_BOOK_ID, today.toString()), placeholder)
                RemoteViews(context.packageName, R.layout.diary_add_widget).apply {
                    applyFixedHeight(context)
                    setTextViewText(R.id.tv_title, diaryWidgetTitle(today))
                    setTextViewText(R.id.tv_content, content)
                    setOnClickPendingIntent(R.id.root, openDiaryPendingIntent(context, widgetId))
                }
            }
        }

        private fun RemoteViews.applyFixedHeight(context: Context) {
            if (Build.VERSION.SDK_INT >= 31) {
                // 对照原版 setViewLayoutHeight(root, 155dp→px, 0=PX)
                val px = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    WIDGET_HEIGHT_DP,
                    context.resources.displayMetrics,
                )
                setViewLayoutHeight(R.id.root, px, 0)
            }
        }

        private fun openDiaryPendingIntent(context: Context, widgetId: Int): PendingIntent {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(EXTRA_START_TARGET, START_TARGET_DIARY)
            }
            return PendingIntent.getActivity(
                context,
                1000 + widgetId.coerceAtLeast(0),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }
    }
}
