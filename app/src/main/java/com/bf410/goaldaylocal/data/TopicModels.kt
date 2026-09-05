package com.bf410.goaldaylocal.data

import androidx.compose.ui.graphics.Color

data class TopicBook(
    val id: String,
    val title: String,
    val subtitle: String,
    val color: Color,
    val pages: List<BookPage>,
)

data class TargetItemMeta(
    val note: String = "",
    val deadlineDay: Int? = null,
)

sealed interface BookPage {
    val title: String
}

data class TargetPage(
    override val title: String,
    val items: List<String>,
) : BookPage

data class SchedulePage(
    override val title: String,
    val items: List<String>,
) : BookPage

data class DiaryPage(
    override val title: String,
    val prompt: String,
) : BookPage

data class PlanItem(
    val title: String,
    val timeText: String = "",
)

data class PlanPage(
    override val title: String,
    val items: List<String>,
    val planItems: List<PlanItem> = items.map { PlanItem(it) },
) : BookPage
