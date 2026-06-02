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
    ): String {
        val current = _uiState.value
        val entry = scheduleRepository.addEntry(
            title = title,
            year = current.year,
            month = current.month,
            day = day,
            note = note,
            timeText = timeText,
            repeatRule = repeatRule,
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
    ) {
        val current = _uiState.value
        val updated = scheduleRepository.entries().map { entry ->
            if (entry.id == id) {
                entry.copy(
                    title = title.trim(),
                    year = current.year,
                    month = current.month,
                    day = day,
                    note = note.trim(),
                    timeText = timeText?.trim() ?: entry.timeText,
                    repeatRule = repeatRule ?: entry.repeatRule,
                )
            } else {
                entry
            }
        }
        scheduleRepository.saveEntries(updated)
        refreshEntries()
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
        val additions = when (entry.repeatRule) {
            "daily" -> {
                val maxDay = YearMonth.of(entry.year, entry.month).lengthOfMonth()
                ((entry.day + 1)..maxDay).map { day ->
                    entry.copy(id = java.util.UUID.randomUUID().toString(), day = day, completed = false)
                }
            }
            "weekly" -> {
                val maxDay = YearMonth.of(entry.year, entry.month).lengthOfMonth()
                generateSequence(entry.day + 7) { it + 7 }
                    .takeWhile { it <= maxDay }
                    .map { day -> entry.copy(id = java.util.UUID.randomUUID().toString(), day = day, completed = false) }
                    .toList()
            }
            "monthly" -> {
                generateSequence(YearMonth.of(entry.year, entry.month).plusMonths(1)) { it.plusMonths(1) }
                    .take(11)
                    .mapNotNull { month ->
                        if (entry.day <= month.lengthOfMonth()) {
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
