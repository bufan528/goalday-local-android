package com.bf410.goaldaylocal.ui.book

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
import androidx.compose.ui.unit.dp
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
    val spineCorner = if (shellStyle == ShellStyle.BOOK) 2.dp else 4.dp
    val foreEdgeCorner = if (shellStyle == ShellStyle.BOOK) GoaldayDesign.RadiusXL else GoaldayDesign.Radius2XL
    val outerShape = RoundedCornerShape(
        topStart = spineCorner,
        bottomStart = spineCorner,
        topEnd = foreEdgeCorner,
        bottomEnd = foreEdgeCorner,
    )

    // 纸张内边距：精装本留白更多，平装本更紧凑
    val pageInsetH = if (shellStyle == ShellStyle.BOOK) GoaldayDesign.Space3 else GoaldayDesign.Space2
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
            .background(bookBoardBrush(shellStyle))
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
        // 精装本：深色书脊 → 封面 → 纸张暖色，硬质包裹感
        Brush.horizontalGradient(
            listOf(boardDark, board, boardLight, GoaldayDesign.PaperWarm, GoaldayDesign.Paper),
            startX = 0f,
            endX = 360f,
        )
    } else {
        // 平装本：柔和封面色，中间露出纸张，边缘略深
        Brush.horizontalGradient(
            listOf(
                board.copy(alpha = 0.85f),
                boardLight.copy(alpha = 0.70f),
                GoaldayDesign.Paper,
                GoaldayDesign.PaperWarm,
                GoaldayDesign.Paper,
                boardLight.copy(alpha = 0.70f),
                board.copy(alpha = 0.85f),
            ),
            startX = 0f,
            endX = 680f,
        )
    }
}

@Composable
private fun BoxScope.HardcoverSpine() {
    val spineWidth = 28.dp
    Box(
        modifier = Modifier
            .align(Alignment.CenterStart)
            .width(spineWidth)
            .fillMaxHeight()
            .background(
                Brush.horizontalGradient(
                    listOf(
                        GoaldayDesign.BookBoardDark.copy(alpha = 0.55f),
                        GoaldayDesign.BookBoardDark.copy(alpha = 0.20f),
                        Color.Transparent,
                    ),
                ),
            ),
    )
    // 装订线：两股线模拟锁线装
    val stitchColor = GoaldayDesign.BookBoardDark.copy(alpha = 0.35f)
    Box(
        modifier = Modifier
            .align(Alignment.CenterStart)
            .padding(start = 8.dp, top = GoaldayDesign.Space5, bottom = GoaldayDesign.Space5)
            .width(0.8.dp)
            .fillMaxHeight()
            .background(stitchColor),
    )
    Box(
        modifier = Modifier
            .align(Alignment.CenterStart)
            .padding(start = 14.dp, top = GoaldayDesign.Space5, bottom = GoaldayDesign.Space5)
            .width(0.6.dp)
            .fillMaxHeight()
            .background(stitchColor.copy(alpha = 0.65f)),
    )
}

@Composable
private fun BoxScope.SoftcoverSpine() {
    val spineWidth = 18.dp
    Box(
        modifier = Modifier
            .align(Alignment.CenterStart)
            .width(spineWidth)
            .fillMaxHeight()
            .background(
                Brush.horizontalGradient(
                    listOf(
                        GoaldayDesign.BookBoardDark.copy(alpha = 0.32f),
                        GoaldayDesign.BookBoardDark.copy(alpha = 0.10f),
                        Color.Transparent,
                    ),
                ),
            ),
    )
    // 单条软脊压痕
    Box(
        modifier = Modifier
            .align(Alignment.CenterStart)
            .padding(start = 8.dp, top = GoaldayDesign.Space6, bottom = GoaldayDesign.Space6)
            .width(0.5.dp)
            .fillMaxHeight()
            .background(GoaldayDesign.BookBoardDark.copy(alpha = 0.22f)),
    )
}

@Composable
private fun BoxScope.PaperForeEdge(shellStyle: ShellStyle) {
    val foreEdgeWidth = if (shellStyle == ShellStyle.BOOK) 10.dp else 6.dp
    Box(
        modifier = Modifier
            .align(Alignment.CenterEnd)
            .width(foreEdgeWidth)
            .fillMaxHeight()
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color.Transparent,
                        GoaldayDesign.PaperAged.copy(alpha = 0.25f),
                        GoaldayDesign.PaperAged.copy(alpha = if (shellStyle == ShellStyle.BOOK) 0.42f else 0.32f),
                    ),
                ),
            ),
    )
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
                val lineColor = GoaldayDesign.PaperLine.copy(alpha = 0.28f)
                val stroke = 1.dp.toPx().coerceAtMost(1.2f)

                // 中央装订阴影：真实书脊处纸张凹陷
                drawRect(
                    brush = Brush.horizontalGradient(
                        listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.03f),
                            Color.Black.copy(alpha = 0.09f),
                            Color.Black.copy(alpha = 0.03f),
                            Color.Transparent,
                        ),
                        startX = centerX - 18.dp.toPx(),
                        endX = centerX + 18.dp.toPx(),
                    ),
                    size = androidx.compose.ui.geometry.Size(36.dp.toPx(), height),
                    topLeft = Offset(centerX - 18.dp.toPx(), 0f),
                )

                // 中央装订线
                drawLine(
                    color = GoaldayDesign.BlackOverlayHairline,
                    start = Offset(centerX, 0f),
                    end = Offset(centerX, height),
                    strokeWidth = stroke,
                )

                // 页面轻微纸张纹理：使用非常淡的噪点感竖线
                val textureColor = GoaldayDesign.PaperAged.copy(alpha = 0.06f)
                val textureSpacing = 6.dp.toPx()
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
                        listOf(GoaldayDesign.WhiteHighlight.copy(alpha = 0.35f), Color.Transparent),
                    ),
                    size = androidx.compose.ui.geometry.Size(width, 20.dp.toPx()),
                )

                // 底部厚度阴影
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(Color.Transparent, GoaldayDesign.BookBoardDark.copy(alpha = 0.06f)),
                    ),
                    topLeft = Offset(0f, height - 20.dp.toPx()),
                    size = androidx.compose.ui.geometry.Size(width, 20.dp.toPx()),
                )
            },
    ) {
        // 左页：略暖
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxHeight()
                .fillMaxWidth(0.5f)
                .background(GoaldayDesign.PaperGradient),
        )
        // 右页：略亮
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .fillMaxWidth(0.5f)
                .background(
                    Brush.linearGradient(
                        listOf(GoaldayDesign.PaperAged, GoaldayDesign.PaperWarm, GoaldayDesign.Paper),
                        start = Offset.Zero,
                        end = Offset(320f, 260f),
                    ),
                ),
        )
    }
}
