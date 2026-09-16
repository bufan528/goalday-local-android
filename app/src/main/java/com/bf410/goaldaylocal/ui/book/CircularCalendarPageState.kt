package com.bf410.goaldaylocal.ui.book

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId

/**
 * 书芯单页：日期 + 是否日程面（对照原版 CalendarPage(date, isSchedule)）。
 * isSchedule=true 渲染该日日程列表，false 渲染该日日记页。
 */
data class DayPage(
    val date: LocalDate,
    val isSchedule: Boolean = false,
)

/**
 * 循环日历页面状态（逐行移植原版 CircularCalendarPageState.kt，jadx 2.5.7）。
 *
 * 10 页环形缓冲，开书可见 spread = 第 4/5 页（左/右）。页表由中心日期重建：
 * - 从中心向后退 6 页、向前进 4 页；
 * - 周一那天会产生相邻两页 (周一, 日程面) + (周一, 日记面)，
 *   因此中心落在周一时 spread 为 [周一日程 | 周一日记]，其余中心为 [D-1 日记 | D 日记]；
 * - 翻页步长按原版 onPageTurned：NEXT = 中心周日 +1 天否则 +2 天，
 *   PREV = 中心周一 -1 天否则 -2 天；4 次翻页循环一周（+7 天）。
 */
class CircularCalendarPageState(
    initialCenterDate: LocalDate = LocalDate.now(),
    private var dateRange: BookPageDateRange? = null,
) {
    private var center: LocalDate = initialCenterDate
    val centerDate: LocalDate get() = center

    private var pages: MutableList<DayPage> = MutableList(10) { DayPage(initialCenterDate) }

    init {
        rebuild()
    }

    fun getPage(index: Int): DayPage = pages[index.coerceIn(0, 9)]

    /** 开书 spread 左页 */
    val leftPage: DayPage get() = pages[4]

    /** 开书 spread 右页 */
    val rightPage: DayPage get() = pages[5]

    fun getCurrentPages(): List<DayPage> = pages.toList()

    fun canGoNext(): Boolean = inRange(nextCenter())

    fun canGoPrevious(): Boolean = inRange(prevCenter())

    fun goNextPage() {
        if (!canGoNext()) return
        center = nextCenter()
        rebuild()
    }

    fun goPreviousPage() {
        if (!canGoPrevious()) return
        center = prevCenter()
        rebuild()
    }

    /** 翻页完成后的 spread（用于渲染翻页背面落点页） */
    fun nextSpread(): Pair<DayPage, DayPage> = spreadAt(nextCenter())

    fun prevSpread(): Pair<DayPage, DayPage> = spreadAt(prevCenter())

    fun jumpToDate(target: LocalDate) {
        val range = dateRange
        if (range != null && !range.containsLocal(target)) return
        center = target
        rebuild()
    }

    fun setDateRange(range: BookPageDateRange) {
        dateRange = range
        if (!range.containsLocal(center)) {
            center = range.startLocal()
            rebuild()
        }
    }

    private fun nextCenter(): LocalDate =
        if (center.dayOfWeek == DayOfWeek.SUNDAY) center.plusDays(1) else center.plusDays(2)

    private fun prevCenter(): LocalDate =
        if (center.dayOfWeek == DayOfWeek.MONDAY) center.minusDays(1) else center.minusDays(2)

    private fun inRange(date: LocalDate): Boolean {
        val range = dateRange ?: return true
        return range.containsLocal(date)
    }

    /** 对照原版 updatePageCache：i=5..0 回退填页，再 6..9 前进填页 */
    private fun rebuild() {
        val arr = arrayOfNulls<DayPage>(10)
        var i = 5
        var value = center
        var mondayPending = false
        while (i >= 0) {
            arr[i] = if (value.dayOfWeek == DayOfWeek.MONDAY && mondayPending) {
                DayPage(value, true)
            } else {
                DayPage(value, false)
            }
            if (value.dayOfWeek != DayOfWeek.MONDAY || mondayPending) {
                value = value.minusDays(1)
            } else {
                mondayPending = true
            }
            i--
        }
        // 注意：原版 jadx 此处循环下界写成 i+1(=0) 会整表覆盖，按语义修正为 6..9（保留回退填充的 0..5）
        var forward = center.plusDays(1)
        var mondayEmitted = true
        for (k in 6..9) {
            if (forward.dayOfWeek == DayOfWeek.MONDAY && mondayEmitted) {
                arr[k] = DayPage(forward, true)
                mondayEmitted = false
            } else {
                arr[k] = DayPage(forward, false)
                forward = forward.plusDays(1)
            }
        }
        pages = MutableList(10) { idx -> arr[idx] ?: DayPage(center) }
    }

    private fun spreadAt(centerDate: LocalDate): Pair<DayPage, DayPage> {
        val saved = center
        center = centerDate
        rebuild()
        val spread = leftPage to rightPage
        center = saved
        rebuild()
        return spread
    }
}

/** BookPageDateRange 的 LocalDate 便捷扩展 */
fun BookPageDateRange.startLocal(): LocalDate =
    java.util.Date(startTimeMills).toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

fun BookPageDateRange.endLocal(): LocalDate =
    java.util.Date(endTimeMills).toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

fun BookPageDateRange.containsLocal(date: LocalDate): Boolean =
    !date.isBefore(startLocal()) && !date.isAfter(endLocal())
