package com.bf410.goaldaylocal.ui.book

import com.bf410.goaldaylocal.data.BookPage
import com.bf410.goaldaylocal.data.DiaryPage
import com.bf410.goaldaylocal.data.PlanPage
import com.bf410.goaldaylocal.data.ScheduleEntry
import com.bf410.goaldaylocal.data.SchedulePage
import com.bf410.goaldaylocal.data.TargetPage

internal fun pagePreviewText(page: BookPage): String =
    when (page) {
        is TargetPage -> page.items.take(3).joinToString("\n") { "• $it" }.ifBlank { BookStrings.contentEmpty }
        is PlanPage -> page.items.take(3).joinToString("\n") { "• $it" }.ifBlank { BookStrings.contentEmpty }
        is SchedulePage -> page.items.take(3).joinToString("\n") { "• $it" }.ifBlank { BookStrings.contentEmpty }
        is DiaryPage -> page.prompt
    }

internal fun scheduleRepeatLabel(entry: ScheduleEntry): String {
    if (entry.repeatRule.isBlank()) return ""
    val unit = when (entry.repeatRule) {
        "daily" -> "天"
        "weekly" -> "周"
        "monthly" -> "月"
        else -> return ""
    }
    val interval = entry.repeatInterval.coerceAtLeast(1)
    val base = if (interval == 1) "重复" else "每${interval}${unit}"
    return if (entry.repeatEndDate.isBlank()) base else "$base 至${entry.repeatEndDate}"
}
