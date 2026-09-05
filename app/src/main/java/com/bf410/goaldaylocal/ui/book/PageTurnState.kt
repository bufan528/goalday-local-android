package com.bf410.goaldaylocal.ui.book

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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

/**
 * 翻页配置：把原先散落在各处的 `if (profile == TurnProfile.HANDBOOK)` 分支
 * 统一收敛到这里，避免每加一个分支就要全文搜改。
 *
 * 对照原版 BaseBookViewKt 行为：
 * - HANDBOOK：maxRotation=180°、tween 自适应时长(progress>0.5→100ms, ≤0.5→300ms)、
 *   LinearEasing、alpha 90° 硬切、cameraDistance=40×density、shadowElevation=10、
 *   TransformOrigin(0, 0.5f) 始终绕书脊、全宽手势 0.5、线性进度。
 * - DEFAULT：保留原仿真曲线 + spring 回弹，边缘热区窄，带纸张位移/缩放/透视。
 */
class TurnProfile private constructor(
    // —— 释放判定阈值 ——
    // 对照原版 BaseBookViewKt.java L232-253：手账翻页阈值分双态
    //   bookIsOpened=false（书闭合，封面→首页）：阈值 0.5
    //   bookIsOpened=true（书已打开，页→页）：阈值 0.3
    // DEFAULT 模式仍用单一 distanceThreshold（与闭合态一致）。
    val distanceThreshold: Float,
    val bookOpenThreshold: Float,
    val flingThreshold: Float,
    val opposingThreshold: Float,
    val nearCommitProgress: Float,
    val edgeTapStartProgress: Float,
    // —— 是否启用双态阈值（HANDBOOK=true，DEFAULT=false） ——
    val useDualStateThreshold: Boolean,
    // —— 手势热区 ——
    val edgeGestureRatio: Float,
    // —— 变换原点 ——
    val transformOriginAlwaysSpine: Boolean,
    // —— 视觉进度曲线 ——
    val linearVisualProgress: Boolean,
    // —— 旋转 / 相机 / 阴影 ——
    val maxRotation: Float,
    val cameraDistanceDensity: Float,
    val shadowElevation: Float,
    // —— Alpha 硬切 ——
    val alphaHardCut: Boolean,
    // —— 仅绕书脊旋转（无位移/缩放/rotationX） ——
    val useSpineOnlyTransform: Boolean,
    // —— 翻页过程中是否绘制额外边缘阴影 Box ——
    val renderTurnShadow: Boolean,
    val turnShadowWidthBase: Float,
    val turnShadowWidthStep: Float,
    // —— Spine 装饰 ——
    val spineBaseWidthDp: Float,
    val spineSideShadowExtraStepDp: Float,
    val spineCenterHighlightStepDp: Float,
    // —— 回弹动画 ——
    private val forwardSpecFactory: (Float) -> AnimationSpec<Float>,
    private val snapBackSpecFactory: (Float) -> AnimationSpec<Float>,
) {
    /**
     * 翻页完成动画 spec。
     * @param currentProgress 当前进度（0..1），HANDBOOK 模式下用于自适应时长：
     *   progress > 0.5 → 100ms，progress ≤ 0.5 → 300ms（对照原版 $4/$5.java L64）
     */
    fun forwardSettleSpec(currentProgress: Float = 0f): AnimationSpec<Float> = forwardSpecFactory(currentProgress)
    fun snapBackSettleSpec(currentProgress: Float = 0f): AnimationSpec<Float> = snapBackSpecFactory(currentProgress)

    /** TransformOrigin.x：HANDBOOK 始终绕左边（书脊），DEFAULT 双向枢轴。 */
    fun transformOriginX(direction: TurnDirection?): Float =
        if (transformOriginAlwaysSpine) 0f
        else if (direction == TurnDirection.NEXT) 0f else 1f

    /** 视觉进度：HANDBOOK 直接用线性值；DEFAULT 走仿真曲线。 */
    fun visualProgress(progress: Float): Float {
        if (linearVisualProgress) return progress.coerceIn(0f, 1f)
        val clamped = progress.coerceIn(0f, 1f)
        val curveA = clamped * clamped * (3f - 2f * clamped)
        val curveB = curveA * curveA
        return (clamped * 0.28f + curveA * 0.44f + curveB * 0.48f).coerceIn(0f, 1f)
    }

    /** 正面 Alpha：HANDBOOK 在 rawRotation ≤ 90° 时为 1，否则 0；DEFAULT 走渐变。 */
    fun frontAlpha(rawRotation: Float, visualProgress: Float): Float =
        if (alphaHardCut) {
            if (rawRotation <= 90f) 1f else 0f
        } else {
            (1f - visualProgress * 0.08f).coerceIn(0.9f, 1f)
        }

    /** 背面 Alpha：HANDBOOK 在 frontRotation > 90° 时为 1，否则 0；DEFAULT 走渐变。 */
    fun backAlpha(frontRotation: Float, visualProgress: Float): Float =
        if (alphaHardCut) {
            if (frontRotation > 90f) 1f else 0f
        } else {
            (0.85f + visualProgress * 0.13f).coerceIn(0.85f, 0.98f)
        }

    companion object {
        val DEFAULT: TurnProfile = TurnProfile(
            distanceThreshold = 0.28f,
            bookOpenThreshold = 0.28f,
            flingThreshold = 820f,
            opposingThreshold = 500f,
            nearCommitProgress = 0.2f,
            edgeTapStartProgress = 0.3f,
            useDualStateThreshold = false,
            edgeGestureRatio = 0.11f,
            transformOriginAlwaysSpine = false,
            linearVisualProgress = false,
            maxRotation = 118f,
            cameraDistanceDensity = 34f,
            shadowElevation = 28f,
            alphaHardCut = false,
            useSpineOnlyTransform = false,
            renderTurnShadow = true,
            turnShadowWidthBase = 14f,
            turnShadowWidthStep = 104f,
            spineBaseWidthDp = 14f,
            spineSideShadowExtraStepDp = 4f,
            spineCenterHighlightStepDp = 3.5f,
            forwardSpecFactory = { _ -> spring(dampingRatio = 0.9f, stiffness = Spring.StiffnessLow) },
            snapBackSpecFactory = { _ -> spring(dampingRatio = 0.84f, stiffness = Spring.StiffnessMediumLow) },
        )

        val HANDBOOK: TurnProfile = TurnProfile(
            // 对照原版 BaseBookViewKt.java L232-253：
            //   bookIsOpened=false → 阈值 0.5（封面→首页）
            //   bookIsOpened=true  → 阈值 0.3（页→页）
            distanceThreshold = 0.5f,
            bookOpenThreshold = 0.3f,
            flingThreshold = 560f,
            opposingThreshold = 300f,
            nearCommitProgress = 0.16f,
            edgeTapStartProgress = 0.20f,
            useDualStateThreshold = true,
            edgeGestureRatio = 0.5f,
            transformOriginAlwaysSpine = true,
            linearVisualProgress = true,
            maxRotation = 180f,
            cameraDistanceDensity = 40f,
            shadowElevation = 10f,
            alphaHardCut = true,
            useSpineOnlyTransform = true,
            renderTurnShadow = false,
            turnShadowWidthBase = 16f,
            turnShadowWidthStep = 120f,
            spineBaseWidthDp = 12f,
            spineSideShadowExtraStepDp = 0f,
            spineCenterHighlightStepDp = 3.5f,
            // 对照原版 BaseBookViewKt$BaseBookView$4/5.java L64-65：
            // duration = if (progress > 0.5) 100ms else 300ms，easing = LinearEasing
            forwardSpecFactory = { currentProgress ->
                tween(
                    durationMillis = if (currentProgress > 0.5f) 100 else 300,
                    easing = LinearEasing,
                )
            },
            snapBackSpecFactory = { currentProgress ->
                tween(
                    durationMillis = if (currentProgress > 0.5f) 100 else 300,
                    easing = LinearEasing,
                )
            },
        )
    }
}

private const val BOUNDARY_RESISTANCE_FACTOR = 0.22f

fun resolvePageTurnRelease(
    direction: TurnDirection,
    progress: Float,
    velocity: Float,
    hasPreviousPage: Boolean,
    hasNextPage: Boolean,
    profile: TurnProfile = TurnProfile.DEFAULT,
    bookIsOpen: Boolean = false,
): TurnReleaseResult {
    val canTurn = when (direction) {
        TurnDirection.NEXT -> hasNextPage
        TurnDirection.PREVIOUS -> hasPreviousPage
    }
    if (!canTurn) {
        return TurnReleaseResult.SnapBack
    }

    // 对照原版 BaseBookViewKt.java L232-253：
    // useDualStateThreshold=true 时根据 bookIsOpen 选择阈值
    //   false（闭合）→ distanceThreshold（HANDBOOK=0.5）
    //   true （打开）→ bookOpenThreshold（HANDBOOK=0.3）
    val effectiveThreshold = if (profile.useDualStateThreshold) {
        if (bookIsOpen) profile.bookOpenThreshold else profile.distanceThreshold
    } else {
        profile.distanceThreshold
    }
    val progressPasses = progress >= effectiveThreshold
    val velocityPasses = when (direction) {
        TurnDirection.NEXT -> velocity <= -profile.flingThreshold
        TurnDirection.PREVIOUS -> velocity >= profile.flingThreshold
    }
    val opposingVelocity = when (direction) {
        TurnDirection.NEXT -> velocity >= profile.opposingThreshold
        TurnDirection.PREVIOUS -> velocity <= -profile.opposingThreshold
    }

    // 对照原版 BaseBookViewKt.java L232-253：直接按 threshold + fling 判定，
    // 没有 nearCommit 提前触发完成，避免轻滑误翻页。
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
    profile.edgeTapStartProgress

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

fun turnTransformOriginX(profile: TurnProfile, direction: TurnDirection?): Float =
    profile.transformOriginX(direction)

fun destinationRevealAlpha(progress: Float, profile: TurnProfile = TurnProfile.DEFAULT): Float {
    val emphasized = profile.visualProgress(progress).coerceIn(0f, 1f)
    return (0.05f + emphasized * 0.95f).coerceIn(0.05f, 1f)
}
