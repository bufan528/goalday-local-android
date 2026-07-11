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
import androidx.compose.ui.input.pointer.changedToUp
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
// HANDBOOK 模式：全宽翻页，原版 App 支持任意位置水平滑动翻页
private const val HANDBOOK_EDGE_GESTURE_RATIO = 0.5f
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
                            // HANDBOOK 对齐原版：更柔和的纸张翻页感，略带弹性
                            dampingRatio = if (profile == TurnProfile.HANDBOOK) 0.85f else 0.9f,
                            stiffness = if (profile == TurnProfile.HANDBOOK) 150f else Spring.StiffnessLow,
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
                            // HANDBOOK 对齐原版：更柔和的纸张翻页感，略带弹性
                            dampingRatio = if (profile == TurnProfile.HANDBOOK) 0.85f else 0.9f,
                            stiffness = if (profile == TurnProfile.HANDBOOK) 150f else Spring.StiffnessLow,
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
                            // HANDBOOK 对齐原版：回弹更柔和，模拟纸张自然回落
                            dampingRatio = if (profile == TurnProfile.HANDBOOK) 0.88f else 0.84f,
                            stiffness = if (profile == TurnProfile.HANDBOOK) 250f else Spring.StiffnessMediumLow,
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

    fun startDragTurn(startOffset: Offset, startDirection: TurnDirection) {
        if (phase != TurnPhase.Idle) return
        direction = startDirection
        turnAnchorY = (startOffset.y / pageHeightPx.coerceAtLeast(1f)).coerceIn(0f, 1f)
        phase = if (startDirection == TurnDirection.NEXT) TurnPhase.DraggingNext else TurnPhase.DraggingPrevious
        scope.launch { progress.snapTo(0f) }
    }

    fun updateDragTurn(deltaX: Float) {
        val currentDirection = direction ?: return
        val canTurn = when (currentDirection) {
            TurnDirection.NEXT -> canTurnNext
            TurnDirection.PREVIOUS -> canTurnPrevious
        }
        val newProgress = updatedTurnProgress(
            currentProgress = progress.value,
            direction = currentDirection,
            dragAmountPx = deltaX,
            pageWidthPx = pageWidthPx,
            canTurn = canTurn,
        )
        scope.launch { progress.snapTo(newProgress) }
    }

    fun releaseDragTurn(velocity: Float) {
        val currentDirection = direction ?: return
        val result = resolvePageTurnRelease(
            direction = currentDirection,
            progress = progress.value,
            velocity = velocity,
            hasPreviousPage = canTurnPrevious,
            hasNextPage = canTurnNext,
            profile = profile,
        )
        settle(result)
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
                .pointerInput(turnEnabled, canTurnPrevious, canTurnNext, profile) {
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
                        onStart = { offset, dir -> startDragTurn(offset, dir) },
                        onDrag = { _, deltaX -> updateDragTurn(deltaX) },
                        onEnd = { velocity -> releaseDragTurn(velocity) },
                        onCancel = {
                            if (progress.value > 0.01f) {
                                settle(TurnReleaseResult.SnapBack)
                            } else {
                                clearState()
                                scope.launch { progress.snapTo(0f) }
                            }
                        },
                    )
                },
        ) {
            spine(visualProgress, direction != null)

            if (direction != null) {
                destination(dragProgress, direction)
            }

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
                            .width((10f + visualProgress * 16f).dp)
                            .fillMaxHeight()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        GoaldayDesign.BlackOverlayMedium.copy(alpha = (0.032f + latePhase * 0.112f).coerceAtMost(0.16f)),
                                        Color.White.copy(alpha = (0.016f + latePhase * 0.08f).coerceAtMost(0.112f)),
                                        GoaldayDesign.BlackOverlayMedium.copy(alpha = (0.032f + latePhase * 0.112f).coerceAtMost(0.16f)),
                                    ),
                                ),
                            ),
                    )
                    // 翻页页缘卷曲高光/阴影：模拟纸张弯折时外侧受光、内侧背光的圆柱体感
                    val curlEdgeWidth = (18f + visualProgress * 64f).dp
                    // 页缘外侧（贴近页面边缘）受光高亮，向卷曲深处渐变为背光阴影
                    val curlBrush = if (draggingToNext) {
                        // 右页外翻：左边界是页缘（亮），向右逐渐变暗
                        Brush.horizontalGradient(
                            listOf(
                                Color.White.copy(alpha = (0.032f + visualProgress * 0.128f).coerceAtMost(0.176f)),
                                Color.Black.copy(alpha = (0.048f + visualProgress * 0.144f).coerceAtMost(0.208f)),
                                Color.Transparent,
                            ),
                        )
                    } else {
                        // 左页外翻：右边界是页缘（亮），向左逐渐变暗
                        Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = (0.048f + visualProgress * 0.144f).coerceAtMost(0.208f)),
                                Color.White.copy(alpha = (0.032f + visualProgress * 0.128f).coerceAtMost(0.176f)),
                            ),
                        )
                    }
                    Box(
                        modifier = Modifier
                            .align(if (draggingToNext) Alignment.CenterEnd else Alignment.CenterStart)
                            .width(curlEdgeWidth)
                            .fillMaxHeight()
                            .background(curlBrush),
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

                val turnShadowStartAlpha = if (profile == TurnProfile.HANDBOOK) 0.048f else 0.06f
                val turnShadowRate = if (profile == TurnProfile.HANDBOOK) 0.272f else 0.34f
                val turnShadowMaxAlpha = if (profile == TurnProfile.HANDBOOK) 0.336f else 0.42f
                val turnShadowMidAlpha = if (profile == TurnProfile.HANDBOOK) 0.08f else 0.10f
                Box(
                    modifier = Modifier
                        .align(if (draggingToNext) Alignment.CenterStart else Alignment.CenterEnd)
                        .fillMaxHeight()
                        .width(turnShadowWidth)
                        .background(
                            if (draggingToNext) {
                                Brush.horizontalGradient(
                                    listOf(
                                        GoaldayDesign.BlackOverlayMedium.copy(alpha = (turnShadowStartAlpha + latePhase * turnShadowRate).coerceAtMost(turnShadowMaxAlpha)),
                                        GoaldayDesign.BlackOverlayMedium.copy(alpha = turnShadowMidAlpha),
                                        Color.Transparent,
                                    ),
                                )
                            } else {
                                Brush.horizontalGradient(
                                    listOf(
                                        Color.Transparent,
                                        GoaldayDesign.BlackOverlayMedium.copy(alpha = turnShadowMidAlpha),
                                        GoaldayDesign.BlackOverlayMedium.copy(alpha = (turnShadowStartAlpha + latePhase * turnShadowRate).coerceAtMost(turnShadowMaxAlpha)),
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
 * 翻页手势检测。
 * edgeRatio >= 0.5f 时为全宽模式（HANDBOOK）：任意位置按下，根据拖动方向决定翻页方向。
 * edgeRatio < 0.5f 时为边缘模式（DEFAULT）：只在左右边缘按下才拦截，避免抢占内容滚动。
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
    val fullWidth = edgeRatio >= 0.5f
    awaitPointerEventScope {
        while (true) {
            val down = awaitFirstDown()
            val width = size.width.toFloat().coerceAtLeast(1f)

            if (fullWidth) {
                // 全宽模式：手动处理事件流，不拦截子元素的点击
                // 只在水平位移超过 touchSlop 时才消费事件，避免阻止按钮点击
                val touchSlop = viewConfiguration.touchSlop
                var startDirection: TurnDirection? = null
                var lastX = down.position.x
                val velocityTracker = VelocityTracker()
                velocityTracker.addPointerInputChange(down)

                var settled = false
                while (!settled) {
                    val event = awaitPointerEvent()
                    val change = event.changes.firstOrNull { it.id == down.id } ?: continue

                    if (change.changedToUp()) {
                        // 指针抬起：如果没有触发翻页，让子元素处理点击
                        if (startDirection != null) {
                            velocityTracker.addPointerInputChange(change)
                            val velocity = velocityTracker.calculateVelocity().x
                            onEnd(velocity)
                        }
                        settled = true
                        break
                    }

                    val totalDx = change.position.x - down.position.x
                    val totalDy = change.position.y - down.position.y

                    if (startDirection == null) {
                        // 还没确定翻页方向
                        if (abs(totalDx) > touchSlop && abs(totalDx) > abs(totalDy)) {
                            startDirection = when {
                                totalDx < 0 && canTurnNext -> TurnDirection.NEXT
                                totalDx > 0 && canTurnPrevious -> TurnDirection.PREVIOUS
                                else -> { settled = true; break }
                            }
                            // 确认翻页：消费事件，开始拖动
                            change.consume()
                            velocityTracker.addPointerInputChange(change)
                            onStart(down.position, startDirection!!)
                            val delta = change.position.x - lastX
                            onDrag(change, delta)
                            lastX = change.position.x
                        } else if (abs(totalDy) > touchSlop) {
                            // 垂直滑动：放弃翻页，让子元素处理滚动
                            settled = true
                            break
                        }
                    } else {
                        // 已确定翻页方向：跟踪拖动
                        change.consume()
                        velocityTracker.addPointerInputChange(change)
                        val delta = change.position.x - lastX
                        onDrag(change, delta)
                        lastX = change.position.x
                    }
                }

                if (startDirection == null) {
                    // 没有触发翻页，继续等待下一次手势
                    continue
                }
            } else {
                // 边缘模式：只在左右边缘按下才拦截
                val edgePx = width * edgeRatio.coerceIn(0.05f, 0.45f)
                val x = down.position.x
                val startDirection = when {
                    canTurnPrevious && x <= edgePx -> TurnDirection.PREVIOUS
                    canTurnNext && x >= width - edgePx -> TurnDirection.NEXT
                    else -> null
                }
                if (startDirection == null) {
                    continue
                }

                var inward = false
                val drag = awaitHorizontalTouchSlopOrCancellation(down.id) { change, overSlop ->
                    inward = when (startDirection) {
                        TurnDirection.NEXT -> overSlop < 0
                        TurnDirection.PREVIOUS -> overSlop > 0
                    }
                    if (inward) change.consume()
                }
                if (drag == null || !inward) {
                    continue
                }

                onStart(down.position, startDirection)

                val velocityTracker = VelocityTracker()
                velocityTracker.addPointerInputChange(drag)

                var previousX = down.position.x
                var currentX = drag.position.x
                onDrag(drag, currentX - previousX)
                previousX = currentX

                val success = horizontalDrag(drag.id) { change ->
                    velocityTracker.addPointerInputChange(change)
                    change.consume()
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
    // 仿真翻页：HANDBOOK 模拟真实书页绕书脊翻越，DEFAULT 保持 118°
    // curlBoost：前 45% 进度加大旋转，让纸张“先翘起来”再翻过，更像真实书页
    val curlBoost = if (profile == TurnProfile.HANDBOOK) {
        val early = 1f - visualProgress.coerceIn(0f, 0.45f) / 0.45f
        early * 0.18f
    } else 0f
    val progressCurve = (visualProgress * 0.22f) + (visualProgress * visualProgress * 0.78f) + curlBoost
    // HANDBOOK 最大旋转略降，避免页面翻转过度、露出过多背面空白
    val maxRotation = if (profile == TurnProfile.HANDBOOK) 96f else 118f
    rotationY = when (direction) {
        TurnDirection.NEXT -> -maxRotation * progressCurve.coerceIn(0f, 1f)
        TurnDirection.PREVIOUS -> maxRotation * progressCurve.coerceIn(0f, 1f)
        null -> 0f
    }
    // HANDBOOK 绕书脊旋转，水平位移尽量小，避免页面“滑出”书壳；主要依靠 rotationY 制造翻越感
    translationX = when {
        draggingToNext -> if (profile == TurnProfile.HANDBOOK) {
            -(progressCurve * 12f)
        } else {
            -(visualProgress * 14f + progressCurve * 68f)
        }
        draggingToPrevious -> if (profile == TurnProfile.HANDBOOK) {
            progressCurve * 12f
        } else {
            visualProgress * 14f + progressCurve * 68f
        }
        else -> 0f
    }
    val yOffsetFactor = (anchorY - 0.5f) * 2f
    translationY = yOffsetFactor * visualProgress * if (profile == TurnProfile.HANDBOOK) 4.2f else 12f
    rotationX = -yOffsetFactor * progressCurve * if (profile == TurnProfile.HANDBOOK) 2.2f else 9.2f
    // HANDBOOK 用更大 cameraDistance 减少 3D 畸变，翻页更干净
    cameraDistance = if (profile == TurnProfile.HANDBOOK) 56f * density else 34f * density
    shadowElevation = if (profile == TurnProfile.HANDBOOK) 14f else 28f
    val subtleDepthScale = if (profile == TurnProfile.HANDBOOK) 1f - visualProgress * 0.015f else 1f - visualProgress * 0.015f
    scaleY = if (profile == TurnProfile.HANDBOOK) {
        subtleDepthScale.coerceIn(0.985f, 1f)
    } else {
        subtleDepthScale.coerceIn(0.965f, 1f)
    }
    // HANDBOOK：翻页末段把正面淡出，避免 content 翻到新页时残留变形残影
    alpha = if (profile == TurnProfile.HANDBOOK) {
        if (visualProgress < 0.85f) {
            (1f - visualProgress * 0.16f).coerceIn(0.84f, 1f)
        } else {
            ((1f - visualProgress) / 0.15f).coerceIn(0f, 1f) * 0.84f
        }
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
    // 背面角度与正面同步，HANDBOOK 模拟真实书页翻越
    val curlBoost = if (profile == TurnProfile.HANDBOOK) {
        val early = 1f - visualProgress.coerceIn(0f, 0.45f) / 0.45f
        early * 0.18f
    } else 0f
    val progressCurve = (visualProgress * 0.22f) + (visualProgress * visualProgress * 0.78f) + curlBoost
    val maxRotation = if (profile == TurnProfile.HANDBOOK) 96f else 118f
    rotationY = when (direction) {
        TurnDirection.NEXT -> -maxRotation * progressCurve.coerceIn(0f, 1f) * 0.92f
        TurnDirection.PREVIOUS -> maxRotation * progressCurve.coerceIn(0f, 1f) * 0.92f
        null -> 0f
    }
    // 背面 translationX 与正面镜像，避免“贴在原地旋转”
    val handbookTailBoost = if (profile == TurnProfile.HANDBOOK) {
        val tail = ((visualProgress - 0.80f) / 0.20f).coerceIn(0f, 1f)
        tail * 12f
    } else {
        0f
    }
    val backShift = if (profile == TurnProfile.HANDBOOK) {
        progressCurve * 34f - handbookTailBoost * 0.60f
    } else {
        visualProgress * 12f + progressCurve * 58f
    }
    translationX = when {
        draggingToNext -> -backShift
        draggingToPrevious -> backShift
        else -> 0f
    }
    val yOffsetFactor = (anchorY - 0.5f) * 2f
    translationY = yOffsetFactor * progressCurve * if (profile == TurnProfile.HANDBOOK) 2.4f else 7.2f
    rotationX = -yOffsetFactor * progressCurve * if (profile == TurnProfile.HANDBOOK) 1.4f else 5.4f
    // HANDBOOK 背面用与正面一致的 cameraDistance
    cameraDistance = if (profile == TurnProfile.HANDBOOK) 56f * density else 34f * density
    val subtleBackScale = if (profile == TurnProfile.HANDBOOK) 1f - visualProgress * 0.014f else 1f - visualProgress * 0.012f
    scaleY = if (profile == TurnProfile.HANDBOOK) {
        subtleBackScale.coerceIn(0.986f, 1f)
    } else {
        subtleBackScale.coerceIn(0.972f, 1f)
    }
    // 背面在中后段渐显，HANDBOOK 末段淡出避免切页残影
    alpha = if (profile == TurnProfile.HANDBOOK) {
        if (visualProgress < 0.82f) {
            (0.74f + visualProgress * 0.24f).coerceIn(0.74f, 0.98f)
        } else {
            ((1f - visualProgress) / 0.18f).coerceIn(0f, 1f) * 0.98f
        }
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
