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
// HANDBOOK 模式：全宽翻页（任意位置水平滑动均可翻页）。
// 原版 APK 书内页使用 NoTouchConstraintLayout(clickable=false)，翻页靠外层 ViewPager2，
// 所以任意位置都能翻页。HANDBOOK 模式下子元素已禁用滑动删除，不存在手势冲突。
private const val HANDBOOK_EDGE_GESTURE_RATIO = 0.5f
// HANDBOOK 拖动阈值：约 14px（按 360px 宽度计），翻页更跟手且不易误触
private const val HANDBOOK_DRAG_START_RATIO = 0.038f

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
        (16f + latePhase * 120f).dp
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
                        animationSpec = if (profile == TurnProfile.HANDBOOK) {
                            // 对照原版 BaseBookViewKt：animationTotalDuration = 250ms
                            tween(durationMillis = 250)
                        } else {
                            spring(
                                dampingRatio = 0.9f,
                                stiffness = Spring.StiffnessLow,
                            )
                        },
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
                        animationSpec = if (profile == TurnProfile.HANDBOOK) {
                            // 对照原版 BaseBookViewKt：animationTotalDuration = 250ms
                            tween(durationMillis = 250)
                        } else {
                            spring(
                                dampingRatio = 0.9f,
                                stiffness = Spring.StiffnessLow,
                            )
                        },
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
                        animationSpec = if (profile == TurnProfile.HANDBOOK) {
                            // 对照原版：回弹也用 250ms tween
                            tween(durationMillis = 250)
                        } else {
                            spring(
                                dampingRatio = 0.84f,
                                stiffness = Spring.StiffnessMediumLow,
                            )
                        },
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
                    // 书脊处纸张弯折的圆柱体阴影：中间暗、两侧受光，但颜色很淡
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .width((12f + visualProgress * 18f).dp)
                            .fillMaxHeight()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        GoaldayDesign.BlackOverlayMedium.copy(alpha = (0.04f + latePhase * 0.12f).coerceAtMost(0.16f)),
                                        Color.White.copy(alpha = (0.03f + latePhase * 0.08f).coerceAtMost(0.10f)),
                                        GoaldayDesign.BlackOverlayMedium.copy(alpha = (0.04f + latePhase * 0.12f).coerceAtMost(0.16f)),
                                    ),
                                ),
                            ),
                    )
                    // 翻页页缘卷曲高光/阴影：模拟纸张弯折时外侧受光、内侧背光的圆柱体感
                    val curlEdgeWidth = (20f + visualProgress * 72f).dp
                    // 页缘外侧（贴近页面边缘）受光高亮，向卷曲深处渐变为背光阴影
                    val curlBrush = if (draggingToNext) {
                        // 右页外翻：左边界是页缘（亮），向右逐渐变暗
                        Brush.horizontalGradient(
                            listOf(
                                Color.White.copy(alpha = (0.05f + visualProgress * 0.14f).coerceAtMost(0.18f)),
                                Color.Black.copy(alpha = (0.06f + visualProgress * 0.16f).coerceAtMost(0.22f)),
                                Color.Transparent,
                            ),
                        )
                    } else {
                        // 左页外翻：右边界是页缘（亮），向左逐渐变暗
                        Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = (0.06f + visualProgress * 0.16f).coerceAtMost(0.22f)),
                                Color.White.copy(alpha = (0.05f + visualProgress * 0.14f).coerceAtMost(0.18f)),
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
                                    Color.Black.copy(alpha = (0.004f + earlyPhase * 0.010f + latePhase * 0.06f).coerceAtMost(0.08f)),
                                ),
                                radius = 2200f,
                            ),
                        ),
                )

                val turnShadowStartAlpha = if (profile == TurnProfile.HANDBOOK) 0.05f else 0.06f
                val turnShadowRate = if (profile == TurnProfile.HANDBOOK) 0.26f else 0.34f
                val turnShadowMaxAlpha = if (profile == TurnProfile.HANDBOOK) 0.30f else 0.42f
                val turnShadowMidAlpha = if (profile == TurnProfile.HANDBOOK) 0.07f else 0.10f
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
 * edgeRatio >= 0.5f 时为全宽模式：任意位置按下，根据拖动方向决定翻页方向。
 * edgeRatio < 0.5f 时为边缘模式：只在左右边缘按下才拦截，避免抢占内容滚动。
 * HANDBOOK 使用 0.25f，在左右边缘保留较宽翻页热区，中间留给子元素滑动删除/点击。
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
            // 等待手指按下
            val down = awaitFirstDown(requireUnconsumed = false)
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
                            down.consume()
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

                // 确认翻页：消费 down，开始拖动
                down.consume()

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
    // 仿真翻页：HANDBOOK 模拟真实书页绕书脊翻越。
    // curlBoost：前 35% 进度加大旋转，让纸张先翘起再翻过，更像真实书页。
    val curlBoost = if (profile == TurnProfile.HANDBOOK) {
        val early = 1f - visualProgress.coerceIn(0f, 0.35f) / 0.35f
        early * 0.22f
    } else 0f
    val progressCurve = (visualProgress * 0.22f) + (visualProgress * visualProgress * 0.78f) + curlBoost
    // HANDBOOK 最大旋转 95°：书页沿书脊翻越，接近垂直时背面已可见，翻完自然
    val maxRotation = if (profile == TurnProfile.HANDBOOK) 95f else 118f
    rotationY = when (direction) {
        TurnDirection.NEXT -> -maxRotation * progressCurve.coerceIn(0f, 1f)
        TurnDirection.PREVIOUS -> maxRotation * progressCurve.coerceIn(0f, 1f)
        null -> 0f
    }
    // HANDBOOK 绕书脊旋转，水平位移尽量小，避免页面“滑出”书壳；只留极少量跟随位移
    translationX = when {
        draggingToNext -> if (profile == TurnProfile.HANDBOOK) {
            -(progressCurve * 10f)
        } else {
            -(visualProgress * 14f + progressCurve * 68f)
        }
        draggingToPrevious -> if (profile == TurnProfile.HANDBOOK) {
            progressCurve * 10f
        } else {
            visualProgress * 14f + progressCurve * 68f
        }
        else -> 0f
    }
    val yOffsetFactor = (anchorY - 0.5f) * 2f
    translationY = yOffsetFactor * visualProgress * if (profile == TurnProfile.HANDBOOK) 3.6f else 12f
    rotationX = -yOffsetFactor * progressCurve * if (profile == TurnProfile.HANDBOOK) 1.8f else 9.2f
    // 对照原版 BaseBookViewKt：cameraDistance = 40 × density
    cameraDistance = if (profile == TurnProfile.HANDBOOK) 40f * density else 34f * density
    shadowElevation = if (profile == TurnProfile.HANDBOOK) 10f else 28f
    // 模拟纸张翻起时近大远小的轻微透视压缩
    val subtleDepthScale = 1f - visualProgress * 0.012f
    scaleY = if (profile == TurnProfile.HANDBOOK) {
        subtleDepthScale.coerceIn(0.988f, 1f)
    } else {
        subtleDepthScale.coerceIn(0.965f, 1f)
    }
    scaleX = if (profile == TurnProfile.HANDBOOK) {
        (1f - visualProgress * 0.025f).coerceIn(0.975f, 1f)
    } else {
        (1f - visualProgress * 0.015f).coerceIn(0.985f, 1f)
    }
    // HANDBOOK：翻页末段把正面淡出，避免 content 翻到新页时残留变形残影
    alpha = if (profile == TurnProfile.HANDBOOK) {
        if (visualProgress < 0.80f) {
            (1f - visualProgress * 0.10f).coerceIn(0.90f, 1f)
        } else {
            ((1f - visualProgress) / 0.20f).coerceIn(0f, 1f) * 0.90f
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
        val early = 1f - visualProgress.coerceIn(0f, 0.35f) / 0.35f
        early * 0.22f
    } else 0f
    val progressCurve = (visualProgress * 0.22f) + (visualProgress * visualProgress * 0.78f) + curlBoost
    val maxRotation = if (profile == TurnProfile.HANDBOOK) 95f else 118f
    rotationY = when (direction) {
        TurnDirection.NEXT -> -maxRotation * progressCurve.coerceIn(0f, 1f) * 0.92f
        TurnDirection.PREVIOUS -> maxRotation * progressCurve.coerceIn(0f, 1f) * 0.92f
        null -> 0f
    }
    // 背面 translationX 与正面镜像，但 HANDBOOK 位移要很小，背面只是从书脊后露出一小部分
    val handbookTailBoost = if (profile == TurnProfile.HANDBOOK) {
        val tail = ((visualProgress - 0.75f) / 0.25f).coerceIn(0f, 1f)
        tail * 8f
    } else {
        0f
    }
    val backShift = if (profile == TurnProfile.HANDBOOK) {
        progressCurve * 22f - handbookTailBoost * 0.50f
    } else {
        visualProgress * 12f + progressCurve * 58f
    }
    translationX = when {
        draggingToNext -> -backShift
        draggingToPrevious -> backShift
        else -> 0f
    }
    val yOffsetFactor = (anchorY - 0.5f) * 2f
    translationY = yOffsetFactor * progressCurve * if (profile == TurnProfile.HANDBOOK) 1.4f else 7.2f
    rotationX = -yOffsetFactor * progressCurve * if (profile == TurnProfile.HANDBOOK) 0.9f else 5.4f
    // HANDBOOK 背面用与正面一致的 cameraDistance（对照原版 40 × density）
    cameraDistance = if (profile == TurnProfile.HANDBOOK) 40f * density else 34f * density
    val subtleBackScale = 1f - visualProgress * 0.012f
    scaleY = if (profile == TurnProfile.HANDBOOK) {
        subtleBackScale.coerceIn(0.988f, 1f)
    } else {
        subtleBackScale.coerceIn(0.972f, 1f)
    }
    scaleX = if (profile == TurnProfile.HANDBOOK) {
        (1f - visualProgress * 0.025f).coerceIn(0.975f, 1f)
    } else {
        (1f - visualProgress * 0.015f).coerceIn(0.985f, 1f)
    }
    // 背面在中后段渐显，HANDBOOK 末段淡出避免切页残影
    alpha = if (profile == TurnProfile.HANDBOOK) {
        if (visualProgress < 0.80f) {
            (0.72f + visualProgress * 0.24f).coerceIn(0.72f, 0.96f)
        } else {
            ((1f - visualProgress) / 0.20f).coerceIn(0f, 1f) * 0.96f
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
