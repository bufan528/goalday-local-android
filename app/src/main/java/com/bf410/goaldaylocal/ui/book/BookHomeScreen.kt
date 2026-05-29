package com.bf410.goaldaylocal.ui.book

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bf410.goaldaylocal.data.BookPage
import com.bf410.goaldaylocal.data.TopicBook

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
    var showCreateDialog by remember { mutableStateOf(false) }
    var showPageDialog by remember { mutableStateOf(false) }
    var showRenamePageDialog by remember { mutableStateOf(false) }
    var showEditBookDialog by remember { mutableStateOf(false) }

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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(BookStrings.backToLibrary, color = Color(0xFF8F684F), modifier = Modifier.clickable(onClick = onBackToLibrary))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState()),
            ) {
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
                .horizontalScroll(rememberScrollState()),
        ) {
            book.pages.forEachIndexed { index, item ->
                Text(
                    text = item.title,
                    color = if (index == uiState.selectedPageIndex) Color(0xFF2F261D) else Color(0xFF7A7065),
                    modifier = Modifier
                        .clip(RoundedCornerShape(99.dp))
                        .background(if (index == uiState.selectedPageIndex) Color(0x4DB88A58) else Color(0x18FFFFFF))
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
    Text(
        text = label,
        color = color,
        modifier = Modifier
            .clip(RoundedCornerShape(99.dp))
            .background(color.copy(alpha = 0.10f))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
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
