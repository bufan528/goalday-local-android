package com.bf410.goaldaylocal.ui.home

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.bf410.goaldaylocal.data.ScheduleEntry
import com.bf410.goaldaylocal.ui.calendar.CalendarViewModel
import com.bf410.goaldaylocal.ui.replica.GoaldayDesign
import com.bf410.goaldaylocal.ui.replica.GoaldayTopBar
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.delay

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun HomeScreen(
    calendarViewModel: CalendarViewModel,
    onOpenCalendar: () -> Unit,
    onOpenCalendarForDay: (Int) -> Unit,
    onOpenHandbook: () -> Unit,
    onOpenInspiration: () -> Unit,
) {
    val state by calendarViewModel.uiState.collectAsState()
    val today = LocalDate.now()
    val todayEntries = state.entries
        .filter { it.year == today.year && it.month == today.monthValue && it.day == today.dayOfMonth }
        .sortedWith(compareBy<ScheduleEntry> { it.completed }.thenBy { it.title.lowercase() })

    val currentMonth = YearMonth.of(state.year, state.month)
    val maxDay = currentMonth.lengthOfMonth()

    val backlog = state.entries
        .filter { it.year == state.year && it.month == state.month }
        .filterNot { it.year == today.year && it.month == today.monthValue && it.day == today.dayOfMonth }
        .filterNot { it.completed }
        .sortedWith(compareBy<ScheduleEntry>({ it.day }, { it.title.lowercase() }))
        .take(8)

    var priorityDay by remember(state.year, state.month) { mutableStateOf(today.dayOfMonth.coerceIn(1, maxDay)) }
    priorityDay = priorityDay.coerceIn(1, maxDay)
    val priorityEntries = state.entries
        .filter { it.year == state.year && it.month == state.month && it.day == priorityDay }
        .filterNot { it.completed }
        .sortedBy { it.title.lowercase() }
        .take(3)

    val morning = todayEntries.filter { parseTimeSlot(it.note) == "上午" }
    val afternoon = todayEntries.filter { parseTimeSlot(it.note) == "下午" }
    val evening = todayEntries.filter { parseTimeSlot(it.note) == "晚上" }
    val unassigned = todayEntries.filter { parseTimeSlot(it.note) == null }
    var grabbedEntry by remember { mutableStateOf<ScheduleEntry?>(null) }
    var draggingEntry by remember { mutableStateOf<ScheduleEntry?>(null) }
    var dragPosition by remember { mutableStateOf(Offset.Zero) }
    var activeDropSlot by remember { mutableStateOf<String?>(null) }
    val dropSlotBounds = remember { mutableStateMapOf<String, Rect>() }
    var hint by remember { mutableStateOf("") }
    LaunchedEffect(hint) {
        if (hint.isBlank()) return@LaunchedEffect
        delay(1000)
        hint = ""
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
        GoaldayTopBar(
            leftTitle = "Goalday",
            rightPrimaryText = "今天",
            onRightPrimaryClick = { calendarViewModel.backToToday() },
        )

        StepCard(title = "1. 在清单中列出一周要做的所有事", subtitle = "计划池") {
            grabbedEntry?.let { g ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("已抓取：${g.title}（点上/下/晚投放）", color = Color(0xFFB07A8F), style = MaterialTheme.typography.labelSmall)
                    Text("取消", color = GoaldayDesign.InkSecondary, style = MaterialTheme.typography.labelSmall, modifier = Modifier.clickable { grabbedEntry = null })
                }
            }
            if (backlog.isEmpty()) {
                Text("暂无待安排事项", color = GoaldayDesign.InkSecondary, style = MaterialTheme.typography.bodySmall)
            }
            backlog.forEach { entry ->
                var rowOrigin by remember(entry.id) { mutableStateOf(Offset.Zero) }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (grabbedEntry?.id == entry.id) Color(0x18E88FAE) else Color.Transparent,
                            RoundedCornerShape(GoaldayDesign.RadiusS),
                        )
                        .combinedClickable(
                            onClick = { onOpenCalendarForDay(entry.day) },
                            onLongClick = { grabbedEntry = entry },
                        )
                        .onGloballyPositioned { coords -> rowOrigin = coords.boundsInRoot().topLeft }
                        .pointerInput(entry.id) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = { start ->
                                    draggingEntry = entry
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
                                    val target = draggingEntry
                                    if (targetSlot != null && target != null) {
                                        calendarViewModel.moveScheduleToDay(target.id, today.dayOfMonth)
                                        calendarViewModel.updateSchedule(target.id, target.title, today.dayOfMonth, mergeTimeSlot(target.note, targetSlot))
                                        hint = "已投放到$targetSlot"
                                    } else if (target != null) {
                                        hint = "未命中投放槽位"
                                    }
                                    draggingEntry = null
                                    activeDropSlot = null
                                },
                                onDragCancel = {
                                    draggingEntry = null
                                    activeDropSlot = null
                                    hint = "已取消拖放"
                                },
                            )
                        }
                        .padding(horizontal = 4.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text("○", color = GoaldayDesign.InkMuted, style = MaterialTheme.typography.labelSmall)
                    Text("${entry.day}日", color = GoaldayDesign.InkMuted, style = MaterialTheme.typography.labelSmall)
                    Text(entry.title, color = GoaldayDesign.InkPrimary, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                    Text("长按抓取", color = Color(0xFFB07A8F), style = MaterialTheme.typography.labelSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        QuickSlotChip("上") {
                            calendarViewModel.moveScheduleToDay(entry.id, today.dayOfMonth)
                            calendarViewModel.updateSchedule(entry.id, entry.title, today.dayOfMonth, mergeTimeSlot(entry.note, "上午"))
                        }
                        QuickSlotChip("下") {
                            calendarViewModel.moveScheduleToDay(entry.id, today.dayOfMonth)
                            calendarViewModel.updateSchedule(entry.id, entry.title, today.dayOfMonth, mergeTimeSlot(entry.note, "下午"))
                        }
                        QuickSlotChip("晚") {
                            calendarViewModel.moveScheduleToDay(entry.id, today.dayOfMonth)
                            calendarViewModel.updateSchedule(entry.id, entry.title, today.dayOfMonth, mergeTimeSlot(entry.note, "晚上"))
                        }
                    }
                }
            }
        }

        StepCard(title = "2. 在每页列出最重要的目标/主题", subtitle = "优先级") {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("日期", color = GoaldayDesign.InkSecondary, style = MaterialTheme.typography.labelSmall)
                listOf(priorityDay, (priorityDay + 1).coerceAtMost(maxDay), (priorityDay + 2).coerceAtMost(maxDay)).distinct().forEach { day ->
                    Text(
                        "${day}日",
                        color = if (day == priorityDay) Color.White else GoaldayDesign.InkSecondary,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier
                            .background(if (day == priorityDay) GoaldayDesign.PrimaryAction else Color(0x12000000), RoundedCornerShape(GoaldayDesign.RadiusPill))
                            .clickable { priorityDay = day }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }
            if (priorityEntries.isEmpty()) {
                Text("该日暂无计划", color = GoaldayDesign.InkSecondary, style = MaterialTheme.typography.bodySmall)
            }
            priorityEntries.forEach { entry ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text("•", color = Color(0xFFE88FAE), style = MaterialTheme.typography.labelSmall)
                    Text(entry.title, color = GoaldayDesign.InkPrimary, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                    Text("移到今天", color = Color(0xFFB07A8F), style = MaterialTheme.typography.labelSmall, modifier = Modifier.clickable {
                        calendarViewModel.moveScheduleToDay(entry.id, today.dayOfMonth)
                    })
                }
            }
        }

        StepCard(title = "3. 到了当天，从右侧清单中拖入计划事项", subtitle = "今日执行") {
            TimeSlotLine(
                "上",
                slotKey = "上午",
                assigned = morning.firstOrNull(),
                fallback = grabbedEntry ?: unassigned.firstOrNull(),
                dropReady = grabbedEntry != null || draggingEntry != null,
                hover = activeDropSlot == "上午",
                onZoneBounds = { rect -> dropSlotBounds["上午"] = rect },
            ) { entry, slot ->
                calendarViewModel.updateSchedule(entry.id, entry.title, today.dayOfMonth, mergeTimeSlot(entry.note, slot))
                grabbedEntry = null
                hint = "已投放到$slot"
            }
            TimeSlotLine(
                "下",
                slotKey = "下午",
                assigned = afternoon.firstOrNull(),
                fallback = grabbedEntry ?: unassigned.drop(1).firstOrNull(),
                dropReady = grabbedEntry != null || draggingEntry != null,
                hover = activeDropSlot == "下午",
                onZoneBounds = { rect -> dropSlotBounds["下午"] = rect },
            ) { entry, slot ->
                calendarViewModel.updateSchedule(entry.id, entry.title, today.dayOfMonth, mergeTimeSlot(entry.note, slot))
                grabbedEntry = null
                hint = "已投放到$slot"
            }
            TimeSlotLine(
                "晚",
                slotKey = "晚上",
                assigned = evening.firstOrNull(),
                fallback = grabbedEntry ?: unassigned.drop(2).firstOrNull(),
                dropReady = grabbedEntry != null || draggingEntry != null,
                hover = activeDropSlot == "晚上",
                onZoneBounds = { rect -> dropSlotBounds["晚上"] = rect },
            ) { entry, slot ->
                calendarViewModel.updateSchedule(entry.id, entry.title, today.dayOfMonth, mergeTimeSlot(entry.note, slot))
                grabbedEntry = null
                hint = "已投放到$slot"
            }
            Spacer(Modifier.height(4.dp))
            todayEntries.forEach { entry ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        if (entry.completed) "✓" else "○",
                        color = if (entry.completed) GoaldayDesign.Positive else GoaldayDesign.InkSecondary,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.clickable { calendarViewModel.toggleScheduleCompleted(entry.id) },
                    )
                    Text(
                        entry.title,
                        color = if (entry.completed) GoaldayDesign.InkSecondary else GoaldayDesign.InkPrimary,
                        style = MaterialTheme.typography.bodySmall,
                        textDecoration = if (entry.completed) TextDecoration.LineThrough else TextDecoration.None,
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ActionPill("打开日历", Modifier.weight(1f)) { onOpenCalendar() }
            ActionPill("灵感模板", Modifier.weight(1f)) { onOpenInspiration() }
            ActionPill("手账本", Modifier.weight(1f)) { onOpenHandbook() }
        }
        if (hint.isNotBlank()) {
            Text(hint, color = GoaldayDesign.InkSecondary, style = MaterialTheme.typography.labelSmall)
        }
        grabbedEntry?.let { entry ->
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
                    QuickSlotChip("上") {
                        calendarViewModel.moveScheduleToDay(entry.id, today.dayOfMonth)
                        calendarViewModel.updateSchedule(entry.id, entry.title, today.dayOfMonth, mergeTimeSlot(entry.note, "上午"))
                        grabbedEntry = null
                        hint = "已投放到上午"
                    }
                    QuickSlotChip("下") {
                        calendarViewModel.moveScheduleToDay(entry.id, today.dayOfMonth)
                        calendarViewModel.updateSchedule(entry.id, entry.title, today.dayOfMonth, mergeTimeSlot(entry.note, "下午"))
                        grabbedEntry = null
                        hint = "已投放到下午"
                    }
                    QuickSlotChip("晚") {
                        calendarViewModel.moveScheduleToDay(entry.id, today.dayOfMonth)
                        calendarViewModel.updateSchedule(entry.id, entry.title, today.dayOfMonth, mergeTimeSlot(entry.note, "晚上"))
                        grabbedEntry = null
                        hint = "已投放到晚上"
                    }
                }
            }
        }
        }

        draggingEntry?.let { entry ->
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
}

@Composable
private fun StepCard(
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(GoaldayDesign.Surface, RoundedCornerShape(GoaldayDesign.RadiusM))
            .border(0.8.dp, Color(0x14000000), RoundedCornerShape(GoaldayDesign.RadiusM))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(title, color = GoaldayDesign.InkPrimary, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
            Text(subtitle, color = GoaldayDesign.InkMuted, style = MaterialTheme.typography.labelSmall)
        }
        content()
    }
}

@Composable
private fun TimeSlotLine(
    slotShort: String,
    slotKey: String,
    assigned: ScheduleEntry?,
    fallback: ScheduleEntry?,
    dropReady: Boolean,
    hover: Boolean,
    onZoneBounds: (Rect) -> Unit,
    onAssign: (ScheduleEntry, String) -> Unit,
) {
    val slot = when (slotShort) {
        "上" -> "上午"
        "下" -> "下午"
        else -> "晚上"
    }
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
                if (dropReady) "（点此投放）" else "（点此分配）",
                color = Color(0xFFB07A8F),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.clickable {
                    val target = fallback ?: return@clickable
                    onAssign(target, slot)
                },
            )
        }
    }
}

@Composable
private fun ActionPill(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .background(GoaldayDesign.PrimaryAction, RoundedCornerShape(GoaldayDesign.RadiusPill))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = Color.White, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun QuickSlotChip(label: String, onClick: () -> Unit) {
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
