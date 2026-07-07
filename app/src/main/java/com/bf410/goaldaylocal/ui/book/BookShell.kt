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
import androidx.compose.foundation.border
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
    // 左侧书脊圆角小，右侧书口圆角大，形成"书翻开"的不对称轮廓
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
        // 简化版：移除重度装饰，保留基本纸张背景
        // 参考 app 的设计是"quiet and flat"，避免过度分层

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


