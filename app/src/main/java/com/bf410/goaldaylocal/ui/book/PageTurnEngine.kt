package com.bf410.goaldaylocal.ui.book

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * 翻页状态控制器：把可变状态与翻页方法集中到一个类，
 * 让 [PageTurnEngine] 只负责渲染、控制器负责逻辑。
 *
 * 对照原版 BaseBookViewKt 的 state 管理：
 * - [progress]：0f..1f，0=未翻，1=翻完。
 * - [direction]：null=空闲，NEXT/PREVIOUS=正在翻。
 * - [pageWidthPx]/[pageHeightPx]：由 onSizeChanged 回写，用于计算拖动进度。
 *
 * 原先散落的 dragStartX / lastVelocityPxPerSecond / lastEventTimeMs 均为只写不读的死状态，已删除。
 * 原先的 TurnPhase 枚举仅用于 Idle 判断，已简化为 [isTurning] 布尔。
 */
class TurnController(
    val profile: TurnProfile,
    private val scope: CoroutineScope,
) {
    var canTurnPrevious: () -> Boolean = { false }
    var canTurnNext: () -> Boolean = { false }
    var onFlipNext: () -> Unit = {}
    var onFlipPrevious: () -> Unit = {}
    // 对照原版 BaseBookViewKt.java L232-253 的 bookIsOpened 状态：
    // 手账首次进入时书闭合（false），翻过一次后置为 true，阈值由 0.5 降到 0.3。
    // 非手账模式 useDualStateThreshold=false，此值被忽略。
    var bookIsOpen: () -> Boolean = { false }

    val progress = Animatable(0f)
    var direction by mutableStateOf<TurnDirection?>(null)
        private set
    var turnAnchorY by mutableFloatStateOf(0.5f)
        private set
    var pageWidthPx by mutableFloatStateOf(1f)
    var pageHeightPx by mutableFloatStateOf(1f)

    // 对照原版 BaseBookViewKt：6-page rotation 配置器，用于 HANDBOOK 模式翻页曲线。
    val configurator = BookPageAnimationConfigurator()

    private var isTurning = false

    val dragProgress: Float get() = progress.value.coerceIn(0f, 1f)
    val visualProgress: Float get() = profile.visualProgress(dragProgress)

    fun clearState() {
        direction = null
        isTurning = false
        turnAnchorY = 0.5f
        configurator.idle()
    }

    fun settle(result: TurnReleaseResult) {
        scope.launch {
            // 对照原版 BaseBookViewKt$BaseBookView$4/5.java L64：
            // 自适应时长需要在 animateTo 之前读取当前 progress
            val currentProgress = progress.value
            when (result) {
                TurnReleaseResult.CompleteNext -> {
                    isTurning = true
                    progress.animateTo(1f, animationSpec = profile.forwardSettleSpec(currentProgress))
                    onFlipNext()
                    if (profile == TurnProfile.HANDBOOK) {
                        // 翻到位后直接归零并清理状态，避免“翻完又晃一下”
                        progress.snapTo(0f)
                        // 对照原版：动画结束后延迟 10ms 再重新启用手势，避免动画结束瞬间触发新翻页
                        delay(10)
                        clearState()
                        return@launch
                    }
                }
                TurnReleaseResult.CompletePrevious -> {
                    isTurning = true
                    progress.animateTo(1f, animationSpec = profile.forwardSettleSpec(currentProgress))
                    onFlipPrevious()
                    if (profile == TurnProfile.HANDBOOK) {
                        progress.snapTo(0f)
                        delay(10)
                        clearState()
                        return@launch
                    }
                }
                TurnReleaseResult.SnapBack -> {
                    isTurning = true
                    progress.animateTo(0f, animationSpec = profile.snapBackSettleSpec(currentProgress))
                    if (profile == TurnProfile.HANDBOOK) {
                        delay(10)
                    }
                }
            }
            progress.snapTo(0f)
            clearState()
        }
    }

    fun beginTapTurn(targetDirection: TurnDirection) {
        direction = targetDirection
        turnAnchorY = 0.5f
        isTurning = true
        // 对照原版 BookPageAnimationConfigurator：进入拖动/动画态，不记录方向。
        configurator.start()
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
        if (isTurning) return
        direction = startDirection
        turnAnchorY = (startOffset.y / pageHeightPx.coerceAtLeast(1f)).coerceIn(0f, 1f)
        isTurning = true
        // 对照原版 BookPageAnimationConfigurator：进入拖动/动画态，不记录方向。
        configurator.start()
        scope.launch { progress.snapTo(0f) }
    }

    fun updateDragTurn(deltaX: Float) {
        val currentDirection = direction ?: return
        val canTurn = when (currentDirection) {
            TurnDirection.NEXT -> canTurnNext()
            TurnDirection.PREVIOUS -> canTurnPrevious()
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
            hasPreviousPage = canTurnPrevious(),
            hasNextPage = canTurnNext(),
            profile = profile,
            bookIsOpen = bookIsOpen(),
        )
        settle(result)
    }

    fun cancel() {
        if (progress.value > 0.01f) {
            settle(TurnReleaseResult.SnapBack)
        } else {
            clearState()
            scope.launch { progress.snapTo(0f) }
        }
    }
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
    // 对照原版 BaseBookViewKt.java L232-253 的 bookIsOpened：
    // 手账首次进入时 false（阈值0.5），翻过一次后 true（阈值0.3）。
    // 非手账模式 useDualStateThreshold=false，此值被忽略。
    bookIsOpen: Boolean = false,
    // 可选引用，用于向调用方暴露 TurnController（含 6-page rotation configurator）。
    controllerRef: androidx.compose.runtime.MutableState<TurnController?>? = null,
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
    val controller = remember(profile) { TurnController(profile = profile, scope = scope) }
    controller.canTurnPrevious = { canTurnPrevious }
    controller.canTurnNext = { canTurnNext }
    controller.onFlipNext = onFlipNext
    controller.onFlipPrevious = onFlipPrevious
    controller.bookIsOpen = { bookIsOpen }
    if (controllerRef != null) {
        androidx.compose.runtime.SideEffect { controllerRef.value = controller }
    }

    val dragProgress = controller.dragProgress
    val visualProgress = controller.visualProgress
    val direction = controller.direction
    val turnAnchorY = controller.turnAnchorY
    val commitProgress = ((visualProgress - 0.22f) / 0.78f).coerceIn(0f, 1f)
    val latePhase = commitProgress * commitProgress
    val earlyPhase = (1f - commitProgress).coerceIn(0f, 1f)
    val draggingToNext = direction == TurnDirection.NEXT
    val draggingToPrevious = direction == TurnDirection.PREVIOUS
    val turnShadowWidth = (profile.turnShadowWidthBase + latePhase * profile.turnShadowWidthStep).dp

    shell(
        canTurnPrevious,
        canTurnNext,
        turnEnabled,
        { controller.beginTapTurn(TurnDirection.PREVIOUS) },
        { controller.beginTapTurn(TurnDirection.NEXT) },
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .onSizeChanged {
                    controller.pageWidthPx = it.width.toFloat().coerceAtLeast(1f)
                    controller.pageHeightPx = it.height.toFloat().coerceAtLeast(1f)
                }
                .pointerInput(turnEnabled, canTurnPrevious, canTurnNext, profile) {
                    if (!turnEnabled) return@pointerInput
                    detectEdgePageTurnGestures(
                        canTurnNext = canTurnNext,
                        canTurnPrevious = canTurnPrevious,
                        edgeRatio = profile.edgeGestureRatio,
                        onStart = { offset, dir -> controller.startDragTurn(offset, dir) },
                        onDrag = { _, deltaX -> controller.updateDragTurn(deltaX) },
                        onEnd = { velocity -> controller.releaseDragTurn(velocity) },
                        onCancel = { controller.cancel() },
                    )
                },
        ) {
            spine(visualProgress, direction != null)

            // 目标页/背面：所有模式都渲染，HANDBOOK 模式下由 BookReader 提供带真实内容的背面，
            // 避免翻页过程中出现空白背景。
            if (direction != null) {
                destination(dragProgress, direction)
            }

            if (direction != null && dragProgress > 0.01f) {
                pageBack(visualProgress, direction, turnAnchorY)
            }

            activePage(visualProgress, direction, turnAnchorY)

            if (direction != null && dragProgress > 0.01f && profile.renderTurnShadow) {
                // 非手账模式：保留原来的仿真阴影与暗角
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

                Box(
                    modifier = Modifier
                        .align(if (draggingToNext) Alignment.CenterStart else Alignment.CenterEnd)
                        .fillMaxHeight()
                        .width(turnShadowWidth)
                        .background(
                            if (draggingToNext) {
                                Brush.horizontalGradient(
                                    listOf(
                                        GoaldayDesign.BlackOverlayMedium.copy(alpha = (0.06f + latePhase * 0.34f).coerceAtMost(0.42f)),
                                        GoaldayDesign.BlackOverlayMedium.copy(alpha = 0.10f),
                                        Color.Transparent,
                                    ),
                                )
                            } else {
                                Brush.horizontalGradient(
                                    listOf(
                                        Color.Transparent,
                                        GoaldayDesign.BlackOverlayMedium.copy(alpha = 0.10f),
                                        GoaldayDesign.BlackOverlayMedium.copy(alpha = (0.06f + latePhase * 0.34f).coerceAtMost(0.42f)),
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
 * HANDBOOK 使用 0.5f，与原版 NoTouchConstraintLayout + 外层 ViewPager2 的全宽翻页行为一致。
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
    val width = size.width.toFloat().coerceAtLeast(1f)

    if (fullWidth) {
        // 全宽模式：任意位置按下均可翻页，但子元素（horizontalScroll / chip 行 / 横向列表）
        // 优先消费。只有子节点未消费水平移动时，外层才接管翻页，避免抢占子元素滚动。
        awaitPointerEventScope {
            while (true) {
                val down = awaitFirstDown(requireUnconsumed = false)
                // down 不消费，让子节点有机会处理（点击按钮、勾选框、滚动列表等）
                var turnDirection: TurnDirection? = null
                val startX = down.position.x
                val startY = down.position.y
                var lastX = startX
                val velocityTracker = VelocityTracker()
                velocityTracker.resetTracking()
                velocityTracker.addPointerInputChange(down)

                var finished = false
                while (!finished) {
                    val event = awaitPointerEvent()
                    val change = event.changes.firstOrNull { it.id == down.id } ?: continue

                    if (change.changedToUp()) {
                        if (turnDirection != null) {
                            onEnd(velocityTracker.calculateVelocity().x)
                        }
                        finished = true
                        break
                    }

                    // 子节点已消费事件（如 horizontalScroll 正在滚动），外层放弃翻页
                    if (change.isConsumed) {
                        if (turnDirection != null) {
                            onCancel()
                        }
                        finished = true
                        break
                    }

                    val totalDx = change.position.x - startX
                    val totalDy = change.position.y - startY
                    val dx = change.position.x - lastX

                    if (turnDirection == null) {
                        // 判定方向：水平位移超过 touchSlop 且水平主导
                        if (abs(totalDx) > viewConfiguration.touchSlop && abs(totalDx) > abs(totalDy)) {
                            turnDirection = when {
                                totalDx < 0 && canTurnNext -> TurnDirection.NEXT
                                totalDx > 0 && canTurnPrevious -> TurnDirection.PREVIOUS
                                else -> null
                            }
                            if (turnDirection != null) {
                                onStart(Offset(startX, change.position.y), turnDirection!!)
                                change.consume()
                            }
                        }
                    }

                    if (turnDirection != null) {
                        velocityTracker.addPointerInputChange(change)
                        onDrag(change, dx)
                        change.consume()
                    }
                    lastX = change.position.x
                }
            }
        }
    } else {
        awaitPointerEventScope {
            while (true) {
                // 等待手指按下
                val down = awaitFirstDown(requireUnconsumed = false)

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
    configurator: BookPageAnimationConfigurator? = null,
): Modifier = graphicsLayer {
    val draggingToNext = direction == TurnDirection.NEXT
    val draggingToPrevious = direction == TurnDirection.PREVIOUS
    transformOrigin = TransformOrigin(profile.transformOriginX(direction), 0.5f)
    // 对照原版 BaseBookViewKt：
    // - HANDBOOK 优先使用 6-page rotation 配置器曲线
    // - DEFAULT 保留仿真曲线和 curlBoost
    val rawRotation = if (profile.useSpineOnlyTransform && configurator != null) {
        kotlin.math.abs(configurator.handbookPageRotationY(direction, visualProgress))
    } else {
        val curlBoost = if (profile.useSpineOnlyTransform) {
            0f
        } else {
            val early = 1f - visualProgress.coerceIn(0f, 0.35f) / 0.35f
            early * 0.22f
        }
        val progressCurve = if (profile.useSpineOnlyTransform) {
            visualProgress
        } else {
            (visualProgress * 0.22f) + (visualProgress * visualProgress * 0.78f) + curlBoost
        }
        profile.maxRotation * progressCurve.coerceIn(0f, 1f)
    }
    rotationY = when (direction) {
        TurnDirection.NEXT -> -rawRotation
        TurnDirection.PREVIOUS -> rawRotation
        null -> 0f
    }
    // HANDBOOK 纯绕书脊 180° 翻转：移除额外位移/缩放/旋转，保持原版 APK 平面书页感
    translationX = if (profile.useSpineOnlyTransform) 0f else when {
        draggingToNext -> -(visualProgress * 14f + rawRotation / profile.maxRotation * 68f)
        draggingToPrevious -> visualProgress * 14f + rawRotation / profile.maxRotation * 68f
        else -> 0f
    }
    val yOffsetFactor = (anchorY - 0.5f) * 2f
    translationY = if (profile.useSpineOnlyTransform) 0f else yOffsetFactor * visualProgress * 12f
    rotationX = if (profile.useSpineOnlyTransform) 0f else -yOffsetFactor * (rawRotation / profile.maxRotation) * 9.2f
    // 对照原版 BaseBookViewKt：cameraDistance = 40 × density
    cameraDistance = profile.cameraDistanceDensity * density
    shadowElevation = profile.shadowElevation
    // 模拟纸张翻起时近大远小的轻微透视压缩
    val subtleDepthScale = 1f - visualProgress * 0.012f
    scaleY = if (profile.useSpineOnlyTransform) 1f else subtleDepthScale.coerceIn(0.965f, 1f)
    scaleX = if (profile.useSpineOnlyTransform) 1f else (1f - visualProgress * 0.015f).coerceIn(0.985f, 1f)
    // 对照原版 BaseBookViewKt：Alpha 硬切
    // (-rotation) <= 90° → 正面可见(alpha=1)；(-rotation) > 90° → 正面不可见(alpha=0)
    alpha = profile.frontAlpha(rawRotation, visualProgress)
}

fun Modifier.pageBackTransform(
    direction: TurnDirection?,
    visualProgress: Float,
    anchorY: Float,
    profile: TurnProfile = TurnProfile.DEFAULT,
    configurator: BookPageAnimationConfigurator? = null,
): Modifier = graphicsLayer {
    val draggingToNext = direction == TurnDirection.NEXT
    val draggingToPrevious = direction == TurnDirection.PREVIOUS
    transformOrigin = TransformOrigin(profile.transformOriginX(direction), 0.5f)
    // 对照原版 BaseBookViewKt：
    // 背面有固定 180° 基础旋转（setRotationY(180)），再随正面一起旋转
    // HANDBOOK：优先使用 6-page rotation 配置器曲线
    // DEFAULT：保留原仿真曲线
    val frontRotation = if (profile.useSpineOnlyTransform && configurator != null) {
        kotlin.math.abs(configurator.handbookPageRotationY(direction, visualProgress))
    } else {
        val curlBoost = if (profile.useSpineOnlyTransform) {
            0f
        } else {
            val early = 1f - visualProgress.coerceIn(0f, 0.35f) / 0.35f
            early * 0.22f
        }
        val progressCurve = if (profile.useSpineOnlyTransform) {
            visualProgress
        } else {
            (visualProgress * 0.22f) + (visualProgress * visualProgress * 0.78f) + curlBoost
        }
        profile.maxRotation * progressCurve.coerceIn(0f, 1f)
    }
    // HANDBOOK：背面 = 180° - frontRotation（NEXT）/ -(180° - frontRotation)（PREVIOUS）
    rotationY = if (profile.useSpineOnlyTransform) {
        val backRotation = 180f - frontRotation
        when (direction) {
            TurnDirection.NEXT -> backRotation
            TurnDirection.PREVIOUS -> -backRotation
            null -> 0f
        }
    } else {
        when (direction) {
            TurnDirection.NEXT -> -frontRotation * 0.92f
            TurnDirection.PREVIOUS -> frontRotation * 0.92f
            null -> 0f
        }
    }
    // HANDBOOK 背面同样纯绕书脊 180° 翻转：无额外位移/缩放/旋转
    translationX = if (profile.useSpineOnlyTransform) 0f else when {
        draggingToNext -> -(visualProgress * 12f + frontRotation / profile.maxRotation * 58f)
        draggingToPrevious -> visualProgress * 12f + frontRotation / profile.maxRotation * 58f
        else -> 0f
    }
    val yOffsetFactor = (anchorY - 0.5f) * 2f
    translationY = if (profile.useSpineOnlyTransform) 0f else yOffsetFactor * (frontRotation / profile.maxRotation) * 7.2f
    rotationX = if (profile.useSpineOnlyTransform) 0f else -yOffsetFactor * (frontRotation / profile.maxRotation) * 5.4f
    // HANDBOOK 背面用与正面一致的 cameraDistance（对照原版 40 × density）
    cameraDistance = profile.cameraDistanceDensity * density
    val subtleBackScale = 1f - visualProgress * 0.012f
    scaleY = if (profile.useSpineOnlyTransform) 1f else subtleBackScale.coerceIn(0.972f, 1f)
    scaleX = if (profile.useSpineOnlyTransform) 1f else (1f - visualProgress * 0.015f).coerceIn(0.985f, 1f)
    // 对照原版 BaseBookViewKt：背面 Alpha 硬切
    // (-rotation) > 90° → 背面可见(alpha=1)；(-rotation) <= 90° → 背面不可见(alpha=0)
    alpha = profile.backAlpha(frontRotation, visualProgress)
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
