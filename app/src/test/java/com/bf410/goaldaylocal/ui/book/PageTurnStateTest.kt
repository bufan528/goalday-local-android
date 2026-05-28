package com.bf410.goaldaylocal.ui.book

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PageTurnStateTest {
    @Test
    fun resolves_next_turn_when_progress_crosses_threshold() {
        val result = resolvePageTurnRelease(
            direction = TurnDirection.NEXT,
            progress = 0.42f,
            velocity = -120f,
            hasPreviousPage = true,
            hasNextPage = true,
        )

        assertEquals(TurnReleaseResult.CompleteNext, result)
    }

    @Test
    fun resolves_previous_turn_when_velocity_is_fast_enough() {
        val result = resolvePageTurnRelease(
            direction = TurnDirection.PREVIOUS,
            progress = 0.12f,
            velocity = 2100f,
            hasPreviousPage = true,
            hasNextPage = true,
        )

        assertEquals(TurnReleaseResult.CompletePrevious, result)
    }

    @Test
    fun snaps_back_when_thresholds_are_not_met() {
        val result = resolvePageTurnRelease(
            direction = TurnDirection.NEXT,
            progress = 0.18f,
            velocity = -200f,
            hasPreviousPage = true,
            hasNextPage = true,
        )

        assertEquals(TurnReleaseResult.SnapBack, result)
    }

    @Test
    fun blocks_next_turn_when_next_page_does_not_exist() {
        val result = resolvePageTurnRelease(
            direction = TurnDirection.NEXT,
            progress = 0.85f,
            velocity = -3000f,
            hasPreviousPage = true,
            hasNextPage = false,
        )

        assertEquals(TurnReleaseResult.SnapBack, result)
    }

    @Test
    fun applies_resistance_when_dragging_past_book_boundary() {
        val resisted = applyBoundaryResistance(rawProgress = 0.4f, canTurn = false)

        assertTrue(resisted in 0f..0.2f)
    }
}
