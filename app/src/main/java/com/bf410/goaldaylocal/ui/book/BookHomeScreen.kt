package com.bf410.goaldaylocal.ui.book

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
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
import com.bf410.goaldaylocal.ui.replica.GoaldaySegmentBar
import com.bf410.goaldaylocal.ui.replica.GoaldayTopBar

private val bookPalette = listOf(
    Color(0xFFF2C0A5),
    Color(0xFFF1A5B6),
    Color(0xFFFFAA5F),
    Color(0xFFBBD1AD),
    Color(0xFF9EAADB),
)

private enum class BookSegment(val label: String) {
    WEEK("日程"),
    DIARY("日记"),
    LIST("清单"),
}

enum class BookEntryMode {
    PLANNER,
    INSPIRATION,
    HANDBOOK,
}

@Composable
fun BookHomeScreen(
    viewModel: BookViewModel,
    entryMode: BookEntryMode = BookEntryMode.PLANNER,
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var showPageDialog by remember { mutableStateOf(false) }
    var showRenamePageDialog by remember { mutableStateOf(false) }
    var showEditBookDialog by remember { mutableStateOf(false) }
    var showManagePanel by remember { mutableStateOf(false) }
    var showInspiration by remember(entryMode) { mutableStateOf(entryMode == BookEntryMode.INSPIRATION) }
    var selectedTemplateIndex by remember { mutableStateOf(0) }

    val hasBooks = uiState.books.isNotEmpty()
    val safeBookIndex = uiState.selectedBookIndex.coerceIn(0, (uiState.books.lastIndex).coerceAtLeast(0))
    LaunchedEffect(entryMode, hasBooks, uiState.selectedBookIndex) {
        if (entryMode == BookEntryMode.HANDBOOK && hasBooks && uiState.selectedBookIndex != 0) {
            viewModel.selectBook(0)
        }
    }

    when (entryMode) {
        BookEntryMode.INSPIRATION -> {
            InspirationCenterView(
                templates = InspirationTemplates.all,
                selectedIndex = selectedTemplateIndex,
                onSelect = { selectedTemplateIndex = it },
                onBack = { },
                onApply = { items, pushToToday, clearSource ->
                    viewModel.applyInspirationTemplate(
                        items = items,
                        pushToToday = pushToToday,
                        clearSourceAfterApply = clearSource,
                    )
                },
                onSaveAsBook = { template, items ->
                    viewModel.createTemplateBook(template.title, template.subtitle, template.color, items)
                },
            )
        }

        BookEntryMode.HANDBOOK -> {
            if (!hasBooks) return
            if (uiState.selectedBookIndex != 0) return
            val book = uiState.books[safeBookIndex]
            val clampedPageIndex = uiState.selectedPageIndex.coerceIn(0, book.pages.lastIndex)
            val currentPage = book.pages[clampedPageIndex]
            val previousPage = book.pages.getOrNull(clampedPageIndex - 1)
            val nextPage = book.pages.getOrNull(clampedPageIndex + 1)
            BookDetailView(
                viewModel = viewModel,
                book = book,
                currentPage = currentPage,
                previousPage = previousPage,
                nextPage = nextPage,
                uiState = uiState,
                onBackToLibrary = { },
                onShowAddPage = { showPageDialog = true },
                onShowRenamePage = { showRenamePageDialog = true },
                onShowEditBook = { showEditBookDialog = true },
                onToggleManagePanel = { },
                showManagePanel = false,
                forcedSegment = null,
                bookOnlyMode = true,
                onShowInspiration = { },
            )
        }

        BookEntryMode.PLANNER -> {
            if (uiState.inLibraryMode) {
                LibraryView(
                    books = uiState.books,
                    customBookCount = uiState.customBookCount,
                    onOpenBook = viewModel::openBook,
                    onCreateBook = { showCreateDialog = true },
                )
            } else {
                if (!hasBooks) return
                val book = uiState.books[safeBookIndex]
                val clampedPageIndex = uiState.selectedPageIndex.coerceIn(0, book.pages.lastIndex)
                val currentPage = book.pages[clampedPageIndex]
                val previousPage = book.pages.getOrNull(clampedPageIndex - 1)
                val nextPage = book.pages.getOrNull(clampedPageIndex + 1)
                if (showInspiration) {
                    InspirationCenterView(
                        templates = InspirationTemplates.all,
                        selectedIndex = selectedTemplateIndex,
                        onSelect = { selectedTemplateIndex = it },
                        onBack = { showInspiration = false },
                        onApply = { items, pushToToday, clearSource ->
                            viewModel.applyInspirationTemplate(
                                items = items,
                                pushToToday = pushToToday,
                                clearSourceAfterApply = clearSource,
                            )
                            showInspiration = false
                        },
                        onSaveAsBook = { template, items ->
                            viewModel.createTemplateBook(template.title, template.subtitle, template.color, items)
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
                        onToggleManagePanel = { showManagePanel = !showManagePanel },
                        showManagePanel = showManagePanel,
                        forcedSegment = BookSegment.WEEK,
                        bookOnlyMode = false,
                        onShowInspiration = { showInspiration = true },
                    )
                }
            }
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
            .padding(top = 4.dp),
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
                LibraryBookCoverCard(
                    book = book,
                    selected = index == 0,
                    onClick = { onOpenBook(index) },
                )
            }
        }
    }
}

@Composable
private fun LibraryBookCoverCard(
    book: TopicBook,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val coverShape = RoundedCornerShape(24.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(148.dp)
            .shadow(12.dp, coverShape, clip = false)
            .clip(coverShape)
            .background(
                Brush.linearGradient(
                    listOf(
                        book.color.copy(alpha = 0.94f),
                        book.color.copy(alpha = 0.74f),
                        Color.White.copy(alpha = 0.28f),
                    ),
                    start = Offset.Zero,
                    end = Offset(920f, 620f),
                ),
            )
            .border(1.dp, Color(0x33FFFFFF), coverShape)
            .clickable(onClick = onClick),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .width(38.dp)
                .fillMaxHeight()
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0x502F261D), Color(0x22FFFFFF), Color.Transparent),
                    ),
                ),
        )
        Text(
            "GOALDAY",
            color = Color(0xCCFFFFFF),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .graphicsLayer { rotationZ = -90f }
                .padding(bottom = 2.dp),
        )
        repeat(4) { layer ->
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(top = (16 + layer).dp, bottom = (14 + layer).dp, end = (3 + layer).dp)
                    .width((4 + layer).dp)
                    .fillMaxHeight()
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0x33FFFFFF), Color(0xCCFFF9F0)),
                        ),
                    ),
            )
        }
        Column(
            modifier = Modifier
                .padding(start = 56.dp, top = 22.dp, end = 28.dp, bottom = 18.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(book.title, style = MaterialTheme.typography.titleLarge, color = Color(0xFF2F261D), fontWeight = FontWeight.SemiBold)
                Text(book.subtitle, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF4B3D31))
            }
            Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("${book.pages.size} ${BookStrings.pageUnit}", style = MaterialTheme.typography.labelLarge, color = Color(0xFF5D4B3D))
                Text(
                    if (selected) "默认手账" else BookStrings.openBook,
                    color = Color(0xFF5D4B3D),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier
                        .clip(RoundedCornerShape(99.dp))
                        .background(Color(0x26FFFFFF))
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                )
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
    onToggleManagePanel: () -> Unit,
    showManagePanel: Boolean,
    forcedSegment: BookSegment?,
    bookOnlyMode: Boolean = false,
    onShowInspiration: () -> Unit,
) {
    val handbookMode = bookOnlyMode || forcedSegment == BookSegment.DIARY
    var segment by remember(book.id) { mutableStateOf(resolveSegment(currentPage)) }
    forcedSegment?.let { desired ->
        if (segment != desired) {
            segment = desired
            val firstIndex = book.pages.indexOfFirst { page -> matchesSegment(page, desired) }
            if (firstIndex >= 0 && uiState.selectedPageIndex != firstIndex) {
                viewModel.setPage(firstIndex)
            }
        }
    }
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
        GoaldayTopBar(
            leftTitle = if (handbookMode) "手账本" else "14周",
            rightPrimaryText = if (handbookMode) "离线手账" else "完成",
            onRightPrimaryClick = {
                if (!handbookMode) onBackToLibrary()
            },
            rightSecondary = {
                if (!handbookMode && forcedSegment != BookSegment.DIARY) {
                    Text("＋灵感", color = Color(0xFF7A736A), style = MaterialTheme.typography.labelSmall, modifier = Modifier.clickable(onClick = onShowInspiration))
                }
                if (!handbookMode && book.id.startsWith("custom_")) {
                    Text("⚙管理", color = Color(0xFF7A736A), style = MaterialTheme.typography.labelSmall, modifier = Modifier.clickable(onClick = onToggleManagePanel))
                }
            },
        )
        if (!handbookMode) {
            GoaldaySegmentBar(
                items = BookSegment.entries.map { it.label },
                selectedIndex = BookSegment.entries.indexOf(segment).coerceAtLeast(0),
                onSelect = { idx -> switchSegment(BookSegment.entries[idx]) },
            )
        }
        if (showManagePanel && book.id.startsWith("custom_")) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
            ) {
                ActionChip(label = BookStrings.editBook, color = Color(0xFF8F684F), onClick = onShowEditBook)
                ActionChip(label = BookStrings.addPage, color = Color(0xFF8F684F), onClick = onShowAddPage)
                ActionChip(label = BookStrings.renamePage, color = Color(0xFF8F684F), onClick = onShowRenamePage)
                ActionChip(label = BookStrings.moveLeft, color = Color(0xFF8F684F), onClick = viewModel::moveCurrentPageLeft)
                ActionChip(label = BookStrings.moveRight, color = Color(0xFF8F684F), onClick = viewModel::moveCurrentPageRight)
                ActionChip(label = BookStrings.deletePage, color = Color(0xFF9C5A52), onClick = viewModel::deleteCurrentPage)
                ActionChip(label = BookStrings.deleteBook, color = Color(0xFF9C5A52), onClick = viewModel::removeCurrentCustomBook)
            }
        }
        if (forcedSegment == null && !handbookMode) {
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
        } else {
            Spacer(Modifier.height(8.dp))
        }
        if (handbookMode) {
            Text(
                text = monthLabelForPage(currentPage.title, fallback = book.title),
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF1F1D1A),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(6.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
            ) {
                book.pages.forEachIndexed { index, item ->
                    val selected = index == uiState.selectedPageIndex
                    Text(
                        text = monthLabelForPage(item.title, fallback = item.title),
                        color = if (selected) Color.White else Color(0xFF6E6258),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        modifier = Modifier
                            .clip(RoundedCornerShape(99.dp))
                            .background(if (selected) Color(0xFFE88FAE) else Color(0x20FFFFFF))
                            .border(0.6.dp, if (selected) Color(0xFFFFF5F8) else Color(0x20A68B71), RoundedCornerShape(99.dp))
                            .clickable { viewModel.setPage(index) }
                            .padding(horizontal = 11.dp, vertical = 5.dp),
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
        }
        if (handbookMode) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFFF3E6D8),
                                Color(0xFFEBD9C6),
                                Color(0xFFE0C8B0),
                            ),
                        ),
                    )
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .fillMaxHeight(0.84f),
                ) {
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
                        onAddHandbookPoolItem = viewModel::addHandbookPoolItem,
                        onRemoveHandbookPoolItem = viewModel::removeHandbookPoolItem,
                        onAddScheduleFromHandbook = viewModel::addScheduleFromHandbook,
                        onWeeklyThemeChange = viewModel::updateWeeklyTheme,
                        onMoveItemToToday = viewModel::moveItemToToday,
                        onMoveItemToCompleted = viewModel::moveItemToCompleted,
                        onRestoreItemFromToday = viewModel::restoreItemFromToday,
                        onRestoreItemFromCompleted = viewModel::restoreItemFromCompleted,
                        onUpdateScheduleTitle = viewModel::updateScheduleTitleFromHandbook,
                        onMoveScheduleDay = viewModel::moveScheduleDayFromHandbook,
                        onToggleScheduleCompleted = viewModel::toggleScheduleCompletedFromHandbook,
                        shellStyle = ShellStyle.BOOK,
                        handbookMode = true,
                        onFlipNext = { if (uiState.selectedPageIndex < book.pages.lastIndex) viewModel.setPage(uiState.selectedPageIndex + 1) },
                        onFlipPrevious = { if (uiState.selectedPageIndex > 0) viewModel.setPage(uiState.selectedPageIndex - 1) },
                    )
                }
            }
        } else {
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
                onAddHandbookPoolItem = viewModel::addHandbookPoolItem,
                onRemoveHandbookPoolItem = viewModel::removeHandbookPoolItem,
                onAddScheduleFromHandbook = viewModel::addScheduleFromHandbook,
                onWeeklyThemeChange = viewModel::updateWeeklyTheme,
                onMoveItemToToday = viewModel::moveItemToToday,
                onMoveItemToCompleted = viewModel::moveItemToCompleted,
                onRestoreItemFromToday = viewModel::restoreItemFromToday,
                onRestoreItemFromCompleted = viewModel::restoreItemFromCompleted,
                onUpdateScheduleTitle = viewModel::updateScheduleTitleFromHandbook,
                onMoveScheduleDay = viewModel::moveScheduleDayFromHandbook,
                onToggleScheduleCompleted = viewModel::toggleScheduleCompletedFromHandbook,
                shellStyle = if (forcedSegment == BookSegment.DIARY || currentPage is DiaryPage) ShellStyle.BOOK else ShellStyle.LIGHT,
                handbookMode = handbookMode,
                onFlipNext = { if (uiState.selectedPageIndex < book.pages.lastIndex) viewModel.setPage(uiState.selectedPageIndex + 1) },
                onFlipPrevious = { if (uiState.selectedPageIndex > 0) viewModel.setPage(uiState.selectedPageIndex - 1) },
            )
        }
    }
}

private fun monthLabelForPage(title: String, fallback: String): String {
    val regex = Regex("(\\d{1,2}月)")
    return regex.find(title)?.value ?: fallback
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
    onApply: (List<String>, Boolean, Boolean) -> Unit,
    onSaveAsBook: (InspirationTemplate, List<String>) -> Unit,
) {
    val selected = templates[selectedIndex.coerceIn(0, templates.lastIndex)]
    var checkedStates by remember(selected.title) { mutableStateOf(List(selected.items.size) { true }) }
    var editableItems by remember(selected.title) { mutableStateOf(selected.items) }
    var pushToToday by remember { mutableStateOf(true) }
    var clearSourceAfterApply by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 4.dp),
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
                            onApply(picked, pushToToday, clearSourceAfterApply)
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
                    Box(
                        modifier = Modifier
                            .width(190.dp)
                            .height(108.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.verticalGradient(
                                    when (index % 4) {
                                        0 -> listOf(Color(0xFF7E8E55), Color(0xFF4D6032))
                                        1 -> listOf(Color(0xFF86674B), Color(0xFF5B3F2D))
                                        2 -> listOf(Color(0xFF6F5A7D), Color(0xFF4E3F5A))
                                        else -> listOf(Color(0xFF5E6F8A), Color(0xFF3C4F69))
                                    },
                                ),
                            )
                            .border(
                                width = if (index == selectedIndex) 2.dp else 1.dp,
                                color = if (index == selectedIndex) Color.White else Color.White.copy(alpha = 0.35f),
                                shape = RoundedCornerShape(14.dp),
                            )
                            .clickable { onSelect(index) }
                            .padding(12.dp),
                    ) {
                        Column(
                            modifier = Modifier.align(Alignment.BottomStart),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(item.title, color = Color.White, style = MaterialTheme.typography.titleSmall)
                            Text(item.subtitle, color = Color.White.copy(alpha = 0.90f), style = MaterialTheme.typography.labelSmall)
                        }
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(126.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(selected.color.copy(alpha = 0.94f), selected.color.copy(alpha = 0.70f), Color.White.copy(alpha = 0.22f)),
                                start = Offset.Zero,
                                end = Offset(800f, 520f),
                            ),
                        )
                        .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(20.dp))
                        .padding(16.dp),
                ) {
                    Column(
                        modifier = Modifier.align(Alignment.BottomStart),
                        verticalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        Text(selected.title, style = MaterialTheme.typography.titleLarge, color = Color(0xFF2F261D), fontWeight = FontWeight.SemiBold)
                        Text(selected.subtitle, style = MaterialTheme.typography.bodySmall, color = Color(0xFF4B3D31))
                        Text(if (selected.linkToSchedule) "可导入任务池 · 可保存成手账本" else "适合复盘记录 · 可保存成手账本", style = MaterialTheme.typography.labelSmall, color = Color(0xFF5D4B3D))
                    }
                }
                Text("目标详情", style = MaterialTheme.typography.titleMedium, color = Color(0xFF2F261D), fontWeight = FontWeight.SemiBold)
                editableItems.chunked(2).forEachIndexed { rowIndex, rowItems ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        rowItems.forEachIndexed { columnIndex, item ->
                            val index = rowIndex * 2 + columnIndex
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (checkedStates.getOrNull(index) == true) Color(0x0FE88FAE) else Color(0x08A68B71))
                                    .border(0.6.dp, if (checkedStates.getOrNull(index) == true) Color(0x35E88FAE) else Color(0x12000000), RoundedCornerShape(12.dp))
                                    .padding(8.dp),
                                verticalArrangement = Arrangement.spacedBy(5.dp),
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
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
                                    Text("目标 ${index + 1}", color = Color(0xFF7A7065), style = MaterialTheme.typography.labelSmall)
                                }
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
                        if (rowItems.size == 1) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
                if (editableItems.size < 18) {
                    Text(
                        "＋ 添加目标",
                        color = Color(0xFFE88FAE),
                        modifier = Modifier
                            .clickable {
                                editableItems = editableItems + "新目标"
                                checkedStates = checkedStates + true
                            }
                            .padding(vertical = 4.dp),
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    ActionChip(
                        label = if (pushToToday) "导入任务池:开" else "导入任务池:关",
                        color = Color(0xFF8F684F),
                        onClick = { pushToToday = !pushToToday },
                    )
                    ActionChip(
                        label = if (clearSourceAfterApply) "应用后移出来源:开" else "应用后移出来源:关",
                        color = Color(0xFF8F684F),
                        onClick = { clearSourceAfterApply = !clearSourceAfterApply },
                    )
                }
                val picked = editableItems.filterIndexed { index, value ->
                    checkedStates.getOrNull(index) == true && value.isNotBlank()
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { onApply(picked, pushToToday, clearSourceAfterApply) },
                        modifier = Modifier.weight(1f),
                    ) { Text("应用到当前页") }
                    Button(
                        onClick = { onSaveAsBook(selected, picked) },
                        modifier = Modifier.weight(1f),
                    ) { Text("保存成手账本") }
                }
                Button(
                    onClick = { onApply(picked, true, false) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("导入任务池") }
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
