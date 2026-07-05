package com.bf410.goaldaylocal.ui.book

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bf410.goaldaylocal.data.BookPage
import com.bf410.goaldaylocal.data.DiaryPage
import com.bf410.goaldaylocal.data.PlanPage
import com.bf410.goaldaylocal.data.ReverseTopicConfigParser
import com.bf410.goaldaylocal.data.ScheduleEntry
import com.bf410.goaldaylocal.data.SchedulePage
import com.bf410.goaldaylocal.data.TargetPage
import com.bf410.goaldaylocal.ui.replica.GoaldayDesign
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.WeekFields
import java.util.Locale
import kotlin.math.abs

internal enum class LocalHandbookSegment(val label: String) {
    PLAN("计划"), SCHEDULE("日程"), DIARY("日记"), TOPICS("主题")
}

private enum class HandbookOverlay { NONE, SETTINGS, MONTH_PICKER, BOOK_OVERVIEW, BOOK_SHELF, EXPORT_OPTIONS }

@Composable
internal fun LocalHandbookWorkspace(
    viewModel: BookViewModel,
    uiState: BookUiState,
    initialSegment: LocalHandbookSegment,
    onBack: () -> Unit,
) {
    val book = uiState.books.getOrNull(uiState.selectedBookIndex.coerceIn(0, uiState.books.lastIndex)) ?: return
    var segment by rememberSaveable(book.id) { mutableStateOf(initialSegment) }
    var overlay by rememberSaveable { mutableStateOf(HandbookOverlay.NONE) }
    var focusedDate by rememberSaveable { mutableStateOf(LocalDate.now()) }
    var overviewWeekOffset by rememberSaveable { mutableStateOf(0) }
    var shelfYear by rememberSaveable { mutableStateOf(LocalDate.now().year) }

    LaunchedEffect(initialSegment) {
        segment = initialSegment
    }
    LaunchedEffect(book.id, segment) {
        val targetType: (BookPage) -> Boolean = when (segment) {
            LocalHandbookSegment.PLAN -> { page -> page is PlanPage || page is TargetPage }
            LocalHandbookSegment.SCHEDULE -> { page -> page is SchedulePage || page is PlanPage || page is TargetPage }
            LocalHandbookSegment.DIARY -> { page -> page is DiaryPage }
            LocalHandbookSegment.TOPICS -> { page -> page is TargetPage || page is PlanPage }
        }
        val index = book.pages.indexOfFirst(targetType)
        if (index >= 0 && index != uiState.selectedPageIndex) viewModel.setPage(index)
    }

    Box(Modifier.fillMaxSize().background(GoaldayDesign.AppBg)) {
        Column(Modifier.fillMaxSize()) {
            ReferenceExactTopBar(
                selected = segment,
                date = focusedDate,
                weekOffset = overviewWeekOffset,
                onSelectPlan = { overlay = HandbookOverlay.NONE; segment = LocalHandbookSegment.PLAN },
                onSelectSchedule = { overlay = HandbookOverlay.NONE; segment = LocalHandbookSegment.SCHEDULE },
                onSelectDiary = { overlay = HandbookOverlay.NONE; segment = LocalHandbookSegment.DIARY },
                onSelectTopics = { overlay = HandbookOverlay.NONE; segment = LocalHandbookSegment.TOPICS },
            )
            Box(Modifier.weight(1f)) {
                when (segment) {
                    LocalHandbookSegment.DIARY -> ReferenceDiaryPage(
                        draft = uiState.diaryDraft,
                        date = focusedDate,
                        onDateChange = { focusedDate = it },
                        onDraftChange = viewModel::updateDiaryDraft,
                    )
                    LocalHandbookSegment.SCHEDULE -> ReferenceWeekPage(
                        entries = uiState.schedulePreviewEntries,
                        poolItems = uiState.todayPlanItems,
                        completedItems = uiState.todayCompletedItems,
                        anchorDate = focusedDate.plusWeeks(overviewWeekOffset.toLong()),
                        onAddPool = viewModel::addHandbookPoolItem,
                        onAddSchedule = { title, day -> viewModel.addScheduleFromHandbook(title, focusedDate.monthValue, day) },
                        onToggleEntry = viewModel::toggleScheduleCompletedFromHandbook,
                    )
                    LocalHandbookSegment.PLAN -> ReferenceListPage(
                        bookTitle = book.title,
                        items = collectListItems(book.pages.getOrNull(uiState.selectedPageIndex), uiState),
                        completed = uiState.todayCompletedItems,
                        onAdd = viewModel::addCustomPageItem,
                        onToggle = { item -> if (item in uiState.todayCompletedItems) viewModel.restoreItemFromCompleted(item) else viewModel.moveItemToCompleted(item) },
                    )
                                    LocalHandbookSegment.TOPICS -> ReferenceTopicsPage(
                        onApply = viewModel::applyInspirationTemplate,
                        onSaveAsBook = { template, items -> viewModel.createTemplateBook(template.title, template.subtitle, template.color, items) },
                    )
}
                ReferenceBottomBookBar(
                    onSettings = { overlay = HandbookOverlay.SETTINGS },
                    onCalendar = { overlay = HandbookOverlay.MONTH_PICKER },
                    onBook = { overlay = HandbookOverlay.BOOK_OVERVIEW },
                )
            }
        }
        when (overlay) {
            HandbookOverlay.NONE -> Unit
            HandbookOverlay.SETTINGS -> ReferenceSettingsPanel { overlay = HandbookOverlay.NONE }
            HandbookOverlay.MONTH_PICKER -> ReferenceMonthPicker(focusedDate, { focusedDate = it; overlay = HandbookOverlay.NONE }, { overlay = HandbookOverlay.NONE })
            HandbookOverlay.BOOK_OVERVIEW -> ReferenceBookOverview(
                date = focusedDate,
                weekOffset = overviewWeekOffset,
                entries = uiState.schedulePreviewEntries,
                diaryDraft = uiState.diaryDraft,
                listItems = collectListItems(book.pages.getOrNull(uiState.selectedPageIndex), uiState),
                onOpenDiary = { focusedDate = it; segment = LocalHandbookSegment.DIARY; overlay = HandbookOverlay.NONE },
                onOpenWeek = { overviewWeekOffset = it; segment = LocalHandbookSegment.SCHEDULE; overlay = HandbookOverlay.NONE },
                onChangeWeek = { overviewWeekOffset += it },
                onExport = { overlay = HandbookOverlay.EXPORT_OPTIONS },
                onShelf = { shelfYear = focusedDate.year; overlay = HandbookOverlay.BOOK_SHELF },
                onReturn = { overlay = HandbookOverlay.NONE },
            )
            HandbookOverlay.BOOK_SHELF -> ReferenceBookShelf(shelfYear, { focusedDate = focusedDate.withYear(it); overlay = HandbookOverlay.BOOK_OVERVIEW }, { overlay = HandbookOverlay.BOOK_OVERVIEW })
            HandbookOverlay.EXPORT_OPTIONS -> ReferenceExportOptions({ overlay = HandbookOverlay.BOOK_OVERVIEW }, { overlay = HandbookOverlay.MONTH_PICKER }, { overlay = HandbookOverlay.MONTH_PICKER })
        }
    }
}

@Composable
private fun ReferenceExactTopBar(
    selected: LocalHandbookSegment,
    date: LocalDate,
    weekOffset: Int,
    onSelectPlan: () -> Unit,
    onSelectSchedule: () -> Unit,
    onSelectDiary: () -> Unit,
    onSelectTopics: () -> Unit,
) {
    val scheduleLabel = if (selected == LocalHandbookSegment.SCHEDULE) "${weekNumber(date.plusWeeks(weekOffset.toLong()))}周" else "日程"
    val diaryLabel = if (selected == LocalHandbookSegment.DIARY) "${date.monthValue}月${date.dayOfMonth}日" else "日记"
    Row(
        modifier = Modifier.fillMaxWidth().height(49.dp).background(GoaldayDesign.MorandiBrownLight).padding(horizontal = 26.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TopText("计划", selected == LocalHandbookSegment.PLAN, onSelectPlan)
        TopText(scheduleLabel, selected == LocalHandbookSegment.SCHEDULE, onSelectSchedule)
        TopText(diaryLabel, selected == LocalHandbookSegment.DIARY, onSelectDiary)
        TopText("主题", selected == LocalHandbookSegment.TOPICS, onSelectTopics)
    }
}

@Composable
private fun TopText(text: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        text = text,
        color = if (selected) GoaldayDesign.InkPrimary else GoaldayDesign.InkSecondary,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        modifier = Modifier.pointerInput(onClick) { detectTapGestures { onClick() } },
    )
}

@Composable
private fun ReferenceDiaryPage(draft: String, date: LocalDate, onDateChange: (LocalDate) -> Unit, onDraftChange: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GoaldayDesign.AppBg)
            .pointerInput(date) {
                detectDragGestures { change, drag ->
                    if (abs(drag.y) > abs(drag.x) && abs(drag.y) > 24f) {
                        onDateChange(if (drag.y < 0f) date.plusDays(1) else date.minusDays(1))
                        change.consume()
                    }
                }
            },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                "${date.monthValue}月${date.dayOfMonth}日 ${date.dayOfWeek.displayName()}",
                color = GoaldayDesign.InkPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
            )
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(start = 10.dp, top = 5.dp, end = 10.dp, bottom = 0.dp)
                .referencePaperLines(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            ) {
                BasicTextField(
                    value = draft,
                    onValueChange = onDraftChange,
                    textStyle = TextStyle(color = Color(0xFF2C2C2C), fontSize = 16.sp, lineHeight = 22.sp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp, bottom = 24.dp),
                    decorationBox = { inner ->
                        if (draft.isBlank()) Text("点击输入", color = GoaldayDesign.InkMuted, fontSize = 16.sp, lineHeight = 22.sp)
                        inner()
                    },
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(31.dp)
                .background(Color.White),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .padding(start = 5.dp)
                    .size(31.dp)
                    .clickable { if (draft.isBlank()) onDraftChange(randomPrompt(date)) },
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(17.dp)
                        .border(1.dp, GoaldayDesign.InkSecondary, RoundedCornerShape(2.dp)),
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 6.dp, bottom = 6.dp)
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(GoaldayDesign.InkSecondary),
                )
            }
        }
    }
}

@Composable
private fun ReferenceWeekPage(
    entries: List<ScheduleEntry>,
    poolItems: List<String>,
    completedItems: List<String>,
    anchorDate: LocalDate,
    onAddPool: (String) -> Unit,
    onAddSchedule: (String, Int) -> Unit,
    onToggleEntry: (String) -> Unit,
) {
    val monday = anchorDate.minusDays((anchorDate.dayOfWeek.value - 1).toLong())
    val days = (0..6).map { monday.plusDays(it.toLong()) }
    var draft by rememberSaveable { mutableStateOf("") }
    var selectedDay by rememberSaveable(anchorDate) { mutableStateOf(anchorDate.dayOfMonth) }
    Column(Modifier.fillMaxSize().background(GoaldayDesign.AppBg).padding(bottom = 48.dp)) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            days.forEach { day ->
                ScheduleLine(
                    day = day,
                    entries = entries.filter { it.month == day.monthValue && it.day == day.dayOfMonth },
                    completedItems = completedItems,
                    onSelect = { selectedDay = day.dayOfMonth },
                    onToggleEntry = onToggleEntry,
                )
            }
            poolItems.distinct().forEach { item ->
                Row(Modifier.fillMaxWidth().height(34.dp).padding(horizontal = 42.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(item, color = GoaldayDesign.InkPrimary, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    Text("排到${selectedDay}日", color = GoaldayDesign.InkSecondary, style = MaterialTheme.typography.labelSmall, modifier = Modifier.clickable { onAddSchedule(item, selectedDay) })
                }
            }
        }
        BasicTextField(
            value = draft,
            onValueChange = { draft = it },
            textStyle = TextStyle(color = GoaldayDesign.InkPrimary, fontSize = 14.sp),
            modifier = Modifier.fillMaxWidth().height(44.dp).padding(horizontal = 42.dp),
            decorationBox = { inner ->
                if (draft.isBlank()) Text("写一件待安排的事", color = GoaldayDesign.InkMuted, style = MaterialTheme.typography.bodyMedium)
                inner()
            },
        )
        if (draft.isNotBlank()) Text("加入", color = GoaldayDesign.InkPrimary, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(horizontal = 42.dp).clickable { onAddPool(draft); draft = "" })
    }
}

@Composable
private fun ReferenceListPage(bookTitle: String, items: List<String>, completed: List<String>, onAdd: (String) -> Unit, onToggle: (String) -> Unit) {
    var draft by rememberSaveable { mutableStateOf("") }
    var showAddSheet by rememberSaveable { mutableStateOf(false) }
    var bindSchedule by rememberSaveable { mutableStateOf(false) }
    var selectedColorIndex by rememberSaveable { mutableStateOf(0) }
    var showTipRows by rememberSaveable { mutableStateOf(false) }
    val planColors = listOf(Color(0xFFF2C0A5), Color(0xFFA1B774), Color(0xFF9EAADB), Color(0xFFF1D179), Color(0xFFED8888))
    val visibleItems = (items.distinct().take(100)).ifEmpty { listOf(bookTitle.ifBlank { "2026年愿望清单" }) }

    Box(Modifier.fillMaxSize().background(GoaldayDesign.AppBg)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, top = 11.dp, end = 20.dp, bottom = 108.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            visibleItems.forEachIndexed { index, item ->
                ReferencePlanRow(
                    title = item,
                    count = if (item in completed) "1/1" else "0/1",
                    color = planColors[index % planColors.size],
                    completed = item in completed,
                    onClick = { onToggle(item) },
                )
            }
            if (showTipRows) {
                listOf("左侧圆点对应清单颜色", "点击条目切换完成状态", "右下按钮新增或查看提示").forEachIndexed { index, tip ->
                    ReferencePlanRow(
                        title = tip,
                        count = "提示",
                        color = Color.Black,
                        completed = false,
                        onClick = {},
                    )
                }
            }
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(13.dp),
            horizontalAlignment = Alignment.End,
        ) {
            FloatingPlanAction("+", tint = planColors[selectedColorIndex]) { showAddSheet = true }
            FloatingPlanAction("?", tint = Color.Black) { showTipRows = !showTipRows }
        }

        if (showAddSheet) {
            ReferencePlanAddSheet(
                draft = draft,
                bindSchedule = bindSchedule,
                colors = planColors,
                selectedColorIndex = selectedColorIndex,
                onDraftChange = { draft = it },
                onBindScheduleChange = { bindSchedule = it },
                onColorSelect = { selectedColorIndex = it },
                onCancel = { showAddSheet = false; draft = "" },
                onComplete = {
                    if (draft.isNotBlank()) onAdd(draft)
                    showAddSheet = false
                    draft = ""
                },
            )
        }
    }
}

@Composable
private fun ReferencePlanRow(title: String, count: String, color: Color, completed: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(65.dp)
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(start = 20.dp, end = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(13.dp)
                .clip(CircleShape)
                .background(color),
        )
        Text(
            title,
            color = if (completed) GoaldayDesign.InkMuted else GoaldayDesign.InkPrimary,
            fontSize = 21.sp,
            textDecoration = if (completed) TextDecoration.LineThrough else TextDecoration.None,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .padding(start = 21.dp, top = 2.dp, bottom = 2.dp),
        )
        Text(count, color = GoaldayDesign.InkSecondary, fontSize = 18.sp, maxLines = 1)
    }
}

@Composable
private fun ColumnScope.FloatingPlanAction(label: String, tint: Color, onClick: () -> Unit) {
    Box(
        Modifier.align(Alignment.End).padding(end = 18.dp, bottom = 12.dp).size(48.dp).shadow(6.dp, CircleShape).clip(CircleShape).background(tint).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ReferencePlanAddSheet(
    draft: String,
    bindSchedule: Boolean,
    colors: List<Color>,
    selectedColorIndex: Int,
    onDraftChange: (String) -> Unit,
    onBindScheduleChange: (Boolean) -> Unit,
    onColorSelect: (Int) -> Unit,
    onCancel: () -> Unit,
    onComplete: () -> Unit,
) {
    Box(Modifier.fillMaxSize().background(Color(0x66000000)).clickable(onClick = onCancel), contentAlignment = Alignment.BottomCenter) {
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)).background(Color(0xFFFFFEFC)).clickable(enabled = false) { }.padding(bottom = 26.dp)) {
            Row(Modifier.fillMaxWidth().height(74.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("取消", color = Color(0xFF397FEA), fontSize = 20.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 22.dp).clickable(onClick = onCancel))
                Spacer(Modifier.weight(1f))
                Text("完成", color = Color(0xFFED8888), fontSize = 20.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 22.dp).clickable(onClick = onComplete))
            }
            Text("清单名称", color = GoaldayDesign.InkPrimary, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 22.dp, bottom = 12.dp))
            BasicTextField(
                value = draft,
                onValueChange = onDraftChange,
                singleLine = true,
                textStyle = TextStyle(color = GoaldayDesign.InkPrimary, fontSize = 20.sp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp).clip(RoundedCornerShape(6.dp)).background(Color(0xFFF8F3EE)).padding(horizontal = 10.dp, vertical = 14.dp),
            )
            Row(Modifier.fillMaxWidth().height(58.dp).padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("关联到日程", color = GoaldayDesign.InkPrimary, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                Box(Modifier.width(48.dp).height(28.dp).clip(RoundedCornerShape(20.dp)).background(if (bindSchedule) Color(0xFFED8888) else Color(0xFFE7DED7)).clickable { onBindScheduleChange(!bindSchedule) }, contentAlignment = if (bindSchedule) Alignment.CenterEnd else Alignment.CenterStart) {
                    Box(Modifier.padding(3.dp).size(22.dp).clip(CircleShape).background(Color.White))
                }
            }
            Text("清单颜色", color = GoaldayDesign.InkPrimary, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 20.dp, top = 8.dp, bottom = 12.dp))
            Row(Modifier.fillMaxWidth().height(40.dp).padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                colors.forEachIndexed { index, color ->
                    Box(Modifier.size(if (index == selectedColorIndex) 30.dp else 24.dp).clip(CircleShape).background(color).border(if (index == selectedColorIndex) 2.dp else 0.dp, GoaldayDesign.InkPrimary, CircleShape).clickable { onColorSelect(index) })
                }
            }
        }
    }
}

@Composable
private fun ListOverviewCard(title: String, progress: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().height(92.dp).clip(RoundedCornerShape(3.dp)).background(Color(0xFFFFFEFC)).border(0.6.dp, Color(0x11000000), RoundedCornerShape(3.dp)).clickable(onClick = onClick).padding(horizontal = 42.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, color = GoaldayDesign.InkPrimary, fontSize = 24.sp, modifier = Modifier.weight(1f))
        Text(progress, color = GoaldayDesign.InkSecondary, fontSize = 20.sp)
    }
}

@Composable
private fun ReferenceTopicsPage(
    onApply: (List<String>, Boolean, Boolean) -> Unit,
    onSaveAsBook: (InspirationTemplate, List<String>) -> Unit,
) {
    val context = LocalContext.current
    val templates: List<InspirationTemplate> = remember(context) { loadReverseTopicTemplates(context) }
    var selectedIndex by rememberSaveable { mutableStateOf(0) }
    val selected = templates.getOrNull(selectedIndex) ?: return
    val targetItems: List<String> = remember(selected.id) {
        loadTargetAssetItems(context, selected.targetAssetPath).ifEmpty { selected.items }
    }
    Column(Modifier.fillMaxSize().background(GoaldayDesign.AppBg).padding(bottom = 48.dp)) {
        Row(Modifier.fillMaxWidth().height(108.dp).padding(horizontal = 18.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.width(62.dp).height(82.dp).clip(RoundedCornerShape(3.dp))) {
                TopicCoverArt(selected, selectedIndex, Modifier.fillMaxSize(), compact = true)
            }
            Column(Modifier.weight(1f).padding(start = 14.dp)) {
                Text(selected.title, color = GoaldayDesign.InkPrimary, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${targetItems.size} 个目标 · ${selected.coverKey}.png", color = GoaldayDesign.InkSecondary, fontSize = 12.sp, maxLines = 1)
            }
            Text("保存", color = GoaldayDesign.InkPrimary, fontSize = 16.sp, modifier = Modifier.clickable { onSaveAsBook(selected, targetItems) })
        }
        Row(Modifier.fillMaxWidth().height(52.dp).padding(horizontal = 18.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("导入日程", color = GoaldayDesign.InkPrimary, fontSize = 15.sp, modifier = Modifier.clickable { onApply(targetItems, true, false) })
            Spacer(Modifier.width(22.dp))
            Text("加入清单", color = GoaldayDesign.InkSecondary, fontSize = 15.sp, modifier = Modifier.clickable { onApply(targetItems, false, false) })
        }
        Row(Modifier.fillMaxWidth().weight(1f)) {
            Column(Modifier.width(132.dp).verticalScroll(rememberScrollState()).padding(start = 12.dp, end = 8.dp)) {
                templates.forEachIndexed { index, item ->
                    Box(Modifier.fillMaxWidth().height(78.dp).padding(vertical = 4.dp).clip(RoundedCornerShape(4.dp)).border(if (index == selectedIndex) 1.2.dp else 0.4.dp, if (index == selectedIndex) GoaldayDesign.InkPrimary else GoaldayDesign.BorderColor, RoundedCornerShape(4.dp)).clickable { selectedIndex = index }) {
                        TopicCoverArt(item, index, Modifier.fillMaxSize(), compact = true)
                    }
                }
            }
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(end = 16.dp)) {
                targetItems.take(80).forEachIndexed { index, item ->
                    TargetDetailLine(index + 1, item, false) { onApply(listOf(item), true, false) }
                }
            }
        }
    }
}
@Composable
private fun ReferenceBottomBookBar(onSettings: () -> Unit, onCalendar: () -> Unit, onBook: () -> Unit) {
    Row(Modifier.fillMaxWidth().height(48.dp).background(Color.White)) {
        BottomIconSlot(Modifier.weight(1f), onSettings, "⚙")
        BottomIconSlot(Modifier.weight(1f), onCalendar, "□")
        BottomIconSlot(Modifier.weight(1f), onBook, "▰")
    }
}

@Composable
private fun BottomIconSlot(modifier: Modifier, onClick: () -> Unit, label: String) {
    Box(modifier.fillMaxHeight().clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Text(label, color = GoaldayDesign.InkPrimary, fontSize = 28.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ReferenceBookOverview(
    date: LocalDate,
    weekOffset: Int,
    entries: List<ScheduleEntry>,
    diaryDraft: String,
    listItems: List<String>,
    onOpenDiary: (LocalDate) -> Unit,
    onOpenWeek: (Int) -> Unit,
    onChangeWeek: (Int) -> Unit,
    onExport: () -> Unit,
    onShelf: () -> Unit,
    onReturn: () -> Unit,
) {
    val anchor = date.plusWeeks(weekOffset.toLong())
    val monday = anchor.minusDays((anchor.dayOfWeek.value - 1).toLong())
    Box(
        Modifier.fillMaxSize().background(GoaldayDesign.AppBg).pointerInput(weekOffset) {
            detectDragGestures { change, drag ->
                if (abs(drag.x) > abs(drag.y) && abs(drag.x) > 20f) {
                    onChangeWeek(if (drag.x < 0f) 1 else -1)
                    change.consume()
                }
            }
        },
    ) {
        Text("${anchor.monthValue}月", color = GoaldayDesign.InkPrimary, fontSize = 22.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.align(Alignment.TopCenter).padding(top = 116.dp))
        Row(Modifier.fillMaxWidth().padding(start = 39.dp, end = 37.dp, top = 140.dp, bottom = 92.dp).align(Alignment.Center), horizontalArrangement = Arrangement.spacedBy(0.dp)) {
            MiniBookPage(
                modifier = Modifier.weight(1f).height(548.dp),
                title = "${monday.minusDays(2).dayOfMonth}",
                subTitle = " | ${weekdayShort(monday.minusDays(2))}",
                mode = "diary",
                diaryDraft = diaryDraft,
                entries = entries,
                listItems = listItems,
                onClick = { onOpenDiary(monday.minusDays(2)) },
            )
            MiniBookPage(
                modifier = Modifier.weight(1f).height(548.dp),
                title = "${monday.monthValue}月",
                subTitle = " | 第${weekNumber(anchor)}周",
                mode = "week",
                diaryDraft = diaryDraft,
                entries = entries,
                listItems = listItems,
                onClick = { onOpenWeek(weekOffset) },
            )
        }
        Row(Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(78.dp).background(GoaldayDesign.AppBg), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.width(194.dp).fillMaxHeight().clickable(onClick = onExport), contentAlignment = Alignment.CenterStart) {
                Text("▣", color = GoaldayDesign.InkPrimary, fontSize = 26.sp, modifier = Modifier.padding(start = 36.dp))
            }
            Box(Modifier.weight(1f).fillMaxHeight().clickable(onClick = onShelf), contentAlignment = Alignment.Center) {
                Text("${anchor.year}", color = GoaldayDesign.InkPrimary, fontSize = 25.sp, fontWeight = FontWeight.SemiBold)
            }
            Box(Modifier.width(256.dp).fillMaxHeight().clickable(onClick = onReturn), contentAlignment = Alignment.Center) {
                Text("返回", color = GoaldayDesign.InkPrimary, fontSize = 25.sp)
            }
        }
    }
}

@Composable
private fun MiniBookPage(
    modifier: Modifier,
    title: String,
    subTitle: String,
    mode: String,
    diaryDraft: String,
    entries: List<ScheduleEntry>,
    listItems: List<String>,
    onClick: () -> Unit,
) {
    Box(
        modifier.shadow(14.dp, RoundedCornerShape(2.dp)).background(Brush.horizontalGradient(listOf(Color(0xFFFFF9F0), Color(0xFFFEFCF8), Color(0xFFF2E3D5)))).border(0.8.dp, Color(0x22000000)).clickable(onClick = onClick).padding(18.dp),
    ) {
        Column(Modifier.fillMaxSize()) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(title, color = GoaldayDesign.InkPrimary, fontSize = if (mode == "week") 18.sp else 20.sp, fontWeight = FontWeight.SemiBold)
                Text(subTitle, color = GoaldayDesign.InkSecondary, fontSize = 13.sp)
            }
            Spacer(Modifier.height(12.dp))
            if (mode == "week") {
                (0..6).forEach { MiniWeekRow(it, entries) }
            } else {
                Text(diaryDraft.ifBlank { "点击输入" }, color = GoaldayDesign.InkPrimary, fontSize = 12.sp, lineHeight = 18.sp, maxLines = 3)
                Spacer(Modifier.height(12.dp))
                listItems.take(5).forEachIndexed { index, item -> Text("${index + 1}. $item", color = GoaldayDesign.InkSecondary, fontSize = 10.sp, maxLines = 1) }
            }
        }
    }
}

@Composable
private fun MiniWeekRow(index: Int, entries: List<ScheduleEntry>) {
    val labels = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
    Row(Modifier.fillMaxWidth().height(43.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.width(42.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("${index + 1}", fontSize = 12.sp, color = GoaldayDesign.InkPrimary)
            Text(labels[index], fontSize = 8.sp, color = GoaldayDesign.InkSecondary)
        }
        Text(entries.getOrNull(index)?.title ?: "—", fontSize = 10.sp, color = GoaldayDesign.InkMuted, maxLines = 1)
    }
}

@Composable
private fun ReferenceBookShelf(selectedYear: Int, onSelect: (Int) -> Unit, onCancel: () -> Unit) {
    Box(Modifier.fillMaxSize().background(GoaldayDesign.AppBg.copy(alpha = 0.96f))) {
        Text("书架", color = GoaldayDesign.InkPrimary, fontSize = 26.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.align(Alignment.Center).padding(bottom = 320.dp))
        Text("取消", color = GoaldayDesign.InkPrimary, fontSize = 24.sp, modifier = Modifier.align(Alignment.CenterEnd).padding(end = 54.dp, bottom = 420.dp).clickable(onClick = onCancel))
        Row(Modifier.align(Alignment.BottomCenter).padding(bottom = 88.dp), horizontalArrangement = Arrangement.spacedBy(54.dp)) {
            listOf(selectedYear, selectedYear - 1, selectedYear - 2).forEach { year ->
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onSelect(year) }) {
                    Box(Modifier.width(80.dp).height(156.dp).shadow(8.dp, RoundedCornerShape(4.dp)).background(GoaldayDesign.MorandiBrownLight).border(1.dp, GoaldayDesign.BorderColor.copy(alpha = 0.2f)))
                    Text("$year", color = GoaldayDesign.InkPrimary, fontSize = 22.sp, modifier = Modifier.padding(top = 10.dp))
                }
            }
        }
    }
}

@Composable
private fun ReferenceExportOptions(onCancel: () -> Unit, onPickStart: () -> Unit, onPickEnd: () -> Unit) {
    Column(Modifier.fillMaxSize().background(GoaldayDesign.AppBg).padding(horizontal = 34.dp)) {
        Row(Modifier.fillMaxWidth().height(92.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("取消", color = GoaldayDesign.InkPrimary, fontSize = 24.sp, modifier = Modifier.clickable(onClick = onCancel))
            Text("设置选项", color = GoaldayDesign.InkPrimary, fontSize = 24.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
            Text("确定", color = GoaldayDesign.InkPrimary, fontSize = 24.sp, modifier = Modifier.clickable(onClick = onCancel))
        }
        Text("打印PDF", color = GoaldayDesign.InkPrimary, fontSize = 22.sp, modifier = Modifier.padding(top = 42.dp, bottom = 20.dp))
        ExportOptionLine("周计划")
        ExportOptionLine("日记")
        Text("时间", color = GoaldayDesign.InkPrimary, fontSize = 21.sp, modifier = Modifier.padding(top = 34.dp, bottom = 18.dp))
        ExportDateLine("开始", onPickStart)
        ExportDateLine("结束", onPickEnd)
        Text("预览", color = GoaldayDesign.InkPrimary, fontSize = 21.sp, modifier = Modifier.padding(top = 38.dp))
        Spacer(Modifier.weight(1f))
        Text("请选择时间范围和内容类型", color = GoaldayDesign.InkSecondary, fontSize = 18.sp, modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 96.dp))
    }
}

@Composable
private fun ExportOptionLine(text: String) {
    Row(Modifier.fillMaxWidth().height(74.dp).clickable { }, verticalAlignment = Alignment.CenterVertically) {
        Text(text, color = GoaldayDesign.InkPrimary, fontSize = 20.sp, modifier = Modifier.weight(1f))
        Box(Modifier.size(18.dp).border(1.dp, GoaldayDesign.InkSecondary, CircleShape))
    }
}

@Composable
private fun ExportDateLine(text: String, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().height(74.dp).clickable(onClick = onClick), verticalAlignment = Alignment.CenterVertically) {
        Text(text, color = GoaldayDesign.InkPrimary, fontSize = 20.sp, modifier = Modifier.weight(1f))
        Text("选择日期", color = GoaldayDesign.InkSecondary, fontSize = 18.sp)
    }
}

@Composable
private fun ReferenceMonthPicker(date: LocalDate, onPick: (LocalDate) -> Unit, onClose: () -> Unit) {
    val month = YearMonth.from(date)
    Box(Modifier.fillMaxSize().background(Color(0x44000000)).clickable(onClick = onClose)) {
        Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(420.dp).background(GoaldayDesign.AppBg).padding(horizontal = 39.dp, vertical = 16.dp)) {
            Row(Modifier.fillMaxWidth().height(52.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("${month.monthValue}月 ${month.year}", color = GoaldayDesign.InkPrimary, fontSize = 22.sp, modifier = Modifier.weight(1f))
                Text("今天", color = GoaldayDesign.InkPrimary, fontSize = 20.sp, modifier = Modifier.clickable { onPick(LocalDate.now()) })
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日").forEach { Text(it, color = GoaldayDesign.InkSecondary, fontSize = 15.sp) }
            }
            val firstOffset = month.atDay(1).dayOfWeek.value - 1
            val cells = List(firstOffset) { 0 } + (1..month.lengthOfMonth()).toList()
            cells.chunked(7).forEach { row ->
                Row(Modifier.fillMaxWidth().height(42.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    row.forEach { day ->
                        Box(Modifier.size(38.dp).clickable(enabled = day > 0) { onPick(month.atDay(day)) }, contentAlignment = Alignment.Center) {
                            if (day > 0) Text("$day", color = GoaldayDesign.InkPrimary, fontSize = 18.sp)
                        }
                    }
                    repeat(7 - row.size) { Spacer(Modifier.size(38.dp)) }
                }
            }
        }
    }
}

@Composable
private fun ReferenceSettingsPanel(onClose: () -> Unit) {
    Column(Modifier.fillMaxSize().background(GoaldayDesign.AppBg).verticalScroll(rememberScrollState())) {
        Row(Modifier.fillMaxWidth().height(92.dp).background(GoaldayDesign.AppBg).padding(horizontal = 24.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("‹", fontSize = 34.sp, color = GoaldayDesign.InkPrimary, modifier = Modifier.clickable(onClick = onClose))
            Text("设置", color = GoaldayDesign.InkPrimary, fontSize = 25.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
            Spacer(Modifier.width(34.dp))
        }
        SettingSection("账号")
        SettingRow("点击注册/登录")
        SettingSection("通用")
        SettingRow("字体大小", "小    中    大")
        SettingRow("数据迁移")
        SettingRow("导入日历")
        SettingSection("日记")
        SettingRow("图片尺寸", "小        大")
        SettingSection("联系我们")
        SettingRow("用户反馈")
        SettingRow("小红书")
        SettingRow("版本信息", "Version 2.5.7")
        SettingRow("软件更新", "点击后检查并更新到最新版")
        SettingRow("隐私政策")
        SettingRow("用户协议")
        Text("备案号：浙ICP备2023035434号-1A", color = GoaldayDesign.InkSecondary, fontSize = 13.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(20.dp))
    }
}

@Composable
private fun SettingSection(title: String) {
    Text(title, color = GoaldayDesign.InkSecondary, fontSize = 18.sp, modifier = Modifier.padding(start = 34.dp, top = 22.dp, bottom = 6.dp))
}

@Composable
private fun SettingRow(title: String, trailing: String = "") {
    Row(Modifier.fillMaxWidth().height(72.dp).padding(horizontal = 38.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(title, color = GoaldayDesign.InkPrimary, fontSize = 19.sp, modifier = Modifier.weight(1f))
        if (trailing.isNotBlank()) Text(trailing, color = GoaldayDesign.InkSecondary, fontSize = 16.sp)
    }
}

@Composable
private fun ScheduleLine(day: LocalDate, entries: List<ScheduleEntry>, completedItems: List<String>, onSelect: () -> Unit, onToggleEntry: (String) -> Unit) {
    Row(Modifier.fillMaxWidth().height(103.dp).clickable(onClick = onSelect), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.width(50.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("${day.dayOfMonth}", color = GoaldayDesign.InkPrimary, fontSize = 22.sp)
            Text(day.dayOfWeek.displayName(), color = GoaldayDesign.MorandiDivider, fontSize = 13.sp)
        }
        Column(Modifier.weight(1f)) {
            val slots = (entries.map { it.title to it } + List((6 - entries.size).coerceAtLeast(0)) { "" to null }).take(6)
            slots.chunked(2).forEach { rowSlots ->
                Row(Modifier.fillMaxWidth().height(33.dp)) {
                    rowSlots.forEach { (title, entry) ->
                        BasicTextField(
                            value = title,
                            onValueChange = {},
                            readOnly = true,
                            textStyle = TextStyle(
                                color = if (entry?.completed == true || title in completedItems) GoaldayDesign.InkMuted else GoaldayDesign.InkPrimary,
                                fontSize = 13.sp,
                                textDecoration = if (entry?.completed == true || title in completedItems) TextDecoration.LineThrough else TextDecoration.None,
                            ),
                            modifier = Modifier.weight(1f).height(33.dp).clickable(enabled = entry != null) { entry?.let { onToggleEntry(it.id) } },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TargetDetailLine(index: Int, item: String, checked: Boolean, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().height(76.dp).clickable(onClick = onClick), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.width(88.dp), contentAlignment = Alignment.Center) { Text("$index", color = GoaldayDesign.InkSecondary, fontSize = 18.sp) }
        Text(item, color = if (checked) GoaldayDesign.InkMuted else GoaldayDesign.InkPrimary, fontSize = 20.sp, textDecoration = if (checked) TextDecoration.LineThrough else TextDecoration.None, modifier = Modifier.weight(1f))
    }
}

private fun Modifier.referencePaperLines(): Modifier = drawBehind {
    val step = 32.dp.toPx()
    var y = 190.dp.toPx()
    while (y < size.height - 24.dp.toPx()) {
        drawLine(GoaldayDesign.PaperLine, Offset(48.dp.toPx(), y), Offset(size.width - 48.dp.toPx(), y), strokeWidth = 0.55.dp.toPx())
        y += step
    }
}

private fun randomPrompt(date: LocalDate): String {
    val prompts = listOf(
        "今天有什么瞬间想按下\"暂停键\"重复体验？",
        "醒来后第一个念头是什么？",
        "今天是否有一个\"啊哈！\"的灵感时刻？",
        "谁或什么事让你今天感到温暖？",
        "给今天的关键词画个简单的符号（描述出来即可）。",
    )
    return prompts[(date.dayOfYear - 1).mod(prompts.size)]
}

private fun weekNumber(date: LocalDate): Int = date.get(WeekFields.of(Locale.CHINA).weekOfWeekBasedYear())

private fun weekdayShort(date: LocalDate): String = when (date.dayOfWeek.value) {
    1 -> "周一"
    2 -> "周二"
    3 -> "周三"
    4 -> "周四"
    5 -> "周五"
    6 -> "周六"
    else -> "周日"
}

private fun java.time.DayOfWeek.displayName(): String = when (this) {
    java.time.DayOfWeek.MONDAY -> "周一"
    java.time.DayOfWeek.TUESDAY -> "周二"
    java.time.DayOfWeek.WEDNESDAY -> "周三"
    java.time.DayOfWeek.THURSDAY -> "周四"
    java.time.DayOfWeek.FRIDAY -> "周五"
    java.time.DayOfWeek.SATURDAY -> "周六"
    java.time.DayOfWeek.SUNDAY -> "周日"
}

private fun collectListItems(page: BookPage?, uiState: BookUiState): List<String> {
    val base = when (page) {
        is TargetPage -> page.items
        is PlanPage -> page.items
        is SchedulePage -> page.items
        else -> emptyList()
    }
    val fallback = listOf("制定2026计划", "坚持早睡早起", "每月复盘一次", "每月存一笔钱", "每月记一次帐", "制定年度预算计划", "闲置物品出售", "减少外卖次数", "定期问候长辈", "看完十本书")
    return (base + uiState.customPageItems + uiState.todayPlanItems + uiState.todayCompletedItems + fallback).filter { it.isNotBlank() }.distinct()
}

private fun loadReverseTopicTemplates(context: android.content.Context): List<InspirationTemplate> {
    val parsed = runCatching {
        context.assets.open("topic_center_config.json").bufferedReader(Charsets.UTF_8).use { reader ->
            ReverseTopicConfigParser.parse(reader.readText())
        }
    }.getOrDefault(emptyList())
    if (parsed.isEmpty()) return InspirationTemplates.all
    return parsed.mapIndexed { index, topic ->
        val color = runCatching { Color(android.graphics.Color.parseColor("#${topic.colorHex}")) }.getOrElse { GoaldayDesign.TopicPeach }
        InspirationTemplate(
            id = topic.id,
            title = topic.title,
            subtitle = if (topic.linkToSchedule) "可加入日程" else "本地清单模板",
            color = color,
            coverKey = topic.coverKey,
            targetKey = topic.targetKey,
            category = "主题",
            catalogPath = "assets/topic_center_config.json",
            coverAssetPath = "assets/cover/${topic.coverKey}.png",
            targetAssetPath = "assets/topictarget/${topic.targetKey}.txt",
            linkToSchedule = topic.linkToSchedule,
            items = topic.targets.ifEmpty { InspirationTemplates.all.getOrNull(index)?.items ?: emptyList() },
        )
    }
}