package com.bf410.goaldaylocal.ui.main

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * 对照原版 DiaryScrollAdapter（5 页窗口，中心页=2，date = center + (position - 2)）。
 */
class DiaryPagerLogicTest {

    private val anchor = LocalDate.of(2026, 9, 16)

    @Test
    fun center_page_maps_to_anchor() {
        assertEquals(anchor, diaryPagerDate(anchor, 2))
    }

    @Test
    fun window_offsets_map_to_adjacent_days() {
        assertEquals(LocalDate.of(2026, 9, 14), diaryPagerDate(anchor, 0))
        assertEquals(LocalDate.of(2026, 9, 15), diaryPagerDate(anchor, 1))
        assertEquals(LocalDate.of(2026, 9, 17), diaryPagerDate(anchor, 3))
        assertEquals(LocalDate.of(2026, 9, 18), diaryPagerDate(anchor, 4))
    }

    @Test
    fun settle_follow_math_matches_adapter() {
        // 落定页=4（+2 天）：锚点跟随，回正后中心页仍是落定日
        val followed = diaryPagerDate(anchor, 4)
        assertEquals(LocalDate.of(2026, 9, 18), followed)
        assertEquals(followed, diaryPagerDate(followed, DIARY_PAGER_CENTER))
    }

    @Test
    fun window_constants_match_original() {
        assertEquals(5, DIARY_PAGER_SIZE)
        assertEquals(2, DIARY_PAGER_CENTER)
    }
}
