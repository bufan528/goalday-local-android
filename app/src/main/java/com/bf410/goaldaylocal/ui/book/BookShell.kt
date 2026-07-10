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
import androidx.compose.ui.Alignment
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
    val edgeZoneWidth = 32.dp

    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        if (isBookStyle) {
            // 外层：硬壳书皮，留出明显的封面边距
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = 10.dp,
                        end = 10.dp,
                        top = 8.dp,
                        bottom = 10.dp,
                    )
                    .shadow(
                        elevation = 26.dp,
                        shape = coverShape,
                        clip = false,
                        ambientColor = Color.Black.copy(alpha = 0.26f),
                        spotColor = Color.Black.copy(alpha = 0.36f),
                    )
                    .clip(coverShape)
                    // 布纹封面贴图：保留原版米白布纹质感
                    .paint(
                        painter = painterResource(R.drawable.book_cover_fabric),
                        contentScale = ContentScale.Crop,
                    )
                    // 叠加柔和暖色，让封面像真实布面硬壳但不掩盖纹理
                    .background(
                        // 模拟硬壳封面受光：中间亮、两侧暗，外缘加一道深色勾边
                        Brush.horizontalGradient(
                            listOf(
                                GoaldayDesign.BookBoardDark.copy(alpha = 0.42f),
                                GoaldayDesign.BookBoard.copy(alpha = 0.18f),
                                GoaldayDesign.BookBoardLight.copy(alpha = 0.10f),
                                GoaldayDesign.BookBoard.copy(alpha = 0.18f),
                                GoaldayDesign.BookBoardDark.copy(alpha = 0.42f),
                            ),
                        ),
                    )
                    .padding(
                        start = 8.dp,
                        end = 8.dp,
                        top = 6.dp,
                        bottom = 6.dp,
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
                            drawRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        GoaldayDesign.BookBoardDark.copy(alpha = 0.32f),
                                        Color.Transparent,
                                    ),
                                ),
                                size = Size(width, 22f),
                            )
                            drawRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        GoaldayDesign.BookBoardDark.copy(alpha = 0.36f),
                                    ),
                                ),
                                topLeft = Offset(0f, height - 24f),
                                size = Size(width, 24f),
                            )
                            drawRect(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        GoaldayDesign.BookBoardDark.copy(alpha = 0.34f),
                                        Color.Transparent,
                                    ),
                                ),
                                size = Size(20f, height),
                            )
                            drawRect(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        GoaldayDesign.BookBoardDark.copy(alpha = 0.34f),
                                    ),
                                ),
                                topLeft = Offset(width - 20f, 0f),
                                size = Size(20f, height),
                            )

                            // 2. 中央书脊/装订沟：书页弯入沟槽的立体感
                            val gutterWidth = 84f
                            drawRect(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        GoaldayDesign.BookSpine.copy(alpha = 0.14f),
                                        GoaldayDesign.BookSpine.copy(alpha = 0.32f),
                                        GoaldayDesign.BookSpine.copy(alpha = 0.48f),
                                        GoaldayDesign.BookSpine.copy(alpha = 0.26f),
                                        GoaldayDesign.BookSpine.copy(alpha = 0.48f),
                                        GoaldayDesign.BookSpine.copy(alpha = 0.32f),
                                        GoaldayDesign.BookSpine.copy(alpha = 0.14f),
                                        Color.Transparent,
                                    ),
                                    startX = centerX - gutterWidth / 2f,
                                    endX = centerX + gutterWidth / 2f,
                                ),
                                topLeft = Offset(centerX - gutterWidth / 2f, 0f),
                                size = Size(gutterWidth, height),
                            )

                            // 中央装订沟深缝（双缝 + 两侧压痕，更像真实书脊折痕）
                            listOf(-1.2f, -0.5f, 0.5f, 1.2f).forEachIndexed { index, dx ->
                                val alpha = if (index == 1 || index == 2) 0.22f else 0.12f
                                drawLine(
                                    color = GoaldayDesign.BookSpine.copy(alpha = alpha),
                                    start = Offset(centerX + dx, 0f),
                                    end = Offset(centerX + dx, height),
                                    strokeWidth = if (index == 1 || index == 2) 1.1f else 0.8f,
                                )
                            }

                            // 装订线：沿书脊上下分布的虚线孔迹，模拟线装/胶装笔记本
                            val stitchCount = 7
                            val stitchGap = height / (stitchCount + 1)
                            val stitchRadius = 1.6f
                            val stitchAlpha = 0.14f
                            for (i in 1..stitchCount) {
                                val y = i * stitchGap
                                // 左页装订孔
                                drawCircle(
                                    color = GoaldayDesign.BookSpine.copy(alpha = stitchAlpha),
                                    radius = stitchRadius,
                                    center = Offset(centerX - 5f, y),
                                )
                                // 右页装订孔
                                drawCircle(
                                    color = GoaldayDesign.BookSpine.copy(alpha = stitchAlpha),
                                    radius = stitchRadius,
                                    center = Offset(centerX + 5f, y),
                                )
                            }

                            // 3. 左右页面向装订沟弯曲的阴影（更宽，增强曲面感）
                            drawRect(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        GoaldayDesign.BookSpine.copy(alpha = 0.34f),
                                        GoaldayDesign.BookSpine.copy(alpha = 0.14f),
                                        Color.Transparent,
                                    ),
                                    startX = 0f,
                                    endX = 54f,
                                ),
                                size = Size(54f, height),
                            )
                            drawRect(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        GoaldayDesign.BookSpine.copy(alpha = 0.14f),
                                        GoaldayDesign.BookSpine.copy(alpha = 0.34f),
                                    ),
                                    startX = width - 54f,
                                    endX = width,
                                ),
                                topLeft = Offset(width - 54f, 0f),
                                size = Size(54f, height),
                            )

                            // 4. 书页外缘厚度：右侧（书口）多层纸页
                            val edgeBase = GoaldayDesign.BookSpine.copy(alpha = 0.22f)
                            listOf(12f, 9f, 6f, 3f, 1f).forEachIndexed { index, offset ->
                                drawLine(
                                    color = edgeBase.copy(alpha = 0.20f - index * 0.03f),
                                    start = Offset(width - offset, 0f),
                                    end = Offset(width - offset, height),
                                    strokeWidth = 1.1f,
                                )
                            }

                            // 5. 书页外缘厚度：底部（书脚）多层纸页
                            listOf(14f, 10f, 7f, 4f, 1.5f).forEachIndexed { index, offset ->
                                drawLine(
                                    color = edgeBase.copy(alpha = 0.20f - index * 0.03f),
                                    start = Offset(0f, height - offset),
                                    end = Offset(width, height - offset),
                                    strokeWidth = 1.1f,
                                )
                            }

                            // 6. 右下角微微卷起：三角阴影 + 边缘高光
                            val curlSize = 56f
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
                                        GoaldayDesign.BookSpine.copy(alpha = 0.26f),
                                    ),
                                    start = Offset(width - curlSize, height - curlSize),
                                    end = Offset(width, height),
                                ),
                            )
                            // 卷曲后的背面亮边
                            drawLine(
                                color = Color.White.copy(alpha = 0.28f),
                                start = Offset(width - curlSize * 0.85f, height - 1.4f),
                                end = Offset(width - 1.4f, height - curlSize * 0.85f),
                                strokeWidth = 1.4f,
                            )

                            // 7. 纸张整体光泽与旧纸感
                            drawRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.16f),
                                        Color.Transparent,
                                        Color.Transparent,
                                        GoaldayDesign.PaperAged.copy(alpha = 0.14f),
                                    ),
                                ),
                                size = Size(width, height),
                            )

                            // 8. 左右页微微隆起的中央高光（让纸面不扁平）
                            drawRect(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.White.copy(alpha = 0.07f),
                                        Color.Transparent,
                                    ),
                                    startX = centerX - width * 0.32f,
                                    endX = centerX + width * 0.32f,
                                ),
                                size = Size(width, height),
                            )
                        },
                ) {
                    // 内容限定在纸面版心内，保留书页边距
                    // 书内页强制使用浅色主题：纸张已经是米白色，文字保持深色墨水，避免暗色模式下对比度不足
                    CompositionLocalProvider(LocalGoaldayDarkMode provides false) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(
                                    start = 12.dp,
                                    end = 12.dp,
                                    top = 6.dp,
                                    bottom = 6.dp,
                                ),
                        ) {
                            content()
                        }
                    }

                    // 左右翻页点击热区
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
