package com.bf410.goaldaylocal.ui.book

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
    val draggingToNext = direction == TurnDirection.NEXT
    val draggingToPrevious = direction == TurnDirection.PREVIOUS
    val turnShadowWidth = if (profile == TurnProfile.HANDBOOK) {
        (22f + visualProgress * visualProgress * 108f).dp
    } else {
        (18f + visualProgress * visualProgress * 92f).dp
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
                            dampingRatio = if (profile == TurnProfile.HANDBOOK) 0.91f else 0.9f,
                            stiffness = if (profile == TurnProfile.HANDBOOK) 170f else Spring.StiffnessLow,
                        ),
                    )
                    onFlipNext()
                }
                TurnReleaseResult.CompletePrevious -> {
                    phase = TurnPhase.SettlingForward
                    progress.animateTo(
                        1f,
                        animationSpec = spring(
                            dampingRatio = if (profile == TurnProfile.HANDBOOK) 0.91f else 0.9f,
                            stiffness = if (profile == TurnProfile.HANDBOOK) 170f else Spring.StiffnessLow,
                        ),
                    )
                    onFlipPrevious()
                }
                TurnReleaseResult.SnapBack -> {
                    phase = TurnPhase.SettlingBack
                    progress.animateTo(
                        0f,
                        animationSpec = spring(
                            dampingRatio = if (profile == TurnProfile.HANDBOOK) 0.82f else 0.84f,
                            stiffness = if (profile == TurnProfile.HANDBOOK) Spring.StiffnessMedium else Spring.StiffnessMediumLow,
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
                            val edgeZonePx = pageWidthPx * EDGE_GESTURE_RATIO
                            val canStartNextFromEdge = dragStartX >= pageWidthPx - edgeZonePx
                            val canStartPreviousFromEdge = dragStartX <= edgeZonePx
                            val startedAtEdge = canStartNextFromEdge || canStartPreviousFromEdge
                            if (direction == null && !startedAtEdge) {
                                return@detectHorizontalDragGestures
                            }
                            val resolvedDirection = direction ?: when {
                                dragAmount <= -0.6f && canStartNextFromEdge -> TurnDirection.NEXT
                                dragAmount >= 0.6f && canStartPreviousFromEdge -> TurnDirection.PREVIOUS
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
                                        Color.Black.copy(alpha = (0.04f + visualProgress * 0.14f).coerceAtMost(0.22f)),
                                        Color.White.copy(alpha = (0.05f + visualProgress * 0.16f).coerceAtMost(0.24f)),
                                        Color.Black.copy(alpha = (0.04f + visualProgress * 0.14f).coerceAtMost(0.22f)),
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
                                .width((2f + visualProgress * (5f + depth * 8f)).dp)
                                .fillMaxHeight()
                                .background(
                                    Brush.horizontalGradient(
                                        if (draggingToNext) {
                                            listOf(
                                                Color.Black.copy(alpha = (0.03f + visualProgress * 0.08f) * depth),
                                                Color(0x33B79678).copy(alpha = (0.03f + visualProgress * 0.06f) * depth),
                                                Color.Transparent,
                                            )
                                        } else {
                                            listOf(
                                                Color.Transparent,
                                                Color(0x33B79678).copy(alpha = (0.03f + visualProgress * 0.06f) * depth),
                                                Color.Black.copy(alpha = (0.03f + visualProgress * 0.08f) * depth),
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
                                    Color.Black.copy(alpha = (0.03f + visualProgress * visualProgress * 0.18f).coerceAtMost(0.19f)),
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
                                        Color.Black.copy(alpha = (0.08f + visualProgress * visualProgress * 0.28f).coerceAtMost(0.42f)),
                                        Color(0x22000000),
                                        Color.Transparent,
                                    ),
                                )
                            } else {
                                Brush.horizontalGradient(
                                    listOf(
                                        Color.Transparent,
                                        Color(0x22000000),
                                        Color.Black.copy(alpha = (0.08f + visualProgress * visualProgress * 0.28f).coerceAtMost(0.42f)),
                                    ),
                                )
                            },
                        ),
                )

                Box(
                    modifier = Modifier
                        .align(if (draggingToNext) Alignment.CenterEnd else Alignment.CenterStart)
                        .width((2f + visualProgress * 8f).dp)
                        .fillMaxHeight()
                        .background(
                            Brush.horizontalGradient(
                                if (draggingToNext) {
                                    listOf(
                                        Color.White.copy(alpha = (0.10f + visualProgress * 0.22f).coerceAtMost(0.28f)),
                                        Color.Black.copy(alpha = (0.06f + visualProgress * 0.16f).coerceAtMost(0.20f)),
                                    )
                                } else {
                                    listOf(
                                        Color.Black.copy(alpha = (0.06f + visualProgress * 0.16f).coerceAtMost(0.20f)),
                                        Color.White.copy(alpha = (0.10f + visualProgress * 0.22f).coerceAtMost(0.28f)),
                                    )
                                },
                            ),
                        ),
                )

                Box(
                    modifier = Modifier
                        .align(if (draggingToNext) Alignment.CenterEnd else Alignment.CenterStart)
                        .width((5f + visualProgress * 12f).dp)
                        .fillMaxHeight()
                        .background(
                            Brush.horizontalGradient(
                                if (draggingToNext) {
                                    listOf(
                                        Color.White.copy(alpha = (0.04f + visualProgress * 0.18f).coerceAtMost(0.22f)),
                                        Color.Transparent,
                                    )
                                } else {
                                    listOf(
                                        Color.Transparent,
                                        Color.White.copy(alpha = (0.04f + visualProgress * 0.18f).coerceAtMost(0.22f)),
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
                                    Color.White.copy(alpha = (0.04f + visualProgress * 0.14f).coerceAtMost(0.2f)),
                                    Color.Black.copy(alpha = (0.02f + visualProgress * 0.10f).coerceAtMost(0.12f)),
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
    val maxRotation = if (profile == TurnProfile.HANDBOOK) 128f + handbookTailBoost else 118f
    rotationY = when (direction) {
        TurnDirection.NEXT -> -maxRotation * visualProgress
        TurnDirection.PREVIOUS -> maxRotation * visualProgress
        null -> 0f
    }
    val tailRetract = if (profile == TurnProfile.HANDBOOK) {
        val tail = ((visualProgress - 0.84f) / 0.16f).coerceIn(0f, 1f)
        tail * 8f
    } else {
        0f
    }
    translationX = when {
        draggingToNext -> -(visualProgress * 16f + visualProgress * visualProgress * 56f - tailRetract)
        draggingToPrevious -> visualProgress * 16f + visualProgress * visualProgress * 56f - tailRetract
        else -> 0f
    }
    val yOffsetFactor = (anchorY - 0.5f) * 2f
    translationY = yOffsetFactor * visualProgress * 12f
    rotationX = -yOffsetFactor * visualProgress * 8.5f
    cameraDistance = if (profile == TurnProfile.HANDBOOK) 38f * density else 34f * density
    shadowElevation = if (profile == TurnProfile.HANDBOOK) 32f else 28f
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
    val maxRotation = if (profile == TurnProfile.HANDBOOK) 128f + handbookTailBoost else 118f
    rotationY = when (direction) {
        TurnDirection.NEXT -> -maxRotation * visualProgress * 0.91f
        TurnDirection.PREVIOUS -> maxRotation * visualProgress * 0.91f
        null -> 0f
    }
    val yOffsetFactor = (anchorY - 0.5f) * 2f
    translationY = yOffsetFactor * visualProgress * 7f
    rotationX = -yOffsetFactor * visualProgress * 5f
    cameraDistance = if (profile == TurnProfile.HANDBOOK) 38f * density else 34f * density
}
