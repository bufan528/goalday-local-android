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

internal data class TopicCoverPalette(
    val base: Color,
    val mid: Color,
    val deep: Color,
    val ink: Color,
    val symbol: String,
)

internal fun topicCoverPalette(template: InspirationTemplate, index: Int): TopicCoverPalette {
    val categoryPalette = when (template.category) {
        "年度" -> TopicCoverPalette(Color(0xFFF2C0A5), Color(0xFFD88F74), Color(0xFF6E4638), Color(0xFFFFF4E8), "26")
        "月份" -> TopicCoverPalette(template.color, Color(0xFFE7B28E), Color(0xFF785740), Color(0xFFFFF7EC), template.coverKey.take(3).uppercase())
        "体验" -> TopicCoverPalette(Color(0xFFFFAA5F), Color(0xFFD4814E), Color(0xFF5F4939), Color(0xFFFFF7EA), "GO")
        "周计划" -> TopicCoverPalette(Color(0xFFC9D6C1), Color(0xFF8FA77D), Color(0xFF3E594A), Color(0xFFF7FFF1), "WK")
        "日记" -> TopicCoverPalette(Color(0xFFF1A5B6), Color(0xFFC97991), Color(0xFF704559), Color(0xFFFFEEF4), "DI")
        "疗愈" -> TopicCoverPalette(Color(0xFF9EAADB), Color(0xFF7C8BC8), Color(0xFF3F496B), Color(0xFFF4F6FF), "CALM")
        "复盘" -> TopicCoverPalette(Color(0xFFF1A5B6), Color(0xFFC98C9B), Color(0xFF5D4A58), Color(0xFFFFF2F5), "RE")
        "整理" -> TopicCoverPalette(Color(0xFFF8D58A), Color(0xFFD0A45A), Color(0xFF665335), Color(0xFFFFF8E6), "CL")
        "阅读" -> TopicCoverPalette(Color(0xFF6D8B7E), Color(0xFF4E6B60), Color(0xFF263D36), Color(0xFFF1FFF8), "BOOK")
        "旅行" -> TopicCoverPalette(Color(0xFFF8D58A), Color(0xFFDFB460), Color(0xFF6A5637), Color(0xFFFFF8E1), "TRIP")
        "观影" -> TopicCoverPalette(Color(0xFF51675F), Color(0xFF334F46), Color(0xFF1E2F2A), Color(0xFFEFFFF8), "FILM")
        "关系" -> TopicCoverPalette(Color(0xFFF1A5B6), Color(0xFFD7859E), Color(0xFF6D4053), Color(0xFFFFEFF4), "2")
        "手作" -> TopicCoverPalette(Color(0xFFF1A5B6), Color(0xFFE1A15F), Color(0xFF6B4E3A), Color(0xFFFFF2EB), "DIY")
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
                            listOf(Color.Transparent, Color(0x33000000)),
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
                        listOf(Color(0x4A2F261D), Color.White.copy(alpha = 0.18f), Color.Transparent),
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
                .clip(RoundedCornerShape(99.dp))
                .background(Color.White.copy(alpha = 0.18f))
                .border(1.dp, Color.White.copy(alpha = 0.28f), RoundedCornerShape(99.dp)),
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
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White.copy(alpha = 0.12f))
                .border(1.dp, Color.White.copy(alpha = 0.20f), RoundedCornerShape(8.dp)),
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
        0 -> Color(0xFF5F6F3D)
        1 -> Color(0xFF754E5E)
        2 -> Color(0xFF6F523D)
        3 -> Color(0xFF445A72)
        4 -> Color(0xFF566B5B)
        else -> Color(0xFF574B6B)
    }

private fun blendWithTemplate(base: Color, templateColor: Color): Color =
    Color(
        red = (base.red * 0.72f + templateColor.red * 0.28f).coerceIn(0f, 1f),
        green = (base.green * 0.72f + templateColor.green * 0.28f).coerceIn(0f, 1f),
        blue = (base.blue * 0.72f + templateColor.blue * 0.28f).coerceIn(0f, 1f),
        alpha = 1f,
    )
