package com.bf410.goaldaylocal.ui.calendar

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.core.content.ContextCompat
import com.bf410.goaldaylocal.data.ScheduleEntry
import com.bf410.goaldaylocal.ui.replica.GoaldayDesign
import java.time.Instant
import java.time.LocalDate
import java.time.DayOfWeek
import java.time.YearMonth
import java.time.ZoneId
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
    var deleteCandidate by remember { mutableStateOf<ScheduleEntry?>(null) }
    var deleteSeries by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showImportPreview by remember { mutableStateOf(false) }
    var showImportSourcePicker by remember { mutableStateOf(false) }
    var showImportRangeDialog by remember { mutableStateOf(false) }
    var importPreviewEvents by remember { mutableStateOf<List<CalendarImportCandidate>>(emptyList()) }
    var importRangeMonths by remember { mutableIntStateOf(1) }
    var selectedImportCalendars by remember { mutableStateOf<Set<String>>(emptySet()) }
    var toast by remember { mutableStateOf("") }
    var grabbedPoolEntry by remember { mutableStateOf<ScheduleEntry?>(null) }
    var draggingPoolEntry by remember { mutableStateOf<ScheduleEntry?>(null) }
    var draggingDayEntry by remember { mutableStateOf<ScheduleEntry?>(null) }
    var dragPosition by remember { mutableStateOf(Offset.Zero) }
    var activeDropSlot by remember { mutableStateOf<String?>(null) }
    var activeDoneDrop by remember { mutableStateOf(false) }
    var doneDropBounds by remember { mutableStateOf(Rect.Zero) }
    val dropSlotBounds = remember { mutableStateMapOf<String, Rect>() }
    val context = LocalContext.current

    fun prepareCalendarImportPreview(rangeMonths: Int) {
        importRangeMonths = rangeMonths
        val events = readSystemCalendarEvents(context, state.year, state.month, rangeMonths)
        importPreviewEvents = events
        if (events.isEmpty()) {
            toast = "没有可导入的新日历事件"
        } else if (events.map { importCalendarName(it) }.distinct().size > 1) {
            selectedImportCalendars = events.map { importCalendarName(it) }.toSet()
            showImportSourcePicker = true
        } else {
            showImportPreview = true
        }
    }

    val calendarPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            showImportRangeDialog = true
        } else {
            toast = "未获得系统日历权限"
        }
    }

    val maxDay = YearMonth.of(state.year, state.month).lengthOfMonth()
    selectedDay = selectedDay.coerceIn(1, maxDay)

    LaunchedEffect(state.year, state.month) {
        selectedDay = selectedDay.coerceIn(1, maxDay)
        grabbedPoolEntry = null
        draggingPoolEntry = null
        draggingDayEntry = null
        activeDropSlot = null
        activeDoneDrop = false
        dropSlotBounds.clear()
        doneDropBounds = Rect.Zero
    }
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
        .let(::sortDayEntries)
    val monthEntries = state.entries
        .filter { it.year == state.year && it.month == state.month }
    val doneEntries = dayEntries.filter { it.completed }
    val todoEntries = dayEntries.filterNot { it.completed }
    val monthTodoCount = monthEntries.count { !it.completed }
    val monthDoneCount = monthEntries.count { it.completed }
    val poolEntries = monthEntries
        .filterNot { it.day == selectedDay }
        .filterNot { it.completed }
        .let(::sortCalendarEntries)
        .take(12)

    val weekStart = ((selectedDay - 1) / 7) * 7 + 1
    val weekDays = (weekStart until (weekStart + 7)).filter { it <= maxDay }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = GoaldayDesign.Space3),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
        CalendarHeroHeader(
            year = state.year,
            month = state.month,
            selectedDay = selectedDay,
            todoCount = monthTodoCount,
            doneCount = monthDoneCount,
            onToday = {
                val today = LocalDate.now()
                viewModel.backToToday()
                selectedDay = today.dayOfMonth
                toast = "已回到今天"
            },
        )

        CalendarThemeField(
            value = state.theme,
            onValueChange = viewModel::updateTheme,
        )

        CalendarMonthControl(
            year = state.year,
            month = state.month,
            onPreviousMonth = viewModel::previousMonth,
            onNextMonth = viewModel::nextMonth,
            onImportCalendar = {
                val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED
                if (granted) {
                    showImportRangeDialog = true
                } else {
                    calendarPermissionLauncher.launch(Manifest.permission.READ_CALENDAR)
                }
            },
        )

        CalendarMonthGrid(
            year = state.year,
            month = state.month,
            selectedDay = selectedDay,
            entries = monthEntries,
            onSelectDay = { selectedDay = it },
        )

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
                        .background(if (day == selectedDay) GoaldayDesign.PrimaryAction else GoaldayDesign.adaptiveSurfaceSoft, RoundedCornerShape(GoaldayDesign.RadiusS))
                        .clickable { selectedDay = day }
                        .padding(vertical = 7.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(weekday, color = if (day == selectedDay) Color.White else GoaldayDesign.adaptiveInkMuted, style = MaterialTheme.typography.labelSmall)
                    Text(day.toString(), color = if (day == selectedDay) Color.White else GoaldayDesign.adaptiveInkPrimary, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        BoardCard(title = "${state.month}月${selectedDay}日 · 今日执行", subtitle = "待办 ${todoEntries.size} / 已完成 ${doneEntries.size}") {
            grabbedPoolEntry?.let { g ->
                Row(horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space2), verticalAlignment = Alignment.CenterVertically) {
                    Text("已抓取：${g.title}（点上/下/晚投放）", color = GoaldayDesign.RouteDiary, style = MaterialTheme.typography.labelSmall)
                    Text("取消", color = GoaldayDesign.adaptiveInkSecondary, style = MaterialTheme.typography.labelSmall, modifier = Modifier.clickable {
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
                    .background(if (activeDoneDrop) GoaldayDesign.GreenSoft else Color.Transparent, RoundedCornerShape(GoaldayDesign.RadiusS))
                    .border(0.5.dp, GoaldayDesign.adaptiveDivider, RoundedCornerShape(GoaldayDesign.RadiusS))
                    .padding(horizontal = GoaldayDesign.Space2, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .onGloballyPositioned { doneDropBounds = it.boundsInRoot() },
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text("已完成", color = GoaldayDesign.adaptiveInkSecondary, style = MaterialTheme.typography.labelSmall)
                    if (doneEntries.isEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(if (activeDoneDrop) Icons.Filled.Check else Icons.Filled.RadioButtonUnchecked, contentDescription = null, tint = if (activeDoneDrop) GoaldayDesign.Positive else GoaldayDesign.adaptiveInkMuted, modifier = Modifier.size(12.dp))
                            if (activeDoneDrop) {
                                Spacer(Modifier.width(4.dp))
                                Text("释放放入已完成", color = GoaldayDesign.Positive, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    } else {
                        doneEntries.take(4).forEach { entry ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space1),
                            ) {
                                Icon(
                                    Icons.Filled.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = GoaldayDesign.adaptiveInkSecondary,
                                )
                                Text(
                                    entry.title,
                                    color = GoaldayDesign.adaptiveInkSecondary,
                                    style = MaterialTheme.typography.bodySmall,
                                    textDecoration = TextDecoration.LineThrough,
                                    maxLines = 1,
                                )
                            }
                        }
                    }
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("待办", color = GoaldayDesign.adaptiveInkSecondary, style = MaterialTheme.typography.labelSmall)
                    if (todoEntries.isEmpty()) {
                        Icon(Icons.Filled.RadioButtonUnchecked, contentDescription = null, tint = GoaldayDesign.adaptiveInkMuted, modifier = Modifier.size(12.dp))
                    } else {
                        todoEntries.take(4).forEach { entry ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space1),
                            ) {
                                Icon(
                                    Icons.Filled.RadioButtonUnchecked,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = GoaldayDesign.adaptiveInkMuted,
                                )
                                Text(
                                    entry.title,
                                    color = GoaldayDesign.adaptiveInkPrimary,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1,
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
            if (dayEntries.isEmpty()) {
                Text("当天暂无任务", color = GoaldayDesign.adaptiveInkSecondary, style = MaterialTheme.typography.bodySmall)
            }
            dayEntries.forEach { entry ->
                var dayRowOrigin by remember(entry.id) { mutableStateOf(Offset.Zero) }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (draggingDayEntry?.id == entry.id) GoaldayDesign.PinkTint else Color.Transparent, RoundedCornerShape(GoaldayDesign.RadiusS))
                        .onGloballyPositioned { dayRowOrigin = it.boundsInRoot().topLeft }
                        .pointerInput(entry.id, entry.completed) {
                            if (entry.completed) return@pointerInput
                            detectDragGesturesAfterLongPress(
                                onDragStart = { start ->
                                    draggingDayEntry = entry
                                    dragPosition = dayRowOrigin + start
                                    activeDoneDrop = doneDropBounds.contains(dragPosition)
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    dragPosition += dragAmount
                                    activeDoneDrop = doneDropBounds.contains(dragPosition)
                                },
                                onDragEnd = {
                                    val target = draggingDayEntry
                                    if (target != null && activeDoneDrop) {
                                        viewModel.toggleScheduleCompleted(target.id)
                                        toast = "已放入已完成"
                                    } else if (target != null) {
                                        toast = "未命中已完成区"
                                    }
                                    draggingDayEntry = null
                                    activeDoneDrop = false
                                },
                                onDragCancel = {
                                    draggingDayEntry = null
                                    activeDoneDrop = false
                                    toast = "已取消拖放"
                                },
                            )
                        }
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        if (entry.completed) Icons.Filled.Check else Icons.Filled.RadioButtonUnchecked,
                        contentDescription = if (entry.completed) "已完成" else "待办",
                        tint = if (entry.completed) GoaldayDesign.Positive else GoaldayDesign.adaptiveInkMuted,
                        modifier = Modifier
                            .size(14.dp)
                            .clickable { viewModel.toggleScheduleCompleted(entry.id) },
                    )
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        Text(
                            entry.title,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (entry.completed) GoaldayDesign.adaptiveInkSecondary else GoaldayDesign.adaptiveInkPrimary,
                            textDecoration = if (entry.completed) TextDecoration.LineThrough else TextDecoration.None,
                        )
                        scheduleMetaText(entry).takeIf { it.isNotBlank() }?.let { meta ->
                            Text(meta, color = GoaldayDesign.adaptiveInkMuted, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                        }
                    }
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = "编辑",
                        tint = GoaldayDesign.adaptiveInkSecondary,
                        modifier = Modifier
                            .size(14.dp)
                            .clickable {
                                editingEntry = entry
                            },
                    )
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "删除",
                            tint = GoaldayDesign.Danger,
                            modifier = Modifier
                                .size(14.dp)
                                .clickable {
                                    deleteCandidate = entry
                                    deleteSeries = false
                                },
                        )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .align(Alignment.End)
                    .background(GoaldayDesign.Pink, RoundedCornerShape(GoaldayDesign.RadiusPill))
                    .clickable { showAddDialog = true }
                    .padding(horizontal = 10.dp, vertical = GoaldayDesign.Space1),
            ) {
                Icon(Icons.Filled.Add, contentDescription = "新增", tint = Color.White, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text(
                    "新增当天任务",
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }

        BoardCard(title = "计划池", subtitle = "从其他日期移动到今天") {
            if (poolEntries.isEmpty()) {
                Text("暂无可移动任务", color = GoaldayDesign.adaptiveInkSecondary, style = MaterialTheme.typography.bodySmall)
            }
            poolEntries.forEach { entry ->
                var rowOrigin by remember(entry.id) { mutableStateOf(Offset.Zero) }
                val grabbed = grabbedPoolEntry?.id == entry.id
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(if (grabbed) 5.dp else 0.dp, RoundedCornerShape(GoaldayDesign.RadiusM), clip = false)
                        .background(
                            if (grabbed) GoaldayDesign.PinkSoft else GoaldayDesign.adaptiveSurface,
                            RoundedCornerShape(GoaldayDesign.RadiusM),
                        )
                        .border(
                            0.7.dp,
                            if (grabbed) GoaldayDesign.Pink.copy(alpha = 0.26f) else GoaldayDesign.BorderColor.copy(alpha = 0.09f),
                            RoundedCornerShape(GoaldayDesign.RadiusM),
                        )
                        .combinedClickable(
                            onClick = {
                                grabbedPoolEntry = entry
                                toast = "已抓取任务，可点上午/下午/晚上投放"
                            },
                        )
                        .onGloballyPositioned { coords -> rowOrigin = coords.boundsInRoot().topLeft }
                        .pointerInput(entry.id) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = { start ->
                                    // P1-2 修复：手势冲突
                                    // 原 combinedClickable.onLongClick 和 detectDragGesturesAfterLongPress
                                    // 都响应长按，导致 grabbedPoolEntry 被设置两次且 toast 冲突
                                    // 现统一由 detectDragGesturesAfterLongPress 处理长按
                                    grabbedPoolEntry = entry
                                    draggingPoolEntry = entry
                                    dragPosition = rowOrigin + start
                                    activeDropSlot = dropSlotBounds.entries.firstOrNull { it.value.contains(dragPosition) }?.key
                                    activeDoneDrop = false
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    dragPosition += dragAmount
                                    activeDropSlot = dropSlotBounds.entries.firstOrNull { it.value.contains(dragPosition) }?.key
                                    activeDoneDrop = false
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
                                    activeDoneDrop = false
                                },
                                onDragCancel = {
                                    draggingPoolEntry = null
                                    activeDropSlot = null
                                    activeDoneDrop = false
                                    toast = "已取消拖放"
                                },
                            )
                        }
                        .padding(horizontal = 9.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space2),
                ) {
                    Text(
                        "${entry.day}日",
                        color = if (grabbed) Color.White else GoaldayDesign.Pink,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .background(if (grabbed) GoaldayDesign.Pink else GoaldayDesign.PinkTint, RoundedCornerShape(GoaldayDesign.RadiusPill))
                            .padding(horizontal = 7.dp, vertical = 3.dp),
                    )
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        Text(entry.title, color = GoaldayDesign.adaptiveInkPrimary, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, maxLines = 1)
                        scheduleMetaText(entry).takeIf { it.isNotBlank() }?.let { meta ->
                            Text(meta, color = GoaldayDesign.adaptiveInkMuted, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                        }
                    }
                    Text(
                        if (grabbed) "已抓取" else "点按抓取",
                        color = if (grabbed) GoaldayDesign.Pink else GoaldayDesign.RouteDiary,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space1)) {
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
            CalendarHintPill(toast)
        }
        grabbedPoolEntry?.let { entry ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GoaldayDesign.PinkTint, RoundedCornerShape(GoaldayDesign.RadiusPill))
                    .border(0.8.dp, GoaldayDesign.Pink.copy(alpha = 0.13f), RoundedCornerShape(GoaldayDesign.RadiusPill))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("快速投放：${entry.title}", color = GoaldayDesign.adaptiveInkPrimary, style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
                Row(horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space1)) {
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

        (draggingPoolEntry ?: draggingDayEntry)?.let { entry ->
            Column(
                modifier = Modifier
                    .offset { IntOffset(dragPosition.x.toInt(), dragPosition.y.toInt()) }
                    .background(if (activeDropSlot != null || activeDoneDrop) GoaldayDesign.Pink else GoaldayDesign.Pink.copy(alpha = 0.87f), RoundedCornerShape(GoaldayDesign.RadiusS))
                    .border(if (activeDropSlot != null || activeDoneDrop) 1.2.dp else 0.8.dp, GoaldayDesign.WhiteOverlayHigh, RoundedCornerShape(GoaldayDesign.RadiusS))
                    .padding(horizontal = GoaldayDesign.Space2, vertical = GoaldayDesign.Space1),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(entry.title, color = Color.White, style = MaterialTheme.typography.labelSmall)
                Text(
                    when {
                        draggingDayEntry != null && activeDoneDrop -> "释放放入已完成"
                        draggingDayEntry != null -> "拖到已完成区"
                        activeDropSlot != null -> "释放投放到$activeDropSlot"
                        else -> "拖到上/下/晚槽位"
                    },
                    color = Color.White.copy(alpha = 0.92f),
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
            initialTimeText = "",
            initialRepeatRule = "",
            initialRepeatInterval = 1,
            initialRepeatEndDate = "",
            allowSeriesEdit = false,
            onDismiss = { showAddDialog = false },
            onConfirm = { title, day, note, timeText, repeatRule, repeatInterval, repeatEndDate, _ ->
                viewModel.addSchedule(title, day, note, timeText, repeatRule, repeatInterval, repeatEndDate)
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
            initialTimeText = entry.timeText,
            initialRepeatRule = entry.repeatRule,
            initialRepeatInterval = entry.repeatInterval,
            initialRepeatEndDate = entry.repeatEndDate,
            allowSeriesEdit = entry.repeatGroupId.isNotBlank(),
            onDismiss = { editingEntry = null },
            onConfirm = { title, day, note, timeText, repeatRule, repeatInterval, repeatEndDate, applySeries ->
                viewModel.updateSchedule(entry.id, title, day, note, timeText, repeatRule, repeatInterval, repeatEndDate, applySeries)
                editingEntry = null
                toast = if (applySeries) "已保存整组重复" else "已保存"
            },
        )
    }

    deleteCandidate?.let { entry ->
        AlertDialog(
            onDismissRequest = { deleteCandidate = null },
            title = { Text("删除这条日程？") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space2)) {
                    Text(
                        "将删除「${entry.title}」以及它的本地日程记录。",
                        color = GoaldayDesign.adaptiveInkSecondary,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    if (entry.repeatGroupId.isNotBlank()) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(false to "仅本次", true to "整组重复").forEach { (value, label) ->
                                Text(
                                    label,
                                    color = if (deleteSeries == value) Color.White else GoaldayDesign.adaptiveInkSecondary,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier
                                        .background(
                                            if (deleteSeries == value) GoaldayDesign.PrimaryAction else GoaldayDesign.adaptiveSurfaceSoft,
                                            RoundedCornerShape(GoaldayDesign.RadiusPill),
                                        )
                                        .clickable { deleteSeries = value }
                                        .padding(horizontal = GoaldayDesign.Space2, vertical = GoaldayDesign.Space1),
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.removeSchedule(entry.id, deleteSeries)
                    deleteCandidate = null
                    toast = if (deleteSeries) "已删除整组重复" else "已删除任务"
                }) { Text("删除") }
            },
            dismissButton = {
                TextButton(onClick = { deleteCandidate = null }) { Text("取消") }
            },
        )
    }

    if (showImportPreview) {
        CalendarImportPreviewDialog(
            year = state.year,
            month = state.month,
            rangeMonths = importRangeMonths,
            events = importPreviewEvents,
            onDismiss = { showImportPreview = false },
            onConfirm = {
                val imported = viewModel.importSystemCalendarEvents(importPreviewEvents)
                showImportPreview = false
                toast = if (imported == 0) "没有可导入的新日历事件" else "已导入 $imported 条系统日历"
            },
        )
    }

    if (showImportRangeDialog) {
        CalendarImportRangeDialog(
            selectedMonths = importRangeMonths,
            onDismiss = { showImportRangeDialog = false },
            onConfirm = { months ->
                showImportRangeDialog = false
                prepareCalendarImportPreview(months)
            },
        )
    }

    if (showImportSourcePicker) {
        CalendarImportSourceDialog(
            events = importPreviewEvents,
            rangeMonths = importRangeMonths,
            selectedCalendars = selectedImportCalendars,
            onToggle = { calendar ->
                selectedImportCalendars = if (calendar in selectedImportCalendars) {
                    selectedImportCalendars - calendar
                } else {
                    selectedImportCalendars + calendar
                }
            },
            onSelectAll = {
                selectedImportCalendars = importPreviewEvents.map { importCalendarName(it) }.toSet()
            },
            onDismiss = { showImportSourcePicker = false },
            onConfirm = {
                val filtered = importPreviewEvents.filter { importCalendarName(it) in selectedImportCalendars }
                if (filtered.isEmpty()) {
                    toast = "请选择至少一个日历来源"
                } else {
                    importPreviewEvents = filtered
                    showImportSourcePicker = false
                    showImportPreview = true
                }
            },
        )
    }
}

@Composable
private fun CalendarHintPill(text: String) {
    Text(
        text,
        color = GoaldayDesign.adaptiveInkPrimary,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .fillMaxWidth()
            .background(GoaldayDesign.PinkSoft, RoundedCornerShape(GoaldayDesign.RadiusL))
            .border(0.7.dp, GoaldayDesign.Pink.copy(alpha = 0.16f), RoundedCornerShape(GoaldayDesign.RadiusL))
            .padding(horizontal = 11.dp, vertical = GoaldayDesign.Space2),
    )
}

@Composable
private fun CalendarThemeField(
    value: String,
    onValueChange: (String) -> Unit,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = MaterialTheme.typography.bodySmall.copy(color = GoaldayDesign.adaptiveInkPrimary),
        decorationBox = { inner ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GoaldayDesign.PinkSoft, RoundedCornerShape(GoaldayDesign.RadiusS))
                    .padding(horizontal = 9.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text("本周主题", color = GoaldayDesign.Pink, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                Box(Modifier.weight(1f)) {
                    if (value.isBlank()) {
                        Text("写下最重要的目标", color = GoaldayDesign.adaptiveInkMuted, style = MaterialTheme.typography.bodySmall)
                    }
                    inner()
                }
            }
        },
    )
}

@Composable
private fun CalendarHeroHeader(
    year: Int,
    month: Int,
    selectedDay: Int,
    todoCount: Int,
    doneCount: Int,
    onToday: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(GoaldayDesign.ShadowLarge, RoundedCornerShape(GoaldayDesign.RadiusXL), clip = false)
            .background(
                Brush.linearGradient(
                    listOf(GoaldayDesign.CalendarHeroStart, GoaldayDesign.CalendarHeroMid, GoaldayDesign.CalendarHeroEnd),
                    start = Offset.Zero,
                    end = Offset(780f, 440f),
                ),
                RoundedCornerShape(GoaldayDesign.RadiusXL),
            )
            .border(0.8.dp, GoaldayDesign.adaptiveSurface.copy(alpha = 0.21f), RoundedCornerShape(GoaldayDesign.RadiusXL))
            .padding(horizontal = 14.dp, vertical = 13.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("月度日程", color = GoaldayDesign.adaptiveInkSecondary, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                    Text("${year}年${month}月", color = GoaldayDesign.adaptiveInkPrimary, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                    Text("当前查看 ${selectedDay} 日 · 本地日历", color = GoaldayDesign.adaptiveInkSecondary, style = MaterialTheme.typography.labelSmall)
                }
                Text(
                    "今天",
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .background(GoaldayDesign.PrimaryAction, RoundedCornerShape(GoaldayDesign.RadiusPill))
                        .clickable(onClick = onToday)
                        .padding(horizontal = 13.dp, vertical = 7.dp),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalAlignment = Alignment.CenterVertically) {
                CalendarHeroMetric("待办", todoCount.toString(), GoaldayDesign.Pink, Modifier.weight(1f))
                CalendarHeroMetric("完成", doneCount.toString(), GoaldayDesign.Positive, Modifier.weight(1f))
                CalendarHeroMetric("进度", if (todoCount + doneCount == 0) "0%" else "${doneCount * 100 / (todoCount + doneCount)}%", GoaldayDesign.RouteOverview, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun CalendarHeroMetric(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(GoaldayDesign.adaptiveSurface.copy(alpha = 0.66f), RoundedCornerShape(GoaldayDesign.RadiusM))
            .border(0.6.dp, color.copy(alpha = 0.18f), RoundedCornerShape(GoaldayDesign.RadiusM))
            .padding(horizontal = 9.dp, vertical = 7.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(value, color = color, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 1)
        Text(label, color = GoaldayDesign.adaptiveInkMuted, style = MaterialTheme.typography.labelSmall, maxLines = 1)
    }
}

@Composable
private fun CalendarMonthControl(
    year: Int,
    month: Int,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onImportCalendar: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(5.dp, RoundedCornerShape(GoaldayDesign.RadiusXL), clip = false)
            .background(GoaldayDesign.adaptiveSurface, RoundedCornerShape(GoaldayDesign.RadiusXL))
            .border(0.8.dp, GoaldayDesign.BorderColor.copy(alpha = 0.09f), RoundedCornerShape(GoaldayDesign.RadiusXL))
            .padding(horizontal = 9.dp, vertical = GoaldayDesign.Space2),
        horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space2),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CalendarControlChip("上月", GoaldayDesign.RouteOverview, Modifier.weight(0.8f), onPreviousMonth)
        Column(modifier = Modifier.weight(1.2f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text("${year}年${month}月", color = GoaldayDesign.adaptiveInkPrimary, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, maxLines = 1)
            Text("系统日历可导入", color = GoaldayDesign.adaptiveInkMuted, style = MaterialTheme.typography.labelSmall, maxLines = 1)
        }
        CalendarControlChip("下月", GoaldayDesign.Pink, Modifier.weight(0.8f), onNextMonth)
        CalendarControlChip("导入", GoaldayDesign.PrimaryAction, Modifier.weight(0.8f), onImportCalendar)
    }
}

@Composable
private fun CalendarControlChip(
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Text(
        label,
        color = Color.White,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        maxLines = 1,
        modifier = modifier
            .background(color, RoundedCornerShape(GoaldayDesign.RadiusPill))
            .clickable(onClick = onClick)
            .padding(horizontal = GoaldayDesign.Space2, vertical = 6.dp),
    )
}

@Composable
private fun CalendarMonthGrid(
    year: Int,
    month: Int,
    selectedDay: Int,
    entries: List<ScheduleEntry>,
    onSelectDay: (Int) -> Unit,
) {
    val yearMonth = YearMonth.of(year, month)
    val firstOffset = yearMonth.atDay(1).dayOfWeek.value - 1
    val days = buildList<Int?> {
        repeat(firstOffset) { add(null) }
        (1..yearMonth.lengthOfMonth()).forEach { add(it) }
        while (size % 7 != 0) add(null)
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(GoaldayDesign.RadiusXL), clip = false)
            .background(GoaldayDesign.adaptiveSurface, RoundedCornerShape(GoaldayDesign.RadiusXL))
            .border(0.8.dp, GoaldayDesign.BorderColor.copy(alpha = 0.09f), RoundedCornerShape(GoaldayDesign.RadiusXL))
            .padding(horizontal = 9.dp, vertical = 9.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text("月份网格", color = GoaldayDesign.adaptiveInkSecondary, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                Text("点击日期切换今日执行", color = GoaldayDesign.adaptiveInkMuted, style = MaterialTheme.typography.labelSmall)
            }
            Text("${entries.size} 条", color = GoaldayDesign.adaptiveInkSecondary, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.fillMaxWidth()) {
            listOf("一", "二", "三", "四", "五", "六", "日").forEach { label ->
                Text(
                    label,
                    color = GoaldayDesign.adaptiveInkMuted,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                )
            }
        }
        days.chunked(7).forEach { week ->
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.fillMaxWidth()) {
                week.forEach { day ->
                    if (day == null) {
                        Spacer(Modifier.weight(1f))
                    } else {
                        val dayEntries = entries.filter { it.day == day }
                        val todo = dayEntries.count { !it.completed }
                        val done = dayEntries.count { it.completed }
                        CalendarMonthDayCell(
                            day = day,
                            selected = day == selectedDay,
                            todoCount = todo,
                            doneCount = done,
                            modifier = Modifier.weight(1f),
                            onClick = { onSelectDay(day) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarMonthDayCell(
    day: Int,
    selected: Boolean,
    todoCount: Int,
    doneCount: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .height(48.dp)
            .shadow(
                if (selected) 7.dp else if (todoCount + doneCount > 0) 3.dp else 0.dp,
                RoundedCornerShape(GoaldayDesign.RadiusM),
                clip = false,
            )
            .background(
                if (selected) {
                    GoaldayDesign.PrimaryAction
                } else if (todoCount + doneCount > 0) {
                    GoaldayDesign.PinkSoft
                } else {
                    GoaldayDesign.adaptiveSurfaceSoft
                },
                RoundedCornerShape(GoaldayDesign.RadiusM),
            )
            .border(
                0.7.dp,
                if (selected) GoaldayDesign.PrimaryAction else if (todoCount + doneCount > 0) GoaldayDesign.Pink.copy(alpha = 0.22f) else Color.Transparent,
                RoundedCornerShape(GoaldayDesign.RadiusM),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = GoaldayDesign.Space1, vertical = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            day.toString(),
            color = if (selected) Color.White else GoaldayDesign.adaptiveInkPrimary,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.CenterVertically) {
            if (todoCount > 0) CalendarDayDot(GoaldayDesign.Pink, todoCount, selected)
            if (doneCount > 0) CalendarDayDot(GoaldayDesign.Positive, doneCount, selected)
        }
    }
}

@Composable
private fun CalendarDayDot(
    color: Color,
    count: Int,
    selected: Boolean,
) {
    Text(
        count.coerceAtMost(9).toString(),
        color = if (selected) color else Color.White,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        maxLines = 1,
        modifier = Modifier
            .background(if (selected) GoaldayDesign.adaptiveWhiteOverlay else color, RoundedCornerShape(GoaldayDesign.RadiusPill))
            .padding(horizontal = GoaldayDesign.Space1 + 1.dp, vertical = 1.dp),
    )
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
            .shadow(7.dp, RoundedCornerShape(GoaldayDesign.RadiusM), clip = false)
            .background(GoaldayDesign.adaptiveSurface, RoundedCornerShape(GoaldayDesign.RadiusM))
            .border(0.8.dp, GoaldayDesign.BorderColor.copy(alpha = 0.09f), RoundedCornerShape(GoaldayDesign.RadiusM))
            .padding(horizontal = GoaldayDesign.Space3, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(title, color = GoaldayDesign.adaptiveInkPrimary, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(
                subtitle,
                color = GoaldayDesign.adaptiveInkSecondary,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier
                    .background(GoaldayDesign.BorderColor.copy(alpha = 0.06f), RoundedCornerShape(GoaldayDesign.RadiusPill))
                    .padding(horizontal = GoaldayDesign.Space2, vertical = 3.dp),
            )
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
                if (hover) GoaldayDesign.Pink.copy(alpha = 0.24f) else if (dropReady && assigned == null) GoaldayDesign.Pink.copy(alpha = 0.13f) else GoaldayDesign.adaptiveSurface,
                RoundedCornerShape(GoaldayDesign.RadiusS),
            )
            .border(if (hover) 1.dp else 0.6.dp, if (hover) GoaldayDesign.Pink else GoaldayDesign.BorderColor.copy(alpha = 0.09f), RoundedCornerShape(GoaldayDesign.RadiusS))
            .padding(horizontal = GoaldayDesign.Space2, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space2),
    ) {
        Text(
            slotShort,
            color = Color.White,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .background(if (assigned != null) GoaldayDesign.PrimaryAction else GoaldayDesign.RouteDiary, RoundedCornerShape(GoaldayDesign.RadiusPill))
                .padding(horizontal = GoaldayDesign.Space2, vertical = GoaldayDesign.Space1),
        )
        if (assigned != null) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(assigned.title, color = GoaldayDesign.adaptiveInkPrimary, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, maxLines = 1)
                scheduleMetaText(assigned).takeIf { it.isNotBlank() }?.let { meta ->
                    Text(meta, color = GoaldayDesign.adaptiveInkMuted, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                }
            }
        } else {
            Text(
                if (dropReady) "点此放入$slotKey" else "空时段",
                color = GoaldayDesign.RouteDiary,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
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
            .background(GoaldayDesign.RouteDiary, RoundedCornerShape(GoaldayDesign.RadiusPill))
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

private fun repeatRuleLabel(rule: String, interval: Int, endDate: String): String {
    val safeInterval = interval.coerceAtLeast(1)
    val base = when (rule) {
        "daily" -> if (safeInterval == 1) "每天" else "每${safeInterval}天"
        "weekly" -> if (safeInterval == 1) "每周" else "每${safeInterval}周"
        "monthly" -> if (safeInterval == 1) "每月" else "每${safeInterval}月"
        else -> ""
    }
    val shortEndDate = endDate.takeIf { it.length == 10 }?.let { "${it.substring(5, 7)}/${it.substring(8, 10)}" } ?: endDate
    return if (base.isBlank() || shortEndDate.isBlank()) base else "$base 至$shortEndDate"
}

private fun scheduleMetaText(entry: ScheduleEntry): String =
    listOfNotNull(
        entry.timeText.takeIf { it.isNotBlank() },
        parseTimeSlot(entry.note),
        repeatRuleLabel(entry.repeatRule, entry.repeatInterval, entry.repeatEndDate).takeIf { it.isNotBlank() },
        entry.note
            .replace(Regex("时段:(上午|下午|晚上)"), "")
            .trim()
            .takeIf { it.isNotBlank() },
    ).joinToString(" · ")

@Composable
private fun CalendarImportRangeDialog(
    selectedMonths: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
) {
    var draftMonths by remember(selectedMonths) { mutableIntStateOf(selectedMonths.coerceIn(1, 6)) }
    val options = listOf(1 to "本月", 3 to "未来3个月", 6 to "未来6个月")
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("导入范围") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space2)) {
                options.forEach { (months, label) ->
                    Text(
                        label,
                        color = if (draftMonths == months) Color.White else GoaldayDesign.adaptiveInkSecondary,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (draftMonths == months) GoaldayDesign.PrimaryAction else GoaldayDesign.adaptiveSurfaceSoft,
                                RoundedCornerShape(GoaldayDesign.RadiusS),
                            )
                            .clickable { draftMonths = months }
                            .padding(horizontal = 10.dp, vertical = GoaldayDesign.Space2),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(draftMonths) }) { Text("读取") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        },
    )
}

@Composable
private fun CalendarImportSourceDialog(
    events: List<CalendarImportCandidate>,
    rangeMonths: Int,
    selectedCalendars: Set<String>,
    onToggle: (String) -> Unit,
    onSelectAll: () -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    val sourceCounts = events
        .groupingBy { importCalendarName(it) }
        .eachCount()
        .toList()
        .sortedBy { it.first }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择日历来源") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space2),
            ) {
                val rangeText = if (rangeMonths <= 1) "本月" else "未来${rangeMonths}个月"
                Text("$rangeText 找到 ${events.size} 条系统日历事件", color = GoaldayDesign.adaptiveInkSecondary, style = MaterialTheme.typography.labelSmall)
                sourceCounts.forEach { (source, count) ->
                    val selected = source in selectedCalendars
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (selected) GoaldayDesign.PinkSoft else GoaldayDesign.adaptiveSurfaceSoft,
                                RoundedCornerShape(GoaldayDesign.RadiusS),
                            )
                            .border(
                                if (selected) 1.dp else 0.5.dp,
                                if (selected) GoaldayDesign.PrimaryAction else GoaldayDesign.adaptiveDivider,
                                RoundedCornerShape(GoaldayDesign.RadiusS),
                            )
                            .clickable { onToggle(source) }
                            .padding(horizontal = 9.dp, vertical = 7.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(source, color = GoaldayDesign.adaptiveInkPrimary, style = MaterialTheme.typography.bodySmall)
                        Text("${count}条", color = if (selected) GoaldayDesign.PrimaryAction else GoaldayDesign.adaptiveInkMuted, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("预览") }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space1)) {
                TextButton(onClick = onSelectAll) { Text("全选") }
                TextButton(onClick = onDismiss) { Text("取消") }
            }
        },
    )
}

@Composable
private fun CalendarImportPreviewDialog(
    year: Int,
    month: Int,
    rangeMonths: Int,
    events: List<CalendarImportCandidate>,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("导入预览") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                val rangeText = if (rangeMonths <= 1) "${year}年${month}月" else "${year}年${month}月起 · ${rangeMonths}个月"
                Text("$rangeText · ${events.size} 条系统日历", color = GoaldayDesign.adaptiveInkSecondary, style = MaterialTheme.typography.labelSmall)
                events.take(12).forEach { event ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(GoaldayDesign.adaptiveSurfaceSoft, RoundedCornerShape(GoaldayDesign.RadiusS))
                            .padding(horizontal = GoaldayDesign.Space2, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        val time = event.timeText.takeIf { it.isNotBlank() }?.let { " $it" }.orEmpty()
                        val dateText = if (rangeMonths <= 1) {
                            "${event.day}日"
                        } else {
                            "${event.year}年${event.month}月${event.day}日"
                        }
                        Text("$dateText$time · ${event.title}", color = GoaldayDesign.adaptiveInkPrimary, style = MaterialTheme.typography.bodySmall)
                        if (event.note.isNotBlank()) {
                            Text(event.note, color = GoaldayDesign.adaptiveInkMuted, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                        }
                    }
                }
                if (events.size > 12) {
                    Text("还有 ${events.size - 12} 条将在确认后一起导入", color = GoaldayDesign.adaptiveInkMuted, style = MaterialTheme.typography.labelSmall)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("确认导入") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        },
    )
}

private fun importCalendarName(event: CalendarImportCandidate): String =
    event.calendarName.ifBlank { "默认日历" }

private fun readSystemCalendarEvents(
    context: Context,
    year: Int,
    month: Int,
    rangeMonths: Int,
): List<CalendarImportCandidate> {
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
        return emptyList()
    }
    val zone = ZoneId.systemDefault()
    val targetMonth = YearMonth.of(year, month)
    val startMillis = targetMonth.atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
    val safeRangeMonths = rangeMonths.coerceIn(1, 6)
    val endMillis = targetMonth.plusMonths(safeRangeMonths.toLong()).atDay(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1L
    val uriBuilder = CalendarContract.Instances.CONTENT_URI.buildUpon()
    ContentUris.appendId(uriBuilder, startMillis)
    ContentUris.appendId(uriBuilder, endMillis)
    val projection = arrayOf(
        CalendarContract.Instances.TITLE,
        CalendarContract.Instances.BEGIN,
        CalendarContract.Instances.DESCRIPTION,
        CalendarContract.Instances.ALL_DAY,
        CalendarContract.Instances.CALENDAR_DISPLAY_NAME,
    )
    return context.contentResolver.query(
        uriBuilder.build(),
        projection,
        null,
        null,
        "${CalendarContract.Instances.BEGIN} ASC",
    )?.use { cursor ->
        buildList {
            val titleIndex = cursor.getColumnIndexOrThrow(CalendarContract.Instances.TITLE)
            val beginIndex = cursor.getColumnIndexOrThrow(CalendarContract.Instances.BEGIN)
            val descriptionIndex = cursor.getColumnIndexOrThrow(CalendarContract.Instances.DESCRIPTION)
            val allDayIndex = cursor.getColumnIndexOrThrow(CalendarContract.Instances.ALL_DAY)
            val calendarNameIndex = cursor.getColumnIndexOrThrow(CalendarContract.Instances.CALENDAR_DISPLAY_NAME)
            while (cursor.moveToNext()) {
                val begin = cursor.getLong(beginIndex)
                val dateTime = Instant.ofEpochMilli(begin).atZone(zone)
                val date = dateTime.toLocalDate()
                val eventMonth = YearMonth.of(date.year, date.monthValue)
                if (eventMonth.isBefore(targetMonth) || !eventMonth.isBefore(targetMonth.plusMonths(safeRangeMonths.toLong()))) continue
                val title = cursor.getString(titleIndex)?.trim().orEmpty().ifBlank { "无标题日程" }
                val description = cursor.getString(descriptionIndex)?.trim().orEmpty()
                val calendarName = cursor.getString(calendarNameIndex)?.trim().orEmpty()
                val allDay = cursor.getInt(allDayIndex) == 1
                val timeText = if (allDay) "" else "%02d:%02d".format(dateTime.hour, dateTime.minute)
                add(
                    CalendarImportCandidate(
                        title = title,
                        year = date.year,
                        month = date.monthValue,
                        day = date.dayOfMonth,
                        note = listOf("系统日历", calendarName, description).filter { it.isNotBlank() }.joinToString(" · "),
                        timeText = timeText,
                        calendarName = calendarName,
                    ),
                )
            }
        }
    } ?: emptyList()
}

@Composable
private fun ScheduleDialog(
    title: String,
    maxDay: Int,
    initialTitle: String,
    initialDay: Int,
    initialNote: String,
    initialTimeText: String,
    initialRepeatRule: String,
    initialRepeatInterval: Int,
    initialRepeatEndDate: String,
    allowSeriesEdit: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (title: String, day: Int, note: String, timeText: String, repeatRule: String, repeatInterval: Int, repeatEndDate: String, applySeries: Boolean) -> Unit,
) {
    var draftTitle by remember(initialTitle) { mutableStateOf(initialTitle) }
    var draftDay by remember(initialDay) { mutableStateOf(initialDay.toString()) }
    var draftNote by remember(initialNote) { mutableStateOf(initialNote) }
    var draftTime by remember(initialTimeText) { mutableStateOf(initialTimeText) }
    var draftRepeatRule by remember(initialRepeatRule) { mutableStateOf(initialRepeatRule) }
    var draftRepeatInterval by remember(initialRepeatInterval) { mutableStateOf(initialRepeatInterval.coerceAtLeast(1).toString()) }
    var draftRepeatEndDate by remember(initialRepeatEndDate) { mutableStateOf(initialRepeatEndDate) }
    var applySeries by remember(allowSeriesEdit, initialTitle) { mutableStateOf(false) }
    val repeatOptions = listOf("" to "不重复", "daily" to "每天", "weekly" to "每周", "monthly" to "每月")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space2)) {
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
                    value = draftTime,
                    onValueChange = { input -> draftTime = input.filter { it.isDigit() || it == ':' }.take(5) },
                    label = { Text("时间，例如 09:30") },
                    singleLine = true,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    repeatOptions.forEach { (rule, label) ->
                        Text(
                            label,
                            color = if (draftRepeatRule == rule) Color.White else GoaldayDesign.adaptiveInkSecondary,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier
                                .background(
                                    if (draftRepeatRule == rule) GoaldayDesign.PrimaryAction else GoaldayDesign.adaptiveSurfaceSoft,
                                    RoundedCornerShape(GoaldayDesign.RadiusPill),
                                )
                                .clickable { draftRepeatRule = rule }
                                .padding(horizontal = GoaldayDesign.Space2, vertical = GoaldayDesign.Space1),
                        )
                    }
                }
                if (draftRepeatRule.isNotBlank()) {
                    OutlinedTextField(
                        value = draftRepeatInterval,
                        onValueChange = { input -> draftRepeatInterval = input.filter { it.isDigit() }.take(2) },
                        label = {
                            Text(
                                when (draftRepeatRule) {
                                    "daily" -> "间隔天数"
                                    "weekly" -> "间隔周数"
                                    "monthly" -> "间隔月数"
                                    else -> "重复间隔"
                                },
                            )
                        },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = draftRepeatEndDate,
                        onValueChange = { input -> draftRepeatEndDate = input.filter { it.isDigit() || it == '-' }.take(10) },
                        label = { Text("结束日期，可空 yyyy-MM-dd") },
                        singleLine = true,
                    )
                }
                if (allowSeriesEdit) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(false to "仅本次", true to "整组").forEach { (value, label) ->
                            Text(
                                label,
                                color = if (applySeries == value) Color.White else GoaldayDesign.adaptiveInkSecondary,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier
                                    .background(
                                        if (applySeries == value) GoaldayDesign.PrimaryAction else GoaldayDesign.adaptiveSurfaceSoft,
                                        RoundedCornerShape(GoaldayDesign.RadiusPill),
                                    )
                                    .clickable { applySeries = value }
                                    .padding(horizontal = GoaldayDesign.Space2, vertical = GoaldayDesign.Space1),
                            )
                        }
                    }
                }
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
                val normalizedTime = draftTime.trim()
                val interval = if (draftRepeatRule.isBlank()) 1 else draftRepeatInterval.toIntOrNull()?.coerceIn(1, 30) ?: 1
                val endDate = if (draftRepeatRule.isBlank()) "" else draftRepeatEndDate.trim()
                if (t.isNotBlank()) onConfirm(t, d, draftNote.trim(), normalizedTime, draftRepeatRule, interval, endDate, applySeries)
            }) { Text("保存") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        },
    )
}
