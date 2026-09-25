package com.bf410.goaldaylocal.ui.calendar

import com.bf410.goaldaylocal.data.ScheduleEntry

internal fun sortCalendarEntries(entries: List<ScheduleEntry>): List<ScheduleEntry> =
    entries.sortedWith(calendarEntryComparator())

internal fun calendarEntryComparator(): Comparator<ScheduleEntry> =
    compareBy<ScheduleEntry>(
        { it.day },
        { it.completed },
        { scheduleTimeRank(it) },
        { it.title.lowercase() },
        { it.id },
    )

internal fun sortDayEntries(entries: List<ScheduleEntry>): List<ScheduleEntry> =
    entries.sortedWith(
        compareBy<ScheduleEntry>(
            { it.completed },
            { scheduleTimeRank(it) },
            { it.title.lowercase() },
            { it.id },
        ),
    )

private fun scheduleTimeRank(entry: ScheduleEntry): Int =
    dayEntryTimeRank(entry.timeText, entry.note)

/** 当天条目时间先后（09:30 不会排到 10:00 后面；无时间走时段/默认末尾，与日历旧逻辑一致） */
internal fun dayEntryTimeRank(timeText: String, note: String): Int {
    val time = timeText.trim()
    val hourMinute = Regex("""^(\d{1,2}):(\d{2})""").find(time)
    if (hourMinute != null) {
        val hour = hourMinute.groupValues[1].toIntOrNull()?.coerceIn(0, 23) ?: 0
        val minute = hourMinute.groupValues[2].toIntOrNull()?.coerceIn(0, 59) ?: 0
        return hour * 60 + minute
    }
    return when {
        "上午" in note -> 8 * 60
        "下午" in note -> 14 * 60
        "晚上" in note || "晚间" in note -> 19 * 60
        else -> 24 * 60
    }
}
