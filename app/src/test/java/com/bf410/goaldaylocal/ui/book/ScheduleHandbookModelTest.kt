package com.bf410.goaldaylocal.ui.book

import com.bf410.goaldaylocal.data.ScheduleEntry
import com.bf410.goaldaylocal.data.SchedulePage
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class ScheduleHandbookModelTest {
    @Test
    fun title_month_prefers_current_year_and_filters_entries_by_year_and_month() {
        val model = buildScheduleHandbookModel(
            page = SchedulePage("6月日程", emptyList()),
            scheduleEntries = listOf(
                entry("old", "去年六月", 2025, 6, 2),
                entry("target", "今年六月", 2026, 6, 2),
                entry("other", "今年七月", 2026, 7, 2),
            ),
            todayPlanItems = emptyList(),
            todayCompletedItems = emptyList(),
            requestedWindowStart = 1,
            today = LocalDate.of(2026, 6, 28),
        )

        assertEquals(2026, model.year)
        assertEquals(6, model.month)
        assertEquals(listOf("今年六月"), model.sortedEntries.map { it.title })
    }

    @Test
    fun window_start_is_clamped_to_month_end() {
        val model = buildScheduleHandbookModel(
            page = SchedulePage("2月日程", emptyList()),
            scheduleEntries = listOf(entry("a", "月末任务", 2026, 2, 28)),
            todayPlanItems = emptyList(),
            todayCompletedItems = emptyList(),
            requestedWindowStart = 99,
            today = LocalDate.of(2026, 6, 28),
        )

        assertEquals(25, model.windowStart)
        assertEquals(listOf(26, 27, 28), model.visibleDays)
        assertEquals("26-28日", model.visibleRangeLabel)
    }

    @Test
    fun pool_hides_items_already_scheduled_in_visible_month_only() {
        val model = buildScheduleHandbookModel(
            page = SchedulePage("6月日程", emptyList()),
            scheduleEntries = listOf(entry("a", "已排", 2026, 6, 3)),
            todayPlanItems = listOf("已排", "未排", "别月也叫已排"),
            todayCompletedItems = listOf("完成一", "完成二", "完成三", "完成四"),
            requestedWindowStart = 0,
            today = LocalDate.of(2026, 6, 28),
        )

        assertFalse("已排" in model.visiblePoolItems)
        assertEquals(listOf("未排", "别月也叫已排"), model.visiblePoolItems)
        assertEquals(listOf("完成一", "完成二", "完成三"), model.fallbackDone)
    }

    private fun entry(
        id: String,
        title: String,
        year: Int,
        month: Int,
        day: Int,
        completed: Boolean = false,
    ) = ScheduleEntry(
        id = id,
        title = title,
        year = year,
        month = month,
        day = day,
        completed = completed,
    )
}
