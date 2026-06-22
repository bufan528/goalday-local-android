package com.bf410.goaldaylocal.data

import java.time.LocalDate
import java.time.YearMonth

internal const val MIN_SCHEDULE_YEAR = 1970
internal const val MAX_SCHEDULE_YEAR = 2100

internal data class SafeScheduleDate(
    val year: Int,
    val month: Int,
    val day: Int,
)

internal fun safeScheduleDate(
    year: Int,
    month: Int,
    day: Int,
    fallback: LocalDate = LocalDate.now(),
): SafeScheduleDate {
    val safeYear = safeScheduleYear(year, fallback)
    val safeMonth = safeScheduleMonth(month, fallback)
    val safeDay = day.coerceIn(1, YearMonth.of(safeYear, safeMonth).lengthOfMonth())
    return SafeScheduleDate(safeYear, safeMonth, safeDay)
}

internal fun safeScheduleYear(
    year: Int,
    fallback: LocalDate = LocalDate.now(),
): Int {
    val fallbackYear = fallback.year.coerceIn(MIN_SCHEDULE_YEAR, MAX_SCHEDULE_YEAR)
    return year.takeIf { it in MIN_SCHEDULE_YEAR..MAX_SCHEDULE_YEAR } ?: fallbackYear
}

internal fun safeScheduleMonth(
    month: Int,
    fallback: LocalDate = LocalDate.now(),
): Int =
    month.takeIf { it in 1..12 } ?: fallback.monthValue.coerceIn(1, 12)
