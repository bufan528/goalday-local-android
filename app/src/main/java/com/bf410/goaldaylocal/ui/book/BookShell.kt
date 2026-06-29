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
    val edgeZoneWidth = if (shellStyle == ShellStyle.BOOK) 6.dp else 20.dp
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
                    // 真实书皮：温润的胭粉渐变（统一走 Pink token 系，原硬编码已收敛）
                    Brush.linearGradient(
                        listOf(GoaldayDesign.Pink, GoaldayDesign.PinkSoft, GoaldayDesign.Pink.copy(alpha = 0.88f)),
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
                    Modifier.border(GoaldayDesign.Hairline, Color.White.copy(alpha = 0.40f), outerShape)
                } else {
                    Modifier
                },
            ),
    ) {
        if (shellStyle == ShellStyle.BOOK) {
            // 书脊（左侧装订线）：粉色系阴影（原棕色与粉色书皮不搭，统一到 Pink 系）
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .width(22.dp)
                    .fillMaxHeight()
                    .background(
                        Brush.horizontalGradient(
                            listOf(GoaldayDesign.Pink.copy(alpha = 0.40f), GoaldayDesign.Pink.copy(alpha = 0.27f), Color.Transparent),
                        ),
                    ),
            )
            // 书脊高光线
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 4.dp, top = 28.dp, bottom = 28.dp)
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(Color.White.copy(alpha = 0.27f)),
            )
            // 书脊烫金标题：用固定尺寸 Box 包裹避免旋转后定位漂移
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .width(22.dp)
                    .fillMaxHeight()
                    .padding(top = 28.dp, bottom = 28.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "GOALDAY",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = GoaldayDesign.PinkSoft.copy(alpha = 0.88f),
                    modifier = Modifier.graphicsLayer { rotationZ = -90f },
                )
            }
            // 书页边缘（右侧）：PaperAged 系阴影暗示书页厚度（原棕色硬编码已收敛）
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .width(8.dp)
                    .fillMaxHeight()
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color.Transparent, GoaldayDesign.PaperAged.copy(alpha = 0.25f), GoaldayDesign.PaperAged.copy(alpha = 0.40f)),
                        ),
                    ),
            )
            // 删除书顶/书底高光阴影（与 OpenBookPaperChrome 内部重复，由其统一负责）
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
                            listOf(Color.Transparent, Color(0x14000000), Color.Transparent),
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
    val pageShape = RoundedCornerShape(20.dp)
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
                        listOf(GoaldayDesign.PaperAged, GoaldayDesign.PaperWarm, GoaldayDesign.Surface),
                        start = Offset.Zero,
                        end = Offset(680f, 420f),
                    ),
                ),
        )
        // 中央书脊阴影：单层柔和（黑色系中性，保留）
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .width(18.dp)
                .fillMaxHeight()
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0x22000000), Color.Transparent, Color(0x14000000)),
                    ),
                ),
        )
        // 顶部高光：单层（收敛到 White alpha）
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(28.dp)
                .background(
                    Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.27f), Color.Transparent)),
                ),
        )
        // 底部柔和阴影：单层（原棕色硬编码收敛到 Pink 系，与书皮统一）
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(32.dp)
                .background(
                    Brush.verticalGradient(listOf(Color.Transparent, GoaldayDesign.Pink.copy(alpha = 0.10f))),
                ),
        )
    }
}
