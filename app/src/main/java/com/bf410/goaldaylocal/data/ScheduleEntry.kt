package com.bf410.goaldaylocal.data

data class ScheduleEntry(
    val id: String,
    val title: String,
    val year: Int,
    val month: Int,
    val day: Int,
    val note: String = "",
    val completed: Boolean = false,
)
