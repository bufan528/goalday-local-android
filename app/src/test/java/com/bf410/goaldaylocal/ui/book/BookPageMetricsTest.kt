package com.bf410.goaldaylocal.ui.book

import androidx.compose.ui.graphics.Color
import com.bf410.goaldaylocal.data.DiaryPage
import com.bf410.goaldaylocal.data.PlanPage
import com.bf410.goaldaylocal.data.SchedulePage
import com.bf410.goaldaylocal.data.TargetPage
import com.bf410.goaldaylocal.data.TopicBook
import org.junit.Assert.assertEquals
import org.junit.Test

class BookPageMetricsTest {
    @Test
    fun page_item_count_counts_diary_as_single_entry() {
        assertEquals(1, pageItemCount(DiaryPage("记录", "prompt")))
    }

    @Test
    fun page_item_count_counts_checklist_items() {
        assertEquals(2, pageItemCount(PlanPage("计划", listOf("A", "B"))))
        assertEquals(3, pageItemCount(SchedulePage("日程", listOf("A", "B", "C"))))
        assertEquals(1, pageItemCount(TargetPage("目标", listOf("A"))))
    }

    @Test
    fun metrics_for_pages_summarizes_routes() {
        val metrics = metricsForPages(
            listOf(
                TargetPage("目标", listOf("A", "B")),
                PlanPage("计划", listOf("P")),
                SchedulePage("日程", listOf("S1", "S2")),
                DiaryPage("记录", "prompt"),
            ),
        )

        assertEquals(4, metrics.pageCount)
        assertEquals(6, metrics.itemCount)
        assertEquals(1, metrics.diaryCount)
        assertEquals(2, metrics.scheduleCount)
        assertEquals(1, metrics.targetCount)
    }

    @Test
    fun metrics_for_book_uses_all_pages() {
        val book = TopicBook(
            id = "local",
            title = "本地手账",
            subtitle = "offline",
            color = Color(0xFFE88FAE),
            pages = listOf(
                TargetPage("目标", listOf("A")),
                DiaryPage("记录", "prompt"),
            ),
        )

        val metrics = metricsForBook(book)

        assertEquals(2, metrics.pageCount)
        assertEquals(2, metrics.itemCount)
        assertEquals(1, metrics.diaryCount)
        assertEquals(0, metrics.scheduleCount)
        assertEquals(1, metrics.targetCount)
    }
}
