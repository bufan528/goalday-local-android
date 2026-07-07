package com.bf410.goaldaylocal.ui.book

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bf410.goaldaylocal.data.ScheduleEntry
import com.bf410.goaldaylocal.ui.replica.GoaldayDesign
import java.util.Date

/**
 * 日历页面内容渲染器 - 对齐逆向资源的日程页布局
 * 左侧日期栏 + 右侧2x3任务格
 */
@Composable
fun CalendarPageContent(
    calendarPage: CalendarPage,
    scheduleEntries: List<ScheduleEntry>,
    modifier: Modifier = Modifier
) {
    // 主布局：左侧日期栏 + 右侧任务格
    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(GoaldayDesign.Space4),
        horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space4)
    ) {
        // 左侧日期栏（占约1/3宽度）
        DateRailSection(
            calendarPage = calendarPage,
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.33f)
        )
        
        // 右侧任务格（占约2/3宽度）
        TaskGridSection(
            calendarPage = calendarPage,
            scheduleEntries = scheduleEntries,
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.67f)
        )
    }
}

/**
 * 左侧日期栏 - 对齐逆向fragment_schedule_inbook.xml
 * 显示日期、星期、年月信息
 */
@Composable
private fun DateRailSection(
    calendarPage: CalendarPage,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(vertical = GoaldayDesign.Space2),
        verticalArrangement = Arrangement.Top
    ) {
        // 大号日期数字
        Text(
            text = "${calendarPage.getDayOfMonth()}",
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold,
            color = GoaldayDesign.adaptiveInkPrimary,
            fontFamily = GoaldayDesign.DisplayFontFamily
        )
        
        Spacer(modifier = Modifier.height(GoaldayDesign.Space2))
        
        // 星期
        Text(
            text = calendarPage.getDayOfWeek(),
            style = MaterialTheme.typography.titleMedium,
            color = GoaldayDesign.adaptiveInkSecondary,
            fontFamily = GoaldayDesign.BodyFontFamily
        )
        
        Spacer(modifier = Modifier.height(GoaldayDesign.Space1))
        
        // 年月
        Text(
            text = "${calendarPage.getYear()}.${calendarPage.getMonth().toString().padStart(2, '0')}",
            style = MaterialTheme.typography.bodyMedium,
            color = GoaldayDesign.adaptiveInkMuted,
            fontFamily = GoaldayDesign.BodyFontFamily
        )
        
        // 今天标记
        if (calendarPage.isToday()) {
            Spacer(modifier = Modifier.height(GoaldayDesign.Space3))
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
 * 右侧任务格 - 对齐逆向item_schedule_item_in_book.xml
 * 2列3行的任务格布局
 */
@Composable
private fun TaskGridSection(
    calendarPage: CalendarPage,
    scheduleEntries: List<ScheduleEntry>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space3)
    ) {
        // 3行任务
        repeat(3) { rowIndex ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space3)
            ) {
                // 每行2个任务格
                repeat(2) { colIndex ->
                    val taskIndex = rowIndex * 2 + colIndex
                    val entry = scheduleEntries.getOrNull(taskIndex)
                    TaskSlot(
                        entry = entry,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // 底部页码装饰
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "— ${calendarPage.getDayOfMonth()} —",
                style = MaterialTheme.typography.bodySmall,
                color = GoaldayDesign.adaptiveInkMuted.copy(alpha = 0.4f),
                fontFamily = GoaldayDesign.DisplayFontFamily
            )
        }
    }
}

/**
 * 单个任务格 - 对齐逆向的ContentTextView样式
 */
@Composable
private fun TaskSlot(
    entry: ScheduleEntry?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(GoaldayDesign.RadiusM))
            .background(GoaldayDesign.adaptiveSurfaceSoft)
            .padding(GoaldayDesign.Space3),
        contentAlignment = Alignment.TopStart
    ) {
        if (entry != null) {
            // 显示真实日程数据
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space1)
            ) {
                // 时间标签
                if (entry.timeText.isNotBlank()) {
                    Text(
                        text = entry.timeText,
                        style = MaterialTheme.typography.labelSmall,
                        color = GoaldayDesign.Pink,
                        fontFamily = GoaldayDesign.BodyFontFamily,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                // 标题
                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.bodySmall,
                    color = GoaldayDesign.adaptiveInkPrimary,
                    fontFamily = GoaldayDesign.BodyFontFamily,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                // 备注（如果有）
                if (entry.note.isNotBlank()) {
                    Text(
                        text = entry.note,
                        style = MaterialTheme.typography.labelSmall,
                        color = GoaldayDesign.adaptiveInkMuted,
                        fontFamily = GoaldayDesign.BodyFontFamily,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        } else {
            // 空状态
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "暂无日程",
                    style = MaterialTheme.typography.bodySmall,
                    color = GoaldayDesign.adaptiveInkMuted.copy(alpha = 0.5f),
                    fontFamily = GoaldayDesign.BodyFontFamily
                )
            }
        }
    }
}
