package com.bf410.goaldaylocal.ui.book

import com.bf410.goaldaylocal.data.DiaryPage
import com.bf410.goaldaylocal.data.PlanPage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PageContentModeTest {
    @Test
    fun diary_page_tap_enters_diary_edit_mode() {
        val result = pageContentModeForTap(DiaryPage(title = "Diary", prompt = "Prompt"))

        assertEquals(PageContentMode.EditingDiary(title = "Diary"), result)
    }

    @Test
    fun checklist_page_without_item_tap_stays_in_browsing_mode() {
        val result = pageContentModeForTap(PlanPage(title = "Plan", items = listOf("A", "B")))

        assertEquals(PageContentMode.Browsing, result)
    }

    @Test
    fun checklist_page_item_tap_enters_single_item_edit_mode() {
        val result = pageContentModeForTap(
            page = PlanPage(title = "Plan", items = listOf("A", "B")),
            tappedItem = "B",
        )

        assertEquals(
            PageContentMode.EditingChecklistItem(title = "Plan", item = "B"),
            result,
        )
    }

    @Test
    fun turning_is_disabled_while_editing() {
        assertTrue(canTurnPage(PageContentMode.Browsing))
        assertFalse(canTurnPage(PageContentMode.EditingDiary(title = "Diary")))
        assertFalse(canTurnPage(PageContentMode.EditingChecklistItem(title = "Plan", item = "A")))
    }

    @Test
    fun renaming_displayed_checklist_item_updates_matching_entry() {
        val result = renameDisplayedChecklistItem(
            items = listOf("A", "B", "C"),
            oldItem = "B",
            newItem = "B edited",
        )

        assertEquals(listOf("A", "B edited", "C"), result)
    }

    @Test
    fun renaming_displayed_checklist_item_ignores_blank_replacement() {
        val result = renameDisplayedChecklistItem(
            items = listOf("A", "B", "C"),
            oldItem = "B",
            newItem = "   ",
        )

        assertEquals(listOf("A", "B", "C"), result)
    }
}
