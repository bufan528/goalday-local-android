package com.bf410.goaldaylocal.ui.book

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardHide
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.bf410.goaldaylocal.R
import com.bf410.goaldaylocal.ui.replica.GoaldayDesign

@Composable
internal fun StructuredDiaryEditor(
    state: StructuredDiary,
    onStateChange: (StructuredDiary) -> Unit,
    onPickDate: () -> Unit,
    onAddImage: () -> Unit,
    onAddTextBlock: () -> Unit,
    onAddTopicTargetBlock: () -> Unit,
    onRemoveImage: (String) -> Unit,
    pendingCommand: RichEditorCommand?,
    onCommand: (RichEditorCommand) -> Unit,
    onDone: () -> Unit,
    isInBook: Boolean = false,
) {
    val dateLabel = remember(state.dateIso) { diaryDateLabel(state.date) }
    var richEditorExpanded by remember(state.dateIso) { mutableStateOf(false) }
    var promptGridVisible by remember { mutableStateOf(false) }
    var focusedBlockIndex by remember(state.blocksRaw) { mutableStateOf(0) }
    val editorTextCount = state.blocks.count { it.type == DiaryBlockType.TEXT } +
        listOf(state.todayDone, state.workTasks, state.smallJoy, state.canImprove, state.richHtml)
            .count { it.isNotBlank() }
    val editorImageCount = state.blocks.count { it.type == DiaryBlockType.IMAGE } + state.legacyImageUris.size
    val editorTargetCount = state.blocks.count {
        it.type == DiaryBlockType.TARGET || it.type == DiaryBlockType.TARGET_CHILD || it.type == DiaryBlockType.TOPIC_TARGET ||
            it.type == DiaryBlockType.TARGET_IN_BOOK || it.type == DiaryBlockType.TARGET_CHILD_IN_BOOK || it.type == DiaryBlockType.TOPIC_TARGET_IN_BOOK
    }
    val normalizedFocusIndex = focusedBlockIndex.coerceIn(0, (state.blocks.size - 1).coerceAtLeast(0))
    val focusedBlock = state.blocks.getOrNull(normalizedFocusIndex)
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        DiaryEditorHeader(
            dateLabel = dateLabel,
            onPickDate = onPickDate,
            onDone = onDone,
        )
        DiaryEditorToolbar(
            textCount = editorTextCount,
            imageCount = editorImageCount,
            targetCount = editorTargetCount,
            onAddImage = onAddImage,
            onAddTextBlock = onAddTextBlock,
            onAddTopicTargetBlock = onAddTopicTargetBlock,
            onAddTargetChildBlock = { onStateChange(state.withTargetChildBlock()) },
            onCommand = onCommand,
            onShowPrompts = {
                promptGridVisible = true
                richEditorExpanded = true
            },
        )
        DiaryFocusedBlockToolbar(
            block = focusedBlock,
            index = normalizedFocusIndex,
            onStyleChange = { style ->
                focusedBlock?.let {
                    onStateChange(state.withBlockStyle(normalizedFocusIndex, style))
                }
            },
            onAddChild = {
                focusedBlock?.let {
                    onStateChange(state.withBlockChild(normalizedFocusIndex))
                    focusedBlockIndex = (normalizedFocusIndex + 1).coerceAtMost(state.blocks.size)
                }
            },
            onInsertAfter = { type ->
                onStateChange(state.withInsertedBlockAfter(normalizedFocusIndex, type))
                focusedBlockIndex = (normalizedFocusIndex + 1).coerceAtMost(state.blocks.size)
            },
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(GoaldayDesign.RadiusM))
                .background(GoaldayDesign.adaptiveSurface)
                .border(GoaldayDesign.Hairline, GoaldayDesign.BorderColor.copy(alpha = 0.09f), RoundedCornerShape(GoaldayDesign.RadiusM))
                .padding(horizontal = GoaldayDesign.Space2 + 1.dp, vertical = GoaldayDesign.Space2),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("富文本记录", style = MaterialTheme.typography.labelMedium, color = GoaldayDesign.adaptiveInkSecondary, fontWeight = FontWeight.SemiBold)
                Text(
                    if (richEditorExpanded) "收起" else "打开富文本编辑器",
                    style = MaterialTheme.typography.labelSmall,
                    color = GoaldayDesign.Pink,
                    modifier = Modifier
                        .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
                        .background(GoaldayDesign.PinkTint)
                        .clickable { richEditorExpanded = !richEditorExpanded }
                        .padding(horizontal = GoaldayDesign.Space2, vertical = GoaldayDesign.Space1),
                )
            }
            if (richEditorExpanded) {
                RichDiaryEditor(
                    html = state.richHtml,
                    placeholder = "写一段更自由的日记，支持加粗、标题、引用和列表。",
                    pendingCommand = pendingCommand,
                    onHtmlChange = { html -> onStateChange(state.withRichHtml(html)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(138.dp)
                        .clip(RoundedCornerShape(GoaldayDesign.RadiusS))
                        .background(GoaldayDesign.adaptiveSurfaceSoft),
                )
            } else {
                Text(
                    plainTextFromHtml(state.richHtml).ifBlank { "点击后加载富文本编辑器，普通日记块不受影响。" },
                    style = MaterialTheme.typography.bodySmall,
                    color = GoaldayDesign.adaptiveInkMuted,
                    maxLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(GoaldayDesign.RadiusS))
                        .background(GoaldayDesign.adaptiveSurfaceSoft)
                        .clickable { richEditorExpanded = true }
                        .padding(horizontal = GoaldayDesign.Space2 + 1.dp, vertical = GoaldayDesign.Space2),
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                OutlinedTextField(
                    value = state.moodTags,
                    onValueChange = { onStateChange(state.copy(moodTags = it)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(GoaldayDesign.RadiusS),
                    label = { Text("心情标签（空格/逗号分隔）") },
                    singleLine = true,
                )
                DiaryEditField("☀️ 今日完成", state.todayDone) { onStateChange(state.copy(todayDone = it)) }
                DiaryEditField("📚 工作任务", state.workTasks) { onStateChange(state.copy(workTasks = it)) }
            }
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(GoaldayDesign.BorderColor.copy(alpha = 0.09f)),
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                DiaryEditField("🍀 小幸福", state.smallJoy) { onStateChange(state.copy(smallJoy = it)) }
                DiaryEditField("📝 可改进", state.canImprove) { onStateChange(state.copy(canImprove = it)) }
                DiaryEditField("📷 图片描述", state.photoText) { onStateChange(state.withPhotoText(it)) }
            }
        }
        DiaryTypedBlockEditor(
            blocks = state.blocks,
            onBlockTextChange = { index, text -> onStateChange(state.withBlockText(index, text)) },
            onBlockStyleChange = { index, style -> onStateChange(state.withBlockStyle(index, style)) },
            onAddChild = { index -> onStateChange(state.withBlockChild(index)) },
            onMoveBlock = { index, direction -> onStateChange(state.withMovedBlock(index, direction)) },
            onRemoveBlock = { index -> onStateChange(state.withoutBlock(index)) },
            focusedIndex = normalizedFocusIndex,
            onFocusBlock = { focusedBlockIndex = it },
        )
        if (state.legacyImageUris.isNotEmpty()) {
            DiaryImageStrip(
                imageUris = state.legacyImageUris,
                onRemoveImage = onRemoveImage,
            )
        }
        if (promptGridVisible) {
            DiaryPromptGridDialog(
                onDismiss = { promptGridVisible = false },
                onInsert = { title, hint ->
                    onStateChange(state.withRichHtml(state.richHtml + "<p><b>${title}</b></p><p>${hint}</p>"))
                    promptGridVisible = false
                },
            )
        }
        DiaryEditorBottomBar(
            isInBook = isInBook,
            onAddImage = onAddImage,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun DiaryEditorHeader(
    dateLabel: String,
    onPickDate: () -> Unit,
    onDone: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(horizontal = 33.33.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = dateLabel,
            fontSize = 24.sp,
            color = Color(0xFF000000),
            fontWeight = FontWeight.Medium,
            modifier = Modifier.clickable { onPickDate() },
        )
        Text(
            text = "完成",
            color = Color.White,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
                .background(GoaldayDesign.PrimaryAction)
                .clickable { onDone() }
                .padding(horizontal = 16.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun DiaryEditorToolbar(
    textCount: Int,
    imageCount: Int,
    targetCount: Int,
    onAddImage: () -> Unit,
    onAddTextBlock: () -> Unit,
    onAddTopicTargetBlock: () -> Unit,
    onAddTargetChildBlock: () -> Unit,
    onCommand: (RichEditorCommand) -> Unit,
    onShowPrompts: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(GoaldayDesign.RadiusM))
                .background(GoaldayDesign.Pink.copy(alpha = 0.09f))
                .border(GoaldayDesign.Hairline, GoaldayDesign.Pink.copy(alpha = 0.16f), RoundedCornerShape(GoaldayDesign.RadiusM))
                .padding(horizontal = GoaldayDesign.Space2, vertical = GoaldayDesign.Space1 + 2.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            DiaryEditorCountPill("文本", textCount, GoaldayDesign.adaptiveInkSecondary, Modifier.weight(1f))
            DiaryEditorCountPill("图片", imageCount, GoaldayDesign.AccentMauve, Modifier.weight(1f))
            DiaryEditorCountPill("目标", targetCount, GoaldayDesign.Positive, Modifier.weight(1f))
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.horizontalScroll(rememberScrollState()),
        ) {
            DiaryEditorToolChip("文字", GoaldayDesign.adaptiveInkSecondary, onAddTextBlock)
            DiaryEditorToolChip("图片", GoaldayDesign.AccentMauve, onAddImage)
            DiaryEditorToolChip("目标", GoaldayDesign.Positive, onAddTopicTargetBlock)
            DiaryEditorToolChip("子目标", GoaldayDesign.AccentSage, onAddTargetChildBlock)
            DiaryEditorToolChip("B", GoaldayDesign.adaptiveInkPrimary) { onCommand(RichEditorCommand("bold")) }
            DiaryEditorToolChip("H1", GoaldayDesign.AccentTerracotta) { onCommand(RichEditorCommand("formatBlock", "h1")) }
            DiaryEditorToolChip("引用", GoaldayDesign.AccentPeriwinkle) { onCommand(RichEditorCommand("formatBlock", "blockquote")) }
            DiaryEditorToolChip("列表", GoaldayDesign.Positive) { onCommand(RichEditorCommand("insertUnorderedList")) }
            DiaryEditorToolChip("灵感", GoaldayDesign.Today, onShowPrompts)
        }
    }
}

@Composable
private fun DiaryEditorCountPill(
    label: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
            .background(GoaldayDesign.adaptiveWhiteOverlayMedium)
            .border(GoaldayDesign.Hairline, color.copy(alpha = 0.16f), RoundedCornerShape(GoaldayDesign.RadiusPill))
            .padding(horizontal = GoaldayDesign.Space2 - 1.dp, vertical = GoaldayDesign.Space1),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.SemiBold, maxLines = 1)
        Text(count.toString(), style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.adaptiveInkMuted, maxLines = 1)
    }
}

@Composable
private fun DiaryFocusedBlockToolbar(
    block: DiaryEntryBlock?,
    index: Int,
    onStyleChange: (DiaryBlockStyle) -> Unit,
    onAddChild: () -> Unit,
    onInsertAfter: (DiaryBlockType) -> Unit,
) {
    val color = block?.let { diaryBlockTypeColor(it.type) } ?: GoaldayDesign.adaptiveInkMuted
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(GoaldayDesign.RadiusM))
                .background(GoaldayDesign.adaptiveSurface)
                .border(GoaldayDesign.Hairline, color.copy(alpha = 0.18f), RoundedCornerShape(GoaldayDesign.RadiusM))
                .padding(horizontal = GoaldayDesign.Space2, vertical = GoaldayDesign.Space2 - 1.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text("块编辑器", style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.adaptiveInkMuted, maxLines = 1)
                Text(
                    block?.let { "%02d · ${diaryBlockDisplayTitle(it.type)}".format(index + 1) } ?: "未选择块",
                    style = MaterialTheme.typography.labelMedium,
                    color = color,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                )
            }
            if (block != null && block.type != DiaryBlockType.IMAGE && block.type != DiaryBlockType.TARGET_CHILD && block.type != DiaryBlockType.TARGET_CHILD_IN_BOOK) {
                DiaryEditorToolChip("子项", color, onAddChild)
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.horizontalScroll(rememberScrollState()),
        ) {
            if (block != null && block.type != DiaryBlockType.IMAGE) {
                DiaryBlockStyle.entries.forEach { style ->
                    DiaryStyleChip(
                        label = style.label,
                        selected = block.style == style,
                        color = color,
                        onClick = { onStyleChange(style) },
                    )
                }
            } else {
                Text("点选下方日记块后可快速改格式", style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.adaptiveInkMuted)
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.horizontalScroll(rememberScrollState()),
        ) {
            Text("后插入", style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.adaptiveInkMuted, maxLines = 1)
            DiaryEditorToolChip("文字", GoaldayDesign.adaptiveInkSecondary) { onInsertAfter(DiaryBlockType.TEXT) }
            DiaryEditorToolChip("目标", GoaldayDesign.Positive) { onInsertAfter(DiaryBlockType.TARGET) }
            DiaryEditorToolChip("子目标", GoaldayDesign.AccentSage) { onInsertAfter(DiaryBlockType.TARGET_CHILD) }
            DiaryEditorToolChip("专题目标", GoaldayDesign.AccentMauve) { onInsertAfter(DiaryBlockType.TOPIC_TARGET) }
        }
    }
}

@Composable
private fun DiaryTypedBlockEditor(
    blocks: List<DiaryEntryBlock>,
    onBlockTextChange: (Int, String) -> Unit,
    onBlockStyleChange: (Int, DiaryBlockStyle) -> Unit,
    onAddChild: (Int) -> Unit,
    onMoveBlock: (Int, Int) -> Unit,
    onRemoveBlock: (Int) -> Unit,
    focusedIndex: Int,
    onFocusBlock: (Int) -> Unit,
) {
    if (blocks.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            DiaryEmptyBlockRow(DiaryBlockType.TEXT, "文字记录")
            DiaryEmptyBlockRow(DiaryBlockType.IMAGE, "图片记录")
            DiaryEmptyBlockRow(DiaryBlockType.TARGET, "目标记录")
        }
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        blocks.forEachIndexed { index, block ->
            DiaryTypedBlockEditRow(
                index = index,
                block = block,
                onTextChange = { onBlockTextChange(index, it) },
                onStyleChange = { onBlockStyleChange(index, it) },
                onAddChild = { onAddChild(index) },
                canMoveUp = index > 0,
                canMoveDown = index < blocks.lastIndex,
                onMoveUp = { onMoveBlock(index, -1) },
                onMoveDown = { onMoveBlock(index, 1) },
                onRemove = { onRemoveBlock(index) },
                selected = index == focusedIndex,
                onFocus = { onFocusBlock(index) },
            )
        }
    }
}

@Composable
private fun DiaryEmptyBlockRow(
    type: DiaryBlockType,
    label: String,
) {
    val color = diaryBlockTypeColor(type)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(GoaldayDesign.RadiusS))
            .background(GoaldayDesign.CoverWhiteOverlayMedium)
            .border(GoaldayDesign.Hairline, color.copy(alpha = 0.16f), RoundedCornerShape(GoaldayDesign.RadiusS))
            .padding(horizontal = GoaldayDesign.Space2, vertical = GoaldayDesign.Space2 - 1.dp),
        horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space2),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(42.dp)
                .clip(RoundedCornerShape(GoaldayDesign.RadiusS))
                .background(color.copy(alpha = 0.12f))
                .padding(horizontal = GoaldayDesign.Space1 + 2.dp, vertical = GoaldayDesign.Space1),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = diaryBlockTypeIcon(type),
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp),
            )
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.adaptiveInkSecondary, fontWeight = FontWeight.SemiBold)
            Text(diaryBlockDisplaySubtitle(type), style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.adaptiveInkMuted, maxLines = 1)
        }
        Text("待添加", style = MaterialTheme.typography.labelSmall, color = color, maxLines = 1)
    }
}

@Composable
private fun DiaryTypedBlockEditRow(
    index: Int,
    block: DiaryEntryBlock,
    selected: Boolean,
    onFocus: () -> Unit,
    onTextChange: (String) -> Unit,
    onStyleChange: (DiaryBlockStyle) -> Unit,
    onAddChild: () -> Unit,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onRemove: () -> Unit,
) {
    val color = diaryBlockTypeColor(block.type)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(GoaldayDesign.RadiusM))
            .background(diaryBlockTypeBackground(block.type))
            .border(if (selected) 1.4.dp else GoaldayDesign.Hairline, color.copy(alpha = if (selected) 0.58f else 0.28f), RoundedCornerShape(GoaldayDesign.RadiusM))
            .clickable(onClick = onFocus)
            .padding(horizontal = GoaldayDesign.Space2 + 1.dp, vertical = GoaldayDesign.Space2 - 1.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalAlignment = Alignment.CenterVertically) {
                DiaryBlockTypeBadge(type = block.type, index = index + 1)
                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Text(diaryBlockDisplayTitle(block.type), style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.SemiBold)
                    Text(diaryBlockDisplaySubtitle(block.type), style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.adaptiveInkMuted, maxLines = 1)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space1), verticalAlignment = Alignment.CenterVertically) {
                DiaryBlockActionChip("上移", color, enabled = canMoveUp, onClick = onMoveUp)
                DiaryBlockActionChip("下移", color, enabled = canMoveDown, onClick = onMoveDown)
                if (block.type != DiaryBlockType.IMAGE && block.type != DiaryBlockType.TARGET_CHILD && block.type != DiaryBlockType.TARGET_CHILD_IN_BOOK) {
                    DiaryBlockActionChip("子项", color, enabled = true, onClick = onAddChild)
                }
                DiaryBlockActionChip("删除", GoaldayDesign.adaptiveInkMuted, enabled = true, onClick = onRemove)
            }
        }
        if (block.type != DiaryBlockType.IMAGE) {
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
                DiaryBlockStyle.entries.forEach { style ->
                    DiaryStyleChip(
                        label = style.label,
                        selected = block.style == style,
                        color = color,
                        onClick = { onStyleChange(style) },
                    )
                }
            }
        }
        if (block.type == DiaryBlockType.IMAGE) {
            DiaryImageTile(
                uri = block.text,
                onRemove = null,
                modifier = Modifier.fillMaxWidth(),
                fixedHeight = false,
            )
            BasicTextField(
                value = block.text,
                onValueChange = onTextChange,
                textStyle = MaterialTheme.typography.labelSmall.copy(color = GoaldayDesign.adaptiveInkMuted),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(GoaldayDesign.RadiusS))
                    .background(GoaldayDesign.adaptiveWhiteOverlayMedium)
                    .padding(horizontal = GoaldayDesign.Space2, vertical = GoaldayDesign.Space1 + 2.dp),
            )
        } else {
            BasicTextField(
                value = block.text,
                onValueChange = onTextChange,
                textStyle = diaryBlockTextStyle(block),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(GoaldayDesign.RadiusS))
                    .background(GoaldayDesign.adaptiveWhiteOverlayMedium)
                    .padding(horizontal = GoaldayDesign.Space2, vertical = GoaldayDesign.Space2 - 1.dp),
            )
        }
    }
}

@Composable
private fun DiaryBlockActionChip(
    label: String,
    color: Color,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Text(
        label,
        style = MaterialTheme.typography.labelSmall,
        color = if (enabled) color else GoaldayDesign.adaptiveInkMuted.copy(alpha = 0.42f),
        maxLines = 1,
        modifier = Modifier
            .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
            .background(if (enabled) GoaldayDesign.adaptiveWhiteOverlayMedium else Color.Transparent)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = GoaldayDesign.Space1 + 2.dp, vertical = 2.dp),
    )
}

@Composable
private fun DiaryStyleChip(
    label: String,
    selected: Boolean,
    color: Color,
    onClick: () -> Unit,
) {
    Text(
        label,
        style = MaterialTheme.typography.labelSmall,
        color = if (selected) Color.White else color,
        maxLines = 1,
        modifier = Modifier
            .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
            .background(if (selected) color else GoaldayDesign.adaptiveWhiteOverlayMedium)
            .clickable(onClick = onClick)
            .padding(horizontal = GoaldayDesign.Space2 - 1.dp, vertical = GoaldayDesign.Space1 - 1.dp),
    )
}

@Composable
private fun DiaryEditorToolChip(
    label: String,
    color: Color,
    onClick: () -> Unit,
) {
    Text(
        label,
        style = MaterialTheme.typography.labelSmall,
        color = color,
        maxLines = 1,
        modifier = Modifier
            .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
            .background(GoaldayDesign.adaptiveWhiteOverlayMedium)
            .clickable(onClick = onClick)
            .padding(horizontal = GoaldayDesign.Space2, vertical = GoaldayDesign.Space1),
    )
}

@Composable
private fun DiaryBlockTypeBadge(
    type: DiaryBlockType,
    index: Int,
) {
    val color = diaryBlockTypeColor(type)
    Column(
        modifier = Modifier
            .width(42.dp)
            .clip(RoundedCornerShape(GoaldayDesign.RadiusS))
            .background(color.copy(alpha = 0.13f))
            .border(GoaldayDesign.Hairline, color.copy(alpha = 0.26f), RoundedCornerShape(GoaldayDesign.RadiusS))
            .padding(horizontal = GoaldayDesign.Space1 + 1.dp, vertical = GoaldayDesign.Space1 + 1.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Icon(
            imageVector = diaryBlockTypeIcon(type),
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(18.dp),
        )
        Text("%02d".format(index), color = GoaldayDesign.adaptiveInkMuted, style = MaterialTheme.typography.labelSmall, maxLines = 1)
    }
}

// 9 宫格灵感 prompt 模板：在富文本编辑器中插入写作提示
private data class DiaryPromptTemplate(
    val emoji: String,
    val title: String,
    val hint: String,
)

private val DiaryPromptTemplates = listOf(
    DiaryPromptTemplate("🙏", "今日感恩", "写下今天值得感恩的三件小事..."),
    DiaryPromptTemplate("🏆", "今日成就", "今天完成了什么让自己骄傲的事？"),
    DiaryPromptTemplate("💭", "情绪记录", "此刻的情绪是什么，从何而来？"),
    DiaryPromptTemplate("🤝", "与人连接", "今天与谁有过温暖的互动？"),
    DiaryPromptTemplate("🌱", "学到的事", "今天学到的新知识或感悟..."),
    DiaryPromptTemplate("⛰️", "今日挑战", "遇到了什么困难，如何应对？"),
    DiaryPromptTemplate("☕", "小确幸", "今天微小但确实的幸福瞬间..."),
    DiaryPromptTemplate("📌", "明日计划", "明天最重要的一件事是什么？"),
    DiaryPromptTemplate("🪞", "自我对话", "对现在的自己说一句话..."),
)

@Composable
private fun DiaryPromptGridDialog(
    onDismiss: () -> Unit,
    onInsert: (title: String, hint: String) -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(GoaldayDesign.RadiusXL),
            color = GoaldayDesign.adaptiveSurface,
            tonalElevation = 6.dp,
            shadowElevation = 12.dp,
            border = BorderStroke(GoaldayDesign.Hairline, GoaldayDesign.BorderColor.copy(alpha = 0.09f)),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(GoaldayDesign.Space5),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            "写作灵感",
                            style = MaterialTheme.typography.titleMedium,
                            color = GoaldayDesign.adaptiveInkPrimary,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            "选择一个提示，开启今日的记录",
                            style = MaterialTheme.typography.labelSmall,
                            color = GoaldayDesign.adaptiveInkMuted,
                        )
                    }
                    TextButton(onClick = onDismiss) {
                        Text("关闭", color = GoaldayDesign.adaptiveInkSecondary)
                    }
                }
                DiaryPromptTemplates.chunked(3).forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        rowItems.forEach { template ->
                            DiaryPromptCell(
                                template = template,
                                modifier = Modifier.weight(1f),
                                onClick = { onInsert(template.title, template.hint) },
                            )
                        }
                        repeat(3 - rowItems.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DiaryPromptCell(
    template: DiaryPromptTemplate,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(GoaldayDesign.RadiusL))
            .background(GoaldayDesign.adaptiveSurfaceSoft)
            .border(GoaldayDesign.Hairline, GoaldayDesign.Today.copy(alpha = 0.18f), RoundedCornerShape(GoaldayDesign.RadiusL))
            .clickable(onClick = onClick)
            .padding(horizontal = GoaldayDesign.Space2 + 2.dp, vertical = GoaldayDesign.Space3),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space1),
    ) {
        Text(template.emoji, style = MaterialTheme.typography.titleLarge)
        Text(
            template.title,
            style = MaterialTheme.typography.labelMedium,
            color = GoaldayDesign.adaptiveInkPrimary,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
        )
        Text(
            template.hint,
            style = MaterialTheme.typography.labelSmall,
            color = GoaldayDesign.adaptiveInkMuted,
            maxLines = 2,
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * 日记编辑器底栏，对齐原 APK fragment_diary.xml / fragment_diary_inbook.xml。
 * - 独立页高度 46dp（fragment_diary.xml fl_bottom_bar=46dip），书内页高度 23dp（fragment_diary_inbook.xml fl_bottom_bar=23dip）
 * - 按钮容器：独立页 25dp，书内页 23dp（apktool误显示为pt，实际为dip）
 * - 图标大小：独立页 24dp，书内页 12.5dp
 * - 书内页图片按钮使用原版图标 ic_select_pic
 * - 背景 #E5DAD4（TabBarBg）
 * - 左侧图片 + 键盘按钮
 */
@Composable
private fun DiaryEditorBottomBar(
    isInBook: Boolean,
    onAddImage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val height = if (isInBook) 23.dp else 46.dp
    val buttonSize = if (isInBook) 23.dp else 25.dp
    val iconSize = if (isInBook) 12.5.dp else 24.dp
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(GoaldayDesign.TabBarBg)
            .padding(horizontal = 3.75.dp),
    ) {
        Row(
            modifier = Modifier.align(Alignment.CenterStart),
            horizontalArrangement = Arrangement.spacedBy(3.75.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(buttonSize)
                    .clip(RoundedCornerShape(GoaldayDesign.RadiusS))
                    .clickable { onAddImage() },
                contentAlignment = Alignment.Center,
            ) {
                if (isInBook) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_select_pic),
                        contentDescription = "插入图片",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(iconSize),
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "插入图片",
                        tint = GoaldayDesign.adaptiveInkPrimary,
                        modifier = Modifier.size(iconSize),
                    )
                }
            }
            Box(
                modifier = Modifier
                    .size(buttonSize)
                    .clip(RoundedCornerShape(GoaldayDesign.RadiusS))
                    .clickable {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardHide,
                    contentDescription = "收起键盘",
                    tint = GoaldayDesign.adaptiveInkPrimary,
                    modifier = Modifier.size(iconSize),
                )
            }
        }
    }
}
