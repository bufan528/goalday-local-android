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
    val outerShape = if (shellStyle == ShellStyle.BOOK) RoundedCornerShape(28.dp) else RoundedCornerShape(GoaldayDesign.Radius2XL)
    val innerShape = if (shellStyle == ShellStyle.BOOK) RoundedCornerShape(22.dp) else RoundedCornerShape(GoaldayDesign.RadiusXL)
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
                    // 真实书皮：温润的胭粉渐变，避免高饱和原色
                    Brush.linearGradient(
                        listOf(Color(0xFFD8A2AE), Color(0xFFFFF2F6), Color(0xFFE4B5C0)),
                        start = Offset.Zero,
                        end = Offset(820f, 640f),
                    )
                } else {
                    Brush.verticalGradient(listOf(Color(0xFFFDFDFD), Color(0xFFFDFDFD)))
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
            // 书脊（左侧装订线）：单层柔和阴影，模拟真实书脊
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .width(22.dp)
                    .fillMaxHeight()
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0x665A3440), Color(0x448A5361), Color.Transparent),
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
                    .background(Color(0x44FFF7FA)),
            )
            // 书脊烫金标题
            Text(
                "GOALDAY",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFFDECF1).copy(alpha = 0.88f),
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .graphicsLayer { rotationZ = -90f }
                    .padding(top = 4.dp),
            )
            // 书页边缘（右侧）：单层柔和阴影暗示书页厚度
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .width(8.dp)
                    .fillMaxHeight()
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color.Transparent, Color(0x40A07D5E), Color(0x66EED8C4)),
                        ),
                    ),
            )
            // 书顶高光：单层柔和
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(24.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.White.copy(alpha = 0.20f), Color.Transparent),
                        ),
                    ),
            )
            // 书底阴影：单层柔和
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(20.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, GoaldayDesign.PrimaryAction.copy(alpha = 0.10f)),
                        ),
                    ),
            )
        }

        // 纸张层：非 BOOK 模式渲染白色背景；BOOK 模式由 OpenBookPaperChrome 统一处理
        if (shellStyle != ShellStyle.BOOK) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = pageInsetH, vertical = pageInsetV)
                    .clip(innerShape)
                    .background(Color(0xFFFFFFFF)),
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
        // 右页：略亮的纸渐变
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .fillMaxWidth(0.5f)
                .background(
                    Brush.linearGradient(
                        listOf(GoaldayDesign.PaperAged, GoaldayDesign.PaperWarm, Color(0xFFFFFFFF)),
                        start = Offset.Zero,
                        end = Offset(680f, 420f),
                    ),
                ),
        )
        // 中央书脊阴影：单层柔和
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
        // 顶部高光：单层
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(28.dp)
                .background(
                    Brush.verticalGradient(listOf(Color(0x44FFFFFF), Color.Transparent)),
                ),
        )
        // 底部柔和阴影：单层
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(32.dp)
                .background(
                    Brush.verticalGradient(listOf(Color.Transparent, Color(0x18B88A58))),
                ),
        )
    }
}
