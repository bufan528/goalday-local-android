package com.bf410.goaldaylocal.ui

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bf410.goaldaylocal.ui.replica.GoaldayDesign

internal const val KEY_GUIDE_SEEN = "guide_seen_v1"

private data class GuidePage(
    val label: String,
    val title: String,
    val body: String,
    val action: String,
)

@Composable
internal fun GuideOverlay(
    onClose: () -> Unit,
) {
    val pages = remember {
        listOf(
            GuidePage("目标", "先把目标放进手账", "从灵感中心选择主题，勾选目标后可以导入任务池，也可以保存成一本手账。", "选主题 · 勾目标"),
            GuidePage("日程", "把任务排进今天", "在日程页把任务拖入日期，或者点目标卡片的排入按钮；桌面小组件会跟着刷新。", "拖入日期 · 标记完成"),
            GuidePage("日记", "每天写成块", "日记支持文字块、目标块、专题目标块和图片；完成的目标也能直接关联到日记。", "写文字 · 贴目标"),
            GuidePage("导出", "最后导出成手账图", "日记和日程手账都可以预览长图，再保存、分享或调用系统打印。", "预览 · 分享 · 打印"),
        )
    }
    var index by remember { mutableIntStateOf(0) }
    val page = pages[index.coerceIn(0, pages.lastIndex)]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xAA2F2922))
            .padding(18.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFFFFFCF7))
                .border(1.dp, Color(0x28E88FAE), RoundedCornerShape(24.dp))
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Goalday 新手引导", style = MaterialTheme.typography.titleMedium, color = GoaldayDesign.InkPrimary, fontWeight = FontWeight.SemiBold)
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
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(page.label, style = MaterialTheme.typography.labelMedium, color = GoaldayDesign.Pink, fontWeight = FontWeight.SemiBold)
                Text(page.title, style = MaterialTheme.typography.headlineSmall, color = GoaldayDesign.InkPrimary, fontWeight = FontWeight.SemiBold)
                Text(page.body, style = MaterialTheme.typography.bodyMedium, color = GoaldayDesign.InkSecondary)
            }
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
                        .background(GoaldayDesign.PrimaryAction)
                        .clickable {
                            if (index == pages.lastIndex) {
                                onClose()
                            } else {
                                index += 1
                            }
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun GuideIllustration(
    page: GuidePage,
    index: Int,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(178.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFFFFECF3),
                        Color(0xFFFFF7EC),
                        Color(0xFFE9F1E5),
                    ),
                ),
            )
            .border(1.dp, Color.White.copy(alpha = 0.55f), RoundedCornerShape(18.dp))
            .padding(16.dp),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .width(72.dp)
                .height(126.dp)
                .clip(RoundedCornerShape(12.dp, 4.dp, 12.dp, 4.dp))
                .background(Color.White.copy(alpha = 0.74f))
                .border(1.dp, Color(0x22B7A893), RoundedCornerShape(12.dp, 4.dp, 12.dp, 4.dp)),
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .width(122.dp)
                .height(126.dp)
                .clip(RoundedCornerShape(4.dp, 12.dp, 4.dp, 12.dp))
                .background(Color.White.copy(alpha = 0.80f))
                .border(1.dp, Color(0x22B7A893), RoundedCornerShape(4.dp, 12.dp, 4.dp, 12.dp)),
        )
        repeat(4) { row ->
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 20.dp, top = (row * 20 - 38).dp)
                    .width((70 + row * 7).dp)
                    .height(5.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .background(if (row <= index) GoaldayDesign.Pink.copy(alpha = 0.48f) else Color(0xFFD8CFC5)),
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 16.dp, end = 20.dp)
                .size(44.dp)
                .clip(RoundedCornerShape(99.dp))
                .background(GoaldayDesign.PrimaryAction.copy(alpha = 0.88f)),
        ) {
            Text("${index + 1}", color = Color.White, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.align(Alignment.Center))
        }
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
