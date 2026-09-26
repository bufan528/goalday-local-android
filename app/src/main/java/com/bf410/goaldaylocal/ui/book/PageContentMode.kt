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
    // 空串也算没点中：否则进 EditingChecklistItem("", "")，改名判空直接返回退不出编辑
    is TargetPage -> tappedItem?.takeIf { it.isNotBlank() }?.let { PageContentMode.EditingChecklistItem(page.title, it) } ?: PageContentMode.Browsing
    is SchedulePage -> tappedItem?.takeIf { it.isNotBlank() }?.let { PageContentMode.EditingChecklistItem(page.title, it) } ?: PageContentMode.Browsing
    is PlanPage -> tappedItem?.takeIf { it.isNotBlank() }?.let { PageContentMode.EditingChecklistItem(page.title, it) } ?: PageContentMode.Browsing
}

fun canTurnPage(mode: PageContentMode): Boolean = mode == PageContentMode.Browsing

fun renameDisplayedChecklistItem(
    items: List<String>,
    oldItem: String,
    newItem: String,
): List<String> {
    val trimmed = newItem.trim()
    val normalizedOld = oldItem.trim()
    if (trimmed.isBlank() || normalizedOld.isBlank()) {
        return items
    }
    // 改到已存在的名字上直接合并去重，不留 [C, C] 双条（与 renameExactItemDistinct 同语义）
    return items.map { item ->
        if (item.trim() == normalizedOld) trimmed else item
    }.distinct()
}

fun destinationPageSubtitle(direction: TurnDirection?, fallback: String): String =
    when (direction) {
        TurnDirection.NEXT -> BookStrings.destinationNext
        TurnDirection.PREVIOUS -> BookStrings.destinationPrevious
        null -> fallback
    }

fun condensedPreviewText(text: String, maxLength: Int = 80): String {
    val safeLength = maxLength.coerceAtLeast(1)
    val normalized = text.replace('\n', ' ').trim()
    if (normalized.codePointCount(0, normalized.length) <= safeLength) {
        return normalized
    }
    // 按码点截：substring 按 Char 会把 emoji 代理对切成 �
    val end = normalized.offsetByCodePoints(0, safeLength)
    return normalized.substring(0, end).trimEnd() + "…"
}
