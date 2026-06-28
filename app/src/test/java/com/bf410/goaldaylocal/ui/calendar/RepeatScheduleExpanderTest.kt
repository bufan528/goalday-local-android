package com.bf410.goaldaylocal.ui.calendar

import com.bf410.goaldaylocal.data.ScheduleEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class RepeatScheduleExpanderTest {
    @Test
    fun daily_repeat_without_end_stays_inside_start_month() {
        val additions = expandRepeatingScheduleEntry(
            baseEntry(day = 28, repeatRule = "daily"),
        )

        assertEquals(listOf(29, 30), additions.map { it.day })
        assertEquals(listOf(6, 6), additions.map { it.month })
        assertFalse(additions.any { it.completed })
    }

    @Test
    fun weekly_repeat_with_end_includes_dates_until_end_date() {
        val additions = expandRepeatingScheduleEntry(
            baseEntry(day = 3, repeatRule = "weekly", repeatEndDate = "2026-06-24"),
        )

        assertEquals(listOf(10, 17, 24), additions.map { it.day })
        assertEquals(listOf(6, 6, 6), additions.map { it.month })
    }

    @Test
    fun monthly_repeat_clamps_to_month_end_when_original_day_is_missing() {
        val additions = expandRepeatingScheduleEntry(
            baseEntry(year = 2026, month = 1, day = 31, repeatRule = "monthly", repeatEndDate = "2026-03-31"),
        )

        assertEquals(listOf(2, 3), additions.map { it.month })
        assertEquals(listOf(28, 31), additions.map { it.day })
    }

    @Test
    fun monthly_repeat_respects_end_date_after_clamping() {
        val additions = expandRepeatingScheduleEntry(
            baseEntry(year = 2026, month = 1, day = 31, repeatRule = "monthly", repeatEndDate = "2026-02-27"),
        )

        assertEquals(emptyList<ScheduleEntry>(), additions)
    }

    @Test
    fun invalid_end_date_is_normalized_to_blank() {
        assertEquals("", normalizeRepeatEndDate("2026-99-99"))
        assertEquals("2026-06-30", normalizeRepeatEndDate(" 2026-06-30 "))
    }

    private fun baseEntry(
        year: Int = 2026,
        month: Int = 6,
        day: Int = 1,
        repeatRule: String,
        repeatEndDate: String = "",
    ): ScheduleEntry =
        ScheduleEntry(
            id = "source",
            title = "Yoga",
            year = year,
            month = month,
            day = day,
            note = "stretch",
            timeText = "08:00",
            repeatRule = repeatRule,
            repeatInterval = 1,
            repeatEndDate = repeatEndDate,
            repeatGroupId = "group",
            completed = true,
        )
}
