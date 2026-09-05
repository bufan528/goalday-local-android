package com.bf410.goaldaylocal.ui.book

import androidx.compose.foundation.ScrollState
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.paint
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.bf410.goaldaylocal.R
import com.bf410.goaldaylocal.data.BookPage
import com.bf410.goaldaylocal.data.ScheduleEntry
import com.bf410.goaldaylocal.ui.replica.GoaldayDesign
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.delay

// 逆向 item_schedule_item_in_book.xml：pt 是解码器对 dip 的误标，直接使用原始 dip 值
private val InBookScheduleColumnPaddingVertical = 3.5.dp
private val InBookScheduleSlotHeight = 12.dp

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
    onAddSchedule: (String, Int, Int, String, Int) -> Unit,
    onWeeklyThemeChange: (String) -> Unit,
    onUpdateScheduleTitle: (String, String) -> Unit,
    onMoveScheduleDay: (String, Int, Int) -> Unit,
    onToggleScheduleCompleted: (String) -> Unit,
    turnProgress: Float,
    turnDirection: TurnDirection?,
    startInMonthBoard: Boolean = false,
) {
    val eased = turnProgress * turnProgress * (3f - 2f * turnProgress)
    val contentShift = when (turnDirection) {
        TurnDirection.NEXT -> -(eased * 8f)
        TurnDirection.PREVIOUS -> eased * 8f
        null -> 0f
    }
    val alpha = (1f - eased * 0.08f).coerceIn(0.92f, 1f)
    var monthOffset by rememberSaveable(page.title) { mutableStateOf(0) }
    val defaultScheduleModel = buildScheduleHandbookModel(
        page = page,
        scheduleEntries = schedulePreviewEntries,
        todayPlanItems = todayPlanItems,
        todayCompletedItems = todayCompletedItems,
        requestedWindowStart = null,
        monthOffset = monthOffset,
    )
    // P1-1 修复：windowStart 改用 rememberSaveable 持久化，且 LaunchedEffect key 移除 defaultScheduleModel.windowStart
    // 原代码 LaunchedEffect key 含 defaultScheduleModel.windowStart，数据更新（如勾选日程）会触发 recomposition
    // 导致 defaultScheduleModel.windowStart 变化，覆盖用户手动滑动的日历窗口位置
    // 现只在 page.title/year/month 变化（真正切换月份）时重置，同月内用户设置不再被覆盖
    var windowStart by rememberSaveable(page.title, defaultScheduleModel.year, defaultScheduleModel.month) {
        mutableStateOf(defaultScheduleModel.windowStart)
    }
    LaunchedEffect(page.title, defaultScheduleModel.year, defaultScheduleModel.month) {
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
    var draftText by rememberSaveable(page.title) { mutableStateOf("") }
    var draftDay by remember(page.title) { mutableStateOf(rightBlocks.firstOrNull()?.day ?: 1) }
    LaunchedEffect(visibleDays) {
        if (draftDay !in visibleDays) {
            draftDay = visibleDays.firstOrNull() ?: 1
        }
    }
    val selectedDraftDay = if (draftDay in visibleDays) draftDay else visibleDays.firstOrNull() ?: 1
    var editingId by rememberSaveable(pageIndex) { mutableStateOf<String?>(null) }
    var editingText by rememberSaveable(pageIndex) { mutableStateOf("") }
    var saveHint by remember(pageIndex) { mutableStateOf("") }
    var boardMode by remember(pageIndex, startInMonthBoard) { mutableStateOf(if (startInMonthBoard) ScheduleBoardMode.MONTH else ScheduleBoardMode.SPREAD) }
    var spreadOrigin by remember(pageIndex) { mutableStateOf(Offset.Zero) }
    // 对照逆向 pop_repeat.xml：新增日程时的重复模式
    var repeatRuleForNewSchedule by rememberSaveable(pageIndex) { mutableStateOf("") }
    var repeatIntervalForNewSchedule by rememberSaveable(pageIndex) { mutableStateOf(1) }
    var showRepeatPicker by rememberSaveable(pageIndex) { mutableStateOf(false) }
    val todoDropBounds = remember(pageIndex) { mutableMapOf<Int, Rect>() }
    val doneDropBounds = remember(pageIndex) { mutableMapOf<Int, Rect>() }
    var draggingPoolItem by remember(pageIndex) { mutableStateOf<String?>(null) }
    var draggingTodoEntry by remember(pageIndex) { mutableStateOf<ScheduleEntry?>(null) }
    var dragPosition by remember(pageIndex) { mutableStateOf(Offset.Zero) }
    var activePoolDropDay by remember(pageIndex) { mutableStateOf<Int?>(null) }
    var activeDoneDropDay by remember(pageIndex) { mutableStateOf<Int?>(null) }
    // P1-1：移除 context/exportHint（合并预览+快存为单个导出入口后，快存直接调用已删除）
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

    // 对照原版 BookViewExampleKt FragmentPage:
    // 左页(i%2==0): TopStart对齐, padding(start=10dp, top=10dp)
    // 右页(i%2!=0): TopEnd对齐, padding(top=10dp, end=10dp)
    // 主文字12sp, 副文字10sp, 颜色color_tab_divider(#FFC5BBB6)
    val isLeftPage = pageIndex % 2 == 0
    val weekNumber = try {
        val firstDay = LocalDate.of(anchorYear, anchorMonth, visibleDays.firstOrNull() ?: 1)
        (firstDay.dayOfYear - firstDay.dayOfWeek.value + 10) / 7
    } catch (_: Exception) { 0 }
    val dateLabelMain = "${anchorMonth}月"
    val dateLabelSub = "第${weekNumber}周"
    val tabDividerColor = Color(0xFFC5BBB6)

    Box(
        modifier = modifier
            // 对照原版 BaseBookViewKt.java L514：页面圆角 = RoundedCornerShape(0, l, l, 0)，l=10dp
            .clip(RoundedCornerShape(0.dp, 10.dp, 10.dp, 0.dp))
            .graphicsLayer {
                translationX = contentShift
                this.alpha = alpha
            }
            .onGloballyPositioned { coordinates ->
                spreadOrigin = coordinates.boundsInRoot().topLeft
            }
            // 对照原版 fragment_schedule_inbook.xml：根布局 NoTouchConstraintLayout 无内边距
            .padding(horizontal = 0.dp, vertical = 0.dp),
    ) {
        // 顶部日期标签（对照 BookViewExampleKt FragmentPage L882-L1028）
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = if (isLeftPage) 10.dp else 0.dp,
                    top = 10.dp,
                    end = if (isLeftPage) 0.dp else 10.dp,
                ),
            contentAlignment = if (isLeftPage) Alignment.TopStart else Alignment.TopEnd,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = dateLabelMain,
                    fontSize = 12.sp,
                    color = tabDividerColor,
                    fontFamily = GoaldayDesign.BodyFontFamily,
                )
                Text(
                    text = " | $dateLabelSub",
                    fontSize = 10.sp,
                    color = tabDividerColor,
                    fontFamily = GoaldayDesign.BodyFontFamily,
                )
            }
        }
        // 对照逆向 fragment_schedule_inbook.xml：书页日程容器无右上角 chip 工具栏，
        // 仅保留 NoTouchConstraintLayout + 日期标签 + RecyclerView 结构。
        // 对照逆向 fragment_schedule_inbook.xml + item_schedule_item_in_book.xml：
        // 简单垂直列表，每行一天：24.5dp 日期列 + 2 列 x3 行目标槽
        // 对照逆向 fragment_schedule_inbook.xml: RecyclerView 无水平 margin（start/end to parent）
        if (boardMode == ScheduleBoardMode.SPREAD) {
            val scheduleScrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    // 对照原版 BookViewExampleKt FragmentPage L772: 顶部25dp Spacer + 0.5dp分割线
                    // schedule页还有5dp top padding(L740: padding(top=5dp))
                    .padding(top = 25.dp + 0.5.dp + 5.dp, bottom = 10.dp)
                    .verticalScroll(scheduleScrollState),
            ) {
                leftBlocks.forEach { block ->
                    ScheduleDayRow(
                        day = block.day,
                        month = anchorMonth,
                        year = anchorYear,
                        entries = sorted.filter { it.day == block.day },
                        onToggleCompleted = { },
                        onAddSchedule = { },
                    )
                }
            }
            // 0.5dp 分割线（仅 schedule 页有，对照 BookViewExampleKt L774-L776）
            Spacer(
                modifier = Modifier
                    .padding(top = 25.dp)
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(tabDividerColor),
            )
        } else {
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
                    .padding(start = 0.dp, end = 0.dp, top = 10.dp, bottom = 10.dp),
            )
        }
    }
    longImagePreview?.let { preview ->
        LongImagePreviewDialog(
            preview = preview,
            onDismiss = { longImagePreview = null },
        )
    }
    if (showRepeatPicker) {
        RepeatModePickerDialog(
            selectedRule = repeatRuleForNewSchedule,
            onDismiss = { showRepeatPicker = false },
            onSelect = { rule, interval ->
                repeatRuleForNewSchedule = rule
                repeatIntervalForNewSchedule = interval.coerceAtLeast(1)
            },
        )
    }
}

@Composable
internal fun HandbookMonthBoard(
    year: Int,
    month: Int,
    monthLength: Int,
    entries: List<ScheduleEntry>,
    selectedDays: List<Int>,
    onSelectDay: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val today = LocalDate.now()
    // 按 1 号是星期几补前置空格；以周日为每周第一天（日一二三四五六）
    val firstWeekday = YearMonth.of(year, month).atDay(1).dayOfWeek.value // 1=周一 ... 7=周日
    val leadingBlanks = firstWeekday % 7
    val allSlots: List<Int?> = List(leadingBlanks) { null } + (1..monthLength).toList()
    val weeks = allSlots.chunked(7)
    val weekdayLabels = listOf("日", "一", "二", "三", "四", "五", "六")
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space1 + 2.dp),
    ) {
        // 星期表头行
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space1 + 1.dp)) {
            weekdayLabels.forEach { label ->
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(label, style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.adaptiveInkMuted, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        weeks.forEach { week ->
            Row(horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space1 + 1.dp), modifier = Modifier.weight(1f)) {
                week.forEach { day ->
                    if (day == null) {
                        Spacer(modifier = Modifier.weight(1f))
                    } else {
                        val dayEntries = entries.filter { it.day == day }
                        val todoCount = dayEntries.count { !it.completed }
                        val doneCount = dayEntries.count { it.completed }
                        // P2-6 修复：空日期不再显示"空白"文字（每个空格都写"空白"造成视觉噪音）
                        // 改为空字符串，空日期只显示日期数字，保持月历干净
                        val title = dayEntries.firstOrNull { !it.completed }?.title
                            ?: dayEntries.firstOrNull { it.completed }?.title
                            ?: ""
                        val isVisible = day in selectedDays
                        val isToday = today.year == year && today.monthValue == month && today.dayOfMonth == day
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(GoaldayDesign.RadiusS))
                                .background(
                                    when {
                                        isToday -> GoaldayDesign.PaperAged.copy(alpha = 0.22f)
                                        else -> Color.Transparent
                                    },
                                )
                                .border(
                                    width = if (isToday) GoaldayDesign.Hairline else 0.dp,
                                    color = if (isToday) GoaldayDesign.BorderColor.copy(alpha = 0.18f) else Color.Transparent,
                                    shape = RoundedCornerShape(GoaldayDesign.RadiusS),
                                )
                                .clickable { onSelectDay(day) }
                                .padding(GoaldayDesign.Space1 + 1.dp),
                            verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space1 - 1.dp),
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "$day",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isToday) GoaldayDesign.adaptiveInkPrimary else GoaldayDesign.adaptiveInkSecondary,
                                    fontWeight = if (isToday) FontWeight.SemiBold else FontWeight.Normal,
                                )
                                if (todoCount + doneCount > 0) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
                                        if (todoCount > 0) ScheduleStatusDot(GoaldayDesign.Pink)
                                        if (doneCount > 0) ScheduleStatusDot(GoaldayDesign.Positive)
                                    }
                                }
                            }
                            // P2-6：空日期不显示标题文字，避免"空白"噪音
                            if (title.isNotBlank()) {
                                Text(title, style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.adaptiveInkSecondary, maxLines = 1)
                            }
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
private fun ScheduleStatusDot(color: Color) {
    Box(
        modifier = Modifier
            .size(5.dp)
            .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
            .background(color),
    )
}

/**
 * 手账内页纸张纹理：叠加原版 APK 的 paper_texture_lined 纹理，低透明度避免干扰内容阅读。
 * 与 handbookPaperRuling 配合使用，先铺渐变/纹理再画横线。
 */
internal fun Modifier.handbookPaperTexture(alpha: Float = 0.12f): Modifier = composed {
    paint(
        painter = painterResource(R.drawable.paper_texture_lined),
        contentScale = ContentScale.Crop,
        alpha = alpha,
    )
}

/**
 * 手账内页横线：模拟信纸/笔记本 ruled page。
 *
 * 现方案：作为 Modifier 应用到滚动 Column 上，drawBehind 在 Column 的视口坐标系绘制，
 * 通过 scrollState.value 偏移横线，使横线随内容同步滚动，恢复"信纸感"。
 *
 * @param scrollState 滚动状态；null 表示非滚动容器，横线静态绘制
 * @param lineSpacingDp 横线间距，默认 24dp（信纸常见行高）
 * @param lineColor 横线颜色，默认低饱和墨色（InkMuted 10% alpha）
 */
internal fun Modifier.handbookPaperRuling(
    scrollState: ScrollState? = null,
    lineSpacingDp: androidx.compose.ui.unit.Dp = GoaldayDesign.Space6,
    lineColor: Color = GoaldayDesign.InkMuted.copy(alpha = 0.10f),
): Modifier = this.drawBehind {
    val spacingPx = lineSpacingDp.toPx()
    val marginPx = 6.dp.toPx()
    val scrollOffset = scrollState?.value?.toFloat() ?: 0f
    // 左右装订边线（垂直，淡墨）
    drawLine(
        color = lineColor.copy(alpha = 0.07f),
        start = Offset(marginPx, 0f),
        end = Offset(marginPx, size.height),
        strokeWidth = 0.6.dp.toPx(),
    )
    drawLine(
        color = lineColor.copy(alpha = 0.07f),
        start = Offset(size.width - marginPx, 0f),
        end = Offset(size.width - marginPx, size.height),
        strokeWidth = 0.6.dp.toPx(),
    )
    // 横线：受 scrollOffset 偏移，随内容同步滚动
    // drawBehind 绘制在视口坐标系，内容向上滚动 scrollOffset 像素，横线也向上偏移同样距离
    var y = -scrollOffset % spacingPx
    if (y < 0f) y += spacingPx
    val startX = marginPx + 2.dp.toPx()
    val endX = size.width - marginPx - 2.dp.toPx()
    while (y < size.height) {
        drawLine(
            color = lineColor,
            start = Offset(startX, y),
            end = Offset(endX, y),
            strokeWidth = 0.5.dp.toPx(),
        )
        y += spacingPx
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
    // 对照原版 fragment_schedule_inbook.xml 顶部：
    // 左右各一组日期/页码小标签，marginTop=10dp，marginStart/End=10dp。
    // 原版标签默认 visibility=gone；这里保留可见页码，结构对齐。
    Row(
        modifier = Modifier
            .align(Alignment.TopCenter)
            .fillMaxWidth()
            .padding(top = 10.dp, start = 10.dp, end = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 左上：月份小标签（11sp + 9sp）
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "${month}月",
                fontSize = 11.sp,
                color = GoaldayDesign.adaptiveInkPrimary,
                fontFamily = GoaldayDesign.BodyFontFamily,
            )
            Text(
                "$year",
                fontSize = 9.sp,
                color = GoaldayDesign.adaptiveInkMuted,
                fontFamily = GoaldayDesign.BodyFontFamily,
            )
        }
        // 右上：页码小标签（12sp + 10sp）
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "${pageIndex + 1}",
                fontSize = 12.sp,
                color = GoaldayDesign.adaptiveInkPrimary,
                fontFamily = GoaldayDesign.BodyFontFamily,
            )
            Text(
                "/$pageCount",
                fontSize = 10.sp,
                color = GoaldayDesign.adaptiveInkMuted,
                fontFamily = GoaldayDesign.BodyFontFamily,
            )
        }
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
            .clip(RoundedCornerShape(GoaldayDesign.RadiusS))
            .background(color)
            .padding(horizontal = GoaldayDesign.Space1 + 2.dp, vertical = 2.dp),
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
            .background(if (activeDrop) GoaldayDesign.GreenSoft else GoaldayDesign.adaptiveSurface.copy(alpha = 0.26f), RoundedCornerShape(GoaldayDesign.RadiusS))
            .border(if (activeDrop) 0.9.dp else 0.45.dp, if (activeDrop) GoaldayDesign.Positive else GoaldayDesign.BorderColor.copy(alpha = 0.09f), RoundedCornerShape(GoaldayDesign.RadiusS))
            .onGloballyPositioned { coordinates -> onBounds(coordinates.boundsInRoot()) }
            .padding(horizontal = GoaldayDesign.Space1 + 2.dp, vertical = GoaldayDesign.Space1),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("${day}日", style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.adaptiveInkPrimary, fontWeight = FontWeight.SemiBold)
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
                    activeDrop && index == 0 -> "释放放入已完成"
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
            .clip(RoundedCornerShape(GoaldayDesign.RadiusS))
            .background(GoaldayDesign.Positive.copy(alpha = 0.09f))
            .border(0.55.dp, GoaldayDesign.Positive.copy(alpha = 0.26f), RoundedCornerShape(GoaldayDesign.RadiusS))
            .clickable(onClick = onToggleCompleted),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space1 + 1.dp),
    ) {
        Text(
            "${day}日",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            modifier = Modifier
                .width(30.dp)
                .fillMaxHeight()
                .background(GoaldayDesign.Positive)
                .padding(top = GoaldayDesign.Space1 + 1.dp),
            textAlign = TextAlign.Center,
        )
        Icon(
            imageVector = Icons.Filled.Check,
            contentDescription = "已完成",
            modifier = Modifier.size(14.dp),
            tint = GoaldayDesign.Positive,
        )
        if (entry.timeText.isNotBlank()) {
            Text(
                entry.timeText,
                style = MaterialTheme.typography.labelSmall,
                color = GoaldayDesign.adaptiveInkMuted,
                maxLines = 1,
                modifier = Modifier
                    .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
                    .background(GoaldayDesign.adaptiveSurface.copy(alpha = 0.64f))
                    .padding(horizontal = GoaldayDesign.Space1, vertical = 1.dp),
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
                    .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
                    .background(GoaldayDesign.adaptiveSurface.copy(alpha = 0.64f))
                    .padding(horizontal = GoaldayDesign.Space1, vertical = 1.dp),
            )
        }
        Text(
            entry.title,
            style = MaterialTheme.typography.bodySmall,
            color = GoaldayDesign.adaptiveInkMuted,
            textDecoration = TextDecoration.LineThrough,
            maxLines = 1,
            modifier = Modifier.weight(1f),
        )
        Text(
            "已完成",
            style = MaterialTheme.typography.labelSmall,
            color = GoaldayDesign.Positive,
            maxLines = 1,
            modifier = Modifier.padding(end = GoaldayDesign.Space1 + 1.dp),
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
            .clip(RoundedCornerShape(GoaldayDesign.RadiusS))
            .background(GoaldayDesign.adaptiveSurface.copy(alpha = 0.06f))
            .border(0.45.dp, GoaldayDesign.Positive.copy(alpha = 0.09f), RoundedCornerShape(GoaldayDesign.RadiusS)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space1 + 1.dp),
    ) {
        Text(
            "${day}日",
            style = MaterialTheme.typography.labelSmall,
            color = GoaldayDesign.Positive,
            modifier = Modifier.width(30.dp),
            textAlign = TextAlign.Center,
        )
        Icon(
            imageVector = Icons.Filled.Check,
            contentDescription = "已完成",
            modifier = Modifier.size(14.dp),
            tint = GoaldayDesign.Positive.copy(alpha = 0.74f),
        )
        Text(
            title,
            style = MaterialTheme.typography.bodySmall,
            color = GoaldayDesign.adaptiveInkMuted,
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
    repeatLabel: String,
    onPickRepeat: () -> Unit,
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
            .background(GoaldayDesign.PinkSoft.copy(alpha = 0.21f))
            .border(0.45.dp, GoaldayDesign.Pink.copy(alpha = 0.13f), RoundedCornerShape(GoaldayDesign.RadiusS))
            .padding(horizontal = GoaldayDesign.Space1 + 2.dp, vertical = GoaldayDesign.Space1 + 1.dp),
        verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space1),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("待安排池", style = MaterialTheme.typography.labelMedium, color = GoaldayDesign.adaptiveInkPrimary, fontWeight = FontWeight.SemiBold)
            Text("点选或长按拖入日期", style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.adaptiveInkMuted)
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            textStyle = MaterialTheme.typography.bodySmall.copy(color = GoaldayDesign.adaptiveInkPrimary),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { submitAndKeepFocus() }),
            decorationBox = { inner ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(GoaldayDesign.RadiusS))
                        .background(GoaldayDesign.adaptiveSurface.copy(alpha = 0.53f))
                        .border(GoaldayDesign.Hairline, GoaldayDesign.adaptiveDivider, RoundedCornerShape(GoaldayDesign.RadiusS))
                        .padding(horizontal = GoaldayDesign.Space1 + 2.dp, vertical = GoaldayDesign.Space1),
                    horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space1 + 1.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(12.dp), tint = GoaldayDesign.adaptiveInkSecondary)
                    Box(Modifier.weight(1f)) {
                        if (value.isBlank()) {
                            Text("写入计划", style = MaterialTheme.typography.bodySmall, color = GoaldayDesign.adaptiveInkMuted)
                        }
                        inner()
                    }
                    Text("入池", style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.adaptiveInkSecondary, modifier = Modifier.clickable(onClick = ::addToPoolAndKeepFocus))
                    Text("排入", style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.adaptiveInkSecondary, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable(onClick = ::submitAndKeepFocus))
                }
            },
        )
        Row(horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space1), verticalAlignment = Alignment.CenterVertically) {
            days.forEach { day ->
                Text(
                    "${day}日",
                    color = if (day == selectedDay) Color.White else GoaldayDesign.adaptiveInkSecondary,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                        .background(if (day == selectedDay) GoaldayDesign.Pink else GoaldayDesign.adaptiveDivider, RoundedCornerShape(GoaldayDesign.RadiusPill))
                        .clickable { onSelectDay(day) }
                        .padding(horizontal = GoaldayDesign.Space1 + 2.dp, vertical = 2.dp),
                )
            }
            // 对照逆向 pop_repeat.xml：重复模式选择按钮
            Text(
                repeatLabel.ifBlank { "不重复" },
                color = if (repeatLabel.isBlank()) GoaldayDesign.adaptiveInkSecondary else GoaldayDesign.Pink,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (repeatLabel.isBlank()) FontWeight.Normal else FontWeight.SemiBold,
                modifier = Modifier
                    .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
                    .background(if (repeatLabel.isBlank()) GoaldayDesign.adaptiveDivider else GoaldayDesign.PinkTint)
                    .clickable { onPickRepeat() }
                    .padding(horizontal = GoaldayDesign.Space1 + 2.dp, vertical = 2.dp),
            )
        }
        if (poolItems.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                poolItems.forEach { item ->
                    var rowOrigin by remember(item) { mutableStateOf(Offset.Zero) }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(GoaldayDesign.RadiusS))
                            .background(GoaldayDesign.adaptiveSurface.copy(alpha = 0.40f))
                            .border(GoaldayDesign.Hairline, GoaldayDesign.adaptiveDivider, RoundedCornerShape(GoaldayDesign.RadiusS))
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
                            .padding(horizontal = GoaldayDesign.Space1 + 1.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space1),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.RadioButtonUnchecked,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = GoaldayDesign.adaptiveInkMuted,
                        )
                        Text(item, style = MaterialTheme.typography.bodySmall, color = GoaldayDesign.adaptiveInkPrimary, maxLines = 1, modifier = Modifier.weight(1f))
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "移除",
                            modifier = Modifier
                                .size(14.dp)
                                .clickable { onRemovePoolItem(item) },
                            tint = GoaldayDesign.adaptiveInkMuted,
                        )
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
    val visibleEntries = entries.take(6)
    val slots = List(6) { index -> visibleEntries.getOrNull(index) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(InBookScheduleSlotHeight * 3 + InBookScheduleColumnPaddingVertical * 2)
            .background(if (activeDrop) GoaldayDesign.PinkSoft else Color.Transparent, RoundedCornerShape(GoaldayDesign.RadiusS))
            .border(if (activeDrop) 0.9.dp else 0.dp, if (activeDrop) GoaldayDesign.Pink else Color.Transparent, RoundedCornerShape(GoaldayDesign.RadiusS))
            .onGloballyPositioned { coordinates -> onBounds(coordinates.boundsInRoot()) },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 对照逆向 item_schedule_item_in_book.xml: FrameLayout layout_width=24.5dip = 24.5dp
        Column(
            modifier = Modifier
                .width(24.5.dp)
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                day.toString().padStart(2, '0'),
                style = MaterialTheme.typography.labelSmall,
                color = GoaldayDesign.adaptiveInkPrimary,
                fontWeight = FontWeight.Normal,
                maxLines = 1,
            )
            Text("—", style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.ScheduleDateColumnSeparator, maxLines = 1)
            Text(
                weekdayLabel(day, visibleDays),
                style = MaterialTheme.typography.labelSmall,
                color = GoaldayDesign.adaptiveInkMuted,
                maxLines = 1,
            )
        }
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                // 对照 item_schedule_item_in_book.xml: paddingVertical=3.5dip
                .padding(vertical = InBookScheduleColumnPaddingVertical),
        ) {
            repeat(2) { columnIndex ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                ) {
                    repeat(3) { rowIndex ->
                        val slotIndex = columnIndex * 3 + rowIndex
                        ReferenceScheduleTargetSlot(
                            day = day,
                            entry = slots[slotIndex],
                            visibleDays = visibleDays,
                            editingId = editingId,
                            editingText = editingText,
                            activeDrop = activeDrop && slotIndex == visibleEntries.size.coerceIn(0, 5),
                            onStartEdit = onStartEdit,
                            onTextChange = onTextChange,
                            onCommit = onCommit,
                            onToggleCompleted = onToggleCompleted,
                            onMoveEntryToDay = onMoveEntryToDay,
                            onEntryDragStart = onEntryDragStart,
                            onEntryDrag = onEntryDrag,
                            onEntryDragEnd = onEntryDragEnd,
                            onEntryDragCancel = onEntryDragCancel,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReferenceScheduleTargetSlot(
    day: Int,
    entry: ScheduleEntry?,
    visibleDays: List<Int>,
    editingId: String?,
    editingText: String,
    activeDrop: Boolean,
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
    if (entry == null) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                // 对照 item_schedule_item_in_book.xml: layout_height=12dip
                .height(InBookScheduleSlotHeight)
                .padding(horizontal = 2.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            if (activeDrop) {
                Text("释放到${day}日", style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.Pink, maxLines = 1)
            } else {
                Box(Modifier.fillMaxWidth().height(0.55.dp).background(GoaldayDesign.adaptiveDivider.copy(alpha = 0.55f)))
            }
        }
        return
    }

    val dayIndex = visibleDays.indexOf(entry.day)
    if (editingId == entry.id) {
        BasicTextField(
            value = editingText,
            onValueChange = onTextChange,
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onCommit(entry) }),
            modifier = Modifier
                .fillMaxWidth()
                // 对照 item_schedule_item_in_book.xml: layout_height=12dip
                .height(InBookScheduleSlotHeight)
                .padding(horizontal = 2.dp)
                .border(0.7.dp, GoaldayDesign.Pink.copy(alpha = 0.45f), RoundedCornerShape(GoaldayDesign.RadiusS))
                .padding(horizontal = 3.dp, vertical = 1.dp),
            textStyle = MaterialTheme.typography.bodySmall.copy(color = GoaldayDesign.adaptiveInkPrimary),
        )
    } else {
        // 对照 item_schedule_item_in_book.xml：cb_target visibility=gone，不显示勾选框；
        // et_target 使用 ContentTextView + FontUtils contentSize；isInBook 时减半为 8sp。
        Row(
            modifier = Modifier
                .fillMaxWidth()
                // 对照 item_schedule_item_in_book.xml: layout_height=12dip
                .height(InBookScheduleSlotHeight)
                .padding(horizontal = 2.dp)
                .clip(RoundedCornerShape(GoaldayDesign.RadiusS))
                .background(if (entry.completed) GoaldayDesign.Positive.copy(alpha = 0.07f) else Color.Transparent)
                .clickable { onToggleCompleted(entry) }
                .padding(horizontal = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                entry.title,
                fontSize = 8.sp,
                color = if (entry.completed) GoaldayDesign.adaptiveInkMuted else GoaldayDesign.adaptiveInkPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textDecoration = if (entry.completed) TextDecoration.LineThrough else TextDecoration.None,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

private fun weekdayLabel(day: Int, visibleDays: List<Int>): String {
    val labels = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
    val index = visibleDays.indexOf(day).takeIf { it >= 0 } ?: 0
    return labels[index % labels.size]
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
        horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space1),
    ) {
        Text(
            label.ifBlank { " " },
            style = MaterialTheme.typography.labelSmall,
            color = if (highlight) GoaldayDesign.Pink else GoaldayDesign.adaptiveInkMuted,
            maxLines = 1,
            modifier = Modifier.width(54.dp),
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(if (highlight) 1.2.dp else 0.6.dp)
                .background(if (highlight) GoaldayDesign.Pink else GoaldayDesign.adaptiveDivider),
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
            .clip(RoundedCornerShape(GoaldayDesign.RadiusS))
            .background(if (entry.completed) GoaldayDesign.Positive.copy(alpha = 0.09f) else GoaldayDesign.adaptiveSurface.copy(alpha = 0.09f))
            .border(0.55.dp, statusColor.copy(alpha = if (entry.completed) 0.28f else 0.18f), RoundedCornerShape(GoaldayDesign.RadiusS))
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
            horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space1 + 1.dp),
        ) {
        Text(
            "${slotLabel}日",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            modifier = Modifier
                .width(30.dp)
                .fillMaxHeight()
                .background(statusColor)
                .padding(top = GoaldayDesign.Space1 + 2.dp),
            textAlign = TextAlign.Center,
        )
        Icon(
            imageVector = if (entry.completed) Icons.Filled.Check else Icons.Filled.RadioButtonUnchecked,
            contentDescription = if (entry.completed) "已完成" else "未完成",
            modifier = Modifier
                .size(18.dp)
                .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
                .background(GoaldayDesign.adaptiveSurface.copy(alpha = 0.72f))
                .clickable { onToggleCompleted() }
                .padding(2.dp),
            tint = statusColor,
        )
        if (editingId == entry.id) {
            BasicTextField(
                value = editingText,
                onValueChange = onTextChange,
                textStyle = TextStyle(color = GoaldayDesign.adaptiveInkPrimary),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    onCommit()
                    focusManager.clearFocus(force = true)
                }),
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(rowEditorFocus)
                    .background(GoaldayDesign.adaptiveSurface.copy(alpha = 0.53f), RoundedCornerShape(GoaldayDesign.RadiusS))
                    .padding(horizontal = GoaldayDesign.Space1 + 1.dp, vertical = GoaldayDesign.Space1 - 1.dp),
            )
            Text("完成", style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.adaptiveInkSecondary, modifier = Modifier.clickable {
                onCommit()
                focusManager.clearFocus(force = true)
            })
        } else {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onStartEdit() }
                    .padding(end = GoaldayDesign.Space1),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space1),
            ) {
                if (entry.timeText.isNotBlank()) {
                    Text(
                        entry.timeText,
                        style = MaterialTheme.typography.labelSmall,
                        color = GoaldayDesign.adaptiveInkMuted,
                        maxLines = 1,
                        modifier = Modifier
                            .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
                            .background(GoaldayDesign.adaptiveWhiteOverlayMedium)
                            .padding(horizontal = GoaldayDesign.Space1, vertical = 1.dp),
                    )
                }
                if (repeatLabel.isNotBlank()) {
                    Text(
                        repeatLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        maxLines = 1,
                        modifier = Modifier
                            .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
                            .background(GoaldayDesign.adaptiveWhiteOverlayMedium)
                            .padding(horizontal = GoaldayDesign.Space1, vertical = 1.dp),
                    )
                }
                Text(
                    entry.title,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (entry.completed) GoaldayDesign.adaptiveInkSecondary else GoaldayDesign.adaptiveInkPrimary,
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
                    .padding(start = 35.dp, end = GoaldayDesign.Space2 - 1.dp, bottom = GoaldayDesign.Space1 + 1.dp),
                verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space1 - 1.dp),
            ) {
                if (repeatLabel.isNotBlank()) {
                    HandbookEntryDetailChip("repeat", repeatLabel, statusColor)
                }
                if (entry.timeText.isNotBlank()) {
                    HandbookEntryDetailChip("time", entry.timeText, GoaldayDesign.adaptiveInkSecondary)
                }
                if (entry.note.isNotBlank()) {
                    HandbookEntryDetailChip("note", entry.note, GoaldayDesign.adaptiveInkMuted)
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
            .background(GoaldayDesign.adaptiveSurface.copy(alpha = 0.58f))
            .border(0.35.dp, color.copy(alpha = 0.16f), RoundedCornerShape(GoaldayDesign.RadiusS))
            .padding(horizontal = GoaldayDesign.Space1 + 2.dp, vertical = GoaldayDesign.Space1 - 1.dp),
        horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space1 + 2.dp),
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
            color = GoaldayDesign.adaptiveInkSecondary,
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
        color = if (enabled) GoaldayDesign.Pink else GoaldayDesign.adaptiveInkMuted.copy(alpha = 0.38f),
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
            .background(if (enabled) GoaldayDesign.adaptiveSurface.copy(alpha = 0.66f) else Color.Transparent)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = GoaldayDesign.Space1, vertical = 1.dp),
    )
}

// 对照逆向 pop_repeat.xml：5 选项重复模式弹窗，左侧勾选图标 + 右侧标题
@Composable
private fun RepeatModePickerDialog(
    selectedRule: String,
    onDismiss: () -> Unit,
    onSelect: (String, Int) -> Unit,
) {
    val options = listOf(
        Triple("", "不重复", 1),
        Triple("daily", "每天重复", 1),
        Triple("weekly", "每周重复", 1),
        Triple("monthly", "每月重复", 1),
        Triple("yearly", "每年重复", 1),
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("重复模式", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                options.forEach { (rule, label, interval) ->
                    val selected = selectedRule == rule
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space2),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(RoundedCornerShape(GoaldayDesign.RadiusS))
                            .clickable {
                                onSelect(rule, interval)
                                onDismiss()
                            }
                            .padding(horizontal = GoaldayDesign.Space2),
                    ) {
                        Icon(
                            if (selected) Icons.Filled.Check else Icons.Filled.RadioButtonUnchecked,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = if (selected) GoaldayDesign.Pink else GoaldayDesign.adaptiveInkMuted,
                        )
                        Text(
                            label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (selected) GoaldayDesign.Pink else GoaldayDesign.adaptiveInkPrimary,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        },
    )
}

// 对照逆向 item_schedule_item_in_book.xml：
// 24.5dp 日期列 + 2列(weight=1) x 3行(12dp) 目标槽
@Composable
private fun ScheduleDayRow(
    day: Int,
    month: Int,
    year: Int,
    entries: List<ScheduleEntry>,
    onToggleCompleted: (String) -> Unit = {},
    onAddSchedule: (String) -> Unit = {},
) {
    val dayDate = try { LocalDate.of(year, month, day) } catch (_: Exception) { null }
    val weekdayNames = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
    val weekdayLabel = dayDate?.dayOfWeek?.value?.let { weekdayNames.getOrNull(it - 1) } ?: ""
    // 对齐原版 item_schedule_item_in_book.xml：日期显示为两位数，补零（如 "01"）
    val dayStr = day.toString().padStart(2, '0')
    // 分成2列，每列最多3条
    val leftEntries = entries.take(3)
    val rightEntries = entries.drop(3).take(3)
    Row(
        // 对照 item_schedule_item_in_book.xml: 根 LinearLayout 高度为 match_parent，
        // 行高由 3 个 12dip 槽 + 上下 3.5dip padding 决定，约 43dp。
        modifier = Modifier.fillMaxWidth(),
    ) {
        // 左侧日期列（对照逆向 item_schedule_item_in_book.xml: FrameLayout layout_width=24.5dip = 24.5dp）：
        // 日期 9sp 在上半区底部（marginBottom=2dp），"—" 9sp 垂直居中，周几 6sp 在下半区顶部（marginTop=2dp）
        Box(
            modifier = Modifier.width(24.5.dp).fillMaxHeight(),
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // 上半区：日期贴底部，距中线 2dp
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    Text(
                        dayStr,
                        fontSize = 9.sp,
                        color = GoaldayDesign.adaptiveInkPrimary,
                        modifier = Modifier.padding(bottom = 2.dp),
                    )
                }
                // 垂直居中的分隔符
                Text(
                    "—",
                    fontSize = 9.sp,
                    color = GoaldayDesign.ScheduleDateColumnSeparator,
                )
                // 下半区：周几贴顶部，距中线 2dp
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    Text(
                        weekdayLabel,
                        fontSize = 6.sp,
                        color = GoaldayDesign.adaptiveInkMuted,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }
        }
        // 第一列（weight=1, paddingVertical=1.75dip=1.75dp）
        ScheduleTargetColumn(
            entries = leftEntries,
            modifier = Modifier.weight(1f),
        )
        // 第二列（weight=1, paddingVertical=1.75dip=1.75dp）
        ScheduleTargetColumn(
            entries = rightEntries,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ScheduleTargetColumn(
    entries: List<ScheduleEntry>,
    onToggleCompleted: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    // 对照 item_schedule_item_in_book.xml: paddingVertical=3.5dip
    Column(
        modifier = modifier.padding(vertical = InBookScheduleColumnPaddingVertical),
    ) {
        for (i in 0 until 3) {
            val entry = entries.getOrNull(i)
            // 对照逆向 item_schedule_item_in_book.xml: layout_height=12dip
            ScheduleTargetSlot(
                entry = entry,
                onToggleCompleted = onToggleCompleted,
                modifier = Modifier.height(InBookScheduleSlotHeight),
            )
        }
    }
}

@Composable
private fun ScheduleTargetSlot(
    entry: ScheduleEntry?,
    onToggleCompleted: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    // 对照 item_schedule_item_in_book.xml：
    // cb_target visibility=gone，因此书页日程槽不显示勾选框；NoTouch 阅读态，不响应点击。
    // et_target 使用 ContentTextView + FontUtils contentSize；默认 mode=1 时 contentSize=16dp，isInBook 减半为 8sp。
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = entry?.title ?: "",
            // 对照 ContentTextView：isInBook=true 时 contentSize 16dp 减半为 8sp
            fontSize = 8.sp,
            lineHeight = 8.sp,
            color = if (entry?.completed == true) GoaldayDesign.adaptiveInkMuted else GoaldayDesign.adaptiveInkPrimary,
            textDecoration = if (entry?.completed == true) TextDecoration.LineThrough else TextDecoration.None,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 2.dp),
        )
    }
}
