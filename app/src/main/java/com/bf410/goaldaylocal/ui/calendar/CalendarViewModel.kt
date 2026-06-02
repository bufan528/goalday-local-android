package com.bf410.goaldaylocal.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.bf410.goaldaylocal.data.LocalStateStore
import com.bf410.goaldaylocal.data.ScheduleEntry
import com.bf410.goaldaylocal.data.ScheduleRepository
import com.bf410.goaldaylocal.data.ScheduleStatus
import com.tencent.mmkv.MMKV
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CalendarUiState(
    val year: Int,
    val month: Int,
    val entries: List<ScheduleEntry>,
    val theme: String,
)

data class CalendarImportCandidate(
    val title: String,
    val day: Int,
    val note: String,
    val timeText: String,
    val calendarName: String = "",
)

class CalendarViewModel(
    private val store: LocalStateStore,
    private val scheduleRepository: ScheduleRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        CalendarUiState(
            year = store.calendarAnchorYear(),
            month = store.calendarAnchorMonth(),
            entries = monthEntries(store.calendarAnchorYear(), store.calendarAnchorMonth()),
            theme = store.calendarTheme(store.calendarAnchorYear(), store.calendarAnchorMonth()),
        ),
    )
    val uiState: StateFlow<CalendarUiState> = _uiState

    init {
        viewModelScope.launch {
            scheduleRepository.revision.drop(1).collect {
                refreshEntries()
            }
        }
    }

    fun backToToday() {
        val today = LocalDate.now()
        setMonth(today.year, today.monthValue)
    }

    fun previousMonth() {
        val current = _uiState.value
        if (current.month == 1) setMonth(current.year - 1, 12) else setMonth(current.year, current.month - 1)
    }

    fun nextMonth() {
        val current = _uiState.value
        if (current.month == 12) setMonth(current.year + 1, 1) else setMonth(current.year, current.month + 1)
    }

    fun addSchedule(
        title: String,
        day: Int,
        note: String,
        timeText: String = "",
        repeatRule: String = "",
        repeatInterval: Int = 1,
        repeatEndDate: String = "",
    ): String {
        val current = _uiState.value
        val repeatGroupId = if (repeatRule.isNotBlank()) java.util.UUID.randomUUID().toString() else ""
        val entry = scheduleRepository.addEntry(
            title = title,
            year = current.year,
            month = current.month,
            day = day,
            note = note,
            timeText = timeText,
            repeatRule = repeatRule,
            repeatInterval = repeatInterval,
            repeatEndDate = normalizeRepeatEndDate(repeatEndDate),
            repeatGroupId = repeatGroupId,
        )
        expandRepeatingEntry(entry)
        refreshEntries()
        return entry.id
    }

    fun updateTheme(text: String) {
        val current = _uiState.value
        store.setCalendarTheme(current.year, current.month, text)
        _uiState.update { it.copy(theme = text) }
    }

    fun removeSchedule(id: String) {
        val updated = scheduleRepository.entries().filterNot { it.id == id }
        scheduleRepository.saveEntries(updated)
        refreshEntries()
    }

    fun toggleScheduleCompleted(id: String) {
        val updated = scheduleRepository.entries().map { entry ->
            if (entry.id == id) {
                entry.withStatus(if (entry.status == ScheduleStatus.DONE) ScheduleStatus.PLANNED else ScheduleStatus.DONE)
            } else {
                entry
            }
        }
        scheduleRepository.saveEntries(updated)
        refreshEntries()
    }

    fun updateSchedule(
        id: String,
        title: String,
        day: Int,
        note: String,
        timeText: String? = null,
        repeatRule: String? = null,
        repeatInterval: Int? = null,
        repeatEndDate: String? = null,
        applySeries: Boolean = false,
    ) {
        val current = _uiState.value
        val all = scheduleRepository.entries()
        val target = all.firstOrNull { it.id == id }
        val targetGroupId = target?.repeatGroupId.orEmpty()
        val updated = all.map { entry ->
            if (entry.id == id) {
                entry.copy(
                    title = title.trim(),
                    year = current.year,
                    month = current.month,
                    day = day,
                    note = note.trim(),
                    timeText = timeText?.trim() ?: entry.timeText,
                    repeatRule = repeatRule ?: entry.repeatRule,
                    repeatInterval = repeatInterval ?: entry.repeatInterval,
                    repeatEndDate = repeatEndDate?.let(::normalizeRepeatEndDate) ?: entry.repeatEndDate,
                )
            } else if (applySeries && targetGroupId.isNotBlank() && entry.repeatGroupId == targetGroupId) {
                entry.copy(
                    title = title.trim(),
                    note = note.trim(),
                    timeText = timeText?.trim() ?: entry.timeText,
                    repeatRule = repeatRule ?: entry.repeatRule,
                    repeatInterval = repeatInterval ?: entry.repeatInterval,
                    repeatEndDate = repeatEndDate?.let(::normalizeRepeatEndDate) ?: entry.repeatEndDate,
                )
            } else {
                entry
            }
        }
        scheduleRepository.saveEntries(updated)
        refreshEntries()
    }

    fun importSystemCalendarEvents(events: List<CalendarImportCandidate>): Int {
        val current = _uiState.value
        val existing = scheduleRepository.entries()
        val maxDay = YearMonth.of(current.year, current.month).lengthOfMonth()
        val additions = events
            .mapNotNull { event ->
                val title = event.title.trim()
                if (title.isBlank()) {
                    null
                } else {
                    ScheduleEntry(
                        id = java.util.UUID.randomUUID().toString(),
                        title = title,
                        year = current.year,
                        month = current.month,
                        day = event.day.coerceIn(1, maxDay),
                        note = event.note.trim(),
                        timeText = event.timeText.trim(),
                    )
                }
            }
            .distinctBy { "${it.title}|${it.year}|${it.month}|${it.day}|${it.timeText}" }
            .filterNot { candidate ->
                existing.any { saved ->
                    saved.title == candidate.title &&
                        saved.year == candidate.year &&
                        saved.month == candidate.month &&
                        saved.day == candidate.day &&
                        saved.timeText == candidate.timeText
                }
            }
        if (additions.isEmpty()) return 0
        scheduleRepository.saveEntries(existing + additions)
        refreshEntries()
        return additions.size
    }

    fun moveScheduleToDay(id: String, day: Int) {
        val current = _uiState.value
        val clampedDay = day.coerceAtLeast(1)
        val updated = scheduleRepository.entries().map { entry ->
            if (entry.id == id) {
                entry.copy(
                    year = current.year,
                    month = current.month,
                    day = clampedDay,
                )
            } else {
                entry
            }
        }
        scheduleRepository.saveEntries(updated)
        refreshEntries()
    }

    fun reorderScheduleInDay(id: String, moveUp: Boolean) {
        val all = scheduleRepository.entries().toMutableList()
        val currentIndex = all.indexOfFirst { it.id == id }
        if (currentIndex < 0) return
        val target = all[currentIndex]
        val dayIndexes = all.withIndex()
            .filter { (_, entry) ->
                entry.year == target.year && entry.month == target.month && entry.day == target.day
            }
            .map { it.index }
        val dayPos = dayIndexes.indexOf(currentIndex)
        if (dayPos < 0) return
        val swapPos = if (moveUp) dayPos - 1 else dayPos + 1
        if (swapPos !in dayIndexes.indices) return
        val swapIndex = dayIndexes[swapPos]
        val temp = all[currentIndex]
        all[currentIndex] = all[swapIndex]
        all[swapIndex] = temp
        scheduleRepository.saveEntries(all)
        refreshEntries()
    }

    private fun setMonth(year: Int, month: Int) {
        store.setCalendarAnchor(year, month)
        _uiState.update {
            it.copy(
                year = year,
                month = month,
                entries = monthEntries(year, month),
                theme = store.calendarTheme(year, month),
            )
        }
    }

    private fun refreshEntries() {
        val current = _uiState.value
        _uiState.update { it.copy(entries = monthEntries(current.year, current.month)) }
    }

    private fun expandRepeatingEntry(entry: ScheduleEntry) {
        val interval = entry.repeatInterval.coerceAtLeast(1)
        val startDate = LocalDate.of(entry.year, entry.month, entry.day)
        val explicitEndDate = parseRepeatEndDate(entry.repeatEndDate)?.takeIf { !it.isBefore(startDate) }
        val additions = when (entry.repeatRule) {
            "daily" -> {
                val fallbackEndDate = YearMonth.of(entry.year, entry.month).atEndOfMonth()
                val endDate = explicitEndDate ?: fallbackEndDate
                generateSequence(startDate.plusDays(interval.toLong())) { it.plusDays(interval.toLong()) }
                    .takeWhile { !it.isAfter(endDate) }
                    .take(365)
                    .map { date ->
                        entry.copy(
                            id = java.util.UUID.randomUUID().toString(),
                            year = date.year,
                            month = date.monthValue,
                            day = date.dayOfMonth,
                            completed = false,
                        )
                    }
                    .toList()
            }
            "weekly" -> {
                val fallbackEndDate = YearMonth.of(entry.year, entry.month).atEndOfMonth()
                val endDate = explicitEndDate ?: fallbackEndDate
                generateSequence(startDate.plusWeeks(interval.toLong())) { it.plusWeeks(interval.toLong()) }
                    .takeWhile { !it.isAfter(endDate) }
                    .take(365)
                    .map { date ->
                        entry.copy(
                            id = java.util.UUID.randomUUID().toString(),
                            year = date.year,
                            month = date.monthValue,
                            day = date.dayOfMonth,
                            completed = false,
                        )
                    }
                    .toList()
            }
            "monthly" -> {
                val monthStep = interval.toLong()
                val startMonth = YearMonth.of(entry.year, entry.month)
                val maxItems = if (explicitEndDate == null) 11 else 36
                generateSequence(startMonth.plusMonths(monthStep)) { it.plusMonths(monthStep) }
                    .take(maxItems)
                    .mapNotNull { month ->
                        if (entry.day <= month.lengthOfMonth()) {
                            val date = month.atDay(entry.day)
                            if (explicitEndDate != null && date.isAfter(explicitEndDate)) return@mapNotNull null
                            entry.copy(
                                id = java.util.UUID.randomUUID().toString(),
                                year = month.year,
                                month = month.monthValue,
                                completed = false,
                            )
                        } else {
                            null
                        }
                    }
                    .toList()
            }
            else -> emptyList()
        }
        if (additions.isEmpty()) return
        val existing = scheduleRepository.entries()
        val uniqueAdditions = additions.filterNot { candidate ->
            existing.any { saved ->
                saved.title == candidate.title &&
                    saved.year == candidate.year &&
                    saved.month == candidate.month &&
                    saved.day == candidate.day &&
                    saved.timeText == candidate.timeText
            }
        }
        if (uniqueAdditions.isNotEmpty()) {
            scheduleRepository.saveEntries(existing + uniqueAdditions)
        }
    }

    private fun normalizeRepeatEndDate(value: String): String =
        parseRepeatEndDate(value)?.toString().orEmpty()

    private fun parseRepeatEndDate(value: String): LocalDate? =
        runCatching { LocalDate.parse(value.trim()) }.getOrNull()

    private fun monthEntries(year: Int, month: Int): List<ScheduleEntry> =
        scheduleRepository.entries()
            .filter { it.year == year && it.month == month }
            .sortedWith(compareBy<ScheduleEntry>({ it.day }, { it.timeText }, { it.completed }, { it.title.lowercase() }))

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val store = LocalStateStore(MMKV.defaultMMKV())
                val scheduleRepository = ScheduleRepository.getInstance(store)
                @Suppress("UNCHECKED_CAST")
                return CalendarViewModel(store, scheduleRepository) as T
            }
        }
    }
}
