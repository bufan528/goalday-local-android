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
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingEntry by remember { mutableStateOf<ScheduleEntry?>(null) }
    var selectedDay by remember { mutableIntStateOf(LocalDate.now().dayOfMonth) }
    val month = YearMonth.of(uiState.year, uiState.month)
    val maxDay = month.lengthOfMonth()
    selectedDay = selectedDay.coerceIn(1, maxDay)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("14周", style = MaterialTheme.typography.labelMedium, color = Color(0xFF7E776E))
                Text("|", style = MaterialTheme.typography.labelMedium, color = Color(0xFFD2CBC1))
                Text("Goalday", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = Color(0xFF2B2824))
            }
            Text(
                "完成",
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier
                    .clip(RoundedCornerShape(99.dp))
                    .background(Color(0xFF222222))
                    .clickable { viewModel.backToToday() }
                    .padding(horizontal = 9.dp, vertical = 4.dp),
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF4EEEC), RoundedCornerShape(10.dp))
                .border(1.dp, Color(0x16000000), RoundedCornerShape(10.dp))
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SegText("日程", true, Modifier.weight(1f))
            SegText("${uiState.month}月${selectedDay}日", false, Modifier.weight(1f))
            SegText("清单", false, Modifier.weight(1f))
        }

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
private fun SegText(text: String, active: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(if (active) Color.White else Color.Transparent, RoundedCornerShape(8.dp))
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = if (active) Color(0xFF2D2823) else Color(0xFF9D958B))
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
