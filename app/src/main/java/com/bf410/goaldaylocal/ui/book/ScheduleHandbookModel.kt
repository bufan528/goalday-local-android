package com.bf410.goaldaylocal.ui.book

import com.bf410.goaldaylocal.data.BookPage
import com.bf410.goaldaylocal.data.ScheduleEntry
import java.time.LocalDate
import java.time.YearMonth

internal data class ScheduleTargetSlot(
    val id: String,
    val title: String,
    val completed: Boolean,
)

internal data class ScheduleDaySpreadBlock(
    val day: Int,
    val done: List<ScheduleEntry>,
    val todo: List<ScheduleEntry>,
    val targetSlots: List<ScheduleTargetSlot>,
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
    monthOffset: Int = 0,
): ScheduleHandbookModel {
    // P2-7 修复：书内日程页/月视图应始终基于当前实际月份，page.title 中的"X月日程"
    // 只是页面模板名称，不代表要显示的具体月份。以 today 为锚点，配合 monthOffset 跨月导航。
    val baseMonth = today.monthValue
    val baseYear = today.year
    val adjusted = YearMonth.of(baseYear, baseMonth).plusMonths(monthOffset.toLong())
    val anchorMonth = adjusted.monthValue
    val anchorYear = adjusted.year
    val monthLength = adjusted.lengthOfMonth()
    // 显示整月所有天：对照原版 RecyclerView 一次渲染整月日程列表
    val maxStart = 0
    val start = 0
    val sorted = scheduleEntries
        .filter { it.year == anchorYear && it.month == anchorMonth }
        .sortedWith(compareBy<ScheduleEntry>({ it.day }, { it.completed }))
    val dayBlocks = List(monthLength) { offset ->
        val day = start + offset + 1
        val dayEntries = sorted.filter { it.day == day }
        val todo = dayEntries.filterNot { it.completed }
        val done = dayEntries.filter { it.completed }
        ScheduleDaySpreadBlock(
            day = day,
            done = done.take(5),
            todo = todo.take(5),
            targetSlots = (todo + done).take(6).map { entry ->
                ScheduleTargetSlot(
                    id = entry.id,
                    title = entry.title,
                    completed = entry.completed,
                )
            },
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

internal fun String.extractMonthNumber(): Int? =
    Regex("(\\d{1,2})月")
        .find(this)
        ?.groupValues
        ?.getOrNull(1)
        ?.toIntOrNull()
        ?.coerceIn(1, 12)
