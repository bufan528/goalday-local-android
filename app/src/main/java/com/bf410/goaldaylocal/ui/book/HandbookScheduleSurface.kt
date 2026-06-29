package com.bf410.goaldaylocal.ui.book

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.bf410.goaldaylocal.data.BookPage
import com.bf410.goaldaylocal.data.ScheduleEntry
import com.bf410.goaldaylocal.ui.replica.GoaldayDesign
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.delay

private enum class ScheduleBoardMode(val label: String) {
    SPREAD("手账"),
    MONTH("整月"),
}


@Composable
internal fun HandbookReplicaPage(
    modifier: Modifier,
    page: BookPage,
    pageIndex: Int,
    pageCount: Int,
    todayPlanItems: List<String>,
    todayCompletedItems: List<String>,
    schedulePreviewEntries: List<ScheduleEntry>,
    weeklyTheme: String,
    onAddPoolItem: (String) -> Unit,
    onRemovePoolItem: (String) -> Unit,
    onAddSchedule: (String, Int, Int) -> Unit,
    onWeeklyThemeChange: (String) -> Unit,
    onUpdateScheduleTitle: (String, String) -> Unit,
    onMoveScheduleDay: (String, Int, Int) -> Unit,
    onToggleScheduleCompleted: (String) -> Unit,
    turnProgress: Float,
    turnDirection: TurnDirection?,
) {
    val eased = turnProgress * turnProgress * (3f - 2f * turnProgress)
    val contentShift = when (turnDirection) {
        TurnDirection.NEXT -> -(eased * 8f)
        TurnDirection.PREVIOUS -> eased * 8f
        null -> 0f
    }
    val alpha = (1f - eased * 0.08f).coerceIn(0.92f, 1f)
    var monthOffset by remember(page.title) { mutableStateOf(0) }
    val defaultScheduleModel = buildScheduleHandbookModel(
        page = page,
        scheduleEntries = schedulePreviewEntries,
        todayPlanItems = todayPlanItems,
        todayCompletedItems = todayCompletedItems,
        requestedWindowStart = null,
        monthOffset = monthOffset,
    )
    var windowStart by remember(page.title, defaultScheduleModel.year, defaultScheduleModel.month) {
        mutableStateOf(defaultScheduleModel.windowStart)
    }
    LaunchedEffect(page.title, defaultScheduleModel.year, defaultScheduleModel.month, defaultScheduleModel.windowStart) {
        windowStart = defaultScheduleModel.windowStart
    }
    val scheduleModel = buildScheduleHandbookModel(
        page = page,
        scheduleEntries = schedulePreviewEntries,
        todayPlanItems = todayPlanItems,
        todayCompletedItems = todayCompletedItems,
        requestedWindowStart = windowStart,
        monthOffset = monthOffset,
    )
    val anchorYear = scheduleModel.year
    val anchorMonth = scheduleModel.month
    val monthLength = scheduleModel.monthLength
    val start = scheduleModel.windowStart
    val maxStart = scheduleModel.maxWindowStart
    val leftBlocks = scheduleModel.dayBlocks
    val rightBlocks = scheduleModel.dayBlocks
    val visibleDays = scheduleModel.visibleDays
    val visibleRangeLabel = scheduleModel.visibleRangeLabel
    val fallbackLeftDone = scheduleModel.fallbackDone
    val fallbackRightTodo = scheduleModel.fallbackTodo
    val visiblePoolItems = scheduleModel.visiblePoolItems
    val sorted = scheduleModel.sortedEntries
    var draftText by remember(page.title) { mutableStateOf("") }
    var draftDay by remember(page.title) { mutableStateOf(rightBlocks.firstOrNull()?.day ?: 1) }
    LaunchedEffect(visibleDays) {
        if (draftDay !in visibleDays) {
            draftDay = visibleDays.firstOrNull() ?: 1
        }
    }
    val selectedDraftDay = if (draftDay in visibleDays) draftDay else visibleDays.firstOrNull() ?: 1
    var editingId by remember(pageIndex) { mutableStateOf<String?>(null) }
    var editingText by remember(pageIndex) { mutableStateOf("") }
    var saveHint by remember(pageIndex) { mutableStateOf("") }
    var boardMode by remember(pageIndex) { mutableStateOf(ScheduleBoardMode.SPREAD) }
    var spreadOrigin by remember(pageIndex) { mutableStateOf(Offset.Zero) }
    val todoDropBounds = remember(pageIndex) { mutableMapOf<Int, Rect>() }
    val doneDropBounds = remember(pageIndex) { mutableMapOf<Int, Rect>() }
    var draggingPoolItem by remember(pageIndex) { mutableStateOf<String?>(null) }
    var draggingTodoEntry by remember(pageIndex) { mutableStateOf<ScheduleEntry?>(null) }
    var dragPosition by remember(pageIndex) { mutableStateOf(Offset.Zero) }
    var activePoolDropDay by remember(pageIndex) { mutableStateOf<Int?>(null) }
    var activeDoneDropDay by remember(pageIndex) { mutableStateOf<Int?>(null) }
    val context = LocalContext.current
    var exportHint by remember(pageIndex) { mutableStateOf("") }
    var longImagePreview by remember(pageIndex) { mutableStateOf<LongImagePreview?>(null) }
    fun clearPoolDrag() {
        draggingPoolItem = null
        activePoolDropDay = null
        dragPosition = Offset.Zero
    }
    fun clearTodoDrag() {
        draggingTodoEntry = null
        activePoolDropDay = null
        activeDoneDropDay = null
        dragPosition = Offset.Zero
    }
    LaunchedEffect(saveHint) {
        if (saveHint.isBlank()) return@LaunchedEffect
        delay(1200)
        saveHint = ""
    }
    LaunchedEffect(exportHint) {
        if (exportHint.isBlank()) return@LaunchedEffect
        delay(1400)
        exportHint = ""
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(GoaldayDesign.RadiusL))
            .graphicsLayer {
                translationX = contentShift
                this.alpha = alpha
            }
            .onGloballyPositioned { coordinates ->
                spreadOrigin = coordinates.boundsInRoot().topLeft
            }
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        // 删除原左右双纸张 Box（色值仅相差 0x10 alpha，视觉无意义却叠了一层）
        // 纸张背景由外层 PageSurface 的 PaperGradient 统一提供
        HandbookPaperRuling()
        HandbookMonthHeader(
            year = anchorYear,
            month = anchorMonth,
            pageIndex = pageIndex,
            pageCount = pageCount,
            weeklyTheme = weeklyTheme,
            onWeeklyThemeChange = onWeeklyThemeChange,
            rangeLabel = "${anchorMonth}月",
            visibleDays = visibleDays,
            canShiftPrevious = true,
            canShiftNext = true,
            onPreviousRange = { monthOffset--; windowStart = 0 },
            onNextRange = { monthOffset++; windowStart = 0 },
            onSelectMonthDay = { day ->
                windowStart = (day - 1).coerceIn(0, maxStart)
                draftDay = day
            },
        )
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 33.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ScheduleBoardMode.entries.forEach { mode ->
                Text(
                    mode.label,
                    color = if (boardMode == mode) Color.White else GoaldayDesign.InkMuted,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                        .clip(RoundedCornerShape(99.dp))
                        .background(if (boardMode == mode) GoaldayDesign.Pink else GoaldayDesign.Pink.copy(alpha = 0.18f))
                        .clickable { boardMode = mode }
                        .padding(horizontal = 7.dp, vertical = 3.dp),
                )
            }
            Text(
                "预览",
                color = GoaldayDesign.InkMuted,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier
                    .clip(RoundedCornerShape(99.dp))
                    .background(GoaldayDesign.Pink.copy(alpha = 0.18f))
                    .clickable {
                        longImagePreview = LongImagePreview(
                            title = "Goalday 日程手账",
                            subtitle = "$anchorYear 年 $anchorMonth 月 · $visibleRangeLabel",
                            filePrefix = "Goalday_schedule",
                            bitmap = renderHandbookScheduleLongImage(anchorYear, anchorMonth, visibleDays, sorted, weeklyTheme),
                        )
                    }
                    .padding(horizontal = 7.dp, vertical = 3.dp),
            )
            Text(
                "快存",
                color = GoaldayDesign.InkMuted,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier
                    .clip(RoundedCornerShape(99.dp))
                    .background(GoaldayDesign.Pink.copy(alpha = 0.18f))
                    .clickable {
                        val uri = exportHandbookScheduleLongImage(context, anchorYear, anchorMonth, visibleDays, sorted, weeklyTheme)
                        exportHint = if (uri != null) "已导出" else "导出失败"
                    }
                    .padding(horizontal = 7.dp, vertical = 3.dp),
            )
            if (exportHint.isNotBlank()) {
                Text(exportHint, style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.InkMuted)
            }
        }
        if (boardMode == ScheduleBoardMode.MONTH) {
            HandbookMonthBoard(
                year = anchorYear,
                month = anchorMonth,
                monthLength = monthLength,
                entries = sorted,
                selectedDays = visibleDays,
                onSelectDay = { day ->
                    windowStart = (day - 1).coerceIn(0, maxStart)
                    draftDay = day
                    boardMode = ScheduleBoardMode.SPREAD
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 14.dp, end = 14.dp, top = 92.dp, bottom = 32.dp),
            )
        } else {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 14.dp, end = 14.dp, top = 92.dp, bottom = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    SectionStamp(
                        label = "DONE",
                        color = GoaldayDesign.Positive,
                    )
                    Text(
                        "已执行",
                        style = MaterialTheme.typography.labelMedium,
                        color = GoaldayDesign.InkMuted,
                    )
                }
                leftBlocks.forEachIndexed { idx, block ->
                    DaySpreadSection(
                        day = block.day,
                        doneEntries = block.done,
                        fallbackDone = if (idx == 0 && block.done.isEmpty()) fallbackLeftDone else emptyList(),
                        todoCount = block.todo.size,
                        accent = GoaldayDesign.Positive,
                        activeDrop = activeDoneDropDay == block.day,
                        onBounds = { rect -> doneDropBounds[block.day] = rect },
                        onToggleCompleted = { entry ->
                            if (!entry.id.startsWith("fallback_")) {
                                onToggleScheduleCompleted(entry.id)
                            }
                        },
                    )
                }
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    SectionStamp(
                        label = "TODO",
                        color = GoaldayDesign.Pink,
                    )
                    Text(
                        "待计划",
                        style = MaterialTheme.typography.labelMedium,
                        color = GoaldayDesign.InkMuted,
                    )
                }
                HandbookQuickAddRow(
                    value = draftText,
                    onValueChange = { draftText = it },
                    days = rightBlocks.map { it.day },
                    selectedDay = selectedDraftDay,
                    onSelectDay = { draftDay = it },
                    poolItems = visiblePoolItems,
                    onAddPoolItem = { text ->
                        onAddPoolItem(text)
                        draftText = ""
                        saveHint = "已加入待安排"
                    },
                    onRemovePoolItem = { text ->
                        onRemovePoolItem(text)
                        saveHint = "已从待安排移除"
                    },
                    onPickPoolItem = { text ->
                        onAddSchedule(text, anchorMonth, selectedDraftDay)
                        saveHint = "已放入${selectedDraftDay}日"
                    },
                    onPoolDragStart = { text, position ->
                        draggingPoolItem = text
                        dragPosition = position
                        activePoolDropDay = todoDropBounds.entries.firstOrNull { it.value.contains(position) }?.key
                    },
                    onPoolDrag = { delta ->
                        dragPosition += delta
                        activePoolDropDay = todoDropBounds.entries.firstOrNull { it.value.contains(dragPosition) }?.key
                    },
                    onPoolDragEnd = {
                        val text = draggingPoolItem
                        val targetDay = activePoolDropDay
                        if (text != null && targetDay != null) {
                            onAddSchedule(text, anchorMonth, targetDay)
                            draftDay = targetDay
                            saveHint = "已拖入${targetDay}日"
                        } else if (text != null) {
                            saveHint = "未命中日期"
                        }
                        clearPoolDrag()
                    },
                    onPoolDragCancel = {
                        saveHint = "已取消拖放"
                        clearPoolDrag()
                    },
                    onDone = {
                        val text = draftText.trim()
                        if (text.isNotBlank()) {
                            onAddSchedule(text, anchorMonth, selectedDraftDay)
                            draftText = ""
                            saveHint = "已加入${selectedDraftDay}日"
                        }
                    },
                )
                rightBlocks.forEachIndexed { idx, block ->
                    DaySpreadEditableSection(
                        day = block.day,
                        visibleDays = visibleDays,
                        entries = if (idx == 0 && block.todo.isEmpty()) {
                            fallbackRightTodo.mapIndexed { i, text ->
                                ScheduleEntry(id = "fallback_${block.day}_$i", title = text, day = block.day, month = anchorMonth, year = anchorYear, completed = false, note = "")
                            }
                        } else {
                            block.todo
                        },
                        editingId = editingId,
                        editingText = editingText,
                        // 修复：拖动 entry 时排除自身所在槽位高亮，避免反馈自相矛盾
                        activeDrop = activePoolDropDay == block.day && draggingTodoEntry?.day != block.day,
                        onBounds = { rect -> todoDropBounds[block.day] = rect },
                        onStartEdit = { entry ->
                            if (!entry.id.startsWith("fallback_")) {
                                editingId = entry.id
                                editingText = entry.title
                            } else {
                                onAddSchedule(entry.title, anchorMonth, entry.day)
                                saveHint = "已放入${entry.day}日"
                            }
                        },
                        onTextChange = { editingText = it },
                        onCommit = { entry ->
                            if (!entry.id.startsWith("fallback_")) {
                                onUpdateScheduleTitle(entry.id, editingText)
                                editingId = null
                                saveHint = "已保存"
                            }
                        },
                        onToggleCompleted = { entry ->
                            if (!entry.id.startsWith("fallback_")) {
                                onToggleScheduleCompleted(entry.id)
                            } else {
                                onAddSchedule(entry.title, anchorMonth, entry.day)
                                saveHint = "已放入${entry.day}日"
                            }
                        },
                        onMoveEntryToDay = { entry, targetDay ->
                            if (!entry.id.startsWith("fallback_")) {
                                onMoveScheduleDay(entry.id, anchorMonth, targetDay)
                                draftDay = targetDay
                                saveHint = "已移动到${targetDay}日"
                            }
                        },
                        onEntryDragStart = { entry, position ->
                            if (!entry.id.startsWith("fallback_")) {
                                draggingTodoEntry = entry
                                dragPosition = position
                                activePoolDropDay = todoDropBounds.entries.firstOrNull { it.value.contains(position) }?.key
                                activeDoneDropDay = doneDropBounds.entries.firstOrNull { it.value.contains(position) }?.key
                            }
                        },
                        onEntryDrag = { delta ->
                            dragPosition += delta
                            activePoolDropDay = todoDropBounds.entries.firstOrNull { it.value.contains(dragPosition) }?.key
                            activeDoneDropDay = doneDropBounds.entries.firstOrNull { it.value.contains(dragPosition) }?.key
                        },
                        onEntryDragEnd = {
                            val entry = draggingTodoEntry
                            val doneTargetDay = activeDoneDropDay
                            val todoTargetDay = activePoolDropDay
                            when {
                                entry != null && doneTargetDay == entry.day -> {
                                    onToggleScheduleCompleted(entry.id)
                                    saveHint = "已放入${doneTargetDay}日 done"
                                }
                                entry != null && todoTargetDay != null && todoTargetDay != entry.day -> {
                                    onMoveScheduleDay(entry.id, anchorMonth, todoTargetDay)
                                    draftDay = todoTargetDay
                                    saveHint = "已拖到${todoTargetDay}日"
                                }
                                // 修复：拖回自己原位时给明确反馈，原代码静默无反馈
                                entry != null && todoTargetDay == entry.day -> saveHint = "未移动"
                                entry != null && doneTargetDay != null -> saveHint = "请拖到同日期 done"
                                entry != null -> saveHint = "未命中日期或 done"
                            }
                            clearTodoDrag()
                        },
                        onEntryDragCancel = {
                            saveHint = "已取消拖放"
                            clearTodoDrag()
                        },
                    )
                }
            }
            }
        }

        (draggingPoolItem ?: draggingTodoEntry?.title)?.let { text ->
            val localX = (dragPosition.x - spreadOrigin.x).toInt()
            val localY = (dragPosition.y - spreadOrigin.y).toInt()
            Text(
                text,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White,
                maxLines = 1,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset { IntOffset(localX, localY) }
                    .clip(RoundedCornerShape(GoaldayDesign.RadiusS))
                    .background(GoaldayDesign.Pink)
                    .border(0.8.dp, Color.White, RoundedCornerShape(GoaldayDesign.RadiusS))
                    .padding(horizontal = 7.dp, vertical = 4.dp),
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
                .background(Color(0x11A68B71))
                .border(0.5.dp, Color(0x1EA68B71), RoundedCornerShape(GoaldayDesign.RadiusPill))
                .padding(horizontal = 9.dp, vertical = 1.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = page.title.ifBlank { "手账" },
                style = MaterialTheme.typography.labelSmall,
                color = GoaldayDesign.InkSecondary,
                modifier = Modifier.alpha(0.86f),
                textAlign = TextAlign.Center,
            )
        }
        if (saveHint.isNotBlank()) {
            Text(
                saveHint,
                style = MaterialTheme.typography.labelSmall,
                color = GoaldayDesign.InkSecondary,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 4.dp, bottom = 2.dp),
            )
        }
    }
    longImagePreview?.let { preview ->
        LongImagePreviewDialog(
            preview = preview,
            onDismiss = { longImagePreview = null },
        )
    }
}

@Composable
private fun HandbookMonthBoard(
    year: Int,
    month: Int,
    monthLength: Int,
    entries: List<ScheduleEntry>,
    selectedDays: List<Int>,
    onSelectDay: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val today = LocalDate.now()
    // 修复：按 1 号是星期几补前置空格，让月历对齐星期（原 chunked(7) 直接按自然数排列会误导用户）
    val firstWeekday = YearMonth.of(year, month).atDay(1).dayOfWeek.value // 1=周一 ... 7=周日
    val leadingBlanks = firstWeekday - 1
    val allSlots: List<Int?> = List(leadingBlanks) { null } + (1..monthLength).toList()
    val weeks = allSlots.chunked(7)
    val weekdayLabels = listOf("一", "二", "三", "四", "五", "六", "日")
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(GoaldayDesign.RadiusL))
            .background(GoaldayDesign.Surface.copy(alpha = 0.72f))
            .border(GoaldayDesign.Hairline, GoaldayDesign.BorderColor.copy(alpha = 0.18f), RoundedCornerShape(GoaldayDesign.RadiusL))
            .padding(GoaldayDesign.Space2),
        verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space1 + 2.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("MONTHLY", style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.Pink, fontWeight = FontWeight.SemiBold)
            Text("${year}年${month}月 · 点击日期展开到手账页", style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.InkMuted)
        }
        // 星期表头行
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            weekdayLabels.forEach { label ->
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(label, style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.InkMuted, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        weeks.forEach { week ->
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.weight(1f)) {
                week.forEach { day ->
                    if (day == null) {
                        Spacer(modifier = Modifier.weight(1f))
                    } else {
                        val dayEntries = entries.filter { it.day == day }
                        val todoCount = dayEntries.count { !it.completed }
                        val doneCount = dayEntries.count { it.completed }
                        val title = dayEntries.firstOrNull { !it.completed }?.title
                            ?: dayEntries.firstOrNull { it.completed }?.title
                            ?: "空白"
                        val isVisible = day in selectedDays
                        val isToday = today.year == year && today.monthValue == month && today.dayOfMonth == day
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(GoaldayDesign.RadiusS))
                                .background(
                                    when {
                                        isVisible -> GoaldayDesign.Pink.copy(alpha = 0.18f)
                                        isToday -> GoaldayDesign.PaperAged.copy(alpha = 0.30f)
                                        else -> GoaldayDesign.Surface
                                    },
                                )
                                .border(
                                    width = if (isVisible || isToday) 1.dp else GoaldayDesign.Hairline,
                                    color = when {
                                        isVisible -> GoaldayDesign.Pink.copy(alpha = 0.42f)
                                        isToday -> GoaldayDesign.RouteOverview.copy(alpha = 0.50f)
                                        else -> GoaldayDesign.BorderColor.copy(alpha = 0.14f)
                                    },
                                    shape = RoundedCornerShape(GoaldayDesign.RadiusS),
                                )
                                .clickable { onSelectDay(day) }
                                .padding(5.dp),
                            verticalArrangement = Arrangement.spacedBy(3.dp),
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("$day", style = MaterialTheme.typography.labelMedium, color = GoaldayDesign.InkPrimary, fontWeight = FontWeight.SemiBold)
                                if (todoCount + doneCount > 0) {
                                    Text("$doneCount/$todoCount", style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.InkMuted)
                                }
                            }
                            Text(title, style = MaterialTheme.typography.labelSmall, color = if (title == "空白") GoaldayDesign.InkMuted else GoaldayDesign.InkSecondary, maxLines = 2)
                        }
                    }
                }
                // 末行不足 7 个时补尾空格
                repeat(7 - week.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
internal fun BoxScope.HandbookPaperRuling() {
    // 信纸横线：14 条覆盖更长内容；alpha 从 0x09(3.5%) 提升到 0x14(8%) 让横线在暖纸上可见
    repeat(14) { index ->
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = (54 + index * 24).dp)
                .fillMaxWidth()
                .height(0.45.dp)
                .background(Color(0x14000000)),
        )
    }
    repeat(2) { index ->
        Box(
            modifier = Modifier
                .align(if (index == 0) Alignment.CenterStart else Alignment.CenterEnd)
                .padding(horizontal = 8.dp)
                .width(0.6.dp)
                .fillMaxHeight()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, GoaldayDesign.Pink.copy(alpha = 0.10f), Color.Transparent),
                    ),
                ),
        )
    }
}

@Composable
private fun BoxScope.HandbookMonthHeader(
    year: Int,
    month: Int,
    pageIndex: Int,
    pageCount: Int,
    weeklyTheme: String,
    onWeeklyThemeChange: (String) -> Unit,
    rangeLabel: String,
    visibleDays: List<Int>,
    canShiftPrevious: Boolean,
    canShiftNext: Boolean,
    onPreviousRange: () -> Unit,
    onNextRange: () -> Unit,
    onSelectMonthDay: (Int) -> Unit,
) {
    val monthModel = remember(year, month) { YearMonth.of(year, month) }
    val today = LocalDate.now()
    val visibleDaySet = remember(visibleDays) { visibleDays.toSet() }
    Column(
        modifier = Modifier
            .align(Alignment.TopCenter)
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(0.88f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    "$year GOALDAY",
                    style = MaterialTheme.typography.labelSmall,
                    color = GoaldayDesign.InkMuted,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.alpha(0.86f),
                )
                Text(
                    "${month}月计划",
                    style = MaterialTheme.typography.titleSmall,
                    color = GoaldayDesign.InkPrimary,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "‹",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (canShiftPrevious) GoaldayDesign.Pink else GoaldayDesign.InkMuted,
                        modifier = Modifier
                            .alpha(if (canShiftPrevious) 1f else 0.35f)
                            .clickable(enabled = canShiftPrevious, onClick = onPreviousRange),
                    )
                    Text(rangeLabel, style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.InkSecondary)
                    Text(
                        "›",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (canShiftNext) GoaldayDesign.Pink else GoaldayDesign.InkMuted,
                        modifier = Modifier
                            .alpha(if (canShiftNext) 1f else 0.35f)
                            .clickable(enabled = canShiftNext, onClick = onNextRange),
                    )
                }
            }
            Column(
                modifier = Modifier
                    .weight(1.12f)
                    .clip(RoundedCornerShape(GoaldayDesign.RadiusS))
                    .background(Color(0x08E88FAE))
                    .border(0.35.dp, Color(0x10E88FAE), RoundedCornerShape(GoaldayDesign.RadiusS))
                    .padding(horizontal = 4.dp, vertical = 3.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(1.dp), verticalAlignment = Alignment.CenterVertically) {
                    (1..monthModel.lengthOfMonth()).forEach { day ->
                        val visible = day in visibleDaySet
                        val isToday = today.year == year && today.monthValue == month && today.dayOfMonth == day
                        // 外层扩大热区（22dp 高 + clickable），内层小圆点保持原视觉
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(22.dp)
                                .clickable { onSelectMonthDay(day) },
                            contentAlignment = Alignment.Center,
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(if (visible) 9.dp else 5.dp)
                                    .clip(RoundedCornerShape(99.dp))
                                    .background(
                                        when {
                                            visible -> GoaldayDesign.Pink
                                            isToday -> GoaldayDesign.InkPrimary.copy(alpha = 0.45f)
                                            else -> Color(0x1A000000)
                                        },
                                    ),
                            )
                        }
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("1", style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.InkMuted)
                    Text(monthModel.lengthOfMonth().toString(), style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.InkMuted)
                }
            }
            Text(
                "${pageIndex + 1}/$pageCount",
                style = MaterialTheme.typography.labelSmall,
                color = GoaldayDesign.InkMuted,
                modifier = Modifier
                    .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
                    .background(Color(0x0D000000))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
            )
        }
        BasicTextField(
            value = weeklyTheme,
            onValueChange = onWeeklyThemeChange,
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(GoaldayDesign.RadiusS))
                .background(GoaldayDesign.PinkSoft)
                .border(0.35.dp, GoaldayDesign.Pink.copy(alpha = 0.18f), RoundedCornerShape(GoaldayDesign.RadiusS))
                .padding(horizontal = 7.dp, vertical = 4.dp),
            textStyle = MaterialTheme.typography.bodySmall.copy(color = GoaldayDesign.InkPrimary),
            decorationBox = { inner ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text("本月重点", style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.Pink, fontWeight = FontWeight.SemiBold)
                    Box(Modifier.weight(1f)) {
                        if (weeklyTheme.isBlank()) {
                            Text("写下最重要的目标", style = MaterialTheme.typography.bodySmall, color = GoaldayDesign.InkMuted)
                        }
                        inner()
                    }
                }
            },
        )
    }
}

@Composable
internal fun SectionStamp(
    label: String,
    color: Color,
) {
    Text(
        label,
        style = MaterialTheme.typography.labelSmall,
        color = Color.White,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color)
            .padding(horizontal = 6.dp, vertical = 2.dp),
    )
}

@Composable
private fun DaySpreadSection(
    day: Int,
    doneEntries: List<ScheduleEntry>,
    fallbackDone: List<String>,
    todoCount: Int,
    accent: Color,
    activeDrop: Boolean,
    onBounds: (Rect) -> Unit,
    onToggleCompleted: (ScheduleEntry) -> Unit,
) {
    val doneCount = doneEntries.size + fallbackDone.size
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (activeDrop) GoaldayDesign.GreenSoft else Color(0x42FFFFFF), RoundedCornerShape(GoaldayDesign.RadiusS))
            .border(if (activeDrop) 0.9.dp else 0.45.dp, if (activeDrop) GoaldayDesign.Positive else Color(0x16A88966), RoundedCornerShape(GoaldayDesign.RadiusS))
            .onGloballyPositioned { coordinates -> onBounds(coordinates.boundsInRoot()) }
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("${day}日", style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.InkPrimary, fontWeight = FontWeight.SemiBold)
            Text("完成 $doneCount · 待办 $todoCount", style = MaterialTheme.typography.labelSmall, color = accent.copy(alpha = 0.88f))
        }
        doneEntries.take(2).forEach { entry ->
            HandbookDoneEntryLine(
                day = day,
                entry = entry,
                onToggleCompleted = { onToggleCompleted(entry) },
            )
        }
        val remainingSlots = (2 - doneEntries.take(2).size).coerceAtLeast(0)
        fallbackDone.take(remainingSlots).forEach { line ->
            HandbookFallbackDoneLine(day = day, title = line)
        }
        val visibleDoneRows = doneEntries.take(2).size + fallbackDone.take(remainingSlots).size
        repeat((2 - visibleDoneRows).coerceAtLeast(0)) { index ->
            EmptyHandbookSlot(
                label = when {
                    activeDrop && index == 0 -> "释放放入 done"
                    doneCount == 0 && index == 0 -> "○"
                    else -> ""
                },
                highlight = activeDrop && index == 0,
            )
        }
    }
}

@Composable
private fun HandbookDoneEntryLine(
    day: Int,
    entry: ScheduleEntry,
    onToggleCompleted: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(26.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0x1739A76D))
            .border(0.55.dp, GoaldayDesign.Positive.copy(alpha = 0.26f), RoundedCornerShape(8.dp))
            .clickable(onClick = onToggleCompleted),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Text(
            "${day}日",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            modifier = Modifier
                .width(30.dp)
                .fillMaxHeight()
                .background(GoaldayDesign.Positive)
                .padding(top = 5.dp),
            textAlign = TextAlign.Center,
        )
        Text("✓", style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.Positive)
        if (entry.timeText.isNotBlank()) {
            Text(
                entry.timeText,
                style = MaterialTheme.typography.labelSmall,
                color = GoaldayDesign.InkMuted,
                maxLines = 1,
                modifier = Modifier
                    .clip(RoundedCornerShape(99.dp))
                    .background(Color.White.copy(alpha = 0.64f))
                    .padding(horizontal = 4.dp, vertical = 1.dp),
            )
        }
        val repeatLabel = scheduleRepeatLabel(entry)
        if (repeatLabel.isNotBlank()) {
            Text(
                repeatLabel,
                style = MaterialTheme.typography.labelSmall,
                color = GoaldayDesign.Positive,
                maxLines = 1,
                modifier = Modifier
                    .clip(RoundedCornerShape(99.dp))
                    .background(Color.White.copy(alpha = 0.64f))
                    .padding(horizontal = 4.dp, vertical = 1.dp),
            )
        }
        Text(
            entry.title,
            style = MaterialTheme.typography.bodySmall,
            color = GoaldayDesign.InkMuted,
            textDecoration = TextDecoration.LineThrough,
            maxLines = 1,
            modifier = Modifier.weight(1f),
        )
        Text(
            "done",
            style = MaterialTheme.typography.labelSmall,
            color = GoaldayDesign.Positive,
            maxLines = 1,
            modifier = Modifier.padding(end = 5.dp),
        )
    }
}

@Composable
private fun HandbookFallbackDoneLine(
    day: Int,
    title: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0x10FFFFFF))
            .border(0.45.dp, Color(0x1839A76D), RoundedCornerShape(8.dp)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Text(
            "${day}日",
            style = MaterialTheme.typography.labelSmall,
            color = GoaldayDesign.Positive,
            modifier = Modifier.width(30.dp),
            textAlign = TextAlign.Center,
        )
        Text("✓", style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.Positive.copy(alpha = 0.74f))
        Text(
            title,
            style = MaterialTheme.typography.bodySmall,
            color = GoaldayDesign.InkMuted,
            textDecoration = TextDecoration.LineThrough,
            maxLines = 1,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun HandbookQuickAddRow(
    value: String,
    onValueChange: (String) -> Unit,
    days: List<Int>,
    selectedDay: Int,
    onSelectDay: (Int) -> Unit,
    poolItems: List<String>,
    onAddPoolItem: (String) -> Unit,
    onRemovePoolItem: (String) -> Unit,
    onPickPoolItem: (String) -> Unit,
    onPoolDragStart: (String, Offset) -> Unit,
    onPoolDrag: (Offset) -> Unit,
    onPoolDragEnd: () -> Unit,
    onPoolDragCancel: () -> Unit,
    onDone: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    fun submitAndKeepFocus() {
        onDone()
        focusRequester.requestFocus()
    }
    fun addToPoolAndKeepFocus() {
        onAddPoolItem(value)
        focusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(GoaldayDesign.RadiusS))
            .background(Color(0x35FFEAF1))
            .border(0.45.dp, Color(0x20E88FAE), RoundedCornerShape(GoaldayDesign.RadiusS))
            .padding(horizontal = 6.dp, vertical = 5.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("待安排池", style = MaterialTheme.typography.labelMedium, color = GoaldayDesign.InkPrimary, fontWeight = FontWeight.SemiBold)
            Text("点选或长按拖入日期", style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.InkMuted)
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            textStyle = MaterialTheme.typography.bodySmall.copy(color = GoaldayDesign.InkPrimary),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { submitAndKeepFocus() }),
            decorationBox = { inner ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(GoaldayDesign.RadiusS))
                        .background(Color(0x88FFFFFF))
                        .border(0.35.dp, GoaldayDesign.Pink.copy(alpha = 0.18f), RoundedCornerShape(GoaldayDesign.RadiusS))
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("＋", style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.Pink)
                    Box(Modifier.weight(1f)) {
                        if (value.isBlank()) {
                            Text("写入计划", style = MaterialTheme.typography.bodySmall, color = GoaldayDesign.InkMuted)
                        }
                        inner()
                    }
                    Text("入池", style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.InkSecondary, modifier = Modifier.clickable(onClick = ::addToPoolAndKeepFocus))
                    Text("排入", style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.Pink, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable(onClick = ::submitAndKeepFocus))
                }
            },
        )
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
            days.forEach { day ->
                Text(
                    "${day}日",
                    color = if (day == selectedDay) Color.White else GoaldayDesign.InkSecondary,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                        .background(if (day == selectedDay) GoaldayDesign.Pink else Color(0x0F000000), RoundedCornerShape(GoaldayDesign.RadiusPill))
                        .clickable { onSelectDay(day) }
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                )
            }
        }
        if (poolItems.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                poolItems.forEach { item ->
                    var rowOrigin by remember(item) { mutableStateOf(Offset.Zero) }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(GoaldayDesign.RadiusS))
                            .background(Color(0x66FFFFFF))
                            .border(0.35.dp, Color(0x10E88FAE), RoundedCornerShape(GoaldayDesign.RadiusS))
                            .onGloballyPositioned { coordinates ->
                                rowOrigin = coordinates.boundsInRoot().topLeft
                            }
                            .pointerInput(item) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = { start -> onPoolDragStart(item, rowOrigin + start) },
                                    onDrag = { _, dragAmount -> onPoolDrag(dragAmount) },
                                    onDragEnd = onPoolDragEnd,
                                    onDragCancel = onPoolDragCancel,
                                )
                            }
                            .clickable { onPickPoolItem(item) }
                            .padding(horizontal = 5.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("□", style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.InkMuted)
                        Text(item, style = MaterialTheme.typography.bodySmall, color = GoaldayDesign.InkPrimary, maxLines = 1, modifier = Modifier.weight(1f))
                        Text("×", style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.InkMuted, modifier = Modifier.clickable { onRemovePoolItem(item) })
                    }
                }
            }
        }
    }
}

@Composable
private fun DaySpreadEditableSection(
    day: Int,
    visibleDays: List<Int>,
    entries: List<ScheduleEntry>,
    editingId: String?,
    editingText: String,
    activeDrop: Boolean,
    onBounds: (Rect) -> Unit,
    onStartEdit: (ScheduleEntry) -> Unit,
    onTextChange: (String) -> Unit,
    onCommit: (ScheduleEntry) -> Unit,
    onToggleCompleted: (ScheduleEntry) -> Unit,
    onMoveEntryToDay: (ScheduleEntry, Int) -> Unit,
    onEntryDragStart: (ScheduleEntry, Offset) -> Unit,
    onEntryDrag: (Offset) -> Unit,
    onEntryDragEnd: () -> Unit,
    onEntryDragCancel: () -> Unit,
) {
    var showAllRows by remember(day, entries.map { it.id }) { mutableStateOf(false) }
    val doneCount = entries.count { it.completed }
    val todoCount = entries.count { !it.completed }
    val visibleLimit = if (showAllRows) 6 else 3
    val visibleEntries = entries.take(visibleLimit)
    val hiddenCount = (entries.size - visibleEntries.size).coerceAtLeast(0)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (activeDrop) GoaldayDesign.PinkSoft else Color(0x42FFFFFF), RoundedCornerShape(GoaldayDesign.RadiusS))
            .border(if (activeDrop) 0.9.dp else 0.45.dp, if (activeDrop) GoaldayDesign.Pink else Color(0x16A88966), RoundedCornerShape(GoaldayDesign.RadiusS))
            .onGloballyPositioned { coordinates -> onBounds(coordinates.boundsInRoot()) }
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("${day}日", style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.InkPrimary, fontWeight = FontWeight.SemiBold)
            Text("待办 $todoCount · 完成 $doneCount", style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.RouteDiary)
        }
        repeat(visibleLimit.coerceAtLeast(3)) { idx ->
            val entry = visibleEntries.getOrNull(idx)
            if (entry == null) {
                EmptyHandbookSlot(
                    label = if (activeDrop && idx == visibleEntries.size.coerceAtMost(2)) "释放到${day}日" else "",
                    highlight = activeDrop && idx == visibleEntries.size.coerceAtMost(2),
                )
            } else {
                val dayIndex = visibleDays.indexOf(entry.day)
                HandbookEntryLine(
                    slotLabel = "${day}",
                    entry = entry,
                    editingId = editingId,
                    editingText = editingText,
                    onStartEdit = { onStartEdit(entry) },
                    onTextChange = onTextChange,
                    onCommit = { onCommit(entry) },
                    onToggleCompleted = { onToggleCompleted(entry) },
                    canMovePrevious = dayIndex > 0 && !entry.id.startsWith("fallback_"),
                    canMoveNext = dayIndex >= 0 && dayIndex < visibleDays.lastIndex && !entry.id.startsWith("fallback_"),
                    onMovePrevious = {
                        visibleDays.getOrNull(dayIndex - 1)?.let { targetDay -> onMoveEntryToDay(entry, targetDay) }
                    },
                    onMoveNext = {
                        visibleDays.getOrNull(dayIndex + 1)?.let { targetDay -> onMoveEntryToDay(entry, targetDay) }
                    },
                    onDragStart = { position -> onEntryDragStart(entry, position) },
                    onDrag = onEntryDrag,
                    onDragEnd = onEntryDragEnd,
                    onDragCancel = onEntryDragCancel,
                )
            }
        }
        if (hiddenCount > 0 || showAllRows) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
                    .background(Color(0x10E88FAE))
                    .clickable { showAllRows = !showAllRows }
                    .padding(horizontal = 7.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    if (showAllRows) "收起日程" else "展开 $hiddenCount 条更多",
                    style = MaterialTheme.typography.labelSmall,
                    color = GoaldayDesign.Pink,
                    fontWeight = FontWeight.SemiBold,
                )
                Text("adaptive", style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.InkMuted)
            }
        }
    }
}

@Composable
private fun EmptyHandbookSlot(
    label: String,
    highlight: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(22.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            label.ifBlank { " " },
            style = MaterialTheme.typography.labelSmall,
            color = if (highlight) GoaldayDesign.Pink else GoaldayDesign.InkMuted,
            maxLines = 1,
            modifier = Modifier.width(54.dp),
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(if (highlight) 1.2.dp else 0.6.dp)
                .background(if (highlight) GoaldayDesign.Pink else Color(0x16000000)),
        )
    }
}


@Composable
private fun HandbookEntryLine(
    slotLabel: String,
    entry: ScheduleEntry,
    editingId: String?,
    editingText: String,
    onStartEdit: () -> Unit,
    onTextChange: (String) -> Unit,
    onCommit: () -> Unit,
    onToggleCompleted: () -> Unit,
    canMovePrevious: Boolean,
    canMoveNext: Boolean,
    onMovePrevious: () -> Unit,
    onMoveNext: () -> Unit,
    onDragStart: (Offset) -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val rowEditorFocus = remember(entry.id) { FocusRequester() }
    var rowOrigin by remember(entry.id) { mutableStateOf(Offset.Zero) }
    var expanded by remember(entry.id) { mutableStateOf(false) }
    val statusColor = if (entry.completed) GoaldayDesign.Positive else GoaldayDesign.Pink
    val repeatLabel = scheduleRepeatLabel(entry)
    val hasDetail = entry.note.isNotBlank() || repeatLabel.isNotBlank() || entry.timeText.isNotBlank()
    LaunchedEffect(editingId) {
        if (editingId == entry.id) {
            rowEditorFocus.requestFocus()
        }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (entry.completed) Color(0x1739A76D) else Color(0x18FFFFFF))
            .border(0.55.dp, statusColor.copy(alpha = if (entry.completed) 0.28f else 0.18f), RoundedCornerShape(8.dp))
            .onGloballyPositioned { coordinates ->
                rowOrigin = coordinates.boundsInRoot().topLeft
            }
            .pointerInput(entry.id, editingId) {
                if (editingId == entry.id || entry.id.startsWith("fallback_")) return@pointerInput
                detectDragGesturesAfterLongPress(
                    onDragStart = { start -> onDragStart(rowOrigin + start) },
                    onDrag = { _, dragAmount -> onDrag(dragAmount) },
                    onDragEnd = onDragEnd,
                    onDragCancel = onDragCancel,
                )
            },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
        Text(
            "${slotLabel}日",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            modifier = Modifier
                .width(30.dp)
                .fillMaxHeight()
                .background(statusColor)
                .padding(top = 6.dp),
            textAlign = TextAlign.Center,
        )
        Text(
            if (entry.completed) "✓" else "○",
            style = MaterialTheme.typography.labelSmall,
            color = statusColor,
            modifier = Modifier
                .clip(RoundedCornerShape(99.dp))
                .background(Color.White.copy(alpha = 0.72f))
                .clickable { onToggleCompleted() }
                .padding(horizontal = 4.dp, vertical = 1.dp),
        )
        if (editingId == entry.id) {
            BasicTextField(
                value = editingText,
                onValueChange = onTextChange,
                textStyle = TextStyle(color = GoaldayDesign.InkPrimary),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    onCommit()
                    focusManager.clearFocus(force = true)
                }),
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(rowEditorFocus)
                    .background(Color(0x88FFFFFF), RoundedCornerShape(GoaldayDesign.RadiusS))
                    .padding(horizontal = 5.dp, vertical = 3.dp),
            )
            Text("Done", style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.Pink, modifier = Modifier.clickable {
                onCommit()
                focusManager.clearFocus(force = true)
            })
        } else {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onStartEdit() }
                    .padding(end = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (entry.timeText.isNotBlank()) {
                    Text(
                        entry.timeText,
                        style = MaterialTheme.typography.labelSmall,
                        color = GoaldayDesign.InkMuted,
                        maxLines = 1,
                        modifier = Modifier
                            .clip(RoundedCornerShape(99.dp))
                            .background(Color.White.copy(alpha = 0.64f))
                            .padding(horizontal = 4.dp, vertical = 1.dp),
                    )
                }
                if (repeatLabel.isNotBlank()) {
                    Text(
                        repeatLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        maxLines = 1,
                        modifier = Modifier
                            .clip(RoundedCornerShape(99.dp))
                            .background(Color.White.copy(alpha = 0.64f))
                            .padding(horizontal = 4.dp, vertical = 1.dp),
                    )
                }
                Text(
                    entry.title,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (entry.completed) GoaldayDesign.InkSecondary else GoaldayDesign.InkPrimary,
                    textDecoration = if (entry.completed) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = 1,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Start,
                )
                // 删除冗余 "todo/done" 文字标签（与 ✓/○ 图标语义重复）
                if (hasDetail) {
                    HandbookMoveTargetButton(
                        label = if (expanded) "⌃" else "⌄",
                        enabled = true,
                        onClick = { expanded = !expanded },
                    )
                }
                HandbookMoveTargetButton(
                    label = "‹",
                    enabled = canMovePrevious,
                    onClick = onMovePrevious,
                )
                HandbookMoveTargetButton(
                    label = "›",
                    enabled = canMoveNext,
                    onClick = onMoveNext,
                )
            }
        }
        }
        if (expanded && editingId != entry.id) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 35.dp, end = 7.dp, bottom = 5.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                if (repeatLabel.isNotBlank()) {
                    HandbookEntryDetailChip("repeat", repeatLabel, statusColor)
                }
                if (entry.timeText.isNotBlank()) {
                    HandbookEntryDetailChip("time", entry.timeText, GoaldayDesign.InkSecondary)
                }
                if (entry.note.isNotBlank()) {
                    HandbookEntryDetailChip("note", entry.note, GoaldayDesign.InkMuted)
                }
            }
        }
    }
}

@Composable
private fun HandbookEntryDetailChip(
    label: String,
    value: String,
    color: Color,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(GoaldayDesign.RadiusS))
            .background(Color.White.copy(alpha = 0.58f))
            .border(0.35.dp, color.copy(alpha = 0.16f), RoundedCornerShape(GoaldayDesign.RadiusS))
            .padding(horizontal = 6.dp, vertical = 3.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.width(36.dp),
        )
        Text(
            value,
            style = MaterialTheme.typography.labelSmall,
            color = GoaldayDesign.InkSecondary,
            maxLines = 2,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun HandbookMoveTargetButton(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Text(
        label,
        style = MaterialTheme.typography.labelSmall,
        color = if (enabled) GoaldayDesign.Pink else GoaldayDesign.InkMuted.copy(alpha = 0.38f),
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .clip(RoundedCornerShape(99.dp))
            .background(if (enabled) Color.White.copy(alpha = 0.66f) else Color.Transparent)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 1.dp),
    )
}
