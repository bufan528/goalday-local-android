package com.bf410.goaldaylocal.ui.book

import kotlin.math.abs

enum class TurnDirection {
    NEXT,
    PREVIOUS,
}

enum class TurnReleaseResult {
    CompleteNext,
    CompletePrevious,
    SnapBack,
}

private const val TURN_DISTANCE_THRESHOLD = 0.32f
private const val TURN_FLING_THRESHOLD = 1600f
private const val BOUNDARY_RESISTANCE_FACTOR = 0.28f

fun resolvePageTurnRelease(
    direction: TurnDirection,
    progress: Float,
    velocity: Float,
    hasPreviousPage: Boolean,
    hasNextPage: Boolean,
): TurnReleaseResult {
    val canTurn = when (direction) {
        TurnDirection.NEXT -> hasNextPage
        TurnDirection.PREVIOUS -> hasPreviousPage
    }
    if (!canTurn) {
        return TurnReleaseResult.SnapBack
    }

    val progressPasses = progress >= TURN_DISTANCE_THRESHOLD
    val velocityPasses = when (direction) {
        TurnDirection.NEXT -> velocity <= -TURN_FLING_THRESHOLD
        TurnDirection.PREVIOUS -> velocity >= TURN_FLING_THRESHOLD
    }

    return when {
        progressPasses || velocityPasses -> {
            when (direction) {
                TurnDirection.NEXT -> TurnReleaseResult.CompleteNext
                TurnDirection.PREVIOUS -> TurnReleaseResult.CompletePrevious
            }
        }
        else -> TurnReleaseResult.SnapBack
    }
}

fun applyBoundaryResistance(rawProgress: Float, canTurn: Boolean): Float {
    if (canTurn) {
        return rawProgress.coerceIn(0f, 1f)
    }
    return (abs(rawProgress) * BOUNDARY_RESISTANCE_FACTOR).coerceIn(0f, 0.18f)
}
