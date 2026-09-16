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
}
