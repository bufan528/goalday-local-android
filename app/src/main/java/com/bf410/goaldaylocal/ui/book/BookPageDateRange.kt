package com.bf410.goaldaylocal.ui.book

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 书本页面日期范围
 * 用于管理日历视图的日期范围
 * 
 * @param startTimeMills 开始时间戳（毫秒）
 * @param endTimeMills 结束时间戳（毫秒）
 */
data class BookPageDateRange(
    val startTimeMills: Long,
    val endTimeMills: Long
) {
    /**
     * 获取开始日期
     */
    fun getStartDate(): Date = Date(startTimeMills)
    
    /**
     * 获取结束日期
     */
    fun getEndDate(): Date = Date(endTimeMills)
    
    /**
     * 格式化开始日期
     */
    fun formatStartDate(pattern: String = "yyyy年MM月dd日"): String {
        val sdf = SimpleDateFormat(pattern, Locale.getDefault())
        return sdf.format(getStartDate())
    }
    
    /**
     * 格式化结束日期
     */
    fun formatEndDate(pattern: String = "yyyy年MM月dd日"): String {
        val sdf = SimpleDateFormat(pattern, Locale.getDefault())
        return sdf.format(getEndDate())
    }
    
    /**
     * 格式化日期范围
     */
    fun formatRange(pattern: String = "yyyy年MM月dd日"): String {
        return "${formatStartDate(pattern)} - ${formatEndDate(pattern)}"
    }
    
    /**
     * 检查给定时间戳是否在范围内
     */
    fun contains(timeMills: Long): Boolean {
        return timeMills in startTimeMills..endTimeMills
    }
    
    /**
     * 检查给定日期是否在范围内
     */
    fun contains(date: Date): Boolean {
        return contains(date.time)
    }
    
    /**
     * 获取范围的天数
     */
    fun getDaysCount(): Int {
        val diffMillis = endTimeMills - startTimeMills
        return (diffMillis / (1000 * 60 * 60 * 24)).toInt() + 1
    }
    
    companion object {
        /**
         * 从年份创建日期范围（该年的第一个周一到最后一个周日）
         */
        fun fromYear(year: Int): BookPageDateRange {
            val calendar = java.util.Calendar.getInstance().apply {
                firstDayOfWeek = java.util.Calendar.MONDAY
                set(year, 0, 1, 0, 0, 0)
                set(java.util.Calendar.MILLISECOND, 0)
                
                // 找到该年第一个周一
                val dayOfWeek = get(java.util.Calendar.DAY_OF_WEEK)
                val daysToAdd = if (dayOfWeek == java.util.Calendar.MONDAY) 0 
                               else (java.util.Calendar.SATURDAY - dayOfWeek + 2) % 7
                add(java.util.Calendar.DAY_OF_MONTH, daysToAdd)
                
                val startTimeMillis = timeInMillis
                
                // 找到该年最后一个周日
                set(year, 11, 31, 23, 59, 59)
                set(java.util.Calendar.MILLISECOND, 999)
                val endDayOfWeek = get(java.util.Calendar.DAY_OF_WEEK)
                val daysToSubtract = if (endDayOfWeek == java.util.Calendar.SUNDAY) 0
                                    else endDayOfWeek - java.util.Calendar.SUNDAY
                add(java.util.Calendar.DAY_OF_MONTH, -daysToSubtract)
                
                val endTimeMillis = timeInMillis
                
                return BookPageDateRange(startTimeMillis, endTimeMillis)
            }
        }
        
        /**
         * 从月份创建日期范围
         */
        fun fromMonth(year: Int, month: Int): BookPageDateRange {
            val calendar = java.util.Calendar.getInstance().apply {
                set(year, month - 1, 1, 0, 0, 0)
                set(java.util.Calendar.MILLISECOND, 0)
                val startTimeMillis = timeInMillis
                
                set(year, month - 1, getActualMaximum(java.util.Calendar.DAY_OF_MONTH), 23, 59, 59)
                set(java.util.Calendar.MILLISECOND, 999)
                val endTimeMillis = timeInMillis
                
                return BookPageDateRange(startTimeMillis, endTimeMillis)
            }
        }
        
        /**
         * 从周创建日期范围（从指定日期开始的7天）
         */
        fun fromWeek(startDate: Date): BookPageDateRange {
            val calendar = java.util.Calendar.getInstance().apply {
                time = startDate
                set(java.util.Calendar.HOUR_OF_DAY, 0)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
                val startTimeMillis = timeInMillis
                
                add(java.util.Calendar.DAY_OF_MONTH, 6)
                set(java.util.Calendar.HOUR_OF_DAY, 23)
                set(java.util.Calendar.MINUTE, 59)
                set(java.util.Calendar.SECOND, 59)
                set(java.util.Calendar.MILLISECOND, 999)
                val endTimeMillis = timeInMillis
                
                return BookPageDateRange(startTimeMillis, endTimeMillis)
            }
        }
    }
}
