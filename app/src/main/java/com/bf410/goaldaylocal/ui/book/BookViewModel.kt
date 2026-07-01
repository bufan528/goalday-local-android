package com.bf410.goaldaylocal.ui.book

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.bf410.goaldaylocal.ui.replica.GoaldayDesign
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.bf410.goaldaylocal.data.BookPage
import com.bf410.goaldaylocal.data.DiaryPage
import com.bf410.goaldaylocal.data.LocalStateStore
import com.bf410.goaldaylocal.data.PlanPage
import com.bf410.goaldaylocal.data.SampleLibrary
import com.bf410.goaldaylocal.data.ScheduleEntry
import com.bf410.goaldaylocal.data.ScheduleRepository
import com.bf410.goaldaylocal.data.ScheduleStatus
import com.bf410.goaldaylocal.data.SchedulePage
import com.bf410.goaldaylocal.data.TargetItemMeta
import com.bf410.goaldaylocal.data.TargetPage
import com.bf410.goaldaylocal.data.TopicBook
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID

class BookViewModel(
    private val store: LocalStateStore,
    private val scheduleRepository: ScheduleRepository,
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
            schedulePreviewEntries = yearEntriesForAnchor(),
            targetItemMeta = emptyMap(),
            customBookCount = store.customBooks().size,
            inLibraryMode = true,
        ),
    )
    val uiState: StateFlow<BookUiState> = _uiState

    init {
        syncPageFromStore()
        syncEditableContent()
        viewModelScope.launch {
            scheduleRepository.revision.drop(1).collect {
                syncEditableContent()
            }
        }
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
        if (books.isEmpty()) return
        val clamped = index.coerceIn(0, books.lastIndex)
        store.setSelectedBookIndex(clamped)
        val book = books[clamped]
        _uiState.update {
            it.copy(
                selectedBookIndex = clamped,
                selectedPageIndex = safePageIndex(book, store.selectedPageIndex(book.id)),
            )
        }
        syncEditableContent()
    }

    fun setPage(index: Int) {
        val book = currentBook()
        val clamped = safePageIndex(book, index)
        store.setSelectedPageIndex(book.id, clamped)
        _uiState.update { it.copy(selectedPageIndex = clamped) }
        syncEditableContent()
    }

    fun isChecked(pageTitle: String, item: String): Boolean =
        store.isChecked(currentBook().id, pageTitle, item)

    fun toggleChecked(pageTitle: String, item: String) {
        val next = !isChecked(pageTitle, item)
        store.setChecked(currentBook().id, pageTitle, item, next)
        _uiState.update { it.copy(checkedRevision = it.checkedRevision + 1L) }
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
        val book = currentBook()
        val page = currentPage() as? DiaryPage ?: return
        store.setDiaryText(book.id, page.title, text)
        syncDiarySchedulesForBook(book)
        _uiState.update { it.copy(diaryDraft = text, schedulePreviewEntries = yearEntriesForAnchor()) }
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
        val book = currentBook()
        val page = currentPage()
        val updated = removeExactItem(_uiState.value.customPageItems, item)
        store.saveCustomPageItems(book.id, page.title, updated)
        store.saveTodayPlanItems(book.id, page.title, removeExactItem(store.todayPlanItems(book.id, page.title), item))
        store.saveTodayCompletedItems(book.id, page.title, removeExactItem(store.todayCompletedItems(book.id, page.title), item))
        store.setChecked(book.id, page.title, item, false)
        if (page is TargetPage) {
            store.setTargetItemMeta(book.id, page.title, item, TargetItemMeta())
        }
        val cleanedSchedules = scheduleRepository.entries().filterNot { entry ->
            entry.title == item && entry.note == book.title
        }
        scheduleRepository.saveEntries(cleanedSchedules)
        syncEditableContent()
    }

    fun renameCustomPageItem(oldItem: String, newItem: String) {
        if (!supportsCustomItems()) return
        val trimmed = newItem.trim()
        if (trimmed.isBlank()) return
        if (trimmed == oldItem) return
        val book = currentBook()
        val page = currentPage()
        val updated = renameExactItemDistinct(_uiState.value.customPageItems, oldItem, trimmed)
        store.saveCustomPageItems(book.id, page.title, updated)
        store.saveTodayPlanItems(book.id, page.title, renameExactItemDistinct(store.todayPlanItems(book.id, page.title), oldItem, trimmed))
        store.saveTodayCompletedItems(book.id, page.title, renameExactItemDistinct(store.todayCompletedItems(book.id, page.title), oldItem, trimmed))
        if (store.isChecked(book.id, page.title, oldItem)) {
            store.setChecked(book.id, page.title, trimmed, true)
            store.setChecked(book.id, page.title, oldItem, false)
        }
        if (page is TargetPage) {
            val meta = store.targetItemMeta(book.id, page.title, oldItem)
            store.setTargetItemMeta(book.id, page.title, trimmed, meta)
            store.setTargetItemMeta(book.id, page.title, oldItem, TargetItemMeta())
        }
        val renamedSchedules = scheduleRepository.entries().map { entry ->
            if (entry.title == oldItem && entry.note == book.title) {
                entry.copy(title = trimmed)
            } else {
                entry
            }
        }
        scheduleRepository.saveEntries(renamedSchedules)
        syncEditableContent()
    }

    fun updateTargetItemNote(item: String, note: String) {
        val page = currentPage() as? TargetPage ?: return
        val normalized = item.trim()
        if (normalized.isBlank()) return
        val bookId = currentBook().id
        val current = store.targetItemMeta(bookId, page.title, normalized)
        store.setTargetItemMeta(bookId, page.title, normalized, current.copy(note = note.trim()))
        syncEditableContent()
    }

    fun updateTargetItemDeadline(item: String, deadlineDay: Int?) {
        val page = currentPage() as? TargetPage ?: return
        val normalized = item.trim()
        if (normalized.isBlank()) return
        val bookId = currentBook().id
        val current = store.targetItemMeta(bookId, page.title, normalized)
        val maxDeadlineDay = YearMonth.now().lengthOfMonth()
        store.setTargetItemMeta(bookId, page.title, normalized, current.copy(deadlineDay = deadlineDay?.coerceIn(1, maxDeadlineDay)))
        syncEditableContent()
    }

    fun addTargetDetailToDiary(item: String, completed: Boolean = false) {
        val normalized = item.trim()
        if (normalized.isBlank()) return
        val book = currentBook()
        val diaryPage = book.pages.firstOrNull { it is DiaryPage } as? DiaryPage ?: return
        val raw = store.diaryText(book.id, diaryPage.title)
        val blockLine = listOf(
            "target",
            if (completed) "check" else "body",
            escapeDiaryScheduleBlockText(normalized),
        ).joinToString("|")
        val updated = appendToDiarySection(
            raw = raw,
            sectionName = "日记块",
            item = blockLine,
        )
        store.setDiaryText(book.id, diaryPage.title, updated)
        if (currentPage() is DiaryPage && currentPage().title == diaryPage.title) {
            _uiState.update { it.copy(diaryDraft = updated) }
        }
        syncDiarySchedulesForBook(book)
        _uiState.update { it.copy(schedulePreviewEntries = yearEntriesForAnchor()) }
    }

    fun addItemToSchedule(item: String, day: Int) {
        val title = item.trim()
        if (title.isBlank()) return
        val year = store.calendarAnchorYear()
        val month = store.calendarAnchorMonth().coerceIn(1, 12)
        val maxDay = YearMonth.of(year, month).lengthOfMonth()
        val safeDay = day.coerceIn(1, maxDay)
        val bookTitle = currentBook().title
        val duplicated = scheduleRepository.entries().any { entry ->
            entry.title == title &&
                entry.year == year &&
                entry.month == month &&
                entry.day == safeDay &&
                entry.note == bookTitle
        }
        if (duplicated) return
        scheduleRepository.addEntry(
            title = title,
            year = year,
            month = month,
            day = safeDay,
            note = bookTitle,
        )
        _uiState.update { it.copy(schedulePreviewEntries = yearEntriesForAnchor()) }
    }

    fun addScheduleFromHandbook(item: String, month: Int, day: Int) {
        val title = item.trim()
        if (title.isBlank()) return
        val year = store.calendarAnchorYear()
        val safeMonth = month.coerceIn(1, 12)
        val maxDay = YearMonth.of(year, safeMonth).lengthOfMonth()
        val safeDay = day.coerceIn(1, maxDay)
        val duplicated = scheduleRepository.entries().any { entry ->
            entry.title == title &&
                entry.year == year &&
                entry.month == safeMonth &&
                entry.day == safeDay
        }
        if (duplicated) return
        scheduleRepository.addEntry(
            title = title,
            year = year,
            month = safeMonth,
            day = safeDay,
            note = currentBook().title,
        )
        syncEditableContent()
    }

    fun addHandbookPoolItem(item: String) {
        val title = item.trim()
        if (title.isBlank()) return
        val context = resolvePlanningContext() ?: return
        val updated = (store.todayPlanItems(context.bookId, context.pageTitle) + title).distinct()
        store.saveTodayPlanItems(context.bookId, context.pageTitle, updated)
        syncEditableContent()
    }

    fun removeHandbookPoolItem(item: String) {
        val title = item.trim()
        if (title.isBlank()) return
        val context = resolvePlanningContext() ?: return
        val updated = store.todayPlanItems(context.bookId, context.pageTitle).filterNot { it == title }
        store.saveTodayPlanItems(context.bookId, context.pageTitle, updated)
        syncEditableContent()
    }

    fun updateWeeklyTheme(text: String) {
        val book = currentBook()
        store.setWeeklyTheme(book.id, text)
        _uiState.update { it.copy(weeklyTheme = text) }
    }

    fun refreshSchedulePreview() {
        syncEditableContent()
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
        val updated = scheduleRepository.entries().map { entry ->
            if (entry.id == entryId) entry.copy(title = normalized) else entry
        }
        scheduleRepository.saveEntries(updated)
        syncEditableContent()
    }

    fun moveScheduleDayFromHandbook(entryId: String, month: Int, day: Int) {
        if (entryId.isBlank()) return
        val year = store.calendarAnchorYear()
        val safeMonth = month.coerceIn(1, 12)
        val maxDay = YearMonth.of(year, safeMonth).lengthOfMonth()
        val safeDay = day.coerceIn(1, maxDay)
        val updated = scheduleRepository.entries().map { entry ->
            if (entry.id == entryId) {
                entry.copy(
                    year = year,
                    month = safeMonth,
                    day = safeDay,
                    completed = false,
                )
            } else {
                entry
            }
        }
        scheduleRepository.saveEntries(updated)
        syncEditableContent()
    }

    fun toggleScheduleCompletedFromHandbook(entryId: String) {
        if (entryId.isBlank()) return
        val updated = scheduleRepository.entries().map { entry ->
            if (entry.id == entryId) {
                entry.withStatus(if (entry.status == ScheduleStatus.DONE) ScheduleStatus.PLANNED else ScheduleStatus.DONE)
            } else {
                entry
            }
        }
        scheduleRepository.saveEntries(updated)
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

    fun createTemplateBook(title: String, subtitle: String, color: Color, items: List<String>) {
        val normalized = items.map(String::trim).filter(String::isNotBlank).distinct()
        if (title.isBlank() || normalized.isEmpty()) return
        val newBook = store.addCustomBook(title.trim(), subtitle.trim(), color)
        val pages = listOf(
            TargetPage("目标详情", normalized.take(18)),
            PlanPage("行动拆解", normalized.take(8)),
            SchedulePage("日程池", listOf("从右侧任务池拖入日期", "完成后拖入已完成", "周末复盘完成情况")),
            DiaryPage("记录页", "记录这个主题今天推进了什么。"),
        )
        store.updateCustomBook(newBook.copy(pages = pages))
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
        currentBook.pages.forEach { page ->
            store.removePageScopedData(
                bookId = currentBook.id,
                pageTitle = page.title,
                checkedItems = checkedItemsForPage(currentBook.id, page),
            )
        }
        store.removeSavedBook(currentBook.id)
        store.removeCustomBook(currentBook.id)
        refreshBooks(selectBookId = allBooks().firstOrNull()?.id.orEmpty(), openBook = false)
    }

    fun renameCurrentPage(title: String) {
        val trimmed = title.trim()
        if (trimmed.isBlank() || !isCurrentBookCustom()) return
        val current = currentBook()
        val oldPage = currentPageOrNull() ?: return
        val safeIndex = safePageIndex(current, _uiState.value.selectedPageIndex)
        store.migratePageScopedData(
            bookId = current.id,
            oldTitle = oldPage.title,
            newTitle = trimmed,
            checkedItems = checkedItemsForPage(current.id, oldPage),
        )
        updateCurrentBookPages { pages ->
            pages.toMutableList().also { list ->
                if (safeIndex in list.indices) {
                    list[safeIndex] = renamePage(list[safeIndex], trimmed)
                }
            }
        }
    }

    fun moveCurrentPageLeft() {
        val currentIndex = _uiState.value.selectedPageIndex
        val pages = currentBook().pages
        if (!isCurrentBookCustom() || currentIndex == 0 || currentIndex !in pages.indices) return
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
        val pages = currentBook().pages
        if (!isCurrentBookCustom() || currentIndex !in pages.indices || currentIndex >= pages.lastIndex) return
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
        val newPageIndex = currentBook().pages.size
        updateCurrentBookPages { it + newPage }
        setPage(newPageIndex)
    }

    fun deleteCurrentPage() {
        val current = currentBook()
        if (!isCurrentBookCustom() || current.pages.size <= 1) return
        val removedIndex = safePageIndex(current, _uiState.value.selectedPageIndex)
        val removedPage = current.pages[removedIndex]
        store.removePageScopedData(
            bookId = current.id,
            pageTitle = removedPage.title,
            checkedItems = checkedItemsForPage(current.id, removedPage),
        )
        updateCurrentBookPages { pages ->
            pages.filterIndexed { index, _ -> index != removedIndex }
        }
        val nextIndex = removedIndex.coerceAtMost(currentBook().pages.lastIndex)
        setPage(nextIndex)
    }

    private fun currentBook(): TopicBook {
        val books = _uiState.value.books
        return books.getOrNull(_uiState.value.selectedBookIndex)
            ?: books.firstOrNull()
            ?: TopicBook(
                id = "empty_local_book",
                title = "本地手账",
                subtitle = "暂无页面",
                color = GoaldayDesign.EmptyBookCover,
                pages = emptyList(),
            )
    }

    private fun currentPage(): BookPage =
        currentPageOrNull() ?: DiaryPage("日记页", "写下这一页最重要的记录。")

    private fun currentPageOrNull(): BookPage? {
        val book = currentBook()
        if (book.pages.isEmpty()) return null
        return book.pages.getOrNull(safePageIndex(book, _uiState.value.selectedPageIndex))
    }

    private fun supportsCustomItems(): Boolean =
        when (currentPageOrNull()) {
            is PlanPage, is TargetPage, is SchedulePage -> true
            else -> false
        }

    private fun isCurrentBookCustom(): Boolean = currentBook().id.startsWith("custom_")

    private fun syncPageFromStore() {
        val book = currentBook()
        _uiState.update {
            it.copy(
                selectedPageIndex = safePageIndex(book, store.selectedPageIndex(book.id)),
            )
        }
    }

    private fun syncEditableContent() {
        val book = currentBook()
        if (book.pages.isEmpty()) {
            _uiState.update {
                it.copy(
                    selectedPageIndex = 0,
                    diaryDraft = "",
                    customPageItems = emptyList(),
                    weeklyTheme = store.weeklyTheme(book.id),
                    todayPlanItems = emptyList(),
                    todayCompletedItems = emptyList(),
                    schedulePreviewEntries = yearEntriesForAnchor(),
                    targetItemMeta = emptyMap(),
                )
            }
            return
        }
        syncDiarySchedulesForBook(book)
        val imported = importTodayFromSchedule()
        when (val page = currentPage()) {
            is DiaryPage -> {
                val planningPage = book.pages.firstOrNull { it is PlanPage || it is TargetPage || it is SchedulePage }
                val storedPool = planningPage?.let { store.todayPlanItems(book.id, it.title) }.orEmpty()
                _uiState.update {
                    it.copy(
                        diaryDraft = store.diaryText(book.id, page.title),
                        customPageItems = emptyList(),
                        weeklyTheme = store.weeklyTheme(book.id),
                        todayPlanItems = (storedPool + imported.todo).distinct(),
                        todayCompletedItems = imported.done,
                        schedulePreviewEntries = yearEntriesForAnchor(),
                        targetItemMeta = emptyMap(),
                    )
                }
            }
            is PlanPage, is TargetPage, is SchedulePage -> {
                val storedPool = store.todayPlanItems(book.id, page.title)
                val pageItems = when (page) {
                    is TargetPage -> page.items + store.customPageItems(book.id, page.title)
                    else -> emptyList()
                }
                _uiState.update {
                    it.copy(
                        diaryDraft = "",
                        customPageItems = store.customPageItems(book.id, page.title),
                        weeklyTheme = store.weeklyTheme(book.id),
                        todayPlanItems = (storedPool + imported.todo).distinct(),
                        todayCompletedItems = imported.done,
                        schedulePreviewEntries = yearEntriesForAnchor(),
                        targetItemMeta = pageItems.distinct().associateWith { item ->
                            store.targetItemMeta(book.id, page.title, item)
                        },
                    )
                }
            }
        }
    }

    private fun syncDiarySchedulesForBook(book: TopicBook) {
        val diaryPages = book.pages.filterIsInstance<DiaryPage>()
        if (diaryPages.isEmpty()) return
        val sourceNotes = diaryPages.map { page -> diaryScheduleSourceNote(book, page) }.toSet()
        val existing = scheduleRepository.entries()
        val existingDiaryEntries = existing.filter { it.note in sourceNotes }
        val existingByKey = existingDiaryEntries.associateBy { entry ->
            diaryScheduleKey(entry.title, entry.year, entry.month, entry.day, entry.completed, entry.note)
        }
        val desired = diaryPages.flatMap { page ->
            parseDiaryScheduleCandidates(
                raw = store.diaryText(book.id, page.title),
                sourceNote = diaryScheduleSourceNote(book, page),
            )
        }
        val desiredEntries = desired.distinctBy { candidate ->
            diaryScheduleKey(
                title = candidate.title,
                year = candidate.date.year,
                month = candidate.date.monthValue,
                day = candidate.date.dayOfMonth,
                completed = candidate.completed,
                note = candidate.sourceNote,
            )
        }.map { candidate ->
            val key = diaryScheduleKey(
                title = candidate.title,
                year = candidate.date.year,
                month = candidate.date.monthValue,
                day = candidate.date.dayOfMonth,
                completed = candidate.completed,
                note = candidate.sourceNote,
            )
            existingByKey[key]?.copy(
                title = candidate.title,
                year = candidate.date.year,
                month = candidate.date.monthValue,
                day = candidate.date.dayOfMonth,
                completed = candidate.completed,
                note = candidate.sourceNote,
            ) ?: ScheduleEntry(
                id = UUID.randomUUID().toString(),
                title = candidate.title,
                year = candidate.date.year,
                month = candidate.date.monthValue,
                day = candidate.date.dayOfMonth,
                completed = candidate.completed,
                note = candidate.sourceNote,
            )
        }
        val next = existing.filterNot { it.note in sourceNotes } + desiredEntries
        if (next.scheduleSignature() != existing.scheduleSignature()) {
            scheduleRepository.saveEntries(next)
        }
    }

    // 那年今日闪回：枚举所有历史日记，筛选与给定日期同月同日且往年的记录
    fun loadOnThisDayFor(date: LocalDate): List<OnThisDayDiary> {
        val results = mutableListOf<OnThisDayDiary>()
        for (book in allBooks()) {
            for (page in book.pages) {
                if (page !is DiaryPage) continue
                val raw = store.diaryText(book.id, page.title)
                if (raw.isBlank()) continue
                val parsed = StructuredDiary.fromRaw(raw)
                val d = parsed.date
                if (d.month == date.month && d.dayOfMonth == date.dayOfMonth && d.year < date.year) {
                    val previewText = plainTextFromHtml(parsed.richHtml).ifBlank {
                        listOf(parsed.todayDone, parsed.smallJoy, parsed.workTasks, parsed.canImprove)
                            .firstOrNull { it.isNotBlank() } ?: ""
                    }
                    results.add(
                        OnThisDayDiary(
                            date = d,
                            yearsAgo = date.year - d.year,
                            bookTitle = book.title,
                            pageTitle = page.title,
                            preview = previewText.take(80),
                            moodTags = parsed.moodTags,
                        ),
                    )
                }
            }
        }
        return results.sortedByDescending { it.date }
    }

    private fun diaryScheduleSourceNote(book: TopicBook, page: DiaryPage): String =
        "日记同步 · ${book.title} · ${page.title}"

    private fun diaryScheduleKey(
        title: String,
        year: Int,
        month: Int,
        day: Int,
        completed: Boolean,
        note: String,
    ): String = listOf(title.trim(), year, month, day, completed, note).joinToString("|")

    private fun List<ScheduleEntry>.scheduleSignature(): List<String> =
        map { entry ->
            diaryScheduleKey(entry.title, entry.year, entry.month, entry.day, entry.completed, entry.note) +
                "|${entry.id}|${entry.timeText}|${entry.repeatRule}|${entry.repeatInterval}|${entry.repeatEndDate}|${entry.repeatGroupId}"
        }.sorted()

    private fun importTodayFromSchedule(): ScheduleImport {
        val year = store.calendarAnchorYear()
        val month = store.calendarAnchorMonth().coerceIn(1, 12)
        val day = LocalDate.now().dayOfMonth
        val entries = scheduleRepository.entries()
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
        val existing = scheduleRepository.entries()
        var matched = false
        val updated = existing.map { entry ->
            if (!matched && entry.year == year && entry.month == month && entry.day == day && entry.title == normalized) {
                matched = true
                entry.withStatus(if (completed) ScheduleStatus.DONE else ScheduleStatus.PLANNED)
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
        scheduleRepository.saveEntries(updated)
    }

    private fun removeTodayScheduleEntry(item: String) {
        val normalized = item.trim()
        if (normalized.isBlank()) return
        val year = store.calendarAnchorYear()
        val month = store.calendarAnchorMonth().coerceIn(1, 12)
        val day = LocalDate.now().dayOfMonth
        val updated = scheduleRepository.entries().filterNot {
            it.year == year && it.month == month && it.day == day && it.title == normalized
        }
        scheduleRepository.saveEntries(updated)
    }

    private fun yearEntriesForAnchor(): List<ScheduleEntry> {
        val year = store.calendarAnchorYear()
        return scheduleRepository.entries()
            .filter { it.year == year }
            .sortedWith(compareBy({ it.month }, { it.day }, { it.title.lowercase() }))
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
        val selectedIndex = updatedBooks.indexOfFirst { it.id == selectBookId }.takeIf { it >= 0 } ?: 0
        store.setSelectedBookIndex(selectedIndex)
        _uiState.update {
            val selectedBook = updatedBooks.getOrNull(selectedIndex) ?: return@update it.copy(
                books = updatedBooks,
                customBookCount = store.customBooks().size,
                inLibraryMode = !openBook,
            )
            it.copy(
                books = updatedBooks,
                selectedBookIndex = selectedIndex,
                selectedPageIndex = safePageIndex(selectedBook, store.selectedPageIndex(selectedBook.id)),
                customBookCount = store.customBooks().size,
                inLibraryMode = !openBook,
            )
        }
        if (openBook) {
            _uiState.update { it.copy(inLibraryMode = false) }
        }
        syncEditableContent()
    }

    private fun safePageIndex(book: TopicBook, requested: Int): Int =
        if (book.pages.isEmpty()) 0 else requested.coerceIn(0, book.pages.lastIndex)

    private fun renamePage(page: BookPage, title: String): BookPage =
        when (page) {
            is TargetPage -> page.copy(title = title)
            is PlanPage -> page.copy(title = title)
            is SchedulePage -> page.copy(title = title)
            is DiaryPage -> page.copy(title = title)
        }

    private fun checkedItemsForPage(bookId: String, page: BookPage): List<String> =
        when (page) {
            is TargetPage -> page.items + store.customPageItems(bookId, page.title)
            is PlanPage -> page.items + store.customPageItems(bookId, page.title)
            is SchedulePage -> page.items + store.customPageItems(bookId, page.title)
            is DiaryPage -> emptyList()
        }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val store = LocalStateStore(MMKV.defaultMMKV())
                val scheduleRepository = ScheduleRepository.getInstance(store)
                @Suppress("UNCHECKED_CAST")
                return BookViewModel(store, scheduleRepository) as T
            }
        }
    }
}
