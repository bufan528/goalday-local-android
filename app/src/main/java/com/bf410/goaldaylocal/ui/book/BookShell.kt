package com.bf410.goaldaylocal.ui.book

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.bf410.goaldaylocal.R
import com.bf410.goaldaylocal.ui.replica.GoaldayDesign

enum class ShellStyle {
    LIGHT,
    BOOK,
}

/**
 * 书壳：模拟一本摊开的实体手账。
 * - LIGHT：软皮平装本，封面与书页边界柔和，适合日常快速翻阅。
 * - BOOK：硬壳精装本，左侧有清晰书脊与装订线，右侧有书口厚度，更像真书。
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
    // 左侧书脊圆角小，右侧书口圆角大，形成“书翻开”的不对称轮廓
    val spineCorner = if (shellStyle == ShellStyle.BOOK) 1.dp else 3.dp
    val foreEdgeCorner = if (shellStyle == ShellStyle.BOOK) GoaldayDesign.RadiusXL else GoaldayDesign.Radius2XL
    val outerShape = RoundedCornerShape(
        topStart = spineCorner,
        bottomStart = spineCorner,
        topEnd = foreEdgeCorner,
        bottomEnd = foreEdgeCorner,
    )

    // 纸张内边距：精装本留白更多，平装本更紧凑
    val pageInsetH = if (shellStyle == ShellStyle.BOOK) GoaldayDesign.Space4 else GoaldayDesign.Space3
    val pageInsetV = if (shellStyle == ShellStyle.BOOK) GoaldayDesign.Space3 else GoaldayDesign.Space2
    // 翻页热区：太宽会误触内容，太窄难触发；0.12 约等于拇指自然落点
    val edgeZoneWidth = GoaldayDesign.Space5

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = GoaldayDesign.Space1, vertical = GoaldayDesign.Space1)
            .shadow(
                elevation = GoaldayDesign.ShadowLarge,
                shape = outerShape,
                clip = false,
            )
            .clip(outerShape)
            .then(
                if (shellStyle == ShellStyle.BOOK) {
                    Modifier.background(Color.Transparent)
                } else {
                    Modifier.background(bookBoardBrush(shellStyle))
                },
            )
            .border(
                width = GoaldayDesign.Hairline,
                color = if (shellStyle == ShellStyle.BOOK) {
                    GoaldayDesign.BookBoardDark.copy(alpha = 0.32f)
                } else {
                    GoaldayDesign.BookBoardDark.copy(alpha = 0.16f)
                },
                shape = outerShape,
            ),
    ) {
        // 精装本使用原版布纹封面纹理
        if (shellStyle == ShellStyle.BOOK) {
            Image(
                painter = painterResource(R.drawable.book_cover_fabric),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }

        // 精装本书脊：硬质封面阴影 + 装订线
        if (shellStyle == ShellStyle.BOOK) {
            HardcoverSpine()
        } else {
            SoftcoverSpine()
        }

        // 书口厚度：右侧纸张堆叠的浅影
        PaperForeEdge(shellStyle)

        // 纸张层：双页展开效果
        OpenBookPaperChrome(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = pageInsetH, vertical = pageInsetV),
        )

        // 内容限定在纸面范围内
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = pageInsetH, vertical = pageInsetV),
        ) {
            content()
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

@Composable
private fun bookBoardBrush(shellStyle: ShellStyle): Brush {
    val boardDark = GoaldayDesign.BookBoardDark
    val board = GoaldayDesign.BookBoard
    val boardLight = GoaldayDesign.BookBoardLight
    return if (shellStyle == ShellStyle.BOOK) {
        // 精装硬壳：深褐书脊 → 皮革封面 → 暖黄书页，边缘压暗营造包裹感
        Brush.horizontalGradient(
            listOf(
                boardDark,
                boardDark.copy(alpha = 0.92f),
                board,
                boardLight,
                boardLight.copy(alpha = 0.88f),
                GoaldayDesign.PaperWarm.copy(alpha = 0.95f),
                GoaldayDesign.Paper,
            ),
            startX = 0f,
            endX = 420f,
        )
    } else {
        // 平装本：柔和封面色，中间露出纸张，边缘略深
        Brush.horizontalGradient(
            listOf(
                board.copy(alpha = 0.90f),
                boardLight.copy(alpha = 0.78f),
                GoaldayDesign.Paper,
                GoaldayDesign.PaperWarm,
                GoaldayDesign.Paper,
                boardLight.copy(alpha = 0.78f),
                board.copy(alpha = 0.90f),
            ),
            startX = 0f,
            endX = 720f,
        )
    }
}

@Composable
private fun BoxScope.HardcoverSpine() {
    val spineWidth = 34.dp
    // 书脊主体：深色皮革渐变，右侧与封面交界处有柔和高光
    Box(
        modifier = Modifier
            .align(Alignment.CenterStart)
            .width(spineWidth)
            .fillMaxHeight()
            .background(
                with(LocalDensity.current) {
                    Brush.horizontalGradient(
                        listOf(
                            GoaldayDesign.BookSpine,
                            GoaldayDesign.BookSpineLight.copy(alpha = 0.70f),
                            GoaldayDesign.BookBoardDark.copy(alpha = 0.35f),
                            Color.Transparent,
                        ),
                        startX = 0f,
                        endX = spineWidth.toPx(),
                    )
                },
            ),
    )
    // 装订凹槽：书脊与封面连接处的压痕阴影
    Box(
        modifier = Modifier
            .align(Alignment.CenterStart)
            .padding(start = spineWidth - 4.dp)
            .width(3.dp)
            .fillMaxHeight()
            .background(
                with(LocalDensity.current) {
                    Brush.horizontalGradient(
                        listOf(
                            GoaldayDesign.BookBoardDark.copy(alpha = 0.28f),
                            Color.Transparent,
                        ),
                        startX = 0f,
                        endX = 3.dp.toPx(),
                    )
                },
            ),
    )
    // 装订线：四股线模拟锁线装，更贴近真实精装书
    val stitchColor = GoaldayDesign.BookSpine.copy(alpha = 0.55f)
    val stitchPositions = listOf(6.dp, 12.dp, 19.dp, 25.dp)
    stitchPositions.forEachIndexed { index, startPadding ->
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = startPadding, top = GoaldayDesign.Space6, bottom = GoaldayDesign.Space6)
                .width(if (index == 0) 1.dp else 0.7.dp)
                .fillMaxHeight()
                .background(
                    if (index == 0) stitchColor else stitchColor.copy(alpha = 0.65f),
                ),
        )
    }
}

@Composable
private fun BoxScope.SoftcoverSpine() {
    val spineWidth = 22.dp
    Box(
        modifier = Modifier
            .align(Alignment.CenterStart)
            .width(spineWidth)
            .fillMaxHeight()
            .background(
                with(LocalDensity.current) {
                    Brush.horizontalGradient(
                        listOf(
                            GoaldayDesign.BookBoardDark.copy(alpha = 0.38f),
                            GoaldayDesign.BookBoardDark.copy(alpha = 0.16f),
                            Color.Transparent,
                        ),
                        startX = 0f,
                        endX = spineWidth.toPx(),
                    )
                },
            ),
    )
    // 软脊压痕：两股细线模拟胶装脊
    listOf(7.dp, 13.dp).forEachIndexed { index, startPadding ->
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = startPadding, top = GoaldayDesign.Space6, bottom = GoaldayDesign.Space6)
                .width(if (index == 0) 0.8.dp else 0.5.dp)
                .fillMaxHeight()
                .background(GoaldayDesign.BookBoardDark.copy(alpha = if (index == 0) 0.26f else 0.16f)),
        )
    }
}

@Composable
private fun BoxScope.PaperForeEdge(shellStyle: ShellStyle) {
    val foreEdgeWidth = if (shellStyle == ShellStyle.BOOK) 12.dp else 8.dp
    val edgeColor = GoaldayDesign.PaperAged
    Box(
        modifier = Modifier
            .align(Alignment.CenterEnd)
            .width(foreEdgeWidth)
            .fillMaxHeight()
            .background(
                with(LocalDensity.current) {
                    Brush.horizontalGradient(
                        listOf(
                            Color.Transparent,
                            edgeColor.copy(alpha = 0.18f),
                            edgeColor.copy(alpha = if (shellStyle == ShellStyle.BOOK) 0.46f else 0.34f),
                            edgeColor.copy(alpha = if (shellStyle == ShellStyle.BOOK) 0.52f else 0.40f),
                        ),
                        startX = 0f,
                        endX = foreEdgeWidth.toPx(),
                    )
                },
            ),
    )
    // 书页层叠细线：模拟纸张堆叠的横纹
    val lineCount = if (shellStyle == ShellStyle.BOOK) 5 else 3
    repeat(lineCount) { index ->
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = (1.5f + index * 1.8f).dp)
                .width(0.6.dp)
                .fillMaxHeight()
                .background(
                    GoaldayDesign.PaperAged.copy(
                        alpha = (0.18f - index * 0.025f).coerceAtLeast(0.08f),
                    ),
                ),
        )
    }
}

@Composable
private fun OpenBookPaperChrome(modifier: Modifier = Modifier) {
    val pageShape = RoundedCornerShape(
        topStart = 2.dp,
        bottomStart = 2.dp,
        topEnd = GoaldayDesign.RadiusM,
        bottomEnd = GoaldayDesign.RadiusM,
    )
    Box(
        modifier = modifier
            .clip(pageShape)
            .background(GoaldayDesign.Paper)
            .drawBehind {
                val width = size.width
                val height = size.height
                val centerX = width / 2f
                val stroke = 1.dp.toPx().coerceAtMost(1.2f)

                // 中央装订阴影：真实书脊处纸张凹陷，比原来更宽更深
                drawRect(
                    brush = Brush.horizontalGradient(
                        listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.04f),
                            Color.Black.copy(alpha = 0.12f),
                            Color.Black.copy(alpha = 0.04f),
                            Color.Transparent,
                        ),
                        startX = centerX - 22.dp.toPx(),
                        endX = centerX + 22.dp.toPx(),
                    ),
                    size = androidx.compose.ui.geometry.Size(44.dp.toPx(), height),
                    topLeft = Offset(centerX - 22.dp.toPx(), 0f),
                )

                // 中央装订线：双股线模拟书脊折痕
                drawLine(
                    color = GoaldayDesign.BlackOverlayHairline,
                    start = Offset(centerX - 0.5f, 0f),
                    end = Offset(centerX - 0.5f, height),
                    strokeWidth = stroke,
                )
                drawLine(
                    color = GoaldayDesign.BlackOverlayHairline.copy(alpha = 0.6f),
                    start = Offset(centerX + 0.5f, 0f),
                    end = Offset(centerX + 0.5f, height),
                    strokeWidth = stroke * 0.7f,
                )

                // 页面轻微纸张纹理：使用非常淡的噪点感竖线
                val textureColor = GoaldayDesign.PaperAged.copy(alpha = 0.05f)
                val textureSpacing = 5.dp.toPx()
                var x = 0f
                while (x < width) {
                    drawLine(
                        color = textureColor,
                        start = Offset(x, 0f),
                        end = Offset(x, height),
                        strokeWidth = 0.5f,
                    )
                    x += textureSpacing
                }

                // 顶部受光：纸张上边缘有轻微反光
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(GoaldayDesign.WhiteHighlight.copy(alpha = 0.40f), Color.Transparent),
                    ),
                    size = androidx.compose.ui.geometry.Size(width, 24.dp.toPx()),
                )

                // 底部厚度阴影
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(Color.Transparent, GoaldayDesign.BookBoardDark.copy(alpha = 0.08f)),
                    ),
                    topLeft = Offset(0f, height - 24.dp.toPx()),
                    size = androidx.compose.ui.geometry.Size(width, 24.dp.toPx()),
                )
            },
    ) {
        // 左页：暖色调，靠书脊处略暗
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxHeight()
                .fillMaxWidth(0.5f)
                .background(GoaldayDesign.PaperGradient),
        )
        // 右页：亮色调，翻开时受光更多
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .fillMaxWidth(0.5f)
                .background(
                    Brush.linearGradient(
                        listOf(
                            GoaldayDesign.PaperWarm.copy(alpha = 0.70f),
                            GoaldayDesign.Paper,
                            GoaldayDesign.Paper.copy(alpha = 0.95f),
                        ),
                        start = Offset.Zero,
                        end = Offset(340f, 280f),
                    ),
                ),
        )
    }
}
