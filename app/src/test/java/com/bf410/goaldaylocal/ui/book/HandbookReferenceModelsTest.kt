package com.bf410.goaldaylocal.ui.book

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HandbookReferenceModelsTest {
    @Test
    fun schedule_day_splits_six_slots_into_two_columns() {
        val day = buildHandbookScheduleDay(
            dayText = "05",
            weekText = "周日",
            items = listOf("写日记", "整理房间", "读书", "运动"),
        )

        assertEquals(listOf("写日记", "整理房间", "读书"), day.leftColumn.map { it.text })
        assertEquals(listOf("运动", "", ""), day.rightColumn.map { it.text })
        assertEquals((0 until 6).toList(), day.slots.map { it.index })
    }

    @Test
    fun schedule_day_marks_completed_only_for_non_blank_matching_items() {
        val day = buildHandbookScheduleDay(
            dayText = "05",
            weekText = "周日",
            items = listOf("写日记", "", "读书"),
            completedItems = setOf("写日记", ""),
        )

        assertTrue(day.slots[0].completed)
        assertFalse(day.slots[1].completed)
        assertFalse(day.slots[2].completed)
    }
}
