package com.bf410.goaldaylocal.ui.book

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bf410.goaldaylocal.ui.replica.GoaldayDesign
import java.util.Date

/**
 * 日历页面内容渲染器
 * 用于渲染单个日历页面的内容
 */
@Composable
fun CalendarPageContent(
    calendarPage: CalendarPage,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 日期标题
        DateHeader(calendarPage)
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 日程状态指示器
        ScheduleIndicator(calendarPage)
        
        Spacer(modifier = Modifier.weight(1f))
        
        // 页面底部信息
        PageFooter(calendarPage)
    }
}

/**
 * 日期标题组件
 */
@Composable
private fun DateHeader(calendarPage: CalendarPage) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // 友好日期格式
        Text(
            text = calendarPage.getFriendlyDateStr(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = GoaldayDesign.adaptiveInkPrimary
        )
        
        // 星期几
        Text(
            text = calendarPage.getDayOfWeek(),
            style = MaterialTheme.typography.titleMedium,
            color = GoaldayDesign.adaptiveInkSecondary
        )
        
        // 今天标记
        if (calendarPage.isToday()) {
            Text(
                text = "今天",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = GoaldayDesign.PrimaryAction,
                modifier = Modifier
                    .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
                    .background(GoaldayDesign.PrimaryAction.copy(alpha = 0.1f))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
    }
}

/**
 * 日程状态指示器
 */
@Composable
private fun ScheduleIndicator(calendarPage: CalendarPage) {
    val hasSchedule = calendarPage.isSchedule
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 状态图标
        Box(
            modifier = Modifier
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(
                    if (hasSchedule) GoaldayDesign.Positive 
                    else GoaldayDesign.adaptiveDivider
                )
                .padding(horizontal = 12.dp)
        )
        
        // 状态文本
        Text(
            text = if (hasSchedule) "有日程安排" else "暂无日程",
            style = MaterialTheme.typography.bodyMedium,
            color = if (hasSchedule) GoaldayDesign.Positive 
                   else GoaldayDesign.adaptiveInkMuted
        )
    }
}

/**
 * 页面底部信息
 */
@Composable
private fun PageFooter(calendarPage: CalendarPage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 工作日/周末标记
        Text(
            text = if (calendarPage.isWeekend()) "周末" else "工作日",
            style = MaterialTheme.typography.labelSmall,
            color = GoaldayDesign.adaptiveInkMuted
        )
        
        // 日期数字
        Text(
            text = "${calendarPage.getDayOfMonth()}",
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Light,
            color = GoaldayDesign.adaptiveInkMuted.copy(alpha = 0.3f)
        )
    }
}
