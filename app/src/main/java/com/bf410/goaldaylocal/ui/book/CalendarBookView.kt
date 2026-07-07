package com.bf410.goaldaylocal.ui.book

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
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
 * 保留书脊、纸张纹理、书口等视觉效果
 */
@Composable
fun CalendarBookView(
    modifier: Modifier = Modifier,
    pageState: CircularCalendarPageState,
    shellStyle: ShellStyle = ShellStyle.LIGHT,
    pageContentType: PageContentType = PageContentType.SCHEDULE,
    onScheduleStatusUpdate: (Int, Boolean) -> Unit = { _, _ -> }
) {
    // 使用BookShell作为外层容器，保留所有视觉效果
    BookShell(
        modifier = modifier,
        shellStyle = shellStyle,
        canTurnPrevious = pageState.canGoPrevious(),
        canTurnNext = pageState.canGoNext(),
        turnEnabled = true,
        onTapPrevious = { pageState.goPreviousPage() },
        onTapNext = { pageState.goNextPage() }
    ) {
        // 在BookShell内部使用BaseBookView实现3D翻页
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(GoaldayDesign.adaptivePaper)
        ) {
            BaseBookView(
                modifier = Modifier.fillMaxSize(),
                pageState = pageState
            ) { calendarPage, index ->
                // 页面内容使用纸张背景
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(GoaldayDesign.adaptivePaperGradient)
                        .padding(GoaldayDesign.Space4)
                ) {
                    // 根据页面类型渲染不同内容
                    when (pageContentType) {
                        PageContentType.SCHEDULE -> {
                            CalendarPageContent(
                                calendarPage = calendarPage,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        PageContentType.DIARY -> {
                            DiaryPageContent(
                                calendarPage = calendarPage,
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
