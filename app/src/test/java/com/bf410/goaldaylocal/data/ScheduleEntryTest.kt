package com.bf410.goaldaylocal.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScheduleEntryTest {
    @Test
    fun status_reflects_completed_flag() {
        val planned = entry(completed = false)
        val done = entry(completed = true)

        assertEquals(ScheduleStatus.PLANNED, planned.status)
        assertEquals(ScheduleStatus.DONE, done.status)
    }

    @Test
    fun with_status_updates_legacy_completed_flag() {
        val planned = entry(completed = false)
        val done = planned.withStatus(ScheduleStatus.DONE)
        val restored = done.withStatus(ScheduleStatus.PLANNED)

        assertTrue(done.completed)
        assertEquals(ScheduleStatus.DONE, done.status)
        assertFalse(restored.completed)
        assertEquals(ScheduleStatus.PLANNED, restored.status)
    }

    private fun entry(completed: Boolean) = ScheduleEntry(
        id = "entry",
        title = "Plan",
        year = 2026,
        month = 6,
        day = 2,
        completed = completed,
    )
}
