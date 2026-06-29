package com.bf410.goaldaylocal.ui.calendar

import com.bf410.goaldaylocal.data.ScheduleEntry
import org.junit.Assert.assertEquals
import org.junit.Test

class CalendarScheduleMutationsTest {
    @Test
    fun remove_single_schedule_keeps_other_repeat_instances() {
        val next = removeScheduleEntries(
            entries = listOf(
                entry("a", group = "g"),
                entry("b", group = "g"),
                entry("c", group = "other"),
            ),
            id = "a",
            applySeries = false,
        )

        assertEquals(listOf("b", "c"), next.map { it.id })
    }

    @Test
    fun remove_series_deletes_same_repeat_group_only() {
        val next = removeScheduleEntries(
            entries = listOf(
                entry("a", group = "g"),
                entry("b", group = "g"),
                entry("c", group = "other"),
                entry("d", group = ""),
            ),
            id = "a",
            applySeries = true,
        )

        assertEquals(listOf("c", "d"), next.map { it.id })
    }

    @Test
    fun remove_series_without_group_falls_back_to_single_entry() {
        val next = removeScheduleEntries(
            entries = listOf(entry("a", group = ""), entry("b", group = "")),
            id = "a",
            applySeries = true,
        )

        assertEquals(listOf("b"), next.map { it.id })
    }

    private fun entry(id: String, group: String): ScheduleEntry =
        ScheduleEntry(
            id = id,
            title = id,
            year = 2026,
            month = 6,
            day = 1,
            repeatGroupId = group,
        )
}
