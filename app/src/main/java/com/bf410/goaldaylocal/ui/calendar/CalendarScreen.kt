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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.bf410.goaldaylocal.data.ScheduleEntry
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
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
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
    val todo = entries.filter { it.day == selectedDay }
    val stacked = (todo + entries.filter { it.day != selectedDay }).take(10)
    var selectedEntryId by remember(stacked) { mutableStateOf(stacked.firstOrNull()?.id) }
    val selectedEntry = stacked.firstOrNull { it.id == selectedEntryId } ?: stacked.firstOrNull()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(412.dp)
            .background(Color(0xFFFBFAF8), RoundedCornerShape(12.dp))
            .border(1.dp, Color(0x14000000), RoundedCornerShape(12.dp)),
    ) {
        Column(
            modifier = Modifier
                .weight(1.1f)
                .fillMaxSize(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF4EEEC))
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                SegText("日程", true, Modifier.weight(1f))
                SegText("${month}月${selectedDay}日", false, Modifier.weight(1f))
                SegText("清单", false, Modifier.weight(1f))
            }
            weekDays.forEachIndexed { idx, label ->
                val day = dayNums[idx]
                val hasEntry = entries.any { it.day == day }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .border(0.5.dp, Color(0x12000000))
                        .clickable { onSelectDay(day) }
                        .padding(horizontal = 6.dp, vertical = 5.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Column(modifier = Modifier.width(30.dp)) {
                        Text(day.toString(), fontWeight = if (day == selectedDay) FontWeight.SemiBold else FontWeight.Normal)
                        Text(label, style = MaterialTheme.typography.labelSmall, color = Color(0xFF7D766C))
                    }
                    Text(
                        text = entries.firstOrNull { it.day == day }?.title ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (hasEntry) Color(0xFF2F2924) else Color(0xFFB8B1A7),
                    )
                }
            }
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
                .border(1.dp, Color(0x12000000)),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("To do  ˅", modifier = Modifier.background(Color(0xFFF8F8F6), RoundedCornerShape(8.dp)).padding(horizontal = 7.dp, vertical = 3.dp), style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("完成", modifier = Modifier.clickable { selectedEntry?.let { onToggleCompleted(it.id) } }, color = Color(0xFF2F2A24))
                    Text("编辑", modifier = Modifier.clickable { selectedEntry?.let(onEdit) }, color = Color(0xFF6F675D))
                    Text("删除", modifier = Modifier.clickable { selectedEntry?.let { onDelete(it.id) } }, color = Color(0xFF9C5A52))
                    Text("新增", modifier = Modifier.clickable(onClick = onAdd))
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                stacked.forEach { entry ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Text(if (entry.completed) "✓" else "·", color = if (entry.completed) Color(0xFF7A9D71) else Color(0xFFD8CFC5), modifier = Modifier.clickable { onToggleCompleted(entry.id) })
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                entry.title,
                                textDecoration = if (entry.completed) TextDecoration.LineThrough else TextDecoration.None,
                                modifier = Modifier.clickable { selectedEntryId = entry.id },
                            )
                            if (entry.note.isNotBlank()) Text(entry.note, style = MaterialTheme.typography.labelSmall, color = Color(0xFF8D857C))
                        }
                        Text("编", modifier = Modifier.clickable { onEdit(entry) }, color = Color(0xFF70685F))
                        Text("删", modifier = Modifier.clickable { onDelete(entry.id) }, color = Color(0xFF9C5A52))
                    }
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Text("${year}年${month}月", modifier = Modifier.padding(12.dp), color = Color(0xFF8D867C), style = MaterialTheme.typography.labelSmall)
        }
    }
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
