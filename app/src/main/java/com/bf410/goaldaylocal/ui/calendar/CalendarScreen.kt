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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.bf410.goaldaylocal.data.ScheduleEntry
import com.bf410.goaldaylocal.ui.replica.BoardTask
import com.bf410.goaldaylocal.ui.replica.DualLaneExecutionBoard
import com.bf410.goaldaylocal.ui.replica.GoaldaySegmentBar
import com.bf410.goaldaylocal.ui.replica.GoaldayTopBar
import java.time.LocalDate
import java.time.YearMonth

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
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingEntry by remember { mutableStateOf<ScheduleEntry?>(null) }
    var selectedDay by remember { mutableIntStateOf(LocalDate.now().dayOfMonth) }
    var mode by remember { mutableStateOf(CalendarMode.SCHEDULE) }
    var agendaFilter by remember { mutableStateOf(AgendaFilter.ALL) }
    val month = YearMonth.of(uiState.year, uiState.month)
    val maxDay = month.lengthOfMonth()
    selectedDay = selectedDay.coerceIn(1, maxDay)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        GoaldayTopBar(
            onRightPrimaryClick = { viewModel.backToToday() },
        )
        GoaldaySegmentBar(
            items = listOf("日程", "${uiState.month}月${selectedDay}日", "清单"),
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
                    onToggleCompleted = { id -> viewModel.toggleScheduleCompleted(id) },
                    onEdit = { editingEntry = it },
                    onDelete = { viewModel.removeSchedule(it) },
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
                    onToggleCompleted = { id -> viewModel.toggleScheduleCompleted(id) },
                    onEdit = { editingEntry = it },
                    onDelete = { viewModel.removeSchedule(it) },
                    onAdd = { showAddDialog = true },
                )
            }
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
                editingEntry = null
            },
        )
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
) {
    val filtered = when (filter) {
        AgendaFilter.ALL -> entries
        AgendaFilter.TODO -> entries.filterNot { it.completed }
        AgendaFilter.DONE -> entries.filter { it.completed }
    }

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
        filtered.sortedBy { it.day }.forEach { entry ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(10.dp))
                    .border(1.dp, Color(0x12000000), RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Text(if (entry.completed) "✓" else "·", color = if (entry.completed) Color(0xFF7A9D71) else Color(0xFFC7BEB4), modifier = Modifier.clickable { onToggleCompleted(entry.id) })
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "${entry.day}日  ${entry.title}",
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
    onAdd: () -> Unit,
) {
    val weekDays = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
    val start = selectedDay.coerceIn(1, maxDay)
    val dayNums = (0..6).map { (start + it).coerceAtMost(maxDay) }
    val todayEntries = entries.filter { it.day == selectedDay }
    val doneEntries = todayEntries.filter { it.completed }
    val todoEntries = todayEntries.filterNot { it.completed }
    val stagedEntries = (todayEntries + entries.filter { it.day != selectedDay }).distinctBy { it.id }.take(14)
    val todoPool = todoEntries.take(6).map { BoardTask(it.id, it.title, it.note, it.completed) }
    val sourcePool = stagedEntries.filter { !it.completed && it.id !in todoPool.map { t -> t.id } }.take(8)
        .map { BoardTask(it.id, it.title, it.note, it.completed) }
    val donePreview = doneEntries.take(3).map { BoardTask(it.id, it.title, it.note, true) }
    val allRight = todoPool + sourcePool + donePreview
    var selectedEntryId by remember(allRight) { mutableStateOf(allRight.firstOrNull()?.id) }
    val selectedEntry = entries.firstOrNull { it.id == selectedEntryId }
    val dayLabels = weekDays.mapIndexed { idx, label -> dayNums[idx].toString() to label }

    DualLaneExecutionBoard(
        leftHeader = "执行",
        rightHeader = "日历执行板",
        dayLabels = dayLabels,
        leftTimelineTasks = doneEntries.take(7).map { it.title },
        todayTasks = todoPool,
        poolTasks = sourcePool,
        donePreviewTasks = donePreview,
        selectedTaskId = selectedEntryId,
        onSelectTask = { selectedEntryId = it },
        onTimelineRowClick = { row -> onSelectDay(dayNums[row]) },
        onActionDone = { task -> onToggleCompleted(task.id) },
        onActionAdd = { task ->
            val raw = entries.firstOrNull { it.id == task.id } ?: return@DualLaneExecutionBoard
            if (raw.day != selectedDay) onEdit(raw.copy(day = selectedDay))
        },
        onActionRestore = { task -> onToggleCompleted(task.id) },
        topActions = {
            Text("✎ 编辑", modifier = Modifier.clickable { selectedEntry?.let(onEdit) }, color = Color(0xFF6F675D), style = MaterialTheme.typography.labelSmall)
            Text("🗑 删除", modifier = Modifier.clickable { selectedEntry?.let { onDelete(it.id) } }, color = Color(0xFF9C5A52), style = MaterialTheme.typography.labelSmall)
            Text(
                "✓ 完成",
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier
                    .clip(RoundedCornerShape(99.dp))
                    .background(Color(0xFF222222))
                    .clickable { selectedEntry?.let { onToggleCompleted(it.id) } }
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            )
            Text("＋ 新增", modifier = Modifier.clickable(onClick = onAdd), color = Color(0xFF2F2A24), style = MaterialTheme.typography.labelSmall)
        },
    )
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
