package com.bf410.goaldaylocal.ui.book

import java.time.LocalDate
import java.time.YearMonth

internal data class TargetDateShortcuts(
    val today: Int,
    val tomorrow: Int,
    val weekend: Int,
)

internal fun targetDateShortcuts(today: LocalDate = LocalDate.now()): TargetDateShortcuts {
    val monthEnd = YearMonth.from(today).lengthOfMonth()
    val day = today.dayOfMonth.coerceIn(1, monthEnd)
    val weekendOffset = when (today.dayOfWeek.value) {
        6, 7 -> 0
        else -> 6 - today.dayOfWeek.value
    }
    return TargetDateShortcuts(
        today = day,
        tomorrow = (day + 1).coerceAtMost(monthEnd),
        weekend = (day + weekendOffset).coerceAtMost(monthEnd),
    )
}
