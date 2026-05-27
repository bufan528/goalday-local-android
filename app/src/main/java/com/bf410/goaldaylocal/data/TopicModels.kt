package com.bf410.goaldaylocal.data

import androidx.compose.ui.graphics.Color

data class TopicBook(
    val id: String,
    val title: String,
    val subtitle: String,
    val color: Color,
    val pages: List<BookPage>,
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

data class PlanPage(
    override val title: String,
    val items: List<String>,
) : BookPage
