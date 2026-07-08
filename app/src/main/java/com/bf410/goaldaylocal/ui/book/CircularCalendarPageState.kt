package com.bf410.goaldaylocal.ui.book

import java.util.Calendar
import java.util.Date

/**
 * 循环日历页面状态管理器
 * 管理书本的10个页面（封面+8个内页+封底）的循环状态
 * 
 * 核心功能：
 * - 维护10个页面的日历数据
 * - 管理中心日期（当前显示的日期）
 * - 处理翻页逻辑（向前/向后翻页）
 * - 支持日期范围限制
 */
class CircularCalendarPageState(
    private val initialCenterDate: Date = Date(),
    private val dateRange: BookPageDateRange? = null
) {
    // 10个页面的循环缓冲区
    private val pages = MutableList(10) { index ->
        CalendarPage(getDateByOffset(index - 4), false)
    }
    
    // 中心日期（当前显示的日期）
    private var _centerDate = initialCenterDate
    val centerDate: Date get() = _centerDate
    
    // 当前页面索引（0-9）
    private var _currentPageIndex = 4
    val currentPageIndex: Int get() = _currentPageIndex
    
    // 翻页配置
    private var flipConfig = BookFlipConfig()
    
    // 页面状态委托
    private var _delegate: AllState = AllState.IDLE
    val delegate: AllState get() = _delegate
    
    /**
     * 获取指定位置的页面
     */
    fun getPage(index: Int): CalendarPage {
        return pages[index.coerceIn(0, 9)]
    }
    
    /**
     * 获取所有当前页面
     */
    fun getCurrentPages(): List<CalendarPage> {
        return pages.toList()
    }
    
    /**
     * 检查是否可以向前翻页（翻到更早的日期）
     */
    fun canGoPrevious(): Boolean {
        if (dateRange == null) return true
        val previousDate = getDateByOffset(-1)
        return previousDate.time >= dateRange.startTimeMills
    }
    
    /**
     * 检查是否可以向后翻页（翻到更晚的日期）
     */
    fun canGoNext(): Boolean {
        if (dateRange == null) return true
        val nextDate = getDateByOffset(1)
        return nextDate.time <= dateRange.endTimeMills
    }
    
    /**
     * 向前翻页（翻到更早的日期）
     */
    fun goPreviousPage(config: BookFlipConfig? = null) {
        if (!canGoPrevious()) return
        
        _delegate = AllState.TURNING_PREVIOUS
        config?.let { flipConfig = it }
        
        // 更新中心日期
        _centerDate = getDateByOffset(-1)
        
        // 循环移动页面
        val lastPage = pages.removeAt(9)
        val newPage = CalendarPage(getDateByOffset(-5), false)
        pages.add(0, newPage)
        
        // 更新当前页面索引
        _currentPageIndex = (_currentPageIndex + 1).coerceIn(0, 9)
        
        _delegate = AllState.IDLE
    }
    
    /**
     * 向后翻页（翻到更晚的日期）
     */
    fun goNextPage(config: BookFlipConfig? = null) {
        if (!canGoNext()) return
        
        _delegate = AllState.TURNING_NEXT
        config?.let { flipConfig = it }
        
        // 更新中心日期
        _centerDate = getDateByOffset(1)
        
        // 循环移动页面
        val firstPage = pages.removeAt(0)
        val newPage = CalendarPage(getDateByOffset(5), false)
        pages.add(newPage)
        
        // 更新当前页面索引
        _currentPageIndex = (_currentPageIndex - 1).coerceIn(0, 9)
        
        _delegate = AllState.IDLE
    }
    
    /**
     * 跳转到指定日期
     */
    fun jumpToDate(targetDate: Date) {
        if (dateRange != null) {
            if (targetDate.time < dateRange.startTimeMills || targetDate.time > dateRange.endTimeMills) {
                return
            }
        }
        
        _centerDate = targetDate
        
        // 重新生成所有页面
        for (i in 0 until 10) {
            pages[i] = CalendarPage(getDateByOffset(i - 4), false)
        }
        
        _currentPageIndex = 4
    }
    
    /**
     * 更新页面的日程状态
     */
    fun updatePageScheduleStatus(pageIndex: Int, hasSchedule: Boolean) {
        if (pageIndex in 0..9) {
            pages[pageIndex] = pages[pageIndex].copy(isSchedule = hasSchedule)
        }
    }
    
    /**
     * 根据偏移量获取日期
     * @param offset 相对于中心日期的偏移天数
     */
    private fun getDateByOffset(offset: Int): Date {
        val calendar = Calendar.getInstance().apply {
            time = _centerDate
            add(Calendar.DAY_OF_MONTH, offset)
        }
        return calendar.time
    }
    
    /**
     * 获取当前日期范围
     */
    fun getCurrentDateRange(): BookPageDateRange {
        val startDate = pages.first().date
        val endDate = pages.last().date
        return BookPageDateRange(startDate.time, endDate.time)
    }
    
    /**
     * 设置日期范围
     */
    fun setDateRange(range: BookPageDateRange) {
        // 如果当前中心日期不在新范围内，调整到范围的开始
        if (_centerDate.time < range.startTimeMills || _centerDate.time > range.endTimeMills) {
            _centerDate = Date(range.startTimeMills)
            
            // 重新生成所有页面
            for (i in 0 until 10) {
                pages[i] = CalendarPage(getDateByOffset(i - 4), false)
            }
        }
    }
    
    /**
     * 所有状态枚举
     */
    enum class AllState {
        IDLE,           // 空闲状态
        TURNING_NEXT,   // 正在向后翻页
        TURNING_PREVIOUS // 正在向前翻页
    }
}
