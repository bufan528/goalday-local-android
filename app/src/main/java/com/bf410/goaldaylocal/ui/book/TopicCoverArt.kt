package com.bf410.goaldaylocal.ui.book

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bf410.goaldaylocal.ui.replica.GoaldayDesign

internal data class TopicCoverPalette(
    val base: Color,
    val mid: Color,
    val deep: Color,
    val ink: Color,
    val symbol: String,
)

internal fun topicCoverPalette(template: InspirationTemplate, index: Int): TopicCoverPalette {
    val categoryPalette = when (template.category) {
        "年度" -> TopicCoverPalette(GoaldayDesign.TopicPeach, GoaldayDesign.TopicPeachMid, GoaldayDesign.TopicPeachDeep, GoaldayDesign.TopicPeachInk, "26")
        "月份" -> TopicCoverPalette(template.color, GoaldayDesign.TopicMonthMid, GoaldayDesign.TopicMonthDeep, GoaldayDesign.TopicMonthInk, template.coverKey.take(3).uppercase())
        "体验" -> TopicCoverPalette(GoaldayDesign.TopicAmber, GoaldayDesign.TopicAmberMid, GoaldayDesign.TopicAmberDeep, GoaldayDesign.TopicAmberInk, "体验")
        "周计划" -> TopicCoverPalette(GoaldayDesign.TopicSage, GoaldayDesign.TopicSageMid, GoaldayDesign.TopicSageDeep, GoaldayDesign.TopicSageInk, "周计划")
        "日记" -> TopicCoverPalette(GoaldayDesign.TopicRose, GoaldayDesign.TopicRoseMid, GoaldayDesign.TopicRoseDeep, GoaldayDesign.TopicRoseInk, "日记")
        "疗愈" -> TopicCoverPalette(GoaldayDesign.TopicPeriwinkle, GoaldayDesign.TopicPeriwinkleMid, GoaldayDesign.TopicPeriwinkleDeep, GoaldayDesign.TopicPeriwinkleInk, "疗愈")
        "复盘" -> TopicCoverPalette(GoaldayDesign.TopicRose, GoaldayDesign.TopicReviewMid, GoaldayDesign.TopicReviewDeep, GoaldayDesign.TopicReviewInk, "复盘")
        "整理" -> TopicCoverPalette(GoaldayDesign.TopicDaffodil, GoaldayDesign.TopicDaffodilMid, GoaldayDesign.TopicDaffodilDeep, GoaldayDesign.TopicDaffodilInk, "整理")
        "阅读" -> TopicCoverPalette(GoaldayDesign.TopicPine, GoaldayDesign.TopicPineMid, GoaldayDesign.TopicPineDeep, GoaldayDesign.TopicPineInk, "阅读")
        "旅行" -> TopicCoverPalette(GoaldayDesign.TopicDaffodil, GoaldayDesign.TopicTravelMid, GoaldayDesign.TopicTravelDeep, GoaldayDesign.TopicTravelInk, "旅行")
        "观影" -> TopicCoverPalette(GoaldayDesign.TopicSpruce, GoaldayDesign.TopicSpruceMid, GoaldayDesign.TopicSpruceDeep, GoaldayDesign.TopicSpruceInk, "观影")
        "关系" -> TopicCoverPalette(GoaldayDesign.TopicRose, GoaldayDesign.TopicBondMid, GoaldayDesign.TopicBondDeep, GoaldayDesign.TopicBondInk, "关系")
        "手作" -> TopicCoverPalette(GoaldayDesign.TopicRose, GoaldayDesign.TopicCraftMid, GoaldayDesign.TopicCraftDeep, GoaldayDesign.TopicCraftInk, "手作")
        else -> TopicCoverPalette(template.color, template.color.copy(alpha = 0.82f), fallbackDeep(index), Color.White, template.coverKey.take(2).uppercase())
    }
    return categoryPalette.copy(base = blendWithTemplate(categoryPalette.base, template.color))
}

internal fun topicCoverBrush(template: InspirationTemplate, index: Int): Brush {
    val palette = topicCoverPalette(template, index)
    return Brush.linearGradient(
        listOf(
            palette.base.copy(alpha = 0.99f),
            palette.mid.copy(alpha = 0.92f),
            palette.deep,
        ),
        start = Offset.Zero,
        end = Offset(760f, 520f),
    )
}

@Composable
internal fun TopicCoverArt(
    template: InspirationTemplate,
    index: Int,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val palette = topicCoverPalette(template, index)
    val context = LocalContext.current
    val assetBitmap = remember(template.coverKey) {
        runCatching {
            context.assets.open("cover/${template.coverKey}.png").use(BitmapFactory::decodeStream)
        }.getOrNull()
    }
    Box(modifier.fillMaxSize()) {
        if (assetBitmap != null) {
            Image(
                bitmap = assetBitmap.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, GoaldayDesign.BlackOverlayMedium),
                        ),
                    ),
            )
            return@Box
        }
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .width(if (compact) 22.dp else 30.dp)
                .fillMaxHeight()
                .background(
                    Brush.horizontalGradient(
                        listOf(GoaldayDesign.CoverDarkSpineStrong, GoaldayDesign.CoverWhiteOverlaySubtle, Color.Transparent),
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = if (compact) 30.dp else 40.dp, top = if (compact) 12.dp else 16.dp)
                .width(if (compact) 42.dp else 62.dp)
                .height(if (compact) 5.dp else 7.dp)
                .clip(RoundedCornerShape(99.dp))
                .background(palette.ink.copy(alpha = 0.62f)),
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = if (compact) 10.dp else 14.dp, end = if (compact) 10.dp else 16.dp)
                .size(if (compact) 36.dp else 52.dp)
                .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
                .background(GoaldayDesign.CoverWhiteOverlaySubtle)
                .border(1.dp, GoaldayDesign.CoverWhiteOverlayLow, RoundedCornerShape(GoaldayDesign.RadiusPill)),
        ) {
            Text(
                palette.symbol.take(4),
                color = palette.ink.copy(alpha = 0.92f),
                style = if (compact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.Center),
            )
        }
        repeat(if (compact) 3 else 5) { layer ->
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = (14 + layer * 9).dp, end = (12 + layer * 16).dp)
                    .width((42 + layer * 9).dp)
                    .height(1.dp)
                    .background(palette.ink.copy(alpha = 0.26f)),
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = if (compact) 36.dp else 48.dp, bottom = if (compact) 13.dp else 17.dp)
                .width(if (compact) 46.dp else 62.dp)
                .height(if (compact) 46.dp else 62.dp)
                .clip(RoundedCornerShape(GoaldayDesign.RadiusS))
                .background(GoaldayDesign.CoverWhiteOverlayHairline)
                .border(1.dp, GoaldayDesign.CoverWhiteOverlaySubtle, RoundedCornerShape(GoaldayDesign.RadiusS)),
        )
        Text(
            template.coverKey.uppercase().take(if (compact) 10 else 14),
            color = palette.ink.copy(alpha = 0.34f),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .graphicsLayer { rotationZ = -90f }
                .padding(bottom = 2.dp),
        )
    }
}

private fun fallbackDeep(index: Int): Color =
    when (index % 6) {
        0 -> GoaldayDesign.TopicDeepOlive
        1 -> GoaldayDesign.TopicDeepPlum
        2 -> GoaldayDesign.TopicDeepBrown
        3 -> GoaldayDesign.TopicDeepSteel
        4 -> GoaldayDesign.TopicDeepMoss
        else -> GoaldayDesign.TopicDeepPurple
    }

private fun blendWithTemplate(base: Color, templateColor: Color): Color =
    Color(
        red = (base.red * 0.72f + templateColor.red * 0.28f).coerceIn(0f, 1f),
        green = (base.green * 0.72f + templateColor.green * 0.28f).coerceIn(0f, 1f),
        blue = (base.blue * 0.72f + templateColor.blue * 0.28f).coerceIn(0f, 1f),
        alpha = 1f,
    )
