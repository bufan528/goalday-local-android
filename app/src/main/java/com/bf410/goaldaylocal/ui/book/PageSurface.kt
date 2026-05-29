package com.bf410.goaldaylocal.ui.book

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.bf410.goaldaylocal.data.BookPage
import com.bf410.goaldaylocal.data.DiaryPage
import com.bf410.goaldaylocal.data.PlanPage
import com.bf410.goaldaylocal.data.SchedulePage
import com.bf410.goaldaylocal.data.TargetPage
import java.time.LocalDate

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
    onToggleSaved: () -> Unit,
    isChecked: (String, String) -> Boolean,
    onToggleChecked: (String, String) -> Unit,
    onDiaryChange: (String) -> Unit,
    onAddCustomItem: (String) -> Unit,
    onRemoveCustomItem: (String) -> Unit,
    onRenameCustomItem: (String, String) -> Unit,
    onAddToSchedule: (String, Int) -> Unit,
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
            is TargetPage -> EditableBulletPage(page.title, page.items, customPageItems, tint, BookStrings.addTarget, isChecked, onToggleChecked, onAddCustomItem, onRemoveCustomItem, onRenameCustomItem, onAddToSchedule, contentMode, onContentModeChange)
            is SchedulePage -> EditableBulletPage(page.title, page.items, customPageItems, tint.copy(alpha = 0.74f), BookStrings.addSchedule, isChecked, onToggleChecked, onAddCustomItem, onRemoveCustomItem, onRenameCustomItem, onAddToSchedule, contentMode, onContentModeChange)
            is PlanPage -> EditableBulletPage(page.title, page.items, customPageItems, Color(0xFFB88A58), BookStrings.addPlan, isChecked, onToggleChecked, onAddCustomItem, onRemoveCustomItem, onRenameCustomItem, onAddToSchedule, contentMode, onContentModeChange)
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
    contentMode: PageContentMode,
    onContentModeChange: (PageContentMode) -> Unit,
) {
    var newItem by remember(pageTitle) { mutableStateOf("") }
    var editedBaseItems by remember(pageTitle, baseItems) { mutableStateOf(baseItems) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        editedBaseItems.forEach { item ->
            ActionBulletRow(
                pageTitle = pageTitle,
                item = item,
                tint = tint,
                checked = isChecked(pageTitle, item),
                removable = false,
                onToggleChecked = onToggleChecked,
                onAddToSchedule = onAddToSchedule,
                onRemoveCustomItem = onRemoveCustomItem,
                onRenameCustomItem = onRenameCustomItem,
                contentMode = contentMode,
                onContentModeChange = onContentModeChange,
                onRenameDisplayedItem = { oldItem, replacement ->
                    editedBaseItems = renameDisplayedChecklistItem(editedBaseItems, oldItem, replacement)
                },
            )
        }
        if (customItems.isNotEmpty()) {
            Text(BookStrings.myContent, style = MaterialTheme.typography.titleSmall, color = Color(0xFF5E4837))
            customItems.forEach { item ->
                ActionBulletRow(
                    pageTitle = pageTitle,
                    item = item,
                    tint = tint,
                    checked = isChecked(pageTitle, item),
                    removable = true,
                    onToggleChecked = onToggleChecked,
                    onAddToSchedule = onAddToSchedule,
                    onRemoveCustomItem = onRemoveCustomItem,
                    onRenameCustomItem = onRenameCustomItem,
                    contentMode = contentMode,
                    onContentModeChange = onContentModeChange,
                    onRenameDisplayedItem = { _, _ -> },
                )
            }
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
    onAddToSchedule: (String, Int) -> Unit,
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
                    text = BookStrings.addToCalendar,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF8F684F),
                    modifier = Modifier
                        .clip(RoundedCornerShape(99.dp))
                        .background(Color(0x1A8F684F))
                        .clickable { onAddToSchedule(item, LocalDate.now().dayOfMonth) }
                        .padding(horizontal = 8.dp, vertical = 7.dp),
                )
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
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFFFFFCF6), Color(0xFFFAF2E8), Color(0xFFF5E7D6))))
            .padding(horizontal = 16.dp, vertical = 14.dp),
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
