package com.bf410.goaldaylocal.ui.book

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 对照原版 PrintPage：分区勾选 id/标题、逐天走表成页、预览截断 30、队列 FIFO。
 */
class ExportPlanTest {

    @Test
    fun default_checkables_match_original() {
        val items = defaultExportCheckables()
        assertEquals(2, items.size)
        assertEquals("1", items[0].id)
        assertEquals("周计划", items[0].title)
        assertEquals("2", items[1].id)
        assertEquals("日记", items[1].title)
    }

    @Test
    fun invalid_range_yields_empty() {
        val start = LocalDate.of(2026, 9, 20)
        val end = LocalDate.of(2026, 9, 14)
        assertTrue(buildExportItems(start, end, true, true, previewCap = false).isEmpty())
        assertTrue(buildExportItems(start, end, true, true, previewCap = true).isEmpty())
    }

    @Test
    fun nothing_checked_yields_empty() {
        val start = LocalDate.of(2026, 9, 14)
        val end = LocalDate.of(2026, 9, 20)
        assertTrue(buildExportItems(start, end, false, false, previewCap = false).isEmpty())
    }

    @Test
    fun schedule_only_emits_in_range_mondays() {
        // 9.14 周一 ~ 9.20 周日：仅一个周计划页（对照 DAY_OF_WEEK==2 才成页）
        val items = buildExportItems(
            LocalDate.of(2026, 9, 14), LocalDate.of(2026, 9, 20),
            includeSchedule = true, includeDiary = false, previewCap = false,
        )
        assertEquals(1, items.size)
        assertEquals(ExportPreviewType.SCHEDULE, items[0].type)
        assertEquals(LocalDate.of(2026, 9, 14), items[0].date)
    }

    @Test
    fun diary_only_emits_every_day() {
        // 空日记也成页（渲染为空白模板页，不跳过）
        val items = buildExportItems(
            LocalDate.of(2026, 9, 14), LocalDate.of(2026, 9, 16),
            includeSchedule = false, includeDiary = true, previewCap = false,
        )
        assertEquals(3, items.size)
        assertTrue(items.all { it.type == ExportPreviewType.DIARY })
        assertEquals(LocalDate.of(2026, 9, 15), items[1].date)
    }

    @Test
    fun both_interleaves_schedule_before_diary_on_monday() {
        // 对照原版逐天走表：周一先周计划后日记，其余天日记
        val items = buildExportItems(
            LocalDate.of(2026, 9, 14), LocalDate.of(2026, 9, 15),
            includeSchedule = true, includeDiary = true, previewCap = false,
        )
        assertEquals(3, items.size)
        assertEquals(ExportPreviewType.SCHEDULE, items[0].type)
        assertEquals(LocalDate.of(2026, 9, 14), items[0].date)
        assertEquals(ExportPreviewType.DIARY, items[1].type)
        assertEquals(ExportPreviewType.DIARY, items[2].type)
        assertEquals(LocalDate.of(2026, 9, 15), items[2].date)
    }

    @Test
    fun preview_caps_at_30_full_list_does_not() {
        val start = LocalDate.of(2026, 1, 1)
        val end = LocalDate.of(2026, 12, 31)
        val preview = buildExportItems(start, end, true, true, previewCap = true)
        assertEquals(EXPORT_PREVIEW_CAP, preview.size)
        val full = buildExportItems(start, end, true, true, previewCap = false)
        assertTrue(full.size > EXPORT_PREVIEW_CAP)
        // 全年：52 个周一（2026 共 52 个周一）+ 365 天日记
        assertEquals(52 + 365, full.size)
    }

    @Test
    fun preview_titles_readable() {
        val diary = ExportPreviewItem(LocalDate.of(2026, 9, 16), ExportPreviewType.DIARY)
        assertEquals("9月16日 周三", diary.title)
        val sched = ExportPreviewItem(LocalDate.of(2026, 9, 14), ExportPreviewType.SCHEDULE)
        assertEquals("9月14日那周", sched.title)
        assertEquals(LocalDate.of(2026, 9, 14), sched.weekMonday())
        // 周三所在的周一是 9.14（页内渲染 Mon-Sun 整周）
        val mid = ExportPreviewItem(LocalDate.of(2026, 9, 16), ExportPreviewType.SCHEDULE)
        assertEquals(LocalDate.of(2026, 9, 14), mid.weekMonday())
    }

    @Test
    fun queue_polls_fifo_with_progress() {
        val items = buildExportItems(
            LocalDate.of(2026, 9, 14), LocalDate.of(2026, 9, 15),
            includeSchedule = true, includeDiary = true, previewCap = false,
        )
        val queue = ExportPdfQueue(items)
        assertEquals(3, queue.total)
        assertEquals(0, queue.doneCount)
        assertEquals(items[0], queue.poll())
        queue.markDone()
        assertEquals(1, queue.doneCount)
        assertEquals(2, queue.remaining)
        queue.cancelled = true
        assertNull(queue.poll())
    }
}
