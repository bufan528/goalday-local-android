package com.bf410.goaldaylocal.ui.book

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bf410.goaldaylocal.data.BookPage
import com.bf410.goaldaylocal.data.DiaryPage
import com.bf410.goaldaylocal.data.PlanPage
import com.bf410.goaldaylocal.data.SchedulePage
import com.bf410.goaldaylocal.data.TargetPage
import com.bf410.goaldaylocal.data.TopicBook

private val bookPalette = listOf(
    Color(0xFFF2C0A5),
    Color(0xFFF1A5B6),
    Color(0xFFFFAA5F),
    Color(0xFFBBD1AD),
    Color(0xFF9EAADB),
)

private enum class BookSegment(val label: String) {
    WEEK("周视图"),
    DIARY("日记"),
    LIST("清单"),
}

@Composable
fun BookHomeScreen(
    viewModel: BookViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var showPageDialog by remember { mutableStateOf(false) }
    var showRenamePageDialog by remember { mutableStateOf(false) }
    var showEditBookDialog by remember { mutableStateOf(false) }
    var showInspiration by remember { mutableStateOf(false) }
    var selectedTemplateIndex by remember { mutableStateOf(0) }

    if (uiState.inLibraryMode) {
        LibraryView(
            books = uiState.books,
            customBookCount = uiState.customBookCount,
            onOpenBook = viewModel::openBook,
            onCreateBook = { showCreateDialog = true },
        )
    } else {
        val book = uiState.books[uiState.selectedBookIndex]
        val currentPage = book.pages[uiState.selectedPageIndex]
        val previousPage = book.pages.getOrNull(uiState.selectedPageIndex - 1)
        val nextPage = book.pages.getOrNull(uiState.selectedPageIndex + 1)
        if (showInspiration) {
            InspirationCenterView(
                templates = InspirationTemplates.all,
                selectedIndex = selectedTemplateIndex,
                onSelect = { selectedTemplateIndex = it },
                onBack = { showInspiration = false },
                onApply = { items ->
                    viewModel.applyInspirationTemplate(items)
                    showInspiration = false
                },
            )
        } else {
            BookDetailView(
                viewModel = viewModel,
                book = book,
                currentPage = currentPage,
                previousPage = previousPage,
                nextPage = nextPage,
                uiState = uiState,
                onBackToLibrary = viewModel::openLibrary,
                onShowAddPage = { showPageDialog = true },
                onShowRenamePage = { showRenamePageDialog = true },
                onShowEditBook = { showEditBookDialog = true },
                onShowInspiration = { showInspiration = true },
            )
        }
    }

    if (showCreateDialog) {
        CreateBookDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { title, subtitle, color ->
                viewModel.createCustomBook(title, subtitle, color)
                showCreateDialog = false
            },
        )
    }

    if (showPageDialog) {
        CreatePageDialog(
            onDismiss = { showPageDialog = false },
            onConfirm = { type, title ->
                viewModel.addPage(type, title)
                showPageDialog = false
            },
        )
    }

    if (showRenamePageDialog) {
        RenamePageDialog(
            currentTitle = uiState.books[uiState.selectedBookIndex].pages[uiState.selectedPageIndex].title,
            onDismiss = { showRenamePageDialog = false },
            onConfirm = { title ->
                viewModel.renameCurrentPage(title)
                showRenamePageDialog = false
            },
        )
    }

    if (showEditBookDialog) {
        val book = uiState.books[uiState.selectedBookIndex]
        EditBookDialog(
            book = book,
            onDismiss = { showEditBookDialog = false },
            onConfirm = { title, subtitle, color ->
                viewModel.updateCurrentBookInfo(title, subtitle, color)
                showEditBookDialog = false
            },
        )
    }
}

@Composable
private fun LibraryView(
    books: List<TopicBook>,
    customBookCount: Int,
    onOpenBook: (Int) -> Unit,
    onCreateBook: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 8.dp),
    ) {
        Text(
            text = BookStrings.appTitle,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 18.dp),
        )
        Text(
            text = BookStrings.librarySubtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF6F675D),
        )
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = BookStrings.librarySummary.format(books.size, customBookCount),
                color = Color(0xFF6F675D),
            )
            Text(
                text = BookStrings.createBook,
                color = Color(0xFF8F684F),
                modifier = Modifier
                    .clip(RoundedCornerShape(99.dp))
                    .background(Color(0x1A8F684F))
                    .clickable(onClick = onCreateBook)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            )
        }
        Spacer(Modifier.height(18.dp))
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.verticalScroll(rememberScrollState()),
        ) {
            books.forEachIndexed { index, book ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(book.color.copy(alpha = 0.92f), book.color.copy(alpha = 0.58f)),
                                start = Offset.Zero,
                                end = Offset(900f, 700f),
                            ),
                        )
                        .clickable { onOpenBook(index) }
                        .padding(horizontal = 22.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(book.title, style = MaterialTheme.typography.titleLarge, color = Color(0xFF2F261D))
                        Text(book.subtitle, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF4B3D31))
                        Text("${book.pages.size} ${BookStrings.pageUnit}", style = MaterialTheme.typography.labelLarge, color = Color(0xFF5D4B3D))
                    }
                    Text(BookStrings.openBook, color = Color(0xFF5D4B3D))
                }
            }
        }
    }
}

@Composable
private fun BookDetailView(
    viewModel: BookViewModel,
    book: TopicBook,
    currentPage: BookPage,
    previousPage: BookPage?,
    nextPage: BookPage?,
    uiState: BookUiState,
    onBackToLibrary: () -> Unit,
    onShowAddPage: () -> Unit,
    onShowRenamePage: () -> Unit,
    onShowEditBook: () -> Unit,
    onShowInspiration: () -> Unit,
) {
    var segment by remember(book.id) { mutableStateOf(resolveSegment(currentPage)) }
    val filteredPages = remember(book.pages, segment) {
        book.pages.filter { page ->
            matchesSegment(page, segment)
        }.ifEmpty { book.pages }
    }
    val segmentPageIndex = filteredPages.indexOfFirst { it.title == currentPage.title }.coerceAtLeast(0)
    var segmentSwipeDistance by remember(book.id) { mutableStateOf(0f) }

    fun switchSegment(next: BookSegment) {
        segment = next
        val firstIndex = book.pages.indexOfFirst { page -> matchesSegment(page, next) }
        if (firstIndex >= 0) viewModel.setPage(firstIndex)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("18周", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Text(
                "完成",
                color = Color.White,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier
                    .clip(RoundedCornerShape(99.dp))
                    .background(Color(0xFF222222))
                    .clickable(onClick = onBackToLibrary)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BookSegment.entries.forEach { item ->
                Text(
                    text = item.label,
                    color = if (item == segment) Color(0xFF2A261F) else Color(0xFF9E978D),
                    fontWeight = if (item == segment) FontWeight.SemiBold else FontWeight.Normal,
                    modifier = Modifier.clickable {
                        switchSegment(item)
                    },
                )
                if (item != BookSegment.entries.last()) {
                    Text("｜", color = Color(0xFFD2CBC1))
                }
            }
            Spacer(Modifier.weight(1f))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState()),
            ) {
                ActionChip(label = "灵感中心", color = Color(0xFF8F684F), onClick = onShowInspiration)
                if (book.id.startsWith("custom_")) {
                    ActionChip(label = BookStrings.editBook, color = Color(0xFF8F684F), onClick = onShowEditBook)
                    ActionChip(label = BookStrings.addPage, color = Color(0xFF8F684F), onClick = onShowAddPage)
                    ActionChip(label = BookStrings.renamePage, color = Color(0xFF8F684F), onClick = onShowRenamePage)
                    ActionChip(label = BookStrings.moveLeft, color = Color(0xFF8F684F), onClick = viewModel::moveCurrentPageLeft)
                    ActionChip(label = BookStrings.moveRight, color = Color(0xFF8F684F), onClick = viewModel::moveCurrentPageRight)
                    ActionChip(label = BookStrings.deletePage, color = Color(0xFF9C5A52), onClick = viewModel::deleteCurrentPage)
                    ActionChip(label = BookStrings.deleteBook, color = Color(0xFF9C5A52), onClick = viewModel::removeCurrentCustomBook)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .pointerInput(segment) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { _, dragAmount ->
                            segmentSwipeDistance += dragAmount
                        },
                        onDragEnd = {
                            if (segmentSwipeDistance <= -68f) {
                                switchSegment(nextSegment(segment))
                            } else if (segmentSwipeDistance >= 68f) {
                                switchSegment(previousSegment(segment))
                            }
                            segmentSwipeDistance = 0f
                        },
                        onDragCancel = { segmentSwipeDistance = 0f },
                    )
                },
        ) {
            filteredPages.forEachIndexed { idx, item ->
                val index = book.pages.indexOfFirst { it.title == item.title }.coerceAtLeast(0)
                Text(
                    text = item.title,
                    color = if (idx == segmentPageIndex) Color(0xFF2F261D) else Color(0xFF7A7065),
                    modifier = Modifier
                        .clip(RoundedCornerShape(99.dp))
                        .background(if (idx == segmentPageIndex) Color(0x4DB88A58) else Color(0x18FFFFFF))
                        .clickable { viewModel.setPage(index) }
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        BookReader(
            bookId = book.id,
            bookTitle = book.title,
            subtitle = book.subtitle,
            page = currentPage,
            previousPage = previousPage,
            nextPage = nextPage,
            pageIndex = uiState.selectedPageIndex,
            pageCount = book.pages.size,
            tint = book.color,
            isSaved = book.id in uiState.savedBookIds,
            diaryDraft = uiState.diaryDraft,
            customPageItems = uiState.customPageItems,
            weeklyTheme = uiState.weeklyTheme,
            todayPlanItems = uiState.todayPlanItems,
            todayCompletedItems = uiState.todayCompletedItems,
            schedulePreviewEntries = uiState.schedulePreviewEntries,
            onToggleSaved = viewModel::toggleSavedCurrentBook,
            isChecked = { pageTitle, item -> viewModel.isChecked(pageTitle, item) },
            onToggleChecked = { pageTitle, item -> viewModel.toggleChecked(pageTitle, item) },
            onDiaryChange = viewModel::updateDiaryDraft,
            onAddCustomItem = viewModel::addCustomPageItem,
            onAddCustomItemWithDeadline = viewModel::addCustomPageItemWithDeadline,
            onRemoveCustomItem = viewModel::removeCustomPageItem,
            onRenameCustomItem = viewModel::renameCustomPageItem,
            onAddToSchedule = viewModel::addItemToSchedule,
            onWeeklyThemeChange = viewModel::updateWeeklyTheme,
            onMoveItemToToday = viewModel::moveItemToToday,
            onMoveItemToCompleted = viewModel::moveItemToCompleted,
            onRestoreItemFromToday = viewModel::restoreItemFromToday,
            onRestoreItemFromCompleted = viewModel::restoreItemFromCompleted,
            onFlipNext = { if (uiState.selectedPageIndex < book.pages.lastIndex) viewModel.setPage(uiState.selectedPageIndex + 1) },
            onFlipPrevious = { if (uiState.selectedPageIndex > 0) viewModel.setPage(uiState.selectedPageIndex - 1) },
        )
    }
}

private fun matchesSegment(page: BookPage, segment: BookSegment): Boolean =
    when (segment) {
        BookSegment.WEEK -> page is TargetPage || page is PlanPage || page is SchedulePage
        BookSegment.DIARY -> page is DiaryPage
        BookSegment.LIST -> page is PlanPage || page is TargetPage
    }

private fun nextSegment(segment: BookSegment): BookSegment =
    when (segment) {
        BookSegment.WEEK -> BookSegment.DIARY
        BookSegment.DIARY -> BookSegment.LIST
        BookSegment.LIST -> BookSegment.WEEK
    }

private fun previousSegment(segment: BookSegment): BookSegment =
    when (segment) {
        BookSegment.WEEK -> BookSegment.LIST
        BookSegment.DIARY -> BookSegment.WEEK
        BookSegment.LIST -> BookSegment.DIARY
    }

private fun resolveSegment(page: BookPage): BookSegment =
    when (page) {
        is DiaryPage -> BookSegment.DIARY
        is PlanPage, is TargetPage -> BookSegment.LIST
        is SchedulePage -> BookSegment.WEEK
    }

@Composable
private fun InspirationCenterView(
    templates: List<InspirationTemplate>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    onBack: () -> Unit,
    onApply: (List<String>) -> Unit,
) {
    val selected = templates[selectedIndex.coerceIn(0, templates.lastIndex)]
    var checkedStates by remember(selected.title) { mutableStateOf(List(selected.items.size) { true }) }
    var editableItems by remember(selected.title) { mutableStateOf(selected.items) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("灵感中心", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("返回", color = Color(0xFF8F684F), modifier = Modifier.clickable(onClick = onBack))
                Text(
                    "完成",
                    color = Color.White,
                    modifier = Modifier
                        .clip(RoundedCornerShape(99.dp))
                        .background(Color(0xFF222222))
                        .clickable {
                            val picked = editableItems.filterIndexed { index, _ ->
                                checkedStates.getOrNull(index) == true
                            }
                            onApply(picked)
                        }
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                templates.forEachIndexed { index, item ->
                    Column(
                        modifier = Modifier
                            .width(170.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(if (index == selectedIndex) Color(0xFFF4ECE2) else Color(0x66FFFFFF))
                            .clickable { onSelect(index) }
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(item.title, color = Color(0xFF2F261D), style = MaterialTheme.typography.titleMedium)
                        Text(item.subtitle, color = Color(0xFF6F675D), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xF9FFFFFF))
                .padding(14.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(selected.title, style = MaterialTheme.typography.titleLarge, color = Color(0xFF2F261D))
                editableItems.forEachIndexed { index, item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (checkedStates.getOrNull(index) == true) Color(0xFF96C08B) else Color(0xFFF1ECE4))
                                .clickable {
                                    checkedStates = checkedStates.toMutableList().also { list ->
                                        list[index] = !list[index]
                                    }
                                },
                        )
                        OutlinedTextField(
                            value = item,
                            onValueChange = { value ->
                                editableItems = editableItems.toMutableList().also { list -> list[index] = value }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    templates.take(4).forEachIndexed { idx, card ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(64.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.linearGradient(
                                        when (idx % 4) {
                                            0 -> listOf(Color(0xFF788B4F), Color(0xFF4E6633))
                                            1 -> listOf(Color(0xFF8A6847), Color(0xFF61482F))
                                            2 -> listOf(Color(0xFF7C5A4A), Color(0xFF5E4236))
                                            else -> listOf(Color(0xFF5D4B3D), Color(0xFF3E3228))
                                        },
                                    ),
                                )
                                .clickable { onSelect(idx) }
                                .padding(7.dp),
                        ) {
                            Text(
                                text = card.title,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                modifier = Modifier.align(Alignment.BottomStart),
                            )
                        }
                    }
                }
                Button(
                    onClick = {
                        val picked = editableItems.filterIndexed { index, _ -> checkedStates.getOrNull(index) == true }
                        onApply(picked)
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("应用到当前页") }
            }
        }
    }
}

@Composable
private fun CreateBookDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, subtitle: String, color: Color) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var subtitle by remember { mutableStateOf("") }
    var colorIndex by remember { mutableStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(title.trim(), subtitle.trim(), bookPalette[colorIndex])
                    }
                },
            ) {
                Text(BookStrings.create)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(BookStrings.cancel) } },
        title = { Text(BookStrings.createBookTitle) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text(BookStrings.bookName) }, singleLine = true)
                OutlinedTextField(value = subtitle, onValueChange = { subtitle = it }, label = { Text(BookStrings.subtitle) }, singleLine = true)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    bookPalette.forEachIndexed { index, color ->
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(99.dp))
                                .background(color)
                                .clickable { colorIndex = index }
                                .shadow(if (colorIndex == index) 8.dp else 0.dp, RoundedCornerShape(99.dp)),
                        )
                    }
                }
            }
        },
    )
}

@Composable
private fun ActionChip(
    label: String,
    color: Color,
    onClick: () -> Unit,
) {
    val isDanger = color == Color(0xFF9C5A52)
    val textColor = if (isDanger) Color(0xFF7A2F2F) else Color(0xFF2D2A26)
    val bgColor = if (isDanger) Color(0x14D17878) else Color(0x12000000)
    Text(
        text = label,
        color = textColor,
        modifier = Modifier
            .clip(RoundedCornerShape(99.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp),
    )
}

@Composable
private fun EditBookDialog(
    book: TopicBook,
    onDismiss: () -> Unit,
    onConfirm: (title: String, subtitle: String, color: Color) -> Unit,
) {
    var title by remember(book.title) { mutableStateOf(book.title) }
    var subtitle by remember(book.subtitle) { mutableStateOf(book.subtitle) }
    var colorIndex by remember { mutableStateOf(bookPalette.indexOfFirst { it == book.color }.coerceAtLeast(0)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(title.trim(), subtitle.trim(), bookPalette[colorIndex])
                    }
                },
            ) {
                Text(BookStrings.save)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(BookStrings.cancel) } },
        title = { Text(BookStrings.editBookTitle) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text(BookStrings.bookName) }, singleLine = true)
                OutlinedTextField(value = subtitle, onValueChange = { subtitle = it }, label = { Text(BookStrings.subtitle) }, singleLine = true)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    bookPalette.forEachIndexed { index, color ->
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(99.dp))
                                .background(color)
                                .clickable { colorIndex = index }
                                .shadow(if (colorIndex == index) 8.dp else 0.dp, RoundedCornerShape(99.dp)),
                        )
                    }
                }
            }
        },
    )
}

@Composable
private fun CreatePageDialog(
    onDismiss: () -> Unit,
    onConfirm: (type: String, title: String) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("target") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = { if (title.isNotBlank()) onConfirm(type, title) }) {
                Text(BookStrings.createPage)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(BookStrings.cancel) } },
        title = { Text(BookStrings.addPageTitle) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text(BookStrings.pageTitle) }, singleLine = true)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf(
                        "target" to "目标",
                        "plan" to "计划",
                        "schedule" to "日程",
                        "diary" to "日记",
                    ).forEach { (key, label) ->
                        Text(
                            text = label,
                            color = if (type == key) Color(0xFF2F261D) else Color(0xFF7A7065),
                            modifier = Modifier
                                .clip(RoundedCornerShape(99.dp))
                                .background(if (type == key) Color(0x33B88A58) else Color.Transparent)
                                .clickable { type = key }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                        )
                    }
                }
            }
        },
    )
}

@Composable
private fun RenamePageDialog(
    currentTitle: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var title by remember(currentTitle) { mutableStateOf(currentTitle) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { Button(onClick = { if (title.isNotBlank()) onConfirm(title.trim()) }) { Text(BookStrings.save) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(BookStrings.cancel) } },
        title = { Text(BookStrings.renamePageTitle) },
        text = { OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text(BookStrings.pageTitle) }, singleLine = true) },
    )
}
