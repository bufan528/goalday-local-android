package com.bf410.goaldaylocal.ui.home

import android.graphics.BitmapFactory
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.bf410.goaldaylocal.data.ScheduleEntry
import com.bf410.goaldaylocal.ui.calendar.CalendarViewModel
import com.bf410.goaldaylocal.ui.replica.GoaldayDesign
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.delay

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun HomeScreen(
    calendarViewModel: CalendarViewModel,
    onOpenCalendar: () -> Unit,
    onOpenCalendarForDay: (Int) -> Unit,
    onOpenBook: () -> Unit,
    onOpenHandbook: () -> Unit,
    onOpenDiary: () -> Unit,
    onOpenInspiration: () -> Unit,
) {
    val state by calendarViewModel.uiState.collectAsState()
    val today = LocalDate.now()
    val currentMonth = YearMonth.of(state.year, state.month)
    val maxDay = currentMonth.lengthOfMonth()
    val defaultDay = if (state.year == today.year && state.month == today.monthValue) today.dayOfMonth else 1
    var selectedDay by remember(state.year, state.month) { mutableIntStateOf(defaultDay.coerceIn(1, maxDay)) }
    selectedDay = selectedDay.coerceIn(1, maxDay)

    var draftTask by rememberSaveable(state.year, state.month) { mutableStateOf("") }
    var draftDay by rememberSaveable(state.year, state.month) { mutableIntStateOf(selectedDay) }
    draftDay = draftDay.coerceIn(1, maxDay)
    var hint by remember { mutableStateOf("") }
    var grabbedEntry by remember { mutableStateOf<ScheduleEntry?>(null) }
    var draggingEntry by remember { mutableStateOf<ScheduleEntry?>(null) }
    var dragPosition by remember { mutableStateOf(Offset.Zero) }
    var activeDropDay by remember { mutableStateOf<Int?>(null) }
    var activeDoneDrop by remember { mutableStateOf(false) }
    val dayDropBounds = remember { mutableStateMapOf<Int, Rect>() }
    var doneDropBounds by remember { mutableStateOf(Rect.Zero) }

    LaunchedEffect(state.year, state.month) {
        grabbedEntry = null
        draggingEntry = null
        activeDropDay = null
        activeDoneDrop = false
        dayDropBounds.clear()
        doneDropBounds = Rect.Zero
    }

    LaunchedEffect(hint) {
        if (hint.isBlank()) return@LaunchedEffect
        delay(1200)
        hint = ""
    }

    fun addDraftTask(day: Int = draftDay) {
        val title = draftTask.trim()
        if (title.isBlank()) {
            hint = "先输入一条任务"
            return
        }
        calendarViewModel.addSchedule(title, day.coerceIn(1, maxDay), "")
        selectedDay = day.coerceIn(1, maxDay)
        draftDay = selectedDay
        draftTask = ""
        hint = "已加入 ${selectedDay} 日"
    }

    fun moveEntryToDay(entry: ScheduleEntry, day: Int) {
        calendarViewModel.moveScheduleToDay(entry.id, day.coerceIn(1, maxDay))
        grabbedEntry = null
        hint = "已拖入 ${day.coerceIn(1, maxDay)} 日"
    }

    fun markDone(entry: ScheduleEntry) {
        if (!entry.completed) {
            calendarViewModel.toggleScheduleCompleted(entry.id)
        }
        grabbedEntry = null
        hint = "已放入 done"
    }

    val weekStart = ((selectedDay - 1) / 7) * 7 + 1
    val weekDays = (weekStart until weekStart + 7).filter { it <= maxDay }
    val monthEntries = state.entries
        .filter { it.year == state.year && it.month == state.month }
        .sortedWith(compareBy<ScheduleEntry>({ it.day }, { it.completed }, { it.title.lowercase() }))
    val selectedDayEntries = monthEntries.filter { it.day == selectedDay }
    val todoEntries = monthEntries.filterNot { it.completed }.take(12)
    val doneEntries = monthEntries.filter { it.completed }.takeLast(6).reversed()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(GoaldayDesign.adaptiveAppBg)
                .verticalScroll(rememberScrollState())
            .padding(horizontal = GoaldayDesign.Space4, vertical = GoaldayDesign.Space3),
        verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space3),
        ) {
            PromoHeader(
                year = state.year,
                month = state.month,
                onToday = {
                    calendarViewModel.backToToday()
                    selectedDay = today.dayOfMonth
                    draftDay = selectedDay
                    hint = "已回到今天"
                },
            )

            PaperPlanner(
                year = state.year,
                month = state.month,
                selectedDay = selectedDay,
                weekDays = weekDays,
                entries = monthEntries,
                selectedDayEntries = selectedDayEntries,
                weeklyGoal = state.theme,
                onWeeklyGoalChange = calendarViewModel::updateTheme,
                draftTask = draftTask,
                onDraftTaskChange = { draftTask = it },
                draftDay = draftDay,
                onDraftDayChange = { draftDay = it },
                onAddDraftTask = { addDraftTask() },
                grabbedEntry = grabbedEntry,
                activeDropDay = activeDropDay,
                onSelectDay = {
                    selectedDay = it
                    draftDay = it
                },
                onOpenDay = onOpenCalendarForDay,
                onDayBounds = { day, rect -> dayDropBounds[day] = rect },
                todoEntries = todoEntries,
                doneEntries = doneEntries,
                activeDoneDrop = activeDoneDrop,
                onDoneBounds = { doneDropBounds = it },
                onGrab = { grabbedEntry = it },
                onMoveToDay = ::moveEntryToDay,
                onMarkDone = ::markDone,
                onToggleDone = { calendarViewModel.toggleScheduleCompleted(it.id) },
                onDragStart = { entry, position ->
                    draggingEntry = entry
                    dragPosition = position
                    activeDropDay = dayDropBounds.entries.firstOrNull { it.value.contains(position) }?.key
                    activeDoneDrop = doneDropBounds.contains(position)
                },
                onDrag = { amount ->
                    dragPosition += amount
                    activeDropDay = dayDropBounds.entries.firstOrNull { it.value.contains(dragPosition) }?.key
                    activeDoneDrop = doneDropBounds.contains(dragPosition)
                },
                onDragEnd = {
                    val target = draggingEntry
                    val targetDay = activeDropDay
                    when {
                        target != null && activeDoneDrop -> markDone(target)
                        target != null && targetDay != null -> moveEntryToDay(target, targetDay)
                        target != null -> hint = "未命中日期或 done"
                    }
                    draggingEntry = null
                    activeDropDay = null
                    activeDoneDrop = false
                },
                onDragCancel = {
                    draggingEntry = null
                    activeDropDay = null
                    activeDoneDrop = false
                    hint = "已取消拖放"
                },
            )

            HomeActionDock(
                onOpenCalendar = onOpenCalendar,
                onOpenInspiration = onOpenInspiration,
                onOpenBook = onOpenBook,
                onOpenHandbook = onOpenHandbook,
                onOpenDiary = onOpenDiary,
            )
            if (hint.isNotBlank()) {
                HomeHintPill(hint)
            }
        }

        FloatingAddButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(18.dp),
            onClick = { addDraftTask() },
        )

        draggingEntry?.let { entry ->
            Column(
                modifier = Modifier
                    .offset { IntOffset(dragPosition.x.toInt(), dragPosition.y.toInt()) }
                    .background(GoaldayDesign.Pink, RoundedCornerShape(GoaldayDesign.RadiusS))
                    .border(1.dp, Color.White, RoundedCornerShape(GoaldayDesign.RadiusS))
                    .padding(horizontal = 9.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(entry.title, color = Color.White, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                Text(
                    when {
                        activeDoneDrop -> "释放放入 done"
                        activeDropDay != null -> "释放拖入 ${activeDropDay} 日"
                        else -> "拖到日期或 done"
                    },
                    color = Color.White.copy(alpha = 0.88f),
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}

@Composable
private fun HomeHintPill(text: String) {
    Text(
        text,
        color = GoaldayDesign.adaptiveInkPrimary,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .fillMaxWidth()
            .background(GoaldayDesign.PinkSoft, RoundedCornerShape(GoaldayDesign.RadiusM))
            .border(GoaldayDesign.Hairline, GoaldayDesign.Pink.copy(alpha = 0.16f), RoundedCornerShape(GoaldayDesign.RadiusM))
            .padding(horizontal = GoaldayDesign.Space3, vertical = GoaldayDesign.Space2),
    )
}

@Composable
private fun PromoHeader(
    year: Int,
    month: Int,
    onToday: () -> Unit,
) {
    val context = LocalContext.current
    val heroBitmap = remember {
        runCatching {
            context.assets.open("lottie/book.png").use(BitmapFactory::decodeStream)
        }.getOrNull()
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(148.dp)
            .shadow(GoaldayDesign.ShadowMedium, RoundedCornerShape(GoaldayDesign.Radius2XL), clip = false)
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFFFFF4EA), Color(0xFFFFE4EC), Color(0xFFF2CFB3)),
                    start = Offset.Zero,
                    end = Offset(760f, 420f),
                ),
                RoundedCornerShape(GoaldayDesign.Radius2XL),
            )
            .border(GoaldayDesign.Hairline, Color.White.copy(alpha = 0.20f), RoundedCornerShape(GoaldayDesign.Radius2XL)),
    ) {
        heroBitmap?.let { bitmap ->
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 6.dp, top = 8.dp, bottom = 8.dp)
                    .size(128.dp),
            )
        }
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xEFFFF4EA), Color(0x88FFF4EA), Color.Transparent),
                    ),
                    RoundedCornerShape(22.dp),
                ),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "今日",
                    color = GoaldayDesign.Pink,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.72f), RoundedCornerShape(GoaldayDesign.RadiusPill))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                )
                Text(
                    "今天",
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                        .background(GoaldayDesign.adaptiveInkPrimary, RoundedCornerShape(GoaldayDesign.RadiusPill))
                        .clickable(onClick = onToday)
                        .padding(horizontal = 13.dp, vertical = 7.dp),
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("Goalday", color = GoaldayDesign.adaptiveInkPrimary, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                Text("${year}年${month}月 · 本地日程手账", color = GoaldayDesign.adaptiveInkSecondary, style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    HomeHeroPill("离线", GoaldayDesign.Positive)
                    HomeHeroPill("周计划", GoaldayDesign.Pink)
                    HomeHeroPill("可拖拽", Color(0xFF8F684F))
                }
            }
        }
    }
}

@Composable
private fun HomeHeroPill(label: String, color: Color) {
    Text(
        label,
        color = color,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .background(Color.White.copy(alpha = 0.72f), RoundedCornerShape(GoaldayDesign.RadiusPill))
            .padding(horizontal = 8.dp, vertical = 3.dp),
    )
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun PaperPlanner(
    year: Int,
    month: Int,
    selectedDay: Int,
    weekDays: List<Int>,
    entries: List<ScheduleEntry>,
    selectedDayEntries: List<ScheduleEntry>,
    weeklyGoal: String,
    onWeeklyGoalChange: (String) -> Unit,
    draftTask: String,
    onDraftTaskChange: (String) -> Unit,
    draftDay: Int,
    onDraftDayChange: (Int) -> Unit,
    onAddDraftTask: () -> Unit,
    grabbedEntry: ScheduleEntry?,
    activeDropDay: Int?,
    onSelectDay: (Int) -> Unit,
    onOpenDay: (Int) -> Unit,
    onDayBounds: (Int, Rect) -> Unit,
    todoEntries: List<ScheduleEntry>,
    doneEntries: List<ScheduleEntry>,
    activeDoneDrop: Boolean,
    onDoneBounds: (Rect) -> Unit,
    onGrab: (ScheduleEntry) -> Unit,
    onMoveToDay: (ScheduleEntry, Int) -> Unit,
    onMarkDone: (ScheduleEntry) -> Unit,
    onToggleDone: (ScheduleEntry) -> Unit,
    onDragStart: (ScheduleEntry, Offset) -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(GoaldayDesign.ShadowSoft, RoundedCornerShape(GoaldayDesign.Radius2XL), clip = false)
            .background(GoaldayDesign.Surface, RoundedCornerShape(GoaldayDesign.Radius2XL))
            .border(GoaldayDesign.Hairline, GoaldayDesign.BorderColor.copy(alpha = GoaldayDesign.HairlineAlpha), RoundedCornerShape(GoaldayDesign.Radius2XL))
            .padding(horizontal = GoaldayDesign.Space3, vertical = GoaldayDesign.Space3),
        verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space3),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text("日程动态", color = GoaldayDesign.Pink, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                Text("${month}月周视图", color = GoaldayDesign.adaptiveInkPrimary, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
                PlannerMetric("待办", todoEntries.size.toString(), GoaldayDesign.Pink)
                PlannerMetric("已完成", doneEntries.size.toString(), GoaldayDesign.Positive)
            }
        }

        BasicTextField(
            value = weeklyGoal,
            onValueChange = onWeeklyGoalChange,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodySmall.copy(color = GoaldayDesign.adaptiveInkPrimary),
            decorationBox = { inner ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(GoaldayDesign.PinkSoft, RoundedCornerShape(GoaldayDesign.RadiusS))
                        .border(0.7.dp, GoaldayDesign.Pink.copy(alpha = 0.14f), RoundedCornerShape(GoaldayDesign.RadiusS))
                        .padding(horizontal = 9.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text("本周主题", color = GoaldayDesign.Pink, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                    Box(Modifier.weight(1f)) {
                        if (weeklyGoal.isBlank()) {
                            Text("写下最重要的目标", color = GoaldayDesign.adaptiveInkMuted, style = MaterialTheme.typography.bodySmall)
                        }
                        inner()
                    }
                }
            },
        )

        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1.03f), verticalArrangement = Arrangement.spacedBy(0.dp)) {
                Text("日期", color = GoaldayDesign.adaptiveInkMuted, style = MaterialTheme.typography.labelSmall)
                weekDays.forEach { day ->
                    val dayEntries = entries.filter { it.day == day }
                    TimelineDayRow(
                        day = day,
                        selected = day == selectedDay,
                        activeDrop = activeDropDay == day || grabbedEntry != null && day == selectedDay,
                        entries = dayEntries,
                        year = year,
                        month = month,
                        onSelect = { onSelectDay(day) },
                        onOpen = { onOpenDay(day) },
                        onBounds = { onDayBounds(day, it) },
                        onGrabDrop = {
                            val entry = grabbedEntry ?: return@TimelineDayRow
                            onMoveToDay(entry, day)
                        },
                    )
                }
            }

            Spacer(
                modifier = Modifier
                    .width(1.dp)
                    .height(430.dp)
                    .background(Color(0x11000000)),
            )

            Column(
                modifier = Modifier
                    .weight(1.18f)
                    .padding(start = 10.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                Text("待办", color = GoaldayDesign.Pink, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                QuickInput(
                    value = draftTask,
                    onValueChange = onDraftTaskChange,
                    weekDays = weekDays,
                    selectedDay = draftDay,
                    onSelectDay = onDraftDayChange,
                    onDone = onAddDraftTask,
                )
                if (selectedDayEntries.isNotEmpty()) {
                    MiniSection("当天") {
                        selectedDayEntries.take(4).forEach { entry ->
                            TaskLine(
                                entry = entry,
                                onGrab = onGrab,
                                onToggleDone = onToggleDone,
                                onDragStart = onDragStart,
                                onDrag = onDrag,
                                onDragEnd = onDragEnd,
                                onDragCancel = onDragCancel,
                            )
                        }
                    }
                }
                MiniSection("右侧清单") {
                    if (todoEntries.isEmpty()) {
                        EmptyHint("在上方输入一周要做的事")
                    } else {
                        todoEntries.forEach { entry ->
                            TaskLine(
                                entry = entry,
                                onGrab = onGrab,
                                onToggleDone = onToggleDone,
                                onDragStart = onDragStart,
                                onDrag = onDrag,
                                onDragEnd = onDragEnd,
                                onDragCancel = onDragCancel,
                            )
                        }
                    }
                }
                MiniSection(
                    title = "done",
                    modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { onDoneBounds(it.boundsInRoot()) }
                        .background(if (activeDoneDrop) GoaldayDesign.GreenSoft else Color.Transparent, RoundedCornerShape(GoaldayDesign.RadiusS))
                        .border(0.7.dp, if (activeDoneDrop) GoaldayDesign.Positive else Color(0x14000000), RoundedCornerShape(GoaldayDesign.RadiusS))
                        .padding(horizontal = 6.dp, vertical = 5.dp),
                ) {
                    if (doneEntries.isEmpty()) {
                        EmptyHint("把完成的事项拖入")
                    } else {
                        doneEntries.forEach { entry ->
                            Text(
                                "✓ ${entry.title}",
                                color = GoaldayDesign.adaptiveInkSecondary,
                                style = MaterialTheme.typography.bodySmall,
                                textDecoration = TextDecoration.LineThrough,
                                maxLines = 1,
                            )
                        }
                    }
                    grabbedEntry?.let {
                        Text(
                            "点此放入 done",
                            color = GoaldayDesign.Positive,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.clickable { onMarkDone(it) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun TimelineDayRow(
    day: Int,
    selected: Boolean,
    activeDrop: Boolean,
    entries: List<ScheduleEntry>,
    year: Int,
    month: Int,
    onSelect: () -> Unit,
    onOpen: () -> Unit,
    onBounds: (Rect) -> Unit,
    onGrabDrop: () -> Unit,
) {
    val currentMonth = YearMonth.of(year, month)
    val date = currentMonth.atDay(day.coerceIn(1, currentMonth.lengthOfMonth()))
    val week = when (date.dayOfWeek) {
        DayOfWeek.MONDAY -> "一"
        DayOfWeek.TUESDAY -> "二"
        DayOfWeek.WEDNESDAY -> "三"
        DayOfWeek.THURSDAY -> "四"
        DayOfWeek.FRIDAY -> "五"
        DayOfWeek.SATURDAY -> "六"
        DayOfWeek.SUNDAY -> "日"
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { onBounds(it.boundsInRoot()) }
            .background(
                if (activeDrop) GoaldayDesign.PinkSoft else if (selected) Color(0x0DE88FAE) else Color.Transparent,
                RoundedCornerShape(GoaldayDesign.RadiusS),
            )
            .combinedClickable(onClick = onSelect, onLongClick = onOpen)
            .padding(horizontal = 6.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(32.dp)) {
            Text(
                day.toString(),
                color = if (selected) Color.White else GoaldayDesign.Pink,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .background(if (selected) GoaldayDesign.Pink else Color(0x12E88FAE), RoundedCornerShape(99.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            )
            Text(week, color = GoaldayDesign.adaptiveInkMuted, style = MaterialTheme.typography.labelSmall)
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            if (entries.isEmpty()) {
                Text(if (activeDrop) "点此拖入" else " ", color = GoaldayDesign.Pink, style = MaterialTheme.typography.labelSmall, modifier = Modifier.clickable(onClick = onGrabDrop))
            } else {
                entries.take(2).forEach { entry ->
                    Text(
                        (if (entry.completed) "✓ " else "· ") + entry.title,
                        color = if (entry.completed) GoaldayDesign.adaptiveInkSecondary else GoaldayDesign.adaptiveInkPrimary,
                        style = MaterialTheme.typography.labelSmall,
                        textDecoration = if (entry.completed) TextDecoration.LineThrough else TextDecoration.None,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickInput(
    value: String,
    onValueChange: (String) -> Unit,
    weekDays: List<Int>,
    selectedDay: Int,
    onSelectDay: (Int) -> Unit,
    onDone: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    fun submitAndKeepFocus() {
        onDone()
        focusRequester.requestFocus()
    }

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        modifier = Modifier.focusRequester(focusRequester),
        textStyle = MaterialTheme.typography.bodySmall.copy(color = GoaldayDesign.adaptiveInkPrimary),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { submitAndKeepFocus() }),
        decorationBox = { inner ->
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFAFFFDF8), RoundedCornerShape(GoaldayDesign.RadiusS))
                        .border(0.7.dp, Color(0x16000000), RoundedCornerShape(GoaldayDesign.RadiusS))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(Icons.Filled.CheckBoxOutlineBlank, contentDescription = null, tint = GoaldayDesign.adaptiveInkMuted, modifier = Modifier.size(12.dp))
                    Box(Modifier.weight(1f)) {
                        if (value.isBlank()) {
                            Text("列出一周要做的所有事", color = GoaldayDesign.adaptiveInkMuted, style = MaterialTheme.typography.bodySmall)
                        }
                        inner()
                    }
                    Text("加入", color = GoaldayDesign.Pink, style = MaterialTheme.typography.labelSmall, modifier = Modifier.clickable(onClick = ::submitAndKeepFocus))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("日期", color = GoaldayDesign.adaptiveInkMuted, style = MaterialTheme.typography.labelSmall)
                    DateChipRow(days = weekDays.take(4), selectedDay = selectedDay, onSelectDay = onSelectDay)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Spacer(Modifier.width(24.dp))
                    DateChipRow(days = weekDays.drop(4).take(3), selectedDay = selectedDay, onSelectDay = onSelectDay)
                }
            }
        },
    )
}

@Composable
private fun DateChipRow(
    days: List<Int>,
    selectedDay: Int,
    onSelectDay: (Int) -> Unit,
) {
    days.forEach { day ->
        Text(
            "${day}日",
            color = if (day == selectedDay) Color.White else GoaldayDesign.adaptiveInkSecondary,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier
                .background(if (day == selectedDay) GoaldayDesign.Pink else Color(0x0F000000), RoundedCornerShape(GoaldayDesign.RadiusPill))
                .clickable { onSelectDay(day) }
                .padding(horizontal = 6.dp, vertical = 3.dp),
        )
    }
}

@Composable
private fun PlannerMetric(
    label: String,
    value: String,
    color: Color,
) {
    Column(
        modifier = Modifier
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(1.dp),
    ) {
        Text(value, color = color, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, maxLines = 1)
        Text(label, color = GoaldayDesign.adaptiveInkMuted, style = MaterialTheme.typography.labelSmall, maxLines = 1)
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun TaskLine(
    entry: ScheduleEntry,
    onGrab: (ScheduleEntry) -> Unit,
    onToggleDone: (ScheduleEntry) -> Unit,
    onDragStart: (ScheduleEntry, Offset) -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit,
) {
    var rowOrigin by remember(entry.id) { mutableStateOf(Offset.Zero) }
    val haptic = LocalHapticFeedback.current
    // 勾选多巴胺动画：完成瞬间图标从 0.6 弹到 1.0（spring 过冲），仿 Things 3 完成反馈
    val bounce = remember(entry.id) { Animatable(1f) }
    LaunchedEffect(entry.completed) {
        if (entry.completed) {
            bounce.snapTo(0.6f)
            bounce.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
        }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(if (entry.completed) 0.dp else 2.dp, RoundedCornerShape(12.dp), clip = false)
            .background(if (entry.completed) Color(0x0D39A76D) else Color(0xFAFFFDF8), RoundedCornerShape(12.dp))
            .border(0.6.dp, if (entry.completed) GoaldayDesign.Positive.copy(alpha = 0.14f) else Color(0x18A88966), RoundedCornerShape(12.dp))
            .combinedClickable(onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onToggleDone(entry)
            }, onLongClick = { onGrab(entry) })
            .onGloballyPositioned { rowOrigin = it.boundsInRoot().topLeft }
            .pointerInput(entry.id) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { onDragStart(entry, rowOrigin + it) },
                    onDrag = { change, amount ->
                        change.consume()
                        onDrag(amount)
                    },
                    onDragEnd = onDragEnd,
                    onDragCancel = onDragCancel,
                )
            }
            .padding(horizontal = 8.dp, vertical = 7.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Box(
            modifier = Modifier
                .graphicsLayer { scaleX = bounce.value; scaleY = bounce.value }
                .background(if (entry.completed) GoaldayDesign.Positive else GoaldayDesign.Pink, RoundedCornerShape(99.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                if (entry.completed) Icons.Filled.Check else Icons.Filled.RadioButtonUnchecked,
                contentDescription = if (entry.completed) "已完成" else "待办",
                tint = Color.White,
                modifier = Modifier.size(12.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                entry.title,
                color = if (entry.completed) GoaldayDesign.adaptiveInkSecondary else GoaldayDesign.adaptiveInkPrimary,
                style = MaterialTheme.typography.bodySmall,
                textDecoration = if (entry.completed) TextDecoration.LineThrough else TextDecoration.None,
                maxLines = 2,
            )
            Text("${entry.day}日", color = GoaldayDesign.adaptiveInkMuted, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun MiniSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(title, color = GoaldayDesign.adaptiveInkSecondary, style = MaterialTheme.typography.labelSmall)
        content()
    }
}

@Composable
private fun EmptyHint(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(GoaldayDesign.RadiusM))
            .background(GoaldayDesign.Paper)
            .border(GoaldayDesign.Hairline, GoaldayDesign.BorderColor.copy(alpha = 0.12f), RoundedCornerShape(GoaldayDesign.RadiusM))
            .padding(horizontal = GoaldayDesign.Space2, vertical = GoaldayDesign.Space2),
        horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space2),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(RoundedCornerShape(99.dp))
                .background(GoaldayDesign.Pink.copy(alpha = 0.72f)),
        )
        Text(text, color = GoaldayDesign.adaptiveInkMuted, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun HomeActionDock(
    onOpenCalendar: () -> Unit,
    onOpenInspiration: () -> Unit,
    onOpenBook: () -> Unit,
    onOpenHandbook: () -> Unit,
    onOpenDiary: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(GoaldayDesign.ShadowSoft, RoundedCornerShape(GoaldayDesign.RadiusXL), clip = false)
            .background(Color.White.copy(alpha = 0.80f), RoundedCornerShape(GoaldayDesign.RadiusXL))
            .border(GoaldayDesign.Hairline, GoaldayDesign.BorderColor.copy(alpha = 0.13f), RoundedCornerShape(GoaldayDesign.RadiusXL))
            .padding(GoaldayDesign.Space2),
        verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space2),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            HomeActionCard("日历", "月视图", Icons.Filled.CalendarMonth, GoaldayDesign.Pink, Modifier.weight(1f), onOpenCalendar)
            HomeActionCard("灵感", "专题库", Icons.Filled.Lightbulb, GoaldayDesign.Today, Modifier.weight(1f), onOpenInspiration)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            HomeActionCard("书库", "本子", Icons.Filled.Book, GoaldayDesign.RouteOverview, Modifier.weight(1f), onOpenBook)
            HomeActionCard("手账", "书内页", Icons.AutoMirrored.Filled.Article, GoaldayDesign.RouteTarget, Modifier.weight(1f), onOpenHandbook)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            HomeActionCard("日记", "记录块", Icons.Filled.EditNote, GoaldayDesign.RouteDiary, Modifier.weight(1f), onOpenDiary)
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun HomeActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .height(62.dp)
            .shadow(4.dp, RoundedCornerShape(GoaldayDesign.RadiusM), clip = false)
            .background(color.copy(alpha = 0.10f), RoundedCornerShape(GoaldayDesign.RadiusM))
            .border(GoaldayDesign.Hairline, color.copy(alpha = 0.20f), RoundedCornerShape(GoaldayDesign.RadiusM))
            .clickable(onClick = onClick)
            .padding(horizontal = GoaldayDesign.Space3, vertical = GoaldayDesign.Space2),
        horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space2),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .background(color, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = title, tint = Color.White, modifier = Modifier.size(18.dp))
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(title, color = GoaldayDesign.adaptiveInkPrimary, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, maxLines = 1)
            Text(subtitle, color = GoaldayDesign.adaptiveInkMuted, style = MaterialTheme.typography.labelSmall, maxLines = 1)
        }
    }
}

@Composable
private fun FloatingAddButton(modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .size(48.dp)
            .background(GoaldayDesign.Pink, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Filled.Add, contentDescription = "新增", tint = Color.White, modifier = Modifier.size(24.dp))
    }
}
