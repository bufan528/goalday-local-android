package com.bf410.goaldaylocal.ui.book

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 日历页面数据模型
 * 用于表示书本中的单个日历页面
 * 
 * @param date 页面日期
 * @param isSchedule 是否有日程安排
 */
data class CalendarPage(
    val date: Date,
    val isSchedule: Boolean = false
) {
    /**
     * 获取日期字符串（yyyy-MM-dd 格式）
     */
    fun getDateStr(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(date)
    }
    
    /**
     * 获取友好日期字符串（yyyy年MM月dd日 格式）
     */
    fun getFriendlyDateStr(): String {
        val sdf = SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault())
        return sdf.format(date)
    }
    
    /**
     * 获取星期几
     */
    fun getDayOfWeek(): String {
        val sdf = SimpleDateFormat("EEEE", Locale.getDefault())
        return sdf.format(date)
    }
    
    /**
     * 获取月份
     */
    fun getMonth(): Int {
        val calendar = java.util.Calendar.getInstance().apply {
            time = this@CalendarPage.date
        }
        return calendar.get(java.util.Calendar.MONTH) + 1
    }
    
    /**
     * 获取日期（几号）
     */
    fun getDayOfMonth(): Int {
        val calendar = java.util.Calendar.getInstance().apply {
            time = this@CalendarPage.date
        }
        return calendar.get(java.util.Calendar.DAY_OF_MONTH)
    }
    
    /**
     * 获取年份
     */
    fun getYear(): Int {
        val calendar = java.util.Calendar.getInstance().apply {
            time = this@CalendarPage.date
        }
        return calendar.get(java.util.Calendar.YEAR)
    }
    
    /**
     * 检查是否是今天
     */
    fun isToday(): Boolean {
        val today = Date()
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(today)
        return getDateStr() == todayStr
    }
    
    /**
     * 检查是否是周末
     */
    fun isWeekend(): Boolean {
        val calendar = java.util.Calendar.getInstance().apply {
            time = this@CalendarPage.date
        }
        val dayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK)
        return dayOfWeek == java.util.Calendar.SATURDAY || dayOfWeek == java.util.Calendar.SUNDAY
    }
    
    /**
     * 检查是否是工作日
     */
    fun isWeekday(): Boolean = !isWeekend()
    
    companion object {
        /**
         * 从今天创建日历页面
         */
        fun today(hasSchedule: Boolean = false): CalendarPage {
            return CalendarPage(Date(), hasSchedule)
        }
        
        /**
         * 从时间戳创建日历页面
         */
        fun fromTimestamp(timestamp: Long, hasSchedule: Boolean = false): CalendarPage {
            return CalendarPage(Date(timestamp), hasSchedule)
        }
        
        /**
         * 从日期字符串创建日历页面
         */
        fun fromDateStr(dateStr: String, hasSchedule: Boolean = false): CalendarPage {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = sdf.parse(dateStr) ?: Date()
            return CalendarPage(date, hasSchedule)
        }
    }
}
