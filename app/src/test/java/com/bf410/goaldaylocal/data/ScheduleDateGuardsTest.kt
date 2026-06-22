package com.bf410.goaldaylocal.data

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class ScheduleDateGuardsTest {
    private val fallback = LocalDate.of(2026, 6, 15)

    @Test
    fun safeScheduleDateKeepsValidLeapDay() {
        val date = safeScheduleDate(2024, 2, 29, fallback)

        assertEquals(SafeScheduleDate(2024, 2, 29), date)
    }

    @Test
    fun safeScheduleDateClampsDayToMonthEnd() {
        val date = safeScheduleDate(2026, 2, 31, fallback)

        assertEquals(SafeScheduleDate(2026, 2, 28), date)
    }

    @Test
    fun safeScheduleDateFallsBackForInvalidMonth() {
        val date = safeScheduleDate(2026, 18, 31, fallback)

        assertEquals(SafeScheduleDate(2026, 6, 30), date)
    }

    @Test
    fun safeScheduleDateFallsBackForOutOfRangeYear() {
        val date = safeScheduleDate(3026, 2, 1, fallback)

        assertEquals(SafeScheduleDate(2026, 2, 1), date)
    }

    @Test
    fun safeScheduleMonthUsesFallbackMonthWhenInvalid() {
        assertEquals(6, safeScheduleMonth(0, fallback))
        assertEquals(12, safeScheduleMonth(12, fallback))
    }
}
