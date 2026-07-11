package com.bf410.goaldaylocal.ui.book

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import android.app.Activity
import android.graphics.Rect
import android.os.Build
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.paint
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.bf410.goaldaylocal.R
import com.bf410.goaldaylocal.ui.replica.GoaldayDesign
import com.bf410.goaldaylocal.ui.replica.LocalGoaldayDarkMode

/**
 * 书页纸张色：始终使用浅色纸张，模拟真实手账页面。
 * 即使在系统暗色模式下，书内页也保持米白/奶白色，避免深色背景破坏书页质感。
 */
private val BookPagePaper = GoaldayDesign.Paper

enum class ShellStyle {
    LIGHT,
    BOOK,
}

/**
 * 书壳：模拟一本真正打开的纸质手账。
 *
 * 设计要点：
 * - 外层为硬壳书皮，使用不透明暖棕色渐变 + 布纹贴图，四周留出明显书边。
 * - 书口处用多重细线模拟一叠纸页的厚度。
 * - 中央/左侧书脊加粗阴影，营造纸张弯入装订处的立体感。
 * - 右下角微微卷起，增加真实纸张质感。
 * - 左右翻页热区保留。
 */
@Composable
fun BookShell(
    modifier: Modifier = Modifier,
    shellStyle: ShellStyle = ShellStyle.LIGHT,
    canTurnPrevious: Boolean,
    canTurnNext: Boolean,
    turnEnabled: Boolean,
    onTapPrevious: () -> Unit,
    onTapNext: () -> Unit,
    content: @Composable BoxScope.() -> Unit,
) {
    val isBookStyle = shellStyle == ShellStyle.BOOK
    val coverCorner = if (isBookStyle) GoaldayDesign.RadiusXL else GoaldayDesign.RadiusL
    val pageCorner = if (isBookStyle) GoaldayDesign.RadiusM else GoaldayDesign.RadiusL
    val coverShape = RoundedCornerShape(coverCorner)
    val pageShape = RoundedCornerShape(pageCorner)
    // 热区加宽，方便拇指翻页
    val edgeZoneWidth = 56.dp
    val view = LocalView.current
    var bookBounds by remember { mutableStateOf<Rect?>(null) }

    // 把当前书壳区域（左右翻页热区）排除在系统边缘返回手势之外，避免翻页时触发系统返回
    DisposableEffect(isBookStyle, turnEnabled, bookBounds) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || !isBookStyle || !turnEnabled) {
            return@DisposableEffect onDispose { }
        }
        val bounds = bookBounds
        val decor = (view.context as? Activity)?.window?.decorView
        if (decor != null && bounds != null && bounds.width() > 0 && bounds.height() > 0) {
            val edgePx = with(view.context.resources.displayMetrics) {
                (56f * density).toInt()
            }
            // 仅排除左右两条翻页热区，保留屏幕中间区域可使用系统返回手势
            val leftRect = Rect(bounds.left, bounds.top, bounds.left + edgePx, bounds.bottom)
            val rightRect = Rect(bounds.right - edgePx, bounds.top, bounds.right, bounds.bottom)
            decor.systemGestureExclusionRects = listOf(leftRect, rightRect)
        }
        onDispose {
            decor?.systemGestureExclusionRects = emptyList()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned {
                val pos = it.positionInWindow()
                bookBounds = Rect(
                    pos.x.toInt(),
                    pos.y.toInt(),
                    (pos.x + it.size.width).toInt(),
                    (pos.y + it.size.height).toInt(),
                )
            },
    ) {
        if (isBookStyle) {
            // 外层：硬壳书皮，留出明显的封面边距，模拟真实精装书的硬壳边框
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = 6.dp,
                        end = 6.dp,
                        top = 5.dp,
                        bottom = 6.dp,
                    )
                    .shadow(
                        elevation = 22.dp,
                        shape = coverShape,
                        clip = false,
                        ambientColor = Color.Black.copy(alpha = 0.14f),
                        spotColor = Color.Black.copy(alpha = 0.22f),
                    )
                    .clip(coverShape)
                    // 布纹封面贴图：保留原版米白布纹质感
                    .paint(
                        painter = painterResource(R.drawable.book_cover_fabric),
                        contentScale = ContentScale.Crop,
                    )
                    // 叠加暖灰米色，让封面颜色贴近原版精装布面
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                GoaldayDesign.BookBoardDark.copy(alpha = 0.11f),
                                GoaldayDesign.BookBoard.copy(alpha = 0.07f),
                                GoaldayDesign.BookBoardLight.copy(alpha = 0.04f),
                                GoaldayDesign.BookBoard.copy(alpha = 0.07f),
                                GoaldayDesign.BookBoardDark.copy(alpha = 0.11f),
                            ),
                        ),
                    )
                    .padding(
                        start = 6.dp,
                        end = 6.dp,
                        top = 5.dp,
                        bottom = 5.dp,
                    ),
            ) {
                // 内层：纸张页面
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(pageShape)
                        .background(BookPagePaper)
                        // 纸张横线：模拟真实笔记本 ruled page
                        .handbookPaperRuling(lineSpacingDp = 26.dp, lineColor = GoaldayDesign.InkMuted.copy(alpha = 0.10f))
                        .handbookPaperTexture(alpha = 0.08f)
                        .drawBehind {
                            val width = size.width
                            val height = size.height
                            val centerX = width / 2f

                            // 1. 封面内边压影：让纸张像嵌入硬壳（更深的压槽）
                            // 配合暖灰米色书壳，压影更自然；alpha 降低避免压影过重
                            drawRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        GoaldayDesign.BookBoardDark.copy(alpha = 0.18f),
                                        Color.Transparent,
                                    ),
                                ),
                                size = Size(width, 22f),
                            )
                            drawRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        GoaldayDesign.BookBoardDark.copy(alpha = 0.20f),
                                    ),
                                ),
                                topLeft = Offset(0f, height - 24f),
                                size = Size(width, 24f),
                            )
                            drawRect(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        GoaldayDesign.BookBoardDark.copy(alpha = 0.20f),
                                        Color.Transparent,
                                    ),
                                ),
                                size = Size(20f, height),
                            )
                            drawRect(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        GoaldayDesign.BookBoardDark.copy(alpha = 0.20f),
                                    ),
                                ),
                                topLeft = Offset(width - 20f, 0f),
                                size = Size(20f, height),
                            )

                            // 2. 中央书脊/装订沟：收窄沟槽、降低高光，避免与 SpineLayer 叠加成粗亮柱子
                            val gutterWidth = 18f
                            drawRect(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        GoaldayDesign.BookSpine.copy(alpha = 0.04f),
                                        GoaldayDesign.BookSpine.copy(alpha = 0.07f),
                                        GoaldayDesign.BookSpineLight.copy(alpha = 0.10f),
                                        GoaldayDesign.BookSpine.copy(alpha = 0.08f),
                                        GoaldayDesign.BookSpineLight.copy(alpha = 0.10f),
                                        GoaldayDesign.BookSpine.copy(alpha = 0.07f),
                                        GoaldayDesign.BookSpine.copy(alpha = 0.04f),
                                        Color.Transparent,
                                    ),
                                    startX = centerX - gutterWidth / 2f,
                                    endX = centerX + gutterWidth / 2f,
                                ),
                                topLeft = Offset(centerX - gutterWidth / 2f, 0f),
                                size = Size(gutterWidth, height),
                            )

                            // 中央装订沟深缝（双缝 + 两侧压痕，更像真实书脊折痕）
                            listOf(-1.0f, -0.45f, 0.45f, 1.0f).forEachIndexed { index, dx ->
                                val alpha = if (index == 1 || index == 2) 0.07f else 0.04f
                                drawLine(
                                    color = GoaldayDesign.BookSpine.copy(alpha = alpha),
                                    start = Offset(centerX + dx, 0f),
                                    end = Offset(centerX + dx, height),
                                    strokeWidth = if (index == 1 || index == 2) 0.9f else 0.6f,
                                )
                            }

                            // 装订线：沿书脊上下分布的虚线孔迹，模拟线装/胶装笔记本
                            val stitchCount = 7
                            val stitchGap = height / (stitchCount + 1)
                            val stitchRadius = 1.2f
                            val stitchAlpha = 0.07f
                            for (i in 1..stitchCount) {
                                val y = i * stitchGap
                                // 左页装订孔
                                drawCircle(
                                    color = GoaldayDesign.BookSpine.copy(alpha = stitchAlpha),
                                    radius = stitchRadius,
                                    center = Offset(centerX - 4f, y),
                                )
                                // 右页装订孔
                                drawCircle(
                                    color = GoaldayDesign.BookSpine.copy(alpha = stitchAlpha),
                                    radius = stitchRadius,
                                    center = Offset(centerX + 4f, y),
                                )
                            }

                            // 3. 左右页面向装订沟弯曲的阴影（更宽，增强曲面感）
                            drawRect(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        GoaldayDesign.BookSpine.copy(alpha = 0.135f),
                                        GoaldayDesign.BookSpine.copy(alpha = 0.0525f),
                                        Color.Transparent,
                                    ),
                                    startX = 0f,
                                    endX = 32f,
                                ),
                                size = Size(32f, height),
                            )
                            drawRect(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        GoaldayDesign.BookSpine.copy(alpha = 0.0525f),
                                        GoaldayDesign.BookSpine.copy(alpha = 0.135f),
                                    ),
                                    startX = width - 32f,
                                    endX = width,
                                ),
                                topLeft = Offset(width - 32f, 0f),
                                size = Size(32f, height),
                            )

                            // 4. 书页外缘厚度：右侧（书口）多层纸页
                            val edgeBase = GoaldayDesign.BookSpine.copy(alpha = 0.18f)
                            listOf(12f, 9f, 6f, 3f, 1f).forEachIndexed { index, offset ->
                                drawLine(
                                    color = edgeBase.copy(alpha = 0.112f - index * 0.0175f),
                                    start = Offset(width - offset, 0f),
                                    end = Offset(width - offset, height),
                                    strokeWidth = 0.8f,
                                )
                            }

                            // 5. 书页外缘厚度：底部（书脚）多层纸页
                            listOf(14f, 10f, 7f, 4f, 1.5f).forEachIndexed { index, offset ->
                                drawLine(
                                    color = edgeBase.copy(alpha = 0.112f - index * 0.0175f),
                                    start = Offset(0f, height - offset),
                                    end = Offset(width, height - offset),
                                    strokeWidth = 0.8f,
                                )
                            }

                            // 6. 右下角微微卷起：三角阴影 + 边缘高光
                            val curlSize = 64f
                            val curlPath = Path().apply {
                                moveTo(width, height - curlSize)
                                lineTo(width, height)
                                lineTo(width - curlSize, height)
                                close()
                            }
                            drawPath(
                                path = curlPath,
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        GoaldayDesign.BookSpine.copy(alpha = 0.12f),
                                    ),
                                    start = Offset(width - curlSize, height - curlSize),
                                    end = Offset(width, height),
                                ),
                            )
                            // 卷曲后的背面亮边
                            drawLine(
                                color = Color.White.copy(alpha = 0.15f),
                                start = Offset(width - curlSize * 0.85f, height - 1.2f),
                                end = Offset(width - 1.2f, height - curlSize * 0.85f),
                                strokeWidth = 1.2f,
                            )

                            // 7. 纸张整体光泽与旧纸感
                            drawRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.09f),
                                        Color.Transparent,
                                        Color.Transparent,
                                        GoaldayDesign.PaperAged.copy(alpha = 0.075f),
                                    ),
                                ),
                                size = Size(width, height),
                            )

                            // 8. 左右页微微隆起的中央高光（让纸面不扁平）
                            drawRect(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.White.copy(alpha = 0.05f),
                                        Color.Transparent,
                                    ),
                                    startX = centerX - width * 0.30f,
                                    endX = centerX + width * 0.30f,
                                ),
                                size = Size(width, height),
                            )
                        },
                ) {
                    // 左右翻页点击热区（先绘制，位于底层，避免遮挡内容按钮）
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .width(edgeZoneWidth)
                            .fillMaxHeight()
                            .clickable(enabled = canTurnPrevious && turnEnabled, onClick = onTapPrevious),
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .width(edgeZoneWidth)
                            .fillMaxHeight()
                            .clickable(enabled = canTurnNext && turnEnabled, onClick = onTapNext),
                    )

                    // 内容限定在纸面版心内，保留书页边距
                    // 书内页强制使用浅色主题：纸张已经是米白色，文字保持深色墨水，避免暗色模式下对比度不足
                    // 内容放在热区之上，确保按钮等可点击元素优先接收点击事件
                    CompositionLocalProvider(LocalGoaldayDarkMode provides false) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(
                                    start = 8.33.dp,
                                    end = 8.33.dp,
                                    top = 8.33.dp,
                                    bottom = 8.33.dp,
                                ),
                        ) {
                            content()
                        }
                    }
                }
            }
        } else {
            // LIGHT 模式：干净卡片式，仅轻微阴影与纸张背景
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .shadow(
                        elevation = 4.dp,
                        shape = pageShape,
                        clip = false,
                    )
                    .clip(pageShape)
                    .background(GoaldayDesign.Paper)
                    .handbookPaperTexture(alpha = 0.08f),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = GoaldayDesign.Space3, vertical = GoaldayDesign.Space2),
                ) {
                    content()
                }
            }
        }
    }
}
