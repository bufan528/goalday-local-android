package com.bf410.goaldaylocal.ui.book

import java.util.Date

/**
 * 翻页方向枚举
 */
enum class PageTurnDirection {
    NEXT,       // 向后翻页
    PREVIOUS    // 向前翻页
}

/**
 * 书本页面状态接口
 * 定义了书本页面状态管理的核心方法
 */
interface BookPageState<T> {
    
    /**
     * 获取中心日期
     */
    fun getCenterDate(): Date
    
    /**
     * 获取当前所有页面
     */
    fun getCurrentPages(): List<T>
    
    /**
     * 是否可以向前翻页
     */
    fun canGoPrevious(): Boolean
    
    /**
     * 是否可以向后翻页
     */
    fun canGoNext(): Boolean
    
    /**
     * 向前翻页
     */
    fun goPreviousPage(config: BookFlipConfig? = null)
    
    /**
     * 向后翻页
     */
    fun goNextPage(config: BookFlipConfig? = null)
    
    /**
     * 跳转到指定日期
     */
    fun jumpToDate(date: Date)
    
    /**
     * 获取当前日期范围
     */
    fun getCurrentDateRange(): BookPageDateRange
    
    /**
     * 设置日期范围
     */
    fun setDateRange(range: BookPageDateRange)
}
