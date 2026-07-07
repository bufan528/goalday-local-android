package com.bf410.goaldaylocal.ui.book

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.VerticalAlignTop
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bf410.goaldaylocal.R
import com.bf410.goaldaylocal.data.ScheduleEntry
import com.bf410.goaldaylocal.data.TargetItemMeta
import com.bf410.goaldaylocal.data.TargetPage
import com.bf410.goaldaylocal.ui.replica.GoaldayDesign
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HandbookTargetReplicaPage(
    modifier: Modifier,
    page: TargetPage,
    pageIndex: Int,
    pageCount: Int,
    tint: Color,
    customPageItems: List<String>,
    schedulePreviewEntries: List<ScheduleEntry>,
    targetItemMeta: Map<String, TargetItemMeta>,
    isChecked: (String, String) -> Boolean,
    onToggleChecked: (String, String) -> Unit,
    onAddCustomItem: (String) -> Unit,
    onRemoveCustomItem: (String) -> Unit,
    onRenameCustomItem: (String, String) -> Unit,
    onAddToSchedule: (String, Int) -> Unit,
    onUpdateTargetNote: (String, String) -> Unit,
    onUpdateTargetDeadline: (String, Int?) -> Unit,
    onOpenTargetDetail: (String) -> Unit,
    turnProgress: Float,
    turnDirection: TurnDirection?,
) {
    val eased = turnProgress * turnProgress * (3f - 2f * turnProgress)
    val contentShift = when (turnDirection) {
        TurnDirection.NEXT -> -(eased * 8f)
        TurnDirection.PREVIOUS -> eased * 8f
        null -> 0f
    }
    val alpha = (1f - eased * 0.08f).coerceIn(0.92f, 1f)
    // 目标页三种展示模式选项（对齐原版 target_detail_options）
    var showCompleted by rememberSaveable(pageIndex) { mutableStateOf(false) }
    var showCompletionTimeAndDiary by rememberSaveable(pageIndex) { mutableStateOf(false) }
    var showSequenceNumber by rememberSaveable(pageIndex) { mutableStateOf(false) }
    var showOptionsMenu by remember { mutableStateOf(false) }
    Box(
        modifier = modifier
            .graphicsLayer {
                translationX = contentShift
                this.alpha = alpha
            }
            .padding(GoaldayDesign.Space3),
    ) {
        // P0-2 大修：HandbookPaperRuling 改为 Modifier 扩展，画在 Column 内部
        Column(
            modifier = Modifier
                .fillMaxSize()
                .handbookPaperRuling(scrollState = null),
            verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space2),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SectionStamp("目标", tint)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space2),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // 视图选项入口：点击弹出底部菜单切换三种展示模式
                    Text(
                        "视图",
                        style = MaterialTheme.typography.labelMedium,
                        color = GoaldayDesign.adaptiveInkSecondary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
                            .background(tint.copy(alpha = 0.14f))
                            .clickable { showOptionsMenu = true }
                            .padding(horizontal = GoaldayDesign.Space2, vertical = GoaldayDesign.Space1),
                    )
                    Text("${pageIndex + 1}/$pageCount", style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.adaptiveInkMuted)
                }
            }
            TargetDetailReplicaPage(
                pageTitle = page.title,
                baseItems = page.items,
                customItems = customPageItems,
                tint = tint,
                schedulePreviewEntries = schedulePreviewEntries,
                targetItemMeta = targetItemMeta,
                isChecked = isChecked,
                onToggleChecked = onToggleChecked,
                onAddCustomItem = onAddCustomItem,
                onRemoveCustomItem = onRemoveCustomItem,
                onRenameCustomItem = onRenameCustomItem,
                onAddToSchedule = onAddToSchedule,
                onUpdateTargetNote = onUpdateTargetNote,
                onUpdateTargetDeadline = onUpdateTargetDeadline,
                onOpenTargetDetail = onOpenTargetDetail,
                showCompleted = showCompleted,
                showCompletionTimeAndDiary = showCompletionTimeAndDiary,
                showSequenceNumber = showSequenceNumber,
            )
        }
    }

    // 目标页展示模式底部菜单
    if (showOptionsMenu) {
        val options = stringArrayResource(R.array.target_detail_options)
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = { showOptionsMenu = false },
            sheetState = sheetState,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = GoaldayDesign.Space3)
                    .padding(bottom = GoaldayDesign.Space3),
                verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space1),
            ) {
                Text("显示选项", style = MaterialTheme.typography.titleMedium, color = GoaldayDesign.adaptiveInkPrimary)
                HorizontalDivider(color = GoaldayDesign.adaptiveDivider)
                if (options.isNotEmpty()) {
                    TargetOptionSheetRow(
                        label = options[0],
                        checked = showCompleted,
                        onCheckedChange = { showCompleted = it },
                    )
                }
                if (options.size > 1) {
                    TargetOptionSheetRow(
                        label = options[1],
                        checked = showCompletionTimeAndDiary,
                        onCheckedChange = { showCompletionTimeAndDiary = it },
                    )
                }
                if (options.size > 2) {
                    TargetOptionSheetRow(
                        label = options[2],
                        checked = showSequenceNumber,
                        onCheckedChange = { showSequenceNumber = it },
                    )
                }
                TextButton(
                    onClick = { showOptionsMenu = false },
                    modifier = Modifier.align(Alignment.End),
                ) {
                    Text("关闭", color = GoaldayDesign.adaptiveInkSecondary)
                }
            }
        }
    }
}

@Composable
private fun TargetOptionSheetRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(GoaldayDesign.RadiusS))
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = GoaldayDesign.Space1),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = GoaldayDesign.adaptiveInkPrimary)
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
internal fun TargetDetailReplicaPage(
    pageTitle: String,
    baseItems: List<String>,
    customItems: List<String>,
    tint: Color,
    schedulePreviewEntries: List<ScheduleEntry>,
    targetItemMeta: Map<String, TargetItemMeta>,
    isChecked: (String, String) -> Boolean,
    onToggleChecked: (String, String) -> Unit,
    onAddCustomItem: (String) -> Unit,
    onRemoveCustomItem: (String) -> Unit,
    onRenameCustomItem: (String, String) -> Unit,
    onAddToSchedule: (String, Int) -> Unit,
    onUpdateTargetNote: (String, String) -> Unit,
    onUpdateTargetDeadline: (String, Int?) -> Unit,
    onOpenTargetDetail: (String) -> Unit,
    showCompleted: Boolean = true,
    showCompletionTimeAndDiary: Boolean = false,
    showSequenceNumber: Boolean = true,
) {
    val items = remember(baseItems, customItems) { (baseItems + customItems).distinct() }
    // 根据“显示已完成”选项过滤列表；关闭时隐藏已完成项
    val visibleItems = if (showCompleted) items else items.filter { !isChecked(pageTitle, it) }
    var draft by rememberSaveable(pageTitle) { mutableStateOf("") }
    var editingItem by rememberSaveable(pageTitle) { mutableStateOf<String?>(null) }
    var editingText by rememberSaveable(pageTitle) { mutableStateOf("") }
    // P1-3 精简：备注框+chip 行默认折叠，点击"展开"才显示，避免每项都像数据看板卡片
    var expandedItem by rememberSaveable(pageTitle) { mutableStateOf<String?>(null) }
    var selectedItem by rememberSaveable(pageTitle) { mutableStateOf<String?>(null) }
    val dateShortcuts = remember { targetDateShortcuts() }
    val completedCount = items.count { isChecked(pageTitle, it) }
    val scheduledByTitle = remember(schedulePreviewEntries, items) {
        items.associateWith { item ->
            schedulePreviewEntries
                .filter { it.title == item }
                .sortedWith(compareBy<ScheduleEntry>({ it.month }, { it.day }, { it.timeText }))
                .take(3)
        }
    }
    val scheduledCount = scheduledByTitle.count { it.value.isNotEmpty() }

    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space3),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(108.dp)
                .clip(RoundedCornerShape(GoaldayDesign.RadiusM))
                .background(
                    Brush.linearGradient(
                        listOf(tint.copy(alpha = 0.86f), tint.copy(alpha = 0.48f), GoaldayDesign.adaptiveWhiteOverlay.copy(alpha = 0.34f)),
                        start = Offset.Zero,
                        end = Offset(760f, 460f),
                    ),
                )
                .border(GoaldayDesign.Hairline, GoaldayDesign.adaptiveSurface.copy(alpha = 0.20f), RoundedCornerShape(GoaldayDesign.RadiusM))
                .padding(GoaldayDesign.Space4),
        ) {
            Column(
                modifier = Modifier.align(Alignment.BottomStart),
                verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space1),
            ) {
                Text(pageTitle, style = MaterialTheme.typography.titleLarge, color = GoaldayDesign.adaptiveInkPrimary, fontWeight = FontWeight.SemiBold)
                // Header 只显示总数，细分统计（已完成/已排期/待整理/自定义）由下方 TargetLedgerSummary 承担，避免信息重复
                Text("目标档案 · 共 ${items.size} 项", style = MaterialTheme.typography.bodySmall, color = GoaldayDesign.adaptiveInkSecondary)
                TargetProgressBar(
                    completed = completedCount,
                    total = items.size,
                    tint = tint,
                )
            }
        }
        TargetLedgerSummary(
            total = items.size,
            completed = completedCount,
            scheduled = scheduledCount,
            custom = customItems.size,
        )

        visibleItems.forEachIndexed { index, item ->
            val checked = isChecked(pageTitle, item)
            val scheduledEntries = scheduledByTitle[item].orEmpty()
            val meta = targetItemMeta[item] ?: TargetItemMeta()
            var noteDraft by remember(item, meta.note) { mutableStateOf(meta.note) }
            val isSelected = selectedItem == item
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(GoaldayDesign.RadiusM))
                    .background(if (checked) GoaldayDesign.GreenSoft else GoaldayDesign.adaptiveSurface)
                    .border(
                        GoaldayDesign.Hairline,
                        if (isSelected) tint.copy(alpha = 0.55f) else if (checked) GoaldayDesign.Positive.copy(alpha = 0.35f) else GoaldayDesign.adaptiveDivider,
                        RoundedCornerShape(GoaldayDesign.RadiusM),
                    )
                    .clickable { selectedItem = if (isSelected) null else item }
                    .padding(GoaldayDesign.Space3),
                verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space2),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space2 + 1.dp), verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space1)) {
                        // 显示序号选项：开启后展示 1. 2. 3. 样式序号徽章
                        if (showSequenceNumber) {
                            Text(
                                "${index + 1}.",
                                color = if (checked) Color.White else GoaldayDesign.Pink,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
                                    .background(if (checked) GoaldayDesign.Positive else tint.copy(alpha = 0.18f))
                                    .padding(horizontal = GoaldayDesign.Space2 - 1.dp, vertical = GoaldayDesign.Space1),
                            )
                        }
                        Icon(
                            imageVector = if (checked) Icons.Filled.Check else Icons.Filled.RadioButtonUnchecked,
                            contentDescription = if (checked) "已完成" else "未完成",
                            tint = if (checked) GoaldayDesign.Positive else GoaldayDesign.adaptiveInkMuted,
                            modifier = Modifier
                                .size(20.dp)
                                .clip(RoundedCornerShape(GoaldayDesign.RadiusS))
                                .clickable { onToggleChecked(pageTitle, item) }
                                .padding(2.dp),
                        )
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            // P1-3 精简：删除冗余"目标档案"标签，改为调度元信息直接显示
                            TargetScheduleMeta(entries = scheduledEntries)
                            Row(horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.CenterVertically) {
                                // P1-3：合并"详情/排入"为单个"展开"切换，点击切换备注+chip 行的显隐
                                TextActionButton(
                                    if (expandedItem == item) "收起" else "展开",
                                    GoaldayDesign.adaptiveInkSecondary,
                                ) {
                                    expandedItem = if (expandedItem == item) null else item
                                }
                            }
                        }
                        if (editingItem == item) {
                            BasicTextField(
                                value = editingText,
                                onValueChange = { editingText = it },
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodySmall.copy(color = GoaldayDesign.adaptiveInkPrimary),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = {
                                    if (item in customItems) onRenameCustomItem(item, editingText)
                                    editingItem = null
                                }),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(GoaldayDesign.adaptiveDivider, RoundedCornerShape(GoaldayDesign.RadiusS))
                                    .padding(horizontal = GoaldayDesign.Space2 - 1.dp, vertical = GoaldayDesign.Space1 + 1.dp),
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space1)) {
                                TextActionButton("保存", GoaldayDesign.adaptiveInkSecondary) {
                                    if (item in customItems) onRenameCustomItem(item, editingText)
                                    editingItem = null
                                }
                                TextActionButton("取消", GoaldayDesign.adaptiveInkMuted) { editingItem = null }
                            }
                        } else {
                            Text(
                                item,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (checked) GoaldayDesign.adaptiveInkSecondary else GoaldayDesign.adaptiveInkPrimary,
                                fontWeight = FontWeight.Medium,
                                textDecoration = if (checked) TextDecoration.LineThrough else TextDecoration.None,
                                maxLines = 2,
                                modifier = Modifier.clickable {
                                    if (item in customItems) {
                                        editingItem = item
                                        editingText = item
                                    }
                                },
                            )
                        }
                        // P1-3：截止日期常驻显示（轻量信息），调度元信息已移到上方行
                        meta.deadlineDay?.let {
                            Text("截止 ${it}日", color = GoaldayDesign.Positive, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
                // 显示完成时间和日记选项：已完成项展示完成日期与相关日记摘要
                if (showCompletionTimeAndDiary && checked) {
                    val completionDate = LocalDate.now()
                    val dateText = completionDate.format(DateTimeFormatter.ofPattern("yyyy/M/d"))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(GoaldayDesign.RadiusS))
                            .background(GoaldayDesign.Positive.copy(alpha = 0.08f))
                            .padding(horizontal = GoaldayDesign.Space2, vertical = GoaldayDesign.Space1),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text("完成时间：$dateText", color = GoaldayDesign.Positive, style = MaterialTheme.typography.labelSmall)
                        if (meta.note.isNotBlank()) {
                            Text("日记摘要：${meta.note}", color = GoaldayDesign.adaptiveInkSecondary, style = MaterialTheme.typography.labelSmall, maxLines = 2)
                        }
                    }
                }
                // P1-3 大修：备注框 + chip 行默认折叠，点击"展开"才显示
                // 原代码每项都常驻 BasicTextField + 6 个 chip，10+ 交互元素像数据看板
                // 折叠后每项仅 5-6 个元素（编号/勾选/标题/调度元信息/截止/展开按钮），回归手账节奏
                if (expandedItem == item) {
                    BasicTextField(
                        value = noteDraft,
                        onValueChange = {
                            noteDraft = it
                            onUpdateTargetNote(item, it)
                        },
                        textStyle = MaterialTheme.typography.labelSmall.copy(color = GoaldayDesign.adaptiveInkSecondary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(GoaldayDesign.RadiusS))
                            .background(GoaldayDesign.adaptiveSurface.copy(alpha = 0.25f))
                            .border(0.45.dp, GoaldayDesign.BorderColor.copy(alpha = 0.07f), RoundedCornerShape(GoaldayDesign.RadiusS))
                            .padding(horizontal = GoaldayDesign.Space2 - 1.dp, vertical = GoaldayDesign.Space1 + 1.dp),
                        decorationBox = { inner ->
                            if (noteDraft.isBlank()) {
                                Text("备注 / 做法 / 灵感", color = GoaldayDesign.adaptiveInkMuted, style = MaterialTheme.typography.labelSmall)
                            }
                            inner()
                        },
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.fillMaxWidth()) {
                        Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
                            TargetScheduleChip("今天") { onAddToSchedule(item, dateShortcuts.today) }
                            TargetScheduleChip("明天") { onAddToSchedule(item, dateShortcuts.tomorrow) }
                            TargetScheduleChip("周末") { onAddToSchedule(item, dateShortcuts.weekend) }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
                            TargetDeadlineChip("今", active = meta.deadlineDay == dateShortcuts.today) { onUpdateTargetDeadline(item, dateShortcuts.today) }
                            TargetDeadlineChip("明", active = meta.deadlineDay == dateShortcuts.tomorrow) { onUpdateTargetDeadline(item, dateShortcuts.tomorrow) }
                            TargetDeadlineChip("清除", active = false) { onUpdateTargetDeadline(item, null) }
                            if (item in customItems) {
                                TextActionButton("删除", GoaldayDesign.Danger) { onRemoveCustomItem(item) }
                            }
                        }
                        // P1-3：详情入口移到展开区内，避免常驻按钮干扰
                        TextActionButton("查看详情", GoaldayDesign.adaptiveInkSecondary) { onOpenTargetDetail(item) }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(GoaldayDesign.RadiusS))
                .background(GoaldayDesign.adaptiveSurface)
                .border(0.6.dp, GoaldayDesign.adaptiveDivider, RoundedCornerShape(GoaldayDesign.RadiusS))
                .padding(horizontal = GoaldayDesign.Space2, vertical = GoaldayDesign.Space1 + 2.dp),
            horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space2),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BasicTextField(
                value = draft,
                onValueChange = { draft = it },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodySmall.copy(color = GoaldayDesign.adaptiveInkPrimary),
                modifier = Modifier.weight(1f),
                decorationBox = { inner ->
                    if (draft.isBlank()) Text("新增一个目标", color = GoaldayDesign.adaptiveInkMuted, style = MaterialTheme.typography.bodySmall)
                    inner()
                },
            )
            Text("添加", color = GoaldayDesign.adaptiveInkSecondary, style = MaterialTheme.typography.labelMedium, modifier = Modifier.clickable {
                val text = draft.trim()
                if (text.isNotBlank()) {
                    onAddCustomItem(text)
                    draft = ""
                }
            })
        }
        // 对照逆向 activity_target_detail.xml：选中目标项后底部显示 46dp 操作栏
        selectedItem?.let { item ->
            TargetBottomActionBar(
                selectedItem = item,
                pageTitle = pageTitle,
                tint = tint,
                onComplete = {
                    if (!isChecked(pageTitle, item)) onToggleChecked(pageTitle, item)
                    selectedItem = null
                },
                onTop = {
                    // 置顶：把该项移到自定义列表最前（仅自定义项）
                    if (item in customItems) {
                        onRemoveCustomItem(item)
                        onAddCustomItem(item)
                    }
                    selectedItem = null
                },
                onDelete = {
                    if (item in customItems) onRemoveCustomItem(item)
                    selectedItem = null
                },
                onDatePick = {
                    onUpdateTargetDeadline(item, dateShortcuts.today)
                    selectedItem = null
                },
            )
        }
    }
}

// 对照逆向 activity_target_detail.xml cl_bottom_board：46dp 白底，日期标签 + 1dp 分隔线 + 删除/置顶/完成
@Composable
private fun TargetBottomActionBar(
    selectedItem: String,
    pageTitle: String,
    tint: Color,
    onComplete: () -> Unit,
    onTop: () -> Unit,
    onDelete: () -> Unit,
    onDatePick: () -> Unit,
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .background(Color.White),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 左侧：日期标签（对照 tv_date: 20sp, bg_target_detail_date, bgTint=#F6F6F6）
        Text(
            text = "日期",
            fontSize = 20.sp,
            color = GoaldayDesign.adaptiveInkPrimary,
            modifier = Modifier
                .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
                .background(GoaldayDesign.adaptiveSurface)
                .clickable { onDatePick() }
                .padding(horizontal = 10.dp),
        )
        // 右侧图标组：分隔线(1dp/22dp 自适应色) + 删除 + 置顶 + 完成（各 padding=13dp）
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(22.dp)
                    .background(GoaldayDesign.adaptiveDivider),
            )
            Icon(
                Icons.Filled.Delete,
                contentDescription = "删除",
                modifier = Modifier
                    .size(22.dp)
                    .padding(13.dp)
                    .clickable { onDelete() },
                tint = GoaldayDesign.Danger,
            )
            Icon(
                Icons.Filled.VerticalAlignTop,
                contentDescription = "置顶",
                modifier = Modifier
                    .size(22.dp)
                    .padding(13.dp)
                    .clickable { onTop() },
                tint = GoaldayDesign.adaptiveInkPrimary,
            )
            Icon(
                Icons.Filled.CheckCircle,
                contentDescription = "完成",
                modifier = Modifier
                    .size(22.dp)
                    .padding(13.dp)
                    .clickable { onComplete() },
                tint = GoaldayDesign.Positive,
            )
        }
    }
}

@Composable
private fun TargetLedgerSummary(
    total: Int,
    completed: Int,
    scheduled: Int,
    custom: Int,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(7.dp), modifier = Modifier.fillMaxWidth()) {
        TargetLedgerCell("待整理", (total - completed).coerceAtLeast(0).toString(), GoaldayDesign.RouteDiary, Modifier.weight(1f))
        TargetLedgerCell("已完成", completed.toString(), GoaldayDesign.Positive, Modifier.weight(1f))
        TargetLedgerCell("已排期", scheduled.toString(), GoaldayDesign.Pink, Modifier.weight(1f))
        TargetLedgerCell("自定义", custom.toString(), GoaldayDesign.RouteOverview, Modifier.weight(1f))
    }
}

@Composable
private fun TargetLedgerCell(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(GoaldayDesign.RadiusS))
            .background(color.copy(alpha = 0.11f))
            .border(0.7.dp, color.copy(alpha = 0.22f), RoundedCornerShape(GoaldayDesign.RadiusS))
            .padding(horizontal = GoaldayDesign.Space2 - 1.dp, vertical = GoaldayDesign.Space1 + 2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(value, color = color, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 1)
        Text(label, color = GoaldayDesign.adaptiveInkSecondary, style = MaterialTheme.typography.labelSmall, maxLines = 1)
    }
}

@Composable
private fun TargetProgressBar(
    completed: Int,
    total: Int,
    tint: Color,
) {
    val progress = if (total <= 0) 0f else completed.toFloat() / total.toFloat()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(7.dp)
            .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
            .background(GoaldayDesign.adaptiveSurface.copy(alpha = 0.27f)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
                .background(tint.copy(alpha = 0.72f)),
        )
    }
}

@Composable
private fun TargetScheduleMeta(entries: List<ScheduleEntry>) {
    val text = if (entries.isEmpty()) {
        "未排期"
    } else {
        entries.joinToString(" · ") { entry ->
            val time = entry.timeText.takeIf { it.isNotBlank() }?.let { " $it" }.orEmpty()
            "${entry.month}/${entry.day}$time"
        }
    }
    Text(
        text,
        color = if (entries.isEmpty()) GoaldayDesign.adaptiveInkMuted else GoaldayDesign.Positive,
        style = MaterialTheme.typography.labelSmall,
        maxLines = 1,
    )
}

@Composable
private fun TargetScheduleChip(
    label: String,
    onClick: () -> Unit,
) {
    Text(
        label,
        color = GoaldayDesign.Pink,
        style = MaterialTheme.typography.labelSmall,
        maxLines = 1,
        modifier = Modifier
            .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
            .background(GoaldayDesign.PinkTint)
            .clickable(onClick = onClick)
            .padding(horizontal = GoaldayDesign.Space1 + 2.dp, vertical = GoaldayDesign.Space1 - 1.dp),
    )
}

@Composable
private fun TargetDeadlineChip(
    label: String,
    active: Boolean,
    onClick: () -> Unit,
) {
    Text(
        label,
        color = if (active) Color.White else GoaldayDesign.adaptiveInkSecondary,
        style = MaterialTheme.typography.labelSmall,
        maxLines = 1,
        modifier = Modifier
            .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
            .background(if (active) GoaldayDesign.Positive else GoaldayDesign.adaptiveDivider)
            .clickable(onClick = onClick)
            .padding(horizontal = GoaldayDesign.Space1 + 2.dp, vertical = GoaldayDesign.Space1 - 1.dp),
    )
}

/**
 * 文字型操作按钮：统一处理点击热区（达 30dp+），避免裸 Text clickable 热区仅 20dp 的问题
 * 与 TargetScheduleChip 风格一致，但 background 透明，保持原视觉
 */
@Composable
private fun TextActionButton(
    text: String,
    color: Color,
    onClick: () -> Unit,
) {
    Text(
        text,
        color = color,
        style = MaterialTheme.typography.labelSmall,
        maxLines = 1,
        modifier = Modifier
            .clip(RoundedCornerShape(GoaldayDesign.RadiusS))
            .clickable(onClick = onClick)
            .padding(horizontal = GoaldayDesign.Space2, vertical = GoaldayDesign.Space1 + 1.dp),
    )
}
