package com.bf410.goaldaylocal.ui.book

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
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
    // 真实书本造型：四周统一圆角，书脊侧略小
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
    val outerPaddingH = if (shellStyle == ShellStyle.BOOK) 6.dp else 10.dp
    val outerPaddingV = if (shellStyle == ShellStyle.BOOK) 4.dp else 8.dp
    // 翻页热区：足够宽让用户轻松点击
    val edgeZoneWidth = if (shellStyle == ShellStyle.BOOK) GoaldayDesign.Space6 else GoaldayDesign.Space5
    val pageInsetH = if (shellStyle == ShellStyle.BOOK) GoaldayDesign.Space3 else GoaldayDesign.Space2
    val pageInsetV = if (shellStyle == ShellStyle.BOOK) GoaldayDesign.Space3 else GoaldayDesign.Space2 + 2.dp

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
                    // 皮革质感：多层渐变模拟真实书皮
                    Brush.linearGradient(
                        listOf(
                            GoaldayDesign.BookSpine,
                            GoaldayDesign.BookBoardDark,
                            GoaldayDesign.BookSpineLight,
                            GoaldayDesign.BookBoardDark,
                            GoaldayDesign.BookSpine,
                        ),
                        start = Offset.Zero,
                        end = Offset(820f, 640f),
                    )
                } else {
                    Brush.verticalGradient(listOf(GoaldayDesign.Paper, GoaldayDesign.PaperWarm))
                },
            )
            .then(
                if (shellStyle == ShellStyle.BOOK) {
                    // 书皮边框：金色细线模拟皮革包边烫金
                    Modifier.border(GoaldayDesign.Hairline, GoaldayDesign.BookSpineLight.copy(alpha = 0.55f), outerShape)
                } else {
                    Modifier
                },
            ),
    ) {
        if (shellStyle == ShellStyle.BOOK) {
            // === 书脊区域（左侧装订侧） ===
            // 书脊渐变：从深到浅模拟装订凹陷
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .width(36.dp)
                    .fillMaxHeight()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                GoaldayDesign.BookSpine,
                                GoaldayDesign.BookBoardDark.copy(alpha = 0.85f),
                                Color.Transparent,
                            ),
                        ),
                    ),
            )
            // 书脊装订缝线：双线模拟手账穿线
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 6.dp, top = GoaldayDesign.Space6, bottom = GoaldayDesign.Space6)
                    .width(0.8.dp)
                    .fillMaxHeight()
                    .background(GoaldayDesign.BookSpineLight.copy(alpha = 0.40f)),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 10.dp, top = GoaldayDesign.Space6, bottom = GoaldayDesign.Space6)
                    .width(0.8.dp)
                    .fillMaxHeight()
                    .background(GoaldayDesign.BookSpineLight.copy(alpha = 0.30f)),
            )
            // 书脊烫金标题
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .width(36.dp)
                    .fillMaxHeight()
                    .padding(top = 32.dp, bottom = 32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "GOALDAY",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = GoaldayDesign.BookSpineLight.copy(alpha = 0.92f),
                    modifier = Modifier.graphicsLayer { rotationZ = -90f },
                )
            }

            // === 书签丝带 ===
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(start = 32.dp, top = 0.dp)
                    .width(10.dp)
                    .height(120.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                GoaldayDesign.Pink.copy(alpha = 0.92f),
                                GoaldayDesign.Pink.copy(alpha = 0.78f),
                                GoaldayDesign.Pink.copy(alpha = 0.55f),
                            ),
                        ),
                    ),
            )
            // 书签丝带末端 V 形切口
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(start = 32.dp, top = 116.dp)
                    .width(10.dp)
                    .height(8.dp)
                    .background(GoaldayDesign.Pink.copy(alpha = 0.55f))
                    .graphicsLayer { rotationZ = 45f },
            )

            // === 书页边缘（右侧书口） ===
            // 多层页面厚度感
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .width(12.dp)
                    .fillMaxHeight()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                GoaldayDesign.PaperAged.copy(alpha = 0.20f),
                                GoaldayDesign.PaperAged.copy(alpha = 0.35f),
                                GoaldayDesign.PaperAged.copy(alpha = 0.50f),
                            ),
                        ),
                    ),
            )
            // 书页层叠细纹 1
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 3.dp)
                    .width(0.5.dp)
                    .fillMaxHeight()
                    .background(GoaldayDesign.BookBoardDark.copy(alpha = 0.18f)),
            )
            // 书页层叠细纹 2
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 6.dp)
                    .width(0.5.dp)
                    .fillMaxHeight()
                    .background(GoaldayDesign.BookBoardDark.copy(alpha = 0.12f)),
            )
            // 书页层叠细纹 3
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 9.dp)
                    .width(0.5.dp)
                    .fillMaxHeight()
                    .background(GoaldayDesign.BookBoardDark.copy(alpha = 0.08f)),
            )
        }

        // 纸张层：非 BOOK 模式渲染纸张背景；BOOK 模式由 OpenBookPaperChrome 统一处理
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
        // 中央书脊阴影：宽暗带模拟书页装订处的深凹陷
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .width(24.dp)
                .fillMaxHeight()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.06f),
                            Color.Black.copy(alpha = 0.14f),
                            Color.Black.copy(alpha = 0.06f),
                            Color.Transparent,
                        ),
                    ),
                ),
        )
        // 中央装订缝线
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .width(0.6.dp)
                .fillMaxHeight()
                .background(GoaldayDesign.BlackOverlayLine),
        )
        // 顶部高光
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(28.dp)
                .background(
                    Brush.verticalGradient(listOf(GoaldayDesign.WhiteHighlight, Color.Transparent)),
                ),
        )
        // 底部阴影
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(32.dp)
                .background(
                    Brush.verticalGradient(listOf(Color.Transparent, GoaldayDesign.BookBoardDark.copy(alpha = 0.18f))),
                ),
        )
    }
}
