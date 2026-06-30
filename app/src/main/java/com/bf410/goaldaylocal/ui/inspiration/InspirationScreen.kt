package com.bf410.goaldaylocal.ui.inspiration

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bf410.goaldaylocal.ui.book.InspirationTemplate
import com.bf410.goaldaylocal.ui.book.BookViewModel
import com.bf410.goaldaylocal.ui.book.InspirationTemplates
import com.bf410.goaldaylocal.ui.book.TopicCoverArt
import com.bf410.goaldaylocal.ui.book.loadTargetAssetItems
import com.bf410.goaldaylocal.ui.book.loadTopicCatalogSummary
import com.bf410.goaldaylocal.ui.book.topicCoverBrush
import com.bf410.goaldaylocal.ui.replica.GoaldayDesign
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
    var selectedCategory by rememberSaveable { mutableStateOf("全部") }

    val templates = InspirationTemplates.all
    val categories = remember(templates) { listOf("全部") + templates.map { it.category }.distinct() }
    val visibleTemplates = remember(selectedCategory, templates) {
        if (selectedCategory == "全部") templates else templates.filter { it.category == selectedCategory }
    }
    if (templates.isEmpty()) {
        InspirationUnavailableState()
        return
    }
    val selectedTemplate = templates[selectedTemplateIndex.coerceIn(0, templates.lastIndex)]
    val context = LocalContext.current
    val selectedTemplateItems = remember(selectedTemplate.id, selectedTemplate.targetAssetPath) {
        loadTargetAssetItems(context, selectedTemplate.targetAssetPath).ifEmpty { selectedTemplate.items }
    }
    val catalogSummary = remember(selectedTemplate.catalogPath) {
        loadTopicCatalogSummary(context, selectedTemplate.catalogPath)
    }
    val draftItems = remember(selectedTemplate.id, selectedTemplateItems) {
        mutableStateListOf<InspirationDraftItem>().apply {
            addAll(selectedTemplateItems.map { InspirationDraftItem(it, selected = true) })
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                GoaldayDesign.DeskGradient,
            )
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp)
            .padding(bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        GoaldayTopBar(
            leftTitle = "灵感中心",
            rightPrimaryText = "导入任务池",
            onRightPrimaryClick = {
                viewModel.applyInspirationToToday(draftItems.filter { it.selected }.map { it.text })
                mode = InspirationMode.SAVE
            },
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0x80FFFDF8), RoundedCornerShape(16.dp))
                .border(0.6.dp, GoaldayDesign.BorderColor.copy(alpha = 0.14f), RoundedCornerShape(16.dp))
                .padding(horizontal = 7.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            listOf("灵感中心", "直接保存", "翻页").forEachIndexed { idx, label ->
                val target = InspirationMode.entries[idx]
                Text(
                    label,
                    modifier = Modifier
                        .weight(1f)
                        .background(if (mode == target) Color.White else Color.Transparent, RoundedCornerShape(99.dp))
                        .clickable { mode = target }
                        .padding(vertical = 6.dp),
                    color = if (mode == target) GoaldayDesign.Pink else Color(0xFF8C7F73),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        if (mode == InspirationMode.CENTER) {
            InspirationHeroCover(
                template = selectedTemplate,
                index = selectedTemplateIndex,
                itemCount = selectedTemplateItems.size,
                selectedCount = draftItems.count { it.selected },
            )
            InspirationCatalogStrip(
                template = selectedTemplate,
                itemCount = selectedTemplateItems.size,
                catalogLabel = catalogSummary.label,
                assetLabel = catalogSummary.assetLabel,
            )
            InspirationCategoryRail(
                categories = categories,
                selectedCategory = selectedCategory,
                onSelect = { category ->
                    selectedCategory = category
                    val next = if (category == "全部") templates.firstOrNull() else templates.firstOrNull { it.category == category }
                    next?.let { template ->
                        selectedTemplateIndex = templates.indexOf(template).coerceAtLeast(0)
                        val items = loadTargetAssetItems(context, template.targetAssetPath).ifEmpty { template.items }
                        draftItems.clear()
                        draftItems.addAll(items.map { InspirationDraftItem(it, selected = true) })
                        focusedIndex = 0
                    }
                },
            )
            Text("专题封面", color = GoaldayDesign.InkPrimary, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            InspirationCoverGrid(
                templates = visibleTemplates,
                allTemplates = templates,
                selectedTemplateIndex = selectedTemplateIndex,
                onSelect = { index, previewItems ->
                    selectedTemplateIndex = index
                    draftItems.clear()
                    draftItems.addAll(previewItems.map { InspirationDraftItem(it, selected = true) })
                    focusedIndex = 0
                },
            )
        }

        InspirationDraftPanel(
            mode = mode,
            selectedTemplate = selectedTemplate,
            draftItems = draftItems,
            focusedIndex = focusedIndex,
            inputText = inputText,
            onFocusedIndexChange = { focusedIndex = it },
            onInputTextChange = { inputText = it },
            onRemoveFocused = {
                if (draftItems.isNotEmpty()) {
                    draftItems.removeAt(focusedIndex.coerceIn(0, draftItems.lastIndex))
                    focusedIndex = (focusedIndex - 1).coerceAtLeast(0)
                }
            },
            onInsertDraft = {
                val text = inputText.trim().ifBlank { "新灵感" }
                draftItems.add(focusedIndex.coerceIn(0, draftItems.size), InspirationDraftItem(text, selected = true))
                inputText = ""
            },
            onToggleFocused = {
                if (draftItems.isNotEmpty()) {
                    val i = focusedIndex.coerceIn(0, draftItems.lastIndex)
                    draftItems[i] = draftItems[i].copy(selected = !draftItems[i].selected)
                }
            },
            onImport = {
                viewModel.applyInspirationToToday(draftItems.filter { it.selected }.map { it.text })
                mode = InspirationMode.SAVE
            },
            onImportAndOpen = {
                viewModel.applyInspirationToToday(draftItems.filter { it.selected }.map { it.text })
                mode = InspirationMode.FLIP
                onOpenHandbook()
            },
        )

        if (mode == InspirationMode.FLIP) {
            Text("翻页", style = MaterialTheme.typography.titleLarge, color = GoaldayDesign.InkPrimary, fontWeight = FontWeight.SemiBold)
            Text("已保存内容可在手账中翻页查看", color = Color(0xFF7E756B), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun InspirationUnavailableState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                GoaldayDesign.DeskGradient,
            )
            .padding(18.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xEFFFFDF8), RoundedCornerShape(22.dp))
                .border(0.8.dp, GoaldayDesign.BorderColor.copy(alpha = 0.14f), RoundedCornerShape(22.dp))
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("暂无灵感模板", style = MaterialTheme.typography.titleMedium, color = GoaldayDesign.InkPrimary, fontWeight = FontWeight.SemiBold)
            Text("本地模板资源为空，先进入手账或日历继续使用已有功能。", style = MaterialTheme.typography.bodySmall, color = GoaldayDesign.InkMuted)
        }
    }
}

@Composable
private fun InspirationCatalogStrip(
    template: InspirationTemplate,
    itemCount: Int,
    catalogLabel: String,
    assetLabel: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0x88FFFDF8), RoundedCornerShape(14.dp))
            .border(0.6.dp, GoaldayDesign.BorderColor.copy(alpha = 0.14f), RoundedCornerShape(14.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(catalogLabel, color = GoaldayDesign.InkPrimary, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            Text(assetLabel, color = GoaldayDesign.InkMuted, style = MaterialTheme.typography.labelSmall)
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("${template.coverKey}.png", color = Color(0xFF6F5E51), style = MaterialTheme.typography.labelSmall)
            Text("${template.targetKey}.txt · $itemCount 条", color = GoaldayDesign.Pink, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun InspirationHeroCover(
    template: InspirationTemplate,
    index: Int,
    itemCount: Int,
    selectedCount: Int,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(184.dp)
            .background(topicCoverBrush(template, index), RoundedCornerShape(18.dp))
            .border(1.dp, Color(0x44FFFFFF), RoundedCornerShape(18.dp)),
    ) {
        TopicCoverArt(template = template, index = index)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xAA1F1712), Color(0x26000000), Color(0x991F1712)),
                        start = Offset.Zero,
                        end = Offset(760f, 420f),
                    ),
                    RoundedCornerShape(18.dp),
                ),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(template.category, color = Color.White, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    if (template.linkToSchedule) "可排日程" else "复盘记录",
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                        .background(Color(0x33FFFFFF), RoundedCornerShape(99.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(template.title, color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold, maxLines = 2)
                Text(template.subtitle, color = Color(0xEFFFFFFF), style = MaterialTheme.typography.bodySmall, maxLines = 2)
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalAlignment = Alignment.CenterVertically) {
                    InspirationHeroMetric("目标", itemCount.toString())
                    InspirationHeroMetric("已选", selectedCount.toString())
                }
            }
        }
    }
}

@Composable
private fun InspirationHeroMetric(label: String, value: String) {
    Row(
        modifier = Modifier
            .background(Color(0x30FFFFFF), RoundedCornerShape(99.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = Color(0xDEFFFFFF), style = MaterialTheme.typography.labelSmall)
        Text(value, color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun InspirationCategoryRail(
    categories: List<String>,
    selectedCategory: String,
    onSelect: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        categories.forEach { category ->
            val selected = category == selectedCategory
            Text(
                category,
                color = if (selected) Color.White else Color(0xFF715B4B),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                modifier = Modifier
                    .background(if (selected) GoaldayDesign.Pink else Color(0x88FFFDF8), RoundedCornerShape(99.dp))
                    .border(0.6.dp, if (selected) Color.White.copy(alpha = 0.45f) else GoaldayDesign.BorderColor.copy(alpha = 0.14f), RoundedCornerShape(99.dp))
                    .clickable { onSelect(category) }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            )
        }
    }
}

@Composable
private fun InspirationCoverGrid(
    templates: List<InspirationTemplate>,
    allTemplates: List<InspirationTemplate>,
    selectedTemplateIndex: Int,
    onSelect: (Int, List<String>) -> Unit,
) {
    val context = LocalContext.current
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        templates.chunked(2).forEach { rowTemplates ->
            Row(horizontalArrangement = Arrangement.spacedBy(9.dp), modifier = Modifier.fillMaxWidth()) {
                rowTemplates.forEach { template ->
                    val realIndex = allTemplates.indexOf(template).coerceAtLeast(0)
                    val previewItems = remember(template.id, template.targetAssetPath) {
                        loadTargetAssetItems(context, template.targetAssetPath).ifEmpty { template.items }
                    }
                    InspirationCoverCard(
                        template = template,
                        index = realIndex,
                        selected = realIndex == selectedTemplateIndex,
                        previewItems = previewItems,
                        modifier = Modifier.weight(1f),
                        onClick = { onSelect(realIndex, previewItems) },
                    )
                }
                repeat(2 - rowTemplates.size) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun InspirationCoverCard(
    template: InspirationTemplate,
    index: Int,
    selected: Boolean,
    previewItems: List<String>,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .height(176.dp)
            .background(Color(0xFFFFFDF8), RoundedCornerShape(16.dp))
            .border(if (selected) 1.6.dp else 0.7.dp, if (selected) GoaldayDesign.Pink else Color(0x22A88966), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(88.dp)
                .background(topicCoverBrush(template, index), RoundedCornerShape(12.dp)),
        ) {
            TopicCoverArt(template = template, index = index, compact = true)
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .background(Color(0x66FFFFFF), RoundedCornerShape(99.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
            ) {
                Text(template.category, color = Color(0xFF5F4939), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, maxLines = 1)
            }
        }
        Text(template.title, color = GoaldayDesign.InkPrimary, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, maxLines = 1)
        Text(template.subtitle, color = Color(0xFF7A7065), style = MaterialTheme.typography.labelSmall, maxLines = 1)
        Text("${previewItems.size} 项 · ${previewItems.take(2).joinToString(" / ")}", color = GoaldayDesign.InkMuted, style = MaterialTheme.typography.labelSmall, maxLines = 1)
    }
}

@Composable
private fun InspirationDraftPanel(
    mode: InspirationMode,
    selectedTemplate: InspirationTemplate,
    draftItems: MutableList<InspirationDraftItem>,
    focusedIndex: Int,
    inputText: String,
    onFocusedIndexChange: (Int) -> Unit,
    onInputTextChange: (String) -> Unit,
    onRemoveFocused: () -> Unit,
    onInsertDraft: () -> Unit,
    onToggleFocused: () -> Unit,
    onImport: () -> Unit,
    onImportAndOpen: () -> Unit,
) {
    Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFFFFEFC), RoundedCornerShape(18.dp))
                .border(0.8.dp, GoaldayDesign.BorderColor.copy(alpha = 0.14f), RoundedCornerShape(18.dp))
                .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    text = when (mode) {
                        InspirationMode.CENTER -> selectedTemplate.title
                        InspirationMode.SAVE -> "已导入任务池"
                        InspirationMode.FLIP -> "翻页"
                    },
                    style = MaterialTheme.typography.titleSmall,
                    color = Color(0xFF28241F),
                    fontWeight = FontWeight.SemiBold,
                )
                Text("${draftItems.count { it.selected }}/${draftItems.size} 已选", style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.InkMuted)
            }
            Text(
                if (selectedTemplate.linkToSchedule) "目标模板" else "记录模板",
                style = MaterialTheme.typography.labelSmall,
                color = GoaldayDesign.Pink,
                modifier = Modifier
                    .background(Color(0x12E88FAE), RoundedCornerShape(99.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }

        draftItems.forEachIndexed { index, item ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (index == focusedIndex) Color(0xFFFFF4F8) else Color.Transparent, RoundedCornerShape(10.dp))
                    .clickable { onFocusedIndexChange(index) }
                    .padding(horizontal = 6.dp, vertical = 5.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (item.selected) "✓" else "□",
                        color = if (item.selected) GoaldayDesign.Positive else Color(0xFFB4ADA4),
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .size(22.dp)
                            .clickable {
                                draftItems[index] = draftItems[index].copy(selected = !draftItems[index].selected)
                            },
                    )
                    Text(item.text, modifier = Modifier.weight(1f), color = Color(0xFF2F2A24), style = MaterialTheme.typography.bodySmall)
                }
            }
        }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF6F0EA), RoundedCornerShape(12.dp))
                    .padding(horizontal = 9.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("灵感草稿", style = MaterialTheme.typography.bodySmall, color = Color(0xFF575757))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("删除", color = GoaldayDesign.InkMuted, style = MaterialTheme.typography.labelSmall, modifier = Modifier.clickable(onClick = onRemoveFocused))
                    Text("插入", color = GoaldayDesign.Pink, style = MaterialTheme.typography.labelSmall, modifier = Modifier.clickable(onClick = onInsertDraft))
                    Text("勾选", color = GoaldayDesign.Positive, style = MaterialTheme.typography.labelSmall, modifier = Modifier.clickable(onClick = onToggleFocused))
                }
            }

            BasicTextField(
                value = inputText,
                onValueChange = onInputTextChange,
                textStyle = TextStyle(color = Color(0xFF2C2925)),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0x0A000000), RoundedCornerShape(10.dp))
                    .padding(horizontal = 9.dp, vertical = 7.dp),
                decorationBox = { inner ->
                    if (inputText.isBlank()) Text("输入灵感内容，点 + 插入", color = Color(0xFF9A9188), style = MaterialTheme.typography.bodySmall)
                    inner()
                },
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "导入任务池",
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier
                        .background(GoaldayDesign.InkPrimary, RoundedCornerShape(GoaldayDesign.RadiusPill))
                        .clickable(onClick = onImport)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                )
                Text(
                    "导入并打开手账",
                    color = GoaldayDesign.Pink,
                    modifier = Modifier
                        .clickable(onClick = onImportAndOpen)
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                )
            }
    }
}
