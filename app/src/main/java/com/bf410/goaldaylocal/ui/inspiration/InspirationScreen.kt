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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bf410.goaldaylocal.ui.book.BookViewModel
import com.bf410.goaldaylocal.ui.book.InspirationTemplates
import com.bf410.goaldaylocal.ui.replica.GoaldayTopBar

data class InspirationDraftItem(
    var text: String,
    var selected: Boolean = true,
)

private enum class InspirationMode {
    CENTER,
    SAVE,
    FLIP,
}

@Composable
fun InspirationScreen(
    viewModel: BookViewModel,
    onOpenHandbook: () -> Unit,
) {
    var selectedTemplateIndex by rememberSaveable { mutableIntStateOf(0) }
    var mode by rememberSaveable { mutableStateOf(InspirationMode.CENTER) }
    var focusedIndex by rememberSaveable { mutableIntStateOf(0) }
    var inputText by rememberSaveable { mutableStateOf("") }

    val selectedTemplate = InspirationTemplates.all[selectedTemplateIndex.coerceIn(0, InspirationTemplates.all.lastIndex)]
    val draftItems = remember(selectedTemplate.title) {
        mutableStateListOf<InspirationDraftItem>().apply {
            addAll(selectedTemplate.items.map { InspirationDraftItem(it, selected = true) })
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        GoaldayTopBar(
            leftTitle = "灵感中心",
            rightPrimaryText = "直接保存",
            onRightPrimaryClick = {
                viewModel.applyInspirationToToday(draftItems.filter { it.selected }.map { it.text })
                mode = InspirationMode.SAVE
            },
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF0E7DE))
                .padding(horizontal = 10.dp, vertical = 8.dp),
        ) {
            listOf("灵感中心", "直接保存", "翻页").forEachIndexed { idx, label ->
                val target = InspirationMode.entries[idx]
                Text(
                    label,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { mode = target },
                    color = if (mode == target) Color(0xFFE88FAE) else Color(0xFF9D948A),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = if (mode == target) FontWeight.SemiBold else FontWeight.Normal,
                )
            }
        }

        if (mode == InspirationMode.CENTER) {
            InspirationTemplates.all.forEachIndexed { index, template ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(92.dp)
                        .background(
                            Brush.horizontalGradient(
                                when (index % 5) {
                                    0 -> listOf(Color(0xFF8A9B68), Color(0xFF798A58))
                                    1 -> listOf(Color(0xFF8D756D), Color(0xFF6C5C56))
                                    2 -> listOf(Color(0xFF5A6650), Color(0xFF495640))
                                    3 -> listOf(Color(0xFF967C66), Color(0xFF7D6652))
                                    else -> listOf(Color(0xFF6F7D61), Color(0xFF5D6952))
                                },
                            ),
                            RoundedCornerShape(10.dp),
                        )
                        .border(if (index == selectedTemplateIndex) 2.dp else 0.dp, Color(0x66FFFFFF), RoundedCornerShape(10.dp))
                        .clickable {
                            selectedTemplateIndex = index
                            draftItems.clear()
                            draftItems.addAll(template.items.map { InspirationDraftItem(it, selected = true) })
                            focusedIndex = 0
                        }
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(template.title, color = Color.White, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text(template.subtitle, color = Color(0xE8FFFFFF), style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFFFFEFC), RoundedCornerShape(10.dp))
                .border(1.dp, Color(0x12000000), RoundedCornerShape(10.dp))
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = when (mode) {
                    InspirationMode.CENTER -> selectedTemplate.title
                    InspirationMode.SAVE -> "直接保存"
                    InspirationMode.FLIP -> "翻页"
                },
                style = MaterialTheme.typography.titleSmall,
                color = Color(0xFF28241F),
                fontWeight = FontWeight.SemiBold,
            )

            draftItems.forEachIndexed { index, item ->
                Column(modifier = Modifier.fillMaxWidth().clickable { focusedIndex = index }) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(if (item.selected) "✓" else "□", color = if (item.selected) Color(0xFF6F8E68) else Color(0xFFB4ADA4), modifier = Modifier.clickable {
                            draftItems[index] = draftItems[index].copy(selected = !draftItems[index].selected)
                        })
                        Text(item.text, modifier = Modifier.weight(1f), color = Color(0xFF2F2A24), style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(modifier = Modifier.fillMaxWidth().padding(top = 3.dp).height(1.dp).background(Color(0x0E000000)))
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF6F6FA), RoundedCornerShape(8.dp))
                    .padding(horizontal = 9.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("灵感草稿", style = MaterialTheme.typography.bodySmall, color = Color(0xFF575757))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("🗑", modifier = Modifier.clickable {
                        if (draftItems.isNotEmpty()) {
                            draftItems.removeAt(focusedIndex.coerceIn(0, draftItems.lastIndex))
                            focusedIndex = (focusedIndex - 1).coerceAtLeast(0)
                        }
                    })
                    Text("＋", modifier = Modifier.clickable {
                        val text = inputText.trim().ifBlank { "新灵感" }
                        draftItems.add(focusedIndex.coerceIn(0, draftItems.size), InspirationDraftItem(text, selected = true))
                        inputText = ""
                    })
                    Text("✓", modifier = Modifier.clickable {
                        if (draftItems.isNotEmpty()) {
                            val i = focusedIndex.coerceIn(0, draftItems.lastIndex)
                            draftItems[i] = draftItems[i].copy(selected = !draftItems[i].selected)
                        }
                    })
                }
            }

            BasicTextField(
                value = inputText,
                onValueChange = { inputText = it },
                textStyle = TextStyle(color = Color(0xFF2C2925)),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0x08000000), RoundedCornerShape(6.dp))
                    .padding(horizontal = 9.dp, vertical = 7.dp),
                decorationBox = { inner ->
                    if (inputText.isBlank()) Text("输入灵感内容，点 + 插入", color = Color(0xFF9A9188), style = MaterialTheme.typography.bodySmall)
                    inner()
                },
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "保存到本周",
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier
                        .background(Color(0xFF212121), RoundedCornerShape(99.dp))
                        .clickable {
                            viewModel.applyInspirationToToday(draftItems.filter { it.selected }.map { it.text })
                            mode = InspirationMode.SAVE
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                )
                Text(
                    "翻页查看",
                    color = Color(0xFFE88FAE),
                    modifier = Modifier
                        .clickable {
                            viewModel.applyInspirationToToday(draftItems.filter { it.selected }.map { it.text })
                            mode = InspirationMode.FLIP
                            onOpenHandbook()
                        }
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                )
            }
        }

        if (mode == InspirationMode.FLIP) {
            Text("翻页", style = MaterialTheme.typography.titleLarge, color = Color(0xFF1F1D1A), fontWeight = FontWeight.SemiBold)
            Text("已保存内容可在手账中翻页查看", color = Color(0xFF7E756B), style = MaterialTheme.typography.bodySmall)
        }
    }
}
