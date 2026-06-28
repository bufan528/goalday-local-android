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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.bf410.goaldaylocal.data.ScheduleEntry
import com.bf410.goaldaylocal.data.TargetItemMeta
import com.bf410.goaldaylocal.data.TargetPage
import com.bf410.goaldaylocal.ui.replica.GoaldayDesign
import java.time.LocalDate

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
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(GoaldayDesign.RadiusL))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFFFFFCF7), tint.copy(alpha = 0.13f), Color(0xFFFFFEFB)),
                    start = Offset.Zero,
                    end = Offset(760f, 900f),
                ),
            )
            .border(0.8.dp, Color(0x1FA88966), RoundedCornerShape(GoaldayDesign.RadiusL))
            .graphicsLayer {
                translationX = contentShift
                this.alpha = alpha
            }
            .padding(12.dp),
    ) {
        HandbookPaperRuling()
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SectionStamp("TARGET", tint)
                Text("${pageIndex + 1}/$pageCount", style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.InkMuted)
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
            )
        }
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
) {
    val items = remember(baseItems, customItems) { (baseItems + customItems).distinct() }
    var draft by remember(pageTitle) { mutableStateOf("") }
    var editingItem by remember(pageTitle) { mutableStateOf<String?>(null) }
    var editingText by remember(pageTitle) { mutableStateOf("") }
    val todayDay = LocalDate.now().dayOfMonth
    val tomorrowDay = todayDay + 1
    val weekendDay = todayDay + (7 - LocalDate.now().dayOfWeek.value).coerceAtLeast(1)
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
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(104.dp)
                .clip(RoundedCornerShape(GoaldayDesign.RadiusM))
                .background(
                    Brush.linearGradient(
                        listOf(tint.copy(alpha = 0.86f), tint.copy(alpha = 0.48f), Color.White.copy(alpha = 0.34f)),
                        start = Offset.Zero,
                        end = Offset(760f, 460f),
                    ),
                )
                .border(0.8.dp, Color(0x33FFFFFF), RoundedCornerShape(GoaldayDesign.RadiusM))
                .padding(14.dp),
        ) {
            Column(
                modifier = Modifier.align(Alignment.BottomStart),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(pageTitle, style = MaterialTheme.typography.titleLarge, color = GoaldayDesign.InkPrimary, fontWeight = FontWeight.SemiBold)
                Text("目标档案 · $completedCount/${items.size} 完成 · $scheduledCount 已排期", style = MaterialTheme.typography.bodySmall, color = GoaldayDesign.InkSecondary)
                TargetProgressBar(
                    completed = completedCount,
                    total = items.size,
                    tint = GoaldayDesign.Positive,
                )
            }
        }
        TargetLedgerSummary(
            total = items.size,
            completed = completedCount,
            scheduled = scheduledCount,
            custom = customItems.size,
        )

        items.forEachIndexed { index, item ->
            val checked = isChecked(pageTitle, item)
            val scheduledEntries = scheduledByTitle[item].orEmpty()
            val meta = targetItemMeta[item] ?: TargetItemMeta()
            var noteDraft by remember(item, meta.note) { mutableStateOf(meta.note) }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(GoaldayDesign.RadiusM))
                    .background(if (checked) GoaldayDesign.GreenSoft else Color(0xFFFFFEFC))
                    .border(0.8.dp, if (checked) GoaldayDesign.Positive.copy(alpha = 0.35f) else Color(0x14000000), RoundedCornerShape(GoaldayDesign.RadiusM))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(9.dp), verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            "%02d".format(index + 1),
                            color = if (checked) Color.White else GoaldayDesign.Pink,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(99.dp))
                                .background(if (checked) GoaldayDesign.Positive else Color(0x18E88FAE))
                                .padding(horizontal = 7.dp, vertical = 4.dp),
                        )
                        Text(
                            if (checked) "✓" else "□",
                            color = if (checked) GoaldayDesign.Positive else GoaldayDesign.InkMuted,
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.clickable { onToggleChecked(pageTitle, item) },
                        )
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Text("目标档案", color = GoaldayDesign.InkMuted, style = MaterialTheme.typography.labelSmall)
                            Row(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("详情", color = GoaldayDesign.InkSecondary, style = MaterialTheme.typography.labelSmall, modifier = Modifier.clickable { onOpenTargetDetail(item) })
                                Text("排入", color = GoaldayDesign.Pink, style = MaterialTheme.typography.labelSmall, modifier = Modifier.clickable { onAddToSchedule(item, todayDay) })
                            }
                        }
                        if (editingItem == item) {
                            BasicTextField(
                                value = editingText,
                                onValueChange = { editingText = it },
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodySmall.copy(color = GoaldayDesign.InkPrimary),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = {
                                    if (item in customItems) onRenameCustomItem(item, editingText)
                                    editingItem = null
                                }),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0x08000000), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 7.dp, vertical = 5.dp),
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("保存", color = GoaldayDesign.Pink, style = MaterialTheme.typography.labelSmall, modifier = Modifier.clickable {
                                    if (item in customItems) onRenameCustomItem(item, editingText)
                                    editingItem = null
                                })
                                Text("取消", color = GoaldayDesign.InkMuted, style = MaterialTheme.typography.labelSmall, modifier = Modifier.clickable { editingItem = null })
                            }
                        } else {
                            Text(
                                item,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (checked) GoaldayDesign.InkSecondary else GoaldayDesign.InkPrimary,
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
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            TargetScheduleMeta(entries = scheduledEntries)
                            meta.deadlineDay?.let {
                                Text("截止 ${it}日", color = GoaldayDesign.Positive, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
                BasicTextField(
                    value = noteDraft,
                    onValueChange = {
                        noteDraft = it
                        onUpdateTargetNote(item, it)
                    },
                    textStyle = MaterialTheme.typography.labelSmall.copy(color = GoaldayDesign.InkSecondary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(GoaldayDesign.RadiusS))
                        .background(Color(0x40FFFFFF))
                        .border(0.45.dp, Color(0x12A88966), RoundedCornerShape(GoaldayDesign.RadiusS))
                        .padding(horizontal = 7.dp, vertical = 5.dp),
                    decorationBox = { inner ->
                        if (noteDraft.isBlank()) {
                            Text("备注 / 做法 / 灵感", color = GoaldayDesign.InkMuted, style = MaterialTheme.typography.labelSmall)
                        }
                        inner()
                    },
                )
                Column(verticalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.fillMaxWidth()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
                        TargetScheduleChip("今天") { onAddToSchedule(item, todayDay) }
                        TargetScheduleChip("明天") { onAddToSchedule(item, tomorrowDay) }
                        TargetScheduleChip("周末") { onAddToSchedule(item, weekendDay) }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
                        TargetDeadlineChip("今", active = meta.deadlineDay == todayDay) { onUpdateTargetDeadline(item, todayDay) }
                        TargetDeadlineChip("明", active = meta.deadlineDay == tomorrowDay) { onUpdateTargetDeadline(item, tomorrowDay) }
                        TargetDeadlineChip("清除", active = false) { onUpdateTargetDeadline(item, null) }
                        if (item in customItems) {
                            Text("删除", color = GoaldayDesign.Danger, style = MaterialTheme.typography.labelSmall, modifier = Modifier.clickable { onRemoveCustomItem(item) })
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(GoaldayDesign.RadiusS))
                .background(Color(0xFFFFFEFC))
                .border(0.6.dp, Color(0x12000000), RoundedCornerShape(GoaldayDesign.RadiusS))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BasicTextField(
                value = draft,
                onValueChange = { draft = it },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodySmall.copy(color = GoaldayDesign.InkPrimary),
                modifier = Modifier.weight(1f),
                decorationBox = { inner ->
                    if (draft.isBlank()) Text("新增一个目标", color = GoaldayDesign.InkMuted, style = MaterialTheme.typography.bodySmall)
                    inner()
                },
            )
            Text("添加", color = GoaldayDesign.Pink, style = MaterialTheme.typography.labelMedium, modifier = Modifier.clickable {
                val text = draft.trim()
                if (text.isNotBlank()) {
                    onAddCustomItem(text)
                    draft = ""
                }
            })
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
        TargetLedgerCell("待整理", (total - completed).coerceAtLeast(0).toString(), Color(0xFFB07A8F), Modifier.weight(1f))
        TargetLedgerCell("已完成", completed.toString(), GoaldayDesign.Positive, Modifier.weight(1f))
        TargetLedgerCell("已排期", scheduled.toString(), GoaldayDesign.Pink, Modifier.weight(1f))
        TargetLedgerCell("自定义", custom.toString(), Color(0xFF8F684F), Modifier.weight(1f))
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
            .padding(horizontal = 7.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(value, color = color, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 1)
        Text(label, color = GoaldayDesign.InkSecondary, style = MaterialTheme.typography.labelSmall, maxLines = 1)
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
            .clip(RoundedCornerShape(99.dp))
            .background(Color(0x44FFFFFF)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .clip(RoundedCornerShape(99.dp))
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
        color = if (entries.isEmpty()) GoaldayDesign.InkMuted else GoaldayDesign.Positive,
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
            .clip(RoundedCornerShape(99.dp))
            .background(Color(0x18E88FAE))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 3.dp),
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
        color = if (active) Color.White else GoaldayDesign.InkSecondary,
        style = MaterialTheme.typography.labelSmall,
        maxLines = 1,
        modifier = Modifier
            .clip(RoundedCornerShape(99.dp))
            .background(if (active) GoaldayDesign.Positive else Color(0x14000000))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 3.dp),
    )
}
