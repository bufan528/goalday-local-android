package com.bf410.goaldaylocal.ui.book

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** 当日灵感提示语：与记录 Tab 共用同一选择器（同一天同一条）。 */
class DiaryPromptsTest {

    @Test
    fun prompt_list_matches_original_count() {
        assertEquals(41, JOURNAL_PROMPTS.size)
        assertTrue(JOURNAL_PROMPTS.all { it.isNotBlank() })
    }

    @Test
    fun same_day_same_prompt() {
        val date = LocalDate.of(2026, 9, 16)
        assertEquals(journalPromptFor(date), journalPromptFor(date))
    }

    @Test
    fun prompt_is_stable_date_mapping() {
        // 记录 Tab 原公式：JOURNAL_PROMPTS[floorMod(epochDay, size)]，搬家后行为不变
        val date = LocalDate.of(2026, 9, 16)
        val expected = JOURNAL_PROMPTS[Math.floorMod(date.toEpochDay().toInt(), JOURNAL_PROMPTS.size)]
        assertEquals(expected, journalPromptFor(date))
    }

    @Test
    fun adjacent_days_rotate() {
        // 相邻两天取模不同即轮换（41 天周期内）
        val a = journalPromptFor(LocalDate.of(2026, 9, 16))
        val b = journalPromptFor(LocalDate.of(2026, 9, 17))
        assertTrue(a.isNotBlank() && b.isNotBlank())
        assertTrue(a != b)
    }

    @Test
    fun rotation_offset_cycles_and_wraps() {
        // 点提示语轮换：offset+1 取下一条，满 41 回绕（floorMod 兜底，offset 只增不减也无妨）
        val date = LocalDate.of(2026, 9, 16)
        val base = journalPromptFor(date, 0)
        assertEquals(JOURNAL_PROMPTS[8], journalPromptFor(date, 1))
        assertEquals(base, journalPromptFor(date, JOURNAL_PROMPTS.size))
        assertEquals(base, journalPromptFor(date, -JOURNAL_PROMPTS.size))
        assertEquals(JOURNAL_PROMPTS[6], journalPromptFor(date, -1))
    }

    @Test
    fun offset_key_is_stable_per_day() {
        assertEquals("diary_prompt_offset_2026-09-16", diaryPromptOffsetKey(LocalDate.of(2026, 9, 16)))
        assertTrue(diaryPromptOffsetKey(LocalDate.of(2026, 9, 16)) != diaryPromptOffsetKey(LocalDate.of(2026, 9, 17)))
    }
}
