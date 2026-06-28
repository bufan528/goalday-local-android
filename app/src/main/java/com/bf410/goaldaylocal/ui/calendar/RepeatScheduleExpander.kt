package com.bf410.goaldaylocal.ui.calendar

import com.bf410.goaldaylocal.data.ScheduleEntry
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID

internal fun expandRepeatingScheduleEntry(entry: ScheduleEntry): List<ScheduleEntry> {
    val startDate = runCatching { LocalDate.of(entry.year, entry.month, entry.day) }.getOrNull() ?: return emptyList()
    val interval = entry.repeatInterval.coerceAtLeast(1)
    val explicitEndDate = parseRepeatEndDate(entry.repeatEndDate)?.takeIf { !it.isBefore(startDate) }

    return when (entry.repeatRule) {
        "daily" -> expandDailyOrWeekly(
            entry = entry,
            firstDate = startDate.plusDays(interval.toLong()),
            endDate = explicitEndDate ?: YearMonth.of(entry.year, entry.month).atEndOfMonth(),
            nextDate = { it.plusDays(interval.toLong()) },
        )
        "weekly" -> expandDailyOrWeekly(
            entry = entry,
            firstDate = startDate.plusWeeks(interval.toLong()),
            endDate = explicitEndDate ?: YearMonth.of(entry.year, entry.month).atEndOfMonth(),
            nextDate = { it.plusWeeks(interval.toLong()) },
        )
        "monthly" -> expandMonthly(entry, explicitEndDate, interval)
        else -> emptyList()
    }
}

internal fun normalizeRepeatEndDate(value: String): String =
    parseRepeatEndDate(value)?.toString().orEmpty()

private fun expandDailyOrWeekly(
    entry: ScheduleEntry,
    firstDate: LocalDate,
    endDate: LocalDate,
    nextDate: (LocalDate) -> LocalDate,
): List<ScheduleEntry> =
    generateSequence(firstDate, nextDate)
        .takeWhile { !it.isAfter(endDate) }
        .take(365)
        .map { date -> entry.copyForRepeatDate(date) }
        .toList()

private fun expandMonthly(
    entry: ScheduleEntry,
    explicitEndDate: LocalDate?,
    interval: Int,
): List<ScheduleEntry> {
    val startMonth = YearMonth.of(entry.year, entry.month)
    val maxItems = if (explicitEndDate == null) 11 else 36
    return generateSequence(startMonth.plusMonths(interval.toLong())) { it.plusMonths(interval.toLong()) }
        .take(maxItems)
        .mapNotNull { month ->
            val date = month.atDay(entry.day.coerceAtMost(month.lengthOfMonth()))
            if (explicitEndDate != null && date.isAfter(explicitEndDate)) {
                null
            } else {
                entry.copyForRepeatDate(date)
            }
        }
        .toList()
}

private fun ScheduleEntry.copyForRepeatDate(date: LocalDate): ScheduleEntry =
    copy(
        id = UUID.randomUUID().toString(),
        year = date.year,
        month = date.monthValue,
        day = date.dayOfMonth,
        completed = false,
    )

private fun parseRepeatEndDate(value: String): LocalDate? =
    runCatching { LocalDate.parse(value.trim()) }.getOrNull()
