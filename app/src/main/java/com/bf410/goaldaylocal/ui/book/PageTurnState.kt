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

enum class TurnProfile {
    DEFAULT,
    HANDBOOK,
}

private const val TURN_DISTANCE_THRESHOLD = 0.3f
private const val TURN_FLING_THRESHOLD = 860f
private const val BOUNDARY_RESISTANCE_FACTOR = 0.22f
private const val OPPOSING_VELOCITY_THRESHOLD = 520f
private const val EDGE_TAP_START_PROGRESS = 0.3f

fun resolvePageTurnRelease(
    direction: TurnDirection,
    progress: Float,
    velocity: Float,
    hasPreviousPage: Boolean,
    hasNextPage: Boolean,
    profile: TurnProfile = TurnProfile.DEFAULT,
): TurnReleaseResult {
    val canTurn = when (direction) {
        TurnDirection.NEXT -> hasNextPage
        TurnDirection.PREVIOUS -> hasPreviousPage
    }
    if (!canTurn) {
        return TurnReleaseResult.SnapBack
    }

    val distanceThreshold = if (profile == TurnProfile.HANDBOOK) 0.26f else TURN_DISTANCE_THRESHOLD
    val flingThreshold = if (profile == TurnProfile.HANDBOOK) 620f else TURN_FLING_THRESHOLD
    val opposingThreshold = if (profile == TurnProfile.HANDBOOK) 360f else OPPOSING_VELOCITY_THRESHOLD
    val progressPasses = progress >= distanceThreshold
    val velocityPasses = when (direction) {
        TurnDirection.NEXT -> velocity <= -flingThreshold
        TurnDirection.PREVIOUS -> velocity >= flingThreshold
    }
    val opposingVelocity = when (direction) {
        TurnDirection.NEXT -> velocity >= opposingThreshold
        TurnDirection.PREVIOUS -> velocity <= -opposingThreshold
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

fun initialEdgeTapProgress(profile: TurnProfile = TurnProfile.DEFAULT): Float =
    if (profile == TurnProfile.HANDBOOK) 0.32f else EDGE_TAP_START_PROGRESS

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
    val curveA = clamped * clamped * (3f - 2f * clamped)
    val curveB = curveA * curveA
    return (clamped * 0.28f + curveA * 0.44f + curveB * 0.48f).coerceIn(0f, 1f)
}

fun visualTurnProgress(progress: Float, profile: TurnProfile): Float {
    if (profile == TurnProfile.DEFAULT) return visualTurnProgress(progress)
    val clamped = progress.coerceIn(0f, 1f)
    val curveA = clamped * clamped * (3f - 2f * clamped)
    val curveB = curveA * curveA
    val committedCurve = curveB * curveA
    return (clamped * 0.12f + curveA * 0.36f + curveB * 0.44f + committedCurve * 0.34f).coerceIn(0f, 1f)
}

fun destinationRevealAlpha(progress: Float): Float {
    val emphasized = visualTurnProgress(progress).coerceIn(0f, 1f)
    return (0.05f + emphasized * 0.95f).coerceIn(0.05f, 1f)
}
