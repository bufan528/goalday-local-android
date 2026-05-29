package com.bf410.goaldaylocal.ui.book

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.bf410.goaldaylocal.data.BookPage
import com.bf410.goaldaylocal.data.DiaryPage
import com.bf410.goaldaylocal.data.LocalStateStore
import com.bf410.goaldaylocal.data.PlanPage
import com.bf410.goaldaylocal.data.SampleLibrary
import com.bf410.goaldaylocal.data.SchedulePage
import com.bf410.goaldaylocal.data.TargetPage
import com.bf410.goaldaylocal.data.TopicBook
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.YearMonth

class BookViewModel(
    private val store: LocalStateStore,
) : ViewModel() {
    private fun allBooks(): List<TopicBook> = SampleLibrary.books + store.customBooks()

    private val _uiState = MutableStateFlow(
        BookUiState(
            books = allBooks(),
            selectedBookIndex = store.selectedBookIndex().coerceIn(0, (allBooks().lastIndex).coerceAtLeast(0)),
            selectedPageIndex = 0,
            savedBookIds = store.savedBookIds(),
            diaryDraft = "",
            customPageItems = emptyList(),
            weeklyTheme = "",
            todayPlanItems = emptyList(),
            todayCompletedItems = emptyList(),
            customBookCount = store.customBooks().size,
            inLibraryMode = true,
        ),
    )
    val uiState: StateFlow<BookUiState> = _uiState

    init {
        syncPageFromStore()
        syncEditableContent()
    }

    fun openLibrary() {
        _uiState.update { it.copy(inLibraryMode = true) }
    }

    fun openBook(index: Int) {
        selectBook(index)
        _uiState.update { it.copy(inLibraryMode = false) }
    }

    fun selectBook(index: Int) {
        val books = _uiState.value.books
        val clamped = index.coerceIn(0, books.lastIndex)
        store.setSelectedBookIndex(clamped)
        _uiState.update {
            it.copy(
                selectedBookIndex = clamped,
                selectedPageIndex = store.selectedPageIndex(books[clamped].id).coerceIn(0, books[clamped].pages.lastIndex),
            )
        }
        syncEditableContent()
    }

    fun setPage(index: Int) {
        val book = currentBook()
        val clamped = index.coerceIn(0, book.pages.lastIndex)
        store.setSelectedPageIndex(book.id, clamped)
        _uiState.update { it.copy(selectedPageIndex = clamped) }
        syncEditableContent()
    }

    fun isChecked(pageTitle: String, item: String): Boolean =
        store.isChecked(currentBook().id, pageTitle, item)

    fun toggleChecked(pageTitle: String, item: String) {
        val next = !isChecked(pageTitle, item)
        store.setChecked(currentBook().id, pageTitle, item, next)
        _uiState.update { it.copy() }
    }

    fun toggleSavedCurrentBook() {
        val book = currentBook()
        if (book.id in _uiState.value.savedBookIds) {
            store.removeSavedBook(book.id)
        } else {
            store.saveBook(book.id)
        }
        _uiState.update { it.copy(savedBookIds = store.savedBookIds()) }
    }

    fun updateDiaryDraft(text: String) {
        val page = currentPage() as? DiaryPage ?: return
        store.setDiaryText(currentBook().id, page.title, text)
        _uiState.update { it.copy(diaryDraft = text) }
    }

    fun addCustomPageItem(text: String) {
        if (!supportsCustomItems()) return
        val trimmed = text.trim()
        if (trimmed.isBlank()) return
        val updated = (_uiState.value.customPageItems + trimmed).distinct()
        store.saveCustomPageItems(currentBook().id, currentPage().title, updated)
        _uiState.update { it.copy(customPageItems = updated) }
    }

    fun removeCustomPageItem(item: String) {
        if (!supportsCustomItems()) return
        val updated = _uiState.value.customPageItems.filterNot { it == item }
        store.saveCustomPageItems(currentBook().id, currentPage().title, updated)
        _uiState.update { it.copy(customPageItems = updated) }
    }

    fun renameCustomPageItem(oldItem: String, newItem: String) {
        if (!supportsCustomItems()) return
        val trimmed = newItem.trim()
        if (trimmed.isBlank()) return
        val updated = _uiState.value.customPageItems.map { item ->
            if (item == oldItem) trimmed else item
        }.distinct()
        store.saveCustomPageItems(currentBook().id, currentPage().title, updated)
        _uiState.update { it.copy(customPageItems = updated) }
    }

    fun addItemToSchedule(item: String, day: Int) {
        val title = item.trim()
        if (title.isBlank()) return
        val year = store.calendarAnchorYear()
        val month = store.calendarAnchorMonth().coerceIn(1, 12)
        val maxDay = YearMonth.of(year, month).lengthOfMonth()
        val safeDay = day.coerceIn(1, maxDay)
        val bookTitle = currentBook().title
        val duplicated = store.scheduleEntries().any { entry ->
            entry.title == title &&
                entry.year == year &&
                entry.month == month &&
                entry.day == safeDay &&
                entry.note == bookTitle
        }
        if (duplicated) return
        store.addScheduleEntry(
            title = title,
            year = year,
            month = month,
            day = safeDay,
            note = bookTitle,
        )
    }

    fun updateWeeklyTheme(text: String) {
        val book = currentBook()
        store.setWeeklyTheme(book.id, text)
        _uiState.update { it.copy(weeklyTheme = text) }
    }

    fun moveItemToToday(item: String) {
        val page = currentPage()
        val book = currentBook()
        val nextPlan = (_uiState.value.todayPlanItems + item).distinct()
        val nextDone = _uiState.value.todayCompletedItems.filterNot { it == item }
        store.saveTodayPlanItems(book.id, page.title, nextPlan)
        store.saveTodayCompletedItems(book.id, page.title, nextDone)
        _uiState.update { it.copy(todayPlanItems = nextPlan, todayCompletedItems = nextDone) }
    }

    fun moveItemToCompleted(item: String) {
        val page = currentPage()
        val book = currentBook()
        val alreadyDone = item in _uiState.value.todayCompletedItems
        val nextDone = (_uiState.value.todayCompletedItems + item).distinct()
        val nextPlan = _uiState.value.todayPlanItems.filterNot { it == item }
        store.saveTodayCompletedItems(book.id, page.title, nextDone)
        store.saveTodayPlanItems(book.id, page.title, nextPlan)
        _uiState.update { it.copy(todayCompletedItems = nextDone, todayPlanItems = nextPlan) }
        if (!alreadyDone) {
            addItemToSchedule(item, LocalDate.now().dayOfMonth)
        }
    }

    fun restoreItemFromToday(item: String) {
        val page = currentPage()
        val book = currentBook()
        val updated = _uiState.value.todayPlanItems.filterNot { it == item }
        store.saveTodayPlanItems(book.id, page.title, updated)
        _uiState.update { it.copy(todayPlanItems = updated) }
    }

    fun restoreItemFromCompleted(item: String) {
        val page = currentPage()
        val book = currentBook()
        val updated = _uiState.value.todayCompletedItems.filterNot { it == item }
        store.saveTodayCompletedItems(book.id, page.title, updated)
        _uiState.update { it.copy(todayCompletedItems = updated) }
    }

    fun createCustomBook(title: String, subtitle: String, color: Color) {
        val newBook = store.addCustomBook(title.trim(), subtitle.trim(), color)
        refreshBooks(selectBookId = newBook.id, openBook = true)
    }

    fun updateCurrentBookInfo(title: String, subtitle: String, color: Color) {
        val current = currentBook()
        if (!isCurrentBookCustom()) return
        store.updateCustomBook(current.copy(title = title.trim(), subtitle = subtitle.trim(), color = color))
        refreshBooks(selectBookId = current.id, openBook = true)
    }

    fun removeCurrentCustomBook() {
        val currentBook = currentBook()
        if (!currentBook.id.startsWith("custom_")) return
        store.removeCustomBook(currentBook.id)
        refreshBooks(selectBookId = SampleLibrary.books.first().id, openBook = false)
    }

    fun renameCurrentPage(title: String) {
        val trimmed = title.trim()
        if (trimmed.isBlank() || !isCurrentBookCustom()) return
        updateCurrentBookPages { pages ->
            pages.toMutableList().also { list ->
                list[_uiState.value.selectedPageIndex] = renamePage(list[_uiState.value.selectedPageIndex], trimmed)
            }
        }
    }

    fun moveCurrentPageLeft() {
        val currentIndex = _uiState.value.selectedPageIndex
        if (!isCurrentBookCustom() || currentIndex == 0) return
        updateCurrentBookPages { pages ->
            pages.toMutableList().also { list ->
                val page = list.removeAt(currentIndex)
                list.add(currentIndex - 1, page)
            }
        }
        setPage(currentIndex - 1)
    }

    fun moveCurrentPageRight() {
        val currentIndex = _uiState.value.selectedPageIndex
        if (!isCurrentBookCustom() || currentIndex >= currentBook().pages.lastIndex) return
        updateCurrentBookPages { pages ->
            pages.toMutableList().also { list ->
                val page = list.removeAt(currentIndex)
                list.add(currentIndex + 1, page)
            }
        }
        setPage(currentIndex + 1)
    }

    fun addPage(type: String, title: String) {
        val trimmed = title.trim()
        if (trimmed.isBlank() || !isCurrentBookCustom()) return
        val newPage = when (type) {
            "target" -> TargetPage(trimmed, emptyList())
            "plan" -> PlanPage(trimmed, emptyList())
            "schedule" -> SchedulePage(trimmed, emptyList())
            else -> DiaryPage(trimmed, "写下这一页最重要的记录。")
        }
        updateCurrentBookPages { it + newPage }
        setPage(currentBook().pages.lastIndex)
    }

    fun deleteCurrentPage() {
        if (!isCurrentBookCustom() || currentBook().pages.size <= 1) return
        val removedIndex = _uiState.value.selectedPageIndex
        updateCurrentBookPages { pages ->
            pages.filterIndexed { index, _ -> index != removedIndex }
        }
        val nextIndex = removedIndex.coerceAtMost(currentBook().pages.lastIndex)
        setPage(nextIndex)
    }

    private fun currentBook() = _uiState.value.books[_uiState.value.selectedBookIndex]

    private fun currentPage() = currentBook().pages[_uiState.value.selectedPageIndex]

    private fun supportsCustomItems(): Boolean =
        when (currentPage()) {
            is PlanPage, is TargetPage, is SchedulePage -> true
            else -> false
        }

    private fun isCurrentBookCustom(): Boolean = currentBook().id.startsWith("custom_")

    private fun syncPageFromStore() {
        val book = currentBook()
        _uiState.update {
            it.copy(
                selectedPageIndex = store.selectedPageIndex(book.id).coerceIn(0, book.pages.lastIndex),
            )
        }
    }

    private fun syncEditableContent() {
        val book = currentBook()
        when (val page = currentPage()) {
            is DiaryPage -> {
                _uiState.update {
                    it.copy(
                        diaryDraft = store.diaryText(book.id, page.title),
                        customPageItems = emptyList(),
                        weeklyTheme = store.weeklyTheme(book.id),
                        todayPlanItems = emptyList(),
                        todayCompletedItems = emptyList(),
                    )
                }
            }
            is PlanPage, is TargetPage, is SchedulePage -> {
                _uiState.update {
                    it.copy(
                        diaryDraft = "",
                        customPageItems = store.customPageItems(book.id, page.title),
                        weeklyTheme = store.weeklyTheme(book.id),
                        todayPlanItems = store.todayPlanItems(book.id, page.title),
                        todayCompletedItems = store.todayCompletedItems(book.id, page.title),
                    )
                }
            }
        }
    }

    private fun updateCurrentBookPages(transform: (List<BookPage>) -> List<BookPage>) {
        val current = currentBook()
        if (!current.id.startsWith("custom_")) return
        store.updateCustomBook(current.copy(pages = transform(current.pages)))
        refreshBooks(selectBookId = current.id, openBook = true)
    }

    private fun refreshBooks(selectBookId: String, openBook: Boolean) {
        val updatedBooks = allBooks()
        val selectedIndex = updatedBooks.indexOfFirst { it.id == selectBookId }.coerceAtLeast(0)
        store.setSelectedBookIndex(selectedIndex)
        _uiState.update {
            it.copy(
                books = updatedBooks,
                selectedBookIndex = selectedIndex,
                selectedPageIndex = store.selectedPageIndex(updatedBooks[selectedIndex].id).coerceIn(0, updatedBooks[selectedIndex].pages.lastIndex),
                customBookCount = store.customBooks().size,
                inLibraryMode = !openBook,
            )
        }
        if (openBook) {
            _uiState.update { it.copy(inLibraryMode = false) }
        }
        syncEditableContent()
    }

    private fun renamePage(page: BookPage, title: String): BookPage =
        when (page) {
            is TargetPage -> page.copy(title = title)
            is PlanPage -> page.copy(title = title)
            is SchedulePage -> page.copy(title = title)
            is DiaryPage -> page.copy(title = title)
        }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val store = LocalStateStore(MMKV.defaultMMKV())
                @Suppress("UNCHECKED_CAST")
                return BookViewModel(store) as T
            }
        }
    }
}
