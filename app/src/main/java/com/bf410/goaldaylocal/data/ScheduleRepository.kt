package com.bf410.goaldaylocal.data

import com.bf410.goaldaylocal.GoaldayApplication
import com.bf410.goaldaylocal.ui.widget.WidgetRefresh
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ScheduleRepository private constructor(
    private val store: LocalStateStore,
) {
    private val _revision = MutableStateFlow(0)
    val revision: StateFlow<Int> = _revision

    fun entries(): List<ScheduleEntry> = store.scheduleEntries()

    fun saveEntries(entries: List<ScheduleEntry>) {
        store.saveScheduleEntries(entries)
        notifyChanged()
        GoaldayApplication.appContext?.let(WidgetRefresh::refreshScheduleWidgets)
    }

    fun addEntry(
        title: String,
        year: Int,
        month: Int,
        day: Int,
        note: String = "",
        timeText: String = "",
        repeatRule: String = "",
        repeatInterval: Int = 1,
        repeatGroupId: String = "",
        status: ScheduleStatus = ScheduleStatus.PLANNED,
    ): ScheduleEntry {
        val entry = ScheduleEntry(
            id = UUID.randomUUID().toString(),
            title = title,
            year = year,
            month = month,
            day = day,
            note = note,
            timeText = timeText,
            repeatRule = repeatRule,
            repeatInterval = repeatInterval.coerceAtLeast(1),
            repeatGroupId = repeatGroupId,
            completed = status == ScheduleStatus.DONE,
        )
        saveEntries(entries() + entry)
        return entry
    }

    private fun notifyChanged() {
        _revision.value += 1
    }

    companion object {
        @Volatile
        private var instance: ScheduleRepository? = null

        fun getInstance(store: LocalStateStore): ScheduleRepository =
            instance ?: synchronized(this) {
                instance ?: ScheduleRepository(store).also { instance = it }
            }
    }
}
