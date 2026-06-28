package com.bf410.goaldaylocal.ui.book

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class TargetDateShortcutsTest {
    @Test
    fun weekday_weekend_points_to_this_saturday() {
        val shortcuts = targetDateShortcuts(LocalDate.of(2026, 6, 24))

        assertEquals(24, shortcuts.today)
        assertEquals(25, shortcuts.tomorrow)
        assertEquals(27, shortcuts.weekend)
    }

    @Test
    fun sunday_weekend_stays_on_today() {
        val shortcuts = targetDateShortcuts(LocalDate.of(2026, 6, 28))

        assertEquals(28, shortcuts.today)
        assertEquals(29, shortcuts.tomorrow)
        assertEquals(28, shortcuts.weekend)
    }

    @Test
    fun month_end_shortcuts_do_not_overflow() {
        val shortcuts = targetDateShortcuts(LocalDate.of(2026, 6, 30))

        assertEquals(30, shortcuts.today)
        assertEquals(30, shortcuts.tomorrow)
        assertEquals(30, shortcuts.weekend)
    }
}
