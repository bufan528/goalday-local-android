package com.bf410.goaldaylocal.ui.book

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.bf410.goaldaylocal.data.SchedulePage
import com.bf410.goaldaylocal.data.TargetPage
import kotlin.math.abs
import kotlin.math.roundToInt

private val BoardTonePlan = Color(0x22D9A97E)
private val BoardToneDone = Color(0x22A5C49D)
private val BoardTitleColor = Color(0xFF5E4837)
private val BoardHintColor = Color(0xFF8B7A68)

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
            .background(Brush.horizontalGradient(listOf(Color(0xFFFFFEFA), Color(0xFFF8F1E7), Color(0xFFEEDBC5))))
            .padding(horizontal = 30.dp, vertical = 28.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0x14FFFFFF),
                            Color.Transparent,
                            Color(0x0F9D7456),
                        ),
                    ),
                ),
        )

        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .width(10.dp)
                .fillMaxHeight()
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0x18000000), Color.Transparent),
                    ),
                ),
        )

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
            Spacer(Modifier.height(16.dp))
            Text(text = title, style = MaterialTheme.typography.headlineSmall, color = Color(0xFF2B241D))
            Spacer(Modifier.height(18.dp))
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
) {
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
    onToggleSaved: () -> Unit,
    isChecked: (String, String) -> Boolean,
    onToggleChecked: (String, String) -> Unit,
    onDiaryChange: (String) -> Unit,
    onAddCustomItem: (String) -> Unit,
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
) {
    PageSurface(
        modifier = modifier,
        title = page.title,
        pageNumber = if (isSaved) BookStrings.savedBook else BookStrings.saveBook,
        headerTitle = bookTitle,
        headerSubtitle = subtitle,
        tint = tint,
        onSavedClick = onToggleSaved,
    ) {
        when (page) {
            is TargetPage -> EditableBulletPage(page.title, page.items, customPageItems, tint, BookStrings.addTarget, isChecked, onToggleChecked, onAddCustomItem, onRemoveCustomItem, onRenameCustomItem, onAddToSchedule, weeklyTheme, todayPlanItems, todayCompletedItems, onWeeklyThemeChange, onMoveItemToToday, onMoveItemToCompleted, onRestoreItemFromToday, onRestoreItemFromCompleted, contentMode, onContentModeChange)
            is SchedulePage -> EditableBulletPage(page.title, page.items, customPageItems, tint.copy(alpha = 0.74f), BookStrings.addSchedule, isChecked, onToggleChecked, onAddCustomItem, onRemoveCustomItem, onRenameCustomItem, onAddToSchedule, weeklyTheme, todayPlanItems, todayCompletedItems, onWeeklyThemeChange, onMoveItemToToday, onMoveItemToCompleted, onRestoreItemFromToday, onRestoreItemFromCompleted, contentMode, onContentModeChange)
            is PlanPage -> EditableBulletPage(page.title, page.items, customPageItems, Color(0xFFB88A58), BookStrings.addPlan, isChecked, onToggleChecked, onAddCustomItem, onRemoveCustomItem, onRenameCustomItem, onAddToSchedule, weeklyTheme, todayPlanItems, todayCompletedItems, onWeeklyThemeChange, onMoveItemToToday, onMoveItemToCompleted, onRestoreItemFromToday, onRestoreItemFromCompleted, contentMode, onContentModeChange)
            is DiaryPage -> DiarySection(page.title, page.prompt, tint, diaryDraft, pendingCommand, onCommand, onDiaryChange, contentMode, onContentModeChange)
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
private fun EditableBulletPage(
    pageTitle: String,
    baseItems: List<String>,
    customItems: List<String>,
    tint: Color,
    inputLabel: String,
    isChecked: (String, String) -> Boolean,
    onToggleChecked: (String, String) -> Unit,
    onAddCustomItem: (String) -> Unit,
    onRemoveCustomItem: (String) -> Unit,
    onRenameCustomItem: (String, String) -> Unit,
    onAddToSchedule: (String, Int) -> Unit,
    weeklyTheme: String,
    todayPlanItems: List<String>,
    todayCompletedItems: List<String>,
    onWeeklyThemeChange: (String) -> Unit,
    onMoveItemToToday: (String) -> Unit,
    onMoveItemToCompleted: (String) -> Unit,
    onRestoreItemFromToday: (String) -> Unit,
    onRestoreItemFromCompleted: (String) -> Unit,
    contentMode: PageContentMode,
    onContentModeChange: (PageContentMode) -> Unit,
) {
    var newItem by remember(pageTitle) { mutableStateOf("") }
    var editedBaseItems by remember(pageTitle, baseItems) { mutableStateOf(baseItems) }
    val stagedItems = remember(todayPlanItems, todayCompletedItems) { (todayPlanItems + todayCompletedItems).toSet() }
    val sourceBaseItems = remember(editedBaseItems, stagedItems) { editedBaseItems.filterNot { it in stagedItems } }
    val sourceCustomItems = remember(customItems, stagedItems) { customItems.filterNot { it in stagedItems } }
    val sourceItems = sourceBaseItems + sourceCustomItems
    var dragPreviewTarget by remember(pageTitle) { mutableStateOf(DragTarget.NONE) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        WeekThemeSection(theme = weeklyTheme, onThemeChange = onWeeklyThemeChange)
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

        sourceBaseItems.forEach { item ->
            ActionBulletRow(
                pageTitle = pageTitle,
                item = item,
                tint = tint,
                checked = isChecked(pageTitle, item),
                removable = false,
                onToggleChecked = onToggleChecked,
                onRemoveCustomItem = onRemoveCustomItem,
                onRenameCustomItem = onRenameCustomItem,
                contentMode = contentMode,
                onContentModeChange = onContentModeChange,
                onRenameDisplayedItem = { oldItem, replacement ->
                    editedBaseItems = renameDisplayedChecklistItem(editedBaseItems, oldItem, replacement)
                },
            )
        }
        sourceCustomItems.forEach { item ->
            ActionBulletRow(
                pageTitle = pageTitle,
                item = item,
                tint = tint,
                checked = isChecked(pageTitle, item),
                removable = true,
                onToggleChecked = onToggleChecked,
                onRemoveCustomItem = onRemoveCustomItem,
                onRenameCustomItem = onRenameCustomItem,
                contentMode = contentMode,
                onContentModeChange = onContentModeChange,
                onRenameDisplayedItem = { _, _ -> },
            )
        }
        PaperNoteCard {
            Text(BookStrings.editHint, style = MaterialTheme.typography.bodySmall, color = Color(0xFF7B6B5A))
            OutlinedTextField(
                value = newItem,
                onValueChange = { newItem = it },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                label = { Text(inputLabel) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            )
            TextButton(onClick = {
                onAddCustomItem(newItem)
                newItem = ""
            }) {
                Text(BookStrings.save)
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
        Text("今日执行看板", style = MaterialTheme.typography.titleMedium, color = Color(0xFF5E4837))
        Text("长按右侧任务拖到计划/完成区", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B7A68))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BoardStatChip(label = "计划 ${todayPlanItems.size}", bg = BoardTonePlan)
            BoardStatChip(label = "完成 ${todayCompletedItems.size}", bg = BoardToneDone)
        }
        if (todayPlanItems.isEmpty() && todayCompletedItems.isEmpty()) {
            Text("从右侧清单拖入（点击）到今日执行或完成区。", color = Color(0xFF7B6B5A))
            return@PaperNoteCard
        }
        Text(
            "今日计划",
            color = Color(0xFF5E4837),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(todayHeaderColor)
                .border(
                    width = 1.dp,
                    color = Color(0xCCB77A5A).copy(alpha = todayBorderAlpha),
                    shape = RoundedCornerShape(10.dp),
                )
                .padding(horizontal = 6.dp, vertical = 4.dp),
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.animateContentSize(),
        ) {
            todayPlanItems.forEach { item ->
                Text(
                    text = "• $item",
                    modifier = Modifier
                        .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                        .background(BoardTonePlan)
                    .clickable { onMoveItemToCompleted(item) }
                    .padding(horizontal = 12.dp, vertical = 9.dp),
            )
            }
        }
        Text(
            "今日完成",
            color = Color(0xFF5E4837),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(doneHeaderColor)
                .border(
                    width = 1.dp,
                    color = Color(0xCC79A16E).copy(alpha = doneBorderAlpha),
                    shape = RoundedCornerShape(10.dp),
                )
                .padding(horizontal = 6.dp, vertical = 4.dp),
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.animateContentSize(),
        ) {
            todayCompletedItems.forEach { item ->
                Text(
                    text = item,
                    style = completedTextStyle(completed = true),
                    color = Color(0xFF8B847D),
                    modifier = Modifier
                        .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                        .background(BoardToneDone)
                    .clickable { onRestoreItemFromCompleted(item) }
                    .padding(horizontal = 12.dp, vertical = 9.dp),
            )
        }
        }
        if (todayPlanItems.isNotEmpty()) {
            TextButton(onClick = { todayPlanItems.forEach(onRestoreItemFromToday) }) {
                Text("清空今日计划区")
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
            DragTarget.NONE -> "左滑到计划，继续左滑到完成"
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
                        detectDragGesturesAfterLongPress(
                            onDragStart = {
                                onDragPreviewTargetChange(DragTarget.TODAY)
                            },
                            onDragEnd = {
                                when {
                                    dragOffsetX <= -165f -> onMoveItemToCompleted(item)
                                    dragOffsetX <= -82f -> onMoveItemToToday(item)
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
                            val proposed = dragOffsetX + dragAmount.x
                            dragOffsetX = applyDragResistance(proposed).coerceIn(-220f, 0f)
                            onDragPreviewTargetChange(
                                when {
                                    dragOffsetX <= -165f -> DragTarget.DONE
                                    dragOffsetX <= -82f -> DragTarget.TODAY
                                    else -> DragTarget.NONE
                                },
                            )
                        }
                    },
                background = if (abs(dragOffsetX) > 0.5f) {
                    Brush.horizontalGradient(
                        listOf(
                            Color.Transparent,
                            if (dragOffsetX <= -165f) Color(0x3379A16E) else Color(0x33D9A97E),
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
                        .size(14.dp)
                        .clip(RoundedCornerShape(99.dp))
                        .background(if (isChecked(pageTitle, item)) tint else Color(0xFFE5DBCD))
                        .clickable { onToggleChecked(pageTitle, item) },
                )
                Text(item, modifier = Modifier.weight(1f))
                Text(
                    "今日",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                        .clip(RoundedCornerShape(99.dp))
                        .background(Color(0x1A8F684F))
                        .clickable(enabled = !isDragging) { onMoveItemToToday(item) }
                        .padding(horizontal = 9.dp, vertical = 5.dp),
                )
                Text(
                    "完成",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                        .clip(RoundedCornerShape(99.dp))
                        .background(Color(0x1A6A9F68))
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

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(text = prompt, style = MaterialTheme.typography.titleSmall, color = Color(0xFF342C24))
        if (editingDiary?.title == title) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DiaryToolChip("加粗") { onCommand(RichEditorCommand("bold")) }
                DiaryToolChip("标题") { onCommand(RichEditorCommand("formatBlock", "<h2>")) }
                DiaryToolChip("列表") { onCommand(RichEditorCommand("insertUnorderedList")) }
                DiaryToolChip("引用") { onCommand(RichEditorCommand("formatBlock", "<blockquote>")) }
                DiaryToolChip("完成") { onContentModeChange(PageContentMode.Browsing) }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFFFFFBF5))
                    .padding(18.dp),
            ) {
                RichDiaryEditor(
                    html = diaryDraft,
                    placeholder = BookStrings.diaryPlaceholder,
                    modifier = Modifier.fillMaxSize(),
                    pendingCommand = pendingCommand,
                    onHtmlChange = onDiaryChange,
                )
            }
        } else {
            PaperNoteCard(
                modifier = Modifier.clickable {
                    onContentModeChange(pageContentModeForTap(DiaryPage(title, prompt)))
                },
            ) {
                Text(
                    text = condensedPreviewText(
                        if (diaryDraft.isBlank()) BookStrings.diaryEmptyPreview else diaryDraft,
                        maxLength = 180,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (diaryDraft.isBlank()) Color(0xFF8E806F) else Color(0xFF342C24),
                )
            }
        }
        Text(
            text = BookStrings.diaryLocalOnly,
            style = MaterialTheme.typography.bodySmall,
            color = tint.copy(alpha = 0.72f),
        )
    }
}

@Composable
private fun PaperNoteCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFFFFFCF6), Color(0xFFFAF2E8), Color(0xFFF5E7D6))))
            .padding(horizontal = 16.dp, vertical = 13.dp),
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0x14A17856)))
        content()
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0x0EA17856)))
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
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(99.dp)).background(tint))
                Spacer(Modifier.width(8.dp))
                Text(bookTitle, style = MaterialTheme.typography.titleMedium, color = Color(0xFF342C24))
            }
            Text(
                text = savedText,
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF8B7660),
                modifier = if (onSavedClick != null) Modifier.clickable(onClick = onSavedClick) else Modifier,
            )
        }
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color(0xFF7B6A59))
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
