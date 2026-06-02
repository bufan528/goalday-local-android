package com.bf410.goaldaylocal.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.bf410.goaldaylocal.data.LocalStateStore
import com.bf410.goaldaylocal.data.ScheduleEntry
import com.bf410.goaldaylocal.data.ScheduleStatus
import com.tencent.mmkv.MMKV
import java.time.LocalDate
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

data class CalendarUiState(
    val year: Int,
    val month: Int,
    val entries: List<ScheduleEntry>,
    val theme: String,
)

class CalendarViewModel(
    private val store: LocalStateStore,
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

    fun addSchedule(title: String, day: Int, note: String): String {
        val current = _uiState.value
        val entry = ScheduleEntry(
            id = UUID.randomUUID().toString(),
            title = title,
            year = current.year,
            month = current.month,
            day = day,
            note = note,
        )
        val updated = store.scheduleEntries() + entry
        store.saveScheduleEntries(updated)
        refreshEntries()
        return entry.id
    }

    fun updateTheme(text: String) {
        val current = _uiState.value
        store.setCalendarTheme(current.year, current.month, text)
        _uiState.update { it.copy(theme = text) }
    }

    fun removeSchedule(id: String) {
        val updated = store.scheduleEntries().filterNot { it.id == id }
        store.saveScheduleEntries(updated)
        refreshEntries()
    }

    fun toggleScheduleCompleted(id: String) {
        val updated = store.scheduleEntries().map { entry ->
            if (entry.id == id) {
                entry.withStatus(if (entry.status == ScheduleStatus.DONE) ScheduleStatus.PLANNED else ScheduleStatus.DONE)
            } else {
                entry
            }
        }
        store.saveScheduleEntries(updated)
        refreshEntries()
    }

    fun updateSchedule(id: String, title: String, day: Int, note: String) {
        val current = _uiState.value
        val updated = store.scheduleEntries().map { entry ->
            if (entry.id == id) {
                entry.copy(
                    title = title.trim(),
                    year = current.year,
                    month = current.month,
                    day = day,
                    note = note.trim(),
                )
            } else {
                entry
            }
        }
        store.saveScheduleEntries(updated)
        refreshEntries()
    }

    fun moveScheduleToDay(id: String, day: Int) {
        val current = _uiState.value
        val clampedDay = day.coerceAtLeast(1)
        val updated = store.scheduleEntries().map { entry ->
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
        store.saveScheduleEntries(updated)
        refreshEntries()
    }

    fun reorderScheduleInDay(id: String, moveUp: Boolean) {
        val all = store.scheduleEntries().toMutableList()
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
        store.saveScheduleEntries(all)
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

    private fun monthEntries(year: Int, month: Int): List<ScheduleEntry> =
        store.scheduleEntries()
            .filter { it.year == year && it.month == month }
            .sortedBy { it.day }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val store = LocalStateStore(MMKV.defaultMMKV())
                @Suppress("UNCHECKED_CAST")
                return CalendarViewModel(store) as T
            }
        }
    }
}
