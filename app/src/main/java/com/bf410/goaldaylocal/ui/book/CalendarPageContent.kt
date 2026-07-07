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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
 * 使用手账风格的视觉设计
 */
@Composable
fun CalendarPageContent(
    calendarPage: CalendarPage,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(GoaldayDesign.Space6),
        verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space4)
    ) {
        // 日期标题区域
        DateHeaderSection(calendarPage)
        
        Spacer(modifier = Modifier.height(GoaldayDesign.Space3))
        
        // 分隔线
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(GoaldayDesign.adaptiveDivider)
        )
        
        Spacer(modifier = Modifier.height(GoaldayDesign.Space3))
        
        // 日程内容区域
        ScheduleContentSection(calendarPage)
        
        Spacer(modifier = Modifier.weight(1f))
        
        // 页面底部装饰
        PageFooterSection(calendarPage)
    }
}

/**
 * 日期标题区域
 * 使用衬线字体和手账风格
 */
@Composable
private fun DateHeaderSection(calendarPage: CalendarPage) {
    Column(
        verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space2)
    ) {
        // 主日期显示 - 大号衬线字体
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space3)
        ) {
            // 日期数字
            Text(
                text = "${calendarPage.getDayOfMonth()}",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = GoaldayDesign.adaptiveInkPrimary,
                fontFamily = GoaldayDesign.DisplayFontFamily
            )
            
            // 年月
            Column {
                Text(
                    text = "${calendarPage.getYear()}年${calendarPage.getMonth()}月",
                    style = MaterialTheme.typography.titleMedium,
                    color = GoaldayDesign.adaptiveInkSecondary,
                    fontFamily = GoaldayDesign.DisplayFontFamily
                )
                
                // 星期
                Text(
                    text = calendarPage.getDayOfWeek(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = GoaldayDesign.adaptiveInkSecondary,
                    fontFamily = GoaldayDesign.BodyFontFamily
                )
            }
        }
        
        // 今天标记
        if (calendarPage.isToday()) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
                    .background(GoaldayDesign.Pink.copy(alpha = 0.15f))
                    .padding(horizontal = GoaldayDesign.Space3, vertical = GoaldayDesign.Space1)
            ) {
                Text(
                    text = "今天",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = GoaldayDesign.Pink,
                    fontFamily = GoaldayDesign.BodyFontFamily
                )
            }
        }
    }
}

/**
 * 日程内容区域
 */
@Composable
private fun ScheduleContentSection(calendarPage: CalendarPage) {
    Column(
        verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space3)
    ) {
        if (calendarPage.isSchedule) {
            // 有日程时的显示
            ScheduleItemList()
        } else {
            // 无日程时的占位显示
            EmptySchedulePlaceholder()
        }
    }
}

/**
 * 日程项目列表
 */
@Composable
private fun ScheduleItemList() {
    Column(
        verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space2)
    ) {
        // 示例日程项（实际应该从数据源获取）
        repeat(3) { index ->
            ScheduleItemRow(
                title = "日程项目 ${index + 1}",
                time = "09:00",
                completed = index == 0
            )
        }
    }
}

/**
 * 单个日程项行
 */
@Composable
private fun ScheduleItemRow(
    title: String,
    time: String,
    completed: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(GoaldayDesign.RadiusM))
            .background(
                if (completed) GoaldayDesign.GreenSoft 
                else GoaldayDesign.adaptiveSurfaceSoft
            )
            .padding(GoaldayDesign.Space3),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space3)
    ) {
        // 完成状态圆圈
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(
                    if (completed) GoaldayDesign.Positive 
                    else GoaldayDesign.adaptiveDivider
                ),
            contentAlignment = Alignment.Center
        ) {
            if (completed) {
                Text(
                    text = "✓",
                    style = MaterialTheme.typography.labelSmall,
                    color = GoaldayDesign.Surface,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        // 时间
        Text(
            text = time,
            style = MaterialTheme.typography.bodyMedium,
            color = GoaldayDesign.adaptiveInkSecondary,
            fontFamily = GoaldayDesign.BodyFontFamily,
            modifier = Modifier.width(50.dp)
        )
        
        // 标题
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = if (completed) GoaldayDesign.adaptiveInkMuted 
                   else GoaldayDesign.adaptiveInkPrimary,
            fontWeight = FontWeight.Medium,
            fontFamily = GoaldayDesign.BodyFontFamily,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * 空日程占位符
 */
@Composable
private fun EmptySchedulePlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = GoaldayDesign.Space8),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space2)
        ) {
            Text(
                text = "今日无事",
                style = MaterialTheme.typography.titleMedium,
                color = GoaldayDesign.adaptiveInkMuted,
                fontFamily = GoaldayDesign.DisplayFontFamily
            )
            
            Text(
                text = "享受悠闲的一天",
                style = MaterialTheme.typography.bodySmall,
                color = GoaldayDesign.adaptiveInkMuted.copy(alpha = 0.7f),
                fontFamily = GoaldayDesign.BodyFontFamily
            )
        }
    }
}

/**
 * 页面底部装饰区域
 */
@Composable
private fun PageFooterSection(calendarPage: CalendarPage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 左侧：工作日/周末标记
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space2)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(
                        if (calendarPage.isWeekend()) GoaldayDesign.Pink 
                        else GoaldayDesign.Positive
                    )
            )
            
            Text(
                text = if (calendarPage.isWeekend()) "周末" else "工作日",
                style = MaterialTheme.typography.labelSmall,
                color = GoaldayDesign.adaptiveInkMuted,
                fontFamily = GoaldayDesign.BodyFontFamily
            )
        }
        
        // 右侧：页码装饰
        Text(
            text = "— ${calendarPage.getDayOfMonth()} —",
            style = MaterialTheme.typography.bodySmall,
            color = GoaldayDesign.adaptiveInkMuted.copy(alpha = 0.5f),
            fontFamily = GoaldayDesign.DisplayFontFamily
        )
    }
}
