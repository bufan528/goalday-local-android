package com.bf410.goaldaylocal.data

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
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

    /** 完成日期（对照原版勾选后行下显示的日期戳，如 2026-09-06） */
    fun checkedDate(bookId: String, pageTitle: String, item: String): String =
        mmkv.decodeString(checkKey(bookId, pageTitle, item) + "_date", "").orEmpty()

    fun setCheckedDate(bookId: String, pageTitle: String, item: String, dateText: String) {
        mmkv.encode(checkKey(bookId, pageTitle, item) + "_date", dateText)
    }

    fun savedBookIds(): Set<String> = mmkv.decodeStringSet(KEY_SAVED_BOOKS, emptySet()) ?: emptySet()

    fun saveBook(bookId: String) {
        mmkv.encode(KEY_SAVED_BOOKS, savedBookIds() + bookId)
    }

    fun removeSavedBook(bookId: String) {
        mmkv.encode(KEY_SAVED_BOOKS, savedBookIds() - bookId)
    }

    fun calendarAnchorYear(): Int = safeScheduleYear(mmkv.decodeInt(KEY_CALENDAR_YEAR, LocalDate.now().year))

    fun calendarAnchorMonth(): Int = safeScheduleMonth(mmkv.decodeInt(KEY_CALENDAR_MONTH, LocalDate.now().monthValue))

    fun setCalendarAnchor(year: Int, month: Int) {
        val safeDate = safeScheduleDate(year, month, 1)
        mmkv.encode(KEY_CALENDAR_YEAR, safeDate.year)
        mmkv.encode(KEY_CALENDAR_MONTH, safeDate.month)
    }

    fun calendarTheme(year: Int, month: Int): String =
        mmkv.decodeString(calendarThemeKey(year, month), "") ?: ""

    fun setCalendarTheme(year: Int, month: Int, text: String) {
        mmkv.encode(calendarThemeKey(year, month), text)
    }

    fun scheduleEntries(): List<ScheduleEntry> {
        val raw = mmkv.decodeString(KEY_SCHEDULES, "[]") ?: "[]"
        val array = runCatching { JSONArray(raw) }.getOrElse { JSONArray() }
        return buildList {
            repeat(array.length()) { index ->
                runCatching {
                    val item = array.getJSONObject(index)
                    val safeDate = safeScheduleDate(
                        year = item.optInt("year", LocalDate.now().year),
                        month = item.optInt("month", LocalDate.now().monthValue),
                        day = item.optInt("day", LocalDate.now().dayOfMonth),
                    )
                    ScheduleEntry(
                        id = item.optString("id").ifBlank { UUID.randomUUID().toString() },
                        title = item.optString("title").ifBlank { "未命名日程" },
                        year = safeDate.year,
                        month = safeDate.month,
                        day = safeDate.day,
                        note = item.optString("note"),
                        timeText = item.optString("timeText"),
                        repeatRule = item.optString("repeatRule"),
                        repeatInterval = item.optInt("repeatInterval", 1).coerceAtLeast(1),
                        repeatEndDate = item.optString("repeatEndDate"),
                        repeatGroupId = item.optString("repeatGroupId"),
                        completed = decodeScheduleStatus(item).let { status ->
                            status == ScheduleStatus.DONE
                        },
                    )
                }.getOrNull()?.let(::add)
            }
        }
    }

    fun saveScheduleEntries(entries: List<ScheduleEntry>) {
        val array = JSONArray()
        entries.forEach { entry ->
            val safeDate = safeScheduleDate(entry.year, entry.month, entry.day)
            array.put(
                JSONObject()
                    .put("id", entry.id)
                    .put("title", entry.title)
                    .put("year", safeDate.year)
                    .put("month", safeDate.month)
                    .put("day", safeDate.day)
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
        val safeDate = safeScheduleDate(year, month, day)
        val updated = scheduleEntries() + ScheduleEntry(
            id = UUID.randomUUID().toString(),
            title = title,
            year = safeDate.year,
            month = safeDate.month,
            day = safeDate.day,
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
        val array = runCatching { JSONArray(raw) }.getOrElse { JSONArray() }
        return buildList {
            repeat(array.length()) { index -> array.optString(index).takeIf(String::isNotBlank)?.let(::add) }
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

    fun targetItemMeta(bookId: String, pageTitle: String, item: String): TargetItemMeta {
        val raw = mmkv.decodeString(targetMetaKey(bookId, pageTitle, item), null) ?: return TargetItemMeta()
        val json = runCatching { JSONObject(raw) }.getOrNull() ?: return TargetItemMeta()
        val maxDeadlineDay = java.time.YearMonth.now().lengthOfMonth()
        return TargetItemMeta(
            note = json.optString("note"),
            deadlineDay = json.optInt("deadlineDay", 0)
                .takeIf { it > 0 }
                ?.coerceIn(1, maxDeadlineDay),
        )
    }

    fun setTargetItemMeta(bookId: String, pageTitle: String, item: String, meta: TargetItemMeta) {
        val key = targetMetaKey(bookId, pageTitle, item)
        if (meta.note.isBlank() && meta.deadlineDay == null) {
            mmkv.removeValueForKey(key)
            return
        }
        mmkv.encode(
            key,
            JSONObject()
                .put("note", meta.note)
                .put("deadlineDay", meta.deadlineDay ?: 0)
                .toString(),
        )
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
            moveRawString(targetMetaKey(bookId, oldTitle, item), targetMetaKey(bookId, newTitle, item))
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
            mmkv.removeValueForKey(targetMetaKey(bookId, pageTitle, item))
        }
    }

    fun customBooks(): List<TopicBook> {
        val raw = mmkv.decodeString(KEY_CUSTOM_BOOKS, "[]") ?: "[]"
        val array = runCatching { JSONArray(raw) }.getOrElse { JSONArray() }
        return buildList {
            repeat(array.length()) { index ->
                runCatching {
                    val item = array.getJSONObject(index)
                    TopicBook(
                        id = item.optString("id").ifBlank { "custom_${UUID.randomUUID()}" },
                        title = item.optString("title").ifBlank { "未命名手账" },
                        subtitle = item.optString("subtitle"),
                        color = Color(item.optInt("color", 0xFFF2C0A5.toInt())),
                        pages = decodePages(item.optJSONArray("pages") ?: JSONArray()),
                    )
                }.getOrNull()?.let(::add)
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

    private fun targetMetaKey(bookId: String, pageTitle: String, item: String): String =
        "target_meta_${bookId}_${pageTitle.hashCode()}_${item.hashCode()}"

    private fun decodeStringList(key: String): List<String> {
        val raw = mmkv.decodeString(key, "[]") ?: "[]"
        val array = runCatching { JSONArray(raw) }.getOrElse { JSONArray() }
        return buildList {
            repeat(array.length()) { index -> array.optString(index).takeIf(String::isNotBlank)?.let(::add) }
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
                    val planArray = JSONArray()
                    page.planItems.forEach { planItem ->
                        planArray.put(
                            JSONObject()
                                .put("title", planItem.title)
                                .put("timeText", planItem.timeText),
                        )
                    }
                    json.put("planItems", planArray)
                    // 保留旧 items 字段用于向下兼容
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
                runCatching {
                    val item = array.getJSONObject(index)
                    val title = item.optString("title").ifBlank { "未命名页面" }
                    when (item.optString("type")) {
                        "target" -> TargetPage(title, item.toStringList("items"))
                        "plan" -> {
                            val planArray = item.optJSONArray("planItems")
                            val planItems = if (planArray != null) {
                                buildList {
                                    repeat(planArray.length()) { i ->
                                        val planItem = planArray.getJSONObject(i)
                                        add(
                                            PlanItem(
                                                title = planItem.optString("title"),
                                                timeText = planItem.optString("timeText"),
                                            ),
                                        )
                                    }
                                }
                            } else {
                                item.toStringList("items").map { PlanItem(it) }
                            }
                            PlanPage(title, planItems.map { it.title }, planItems)
                        }
                        "schedule" -> SchedulePage(title, item.toStringList("items"))
                        "diary" -> DiaryPage(title, item.optString("prompt", "写下这一页最重要的记录。"))
                        else -> DiaryPage(title, "写下这一页最重要的记录。")
                    }
                }.getOrNull()?.let(::add)
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

private fun Color.toArgbCompat(): Int = toArgb()
