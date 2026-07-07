package com.bf410.goaldaylocal.ui.book

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import java.util.Date

/**
 * 日历书本视图
 * 整合了 BaseBookView 和 CalendarPageContent
 * 用于展示完整的日历翻页效果
 */
@Composable
fun CalendarBookView(
    modifier: Modifier = Modifier,
    pageState: CircularCalendarPageState,
    onScheduleStatusUpdate: (Int, Boolean) -> Unit = { _, _ -> }
) {
    BaseBookView(
        modifier = modifier.fillMaxSize(),
        pageState = pageState
    ) { calendarPage, index ->
        CalendarPageContent(
            calendarPage = calendarPage,
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * 创建默认的日历书本视图
 * 使用当前日期作为中心日期
 */
@Composable
fun DefaultCalendarBookView(
    modifier: Modifier = Modifier
) {
    val pageState = remember {
        CircularCalendarPageState(
            initialCenterDate = Date(),
            dateRange = null
        )
    }
    
    CalendarBookView(
        modifier = modifier.fillMaxSize(),
        pageState = pageState
    )
}

/**
 * 带日期范围的日历书本视图
 */
@Composable
fun RangedCalendarBookView(
    modifier: Modifier = Modifier,
    initialDate: Date,
    dateRange: BookPageDateRange
) {
    val pageState = remember(initialDate, dateRange) {
        CircularCalendarPageState(
            initialCenterDate = initialDate,
            dateRange = dateRange
        )
    }
    
    CalendarBookView(
        modifier = modifier.fillMaxSize(),
        pageState = pageState
    )
}
