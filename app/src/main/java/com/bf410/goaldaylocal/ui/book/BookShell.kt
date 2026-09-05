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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import android.app.Activity
import android.graphics.Rect
import android.os.Build
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import com.bf410.goaldaylocal.ui.replica.GoaldayDesign
import com.bf410.goaldaylocal.ui.replica.LocalGoaldayDarkMode

/**
 * 书页纸张色：对照原版 BaseBookViewKt，页面使用纯白 Color.White。
 */
private val BookPagePaper = Color.White

enum class ShellStyle {
    LIGHT,
    BOOK,
}

/**
 * 书壳：模拟一本真正打开的纸质手账。
 *
 * 设计要点（对照原版 APK）：
 * - 外层为硬壳书皮，使用 book_cover_fabric 布纹贴图，叠加极淡暖色，保留原版米白布纹质感。
 * - 四周留出适度书边，但不过度挤压版心。
 * - 中央书脊只有一道极淡的装订压痕，不抢戏。
 * - 左右页面向书脊微微弯曲的阴影，营造纸张弯入装订处的立体感。
 * - 书口/书脚用多层细线模拟一叠纸页的厚度，但颜色很淡。
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
    // 对照原版 BaseBookViewKt.java L499-514 + BookFlipConfig.java L196：
    // 书壳右侧圆角 = 10dp（原版默认值），左平右圆 RoundedCornerShape(0, 10, 10, 0)
    val coverCorner = if (isBookStyle) 10.dp else GoaldayDesign.RadiusL
    val pageCorner = if (isBookStyle) 10.dp else GoaldayDesign.RadiusL
    // 对照项目硬约束：书本容器占满可用内容区域（fillMaxSize）
    // 对照原版 BaseBookViewKt：RoundedCornerShape(0, l, l, 0)
    // 左侧（书脊侧）平角，右侧（页缘侧）圆角
    val coverShape = if (isBookStyle) {
        RoundedCornerShape(
            topStart = 0.dp,
            topEnd = coverCorner,
            bottomEnd = coverCorner,
            bottomStart = 0.dp,
        )
    } else {
        RoundedCornerShape(coverCorner)
    }
    val pageShape = if (isBookStyle) {
        RoundedCornerShape(
            topStart = 0.dp,
            topEnd = pageCorner,
            bottomEnd = pageCorner,
            bottomStart = 0.dp,
        )
    } else {
        RoundedCornerShape(pageCorner)
    }
    // 对照项目硬约束：HANDBOOK 书本容器占满可用内容区域（fillMaxSize）。
    // 该尺寸行为用于匹配原始 APK 的书页显示比例与交互热区。
    // 热区加宽，方便拇指翻页，同时避免系统返回手势冲突
    val edgeZoneWidth = 56.dp
    val view = LocalView.current
    var bookBounds by remember { mutableStateOf<Rect?>(null) }

    // 把当前书壳区域（左右翻页热区）排除在系统边缘返回手势之外
    DisposableEffect(isBookStyle, turnEnabled, bookBounds) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || !isBookStyle || !turnEnabled) {
            return@DisposableEffect onDispose { }
        }
        val bounds = bookBounds
        val decor = (view.context as? Activity)?.window?.decorView
        if (decor != null && bounds != null && bounds.width() > 0 && bounds.height() > 0) {
            val edgePx = with(view.context.resources.displayMetrics) {
                (56f * density).toInt()
            }
            val leftRect = Rect(bounds.left, bounds.top, bounds.left + edgePx, bounds.bottom)
            val rightRect = Rect(bounds.right - edgePx, bounds.top, bounds.right, bounds.bottom)
            decor.systemGestureExclusionRects = listOf(leftRect, rightRect)
        }
        onDispose {
            decor?.systemGestureExclusionRects = emptyList()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned {
                val pos = it.positionInWindow()
                bookBounds = Rect(
                    pos.x.toInt(),
                    pos.y.toInt(),
                    (pos.x + it.size.width).toInt(),
                    (pos.y + it.size.height).toInt(),
                )
            },
    ) {
        if (isBookStyle) {
            // 对照原版 BaseBookViewKt.java L494-501：
            // 书壳阴影 = 10dp（原版值），阴影颜色 = #FFC5BBB6 (color_tab_divider)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .shadow(
                        elevation = 10.dp,
                        shape = coverShape,
                        clip = false,
                        ambientColor = Color(0xFFC5BBB6),
                        spotColor = Color(0xFFC5BBB6),
                    )
                    .clip(coverShape)
                    .background(BookPagePaper),
            ) {
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

                // 内容直接填充书页
                CompositionLocalProvider(LocalGoaldayDarkMode provides false) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        content()
                    }
                }
            }
        } else {
            // LIGHT 模式：干净卡片式
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .shadow(
                        elevation = 4.dp,
                        shape = pageShape,
                        clip = false,
                    )
                    .clip(pageShape)
                    .background(GoaldayDesign.Paper),
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
