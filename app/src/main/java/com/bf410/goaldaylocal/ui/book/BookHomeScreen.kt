package com.bf410.goaldaylocal.ui.book

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.YearMonth
import com.bf410.goaldaylocal.data.BookPage
import com.bf410.goaldaylocal.data.DiaryPage
import com.bf410.goaldaylocal.data.PlanPage
import com.bf410.goaldaylocal.data.ScheduleEntry
import com.bf410.goaldaylocal.data.SchedulePage
import com.bf410.goaldaylocal.data.TargetItemMeta
import com.bf410.goaldaylocal.data.TargetPage
import com.bf410.goaldaylocal.data.TopicBook
import com.bf410.goaldaylocal.ui.inspiration.InspirationScreen
import com.bf410.goaldaylocal.ui.replica.GoaldayDesign
import com.bf410.goaldaylocal.ui.replica.GoaldaySegmentBar
import com.bf410.goaldaylocal.ui.replica.GoaldayTopBar

private val bookPalette = GoaldayDesign.BookCoverPalette

private enum class BookSegment(val label: String) {
    LIST("清单"),
    WEEK("周"),
    MONTH("月"),
    DIARY("记录"),
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
    onBack: () -> Unit = {},
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
    val landingBookIndex = remember(uiState.books, entryMode) { entryLandingBookIndex(uiState.books, entryMode) }
    // P0-5 修复：HANDBOOK/DIARY 模式下优先用 landingBookIndex 渲染，避免入口 loading 闪烁
    // 原代码在 selectedBookIndex != landingBookIndex 时 return loading，进入时短暂闪烁
    // 现在直接用 landingBookIndex 渲染，LaunchedEffect 后台同步 selectedBookIndex，无闪烁
    val safeBookIndex = if (entryMode == BookEntryMode.HANDBOOK || entryMode == BookEntryMode.DIARY) {
        landingBookIndex.coerceIn(0, (uiState.books.lastIndex).coerceAtLeast(0))
    } else {
        uiState.selectedBookIndex.coerceIn(0, (uiState.books.lastIndex).coerceAtLeast(0))
    }
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
    // P0-5 修复：删除原 loading return 块
    // 原代码在此处 return "正在打开本地手账..."，但 selectedBookIndex 与 landingBookIndex
    // 的短暂不一致会导致进入时 loading 闪烁。现已让 safeBookIndex 直接用 landingBookIndex
    // 渲染正确内容，LaunchedEffect 在后台同步 viewModel.selectedBookIndex，无需 loading 中转

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
            if (showInspiration) {
                InspirationScreen(
                    viewModel = viewModel,
                    onOpenHandbook = { showInspiration = false },
                )
            } else {
                BookDetailView(
                    viewModel = viewModel,
                    book = book,
                    currentPage = currentPage,
                    previousPage = previousPage,
                    nextPage = nextPage,
                    uiState = uiState.copy(selectedBookIndex = safeBookIndex),
                    onBackToLibrary = onBack,
                    onShowAddPage = {
                        pageDialogPreset = PageDialogPreset(type = "schedule", title = "日程页")
                        showPageDialog = true
                    },
                    onShowRenamePage = { showRenamePageDialog = true },
                    onShowEditBook = { showEditBookDialog = true },
                    onToggleManagePanel = { showManagePanel = !showManagePanel },
                    showManagePanel = showManagePanel,
                    forcedSegment = null,
                    bookOnlyMode = true,
                    onShowInspiration = { showInspiration = true },
                )
            }
        }

        BookEntryMode.DIARY -> {
            if (!hasBooks) return
            val book = uiState.books[safeBookIndex]
            if (book.pages.none { it is DiaryPage }) {
                BookUnavailableState("这本手账没有日记页", "日记入口需要至少一个日记页，添加后会自动保存到本机。", "添加日记页") {
                    pageDialogPreset = PageDialogPreset(type = "diary", title = "日记页")
                    showPageDialog = true
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
            if (showInspiration) {
                InspirationScreen(
                    viewModel = viewModel,
                    onOpenHandbook = { showInspiration = false },
                )
            } else {
                BookDetailView(
                    viewModel = viewModel,
                    book = book,
                    currentPage = currentPage,
                    previousPage = previousPage,
                    nextPage = nextPage,
                    uiState = uiState.copy(selectedBookIndex = safeBookIndex),
                    onBackToLibrary = onBack,
                    onShowAddPage = {
                        pageDialogPreset = PageDialogPreset(type = "diary", title = "日记页")
                        showPageDialog = true
                    },
                    onShowRenamePage = { showRenamePageDialog = true },
                    onShowEditBook = { showEditBookDialog = true },
                    onToggleManagePanel = { showManagePanel = !showManagePanel },
                    showManagePanel = showManagePanel,
                    forcedSegment = BookSegment.DIARY,
                    bookOnlyMode = false,
                    onShowInspiration = { showInspiration = true },
                )
            }
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
                        GoaldayDesign.adaptiveSurface,
                        GoaldayDesign.PaperWarm,
                        GoaldayDesign.PaperAged,
                    ),
                ),
            )
            .padding(GoaldayDesign.Space6),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(GoaldayDesign.Radius2XL))
                .background(GoaldayDesign.adaptiveSurface.copy(alpha = 0.94f))
                .border(0.8.dp, GoaldayDesign.adaptiveBorderColor.copy(alpha = 0.14f), RoundedCornerShape(GoaldayDesign.Radius2XL))
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space2 + 2.dp),
        ) {
            Text(
                title,
                color = GoaldayDesign.adaptiveInkPrimary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
            Text(
                body,
                color = GoaldayDesign.adaptiveInkMuted,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
            )
            Text(
                action,
                color = GoaldayDesign.adaptiveSurface,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
                    .background(GoaldayDesign.PrimaryAction)
                    .clickable(onClick = onAction)
                    .padding(horizontal = GoaldayDesign.Space4, vertical = GoaldayDesign.Space2 + 1.dp),
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
            .background(GoaldayDesign.LibraryDeskGradient)
            .padding(top = GoaldayDesign.Space3),
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
                    color = GoaldayDesign.adaptiveInkPrimary,
                )
                Text(
                    text = BookStrings.librarySummary.format(books.size, customBookCount),
                    style = MaterialTheme.typography.labelMedium,
                    color = GoaldayDesign.adaptiveInkSecondary,
                )
            }
            Text(
                text = BookStrings.createBook,
                color = GoaldayDesign.adaptiveSurface,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
                    .background(GoaldayDesign.adaptiveInkPrimary)
                    .clickable(onClick = onCreateBook)
                    .padding(horizontal = GoaldayDesign.Space3 + 1.dp, vertical = GoaldayDesign.Space2),
            )
        }
        Spacer(Modifier.height(GoaldayDesign.Space3))
        Text(
            text = BookStrings.librarySubtitle,
            style = MaterialTheme.typography.bodySmall,
            color = GoaldayDesign.adaptiveInkSecondary,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(GoaldayDesign.RadiusL))
                .background(GoaldayDesign.adaptiveSurface.copy(alpha = 0.4f))
                .border(0.7.dp, GoaldayDesign.adaptiveBorderColor.copy(alpha = 0.09f), RoundedCornerShape(GoaldayDesign.RadiusL))
                .padding(horizontal = GoaldayDesign.Space3, vertical = GoaldayDesign.Space2 + 1.dp),
        )
        Spacer(Modifier.height(GoaldayDesign.Space3 + 2.dp))
        Column(
            verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space4 + 2.dp),
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
    val coverShape = RoundedCornerShape(GoaldayDesign.Radius3XL)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(GoaldayDesign.Space12 + GoaldayDesign.Space12 + GoaldayDesign.Space3 + 2.dp)
            .shadow(GoaldayDesign.ShadowLarge + GoaldayDesign.Space2, coverShape, clip = false)
            .clip(coverShape)
            .background(
                Brush.linearGradient(
                    listOf(
                        book.color.copy(alpha = 0.94f),
                        book.color.copy(alpha = 0.74f),
                        GoaldayDesign.CoverWhiteOverlayLow,
                    ),
                    start = Offset.Zero,
                    end = Offset(920f, 620f),
                ),
            )
            .border(GoaldayDesign.Hairline, GoaldayDesign.CoverWhiteOverlaySubtle, coverShape)
            .clickable(onClick = onClick),
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.radialGradient(
                        listOf(GoaldayDesign.CoverWhiteOverlayMedium, Color.Transparent),
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
                        listOf(GoaldayDesign.CoverDarkSpineStrong, GoaldayDesign.CoverWhiteOverlayHairline, Color.Transparent),
                    ),
                ),
        )
        Text(
            "GOALDAY",
            color = GoaldayDesign.CoverWhiteOverlayHigh,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .graphicsLayer { rotationZ = -90f }
                    .padding(bottom = GoaldayDesign.Space2),
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
                            listOf(GoaldayDesign.CoverWhiteOverlaySubtle, GoaldayDesign.CoverPageEdgeWarm),
                        ),
                    ),
            )
        }
        Column(
            modifier = Modifier
                .padding(start = GoaldayDesign.Space12 + GoaldayDesign.Space2 + 2.dp, top = GoaldayDesign.Space5 + GoaldayDesign.Space1, end = GoaldayDesign.Space6 + GoaldayDesign.Space2, bottom = GoaldayDesign.Space5 + 2.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space2)) {
                Text("本地手账", style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.CoverDarkSpineStrong, fontWeight = FontWeight.SemiBold)
                Text(book.title, style = MaterialTheme.typography.headlineSmall, color = GoaldayDesign.adaptiveInkPrimary, fontWeight = FontWeight.SemiBold)
                Text(book.subtitle, style = MaterialTheme.typography.bodyMedium, color = GoaldayDesign.adaptiveInkSecondary)
            }
            Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("${book.pages.size} ${BookStrings.pageUnit}", style = MaterialTheme.typography.labelLarge, color = GoaldayDesign.adaptiveInkSecondary)
                    book.pages.take(3).forEach { page ->
                        Text(
                            pageRouteLabel(page),
                            color = routeColor(resolveSegment(page)),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
                                .background(GoaldayDesign.CoverWhiteOverlayMedium)
                                .padding(horizontal = GoaldayDesign.Space2, vertical = GoaldayDesign.Space1),
                        )
                    }
                }
                Text(
                    "打开这本",
                    color = GoaldayDesign.adaptiveSurface,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
                        .background(GoaldayDesign.adaptiveInkPrimary)
                        .padding(horizontal = GoaldayDesign.Space3, vertical = GoaldayDesign.Space2),
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
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("书架", color = GoaldayDesign.adaptiveInkPrimary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                "${books.size} 本",
                color = GoaldayDesign.adaptiveInkSecondary,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
                    .background(GoaldayDesign.adaptiveSurface.copy(alpha = 0.4f))
                    .border(0.6.dp, GoaldayDesign.adaptiveBorderColor.copy(alpha = 0.12f), RoundedCornerShape(GoaldayDesign.RadiusPill))
                    .padding(horizontal = GoaldayDesign.Space2 + 1.dp, vertical = GoaldayDesign.Space1),
            )
        }
        books.chunked(3).forEachIndexed { rowIndex, row ->
            Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space2 + 1.dp),
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(GoaldayDesign.Space12 + GoaldayDesign.Space12 + GoaldayDesign.Space3 - 2.dp),
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
                        .height(GoaldayDesign.Space3 + 2.dp)
                        .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
                        .background(
                            Brush.verticalGradient(
                                listOf(GoaldayDesign.ShelfWood, GoaldayDesign.ShelfWoodDark),
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
    val shape = RoundedCornerShape(GoaldayDesign.RadiusL, GoaldayDesign.RadiusL, 9.dp, 9.dp)
    val lineColor = GoaldayDesign.adaptiveInkSecondary
    Box(
        modifier = modifier
            .fillMaxHeight()
            .shadow(GoaldayDesign.Space2, shape, clip = false)
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(book.color.copy(alpha = 0.96f), book.color.copy(alpha = 0.76f), GoaldayDesign.Paper),
                    start = Offset.Zero,
                    end = Offset(280f, 520f),
                ),
            )
            .border(GoaldayDesign.Hairline + 0.1.dp, GoaldayDesign.CoverWhiteOverlayBorder, shape)
            .drawBehind {
                // 书页厚度堆叠线：右侧 3 条递减细线，模拟内页层叠纹理
                val w = size.width
                val h = size.height
                val top = 6.dp.toPx()
                repeat(3) { i ->
                    val x = w - 4.dp.toPx() - i * 3.dp.toPx()
                    drawLine(
                        color = lineColor.copy(alpha = 0.20f - i * 0.05f),
                        start = Offset(x, top),
                        end = Offset(x, h - top),
                        strokeWidth = 0.6.dp.toPx(),
                    )
                }
            }
            .clickable(onClick = onClick)
            .padding(GoaldayDesign.Space2 + 2.dp),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .width(GoaldayDesign.Space4)
                .fillMaxHeight()
                .background(GoaldayDesign.CoverDarkSpine, RoundedCornerShape(GoaldayDesign.RadiusPill)),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = GoaldayDesign.Space5),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(book.title, color = GoaldayDesign.adaptiveInkPrimary, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 3)
            Column(verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space1)) {
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.CenterVertically) {
                    book.pages.take(3).forEach { page ->
                        Box(
                            modifier = Modifier
                                .width(10.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
                                .background(routeColor(resolveSegment(page)).copy(alpha = 0.76f)),
                        )
                    }
                }
                Text("${book.pages.size}页", color = GoaldayDesign.adaptiveInkSecondary, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun AddBookShelfCard(onCreateBook: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(GoaldayDesign.Space12 + GoaldayDesign.Space4 + 4.dp)
            .shadow(GoaldayDesign.Space1 + 2.dp, RoundedCornerShape(GoaldayDesign.RadiusXL), clip = false)
            .clip(RoundedCornerShape(GoaldayDesign.RadiusXL))
            .background(GoaldayDesign.adaptiveSurface.copy(alpha = 0.48f))
            .border(GoaldayDesign.Hairline, GoaldayDesign.adaptiveBorderColor.copy(alpha = 0.19f), RoundedCornerShape(GoaldayDesign.RadiusXL))
            .clickable(onClick = onCreateBook)
            .padding(horizontal = GoaldayDesign.Space4),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space1)) {
            Icon(Icons.Filled.Add, contentDescription = null, tint = GoaldayDesign.adaptiveInkSecondary, modifier = Modifier.size(16.dp))
            Text("新建一本手账", color = GoaldayDesign.adaptiveInkSecondary, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        }
    }
}

private fun pageRouteLabel(page: BookPage): String =
    when (page) {
        is SchedulePage -> "日程"
        is DiaryPage -> "日记"
        is TargetPage -> "目标"
        is PlanPage -> "计划"
    }

@Composable
private fun HandbookReadingDeskHeader(
    book: TopicBook,
    currentPage: BookPage,
    filteredPages: List<BookPage>,
    selectedRealPageIndex: Int,
    onOpenPage: (BookPage) -> Unit,
) {
    val route = resolveSegment(currentPage)
    val routeMetrics = metricsForPages(filteredPages)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(GoaldayDesign.RadiusL))
            .background(GoaldayDesign.CardPaperGradient)
            .border(GoaldayDesign.Hairline, GoaldayDesign.adaptiveBorderColor.copy(alpha = 0.16f), RoundedCornerShape(GoaldayDesign.RadiusL))
            .padding(horizontal = GoaldayDesign.Space3, vertical = GoaldayDesign.Space2),
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // P1-4 精简：删除原副标题"${book.title} · ${route.label}路线 · 本地保存"
            // book.title 已在 TopBar 显示，"本地保存"与右侧"离线"chip 重复，route.label 由指标行体现
            Text(
                text = monthLabelForPage(currentPage.title, fallback = book.title),
                color = GoaldayDesign.adaptiveInkPrimary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "离线",
                color = GoaldayDesign.adaptiveSurface,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
                    .background(routeColor(route))
                    .padding(horizontal = GoaldayDesign.Space2 + 1.dp, vertical = GoaldayDesign.Space1),
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            // P1-4 精简：删除"全书"指标（与 TopBar 的书本信息重复），只保留页数+内容
            HandbookDeskMetric("页数", routeMetrics.pageCount.coerceAtLeast(1).toString(), routeColor(route), Modifier.weight(1f))
            HandbookDeskMetric("内容", routeMetrics.itemCount.toString(), GoaldayDesign.adaptiveInkSecondary, Modifier.weight(1f))
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
        ) {
            filteredPages.forEach { item ->
                val selected = book.pages.indexOf(item) == selectedRealPageIndex
                Text(
                    text = monthLabelForPage(item.title, fallback = item.title),
                    color = if (selected) GoaldayDesign.adaptiveSurface else GoaldayDesign.adaptiveInkSecondary,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    modifier = Modifier
                        .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
                        .background(if (selected) routeColor(route) else GoaldayDesign.CoverWhiteOverlayMedium)
                        .border(0.6.dp, if (selected) GoaldayDesign.CoverWhiteOverlayHigh else GoaldayDesign.adaptiveBorderColor.copy(alpha = 0.12f), RoundedCornerShape(GoaldayDesign.RadiusPill))
                        .clickable { onOpenPage(item) }
                        .padding(horizontal = GoaldayDesign.Space3 - 1.dp, vertical = GoaldayDesign.Space2 - 3.dp),
                )
            }
        }
    }
}

@Composable
private fun HandbookDeskMetric(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(GoaldayDesign.RadiusXL))
            .background(GoaldayDesign.CoverWhiteOverlayMedium.copy(alpha = 0.58f))
            .border(0.6.dp, color.copy(alpha = 0.16f), RoundedCornerShape(GoaldayDesign.RadiusXL))
            .padding(horizontal = GoaldayDesign.Space2, vertical = GoaldayDesign.Space2 - 1.dp),
        verticalArrangement = Arrangement.spacedBy(1.dp),
    ) {
        Text(value, color = color, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 1)
        Text(label, color = GoaldayDesign.adaptiveInkMuted, style = MaterialTheme.typography.labelSmall, maxLines = 1)
    }
}

@Composable
private fun routeColor(route: BookSegment): Color =
    when (route) {
        BookSegment.LIST -> GoaldayDesign.RouteTarget
        BookSegment.WEEK, BookSegment.MONTH -> GoaldayDesign.RouteSchedule
        BookSegment.DIARY -> GoaldayDesign.RouteDiary
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
        // 切换入口模式时，如果当前页已经符合目标 segment，优先保留当前页
        val targetIndex = if (matchesSegment(currentPage, desired)) {
            uiState.selectedPageIndex
        } else {
            book.pages.indexOfFirst { page -> matchesSegment(page, desired) }
        }
        if (targetIndex >= 0 && uiState.selectedPageIndex != targetIndex) {
            viewModel.setPage(targetIndex)
        }
    }
    LaunchedEffect(currentPage, book.id) {
        segment = resolveSegment(currentPage)
    }
    val filteredPages = remember(book.pages, segment, bookOnlyMode) {
        // HANDBOOK 入口（bookOnlyMode=true）：显示全部页面，允许翻到日记页
        // 其他入口（DIARY 等）：按 segment 过滤
        if (bookOnlyMode) {
            book.pages
        } else {
            book.pages.filter { page -> matchesSegment(page, segment) }.ifEmpty { book.pages }
        }
    }
    val segmentPageIndex = filteredPages.indexOfFirst { it === currentPage }.coerceAtLeast(0)
    val readerPreviousPage = filteredPages.getOrNull(segmentPageIndex - 1)
    val readerNextPage = filteredPages.getOrNull(segmentPageIndex + 1)
    var segmentSwipeDistance by remember(book.id) { mutableStateOf(0f) }
    var openedTargetDetail by remember(book.id, currentPage.title) { mutableStateOf<String?>(null) }
    var confirmDeletePage by remember(book.id, currentPage.title) { mutableStateOf(false) }
    var confirmDeleteBook by remember(book.id) { mutableStateOf(false) }

    fun realPageIndex(page: BookPage): Int =
        book.pages.indexOf(page).takeIf { it >= 0 }
            ?: book.pages.indexOfFirst { it === page }

    fun goToFilteredPage(filteredIndex: Int) {
        val page = filteredPages.getOrNull(filteredIndex) ?: return
        val realIndex = realPageIndex(page)
        if (realIndex in book.pages.indices) viewModel.setPage(realIndex)
    }

    fun switchSegment(next: BookSegment) {
        segment = next
        // 切换 segment 时，如果当前页已符合目标 segment，优先保留当前页（周/月共享日程页）
        val targetIndex = if (matchesSegment(currentPage, next)) {
            uiState.selectedPageIndex
        } else {
            book.pages.indexOfFirst { page -> matchesSegment(page, next) }
        }
        if (targetIndex >= 0 && uiState.selectedPageIndex != targetIndex) {
            viewModel.setPage(targetIndex)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = if (handbookMode) 0.dp else GoaldayDesign.Space2),
    ) {
        Column(Modifier.fillMaxSize()) {
            if (handbookMode) {
                // 对照 fragment_main_page.xml: 顶部 Tab 导航栏
                // 背景色 #E5DAD4，minHeight=49dp，paddingBottom=5dp
                // Tab 文字 18sp bold，选中黑色，未选中 #36000000
                // 对照 fragment_main_page.xml / toolbar_normal.xml：
                // 顶部 Toolbar 为左右结构，左侧返回、中间标题/tab、右侧“完成”按钮。
                // 顶部 tab 回退到原版 BookSegment（清单/周/月/记录）。
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 49.dp)
                        .background(Color(0xFFE5DAD4))
                        .padding(bottom = 5.dp),
                ) {
                    // 左侧返回按钮
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回",
                        tint = Color(0x36000000),
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = GoaldayDesign.Space2, top = 8.dp, bottom = 8.dp)
                            .size(22.dp)
                            .clickable { onBackToLibrary() }
                            .padding(2.dp),
                    )
                    // 中间 Tab 文字：对照 tab_main.xml textSize=18dip bold，严格居中
                    Row(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        BookSegment.entries.forEachIndexed { idx, seg ->
                            val isSelected = BookSegment.entries.indexOf(segment) == idx
                            Text(
                                text = seg.label,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.Black else Color(0x36000000),
                                modifier = Modifier
                                    .clickable { switchSegment(seg) }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                            )
                        }
                    }
                    // 右侧“完成”按钮（对齐原 APK toolbar_normal.xml 右上角）
                    Text(
                        text = "完成",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GoaldayDesign.adaptiveInkSecondary,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = GoaldayDesign.Space2, top = 8.dp, bottom = 8.dp)
                            .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
                            .clickable { onBackToLibrary() }
                            .padding(horizontal = GoaldayDesign.Space2 + 2.dp, vertical = GoaldayDesign.Space1 + 2.dp),
                    )
                }
            } else {
                GoaldayTopBar(
                leftTitle = book.title,
                rightPrimaryText = "完成",
                onRightPrimaryClick = { onBackToLibrary() },
                onBackClick = { onBackToLibrary() },
                rightSecondary = {
                    if (forcedSegment != BookSegment.DIARY) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
                                .clickable(onClick = onShowInspiration)
                                .padding(horizontal = GoaldayDesign.Space1 + 2.dp, vertical = 2.dp),
                        ) {
                            Icon(Icons.Filled.Lightbulb, contentDescription = "灵感", tint = GoaldayDesign.adaptiveInkMuted, modifier = Modifier.size(14.dp))
                            Text("灵感", color = GoaldayDesign.adaptiveInkMuted, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    if (book.id.startsWith("custom_")) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
                                .clickable(onClick = onToggleManagePanel)
                                .padding(horizontal = GoaldayDesign.Space1 + 2.dp, vertical = 2.dp),
                        ) {
                            Icon(Icons.Filled.Settings, contentDescription = "管理", tint = GoaldayDesign.adaptiveInkMuted, modifier = Modifier.size(14.dp))
                            Text("管理", color = GoaldayDesign.adaptiveInkMuted, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                },
                )
            }
        if (!handbookMode) {
            GoaldaySegmentBar(
                items = BookSegment.entries.map { it.label },
                selectedIndex = BookSegment.entries.indexOf(segment).coerceAtLeast(0),
                onSelect = { idx -> switchSegment(BookSegment.entries[idx]) },
            )
        }
        if (showManagePanel && book.id.startsWith("custom_")) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(GoaldayDesign.RadiusL))
                    .background(GoaldayDesign.adaptiveSurface.copy(alpha = 0.96f))
                    .border(0.7.dp, GoaldayDesign.adaptiveBorderColor.copy(alpha = 0.13f), RoundedCornerShape(GoaldayDesign.RadiusL))
                    .padding(horizontal = GoaldayDesign.Space2 + 1.dp, vertical = GoaldayDesign.Space2),
                verticalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("手账管理", color = GoaldayDesign.adaptiveInkSecondary, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    Text("${uiState.selectedPageIndex + 1}/${book.pages.size}", color = GoaldayDesign.adaptiveInkMuted, style = MaterialTheme.typography.labelSmall)
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space2),
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                ) {
                    ActionChip(label = BookStrings.editBook, color = GoaldayDesign.adaptiveInkSecondary, onClick = onShowEditBook)
                    ActionChip(label = BookStrings.addPage, color = GoaldayDesign.adaptiveInkSecondary, onClick = onShowAddPage)
                    ActionChip(label = BookStrings.renamePage, color = GoaldayDesign.adaptiveInkSecondary, onClick = onShowRenamePage)
                    ActionChip(label = BookStrings.moveLeft, color = GoaldayDesign.adaptiveInkSecondary, onClick = viewModel::moveCurrentPageLeft)
                    ActionChip(label = BookStrings.moveRight, color = GoaldayDesign.adaptiveInkSecondary, onClick = viewModel::moveCurrentPageRight)
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space2),
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                ) {
                    ActionChip(label = BookStrings.deletePage, color = GoaldayDesign.Danger, onClick = { confirmDeletePage = true })
                    ActionChip(label = BookStrings.deleteBook, color = GoaldayDesign.Danger, onClick = { confirmDeleteBook = true })
                }
            }
        }
        if (forcedSegment == null && !handbookMode) {
            Spacer(Modifier.height(GoaldayDesign.Space3))
            Row(
                horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space2 + 2.dp),
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
                    val index = book.pages.indexOfFirst { it === item }.coerceAtLeast(0)
                    Text(
                        text = item.title,
                        color = if (idx == segmentPageIndex) GoaldayDesign.adaptiveInkPrimary else GoaldayDesign.adaptiveInkSecondary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
                            .background(if (idx == segmentPageIndex) GoaldayDesign.adaptiveBorderColor.copy(alpha = 0.30f) else GoaldayDesign.adaptiveSurface.copy(alpha = 0.09f))
                            .clickable { viewModel.setPage(index) }
                            .padding(horizontal = GoaldayDesign.Space3, vertical = GoaldayDesign.Space2 - 1.dp),
                    )
                }
            }
            Spacer(Modifier.height(GoaldayDesign.Space4))
        } else if (handbookMode) {
            // 原版书页阅读没有顶部月份条，翻页靠左右滑动/点击热区，保持页面沉浸
        }
        if (handbookMode && segment != BookSegment.MONTH) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
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
        } else if (segment == BookSegment.MONTH) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                MonthPageContent(
                    page = currentPage,
                    scheduleEntries = uiState.schedulePreviewEntries,
                    onSwitchToWeek = { switchSegment(BookSegment.WEEK) },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
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
                    handbookMode = handbookMode,
                    onFlipNext = { goToFilteredPage(segmentPageIndex + 1) },
                    onFlipPrevious = { goToFilteredPage(segmentPageIndex - 1) },
                )
            }
        }
        }
        val targetPage = currentPage as? TargetPage
        val targetItems = targetPage?.let { (it.items + uiState.customPageItems).distinct() }.orEmpty()
        val targetItem = openedTargetDetail?.takeIf { it in targetItems }
        if (targetPage != null && targetItem != null) {
            val isCustomTarget = targetItem in uiState.customPageItems
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
                onDelete = {
                    if (isCustomTarget) {
                        viewModel.removeCustomPageItem(targetItem)
                    }
                    openedTargetDetail = null
                },
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
                color = GoaldayDesign.adaptiveInkSecondary,
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
    onDelete: () -> Unit,
    onPinToTop: () -> Unit = {},
) {
    val dateShortcuts = remember { targetDateShortcuts() }
    val deadlineLabel = meta.deadlineDay?.let { "${it}日" } ?: "未设置"
    val scheduleLabel = if (scheduledEntries.isEmpty()) "未排期" else "${scheduledEntries.size}条"
    var noteDraft by remember(item, meta.note) { mutableStateOf(meta.note) }
    var actionHint by remember(item) { mutableStateOf("") }
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
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
                    listOf(GoaldayDesign.adaptiveSurface, GoaldayDesign.PaperWarm, GoaldayDesign.TargetDetailGradientEnd),
                ),
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(GoaldayDesign.adaptiveSurface.copy(alpha = 0.8f))
                .border(0.6.dp, GoaldayDesign.adaptiveBorderColor.copy(alpha = 0.09f), RoundedCornerShape(bottomStart = 22.dp, bottomEnd = 22.dp))
                .padding(horizontal = GoaldayDesign.Space4 - 2.dp, vertical = GoaldayDesign.Space3 - 1.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
                    .background(GoaldayDesign.adaptiveBorderColor.copy(alpha = 0.06f))
                    .clickable(onClick = onClose)
                    .padding(horizontal = GoaldayDesign.Space3 - 2.dp, vertical = GoaldayDesign.Space1 + 2.dp),
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = GoaldayDesign.adaptiveInkSecondary, modifier = Modifier.size(14.dp))
                Text(
                    "返回",
                    color = GoaldayDesign.adaptiveInkSecondary,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text("目标详情", color = GoaldayDesign.adaptiveInkSecondary, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                Text("本地目标详情", color = GoaldayDesign.adaptiveInkSecondary, style = MaterialTheme.typography.labelSmall)
            }
            Text(
                if (checked) "已完成" else "待办",
                color = if (checked) GoaldayDesign.Positive else GoaldayDesign.Pink,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(horizontal = GoaldayDesign.Space4 - 2.dp, vertical = GoaldayDesign.Space3),
            verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space3),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(GoaldayDesign.Radius2XL))
                    .background(
                        Brush.linearGradient(
                            listOf(GoaldayDesign.adaptiveSurface, GoaldayDesign.PinkTint, GoaldayDesign.TargetDetailCardEnd),
                            start = Offset.Zero,
                            end = Offset(900f, 420f),
                        ),
                    )
                    .border(GoaldayDesign.Hairline, GoaldayDesign.adaptiveBorderColor.copy(alpha = 0.14f), RoundedCornerShape(GoaldayDesign.Radius2XL))
                    .padding(GoaldayDesign.Space4),
                verticalArrangement = Arrangement.spacedBy(11.dp),
            ) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.weight(1f)) {
                        Text(pageTitle, color = GoaldayDesign.adaptiveInkMuted, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                        Text(item, color = GoaldayDesign.adaptiveInkPrimary, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                        Text("目标 ${itemIndex + 1}/$itemCount · $deadlineLabel · $scheduleLabel", color = GoaldayDesign.adaptiveInkSecondary, style = MaterialTheme.typography.bodySmall)
                    }
                    Box(
                        modifier = Modifier
                            .size(58.dp)
                            .clip(RoundedCornerShape(GoaldayDesign.RadiusXL))
                            .background(if (checked) GoaldayDesign.RouteTarget else GoaldayDesign.Pink),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (checked) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = "已完成",
                                tint = GoaldayDesign.adaptiveSurface,
                                modifier = Modifier.size(28.dp),
                            )
                        } else {
                            Text("%02d".format(itemIndex + 1), color = GoaldayDesign.adaptiveSurface, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        }
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
                horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space2),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DetailPill(if (checked) "切换完成" else "标记完成", active = checked, onClick = onToggleChecked)
                DetailPill("排入今天", active = false) { onAddToSchedule(dateShortcuts.today) }
                DetailPill("排入明天", active = false) { onAddToSchedule(dateShortcuts.tomorrow) }
                DetailPill("排入周末", active = false) { onAddToSchedule(dateShortcuts.weekend) }
                DetailPill("生成下一步", active = false) {
                    appendDetailNote("下一步：为「$item」安排一个 15 分钟行动")
                    actionHint = "已生成下一步"
                }
            }

            TargetDetailPanel(title = "目标选项", trailing = "本地保存") {
                Column(verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space2)) {
                    TargetOptionRow(
                        code = "我的",
                        title = "保存为我的目标",
                        subtitle = "加入当前目标页，后续可继续备注、排期和完成",
                        accent = GoaldayDesign.Positive,
                    ) {
                        onSaveAsOwnTarget()
                        appendDetailNote("我的目标：$item")
                        actionHint = "已保存为我的目标"
                    }
                    TargetOptionRow(
                        code = "日记",
                        title = "写入日记目标块",
                        subtitle = "生成 item_diary_target 风格记录，并同步到本地日程",
                        accent = GoaldayDesign.adaptiveInkSecondary,
                    ) {
                        onAddToDiary(checked)
                        appendDetailNote("日记：已把「$item」写入目标记录块")
                        actionHint = "已写入日记"
                    }
                    TargetOptionRow(
                        code = "复盘",
                        title = "加入复盘",
                        subtitle = "排入本周末，并写入复盘备注",
                        accent = GoaldayDesign.adaptiveInkSecondary,
                    ) {
                        appendDetailNote("复盘：本周检查「$item」推进情况")
                        onAddToSchedule(dateShortcuts.weekend)
                        actionHint = "已排入周末复盘"
                    }
                    TargetOptionRow(
                        code = "执行",
                        title = "生成执行句",
                        subtitle = "把今天要推进的行动写进备注",
                        accent = GoaldayDesign.Pink,
                    ) {
                        appendDetailNote("执行句：今天推进「$item」")
                        actionHint = "已写入执行句"
                    }
                }
                if (actionHint.isNotBlank()) {
                    Text(actionHint, color = GoaldayDesign.adaptiveInkSecondary, style = MaterialTheme.typography.labelSmall)
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
                Text(meta.deadlineDay?.let { "当前截止：${it}日" } ?: "当前未设置截止日", color = GoaldayDesign.adaptiveInkSecondary, style = MaterialTheme.typography.bodySmall)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space2),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    DetailPill("今天", active = meta.deadlineDay == dateShortcuts.today) { onUpdateDeadline(dateShortcuts.today) }
                    DetailPill("明天", active = meta.deadlineDay == dateShortcuts.tomorrow) { onUpdateDeadline(dateShortcuts.tomorrow) }
                    DetailPill("周末", active = meta.deadlineDay == dateShortcuts.weekend) { onUpdateDeadline(dateShortcuts.weekend) }
                    DetailPill("清除", active = false) { onUpdateDeadline(null) }
                }
            }

            TargetDetailPanel(title = "已排期", trailing = scheduleLabel) {
                if (scheduledEntries.isEmpty()) {
                    Text("还没有排入日程", color = GoaldayDesign.adaptiveInkMuted, style = MaterialTheme.typography.bodySmall)
                } else {
                    scheduledEntries.take(10).forEach { entry ->
                        TargetScheduledEntryRow(
                            entry = entry,
                            onPinToTop = onPinToTop,
                        )
                    }
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .background(Color.White),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "${LocalDate.now().monthValue}月${LocalDate.now().dayOfMonth}日",
                fontSize = 20.sp,
                color = Color.Black,
                modifier = Modifier
                    .background(Color(0xCFF6F6F6))
                    .padding(horizontal = 10.dp),
            )
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(22.dp)
                    .background(GoaldayDesign.ScheduleDateColumnSeparator),
            )
            Icon(
                imageVector = Icons.Default.ArrowUpward,
                contentDescription = "置顶",
                tint = GoaldayDesign.adaptiveInkPrimary,
                modifier = Modifier
                    .padding(13.dp)
                    .clickable { onPinToTop() },
            )
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "完成",
                tint = GoaldayDesign.adaptiveInkPrimary,
                modifier = Modifier
                    .padding(13.dp)
                    .clickable { onToggleChecked() },
            )
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "删除",
                tint = GoaldayDesign.adaptiveInkPrimary,
                modifier = Modifier
                    .padding(13.dp)
                    .padding(end = 13.dp)
                    .clickable { onDelete() },
            )
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
            .clip(RoundedCornerShape(GoaldayDesign.RadiusXL))
            .background(GoaldayDesign.adaptiveSurface.copy(alpha = 0.87f))
            .border(0.8.dp, GoaldayDesign.adaptiveBorderColor.copy(alpha = 0.09f), RoundedCornerShape(GoaldayDesign.RadiusXL))
            .padding(GoaldayDesign.Space3),
        verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space2),
    ) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(title, color = GoaldayDesign.adaptiveInkPrimary, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(trailing, color = GoaldayDesign.adaptiveInkMuted, style = MaterialTheme.typography.labelSmall, maxLines = 1)
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
            .clip(RoundedCornerShape(GoaldayDesign.RadiusXL))
            .background(GoaldayDesign.adaptiveSurface.copy(alpha = 0.8f))
            .border(0.8.dp, GoaldayDesign.adaptiveBorderColor.copy(alpha = 0.12f), RoundedCornerShape(GoaldayDesign.RadiusXL))
            .padding(horizontal = GoaldayDesign.Space3 - 2.dp, vertical = GoaldayDesign.Space2 + 1.dp),
        horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space2),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TargetDetailMetric(
            label = "状态",
            value = if (checked) "已完成" else "待办",
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
            .clip(RoundedCornerShape(GoaldayDesign.RadiusM))
            .background(if (active) GoaldayDesign.Positive.copy(alpha = 0.08f) else GoaldayDesign.PinkTint)
            .padding(horizontal = GoaldayDesign.Space2, vertical = GoaldayDesign.Space2 - 1.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(label, color = GoaldayDesign.adaptiveInkMuted, style = MaterialTheme.typography.labelSmall, maxLines = 1)
        Text(value, color = if (active) GoaldayDesign.Positive else GoaldayDesign.adaptiveInkSecondary, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}

@Composable
private fun TargetScheduledEntryRow(
    entry: ScheduleEntry,
    onPinToTop: () -> Unit = {},
) {
    val color = if (entry.completed) GoaldayDesign.RouteTarget else GoaldayDesign.Pink
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(GoaldayDesign.RadiusM))
            .background(if (entry.completed) GoaldayDesign.Positive.copy(alpha = 0.06f) else GoaldayDesign.PinkTint)
            .border(0.7.dp, color.copy(alpha = 0.2f), RoundedCornerShape(GoaldayDesign.RadiusM))
            .padding(horizontal = GoaldayDesign.Space3 - 2.dp, vertical = GoaldayDesign.Space2),
        horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space2),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "${entry.month}/${entry.day}",
            color = GoaldayDesign.adaptiveSurface,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .clip(RoundedCornerShape(GoaldayDesign.RadiusS))
                .background(color)
                .padding(horizontal = GoaldayDesign.Space2, vertical = GoaldayDesign.Space1),
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(entry.title, color = GoaldayDesign.adaptiveInkPrimary, style = MaterialTheme.typography.bodySmall, maxLines = 1)
            val repeatLabel = targetScheduleRepeatLabel(entry)
            Text(
                listOf(entry.note.ifBlank { "Goalday 本地日程" }, repeatLabel)
                    .filter(String::isNotBlank)
                    .joinToString(" · "),
                color = GoaldayDesign.adaptiveInkMuted,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
            )
        }
        Text(
            entry.timeText.ifBlank { if (entry.completed) "已完成" else "待办" },
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
        color = if (active) GoaldayDesign.adaptiveSurface else GoaldayDesign.adaptiveInkSecondary,
        style = MaterialTheme.typography.labelMedium,
        modifier = Modifier
            .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
            .background(if (active) GoaldayDesign.RouteTarget else GoaldayDesign.adaptiveDivider)
            .clickable(onClick = onClick)
            .padding(horizontal = GoaldayDesign.Space3 - 2.dp, vertical = GoaldayDesign.Space2 - 2.dp),
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
            .clip(RoundedCornerShape(GoaldayDesign.RadiusL))
            .background(accent.copy(alpha = 0.10f))
            .border(0.7.dp, accent.copy(alpha = 0.22f), RoundedCornerShape(GoaldayDesign.RadiusL))
            .clickable(onClick = onClick)
            .padding(horizontal = GoaldayDesign.Space3 - 2.dp, vertical = GoaldayDesign.Space2 + 1.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(58.dp)
                .height(34.dp)
                .clip(RoundedCornerShape(GoaldayDesign.RadiusS))
                .background(accent),
            contentAlignment = Alignment.Center,
        ) {
            Text(code, color = GoaldayDesign.adaptiveSurface, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, maxLines = 1)
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, color = GoaldayDesign.adaptiveInkPrimary, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, maxLines = 1)
            Text(subtitle, color = GoaldayDesign.adaptiveInkSecondary, style = MaterialTheme.typography.labelSmall, maxLines = 2)
        }
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
    }
}

private fun matchesSegment(page: BookPage, segment: BookSegment): Boolean =
    when (segment) {
        BookSegment.LIST -> page is PlanPage || page is TargetPage
        BookSegment.WEEK -> page is SchedulePage
        BookSegment.MONTH -> page is SchedulePage
        BookSegment.DIARY -> page is DiaryPage
    }

private fun nextSegment(segment: BookSegment): BookSegment =
    when (segment) {
        BookSegment.LIST -> BookSegment.WEEK
        BookSegment.WEEK -> BookSegment.MONTH
        BookSegment.MONTH -> BookSegment.DIARY
        BookSegment.DIARY -> BookSegment.LIST
    }

private fun previousSegment(segment: BookSegment): BookSegment =
    when (segment) {
        BookSegment.LIST -> BookSegment.DIARY
        BookSegment.WEEK -> BookSegment.LIST
        BookSegment.MONTH -> BookSegment.WEEK
        BookSegment.DIARY -> BookSegment.MONTH
    }

private fun resolveSegment(page: BookPage): BookSegment =
    when (page) {
        is DiaryPage -> BookSegment.DIARY
        is PlanPage, is TargetPage -> BookSegment.LIST
        is SchedulePage -> BookSegment.WEEK
    }

@Composable
private fun MonthPageContent(
    page: BookPage,
    scheduleEntries: List<ScheduleEntry>,
    onSwitchToWeek: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (page !is SchedulePage) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "月视图需要日程页",
                color = GoaldayDesign.adaptiveInkSecondary,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
        }
        return
    }
    val today = LocalDate.now()
    val pageMonth = page.title.extractMonthNumber()
    val baseMonth = pageMonth ?: scheduleEntries.firstOrNull()?.month ?: today.monthValue
    val baseYear = if (pageMonth != null) {
        today.year
    } else {
        scheduleEntries.firstOrNull { it.year == today.year && it.month == baseMonth }?.year
            ?: scheduleEntries.firstOrNull { it.month == baseMonth }?.year
            ?: scheduleEntries.firstOrNull()?.year
            ?: today.year
    }
    val yearMonth = YearMonth.of(baseYear, baseMonth)
    val year = yearMonth.year
    val month = yearMonth.monthValue
    val monthLength = yearMonth.lengthOfMonth()
    val entriesForMonth = scheduleEntries.filter { it.year == year && it.month == month }
    Column(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(GoaldayDesign.RadiusL))
            .background(GoaldayDesign.adaptivePaperGradient)
            .handbookPaperTexture(alpha = 0.10f)
            .padding(horizontal = GoaldayDesign.Space3, vertical = GoaldayDesign.Space2),
        verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space3),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    "$year GOALDAY",
                    style = MaterialTheme.typography.labelSmall,
                    color = GoaldayDesign.adaptiveInkMuted,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    "${month}月",
                    style = MaterialTheme.typography.titleMedium,
                    color = GoaldayDesign.adaptiveInkPrimary,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Text(
                page.title,
                style = MaterialTheme.typography.labelSmall,
                color = GoaldayDesign.adaptiveInkSecondary,
            )
        }
        HandbookMonthBoard(
            year = year,
            month = month,
            monthLength = monthLength,
            entries = entriesForMonth,
            selectedDays = emptyList(),
            onSelectDay = { onSwitchToWeek() },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        )
    }
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
    if (templates.isEmpty()) {
        EmbeddedInspirationUnavailable(onBack)
        return
    }
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
            .padding(top = GoaldayDesign.Space1),
        verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space3),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("灵感中心", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("返回", color = GoaldayDesign.adaptiveInkSecondary, modifier = Modifier.clickable(onClick = onBack))
                Text(
                    "完成",
                    color = GoaldayDesign.adaptiveSurface,
                    modifier = Modifier
                        .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
                        .background(GoaldayDesign.PrimaryAction)
                        .clickable {
                            val picked = editableItems.filterIndexed { index, _ ->
                                checkedStates.getOrNull(index) == true
                            }
                            onApply(picked, pushToToday, clearSourceAfterApply)
                        }
                        .padding(horizontal = GoaldayDesign.Space3 - 2.dp, vertical = GoaldayDesign.Space1),
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space2 + 2.dp),
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
                            .clip(RoundedCornerShape(GoaldayDesign.RadiusXL))
                            .background(topicCoverBrush(item, index))
                            .border(
                                width = if (index == selectedIndex) 2.dp else 1.dp,
                                color = if (index == selectedIndex) GoaldayDesign.CoverWhiteOverlayHigh else GoaldayDesign.CoverWhiteOverlayLow,
                                shape = RoundedCornerShape(GoaldayDesign.RadiusXL),
                            )
                            .clickable { onSelect(index) }
                            .padding(GoaldayDesign.Space3),
                    ) {
                        TopicCoverArt(template = item, index = index, compact = true)
                        Text(
                            "${previewCount}项",
                            color = GoaldayDesign.CoverWhiteOverlayHigh,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
                                .background(GoaldayDesign.CoverWhiteOverlayHairline)
                                .padding(horizontal = GoaldayDesign.Space2, vertical = GoaldayDesign.Space1),
                        )
                        Column(
                            modifier = Modifier.align(Alignment.BottomStart),
                            verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space1),
                        ) {
                            Text(item.title, color = GoaldayDesign.CoverWhiteOverlayHigh, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Text("${item.category} · ${item.subtitle}", color = GoaldayDesign.CoverWhiteOverlayMedium, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                        }
                    }
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(GoaldayDesign.Radius2XL))
                .background(GoaldayDesign.adaptiveSurface.copy(alpha = 0.98f))
                .padding(14.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space2),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(GoaldayDesign.RadiusXL))
                        .background(topicCoverBrush(selected, selectedIndex))
                        .border(GoaldayDesign.Hairline, GoaldayDesign.CoverWhiteOverlaySubtle, RoundedCornerShape(GoaldayDesign.RadiusXL))
                        .padding(GoaldayDesign.Space4),
                ) {
                    TopicCoverArt(template = selected, index = selectedIndex)
                    Text(
                        "${selected.category} · ${loadedTargetItems.size} 个目标",
                        color = GoaldayDesign.CoverWhiteOverlayHigh,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
                            .background(GoaldayDesign.CoverWhiteOverlayHairline)
                            .padding(horizontal = GoaldayDesign.Space2, vertical = GoaldayDesign.Space1),
                    )
                    Column(
                        modifier = Modifier.align(Alignment.BottomStart),
                        verticalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        Text(selected.title, style = MaterialTheme.typography.titleLarge, color = GoaldayDesign.CoverWhiteOverlayHigh, fontWeight = FontWeight.SemiBold)
                        Text(selected.subtitle, style = MaterialTheme.typography.bodySmall, color = GoaldayDesign.CoverWhiteOverlayHigh)
                        Text(if (selected.linkToSchedule) "可导入任务池 · 可保存成手账本" else "适合复盘记录 · 可保存成手账本", style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.CoverWhiteOverlayMedium)
                    }
                }
                Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text("目标详情", style = MaterialTheme.typography.titleMedium, color = GoaldayDesign.adaptiveInkPrimary, fontWeight = FontWeight.SemiBold)
                    Column(horizontalAlignment = Alignment.End) {
                        Text(catalogStatus.label, style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.adaptiveInkMuted)
                        Text(catalogStatus.assetLabel, style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.adaptiveInkMuted)
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(GoaldayDesign.RadiusL))
                        .background(GoaldayDesign.adaptiveBorderColor.copy(alpha = 0.06f))
                        .padding(horizontal = GoaldayDesign.Space3 - 2.dp, vertical = GoaldayDesign.Space2 - 1.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "本地资源",
                        style = MaterialTheme.typography.labelMedium,
                        color = GoaldayDesign.adaptiveInkSecondary,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Column(horizontalAlignment = Alignment.End) {
                        Text("${selected.coverKey}.png", style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.adaptiveInkMuted)
                        Text("${selected.targetKey}.txt · ${loadedTargetItems.size} 条", style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.adaptiveInkMuted)
                    }
                }
                editableItems.chunked(2).forEachIndexed { rowIndex, rowItems ->
                    Row(horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space2), modifier = Modifier.fillMaxWidth()) {
                        rowItems.forEachIndexed { columnIndex, item ->
                            val index = rowIndex * 2 + columnIndex
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(GoaldayDesign.RadiusM))
                                    .background(if (checkedStates.getOrNull(index) == true) GoaldayDesign.PinkTint else GoaldayDesign.adaptiveBorderColor.copy(alpha = 0.03f))
                                    .border(0.6.dp, if (checkedStates.getOrNull(index) == true) GoaldayDesign.Pink.copy(alpha = 0.21f) else GoaldayDesign.adaptiveDivider, RoundedCornerShape(GoaldayDesign.RadiusM))
                                    .padding(GoaldayDesign.Space2),
                                verticalArrangement = Arrangement.spacedBy(5.dp),
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(RoundedCornerShape(GoaldayDesign.RadiusS))
                                            .background(if (checkedStates.getOrNull(index) == true) GoaldayDesign.Positive else GoaldayDesign.adaptiveSurfaceSoft)
                                            .clickable {
                                                checkedStates = checkedStates.toMutableList().also { list ->
                                                    list[index] = !list[index]
                                                }
                                            },
                                    )
                                    Text("目标 ${index + 1}", color = GoaldayDesign.adaptiveInkSecondary, style = MaterialTheme.typography.labelSmall)
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier
                            .clickable {
                                editableItems = editableItems + "新目标"
                                checkedStates = checkedStates + true
                            }
                            .padding(vertical = GoaldayDesign.Space1),
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, tint = GoaldayDesign.Pink, modifier = Modifier.size(14.dp))
                        Text("添加目标", color = GoaldayDesign.Pink, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space2), verticalAlignment = Alignment.CenterVertically) {
                    ActionChip(
                        label = if (pushToToday) "导入任务池:开" else "导入任务池:关",
                        color = GoaldayDesign.adaptiveInkSecondary,
                        onClick = { pushToToday = !pushToToday },
                    )
                    ActionChip(
                        label = if (clearSourceAfterApply) "应用后移出来源:开" else "应用后移出来源:关",
                        color = GoaldayDesign.adaptiveInkSecondary,
                        onClick = { clearSourceAfterApply = !clearSourceAfterApply },
                    )
                }
                val picked = editableItems.filterIndexed { index, value ->
                    checkedStates.getOrNull(index) == true && value.isNotBlank()
                }
                Row(horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space2), modifier = Modifier.fillMaxWidth()) {
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
private fun EmbeddedInspirationUnavailable(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = GoaldayDesign.Space1),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(GoaldayDesign.Radius2XL))
                .background(GoaldayDesign.adaptiveSurface.copy(alpha = 0.94f))
                .border(0.8.dp, GoaldayDesign.adaptiveBorderColor.copy(alpha = 0.14f), RoundedCornerShape(GoaldayDesign.Radius2XL))
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            Text("暂无灵感模板", style = MaterialTheme.typography.titleMedium, color = GoaldayDesign.adaptiveInkPrimary, fontWeight = FontWeight.SemiBold)
            Text("本地模板资源为空，返回手账继续编辑已有页面。", style = MaterialTheme.typography.bodySmall, color = GoaldayDesign.adaptiveInkMuted, textAlign = TextAlign.Center)
            Text(
                "返回手账",
                color = GoaldayDesign.adaptiveSurface,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
                    .background(GoaldayDesign.PrimaryAction)
                    .clickable(onClick = onBack)
                    .padding(horizontal = GoaldayDesign.Space4, vertical = GoaldayDesign.Space2 + 1.dp),
            )
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
            Column(verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space3)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text(BookStrings.bookName) }, singleLine = true)
                OutlinedTextField(value = subtitle, onValueChange = { subtitle = it }, label = { Text(BookStrings.subtitle) }, singleLine = true)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    bookPalette.forEachIndexed { index, color ->
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
                                .background(color)
                                .clickable { colorIndex = index }
                                .shadow(if (colorIndex == index) 8.dp else 0.dp, RoundedCornerShape(GoaldayDesign.RadiusPill)),
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
    val isDanger = color == GoaldayDesign.Danger
    val textColor = if (isDanger) GoaldayDesign.DangerInk else GoaldayDesign.adaptiveInkPrimary
    val bgColor = if (isDanger) GoaldayDesign.DangerTint else color.copy(alpha = 0.10f)
    Text(
        text = label,
        color = textColor,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        maxLines = 1,
        modifier = Modifier
            .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
            .background(bgColor)
            .border(0.6.dp, color.copy(alpha = 0.18f), RoundedCornerShape(GoaldayDesign.RadiusPill))
            .clickable(onClick = onClick)
            .padding(horizontal = GoaldayDesign.Space3 - 1.dp, vertical = GoaldayDesign.Space1 + 2.dp),
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
            Column(verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space3)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text(BookStrings.bookName) }, singleLine = true)
                OutlinedTextField(value = subtitle, onValueChange = { subtitle = it }, label = { Text(BookStrings.subtitle) }, singleLine = true)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    bookPalette.forEachIndexed { index, color ->
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
                                .background(color)
                                .clickable { colorIndex = index }
                                .shadow(if (colorIndex == index) 8.dp else 0.dp, RoundedCornerShape(GoaldayDesign.RadiusPill)),
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
            Column(verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space3)) {
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
                            color = if (type == key) GoaldayDesign.adaptiveInkPrimary else GoaldayDesign.adaptiveInkSecondary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
                                .background(if (type == key) GoaldayDesign.adaptiveBorderColor.copy(alpha = 0.20f) else Color.Transparent)
                                .clickable {
                                    if (title.isBlank() || title in defaultPageTitles) {
                                        title = defaultPageTitle(key)
                                    }
                                    type = key
                                }
                                .padding(horizontal = GoaldayDesign.Space3 - 2.dp, vertical = GoaldayDesign.Space1 + 2.dp),
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



