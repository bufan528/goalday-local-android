package com.bf410.goaldaylocal.ui.book

import androidx.compose.runtime.Immutable
import com.bf410.goaldaylocal.data.ScheduleEntry
import com.bf410.goaldaylocal.data.TargetItemMeta
import com.bf410.goaldaylocal.data.TopicBook
import java.time.LocalDate

@Immutable
data class OnThisDayDiary(
    val date: LocalDate,
    val yearsAgo: Int,
    val bookTitle: String,
    val pageTitle: String,
    val preview: String,
    val moodTags: String,
)

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
    val schedulePreviewEntries: List<ScheduleEntry>,
    val targetItemMeta: Map<String, TargetItemMeta>,
    val customBookCount: Int,
    val inLibraryMode: Boolean,
    val checkedRevision: Long = 0L,
)
