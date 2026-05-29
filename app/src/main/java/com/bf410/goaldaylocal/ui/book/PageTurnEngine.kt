package com.bf410.goaldaylocal.ui.book

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
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

private const val EDGE_GESTURE_RATIO = 0.22f

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
    val visualProgress = visualTurnProgress(dragProgress)
    val draggingToNext = direction == TurnDirection.NEXT
    val draggingToPrevious = direction == TurnDirection.PREVIOUS
    val turnShadowWidth = (18f + visualProgress * visualProgress * 92f).dp

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
                    progress.animateTo(1f, animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing))
                    onFlipNext()
                }
                TurnReleaseResult.CompletePrevious -> {
                    phase = TurnPhase.SettlingForward
                    progress.animateTo(1f, animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing))
                    onFlipPrevious()
                }
                TurnReleaseResult.SnapBack -> {
                    phase = TurnPhase.SettlingBack
                    progress.animateTo(0f, animationSpec = tween(durationMillis = 280, easing = LinearOutSlowInEasing))
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
        scope.launch { progress.snapTo(initialEdgeTapProgress()) }
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
                Box(
                    modifier = Modifier
                        .align(if (draggingToNext) Alignment.CenterStart else Alignment.CenterEnd)
                        .fillMaxHeight()
                        .width(turnShadowWidth)
                        .background(
                            if (draggingToNext) {
                                Brush.horizontalGradient(
                                    listOf(
                                        Color.Black.copy(alpha = (0.10f + visualProgress * visualProgress * 0.34f).coerceAtMost(0.50f)),
                                        Color(0x22000000),
                                        Color.Transparent,
                                    ),
                                )
                            } else {
                                Brush.horizontalGradient(
                                    listOf(
                                        Color.Transparent,
                                        Color(0x22000000),
                                        Color.Black.copy(alpha = (0.10f + visualProgress * visualProgress * 0.34f).coerceAtMost(0.50f)),
                                    ),
                                )
                            },
                        ),
                )

                Box(
                    modifier = Modifier
                        .align(if (draggingToNext) Alignment.CenterEnd else Alignment.CenterStart)
                        .width((2f + visualProgress * 6f).dp)
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
            }
        }
    }
}

fun Modifier.turningPageTransform(
    direction: TurnDirection?,
    visualProgress: Float,
    anchorY: Float,
): Modifier = graphicsLayer {
    val draggingToNext = direction == TurnDirection.NEXT
    val draggingToPrevious = direction == TurnDirection.PREVIOUS
    transformOrigin = if (draggingToNext) TransformOrigin(0f, 0.5f) else TransformOrigin(1f, 0.5f)
    rotationY = when (direction) {
        TurnDirection.NEXT -> -108f * visualProgress
        TurnDirection.PREVIOUS -> 108f * visualProgress
        null -> 0f
    }
    translationX = when {
        draggingToNext -> -(visualProgress * 22f + visualProgress * visualProgress * 42f)
        draggingToPrevious -> visualProgress * 22f + visualProgress * visualProgress * 42f
        else -> 0f
    }
    val yOffsetFactor = (anchorY - 0.5f) * 2f
    translationY = yOffsetFactor * visualProgress * 12f
    rotationX = -yOffsetFactor * visualProgress * 8.5f
    cameraDistance = 30f * density
    shadowElevation = 24f
}

fun Modifier.pageBackTransform(
    direction: TurnDirection?,
    visualProgress: Float,
    anchorY: Float,
): Modifier = graphicsLayer {
    transformOrigin = if (direction == TurnDirection.NEXT) TransformOrigin(0f, 0.5f) else TransformOrigin(1f, 0.5f)
    rotationY = when (direction) {
        TurnDirection.NEXT -> -108f * visualProgress * 0.92f
        TurnDirection.PREVIOUS -> 108f * visualProgress * 0.92f
        null -> 0f
    }
    val yOffsetFactor = (anchorY - 0.5f) * 2f
    translationY = yOffsetFactor * visualProgress * 7f
    rotationX = -yOffsetFactor * visualProgress * 5f
    cameraDistance = 30f * density
}
