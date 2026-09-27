package com.bf410.goaldaylocal.ui.reminder

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.bf410.goaldaylocal.MainActivity
import com.bf410.goaldaylocal.R
import com.bf410.goaldaylocal.data.LocalStateStore
import com.bf410.goaldaylocal.data.ScheduleEntry
import com.bf410.goaldaylocal.ui.calendar.dayEntryTimeRank
import com.tencent.mmkv.MMKV
import java.time.LocalDate
import java.util.Calendar

/**
 * 每日提醒：早 8 点今日待办、晚 21 点当日剩余（含逾期未做）。
 *
 * 实现对照 DailyAgendaWorker 一类做法，但离线构建拉不到 WorkManager，
 * 只用框架能力：AlarmManager.setInexactRepeating（无需精确闹钟权限，
 *  Doze 下最多延迟一批次，提醒场景可接受）+ BroadcastReceiver + NotificationCompat。
 */
const val REMINDER_MORNING_HOUR = 8
const val REMINDER_EVENING_HOUR = 21

private const val CHANNEL_ID = "goalday_reminder"
private const val NOTIFICATION_ID_MORNING = 1001
private const val NOTIFICATION_ID_EVENING = 1002
private const val REQUEST_MORNING = 2001
private const val REQUEST_EVENING = 2002
private const val ACTION_MORNING = "com.bf410.goaldaylocal.action.REMINDER_MORNING"
private const val ACTION_EVENING = "com.bf410.goaldaylocal.action.REMINDER_EVENING"
private const val ACTION_ROLLOVER = "com.bf410.goaldaylocal.action.REMINDER_ROLLOVER"
private const val KEY_REMINDER_ENABLED = "reminder_daily_enabled"
private const val MAX_LISTED_TITLES = 4

enum class ReminderKind {
    MORNING,
    EVENING,
}

/** 提醒摘要（纯函数可单测）：未完成按逾期在前、当天在后，标题去重不断尾。 */
data class ReminderDigest(
    val kind: ReminderKind,
    val overdueTitles: List<String>,
    val todayTitles: List<String>,
) {
    val totalUndone: Int get() = overdueTitles.size + todayTitles.size
}

internal fun buildReminderDigest(entries: List<ScheduleEntry>, today: LocalDate): ReminderDigest {
    fun entryDate(e: ScheduleEntry) = runCatching { LocalDate.of(e.year, e.month, e.day) }.getOrNull()
    val undone = entries.filter { !it.completed && entryDate(it) != null }
    // 时间按数值排（09:00 不会掉到 10:00 后面），无时间沉底
    fun timeRank(e: ScheduleEntry): Int =
        if (e.timeText.isBlank()) Int.MAX_VALUE else dayEntryTimeRank(e.timeText, e.note)
    val overdue = undone.filter { entryDate(it)!!.isBefore(today) }
        .sortedWith(compareBy({ entryDate(it)!! }, { timeRank(it) }, { it.title }))
        .map { it.title.trim() }
        .filter { it.isNotBlank() }
        .distinct()
    val todayTodo = undone.filter { entryDate(it)!! == today }
        .sortedWith(compareBy({ timeRank(it) }, { it.title }))
        .map { it.title.trim() }
        .filter { it.isNotBlank() }
        .distinct()
    return ReminderDigest(ReminderKind.MORNING, overdue, todayTodo)
}

internal fun ReminderDigest.eveningCopy(): ReminderDigest = copy(kind = ReminderKind.EVENING)

/**
 * 逾期顺延到今天（纯函数可单测）：只动未完成且早于今天的条目；
 * 今天已有同名未完成的不重复搬（留在原地，避免成双）。
 */
internal fun rolloverOverdue(entries: List<ScheduleEntry>, today: LocalDate): List<ScheduleEntry> {
    fun entryDate(e: ScheduleEntry) = runCatching { LocalDate.of(e.year, e.month, e.day) }.getOrNull()
    val todayTodoTitles = entries
        .filter { !it.completed && entryDate(it) == today }
        .map { it.title.trim() }
        .toSet()
    return entries.map { e ->
        val date = entryDate(e)
        if (!e.completed && date != null && date.isBefore(today) && e.title.trim() !in todayTodoTitles) {
            e.copy(year = today.year, month = today.monthValue, day = today.dayOfMonth)
        } else {
            e
        }
    }
}

internal fun ReminderDigest.notificationTitle(): String = when (kind) {
    ReminderKind.MORNING -> if (overdueTitles.isNotEmpty()) {
        "逾期${overdueTitles.size}件，今日还有${todayTitles.size}件没做"
    } else {
        "今日还有${todayTitles.size}件事没做"
    }
    ReminderKind.EVENING -> if (totalUndone > 0) {
        "今天还剩${totalUndone}件事没做完"
    } else {
        "今日事项已清完"
    }
}

internal fun ReminderDigest.notificationContent(): String {
    val shown = (overdueTitles.map { "逾期·$it" } + todayTitles).take(MAX_LISTED_TITLES)
    val rest = totalUndone - shown.size
    return buildList {
        addAll(shown)
        if (rest > 0) add("等${rest}件")
    }.joinToString("\n")
}

object ReminderScheduler {
    fun isEnabled(mmkv: MMKV = MMKV.defaultMMKV()): Boolean =
        runCatching { mmkv.decodeBool(KEY_REMINDER_ENABLED, true) }.getOrDefault(true)

    fun setEnabled(context: Context, enabled: Boolean) {
        runCatching { MMKV.defaultMMKV().encode(KEY_REMINDER_ENABLED, enabled) }
        if (enabled) schedule(context) else cancel(context)
    }

    /** 开机/升级/安装后重排：幂等，先取消再设。 */
    fun schedule(context: Context) {
        cancel(context)
        val alarm = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        alarm.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            nextTriggerMillis(REMINDER_MORNING_HOUR),
            AlarmManager.INTERVAL_DAY,
            operationFor(context, ACTION_MORNING, REQUEST_MORNING),
        )
        alarm.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            nextTriggerMillis(REMINDER_EVENING_HOUR),
            AlarmManager.INTERVAL_DAY,
            operationFor(context, ACTION_EVENING, REQUEST_EVENING),
        )
    }

    fun cancel(context: Context) {
        val alarm = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        alarm.cancel(operationFor(context, ACTION_MORNING, REQUEST_MORNING))
        alarm.cancel(operationFor(context, ACTION_EVENING, REQUEST_EVENING))
    }

    private fun nextTriggerMillis(hour: Int): Long {
        val now = Calendar.getInstance()
        val next = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (!next.after(now)) next.add(Calendar.DAY_OF_YEAR, 1)
        return next.timeInMillis
    }

    private fun operationFor(context: Context, action: String, requestCode: Int): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).setAction(action)
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                if (ReminderScheduler.isEnabled()) ReminderScheduler.schedule(context)
            }
            ACTION_MORNING -> fireReminder(context, ReminderKind.MORNING)
            ACTION_EVENING -> fireReminder(context, ReminderKind.EVENING)
            ACTION_ROLLOVER -> rolloverOverdueToToday(context)
        }
    }
}

internal fun fireReminder(context: Context, kind: ReminderKind) {
    if (!ReminderScheduler.isEnabled()) return
    if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return
    val mmkv = runCatching { MMKV.defaultMMKV() }.getOrNull() ?: return
    val today = LocalDate.now()
    val digest = buildReminderDigest(LocalStateStore(mmkv).scheduleEntries(), today)
        .let { if (kind == ReminderKind.EVENING) it.eveningCopy() else it }
    // 没欠账不打扰：对照 DailyAgendaWorker 的空跳过
    if (digest.totalUndone == 0) return
    ensureChannel(context)
    val tap = PendingIntent.getActivity(
        context,
        3000 + kind.ordinal,
        Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        },
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_reward)
        .setContentTitle(digest.notificationTitle())
        .setContentText(digest.notificationContent().lines().firstOrNull().orEmpty())
        .setStyle(NotificationCompat.BigTextStyle().bigText(digest.notificationContent()))
        .setContentIntent(tap)
        .setAutoCancel(true)
        .setWhen(System.currentTimeMillis())
        // 关掉系统推测动作，只留手写的按钮
        .setAllowSystemGeneratedContextualActions(false)
    // 有逾期才给顺延按钮：点一下把它们搬到今天，不用进应用一条条改期
    if (digest.overdueTitles.isNotEmpty()) {
        val rollover = PendingIntent.getBroadcast(
            context,
            3000 + kind.ordinal + 10,
            Intent(context, ReminderReceiver::class.java).setAction(ACTION_ROLLOVER),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        builder.addAction(android.R.drawable.ic_menu_today, "顺延到今天", rollover)
    }
    val notification = builder.build()
    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
    manager.notify(if (kind == ReminderKind.MORNING) NOTIFICATION_ID_MORNING else NOTIFICATION_ID_EVENING, notification)
}

/** 通知上的“顺延到今天”：把逾期未做搬到今天，刷掉通知并刷新组件。 */
internal fun rolloverOverdueToToday(context: Context) {
    val mmkv = runCatching { MMKV.defaultMMKV() }.getOrNull() ?: return
    val store = LocalStateStore(mmkv)
    val today = LocalDate.now()
    val moved = rolloverOverdue(store.scheduleEntries(), today)
    com.bf410.goaldaylocal.data.ScheduleRepository.getInstance(store).saveEntries(moved)
    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
    manager?.cancel(NOTIFICATION_ID_MORNING)
    manager?.cancel(NOTIFICATION_ID_EVENING)
    android.widget.Toast.makeText(context, "逾期事项已顺延到今天", android.widget.Toast.LENGTH_SHORT).show()
}

private fun ensureChannel(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
    if (manager.getNotificationChannel(CHANNEL_ID) == null) {
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "每日提醒", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "早晚各一次：今日待办与逾期未做事项"
            },
        )
    }
}
