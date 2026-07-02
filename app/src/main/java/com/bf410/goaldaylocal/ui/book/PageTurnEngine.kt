package com.bf410.goaldaylocal.ui.book

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitHorizontalTouchSlopOrCancellation
import androidx.compose.foundation.gestures.horizontalDrag
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.input.pointer.util.addPointerInputChange
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import com.bf410.goaldaylocal.ui.replica.GoaldayDesign
import kotlinx.coroutines.launch
import kotlin.math.abs

// DEFAULT 模式：边缘热区略窄，减少与内容横向滚动的冲突
private const val DEFAULT_EDGE_GESTURE_RATIO = 0.11f
// HANDBOOK 模式：书页边缘应更宽，方便单手持书时拇指翻页
private const val HANDBOOK_EDGE_GESTURE_RATIO = 0.14f
// HANDBOOK 拖动阈值：约 10px（按 360px 宽度计），翻页更跟手
private const val HANDBOOK_DRAG_START_RATIO = 0.028f

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
        (20f + latePhase * 148f).dp
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
                            // HANDBOOK 提高刚度，让翻页收尾更利落、减少卡顿感
                            dampingRatio = if (profile == TurnProfile.HANDBOOK) 0.78f else 0.9f,
                            stiffness = if (profile == TurnProfile.HANDBOOK) 180f else Spring.StiffnessLow,
                        ),
                    )
                    onFlipNext()
                    if (profile == TurnProfile.HANDBOOK) {
                        // 翻到位后直接归零并清理状态，避免“翻完又晃一下”
                        progress.snapTo(0f)
                        clearState()
                        return@launch
                    }
                }
                TurnReleaseResult.CompletePrevious -> {
                    phase = TurnPhase.SettlingForward
                    progress.animateTo(
                        1f,
                        animationSpec = spring(
                            dampingRatio = if (profile == TurnProfile.HANDBOOK) 0.78f else 0.9f,
                            stiffness = if (profile == TurnProfile.HANDBOOK) 180f else Spring.StiffnessLow,
                        ),
                    )
                    onFlipPrevious()
                    if (profile == TurnProfile.HANDBOOK) {
                        progress.snapTo(0f)
                        clearState()
                        return@launch
                    }
                }
                TurnReleaseResult.SnapBack -> {
                    phase = TurnPhase.SettlingBack
                    progress.animateTo(
                        0f,
                        animationSpec = spring(
                            dampingRatio = if (profile == TurnProfile.HANDBOOK) 0.92f else 0.84f,
                            stiffness = if (profile == TurnProfile.HANDBOOK) 300f else Spring.StiffnessMediumLow,
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
                .pointerInput(canTurnNext, canTurnPrevious, turnEnabled, profile) {
                    if (!turnEnabled) return@pointerInput
                    val edgeRatio = if (profile == TurnProfile.HANDBOOK) {
                        HANDBOOK_EDGE_GESTURE_RATIO
                    } else {
                        DEFAULT_EDGE_GESTURE_RATIO
                    }
                    detectEdgePageTurnGestures(
                        canTurnNext = canTurnNext,
                        canTurnPrevious = canTurnPrevious,
                        edgeRatio = edgeRatio,
                        onStart = { startOffset, startDirection ->
                            lastVelocityPxPerSecond = 0f
                            lastEventTimeMs = 0L
                            direction = startDirection
                            dragStartX = startOffset.x
                            turnAnchorY = (startOffset.y / pageHeightPx).coerceIn(0.12f, 0.88f)
                            phase = if (startDirection == TurnDirection.NEXT) {
                                TurnPhase.DraggingNext
                            } else {
                                TurnPhase.DraggingPrevious
                            }
                        },
                        onDrag = { change, dragAmountPx ->
                            val resolvedDirection = direction ?: return@detectEdgePageTurnGestures
                            val canTurn = when (resolvedDirection) {
                                TurnDirection.NEXT -> canTurnNext
                                TurnDirection.PREVIOUS -> canTurnPrevious
                            }
                            val adjustedProgress = updatedTurnProgress(
                                currentProgress = progress.value,
                                direction = resolvedDirection,
                                dragAmountPx = dragAmountPx,
                                pageWidthPx = pageWidthPx,
                                canTurn = canTurn,
                            )

                            val nowMs = change.uptimeMillis
                            val deltaMs = (nowMs - lastEventTimeMs).coerceAtLeast(1L)
                            lastVelocityPxPerSecond = if (abs(dragAmountPx) < 0.3f) {
                                0f
                            } else {
                                (dragAmountPx / deltaMs) * 1000f
                            }
                            lastEventTimeMs = nowMs

                            scope.launch { progress.snapTo(adjustedProgress) }
                        },
                        onEnd = { velocityX ->
                            lastVelocityPxPerSecond = velocityX
                            val activeDirection = direction ?: return@detectEdgePageTurnGestures
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
                        onCancel = { settle(TurnReleaseResult.SnapBack) },
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
                // 翻页过程中的核心阴影与暗角，营造 3D 卷曲感
                if (profile == TurnProfile.HANDBOOK) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .width((8f + visualProgress * 12f).dp)
                            .fillMaxHeight()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color.Black.copy(alpha = (0.02f + latePhase * 0.10f).coerceAtMost(0.14f)),
                                        Color.White.copy(alpha = (0.02f + latePhase * 0.10f).coerceAtMost(0.14f)),
                                        Color.Black.copy(alpha = (0.02f + latePhase * 0.10f).coerceAtMost(0.14f)),
                                    ),
                                ),
                            ),
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = (0.006f + earlyPhase * 0.012f + latePhase * 0.08f).coerceAtMost(0.10f)),
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
                                        Color.Black.copy(alpha = (0.04f + latePhase * 0.24f).coerceAtMost(0.30f)),
                                        GoaldayDesign.BlackOverlaySoft,
                                        Color.Transparent,
                                    ),
                                )
                            } else {
                                Brush.horizontalGradient(
                                    listOf(
                                        Color.Transparent,
                                        GoaldayDesign.BlackOverlaySoft,
                                        Color.Black.copy(alpha = (0.04f + latePhase * 0.24f).coerceAtMost(0.30f)),
                                    ),
                                )
                            },
                        ),
                )
            }
        }
    }
}

/**
 * 只在屏幕左右边缘拦截水平拖动手势。
 * 非边缘按下时不消费事件，内部滚动/WebView 等内容优先响应，避免翻页手势抢占内容滚动。
 */
private suspend fun PointerInputScope.detectEdgePageTurnGestures(
    canTurnNext: Boolean,
    canTurnPrevious: Boolean,
    edgeRatio: Float,
    onStart: (Offset, TurnDirection) -> Unit,
    onDrag: (PointerInputChange, Float) -> Unit,
    onEnd: (Float) -> Unit,
    onCancel: () -> Unit,
) {
    awaitPointerEventScope {
        while (true) {
            val down = awaitFirstDown()
            val width = size.width.toFloat().coerceAtLeast(1f)
            val edgePx = width * edgeRatio.coerceIn(0.05f, 0.45f)
            val x = down.position.x
            val startDirection = when {
                canTurnPrevious && x <= edgePx -> TurnDirection.PREVIOUS
                canTurnNext && x >= width - edgePx -> TurnDirection.NEXT
                else -> null
            }
            if (startDirection == null) {
                // 非边缘区域：不拦截，交给子组件处理
                continue
            }
            down.consume()
            onStart(down.position, startDirection)

            // 等待水平 touch slop，只接受“向书内”拖动（右边缘向左滑、左边缘向右滑）
            var inward = false
            val drag = awaitHorizontalTouchSlopOrCancellation(down.id) { change, overSlop ->
                inward = when (startDirection) {
                    TurnDirection.NEXT -> overSlop < 0
                    TurnDirection.PREVIOUS -> overSlop > 0
                }
                if (inward) change.consume()
            }
            if (drag == null || !inward) {
                onCancel()
                continue
            }

            val velocityTracker = VelocityTracker()
            velocityTracker.addPointerInputChange(drag)

            var previousX = down.position.x
            var currentX = drag.position.x
            onDrag(drag, currentX - previousX)
            previousX = currentX

            val success = horizontalDrag(drag.id) { change ->
                velocityTracker.addPointerInputChange(change)
                currentX = change.position.x
                val deltaX = currentX - previousX
                previousX = currentX
                onDrag(change, deltaX)
                true
            }

            val velocity = velocityTracker.calculateVelocity().x
            if (success) {
                onEnd(velocity)
            } else {
                onCancel()
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
    transformOrigin = TransformOrigin(turnTransformOriginX(profile, direction), 0.5f)
    // 简化翻页：HANDBOOK 用 75° 柔和旋转，DEFAULT 保持 118°
    val progressCurve = (visualProgress * 0.35f) + (visualProgress * visualProgress * 0.65f)
    val maxRotation = if (profile == TurnProfile.HANDBOOK) 75f else 118f
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
    // HANDBOOK translationX 从 ~20px 提升到 ~60px 量级，让水平扫过明显可见
    translationX = when {
        draggingToNext -> if (profile == TurnProfile.HANDBOOK) {
            -(visualProgress * 10f + progressCurve * 50f - tailRetract * 0.35f)
        } else {
            -(visualProgress * 14f + progressCurve * 68f - tailRetract)
        }
        draggingToPrevious -> if (profile == TurnProfile.HANDBOOK) {
            visualProgress * 10f + progressCurve * 50f - tailRetract * 0.35f
        } else {
            visualProgress * 14f + progressCurve * 68f - tailRetract
        }
        else -> 0f
    }
    val yOffsetFactor = (anchorY - 0.5f) * 2f
    translationY = yOffsetFactor * visualProgress * if (profile == TurnProfile.HANDBOOK) 5.8f else 12f
    rotationX = -yOffsetFactor * progressCurve * if (profile == TurnProfile.HANDBOOK) 3.0f else 9.2f
    // HANDBOOK 用更大 cameraDistance 减少 3D 畸变，翻页更干净
    cameraDistance = if (profile == TurnProfile.HANDBOOK) 48f * density else 34f * density
    shadowElevation = if (profile == TurnProfile.HANDBOOK) 18f else 28f
    val subtleDepthScale = if (profile == TurnProfile.HANDBOOK) 1f - visualProgress * 0.022f else 1f - visualProgress * 0.015f
    scaleY = if (profile == TurnProfile.HANDBOOK) {
        subtleDepthScale.coerceIn(0.978f, 1f)
    } else {
        subtleDepthScale.coerceIn(0.965f, 1f)
    }
    // HANDBOOK alpha 从 0.78-1.0，让翻页中后段明显变暗，模拟纸张透视
    alpha = if (profile == TurnProfile.HANDBOOK) {
        (1f - visualProgress * 0.22f).coerceIn(0.78f, 1f)
    } else {
        (1f - visualProgress * 0.08f).coerceIn(0.9f, 1f)
    }
}

fun Modifier.pageBackTransform(
    direction: TurnDirection?,
    visualProgress: Float,
    anchorY: Float,
    profile: TurnProfile = TurnProfile.DEFAULT,
): Modifier = graphicsLayer {
    val draggingToNext = direction == TurnDirection.NEXT
    val draggingToPrevious = direction == TurnDirection.PREVIOUS
    transformOrigin = TransformOrigin(turnTransformOriginX(profile, direction), 0.5f)
    // 背面角度从 80° 提升到 115°，与正面同步翻越中轴线
    val handbookTailBoost = if (profile == TurnProfile.HANDBOOK) {
        val tail = ((visualProgress - 0.82f) / 0.18f).coerceIn(0f, 1f)
        tail * 7f
    } else {
        0f
    }
    val progressCurve = (visualProgress * 0.32f) + (visualProgress * visualProgress * 0.68f)
    val maxRotation = if (profile == TurnProfile.HANDBOOK) 75f else 118f
    rotationY = when (direction) {
        TurnDirection.NEXT -> -maxRotation * progressCurve * 0.92f
        TurnDirection.PREVIOUS -> maxRotation * progressCurve * 0.92f
        null -> 0f
    }
    // 背面 translationX 与正面镜像，避免“贴在原地旋转”
    val backShift = if (profile == TurnProfile.HANDBOOK) {
        visualProgress * 8f + progressCurve * 42f
    } else {
        visualProgress * 12f + progressCurve * 58f
    }
    translationX = when {
        draggingToNext -> -backShift
        draggingToPrevious -> backShift
        else -> 0f
    }
    val yOffsetFactor = (anchorY - 0.5f) * 2f
    translationY = yOffsetFactor * progressCurve * if (profile == TurnProfile.HANDBOOK) 3.2f else 7.2f
    rotationX = -yOffsetFactor * progressCurve * if (profile == TurnProfile.HANDBOOK) 1.8f else 5.4f
    // HANDBOOK 背面用与正面一致的 cameraDistance
    cameraDistance = if (profile == TurnProfile.HANDBOOK) 48f * density else 34f * density
    val subtleBackScale = if (profile == TurnProfile.HANDBOOK) 1f - visualProgress * 0.018f else 1f - visualProgress * 0.012f
    scaleY = if (profile == TurnProfile.HANDBOOK) {
        subtleBackScale.coerceIn(0.982f, 1f)
    } else {
        subtleBackScale.coerceIn(0.972f, 1f)
    }
    // 背面在中后段渐显（模拟纸张背面从暗到亮）
    alpha = if (profile == TurnProfile.HANDBOOK) {
        (0.70f + visualProgress * 0.28f).coerceIn(0.70f, 0.98f)
    } else {
        (0.85f + visualProgress * 0.13f).coerceIn(0.85f, 0.98f)
    }
}

// 翻页动画模式：SIMULATION=仿真翻页(默认) / COVER=水平覆盖 / SCROLL=垂直滚动 / NONE=无动画
enum class PageTurnStyle { SIMULATION, COVER, SCROLL, NONE }

/**
 * 简化翻页器：用于 COVER/SCROLL/NONE 三种非仿真模式。
 * 复用 BookShell 外壳，用 AnimatedContent 做覆盖/滚动/淡入切换，不渲染仿真翻页层。
 * 对标微信读书"覆盖/上下滚动/无动画"翻页选项。
 */
@Composable
fun SimplePageTurner(
    pageKey: Int,
    canTurnPrevious: Boolean,
    canTurnNext: Boolean,
    turnEnabled: Boolean,
    onFlipNext: () -> Unit,
    onFlipPrevious: () -> Unit,
    shellStyle: ShellStyle,
    style: PageTurnStyle,
    activePage: @Composable () -> Unit,
) {
    var direction by remember { mutableStateOf(TurnDirection.NEXT) }
    BookShell(
        shellStyle = shellStyle,
        canTurnPrevious = canTurnPrevious,
        canTurnNext = canTurnNext,
        turnEnabled = turnEnabled,
        onTapPrevious = {
            direction = TurnDirection.PREVIOUS
            onFlipPrevious()
        },
        onTapNext = {
            direction = TurnDirection.NEXT
            onFlipNext()
        },
    ) {
        AnimatedContent(
            targetState = pageKey,
            transitionSpec = {
                val enter = when (style) {
                    PageTurnStyle.NONE -> fadeIn(animationSpec = tween(180))
                    PageTurnStyle.SCROLL ->
                        if (direction == TurnDirection.NEXT) slideInVertically(animationSpec = tween(260)) { it }
                        else slideInVertically(animationSpec = tween(260)) { -it }
                    PageTurnStyle.COVER ->
                        if (direction == TurnDirection.NEXT) slideInHorizontally(animationSpec = tween(260)) { it }
                        else slideInHorizontally(animationSpec = tween(260)) { -it }
                    PageTurnStyle.SIMULATION -> fadeIn(animationSpec = tween(180))
                }
                val exit = when (style) {
                    PageTurnStyle.NONE -> fadeOut(animationSpec = tween(180))
                    PageTurnStyle.SCROLL ->
                        if (direction == TurnDirection.NEXT) slideOutVertically(animationSpec = tween(260)) { -it }
                        else slideOutVertically(animationSpec = tween(260)) { it }
                    PageTurnStyle.COVER ->
                        if (direction == TurnDirection.NEXT) slideOutHorizontally(animationSpec = tween(260)) { -it }
                        else slideOutHorizontally(animationSpec = tween(260)) { it }
                    PageTurnStyle.SIMULATION -> fadeOut(animationSpec = tween(180))
                }
                enter togetherWith exit
            },
            contentKey = { it },
            label = "simple-page-turn",
        ) {
            activePage()
        }
    }
}
