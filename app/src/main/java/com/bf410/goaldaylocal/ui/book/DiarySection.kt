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

// 对照逆向 fragment_diary_inbook.xml：日期标签 + 内容区 + 底部图片按钮栏
@Composable
private fun HandbookDiaryReplicaPage(
    modifier: Modifier,
    title: String,
    prompt: String,
    tint: Color,
    diaryDraft: String,
    todayPlanItems: List<String>,
    todayCompletedItems: List<String>,
    pendingCommand: RichEditorCommand?,
    onCommand: (RichEditorCommand) -> Unit,
    onDiaryChange: (String) -> Unit,
    contentMode: PageContentMode,
    onContentModeChange: (PageContentMode) -> Unit,
    pageIndex: Int,
    pageCount: Int,
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
    val editing = contentMode is PageContentMode.EditingDiary && contentMode.title == title
    val today = LocalDate.now()
    // 对照 fragment_diary_inbook.xml fl_date：24dp 高，paddingStart/End=7.5dp，tv_date 12sp
    val diaryScrollState = rememberScrollState()
    Column(
        modifier = modifier
            .graphicsLayer {
                translationX = contentShift
                this.alpha = alpha
            }
            .fillMaxSize(),
    ) {
        // 日期标签行（对照 fl_date: visibility=2 GONE, paddingStart/End=7.5pt=16.67dp）
        // 逆向资源中默认隐藏,日期信息已在 DiaryInBookHeader 中显示
        // Row(
        //     modifier = Modifier
        //         .fillMaxWidth()
        //         .height(24.dp)
        //         .padding(horizontal = 10.dp),
        //     verticalAlignment = Alignment.Bottom,
        // ) {
        //     Text(
        //         "${today.monthValue}月${today.dayOfMonth}日 周${today.dayOfWeek.value}",
        //         color = GoaldayDesign.adaptiveInkPrimary,
        //         fontSize = 12.sp,
        //         fontWeight = FontWeight.Medium,
        //         maxLines = 1,
        //     )
        // }
        // 内容区（对照 rv_container: marginTop=5dp, marginStart/End=7.5pt → 10dp, marginBottom=30dp）
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(start = 10.dp, top = 5.dp, end = 10.dp, bottom = 30.dp)
                .handbookPaperRuling(diaryScrollState),
        ) {
            if (editing) {
                DiarySection(
                    title = title,
                    prompt = prompt,
                    tint = tint,
                    diaryDraft = diaryDraft,
                    todayPlanItems = todayPlanItems,
                    todayCompletedItems = todayCompletedItems,
                    pendingCommand = pendingCommand,
                    onCommand = onCommand,
                    onDiaryChange = onDiaryChange,
                    contentMode = contentMode,
                    onContentModeChange = onContentModeChange,
                    inBook = true,
                )
            } else {
                // 对照 item_diary_text.xml：16sp #2C2C2C，hint="点击输入"
                val plainText = plainTextFromHtml(diaryDraft).ifBlank { diaryDraft }
                if (plainText.isNotBlank()) {
                    // 对照 item_diary_text.xml：16sp 字体 + 2dp 行距 = 18sp lineHeight
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(diaryScrollState),
                    ) {
                        Text(
                            plainText,
                            fontSize = 16.sp,
                            color = GoaldayDesign.adaptiveInkPrimary,
                            lineHeight = 18.sp,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(diaryScrollState)
                            .clickable { onContentModeChange(PageContentMode.EditingDiary(title)) },
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            "点击输入",
                            fontSize = 16.sp,
                            color = GoaldayDesign.adaptiveInkMuted,
                        )
                    }
                }
            }
        }
        // 底部图片按钮栏（对照 fragment_diary_inbook.xml：fl_bottom_bar 23dip白底，
        // fl_select_pic 23dip，ic_select_pic 12.5dip，marginStart 3.75dip）
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(23.dp)
                .background(Color.White),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .padding(start = 3.75.dp)
                    .size(23.dp)
                    .clickable { onContentModeChange(PageContentMode.EditingDiary(title)) },
                contentAlignment = Alignment.Center,
            ) {
                // 对照 ic_select_pic：图片选择图标 12.5dip
                Image(
                    painter = painterResource(R.drawable.ic_select_pic),
                    contentDescription = "插入图片",
                    modifier = Modifier.size(12.5.dp),
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun EditableBulletPage(
    pageTitle: String,
    baseItems: List<String>,
    customItems: List<String>,
    tint: Color,
    inputLabel: String,
    isSchedulePage: Boolean,
    isChecked: (String, String) -> Boolean,
    onToggleChecked: (String, String) -> Unit,
    onAddCustomItem: (String) -> Unit,
    onAddCustomItemWithDeadline: (String, Int?) -> Unit,
    onRemoveCustomItem: (String) -> Unit,
    onRenameCustomItem: (String, String) -> Unit,
    onAddToSchedule: (String, Int) -> Unit,
    weeklyTheme: String,
    todayPlanItems: List<String>,
    todayCompletedItems: List<String>,
    schedulePreviewEntries: List<ScheduleEntry>,
    onWeeklyThemeChange: (String) -> Unit,
    onMoveItemToToday: (String) -> Unit,
    onMoveItemToCompleted: (String) -> Unit,
    onRestoreItemFromToday: (String) -> Unit,
    onRestoreItemFromCompleted: (String) -> Unit,
    contentMode: PageContentMode,
    onContentModeChange: (PageContentMode) -> Unit,
    // 时间窗口参数化：默认今天+7天，透传给 ReferencePlannerBoard 实现月历联动
    windowStart: LocalDate = LocalDate.now(),
) {
    val stagedItems = remember(todayPlanItems, todayCompletedItems) { (todayPlanItems + todayCompletedItems).toSet() }
    val sourceBaseItems = remember(baseItems, stagedItems) { baseItems.filterNot { it in stagedItems } }
    val sourceCustomItems = remember(customItems, stagedItems) { customItems.filterNot { it in stagedItems } }
    val sourceItems = sourceBaseItems + sourceCustomItems
    val listNames = remember { listOf("待办", "未来的自己", "奖励清单", "电影清单") }
    var selectedListIndex by remember(pageTitle) { mutableStateOf(0) }
    val shownSourceItems = remember(sourceItems, selectedListIndex) {
        when (selectedListIndex) {
            1 -> sourceItems.filter { it.contains("目标") || it.contains("学习") || it.contains("计划") || it.contains("未来") }
            2 -> sourceItems.filter { it.contains("奖励") || it.contains("完成") || it.contains("复盘") || it.contains("打卡") }
            3 -> sourceItems.filter { it.contains("电影") || it.contains("读书") || it.contains("阅读") || it.contains("TED") }
            else -> sourceItems
        }.ifEmpty { sourceItems }
    }

    Column(verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space2 + 2.dp)) {
        ExecutionBoardHeader(
            title = if (isSchedulePage) "日程执行板" else "任务执行板",
        )
        PlannerLedgerSummary(
            sourceCount = shownSourceItems.size,
            todayCount = todayPlanItems.size,
            doneCount = todayCompletedItems.size,
            scheduledCount = schedulePreviewEntries.count { !it.completed },
            tint = tint,
        )
        ReferencePlannerBoard(
            sourceItems = shownSourceItems,
            todayItems = todayPlanItems,
            doneItems = todayCompletedItems,
            schedulePreviewEntries = schedulePreviewEntries,
            selectedListName = listNames[selectedListIndex],
            onSwitchList = { selectedListIndex = (selectedListIndex + 1) % listNames.size },
            onMoveItemToToday = onMoveItemToToday,
            windowStart = windowStart,
            onMoveItemToCompleted = onMoveItemToCompleted,
            onRestoreItemFromDone = onRestoreItemFromCompleted,
            onEditTask = onRenameCustomItem,
            onDeleteTask = onRemoveCustomItem,
        )
        // 对照逆向 fragment_plan.xml：右下角添加 + 提示按钮
        PlannerFloatingActionStrip(
            onAdd = { onAddCustomItem("") },
            onTip = { selectedListIndex = (selectedListIndex + 1) % listNames.size },
        )
    }
}


@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun DiarySection(
    title: String,
    prompt: String,
    tint: Color,
    diaryDraft: String,
    todayPlanItems: List<String>,
    todayCompletedItems: List<String>,
    pendingCommand: RichEditorCommand?,
    onCommand: (RichEditorCommand) -> Unit,
    onDiaryChange: (String) -> Unit,
    contentMode: PageContentMode,
    onContentModeChange: (PageContentMode) -> Unit,
    inBook: Boolean = false,
) {
    val editingDiary = contentMode as? PageContentMode.EditingDiary
    var structured by remember(title, diaryDraft) { mutableStateOf(StructuredDiary.fromRaw(diaryDraft)) }
    // 那年今日闪回：基于当前日记日期查询往年同月同日记录
    val bookViewModel: BookViewModel = viewModel(factory = BookViewModel.Factory)
    val onThisDayFlashbacks = remember(structured.date, diaryDraft) {
        bookViewModel.loadOnThisDayFor(structured.date)
    }
    var expandedFlashback by remember(title) { mutableStateOf<OnThisDayDiary?>(null) }
    var exportHint by remember(title) { mutableStateOf("") }
    var showDatePicker by remember(title) { mutableStateOf(false) }
    var longImagePreview by remember(title) { mutableStateOf<LongImagePreview?>(null) }
    val context = LocalContext.current
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = structured.date.toEpochMillis())
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            structured = structured.withImageUri(uri.toString())
            onDiaryChange(structured.toRaw())
            onContentModeChange(PageContentMode.EditingDiary(title))
        }
    }
    fun currentDiaryState(): StructuredDiary =
        StructuredDiary.fromRaw(diaryDraft).let { saved ->
            if (editingDiary?.title == title) structured else saved
        }
    fun beginDiaryEditing(nextState: StructuredDiary = currentDiaryState()) {
        structured = nextState
        onDiaryChange(nextState.toRaw())
        onContentModeChange(PageContentMode.EditingDiary(title))
    }
    fun applyLinkedTarget(item: String, completed: Boolean) {
        structured = if (completed) {
            structured.withCompletedTarget(item).withTargetBlock(item, completed = true)
        } else {
            structured.withWorkTarget(item).withTargetBlock(item, completed = false)
        }
        onDiaryChange(structured.toRaw())
        onContentModeChange(PageContentMode.EditingDiary(title))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(GoaldayDesign.RadiusXL))
            .background(GoaldayDesign.CardPaperGradient)
            .border(GoaldayDesign.Hairline, GoaldayDesign.BorderColor.copy(alpha = 0.14f), RoundedCornerShape(GoaldayDesign.RadiusXL))
            .padding(horizontal = GoaldayDesign.Space2 + 2.dp, vertical = GoaldayDesign.Space2 + 2.dp),
        verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space2 + 1.dp),
    ) {
        if (onThisDayFlashbacks.isNotEmpty()) {
            OnThisDayFlashbackStrip(
                flashbacks = onThisDayFlashbacks,
                onClick = { expandedFlashback = it },
            )
        }
        DiaryWorkspaceHeader(
            title = title,
            prompt = prompt,
            state = currentDiaryState(),
            todoCount = todayPlanItems.size,
            doneCount = todayCompletedItems.size,
            editing = editingDiary?.title == title,
            onEdit = { beginDiaryEditing() },
            onPickDate = { showDatePicker = true },
        )
        DiaryBlockRail(
            state = currentDiaryState(),
            todoItems = todayPlanItems,
            doneItems = todayCompletedItems,
            editing = editingDiary?.title == title,
        )
        DiaryLinkedTargetStrip(
            doneItems = todayCompletedItems,
            todoItems = todayPlanItems,
            onPickDone = { applyLinkedTarget(it, true) },
            onPickTodo = { applyLinkedTarget(it, false) },
        )
        if (editingDiary?.title == title) {
            StructuredDiaryEditor(
                state = structured,
                onStateChange = { structured = it },
                onPickDate = { showDatePicker = true },
                onAddImage = { imagePicker.launch(arrayOf("image/*")) },
                onAddTextBlock = {
                    structured = structured.withTextBlock()
                    onDiaryChange(structured.toRaw())
                },
                onAddTopicTargetBlock = {
                    structured = structured.withTopicTargetBlock()
                    onDiaryChange(structured.toRaw())
                },
                onRemoveImage = { uri ->
                    structured = structured.withoutImageUri(uri)
                    onDiaryChange(structured.toRaw())
                },
                pendingCommand = pendingCommand,
                onCommand = onCommand,
                onDone = {
                    onDiaryChange(structured.toRaw())
                    onContentModeChange(PageContentMode.Browsing)
                },
                isInBook = true,
            )
        } else {
            val previewState = currentDiaryState()
            if (!previewState.hasUserContent) {
                DiaryStartPanel(
                    todoCount = todayPlanItems.size,
                    doneCount = todayCompletedItems.size,
                    onStart = { beginDiaryEditing(previewState.withTextBlock("")) },
                    onAddImage = {
                        structured = previewState
                        onContentModeChange(PageContentMode.EditingDiary(title))
                        imagePicker.launch(arrayOf("image/*"))
                    },
                    onAddTarget = { beginDiaryEditing(previewState.withTopicTargetBlock("")) },
                )
            }
            DiaryQuickActionRow(
                onEdit = { beginDiaryEditing() },
                onAddText = { beginDiaryEditing(currentDiaryState().withTextBlock()) },
                onAddImage = {
                    structured = currentDiaryState()
                    onContentModeChange(PageContentMode.EditingDiary(title))
                    imagePicker.launch(arrayOf("image/*"))
                },
                onAddTopicTarget = { beginDiaryEditing(currentDiaryState().withTopicTargetBlock()) },
            )
            PaperNoteCard(
                modifier = Modifier.clickable {
                    structured = currentDiaryState()
                    onContentModeChange(pageContentModeForTap(DiaryPage(title, prompt)))
                },
            ) {
                StructuredDiaryPreview(
                    state = StructuredDiary.fromRaw(diaryDraft),
                    onAddImage = {
                        structured = currentDiaryState()
                        onContentModeChange(PageContentMode.EditingDiary(title))
                        imagePicker.launch(arrayOf("image/*"))
                    },
                )
            }
        }
        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        val millis = datePickerState.selectedDateMillis
                        if (millis != null) {
                            structured = structured.withDate(millis.toLocalDate())
                            onDiaryChange(structured.toRaw())
                        }
                        showDatePicker = false
                    }) { Text("确定") }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) { Text("取消") }
                },
            ) {
                DatePicker(state = datePickerState)
            }
        }
        // 对照逆向 fragment_diary.xml：编辑态底部固定工具栏（图片 + 键盘收起）
        if (editingDiary?.title == title) {
            DiaryBottomToolbar(
                onPickImage = { imagePicker.launch(arrayOf("image/*")) },
                onDismissKeyboard = {
                    // 关闭当前编辑器键盘焦点，仅切换浏览态
                    onDiaryChange(structured.toRaw())
                    onContentModeChange(PageContentMode.Browsing)
                },
                inBook = inBook,
            )
        }
        DiaryExportDock(
            hint = exportHint,
            onPreview = {
                val state = currentDiaryState()
                longImagePreview = LongImagePreview(
                    title = title.ifBlank { "Goalday 日记" },
                    subtitle = diaryDateLabel(state.date),
                    filePrefix = "Goalday_diary",
                    bitmap = renderDiaryLongImage(context, title, state),
                )
            },
            onExport = {
                val uri = exportDiaryLongImage(context, title, currentDiaryState())
                exportHint = if (uri != null) "已导出长图" else "导出失败"
            },
        )
        Text(text = BookStrings.diaryLocalOnly, style = MaterialTheme.typography.labelSmall, color = tint.copy(alpha = 0.62f))
    }
    longImagePreview?.let { preview ->
        LongImagePreviewDialog(
            preview = preview,
            onDismiss = { longImagePreview = null },
        )
    }
    expandedFlashback?.let { flashback ->
        OnThisDayFlashbackDialog(
            flashback = flashback,
            onDismiss = { expandedFlashback = null },
        )
    }
}

// 对照逆向 fragment_diary.xml / fragment_diary_inbook.xml：
// 独立日记页有图片+键盘两个按钮，手账内日记页仅保留图片按钮且高度更小
@Composable
private fun DiaryBottomToolbar(
    onPickImage: () -> Unit,
    onDismissKeyboard: () -> Unit,
    inBook: Boolean = false,
) {
    // 对照 fragment_diary.xml：底栏 bg=#E5DAD4，高 46dp
    // 书内日记页 fragment_diary_inbook.xml：底栏 23dp，仅 ic_select_pic 12.5dp
    val toolbarHeight = if (inBook) 23.dp else 46.dp
    val iconSize = if (inBook) 12.5.dp else 25.dp
    val contentPadding = if (inBook) 3.75.dp else 7.5.dp
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(toolbarHeight)
            .clip(RoundedCornerShape(GoaldayDesign.RadiusS))
            .background(GoaldayDesign.TabBarBg)
            .padding(horizontal = contentPadding),
        horizontalArrangement = if (inBook) Arrangement.Start else Arrangement.spacedBy(contentPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_select_pic),
            contentDescription = "插入图片",
            modifier = Modifier.size(iconSize).clickable(onClick = onPickImage),
            contentScale = ContentScale.Fit,
        )
        if (!inBook) {
            Image(
                painter = painterResource(R.drawable.ic_keyboard),
                contentDescription = "收起键盘",
                modifier = Modifier.size(iconSize).clickable(onClick = onDismissKeyboard),
                contentScale = ContentScale.Fit,
            )
        }
    }
}

// 那年今日闪回卡片条：横向滚动展示往年同月同日的日记
@Composable
private fun OnThisDayFlashbackStrip(
    flashbacks: List<OnThisDayDiary>,
    onClick: (OnThisDayDiary) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space1 + 2.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space1 + 2.dp),
        ) {
            Text(
                "💌 那年今日",
                style = MaterialTheme.typography.labelMedium,
                color = GoaldayDesign.Today,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                "${flashbacks.size} 条回忆",
                style = MaterialTheme.typography.labelSmall,
                color = GoaldayDesign.adaptiveInkMuted,
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space2),
        ) {
            flashbacks.forEach { flashback ->
                OnThisDayFlashbackChip(flashback = flashback, onClick = { onClick(flashback) })
            }
        }
    }
}

@Composable
private fun OnThisDayFlashbackChip(
    flashback: OnThisDayDiary,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .width(180.dp)
            .clip(RoundedCornerShape(GoaldayDesign.RadiusM))
            .background(GoaldayDesign.adaptiveSurface)
            .border(GoaldayDesign.Hairline, GoaldayDesign.Today.copy(alpha = 0.22f), RoundedCornerShape(GoaldayDesign.RadiusM))
            .clickable(onClick = onClick)
            .padding(horizontal = GoaldayDesign.Space2 + 2.dp, vertical = GoaldayDesign.Space2),
        verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space1 - 1.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "${flashback.yearsAgo} 年前",
                style = MaterialTheme.typography.labelSmall,
                color = GoaldayDesign.Today,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                diaryDateLabel(flashback.date),
                style = MaterialTheme.typography.labelSmall,
                color = GoaldayDesign.adaptiveInkMuted,
            )
        }
        Text(
            flashback.preview.ifBlank { "（这一天没有留下文字）" },
            style = MaterialTheme.typography.bodySmall,
            color = GoaldayDesign.adaptiveInkSecondary,
            maxLines = 2,
        )
    }
}

@Composable
private fun OnThisDayFlashbackDialog(
    flashback: OnThisDayDiary,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(GoaldayDesign.RadiusXL),
            color = GoaldayDesign.adaptiveSurface,
            tonalElevation = 6.dp,
            shadowElevation = GoaldayDesign.ShadowMedium,
            border = BorderStroke(GoaldayDesign.Hairline, GoaldayDesign.Today.copy(alpha = 0.22f)),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(GoaldayDesign.Space5),
                verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space3),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space1 / 2)) {
                        Text(
                            "那年今日 · ${flashback.yearsAgo} 年前",
                            style = MaterialTheme.typography.titleMedium,
                            color = GoaldayDesign.Today,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            flashback.date.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = GoaldayDesign.adaptiveInkMuted,
                        )
                    }
                    TextButton(onClick = onDismiss) {
                        Text("关闭", color = GoaldayDesign.adaptiveInkSecondary)
                    }
                }
                if (flashback.moodTags.isNotBlank()) {
                    Text(
                        "心情：${flashback.moodTags}",
                        style = MaterialTheme.typography.labelMedium,
                        color = GoaldayDesign.Pink,
                    )
                }
                Text(
                    flashback.preview.ifBlank { "（这一天没有留下更多文字）" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = GoaldayDesign.adaptiveInkPrimary,
                )
                Text(
                    "来自《${flashback.bookTitle}》· ${flashback.pageTitle}",
                    style = MaterialTheme.typography.labelSmall,
                    color = GoaldayDesign.adaptiveInkMuted,
                )
            }
        }
    }
}

@Composable
private fun DiaryWorkspaceHeader(
    title: String,
    prompt: String,
    state: StructuredDiary,
    todoCount: Int,
    doneCount: Int,
    editing: Boolean,
    onEdit: () -> Unit,
    onPickDate: () -> Unit,
) {
    val imageCount = (state.imageBlockUris + state.legacyImageUris).distinct().size
    val textCount = listOf(state.todayDone, state.workTasks, state.smallJoy, state.canImprove, state.photoText, state.richHtml)
        .count { it.isNotBlank() } + state.blocks.count { it.type == DiaryBlockType.TEXT }
    val targetCount = state.blocks.count {
        it.type == DiaryBlockType.TARGET || it.type == DiaryBlockType.TARGET_CHILD || it.type == DiaryBlockType.TOPIC_TARGET
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(GoaldayDesign.RadiusL))
            .background(
                Brush.verticalGradient(
                    listOf(GoaldayDesign.DiaryPromptGradientStart, GoaldayDesign.PinkTint, GoaldayDesign.adaptiveSurface),
                ),
            )
            .border(GoaldayDesign.Hairline, GoaldayDesign.Pink.copy(alpha = 0.16f), RoundedCornerShape(GoaldayDesign.RadiusL))
            .padding(horizontal = GoaldayDesign.Space3 - 1.dp, vertical = GoaldayDesign.Space2 + 2.dp),
        verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space2 + 1.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space1 - 1.dp),
            ) {
                Text(
                    title.ifBlank { "日记页" },
                    color = GoaldayDesign.adaptiveInkPrimary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                )
                Text(
                    prompt,
                    color = GoaldayDesign.adaptiveInkSecondary,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 2,
                )
            }
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space1 + 1.dp),
            ) {
                Text(
                    if (editing) "编辑中" else "书内预览",
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
                        .background(if (editing) GoaldayDesign.Pink else GoaldayDesign.PrimaryAction)
                        .padding(horizontal = GoaldayDesign.Space2 + 1.dp, vertical = GoaldayDesign.Space1),
                )
                Text(
                    diaryDateLabel(state.date),
                    color = GoaldayDesign.Deadline,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
                        .background(GoaldayDesign.Deadline.copy(alpha = 0.08f))
                        .clickable(onClick = onPickDate)
                        .padding(horizontal = GoaldayDesign.Space2, vertical = GoaldayDesign.Space1),
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space1 + 2.dp), modifier = Modifier.fillMaxWidth()) {
            DiaryWorkspaceMetric("文字", textCount, GoaldayDesign.adaptiveInkSecondary, Modifier.weight(1f))
            DiaryWorkspaceMetric("图片", imageCount, GoaldayDesign.RouteDiary, Modifier.weight(1f))
            DiaryWorkspaceMetric("目标", targetCount, GoaldayDesign.Positive, Modifier.weight(1f))
            DiaryWorkspaceMetric("待办", todoCount + doneCount, GoaldayDesign.Pink, Modifier.weight(1f))
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(GoaldayDesign.RadiusM))
                .background(GoaldayDesign.adaptiveWhiteOverlayMedium)
                .padding(horizontal = GoaldayDesign.Space2, vertical = GoaldayDesign.Space2 - 1.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                if (state.hasUserContent) "日记已经保存为本地书页" else "今天还没有内容，先写一条记录",
                color = GoaldayDesign.adaptiveInkMuted,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.weight(1f),
                maxLines = 1,
            )
            Text(
                if (editing) "继续写" else "进入编辑",
                color = GoaldayDesign.adaptiveInkSecondary,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
                    .background(GoaldayDesign.PinkSoft)
                    .clickable(onClick = onEdit)
                    .padding(horizontal = GoaldayDesign.Space2 + 2.dp, vertical = GoaldayDesign.Space1 + 1.dp),
            )
        }
    }
}

@Composable
private fun DiaryWorkspaceMetric(
    label: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(GoaldayDesign.RadiusM))
            .background(GoaldayDesign.adaptiveWhiteOverlayMedium)
            .border(GoaldayDesign.Hairline, color.copy(alpha = 0.18f), RoundedCornerShape(GoaldayDesign.RadiusM))
            .padding(horizontal = GoaldayDesign.Space2 - 1.dp, vertical = GoaldayDesign.Space1 + 2.dp),
        verticalArrangement = Arrangement.spacedBy(1.dp),
    ) {
        Text(count.toString(), color = color, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 1)
        Text(label, color = GoaldayDesign.adaptiveInkMuted, style = MaterialTheme.typography.labelSmall, maxLines = 1)
    }
}

@Composable
private fun DiaryExportDock(
    hint: String,
    onPreview: () -> Unit,
    onExport: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(GoaldayDesign.RadiusL))
            .background(GoaldayDesign.Paper)
            .border(GoaldayDesign.Hairline, GoaldayDesign.BorderColor.copy(alpha = 0.13f), RoundedCornerShape(GoaldayDesign.RadiusL))
            .padding(horizontal = GoaldayDesign.Space2 + 1.dp, vertical = GoaldayDesign.Space2 - 1.dp),
        horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space2 - 1.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "导出",
            color = GoaldayDesign.adaptiveInkPrimary,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.width(42.dp),
        )
        DiaryDockAction("预览长图", GoaldayDesign.Pink, Modifier.weight(1f), onPreview)
        DiaryDockAction("快速导出", GoaldayDesign.Positive, Modifier.weight(1f), onExport)
        if (hint.isNotBlank()) {
            Text(hint, style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.adaptiveInkMuted, maxLines = 1)
        }
    }
}

@Composable
private fun DiaryDockAction(
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Text(
        label,
        color = color,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        textAlign = TextAlign.Center,
        modifier = modifier
            .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
            .background(color.copy(alpha = 0.12f))
            .border(GoaldayDesign.Hairline, color.copy(alpha = 0.20f), RoundedCornerShape(GoaldayDesign.RadiusPill))
            .clickable(onClick = onClick)
            .padding(horizontal = GoaldayDesign.Space2, vertical = GoaldayDesign.Space1 + 2.dp),
    )
}

@Composable
private fun DiaryStartPanel(
    todoCount: Int,
    doneCount: Int,
    onStart: () -> Unit,
    onAddImage: () -> Unit,
    onAddTarget: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(GoaldayDesign.RadiusL))
            .background(Brush.verticalGradient(listOf(GoaldayDesign.PinkTint, GoaldayDesign.adaptiveSurface)))
            .border(GoaldayDesign.Hairline, GoaldayDesign.Pink.copy(alpha = 0.15f), RoundedCornerShape(GoaldayDesign.RadiusL))
            .padding(horizontal = GoaldayDesign.Space3, vertical = GoaldayDesign.Space3 - 1.dp),
        verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space2),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space1 / 2), modifier = Modifier.weight(1f)) {
                Text("开始今天的日记", color = GoaldayDesign.adaptiveInkPrimary, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text("会自动关联今日待办 $todoCount 条、已完成 $doneCount 条", color = GoaldayDesign.adaptiveInkMuted, style = MaterialTheme.typography.labelSmall)
            }
            Text(
                "写一条",
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
                    .background(GoaldayDesign.Pink)
                    .clickable(onClick = onStart)
                    .padding(horizontal = GoaldayDesign.Space3, vertical = GoaldayDesign.Space2 - 1.dp),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space2 - 1.dp), modifier = Modifier.fillMaxWidth()) {
            DiaryStartAction("图片", GoaldayDesign.RouteDiary, Modifier.weight(1f), onAddImage)
            DiaryStartAction("目标块", GoaldayDesign.Positive, Modifier.weight(1f), onAddTarget)
        }
    }
}

@Composable
private fun DiaryStartAction(
    label: String,
    color: Color,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    Text(
        label,
        color = color,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        textAlign = TextAlign.Center,
        modifier = modifier
            .clip(RoundedCornerShape(GoaldayDesign.RadiusM))
            .background(GoaldayDesign.adaptiveWhiteOverlayMedium)
            .border(GoaldayDesign.Hairline, color.copy(alpha = 0.18f), RoundedCornerShape(GoaldayDesign.RadiusM))
            .clickable(onClick = onClick)
            .padding(vertical = GoaldayDesign.Space2 - 1.dp),
    )
}

@Composable
private fun DiaryQuickActionRow(
    onEdit: () -> Unit,
    onAddText: () -> Unit,
    onAddImage: () -> Unit,
    onAddTopicTarget: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(GoaldayDesign.RadiusM))
            .background(GoaldayDesign.PinkTint)
            .border(GoaldayDesign.Hairline, GoaldayDesign.Pink.copy(alpha = 0.12f), RoundedCornerShape(GoaldayDesign.RadiusM))
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = GoaldayDesign.Space2 - 1.dp, vertical = GoaldayDesign.Space1 + 2.dp),
        horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space1 + 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DiaryQuickActionChip("编辑", GoaldayDesign.Pink, onEdit)
        DiaryQuickActionChip("文字", GoaldayDesign.adaptiveInkSecondary, onAddText)
        DiaryQuickActionChip("图片", GoaldayDesign.RouteDiary, onAddImage)
        DiaryQuickActionChip("专题目标", GoaldayDesign.Positive, onAddTopicTarget)
    }
}

@Composable
private fun DiaryQuickActionChip(
    label: String,
    color: Color,
    onClick: () -> Unit,
) {
    Text(
        label,
        style = MaterialTheme.typography.labelSmall,
        color = color,
        fontWeight = FontWeight.SemiBold,
        maxLines = 1,
        modifier = Modifier
            .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
            .background(GoaldayDesign.adaptiveWhiteOverlayMedium)
            .border(GoaldayDesign.Hairline, color.copy(alpha = 0.18f), RoundedCornerShape(GoaldayDesign.RadiusPill))
            .clickable(onClick = onClick)
            .padding(horizontal = GoaldayDesign.Space2 + 2.dp, vertical = GoaldayDesign.Space1 + 1.dp),
    )
}

private fun LocalDate.toEpochMillis(): Long =
    atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

private fun Long.toLocalDate(): LocalDate =
    Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()

@Composable
private fun DiaryBlockRail(
    state: StructuredDiary,
    todoItems: List<String>,
    doneItems: List<String>,
    editing: Boolean,
) {
    val textBlocks = listOf(state.todayDone, state.workTasks, state.smallJoy, state.canImprove, state.photoText)
        .count { it.isNotBlank() } + state.blocks.count { it.type == DiaryBlockType.TEXT }
    val imageBlocks = (state.imageBlockUris + state.legacyImageUris).distinct().size
    val targetBlocks = state.blocks.count { it.type == DiaryBlockType.TARGET || it.type == DiaryBlockType.TARGET_CHILD || it.type == DiaryBlockType.TOPIC_TARGET }
        .takeIf { it > 0 }
        ?: (todoItems + doneItems).map(String::trim).filter(String::isNotBlank).distinct().size
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(GoaldayDesign.RadiusM))
            .background(GoaldayDesign.PinkTint)
            .border(GoaldayDesign.Hairline, GoaldayDesign.Pink.copy(alpha = 0.13f), RoundedCornerShape(GoaldayDesign.RadiusM))
            .padding(horizontal = GoaldayDesign.Space2 - 1.dp, vertical = GoaldayDesign.Space1 + 2.dp),
        horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space1 + 1.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DiaryBlockPill("TEXT", "$textBlocks", GoaldayDesign.adaptiveInkPrimary, Modifier.weight(1f))
        DiaryBlockPill("IMAGE", "$imageBlocks", GoaldayDesign.RouteDiary, Modifier.weight(1f))
        DiaryBlockPill("TARGET", "$targetBlocks", GoaldayDesign.Positive, Modifier.weight(1f))
        Text(
            if (editing) "编辑中" else "预览",
            style = MaterialTheme.typography.labelSmall,
            color = GoaldayDesign.adaptiveInkMuted,
        )
    }
}

@Composable
private fun DiaryBlockPill(
    label: String,
    count: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
            .background(GoaldayDesign.adaptiveWhiteOverlayMedium)
            .padding(horizontal = GoaldayDesign.Space2 - 1.dp, vertical = GoaldayDesign.Space1),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.SemiBold, maxLines = 1)
        Text(count, style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.adaptiveInkMuted)
    }
}

