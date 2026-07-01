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
import com.bf410.goaldaylocal.ui.replica.GoaldayDesign
import kotlinx.coroutines.launch
import kotlin.math.abs

private const val EDGE_GESTURE_RATIO = 0.13f
// P0-1 修复：HANDBOOK 热区从 0.28f 收窄到 0.10f，避免覆盖日程项/池子项的拖放手势区域
// 原值 0.28f 意味着左右各 28% 宽度都算翻页热区，几乎覆盖整页，导致长按拖放被翻页抢占
private const val HANDBOOK_EDGE_GESTURE_RATIO = 0.10f
// P0-1 修复：原值 0.28f 作为 px 阈值（极小，任何微动都触发），改为基于页面宽度的比例
// 0.04f 表示拖动距离需达到页面宽度 4%（约 30px）才判定翻页方向，给长按拖放留出识别空间
private const val HANDBOOK_DRAG_START_RATIO = 0.04f

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
                            dampingRatio = if (profile == TurnProfile.HANDBOOK) 0.9f else 0.9f,
                            stiffness = if (profile == TurnProfile.HANDBOOK) 92f else Spring.StiffnessLow,
                        ),
                    )
                    onFlipNext()
                    if (profile == TurnProfile.HANDBOOK) {
                        // P0-3 修复：删除原 snapTo(0.12f)→animateTo(0f) 二段动画
                        // 原逻辑翻完后跳到 0.12 再回弹到 0，造成"翻完又晃一下"的视觉跳变
                        // 直接归零并清理状态，让翻页在到达 1f 时干净结束
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
                            dampingRatio = if (profile == TurnProfile.HANDBOOK) 0.9f else 0.9f,
                            stiffness = if (profile == TurnProfile.HANDBOOK) 92f else Spring.StiffnessLow,
                        ),
                    )
                    onFlipPrevious()
                    if (profile == TurnProfile.HANDBOOK) {
                        // P0-3 修复：同 CompleteNext，删除二段回弹动画
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
                            dampingRatio = if (profile == TurnProfile.HANDBOOK) 0.98f else 0.84f,
                            stiffness = if (profile == TurnProfile.HANDBOOK) 240f else Spring.StiffnessMediumLow,
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
                .pointerInput(canTurnNext, canTurnPrevious, turnEnabled) {
                    // P0-4 修复：从 key 中移除 pageWidthPx
                    // 原代码 pageWidthPx 作为 key，尺寸变化（如旋转、动态布局）时 pointerInput 重启，
                    // 导致正在进行的手势协程被取消，用户感觉"滑一半手势丢了"
                    // pageWidthPx 是 var，lambda 内通过闭包读取最新值即可，无需作为 key 触发重启
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
                            if (direction == null && !startedAtEdge) {
                                return@detectHorizontalDragGestures
                            }
                            val resolvedDirection = direction ?: resolveDragTurnDirection(
                                profile = profile,
                                dragStartX = dragStartX,
                                pageWidthPx = pageWidthPx,
                                dragAmountPx = dragAmount,
                                edgeGestureRatio = if (profile == TurnProfile.HANDBOOK) HANDBOOK_EDGE_GESTURE_RATIO else EDGE_GESTURE_RATIO,
                                dragStartThreshold = HANDBOOK_DRAG_START_RATIO,
                            ) ?: return@detectHorizontalDragGestures

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
                // P1-2 精简：原 10 层装饰收敛为 3 层核心，消除视觉过载与渲染负担
                // 删除：4 层 depth 阴影、底部/顶部角落 radial 高光、edge 高光窄条/宽条、顶部/底部 linear 高光
                // 保留：center 暗带（书脊感）+ 全屏暗角（深度感）+ turnShadow（翻页核心阴影）
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

fun Modifier.turningPageTransform(
    direction: TurnDirection?,
    visualProgress: Float,
    anchorY: Float,
    profile: TurnProfile = TurnProfile.DEFAULT,
): Modifier = graphicsLayer {
    val draggingToNext = direction == TurnDirection.NEXT
    val draggingToPrevious = direction == TurnDirection.PREVIOUS
    transformOrigin = TransformOrigin(turnTransformOriginX(profile, direction), 0.5f)
    // P0-1 大修：HANDBOOK 翻页角度从 82° 提升到 115°，与 DEFAULT 看齐
    // 原 82° 意味着页面永远翻不过中轴线，像在原地震荡；115° 让页面真正翻越到背面
    val handbookTailBoost = if (profile == TurnProfile.HANDBOOK) {
        val tail = ((visualProgress - 0.82f) / 0.18f).coerceIn(0f, 1f)
        tail * 8f
    } else {
        0f
    }
    val progressCurve = (visualProgress * 0.35f) + (visualProgress * visualProgress * 0.65f)
    val maxRotation = if (profile == TurnProfile.HANDBOOK) 115f + handbookTailBoost else 118f
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
    // P0-1 大修：HANDBOOK translationX 从 ~20px 提升到 ~60px 量级，让水平扫过明显可见
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
    // P0-1 大修：cameraDistance 从 46f 降到 34f，增强 3D 透视感（值越小透视越强）
    cameraDistance = if (profile == TurnProfile.HANDBOOK) 34f * density else 34f * density
    shadowElevation = if (profile == TurnProfile.HANDBOOK) 18f else 28f
    val subtleDepthScale = if (profile == TurnProfile.HANDBOOK) 1f - visualProgress * 0.022f else 1f - visualProgress * 0.015f
    scaleY = if (profile == TurnProfile.HANDBOOK) {
        subtleDepthScale.coerceIn(0.978f, 1f)
    } else {
        subtleDepthScale.coerceIn(0.965f, 1f)
    }
    // P0-1 大修：HANDBOOK alpha 从 0.94-1.0 改为 0.78-1.0，让翻页中后段明显变暗，模拟纸张透视
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
    // P0-1 大修：背面角度从 80° 提升到 115°，与正面同步翻越中轴线
    val handbookTailBoost = if (profile == TurnProfile.HANDBOOK) {
        val tail = ((visualProgress - 0.82f) / 0.18f).coerceIn(0f, 1f)
        tail * 7f
    } else {
        0f
    }
    val progressCurve = (visualProgress * 0.32f) + (visualProgress * visualProgress * 0.68f)
    val maxRotation = if (profile == TurnProfile.HANDBOOK) 115f + handbookTailBoost else 118f
    rotationY = when (direction) {
        TurnDirection.NEXT -> -maxRotation * progressCurve * 0.92f
        TurnDirection.PREVIOUS -> maxRotation * progressCurve * 0.92f
        null -> 0f
    }
    // P0-1 大修：补上 translationX，与正面镜像（背面应该跟随正面一起水平移动）
    // 原 pageBackTransform 无 translationX，背面"贴在原地旋转"与正面脱节
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
    // P0-1 大修：cameraDistance 与正面对齐
    cameraDistance = if (profile == TurnProfile.HANDBOOK) 34f * density else 34f * density
    val subtleBackScale = if (profile == TurnProfile.HANDBOOK) 1f - visualProgress * 0.018f else 1f - visualProgress * 0.012f
    scaleY = if (profile == TurnProfile.HANDBOOK) {
        subtleBackScale.coerceIn(0.982f, 1f)
    } else {
        subtleBackScale.coerceIn(0.972f, 1f)
    }
    // P0-1 大修：补上 alpha 变化，让背面在中后段渐显（模拟纸张背面从暗到亮）
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
