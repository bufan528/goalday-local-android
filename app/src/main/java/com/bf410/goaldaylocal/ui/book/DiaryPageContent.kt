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

/**
 * 日记页面内容渲染器 - 对齐逆向资源的日记页布局
 * 日期头 + 内容列表 + 底部工具栏
 */
@Composable
fun DiaryPageContent(
    calendarPage: CalendarPage,
    diaryText: String,
    completedItems: List<String>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(GoaldayDesign.Space4),
        verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space3)
    ) {
        // 顶部日期头
        DiaryDateHeader(calendarPage)
        
        Spacer(modifier = Modifier.height(GoaldayDesign.Space2))
        
        // 分隔线
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(GoaldayDesign.adaptiveDivider)
        )
        
        Spacer(modifier = Modifier.height(GoaldayDesign.Space2))
        
        // 日记内容区域（可滚动）
        DiaryContentArea(
            diaryText = diaryText,
            completedItems = completedItems
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        // 底部工具栏
        DiaryBottomToolbar()
    }
}

/**
 * 日记日期头部 - 对齐逆向fragment_diary_inbook.xml的fl_date
 */
@Composable
private fun DiaryDateHeader(calendarPage: CalendarPage) {
    Column(
        verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space1)
    ) {
        // 主日期和星期
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space3)
        ) {
            // 大号日期
            Text(
                text = "${calendarPage.getDayOfMonth()}",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = GoaldayDesign.adaptiveInkPrimary,
                fontFamily = GoaldayDesign.DisplayFontFamily
            )
            
            // 年月和星期
            Column {
                Text(
                    text = "${calendarPage.getYear()}年${calendarPage.getMonth()}月",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GoaldayDesign.adaptiveInkSecondary,
                    fontFamily = GoaldayDesign.BodyFontFamily
                )
                
                Text(
                    text = calendarPage.getDayOfWeek(),
                    style = MaterialTheme.typography.bodySmall,
                    color = GoaldayDesign.adaptiveInkMuted,
                    fontFamily = GoaldayDesign.BodyFontFamily
                )
            }
        }
        
        // 今天标记
        if (calendarPage.isToday()) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
                    .background(GoaldayDesign.Pink.copy(alpha = 0.12f))
                    .padding(horizontal = GoaldayDesign.Space2, vertical = 2.dp)
            ) {
                Text(
                    text = "今天",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = GoaldayDesign.Pink,
                    fontFamily = GoaldayDesign.BodyFontFamily
                )
            }
        }
    }
}

/**
 * 日记内容区域 - 对齐逆向fragment_diary_inbook.xml的rv_container
 */
@Composable
private fun DiaryContentArea(
    diaryText: String,
    completedItems: List<String>
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space3)
    ) {
        // 日记文本内容
        DiaryTextBlock(diaryText)
        
        // 已完成目标回顾块（如果有）
        if (completedItems.isNotEmpty()) {
            CompletedTargetRecap(completedItems)
        }
    }
}

/**
 * 日记文本块 - 对齐逆向item_diary_text.xml
 */
@Composable
private fun DiaryTextBlock(diaryText: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(GoaldayDesign.RadiusS))
            .background(GoaldayDesign.adaptiveSurfaceSoft.copy(alpha = 0.5f))
            .padding(GoaldayDesign.Space3)
    ) {
        if (diaryText.isNotBlank()) {
            Text(
                text = diaryText,
                style = MaterialTheme.typography.bodyLarge,
                color = GoaldayDesign.adaptiveInkPrimary,
                fontFamily = GoaldayDesign.BodyFontFamily,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
            )
        } else {
            Text(
                text = "点击输入日记内容...",
                style = MaterialTheme.typography.bodyLarge,
                color = GoaldayDesign.adaptiveInkMuted.copy(alpha = 0.6f),
                fontFamily = GoaldayDesign.BodyFontFamily,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
            )
        }
    }
}

/**
 * 已完成目标回顾块 - 对齐逆向item_diary_target_in_book.xml
 */
@Composable
private fun CompletedTargetRecap(completedItems: List<String>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(GoaldayDesign.RadiusM))
            .background(GoaldayDesign.GreenSoft.copy(alpha = 0.3f))
            .padding(GoaldayDesign.Space3)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space2)
        ) {
            // 标题
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space2)
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(GoaldayDesign.Positive)
                )
                
                Text(
                    text = "今日完成",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = GoaldayDesign.adaptiveInkPrimary,
                    fontFamily = GoaldayDesign.BodyFontFamily
                )
            }
            
            // 已完成项目列表
            completedItems.forEach { item ->
                Text(
                    text = "• $item",
                    style = MaterialTheme.typography.bodySmall,
                    color = GoaldayDesign.adaptiveInkSecondary,
                    fontFamily = GoaldayDesign.BodyFontFamily
                )
            }
        }
    }
}

/**
 * 底部工具栏 - 对齐逆向fragment_diary_inbook.xml的fl_bottom_bar
 */
@Composable
private fun DiaryBottomToolbar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = GoaldayDesign.RadiusM, topEnd = GoaldayDesign.RadiusM))
            .background(GoaldayDesign.adaptiveSurfaceSoft)
            .padding(horizontal = GoaldayDesign.Space3, vertical = GoaldayDesign.Space2),
        horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space4),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 图片选择按钮
        ToolbarIconButton(
            iconText = "📷",
            label = "图片"
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        // 页码显示
        Text(
            text = "日记页",
            style = MaterialTheme.typography.bodySmall,
            color = GoaldayDesign.adaptiveInkMuted,
            fontFamily = GoaldayDesign.BodyFontFamily
        )
    }
}

/**
 * 工具栏图标按钮
 */
@Composable
private fun ToolbarIconButton(
    iconText: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(GoaldayDesign.adaptiveSurface),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = iconText,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = GoaldayDesign.adaptiveInkSecondary,
            fontFamily = GoaldayDesign.BodyFontFamily
        )
    }
}
