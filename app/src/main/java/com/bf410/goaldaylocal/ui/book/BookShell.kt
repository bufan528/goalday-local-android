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
    // 真实书本造型：书脊侧（start/左）保留极小圆角近似直角，书口侧（end/右）大圆角
    val outerShape = if (shellStyle == ShellStyle.BOOK) {
        RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp, topEnd = 28.dp, bottomEnd = 28.dp)
    } else {
        RoundedCornerShape(GoaldayDesign.Radius2XL)
    }
    val innerShape = if (shellStyle == ShellStyle.BOOK) {
        RoundedCornerShape(topStart = 2.dp, bottomStart = 2.dp, topEnd = 22.dp, bottomEnd = 22.dp)
    } else {
        RoundedCornerShape(GoaldayDesign.RadiusXL)
    }
    val outerPaddingH = if (shellStyle == ShellStyle.BOOK) 6.dp else 10.dp
    val outerPaddingV = if (shellStyle == ShellStyle.BOOK) 4.dp else 8.dp
    // P0-2 修复：BOOK 模式翻页热区从 6dp 提升到 24dp
    // 原 6dp 宽度过窄，用户难以精确点击翻页，尤其与内容区边缘重合时容易误触
    // 24dp 宽 + 撑满高度的触控面积满足无障碍标准，且不侵占内容区
    val edgeZoneWidth = if (shellStyle == ShellStyle.BOOK) 24.dp else 20.dp
    val pageInsetH = if (shellStyle == ShellStyle.BOOK) 12.dp else 8.dp
    val pageInsetV = if (shellStyle == ShellStyle.BOOK) 12.dp else 10.dp

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
                    // P0-3 大修：书皮改用深色皮革/木质 token，替代原粉色渐变
                    // 真实手账书皮多为深棕皮革或亚麻布面，原 Pink 渐变看起来像粉色塑料壳
                    // 用 BookSpine(深棕)→BookBoardDark(中棕)→BookSpine(深棕) 模拟皮革光感
                    Brush.linearGradient(
                        listOf(GoaldayDesign.BookSpine, GoaldayDesign.BookBoardDark, GoaldayDesign.BookSpine),
                        start = Offset.Zero,
                        end = Offset(820f, 640f),
                    )
                } else {
                    // 非 BOOK 模式：用 Paper 系纸张渐变替代原纯白（提升纸张暖意）
                    Brush.verticalGradient(listOf(GoaldayDesign.Paper, GoaldayDesign.PaperWarm))
                },
            )
            .then(
                if (shellStyle == ShellStyle.BOOK) {
                    // P0-3：书皮边框改用金色细线（模拟皮革包边烫金），原白色细线与深色书皮不搭
                    Modifier.border(GoaldayDesign.Hairline, GoaldayDesign.BookSpineLight.copy(alpha = 0.55f), outerShape)
                } else {
                    Modifier
                },
            ),
    ) {
        if (shellStyle == ShellStyle.BOOK) {
            // P0-3 大修：书脊加宽到 32dp（原 22dp 烫金标题几乎不可读）
            // 书脊阴影改用 BookSpineDark 深色，模拟装订线凹陷的深色暗调（原 Pink.alpha 与深色书皮不搭）
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .width(32.dp)
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
            // 书脊装订缝线（双线模拟手账穿线装订）
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 6.dp, top = 24.dp, bottom = 24.dp)
                    .width(0.8.dp)
                    .fillMaxHeight()
                    .background(GoaldayDesign.BookSpineLight.copy(alpha = 0.45f)),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 10.dp, top = 24.dp, bottom = 24.dp)
                    .width(0.8.dp)
                    .fillMaxHeight()
                    .background(GoaldayDesign.BookSpineLight.copy(alpha = 0.30f)),
            )
            // 书脊烫金标题：加宽后用 labelMedium 字号提升可读性
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .width(32.dp)
                    .fillMaxHeight()
                    .padding(top = 28.dp, bottom = 28.dp),
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
            // P0-3 新增：书签丝带（手账标志性元素），从书脊顶部垂下，丝带颜色用 Pink 形成深棕底色中的点睛之笔
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(start = 28.dp, top = 0.dp)
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
            // 书签丝带末端 V 形切口（用两个旋转小方块模拟，简化实现）
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(start = 28.dp, top = 116.dp)
                    .width(10.dp)
                    .height(8.dp)
                    .background(GoaldayDesign.Pink.copy(alpha = 0.55f))
                    .graphicsLayer { rotationZ = 45f },
            )
            // P0-3 大修：书页边缘（右侧）用多条间隔线模拟书页层叠纹理
            // 原 8dp 单层 PaperAged 阴影无法表现多页厚度，现用 3 条间隔线 + 渐变阴影
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
            // BOOK 模式：OpenBookPaperChrome 统一处理纸张背景 + 左右页渐变 + 中央书脊
            OpenBookPaperChrome(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = pageInsetH, vertical = pageInsetV),
            )
        }

        // 中央书脊线：仅非 BOOK 模式（BOOK 模式由 OpenBookPaperChrome 内部处理）
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
        // 右页：略亮的纸渐变（末段硬编码白收敛到 Surface token）
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
        // P1 修复：中央书脊阴影精简为 3 段渐变，宽度从 28dp 收窄到 18dp
        // 原 7 段渐变 + 28dp 宽 + BlackOverlayStrong 最深处像一道黑色横条横贯页面
        // 现 3 段渐变（Transparent→Medium→Transparent）+ 18dp 宽，凹陷感柔和自然
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .width(18.dp)
                .fillMaxHeight()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color.Transparent,
                            GoaldayDesign.BlackOverlayMedium,
                            Color.Transparent,
                        ),
                    ),
                ),
        )
        // 中央书脊装订缝线（细深线，强化"书被翻开"的视觉）
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .width(0.6.dp)
                .fillMaxHeight()
                .background(GoaldayDesign.BlackOverlayLine),
        )
        // 顶部高光：单层（收敛到 White alpha）
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(28.dp)
                .background(
                    Brush.verticalGradient(listOf(GoaldayDesign.WhiteHighlight, Color.Transparent)),
                ),
        )
        // P0-3 大修：底部阴影改用 BookBoardDark 暖棕系，与深色书皮统一（原 Pink alpha 与深棕书皮不搭）
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
