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
 */
private val BookPagePaper = GoaldayDesign.Paper

enum class ShellStyle {
    LIGHT,
    BOOK,
}

/**
 * 书壳：模拟一本真正打开的纸质手账。
 *
 * 设计要点（对照原版 APK）：
 * - 外层为硬壳书皮，使用 book_cover_fabric 布纹贴图，叠加极淡暖色，保留原版米白布纹质感。
 * - 四周留出适度书边，但不过度挤压版心。
 * - 中央书脊只有一道极淡的装订压痕，不抢戏。
 * - 左右页面向书脊微微弯曲的阴影，营造纸张弯入装订处的立体感。
 * - 书口/书脚用多层细线模拟一叠纸页的厚度，但颜色很淡。
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
    // 热区加宽，方便拇指翻页，同时避免系统返回手势冲突
    val edgeZoneWidth = 56.dp
    val view = LocalView.current
    var bookBounds by remember { mutableStateOf<Rect?>(null) }

    // 把当前书壳区域（左右翻页热区）排除在系统边缘返回手势之外
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
            // 外层：硬壳书皮
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = 5.dp,
                        end = 5.dp,
                        top = 4.dp,
                        bottom = 5.dp,
                    )
                    .shadow(
                        elevation = 18.dp,
                        shape = coverShape,
                        clip = false,
                        ambientColor = Color.Black.copy(alpha = 0.12f),
                        spotColor = Color.Black.copy(alpha = 0.18f),
                    )
                    .clip(coverShape)
                    // 布纹封面贴图：保留原版米白布纹质感
                    .paint(
                        painter = painterResource(R.drawable.book_cover_fabric),
                        contentScale = ContentScale.Crop,
                    )
                    // 极淡的暖色叠层，统一封面色调，同时让布纹可见
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                GoaldayDesign.BookBoard.copy(alpha = 0.03f),
                                GoaldayDesign.BookBoardDark.copy(alpha = 0.06f),
                            ),
                        ),
                    )
                    .padding(
                        start = 5.dp,
                        end = 5.dp,
                        top = 4.dp,
                        bottom = 4.dp,
                    ),
            ) {
                // 内层：纸张页面
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(pageShape)
                        .background(BookPagePaper)
                        // 纸张横线：极淡，只在空白处隐约可见
                        .handbookPaperRuling(lineSpacingDp = 28.dp, lineColor = GoaldayDesign.InkMuted.copy(alpha = 0.045f))
                        .handbookPaperTexture(alpha = 0.06f)
                        .drawBehind {
                            val width = size.width
                            val height = size.height
                            val centerX = width / 2f

                            // 1. 封面内边压影：让纸张像嵌入硬壳（极淡）
                            drawRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        GoaldayDesign.BookBoardDark.copy(alpha = 0.10f),
                                        Color.Transparent,
                                    ),
                                ),
                                size = Size(width, 18f),
                            )
                            drawRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        GoaldayDesign.BookBoardDark.copy(alpha = 0.12f),
                                    ),
                                ),
                                topLeft = Offset(0f, height - 20f),
                                size = Size(width, 20f),
                            )
                            drawRect(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        GoaldayDesign.BookBoardDark.copy(alpha = 0.12f),
                                        Color.Transparent,
                                    ),
                                ),
                                size = Size(16f, height),
                            )
                            drawRect(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        GoaldayDesign.BookBoardDark.copy(alpha = 0.12f),
                                    ),
                                ),
                                topLeft = Offset(width - 16f, 0f),
                                size = Size(16f, height),
                            )

                            // 2. 中央书脊/装订沟：模拟摊开书本的左右页弯曲汇入书脊的暗沟
                            val gutterWidth = 18f
                            drawRect(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        GoaldayDesign.BookSpine.copy(alpha = 0.06f),
                                        GoaldayDesign.BookSpine.copy(alpha = 0.14f),
                                        GoaldayDesign.BookSpine.copy(alpha = 0.06f),
                                        Color.Transparent,
                                    ),
                                    startX = centerX - gutterWidth / 2f,
                                    endX = centerX + gutterWidth / 2f,
                                ),
                                topLeft = Offset(centerX - gutterWidth / 2f, 0f),
                                size = Size(gutterWidth, height),
                            )
                            // 中央装订线：两道折痕，模拟书脊处纸张折叠
                            listOf(-0.6f, 0.6f).forEach { dx ->
                                drawLine(
                                    color = GoaldayDesign.BookSpine.copy(alpha = 0.10f),
                                    start = Offset(centerX + dx, 0f),
                                    end = Offset(centerX + dx, height),
                                    strokeWidth = 0.8f,
                                )
                            }
                            // 书脊中央极细高光：纸张隆起处的受光
                            drawLine(
                                color = Color.White.copy(alpha = 0.10f),
                                start = Offset(centerX, 0f),
                                end = Offset(centerX, height),
                                strokeWidth = 1.0f,
                            )

                            // 3. 左右页面向装订沟弯曲的阴影：范围加宽，营造两页分别拱起的体积感
                            val pageCurveWidth = 36f
                            drawRect(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        GoaldayDesign.BookSpine.copy(alpha = 0.14f),
                                        GoaldayDesign.BookSpine.copy(alpha = 0.05f),
                                        Color.Transparent,
                                    ),
                                    startX = 0f,
                                    endX = pageCurveWidth,
                                ),
                                size = Size(pageCurveWidth, height),
                            )
                            drawRect(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        GoaldayDesign.BookSpine.copy(alpha = 0.05f),
                                        GoaldayDesign.BookSpine.copy(alpha = 0.14f),
                                    ),
                                    startX = width - pageCurveWidth,
                                    endX = width,
                                ),
                                topLeft = Offset(width - pageCurveWidth, 0f),
                                size = Size(pageCurveWidth, height),
                            )

                            // 3.5 左右页心轻微提亮：让两页与书脊形成明暗对比，强化双页摊开感
                            drawRect(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.04f),
                                        Color.Transparent,
                                        Color.Transparent,
                                        Color.White.copy(alpha = 0.04f),
                                    ),
                                    startX = 0f,
                                    endX = width,
                                ),
                                size = Size(width, height),
                            )

                            // 4. 书页外缘厚度：右侧（书口）多层纸页
                            val edgeBase = GoaldayDesign.BookSpine.copy(alpha = 0.14f)
                            listOf(10f, 7f, 4.5f, 2.5f, 1f).forEachIndexed { index, offset ->
                                drawLine(
                                    color = edgeBase.copy(alpha = 0.085f - index * 0.012f),
                                    start = Offset(width - offset, 0f),
                                    end = Offset(width - offset, height),
                                    strokeWidth = 0.7f,
                                )
                            }

                            // 5. 书页外缘厚度：底部（书脚）多层纸页
                            listOf(11f, 8f, 5.5f, 3f, 1.2f).forEachIndexed { index, offset ->
                                drawLine(
                                    color = edgeBase.copy(alpha = 0.085f - index * 0.012f),
                                    start = Offset(0f, height - offset),
                                    end = Offset(width, height - offset),
                                    strokeWidth = 0.7f,
                                )
                            }

                            // 6. 右下角微微卷起
                            val curlSize = 48f
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
                                        GoaldayDesign.BookSpine.copy(alpha = 0.09f),
                                    ),
                                    start = Offset(width - curlSize, height - curlSize),
                                    end = Offset(width, height),
                                ),
                            )
                            // 卷曲后的背面亮边
                            drawLine(
                                color = Color.White.copy(alpha = 0.12f),
                                start = Offset(width - curlSize * 0.85f, height - 1f),
                                end = Offset(width - 1f, height - curlSize * 0.85f),
                                strokeWidth = 1f,
                            )

                            // 7. 纸张整体光泽与旧纸感（极淡）
                            drawRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.06f),
                                        Color.Transparent,
                                        Color.Transparent,
                                        GoaldayDesign.PaperAged.copy(alpha = 0.05f),
                                    ),
                                ),
                                size = Size(width, height),
                            )
                        },
                ) {
                    // 左右翻页点击热区（先绘制，位于底层）
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

                    // 内容限定在纸面版心内
                    CompositionLocalProvider(LocalGoaldayDarkMode provides false) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(
                                    start = 7.dp,
                                    end = 7.dp,
                                    top = 5.dp,
                                    bottom = 5.dp,
                                ),
                        ) {
                            content()
                        }
                    }
                }
            }
        } else {
            // LIGHT 模式：干净卡片式
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
                    .handbookPaperTexture(alpha = 0.06f),
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
