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
    val repeatInterval: Int = 1,
    val repeatEndDate: String = "",
    val repeatGroupId: String = "",
    val completed: Boolean = false,
    /** 清单专题色（ARGB），有值=专题关联条目；null=普通条目 */
    val colorArgb: Int? = null,
) {
    val status: ScheduleStatus
        get() = if (completed) ScheduleStatus.DONE else ScheduleStatus.PLANNED

    fun withStatus(status: ScheduleStatus): ScheduleEntry =
        copy(completed = status == ScheduleStatus.DONE)
}
