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
 * 书壳：对照逆向资源的"quiet and flat"设计风格。
 * - 移除书脊渐变、阴影、边框等重度装饰
 * - 只保留基本纸张背景和圆角
 * - 内容直接铺满，让页面看起来像一张干净的纸
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
    // 统一圆角，不再区分书脊和书口
    val cornerRadius = GoaldayDesign.RadiusL
    val outerShape = RoundedCornerShape(cornerRadius)

    // 纸张内边距：给内容留出呼吸空间
    val pageInsetH = GoaldayDesign.Space3
    val pageInsetV = GoaldayDesign.Space2
    // 翻页热区
    val edgeZoneWidth = GoaldayDesign.Space5

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(outerShape)
            .background(GoaldayDesign.adaptivePaperGradient),
    ) {
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


