package com.bf410.goaldaylocal.ui.book

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.size
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.Image
import com.bf410.goaldaylocal.R
import com.bf410.goaldaylocal.ui.replica.GoaldayDesign
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * 基础书本视图
 * 实现6页旋转系统的核心视图组件
 * 
 * @param pageState 页面状态管理器
 * @param pageContent 页面内容渲染器
 */
@Composable
fun BaseBookView(
    modifier: Modifier = Modifier,
    pageState: CircularCalendarPageState,
    pageContent: @Composable (CalendarPage, Int) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val configurator = remember { BookPageAnimationConfigurator() }
    
    // 翻页进度动画
    val flipProgress = remember { Animatable(0f) }
    var flipDirection by remember { mutableStateOf<TurnDirection?>(null) }
    var isDragging by remember { mutableStateOf(false) }
    
    // 获取当前所有页面
    val pages = pageState.getCurrentPages()
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(pageState) {
                detectHorizontalDragGestures(
                    onDragStart = {
                        isDragging = true
                        configurator.startAnimation()
                    },
                    onDragEnd = {
                        isDragging = false
                        coroutineScope.launch {
                            val currentProgress = flipProgress.value
                            val shouldComplete = abs(currentProgress) > 0.3f
                            
                            if (shouldComplete) {
                                // 完成翻页
                                val targetValue = if (currentProgress > 0) 1f else -1f
                                flipProgress.animateTo(
                                    targetValue = targetValue,
                                    animationSpec = tween(durationMillis = 300)
                                )
                                
                                // 执行翻页逻辑
                                when {
                                    currentProgress > 0 && pageState.canGoPrevious() -> {
                                        pageState.goPreviousPage()
                                    }
                                    currentProgress < 0 && pageState.canGoNext() -> {
                                        pageState.goNextPage()
                                    }
                                }
                            } else {
                                // 回弹到初始位置
                                flipProgress.animateTo(
                                    targetValue = 0f,
                                    animationSpec = tween(durationMillis = 200)
                                )
                            }
                            
                            configurator.setIdle()
                            flipDirection = null
                        }
                    },
                    onDragCancel = {
                        isDragging = false
                        coroutineScope.launch {
                            flipProgress.animateTo(
                                targetValue = 0f,
                                animationSpec = tween(durationMillis = 200)
                            )
                            configurator.setIdle()
                            flipDirection = null
                        }
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        val dragProgress = (dragAmount / size.width).coerceIn(-1f, 1f)
                        
                        // 判断翻页方向
                        if (flipDirection == null) {
                            flipDirection = when {
                                dragProgress > 0 && pageState.canGoPrevious() -> TurnDirection.PREVIOUS
                                dragProgress < 0 && pageState.canGoNext() -> TurnDirection.NEXT
                                else -> null
                            }
                        }
                        
                        // 更新翻页进度
                        coroutineScope.launch {
                            val newProgress = when (flipDirection) {
                                TurnDirection.PREVIOUS -> (flipProgress.value + dragProgress).coerceIn(0f, 1f)
                                TurnDirection.NEXT -> (flipProgress.value + dragProgress).coerceIn(-1f, 0f)
                                null -> 0f
                            }
                            flipProgress.snapTo(newProgress)
                        }
                    }
                )
            }
    ) {
        // 计算当前所有页面的旋转角度
        val progress = abs(flipProgress.value)
        val rotations = configurator.calculateAllRotations(progress)
        
        // 渲染6个页面层
        // 层1: 封面（最底层）
        BookPageLayer(
            modifier = Modifier.fillMaxSize(),
            rotationY = rotations.frontRotation,
            zIndex = 0f
        ) {
            pageContent(pages[0], 0)
        }
        
        // 层2: 第1页
        BookPageLayer(
            modifier = Modifier.fillMaxSize(),
            rotationY = rotations.pageOneRotation,
            zIndex = 1f
        ) {
            pageContent(pages[1], 1)
        }
        
        // 层3: 第2页
        BookPageLayer(
            modifier = Modifier.fillMaxSize(),
            rotationY = rotations.pageTwoRotation,
            zIndex = 2f
        ) {
            pageContent(pages[2], 2)
        }
        
        // 层4: 第3页
        BookPageLayer(
            modifier = Modifier.fillMaxSize(),
            rotationY = rotations.pageThreeRotation,
            zIndex = 3f
        ) {
            pageContent(pages[3], 3)
        }
        
        // 层5: 第4页
        BookPageLayer(
            modifier = Modifier.fillMaxSize(),
            rotationY = rotations.pageFourRotation,
            zIndex = 4f
        ) {
            pageContent(pages[4], 4)
        }
        
        // 层6: 最后一页（最顶层）
        BookPageLayer(
            modifier = Modifier.fillMaxSize(),
            rotationY = rotations.lastRotation,
            zIndex = 5f
        ) {
            pageContent(pages[9], 9)
        }
    }
}

/**
 * 书本页面层
 * 用于渲染单个页面，支持3D旋转和真实纸张效果
 */
@Composable
private fun BookPageLayer(
    modifier: Modifier = Modifier,
    rotationY: Float,
    zIndex: Float,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .graphicsLayer {
                this.rotationY = rotationY
                this.cameraDistance = 12f * density
                // 根据旋转角度调整透明度，模拟纸张背面
                this.alpha = if (abs(rotationY) > 90f) 0f else 1f
                // 添加轻微阴影增强立体感
                this.shadowElevation = if (abs(rotationY) > 5f) 4f else 0f
            }
            .fillMaxSize()
            .drawBehind {
                // 绘制纸张边缘阴影，增强立体感
                val shadowAlpha = (abs(rotationY) / 180f).coerceIn(0f, 0.3f)
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = shadowAlpha),
                            Color.Transparent
                        ),
                        startX = 0f,
                        endX = 20f
                    ),
                    size = androidx.compose.ui.geometry.Size(20f, size.height)
                )
                
                // 右侧边缘高光，模拟纸张反光
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.05f)
                        ),
                        startX = size.width - 10f,
                        endX = size.width
                    ),
                    size = androidx.compose.ui.geometry.Size(10f, size.height)
                )
            }
    ) {
        content()
    }
}

/**
 * 简化版书本视图（不使用6页旋转系统）
 * 用于快速集成，只渲染当前页面
 */
@Composable
fun SimpleBookView(
    modifier: Modifier = Modifier,
    pageState: CircularCalendarPageState,
    pageContent: @Composable (CalendarPage) -> Unit
) {
    val currentPage = pageState.getPage(pageState.currentPageIndex)
    
    Box(modifier = modifier.fillMaxSize()) {
        pageContent(currentPage)
    }
}
