package com.bf410.goaldaylocal.ui.book

import org.junit.Assert.assertEquals
import org.junit.Test

class DiaryImageDecodeTest {
    @Test
    fun sample_size_is_power_of_two_covering_max_side() {
        assertEquals(1, sampleSizeForBounds(800, 600, 2160))
        assertEquals(2, sampleSizeForBounds(4000, 3000, 2160))
        assertEquals(4, sampleSizeForBounds(5000, 4000, 1440))
        assertEquals(1, sampleSizeForBounds(1440, 1000, 1440))
    }

    @Test
    fun sample_size_guards_invalid_input() {
        assertEquals(1, sampleSizeForBounds(0, 0, 1440))
        assertEquals(1, sampleSizeForBounds(800, 600, 0))
    }
}
