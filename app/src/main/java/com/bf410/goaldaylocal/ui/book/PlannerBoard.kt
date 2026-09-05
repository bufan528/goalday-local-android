package com.bf410.goaldaylocal.ui.book

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.CancellationSignal
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.print.PageRange
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SubdirectoryArrowRight
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bf410.goaldaylocal.R
import com.bf410.goaldaylocal.data.BookPage
import com.bf410.goaldaylocal.data.DiaryPage
import com.bf410.goaldaylocal.data.PlanPage
import com.bf410.goaldaylocal.data.ScheduleEntry
import com.bf410.goaldaylocal.data.SchedulePage
import com.bf410.goaldaylocal.data.TargetItemMeta
import com.bf410.goaldaylocal.data.TargetPage
import com.bf410.goaldaylocal.ui.replica.BoardTask
import com.bf410.goaldaylocal.ui.replica.DualLaneExecutionBoard
import com.bf410.goaldaylocal.ui.replica.ExecutionBoardHeader
import com.bf410.goaldaylocal.ui.replica.GoaldayDesign
import com.bf410.goaldaylocal.ui.replica.TimelineTask
import com.tencent.mmkv.MMKV
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.delay
import java.io.File
import java.io.FileOutputStream

// 对照逆向 fragment_plan.xml：右下角独立浮动按钮，添加按钮在上(marginBottom=93dp)，提示按钮在下(marginBottom=32dp)
@Composable
internal fun PlannerFloatingActionStrip(
    onAdd: () -> Unit,
    onTip: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxHeight(),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.Bottom,
    ) {
        // 添加按钮：bg_plan_menu 背景 tint=#E5DAD4(mi色)
        Box(
            modifier = Modifier
                .padding(end = 20.dp, bottom = 93.dp)
                .size(46.dp)
                .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
                .background(GoaldayDesign.TabBarBg)
                .clickable { onAdd() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Filled.Add,
                contentDescription = "添加",
                tint = GoaldayDesign.adaptiveInkPrimary,
                modifier = Modifier.size(24.dp),
            )
        }
        // 提示/切换清单按钮：bg_plan_menu 背景 tint=黑色
        Box(
            modifier = Modifier
                .padding(end = 20.dp, bottom = 32.dp)
                .size(46.dp)
                .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
                .background(GoaldayDesign.adaptiveInkPrimary)
                .clickable { onTip() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Filled.Lightbulb,
                contentDescription = "切换清单",
                tint = GoaldayDesign.adaptivePaper,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
internal fun PlannerLedgerSummary(
    sourceCount: Int,
    todayCount: Int,
    doneCount: Int,
    scheduledCount: Int,
    tint: Color,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space2 - 1.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        PlannerLedgerCell("任务池", sourceCount, tint, Modifier.weight(1f))
        PlannerLedgerCell("今日", todayCount, GoaldayDesign.Pink, Modifier.weight(1f))
        PlannerLedgerCell("已完成", doneCount, GoaldayDesign.Positive, Modifier.weight(1f))
        PlannerLedgerCell("日程", scheduledCount, GoaldayDesign.RouteOverview, Modifier.weight(1f))
    }
}

@Composable
private fun PlannerLedgerCell(
    label: String,
    value: Int,
    color: Color,
    modifier: Modifier = Modifier,
) {
    // 统一为 Column（value 上 label 下），与 TargetLedgerCell 视觉语言一致，便于扫读数字
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(GoaldayDesign.RadiusS))
            .background(color.copy(alpha = 0.11f))
            .border(GoaldayDesign.Hairline, color.copy(alpha = 0.22f), RoundedCornerShape(GoaldayDesign.RadiusS))
            .padding(horizontal = GoaldayDesign.Space2 - 1.dp, vertical = GoaldayDesign.Space1 + 2.dp),
        verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space1 / 2),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(value.toString(), color = color, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 1)
        Text(label, color = GoaldayDesign.adaptiveInkSecondary, style = MaterialTheme.typography.labelSmall, maxLines = 1)
    }
}

@Composable
internal fun ReferencePlannerBoard(
    sourceItems: List<String>,
    todayItems: List<String>,
    doneItems: List<String>,
    schedulePreviewEntries: List<ScheduleEntry>,
    selectedListName: String,
    onSwitchList: () -> Unit,
    onMoveItemToToday: (String) -> Unit,
    onMoveItemToCompleted: (String) -> Unit,
    onRestoreItemFromDone: (String) -> Unit,
    onEditTask: (String, String) -> Unit = { _, _ -> },
    onDeleteTask: (String) -> Unit = {},
    // 时间窗口参数化：默认今天+7天，调用方可传入任意起点实现月历联动
    windowStart: LocalDate = LocalDate.now(),
) {
    var editingTask by remember { mutableStateOf<String?>(null) }
    val weekDates = (0..6).map { windowStart.plusDays(it.toLong()) }
    val weekday = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
    val dayLabels = weekDates.map { date ->
        date.dayOfMonth.toString() to weekday[(date.dayOfWeek.value - 1).coerceIn(0, 6)]
    }
    val scheduleByDay = weekDates.associateWith { date ->
        schedulePreviewEntries
            .filter { entry ->
                entry.year == date.year &&
                    entry.month == date.monthValue &&
                    entry.day == date.dayOfMonth
            }
            .sortedWith(
                compareBy<ScheduleEntry> { it.completed }
                    .thenBy { it.title.length }
                    .thenBy { it.title },
            )
    }
    val leftItems = weekDates.map { date ->
        val entries = scheduleByDay[date].orEmpty()
        if (entries.isEmpty()) TimelineTask("", false) else TimelineTask(entries.first().title, entries.first().completed)
    }
    val todayPool = todayItems.distinct().take(6).map { BoardTask(id = "today_$it", title = it) }
    val poolSource = sourceItems.filterNot { it in todayItems }.distinct().take(8).map { BoardTask(id = "pool_$it", title = it) }
    val donePreview = doneItems.take(3).map { BoardTask(id = "done_$it", title = it, completed = true) }
    val allRight = (todayPool + poolSource + donePreview)
    var selectedId by remember(allRight) { mutableStateOf(allRight.firstOrNull()?.id) }

    if (editingTask != null) {
        RenameTaskDialog(
            initial = editingTask!!,
            onDismiss = { editingTask = null },
            onConfirm = { newName ->
                onEditTask(editingTask!!, newName)
                editingTask = null
            },
        )
    }
    DualLaneExecutionBoard(
        leftHeader = "执行",
        rightHeader = selectedListName,
        dayLabels = dayLabels,
        leftTimelineTasks = leftItems,
        todayTasks = todayPool,
        poolTasks = poolSource,
        donePreviewTasks = donePreview,
        selectedTaskId = selectedId,
        onSelectTask = { selectedId = it },
        onActionDone = { onMoveItemToCompleted(it.title) },
        onActionAdd = { onMoveItemToToday(it.title) },
        onActionRestore = { onRestoreItemFromDone(it.title) },
        onEditTask = { editingTask = it.title },
        onDeleteTask = { onDeleteTask(it.title) },
        topActions = {
            Text("切换", color = GoaldayDesign.adaptiveInkMuted, style = MaterialTheme.typography.labelSmall, maxLines = 1, modifier = Modifier
                .clip(RoundedCornerShape(GoaldayDesign.RadiusS))
                .clickable(onClick = onSwitchList)
                .padding(horizontal = GoaldayDesign.Space2, vertical = GoaldayDesign.Space1 + 1.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space1),
                modifier = Modifier
                    .clip(RoundedCornerShape(GoaldayDesign.RadiusS))
                    .clickable { allRight.firstOrNull { it.id == selectedId }?.let { onRestoreItemFromDone(it.title) } }
                    .padding(horizontal = GoaldayDesign.Space2, vertical = GoaldayDesign.Space1 + 1.dp),
            ) {
                Icon(
                    Icons.Filled.Restore,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = GoaldayDesign.adaptiveInkSecondary,
                )
                Text(
                    "回收",
                    color = GoaldayDesign.adaptiveInkSecondary,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space1),
                modifier = Modifier
                    .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
                    .background(GoaldayDesign.PrimaryAction)
                    .clickable { allRight.firstOrNull { it.id == selectedId }?.let { onMoveItemToCompleted(it.title) } }
                    .padding(horizontal = GoaldayDesign.Space2, vertical = GoaldayDesign.Space1 - 1.dp),
            ) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = Color.White,
                )
                Text(
                    "完成",
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        },
    )
}

@Composable
private fun RenameTaskDialog(
    initial: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var text by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("重命名任务") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("任务名称") },
                singleLine = true,
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(text.trim()) }) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
    )
}

