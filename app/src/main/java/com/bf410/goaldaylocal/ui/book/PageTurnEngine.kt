package com.bf410.goaldaylocal.ui.book

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.abs

private const val EDGE_GESTURE_RATIO = 0.13f
private const val HANDBOOK_EDGE_GESTURE_RATIO = 0.19f
private const val HANDBOOK_DRAG_START_THRESHOLD = 0.45f

sealed interface TurnPhase {
    data object Idle : TurnPhase
    data object DraggingNext : TurnPhase
    data object DraggingPrevious : TurnPhase
    data object SettlingForward : TurnPhase
    data object SettlingBack : TurnPhase
}

@Composable
fun PageTurnEngine(
    modifier: Modifier = Modifier,
    canTurnPrevious: Boolean,
    canTurnNext: Boolean,
    turnEnabled: Boolean,
    onFlipNext: () -> Unit,
    onFlipPrevious: () -> Unit,
    profile: TurnProfile = TurnProfile.DEFAULT,
    shell: @Composable (
        canTurnPrevious: Boolean,
        canTurnNext: Boolean,
        turnEnabled: Boolean,
        onTapPrevious: () -> Unit,
        onTapNext: () -> Unit,
        content: @Composable BoxScope.() -> Unit,
    ) -> Unit,
    destination: @Composable BoxScope.(Float, TurnDirection?) -> Unit,
    pageBack: @Composable BoxScope.(Float, TurnDirection?, Float) -> Unit,
    activePage: @Composable BoxScope.(Float, TurnDirection?, Float) -> Unit,
    spine: @Composable BoxScope.(Float, Boolean) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val progress = remember { Animatable(0f) }
    var direction by remember { mutableStateOf<TurnDirection?>(null) }
    var phase by remember { mutableStateOf<TurnPhase>(TurnPhase.Idle) }
    var pageWidthPx by remember { mutableFloatStateOf(1f) }
    var pageHeightPx by remember { mutableFloatStateOf(1f) }
    var dragStartX by remember { mutableFloatStateOf(0f) }
    var turnAnchorY by remember { mutableFloatStateOf(0.5f) }
    var lastVelocityPxPerSecond by remember { mutableFloatStateOf(0f) }
    var lastEventTimeMs by remember { mutableStateOf(0L) }

    val dragProgress = progress.value.coerceIn(0f, 1f)
    val visualProgress = visualTurnProgress(dragProgress, profile)
    val commitProgress = ((visualProgress - 0.22f) / 0.78f).coerceIn(0f, 1f)
    val latePhase = commitProgress * commitProgress
    val earlyPhase = (1f - commitProgress).coerceIn(0f, 1f)
    val draggingToNext = direction == TurnDirection.NEXT
    val draggingToPrevious = direction == TurnDirection.PREVIOUS
    val turnShadowWidth = if (profile == TurnProfile.HANDBOOK) {
        (16f + latePhase * 132f).dp
    } else {
        (14f + latePhase * 104f).dp
    }

    fun clearState() {
        direction = null
        phase = TurnPhase.Idle
        turnAnchorY = 0.5f
        lastVelocityPxPerSecond = 0f
        lastEventTimeMs = 0L
    }

    fun settle(result: TurnReleaseResult) {
        scope.launch {
            when (result) {
                TurnReleaseResult.CompleteNext -> {
                    phase = TurnPhase.SettlingForward
                    progress.animateTo(
                        1f,
                        animationSpec = spring(
                            dampingRatio = if (profile == TurnProfile.HANDBOOK) 0.86f else 0.9f,
                            stiffness = if (profile == TurnProfile.HANDBOOK) 140f else Spring.StiffnessLow,
                        ),
                    )
                    onFlipNext()
                }
                TurnReleaseResult.CompletePrevious -> {
                    phase = TurnPhase.SettlingForward
                    progress.animateTo(
                        1f,
                        animationSpec = spring(
                            dampingRatio = if (profile == TurnProfile.HANDBOOK) 0.86f else 0.9f,
                            stiffness = if (profile == TurnProfile.HANDBOOK) 140f else Spring.StiffnessLow,
                        ),
                    )
                    onFlipPrevious()
                }
                TurnReleaseResult.SnapBack -> {
                    phase = TurnPhase.SettlingBack
                    progress.animateTo(
                        0f,
                        animationSpec = spring(
                            dampingRatio = if (profile == TurnProfile.HANDBOOK) 0.8f else 0.84f,
                            stiffness = if (profile == TurnProfile.HANDBOOK) Spring.StiffnessMediumLow else Spring.StiffnessMediumLow,
                        ),
                    )
                }
            }
            progress.snapTo(0f)
            clearState()
        }
    }

    fun beginTapTurn(targetDirection: TurnDirection) {
        direction = targetDirection
        turnAnchorY = 0.5f
        phase = if (targetDirection == TurnDirection.NEXT) TurnPhase.DraggingNext else TurnPhase.DraggingPrevious
        scope.launch { progress.snapTo(initialEdgeTapProgress(profile)) }
        settle(
            if (targetDirection == TurnDirection.NEXT) {
                TurnReleaseResult.CompleteNext
            } else {
                TurnReleaseResult.CompletePrevious
            },
        )
    }

    shell(
        canTurnPrevious,
        canTurnNext,
        turnEnabled,
        { beginTapTurn(TurnDirection.PREVIOUS) },
        { beginTapTurn(TurnDirection.NEXT) },
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .onSizeChanged {
                    pageWidthPx = it.width.toFloat().coerceAtLeast(1f)
                    pageHeightPx = it.height.toFloat().coerceAtLeast(1f)
                }
                .pointerInput(canTurnNext, canTurnPrevious, pageWidthPx, turnEnabled) {
                    if (!turnEnabled) return@pointerInput
                    detectHorizontalDragGestures(
                        onDragStart = { startOffset ->
                            lastVelocityPxPerSecond = 0f
                            lastEventTimeMs = 0L
                            direction = null
                            dragStartX = startOffset.x
                            turnAnchorY = (startOffset.y / pageHeightPx).coerceIn(0.12f, 0.88f)
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            val edgeZonePx = pageWidthPx * if (profile == TurnProfile.HANDBOOK) HANDBOOK_EDGE_GESTURE_RATIO else EDGE_GESTURE_RATIO
                            val canStartNextFromEdge = dragStartX >= pageWidthPx - edgeZonePx
                            val canStartPreviousFromEdge = dragStartX <= edgeZonePx
                            val startedAtEdge = canStartNextFromEdge || canStartPreviousFromEdge
                            val handbookFreeStart = profile == TurnProfile.HANDBOOK
                            if (direction == null && !startedAtEdge) {
                                if (!handbookFreeStart) return@detectHorizontalDragGestures
                            }
                            if (direction == null && handbookFreeStart) {
                                val resolvedDirection = when {
                                    dragAmount <= -(if (profile == TurnProfile.HANDBOOK) HANDBOOK_DRAG_START_THRESHOLD else 0.6f) -> TurnDirection.NEXT
                                    dragAmount >= (if (profile == TurnProfile.HANDBOOK) HANDBOOK_DRAG_START_THRESHOLD else 0.6f) -> TurnDirection.PREVIOUS
                                    else -> null
                                }
                                if (resolvedDirection != null) {
                                    direction = resolvedDirection
                                } else if (!startedAtEdge) {
                                    return@detectHorizontalDragGestures
                                }
                            }
                            val resolvedDirection = direction ?: when {
                                dragAmount <= -(if (profile == TurnProfile.HANDBOOK) HANDBOOK_DRAG_START_THRESHOLD else 0.6f) && canStartNextFromEdge -> TurnDirection.NEXT
                                dragAmount >= (if (profile == TurnProfile.HANDBOOK) HANDBOOK_DRAG_START_THRESHOLD else 0.6f) && canStartPreviousFromEdge -> TurnDirection.PREVIOUS
                                else -> null
                            } ?: return@detectHorizontalDragGestures

                            direction = resolvedDirection
                            phase = if (resolvedDirection == TurnDirection.NEXT) TurnPhase.DraggingNext else TurnPhase.DraggingPrevious

                            val canTurn = when (resolvedDirection) {
                                TurnDirection.NEXT -> canTurnNext
                                TurnDirection.PREVIOUS -> canTurnPrevious
                            }
                            val adjustedProgress = updatedTurnProgress(
                                currentProgress = progress.value,
                                direction = resolvedDirection,
                                dragAmountPx = dragAmount,
                                pageWidthPx = pageWidthPx,
                                canTurn = canTurn,
                            )

                            val nowMs = change.uptimeMillis
                            val deltaMs = (nowMs - lastEventTimeMs).coerceAtLeast(1L)
                            lastVelocityPxPerSecond = if (abs(dragAmount) < 0.3f) {
                                0f
                            } else {
                                (dragAmount / deltaMs) * 1000f
                            }
                            lastEventTimeMs = nowMs

                            scope.launch { progress.snapTo(adjustedProgress) }
                        },
                        onDragCancel = { settle(TurnReleaseResult.SnapBack) },
                        onDragEnd = {
                            val activeDirection = direction ?: return@detectHorizontalDragGestures
                            settle(
                                resolvePageTurnRelease(
                                    direction = activeDirection,
                                    progress = progress.value.coerceIn(0f, 1f),
                                    velocity = lastVelocityPxPerSecond,
                                    hasPreviousPage = canTurnPrevious,
                                    hasNextPage = canTurnNext,
                                    profile = profile,
                                ),
                            )
                        },
                    )
                },
        ) {
            spine(visualProgress, direction != null)
            destination(dragProgress, direction)

            if (direction != null && dragProgress > 0.01f) {
                pageBack(visualProgress, direction, turnAnchorY)
            }

            activePage(visualProgress, direction, turnAnchorY)

            if (direction != null && dragProgress > 0.01f) {
                if (profile == TurnProfile.HANDBOOK) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .width((8f + visualProgress * 18f).dp)
                            .fillMaxHeight()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color.Black.copy(alpha = (0.03f + latePhase * 0.2f).coerceAtMost(0.26f)),
                                        Color.White.copy(alpha = (0.04f + latePhase * 0.22f).coerceAtMost(0.28f)),
                                        Color.Black.copy(alpha = (0.03f + latePhase * 0.2f).coerceAtMost(0.26f)),
                                    ),
                                ),
                            ),
                    )
                }
                if (profile == TurnProfile.HANDBOOK) {
                    repeat(4) { layer ->
                        val depth = (layer + 1) / 4f
                        Box(
                            modifier = Modifier
                                .align(if (draggingToNext) Alignment.CenterStart else Alignment.CenterEnd)
                                .width((2f + latePhase * (4f + depth * 11f)).dp)
                                .fillMaxHeight()
                                .background(
                                    Brush.horizontalGradient(
                                        if (draggingToNext) {
                                            listOf(
                                                Color.Black.copy(alpha = (0.02f + latePhase * 0.11f) * depth),
                                                Color(0x33B79678).copy(alpha = (0.02f + latePhase * 0.09f) * depth),
                                                Color.Transparent,
                                            )
                                        } else {
                                            listOf(
                                                Color.Transparent,
                                                Color(0x33B79678).copy(alpha = (0.02f + latePhase * 0.09f) * depth),
                                                Color.Black.copy(alpha = (0.02f + latePhase * 0.11f) * depth),
                                            )
                                        },
                                    ),
                                ),
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = (0.015f + earlyPhase * 0.04f + latePhase * 0.2f).coerceAtMost(0.24f)),
                                ),
                                radius = 2200f,
                            ),
                        ),
                )

                Box(
                    modifier = Modifier
                        .align(if (draggingToNext) Alignment.CenterStart else Alignment.CenterEnd)
                        .fillMaxHeight()
                        .width(turnShadowWidth)
                        .background(
                            if (draggingToNext) {
                                Brush.horizontalGradient(
                                    listOf(
                                        Color.Black.copy(alpha = (0.05f + latePhase * 0.34f).coerceAtMost(0.45f)),
                                        Color(0x22000000),
                                        Color.Transparent,
                                    ),
                                )
                            } else {
                                Brush.horizontalGradient(
                                    listOf(
                                        Color.Transparent,
                                        Color(0x22000000),
                                        Color.Black.copy(alpha = (0.05f + latePhase * 0.34f).coerceAtMost(0.45f)),
                                    ),
                                )
                            },
                        ),
                )

                Box(
                    modifier = Modifier
                        .align(if (draggingToNext) Alignment.CenterEnd else Alignment.CenterStart)
                        .width((2f + latePhase * 10f).dp)
                        .fillMaxHeight()
                        .background(
                            Brush.horizontalGradient(
                                if (draggingToNext) {
                                    listOf(
                                        Color.White.copy(alpha = (0.08f + latePhase * 0.26f).coerceAtMost(0.31f)),
                                        Color.Black.copy(alpha = (0.04f + latePhase * 0.20f).coerceAtMost(0.24f)),
                                    )
                                } else {
                                    listOf(
                                        Color.Black.copy(alpha = (0.04f + latePhase * 0.20f).coerceAtMost(0.24f)),
                                        Color.White.copy(alpha = (0.08f + latePhase * 0.26f).coerceAtMost(0.31f)),
                                    )
                                },
                            ),
                        ),
                )

                Box(
                    modifier = Modifier
                        .align(if (draggingToNext) Alignment.CenterEnd else Alignment.CenterStart)
                        .width((4f + latePhase * 16f).dp)
                        .fillMaxHeight()
                        .background(
                            Brush.horizontalGradient(
                                if (draggingToNext) {
                                    listOf(
                                        Color.White.copy(alpha = (0.03f + latePhase * 0.22f).coerceAtMost(0.24f)),
                                        Color.Transparent,
                                    )
                                } else {
                                    listOf(
                                        Color.Transparent,
                                        Color.White.copy(alpha = (0.03f + latePhase * 0.22f).coerceAtMost(0.24f)),
                                    )
                                },
                            ),
                        ),
                )

                Box(
                    modifier = Modifier
                        .align(if (draggingToNext) Alignment.TopEnd else Alignment.TopStart)
                        .width((18f + visualProgress * 34f).dp)
                        .height((18f + visualProgress * 34f).dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = (0.03f + latePhase * 0.18f).coerceAtMost(0.22f)),
                                    Color.Black.copy(alpha = (0.015f + latePhase * 0.12f).coerceAtMost(0.14f)),
                                    Color.Transparent,
                                ),
                                radius = 120f,
                            ),
                        ),
                )
            }
        }
    }
}

fun Modifier.turningPageTransform(
    direction: TurnDirection?,
    visualProgress: Float,
    anchorY: Float,
    profile: TurnProfile = TurnProfile.DEFAULT,
): Modifier = graphicsLayer {
    val draggingToNext = direction == TurnDirection.NEXT
    val draggingToPrevious = direction == TurnDirection.PREVIOUS
    transformOrigin = if (draggingToNext) TransformOrigin(0f, 0.5f) else TransformOrigin(1f, 0.5f)
    val handbookTailBoost = if (profile == TurnProfile.HANDBOOK) {
        val tail = ((visualProgress - 0.78f) / 0.22f).coerceIn(0f, 1f)
        tail * 12f
    } else {
        0f
    }
    val progressCurve = (visualProgress * 0.35f) + (visualProgress * visualProgress * 0.65f)
    val maxRotation = if (profile == TurnProfile.HANDBOOK) 128f + handbookTailBoost else 118f
    rotationY = when (direction) {
        TurnDirection.NEXT -> -maxRotation * progressCurve
        TurnDirection.PREVIOUS -> maxRotation * progressCurve
        null -> 0f
    }
    val tailRetract = if (profile == TurnProfile.HANDBOOK) {
        val tail = ((visualProgress - 0.84f) / 0.16f).coerceIn(0f, 1f)
        tail * 8f
    } else {
        0f
    }
    translationX = when {
        draggingToNext -> -(visualProgress * 12f + progressCurve * 62f - tailRetract)
        draggingToPrevious -> visualProgress * 12f + progressCurve * 62f - tailRetract
        else -> 0f
    }
    val yOffsetFactor = (anchorY - 0.5f) * 2f
    translationY = yOffsetFactor * visualProgress * 12f
    rotationX = -yOffsetFactor * progressCurve * 9.2f
    cameraDistance = if (profile == TurnProfile.HANDBOOK) 38f * density else 34f * density
    shadowElevation = if (profile == TurnProfile.HANDBOOK) 32f else 28f
    val subtleDepthScale = if (profile == TurnProfile.HANDBOOK) 1f - visualProgress * 0.022f else 1f - visualProgress * 0.015f
    scaleY = subtleDepthScale.coerceIn(0.965f, 1f)
    alpha = (1f - visualProgress * 0.08f).coerceIn(0.9f, 1f)
}

fun Modifier.pageBackTransform(
    direction: TurnDirection?,
    visualProgress: Float,
    anchorY: Float,
    profile: TurnProfile = TurnProfile.DEFAULT,
): Modifier = graphicsLayer {
    transformOrigin = if (direction == TurnDirection.NEXT) TransformOrigin(0f, 0.5f) else TransformOrigin(1f, 0.5f)
    val handbookTailBoost = if (profile == TurnProfile.HANDBOOK) {
        val tail = ((visualProgress - 0.78f) / 0.22f).coerceIn(0f, 1f)
        tail * 10f
    } else {
        0f
    }
    val progressCurve = (visualProgress * 0.32f) + (visualProgress * visualProgress * 0.68f)
    val maxRotation = if (profile == TurnProfile.HANDBOOK) 128f + handbookTailBoost else 118f
    rotationY = when (direction) {
        TurnDirection.NEXT -> -maxRotation * progressCurve * 0.9f
        TurnDirection.PREVIOUS -> maxRotation * progressCurve * 0.9f
        null -> 0f
    }
    val yOffsetFactor = (anchorY - 0.5f) * 2f
    translationY = yOffsetFactor * progressCurve * 7.2f
    rotationX = -yOffsetFactor * progressCurve * 5.4f
    cameraDistance = if (profile == TurnProfile.HANDBOOK) 38f * density else 34f * density
    val subtleBackScale = if (profile == TurnProfile.HANDBOOK) 1f - visualProgress * 0.018f else 1f - visualProgress * 0.012f
    scaleY = subtleBackScale.coerceIn(0.972f, 1f)
}
