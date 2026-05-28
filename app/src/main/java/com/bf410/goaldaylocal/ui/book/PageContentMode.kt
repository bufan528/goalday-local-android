package com.bf410.goaldaylocal.ui.book

import com.bf410.goaldaylocal.data.BookPage
import com.bf410.goaldaylocal.data.DiaryPage
import com.bf410.goaldaylocal.data.PlanPage
import com.bf410.goaldaylocal.data.SchedulePage
import com.bf410.goaldaylocal.data.TargetPage

sealed interface PageContentMode {
    data object Browsing : PageContentMode

    data class EditingDiary(
        val title: String,
    ) : PageContentMode

    data class EditingChecklistItem(
        val title: String,
        val item: String,
    ) : PageContentMode
}

fun pageContentModeForTap(
    page: BookPage,
    tappedItem: String? = null,
): PageContentMode = when (page) {
    is DiaryPage -> PageContentMode.EditingDiary(page.title)
    is TargetPage -> tappedItem?.let { PageContentMode.EditingChecklistItem(page.title, it) } ?: PageContentMode.Browsing
    is SchedulePage -> tappedItem?.let { PageContentMode.EditingChecklistItem(page.title, it) } ?: PageContentMode.Browsing
    is PlanPage -> tappedItem?.let { PageContentMode.EditingChecklistItem(page.title, it) } ?: PageContentMode.Browsing
}

fun canTurnPage(mode: PageContentMode): Boolean = mode == PageContentMode.Browsing

fun renameDisplayedChecklistItem(
    items: List<String>,
    oldItem: String,
    newItem: String,
): List<String> {
    val trimmed = newItem.trim()
    if (trimmed.isBlank()) {
        return items
    }
    return items.map { item ->
        if (item == oldItem) trimmed else item
    }
}
