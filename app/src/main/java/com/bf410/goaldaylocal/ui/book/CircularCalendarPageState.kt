package com.bf410.goaldaylocal.ui.book

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId

/**
 * 书芯单页：日期 + 是否日程面（月历视图）。
 * isSchedule=true 渲染该日月历页，false 渲染该日日记页。
 */
data class DayPage(
    val date: LocalDate,
    val isSchedule: Boolean = false,
)

/**
 * 循环日历页面状态。真机原版行为：
 *
 * 每个摊开页 = 同一天 D：左页 = D 的月历视图（isSchedule=true），
 * 右页 = D 的日记（isSchedule=false）。翻一页 = D±1 天，左右始终同一天。
 * 10 页环形缓冲：pages[4]=左月历，pages[5]=右日记；前后各留 4 页供翻页背面落点。
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

    private fun nextCenter(): LocalDate = center.plusDays(1)

    private fun prevCenter(): LocalDate = center.minusDays(1)

    private fun inRange(date: LocalDate): Boolean {
        val range = dateRange ?: return true
        return range.containsLocal(date)
    }

    /** 环形缓冲：pages[4]=center 月历，pages[5]=center 日记；0..3 为前 4 天，6..9 为后 4 天 */
    private fun rebuild() {
        val arr = arrayOfNulls<DayPage>(10)
        arr[4] = DayPage(center, isSchedule = true)
        arr[5] = DayPage(center, isSchedule = false)
        for (k in 0..3) arr[k] = DayPage(center.minusDays((4 - k).toLong()), isSchedule = false)
        for (k in 6..9) arr[k] = DayPage(center.plusDays((k - 5).toLong()), isSchedule = false)
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
