package com.bf410.goaldaylocal.ui.book

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class DiaryScheduleParserTest {
    @Test
    fun parses_plain_diary_sections_into_schedule_candidates() {
        val raw = """
            # 日期
            2026-06-28
            # 工作任务
            - 推进 A
            # 今日完成
            ✓ 完成 B
        """.trimIndent()

        val candidates = parseDiaryScheduleCandidates(raw, "diary-note")

        assertEquals(2, candidates.size)
        assertEquals(DiaryScheduleCandidate("推进 A", LocalDate.of(2026, 6, 28), false, "diary-note"), candidates[0])
        assertEquals(DiaryScheduleCandidate("完成 B", LocalDate.of(2026, 6, 28), true, "diary-note"), candidates[1])
    }

    @Test
    fun parses_structured_target_blocks_with_escaped_text() {
        val escaped = escapeDiaryScheduleBlockText("✓ 目标 A|带竖线\n第二行")
        val raw = """
            # 日期
            2026-06-28
            # 日记块
            target|check|$escaped
            topic_target|body|○ 主题目标
            image|body|content://local/image
        """.trimIndent()

        val candidates = parseDiaryScheduleCandidates(raw, "diary-note")

        assertEquals(2, candidates.size)
        assertEquals(DiaryScheduleCandidate("目标 A|带竖线", LocalDate.of(2026, 6, 28), true, "diary-note"), candidates[0])
        assertEquals(DiaryScheduleCandidate("主题目标", LocalDate.of(2026, 6, 28), false, "diary-note"), candidates[1])
    }

    @Test
    fun invalid_or_missing_date_uses_supplied_today() {
        val raw = """
            # 日期
            not-a-date
            # 待办
            [ ] 收尾
        """.trimIndent()

        val candidates = parseDiaryScheduleCandidates(raw, "diary-note", today = LocalDate.of(2026, 7, 2))

        assertEquals(listOf(DiaryScheduleCandidate("收尾", LocalDate.of(2026, 7, 2), false, "diary-note")), candidates)
    }
}
