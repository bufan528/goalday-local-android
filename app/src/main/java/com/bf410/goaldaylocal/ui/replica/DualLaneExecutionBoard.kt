package com.bf410.goaldaylocal.ui.replica

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

data class BoardTask(
    val id: String,
    val title: String,
    val subtitle: String = "",
    val completed: Boolean = false,
)

data class TimelineTask(
    val title: String,
    val completed: Boolean = false,
)

@Composable
fun DualLaneExecutionBoard(
    modifier: Modifier = Modifier,
    leftHeader: String,
    rightHeader: String,
    dayLabels: List<Pair<String, String>>,
    leftTimelineTasks: List<TimelineTask>,
    todayTasks: List<BoardTask>,
    poolTasks: List<BoardTask>,
    donePreviewTasks: List<BoardTask>,
    selectedTaskId: String?,
    onSelectTask: (String) -> Unit,
    onTimelineRowClick: ((Int) -> Unit)? = null,
    onActionDone: (BoardTask) -> Unit,
    onActionAdd: (BoardTask) -> Unit,
    onActionRestore: (BoardTask) -> Unit,
    onEditTask: (BoardTask) -> Unit = {},
    onDeleteTask: (BoardTask) -> Unit = {},
    topActions: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(472.dp)
            .background(GoaldayDesign.adaptiveSurface, RoundedCornerShape(GoaldayDesign.RadiusL))
            .border(GoaldayDesign.Hairline, GoaldayDesign.adaptiveDivider, RoundedCornerShape(GoaldayDesign.RadiusL)),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GoaldayDesign.adaptiveSurfaceSoft)
                    .padding(horizontal = GoaldayDesign.Space2, vertical = GoaldayDesign.Space1 + 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(leftHeader, color = GoaldayDesign.adaptiveInkPrimary, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                Text("已完成", color = GoaldayDesign.adaptiveInkSecondary, style = MaterialTheme.typography.labelSmall)
            }
            dayLabels.take(7).forEachIndexed { index, day ->
                val dayTask = leftTimelineTasks.getOrNull(index)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .border(GoaldayDesign.Hairline, GoaldayDesign.adaptiveDivider)
                        .clickable(enabled = onTimelineRowClick != null) { onTimelineRowClick?.invoke(index) }
                        .padding(horizontal = GoaldayDesign.Space2, vertical = GoaldayDesign.Space1 + 2.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space2), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.width(30.dp)) {
                            Text(day.first, fontWeight = FontWeight.SemiBold, color = GoaldayDesign.adaptiveInkPrimary)
                            Text(day.second, style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.adaptiveInkSecondary)
                        }
                        Icon(
                            if (dayTask?.completed == true) Icons.Filled.Check else Icons.Filled.RadioButtonUnchecked,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = when {
                                dayTask == null -> GoaldayDesign.Clay
                                dayTask.completed -> GoaldayDesign.Positive
                                else -> GoaldayDesign.adaptiveInkSecondary
                            },
                        )
                    }
                    Text(
                        dayTask?.title ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (dayTask == null || dayTask.title.isBlank()) GoaldayDesign.adaptiveInkMuted else GoaldayDesign.adaptiveInkPrimary,
                        maxLines = 2,
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1.08f)
                .fillMaxHeight()
                .border(GoaldayDesign.Hairline, GoaldayDesign.adaptiveDivider),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = GoaldayDesign.Space2, vertical = GoaldayDesign.Space2),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    rightHeader,
                    modifier = Modifier
                        .background(GoaldayDesign.adaptiveSurface, RoundedCornerShape(GoaldayDesign.RadiusS))
                        .padding(horizontal = GoaldayDesign.Space2 - 1.dp, vertical = GoaldayDesign.Space1 - 1.dp),
                    style = MaterialTheme.typography.labelMedium,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space2), verticalAlignment = Alignment.CenterVertically, content = topActions)
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = GoaldayDesign.Space2),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text("今日待办", color = GoaldayDesign.adaptiveInkSecondary, style = MaterialTheme.typography.labelSmall)
                todayTasks.forEach { task ->
                    BoardRow(
                        task = task,
                        selected = selectedTaskId == task.id,
                        actionIcon = Icons.Filled.Check,
                        onSelect = { onSelectTask(task.id) },
                        onAction = { onActionDone(task) },
                        onEdit = { onEditTask(task) },
                        onDelete = { onDeleteTask(task) },
                    )
                }
                Text("任务池", color = GoaldayDesign.adaptiveInkSecondary, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 2.dp))
                poolTasks.forEach { task ->
                    BoardRow(
                        task = task,
                        selected = selectedTaskId == task.id,
                        actionIcon = Icons.Filled.Add,
                        onSelect = { onSelectTask(task.id) },
                        onAction = { onActionAdd(task) },
                        onEdit = { onEditTask(task) },
                        onDelete = { onDeleteTask(task) },
                    )
                }
                Text("已完成", color = GoaldayDesign.Positive, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 2.dp))
                donePreviewTasks.forEach { task ->
                    BoardRow(
                        task = task,
                        selected = selectedTaskId == task.id,
                        actionIcon = Icons.Filled.Restore,
                        onSelect = { onSelectTask(task.id) },
                        onAction = { onActionRestore(task) },
                        onEdit = { onEditTask(task) },
                        onDelete = { onDeleteTask(task) },
                        completed = true,
                    )
                }
            }
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun BoardRow(
    task: BoardTask,
    selected: Boolean,
    actionIcon: ImageVector,
    completed: Boolean = false,
    onSelect: () -> Unit,
    onAction: () -> Unit,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
) {
    // 对照逆向 item_plan_item.xml：SwipeRevealLayout 左滑露出编辑(黑底)+删除(#ED8888)按钮
    var swipeOffset by remember(task.id) { mutableStateOf(0f) }
    val density = LocalDensity.current
    val revealWidth = with(density) { 100.dp.toPx() }
    val clampedOffset = swipeOffset.coerceIn(-revealWidth, 0f)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(GoaldayDesign.RadiusS)),
    ) {
        // 底层：编辑 + 删除按钮（右对齐，左侧滑出，各 50dp 宽）
        if (onEdit != null || onDelete != null) {
            Row(
                modifier = Modifier
                    .matchParentSize()
                    .background(GoaldayDesign.adaptiveSurface),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (onEdit != null) {
                    Box(
                        modifier = Modifier
                            .width(50.dp)
                            .fillMaxHeight()
                            .background(Color.Black)
                            .clickable {
                                swipeOffset = 0f
                                onEdit()
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = "编辑", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
                if (onDelete != null) {
                    Box(
                        modifier = Modifier
                            .width(50.dp)
                            .fillMaxHeight()
                            .background(GoaldayDesign.Danger)
                            .clickable {
                                swipeOffset = 0f
                                onDelete()
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Filled.Delete, contentDescription = "删除", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
        // 上层：内容卡片，可拖动。对照 item_plan_item.xml：圆点 10dp + 文本 16sp + 计数 14sp，minHeight 49dp
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(clampedOffset.roundToInt(), 0) }
                .background(if (selected) GoaldayDesign.BorderColor.copy(alpha = 0.09f) else GoaldayDesign.adaptiveSurface)
                .draggable(
                    orientation = Orientation.Horizontal,
                    enabled = onEdit != null || onDelete != null,
                    state = rememberDraggableState { delta ->
                        swipeOffset = (swipeOffset + delta).coerceIn(-revealWidth, 0f)
                    },
                    onDragStopped = {
                        swipeOffset = if (swipeOffset < -revealWidth / 2) -revealWidth else 0f
                    },
                )
                .padding(start = 15.dp, end = 14.dp)
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 对照 v_dot：10dp 圆点（bg_toolbar_plan_dot），选中时填充强调色
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
                    .background(
                        when {
                            completed -> GoaldayDesign.Positive
                            selected -> GoaldayDesign.Pink
                            else -> GoaldayDesign.MorandiCoral
                        }
                    ),
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSelect() }
                    .padding(vertical = 14.dp),
            ) {
                Text(
                    task.title,
                    color = if (completed || task.completed) GoaldayDesign.adaptiveInkMuted else GoaldayDesign.adaptiveInkPrimary,
                    textDecoration = if (completed || task.completed) TextDecoration.LineThrough else TextDecoration.None,
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 16.sp,
                    maxLines = 2,
                )
                if (task.subtitle.isNotBlank()) {
                    Text(task.subtitle, style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.adaptiveInkSecondary, fontSize = 14.sp, maxLines = 1)
                }
            }
            Icon(
                actionIcon,
                contentDescription = null,
                modifier = Modifier
                    .size(16.dp)
                    .clickable { onAction() },
                tint = GoaldayDesign.adaptiveInkSecondary,
            )
        }
    }
}
