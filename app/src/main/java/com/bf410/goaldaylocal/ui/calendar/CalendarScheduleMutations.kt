package com.bf410.goaldaylocal.ui.calendar

import com.bf410.goaldaylocal.data.ScheduleEntry

internal fun removeScheduleEntries(
    entries: List<ScheduleEntry>,
    id: String,
    applySeries: Boolean = false,
): List<ScheduleEntry> {
    val target = entries.firstOrNull { it.id == id } ?: return entries
    val groupId = target.repeatGroupId.takeIf { it.isNotBlank() }
    return if (applySeries && groupId != null) {
        entries.filterNot { it.repeatGroupId == groupId }
    } else {
        entries.filterNot { it.id == id }
    }
}
