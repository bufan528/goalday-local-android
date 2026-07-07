package com.bf410.goaldaylocal.ui.book

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.bf410.goaldaylocal.data.ScheduleEntry
import com.bf410.goaldaylocal.ui.replica.GoaldayDesign
import java.util.Date

/**
 * 页面内容类型
 */
enum class PageContentType {
    SCHEDULE,  // 日程页
    DIARY      // 日记页
}

/**
 * 日历书本视图
 * 将3D翻页系统集成到BookShell视觉框架中
 */
@Composable
fun CalendarBookView(
    modifier: Modifier = Modifier,
    pageState: CircularCalendarPageState,
    shellStyle: ShellStyle = ShellStyle.LIGHT,
    pageContentType: PageContentType = PageContentType.SCHEDULE,
    scheduleEntries: List<ScheduleEntry> = emptyList(),
    diaryText: String = "",
    completedItems: List<String> = emptyList(),
    onScheduleStatusUpdate: (Int, Boolean) -> Unit = { _, _ -> }
) {
    BookShell(
        modifier = modifier,
        shellStyle = shellStyle,
        canTurnPrevious = pageState.canGoPrevious(),
        canTurnNext = pageState.canGoNext(),
        turnEnabled = true,
        onTapPrevious = { pageState.goPreviousPage() },
        onTapNext = { pageState.goNextPage() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(GoaldayDesign.adaptivePaper)
        ) {
            BaseBookView(
                modifier = Modifier.fillMaxSize(),
                pageState = pageState
            ) { calendarPage, index ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(GoaldayDesign.adaptivePaperGradient)
                        .padding(GoaldayDesign.Space4)
                ) {
                    when (pageContentType) {
                        PageContentType.SCHEDULE -> {
                            CalendarPageContent(
                                calendarPage = calendarPage,
                                scheduleEntries = scheduleEntries,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        PageContentType.DIARY -> {
                            DiaryPageContent(
                                calendarPage = calendarPage,
                                diaryText = diaryText,
                                completedItems = completedItems,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 创建默认的日历书本视图
 * 使用当前日期作为中心日期
 */
@Composable
fun DefaultCalendarBookView(
    modifier: Modifier = Modifier,
    shellStyle: ShellStyle = ShellStyle.BOOK
) {
    val pageState = remember {
        CircularCalendarPageState(
            initialCenterDate = Date(),
            dateRange = null
        )
    }
    
    CalendarBookView(
        modifier = modifier.fillMaxSize(),
        pageState = pageState,
        shellStyle = shellStyle
    )
}

/**
 * 带日期范围的日历书本视图
 */
@Composable
fun RangedCalendarBookView(
    modifier: Modifier = Modifier,
    initialDate: Date,
    dateRange: BookPageDateRange,
    shellStyle: ShellStyle = ShellStyle.BOOK
) {
    val pageState = remember(initialDate, dateRange) {
        CircularCalendarPageState(
            initialCenterDate = initialDate,
            dateRange = dateRange
        )
    }
    
    CalendarBookView(
        modifier = modifier.fillMaxSize(),
        pageState = pageState,
        shellStyle = shellStyle
    )
}
