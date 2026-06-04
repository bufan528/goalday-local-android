package com.bf410.goaldaylocal.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bf410.goaldaylocal.ui.replica.GoaldayDesign

internal const val KEY_GUIDE_SEEN = "guide_seen_v1"

private data class GuidePage(
    val label: String,
    val title: String,
    val body: String,
    val action: String,
    val focus: String,
    val asset: String,
    val star: String,
    val tone: Color,
)

@Composable
internal fun GuideOverlay(
    onClose: () -> Unit,
) {
    val pages = remember {
        listOf(
            GuidePage("目标", "先把目标放进手账", "从灵感中心选择主题，勾选目标后可以导入任务池，也可以保存成一本手账。", "选主题 · 勾目标", "聚焦：灵感中心", "lottie/book.png", "lottie/star_pink.png", Color(0xFFE88FAE)),
            GuidePage("日程", "把任务排进今天", "在日程页把任务拖入日期，或者点目标卡片的排入按钮；桌面小组件会跟着刷新。", "拖入日期 · 标记完成", "聚焦：手账/整月", "lottie/img_4.png", "lottie/star_green.png", Color(0xFF6F8E68)),
            GuidePage("日记", "每天写成块", "日记支持文字块、目标块、专题目标块和图片；完成的目标也能直接关联到日记。", "写文字 · 贴目标", "聚焦：日记块", "lottie/card.png", "lottie/star_purple.png", Color(0xFFB07A8F)),
            GuidePage("导出", "最后导出成手账图", "日记和日程手账都可以预览长图，再保存、分享或调用系统打印。", "预览 · 分享 · 打印", "聚焦：长图预览", "lottie/img_8.png", "lottie/star_yellow.png", Color(0xFFB88A58)),
        )
    }
    var index by remember { mutableIntStateOf(0) }
    val page = pages[index.coerceIn(0, pages.lastIndex)]
    val context = LocalContext.current
    val hasLocalGuideAssets = remember {
        runCatching {
            context.assets.open("lottie/goalday.json").use { }
            context.assets.open("lottie/coupon.json").use { }
            true
        }.getOrDefault(false)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFFFFCF7),
                        Color(0xFFFFF0E4),
                        page.tone.copy(alpha = 0.22f),
                    ),
                ),
            )
            .padding(horizontal = 18.dp, vertical = 16.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Goalday", style = MaterialTheme.typography.titleLarge, color = GoaldayDesign.InkPrimary, fontWeight = FontWeight.SemiBold)
                    Text(
                        if (hasLocalGuideAssets) "GuideActivity style · local assets" else "compose guide",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (hasLocalGuideAssets) GoaldayDesign.Pink else GoaldayDesign.InkMuted,
                    )
                }
                Text(
                    "跳过",
                    style = MaterialTheme.typography.labelSmall,
                    color = GoaldayDesign.InkMuted,
                    modifier = Modifier
                        .clip(RoundedCornerShape(99.dp))
                        .clickable(onClick = onClose)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }
            GuideIllustration(page = page, index = index)
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalAlignment = Alignment.CenterVertically) {
                pages.forEachIndexed { dotIndex, _ ->
                    Box(
                        modifier = Modifier
                            .width(if (dotIndex == index) 24.dp else 8.dp)
                            .height(8.dp)
                            .clip(RoundedCornerShape(99.dp))
                            .background(if (dotIndex == index) GoaldayDesign.Pink else Color(0xFFD8CFC5)),
                    )
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White.copy(alpha = 0.78f))
                    .border(1.dp, Color.White.copy(alpha = 0.72f), RoundedCornerShape(24.dp))
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(13.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(page.label, style = MaterialTheme.typography.labelMedium, color = page.tone, fontWeight = FontWeight.SemiBold)
                    Text(page.title, style = MaterialTheme.typography.headlineSmall, color = GoaldayDesign.InkPrimary, fontWeight = FontWeight.SemiBold)
                    Text(page.body, style = MaterialTheme.typography.bodyMedium, color = GoaldayDesign.InkSecondary)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (index == 0) "从头开始" else "上一步",
                        color = if (index == 0) GoaldayDesign.InkMuted else GoaldayDesign.InkSecondary,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier
                            .clip(RoundedCornerShape(99.dp))
                            .clickable {
                                if (index > 0) index -= 1
                            }
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                    )
                    Text(
                        if (index == pages.lastIndex) "完成" else "下一步",
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(99.dp))
                            .background(page.tone)
                            .clickable {
                                if (index == pages.lastIndex) {
                                    onClose()
                                } else {
                                    index += 1
                                }
                            }
                            .padding(horizontal = 18.dp, vertical = 9.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun GuideIllustration(
    page: GuidePage,
    index: Int,
) {
    val context = LocalContext.current
    val mainAsset = remember(page.asset) { loadGuideAssetBitmap(context, page.asset) }
    val starAsset = remember(page.star) { loadGuideAssetBitmap(context, page.star) }
    val whiteStar = remember { loadGuideAssetBitmap(context, "lottie/star_white.png") }
    val motion = rememberInfiniteTransition(label = "guide-motion")
    val pulse by motion.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(animation = tween(900), repeatMode = RepeatMode.Reverse),
        label = "guide-pulse",
    )
    val glide by motion.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(1800), repeatMode = RepeatMode.Reverse),
        label = "guide-glide",
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(330.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.radialGradient(
                    listOf(
                        Color.White.copy(alpha = 0.95f),
                        page.tone.copy(alpha = 0.24f),
                        Color(0xFFFFF5EA),
                    ),
                ),
            )
            .border(1.dp, Color.White.copy(alpha = 0.72f), RoundedCornerShape(28.dp))
            .padding(18.dp),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(228.dp)
                .graphicsLayer {
                    translationY = (glide - 0.5f) * 18f
                    scaleX = 0.98f + (pulse - 0.88f) * 0.12f
                    scaleY = 0.98f + (pulse - 0.88f) * 0.12f
                },
            contentAlignment = Alignment.Center,
        ) {
            if (mainAsset != null) {
                Image(
                    bitmap = mainAsset.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White.copy(alpha = 0.72f))
                        .border(1.dp, Color(0x22B7A893), RoundedCornerShape(24.dp)),
                )
            }
        }
        listOf(
            Triple(Alignment.TopEnd, 8.dp, 54.dp),
            Triple(Alignment.CenterStart, 18.dp, 42.dp),
            Triple(Alignment.BottomEnd, 34.dp, 34.dp),
        ).forEachIndexed { starIndex, item ->
            val bitmap = if (starIndex == 1) whiteStar else starAsset
            val (alignment, pad, size) = item
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .align(alignment)
                        .padding(pad)
                        .size(size)
                        .graphicsLayer {
                            rotationZ = (glide * 16f) - 8f + starIndex * 7f
                            scaleX = pulse
                            scaleY = pulse
                        },
                )
            } else {
                Box(
                    modifier = Modifier
                        .align(alignment)
                        .padding(pad)
                        .size(size)
                        .clip(RoundedCornerShape(99.dp))
                        .background(page.tone.copy(alpha = 0.25f)),
                )
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = (-96 + glide * 18).dp, y = 92.dp)
                .width(148.dp)
                .height(24.dp)
                .clip(RoundedCornerShape(99.dp))
                .background(Color.White.copy(alpha = 0.52f))
                .border(1.dp, page.tone.copy(alpha = 0.22f), RoundedCornerShape(99.dp)),
        )
        repeat(3) { row ->
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = (-16).dp, y = (row * -20 - 24).dp)
                    .width((68 + row * 12).dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .background(if (row <= index) page.tone.copy(alpha = 0.42f) else Color.White.copy(alpha = 0.62f)),
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = (-24 + glide * 24).dp, y = (-104 + index * 5).dp)
                .width(92.dp)
                .height(22.dp)
                .clip(RoundedCornerShape(99.dp))
                .background(Color.White.copy(alpha = 0.64f))
                .border(1.dp, page.tone.copy(alpha = 0.28f), RoundedCornerShape(99.dp)),
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 76.dp, end = 24.dp)
                .size(58.dp)
                .graphicsLayer {
                    scaleX = pulse
                    scaleY = pulse
                    alpha = 0.24f + (pulse - 0.88f) * 0.9f
                }
                .clip(RoundedCornerShape(99.dp))
                .background(page.tone.copy(alpha = 0.28f)),
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 82.dp, end = 30.dp)
                .size(46.dp)
                .clip(RoundedCornerShape(99.dp))
                .background(page.tone.copy(alpha = 0.88f)),
        ) {
            Text("${index + 1}", color = Color.White, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.align(Alignment.Center))
        }
        Text(
            page.focus,
            color = Color.White,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .align(Alignment.TopStart)
                .clip(RoundedCornerShape(99.dp))
                .background(page.tone.copy(alpha = 0.78f))
                .padding(horizontal = 10.dp, vertical = 5.dp),
        )
        Text(
            page.action,
            color = GoaldayDesign.InkPrimary,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .clip(RoundedCornerShape(99.dp))
                .background(Color.White.copy(alpha = 0.70f))
                .padding(horizontal = 12.dp, vertical = 7.dp),
        )
    }
}

private fun loadGuideAssetBitmap(context: Context, path: String): Bitmap? =
    runCatching {
        context.assets.open(path).use(BitmapFactory::decodeStream)
    }.getOrNull()
