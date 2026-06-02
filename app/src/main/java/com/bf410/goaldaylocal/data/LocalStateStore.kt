package com.bf410.goaldaylocal.data

import androidx.compose.ui.graphics.Color
import com.tencent.mmkv.MMKV
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.util.UUID

class LocalStateStore(
    private val mmkv: MMKV,
) {
    fun selectedBookIndex(): Int = mmkv.decodeInt(KEY_BOOK, 0)

    fun selectedPageIndex(bookId: String): Int = mmkv.decodeInt("page_$bookId", 0)

    fun setSelectedBookIndex(index: Int) {
        mmkv.encode(KEY_BOOK, index)
    }

    fun setSelectedPageIndex(bookId: String, index: Int) {
        mmkv.encode("page_$bookId", index)
    }

    fun isChecked(bookId: String, pageTitle: String, item: String): Boolean =
        mmkv.decodeBool(checkKey(bookId, pageTitle, item), false)

    fun setChecked(bookId: String, pageTitle: String, item: String, checked: Boolean) {
        mmkv.encode(checkKey(bookId, pageTitle, item), checked)
    }

    fun savedBookIds(): Set<String> = mmkv.decodeStringSet(KEY_SAVED_BOOKS, emptySet()) ?: emptySet()

    fun saveBook(bookId: String) {
        mmkv.encode(KEY_SAVED_BOOKS, savedBookIds() + bookId)
    }

    fun removeSavedBook(bookId: String) {
        mmkv.encode(KEY_SAVED_BOOKS, savedBookIds() - bookId)
    }

    fun calendarAnchorYear(): Int = mmkv.decodeInt(KEY_CALENDAR_YEAR, LocalDate.now().year)

    fun calendarAnchorMonth(): Int = mmkv.decodeInt(KEY_CALENDAR_MONTH, LocalDate.now().monthValue)

    fun setCalendarAnchor(year: Int, month: Int) {
        mmkv.encode(KEY_CALENDAR_YEAR, year)
        mmkv.encode(KEY_CALENDAR_MONTH, month)
    }

    fun calendarTheme(year: Int, month: Int): String =
        mmkv.decodeString(calendarThemeKey(year, month), "") ?: ""

    fun setCalendarTheme(year: Int, month: Int, text: String) {
        mmkv.encode(calendarThemeKey(year, month), text)
    }

    fun scheduleEntries(): List<ScheduleEntry> {
        val raw = mmkv.decodeString(KEY_SCHEDULES, "[]") ?: "[]"
        val array = JSONArray(raw)
        return buildList {
            repeat(array.length()) { index ->
                val item = array.getJSONObject(index)
                add(
                    ScheduleEntry(
                        id = item.getString("id"),
                        title = item.getString("title"),
                        year = item.getInt("year"),
                        month = item.getInt("month"),
                        day = item.getInt("day"),
                        note = item.optString("note"),
                        timeText = item.optString("timeText"),
                        repeatRule = item.optString("repeatRule"),
                        repeatInterval = item.optInt("repeatInterval", 1).coerceAtLeast(1),
                        repeatEndDate = item.optString("repeatEndDate"),
                        repeatGroupId = item.optString("repeatGroupId"),
                        completed = decodeScheduleStatus(item).let { status ->
                            status == ScheduleStatus.DONE
                        },
                    ),
                )
            }
        }
    }

    fun saveScheduleEntries(entries: List<ScheduleEntry>) {
        val array = JSONArray()
        entries.forEach { entry ->
            array.put(
                JSONObject()
                    .put("id", entry.id)
                    .put("title", entry.title)
                    .put("year", entry.year)
                    .put("month", entry.month)
                    .put("day", entry.day)
                    .put("note", entry.note)
                    .put("timeText", entry.timeText)
                    .put("repeatRule", entry.repeatRule)
                    .put("repeatInterval", entry.repeatInterval)
                    .put("repeatEndDate", entry.repeatEndDate)
                    .put("repeatGroupId", entry.repeatGroupId)
                    .put("status", entry.status.name)
                    .put("completed", entry.completed),
            )
        }
        mmkv.encode(KEY_SCHEDULES, array.toString())
    }

    fun addScheduleEntry(
        title: String,
        year: Int,
        month: Int,
        day: Int,
        note: String = "",
        timeText: String = "",
        repeatRule: String = "",
        repeatInterval: Int = 1,
        repeatEndDate: String = "",
        repeatGroupId: String = "",
    ) {
        val updated = scheduleEntries() + ScheduleEntry(
            id = UUID.randomUUID().toString(),
            title = title,
            year = year,
            month = month,
            day = day,
            note = note,
            timeText = timeText,
            repeatRule = repeatRule,
            repeatInterval = repeatInterval.coerceAtLeast(1),
            repeatEndDate = repeatEndDate,
            repeatGroupId = repeatGroupId,
        )
        saveScheduleEntries(updated)
    }

    fun diaryText(bookId: String, pageTitle: String): String =
        mmkv.decodeString(diaryKey(bookId, pageTitle), "") ?: ""

    fun setDiaryText(bookId: String, pageTitle: String, text: String) {
        mmkv.encode(diaryKey(bookId, pageTitle), text)
    }

    fun customPageItems(bookId: String, pageTitle: String): List<String> {
        val raw = mmkv.decodeString(pageItemsKey(bookId, pageTitle), "[]") ?: "[]"
        val array = JSONArray(raw)
        return buildList {
            repeat(array.length()) { index ->
                add(array.getString(index))
            }
        }
    }

    fun saveCustomPageItems(bookId: String, pageTitle: String, items: List<String>) {
        val array = JSONArray()
        items.forEach(array::put)
        mmkv.encode(pageItemsKey(bookId, pageTitle), array.toString())
    }

    fun weeklyTheme(bookId: String): String =
        mmkv.decodeString(weeklyThemeKey(bookId), "") ?: ""

    fun setWeeklyTheme(bookId: String, text: String) {
        mmkv.encode(weeklyThemeKey(bookId), text)
    }

    fun todayPlanItems(bookId: String, pageTitle: String): List<String> =
        decodeStringList(todayPlanKey(bookId, pageTitle))

    fun saveTodayPlanItems(bookId: String, pageTitle: String, items: List<String>) {
        encodeStringList(todayPlanKey(bookId, pageTitle), items)
    }

    fun todayCompletedItems(bookId: String, pageTitle: String): List<String> =
        decodeStringList(todayDoneKey(bookId, pageTitle))

    fun saveTodayCompletedItems(bookId: String, pageTitle: String, items: List<String>) {
        encodeStringList(todayDoneKey(bookId, pageTitle), items)
    }

    fun migratePageScopedData(
        bookId: String,
        oldTitle: String,
        newTitle: String,
        checkedItems: List<String> = emptyList(),
    ) {
        if (oldTitle == newTitle) return
        moveRawString(diaryKey(bookId, oldTitle), diaryKey(bookId, newTitle))
        moveRawString(pageItemsKey(bookId, oldTitle), pageItemsKey(bookId, newTitle))
        moveRawString(todayPlanKey(bookId, oldTitle), todayPlanKey(bookId, newTitle))
        moveRawString(todayDoneKey(bookId, oldTitle), todayDoneKey(bookId, newTitle))
        checkedItems.distinct().forEach { item ->
            val oldKey = checkKey(bookId, oldTitle, item)
            if (mmkv.decodeBool(oldKey, false)) {
                mmkv.encode(checkKey(bookId, newTitle, item), true)
                mmkv.removeValueForKey(oldKey)
            }
        }
    }

    fun removePageScopedData(
        bookId: String,
        pageTitle: String,
        checkedItems: List<String> = emptyList(),
    ) {
        mmkv.removeValueForKey(diaryKey(bookId, pageTitle))
        mmkv.removeValueForKey(pageItemsKey(bookId, pageTitle))
        mmkv.removeValueForKey(todayPlanKey(bookId, pageTitle))
        mmkv.removeValueForKey(todayDoneKey(bookId, pageTitle))
        checkedItems.distinct().forEach { item ->
            mmkv.removeValueForKey(checkKey(bookId, pageTitle, item))
        }
    }

    fun customBooks(): List<TopicBook> {
        val raw = mmkv.decodeString(KEY_CUSTOM_BOOKS, "[]") ?: "[]"
        val array = JSONArray(raw)
        return buildList {
            repeat(array.length()) { index ->
                val item = array.getJSONObject(index)
                add(
                    TopicBook(
                        id = item.getString("id"),
                        title = item.getString("title"),
                        subtitle = item.getString("subtitle"),
                        color = Color(item.getInt("color")),
                        pages = decodePages(item.getJSONArray("pages")),
                    ),
                )
            }
        }
    }

    fun saveCustomBooks(books: List<TopicBook>) {
        val array = JSONArray()
        books.forEach { book ->
            array.put(
                JSONObject()
                    .put("id", book.id)
                    .put("title", book.title)
                    .put("subtitle", book.subtitle)
                    .put("color", book.color.toArgbCompat())
                    .put("pages", encodePages(book.pages)),
            )
        }
        mmkv.encode(KEY_CUSTOM_BOOKS, array.toString())
    }

    fun addCustomBook(title: String, subtitle: String, color: Color): TopicBook {
        val book = TopicBook(
            id = "custom_${UUID.randomUUID()}",
            title = title,
            subtitle = subtitle,
            color = color,
            pages = listOf(
                TargetPage("目标页", emptyList()),
                PlanPage("计划页", emptyList()),
                SchedulePage("日程页", emptyList()),
                DiaryPage("日记页", "写下这本书今天最重要的一条记录。"),
            ),
        )
        saveCustomBooks(customBooks() + book)
        return book
    }

    fun updateCustomBook(book: TopicBook) {
        saveCustomBooks(
            customBooks().map { existing ->
                if (existing.id == book.id) book else existing
            },
        )
    }

    fun removeCustomBook(bookId: String) {
        saveCustomBooks(customBooks().filterNot { it.id == bookId })
    }

    private fun checkKey(bookId: String, pageTitle: String, item: String): String =
        "check_${bookId}_${pageTitle}_${item.hashCode()}"

    private fun diaryKey(bookId: String, pageTitle: String): String =
        "diary_${bookId}_${pageTitle.hashCode()}"

    private fun pageItemsKey(bookId: String, pageTitle: String): String =
        "page_items_${bookId}_${pageTitle.hashCode()}"

    private fun weeklyThemeKey(bookId: String): String = "week_theme_$bookId"

    private fun calendarThemeKey(year: Int, month: Int): String = "calendar_theme_${year}_$month"

    private fun todayPlanKey(bookId: String, pageTitle: String): String =
        "today_plan_${bookId}_${pageTitle.hashCode()}"

    private fun todayDoneKey(bookId: String, pageTitle: String): String =
        "today_done_${bookId}_${pageTitle.hashCode()}"

    private fun decodeStringList(key: String): List<String> {
        val raw = mmkv.decodeString(key, "[]") ?: "[]"
        val array = JSONArray(raw)
        return buildList {
            repeat(array.length()) { index -> add(array.getString(index)) }
        }
    }

    private fun encodeStringList(key: String, items: List<String>) {
        val array = JSONArray()
        items.forEach(array::put)
        mmkv.encode(key, array.toString())
    }

    private fun moveRawString(oldKey: String, newKey: String) {
        val oldValue = mmkv.decodeString(oldKey, null) ?: return
        val newValue = mmkv.decodeString(newKey, null)
        if (newValue.isNullOrBlank() || newValue == "[]") {
            mmkv.encode(newKey, oldValue)
        }
        mmkv.removeValueForKey(oldKey)
    }

    private fun decodeScheduleStatus(item: JSONObject): ScheduleStatus {
        val rawStatus = item.optString("status", "")
        return ScheduleStatus.entries.firstOrNull { it.name == rawStatus }
            ?: if (item.optBoolean("completed", false)) ScheduleStatus.DONE else ScheduleStatus.PLANNED
    }

    private fun encodePages(pages: List<BookPage>): JSONArray {
        val array = JSONArray()
        pages.forEach { page ->
            val json = JSONObject().put("title", page.title)
            when (page) {
                is TargetPage -> {
                    json.put("type", "target")
                    json.put("items", JSONArray(page.items))
                }
                is PlanPage -> {
                    json.put("type", "plan")
                    json.put("items", JSONArray(page.items))
                }
                is SchedulePage -> {
                    json.put("type", "schedule")
                    json.put("items", JSONArray(page.items))
                }
                is DiaryPage -> {
                    json.put("type", "diary")
                    json.put("prompt", page.prompt)
                }
            }
            array.put(json)
        }
        return array
    }

    private fun decodePages(array: JSONArray): List<BookPage> =
        buildList {
            repeat(array.length()) { index ->
                val item = array.getJSONObject(index)
                val title = item.getString("title")
                when (item.getString("type")) {
                    "target" -> add(TargetPage(title, item.toStringList("items")))
                    "plan" -> add(PlanPage(title, item.toStringList("items")))
                    "schedule" -> add(SchedulePage(title, item.toStringList("items")))
                    "diary" -> add(DiaryPage(title, item.optString("prompt", "")))
                }
            }
        }

    private companion object {
        const val KEY_BOOK = "selected_book"
        const val KEY_SAVED_BOOKS = "saved_books"
        const val KEY_CALENDAR_YEAR = "calendar_year"
        const val KEY_CALENDAR_MONTH = "calendar_month"
        const val KEY_SCHEDULES = "schedules"
        const val KEY_CUSTOM_BOOKS = "custom_books"
    }
}

private fun JSONObject.toStringList(key: String): List<String> {
    val array = optJSONArray(key) ?: JSONArray()
    return buildList {
        repeat(array.length()) { index -> add(array.getString(index)) }
    }
}

private fun Color.toArgbCompat(): Int = value.toLong().toInt()
