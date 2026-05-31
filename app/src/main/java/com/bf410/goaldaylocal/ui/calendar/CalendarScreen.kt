package com.bf410.goaldaylocal.ui.calendar

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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.bf410.goaldaylocal.data.ScheduleEntry
import com.bf410.goaldaylocal.ui.replica.GoaldaySegmentBar
import com.bf410.goaldaylocal.ui.replica.GoaldayTopBar
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.delay

private enum class CalendarMode {
    SCHEDULE,
    DATE,
    LIST,
}

private enum class AgendaFilter {
    ALL,
    TODO,
    DONE,
}

@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel,
    focusDay: Int? = null,
    onFocusConsumed: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingEntry by remember { mutableStateOf<ScheduleEntry?>(null) }
    var selectedDay by remember { mutableIntStateOf(LocalDate.now().dayOfMonth) }
    var mode by remember { mutableStateOf(CalendarMode.SCHEDULE) }
    var agendaFilter by remember { mutableStateOf(AgendaFilter.ALL) }
    var actionHint by remember { mutableStateOf("") }
    val month = YearMonth.of(uiState.year, uiState.month)
    val maxDay = month.lengthOfMonth()
    selectedDay = selectedDay.coerceIn(1, maxDay)

    LaunchedEffect(focusDay, maxDay) {
        val day = focusDay ?: return@LaunchedEffect
        selectedDay = day.coerceIn(1, maxDay)
        mode = CalendarMode.SCHEDULE
        onFocusConsumed()
    }
    LaunchedEffect(actionHint) {
        if (actionHint.isBlank()) return@LaunchedEffect
        delay(1400)
        actionHint = ""
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        GoaldayTopBar(
            onRightPrimaryClick = { viewModel.backToToday() },
        )
        CalendarMonthBar(
            year = uiState.year,
            month = uiState.month,
            onPrevious = viewModel::previousMonth,
            onNext = viewModel::nextMonth,
            onToday = {
                viewModel.backToToday()
                selectedDay = LocalDate.now().dayOfMonth
            },
        )
        CalendarStats(
            total = uiState.entries.size,
            done = uiState.entries.count { it.completed },
            todo = uiState.entries.count { !it.completed },
        )
        GoaldaySegmentBar(
            items = listOf("日程", "日期", "清单"),
            selectedIndex = when (mode) {
                CalendarMode.SCHEDULE -> 0
                CalendarMode.DATE -> 1
                CalendarMode.LIST -> 2
            },
            onSelect = { index ->
                mode = when (index) {
                    0 -> CalendarMode.SCHEDULE
                    1 -> CalendarMode.DATE
                    else -> CalendarMode.LIST
                }
            },
        )

        when (mode) {
            CalendarMode.SCHEDULE -> {
                ReferenceCalendarBoard(
                    year = uiState.year,
                    month = uiState.month,
                    maxDay = maxDay,
                    selectedDay = selectedDay,
                    entries = uiState.entries,
                    onSelectDay = { selectedDay = it.coerceIn(1, maxDay) },
                    onToggleCompleted = { id ->
                        viewModel.toggleScheduleCompleted(id)
                        actionHint = "已更新完成状态"
                    },
                    onEdit = { editingEntry = it },
                    onDelete = {
                        viewModel.removeSchedule(it)
                        actionHint = "已删除日程"
                    },
                    onMoveToSelectedDay = { id -> viewModel.moveScheduleToDay(id, selectedDay) },
                    onAdd = { showAddDialog = true },
                )
            }
            CalendarMode.DATE -> {
                MonthGridBoard(
                    year = uiState.year,
                    month = uiState.month,
                    maxDay = maxDay,
                    selectedDay = selectedDay,
                    entries = uiState.entries,
                    onSelectDay = {
                        selectedDay = it.coerceIn(1, maxDay)
                        mode = CalendarMode.SCHEDULE
                        actionHint = "已切换到 ${selectedDay} 日"
                    },
                )
            }
            CalendarMode.LIST -> {
                AgendaListBoard(
                    year = uiState.year,
                    month = uiState.month,
                    entries = uiState.entries,
                    filter = agendaFilter,
                    onFilterChange = { agendaFilter = it },
                    onToggleCompleted = { id ->
                        viewModel.toggleScheduleCompleted(id)
                        actionHint = "已更新完成状态"
                    },
                    onEdit = { editingEntry = it },
                    onDelete = {
                        viewModel.removeSchedule(it)
                        actionHint = "已删除日程"
                    },
                    onAdd = { showAddDialog = true },
                    onOpenDay = { day ->
                        selectedDay = day.coerceIn(1, maxDay)
                        mode = CalendarMode.SCHEDULE
                        actionHint = "已跳转到 ${selectedDay} 日"
                    },
                )
            }
        }
        if (actionHint.isNotBlank()) {
            Text(actionHint, color = Color(0xFF7A7269), style = MaterialTheme.typography.labelSmall)
        }
    }

    if (showAddDialog) {
        ScheduleDialog(
            title = "新增日程",
            maxDay = maxDay,
            initialTitle = "",
            initialDay = selectedDay.coerceIn(1, maxDay),
            initialNote = "",
            onDismiss = { showAddDialog = false },
            onConfirm = { title, day, note ->
                viewModel.addSchedule(title, day, note)
                actionHint = "已新增日程"
                showAddDialog = false
            },
        )
    }

    editingEntry?.let { entry ->
        ScheduleDialog(
            title = "编辑日程",
            maxDay = maxDay,
            initialTitle = entry.title,
            initialDay = entry.day,
            initialNote = entry.note,
            onDismiss = { editingEntry = null },
            onConfirm = { title, day, note ->
                viewModel.updateSchedule(entry.id, title, day, note)
                actionHint = "已保存修改"
                editingEntry = null
            },
        )
    }
}

@Composable
private fun CalendarMonthBar(
    year: Int,
    month: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onToday: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFBFAF8), RoundedCornerShape(14.dp))
            .border(1.dp, Color(0x14000000), RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("‹ 上月", color = Color(0xFF6F675D), style = MaterialTheme.typography.labelLarge, modifier = Modifier.clickable { onPrevious() })
        Text("${year}年${month}月", color = Color(0xFF2F2A24), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("今天", color = Color.White, style = MaterialTheme.typography.labelSmall, modifier = Modifier
                .clip(RoundedCornerShape(99.dp))
                .background(Color(0xFF222222))
                .clickable { onToday() }
                .padding(horizontal = 10.dp, vertical = 5.dp))
            Text("下月 ›", color = Color(0xFF6F675D), style = MaterialTheme.typography.labelLarge, modifier = Modifier.clickable { onNext() })
        }
    }
}

@Composable
private fun CalendarStats(
    total: Int,
    done: Int,
    todo: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        StatPill(label = "总任务", value = total.toString(), modifier = Modifier.weight(1f))
        StatPill(label = "待完成", value = todo.toString(), modifier = Modifier.weight(1f))
        StatPill(label = "已完成", value = done.toString(), modifier = Modifier.weight(1f))
    }
}

@Composable
private fun StatPill(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(Color(0xFFFBFAF8), RoundedCornerShape(12.dp))
            .border(1.dp, Color(0x14000000), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(label, color = Color(0xFF90887F), style = MaterialTheme.typography.labelSmall)
        Text(value, color = Color(0xFF2F2A24), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun MonthGridBoard(
    year: Int,
    month: Int,
    maxDay: Int,
    selectedDay: Int,
    entries: List<ScheduleEntry>,
    onSelectDay: (Int) -> Unit,
) {
    val firstWeekOffset = YearMonth.of(year, month).atDay(1).dayOfWeek.value - 1
    val cells = List(firstWeekOffset) { 0 } + (1..maxDay).toList()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFBFAF8), RoundedCornerShape(14.dp))
            .border(1.dp, Color(0x14000000), RoundedCornerShape(14.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("${year}年${month}月", style = MaterialTheme.typography.titleSmall, color = Color(0xFF2F2A24))
        val weekdays = listOf("一", "二", "三", "四", "五", "六", "日")
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            weekdays.forEach { wd ->
                Text(wd, color = Color(0xFF8D857C), style = MaterialTheme.typography.labelSmall)
            }
        }
        cells.chunked(7).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                row.forEach { day ->
                    if (day == 0) {
                        Spacer(modifier = Modifier.weight(1f))
                        return@forEach
                    }
                    val hasEntry = entries.any { it.day == day }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (day == selectedDay) Color(0xFFF2EADF) else Color(0xFFF8F6F2),
                                RoundedCornerShape(8.dp),
                            )
                            .border(
                                1.dp,
                                if (day == selectedDay) Color(0xFFBDA17F) else Color(0x12000000),
                                RoundedCornerShape(8.dp),
                            )
                            .clickable { onSelectDay(day) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(if (hasEntry) "$day •" else day.toString(), color = Color(0xFF3A332C), style = MaterialTheme.typography.bodySmall)
                    }
                }
                repeat(7 - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun AgendaListBoard(
    year: Int,
    month: Int,
    entries: List<ScheduleEntry>,
    filter: AgendaFilter,
    onFilterChange: (AgendaFilter) -> Unit,
    onToggleCompleted: (String) -> Unit,
    onEdit: (ScheduleEntry) -> Unit,
    onDelete: (String) -> Unit,
    onAdd: () -> Unit,
    onOpenDay: (Int) -> Unit,
) {
    val filtered = when (filter) {
        AgendaFilter.ALL -> entries
        AgendaFilter.TODO -> entries.filterNot { it.completed }
        AgendaFilter.DONE -> entries.filter { it.completed }
    }
    val grouped = filtered.sortedWith(compareBy<ScheduleEntry> { it.day }.thenBy { it.completed }.thenBy { it.title }).groupBy { it.day }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFBFAF8), RoundedCornerShape(14.dp))
            .border(1.dp, Color(0x14000000), RoundedCornerShape(14.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("${year}年${month}月 · 清单", style = MaterialTheme.typography.titleSmall, color = Color(0xFF2F2A24))
            Text("＋ 新增", color = Color(0xFF2F2A24), style = MaterialTheme.typography.labelSmall, modifier = Modifier.clickable { onAdd() })
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            FilterChip(label = "全部", active = filter == AgendaFilter.ALL, onClick = { onFilterChange(AgendaFilter.ALL) })
            FilterChip(label = "未完成", active = filter == AgendaFilter.TODO, onClick = { onFilterChange(AgendaFilter.TODO) })
            FilterChip(label = "已完成", active = filter == AgendaFilter.DONE, onClick = { onFilterChange(AgendaFilter.DONE) })
        }
        grouped.forEach { (day, dayEntries) ->
            Text(
                "${month}月${day}日",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF7B7268),
                modifier = Modifier
                    .clip(RoundedCornerShape(99.dp))
                    .background(Color(0x12000000))
                    .clickable { onOpenDay(day) }
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            )
            dayEntries.forEach { entry ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(10.dp))
                        .border(1.dp, Color(0x12000000), RoundedCornerShape(10.dp))
                        .clickable { onOpenDay(day) }
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Text(if (entry.completed) "✓" else "·", color = if (entry.completed) Color(0xFF7A9D71) else Color(0xFFC7BEB4), modifier = Modifier.clickable { onToggleCompleted(entry.id) })
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            entry.title,
                            color = Color(0xFF2D2823),
                            textDecoration = if (entry.completed) TextDecoration.LineThrough else TextDecoration.None,
                        )
                        if (entry.note.isNotBlank()) Text(entry.note, style = MaterialTheme.typography.labelSmall, color = Color(0xFF8D857C))
                    }
                    Text("✎", color = Color(0xFF70685F), modifier = Modifier.clickable { onEdit(entry) })
                    Text("🗑", color = Color(0xFF9C5A52), modifier = Modifier.clickable { onDelete(entry.id) })
                }
            }
        }
    }
}

@Composable
private fun FilterChip(
    label: String,
    active: Boolean,
    onClick: () -> Unit,
) {
    Text(
        text = label,
        color = if (active) Color.White else Color(0xFF6F675D),
        style = MaterialTheme.typography.labelSmall,
        modifier = Modifier
            .background(if (active) Color(0xFF2D2A26) else Color(0x12000000), RoundedCornerShape(99.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp),
    )
}

@Composable
private fun ReferenceCalendarBoard(
    year: Int,
    month: Int,
    maxDay: Int,
    selectedDay: Int,
    entries: List<ScheduleEntry>,
    onSelectDay: (Int) -> Unit,
    onToggleCompleted: (String) -> Unit,
    onEdit: (ScheduleEntry) -> Unit,
    onDelete: (String) -> Unit,
    onMoveToSelectedDay: (String) -> Unit,
    onAdd: () -> Unit,
) {
    val todayEntries = entries.filter { it.day == selectedDay }
    val doneEntries = todayEntries.filter { it.completed }
    val todoEntries = todayEntries.filterNot { it.completed }
    var selectedEntryId by remember(todayEntries) { mutableStateOf(todayEntries.firstOrNull()?.id) }
    val selectedEntry = todayEntries.firstOrNull { it.id == selectedEntryId }

    val week = remember(year, month, selectedDay, maxDay) {
        val start = ((selectedDay - 1) / 7) * 7 + 1
        (start until (start + 7)).filter { it <= maxDay }
    }
    val weekLabel = listOf("一", "二", "三", "四", "五", "六", "日")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFBFAF8), RoundedCornerShape(14.dp))
            .border(1.dp, Color(0x14000000), RoundedCornerShape(14.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("${year}年${month}月", color = Color(0xFF2F2A24), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("＋ 新增", color = Color(0xFF2F2A24), style = MaterialTheme.typography.labelSmall, modifier = Modifier.clickable { onAdd() })
                Text("◀", color = Color(0xFF6F675D), modifier = Modifier.clickable { onSelectDay((selectedDay - 1).coerceAtLeast(1)) })
                Text("▶", color = Color(0xFF6F675D), modifier = Modifier.clickable { onSelectDay((selectedDay + 1).coerceAtMost(maxDay)) })
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            week.forEachIndexed { idx, day ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (day == selectedDay) Color(0xFF2D2A26) else Color(0xFFF5F1EB))
                        .clickable { onSelectDay(day) }
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = weekLabel[idx],
                        color = if (day == selectedDay) Color(0xFFF4E2D6) else Color(0xFF8D857C),
                        style = MaterialTheme.typography.labelSmall,
                    )
                    Text(
                        text = day.toString(),
                        color = if (day == selectedDay) Color.White else Color(0xFF2F2A24),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    if (entries.any { it.day == day && !it.completed }) {
                        Text("•", color = if (day == selectedDay) Color(0xFFFFC6D9) else Color(0xFFE88FAE), modifier = Modifier.offset(y = (-2).dp))
                    } else {
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }
        }

        Text(
            text = "${month}月${selectedDay}日  待办 ${todoEntries.size} · 已完成 ${doneEntries.size}",
            color = Color(0xFF746D65),
            style = MaterialTheme.typography.labelSmall,
        )

        if (todayEntries.isEmpty()) {
            Text(
                "今天还没有任务，点击右上角新增",
                color = Color(0xFF8D857C),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(10.dp))
                    .border(1.dp, Color(0x12000000), RoundedCornerShape(10.dp))
                    .padding(vertical = 18.dp),
            )
        } else {
            todayEntries.forEach { entry ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (entry.id == selectedEntryId) Color(0xFFF0E7DC) else Color.White)
                        .border(1.dp, Color(0x12000000), RoundedCornerShape(10.dp))
                        .clickable { selectedEntryId = entry.id }
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Text(
                        if (entry.completed) "✓" else "○",
                        color = if (entry.completed) Color(0xFF7A9D71) else Color(0xFFB0A89E),
                        modifier = Modifier.clickable { onToggleCompleted(entry.id) },
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            entry.title,
                            color = Color(0xFF2D2823),
                            textDecoration = if (entry.completed) TextDecoration.LineThrough else TextDecoration.None,
                        )
                        if (entry.note.isNotBlank()) {
                            Text(entry.note, style = MaterialTheme.typography.labelSmall, color = Color(0xFF8D857C))
                        }
                    }
                    Text("移动", color = Color(0xFF8A8178), style = MaterialTheme.typography.labelSmall, modifier = Modifier.clickable { onMoveToSelectedDay(entry.id) })
                    Text("✎", color = Color(0xFF70685F), modifier = Modifier.clickable { onEdit(entry) })
                    Text("🗑", color = Color(0xFF9C5A52), modifier = Modifier.clickable { onDelete(entry.id) })
                }
            }
        }

        if (selectedEntry != null) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("选中：${selectedEntry.title}", color = Color(0xFF5E5750), style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
                Text(
                    if (selectedEntry.completed) "恢复" else "完成",
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                        .clip(RoundedCornerShape(99.dp))
                        .background(Color(0xFF222222))
                        .clickable { onToggleCompleted(selectedEntry.id) }
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                )
            }
        }
    }
}

@Composable
private fun ScheduleDialog(
    title: String,
    maxDay: Int,
    initialTitle: String,
    initialDay: Int,
    initialNote: String,
    onDismiss: () -> Unit,
    onConfirm: (title: String, day: Int, note: String) -> Unit,
) {
    var draftTitle by remember(initialTitle) { mutableStateOf(initialTitle) }
    var draftDay by remember(initialDay) { mutableStateOf(initialDay.toString()) }
    var draftNote by remember(initialNote) { mutableStateOf(initialNote) }
    val quickDays = remember(maxDay, initialDay) {
        listOf(initialDay, (initialDay + 1).coerceAtMost(maxDay), (initialDay + 2).coerceAtMost(maxDay), maxDay)
            .distinct()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    val parsedDay = draftDay.toIntOrNull()
                    if (draftTitle.isNotBlank() && parsedDay != null && parsedDay in 1..maxDay) {
                        onConfirm(draftTitle.trim(), parsedDay, draftNote.trim())
                    }
                },
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = draftTitle,
                    onValueChange = { draftTitle = it },
                    label = { Text("任务标题") },
                    singleLine = true,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    quickDays.forEach { day ->
                        Text(
                            text = "${day}日",
                            color = if (draftDay == day.toString()) Color.White else Color(0xFF6F675D),
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier
                                .background(
                                    if (draftDay == day.toString()) Color(0xFF2D2A26) else Color(0x12000000),
                                    RoundedCornerShape(99.dp),
                                )
                                .clickable { draftDay = day.toString() }
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                        )
                    }
                }
                OutlinedTextField(
                    value = draftDay,
                    onValueChange = { draftDay = it.filter(Char::isDigit) },
                    label = { Text("日期（1-$maxDay）") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
                OutlinedTextField(
                    value = draftNote,
                    onValueChange = { draftNote = it },
                    label = { Text("备注（可选）") },
                )
            }
        },
    )
}
