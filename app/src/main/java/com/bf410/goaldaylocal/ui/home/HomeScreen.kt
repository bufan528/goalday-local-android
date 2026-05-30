package com.bf410.goaldaylocal.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
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

private enum class WeeklyMode { WEEK, DIARY, CHECKLIST }

data class ChecklistDraftItem(
    var text: String,
    var deadline: String = "",
    var checked: Boolean = false,
)

@Composable
fun HomeScreen(
    calendarViewModel: CalendarViewModel,
    onOpenCalendar: () -> Unit,
    onOpenHandbook: () -> Unit,
    onOpenInspiration: () -> Unit,
) {
    val calendarState by calendarViewModel.uiState.collectAsState()
    var mode by rememberSaveable { mutableStateOf(WeeklyMode.WEEK) }
    val weekDates = remember { buildCurrentWeek() }

    val checklistDraft = remember {
        mutableStateListOf(
            ChecklistDraftItem("托福单词 w117", "02-24-2026"),
            ChecklistDraftItem("组会1-1"),
            ChecklistDraftItem("听力真题5篇"),
            ChecklistDraftItem("阅读", "02-26-2026"),
            ChecklistDraftItem("投简历"),
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            "为J人而生的APP",
            color = Color.White,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier
                .background(Color(0xFF101010), RoundedCornerShape(4.dp))
                .padding(horizontal = 8.dp, vertical = 3.dp),
        )
        GoaldayTopBar(
            leftTitle = "Goalday",
            rightPrimaryText = "今天",
            onRightPrimaryClick = { calendarViewModel.backToToday() },
        )

        SegmentedHeader(
            items = listOf("1周", "日记", "清单"),
            selected = mode.ordinal,
            onSelect = { mode = WeeklyMode.entries[it] },
        )

        when (mode) {
            WeeklyMode.WEEK -> WeekBoard(weekDates, calendarState.entries, onOpenCalendar)
            WeeklyMode.DIARY -> DiaryBoard(calendarState.entries, onOpenHandbook)
            WeeklyMode.CHECKLIST -> ChecklistBoard(
                checklistDraft = checklistDraft,
                scheduleEntries = calendarState.entries,
                onAddSchedule = { title ->
                    calendarViewModel.addSchedule(title = title, day = LocalDate.now().dayOfMonth, note = "首页清单")
                },
                onRemoveSchedule = { id -> calendarViewModel.removeSchedule(id) },
                onToggleScheduleDone = { id -> calendarViewModel.toggleScheduleCompleted(id) },
                onUpdateScheduleTitle = { id, title, day, note ->
                    calendarViewModel.updateSchedule(id, title, day, note)
                },
                onUpdateScheduleDay = { id, day -> calendarViewModel.moveScheduleToDay(id, day) },
                onOpenInspiration = onOpenInspiration,
            )
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
            .background(Color(0xFFF0E8DF))
            .padding(horizontal = 10.dp, vertical = 8.dp),
    ) {
        items.forEachIndexed { index, label ->
            Text(
                text = label,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSelect(index) },
                textAlign = TextAlign.Center,
                color = if (index == selected) Color(0xFFE88FAE) else Color(0xFF9B9389),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = if (index == selected) FontWeight.SemiBold else FontWeight.Normal,
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
    val weekTodo = weekEntries.filterNot { it.completed }.map { it.title }.distinct().take(8)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFFFEFC)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
        ) {
            Spacer(modifier = Modifier.weight(0.2f))
            Text(
                "● 本周Todo",
                modifier = Modifier.weight(0.8f),
                color = Color(0xFF22201C),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
        }

        weekDates.forEach { day ->
            val dayEntries = entries
                .filter { it.year == day.year && it.month == day.monthValue && it.day == day.dayOfMonth }
                .sortedWith(compareBy<ScheduleEntry> { it.completed }.thenBy { it.title })
            val doneText = dayEntries.filter { it.completed }.take(2).joinToString("\n") { "✓${it.title}" }
            val todoText = dayEntries.filterNot { it.completed }.take(3).joinToString("\n") { "▪ ${it.title}" }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(0.5.dp, Color(0x13000000))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(0.2f)) {
                    Text(day.dayOfMonth.toString(), style = MaterialTheme.typography.titleMedium, color = Color(0xFF27231F), fontWeight = FontWeight.SemiBold)
                    Text(weekdayText(day.dayOfWeek), style = MaterialTheme.typography.labelSmall, color = Color(0xFF34302C))
                }
                Text(doneText, modifier = Modifier.weight(0.4f), style = MaterialTheme.typography.labelSmall, color = Color(0xFF37322D))
                Text(todoText, modifier = Modifier.weight(0.4f), style = MaterialTheme.typography.labelSmall, color = Color(0xFF2D2925), textDecoration = TextDecoration.None)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                weekTodo.take(4).forEach { Text("▪ $it", style = MaterialTheme.typography.labelSmall, color = Color(0xFF2D2925)) }
            }
            Text(
                "›",
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier
                    .background(Color(0xFFF6AFC2), RoundedCornerShape(99.dp))
                    .clickable { onOpenCalendar() }
                    .padding(horizontal = 14.dp, vertical = 2.dp),
            )
        }
    }
}

@Composable
private fun DiaryBoard(
    entries: List<ScheduleEntry>,
    onOpenHandbook: () -> Unit,
) {
    val done = entries.filter { it.completed }.take(12)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFFFEFC))
            .border(1.dp, Color(0x12000000))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Text("日记", color = Color(0xFF22201C), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        done.forEachIndexed { idx, entry ->
            Text("${idx + 1}. ${entry.title}", color = Color(0xFF2F2A24), style = MaterialTheme.typography.bodySmall)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.fillMaxWidth()) {
            repeat(3) { i ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .background(
                            when (i) {
                                0 -> Color(0xFFD6D1CA)
                                1 -> Color(0xFFD9C1B5)
                                else -> Color(0xFFC8D3BF)
                            },
                            RoundedCornerShape(6.dp),
                        ),
                )
            }
        }
        Text(
            "翻页查看手账",
            color = Color.White,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier
                .background(Color(0xFF1E1E1E), RoundedCornerShape(7.dp))
                .clickable { onOpenHandbook() }
                .padding(horizontal = 12.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun ChecklistBoard(
    checklistDraft: MutableList<ChecklistDraftItem>,
    scheduleEntries: List<ScheduleEntry>,
    onAddSchedule: (String) -> Unit,
    onRemoveSchedule: (String) -> Unit,
    onToggleScheduleDone: (String) -> Unit,
    onUpdateScheduleTitle: (String, String, Int, String) -> Unit,
    onUpdateScheduleDay: (String, Int) -> Unit,
    onOpenInspiration: () -> Unit,
) {
    var focusedIndex by remember { mutableIntStateOf(0) }
    var inputText by remember { mutableStateOf("") }
    var editingIndex by remember { mutableIntStateOf(-1) }
    var editingText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFFFEFC))
            .border(1.dp, Color(0x12000000))
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("‹", color = Color(0xFFD28CA3), style = MaterialTheme.typography.headlineSmall)
            Text("● 本周 Todo", color = Color(0xFF22201C), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text("Done", color = Color.White, modifier = Modifier.background(Color(0xFF111111), RoundedCornerShape(8.dp)).padding(horizontal = 14.dp, vertical = 5.dp))
        }

        checklistDraft.forEachIndexed { index, item ->
            Column(modifier = Modifier.fillMaxWidth().clickable { focusedIndex = index }) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(if (item.checked) "✓" else "□", color = if (item.checked) Color(0xFF7AA071) else Color(0xFF302D28), style = MaterialTheme.typography.bodySmall)
                    Text(" ${index + 1}", color = Color(0xFF302D28), style = MaterialTheme.typography.bodySmall)
                    if (editingIndex == index) {
                        BasicTextField(
                            value = editingText,
                            onValueChange = { editingText = it },
                            textStyle = TextStyle(color = Color(0xFF302D28)),
                            modifier = Modifier
                                .weight(1f)
                                .background(Color(0x0A000000), RoundedCornerShape(4.dp))
                                .padding(horizontal = 5.dp, vertical = 2.dp),
                        )
                        Text("存", color = Color(0xFFE88FAE), style = MaterialTheme.typography.labelSmall, modifier = Modifier.clickable {
                            val next = editingText.trim().ifBlank { item.text }
                            val old = item.text
                            checklistDraft[index] = checklistDraft[index].copy(text = next)
                            val today = LocalDate.now().dayOfMonth
                            scheduleEntries.firstOrNull { it.day == today && it.title == old }?.let {
                                onUpdateScheduleTitle(it.id, next, it.day, it.note)
                            }
                            editingIndex = -1
                        })
                    } else {
                        Text("  ${item.text}", color = Color(0xFF302D28), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        Text("✎", color = Color(0xFF9A9085), style = MaterialTheme.typography.labelSmall, modifier = Modifier.clickable {
                            editingIndex = index
                            editingText = item.text
                        })
                    }
                }
                if (item.deadline.isNotBlank()) {
                    Text(
                        item.deadline,
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier
                            .padding(start = 28.dp, bottom = 2.dp)
                            .background(Color(0xFFADADB3), RoundedCornerShape(99.dp))
                            .clickable {
                                val nextDate = nextDeadlineDate(item.deadline)
                                checklistDraft[index] = checklistDraft[index].copy(deadline = nextDate)
                                val today = LocalDate.now().dayOfMonth
                                scheduleEntries.firstOrNull { it.day == today && it.title == checklistDraft[index].text }?.let { entry ->
                                    val day = parseDeadlineDay(nextDate).coerceAtLeast(1)
                                    onUpdateScheduleDay(entry.id, day)
                                }
                            }
                            .padding(horizontal = 9.dp, vertical = 1.dp),
                    )
                }
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0x12000000)))
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF6F6FA), RoundedCornerShape(8.dp))
                .padding(horizontal = 9.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(LocalDate.now().format(DateTimeFormatter.ofPattern("MMM d, yyyy")), style = MaterialTheme.typography.bodySmall, color = Color(0xFF575757))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("🗑", modifier = Modifier.clickable {
                    if (checklistDraft.isNotEmpty()) {
                        val idx = focusedIndex.coerceIn(0, checklistDraft.lastIndex)
                        val removed = checklistDraft.removeAt(idx)
                        val today = LocalDate.now().dayOfMonth
                        scheduleEntries.firstOrNull { it.day == today && it.title == removed.text }?.let { onRemoveSchedule(it.id) }
                        focusedIndex = (focusedIndex - 1).coerceAtLeast(0)
                    }
                })
                Text("＋", modifier = Modifier.clickable {
                    val text = inputText.trim().ifBlank { "新任务" }
                    val deadline = LocalDate.now().plusDays(3).format(DateTimeFormatter.ofPattern("MM-dd-yyyy"))
                    checklistDraft.add(focusedIndex.coerceIn(0, checklistDraft.size), ChecklistDraftItem(text, deadline = deadline))
                    onAddSchedule(text)
                    inputText = ""
                })
                Text("✓", modifier = Modifier.clickable {
                    if (checklistDraft.isNotEmpty()) {
                        val i = focusedIndex.coerceIn(0, checklistDraft.lastIndex)
                        val next = !checklistDraft[i].checked
                        checklistDraft[i] = checklistDraft[i].copy(checked = next)
                        val today = LocalDate.now().dayOfMonth
                        scheduleEntries.firstOrNull { it.day == today && it.title == checklistDraft[i].text }?.let { onToggleScheduleDone(it.id) }
                    }
                })
            }
        }

        BasicTextField(
            value = inputText,
            onValueChange = { inputText = it },
            textStyle = TextStyle(color = Color(0xFF2C2925)),
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0x08000000), RoundedCornerShape(6.dp))
                .padding(horizontal = 9.dp, vertical = 7.dp),
            decorationBox = { inner ->
                if (inputText.isBlank()) Text("输入任务内容，点 + 插入", color = Color(0xFF9A9188), style = MaterialTheme.typography.bodySmall)
                inner()
            },
        )

        Text(
            "补充灵感",
            color = Color(0xFFE88FAE),
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.clickable { onOpenInspiration() },
        )

        Text(
            "快速添加日程",
            color = Color.White,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier
                .background(Color(0xFF242424), RoundedCornerShape(7.dp))
                .clickable {
                    val text = inputText.trim().ifBlank { "新日程" }
                    val deadline = LocalDate.now().plusDays(3).format(DateTimeFormatter.ofPattern("MM-dd-yyyy"))
                    checklistDraft.add(0, ChecklistDraftItem(text, deadline = deadline))
                    onAddSchedule(text)
                    inputText = ""
                }
                .padding(horizontal = 10.dp, vertical = 5.dp),
        )
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

private fun nextDeadlineDate(current: String): String {
    val formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy")
    val parsed = runCatching { LocalDate.parse(current, formatter) }.getOrNull() ?: LocalDate.now()
    return parsed.plusDays(1).format(formatter)
}

private fun parseDeadlineDay(current: String): Int {
    val formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy")
    return runCatching { LocalDate.parse(current, formatter).dayOfMonth }.getOrDefault(LocalDate.now().dayOfMonth)
}
