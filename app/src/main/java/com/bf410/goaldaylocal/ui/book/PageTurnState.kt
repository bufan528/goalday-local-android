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
private const val OPPOSING_VELOCITY_THRESHOLD = 600f
private const val EDGE_TAP_START_PROGRESS = 0.24f

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
    val opposingVelocity = when (direction) {
        TurnDirection.NEXT -> velocity >= OPPOSING_VELOCITY_THRESHOLD
        TurnDirection.PREVIOUS -> velocity <= -OPPOSING_VELOCITY_THRESHOLD
    }

    return when {
        opposingVelocity -> TurnReleaseResult.SnapBack
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
    val distance = abs(rawProgress)
    val curved = (distance * BOUNDARY_RESISTANCE_FACTOR) / (1f + distance * 0.9f)
    return curved.coerceIn(0f, 0.17f)
}

fun initialEdgeTapProgress(): Float = EDGE_TAP_START_PROGRESS

fun visualTurnProgress(progress: Float): Float {
    val clamped = progress.coerceIn(0f, 1f)
    val lateLift = clamped * clamped * (3f - 2f * clamped)
    return (clamped * 0.55f + lateLift * 0.65f).coerceIn(0f, 1f)
}
