package com.bf410.goaldaylocal.ui.book

import androidx.compose.runtime.Immutable
import com.bf410.goaldaylocal.data.TopicBook

@Immutable
data class BookUiState(
    val books: List<TopicBook>,
    val selectedBookIndex: Int,
    val selectedPageIndex: Int,
    val savedBookIds: Set<String>,
    val diaryDraft: String,
    val customPageItems: List<String>,
    val weeklyTheme: String,
    val todayPlanItems: List<String>,
    val todayCompletedItems: List<String>,
    val customBookCount: Int,
    val inLibraryMode: Boolean,
)
