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

private const val TURN_DISTANCE_THRESHOLD = 0.28f
private const val TURN_FLING_THRESHOLD = 820f
private const val BOUNDARY_RESISTANCE_FACTOR = 0.22f
private const val OPPOSING_VELOCITY_THRESHOLD = 500f
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

    val distanceThreshold = if (profile == TurnProfile.HANDBOOK) 0.20f else TURN_DISTANCE_THRESHOLD
    val flingThreshold = if (profile == TurnProfile.HANDBOOK) 560f else TURN_FLING_THRESHOLD
    val opposingThreshold = if (profile == TurnProfile.HANDBOOK) 300f else OPPOSING_VELOCITY_THRESHOLD
    val progressPasses = progress >= distanceThreshold
    val nearCommitProgress = if (profile == TurnProfile.HANDBOOK) progress >= 0.16f else progress >= 0.2f
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
        progressPasses || velocityPasses || (nearCommitProgress && !opposingVelocity) -> {
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
    if (profile == TurnProfile.HANDBOOK) 0.20f else EDGE_TAP_START_PROGRESS

fun resolveDragTurnDirection(
    profile: TurnProfile,
    dragStartX: Float,
    pageWidthPx: Float,
    dragAmountPx: Float,
    edgeGestureRatio: Float,
    dragStartThreshold: Float,
): TurnDirection? {
    val safeWidth = pageWidthPx.coerceAtLeast(1f)
    val edgeZonePx = safeWidth * edgeGestureRatio.coerceIn(0f, 0.5f)
    val canStartNextFromEdge = dragStartX >= safeWidth - edgeZonePx
    val canStartPreviousFromEdge = dragStartX <= edgeZonePx
    // P0-1 修复：HANDBOOK 模式�?dragStartThreshold 现在是比例值（0.04f），需乘以页面宽度得到 px 阈�?
    // 原代码用 0.28f 作为 px 值（极小），导致任何微动都触发翻页方向判定，抢占长按拖放手势
    // DEFAULT 模式仍用 0.6f 作为 px 值（边缘热区窄，敏感度可接受�?
    val threshold = if (profile == TurnProfile.HANDBOOK) {
        dragStartThreshold.coerceIn(0.02f, 0.18f).let { ratio -> (safeWidth * ratio).coerceIn(8f, 56f) }
    } else {
        0.6f
    }
    return when {
        dragAmountPx <= -threshold && canStartNextFromEdge -> TurnDirection.NEXT
        dragAmountPx >= threshold && canStartPreviousFromEdge -> TurnDirection.PREVIOUS
        else -> null
    }
}

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
    val raw = currentProgress + directionalDelta
    return applyBoundaryResistance(raw, canTurn)
}

fun turnTransformOriginX(profile: TurnProfile, direction: TurnDirection?): Float {
    // HANDBOOK 也围绕书脊（内页边缘）旋转，才像真实书页翻越；
    // 绕中心旋转会变成“卡片翻转”，不像书本。
    return if (direction == TurnDirection.NEXT) 0f else 1f
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
    return (clamped * 0.08f + curveA * 0.30f + curveB * 0.42f + committedCurve * 0.40f).coerceIn(0f, 1f)
}

fun destinationRevealAlpha(progress: Float, profile: TurnProfile = TurnProfile.DEFAULT): Float {
    val emphasized = visualTurnProgress(progress, profile).coerceIn(0f, 1f)
    return (0.05f + emphasized * 0.95f).coerceIn(0.05f, 1f)
}
