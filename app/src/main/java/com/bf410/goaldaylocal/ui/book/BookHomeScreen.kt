package com.bf410.goaldaylocal.ui.book

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bf410.goaldaylocal.data.BookPage
import com.bf410.goaldaylocal.data.DiaryPage
import com.bf410.goaldaylocal.data.PlanPage
import com.bf410.goaldaylocal.data.ScheduleEntry
import com.bf410.goaldaylocal.data.SchedulePage
import com.bf410.goaldaylocal.data.TargetItemMeta
import com.bf410.goaldaylocal.data.TargetPage
import com.bf410.goaldaylocal.data.TopicBook
import com.bf410.goaldaylocal.ui.replica.GoaldayDesign
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

private enum class HandbookSection(val label: String) {
    OVERVIEW("总览"),
    SCHEDULE("日程"),
    DIARY("日记"),
    TARGET("目标"),
}

private data class PageDialogPreset(
    val type: String = "schedule",
    val title: String = "",
)

enum class BookEntryMode {
    PLANNER,
    INSPIRATION,
    HANDBOOK,
    DIARY,
}

private fun entryLandingPageIndex(book: TopicBook, entryMode: BookEntryMode): Int =
    when (entryMode) {
        BookEntryMode.HANDBOOK -> book.pages.indexOfFirst { it is SchedulePage || it is PlanPage }
        BookEntryMode.DIARY -> book.pages.indexOfFirst { it is DiaryPage }
        else -> -1
    }.takeIf { it >= 0 } ?: 0

private fun entryLandingBookIndex(books: List<TopicBook>, entryMode: BookEntryMode): Int =
    when (entryMode) {
        BookEntryMode.HANDBOOK -> books.indexOfFirst { book -> book.pages.any { it is SchedulePage || it is PlanPage } }
        BookEntryMode.DIARY -> books.indexOfFirst { book -> book.pages.any { it is DiaryPage } }
        else -> -1
    }.takeIf { it >= 0 } ?: 0

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
    var pageDialogPreset by remember { mutableStateOf(PageDialogPreset()) }
    var showInspiration by remember(entryMode) { mutableStateOf(entryMode == BookEntryMode.INSPIRATION) }
    var selectedTemplateIndex by remember { mutableStateOf(0) }
    var consumedEntryLandingKey by remember { mutableStateOf<String?>(null) }

    val hasBooks = uiState.books.isNotEmpty()
    val safeBookIndex = uiState.selectedBookIndex.coerceIn(0, (uiState.books.lastIndex).coerceAtLeast(0))
    val landingBookIndex = remember(uiState.books, entryMode) { entryLandingBookIndex(uiState.books, entryMode) }
    if (!hasBooks) {
        BookUnavailableState(
            title = "还没有可用手账",
            body = "本地样例和自建手账都为空，先新建一本手账后再进入日程或日记。",
            action = "新建手账",
            onAction = { showCreateDialog = true },
        )
        if (showCreateDialog) {
            CreateBookDialog(
                onDismiss = { showCreateDialog = false },
                onConfirm = { title, subtitle, color ->
                    viewModel.createCustomBook(title, subtitle, color)
                    showCreateDialog = false
                },
            )
        }
        return
    }
    LaunchedEffect(entryMode) {
        if (entryMode != BookEntryMode.HANDBOOK && entryMode != BookEntryMode.DIARY) {
            consumedEntryLandingKey = null
        }
    }
    LaunchedEffect(entryMode, hasBooks, landingBookIndex, uiState.selectedBookIndex) {
        if ((entryMode == BookEntryMode.HANDBOOK || entryMode == BookEntryMode.DIARY) && hasBooks && uiState.selectedBookIndex != landingBookIndex) {
            viewModel.selectBook(landingBookIndex)
        }
    }
    if ((entryMode == BookEntryMode.HANDBOOK || entryMode == BookEntryMode.DIARY) && hasBooks && uiState.selectedBookIndex != landingBookIndex) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "正在打开本地手账...",
                color = GoaldayDesign.InkSecondary,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        return
    }

    when (entryMode) {
        BookEntryMode.INSPIRATION -> {
            InspirationCenterView(
                templates = InspirationTemplates.all,
                selectedIndex = selectedTemplateIndex,
                onSelect = { selectedTemplateIndex = it },
                onBack = { viewModel.openLibrary() },
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
            val book = uiState.books[safeBookIndex]
            if (book.pages.isEmpty()) {
                BookUnavailableState("这本手账没有页面", "请先新建一个日程页、计划页或日记页。", "添加页面") {
                    pageDialogPreset = PageDialogPreset(type = "schedule", title = "日程页")
                    showPageDialog = true
                }
                return
            }
            LaunchedEffect(entryMode, book.id) {
                val landingIndex = entryLandingPageIndex(book, entryMode)
                val landingKey = "${entryMode.name}:${book.id}"
                if (consumedEntryLandingKey != landingKey && uiState.selectedPageIndex != landingIndex) {
                    consumedEntryLandingKey = landingKey
                    viewModel.setPage(landingIndex)
                } else {
                    consumedEntryLandingKey = landingKey
                }
            }
            val clampedPageIndex = uiState.selectedPageIndex.coerceIn(0, book.pages.lastIndex)
            val currentPage = book.pages[clampedPageIndex]
            val previousPage = book.pages.getOrNull(clampedPageIndex - 1)
            val nextPage = book.pages.getOrNull(clampedPageIndex + 1)
            GoaldayHandbookScreen(
                viewModel = viewModel,
                book = book,
                currentPage = currentPage,
                previousPage = previousPage,
                nextPage = nextPage,
                uiState = uiState,
            )
        }

        BookEntryMode.DIARY -> {
            if (!hasBooks) return
            val book = uiState.books[safeBookIndex]
            if (book.pages.none { it is DiaryPage }) {
                BookUnavailableState("这本手账没有日记页", "日记入口需要至少一个日记页，添加后会自动保存到本机。", "添加日记页") {
                    pageDialogPreset = PageDialogPreset(type = "diary", title = "日记页")
                    showPageDialog = true
                }
                return
            }
            LaunchedEffect(entryMode, book.id) {
                val landingIndex = entryLandingPageIndex(book, entryMode)
                val landingKey = "${entryMode.name}:${book.id}"
                if (consumedEntryLandingKey != landingKey && uiState.selectedPageIndex != landingIndex) {
                    consumedEntryLandingKey = landingKey
                    viewModel.setPage(landingIndex)
                } else {
                    consumedEntryLandingKey = landingKey
                }
            }
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
                onShowAddPage = {
                    pageDialogPreset = PageDialogPreset(type = "diary", title = "日记页")
                    showPageDialog = true
                },
                onShowRenamePage = { showRenamePageDialog = true },
                onShowEditBook = { showEditBookDialog = true },
                onToggleManagePanel = { },
                showManagePanel = false,
                forcedSegment = BookSegment.DIARY,
                bookOnlyMode = false,
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
                if (book.pages.isEmpty()) {
                    BookUnavailableState("这本手账没有页面", "先添加一页，日程、清单和日记功能才有地方保存。", "添加页面") {
                        pageDialogPreset = PageDialogPreset(type = "schedule", title = "日程页")
                        showPageDialog = true
                    }
                    return
                }
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
                        onShowAddPage = {
                            pageDialogPreset = PageDialogPreset(type = "schedule", title = "日程页")
                            showPageDialog = true
                        },
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
            preset = pageDialogPreset,
            onDismiss = { showPageDialog = false },
            onConfirm = { type, title ->
                viewModel.addPage(type, title)
                showPageDialog = false
            },
        )
    }

    if (showRenamePageDialog) {
        val book = uiState.books.getOrNull(safeBookIndex)
        val page = book?.pages?.getOrNull(uiState.selectedPageIndex.coerceIn(0, (book.pages.lastIndex).coerceAtLeast(0)))
        if (page == null) {
            LaunchedEffect(showRenamePageDialog) {
                showRenamePageDialog = false
            }
        } else {
            RenamePageDialog(
                currentTitle = page.title,
                onDismiss = { showRenamePageDialog = false },
                onConfirm = { title ->
                    viewModel.renameCurrentPage(title)
                    showRenamePageDialog = false
                },
            )
        }
    }

    if (showEditBookDialog) {
        val book = uiState.books.getOrNull(safeBookIndex)
        if (book == null) {
            LaunchedEffect(showEditBookDialog) {
                showEditBookDialog = false
            }
        } else {
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
}

@Composable
private fun BookUnavailableState(
    title: String,
    body: String,
    action: String,
    onAction: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFFFFCF7),
                        Color(0xFFF8EFE5),
                        Color(0xFFF0DDCA),
                    ),
                ),
            )
            .padding(22.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xEFFFFDF8))
                .border(0.8.dp, Color(0x24A88966), RoundedCornerShape(24.dp))
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                title,
                color = Color(0xFF2F261D),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
            Text(
                body,
                color = GoaldayDesign.InkMuted,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
            )
            Text(
                action,
                color = Color.White,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clip(RoundedCornerShape(99.dp))
                    .background(GoaldayDesign.PrimaryAction)
                    .clickable(onClick = onAction)
                    .padding(horizontal = 15.dp, vertical = 9.dp),
            )
        }
    }
}

@Composable
private fun LibraryView(
    books: List<TopicBook>,
    customBookCount: Int,
    onOpenBook: (Int) -> Unit,
    onCreateBook: () -> Unit,
) {
    val primaryBook = books.firstOrNull()
    val shelfBooks = books.drop(1)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFF8EFE5),
                        Color(0xFFF6E8DA),
                        Color(0xFFF1DDCC),
                    ),
                ),
            )
            .padding(top = 12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = BookStrings.appTitle,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2A211A),
                )
                Text(
                    text = BookStrings.librarySummary.format(books.size, customBookCount),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF7A6657),
                )
            }
            Text(
                text = BookStrings.createBook,
                color = Color.White,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clip(RoundedCornerShape(99.dp))
                    .background(Color(0xFF2F2923))
                    .clickable(onClick = onCreateBook)
                    .padding(horizontal = 13.dp, vertical = 8.dp),
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = BookStrings.librarySubtitle,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF6F675D),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0x66FFFDF8))
                .padding(horizontal = 12.dp, vertical = 9.dp),
        )
        Spacer(Modifier.height(14.dp))
        Column(
            verticalArrangement = Arrangement.spacedBy(18.dp),
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            primaryBook?.let { book ->
                FeaturedHandbookCover(
                    book = book,
                    onClick = { onOpenBook(0) },
                )
            }
            BookShelfRow(
                books = shelfBooks,
                indexOffset = 1,
                onOpenBook = onOpenBook,
            )
            AddBookShelfCard(onCreateBook = onCreateBook)
        }
    }
}

@Composable
private fun FeaturedHandbookCover(
    book: TopicBook,
    onClick: () -> Unit,
) {
    val coverShape = RoundedCornerShape(30.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(230.dp)
            .shadow(24.dp, coverShape, clip = false)
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
                .matchParentSize()
                .background(
                    Brush.radialGradient(
                        listOf(Color(0x55FFFFFF), Color.Transparent),
                        center = Offset(700f, 80f),
                        radius = 620f,
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .width(48.dp)
                .fillMaxHeight()
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0x662F261D), Color(0x22FFFFFF), Color.Transparent),
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
                    .padding(bottom = 7.dp),
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
                .padding(start = 70.dp, top = 26.dp, end = 28.dp, bottom = 22.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("LOCAL HANDBOOK", style = MaterialTheme.typography.labelSmall, color = Color(0xAA2F261D), fontWeight = FontWeight.SemiBold)
                Text(book.title, style = MaterialTheme.typography.headlineSmall, color = Color(0xFF2F261D), fontWeight = FontWeight.SemiBold)
                Text(book.subtitle, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF4B3D31))
            }
            Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("${book.pages.size} ${BookStrings.pageUnit}", style = MaterialTheme.typography.labelLarge, color = Color(0xFF5D4B3D))
                Text(
                    "打开这本",
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(99.dp))
                        .background(Color(0xFF2F2923))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                )
            }
        }
    }
}

@Composable
private fun BookShelfRow(
    books: List<TopicBook>,
    indexOffset: Int,
    onOpenBook: (Int) -> Unit,
) {
    if (books.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("书架", color = Color(0xFF3A2D24), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        books.chunked(3).forEachIndexed { rowIndex, row ->
            Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(9.dp),
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(154.dp),
                ) {
                    row.forEachIndexed { columnIndex, book ->
                        ShelfBookCover(
                            book = book,
                            modifier = Modifier.weight(1f),
                            onClick = { onOpenBook(indexOffset + rowIndex * 3 + columnIndex) },
                        )
                    }
                    repeat(3 - row.size) {
                        Spacer(Modifier.weight(1f))
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .clip(RoundedCornerShape(99.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFFD3A681), Color(0xFF8F6042)),
                            ),
                        ),
                )
            }
        }
    }
}

@Composable
private fun ShelfBookCover(
    book: TopicBook,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(16.dp, 16.dp, 9.dp, 9.dp)
    Box(
        modifier = modifier
            .fillMaxHeight()
            .shadow(8.dp, shape, clip = false)
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(book.color.copy(alpha = 0.96f), book.color.copy(alpha = 0.76f), Color(0xFFFFFAF2)),
                    start = Offset.Zero,
                    end = Offset(280f, 520f),
                ),
            )
            .border(0.8.dp, Color(0x38FFFFFF), shape)
            .clickable(onClick = onClick)
            .padding(10.dp),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .width(16.dp)
                .fillMaxHeight()
                .background(Color(0x332F261D), RoundedCornerShape(99.dp)),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 20.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(book.title, color = Color(0xFF2F261D), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 3)
            Text("${book.pages.size}页", color = Color(0xFF5D4B3D), style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun AddBookShelfCard(onCreateBook: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0x7AFFFDF8))
            .border(1.dp, Color(0x30A8795E), RoundedCornerShape(18.dp))
            .clickable(onClick = onCreateBook)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text("+ 新建一本手账", color = Color(0xFF6F4D3A), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun GoaldayHandbookScreen(
    viewModel: BookViewModel,
    book: TopicBook,
    currentPage: BookPage,
    previousPage: BookPage?,
    nextPage: BookPage?,
    uiState: BookUiState,
) {
    var section by remember(book.id) { mutableStateOf(resolveHandbookSection(currentPage)) }
    var openedTargetDetail by remember(book.id, currentPage.title) { mutableStateOf<String?>(null) }
    val sectionPageEntries = remember(book.pages, section) {
        book.pages
            .mapIndexed { index, page -> IndexedValue(index, page) }
            .filter { (_, page) -> matchesHandbookSection(page, section) }
            .ifEmpty { book.pages.mapIndexed { index, page -> IndexedValue(index, page) } }
    }
    val sectionPages = sectionPageEntries.map { it.value }
    val visiblePageIndex = uiState.selectedPageIndex.coerceIn(0, book.pages.lastIndex)
    val selectedSectionIndex = sectionPageEntries.indexOfFirst { it.index == visiblePageIndex }.coerceAtLeast(0)
    LaunchedEffect(currentPage.title) {
        if (section != HandbookSection.OVERVIEW && !matchesHandbookSection(currentPage, section)) {
            section = resolveHandbookSection(currentPage)
        }
    }

    fun goToPage(index: Int) {
        val page = book.pages.getOrNull(index) ?: return
        section = resolveHandbookSection(page)
        viewModel.setPage(index)
    }

    fun openSection(next: HandbookSection) {
        section = next
        val firstIndex = book.pages.indexOfFirst { page -> matchesHandbookSection(page, next) }
        if (firstIndex >= 0 && firstIndex != uiState.selectedPageIndex) {
            viewModel.setPage(firstIndex)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFFFFBF6),
                        Color(0xFFF5E4D2),
                        Color(0xFFE4C7AE),
                    ),
                ),
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 8.dp),
        ) {
            HandbookTopChrome(
                book = book,
                currentPage = currentPage,
                section = section,
                visiblePageIndex = visiblePageIndex,
                pageCount = book.pages.size,
                onOpenSection = ::openSection,
            )
            Spacer(Modifier.height(6.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
            ) {
                sectionPageEntries.forEach { (realIndex, page) ->
                    val selected = realIndex == visiblePageIndex
                    Text(
                        text = monthLabelForPage(page.title, fallback = page.title),
                        color = if (selected) Color(0xFF2F261D) else Color(0xFF8A7C70),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier
                            .clip(RoundedCornerShape(99.dp))
                            .background(if (selected) Color(0x66FFFFFF) else Color(0x24FFFFFF))
                            .border(0.5.dp, if (selected) Color(0x55B88A58) else Color.Transparent, RoundedCornerShape(99.dp))
                            .clickable {
                                goToPage(realIndex)
                            }
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .padding(start = 18.dp, top = 20.dp, end = 4.dp, bottom = 12.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(Color(0x55A87658)),
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .padding(start = 12.dp, top = 12.dp, end = 10.dp, bottom = 7.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(Color(0x99F3D7BC)),
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .padding(start = 4.dp, top = 4.dp, end = 16.dp, bottom = 2.dp)
                        .shadow(16.dp, RoundedCornerShape(30.dp))
                        .clip(RoundedCornerShape(30.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0xFF6C4A39),
                                    Color(0xFFE2B997),
                                    Color(0xFFFFFBF4),
                                    Color(0xFFFFF8EF),
                                    Color(0xFFE0B895),
                                ),
                            ),
                        ),
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 5.dp, top = 28.dp, bottom = 30.dp)
                        .width(23.dp)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(99.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF503529), Color(0xFFB47C62), Color(0xFF5A3B2E)),
                            ),
                        )
                        .border(0.7.dp, Color(0x55F5D7BE), RoundedCornerShape(99.dp)),
                )
                HandbookPhysicalBookDetails(
                    pageProgress = ((visiblePageIndex + 1).toFloat() / book.pages.size.coerceAtLeast(1)).coerceIn(0.08f, 1f),
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 24.dp, top = 10.dp, end = 18.dp, bottom = 12.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color(0xF8FFFDF8))
                        .border(0.8.dp, Color(0x24A88966), RoundedCornerShape(22.dp))
                        .padding(6.dp),
                ) {
                    HandbookRouteSurface(
                        route = section,
                        viewModel = viewModel,
                        book = book,
                        currentPage = currentPage,
                        previousPage = previousPage,
                        nextPage = nextPage,
                        uiState = uiState,
                        sectionPages = sectionPages,
                        selectedSectionIndex = selectedSectionIndex,
                        onOpenTargetDetail = { openedTargetDetail = it },
                        onOpenSection = ::openSection,
                        onOpenPage = ::goToPage,
                    )
                }
            }
            Spacer(Modifier.height(7.dp))
            HandbookPageControlDock(
                section = section,
                pageIndex = visiblePageIndex,
                pageCount = book.pages.size,
                currentPage = currentPage,
                previousPage = previousPage,
                nextPage = nextPage,
                sectionPages = sectionPages,
                selectedSectionIndex = selectedSectionIndex,
                onPrevious = {
                    val previousIndex = sectionPages.getOrNull(selectedSectionIndex - 1)
                        ?.let { page -> book.pages.indexOf(page) }
                        ?: (visiblePageIndex - 1)
                    if (previousIndex in book.pages.indices) goToPage(previousIndex)
                },
                onNext = {
                    val nextIndex = sectionPages.getOrNull(selectedSectionIndex + 1)
                        ?.let { page -> book.pages.indexOf(page) }
                        ?: (visiblePageIndex + 1)
                    if (nextIndex in book.pages.indices) goToPage(nextIndex)
                },
            )
        }
        val targetPage = currentPage as? TargetPage
        val targetItems = targetPage?.let { (it.items + uiState.customPageItems).distinct() }.orEmpty()
        val targetItem = openedTargetDetail?.takeIf { it in targetItems }
        if (targetPage != null && targetItem != null) {
            TargetDetailRouteOverlay(
                pageTitle = targetPage.title,
                item = targetItem,
                itemIndex = targetItems.indexOf(targetItem).coerceAtLeast(0),
                itemCount = targetItems.size,
                checked = viewModel.isChecked(targetPage.title, targetItem),
                meta = uiState.targetItemMeta[targetItem] ?: TargetItemMeta(),
                scheduledEntries = uiState.schedulePreviewEntries
                    .filter { it.title == targetItem }
                    .sortedWith(compareBy<ScheduleEntry>({ it.month }, { it.day }, { it.timeText })),
                onClose = { openedTargetDetail = null },
                onToggleChecked = { viewModel.toggleChecked(targetPage.title, targetItem) },
                onUpdateNote = { viewModel.updateTargetItemNote(targetItem, it) },
                onUpdateDeadline = { viewModel.updateTargetItemDeadline(targetItem, it) },
                onAddToSchedule = { day -> viewModel.addItemToSchedule(targetItem, day) },
                onSaveAsOwnTarget = { viewModel.addCustomPageItem(targetItem) },
                onAddToDiary = { done -> viewModel.addTargetDetailToDiary(targetItem, completed = done) },
            )
        }
    }
}

@Composable
private fun HandbookTopChrome(
    book: TopicBook,
    currentPage: BookPage,
    section: HandbookSection,
    visiblePageIndex: Int,
    pageCount: Int,
    onOpenSection: (HandbookSection) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xCAFFFDF8))
            .border(0.7.dp, Color(0x24A88966), RoundedCornerShape(18.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(1.dp), modifier = Modifier.weight(1f)) {
                Text("Goalday 手账", color = Color(0xFF2F261D), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 1)
                Text(monthLabelForPage(currentPage.title, fallback = book.title), color = Color(0xFF7A7065), style = MaterialTheme.typography.labelSmall, maxLines = 1)
            }
            Text(
                "${visiblePageIndex + 1}/$pageCount",
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clip(RoundedCornerShape(99.dp))
                    .background(routeColor(section))
                    .padding(horizontal = 9.dp, vertical = 4.dp),
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
        ) {
            HandbookSection.entries.forEach { item ->
                val selected = item == section
                Text(
                    item.label,
                    color = if (selected) Color.White else routeColor(item),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(99.dp))
                        .background(if (selected) routeColor(item) else routeColor(item).copy(alpha = 0.12f))
                        .border(0.6.dp, routeColor(item).copy(alpha = 0.20f), RoundedCornerShape(99.dp))
                        .clickable { onOpenSection(item) }
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun HandbookPageControlDock(
    section: HandbookSection,
    pageIndex: Int,
    pageCount: Int,
    currentPage: BookPage,
    previousPage: BookPage?,
    nextPage: BookPage?,
    sectionPages: List<BookPage>,
    selectedSectionIndex: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    val canPrevious = selectedSectionIndex > 0 || previousPage != null
    val canNext = selectedSectionIndex < sectionPages.lastIndex || nextPage != null
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xDFFFFCF7))
            .border(0.7.dp, Color(0x24A88966), RoundedCornerShape(18.dp))
            .padding(horizontal = 9.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HandbookDockButton(
                label = "上一页",
                enabled = canPrevious,
                color = Color(0xFF8F684F),
                modifier = Modifier.weight(0.9f),
                onClick = onPrevious,
            )
            Column(
                modifier = Modifier.weight(1.5f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    monthLabelForPage(currentPage.title, fallback = currentPage.title),
                    color = Color(0xFF2F261D),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                )
                Text(
                    "${pageIndex + 1}/$pageCount · ${section.label}",
                    color = GoaldayDesign.InkMuted,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                )
            }
            HandbookDockButton(
                label = "下一页",
                enabled = canNext,
                color = routeColor(section),
                modifier = Modifier.weight(0.9f),
                onClick = onNext,
            )
        }
    }
}

@Composable
private fun HandbookDockButton(
    label: String,
    enabled: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Text(
        label,
        color = if (enabled) Color.White else GoaldayDesign.InkMuted,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        maxLines = 1,
        textAlign = TextAlign.Center,
        modifier = modifier
            .clip(RoundedCornerShape(99.dp))
            .background(if (enabled) color else Color(0x1AA88966))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 9.dp, vertical = 5.dp),
    )
}

@Composable
private fun BoxScope.HandbookPhysicalBookDetails(pageProgress: Float) {
    repeat(9) { index ->
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(top = 28.dp, end = (18 + index * 2).dp, bottom = 30.dp)
                .width(0.7.dp)
                .fillMaxHeight(0.82f)
                .background(Color(0x22A98566).copy(alpha = 0.18f + index * 0.018f)),
        )
    }
    repeat(7) { index ->
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 44.dp, end = 38.dp, bottom = (8 + index * 2).dp)
                .fillMaxWidth(0.86f)
                .height(0.8.dp)
                .background(Color(0x32A98566).copy(alpha = 0.15f + index * 0.02f)),
        )
    }
    Box(
        modifier = Modifier
            .align(Alignment.CenterStart)
            .padding(start = 13.dp, top = 30.dp, bottom = 34.dp)
            .width(1.2.dp)
            .fillMaxHeight(0.78f)
            .background(
                Brush.verticalGradient(
                    listOf(Color.Transparent, Color(0x66FFF4E6), Color.Transparent),
                ),
            ),
    )
    Column(
        modifier = Modifier
            .align(Alignment.CenterStart)
            .padding(start = 3.dp, top = 42.dp, bottom = 42.dp)
            .width(24.dp)
            .fillMaxHeight(0.72f),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        repeat(5) { index ->
            Box(
                modifier = Modifier
                    .size(if (index == 2) 9.dp else 7.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .background(Color(0xFF4C3329).copy(alpha = 0.74f))
                    .border(0.8.dp, Color(0x88F6D8BE), RoundedCornerShape(99.dp)),
            )
        }
    }
    Box(
        modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(top = 17.dp, end = (28 + (pageProgress * 10).toInt()).dp)
            .width(84.dp)
            .height(2.dp)
            .clip(RoundedCornerShape(99.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(Color.Transparent, Color(0x66FFFDF8), Color.Transparent),
                ),
            ),
    )
    Box(
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(end = 38.dp, bottom = 18.dp)
            .width(130.dp)
            .height(5.dp)
            .clip(RoundedCornerShape(99.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(Color.Transparent, Color(0x33835A45), Color.Transparent),
                ),
            ),
    )
}

private data class HandbookRoutePayload(
    val viewModel: BookViewModel,
    val book: TopicBook,
    val currentPage: BookPage,
    val previousPage: BookPage?,
    val nextPage: BookPage?,
    val uiState: BookUiState,
    val sectionPages: List<BookPage>,
    val selectedSectionIndex: Int,
    val onOpenTargetDetail: (String) -> Unit,
)

@Composable
private fun HandbookRouteSurface(
    route: HandbookSection,
    viewModel: BookViewModel,
    book: TopicBook,
    currentPage: BookPage,
    previousPage: BookPage?,
    nextPage: BookPage?,
    uiState: BookUiState,
    sectionPages: List<BookPage>,
    selectedSectionIndex: Int,
    onOpenTargetDetail: (String) -> Unit,
    onOpenSection: (HandbookSection) -> Unit,
    onOpenPage: (Int) -> Unit,
) {
    val payload = HandbookRoutePayload(
        viewModel = viewModel,
        book = book,
        currentPage = currentPage,
        previousPage = previousPage,
        nextPage = nextPage,
        uiState = uiState,
        sectionPages = sectionPages,
        selectedSectionIndex = selectedSectionIndex,
        onOpenTargetDetail = onOpenTargetDetail,
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFFFFFEFC))
            .border(0.7.dp, Color(0x18A88966), RoundedCornerShape(18.dp))
            .padding(10.dp),
    ) {
        when (route) {
            HandbookSection.OVERVIEW -> HandbookOverviewRoute(payload, onOpenSection, onOpenPage)
            HandbookSection.SCHEDULE -> HandbookScheduleRoute(payload, onOpenPage)
            HandbookSection.DIARY -> HandbookDiaryRoute(payload, onOpenPage)
            HandbookSection.TARGET -> HandbookTargetRoute(payload, onOpenPage)
        }
    }
}

@Composable
private fun HandbookOpenSpreadSurface(
    route: HandbookSection,
    payload: HandbookRoutePayload,
    onOpenSection: (HandbookSection) -> Unit,
    onOpenPage: (Int) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        HandbookLeftIndexPage(
            route = route,
            payload = payload,
            onOpenSection = onOpenSection,
            onOpenPage = onOpenPage,
            modifier = Modifier
                .weight(0.39f)
                .fillMaxHeight(),
        )
        Box(
            modifier = Modifier
                .width(9.dp)
                .fillMaxHeight()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color(0x22835A45),
                            Color(0x55F7D9BE),
                            Color(0x33835A45),
                        ),
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .weight(0.61f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(topEnd = 18.dp, bottomEnd = 18.dp))
                .background(Color(0xF7FFFDF8))
                .border(0.7.dp, Color(0x1EA88966), RoundedCornerShape(topEnd = 18.dp, bottomEnd = 18.dp))
                .padding(8.dp),
        ) {
            when (route) {
                HandbookSection.OVERVIEW -> HandbookOverviewRoute(payload, onOpenSection, onOpenPage)
                HandbookSection.SCHEDULE -> HandbookScheduleRoute(payload, onOpenPage)
                HandbookSection.DIARY -> HandbookDiaryRoute(payload, onOpenPage)
                HandbookSection.TARGET -> HandbookTargetRoute(payload, onOpenPage)
            }
        }
    }
}

@Composable
private fun HandbookLeftIndexPage(
    route: HandbookSection,
    payload: HandbookRoutePayload,
    onOpenSection: (HandbookSection) -> Unit,
    onOpenPage: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheduleCount = payload.uiState.schedulePreviewEntries.size
    val doneCount = payload.uiState.schedulePreviewEntries.count { it.completed }
    val diaryBlockCount = payload.uiState.diaryDraft.lines().count { it.contains("|") || it.startsWith("#") }.coerceAtLeast(
        if (payload.uiState.diaryDraft.isBlank()) 0 else 1,
    )
    val targetPages = payload.book.pages.filterIsInstance<TargetPage>()
    val targetCount = targetPages.sumOf { page -> page.items.size }
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(topStart = 18.dp, bottomStart = 18.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFFFFEFC), Color(0xFFFFF8EE), Color(0xFFF3DEC8)),
                ),
            )
            .border(0.7.dp, Color(0x22A88966), RoundedCornerShape(topStart = 18.dp, bottomStart = 18.dp))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        Text("BOOK ACTIVITY", color = Color(0xFF8F684F), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
        Text(payload.book.title, color = Color(0xFF2F261D), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 2)
        Text(payload.book.subtitle, color = Color(0xFF7A7065), style = MaterialTheme.typography.labelSmall, maxLines = 2)

        Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.fillMaxWidth()) {
            HandbookIndexMiniMetric("日程", scheduleCount.toString(), routeColor(HandbookSection.SCHEDULE), Modifier.weight(1f))
            HandbookIndexMiniMetric("完成", doneCount.toString(), GoaldayDesign.Positive, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.fillMaxWidth()) {
            HandbookIndexMiniMetric("日记", diaryBlockCount.toString(), routeColor(HandbookSection.DIARY), Modifier.weight(1f))
            HandbookIndexMiniMetric("目标", targetCount.toString(), routeColor(HandbookSection.TARGET), Modifier.weight(1f))
        }

        Text("目录", color = Color(0xFF6F5B4B), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
        HandbookSection.entries.forEach { item ->
            HandbookIndexSectionRow(
                section = item,
                selected = item == route,
                count = payload.book.pages.count { page -> matchesHandbookSection(page, item) },
                onClick = { onOpenSection(item) },
            )
        }

        Text("页签", color = Color(0xFF6F5B4B), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
        payload.book.pages.forEachIndexed { index, page ->
            val selected = index == payload.uiState.selectedPageIndex
            HandbookIndexPageRow(
                title = monthLabelForPage(page.title, fallback = page.title),
                type = page.handbookPageTypeLabel(),
                selected = selected,
                onClick = { onOpenPage(index) },
            )
        }
    }
}

@Composable
private fun HandbookIndexMiniMetric(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 7.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(1.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(value, color = color, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, maxLines = 1)
        Text(label, color = GoaldayDesign.InkMuted, style = MaterialTheme.typography.labelSmall, maxLines = 1)
    }
}

@Composable
private fun HandbookIndexSectionRow(
    section: HandbookSection,
    selected: Boolean,
    count: Int,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) routeColor(section).copy(alpha = 0.16f) else Color.White.copy(alpha = 0.45f))
            .border(0.6.dp, if (selected) routeColor(section).copy(alpha = 0.28f) else Color(0x10A88966), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 7.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(1.dp), modifier = Modifier.weight(1f)) {
            Text(section.label, color = if (selected) routeColor(section) else Color(0xFF3D332A), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, maxLines = 1)
            Text(routeSubtitle(section), color = GoaldayDesign.InkMuted, style = MaterialTheme.typography.labelSmall, maxLines = 1)
        }
        Text(count.toString(), color = routeColor(section), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun HandbookIndexPageRow(
    title: String,
    type: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) Color(0x33E88FAE) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 7.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, color = if (selected) GoaldayDesign.Pink else GoaldayDesign.InkSecondary, style = MaterialTheme.typography.labelSmall, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal, maxLines = 1, modifier = Modifier.weight(1f))
        Text(type, color = GoaldayDesign.InkMuted, style = MaterialTheme.typography.labelSmall, maxLines = 1)
    }
}

private fun BookPage.handbookPageTypeLabel(): String =
    when (this) {
        is DiaryPage -> "DIARY"
        is TargetPage -> "TARGET"
        is SchedulePage -> "SCHEDULE"
        is PlanPage -> "PLAN"
    }

@Composable
private fun HandbookOverviewRoute(
    payload: HandbookRoutePayload,
    onOpenSection: (HandbookSection) -> Unit,
    onOpenPage: (Int) -> Unit,
) {
    val scheduleCount = payload.uiState.schedulePreviewEntries.size
    val doneCount = payload.uiState.schedulePreviewEntries.count { it.completed }
    val diaryBlockCount = payload.uiState.diaryDraft.lines().count { it.contains("|") || it.startsWith("#") }.coerceAtLeast(
        if (payload.uiState.diaryDraft.isBlank()) 0 else 1,
    )
    val targetCount = payload.book.pages.filterIsInstance<TargetPage>().sumOf { it.items.size }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        HandbookRouteHeader(
            route = HandbookSection.OVERVIEW,
            title = payload.book.title,
            subtitle = payload.book.subtitle,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            HandbookIndexMiniMetric("日程", scheduleCount.toString(), routeColor(HandbookSection.SCHEDULE), Modifier.weight(1f))
            HandbookIndexMiniMetric("完成", doneCount.toString(), GoaldayDesign.Positive, Modifier.weight(1f))
            HandbookIndexMiniMetric("日记", diaryBlockCount.toString(), routeColor(HandbookSection.DIARY), Modifier.weight(1f))
            HandbookIndexMiniMetric("目标", targetCount.toString(), routeColor(HandbookSection.TARGET), Modifier.weight(1f))
        }
        HandbookSection.entries
            .filterNot { it == HandbookSection.OVERVIEW }
            .forEach { item ->
                HandbookIndexSectionRow(
                    section = item,
                    selected = false,
                    count = payload.book.pages.count { page -> matchesHandbookSection(page, item) },
                    onClick = { onOpenSection(item) },
                )
            }
        Text("最近页签", color = Color(0xFF6F5B4B), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
        payload.book.pages.take(8).forEachIndexed { index, page ->
            HandbookIndexPageRow(
                title = monthLabelForPage(page.title, fallback = page.title),
                type = page.handbookPageTypeLabel(),
                selected = index == payload.uiState.selectedPageIndex,
                onClick = { onOpenPage(index) },
            )
        }
    }
}

@Composable
private fun HandbookScheduleRoute(
    payload: HandbookRoutePayload,
    onOpenPage: (Int) -> Unit,
) {
    HandbookRouteContent(HandbookSection.SCHEDULE, payload, onOpenPage) {
        HandbookScheduleRouteStrip(payload)
    }
}

@Composable
private fun HandbookDiaryRoute(
    payload: HandbookRoutePayload,
    onOpenPage: (Int) -> Unit,
) {
    HandbookRouteContent(HandbookSection.DIARY, payload, onOpenPage) {
        HandbookDiaryRouteStrip(payload)
    }
}

@Composable
private fun HandbookTargetRoute(
    payload: HandbookRoutePayload,
    onOpenPage: (Int) -> Unit,
) {
    HandbookRouteContent(HandbookSection.TARGET, payload, onOpenPage) {
        HandbookTargetRouteStrip(payload)
    }
}

@Composable
private fun HandbookRouteContent(
    route: HandbookSection,
    payload: HandbookRoutePayload,
    onOpenPage: (Int) -> Unit,
    routeAccessory: (@Composable () -> Unit)? = null,
) {
    fun routeNextIndex(): Int =
        if (route == HandbookSection.OVERVIEW) {
            payload.uiState.selectedPageIndex + 1
        } else {
            payload.sectionPages.getOrNull(payload.selectedSectionIndex + 1)
                ?.let { page -> payload.book.pages.indexOfFirst { it.title == page.title } }
                ?: -1
        }

    fun routePreviousIndex(): Int =
        if (route == HandbookSection.OVERVIEW) {
            payload.uiState.selectedPageIndex - 1
        } else {
            payload.sectionPages.getOrNull(payload.selectedSectionIndex - 1)
                ?.let { page -> payload.book.pages.indexOfFirst { it.title == page.title } }
                ?: -1
        }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        HandbookRouteHeader(
            route = route,
            title = payload.currentPage.title,
            subtitle = routeSubtitle(route),
        )
        routeAccessory?.invoke()
        Box(modifier = Modifier.weight(1f)) {
            BookReader(
                bookId = payload.book.id,
                bookTitle = payload.book.title,
                subtitle = "${route.label} · ${payload.book.subtitle}",
                page = payload.currentPage,
                previousPage = payload.previousPage,
                nextPage = payload.nextPage,
                pageIndex = payload.uiState.selectedPageIndex,
                pageCount = payload.book.pages.size,
                tint = payload.book.color,
                isSaved = payload.book.id in payload.uiState.savedBookIds,
                diaryDraft = payload.uiState.diaryDraft,
                customPageItems = payload.uiState.customPageItems,
                weeklyTheme = payload.uiState.weeklyTheme,
                todayPlanItems = payload.uiState.todayPlanItems,
                todayCompletedItems = payload.uiState.todayCompletedItems,
                schedulePreviewEntries = payload.uiState.schedulePreviewEntries,
                targetItemMeta = payload.uiState.targetItemMeta,
                onToggleSaved = payload.viewModel::toggleSavedCurrentBook,
                isChecked = { pageTitle, item -> payload.viewModel.isChecked(pageTitle, item) },
                onToggleChecked = { pageTitle, item -> payload.viewModel.toggleChecked(pageTitle, item) },
                onDiaryChange = payload.viewModel::updateDiaryDraft,
                onAddCustomItem = payload.viewModel::addCustomPageItem,
                onAddCustomItemWithDeadline = payload.viewModel::addCustomPageItemWithDeadline,
                onRemoveCustomItem = payload.viewModel::removeCustomPageItem,
                onRenameCustomItem = payload.viewModel::renameCustomPageItem,
                onAddToSchedule = payload.viewModel::addItemToSchedule,
                onAddHandbookPoolItem = payload.viewModel::addHandbookPoolItem,
                onRemoveHandbookPoolItem = payload.viewModel::removeHandbookPoolItem,
                onAddScheduleFromHandbook = payload.viewModel::addScheduleFromHandbook,
                onWeeklyThemeChange = payload.viewModel::updateWeeklyTheme,
                onMoveItemToToday = payload.viewModel::moveItemToToday,
                onMoveItemToCompleted = payload.viewModel::moveItemToCompleted,
                onRestoreItemFromToday = payload.viewModel::restoreItemFromToday,
                onRestoreItemFromCompleted = payload.viewModel::restoreItemFromCompleted,
                onUpdateScheduleTitle = payload.viewModel::updateScheduleTitleFromHandbook,
                onMoveScheduleDay = payload.viewModel::moveScheduleDayFromHandbook,
                onToggleScheduleCompleted = payload.viewModel::toggleScheduleCompletedFromHandbook,
                onUpdateTargetNote = payload.viewModel::updateTargetItemNote,
                onUpdateTargetDeadline = payload.viewModel::updateTargetItemDeadline,
                onOpenTargetDetail = payload.onOpenTargetDetail,
                shellStyle = ShellStyle.LIGHT,
                handbookMode = true,
                onFlipNext = {
                    val nextIndex = routeNextIndex()
                    if (nextIndex in payload.book.pages.indices) onOpenPage(nextIndex)
                },
                onFlipPrevious = {
                    val previousIndex = routePreviousIndex()
                    if (previousIndex in payload.book.pages.indices) onOpenPage(previousIndex)
                },
            )
        }
    }
}

@Composable
private fun HandbookScheduleRouteStrip(payload: HandbookRoutePayload) {
    val entries = payload.uiState.schedulePreviewEntries
    val todoCount = entries.count { !it.completed }
    val doneCount = entries.count { it.completed }
    val repeatCount = entries.count { it.repeatRule.isNotBlank() }
    HandbookRouteMetricStrip {
        HandbookRouteMetricPill("TODO", todoCount.toString(), GoaldayDesign.Pink, Modifier.weight(1f))
        HandbookRouteMetricPill("DONE", doneCount.toString(), GoaldayDesign.Positive, Modifier.weight(1f))
        HandbookRouteMetricPill("REPEAT", repeatCount.toString(), Color(0xFFB07A8F), Modifier.weight(1f))
    }
}

@Composable
private fun HandbookDiaryRouteStrip(payload: HandbookRoutePayload) {
    val raw = payload.uiState.diaryDraft
    val imageCount = raw.lines().count { line -> line.trim().startsWith("image:") || line.trim().startsWith("image|") }
    val targetCount = raw.lines().count { line ->
        val trimmed = line.trim()
        trimmed.startsWith("target|") || trimmed.startsWith("target_child|") || trimmed.startsWith("topic_target|")
    }
    val textCount = raw.lines().count { line -> line.trim().startsWith("text|") } +
        listOf("# 今日完成", "# 工作任务", "# 小幸福", "# 可改进").count { marker -> raw.contains(marker) }
    HandbookRouteMetricStrip {
        HandbookRouteMetricPill("TEXT", textCount.toString(), GoaldayDesign.InkSecondary, Modifier.weight(1f))
        HandbookRouteMetricPill("IMAGE", imageCount.toString(), Color(0xFFB07A8F), Modifier.weight(1f))
        HandbookRouteMetricPill("TARGET", targetCount.toString(), GoaldayDesign.Positive, Modifier.weight(1f))
    }
}

@Composable
private fun HandbookTargetRouteStrip(payload: HandbookRoutePayload) {
    val targetPage = payload.currentPage as? TargetPage
    val items = targetPage?.let { (it.items + payload.uiState.customPageItems).distinct() }.orEmpty()
    val checkedCount = targetPage?.let { page -> items.count { item -> payload.viewModel.isChecked(page.title, item) } } ?: 0
    val scheduledCount = items.count { item -> payload.uiState.schedulePreviewEntries.any { it.title == item } }
    HandbookRouteMetricStrip {
        HandbookRouteMetricPill("ITEMS", items.size.toString(), routeColor(HandbookSection.TARGET), Modifier.weight(1f))
        HandbookRouteMetricPill("DONE", checkedCount.toString(), GoaldayDesign.Positive, Modifier.weight(1f))
        HandbookRouteMetricPill("PLAN", scheduledCount.toString(), GoaldayDesign.Pink, Modifier.weight(1f))
    }
}

@Composable
private fun HandbookRouteMetricStrip(
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0x9FFFFCF6))
            .border(0.6.dp, Color(0x1EA88966), RoundedCornerShape(14.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        content()
    }
}

@Composable
private fun HandbookRouteMetricPill(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(99.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.SemiBold, maxLines = 1)
        Text(value, style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.InkMuted, maxLines = 1)
    }
}

@Composable
private fun HandbookRouteHeader(
    route: HandbookSection,
    title: String,
    subtitle: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(routeColor(route).copy(alpha = 0.08f))
            .border(0.5.dp, routeColor(route).copy(alpha = 0.16f), RoundedCornerShape(12.dp))
            .padding(horizontal = 9.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.weight(1f)) {
            Text(route.label, color = routeColor(route), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
            Text(title, color = Color(0xFF2F261D), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, maxLines = 1)
        }
        Text(
            subtitle,
            color = Color(0xFF7A7065),
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End,
        )
    }
}

private fun routeSubtitle(route: HandbookSection): String =
    when (route) {
        HandbookSection.OVERVIEW -> "整本手账 · 快速翻阅"
        HandbookSection.SCHEDULE -> "书内日程 · 月视图 / 待办 / 完成"
        HandbookSection.DIARY -> "日记块 · 图片 / 文字 / 目标"
        HandbookSection.TARGET -> "目标档案 · 详情 / 排期 / 备注"
    }

private fun routeColor(route: HandbookSection): Color =
    when (route) {
        HandbookSection.OVERVIEW -> Color(0xFF8F684F)
        HandbookSection.SCHEDULE -> Color(0xFFE88FAE)
        HandbookSection.DIARY -> Color(0xFFB07A8F)
        HandbookSection.TARGET -> Color(0xFF6F8E68)
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
    LaunchedEffect(forcedSegment, book.id) {
        val desired = forcedSegment ?: return@LaunchedEffect
        segment = desired
        val firstIndex = book.pages.indexOfFirst { page -> matchesSegment(page, desired) }
        if (firstIndex >= 0 && uiState.selectedPageIndex != firstIndex) {
            viewModel.setPage(firstIndex)
        }
    }
    val filteredPages = remember(book.pages, segment) {
        book.pages.filter { page ->
            matchesSegment(page, segment)
        }.ifEmpty { book.pages }
    }
    val segmentPageIndex = filteredPages.indexOfFirst { it.title == currentPage.title }.coerceAtLeast(0)
    val readerPreviousPage = filteredPages.getOrNull(segmentPageIndex - 1)
    val readerNextPage = filteredPages.getOrNull(segmentPageIndex + 1)
    var segmentSwipeDistance by remember(book.id) { mutableStateOf(0f) }
    var openedTargetDetail by remember(book.id, currentPage.title) { mutableStateOf<String?>(null) }
    var confirmDeletePage by remember(book.id, currentPage.title) { mutableStateOf(false) }
    var confirmDeleteBook by remember(book.id) { mutableStateOf(false) }

    fun realPageIndex(page: BookPage): Int =
        book.pages.indexOf(page).takeIf { it >= 0 }
            ?: book.pages.indexOfFirst { it.title == page.title }

    fun goToFilteredPage(filteredIndex: Int) {
        val page = filteredPages.getOrNull(filteredIndex) ?: return
        val realIndex = realPageIndex(page)
        if (realIndex in book.pages.indices) viewModel.setPage(realIndex)
    }

    fun switchSegment(next: BookSegment) {
        segment = next
        val firstIndex = book.pages.indexOfFirst { page -> matchesSegment(page, next) }
        if (firstIndex >= 0) viewModel.setPage(firstIndex)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 8.dp),
    ) {
        Column(Modifier.fillMaxSize()) {
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
                ActionChip(label = BookStrings.deletePage, color = Color(0xFF9C5A52), onClick = { confirmDeletePage = true })
                ActionChip(label = BookStrings.deleteBook, color = Color(0xFF9C5A52), onClick = { confirmDeleteBook = true })
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
                filteredPages.forEachIndexed { _, item ->
                    val realIndex = realPageIndex(item)
                    val selected = realIndex == uiState.selectedPageIndex
                    Text(
                        text = monthLabelForPage(item.title, fallback = item.title),
                        color = if (selected) Color.White else Color(0xFF6E6258),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        modifier = Modifier
                            .clip(RoundedCornerShape(99.dp))
                            .background(if (selected) Color(0xFFE88FAE) else Color(0x20FFFFFF))
                            .border(0.6.dp, if (selected) Color(0xFFFFF5F8) else Color(0x20A68B71), RoundedCornerShape(99.dp))
                            .clickable {
                                if (realIndex in book.pages.indices) viewModel.setPage(realIndex)
                            }
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
                    .background(
                        Brush.radialGradient(
                            listOf(
                                Color(0xFFFFF8F1),
                                Color(0xFFF1DECC),
                                Color(0xFFE4C8B2),
                            ),
                            center = Offset(520f, 260f),
                            radius = 900f,
                        ),
                    )
                    .padding(top = 6.dp, bottom = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.98f)
                        .fillMaxHeight(0.9f),
                ) {
                    BookReader(
                        bookId = book.id,
                        bookTitle = book.title,
                        subtitle = book.subtitle,
                        page = currentPage,
                        previousPage = readerPreviousPage,
                        nextPage = readerNextPage,
                        pageIndex = segmentPageIndex,
                        pageCount = filteredPages.size,
                        tint = book.color,
                        isSaved = book.id in uiState.savedBookIds,
                        diaryDraft = uiState.diaryDraft,
                        customPageItems = uiState.customPageItems,
                        weeklyTheme = uiState.weeklyTheme,
                        todayPlanItems = uiState.todayPlanItems,
                        todayCompletedItems = uiState.todayCompletedItems,
                        schedulePreviewEntries = uiState.schedulePreviewEntries,
                        targetItemMeta = uiState.targetItemMeta,
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
                        onUpdateTargetNote = viewModel::updateTargetItemNote,
                        onUpdateTargetDeadline = viewModel::updateTargetItemDeadline,
                        onOpenTargetDetail = { openedTargetDetail = it },
                        shellStyle = ShellStyle.BOOK,
                        handbookMode = true,
                        onFlipNext = { goToFilteredPage(segmentPageIndex + 1) },
                        onFlipPrevious = { goToFilteredPage(segmentPageIndex - 1) },
                    )
                }
            }
        } else {
            BookReader(
                bookId = book.id,
                bookTitle = book.title,
                subtitle = book.subtitle,
                page = currentPage,
                previousPage = readerPreviousPage,
                nextPage = readerNextPage,
                pageIndex = segmentPageIndex,
                pageCount = filteredPages.size,
                tint = book.color,
                isSaved = book.id in uiState.savedBookIds,
                diaryDraft = uiState.diaryDraft,
                customPageItems = uiState.customPageItems,
                weeklyTheme = uiState.weeklyTheme,
                todayPlanItems = uiState.todayPlanItems,
                todayCompletedItems = uiState.todayCompletedItems,
                schedulePreviewEntries = uiState.schedulePreviewEntries,
                targetItemMeta = uiState.targetItemMeta,
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
                onUpdateTargetNote = viewModel::updateTargetItemNote,
                onUpdateTargetDeadline = viewModel::updateTargetItemDeadline,
                onOpenTargetDetail = { openedTargetDetail = it },
                shellStyle = if (forcedSegment == BookSegment.DIARY || currentPage is DiaryPage) ShellStyle.BOOK else ShellStyle.LIGHT,
                handbookMode = handbookMode,
                onFlipNext = { goToFilteredPage(segmentPageIndex + 1) },
                onFlipPrevious = { goToFilteredPage(segmentPageIndex - 1) },
            )
        }
        }
        val targetPage = currentPage as? TargetPage
        val targetItems = targetPage?.let { (it.items + uiState.customPageItems).distinct() }.orEmpty()
        val targetItem = openedTargetDetail?.takeIf { it in targetItems }
        if (targetPage != null && targetItem != null) {
            TargetDetailRouteOverlay(
                pageTitle = targetPage.title,
                item = targetItem,
                itemIndex = targetItems.indexOf(targetItem).coerceAtLeast(0),
                itemCount = targetItems.size,
                checked = viewModel.isChecked(targetPage.title, targetItem),
                meta = uiState.targetItemMeta[targetItem] ?: TargetItemMeta(),
                scheduledEntries = uiState.schedulePreviewEntries
                    .filter { it.title == targetItem }
                    .sortedWith(compareBy<ScheduleEntry>({ it.month }, { it.day }, { it.timeText })),
                onClose = { openedTargetDetail = null },
                onToggleChecked = { viewModel.toggleChecked(targetPage.title, targetItem) },
                onUpdateNote = { viewModel.updateTargetItemNote(targetItem, it) },
                onUpdateDeadline = { viewModel.updateTargetItemDeadline(targetItem, it) },
                onAddToSchedule = { day -> viewModel.addItemToSchedule(targetItem, day) },
                onSaveAsOwnTarget = { viewModel.addCustomPageItem(targetItem) },
                onAddToDiary = { done -> viewModel.addTargetDetailToDiary(targetItem, completed = done) },
            )
        }
        if (confirmDeletePage) {
            DangerConfirmDialog(
                title = "删除这一页？",
                body = "将删除「${currentPage.title}」以及这一页的本地日记、目标勾选和任务池数据。",
                confirmText = "删除页",
                onDismiss = { confirmDeletePage = false },
                onConfirm = {
                    confirmDeletePage = false
                    viewModel.deleteCurrentPage()
                },
            )
        }
        if (confirmDeleteBook) {
            DangerConfirmDialog(
                title = "删除这本手账？",
                body = "将删除「${book.title}」及其中所有自定义页面和本地记录。这个操作无法撤销。",
                confirmText = "删除书",
                onDismiss = { confirmDeleteBook = false },
                onConfirm = {
                    confirmDeleteBook = false
                    viewModel.removeCurrentCustomBook()
                },
            )
        }
    }
}

@Composable
private fun DangerConfirmDialog(
    title: String,
    body: String,
    confirmText: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Text(
                body,
                color = GoaldayDesign.InkSecondary,
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(BookStrings.cancel)
            }
        },
    )
}

private fun monthLabelForPage(title: String, fallback: String): String {
    val regex = Regex("(\\d{1,2}月)")
    return regex.find(title)?.value ?: fallback
}

private fun matchesHandbookSection(page: BookPage, section: HandbookSection): Boolean =
    when (section) {
        HandbookSection.OVERVIEW -> true
        HandbookSection.SCHEDULE -> page is SchedulePage || page is PlanPage
        HandbookSection.DIARY -> page is DiaryPage
        HandbookSection.TARGET -> page is TargetPage
    }

private fun resolveHandbookSection(page: BookPage): HandbookSection =
    when (page) {
        is DiaryPage -> HandbookSection.DIARY
        is TargetPage -> HandbookSection.TARGET
        is SchedulePage, is PlanPage -> HandbookSection.SCHEDULE
    }

@Composable
private fun TargetDetailRouteOverlay(
    pageTitle: String,
    item: String,
    itemIndex: Int,
    itemCount: Int,
    checked: Boolean,
    meta: TargetItemMeta,
    scheduledEntries: List<ScheduleEntry>,
    onClose: () -> Unit,
    onToggleChecked: () -> Unit,
    onUpdateNote: (String) -> Unit,
    onUpdateDeadline: (Int?) -> Unit,
    onAddToSchedule: (Int) -> Unit,
    onSaveAsOwnTarget: () -> Unit,
    onAddToDiary: (Boolean) -> Unit,
) {
    val today = java.time.LocalDate.now().dayOfMonth
    val tomorrow = today + 1
    val weekend = today + (7 - java.time.LocalDate.now().dayOfWeek.value).coerceAtLeast(1)
    val deadlineLabel = meta.deadlineDay?.let { "${it}日" } ?: "未设置"
    val scheduleLabel = if (scheduledEntries.isEmpty()) "未排期" else "${scheduledEntries.size}条"
    var noteDraft by remember(item, meta.note) { mutableStateOf(meta.note) }
    var actionHint by remember(item) { mutableStateOf("") }
    fun appendDetailNote(line: String) {
        val next = buildList {
            noteDraft.lines().map(String::trim).filter(String::isNotBlank).forEach(::add)
            if (line !in this) add(line)
        }.joinToString("\n")
        noteDraft = next
        onUpdateNote(next)
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFFFFCF7), Color(0xFFFFF2E7), Color(0xFFF1D9C4)),
                ),
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xCCFFFDF8))
                .border(0.6.dp, Color(0x18A88966), RoundedCornerShape(bottomStart = 22.dp, bottomEnd = 22.dp))
                .padding(horizontal = 14.dp, vertical = 11.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "‹ 返回",
                color = Color(0xFF6F5B4B),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clip(RoundedCornerShape(99.dp))
                    .background(Color(0x10A88966))
                    .clickable(onClick = onClose)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text("TargetDetailActivity", color = Color(0xFFB07A8F), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                Text("本地目标详情", color = Color(0xFF7A7065), style = MaterialTheme.typography.labelSmall)
            }
            Text(
                if (checked) "DONE" else "TODO",
                color = if (checked) Color(0xFF4F7E55) else Color(0xFFE88FAE),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFFFFFEFC), Color(0xFFFFEAF1), Color(0xFFFFF3D7)),
                            start = Offset.Zero,
                            end = Offset(900f, 420f),
                        ),
                    )
                    .border(1.dp, Color(0x24A88966), RoundedCornerShape(24.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(11.dp),
            ) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.weight(1f)) {
                        Text(pageTitle, color = Color(0xFF8B7B6B), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                        Text(item, color = Color(0xFF2F261D), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                        Text("目标 ${itemIndex + 1}/$itemCount · $deadlineLabel · $scheduleLabel", color = Color(0xFF7A7065), style = MaterialTheme.typography.bodySmall)
                    }
                    Box(
                        modifier = Modifier
                            .size(58.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(if (checked) Color(0xFF6F8E68) else Color(0xFFE88FAE)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(if (checked) "✓" else "%02d".format(itemIndex + 1), color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    }
                }
                TargetDetailSummaryStrip(
                    checked = checked,
                    deadlineLabel = deadlineLabel,
                    scheduleLabel = scheduleLabel,
                    noteReady = noteDraft.isNotBlank(),
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DetailPill(if (checked) "切换完成" else "标记完成", active = checked, onClick = onToggleChecked)
                DetailPill("排入今天", active = false) { onAddToSchedule(today) }
                DetailPill("排入明天", active = false) { onAddToSchedule(tomorrow) }
                DetailPill("排入周末", active = false) { onAddToSchedule(weekend) }
                DetailPill("生成下一步", active = false) {
                    appendDetailNote("下一步：为「$item」安排一个 15 分钟行动")
                    actionHint = "已生成下一步"
                }
            }

            TargetDetailPanel(title = "目标选项", trailing = "本地保存") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TargetOptionRow(
                        code = "OWN",
                        title = "保存为我的目标",
                        subtitle = "加入当前目标页，后续可继续备注、排期和完成",
                        accent = GoaldayDesign.Positive,
                    ) {
                        onSaveAsOwnTarget()
                        appendDetailNote("我的目标：$item")
                        actionHint = "已保存为我的目标"
                    }
                    TargetOptionRow(
                        code = "DIARY",
                        title = "写入日记目标块",
                        subtitle = "生成 item_diary_target 风格记录，并同步到本地日程",
                        accent = Color(0xFFB07A8F),
                    ) {
                        onAddToDiary(checked)
                        appendDetailNote("日记：已把「$item」写入目标记录块")
                        actionHint = "已写入日记"
                    }
                    TargetOptionRow(
                        code = "REVIEW",
                        title = "加入复盘",
                        subtitle = "排入本周末，并写入复盘备注",
                        accent = Color(0xFFB07A8F),
                    ) {
                        appendDetailNote("复盘：本周检查「$item」推进情况")
                        onAddToSchedule(weekend)
                        actionHint = "已排入周末复盘"
                    }
                    TargetOptionRow(
                        code = "COPY",
                        title = "生成执行句",
                        subtitle = "把今天要推进的行动写进备注",
                        accent = GoaldayDesign.Pink,
                    ) {
                        appendDetailNote("执行句：今天推进「$item」")
                        actionHint = "已写入执行句"
                    }
                }
                if (actionHint.isNotBlank()) {
                    Text(actionHint, color = Color(0xFF7A7065), style = MaterialTheme.typography.labelSmall)
                }
            }

            TargetDetailPanel(title = "执行备注", trailing = if (noteDraft.isBlank()) "空白" else "已写") {
                OutlinedTextField(
                    value = noteDraft,
                    onValueChange = {
                        noteDraft = it
                        onUpdateNote(it)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 5,
                    placeholder = { Text("写下做法、灵感、阻碍或复盘") },
                )
            }

            TargetDetailPanel(title = "截止日", trailing = deadlineLabel) {
                Text(meta.deadlineDay?.let { "当前截止：${it}日" } ?: "当前未设置截止日", color = Color(0xFF7A7065), style = MaterialTheme.typography.bodySmall)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    DetailPill("今天", active = meta.deadlineDay == today) { onUpdateDeadline(today) }
                    DetailPill("明天", active = meta.deadlineDay == tomorrow) { onUpdateDeadline(tomorrow) }
                    DetailPill("周末", active = meta.deadlineDay == weekend) { onUpdateDeadline(weekend) }
                    DetailPill("清除", active = false) { onUpdateDeadline(null) }
                }
            }

            TargetDetailPanel(title = "已排期", trailing = scheduleLabel) {
                if (scheduledEntries.isEmpty()) {
                    Text("还没有排入日程", color = Color(0xFF8B7B6B), style = MaterialTheme.typography.bodySmall)
                } else {
                    scheduledEntries.take(10).forEach { entry ->
                        TargetScheduledEntryRow(entry = entry)
                    }
                }
            }
        }
    }
}

@Composable
private fun TargetDetailPanel(
    title: String,
    trailing: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xDFFFFEFC))
            .border(0.8.dp, Color(0x18A88966), RoundedCornerShape(18.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(title, color = Color(0xFF2F261D), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(trailing, color = Color(0xFF8B7B6B), style = MaterialTheme.typography.labelSmall, maxLines = 1)
        }
        content()
    }
}

@Composable
private fun TargetDetailSummaryStrip(
    checked: Boolean,
    deadlineLabel: String,
    scheduleLabel: String,
    noteReady: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xCCFFFDF8))
            .border(0.8.dp, Color(0x20A88966), RoundedCornerShape(18.dp))
            .padding(horizontal = 10.dp, vertical = 9.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TargetDetailMetric(
            label = "状态",
            value = if (checked) "DONE" else "TODO",
            active = checked,
            modifier = Modifier.weight(1f),
        )
        TargetDetailMetric(
            label = "截止",
            value = deadlineLabel,
            active = deadlineLabel != "未设置",
            modifier = Modifier.weight(1f),
        )
        TargetDetailMetric(
            label = "排期",
            value = scheduleLabel,
            active = scheduleLabel != "未排期",
            modifier = Modifier.weight(1f),
        )
        TargetDetailMetric(
            label = "备注",
            value = if (noteReady) "已写" else "空白",
            active = noteReady,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun TargetDetailMetric(
    label: String,
    value: String,
    active: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (active) Color(0x1439A76D) else Color(0x12E88FAE))
            .padding(horizontal = 8.dp, vertical = 7.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(label, color = Color(0xFF8B7B6B), style = MaterialTheme.typography.labelSmall, maxLines = 1)
        Text(value, color = if (active) Color(0xFF4F7E55) else Color(0xFFB07A8F), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}

@Composable
private fun TargetScheduledEntryRow(entry: ScheduleEntry) {
    val color = if (entry.completed) Color(0xFF6F8E68) else Color(0xFFE88FAE)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (entry.completed) Color(0x1039A76D) else Color(0x12E88FAE))
            .border(0.7.dp, color.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "${entry.month}/${entry.day}",
            color = Color.White,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(color)
                .padding(horizontal = 7.dp, vertical = 4.dp),
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(entry.title, color = Color(0xFF2F261D), style = MaterialTheme.typography.bodySmall, maxLines = 1)
            val repeatLabel = targetScheduleRepeatLabel(entry)
            Text(
                listOf(entry.note.ifBlank { "Goalday 本地日程" }, repeatLabel)
                    .filter(String::isNotBlank)
                    .joinToString(" · "),
                color = Color(0xFF8B7B6B),
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
            )
        }
        Text(
            entry.timeText.ifBlank { if (entry.completed) "done" else "todo" },
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
        )
    }
}

private fun targetScheduleRepeatLabel(entry: ScheduleEntry): String {
    if (entry.repeatRule.isBlank()) return ""
    val interval = entry.repeatInterval.coerceAtLeast(1)
    return when (entry.repeatRule) {
        "daily" -> if (interval == 1) "每天重复" else "每${interval}天重复"
        "weekly" -> if (interval == 1) "每周重复" else "每${interval}周重复"
        "monthly" -> if (interval == 1) "每月重复" else "每${interval}月重复"
        else -> ""
    }
}

@Composable
private fun DetailPill(
    label: String,
    active: Boolean,
    onClick: () -> Unit,
) {
    Text(
        label,
        color = if (active) Color.White else Color(0xFF6F5B4B),
        style = MaterialTheme.typography.labelMedium,
        modifier = Modifier
            .clip(RoundedCornerShape(99.dp))
            .background(if (active) Color(0xFF6F8E68) else Color(0x14000000))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
    )
}

@Composable
private fun TargetOptionRow(
    code: String,
    title: String,
    subtitle: String,
    accent: Color,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(accent.copy(alpha = 0.10f))
            .border(0.7.dp, accent.copy(alpha = 0.22f), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 9.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(58.dp)
                .height(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(accent),
            contentAlignment = Alignment.Center,
        ) {
            Text(code, color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, maxLines = 1)
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, color = Color(0xFF2F261D), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, maxLines = 1)
            Text(subtitle, color = Color(0xFF7A7065), style = MaterialTheme.typography.labelSmall, maxLines = 2)
        }
        Text("›", color = accent, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
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
    onApply: (List<String>, Boolean, Boolean) -> Unit,
    onSaveAsBook: (InspirationTemplate, List<String>) -> Unit,
) {
    val selected = templates[selectedIndex.coerceIn(0, templates.lastIndex)]
    val context = LocalContext.current
    val loadedTargetItems = remember(selected.id, selected.targetAssetPath) {
        loadTargetAssetItems(context, selected.targetAssetPath).ifEmpty { selected.items }
    }
    var checkedStates by remember(selected.id, loadedTargetItems) { mutableStateOf(List(loadedTargetItems.size) { true }) }
    var editableItems by remember(selected.id, loadedTargetItems) { mutableStateOf(loadedTargetItems) }
    var pushToToday by remember { mutableStateOf(true) }
    var clearSourceAfterApply by remember { mutableStateOf(false) }
    val catalogStatus = remember(selected.catalogPath) {
        loadTopicCatalogSummary(context, selected.catalogPath)
    }

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
                    val previewCount = remember(item.id, item.targetAssetPath) {
                        loadTargetAssetItems(context, item.targetAssetPath).ifEmpty { item.items }.size
                    }
                    Box(
                        modifier = Modifier
                            .width(190.dp)
                            .height(118.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(topicCoverBrush(item, index))
                            .border(
                                width = if (index == selectedIndex) 2.dp else 1.dp,
                                color = if (index == selectedIndex) Color.White else Color.White.copy(alpha = 0.35f),
                                shape = RoundedCornerShape(14.dp),
                            )
                            .clickable { onSelect(index) }
                            .padding(12.dp),
                    ) {
                        TopicCoverArt(template = item, index = index, compact = true)
                        Text(
                            "${previewCount}项",
                            color = Color(0xEFFFFFFF),
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .clip(RoundedCornerShape(99.dp))
                                .background(Color(0x22FFFFFF))
                                .padding(horizontal = 7.dp, vertical = 2.dp),
                        )
                        Column(
                            modifier = Modifier.align(Alignment.BottomStart),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(item.title, color = Color.White, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Text("${item.category} · ${item.subtitle}", color = Color.White.copy(alpha = 0.84f), style = MaterialTheme.typography.labelSmall, maxLines = 1)
                        }
                    }
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xF9FFFFFF))
                .padding(14.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(topicCoverBrush(selected, selectedIndex))
                        .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(20.dp))
                        .padding(16.dp),
                ) {
                    TopicCoverArt(template = selected, index = selectedIndex)
                    Text(
                        "${selected.category} · ${loadedTargetItems.size} 个目标",
                        color = Color(0xEFFFFFFF),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .clip(RoundedCornerShape(99.dp))
                            .background(Color(0x22FFFFFF))
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                    )
                    Column(
                        modifier = Modifier.align(Alignment.BottomStart),
                        verticalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        Text(selected.title, style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.SemiBold)
                        Text(selected.subtitle, style = MaterialTheme.typography.bodySmall, color = Color(0xEFFFFFFF))
                        Text(if (selected.linkToSchedule) "可导入任务池 · 可保存成手账本" else "适合复盘记录 · 可保存成手账本", style = MaterialTheme.typography.labelSmall, color = Color(0xDFFFFFFF))
                    }
                }
                Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text("目标详情", style = MaterialTheme.typography.titleMedium, color = Color(0xFF2F261D), fontWeight = FontWeight.SemiBold)
                    Column(horizontalAlignment = Alignment.End) {
                        Text(catalogStatus.label, style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B7B6B))
                        Text(catalogStatus.assetLabel, style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B7B6B))
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0x0FA68B71))
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "本地资源",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF5E5147),
                        fontWeight = FontWeight.SemiBold,
                    )
                    Column(horizontalAlignment = Alignment.End) {
                        Text("${selected.coverKey}.png", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B7B6B))
                        Text("${selected.targetKey}.txt · ${loadedTargetItems.size} 条", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B7B6B))
                    }
                }
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
                if (editableItems.size < 60) {
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
    preset: PageDialogPreset,
    onDismiss: () -> Unit,
    onConfirm: (type: String, title: String) -> Unit,
) {
    var title by remember(preset) { mutableStateOf(preset.title) }
    var type by remember(preset) { mutableStateOf(preset.type) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                enabled = title.isNotBlank(),
                onClick = { onConfirm(type, title) },
            ) {
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
                                .clickable {
                                    if (title.isBlank() || title in defaultPageTitles) {
                                        title = defaultPageTitle(key)
                                    }
                                    type = key
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                        )
                    }
                }
            }
        },
    )
}

private val defaultPageTitles = setOf("目标页", "计划页", "日程页", "日记页")

private fun defaultPageTitle(type: String): String =
    when (type) {
        "target" -> "目标页"
        "plan" -> "计划页"
        "schedule" -> "日程页"
        "diary" -> "日记页"
        else -> "新页面"
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
