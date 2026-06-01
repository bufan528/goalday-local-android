package com.bf410.goaldaylocal.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import com.bf410.goaldaylocal.data.ScheduleEntry
import com.bf410.goaldaylocal.ui.replica.GoaldayDesign
import com.bf410.goaldaylocal.ui.replica.GoaldayTopBar
import java.time.LocalDate
import java.time.YearMonth
import java.time.DayOfWeek
import kotlinx.coroutines.delay

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun CalendarScreen(
    viewModel: CalendarViewModel,
    focusDay: Int? = null,
    onFocusConsumed: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()
    var selectedDay by remember { mutableIntStateOf(LocalDate.now().dayOfMonth) }
    var editingEntry by remember { mutableStateOf<ScheduleEntry?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var toast by remember { mutableStateOf("") }
    var grabbedPoolEntry by remember { mutableStateOf<ScheduleEntry?>(null) }
    var draggingPoolEntry by remember { mutableStateOf<ScheduleEntry?>(null) }
    var dragPosition by remember { mutableStateOf(Offset.Zero) }
    var activeDropSlot by remember { mutableStateOf<String?>(null) }
    val dropSlotBounds = remember { mutableStateMapOf<String, Rect>() }

    val maxDay = YearMonth.of(state.year, state.month).lengthOfMonth()
    selectedDay = selectedDay.coerceIn(1, maxDay)

    LaunchedEffect(focusDay, maxDay) {
        val day = focusDay ?: return@LaunchedEffect
        selectedDay = day.coerceIn(1, maxDay)
        toast = "已定位到 ${selectedDay} 日"
        onFocusConsumed()
    }
    LaunchedEffect(toast) {
        if (toast.isBlank()) return@LaunchedEffect
        delay(1200)
        toast = ""
    }

    val dayEntries = state.entries
        .filter { it.year == state.year && it.month == state.month && it.day == selectedDay }
        .sortedWith(compareBy<ScheduleEntry> { it.completed }.thenBy { it.title.lowercase() })
    val doneEntries = dayEntries.filter { it.completed }
    val todoEntries = dayEntries.filterNot { it.completed }
    val poolEntries = state.entries
        .filter { it.year == state.year && it.month == state.month }
        .filterNot { it.day == selectedDay }
        .filterNot { it.completed }
        .sortedWith(compareBy<ScheduleEntry>({ it.day }, { it.title.lowercase() }))
        .take(12)

    val weekStart = ((selectedDay - 1) / 7) * 7 + 1
    val weekDays = (weekStart until (weekStart + 7)).filter { it <= maxDay }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
        GoaldayTopBar(
            rightPrimaryText = "今天",
            onRightPrimaryClick = {
                viewModel.backToToday()
                selectedDay = LocalDate.now().dayOfMonth.coerceIn(1, maxDay)
                toast = "已回到今天"
            },
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(GoaldayDesign.Surface, RoundedCornerShape(GoaldayDesign.RadiusM))
                .border(1.dp, Color(0x14000000), RoundedCornerShape(GoaldayDesign.RadiusM))
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("‹ 上月", color = GoaldayDesign.InkSecondary, modifier = Modifier.clickable { viewModel.previousMonth() })
            Text("${state.year}年${state.month}月", color = GoaldayDesign.InkPrimary, fontWeight = FontWeight.Medium)
            Text("下月 ›", color = GoaldayDesign.InkSecondary, modifier = Modifier.clickable { viewModel.nextMonth() })
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            weekDays.forEach { day ->
                val weekday = when (YearMonth.of(state.year, state.month).atDay(day).dayOfWeek) {
                    DayOfWeek.MONDAY -> "一"
                    DayOfWeek.TUESDAY -> "二"
                    DayOfWeek.WEDNESDAY -> "三"
                    DayOfWeek.THURSDAY -> "四"
                    DayOfWeek.FRIDAY -> "五"
                    DayOfWeek.SATURDAY -> "六"
                    DayOfWeek.SUNDAY -> "日"
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(if (day == selectedDay) GoaldayDesign.PrimaryAction else GoaldayDesign.SurfaceSoft, RoundedCornerShape(GoaldayDesign.RadiusS))
                        .clickable { selectedDay = day }
                        .padding(vertical = 7.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(weekday, color = if (day == selectedDay) Color(0xFFDECFC3) else Color(0xFF8D857C), style = MaterialTheme.typography.labelSmall)
                    Text(day.toString(), color = if (day == selectedDay) Color.White else GoaldayDesign.InkPrimary, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        BoardCard(title = "${state.month}月${selectedDay}日 · 今日执行", subtitle = "待办 ${todoEntries.size} / 已完成 ${doneEntries.size}") {
            grabbedPoolEntry?.let { g ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("已抓取：${g.title}（点上/下/晚投放）", color = Color(0xFFB07A8F), style = MaterialTheme.typography.labelSmall)
                    Text("取消", color = GoaldayDesign.InkSecondary, style = MaterialTheme.typography.labelSmall, modifier = Modifier.clickable {
                        grabbedPoolEntry = null
                        toast = "已取消抓取"
                    })
                }
            }
            TimeSlotRow(
                "上",
                slotKey = "上午",
                assigned = todoEntries.firstOrNull { parseTimeSlot(it.note) == "上午" },
                fallback = grabbedPoolEntry ?: todoEntries.firstOrNull { parseTimeSlot(it.note) == null },
                dropReady = grabbedPoolEntry != null || draggingPoolEntry != null,
                hover = activeDropSlot == "上午",
                onZoneBounds = { rect -> dropSlotBounds["上午"] = rect },
            ) { e ->
                if (grabbedPoolEntry?.id == e.id) viewModel.moveScheduleToDay(e.id, selectedDay)
                viewModel.updateSchedule(e.id, e.title, selectedDay, mergeTimeSlot(e.note, "上午"))
                grabbedPoolEntry = null
                toast = "已投放到上午"
            }
            TimeSlotRow(
                "下",
                slotKey = "下午",
                assigned = todoEntries.firstOrNull { parseTimeSlot(it.note) == "下午" },
                fallback = grabbedPoolEntry ?: todoEntries.drop(1).firstOrNull { parseTimeSlot(it.note) == null },
                dropReady = grabbedPoolEntry != null || draggingPoolEntry != null,
                hover = activeDropSlot == "下午",
                onZoneBounds = { rect -> dropSlotBounds["下午"] = rect },
            ) { e ->
                if (grabbedPoolEntry?.id == e.id) viewModel.moveScheduleToDay(e.id, selectedDay)
                viewModel.updateSchedule(e.id, e.title, selectedDay, mergeTimeSlot(e.note, "下午"))
                grabbedPoolEntry = null
                toast = "已投放到下午"
            }
            TimeSlotRow(
                "晚",
                slotKey = "晚上",
                assigned = todoEntries.firstOrNull { parseTimeSlot(it.note) == "晚上" },
                fallback = grabbedPoolEntry ?: todoEntries.drop(2).firstOrNull { parseTimeSlot(it.note) == null },
                dropReady = grabbedPoolEntry != null || draggingPoolEntry != null,
                hover = activeDropSlot == "晚上",
                onZoneBounds = { rect -> dropSlotBounds["晚上"] = rect },
            ) { e ->
                if (grabbedPoolEntry?.id == e.id) viewModel.moveScheduleToDay(e.id, selectedDay)
                viewModel.updateSchedule(e.id, e.title, selectedDay, mergeTimeSlot(e.note, "晚上"))
                grabbedPoolEntry = null
                toast = "已投放到晚上"
            }
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(0.5.dp, Color(0x12000000), RoundedCornerShape(GoaldayDesign.RadiusS))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("done", color = GoaldayDesign.InkSecondary, style = MaterialTheme.typography.labelSmall)
                    if (doneEntries.isEmpty()) {
                        Text("○", color = GoaldayDesign.InkMuted, style = MaterialTheme.typography.bodySmall)
                    } else {
                        doneEntries.take(4).forEach { entry ->
                            Text(
                                "✓ ${entry.title}",
                                color = GoaldayDesign.InkSecondary,
                                style = MaterialTheme.typography.bodySmall,
                                textDecoration = TextDecoration.LineThrough,
                                maxLines = 1,
                            )
                        }
                    }
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("todo", color = GoaldayDesign.InkSecondary, style = MaterialTheme.typography.labelSmall)
                    if (todoEntries.isEmpty()) {
                        Text("○", color = GoaldayDesign.InkMuted, style = MaterialTheme.typography.bodySmall)
                    } else {
                        todoEntries.take(4).forEach { entry ->
                            Text(
                                "○ ${entry.title}",
                                color = GoaldayDesign.InkPrimary,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
            if (dayEntries.isEmpty()) {
                Text("当天暂无任务", color = GoaldayDesign.InkSecondary, style = MaterialTheme.typography.bodySmall)
            }
            dayEntries.forEach { entry ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        if (entry.completed) "✓" else "○",
                        color = if (entry.completed) GoaldayDesign.Positive else GoaldayDesign.InkMuted,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.clickable { viewModel.toggleScheduleCompleted(entry.id) },
                    )
                    Text(
                        entry.title,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (entry.completed) GoaldayDesign.InkSecondary else GoaldayDesign.InkPrimary,
                        textDecoration = if (entry.completed) TextDecoration.LineThrough else TextDecoration.None,
                        modifier = Modifier.weight(1f),
                    )
                    Text("✎", color = GoaldayDesign.InkSecondary, style = MaterialTheme.typography.labelSmall, modifier = Modifier.clickable { editingEntry = entry })
                    Text("🗑", color = GoaldayDesign.Danger, style = MaterialTheme.typography.labelSmall, modifier = Modifier.clickable { viewModel.removeSchedule(entry.id) })
                }
            }
            Text(
                "＋ 新增当天任务",
                color = Color.White,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier
                    .align(Alignment.End)
                    .background(Color(0xFFE88FAE), RoundedCornerShape(GoaldayDesign.RadiusPill))
                    .clickable { showAddDialog = true }
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            )
        }

        BoardCard(title = "计划池", subtitle = "从其他日期移动到今天") {
            if (poolEntries.isEmpty()) {
                Text("暂无可移动任务", color = GoaldayDesign.InkSecondary, style = MaterialTheme.typography.bodySmall)
            }
            poolEntries.forEach { entry ->
                var rowOrigin by remember(entry.id) { mutableStateOf(Offset.Zero) }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (grabbedPoolEntry?.id == entry.id) Color(0x18E88FAE) else Color.Transparent,
                            RoundedCornerShape(GoaldayDesign.RadiusS),
                        )
                        .combinedClickable(
                            onClick = {},
                            onLongClick = {
                                grabbedPoolEntry = entry
                                toast = "已抓取任务"
                            },
                        )
                        .onGloballyPositioned { coords -> rowOrigin = coords.boundsInRoot().topLeft }
                        .pointerInput(entry.id) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = { start ->
                                    draggingPoolEntry = entry
                                    dragPosition = rowOrigin + start
                                    activeDropSlot = dropSlotBounds.entries.firstOrNull { it.value.contains(dragPosition) }?.key
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    dragPosition += dragAmount
                                    activeDropSlot = dropSlotBounds.entries.firstOrNull { it.value.contains(dragPosition) }?.key
                                },
                                onDragEnd = {
                                    val targetSlot = activeDropSlot
                                    val targetEntry = draggingPoolEntry
                                    if (targetSlot != null && targetEntry != null) {
                                        viewModel.moveScheduleToDay(targetEntry.id, selectedDay)
                                        viewModel.updateSchedule(targetEntry.id, targetEntry.title, selectedDay, mergeTimeSlot(targetEntry.note, targetSlot))
                                        toast = "已投放到$targetSlot"
                                    } else if (targetEntry != null) {
                                        toast = "未命中投放槽位"
                                    }
                                    draggingPoolEntry = null
                                    activeDropSlot = null
                                },
                                onDragCancel = {
                                    draggingPoolEntry = null
                                    activeDropSlot = null
                                    toast = "已取消拖放"
                                },
                            )
                        }
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text("○", color = GoaldayDesign.InkMuted, style = MaterialTheme.typography.labelSmall)
                    Text("${entry.day}日", color = GoaldayDesign.InkSecondary, style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(30.dp))
                    Text(entry.title, color = GoaldayDesign.InkPrimary, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                    Text("长按抓取", color = Color(0xFFB07A8F), style = MaterialTheme.typography.labelSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        DropToSlotChip("上") {
                            viewModel.moveScheduleToDay(entry.id, selectedDay)
                            viewModel.updateSchedule(entry.id, entry.title, selectedDay, mergeTimeSlot(entry.note, "上午"))
                            toast = "已投放到上午"
                        }
                        DropToSlotChip("下") {
                            viewModel.moveScheduleToDay(entry.id, selectedDay)
                            viewModel.updateSchedule(entry.id, entry.title, selectedDay, mergeTimeSlot(entry.note, "下午"))
                            toast = "已投放到下午"
                        }
                        DropToSlotChip("晚") {
                            viewModel.moveScheduleToDay(entry.id, selectedDay)
                            viewModel.updateSchedule(entry.id, entry.title, selectedDay, mergeTimeSlot(entry.note, "晚上"))
                            toast = "已投放到晚上"
                        }
                    }
                }
            }
        }

        if (toast.isNotBlank()) {
            Text(toast, color = Color(0xFF7A7269), style = MaterialTheme.typography.labelSmall)
        }
        grabbedPoolEntry?.let { entry ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0x1AE88FAE), RoundedCornerShape(GoaldayDesign.RadiusPill))
                    .border(0.8.dp, Color(0x22E88FAE), RoundedCornerShape(GoaldayDesign.RadiusPill))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("快速投放：${entry.title}", color = GoaldayDesign.InkPrimary, style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    DropToSlotChip("上") {
                        viewModel.moveScheduleToDay(entry.id, selectedDay)
                        viewModel.updateSchedule(entry.id, entry.title, selectedDay, mergeTimeSlot(entry.note, "上午"))
                        grabbedPoolEntry = null
                        toast = "已投放到上午"
                    }
                    DropToSlotChip("下") {
                        viewModel.moveScheduleToDay(entry.id, selectedDay)
                        viewModel.updateSchedule(entry.id, entry.title, selectedDay, mergeTimeSlot(entry.note, "下午"))
                        grabbedPoolEntry = null
                        toast = "已投放到下午"
                    }
                    DropToSlotChip("晚") {
                        viewModel.moveScheduleToDay(entry.id, selectedDay)
                        viewModel.updateSchedule(entry.id, entry.title, selectedDay, mergeTimeSlot(entry.note, "晚上"))
                        grabbedPoolEntry = null
                        toast = "已投放到晚上"
                    }
                }
            }
        }
        }

        draggingPoolEntry?.let { entry ->
            Column(
                modifier = Modifier
                    .offset { IntOffset(dragPosition.x.toInt(), dragPosition.y.toInt()) }
                    .background(if (activeDropSlot != null) Color(0xFFE88FAE) else Color(0xDDE88FAE), RoundedCornerShape(GoaldayDesign.RadiusS))
                    .border(if (activeDropSlot != null) 1.2.dp else 0.8.dp, Color.White, RoundedCornerShape(GoaldayDesign.RadiusS))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(entry.title, color = Color.White, style = MaterialTheme.typography.labelSmall)
                Text(
                    if (activeDropSlot != null) "释放投放到$activeDropSlot" else "拖到上/下/晚槽位",
                    color = Color.White.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }

    if (showAddDialog) {
        ScheduleDialog(
            title = "新增日程",
            maxDay = maxDay,
            initialTitle = "",
            initialDay = selectedDay,
            initialNote = "",
            onDismiss = { showAddDialog = false },
            onConfirm = { title, day, note ->
                viewModel.addSchedule(title, day, note)
                selectedDay = day
                showAddDialog = false
                toast = "已新增任务"
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
                toast = "已保存"
            },
        )
    }
}

@Composable
private fun BoardCard(
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(GoaldayDesign.Surface, RoundedCornerShape(GoaldayDesign.RadiusM))
            .border(1.dp, Color(0x14000000), RoundedCornerShape(GoaldayDesign.RadiusM))
            .padding(horizontal = 11.dp, vertical = 9.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(title, color = GoaldayDesign.InkPrimary, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
            Text(subtitle, color = GoaldayDesign.InkSecondary, style = MaterialTheme.typography.labelSmall)
        }
        content()
    }
}

@Composable
private fun TimeSlotRow(
    slotShort: String,
    slotKey: String,
    assigned: ScheduleEntry?,
    fallback: ScheduleEntry?,
    dropReady: Boolean,
    hover: Boolean,
    onZoneBounds: (Rect) -> Unit,
    onAssign: (ScheduleEntry) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { coords -> onZoneBounds(coords.boundsInRoot()) }
            .background(
                if (hover) Color(0x33E88FAE) else if (dropReady && assigned == null) Color(0x22E88FAE) else Color.Transparent,
                RoundedCornerShape(GoaldayDesign.RadiusS),
            )
            .border(if (hover) 1.dp else 0.5.dp, if (hover) Color(0xFFE88FAE) else Color(0x12000000), RoundedCornerShape(GoaldayDesign.RadiusS))
            .padding(horizontal = 8.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(slotShort, color = GoaldayDesign.InkSecondary, style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(16.dp))
        if (assigned != null) {
            Text(assigned.title, color = GoaldayDesign.InkPrimary, style = MaterialTheme.typography.bodySmall)
        } else {
            Text(
                "（点此分配）",
                color = Color(0xFFB07A8F),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.clickable {
                    val target = fallback ?: return@clickable
                    onAssign(target)
                },
            )
        }
    }
}

@Composable
private fun DropToSlotChip(label: String, onClick: () -> Unit) {
    Text(
        label,
        color = Color.White,
        style = MaterialTheme.typography.labelSmall,
        modifier = Modifier
            .background(Color(0xFFB07A8F), RoundedCornerShape(GoaldayDesign.RadiusPill))
            .clickable { onClick() }
            .padding(horizontal = 6.dp, vertical = 2.dp),
    )
}

private fun parseTimeSlot(note: String): String? {
    val slotPrefix = "时段:"
    val index = note.indexOf(slotPrefix)
    if (index < 0) return null
    val raw = note.substring(index + slotPrefix.length).trim()
    return raw.split(" ").firstOrNull()?.takeIf { it in listOf("上午", "下午", "晚上") }
}

private fun mergeTimeSlot(note: String, slot: String): String {
    val cleaned = note.replace(Regex("时段:(上午|下午|晚上)"), "").trim()
    return if (cleaned.isBlank()) "时段:$slot" else "时段:$slot $cleaned"
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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = draftTitle,
                    onValueChange = { draftTitle = it },
                    label = { Text("任务") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = draftDay,
                    onValueChange = { input -> draftDay = input.filter { it.isDigit() }.take(2) },
                    label = { Text("日期(1-$maxDay)") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = draftNote,
                    onValueChange = { draftNote = it },
                    label = { Text("备注") },
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val t = draftTitle.trim()
                val d = draftDay.toIntOrNull()?.coerceIn(1, maxDay) ?: initialDay
                if (t.isNotBlank()) onConfirm(t, d, draftNote.trim())
            }) { Text("保存") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        },
    )
}
