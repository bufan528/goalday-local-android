package com.bf410.goaldaylocal.ui.replica

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp

data class BoardTask(
    val id: String,
    val title: String,
    val subtitle: String = "",
    val completed: Boolean = false,
)

@Composable
fun DualLaneExecutionBoard(
    modifier: Modifier = Modifier,
    leftHeader: String,
    rightHeader: String,
    dayLabels: List<Pair<String, String>>,
    leftTimelineTasks: List<String>,
    todayTasks: List<BoardTask>,
    poolTasks: List<BoardTask>,
    donePreviewTasks: List<BoardTask>,
    selectedTaskId: String?,
    onSelectTask: (String) -> Unit,
    onTimelineRowClick: ((Int) -> Unit)? = null,
    onActionDone: (BoardTask) -> Unit,
    onActionAdd: (BoardTask) -> Unit,
    onActionRestore: (BoardTask) -> Unit,
    topActions: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(472.dp)
            .background(GoaldayDesign.Surface, RoundedCornerShape(GoaldayDesign.RadiusL))
            .border(1.dp, Color(0x14000000), RoundedCornerShape(GoaldayDesign.RadiusL)),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GoaldayDesign.SurfaceSoft)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(leftHeader, color = GoaldayDesign.InkPrimary, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                Text("已完成", color = GoaldayDesign.InkSecondary, style = MaterialTheme.typography.labelSmall)
            }
            dayLabels.take(7).forEachIndexed { index, day ->
                val dayTask = leftTimelineTasks.getOrNull(index).orEmpty()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .border(0.5.dp, Color(0x12000000))
                        .clickable(enabled = onTimelineRowClick != null) { onTimelineRowClick?.invoke(index) }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.width(30.dp)) {
                            Text(day.first, fontWeight = FontWeight.SemiBold, color = GoaldayDesign.InkPrimary)
                            Text(day.second, style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.InkSecondary)
                        }
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = if (dayTask.isNotBlank()) GoaldayDesign.Positive else Color(0xFFE0D7CD),
                        )
                    }
                    Text(dayTask, style = MaterialTheme.typography.bodySmall, color = if (dayTask.isBlank()) GoaldayDesign.InkMuted else GoaldayDesign.InkPrimary, maxLines = 2)
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1.08f)
                .fillMaxHeight()
                .border(1.dp, Color(0x12000000)),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    rightHeader,
                    modifier = Modifier
                        .background(Color.White, RoundedCornerShape(GoaldayDesign.RadiusS))
                        .padding(horizontal = 7.dp, vertical = 3.dp),
                    style = MaterialTheme.typography.labelMedium,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically, content = topActions)
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text("今日 Todo", color = GoaldayDesign.InkSecondary, style = MaterialTheme.typography.labelSmall)
                todayTasks.forEach { task ->
                    BoardRow(task = task, selected = selectedTaskId == task.id, actionIcon = Icons.Filled.Check, onSelect = { onSelectTask(task.id) }, onAction = { onActionDone(task) })
                }
                Text("任务池", color = GoaldayDesign.InkSecondary, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 2.dp))
                poolTasks.forEach { task ->
                    BoardRow(task = task, selected = selectedTaskId == task.id, actionIcon = Icons.Filled.Add, onSelect = { onSelectTask(task.id) }, onAction = { onActionAdd(task) })
                }
                Text("已完成", color = GoaldayDesign.Positive, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 2.dp))
                donePreviewTasks.forEach { task ->
                    BoardRow(task = task, selected = selectedTaskId == task.id, actionIcon = Icons.Filled.Restore, onSelect = { onSelectTask(task.id) }, onAction = { onActionRestore(task) }, completed = true)
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
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (selected) Color(0x18B08963) else Color.Transparent, RoundedCornerShape(GoaldayDesign.RadiusS))
            .padding(horizontal = 6.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            if (selected) Icons.Filled.RadioButtonChecked else if (completed) Icons.Filled.Check else Icons.Filled.RadioButtonUnchecked,
            contentDescription = null,
            modifier = Modifier.size(12.dp),
            tint = if (completed) GoaldayDesign.Positive else if (selected) GoaldayDesign.InkSecondary else Color(0xFFD8CFC5),
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .clickable { onSelect() },
        ) {
            Text(
                task.title,
                color = GoaldayDesign.InkPrimary,
                textDecoration = if (completed || task.completed) TextDecoration.LineThrough else TextDecoration.None,
                maxLines = 2,
            )
            if (task.subtitle.isNotBlank()) {
                Text(task.subtitle, style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.InkSecondary, maxLines = 1)
            }
        }
        Icon(
            actionIcon,
            contentDescription = null,
            modifier = Modifier
                .size(16.dp)
                .clickable { onAction() },
            tint = GoaldayDesign.InkSecondary,
        )
    }
}
