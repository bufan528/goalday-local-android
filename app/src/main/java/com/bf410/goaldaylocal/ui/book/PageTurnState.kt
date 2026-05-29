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

private const val TURN_DISTANCE_THRESHOLD = 0.42f
private const val TURN_FLING_THRESHOLD = 1650f
private const val BOUNDARY_RESISTANCE_FACTOR = 0.22f
private const val OPPOSING_VELOCITY_THRESHOLD = 600f
private const val EDGE_TAP_START_PROGRESS = 0.22f

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
    val curved = (distance * BOUNDARY_RESISTANCE_FACTOR) / (1f + distance * 1.15f)
    return curved.coerceIn(0f, 0.15f)
}

fun initialEdgeTapProgress(): Float = EDGE_TAP_START_PROGRESS

fun updatedTurnProgress(
    currentProgress: Float,
    direction: TurnDirection,
    dragAmountPx: Float,
    pageWidthPx: Float,
    canTurn: Boolean,
): Float {
    val safeWidth = pageWidthPx.coerceAtLeast(1f)
    val directionalDelta = when (direction) {
        TurnDirection.NEXT -> (-dragAmountPx) / safeWidth
        TurnDirection.PREVIOUS -> dragAmountPx / safeWidth
    }
    val delta = directionalDelta.coerceAtLeast(0f)
    val raw = currentProgress + delta
    return applyBoundaryResistance(raw, canTurn)
}

fun visualTurnProgress(progress: Float): Float {
    val clamped = progress.coerceIn(0f, 1f)
    val lateLift = clamped * clamped * (3f - 2f * clamped)
    val committedLift = lateLift * lateLift
    return (clamped * 0.42f + lateLift * 0.36f + committedLift * 0.38f).coerceIn(0f, 1f)
}

fun destinationRevealAlpha(progress: Float): Float {
    val emphasized = visualTurnProgress(progress).coerceIn(0f, 1f)
    return (0.05f + emphasized * 0.95f).coerceIn(0.05f, 1f)
}
