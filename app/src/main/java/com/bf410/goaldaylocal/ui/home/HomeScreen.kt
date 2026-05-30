package com.bf410.goaldaylocal.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.bf410.goaldaylocal.data.ScheduleEntry
import com.bf410.goaldaylocal.ui.calendar.CalendarViewModel
import com.bf410.goaldaylocal.ui.replica.GoaldayTopBar
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(
    calendarViewModel: CalendarViewModel,
    onOpenCalendar: () -> Unit,
    onOpenHandbook: () -> Unit,
    onOpenInspiration: () -> Unit,
) {
    val calendarState by calendarViewModel.uiState.collectAsState()
    var mode by rememberSaveable { mutableIntStateOf(0) }
    var checklistEditing by rememberSaveable { mutableIntStateOf(0) }
    val weekDates = remember { buildCurrentWeek() }
    val checklistDraft = remember {
        mutableStateListOf("托福单词 w117", "组会1-1", "听力真题5篇", "阅读", "投简历")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        GoaldayTopBar(leftTitle = "Goalday", rightPrimaryText = "今天", onRightPrimaryClick = { calendarViewModel.backToToday() })

        SegmentedHeader(
            items = listOf("1周", "日记", "清单"),
            selected = mode,
            onSelect = { mode = it },
        )

        when (mode) {
            0 -> WeekBoard(
                weekDates = weekDates,
                entries = calendarState.entries,
                onOpenCalendar = onOpenCalendar,
            )
            1 -> JournalQuickBoard(
                entries = calendarState.entries,
                onOpenHandbook = onOpenHandbook,
            )
            else -> if (checklistEditing == 1) {
                ChecklistEditorBoard(
                    items = checklistDraft,
                    onBack = { checklistEditing = 0 },
                    onDone = { checklistEditing = 0 },
                )
            } else {
                ChecklistQuickBoard(
                    entries = calendarState.entries,
                    onOpenInspiration = onOpenInspiration,
                    onEdit = { checklistEditing = 1 },
                )
            }
        }
    }
}

@Composable
private fun SegmentedHeader(
    items: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF0E7DE), RoundedCornerShape(0.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(0.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items.forEachIndexed { index, label ->
            Text(
                text = label,
                color = if (selected == index) Color(0xFF1F1C19) else Color(0xFF9E958A),
                fontWeight = if (selected == index) FontWeight.SemiBold else FontWeight.Normal,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 2.dp)
                    .clickable { onSelect(index) },
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun WeekBoard(
    weekDates: List<LocalDate>,
    entries: List<ScheduleEntry>,
    onOpenCalendar: () -> Unit,
) {
    val weekEntries = entries.filter { e -> weekDates.any { it.year == e.year && it.monthValue == e.month && it.dayOfMonth == e.day } }
    val weekTodo = weekEntries.filterNot { it.completed }.map { it.title }.distinct().take(6)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFFFEFC), RoundedCornerShape(0.dp)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Text(
                "",
                modifier = Modifier.weight(0.22f),
            )
            Text(
                "● 本周Todo",
                modifier = Modifier.weight(0.80f),
                color = Color(0xFF1F1D1A),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
        }
        weekDates.forEach { day ->
            val dayEntries = entries
                .filter { it.year == day.year && it.month == day.monthValue && it.day == day.dayOfMonth }
                .sortedWith(compareBy<ScheduleEntry> { it.completed }.thenBy { it.title })
            val leftText = dayEntries.filter { it.completed }.take(2).joinToString("\n") { "✓${it.title}" }
            val rightText = dayEntries.filterNot { it.completed }.take(3).joinToString("\n") { "▪ ${it.title}" }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(0.5.dp, Color(0x11000000))
                    .padding(vertical = 7.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(0.18f)) {
                    Text(day.dayOfMonth.toString(), style = MaterialTheme.typography.titleMedium, color = Color(0xFF25221D), fontWeight = FontWeight.SemiBold)
                    Text(weekdayText(day.dayOfWeek), style = MaterialTheme.typography.labelSmall, color = Color(0xFF2C2924))
                }
                Text(
                    text = if (leftText.isBlank()) "" else leftText,
                    modifier = Modifier.weight(0.42f),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF35312C),
                )
                Text(
                    text = if (rightText.isBlank()) "" else rightText,
                    modifier = Modifier.weight(0.40f),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF2A2723),
                    textDecoration = TextDecoration.None,
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            contentAlignment = Alignment.CenterEnd,
        ) {
            Column(
                modifier = Modifier
                    .size(44.dp)
                    .background(Color(0xFFF6AFC2), RoundedCornerShape(99.dp))
                    .clickable { onOpenCalendar() },
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("›", color = Color.White, style = MaterialTheme.typography.headlineSmall)
            }
        }

        if (weekTodo.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                weekTodo.forEach { item ->
                    Text("▪ $item", color = Color(0xFF2A2723), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun JournalQuickBoard(
    entries: List<ScheduleEntry>,
    onOpenHandbook: () -> Unit,
) {
    val done = entries.filter { it.completed }.take(10)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFFFEFC))
            .border(1.dp, Color(0x12000000))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("在清单中勾选一周要做的所有事", color = Color(0xFF2A2723), style = MaterialTheme.typography.titleSmall)
        done.forEachIndexed { index, entry ->
            Text("${index + 1}  ${entry.title}", color = Color(0xFF2F2A24), style = MaterialTheme.typography.bodyMedium)
        }
        Text(
            "进入手账",
            color = Color.White,
            modifier = Modifier
                .background(Color(0xFF212121), RoundedCornerShape(8.dp))
                .clickable { onOpenHandbook() }
                .padding(horizontal = 12.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun ChecklistQuickBoard(
    entries: List<ScheduleEntry>,
    onOpenInspiration: () -> Unit,
    onEdit: () -> Unit,
) {
    val todo = entries.filterNot { it.completed }.take(12)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFFFEFC))
            .border(1.dp, Color(0x12000000))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("在清单中列出这周最重要的目标/主题", color = Color(0xFF2A2723), style = MaterialTheme.typography.titleSmall)
        todo.forEachIndexed { index, entry ->
            Text("□ ${index + 1}  ${entry.title}", color = Color(0xFF2F2A24), style = MaterialTheme.typography.bodyMedium)
        }
        Text(
            "补充灵感",
            color = Color.White,
            modifier = Modifier
                .background(Color(0xFF212121), RoundedCornerShape(8.dp))
                .clickable { onOpenInspiration() }
                .padding(horizontal = 12.dp, vertical = 6.dp),
        )
        Text(
            "编辑清单",
            color = Color(0xFF2F2A24),
            modifier = Modifier
                .clickable { onEdit() }
                .padding(horizontal = 2.dp, vertical = 2.dp),
        )
    }
}

@Composable
private fun ChecklistEditorBoard(
    items: List<String>,
    onBack: () -> Unit,
    onDone: () -> Unit,
) {
    val dateTag = LocalDate.now().plusDays(4).format(DateTimeFormatter.ofPattern("MM-dd-yyyy"))
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFFFEFC))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("‹", color = Color(0xFFD38DA4), style = MaterialTheme.typography.headlineSmall, modifier = Modifier.clickable { onBack() }.padding(horizontal = 4.dp))
            Text(
                "Done",
                color = Color.White,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier
                    .background(Color(0xFF121212), RoundedCornerShape(8.dp))
                    .clickable { onDone() }
                    .padding(horizontal = 16.dp, vertical = 5.dp),
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("● 本周 Todo", color = Color(0xFF1E1D1A), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text("⋯", color = Color(0xFFB5AFAB), style = MaterialTheme.typography.titleLarge)
        }

        items.forEachIndexed { index, item ->
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("□", color = Color(0xFF2A2723), style = MaterialTheme.typography.titleMedium, modifier = Modifier.offset(y = (-1).dp))
                    Text(" ${index + 1}", color = Color(0xFF2A2723), style = MaterialTheme.typography.bodyMedium)
                    Text("  $item", color = Color(0xFF2A2723), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                }
                if (index == 3 || index == items.lastIndex) {
                    Text(
                        dateTag,
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier
                            .padding(start = 30.dp, top = 1.dp)
                            .background(Color(0xFFAFAFB4), RoundedCornerShape(99.dp))
                            .padding(horizontal = 10.dp, vertical = 2.dp),
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 3.dp)
                        .border(0.5.dp, Color(0x18000000)),
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF7F7FA), RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(LocalDate.now().format(DateTimeFormatter.ofPattern("MMM d, yyyy")), color = Color(0xFF555555), style = MaterialTheme.typography.bodySmall)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("◍", color = Color(0xFF3C3C3C))
                Text("↑", color = Color(0xFF3C3C3C))
                Text("✓", color = Color(0xFF3C3C3C))
            }
        }
    }
}

private fun buildCurrentWeek(): List<LocalDate> {
    val today = LocalDate.now()
    val monday = today.with(DayOfWeek.MONDAY)
    return (0..6).map { monday.plusDays(it.toLong()) }
}

private fun weekdayText(dayOfWeek: DayOfWeek): String = when (dayOfWeek) {
    DayOfWeek.MONDAY -> "周一"
    DayOfWeek.TUESDAY -> "周二"
    DayOfWeek.WEDNESDAY -> "周三"
    DayOfWeek.THURSDAY -> "周四"
    DayOfWeek.FRIDAY -> "周五"
    DayOfWeek.SATURDAY -> "周六"
    DayOfWeek.SUNDAY -> "周日"
}
