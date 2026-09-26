package com.bf410.goaldaylocal.ui.book

import org.junit.Assert.assertEquals
import org.junit.Test

class TargetItemMigrationTest {
    @Test
    fun remove_exact_item_preserves_other_items() {
        assertEquals(listOf("A", "C"), removeExactItem(listOf("A", "B", "C", "B"), "B"))
    }

    @Test
    fun rename_exact_item_deduplicates_destination() {
        assertEquals(listOf("A", "C"), renameExactItemDistinct(listOf("A", "B", "C"), "B", "C"))
    }

    @Test
    fun trim_and_blank_guarded() {
        assertEquals(listOf("A", "C"), removeExactItem(listOf("A", " B ", "C"), "B"))
        assertEquals(listOf("A", "B"), removeExactItem(listOf("A", "B"), "  "))
        assertEquals(listOf("A", "C"), renameExactItemDistinct(listOf("A", " B ", "C"), "B", "C "))
        assertEquals(listOf("A", "B"), renameExactItemDistinct(listOf("A", "B"), "B", "  "))
    }
}
