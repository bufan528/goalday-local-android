package com.bf410.goaldaylocal.ui.calendar

import com.bf410.goaldaylocal.data.ScheduleEntry
import org.junit.Assert.assertEquals
import org.junit.Test

class CalendarEntryOrderingTest {
    @Test
    fun day_entries_show_todo_before_done_and_time_before_title() {
        val ordered = sortDayEntries(
            listOf(
                entry("done", "A done", completed = true, timeText = "08:00"),
                entry("night", "Night", note = "slot:晚上"),
                entry("morning", "Morning", note = "slot:上午"),
                entry("timed", "Timed", timeText = "07:30"),
                entry("plain", "Plain"),
            ),
        )

        assertEquals(listOf("timed", "morning", "night", "plain", "done"), ordered.map { it.id })
    }

    @Test
    fun month_entries_group_by_day_then_pending_status_then_time() {
        val ordered = sortCalendarEntries(
            listOf(
                entry("day2", "Day two", day = 2),
                entry("done", "Done first day", day = 1, completed = true, timeText = "06:00"),
                entry("todo", "Todo first day", day = 1, timeText = "18:00"),
            ),
        )

        assertEquals(listOf("todo", "done", "day2"), ordered.map { it.id })
    }

    private fun entry(
        id: String,
        title: String,
        day: Int = 1,
        completed: Boolean = false,
        timeText: String = "",
        note: String = "",
    ): ScheduleEntry =
        ScheduleEntry(
            id = id,
            title = title,
            year = 2026,
            month = 6,
            day = day,
            completed = completed,
            timeText = timeText,
            note = note,
        )
}
