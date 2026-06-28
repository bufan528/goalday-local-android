package com.bf410.goaldaylocal.ui.book

import com.bf410.goaldaylocal.data.BookPage
import com.bf410.goaldaylocal.data.DiaryPage
import com.bf410.goaldaylocal.data.PlanPage
import com.bf410.goaldaylocal.data.SchedulePage
import com.bf410.goaldaylocal.data.TargetPage
import com.bf410.goaldaylocal.data.TopicBook

internal data class BookPageMetrics(
    val pageCount: Int,
    val itemCount: Int,
    val diaryCount: Int,
    val scheduleCount: Int,
    val targetCount: Int,
)

internal fun pageItemCount(page: BookPage): Int =
    when (page) {
        is DiaryPage -> 1
        is TargetPage -> page.items.size
        is SchedulePage -> page.items.size
        is PlanPage -> page.items.size
    }

internal fun metricsForPages(pages: List<BookPage>): BookPageMetrics =
    BookPageMetrics(
        pageCount = pages.size,
        itemCount = pages.sumOf(::pageItemCount),
        diaryCount = pages.count { it is DiaryPage },
        scheduleCount = pages.count { it is SchedulePage || it is PlanPage },
        targetCount = pages.count { it is TargetPage },
    )

internal fun metricsForBook(book: TopicBook): BookPageMetrics =
    metricsForPages(book.pages)
