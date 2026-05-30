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
import com.bf410.goaldaylocal.data.ScheduleEntry
import com.bf410.goaldaylocal.data.SchedulePage
import com.bf410.goaldaylocal.data.TargetPage
import com.bf410.goaldaylocal.data.TopicBook
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID

class BookViewModel(
    private val store: LocalStateStore,
) : ViewModel() {
    private data class ScheduleImport(
        val todo: List<String>,
        val done: List<String>,
    )

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
            schedulePreviewEntries = monthEntriesForAnchor(),
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

    fun addCustomPageItemWithDeadline(text: String, day: Int?) {
        addCustomPageItem(text)
        val resolvedDay = day ?: return
        addItemToSchedule(text, resolvedDay)
    }

    fun applyInspirationTemplate(
        items: List<String>,
        pushToToday: Boolean = false,
        clearSourceAfterApply: Boolean = false,
    ) {
        if (!supportsCustomItems()) return
        val normalized = items.map(String::trim).filter(String::isNotBlank).distinct()
        val merged = (_uiState.value.customPageItems + normalized).distinct()
        store.saveCustomPageItems(currentBook().id, currentPage().title, merged)
        var nextToday = _uiState.value.todayPlanItems
        var nextCustom = merged
        if (pushToToday) {
            nextToday = (nextToday + normalized).distinct()
            store.saveTodayPlanItems(currentBook().id, currentPage().title, nextToday)
        }
        if (clearSourceAfterApply) {
            nextCustom = nextCustom.filterNot { it in normalized }
            store.saveCustomPageItems(currentBook().id, currentPage().title, nextCustom)
        }
        _uiState.update { it.copy(customPageItems = nextCustom, todayPlanItems = nextToday) }
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
        _uiState.update { it.copy(schedulePreviewEntries = monthEntriesForAnchor()) }
    }

    fun updateWeeklyTheme(text: String) {
        val book = currentBook()
        store.setWeeklyTheme(book.id, text)
        _uiState.update { it.copy(weeklyTheme = text) }
    }

    fun moveItemToToday(item: String) {
        upsertTodayScheduleEntry(item, completed = false)
        syncEditableContent()
    }

    fun moveItemToCompleted(item: String) {
        val wasDone = _uiState.value.todayCompletedItems.contains(item)
        upsertTodayScheduleEntry(item, completed = true)
        if (!wasDone) {
            syncCompletedItemToDiary(currentBook().id, item)
        }
        syncEditableContent()
    }

    fun restoreItemFromToday(item: String) {
        removeTodayScheduleEntry(item)
        syncEditableContent()
    }

    fun restoreItemFromCompleted(item: String) {
        upsertTodayScheduleEntry(item, completed = false)
        removeCompletedItemFromDiary(currentBook().id, item)
        syncEditableContent()
    }

    fun addQuickTodo(item: String) {
        val normalized = item.trim()
        if (normalized.isBlank()) return
        val context = resolvePlanningContext() ?: return
        val nextTodo = (store.todayPlanItems(context.bookId, context.pageTitle) + normalized).distinct()
        store.saveTodayPlanItems(context.bookId, context.pageTitle, nextTodo)
        if (context.matchesCurrentPage) {
            _uiState.update { it.copy(todayPlanItems = nextTodo) }
        }
    }

    fun applyInspirationToToday(items: List<String>) {
        val normalized = items.map(String::trim).filter(String::isNotBlank).distinct()
        if (normalized.isEmpty()) return
        val context = resolvePlanningContext() ?: return
        val nextTodo = (store.todayPlanItems(context.bookId, context.pageTitle) + normalized).distinct()
        store.saveTodayPlanItems(context.bookId, context.pageTitle, nextTodo)
        if (context.matchesCurrentPage) {
            _uiState.update { it.copy(todayPlanItems = nextTodo) }
        }
    }

    fun updateScheduleTitleFromHandbook(entryId: String, newTitle: String) {
        val normalized = newTitle.trim()
        if (entryId.isBlank() || normalized.isBlank()) return
        val updated = store.scheduleEntries().map { entry ->
            if (entry.id == entryId) entry.copy(title = normalized) else entry
        }
        store.saveScheduleEntries(updated)
        syncEditableContent()
    }

    private fun syncCompletedItemToDiary(bookId: String, item: String) {
        val diaryPage = currentBook().pages.firstOrNull { it is DiaryPage } as? DiaryPage ?: return
        val raw = store.diaryText(bookId, diaryPage.title)
        val updated = appendToDiarySection(
            raw = raw,
            sectionName = "今日完成",
            item = item,
        )
        store.setDiaryText(bookId, diaryPage.title, updated)
        if (currentPage() is DiaryPage && currentPage().title == diaryPage.title) {
            _uiState.update { it.copy(diaryDraft = updated) }
        }
    }

    private fun removeCompletedItemFromDiary(bookId: String, item: String) {
        val diaryPage = currentBook().pages.firstOrNull { it is DiaryPage } as? DiaryPage ?: return
        val raw = store.diaryText(bookId, diaryPage.title)
        val updated = removeFromDiarySection(
            raw = raw,
            sectionName = "今日完成",
            item = item,
        )
        store.setDiaryText(bookId, diaryPage.title, updated)
        if (currentPage() is DiaryPage && currentPage().title == diaryPage.title) {
            _uiState.update { it.copy(diaryDraft = updated) }
        }
    }

    private fun appendToDiarySection(raw: String, sectionName: String, item: String): String {
        val normalized = item.trim()
        if (normalized.isBlank()) return raw
        val marker = "# $sectionName"
        val sections = raw.lines().toMutableList()
        val markerIndex = sections.indexOfFirst { it.trim() == marker }
        if (markerIndex < 0) {
            val prefix = if (raw.isBlank()) "" else raw.trimEnd() + "\n"
            return prefix + marker + "\n" + normalized
        }
        val insertStart = markerIndex + 1
        var insertEnd = sections.size
        for (i in insertStart until sections.size) {
            if (sections[i].trim().startsWith("# ")) {
                insertEnd = i
                break
            }
        }
        val existing = sections.subList(insertStart, insertEnd)
            .map { it.trim().removePrefix("- ").removePrefix("• ").removePrefix("✓ ").trim() }
            .filter { it.isNotBlank() }
        if (normalized in existing) return raw
        sections.add(insertEnd, normalized)
        return sections.joinToString("\n").trimEnd()
    }

    private fun removeFromDiarySection(raw: String, sectionName: String, item: String): String {
        val normalized = item.trim()
        if (normalized.isBlank() || raw.isBlank()) return raw
        val marker = "# $sectionName"
        val lines = raw.lines().toMutableList()
        val markerIndex = lines.indexOfFirst { it.trim() == marker }
        if (markerIndex < 0) return raw
        val start = markerIndex + 1
        var end = lines.size
        for (i in start until lines.size) {
            if (lines[i].trim().startsWith("# ")) {
                end = i
                break
            }
        }
        for (i in start until end) {
            val current = lines[i].trim().removePrefix("- ").removePrefix("• ").removePrefix("✓ ").trim()
            if (current == normalized) {
                lines.removeAt(i)
                break
            }
        }
        return lines.joinToString("\n").trimEnd()
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
        val imported = importTodayFromSchedule()
        when (val page = currentPage()) {
            is DiaryPage -> {
                _uiState.update {
                    it.copy(
                        diaryDraft = store.diaryText(book.id, page.title),
                        customPageItems = emptyList(),
                        weeklyTheme = store.weeklyTheme(book.id),
                        todayPlanItems = imported.todo,
                        todayCompletedItems = imported.done,
                        schedulePreviewEntries = monthEntriesForAnchor(),
                    )
                }
            }
            is PlanPage, is TargetPage, is SchedulePage -> {
                _uiState.update {
                    it.copy(
                        diaryDraft = "",
                        customPageItems = store.customPageItems(book.id, page.title),
                        weeklyTheme = store.weeklyTheme(book.id),
                        todayPlanItems = imported.todo,
                        todayCompletedItems = imported.done,
                        schedulePreviewEntries = monthEntriesForAnchor(),
                    )
                }
            }
        }
    }

    private fun importTodayFromSchedule(): ScheduleImport {
        val year = store.calendarAnchorYear()
        val month = store.calendarAnchorMonth().coerceIn(1, 12)
        val day = LocalDate.now().dayOfMonth
        val entries = store.scheduleEntries()
            .filter { it.year == year && it.month == month && it.day == day }
            .sortedBy { it.title }
        val todo = entries.filterNot { it.completed }.map { it.title }.distinct()
        val done = entries.filter { it.completed }.map { it.title }.distinct()
        return ScheduleImport(todo = todo, done = done)
    }

    private fun upsertTodayScheduleEntry(item: String, completed: Boolean) {
        val normalized = item.trim()
        if (normalized.isBlank()) return
        val year = store.calendarAnchorYear()
        val month = store.calendarAnchorMonth().coerceIn(1, 12)
        val day = LocalDate.now().dayOfMonth
        val existing = store.scheduleEntries()
        var matched = false
        val updated = existing.map { entry ->
            if (!matched && entry.year == year && entry.month == month && entry.day == day && entry.title == normalized) {
                matched = true
                entry.copy(completed = completed)
            } else {
                entry
            }
        }.toMutableList()
        if (!matched) {
            updated += ScheduleEntry(
                id = UUID.randomUUID().toString(),
                title = normalized,
                year = year,
                month = month,
                day = day,
                note = currentBook().title,
                completed = completed,
            )
        }
        store.saveScheduleEntries(updated)
    }

    private fun removeTodayScheduleEntry(item: String) {
        val normalized = item.trim()
        if (normalized.isBlank()) return
        val year = store.calendarAnchorYear()
        val month = store.calendarAnchorMonth().coerceIn(1, 12)
        val day = LocalDate.now().dayOfMonth
        val updated = store.scheduleEntries().filterNot {
            it.year == year && it.month == month && it.day == day && it.title == normalized
        }
        store.saveScheduleEntries(updated)
    }

    private fun monthEntriesForAnchor(): List<ScheduleEntry> {
        val year = store.calendarAnchorYear()
        val month = store.calendarAnchorMonth().coerceIn(1, 12)
        return store.scheduleEntries()
            .filter { it.year == year && it.month == month }
            .sortedWith(compareBy({ it.day }, { it.title }))
    }

    private data class PlanningContext(
        val bookId: String,
        val pageTitle: String,
        val matchesCurrentPage: Boolean,
    )

    private fun resolvePlanningContext(): PlanningContext? {
        val current = currentPage()
        if (current is PlanPage || current is TargetPage || current is SchedulePage) {
            return PlanningContext(
                bookId = currentBook().id,
                pageTitle = current.title,
                matchesCurrentPage = true,
            )
        }
        val currentBookContext = currentBook().pages.firstOrNull {
            it is PlanPage || it is TargetPage || it is SchedulePage
        }
        if (currentBookContext != null) {
            return PlanningContext(
                bookId = currentBook().id,
                pageTitle = currentBookContext.title,
                matchesCurrentPage = false,
            )
        }
        val fallback = _uiState.value.books.asSequence()
            .flatMap { book -> book.pages.asSequence().map { page -> book to page } }
            .firstOrNull { (_, page) -> page is PlanPage || page is TargetPage || page is SchedulePage }
            ?: return null
        return PlanningContext(
            bookId = fallback.first.id,
            pageTitle = fallback.second.title,
            matchesCurrentPage = false,
        )
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
