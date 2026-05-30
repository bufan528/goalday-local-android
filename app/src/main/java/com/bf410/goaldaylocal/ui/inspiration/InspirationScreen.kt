package com.bf410.goaldaylocal.ui.inspiration

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bf410.goaldaylocal.ui.book.BookViewModel
import com.bf410.goaldaylocal.ui.book.InspirationTemplates
import com.bf410.goaldaylocal.ui.replica.ExecutionBoardHeader
import com.bf410.goaldaylocal.ui.replica.GoaldaySegmentBar
import com.bf410.goaldaylocal.ui.replica.GoaldayTopBar

private enum class InspirationMode {
    TEMPLATE,
    PREVIEW,
    APPLY,
}

@Composable
fun InspirationScreen(
    viewModel: BookViewModel,
) {
    var selectedIndex by remember { mutableIntStateOf(0) }
    var mode by remember { mutableIntStateOf(0) }
    val selected = InspirationTemplates.all[selectedIndex.coerceIn(0, InspirationTemplates.all.lastIndex)]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 8.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        GoaldayTopBar(
            leftTitle = "灵感",
            rightPrimaryText = "应用",
            onRightPrimaryClick = { viewModel.applyInspirationToToday(selected.items) },
        )
        GoaldaySegmentBar(
            items = listOf("模板", "预览", "应用"),
            selectedIndex = mode.coerceIn(0, 2),
            onSelect = { mode = it },
        )
        ExecutionBoardHeader(title = "灵感执行板", subtitle = "选模板 -> 预览 -> 应用到今日")

        if (mode == InspirationMode.TEMPLATE.ordinal) {
            InspirationTemplates.all.forEachIndexed { index, template ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(108.dp)
                        .background(
                            Brush.horizontalGradient(
                                when (index % 4) {
                                    0 -> listOf(Color(0xFFCEC8BE), Color(0xFFA9A49D))
                                    1 -> listOf(Color(0xFF8B7165), Color(0xFFB08E7D))
                                    2 -> listOf(Color(0xFF5D4C3A), Color(0xFF8A745F))
                                    else -> listOf(Color(0xFF8A869F), Color(0xFFA6A2BA))
                                },
                            ),
                            RoundedCornerShape(14.dp),
                        )
                        .border(if (index == selectedIndex) 2.dp else 1.dp, if (index == selectedIndex) Color.White else Color(0x24FFFFFF), RoundedCornerShape(14.dp))
                        .clickable { selectedIndex = index }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(template.title, fontWeight = FontWeight.SemiBold, color = Color.White)
                            Text(template.subtitle, style = MaterialTheme.typography.bodySmall, color = Color(0xEFFFFFFF))
                        }
                        Text(if (index == selectedIndex) "已选" else "选择", color = Color.White, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFFBFAF8), RoundedCornerShape(14.dp))
                .border(1.dp, Color(0x14000000), RoundedCornerShape(14.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("预览：${selected.title}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = Color(0xFF2E2A26))
            val previewItems = if (mode == InspirationMode.PREVIEW.ordinal) selected.items else selected.items.take(8)
            previewItems.forEach { item ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
                    Text("·", color = Color(0xFF9E978D))
                    Text(item, modifier = Modifier.weight(1f), color = Color(0xFF3A332C), style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "＋",
                        color = Color(0xFF6F675D),
                        modifier = Modifier
                            .width(18.dp)
                            .clickable { viewModel.applyInspirationToToday(listOf(item)) },
                    )
                }
            }
            if (mode == InspirationMode.APPLY.ordinal) {
                Text(
                    "一键应用全部",
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier
                        .background(Color(0xFF222222), RoundedCornerShape(99.dp))
                        .clickable { viewModel.applyInspirationToToday(selected.items) }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                )
            }
        }
    }
}
