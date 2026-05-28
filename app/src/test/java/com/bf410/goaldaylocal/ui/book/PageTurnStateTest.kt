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

    @Test
    fun boundary_resistance_clamps_large_unavailable_drag_more_aggressively() {
        val resisted = applyBoundaryResistance(rawProgress = 0.9f, canTurn = false)

        assertTrue(resisted < 0.18f)
    }

    @Test
    fun opposing_velocity_snaps_back_even_when_progress_is_near_threshold() {
        val result = resolvePageTurnRelease(
            direction = TurnDirection.NEXT,
            progress = 0.30f,
            velocity = 900f,
            hasPreviousPage = true,
            hasNextPage = true,
        )

        assertEquals(TurnReleaseResult.SnapBack, result)
    }

    @Test
    fun edge_tap_progress_starts_in_turning_range() {
        val progress = initialEdgeTapProgress()

        assertTrue(progress in 0.18f..0.35f)
    }
}
