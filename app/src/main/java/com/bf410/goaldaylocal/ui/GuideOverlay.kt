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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import org.json.JSONObject

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
    val target: GuideTarget,
    val tasks: List<String>,
)

internal enum class GuideTarget {
    INSPIRATION,
    HANDBOOK,
    DIARY,
    HOME,
}

@Composable
internal fun GuideOverlay(
    onClose: () -> Unit,
    onOpenTarget: (GuideTarget) -> Unit,
) {
    val pages = remember {
        listOf(
            GuidePage(
                "目标",
                "先把目标放进手账",
                "从灵感中心选择主题，勾选目标后可以导入任务池，也可以保存成一本手账。",
                "进入灵感中心",
                "聚焦：灵感中心",
                "lottie/book.png",
                "lottie/star_pink.png",
                GoaldayDesign.RouteSchedule,
                GuideTarget.INSPIRATION,
                listOf("挑选主题模板", "勾选要执行的目标", "导入今日或保存成新手账"),
            ),
            GuidePage(
                "日程",
                "把任务排进今天",
                "在手账页查看本月日程，把任务放进 todo/done；桌面小组件会跟着刷新。",
                "打开日程手账",
                "聚焦：手账/整月",
                "lottie/img_4.png",
                "lottie/star_green.png",
                GoaldayDesign.RouteTarget,
                GuideTarget.HANDBOOK,
                listOf("翻到日程路线", "把任务安排到日期", "完成后移动到 DONE"),
            ),
            GuidePage(
                "日记",
                "每天写成块",
                "日记支持文字块、目标块、专题目标块和图片；完成的目标也能直接关联到日记。",
                "打开日记页",
                "聚焦：日记块",
                "lottie/card.png",
                "lottie/star_purple.png",
                GoaldayDesign.RouteDiary,
                GuideTarget.DIARY,
                listOf("选择当天日记页", "添加文字、图片或目标块", "保存后自动留在本机"),
            ),
            GuidePage(
                "导出",
                "最后导出成手账图",
                "日记和日程手账都可以预览长图，再保存、分享或调用系统打印。",
                "回到今日",
                "聚焦：长图预览",
                "lottie/img_8.png",
                "lottie/star_yellow.png",
                GoaldayDesign.BorderColor,
                GuideTarget.HOME,
                listOf("回到今日总览", "检查日程和日记", "预览长图后保存或分享"),
            ),
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
    val lottieMeta = remember { loadGuideLottieMeta(context) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        GoaldayDesign.adaptiveSurface,
                        GoaldayDesign.PaperWarm,
                        page.tone.copy(alpha = 0.22f),
                    ),
                ),
            )
            .padding(horizontal = 18.dp, vertical = GoaldayDesign.Space4),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space3),
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Goalday", style = MaterialTheme.typography.titleLarge, color = GoaldayDesign.adaptiveInkPrimary, fontWeight = FontWeight.SemiBold)
                    Text(
                        if (hasLocalGuideAssets) "本地手账指南" else "使用指南",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (hasLocalGuideAssets) GoaldayDesign.Pink else GoaldayDesign.adaptiveInkMuted,
                    )
                }
                Text(
                    "跳过",
                    style = MaterialTheme.typography.labelSmall,
                    color = GoaldayDesign.adaptiveInkMuted,
                    modifier = Modifier
                        .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
                        .clickable(onClick = onClose)
                        .padding(horizontal = GoaldayDesign.Space2, vertical = GoaldayDesign.Space1),
                )
            }
            GuideIllustration(page = page, index = index, meta = lottieMeta)
            GuideStepTimeline(pages = pages, selectedIndex = index)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(GoaldayDesign.Radius2XL))
                    .background(GoaldayDesign.adaptiveSurface.copy(alpha = 0.78f))
                    .border(1.dp, GoaldayDesign.adaptiveDivider.copy(alpha = 0.72f), RoundedCornerShape(GoaldayDesign.Radius2XL))
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(13.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(page.label, style = MaterialTheme.typography.labelMedium, color = page.tone, fontWeight = FontWeight.SemiBold)
                    Text(page.title, style = MaterialTheme.typography.headlineSmall, color = GoaldayDesign.adaptiveInkPrimary, fontWeight = FontWeight.SemiBold)
                    Text(page.body, style = MaterialTheme.typography.bodyMedium, color = GoaldayDesign.adaptiveInkSecondary)
                }
                GuideActionPreview(page)
                GuideTaskRail(page = page)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (index == 0) "从头开始" else "上一步",
                        color = if (index == 0) GoaldayDesign.adaptiveInkMuted else GoaldayDesign.adaptiveInkSecondary,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier
                            .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
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
                            .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
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
                Text(
                    page.action,
                    color = page.tone,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(GoaldayDesign.RadiusL))
                        .background(page.tone.copy(alpha = 0.12f))
                        .border(0.7.dp, page.tone.copy(alpha = 0.22f), RoundedCornerShape(GoaldayDesign.RadiusL))
                        .clickable {
                            onClose()
                            onOpenTarget(page.target)
                        }
                        .padding(horizontal = GoaldayDesign.Space3, vertical = 10.dp),
                )
            }
        }
    }
}

private data class GuideLottieMeta(
    val name: String,
    val frameRate: Int,
    val startFrame: Int,
    val endFrame: Int,
    val width: Int,
    val height: Int,
    val companionName: String,
) {
    val frameCount: Int get() = (endFrame - startFrame).coerceAtLeast(0)
    val label: String get() = "$frameCount 帧 · ${frameRate}fps · ${width}x$height · $companionName"
}

@Composable
private fun GuideStepTimeline(
    pages: List<GuidePage>,
    selectedIndex: Int,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(GoaldayDesign.RadiusXL))
            .background(GoaldayDesign.adaptiveSurface.copy(alpha = 0.62f))
            .border(0.8.dp, GoaldayDesign.adaptiveDivider.copy(alpha = 0.72f), RoundedCornerShape(GoaldayDesign.RadiusXL))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "步骤 ${selectedIndex + 1}/${pages.size}",
                color = GoaldayDesign.adaptiveInkPrimary,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                pages[selectedIndex].focus,
                color = pages[selectedIndex].tone,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
                .background(GoaldayDesign.adaptiveSurfaceSoft),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(((selectedIndex + 1).toFloat() / pages.size).coerceIn(0.1f, 1f))
                    .height(6.dp)
                    .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
                    .background(pages[selectedIndex].tone),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            pages.forEachIndexed { stepIndex, step ->
                val selected = stepIndex == selectedIndex
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(GoaldayDesign.RadiusM))
                        .background(if (selected) step.tone.copy(alpha = 0.14f) else GoaldayDesign.adaptiveSurface.copy(alpha = 0.52f))
                        .border(
                            0.7.dp,
                            if (selected) step.tone.copy(alpha = 0.28f) else GoaldayDesign.BorderColor.copy(alpha = 0.09f),
                            RoundedCornerShape(GoaldayDesign.RadiusM),
                        )
                        .padding(horizontal = 7.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(
                        "${stepIndex + 1}",
                        color = if (selected) Color.White else GoaldayDesign.adaptiveInkMuted,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
                            .background(if (selected) step.tone else GoaldayDesign.BorderColor.copy(alpha = 0.07f))
                            .padding(horizontal = 7.dp, vertical = 2.dp),
                    )
                    Text(
                        step.label,
                        color = if (selected) GoaldayDesign.adaptiveInkPrimary else GoaldayDesign.adaptiveInkMuted,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    )
                }
            }
        }
    }
}

@Composable
private fun GuideActionPreview(page: GuidePage) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(GoaldayDesign.RadiusL))
            .background(GoaldayDesign.adaptiveSurface.copy(alpha = 0.80f))
            .border(0.7.dp, page.tone.copy(alpha = 0.18f), RoundedCornerShape(GoaldayDesign.RadiusL))
            .padding(horizontal = 11.dp, vertical = 9.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        GuidePreviewRow("入口", page.action, page.tone)
        GuidePreviewRow("方式", "不需要服务器，数据保存在本机", page.tone)
        GuidePreviewRow("目标", routeCopyFor(page.target), page.tone)
    }
}

@Composable
private fun GuideTaskRail(page: GuidePage) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(GoaldayDesign.RadiusXL))
            .background(GoaldayDesign.CardPaperGradient)
            .border(0.8.dp, page.tone.copy(alpha = 0.18f), RoundedCornerShape(GoaldayDesign.RadiusXL))
            .padding(GoaldayDesign.Space3),
        verticalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "本地操作流",
                color = GoaldayDesign.adaptiveInkPrimary,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                page.focus.removePrefix("聚焦："),
                color = page.tone,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
                    .background(page.tone.copy(alpha = 0.12f))
                    .padding(horizontal = GoaldayDesign.Space2, vertical = 3.dp),
            )
        }
        page.tasks.forEachIndexed { taskIndex, task ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(9.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
                        .background(if (taskIndex == 0) page.tone else page.tone.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "${taskIndex + 1}",
                        color = if (taskIndex == 0) Color.White else page.tone,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Text(
                    task,
                    color = GoaldayDesign.adaptiveInkSecondary,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun GuidePreviewRow(
    label: String,
    value: String,
    tone: Color,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space2),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            color = tone,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
                .background(tone.copy(alpha = 0.12f))
                .padding(horizontal = GoaldayDesign.Space2, vertical = 3.dp),
        )
        Text(value, color = GoaldayDesign.adaptiveInkSecondary, style = MaterialTheme.typography.labelSmall)
    }
}

private fun routeCopyFor(target: GuideTarget): String =
    when (target) {
        GuideTarget.INSPIRATION -> "选主题、导入任务、保存成手账"
        GuideTarget.HANDBOOK -> "翻页查看日程，把任务排进 待办/已完成"
        GuideTarget.DIARY -> "用文字、图片和目标块写当天记录"
        GuideTarget.HOME -> "回到今日，预览长图后保存或分享"
    }

@Composable
private fun GuideIllustration(
    page: GuidePage,
    index: Int,
    meta: GuideLottieMeta?,
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
    val frameProgress by motion.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(1600), repeatMode = RepeatMode.Restart),
        label = "guide-frame-progress",
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(236.dp)
            .clip(RoundedCornerShape(GoaldayDesign.Radius3XL))
            .background(
                Brush.radialGradient(
                    listOf(
                        GoaldayDesign.adaptiveSurface.copy(alpha = 0.95f),
                        page.tone.copy(alpha = 0.24f),
                        GoaldayDesign.adaptiveSurfaceSoft,
                    ),
                ),
            )
            .border(1.dp, GoaldayDesign.adaptiveDivider.copy(alpha = 0.72f), RoundedCornerShape(GoaldayDesign.Radius3XL))
            .padding(18.dp),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(178.dp)
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
                        .clip(RoundedCornerShape(GoaldayDesign.Radius2XL))
                        .background(GoaldayDesign.adaptiveSurface.copy(alpha = 0.72f))
                        .border(1.dp, GoaldayDesign.BorderColor.copy(alpha = 0.13f), RoundedCornerShape(GoaldayDesign.Radius2XL)),
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
                        .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
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
                .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
                .background(GoaldayDesign.adaptiveSurface.copy(alpha = 0.52f))
                .border(1.dp, page.tone.copy(alpha = 0.22f), RoundedCornerShape(GoaldayDesign.RadiusPill)),
        )
        repeat(3) { row ->
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = (-16).dp, y = (row * -20 - 24).dp)
                    .width((68 + row * 12).dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
                    .background(if (row <= index) page.tone.copy(alpha = 0.42f) else GoaldayDesign.adaptiveSurface.copy(alpha = 0.62f)),
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = (-24 + glide * 24).dp, y = (-104 + index * 5).dp)
                .width(92.dp)
                .height(22.dp)
                .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
                .background(GoaldayDesign.adaptiveSurface.copy(alpha = 0.64f))
                .border(1.dp, page.tone.copy(alpha = 0.28f), RoundedCornerShape(GoaldayDesign.RadiusPill)),
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 76.dp, end = GoaldayDesign.Space6)
                .size(58.dp)
                .graphicsLayer {
                    scaleX = pulse
                    scaleY = pulse
                    alpha = 0.24f + (pulse - 0.88f) * 0.9f
                }
                .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
                .background(page.tone.copy(alpha = 0.28f)),
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 82.dp, end = 30.dp)
                .size(46.dp)
                .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
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
                .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
                .background(page.tone.copy(alpha = 0.78f))
                .padding(horizontal = 10.dp, vertical = 5.dp),
        )
        GuideLottieStatusPill(
            meta = meta,
            progress = frameProgress,
            tone = page.tone,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 38.dp),
        )
        Text(
            page.action,
            color = GoaldayDesign.adaptiveInkPrimary,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
                .background(GoaldayDesign.adaptiveSurface.copy(alpha = 0.70f))
                .padding(horizontal = GoaldayDesign.Space3, vertical = 7.dp),
        )
    }
}

@Composable
private fun GuideLottieStatusPill(
    meta: GuideLottieMeta?,
    progress: Float,
    tone: Color,
    modifier: Modifier = Modifier,
) {
    val currentFrame = meta?.let { (it.startFrame + it.frameCount * progress).toInt().coerceIn(it.startFrame, it.endFrame) }
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(GoaldayDesign.RadiusL))
            .background(GoaldayDesign.adaptiveSurface.copy(alpha = 0.72f))
            .border(0.8.dp, tone.copy(alpha = 0.24f), RoundedCornerShape(GoaldayDesign.RadiusL))
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Text(
            meta?.name ?: "手账动画资源",
            color = tone,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .width(82.dp)
                    .height(5.dp)
                    .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
                    .background(tone.copy(alpha = 0.16f)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress.coerceIn(0.05f, 1f))
                        .height(5.dp)
                        .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
                        .background(tone),
                )
            }
            Text(
                currentFrame?.let { "第 $it 帧" } ?: "本地",
                color = GoaldayDesign.adaptiveInkMuted,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
            )
        }
        Text(
            meta?.label ?: "动画资源已就绪",
            color = GoaldayDesign.adaptiveInkMuted,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
        )
    }
}

private fun loadGuideAssetBitmap(context: Context, path: String): Bitmap? =
    runCatching {
        context.assets.open(path).use(BitmapFactory::decodeStream)
    }.getOrNull()

private fun loadGuideLottieMeta(context: Context): GuideLottieMeta? =
    runCatching {
        val goalday = context.assets.open("lottie/goalday.json").bufferedReader().use { JSONObject(it.readText()) }
        val coupon = context.assets.open("lottie/coupon.json").bufferedReader().use { JSONObject(it.readText()) }
        GuideLottieMeta(
            name = goalday.optString("nm", "goalday-guide"),
            frameRate = goalday.optInt("fr", 0),
            startFrame = goalday.optInt("ip", 0),
            endFrame = goalday.optInt("op", 0),
            width = goalday.optInt("w", 0),
            height = goalday.optInt("h", 0),
            companionName = coupon.optString("nm", "coupon"),
        )
    }.getOrNull()
