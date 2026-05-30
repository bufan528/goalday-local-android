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
import androidx.compose.ui.graphics.Brush
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
    val today = LocalDate.now()
    val firstOffset = month.atDay(1).dayOfWeek.value - 1
    val totalCells = ((firstOffset + maxDay + 6) / 7) * 7
    val entryDays = uiState.entries.map { it.day }.toSet()
    selectedDay = selectedDay.coerceIn(1, maxDay)
    val selectedDayEntries = uiState.entries.filter { it.day == selectedDay }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("${uiState.year}年${uiState.month}月", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, color = Color(0xFF2E2A26))
            Text(
                "回到今天",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF2D2A26),
                modifier = Modifier
                    .background(Color(0x12000000), RoundedCornerShape(99.dp))
                    .clickable(onClick = viewModel::backToToday)
                    .padding(horizontal = 10.dp, vertical = 5.dp),
            )
        }

        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text("‹ 上个月", modifier = Modifier.clickable(onClick = viewModel::previousMonth), color = Color(0xFF888177))
            Text(
                "新增日程",
                modifier = Modifier.clickable {
                    selectedDay = selectedDay.coerceIn(1, maxDay)
                    showAddDialog = true
                },
                color = Color(0xFF2D2A26),
            )
            Text("下个月 ›", modifier = Modifier.clickable(onClick = viewModel::nextMonth), color = Color(0xFF888177))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(listOf(Color(0xFFFFFFFF), Color(0xFFFAF7F2))),
                    RoundedCornerShape(18.dp),
                )
                .padding(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .width(1.dp)
                    .fillMaxSize()
                    .background(Color(0x12000000)),
            )
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                CalendarHeader()
                repeat(totalCells / 7) { rowIndex ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        repeat(7) { columnIndex ->
                            val cellIndex = rowIndex * 7 + columnIndex
                            val day = cellIndex - firstOffset + 1
                            val enabled = day in 1..maxDay
                            DayCell(
                                day = if (enabled) day else null,
                                marked = day in entryDays,
                                selected = enabled && day == selectedDay,
                                isToday = enabled &&
                                    uiState.year == today.year &&
                                    uiState.month == today.monthValue &&
                                    day == today.dayOfMonth,
                                onClick = {
                                    if (enabled) {
                                        selectedDay = day
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("${uiState.month}月${selectedDay}日 日程", style = MaterialTheme.typography.titleSmall)
                Text(
                    "新增",
                    color = Color(0xFF2D2A26),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                        .background(Color(0x12000000), RoundedCornerShape(99.dp))
                        .clickable { showAddDialog = true }
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                )
            }
            if (selectedDayEntries.isEmpty()) {
                Text("当天还没有日程，点“新增”即可添加。", color = Color(0xFF7D7266))
            } else {
                selectedDayEntries.forEach { entry ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF8F8F6), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(
                                        if (entry.completed) Color(0xFFD8D1C8) else Color(0xFFF4F2EE),
                                        RoundedCornerShape(4.dp),
                                    )
                                    .border(1.dp, Color(0xFFBEB8AF), RoundedCornerShape(4.dp))
                                    .clickable { viewModel.toggleScheduleCompleted(entry.id) },
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    "${entry.month}月${entry.day}日 ${entry.title}",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        textDecoration = if (entry.completed) TextDecoration.LineThrough else TextDecoration.None,
                                    ),
                                    color = if (entry.completed) Color(0xFF8B847D) else Color(0xFF342C24),
                                )
                                if (entry.note.isNotBlank()) {
                                    Text(
                                        entry.note,
                                        color = if (entry.completed) Color(0xFFAAA39C) else Color(0xFF7D7266),
                                        textDecoration = if (entry.completed) TextDecoration.LineThrough else TextDecoration.None,
                                    )
                                }
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("编辑", color = Color(0xFF6F675D), modifier = Modifier.clickable { editingEntry = entry })
                            Text("删除", color = Color(0xFF9C5A52), modifier = Modifier.clickable { viewModel.removeSchedule(entry.id) })
                        }
                    }
                }
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
private fun DayCell(
    day: Int?,
    marked: Boolean,
    selected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .width(36.dp)
            .height(40.dp)
            .background(
                when {
                    selected -> Color(0xFFEEE3D4)
                    isToday -> Color(0xFFF1E1CF)
                    day != null -> Color(0x10FFFFFF)
                    else -> Color.Transparent
                },
                RoundedCornerShape(10.dp),
            )
            .clickable(enabled = day != null, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (day != null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(day.toString(), color = Color(0xFF3A3028))
                if (marked) {
                    Spacer(Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .background(Color(0xFF7FA579), RoundedCornerShape(99.dp)),
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        listOf("一", "二", "三", "四", "五", "六", "日").forEach {
            Text(it, color = Color(0xFF7D7266), style = MaterialTheme.typography.labelLarge)
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
