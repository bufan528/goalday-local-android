package com.bf410.goaldaylocal.ui.book

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.border
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.bf410.goaldaylocal.data.BookPage
import com.bf410.goaldaylocal.data.DiaryPage
import com.bf410.goaldaylocal.data.PlanPage
import com.bf410.goaldaylocal.data.ScheduleEntry
import com.bf410.goaldaylocal.data.SchedulePage
import com.bf410.goaldaylocal.data.TargetPage
import kotlin.math.abs
import kotlin.math.roundToInt
import java.time.Instant
import java.time.ZoneId

private val BoardTonePlan = Color(0x22D9A97E)
private val BoardToneDone = Color(0x22A5C49D)
private val BoardTitleColor = Color(0xFF5E4837)
private val BoardHintColor = Color(0xFF8B7A68)
private const val TODAY_SWIPE_THRESHOLD = -88f
private const val DONE_SWIPE_THRESHOLD = -170f

@Composable
fun BoxScope.SpineLayer(
    visualProgress: Float,
    active: Boolean,
) {
    Box(
        modifier = Modifier
            .align(Alignment.Center)
            .width(20.dp)
            .fillMaxHeight()
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color(0xFF7F4F31).copy(alpha = if (active) 0.88f else 0.74f),
                        Color(0xFFF6E4D0),
                        Color(0xFF7F4F31).copy(alpha = if (active) 0.88f else 0.74f),
                    ),
                ),
            ),
    )

    if (active) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .width((16f + visualProgress * 10f).dp)
                .fillMaxHeight()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color.Black.copy(alpha = (0.09f + visualProgress * 0.18f).coerceAtMost(0.24f)),
                            Color.Transparent,
                            Color.Black.copy(alpha = (0.09f + visualProgress * 0.18f).coerceAtMost(0.24f)),
                        ),
                    ),
                ),
        )
    }
}

@Composable
fun PageSurface(
    modifier: Modifier = Modifier,
    title: String,
    pageNumber: String,
    headerTitle: String,
    headerSubtitle: String,
    tint: Color,
    onSavedClick: (() -> Unit)? = null,
    body: @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp, 28.dp, 32.dp, 32.dp))
            .background(Color(0xFFFFFDFC))
            .padding(horizontal = 18.dp, vertical = 16.dp),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .width(2.dp)
                .fillMaxHeight()
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0x12000000), Color.Transparent))),
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .alpha(0.12f)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0x10C7B39C),
                            Color.Transparent,
                            Color(0x08C7B39C),
                            Color.Transparent,
                            Color(0x0CC7B39C),
                        ),
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .width(4.dp)
                .fillMaxHeight()
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0x12000000), Color.Transparent),
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .width(20.dp)
                .height(20.dp)
                .background(
                    Brush.linearGradient(
                        listOf(Color.Transparent, Color(0x12C2AE95)),
                        start = Offset(0f, 20f),
                        end = Offset(20f, 0f),
                    ),
                ),
        )
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 2.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.End,
        ) {
            listOf(Color(0x33E693B1), Color(0x33F0C187), Color(0x3394C8E8), Color(0x339FD39B)).forEach { c ->
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(16.dp)
                        .clip(RoundedCornerShape(99.dp))
                        .background(c),
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            PageHeaderLine(
                bookTitle = headerTitle,
                subtitle = headerSubtitle,
                tint = tint,
                savedText = pageNumber,
                onSavedClick = onSavedClick,
            )
            Spacer(Modifier.height(6.dp))
            Text(text = title, style = MaterialTheme.typography.titleMedium, color = Color(0xFF26211C))
            Spacer(Modifier.height(8.dp))
            body()
        }
    }
}

@Composable
fun DestinationPageLayer(
    modifier: Modifier,
    bookTitle: String,
    subtitle: String,
    page: BookPage?,
    pageIndex: Int,
    pageCount: Int,
    tint: Color,
    revealProgress: Float,
    direction: TurnDirection?,
) {
    val pageTitle = page?.title ?: BookStrings.cover
    val pageSubtitle = destinationPageSubtitle(direction, subtitle)
    val pageNumber = when {
        page == null -> ""
        direction == TurnDirection.NEXT -> "${pageIndex + 2} / $pageCount"
        direction == TurnDirection.PREVIOUS -> "$pageIndex / $pageCount"
        else -> "${pageIndex + 1} / $pageCount"
    }

    PageSurface(
        modifier = modifier,
        title = pageTitle,
        pageNumber = pageNumber.ifBlank { BookStrings.pagePreview },
        headerTitle = bookTitle,
        headerSubtitle = pageSubtitle,
        tint = tint.copy(alpha = 0.68f),
    ) {
        Text(
            text = condensedPreviewText(page?.let(::pagePreviewText) ?: subtitle, maxLength = 92),
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF6A5D4F).copy(alpha = destinationRevealAlpha(revealProgress)),
        )
        Spacer(Modifier.weight(1f))
    }
}

@Composable
fun PageBackLayer(
    modifier: Modifier,
    tint: Color,
    progress: Float,
    direction: TurnDirection?,
    anchorY: Float = 0.5f,
) {
    val curlAlignTop = anchorY < 0.46f
    val curlStrength = (0.16f + progress * 0.56f).coerceIn(0f, 0.72f)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp, 24.dp, 30.dp, 30.dp))
            .background(
                Brush.horizontalGradient(
                    if (direction == TurnDirection.NEXT) {
                        listOf(Color(0xFFDCCAB4), Color(0xFFF4E9DD), Color(0xFFFFFCF8))
                    } else {
                        listOf(Color(0xFFFFFCF8), Color(0xFFF4E9DD), Color(0xFFDCCAB4))
                    },
                ),
            )
            .padding(horizontal = 28.dp, vertical = 26.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color(0x12FFFFFF), Color.Transparent),
                    ),
                ),
        )

        Box(
            modifier = Modifier
                .align(if (direction == TurnDirection.NEXT) Alignment.CenterStart else Alignment.CenterEnd)
                .width((12f + progress * 24f).dp)
                .fillMaxHeight()
                .background(
                    Brush.horizontalGradient(
                        if (direction == TurnDirection.NEXT) {
                            listOf(
                                Color.Black.copy(alpha = (0.10f + progress * 0.18f).coerceAtMost(0.24f)),
                                Color.White.copy(alpha = (0.14f + progress * 0.22f).coerceAtMost(0.30f)),
                                Color.Transparent,
                            )
                        } else {
                            listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = (0.14f + progress * 0.22f).coerceAtMost(0.30f)),
                                Color.Black.copy(alpha = (0.10f + progress * 0.18f).coerceAtMost(0.24f)),
                            )
                        },
                    ),
                ),
        )

        Box(
            modifier = Modifier
                .align(
                    when {
                        direction == TurnDirection.NEXT && curlAlignTop -> Alignment.TopStart
                        direction == TurnDirection.PREVIOUS && curlAlignTop -> Alignment.TopEnd
                        direction == TurnDirection.NEXT -> Alignment.BottomStart
                        else -> Alignment.BottomEnd
                    },
                )
                .width((24f + progress * 58f).dp)
                .height((26f + progress * 64f).dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = (0.22f + curlStrength * 0.28f).coerceAtMost(0.54f)),
                            Color(0x22A48A70).copy(alpha = (0.18f + curlStrength * 0.24f).coerceAtMost(0.46f)),
                            Color.Transparent,
                        ),
                        radius = 180f,
                    ),
                ),
        )

        Box(
            modifier = Modifier
                .align(
                    when {
                        direction == TurnDirection.NEXT && curlAlignTop -> Alignment.TopEnd
                        direction == TurnDirection.PREVIOUS && curlAlignTop -> Alignment.TopStart
                        direction == TurnDirection.NEXT -> Alignment.BottomEnd
                        else -> Alignment.BottomStart
                    },
                )
                .width((12f + progress * 24f).dp)
                .height((20f + progress * 34f).dp)
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color.Black.copy(alpha = (0.08f + curlStrength * 0.20f).coerceAtMost(0.30f)),
                            Color.Transparent,
                        ),
                    ),
                ),
        )

        Column(modifier = Modifier.fillMaxSize()) {
            PageHeaderLine(
                bookTitle = BookStrings.pageBack,
                subtitle = BookStrings.pageTurning,
                tint = tint.copy(alpha = 0.56f),
                savedText = BookStrings.turnProgress.format((progress * 100).toInt()),
            )
            Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
fun ActivePageLayer(
    modifier: Modifier,
    page: BookPage,
    pageIndex: Int,
    pageCount: Int,
    bookTitle: String,
    subtitle: String,
    tint: Color,
    isSaved: Boolean,
    diaryDraft: String,
    customPageItems: List<String>,
    weeklyTheme: String,
    todayPlanItems: List<String>,
    todayCompletedItems: List<String>,
    schedulePreviewEntries: List<ScheduleEntry>,
    onToggleSaved: () -> Unit,
    isChecked: (String, String) -> Boolean,
    onToggleChecked: (String, String) -> Unit,
    onDiaryChange: (String) -> Unit,
    onAddCustomItem: (String) -> Unit,
    onAddCustomItemWithDeadline: (String, Int?) -> Unit,
    onRemoveCustomItem: (String) -> Unit,
    onRenameCustomItem: (String, String) -> Unit,
    onAddToSchedule: (String, Int) -> Unit,
    onWeeklyThemeChange: (String) -> Unit,
    onMoveItemToToday: (String) -> Unit,
    onMoveItemToCompleted: (String) -> Unit,
    onRestoreItemFromToday: (String) -> Unit,
    onRestoreItemFromCompleted: (String) -> Unit,
    pendingCommand: RichEditorCommand?,
    onCommand: (RichEditorCommand) -> Unit,
    contentMode: PageContentMode,
    onContentModeChange: (PageContentMode) -> Unit,
    turnProgress: Float = 0f,
    turnDirection: TurnDirection? = null,
) {
    val contentShift = when (turnDirection) {
        TurnDirection.NEXT -> -turnProgress * 16f
        TurnDirection.PREVIOUS -> turnProgress * 16f
        null -> 0f
    }
    val contentAlpha = (1f - turnProgress * 0.22f).coerceIn(0.78f, 1f)
    PageSurface(
        modifier = modifier,
        title = page.title,
        pageNumber = if (isSaved) BookStrings.savedBook else BookStrings.saveBook,
        headerTitle = bookTitle,
        headerSubtitle = subtitle,
        tint = tint,
        onSavedClick = onToggleSaved,
    ) {
        Column(
            modifier = Modifier.graphicsLayer {
                alpha = contentAlpha
                translationX = contentShift
            },
        ) {
            when (page) {
                is TargetPage -> EditableBulletPage(page.title, page.items, customPageItems, tint, BookStrings.addTarget, isChecked, onToggleChecked, onAddCustomItem, onAddCustomItemWithDeadline, onRemoveCustomItem, onRenameCustomItem, onAddToSchedule, weeklyTheme, todayPlanItems, todayCompletedItems, schedulePreviewEntries, onWeeklyThemeChange, onMoveItemToToday, onMoveItemToCompleted, onRestoreItemFromToday, onRestoreItemFromCompleted, contentMode, onContentModeChange)
                is SchedulePage -> EditableBulletPage(page.title, page.items, customPageItems, tint.copy(alpha = 0.74f), BookStrings.addSchedule, isChecked, onToggleChecked, onAddCustomItem, onAddCustomItemWithDeadline, onRemoveCustomItem, onRenameCustomItem, onAddToSchedule, weeklyTheme, todayPlanItems, todayCompletedItems, schedulePreviewEntries, onWeeklyThemeChange, onMoveItemToToday, onMoveItemToCompleted, onRestoreItemFromToday, onRestoreItemFromCompleted, contentMode, onContentModeChange)
                is PlanPage -> EditableBulletPage(page.title, page.items, customPageItems, Color(0xFFB88A58), BookStrings.addPlan, isChecked, onToggleChecked, onAddCustomItem, onAddCustomItemWithDeadline, onRemoveCustomItem, onRenameCustomItem, onAddToSchedule, weeklyTheme, todayPlanItems, todayCompletedItems, schedulePreviewEntries, onWeeklyThemeChange, onMoveItemToToday, onMoveItemToCompleted, onRestoreItemFromToday, onRestoreItemFromCompleted, contentMode, onContentModeChange)
                is DiaryPage -> DiarySection(page.title, page.prompt, tint, diaryDraft, pendingCommand, onCommand, onDiaryChange, contentMode, onContentModeChange)
            }
        }
        Spacer(Modifier.height(24.dp))
        Text(text = "${pageIndex + 1} / $pageCount", style = MaterialTheme.typography.labelMedium, color = Color(0xFF7A7065))
    }
}

private fun pagePreviewText(page: BookPage): String =
    when (page) {
        is TargetPage -> page.items.take(3).joinToString("\n") { "• $it" }.ifBlank { BookStrings.contentEmpty }
        is PlanPage -> page.items.take(3).joinToString("\n") { "• $it" }.ifBlank { BookStrings.contentEmpty }
        is SchedulePage -> page.items.take(3).joinToString("\n") { "• $it" }.ifBlank { BookStrings.contentEmpty }
        is DiaryPage -> page.prompt
    }

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun EditableBulletPage(
    pageTitle: String,
    baseItems: List<String>,
    customItems: List<String>,
    tint: Color,
    inputLabel: String,
    isChecked: (String, String) -> Boolean,
    onToggleChecked: (String, String) -> Unit,
    onAddCustomItem: (String) -> Unit,
    onAddCustomItemWithDeadline: (String, Int?) -> Unit,
    onRemoveCustomItem: (String) -> Unit,
    onRenameCustomItem: (String, String) -> Unit,
    onAddToSchedule: (String, Int) -> Unit,
    weeklyTheme: String,
    todayPlanItems: List<String>,
    todayCompletedItems: List<String>,
    schedulePreviewEntries: List<ScheduleEntry>,
    onWeeklyThemeChange: (String) -> Unit,
    onMoveItemToToday: (String) -> Unit,
    onMoveItemToCompleted: (String) -> Unit,
    onRestoreItemFromToday: (String) -> Unit,
    onRestoreItemFromCompleted: (String) -> Unit,
    contentMode: PageContentMode,
    onContentModeChange: (PageContentMode) -> Unit,
) {
    var newItem by remember(pageTitle) { mutableStateOf("") }
    var dueDayText by remember(pageTitle) { mutableStateOf("") }
    var showDeadlinePicker by remember(pageTitle) { mutableStateOf(false) }
    val stagedItems = remember(todayPlanItems, todayCompletedItems) { (todayPlanItems + todayCompletedItems).toSet() }
    val sourceBaseItems = remember(baseItems, stagedItems) { baseItems.filterNot { it in stagedItems } }
    val sourceCustomItems = remember(customItems, stagedItems) { customItems.filterNot { it in stagedItems } }
    val sourceItems = sourceBaseItems + sourceCustomItems
    var dragPreviewTarget by remember(pageTitle) { mutableStateOf(DragTarget.NONE) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        WeekThemeSection(theme = weeklyTheme, onThemeChange = onWeeklyThemeChange)
        QuickAddTaskSection(
            inputLabel = inputLabel,
            newItem = newItem,
            onNewItemChange = { newItem = it },
            dueDayText = dueDayText,
            onDueDayChange = { dueDayText = it.filter(Char::isDigit).take(2) },
            onOpenDatePicker = { showDeadlinePicker = true },
            onSaveWithDeadline = {
                val parsedDay = dueDayText.toIntOrNull()
                onAddCustomItemWithDeadline(newItem, parsedDay)
                newItem = ""
                dueDayText = ""
            },
            onSaveOnly = {
                onAddCustomItem(newItem)
                newItem = ""
                dueDayText = ""
            },
        )
        FocusTimelineSection(
            entries = schedulePreviewEntries,
            todoCount = todayPlanItems.size,
            doneCount = todayCompletedItems.size,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            TodayBoardSection(
                todayPlanItems = todayPlanItems,
                todayCompletedItems = todayCompletedItems,
                onMoveItemToCompleted = onMoveItemToCompleted,
                onRestoreItemFromToday = onRestoreItemFromToday,
                onRestoreItemFromCompleted = onRestoreItemFromCompleted,
                dragPreviewTarget = dragPreviewTarget,
                modifier = Modifier.weight(1f),
            )
            SourcePoolSection(
                items = sourceItems,
                pageTitle = pageTitle,
                tint = tint,
                isChecked = isChecked,
                onToggleChecked = onToggleChecked,
                onMoveItemToToday = onMoveItemToToday,
                onMoveItemToCompleted = onMoveItemToCompleted,
                dragPreviewTarget = dragPreviewTarget,
                onDragPreviewTargetChange = { dragPreviewTarget = it },
                modifier = Modifier.weight(1f),
            )
        }
    }

    if (showDeadlinePicker) {
        val pickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDeadlinePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val millis = pickerState.selectedDateMillis
                        if (millis != null) {
                            val day = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                                .dayOfMonth
                            dueDayText = day.toString()
                        }
                        showDeadlinePicker = false
                    },
                ) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showDeadlinePicker = false }) { Text("取消") }
            },
        ) {
            DatePicker(state = pickerState)
        }
    }
}

@Composable
private fun FocusTimelineSection(
    entries: List<ScheduleEntry>,
    todoCount: Int,
    doneCount: Int,
) {
    PaperNoteCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("今日时间流", style = MaterialTheme.typography.titleSmall, color = BoardTitleColor)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                BoardStatChip(label = "To do $todoCount", bg = BoardTonePlan)
                BoardStatChip(label = "Done $doneCount", bg = BoardToneDone)
            }
        }
        if (entries.isEmpty()) {
            Text("暂无排期，先把任务放进 To do", color = BoardHintColor)
            return@PaperNoteCard
        }
        val timeline = entries.take(5)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            timeline.forEach { entry ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0x06A17856))
                        .border(1.dp, Color(0x12A17856), RoundedCornerShape(6.dp))
                        .padding(horizontal = 5.dp, vertical = 5.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text("${entry.day}", style = MaterialTheme.typography.labelMedium, color = Color(0xFF7E6A58))
                    Text(
                        text = entry.title,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (entry.completed) Color(0xFF8B847D) else Color(0xFF2F2922),
                        maxLines = 2,
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickAddTaskSection(
    inputLabel: String,
    newItem: String,
    onNewItemChange: (String) -> Unit,
    dueDayText: String,
    onDueDayChange: (String) -> Unit,
    onOpenDatePicker: () -> Unit,
    onSaveWithDeadline: () -> Unit,
    onSaveOnly: () -> Unit,
) {
    PaperNoteCard {
        Text("快速新增任务", style = MaterialTheme.typography.titleMedium, color = BoardTitleColor)
        OutlinedTextField(
            value = newItem,
            onValueChange = onNewItemChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            label = { Text(inputLabel) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (newItem.isNotBlank()) onSaveOnly()
                },
            ),
        )
        OutlinedTextField(
            value = dueDayText,
            onValueChange = onDueDayChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            label = { Text("截止日（可选）") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (newItem.isNotBlank()) onSaveWithDeadline()
                },
            ),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = onOpenDatePicker) { Text("选日期") }
            TextButton(onClick = onSaveWithDeadline) { Text("保存并进日程") }
            TextButton(onClick = onSaveOnly) { Text("仅保存") }
        }
    }
}

@Composable
private fun SchedulePreviewSection(entries: List<ScheduleEntry>) {
    PaperNoteCard {
        Text("本月日程预览", style = MaterialTheme.typography.titleMedium, color = BoardTitleColor)
        if (entries.isEmpty()) {
            Text("暂未安排事项", color = BoardHintColor)
            return@PaperNoteCard
        }
        entries.take(10).forEach { entry ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Text("${entry.day}", color = Color(0xFF9D6A4A), style = MaterialTheme.typography.labelLarge)
                Text(
                    text = entry.title,
                    color = if (entry.completed) Color(0xFF8B847D) else Color(0xFF27231E),
                    style = completedTextStyle(entry.completed),
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun ActionBulletRow(
    pageTitle: String,
    item: String,
    tint: Color,
    checked: Boolean,
    removable: Boolean,
    onToggleChecked: (String, String) -> Unit,
    onRemoveCustomItem: (String) -> Unit,
    onRenameCustomItem: (String, String) -> Unit,
    contentMode: PageContentMode,
    onContentModeChange: (PageContentMode) -> Unit,
    onRenameDisplayedItem: (String, String) -> Unit,
) {
    val activeEdit = contentMode as? PageContentMode.EditingChecklistItem
    val isEditingThisRow = activeEdit?.title == pageTitle && activeEdit.item == item
    var draft by remember(pageTitle, item) { mutableStateOf(item) }

    PaperNoteCard {
        if (isEditingThisRow) {
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                label = { Text(BookStrings.editContent) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TextButton(onClick = {
                    if (removable) onRenameCustomItem(item, draft) else onRenameDisplayedItem(item, draft)
                    onContentModeChange(PageContentMode.Browsing)
                }) { Text(BookStrings.save) }
                TextButton(onClick = {
                    draft = item
                    onContentModeChange(PageContentMode.Browsing)
                }) { Text(BookStrings.cancel) }
                if (removable) {
                    TextButton(onClick = {
                        onRemoveCustomItem(item)
                        onContentModeChange(PageContentMode.Browsing)
                    }) { Text(BookStrings.delete) }
                }
            }
        } else {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier
                        .clickable { onToggleChecked(pageTitle, item) }
                        .fillMaxWidth(0.62f),
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .size(18.dp)
                            .clip(RoundedCornerShape(99.dp))
                            .background(if (checked) tint else Color(0xFFE5DBCD)),
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = item,
                        style = completedTextStyle(checked),
                        color = if (checked) Color(0xFF8B847D) else Color(0xFF342C24),
                    )
                }
                Text(
                    text = BookStrings.edit,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF8F684F),
                    modifier = Modifier
                        .clickable { onContentModeChange(PageContentMode.EditingChecklistItem(pageTitle, item)) }
                        .padding(top = 7.dp),
                )
            }
        }
    }
}

@Composable
private fun WeekThemeSection(
    theme: String,
    onThemeChange: (String) -> Unit,
) {
    var draft by remember(theme) { mutableStateOf(theme) }
    PaperNoteCard {
        Text("本周重点目标/主题", style = MaterialTheme.typography.titleSmall, color = Color(0xFF5E4837))
        OutlinedTextField(
            value = draft,
            onValueChange = { draft = it },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            label = { Text("例如：稳定作息 + 每天推进主任务") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        )
        TextButton(onClick = { onThemeChange(draft.trim()) }) { Text("保存本周主题") }
    }
}

@Composable
private fun TodayBoardSection(
    todayPlanItems: List<String>,
    todayCompletedItems: List<String>,
    onMoveItemToCompleted: (String) -> Unit,
    onRestoreItemFromToday: (String) -> Unit,
    onRestoreItemFromCompleted: (String) -> Unit,
    onMoveCompletedToTodo: (String) -> Unit = onRestoreItemFromCompleted,
    dragPreviewTarget: DragTarget,
    modifier: Modifier = Modifier,
) {
    val todayHeaderColor by animateColorAsState(
        targetValue = if (dragPreviewTarget == DragTarget.TODAY) Color(0x44D9A97E) else Color(0x14A17856),
        label = "todayHeaderColor",
    )
    val doneHeaderColor by animateColorAsState(
        targetValue = if (dragPreviewTarget == DragTarget.DONE) Color(0x44A5C49D) else Color(0x14A17856),
        label = "doneHeaderColor",
    )
    val todayBorderAlpha by animateFloatAsState(
        targetValue = if (dragPreviewTarget == DragTarget.TODAY) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 500f),
        label = "todayBorderAlpha",
    )
    val doneBorderAlpha by animateFloatAsState(
        targetValue = if (dragPreviewTarget == DragTarget.DONE) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 500f),
        label = "doneBorderAlpha",
    )
    PaperNoteCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("6", style = MaterialTheme.typography.titleMedium, color = Color(0xFF3A342E))
            Text("To do", style = MaterialTheme.typography.labelMedium, color = Color(0xFF3A342E))
        }
        Text("左侧时间栏 · 右侧任务栏", style = MaterialTheme.typography.labelSmall, color = Color(0xFF948778))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BoardStatChip(label = "计划 ${todayPlanItems.size}", bg = BoardTonePlan)
            BoardStatChip(label = "完成 ${todayCompletedItems.size}", bg = BoardToneDone)
        }
        if (todayPlanItems.isEmpty() && todayCompletedItems.isEmpty()) {
            Text("从右侧清单拖入（点击）到今日执行或完成区。", color = Color(0xFF7B6B5A))
            return@PaperNoteCard
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Done  ${todayCompletedItems.size}",
                    color = Color(0xFF5E4837),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(doneHeaderColor)
                        .border(1.dp, Color(0xCC79A16E).copy(alpha = doneBorderAlpha), RoundedCornerShape(10.dp))
                        .padding(horizontal = 6.dp, vertical = 3.dp),
                )
                if (dragPreviewTarget == DragTarget.DONE) {
                    Text(
                        "松手即加入 Done",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF6F8F63),
                        modifier = Modifier
                            .clip(RoundedCornerShape(99.dp))
                            .background(Color(0x1F79A16E))
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.animateContentSize()) {
                    todayCompletedItems.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0x08A5C49D))
                                .border(1.dp, Color(0x18A5C49D), RoundedCornerShape(6.dp))
                                .clickable { onMoveCompletedToTodo(item) }
                                .padding(horizontal = 6.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text("✓", color = Color(0xFF7FA579), style = MaterialTheme.typography.labelSmall)
                            Text(text = item, style = completedTextStyle(completed = true), color = Color(0xFF8B847D), modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "To do  ${todayPlanItems.size}",
                    color = Color(0xFF5E4837),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(todayHeaderColor)
                        .border(1.dp, Color(0xCCB77A5A).copy(alpha = todayBorderAlpha), RoundedCornerShape(10.dp))
                        .padding(horizontal = 6.dp, vertical = 3.dp),
                )
                if (dragPreviewTarget == DragTarget.TODAY) {
                    Text(
                        "松手即加入 To do",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF9B7352),
                        modifier = Modifier
                            .clip(RoundedCornerShape(99.dp))
                            .background(Color(0x1FD9A97E))
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.animateContentSize()) {
                    todayPlanItems.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0x08D9A97E))
                                .border(1.dp, Color(0x18D9A97E), RoundedCornerShape(6.dp))
                                .clickable { onMoveItemToCompleted(item) }
                                .padding(horizontal = 6.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text("◌", color = Color(0xFFB2A89B), style = MaterialTheme.typography.labelSmall)
                            Text(text = item, color = Color(0xFF2F2922), style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
        if (todayPlanItems.isNotEmpty()) {
            TextButton(onClick = { todayPlanItems.forEach(onRestoreItemFromToday) }) {
                Text("回收今日 To do 到来源池")
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(99.dp))
                .background(Color(0x08A17856))
                .padding(horizontal = 8.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            listOf("◷", "↻", "✎", "☺", "☁").forEach { icon ->
                Text(
                    text = icon,
                    color = Color(0xFF8F857A),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier
                        .clip(RoundedCornerShape(99.dp))
                        .background(Color(0x12FFFFFF))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                )
            }
        }
    }
}

@Composable
private fun SourcePoolSection(
    items: List<String>,
    pageTitle: String,
    tint: Color,
    isChecked: (String, String) -> Boolean,
    onToggleChecked: (String, String) -> Unit,
    onMoveItemToToday: (String) -> Unit,
    onMoveItemToCompleted: (String) -> Unit,
    dragPreviewTarget: DragTarget,
    onDragPreviewTargetChange: (DragTarget) -> Unit,
    modifier: Modifier = Modifier,
) {
    PaperNoteCard(modifier = modifier) {
        Text("待办来源池", style = MaterialTheme.typography.titleMedium, color = BoardTitleColor)
        val hintText = when (dragPreviewTarget) {
            DragTarget.TODAY -> "松手将进入今日计划"
            DragTarget.DONE -> "松手将直接标记完成"
            DragTarget.NONE -> "直接左滑到计划，继续左滑到完成"
        }
        Text(hintText, style = MaterialTheme.typography.labelSmall, color = BoardHintColor)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(99.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0x00D9A97E), Color(0x55D9A97E), Color(0x66A5C49D)),
                    ),
                ),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("轻滑", style = MaterialTheme.typography.labelSmall, color = BoardHintColor)
            Text("到计划", style = MaterialTheme.typography.labelSmall, color = BoardHintColor)
            Text("到完成", style = MaterialTheme.typography.labelSmall, color = BoardHintColor)
        }
        if (items.isEmpty()) {
            Text("来源池已清空", color = Color(0xFF7B6B5A))
            return@PaperNoteCard
        }
        items.forEach { item ->
            var dragOffsetX by remember(item) { mutableStateOf(0f) }
            val isDragging = dragOffsetX < -2f
            val animatedOffsetX by animateFloatAsState(
                targetValue = dragOffsetX,
                animationSpec = spring(dampingRatio = 0.78f, stiffness = 420f),
                label = "animatedOffsetX",
            )
            val rowAlpha by animateFloatAsState(
                targetValue = if (dragOffsetX <= -165f) 0.82f else 1f,
                label = "rowAlpha",
            )
            val rowScale by animateFloatAsState(
                targetValue = if (dragOffsetX < -6f) 0.985f else 1f,
                animationSpec = spring(dampingRatio = 0.80f, stiffness = 420f),
                label = "rowScale",
            )
            val rowLift by animateFloatAsState(
                targetValue = if (dragOffsetX < -6f) -3f else 0f,
                animationSpec = spring(dampingRatio = 0.82f, stiffness = 380f),
                label = "rowLift",
            )
            RowWithDragFeedback(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset { IntOffset(animatedOffsetX.roundToInt(), 0) }
                    .alpha(rowAlpha)
                    .graphicsLayer {
                        scaleX = rowScale
                        scaleY = rowScale
                        translationY = rowLift
                    }
                    .pointerInput(item) {
                        detectHorizontalDragGestures(
                            onDragStart = {
                                onDragPreviewTargetChange(DragTarget.NONE)
                            },
                            onDragEnd = {
                                when {
                                    dragOffsetX <= DONE_SWIPE_THRESHOLD -> onMoveItemToCompleted(item)
                                    dragOffsetX <= TODAY_SWIPE_THRESHOLD -> onMoveItemToToday(item)
                                }
                                dragOffsetX = 0f
                                onDragPreviewTargetChange(DragTarget.NONE)
                            },
                            onDragCancel = {
                                dragOffsetX = 0f
                                onDragPreviewTargetChange(DragTarget.NONE)
                            },
                        ) { change, dragAmount ->
                            change.consume()
                            val proposed = dragOffsetX + dragAmount
                            dragOffsetX = applyDragResistance(proposed).coerceIn(-220f, 0f)
                            onDragPreviewTargetChange(
                                when {
                                    dragOffsetX <= DONE_SWIPE_THRESHOLD -> DragTarget.DONE
                                    dragOffsetX <= TODAY_SWIPE_THRESHOLD -> DragTarget.TODAY
                                    else -> DragTarget.NONE
                                },
                            )
                        }
                    },
                background = if (abs(dragOffsetX) > 0.5f) {
                    Brush.horizontalGradient(
                        listOf(
                            Color.Transparent,
                            if (dragOffsetX <= DONE_SWIPE_THRESHOLD) Color(0x3379A16E) else Color(0x33D9A97E),
                        ),
                    )
                } else {
                    Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isChecked(pageTitle, item)) tint.copy(alpha = 0.82f) else Color(0xFFF1ECE4))
                        .border(1.dp, Color(0xFFD5CEC2), RoundedCornerShape(4.dp))
                        .clickable { onToggleChecked(pageTitle, item) },
                )
                Text(item, color = Color(0xFF27231E), modifier = Modifier.weight(1f))
                Text(
                    "今日",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                        .clip(RoundedCornerShape(99.dp))
                        .background(Color(0x148F684F))
                        .clickable(enabled = !isDragging) { onMoveItemToToday(item) }
                        .padding(horizontal = 9.dp, vertical = 5.dp),
                )
                Text(
                    "完成",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                        .clip(RoundedCornerShape(99.dp))
                        .background(Color(0x146A9F68))
                        .clickable(enabled = !isDragging) { onMoveItemToCompleted(item) }
                        .padding(horizontal = 9.dp, vertical = 5.dp),
                )
            }
        }
    }
}

@Composable
private fun BoardStatChip(
    label: String,
    bg: Color,
) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        color = Color(0xFF5E4837),
        modifier = Modifier
            .clip(RoundedCornerShape(99.dp))
            .background(bg)
            .padding(horizontal = 11.dp, vertical = 6.dp),
    )
}

private fun applyDragResistance(offset: Float): Float {
    if (offset >= 0f) return offset
    return when {
        offset > -70f -> offset
        offset > -150f -> -70f + (offset + 70f) * 0.72f
        else -> -127.6f + (offset + 150f) * 0.56f
    }
}

@Composable
private fun RowWithDragFeedback(
    modifier: Modifier,
    background: Brush,
    verticalAlignment: Alignment.Vertical,
    horizontalArrangement: Arrangement.Horizontal,
    content: @Composable () -> Unit,
) {
    Row(
        modifier = modifier
            .shadow(1.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(background)
            .padding(horizontal = 5.dp, vertical = 3.dp)
            .animateContentSize(),
        verticalAlignment = verticalAlignment,
        horizontalArrangement = horizontalArrangement,
    ) {
        content()
    }
}

private enum class DragTarget {
    NONE,
    TODAY,
    DONE,
}

@Composable
private fun completedTextStyle(completed: Boolean): TextStyle =
    MaterialTheme.typography.bodyMedium.copy(
        textDecoration = if (completed) TextDecoration.LineThrough else TextDecoration.None,
    )

@Composable
private fun DiarySection(
    title: String,
    prompt: String,
    tint: Color,
    diaryDraft: String,
    pendingCommand: RichEditorCommand?,
    onCommand: (RichEditorCommand) -> Unit,
    onDiaryChange: (String) -> Unit,
    contentMode: PageContentMode,
    onContentModeChange: (PageContentMode) -> Unit,
) {
    val editingDiary = contentMode as? PageContentMode.EditingDiary
    var structured by remember(title, diaryDraft) { mutableStateOf(StructuredDiary.fromRaw(diaryDraft)) }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(text = prompt, style = MaterialTheme.typography.labelSmall, color = Color(0xFF6E665D))
        if (editingDiary?.title == title) {
            StructuredDiaryEditor(
                state = structured,
                onStateChange = { structured = it },
                onDone = {
                    onDiaryChange(structured.toRaw())
                    onContentModeChange(PageContentMode.Browsing)
                },
            )
        } else {
            PaperNoteCard(
                modifier = Modifier.clickable {
                    onContentModeChange(pageContentModeForTap(DiaryPage(title, prompt)))
                },
            ) {
                StructuredDiaryPreview(state = StructuredDiary.fromRaw(diaryDraft))
            }
        }
        Text(text = BookStrings.diaryLocalOnly, style = MaterialTheme.typography.labelSmall, color = tint.copy(alpha = 0.62f))
    }
}

private data class StructuredDiary(
    val moodTags: String,
    val todayDone: String,
    val workTasks: String,
    val smallJoy: String,
    val canImprove: String,
    val photoNotes: String,
) {
    fun toRaw(): String = buildString {
        appendLine("# 心情标签")
        appendLine(moodTags.trim())
        appendLine("# 今日完成")
        appendLine(todayDone.trim())
        appendLine("# 工作任务")
        appendLine(workTasks.trim())
        appendLine("# 小幸福")
        appendLine(smallJoy.trim())
        appendLine("# 可改进")
        appendLine(canImprove.trim())
        appendLine("# 图片")
        append(photoNotes.trim())
    }

    companion object {
        fun fromRaw(raw: String): StructuredDiary {
            if (raw.isBlank()) return StructuredDiary("", "", "", "", "", "")
            fun section(name: String, next: String?): String {
                val start = raw.indexOf("# $name")
                if (start < 0) return ""
                val bodyStart = raw.indexOf('\n', start).takeIf { it >= 0 }?.plus(1) ?: return ""
                val bodyEnd = next?.let { marker ->
                    raw.indexOf("# $marker", bodyStart).takeIf { it >= 0 }
                } ?: raw.length
                return raw.substring(bodyStart, bodyEnd).trim()
            }
            return StructuredDiary(
                moodTags = section("心情标签", "今日完成"),
                todayDone = section("今日完成", "工作任务"),
                workTasks = section("工作任务", "小幸福"),
                smallJoy = section("小幸福", "可改进"),
                canImprove = section("可改进", "图片"),
                photoNotes = section("图片", null),
            )
        }
    }
}

@Composable
private fun StructuredDiaryEditor(
    state: StructuredDiary,
    onStateChange: (StructuredDiary) -> Unit,
    onDone: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("4月4日", style = MaterialTheme.typography.labelLarge, color = Color(0xFF3A342E))
            Text("周四", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8A8177))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                OutlinedTextField(
                    value = state.moodTags,
                    onValueChange = { onStateChange(state.copy(moodTags = it)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    label = { Text("心情标签（空格/逗号分隔）") },
                    singleLine = true,
                )
                DiaryEditField("☀️ 今日完成", state.todayDone) { onStateChange(state.copy(todayDone = it)) }
                DiaryEditField("📚 工作任务", state.workTasks) { onStateChange(state.copy(workTasks = it)) }
            }
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(Color(0x18B7A893)),
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                DiaryEditField("🍀 小幸福", state.smallJoy) { onStateChange(state.copy(smallJoy = it)) }
                DiaryEditField("📝 可改进", state.canImprove) { onStateChange(state.copy(canImprove = it)) }
                DiaryEditField("📷 图片描述", state.photoNotes) { onStateChange(state.copy(photoNotes = it)) }
            }
        }
        TextButton(onClick = onDone) { Text("完成") }
    }
}

@Composable
private fun StructuredDiaryPreview(state: StructuredDiary) {
    val moodItems = remember(state.moodTags) {
        state.moodTags.split(',', '，', ' ').map(String::trim).filter(String::isNotBlank).take(6)
    }
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("4月4日", style = MaterialTheme.typography.labelLarge, color = Color(0xFF3A342E))
                Text("周四", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8A8177))
            }
            if (moodItems.isNotEmpty()) {
                Text(
                    moodItems.joinToString("  ") { "#$it" },
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF8B7A68),
                )
            }
            DiaryLine("☀️ 今日完成", state.todayDone)
            DiaryLine("📚 工作任务", state.workTasks)
        }
        Box(
            modifier = Modifier
                .width(1.dp)
                .fillMaxHeight()
                .background(Color(0x18B7A893)),
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            DiaryLine("🍀 小幸福", state.smallJoy)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0x14E9AFC0))
                    .border(1.dp, Color(0x2AE9AFC0), RoundedCornerShape(8.dp))
                    .padding(horizontal = 6.dp, vertical = 5.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text("📝 可改进", style = MaterialTheme.typography.labelSmall, color = Color(0xFF5A4A3B))
                    Text(
                        state.canImprove.ifBlank { "记录今天想优化的一件小事" },
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF2F2922),
                    )
                }
            }
            val photos = state.photoNotes.lines().map(String::trim).filter(String::isNotBlank)
            if (photos.isNotEmpty()) {
                Text("📷 图片", style = MaterialTheme.typography.labelSmall, color = Color(0xFF5A4A3B))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                    photos.take(2).forEach { note ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(62.dp)
                                .clip(RoundedCornerShape(7.dp))
                                .background(Color(0xFFF2EFE9))
                                .border(1.dp, Color(0xFFE6DED2), RoundedCornerShape(7.dp))
                                .padding(5.dp),
                        ) {
                            Text(note, style = MaterialTheme.typography.labelSmall, color = Color(0xFF6B6258))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DiaryBlock(title: String, content: String) {
    if (content.isBlank()) return
    DiarySticker(title)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0x1AF4DABB), Color(0x0EF4DABB), Color(0x14F6E8D3)),
                ),
            )
            .border(1.dp, Color(0x22C8AF91), RoundedCornerShape(12.dp))
            .padding(horizontal = 9.dp, vertical = 7.dp),
    ) {
        Text(content, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF342C24))
    }
}

@Composable
private fun DiaryLine(title: String, content: String) {
    if (content.isBlank()) return
    Text(title, style = MaterialTheme.typography.labelMedium, color = Color(0xFF5A4A3B))
    content.lines().map(String::trim).filter(String::isNotBlank).take(3).forEachIndexed { index, line ->
        Text("${index + 1}. $line", style = MaterialTheme.typography.bodySmall, color = Color(0xFF2F2922))
    }
}

@Composable
private fun DiaryEditField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
) {
    Text(label, style = MaterialTheme.typography.labelSmall, color = Color(0xFF5A4A3B))
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        minLines = 1,
        maxLines = 3,
        shape = RoundedCornerShape(8.dp),
    )
}

@Composable
private fun NotebookSpread(content: @Composable ColumnScope.() -> Unit) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFFFFFEFB), Color(0xFFFFFCF7), Color(0xFFFEF8EF)),
                ),
            )
            .border(1.dp, Color(0xFFE8DFD3), RoundedCornerShape(18.dp))
            .padding(12.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(Brush.horizontalGradient(listOf(Color(0x22C6B8A5), Color.Transparent, Color(0x22C6B8A5)))),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Brush.horizontalGradient(listOf(Color.Transparent, Color(0x18BFA991), Color.Transparent))),
        )
        content()
    }
}

@Composable
private fun DiarySticker(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = Color(0xFF5B4A3C),
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0x33E9D6BC))
            .padding(horizontal = 8.dp, vertical = 4.dp),
    )
}

@Composable
private fun SectionField(value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        minLines = 2,
        maxLines = 5,
    )
}

@Composable
private fun PaperNoteCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(5.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFFEFEFE))
            .border(1.dp, Color(0xFFEDE7DE), RoundedCornerShape(14.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0x10A17856)))
        content()
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0x08A17856)))
    }
}

@Composable
private fun PageHeaderLine(
    bookTitle: String,
    subtitle: String,
    tint: Color,
    savedText: String,
    onSavedClick: (() -> Unit)? = null,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(99.dp)).background(tint))
                Spacer(Modifier.width(6.dp))
                Text(bookTitle, style = MaterialTheme.typography.titleSmall, color = Color(0xFF342C24))
            }
            Text(
                text = savedText,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF8B7660),
                modifier = if (onSavedClick != null) Modifier.clickable(onClick = onSavedClick) else Modifier,
            )
        }
        Text(subtitle, style = MaterialTheme.typography.labelSmall, color = Color(0xFF7B6A59))
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0x1A9C7C5C)))
    }
}

@Composable
private fun DiaryToolChip(
    label: String,
    onClick: () -> Unit,
) {
    Text(
        text = label,
        color = Color(0xFF8F684F),
        modifier = Modifier
            .clip(RoundedCornerShape(99.dp))
            .background(Color(0x1A8F684F))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    )
}
