package com.bf410.goaldaylocal.ui.book

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StructuredDiaryTest {
    @Test
    fun blank_raw_creates_empty_local_diary() {
        val diary = StructuredDiary.fromRaw("")

        assertFalse(diary.hasUserContent)
        assertTrue(diary.dateIso.isNotBlank())
    }

    @Test
    fun raw_round_trip_preserves_sections_blocks_and_rich_html() {
        val diary = StructuredDiary("2026-06-28", "平静", "完成 A", "推进 B", "咖啡", "早点睡", "照片说明", "<b>A|B</b>\nline", "")
            .withTextBlock("第一行\n第二行|带分隔符")
            .withTargetBlock("目标 A", completed = true)

        val restored = StructuredDiary.fromRaw(diary.toRaw())

        assertEquals(LocalDate.of(2026, 6, 28), restored.date)
        assertEquals("平静", restored.moodTags)
        assertEquals("<b>A|B</b>\nline", restored.richHtml)
        assertEquals(2, restored.blocks.size)
        assertEquals("第一行\n第二行|带分隔符", restored.blocks[0].text)
        assertEquals(DiaryBlockType.TARGET, restored.blocks[1].type)
    }

    @Test
    fun image_uri_updates_legacy_notes_and_block_list_together() {
        val diary = StructuredDiary.fromRaw("")
            .withPhotoText("照片描述")
            .withImageUri("content://local/a")

        assertEquals(listOf("content://local/a"), diary.imageUris)
        assertEquals(listOf("content://local/a"), diary.imageBlockUris)
        assertEquals("照片描述", diary.photoText)

        val removed = diary.withoutImageUri("content://local/a")

        assertTrue(removed.imageUris.isEmpty())
        assertTrue(removed.imageBlockUris.isEmpty())
        assertEquals("照片描述", removed.photoText)
    }

    @Test
    fun target_block_can_insert_child_and_move() {
        val diary = StructuredDiary.fromRaw("")
            .withTargetBlock("目标 A", completed = false)
            .withTextBlock("记录")
            .withBlockChild(0)
            .withMovedBlock(2, -1)

        assertEquals(DiaryBlockType.TARGET, diary.blocks[0].type)
        assertEquals(DiaryBlockType.TEXT, diary.blocks[1].type)
        assertEquals(DiaryBlockType.TARGET_CHILD, diary.blocks[2].type)
    }

    @Test
    fun unique_lines_do_not_duplicate_targets() {
        val diary = StructuredDiary.fromRaw("")
            .withCompletedTarget("完成 A")
            .withCompletedTarget("完成 A")
            .withWorkTarget("推进 B")
            .withWorkTarget("推进 B")

        assertEquals("完成 A", diary.todayDone)
        assertEquals("推进 B", diary.workTasks)
    }

    @Test
    fun diary_date_label_uses_chinese_weekday() {
        assertEquals("6月28日 · 周日", diaryDateLabel(LocalDate.of(2026, 6, 28)))
    }

    @Test
    fun body_text_containing_marker_like_line_is_not_cut_into_sections() {
        val raw = "# 日期\n2026-06-28\n# 工作任务\n正文里写 # 今日完成 不算分区\n# 小幸福\n咖啡"
        val diary = StructuredDiary.fromRaw(raw)

        assertEquals("正文里写 # 今日完成 不算分区", diary.workTasks)
        assertEquals("咖啡", diary.smallJoy)
        assertEquals("", diary.todayDone)
    }

    @Test
    fun rich_formatting_detection_only_matches_real_tags() {
        assertTrue(diaryHtmlHasRichFormatting("<p>a</p><p><b>加粗</b></p>"))
        assertTrue(diaryHtmlHasRichFormatting("<blockquote>引用</blockquote>"))
        assertFalse(diaryHtmlHasRichFormatting("<p>纯文本</p><p></p>"))
        assertFalse(diaryHtmlHasRichFormatting("3 < 5 纯文本"))
    }

    @Test
    fun edit_plain_text_keeps_blank_lines_for_stable_round_trip() {
        val displayed = plainTextFromHtmlForEdit("<p>第一行</p><p></p><p>第三行</p>")

        assertEquals("第一行\n\n第三行", displayed)
    }
}
