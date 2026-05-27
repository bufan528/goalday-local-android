package com.bf410.goaldaylocal.ui.book

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.bf410.goaldaylocal.data.BookPage
import com.bf410.goaldaylocal.data.DiaryPage
import com.bf410.goaldaylocal.data.PlanPage
import com.bf410.goaldaylocal.data.SchedulePage
import com.bf410.goaldaylocal.data.TargetPage
import com.bf410.goaldaylocal.data.TopicBook
import java.time.LocalDate
import kotlin.math.absoluteValue

private val bookPalette = listOf(
    Color(0xFFF2C0A5),
    Color(0xFFF1A5B6),
    Color(0xFFFFAA5F),
    Color(0xFFBBD1AD),
    Color(0xFF9EAADB),
)

@Composable
fun BookHomeScreen(
    viewModel: BookViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()
    var dragRatio by remember(uiState.selectedBookIndex, uiState.selectedPageIndex) { mutableFloatStateOf(0f) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var showPageDialog by remember { mutableStateOf(false) }
    var showRenamePageDialog by remember { mutableStateOf(false) }
    var showEditBookDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.selectedBookIndex, uiState.selectedPageIndex) {
        dragRatio = 0f
    }

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
        BookDetailView(
            viewModel = viewModel,
            book = book,
            currentPage = currentPage,
            previousPage = previousPage,
            nextPage = nextPage,
            uiState = uiState,
            dragRatio = dragRatio,
            onDragRatioChange = { dragRatio = it },
            onBackToLibrary = viewModel::openLibrary,
            onShowAddPage = { showPageDialog = true },
            onShowRenamePage = { showRenamePageDialog = true },
            onShowEditBook = { showEditBookDialog = true },
        )
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
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Goalday Local",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 18.dp),
        )
        Text(
            text = "离线书库与手帐",
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
                text = "书库 ${books.size} 本 · 自建 $customBookCount 本",
                color = Color(0xFF6F675D),
            )
            Text(
                text = "新建一本书",
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
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(book.title, style = MaterialTheme.typography.titleLarge, color = Color(0xFF2F261D))
                        Text(book.subtitle, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF4B3D31))
                        Text("${book.pages.size} 页", style = MaterialTheme.typography.labelLarge, color = Color(0xFF5D4B3D))
                    }
                    Text("打开", color = Color(0xFF5D4B3D))
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
    dragRatio: Float,
    onDragRatioChange: (Float) -> Unit,
    onBackToLibrary: () -> Unit,
    onShowAddPage: () -> Unit,
    onShowRenamePage: () -> Unit,
    onShowEditBook: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Goalday Local",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 18.dp),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("返回书库", color = Color(0xFF8F684F), modifier = Modifier.clickable(onClick = onBackToLibrary))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (book.id.startsWith("custom_")) {
                    Text("改书", color = Color(0xFF8F684F), modifier = Modifier.clickable(onClick = onShowEditBook))
                    Text("新增页", color = Color(0xFF8F684F), modifier = Modifier.clickable(onClick = onShowAddPage))
                    Text("改页名", color = Color(0xFF8F684F), modifier = Modifier.clickable(onClick = onShowRenamePage))
                    Text("左移", color = Color(0xFF8F684F), modifier = Modifier.clickable(onClick = viewModel::moveCurrentPageLeft))
                    Text("右移", color = Color(0xFF8F684F), modifier = Modifier.clickable(onClick = viewModel::moveCurrentPageRight))
                    Text("删页", color = Color(0xFF9C5A52), modifier = Modifier.clickable(onClick = viewModel::deleteCurrentPage))
                    Text("删书", color = Color(0xFF9C5A52), modifier = Modifier.clickable(onClick = viewModel::removeCurrentCustomBook))
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            book.pages.forEachIndexed { index, page ->
                Text(
                    text = page.title,
                    color = if (index == uiState.selectedPageIndex) Color(0xFF2F261D) else Color(0xFF7A7065),
                    modifier = Modifier
                        .clip(RoundedCornerShape(99.dp))
                        .background(if (index == uiState.selectedPageIndex) Color(0x33B88A58) else Color(0x10FFFFFF))
                        .clickable { viewModel.setPage(index) }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        BookPageSurface(
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
            onToggleSaved = viewModel::toggleSavedCurrentBook,
            dragRatio = dragRatio,
            isChecked = { pageTitle, item -> viewModel.isChecked(pageTitle, item) },
            onToggleChecked = { pageTitle, item -> viewModel.toggleChecked(pageTitle, item) },
            onDiaryChange = viewModel::updateDiaryDraft,
            onAddCustomItem = viewModel::addCustomPageItem,
            onRemoveCustomItem = viewModel::removeCustomPageItem,
            onAddToSchedule = viewModel::addItemToSchedule,
            onDragRatioChange = onDragRatioChange,
            onFlipNext = { if (uiState.selectedPageIndex < book.pages.lastIndex) viewModel.setPage(uiState.selectedPageIndex + 1) },
            onFlipPrevious = { if (uiState.selectedPageIndex > 0) viewModel.setPage(uiState.selectedPageIndex - 1) },
        )
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
            Button(onClick = { if (title.isNotBlank()) onConfirm(title.trim(), subtitle.trim(), bookPalette[colorIndex]) }) {
                Text("创建")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } },
        title = { Text("新建一本书") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("书名") }, singleLine = true)
                OutlinedTextField(value = subtitle, onValueChange = { subtitle = it }, label = { Text("副标题") }, singleLine = true)
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
            Button(onClick = { if (title.isNotBlank()) onConfirm(title.trim(), subtitle.trim(), bookPalette[colorIndex]) }) {
                Text("保存")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } },
        title = { Text("编辑这本书") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("书名") }, singleLine = true)
                OutlinedTextField(value = subtitle, onValueChange = { subtitle = it }, label = { Text("副标题") }, singleLine = true)
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
                Text("创建页面")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } },
        title = { Text("新增页面") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("页面标题") }, singleLine = true)
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
        confirmButton = { Button(onClick = { if (title.isNotBlank()) onConfirm(title.trim()) }) { Text("保存") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } },
        title = { Text("修改页名") },
        text = { OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("页面标题") }, singleLine = true) },
    )
}

@Composable
private fun ColumnScope.BookPageSurface(
    bookId: String,
    bookTitle: String,
    subtitle: String,
    page: BookPage,
    previousPage: BookPage?,
    nextPage: BookPage?,
    pageIndex: Int,
    pageCount: Int,
    tint: Color,
    isSaved: Boolean,
    diaryDraft: String,
    customPageItems: List<String>,
    onToggleSaved: () -> Unit,
    dragRatio: Float,
    isChecked: (String, String) -> Boolean,
    onToggleChecked: (String, String) -> Unit,
    onDiaryChange: (String) -> Unit,
    onAddCustomItem: (String) -> Unit,
    onRemoveCustomItem: (String) -> Unit,
    onAddToSchedule: (String, Int) -> Unit,
    onDragRatioChange: (Float) -> Unit,
    onFlipNext: () -> Unit,
    onFlipPrevious: () -> Unit,
) {
    val absoluteDrag = dragRatio.absoluteValue.coerceIn(0f, 1f)
    val rightDrag = (-dragRatio).coerceIn(0f, 1f)
    val leftDrag = dragRatio.coerceIn(0f, 1f)
    val rightPageRotation = rightDrag * -94f
    val leftPageRotation = leftDrag * 94f
    val rightShadowWidth = (rightDrag * 62).dp
    val leftShadowWidth = (leftDrag * 62).dp
    val animatedRightRotation by animateFloatAsState(rightPageRotation, tween(160))
    val animatedLeftRotation by animateFloatAsState(leftPageRotation, tween(160))
    var diaryCommand by remember(pageIndex) { mutableStateOf<RichEditorCommand?>(null) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .pointerInput(pageIndex, bookId) {
                detectHorizontalDragGestures(
                    onHorizontalDrag = { _, dragAmount ->
                        val next = (dragRatio + dragAmount / 720f).coerceIn(-1f, 1f)
                        onDragRatioChange(next)
                    },
                    onDragEnd = {
                        when {
                            dragRatio <= -0.22f && nextPage != null -> onFlipNext()
                            dragRatio >= 0.22f && previousPage != null -> onFlipPrevious()
                            else -> onDragRatioChange(0f)
                        }
                    },
                )
            },
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp)
                .shadow(28.dp, RoundedCornerShape(30.dp), clip = false)
                .clip(RoundedCornerShape(30.dp))
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFFF0E4D5), Color(0xFFF8F1E8), Color(0xFFE8D7C2)),
                        start = Offset.Zero,
                        end = Offset(1000f, 1400f),
                    ),
                ),
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                StaticPage(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f),
                    title = previousPage?.title ?: "封面",
                    subtitle = if (previousPage == null) "向右轻扫可返回上一页" else "上一页预览",
                    bookTitle = bookTitle,
                    pageContent = previousPage?.let(::pagePreviewText) ?: subtitle,
                    tint = tint,
                    pageNumber = if (previousPage == null) "" else "$pageIndex / $pageCount",
                    alpha = (1f - rightDrag * 0.55f).coerceIn(0.42f, 1f),
                )

                Box(
                    modifier = Modifier
                        .width(10.dp)
                        .fillMaxHeight()
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFFBCA891), Color(0xFFE8D9C6), Color(0xFFBCA891)),
                            ),
                        ),
                )

                Box(modifier = Modifier.fillMaxHeight().weight(1f)) {
                    if (leftDrag > 0.01f && previousPage != null) {
                        FlippingPreviewPage(
                            modifier = Modifier
                                .fillMaxSize()
                                .align(Alignment.CenterStart),
                            rotationY = animatedLeftRotation,
                            transformOrigin = TransformOrigin(1f, 0.5f),
                            tint = tint,
                            title = previousPage.title,
                            subtitle = "上一页",
                            content = pagePreviewText(previousPage),
                            pageNumber = "$pageIndex / $pageCount",
                            reverseGradient = true,
                        )
                    }

                    FlippingCurrentPage(
                        page = page,
                        pageIndex = pageIndex,
                        pageCount = pageCount,
                        bookTitle = bookTitle,
                        subtitle = subtitle,
                        tint = tint,
                        isSaved = isSaved,
                        diaryDraft = diaryDraft,
                        customPageItems = customPageItems,
                        onToggleSaved = onToggleSaved,
                        isChecked = isChecked,
                        onToggleChecked = onToggleChecked,
                        onDiaryChange = onDiaryChange,
                        onAddCustomItem = onAddCustomItem,
                        onRemoveCustomItem = onRemoveCustomItem,
                        onAddToSchedule = onAddToSchedule,
                        pendingCommand = diaryCommand,
                        onCommand = { diaryCommand = it },
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                transformOrigin = TransformOrigin(0f, 0.5f)
                                rotationY = animatedRightRotation
                                cameraDistance = 34f * density
                                shadowElevation = 28f
                            },
                    )
                }
            }

            if (rightDrag > 0.01f) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .fillMaxHeight()
                        .width(rightShadowWidth)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color.Transparent,
                                    Color(0x22000000),
                                    Color.Black.copy(alpha = (0.22f + rightDrag * 0.24f).coerceAtMost(0.48f)),
                                ),
                            ),
                        ),
                )
            }

            if (leftDrag > 0.01f) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .fillMaxHeight()
                        .width(leftShadowWidth)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color.Black.copy(alpha = (0.22f + leftDrag * 0.24f).coerceAtMost(0.48f)),
                                    Color(0x22000000),
                                    Color.Transparent,
                                ),
                            ),
                        ),
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .width(48.dp)
                    .fillMaxHeight()
                    .clickable(enabled = previousPage != null) {
                        if (previousPage != null) {
                            onDragRatioChange(0.25f)
                            onFlipPrevious()
                        }
                    },
            )

            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .width(48.dp)
                    .fillMaxHeight()
                    .clickable(enabled = nextPage != null) {
                        if (nextPage != null) {
                            onDragRatioChange(-0.25f)
                            onFlipNext()
                        }
                    },
            )
        }
    }
}

@Composable
private fun StaticPage(
    modifier: Modifier,
    title: String,
    subtitle: String,
    bookTitle: String,
    pageContent: String,
    tint: Color,
    pageNumber: String,
    alpha: Float,
) {
    Box(
        modifier = modifier
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFFF6EBDC), Color(0xFFFCF8F2), Color(0xFFF2E3CF)),
                ),
            )
            .padding(24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { this.alpha = alpha },
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(99.dp)).background(tint.copy(alpha = 0.85f)))
                Spacer(Modifier.width(8.dp))
                Text(bookTitle, style = MaterialTheme.typography.titleMedium, color = Color(0xFF2D261F))
            }
            Spacer(Modifier.height(18.dp))
            Text(title, style = MaterialTheme.typography.titleLarge, color = Color(0xFF2D261F))
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color(0xFF8A7B6E), modifier = Modifier.padding(top = 4.dp))
            Spacer(Modifier.height(20.dp))
            Text(pageContent, style = MaterialTheme.typography.bodyLarge, color = Color(0xFF6C635A))
            Spacer(Modifier.weight(1f))
            if (pageNumber.isNotBlank()) {
                Text(pageNumber, style = MaterialTheme.typography.labelLarge, color = Color(0xFF8A7B6E))
            }
        }
    }
}

@Composable
private fun FlippingPreviewPage(
    modifier: Modifier,
    rotationY: Float,
    transformOrigin: TransformOrigin,
    tint: Color,
    title: String,
    subtitle: String,
    content: String,
    pageNumber: String,
    reverseGradient: Boolean,
) {
    Box(
        modifier = modifier
            .graphicsLayer {
                this.transformOrigin = transformOrigin
                this.rotationY = rotationY
                cameraDistance = 34f * density
                shadowElevation = 22f
            }
            .clip(RoundedCornerShape(topStart = 30.dp, bottomStart = 30.dp, topEnd = 8.dp, bottomEnd = 8.dp))
            .background(
                Brush.horizontalGradient(
                    if (reverseGradient) {
                        listOf(Color(0xFFE7D6C1), Color(0xFFF8F1E7), Color(0xFFFFFEFB))
                    } else {
                        listOf(Color(0xFFFFFEFB), Color(0xFFF8F1E7), Color(0xFFE7D6C1))
                    },
                ),
            )
            .padding(24.dp),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(99.dp)).background(tint.copy(alpha = 0.82f)))
                Spacer(Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, color = Color(0xFF2D261F))
            }
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color(0xFF8A7B6E), modifier = Modifier.padding(top = 4.dp))
            Spacer(Modifier.height(18.dp))
            Text(content, style = MaterialTheme.typography.bodyLarge, color = Color(0xFF5E554D))
            Spacer(Modifier.weight(1f))
            Text(pageNumber, style = MaterialTheme.typography.labelLarge, color = Color(0xFF8A7B6E))
        }
    }
}

@Composable
private fun FlippingCurrentPage(
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
    onAddToSchedule: (String, Int) -> Unit,
    pendingCommand: RichEditorCommand?,
    onCommand: (RichEditorCommand) -> Unit,
    modifier: Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(topEnd = 30.dp, bottomEnd = 30.dp, topStart = 8.dp, bottomStart = 8.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFFFFFEFB), Color(0xFFF9F2E9), Color(0xFFEBDAC4)),
                ),
            )
            .padding(26.dp),
    ) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(99.dp)).background(tint))
                Spacer(Modifier.width(8.dp))
                Text(bookTitle, style = MaterialTheme.typography.titleLarge)
            }
            Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF6C635A), modifier = Modifier.padding(top = 4.dp))
            TextButton(onClick = onToggleSaved, modifier = Modifier.padding(top = 2.dp)) {
                Text(if (isSaved) "已保存到我的书" else "保存为我的书")
            }
            Spacer(Modifier.height(10.dp))
            Text(text = page.title, style = MaterialTheme.typography.headlineMedium, color = Color(0xFF2D261F))
            Spacer(Modifier.height(18.dp))
            when (page) {
                is TargetPage -> EditableBulletPage(page.title, page.items, customPageItems, tint, "添加你的目标", isChecked, onToggleChecked, onAddCustomItem, onRemoveCustomItem, onAddToSchedule)
                is SchedulePage -> EditableBulletPage(page.title, page.items, customPageItems, tint.copy(alpha = 0.74f), "添加你的日程任务", isChecked, onToggleChecked, onAddCustomItem, onRemoveCustomItem, onAddToSchedule)
                is PlanPage -> EditableBulletPage(page.title, page.items, customPageItems, Color(0xFFB88A58), "添加你的计划", isChecked, onToggleChecked, onAddCustomItem, onRemoveCustomItem, onAddToSchedule)
                is DiaryPage -> DiarySection(
                    prompt = page.prompt,
                    tint = tint,
                    diaryDraft = diaryDraft,
                    pendingCommand = pendingCommand,
                    onCommand = onCommand,
                    onDiaryChange = onDiaryChange,
                )
            }
            Spacer(Modifier.height(28.dp))
            Text(text = "${pageIndex + 1} / $pageCount", style = MaterialTheme.typography.labelLarge, color = Color(0xFF7A7065))
        }
    }
}

private fun pagePreviewText(page: BookPage): String =
    when (page) {
        is TargetPage -> page.items.take(3).joinToString("\n") { "• $it" }.ifBlank { "这一页还没有内容" }
        is PlanPage -> page.items.take(3).joinToString("\n") { "• $it" }.ifBlank { "这一页还没有内容" }
        is SchedulePage -> page.items.take(3).joinToString("\n") { "• $it" }.ifBlank { "这一页还没有内容" }
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
    onAddToSchedule: (String, Int) -> Unit,
) {
    var newItem by remember(pageTitle) { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        baseItems.forEach { item ->
            ActionBulletRow(pageTitle, item, tint, isChecked(pageTitle, item), false, onToggleChecked, onAddToSchedule, onRemoveCustomItem)
        }
        if (customItems.isNotEmpty()) {
            Text("我的内容", style = MaterialTheme.typography.titleMedium, color = Color(0xFF5E4837))
            customItems.forEach { item ->
                ActionBulletRow(pageTitle, item, tint, isChecked(pageTitle, item), true, onToggleChecked, onAddToSchedule, onRemoveCustomItem)
            }
        }
        OutlinedTextField(
            value = newItem,
            onValueChange = { newItem = it },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            label = { Text(inputLabel) },
            singleLine = true,
        )
        TextButton(onClick = {
            onAddCustomItem(newItem)
            newItem = ""
        }) {
            Text("保存")
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
) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier
                .clickable { onToggleChecked(pageTitle, item) }
                .fillMaxWidth(0.66f),
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
            text = "加入日历",
            style = MaterialTheme.typography.labelLarge,
            color = Color(0xFF8F684F),
            modifier = Modifier
                .clip(RoundedCornerShape(99.dp))
                .background(Color(0x1A8F684F))
                .clickable { onAddToSchedule(item, LocalDate.now().dayOfMonth) }
                .padding(horizontal = 10.dp, vertical = 8.dp),
        )
        if (removable) {
            Text(
                text = "删除",
                style = MaterialTheme.typography.labelLarge,
                color = Color(0xFF9C5A52),
                modifier = Modifier
                    .clickable { onRemoveCustomItem(item) }
                    .padding(top = 8.dp),
            )
        }
    }
}

@Composable
private fun completedTextStyle(completed: Boolean): TextStyle =
    MaterialTheme.typography.bodyLarge.copy(
        textDecoration = if (completed) TextDecoration.LineThrough else TextDecoration.None,
    )

@Composable
private fun DiarySection(
    prompt: String,
    tint: Color,
    diaryDraft: String,
    pendingCommand: RichEditorCommand?,
    onCommand: (RichEditorCommand) -> Unit,
    onDiaryChange: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(text = prompt, style = MaterialTheme.typography.titleMedium, color = Color(0xFF342C24))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            DiaryToolChip("加粗") { onCommand(RichEditorCommand("bold")) }
            DiaryToolChip("标题") { onCommand(RichEditorCommand("formatBlock", "<h2>")) }
            DiaryToolChip("列表") { onCommand(RichEditorCommand("insertUnorderedList")) }
            DiaryToolChip("引用") { onCommand(RichEditorCommand("formatBlock", "<blockquote>")) }
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
                placeholder = "写下今天的记录、感受或下一步。",
                modifier = Modifier.fillMaxSize(),
                pendingCommand = pendingCommand,
                onHtmlChange = onDiaryChange,
            )
        }
        Text(
            text = "纸页内容会保存在本地，不依赖服务器。",
            style = MaterialTheme.typography.bodySmall,
            color = tint.copy(alpha = 0.72f),
        )
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
