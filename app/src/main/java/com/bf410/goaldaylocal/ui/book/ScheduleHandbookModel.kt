package com.bf410.goaldaylocal.ui.book

import com.bf410.goaldaylocal.data.BookPage
import com.bf410.goaldaylocal.data.ScheduleEntry
import java.time.LocalDate
import java.time.YearMonth

internal data class ScheduleDaySpreadBlock(
    val day: Int,
    val done: List<ScheduleEntry>,
    val todo: List<ScheduleEntry>,
)

internal data class ScheduleHandbookModel(
    val year: Int,
    val month: Int,
    val monthLength: Int,
    val windowStart: Int,
    val maxWindowStart: Int,
    val dayBlocks: List<ScheduleDaySpreadBlock>,
    val visibleDays: List<Int>,
    val visibleRangeLabel: String,
    val fallbackDone: List<String>,
    val fallbackTodo: List<String>,
    val visiblePoolItems: List<String>,
    val sortedEntries: List<ScheduleEntry>,
)

internal fun buildScheduleHandbookModel(
    page: BookPage,
    scheduleEntries: List<ScheduleEntry>,
    todayPlanItems: List<String>,
    todayCompletedItems: List<String>,
    requestedWindowStart: Int?,
    today: LocalDate = LocalDate.now(),
): ScheduleHandbookModel {
    val pageMonth = page.title.extractMonthNumber()
    val anchorMonth = pageMonth ?: scheduleEntries.firstOrNull()?.month ?: today.monthValue
    val anchorYear = scheduleEntries.firstOrNull { it.year == today.year && it.month == anchorMonth }?.year
        ?: scheduleEntries.firstOrNull { it.month == anchorMonth }?.year
        ?: scheduleEntries.firstOrNull()?.year
        ?: today.year
    val monthLength = YearMonth.of(anchorYear, anchorMonth).lengthOfMonth()
    val defaultStart = if (anchorYear == today.year && anchorMonth == today.monthValue) {
        (today.dayOfMonth - 1).coerceIn(0, monthLength - 1)
    } else {
        0
    }
    val maxStart = (monthLength - 3).coerceAtLeast(0)
    val start = (requestedWindowStart ?: defaultStart).coerceIn(0, maxStart)
    val sorted = scheduleEntries
        .filter { it.year == anchorYear && it.month == anchorMonth }
        .sortedWith(compareBy<ScheduleEntry>({ it.day }, { it.completed }, { it.title.lowercase() }, { it.id }))
    val dayBlocks = List(3) { offset ->
        val day = start + offset + 1
        val dayEntries = sorted.filter { it.day == day }
        ScheduleDaySpreadBlock(
            day = day,
            done = dayEntries.filter { it.completed }.take(5),
            todo = dayEntries.filterNot { it.completed }.take(5),
        )
    }
    val scheduledTitles = sorted.map { it.title }.toSet()
    return ScheduleHandbookModel(
        year = anchorYear,
        month = anchorMonth,
        monthLength = monthLength,
        windowStart = start,
        maxWindowStart = maxStart,
        dayBlocks = dayBlocks,
        visibleDays = dayBlocks.map { it.day },
        visibleRangeLabel = "${dayBlocks.first().day}-${dayBlocks.last().day}日",
        fallbackDone = todayCompletedItems.take(3),
        fallbackTodo = todayPlanItems.take(3),
        visiblePoolItems = todayPlanItems.filterNot { it in scheduledTitles }.take(6),
        sortedEntries = sorted,
    )
}

private fun String.extractMonthNumber(): Int? =
    Regex("(\\d{1,2})月")
        .find(this)
        ?.groupValues
        ?.getOrNull(1)
        ?.toIntOrNull()
        ?.coerceIn(1, 12)
