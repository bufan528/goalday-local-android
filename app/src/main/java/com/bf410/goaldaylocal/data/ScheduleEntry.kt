package com.bf410.goaldaylocal.data

enum class ScheduleStatus {
    PLANNED,
    DONE,
}

data class ScheduleEntry(
    val id: String,
    val title: String,
    val year: Int,
    val month: Int,
    val day: Int,
    val note: String = "",
    val timeText: String = "",
    val repeatRule: String = "",
    val completed: Boolean = false,
) {
    val status: ScheduleStatus
        get() = if (completed) ScheduleStatus.DONE else ScheduleStatus.PLANNED

    fun withStatus(status: ScheduleStatus): ScheduleEntry =
        copy(completed = status == ScheduleStatus.DONE)
}
