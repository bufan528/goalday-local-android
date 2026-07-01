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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bf410.goaldaylocal.ui.replica.GoaldayDesign

enum class ShellStyle {
    LIGHT,
    BOOK,
}

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
    // Hobonichi 风格：清爽圆角，书脊侧略小
    val outerShape = if (shellStyle == ShellStyle.BOOK) {
        RoundedCornerShape(
            topStart = GoaldayDesign.Space1,
            bottomStart = GoaldayDesign.Space1,
            topEnd = GoaldayDesign.RadiusL,
            bottomEnd = GoaldayDesign.RadiusL,
        )
    } else {
        RoundedCornerShape(GoaldayDesign.Radius2XL)
    }
    val innerShape = if (shellStyle == ShellStyle.BOOK) {
        RoundedCornerShape(
            topStart = 2.dp,
            bottomStart = 2.dp,
            topEnd = GoaldayDesign.RadiusM,
            bottomEnd = GoaldayDesign.RadiusM,
        )
    } else {
        RoundedCornerShape(GoaldayDesign.RadiusXL)
    }
    val outerPaddingH = if (shellStyle == ShellStyle.BOOK) 5.dp else 10.dp
    val outerPaddingV = if (shellStyle == ShellStyle.BOOK) 4.dp else 8.dp
    val edgeZoneWidth = if (shellStyle == ShellStyle.BOOK) GoaldayDesign.Space6 else GoaldayDesign.Space5
    val pageInsetH = if (shellStyle == ShellStyle.BOOK) GoaldayDesign.Space2 else GoaldayDesign.Space2
    val pageInsetV = if (shellStyle == ShellStyle.BOOK) GoaldayDesign.Space2 else GoaldayDesign.Space2 + 2.dp

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = outerPaddingH, vertical = outerPaddingV)
            .shadow(
                if (shellStyle == ShellStyle.BOOK) GoaldayDesign.ShadowLarge else GoaldayDesign.ShadowMedium,
                outerShape,
                clip = false,
            )
            .clip(outerShape)
            .background(
                if (shellStyle == ShellStyle.BOOK) {
                    // Hobonichi 风格封面：暖色棉麻质感，轻渐变
                    Brush.linearGradient(
                        listOf(
                            GoaldayDesign.BookBoard,
                            GoaldayDesign.BookBoardLight,
                            GoaldayDesign.BookBoard,
                        ),
                        start = Offset.Zero,
                        end = Offset(680f, 920f),
                    )
                } else {
                    Brush.verticalGradient(listOf(GoaldayDesign.Paper, GoaldayDesign.PaperWarm))
                },
            )
            .then(
                if (shellStyle == ShellStyle.BOOK) {
                    // 封面边框：细线烫印
                    Modifier.border(GoaldayDesign.Hairline, GoaldayDesign.BookBoardDark.copy(alpha = 0.25f), outerShape)
                } else {
                    Modifier
                },
            ),
    ) {
        if (shellStyle == ShellStyle.BOOK) {
            // === 书脊区域（左侧装订） ===
            // 装订凹陷渐变：从深到透明
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .width(28.dp)
                    .fillMaxHeight()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                GoaldayDesign.BookBoardDark.copy(alpha = 0.35f),
                                GoaldayDesign.BookBoardDark.copy(alpha = 0.12f),
                                Color.Transparent,
                            ),
                        ),
                    ),
            )
            // 装订缝线：双线模拟穿线
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 5.dp, top = GoaldayDesign.Space6, bottom = GoaldayDesign.Space6)
                    .width(0.6.dp)
                    .fillMaxHeight()
                    .background(GoaldayDesign.BookBoardDark.copy(alpha = 0.25f)),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 9.dp, top = GoaldayDesign.Space6, bottom = GoaldayDesign.Space6)
                    .width(0.6.dp)
                    .fillMaxHeight()
                    .background(GoaldayDesign.BookBoardDark.copy(alpha = 0.18f)),
            )

            // === 书页边缘（右侧书口） ===
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .width(8.dp)
                    .fillMaxHeight()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                GoaldayDesign.PaperAged.copy(alpha = 0.25f),
                                GoaldayDesign.PaperAged.copy(alpha = 0.40f),
                            ),
                        ),
                    ),
            )
            // 书页层叠细纹
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 2.dp)
                    .width(0.4.dp)
                    .fillMaxHeight()
                    .background(GoaldayDesign.BookBoardDark.copy(alpha = 0.12f)),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 5.dp)
                    .width(0.4.dp)
                    .fillMaxHeight()
                    .background(GoaldayDesign.BookBoardDark.copy(alpha = 0.08f)),
            )
        }

        // 纸张层
        if (shellStyle != ShellStyle.BOOK) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = pageInsetH, vertical = pageInsetV)
                    .clip(innerShape)
                    .background(GoaldayDesign.Paper),
            )
        }

        if (shellStyle == ShellStyle.BOOK) {
            OpenBookPaperChrome(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = pageInsetH, vertical = pageInsetV),
            )
        }

        // 中央书脊线：仅非 BOOK 模式
        if (shellStyle != ShellStyle.BOOK) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color.Transparent, GoaldayDesign.BlackOverlayHairline, Color.Transparent),
                        ),
                    ),
            )
        }

        content()

        // 左右翻页热区
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
private fun OpenBookPaperChrome(modifier: Modifier = Modifier) {
    val pageShape = RoundedCornerShape(GoaldayDesign.RadiusXL)
    Box(
        modifier = modifier
            .clip(pageShape)
            .background(GoaldayDesign.Paper),
    ) {
        // 左页：温暖纸渐变
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxHeight()
                .fillMaxWidth(0.5f)
                .background(GoaldayDesign.PaperGradient),
        )
        // 右页：略亮的纸渐变
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .fillMaxWidth(0.5f)
                .background(
                    Brush.linearGradient(
                        listOf(GoaldayDesign.PaperAged, GoaldayDesign.PaperWarm, GoaldayDesign.adaptiveSurface),
                        start = Offset.Zero,
                        end = Offset(680f, 420f),
                    ),
                ),
        )
        // 中央书脊阴影：宽暗带模拟装订凹陷
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .width(20.dp)
                .fillMaxHeight()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.04f),
                            Color.Black.copy(alpha = 0.10f),
                            Color.Black.copy(alpha = 0.04f),
                            Color.Transparent,
                        ),
                    ),
                ),
        )
        // 中央装订线
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .width(0.5.dp)
                .fillMaxHeight()
                .background(GoaldayDesign.BlackOverlayHairline),
        )
        // 顶部高光
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(20.dp)
                .background(
                    Brush.verticalGradient(listOf(GoaldayDesign.WhiteHighlight, Color.Transparent)),
                ),
        )
        // 底部暗影
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(24.dp)
                .background(
                    Brush.verticalGradient(listOf(Color.Transparent, GoaldayDesign.BookBoardDark.copy(alpha = 0.10f))),
                ),
        )
    }
}
