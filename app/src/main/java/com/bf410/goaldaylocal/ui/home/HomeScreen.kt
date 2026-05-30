package com.bf410.goaldaylocal.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bf410.goaldaylocal.ui.calendar.CalendarViewModel
import com.bf410.goaldaylocal.ui.replica.GoaldaySegmentBar
import com.bf410.goaldaylocal.ui.replica.GoaldayTopBar
import java.time.LocalDate

@Composable
fun HomeScreen(
    calendarViewModel: CalendarViewModel,
    onOpenCalendar: () -> Unit,
    onOpenHandbook: () -> Unit,
    onOpenInspiration: () -> Unit,
) {
    val calendarState by calendarViewModel.uiState.collectAsState()
    val today = LocalDate.now()
    val todayEntries = calendarState.entries.filter {
        it.year == today.year && it.month == today.monthValue && it.day == today.dayOfMonth
    }
    val doneCount = todayEntries.count { it.completed }
    val todoCount = todayEntries.size - doneCount

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        GoaldayTopBar(leftTitle = "Goalday", rightPrimaryText = "今天", onRightPrimaryClick = { calendarViewModel.backToToday() })

        GoaldaySegmentBar(
            items = listOf("首页", "日历", "手账"),
            selectedIndex = 0,
            onSelect = {
                when (it) {
                    1 -> onOpenCalendar()
                    2 -> onOpenHandbook()
                }
            },
        )

        SummaryCard(title = "今日执行", value = "${doneCount}/${todayEntries.size}", subtitle = "已完成 ${doneCount} · 待做 ${todoCount}")

        ActionCard(
            title = "去日历安排",
            subtitle = "查看整月并拖拽调整任务日期",
            action = "打开日历",
            onClick = onOpenCalendar,
        )
        ActionCard(
            title = "去手账复盘",
            subtitle = "翻页查看执行记录与本周主题",
            action = "打开手账",
            onClick = onOpenHandbook,
        )
        ActionCard(
            title = "去灵感补充",
            subtitle = "从模板直接补到今日任务池",
            action = "打开灵感",
            onClick = onOpenInspiration,
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFFBFAF8), RoundedCornerShape(14.dp))
                .border(1.dp, Color(0x14000000), RoundedCornerShape(14.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("今日任务", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = Color(0xFF2F2A24))
            if (todayEntries.isEmpty()) {
                Text("今天还没有任务，去日历添加一条。", color = Color(0xFF8E877E), style = MaterialTheme.typography.bodySmall)
            } else {
                todayEntries.take(8).forEach { entry ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(if (entry.completed) "✓" else "·", color = if (entry.completed) Color(0xFF7AA171) else Color(0xFF9D9589))
                        Text(
                            entry.title,
                            modifier = Modifier.weight(1f),
                            color = if (entry.completed) Color(0xFF8E877E) else Color(0xFF3A332C),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text("${entry.month}/${entry.day}", color = Color(0xFF9B9388), style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(title: String, value: String, subtitle: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFBFAF8), RoundedCornerShape(14.dp))
            .border(1.dp, Color(0x14000000), RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(title, style = MaterialTheme.typography.labelLarge, color = Color(0xFF8D867D))
        Text(value, style = MaterialTheme.typography.headlineMedium, color = Color(0xFF2F2A24), fontWeight = FontWeight.Bold)
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color(0xFF8E877E))
    }
}

@Composable
private fun ActionCard(
    title: String,
    subtitle: String,
    action: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFBFAF8), RoundedCornerShape(14.dp))
            .border(1.dp, Color(0x14000000), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, color = Color(0xFF2F2A24), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = Color(0xFF8E877E), style = MaterialTheme.typography.bodySmall)
        }
        Spacer(Modifier.width(8.dp))
        Text(
            action,
            color = Color.White,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier
                .background(Color(0xFF222222), RoundedCornerShape(99.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp),
        )
    }
    Spacer(Modifier.height(0.dp))
}
