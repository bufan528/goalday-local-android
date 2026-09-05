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

@Composable
fun BoxScope.SpineLayer(
    visualProgress: Float,
    active: Boolean,
    profile: TurnProfile = TurnProfile.DEFAULT,
) {
    // spine：极细并极淡，避免与 BookShell 中央沟槽叠加后形成粗亮柱子
    val baseWidth = profile.spineBaseWidthDp.dp
    Box(
        modifier = Modifier
            .align(Alignment.Center)
            .width(baseWidth)
            .fillMaxHeight()
            .background(
                Brush.horizontalGradient(
                    listOf(
                        GoaldayDesign.BookSpine.copy(alpha = if (active) 0.38f else 0.28f),
                        GoaldayDesign.BookSpine.copy(alpha = if (active) 0.52f else 0.38f),
                        GoaldayDesign.BookSpineLight.copy(alpha = if (active) 0.58f else 0.44f),
                        GoaldayDesign.BookSpine.copy(alpha = if (active) 0.52f else 0.38f),
                        GoaldayDesign.BookSpine.copy(alpha = if (active) 0.38f else 0.28f),
                    ),
                ),
            ),
    )

    if (active) {
        // 两侧阴影：模拟书页弯入书脊的暗部，范围收窄且很淡
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .width((14f + visualProgress * profile.spineSideShadowExtraStepDp).dp)
                .fillMaxHeight()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color.Black.copy(alpha = (0.04f + visualProgress * 0.08f).coerceAtMost(0.14f)),
                            Color.Transparent,
                            Color.Black.copy(alpha = (0.04f + visualProgress * 0.08f).coerceAtMost(0.14f)),
                        ),
                    ),
                ),
        )
        // 中央极细高光：仅保留微弱圆柱感
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .width((2f + visualProgress * profile.spineCenterHighlightStepDp).dp)
                .fillMaxHeight()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            GoaldayDesign.BookSpineLight.copy(alpha = (0.14f + visualProgress * 0.12f).coerceAtMost(0.28f)),
                            GoaldayDesign.Paper.copy(alpha = (0.18f + visualProgress * 0.16f).coerceAtMost(0.36f)),
                            GoaldayDesign.BookSpineLight.copy(alpha = (0.14f + visualProgress * 0.12f).coerceAtMost(0.28f)),
                        ),
                    ),
                ),
        )
    }
}

@Composable
fun PageSurface(
    modifier: Modifier = Modifier,
    title: String,
    pageNumber: String,
    headerTitle: String,
    headerSubtitle: String,
    tint: Color,
    onSavedClick: (() -> Unit)? = null,
    body: @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(GoaldayDesign.RadiusL))
            .background(GoaldayDesign.adaptivePaperGradient)
            .padding(horizontal = GoaldayDesign.Space4, vertical = GoaldayDesign.Space3),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            PageHeaderLine(
                bookTitle = headerTitle,
                subtitle = headerSubtitle,
                tint = tint,
                savedText = pageNumber,
                onSavedClick = onSavedClick,
            )
            Spacer(Modifier.height(GoaldayDesign.Space1 + 2.dp))
            Text(text = title, style = MaterialTheme.typography.titleMedium, color = GoaldayDesign.adaptiveInkPrimary)
            Spacer(Modifier.height(GoaldayDesign.Space2))
            body()
        }
    }
}

@Composable
fun DestinationPageLayer(
    modifier: Modifier,
    bookTitle: String,
    subtitle: String,
    page: BookPage?,
    pageIndex: Int,
    pageCount: Int,
    tint: Color,
    revealProgress: Float,
    direction: TurnDirection?,
) {
    val pageTitle = page?.title ?: BookStrings.cover
    val pageSubtitle = destinationPageSubtitle(direction, subtitle)
    val pageNumber = when {
        page == null -> ""
        direction == TurnDirection.NEXT -> "${pageIndex + 2} / $pageCount"
        direction == TurnDirection.PREVIOUS -> "$pageIndex / $pageCount"
        else -> "${pageIndex + 1} / $pageCount"
    }

    PageSurface(
        modifier = modifier,
        title = pageTitle,
        pageNumber = pageNumber.ifBlank { BookStrings.pagePreview },
        headerTitle = bookTitle,
        headerSubtitle = pageSubtitle,
        tint = tint.copy(alpha = 0.68f),
    ) {
        Text(
            text = condensedPreviewText(page?.let(::pagePreviewText) ?: subtitle, maxLength = 92),
            style = MaterialTheme.typography.bodyMedium,
            color = GoaldayDesign.adaptiveInkSecondary.copy(alpha = destinationRevealAlpha(revealProgress)),
        )
        Spacer(Modifier.weight(1f))
    }
}

@Composable
fun PageBackLayer(
    modifier: Modifier,
    tint: Color,
    progress: Float,
    direction: TurnDirection?,
    anchorY: Float = 0.5f,
) {
    // 背面纸张：比正面稍深、稍黄，模拟纸张背面与厚度；颜色过渡更柔和
    val curlAlignTop = anchorY < 0.46f
    val easedCurl = progress * progress * (3f - 2f * progress)
    val curlStrength = (0.12f + easedCurl * 0.88f).coerceIn(0f, 1.0f)
    val stackShadow = (0.08f + progress * 0.18f).coerceAtMost(0.26f)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(GoaldayDesign.Radius2XL, GoaldayDesign.Radius2XL, GoaldayDesign.Radius3XL, GoaldayDesign.Radius3XL))
            .background(
                Brush.horizontalGradient(
                    if (direction == TurnDirection.NEXT) {
                        listOf(GoaldayDesign.PageTurnEdgeStart, GoaldayDesign.PaperWarm, GoaldayDesign.ExportPaperWarm)
                    } else {
                        listOf(GoaldayDesign.ExportPaperWarm, GoaldayDesign.PaperWarm, GoaldayDesign.PageTurnEdgeStart)
                    },
                ),
            )
            .padding(horizontal = GoaldayDesign.Space6 + 4.dp, vertical = GoaldayDesign.Space6 + 2.dp),
    ) {
        // 层 1：中央折痕阴影（翻页时纸张弯折的暗带，方向跟随翻页方向）
        Box(
            modifier = Modifier
                .align(if (direction == TurnDirection.NEXT) Alignment.CenterStart else Alignment.CenterEnd)
                .width((14f + progress * 26f).dp)
                .fillMaxHeight()
                .background(
                    Brush.horizontalGradient(
                        if (direction == TurnDirection.NEXT) {
                            listOf(
                                Color.Black.copy(alpha = stackShadow),
                                Color.White.copy(alpha = (0.08f + progress * 0.12f).coerceAtMost(0.18f)),
                                Color.Transparent,
                            )
                        } else {
                            listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = (0.08f + progress * 0.12f).coerceAtMost(0.18f)),
                                Color.Black.copy(alpha = stackShadow),
                            )
                        },
                    ),
                ),
        )
        // 层 2：纸张翻起背面的角部阴影，强化圆柱体感
        Box(
            modifier = Modifier
                .align(
                    when {
                        direction == TurnDirection.NEXT && curlAlignTop -> Alignment.BottomEnd
                        direction == TurnDirection.PREVIOUS && curlAlignTop -> Alignment.BottomStart
                        direction == TurnDirection.NEXT -> Alignment.TopEnd
                        else -> Alignment.TopStart
                    },
                )
                .width((40f + progress * 80f).dp)
                .height((50f + progress * 100f).dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = (curlStrength * 0.18f).coerceAtMost(0.24f)),
                            Color.Transparent,
                        ),
                        radius = 280f,
                    ),
                ),
        )
        // 层 3：纸张厚度侧影——翻动页边缘露出下方纸张的层叠暗边
        Box(
            modifier = Modifier
                .align(if (direction == TurnDirection.NEXT) Alignment.CenterEnd else Alignment.CenterStart)
                .width((5f + progress * 10f).dp)
                .fillMaxHeight()
                .background(
                    Brush.horizontalGradient(
                        if (direction == TurnDirection.NEXT) {
                            listOf(Color.Transparent, Color.Black.copy(alpha = (0.08f + progress * 0.14f).coerceAtMost(0.20f)))
                        } else {
                            listOf(Color.Black.copy(alpha = (0.08f + progress * 0.14f).coerceAtMost(0.20f)), Color.Transparent)
                        },
                    ),
                ),
        )

        Column(modifier = Modifier.fillMaxSize()) {
            PageHeaderLine(
                bookTitle = BookStrings.pageBack,
                subtitle = BookStrings.pageTurning,
                tint = tint.copy(alpha = 0.50f),
                savedText = BookStrings.turnProgress.format((progress * 100).toInt()),
            )
            Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
fun ActivePageLayer(
    modifier: Modifier,
    page: BookPage,
    pageIndex: Int,
    pageCount: Int,
    bookTitle: String,
    subtitle: String,
    tint: Color,
    isSaved: Boolean,
    diaryDraft: String,
    customPageItems: List<String>,
    weeklyTheme: String,
    todayPlanItems: List<String>,
    todayCompletedItems: List<String>,
    schedulePreviewEntries: List<ScheduleEntry>,
    targetItemMeta: Map<String, TargetItemMeta>,
    onToggleSaved: () -> Unit,
    isChecked: (String, String) -> Boolean,
    onToggleChecked: (String, String) -> Unit,
    onDiaryChange: (String) -> Unit,
    onAddCustomItem: (String) -> Unit,
    onAddCustomItemWithDeadline: (String, Int?) -> Unit,
    onRemoveCustomItem: (String) -> Unit,
    onRenameCustomItem: (String, String) -> Unit,
    onAddToSchedule: (String, Int) -> Unit,
    onAddHandbookPoolItem: (String) -> Unit,
    onRemoveHandbookPoolItem: (String) -> Unit,
    onAddScheduleFromHandbook: (String, Int, Int, String, Int) -> Unit,
    onWeeklyThemeChange: (String) -> Unit,
    onMoveItemToToday: (String) -> Unit,
    onMoveItemToCompleted: (String) -> Unit,
    onRestoreItemFromToday: (String) -> Unit,
    onRestoreItemFromCompleted: (String) -> Unit,
    onUpdateScheduleTitle: (String, String) -> Unit,
    onMoveScheduleDay: (String, Int, Int) -> Unit,
    onToggleScheduleCompleted: (String) -> Unit,
    onUpdateTargetNote: (String, String) -> Unit,
    onUpdateTargetDeadline: (String, Int?) -> Unit,
    onOpenTargetDetail: (String) -> Unit,
    pendingCommand: RichEditorCommand?,
    onCommand: (RichEditorCommand) -> Unit,
    contentMode: PageContentMode,
    onContentModeChange: (PageContentMode) -> Unit,
    handbookMode: Boolean = false,
    turnProgress: Float = 0f,
    turnDirection: TurnDirection? = null,
    startInMonthBoard: Boolean = false,
) {
    // 日记图片选择器：对齐参考APK底部图片栏的"插入图片"按钮
    val diaryContext = LocalContext.current
    val diaryImagePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            runCatching {
                diaryContext.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val updated = StructuredDiary.fromRaw(diaryDraft).withImageUri(uri.toString())
            onDiaryChange(updated.toRaw())
        }
    }
    if (handbookMode) {
        when (page) {
            is SchedulePage -> HandbookReplicaPage(
                modifier = modifier,
                page = page,
                pageIndex = pageIndex,
                pageCount = pageCount,
                todayPlanItems = todayPlanItems,
                todayCompletedItems = todayCompletedItems,
                schedulePreviewEntries = schedulePreviewEntries,
                weeklyTheme = weeklyTheme,
                onAddPoolItem = onAddHandbookPoolItem,
                onRemovePoolItem = onRemoveHandbookPoolItem,
                onAddSchedule = onAddScheduleFromHandbook,
                onWeeklyThemeChange = onWeeklyThemeChange,
                onUpdateScheduleTitle = onUpdateScheduleTitle,
                onMoveScheduleDay = onMoveScheduleDay,
                onToggleScheduleCompleted = onToggleScheduleCompleted,
                turnProgress = turnProgress,
                turnDirection = turnDirection,
                startInMonthBoard = startInMonthBoard,
            )
            is PlanPage -> InBookPlanPreview(
                modifier = modifier,
                page = page,
                pageIndex = pageIndex,
                pageCount = pageCount,
                customPageItems = customPageItems,
                isChecked = isChecked,
                onToggleChecked = onToggleChecked,
                onDeleteItem = { item -> onRemoveCustomItem(item) },
                onEditItem = { oldItem, newItem -> onRenameCustomItem(oldItem, newItem) },
                onAddItem = { text -> onAddCustomItem(text) },
                tint = tint,
                turnProgress = turnProgress,
                turnDirection = turnDirection,
                handbookMode = true,
            )
            is DiaryPage -> InBookDiaryPreview(
                modifier = modifier,
                page = page,
                pageIndex = pageIndex,
                pageCount = pageCount,
                diaryDraft = diaryDraft,
                tint = tint,
                turnProgress = turnProgress,
                turnDirection = turnDirection,
                handbookMode = true,
                onAddImage = {
                    diaryImagePicker.launch(arrayOf("image/*"))
                },
            )
            is TargetPage -> InBookTargetPreview(
                modifier = modifier,
                page = page,
                pageIndex = pageIndex,
                pageCount = pageCount,
                customPageItems = customPageItems,
                targetItemMeta = targetItemMeta,
                isChecked = isChecked,
                onToggleChecked = onToggleChecked,
                onOpenTargetDetail = onOpenTargetDetail,
                onDeleteItem = { item -> onRemoveCustomItem(item) },
                onEditItem = { oldItem, newItem -> onRenameCustomItem(oldItem, newItem) },
                onAddItem = { text -> onAddCustomItem(text) },
                tint = tint,
                turnProgress = turnProgress,
                turnDirection = turnDirection,
                handbookMode = true,
            )
        }
        return
    } else {
    // 翻页内层视差：统一 smoothstep 缓动，删除原线性项（消除起步瞬时速度导致的跳变）
    // 量级从 27f 降到 14f，与 handbook 路径 8f 接近，避免 handbook/非 handbook 切换时位移差距过大
    val easedShift = turnProgress * turnProgress * (3f - 2f * turnProgress)
    val contentShift = when (turnDirection) {
        TurnDirection.NEXT -> -(easedShift * 14f)
        TurnDirection.PREVIOUS -> easedShift * 14f
        null -> 0f
    }
    val contentAlpha = (1f - turnProgress * 0.24f).coerceIn(0.74f, 1f)
    val backgroundShift = when (turnDirection) {
        TurnDirection.NEXT -> -turnProgress * 7f
        TurnDirection.PREVIOUS -> turnProgress * 7f
        null -> 0f
    }
    val contentTiltY = when (turnDirection) {
        TurnDirection.NEXT -> -turnProgress * 2.2f
        TurnDirection.PREVIOUS -> turnProgress * 2.2f
        null -> 0f
    }
    PageSurface(
        modifier = modifier,
        title = page.title,
        pageNumber = if (isSaved) BookStrings.savedBook else BookStrings.saveBook,
        headerTitle = bookTitle,
        headerSubtitle = subtitle,
        tint = tint,
        onSavedClick = onToggleSaved,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(94.dp)
                .graphicsLayer {
                    translationX = backgroundShift
                    alpha = (0.11f + turnProgress * 0.08f).coerceAtMost(0.22f)
                }
                .background(
                    Brush.verticalGradient(
                        listOf(
                            GoaldayDesign.BorderColor.copy(alpha = 0.09f),
                            Color.Transparent,
                        ),
                    ),
                ),
        )
        Column(
            modifier = Modifier.graphicsLayer {
                alpha = contentAlpha
                translationX = contentShift
                rotationZ = contentTiltY * 0.12f
            },
        ) {
            when (page) {
                is TargetPage -> TargetDetailReplicaPage(page.title, page.items, customPageItems, tint, schedulePreviewEntries, targetItemMeta, isChecked, onToggleChecked, onAddCustomItem, onRemoveCustomItem, onRenameCustomItem, onAddToSchedule, onUpdateTargetNote, onUpdateTargetDeadline, onOpenTargetDetail)
                is SchedulePage -> EditableBulletPage(page.title, page.items, customPageItems, tint.copy(alpha = 0.74f), BookStrings.addSchedule, true, isChecked, onToggleChecked, onAddCustomItem, onAddCustomItemWithDeadline, onRemoveCustomItem, onRenameCustomItem, onAddToSchedule, weeklyTheme, todayPlanItems, todayCompletedItems, schedulePreviewEntries, onWeeklyThemeChange, onMoveItemToToday, onMoveItemToCompleted, onRestoreItemFromToday, onRestoreItemFromCompleted, contentMode, onContentModeChange)
                is PlanPage -> EditableBulletPage(page.title, page.items, customPageItems, GoaldayDesign.BorderColor, BookStrings.addPlan, false, isChecked, onToggleChecked, onAddCustomItem, onAddCustomItemWithDeadline, onRemoveCustomItem, onRenameCustomItem, onAddToSchedule, weeklyTheme, todayPlanItems, todayCompletedItems, schedulePreviewEntries, onWeeklyThemeChange, onMoveItemToToday, onMoveItemToCompleted, onRestoreItemFromToday, onRestoreItemFromCompleted, contentMode, onContentModeChange)
                is DiaryPage -> DiarySection(page.title, page.prompt, tint, diaryDraft, todayPlanItems, todayCompletedItems, pendingCommand, onCommand, onDiaryChange, contentMode, onContentModeChange)
            }
        }
        Spacer(Modifier.height(GoaldayDesign.Space6))
        Text(text = "${pageIndex + 1} / $pageCount", style = MaterialTheme.typography.labelMedium, color = GoaldayDesign.adaptiveInkSecondary)
    }
    }
}

@Composable
internal fun DiaryLinkedTargetStrip(
    doneItems: List<String>,
    todoItems: List<String>,
    onPickDone: (String) -> Unit,
    onPickTodo: (String) -> Unit,
) {
    // 对照逆向 item_diary_target.xml：标题「今日完成」+奖励图标，空态文案，圆点+文本子项
    val done = doneItems.map(String::trim).filter(String::isNotBlank).distinct()
    val todo = todoItems.map(String::trim).filter(String::isNotBlank).filterNot { it in done }.distinct()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(GoaldayDesign.RadiusS))
            .background(GoaldayDesign.PinkTint)
            .border(GoaldayDesign.Hairline, GoaldayDesign.Pink.copy(alpha = 0.12f), RoundedCornerShape(GoaldayDesign.RadiusS))
            .padding(bottom = GoaldayDesign.Space2),
        verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space1),
    ) {
        // 标题行：奖励图标 + 「今日完成」
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space1),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = GoaldayDesign.Space3, top = GoaldayDesign.Space2 + 1.dp, bottom = GoaldayDesign.Space1),
        ) {
            Icon(
                Icons.Filled.CardGiftcard,
                contentDescription = null,
                tint = GoaldayDesign.Pink,
                modifier = Modifier.size(16.dp),
            )
            Text(
                "今日完成",
                style = MaterialTheme.typography.titleSmall,
                color = GoaldayDesign.Pink,
                fontWeight = FontWeight.SemiBold,
            )
        }
        if (done.isEmpty() && todo.isEmpty()) {
            // 空态文案：对照逆向 tv_empty
            Text(
                "这里会自动记录清单中完成的事项。",
                style = MaterialTheme.typography.bodySmall,
                color = GoaldayDesign.adaptiveInkMuted,
                modifier = Modifier.padding(start = GoaldayDesign.Space3, top = GoaldayDesign.Space1),
            )
        } else {
            // 子项列表：圆点 + 文本（对照 item_diary_target_child.xml）
            done.forEach { item ->
                DiaryLinkedTargetChildRow(
                    text = item,
                    completed = true,
                    color = GoaldayDesign.Positive,
                    onClick = { onPickDone(item) },
                )
            }
            todo.forEach { item ->
                DiaryLinkedTargetChildRow(
                    text = item,
                    completed = false,
                    color = GoaldayDesign.RouteDiary,
                    onClick = { onPickTodo(item) },
                )
            }
        }
    }
}

// 对照逆向 item_diary_target_child.xml：5dip圆点, paddingStart=10dip, marginStart/End=8dip
@Composable
private fun DiaryLinkedTargetChildRow(
    text: String,
    completed: Boolean,
    color: Color,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space2),
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp)
            .clickable { onClick() }
            .padding(start = 10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(5.dp)
                .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
                .background(color),
        )
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = if (completed) GoaldayDesign.adaptiveInkSecondary else GoaldayDesign.adaptiveInkPrimary,
            maxLines = 1,
        )
    }
}

internal fun diaryBlockTypeIcon(type: DiaryBlockType): ImageVector =
    when (type) {
        DiaryBlockType.IMAGE -> Icons.Filled.Image
        DiaryBlockType.TEXT -> Icons.Filled.TextFields
        DiaryBlockType.TARGET,
        DiaryBlockType.TARGET_IN_BOOK -> Icons.Filled.Check
        DiaryBlockType.TARGET_CHILD,
        DiaryBlockType.TARGET_CHILD_IN_BOOK -> Icons.Filled.SubdirectoryArrowRight
        DiaryBlockType.TOPIC_TARGET,
        DiaryBlockType.TOPIC_TARGET_IN_BOOK -> Icons.Filled.RadioButtonChecked
    }

internal fun diaryBlockDisplayTitle(type: DiaryBlockType): String =
    when (type) {
        DiaryBlockType.IMAGE -> "图片记录"
        DiaryBlockType.TEXT -> "文字记录"
        DiaryBlockType.TARGET,
        DiaryBlockType.TARGET_IN_BOOK -> "关联目标"
        DiaryBlockType.TARGET_CHILD,
        DiaryBlockType.TARGET_CHILD_IN_BOOK -> "目标子项"
        DiaryBlockType.TOPIC_TARGET,
        DiaryBlockType.TOPIC_TARGET_IN_BOOK -> "专题目标"
    }

internal fun diaryBlockDisplaySubtitle(type: DiaryBlockType): String =
    when (type) {
        DiaryBlockType.IMAGE -> "照片 / 截图 / 本地图片"
        DiaryBlockType.TEXT -> "自由文字 / 摘要"
        DiaryBlockType.TARGET,
        DiaryBlockType.TARGET_IN_BOOK -> "完成项 / 工作项"
        DiaryBlockType.TARGET_CHILD,
        DiaryBlockType.TARGET_CHILD_IN_BOOK -> "下一步 / 子任务"
        DiaryBlockType.TOPIC_TARGET,
        DiaryBlockType.TOPIC_TARGET_IN_BOOK -> "来自灵感主题"
    }

@Composable
internal fun diaryBlockTypeColor(type: DiaryBlockType): Color =
    when (type) {
        DiaryBlockType.IMAGE -> GoaldayDesign.RouteDiary
        DiaryBlockType.TEXT -> GoaldayDesign.adaptiveInkSecondary
        DiaryBlockType.TARGET,
        DiaryBlockType.TARGET_IN_BOOK -> GoaldayDesign.Positive
        DiaryBlockType.TARGET_CHILD,
        DiaryBlockType.TARGET_CHILD_IN_BOOK -> GoaldayDesign.RouteTarget
        DiaryBlockType.TOPIC_TARGET,
        DiaryBlockType.TOPIC_TARGET_IN_BOOK -> GoaldayDesign.RouteDiary
    }

@Composable
internal fun diaryBlockTypeBackground(type: DiaryBlockType): Color =
    when (type) {
        DiaryBlockType.IMAGE -> GoaldayDesign.Pink.copy(alpha = 0.09f)
        DiaryBlockType.TEXT -> GoaldayDesign.adaptiveSurfaceSoft.copy(alpha = 0.08f)
        DiaryBlockType.TARGET,
        DiaryBlockType.TARGET_IN_BOOK -> GoaldayDesign.Positive.copy(alpha = 0.08f)
        DiaryBlockType.TARGET_CHILD,
        DiaryBlockType.TARGET_CHILD_IN_BOOK -> GoaldayDesign.Positive.copy(alpha = 0.07f)
        DiaryBlockType.TOPIC_TARGET,
        DiaryBlockType.TOPIC_TARGET_IN_BOOK -> GoaldayDesign.Pink.copy(alpha = 0.09f)
    }

internal fun plainTextFromHtml(html: String): String =
    html
        .replace(Regex("(?i)<br\\s*/?>"), "\n")
        .replace(Regex("(?i)</(p|div|h1|h2|blockquote|li)>"), "\n")
        .replace(Regex("<[^>]+>"), "")
        .replace("&nbsp;", " ")
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .lines()
        .map(String::trim)
        .filter(String::isNotBlank)
        .joinToString("\n")

// 对照逆向 item_diary_text.xml：文字块 16sp、自适应色、行间距 2dp（lineHeight ≈ 18sp）
@Composable
internal fun diaryBlockTextStyle(block: DiaryEntryBlock): TextStyle {
    val base = MaterialTheme.typography.bodyMedium.copy(
        color = GoaldayDesign.DiarySectionInk,
        fontSize = 16.sp,
        lineHeight = 18.sp,
    )
    return when (block.style) {
        DiaryBlockStyle.BODY -> base
        DiaryBlockStyle.BOLD -> base.copy(fontWeight = FontWeight.SemiBold)
        DiaryBlockStyle.QUOTE -> base.copy(color = GoaldayDesign.adaptiveInkSecondary)
        DiaryBlockStyle.CHECK -> base.copy(textDecoration = TextDecoration.None)
    }
}

@Composable
internal fun StructuredDiaryPreview(
    state: StructuredDiary,
    onAddImage: () -> Unit,
) {
    val dateLabel = remember(state.dateIso) { diaryDateLabel(state.date) }
    val moodItems = remember(state.moodTags) {
        state.moodTags.split(',', '，', ' ').map(String::trim).filter(String::isNotBlank).take(6)
    }
    val photoNotes = remember(state.photoText) {
        state.photoText.lines().map(String::trim).filter(String::isNotBlank)
    }
    val imageUris = remember(state.blocksRaw, state.photoNotes) {
        (state.imageBlockUris + state.legacyImageUris).distinct()
    }
    val summaryRows = listOf(
        DiaryPreviewRow("完成", "今日完成", state.todayDone, DiaryBlockType.TARGET),
        DiaryPreviewRow("工作", "工作任务", state.workTasks, DiaryBlockType.TARGET),
        DiaryPreviewRow("幸福", "小幸福", state.smallJoy, DiaryBlockType.TEXT),
        DiaryPreviewRow("改进", "可改进", state.canImprove, DiaryBlockType.TEXT),
        DiaryPreviewRow("图片", "图片描述", state.photoText, DiaryBlockType.IMAGE),
    )
    val hasContent = moodItems.isNotEmpty() ||
        photoNotes.isNotEmpty() ||
        imageUris.isNotEmpty() ||
        state.richHtml.isNotBlank() ||
        state.blocks.isNotEmpty() ||
        summaryRows.any { it.content.isNotBlank() }

    Column(verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space2 - 1.dp), modifier = Modifier.fillMaxWidth()) {
        DiaryInBookHeader(
            title = "日记活动",
            subtitle = dateLabel,
            blockCount = state.blocks.size,
            imageCount = imageUris.size,
        )
        if (!hasContent) {
            DiaryEmptyInBookPage(onAddImage = onAddImage)
            return@Column
        }
        if (moodItems.isNotEmpty()) {
            DiaryMoodRibbon(items = moodItems)
        }
        if (imageUris.isNotEmpty()) {
            DiaryMediaMosaic(imageUris = imageUris, notes = photoNotes)
        } else if (photoNotes.isNotEmpty()) {
            DiaryPhotoNoteGrid(notes = photoNotes)
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .align(Alignment.End)
                    .clickable { onAddImage() }
                    .padding(horizontal = GoaldayDesign.Space1, vertical = 2.dp),
            ) {
                Icon(Icons.Filled.Add, contentDescription = "添加图片", tint = GoaldayDesign.RouteDiary, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(2.dp))
                Text(
                    "添加图片",
                    color = GoaldayDesign.RouteDiary,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
        if (state.richHtml.isNotBlank()) {
            DiaryBlock("富文本记录", plainTextFromHtml(state.richHtml))
        }
        DiaryTypedBlockPreview(blocks = state.blocks)
        Column(verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space1 + 1.dp), modifier = Modifier.fillMaxWidth()) {
            summaryRows
                .filter { it.content.isNotBlank() }
                .forEach { row ->
                    DiaryInBookRow(row.code, row.title, row.content, row.type)
                }
        }
    }
}

private data class DiaryPreviewRow(
    val code: String,
    val title: String,
    val content: String,
    val type: DiaryBlockType,
)

@Composable
private fun DiaryInBookHeader(
    title: String,
    subtitle: String,
    blockCount: Int,
    imageCount: Int,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(GoaldayDesign.RadiusL))
            .background(Brush.verticalGradient(listOf(GoaldayDesign.adaptiveSurface, GoaldayDesign.adaptivePaperWarm)))
            .border(GoaldayDesign.Hairline, GoaldayDesign.BorderColor.copy(alpha = 0.14f), RoundedCornerShape(GoaldayDesign.RadiusL))
            .padding(horizontal = GoaldayDesign.Space2 + 2.dp, vertical = GoaldayDesign.Space2),
        verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space1 + 2.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(title, style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.adaptiveInkSecondary, fontWeight = FontWeight.SemiBold, maxLines = 1)
                Text(subtitle, style = MaterialTheme.typography.titleSmall, color = GoaldayDesign.DiaryTimeInk, fontWeight = FontWeight.SemiBold, maxLines = 1)
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text("书内日记", style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.adaptiveInkMuted, maxLines = 1)
                Text("$blockCount 条目 · $imageCount 图片", style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.adaptiveInkMuted, maxLines = 1)
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Brush.horizontalGradient(listOf(GoaldayDesign.BorderColor.copy(alpha = 0f), GoaldayDesign.BorderColor.copy(alpha = 0.27f), GoaldayDesign.BorderColor.copy(alpha = 0f)))),
        )
    }
}

@Composable
private fun DiaryEmptyInBookPage(onAddImage: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(GoaldayDesign.RadiusM))
            .background(GoaldayDesign.Paper)
            .border(GoaldayDesign.Hairline, GoaldayDesign.BorderColor.copy(alpha = 0.12f), RoundedCornerShape(GoaldayDesign.RadiusM))
            .clickable { onAddImage() }
            .padding(horizontal = GoaldayDesign.Space3, vertical = GoaldayDesign.Space4),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space2 - 1.dp),
    ) {
        Text("今日还没有日记", style = MaterialTheme.typography.labelLarge, color = GoaldayDesign.adaptiveInkPrimary, fontWeight = FontWeight.SemiBold)
        Text("点击添加图片，或进入编辑补充文字、目标和专题条目。", style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.adaptiveInkMuted, textAlign = TextAlign.Center)
    }
}

@Composable
private fun DiaryMoodRibbon(items: List<String>) {
    Row(horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space1 + 1.dp), modifier = Modifier.fillMaxWidth()) {
        items.take(4).forEach { item ->
            Text(
                "#$item",
                style = MaterialTheme.typography.labelSmall,
                color = GoaldayDesign.TagMauve,
                maxLines = 1,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
                    .background(GoaldayDesign.PinkTint)
                    .border(GoaldayDesign.Hairline, GoaldayDesign.Pink.copy(alpha = 0.14f), RoundedCornerShape(GoaldayDesign.RadiusPill))
                    .padding(horizontal = GoaldayDesign.Space2 - 1.dp, vertical = GoaldayDesign.Space1),
            )
        }
    }
}

@Composable
private fun DiaryMediaMosaic(
    imageUris: List<String>,
    notes: List<String>,
) {
    Column(verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space1 + 1.dp), modifier = Modifier.fillMaxWidth()) {
        DiaryImageStrip(imageUris = imageUris, onRemoveImage = null)
        if (notes.isNotEmpty()) {
            DiaryPhotoNoteGrid(notes = notes)
        }
    }
}

@Composable
private fun DiaryPhotoNoteGrid(notes: List<String>) {
    Row(horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space1 + 1.dp), modifier = Modifier.fillMaxWidth()) {
        notes.take(3).forEach { note ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(58.dp)
                    .clip(RoundedCornerShape(GoaldayDesign.RadiusS))
                    .background(GoaldayDesign.adaptiveSurfaceSoft)
                    .border(GoaldayDesign.Hairline, GoaldayDesign.BorderColor.copy(alpha = 0.5f), RoundedCornerShape(GoaldayDesign.RadiusS))
                    .padding(GoaldayDesign.Space1 + 2.dp),
            ) {
                Text(note, style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.adaptiveInkSecondary, maxLines = 3)
            }
        }
        repeat((3 - notes.take(3).size).coerceAtLeast(0)) {
            Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
private fun DiaryInBookRow(
    code: String,
    title: String,
    content: String,
    type: DiaryBlockType,
) {
    val color = diaryBlockTypeColor(type)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(GoaldayDesign.RadiusS))
            .background(diaryBlockTypeBackground(type))
            .border(GoaldayDesign.Hairline, color.copy(alpha = 0.22f), RoundedCornerShape(GoaldayDesign.RadiusS))
            .padding(horizontal = GoaldayDesign.Space2 - 1.dp, vertical = GoaldayDesign.Space1 + 2.dp),
        horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space2 - 1.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            modifier = Modifier
                .width(48.dp)
                .clip(RoundedCornerShape(GoaldayDesign.RadiusS))
                .background(color.copy(alpha = 0.13f))
                .padding(horizontal = GoaldayDesign.Space1, vertical = GoaldayDesign.Space1),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(code, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.SemiBold, maxLines = 1)
            Text("内页", style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.adaptiveInkMuted, maxLines = 1)
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space1 / 2)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.SemiBold)
                Text(diaryBlockDisplayTitle(type), style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.adaptiveInkMuted, maxLines = 1)
            }
            Text(
                content.ifBlank { "暂未填写" },
                style = MaterialTheme.typography.bodySmall,
                color = if (content.isBlank()) GoaldayDesign.adaptiveInkMuted else GoaldayDesign.adaptiveInkPrimary,
                maxLines = 4,
            )
        }
    }
}

// 对照逆向 item_diary_target_in_book.xml / item_diary_topic_target_inbook.xml / item_diary_text.xml
@Composable
internal fun DiaryTypedBlockPreview(
    blocks: List<DiaryEntryBlock>,
) {
    if (blocks.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.fillMaxWidth()) {
        blocks.take(6).forEachIndexed { index, block ->
            when (block.type) {
                DiaryBlockType.TARGET -> DiaryTargetBlockPreview(block)
                DiaryBlockType.TOPIC_TARGET -> DiaryTopicTargetBlockPreview(block)
                DiaryBlockType.TARGET_CHILD -> DiaryTargetChildPreview(block)
                else -> DiaryTextBlockPreviewRow(block)
            }
        }
    }
}

// 对照 item_diary_target_in_book.xml：bg_diary_target 背景，"今日完成"标签(9sp #503311) + 子目标列表
@Composable
private fun DiaryTargetBlockPreview(block: DiaryEntryBlock) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 5.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(GoaldayDesign.DiaryTargetBackground)
            .border(0.5.dp, Color(0x4D000000), RoundedCornerShape(8.dp))
            .padding(bottom = 4.5.dp),
    ) {
        Row(
            modifier = Modifier.padding(start = 8.dp, top = 4.5.dp, bottom = 1.5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.ic_reward),
                contentDescription = null,
                modifier = Modifier.size(10.dp),
                contentScale = ContentScale.Fit,
            )
            Text(
                if (block.style == DiaryBlockStyle.CHECK) "今日完成" else "今日待办",
                fontSize = 9.sp,
                color = Color(0xFF503311),
                fontWeight = FontWeight.SemiBold,
            )
        }
        if (block.mainText.isBlank() && block.childLines.isEmpty()) {
            Text(
                "这里会自动记录清单中完成的事项。",
                fontSize = 9.sp,
                color = GoaldayDesign.adaptiveInkMuted,
                modifier = Modifier.padding(start = 5.dp, bottom = 2.dp),
            )
        } else {
            if (block.mainText.isNotBlank()) {
                DiaryTargetChildRow(text = block.mainText)
            }
            block.childLines.take(4).forEach { child ->
                DiaryTargetChildRow(text = child)
            }
        }
    }
}

// 对照 item_diary_target_child_inbook.xml：12dp 高，2.5dp 圆点 + 文字
// paddingStart=5dp, dot marginStart/End=4dp
@Composable
private fun DiaryTargetChildRow(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(12.dp)
            .padding(start = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .padding(start = 4.dp, end = 4.dp)
                .size(2.5.dp)
                .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
                .background(GoaldayDesign.DiaryTargetChildDot),
        )
        Text(
            text,
            fontSize = 9.sp,
            color = GoaldayDesign.adaptiveInkPrimary,
            maxLines = 1,
        )
    }
}

// 对照 item_diary_topic_target_inbook.xml：bg_diary_topic_target 黑色背景，8sp 白色标题 + 9sp 副标题
@Composable
private fun DiaryTopicTargetBlockPreview(block: DiaryEntryBlock) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 5.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black)
            .padding(horizontal = 5.dp)
            .padding(bottom = 3.5.dp),
    ) {
        Text(
            block.mainText.ifBlank { "专题目标" },
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 1,
            modifier = Modifier.padding(top = 5.dp, bottom = 3.5.dp),
        )
        if (block.childLines.isNotEmpty()) {
            Text(
                block.childLines.first(),
                fontSize = 9.sp,
                color = Color(0x9CFFFFFF),
                maxLines = 1,
                modifier = Modifier.padding(bottom = 3.5.dp),
            )
        }
    }
}

// 对照 item_diary_target_child_inbook.xml：单独子目标预览
@Composable
private fun DiaryTargetChildPreview(block: DiaryEntryBlock) {
    DiaryTargetChildRow(text = block.mainText.ifBlank { "下一步行动" })
}

// 对照 item_diary_text.xml：16sp #2C2C2C 文字块，lineSpacingExtra="2.0dip"
@Composable
private fun DiaryTextBlockPreviewRow(block: DiaryEntryBlock) {
    Text(
        block.mainText.ifBlank { "空内容" },
        fontSize = 16.sp,
        color = GoaldayDesign.DiarySectionInk,
        lineHeight = 18.sp,  // 16sp + 2dp 行距
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun DiaryInBookTypeMarker(
    type: DiaryBlockType,
    index: Int,
) {
    val color = diaryBlockTypeColor(type)
    Column(
        modifier = Modifier
            .width(50.dp)
            .clip(RoundedCornerShape(GoaldayDesign.RadiusS))
            .background(GoaldayDesign.adaptiveWhiteOverlayMedium)
            .border(GoaldayDesign.Hairline, color.copy(alpha = 0.28f), RoundedCornerShape(GoaldayDesign.RadiusS))
            .padding(horizontal = GoaldayDesign.Space1 + 1.dp, vertical = GoaldayDesign.Space1 + 1.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space1 / 2),
    ) {
        Icon(diaryBlockTypeIcon(type), contentDescription = null, modifier = Modifier.size(14.dp), tint = color)
        Text("条目 %02d".format(index), color = GoaldayDesign.adaptiveInkMuted, style = MaterialTheme.typography.labelSmall, maxLines = 1)
    }
}

@Composable
private fun DiaryChildPreviewRow(
    index: Int,
    text: String,
    color: Color,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space1 + 2.dp),
        verticalAlignment = Alignment.Top,
        modifier = Modifier
            .padding(start = 57.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(GoaldayDesign.RadiusS))
            .background(GoaldayDesign.adaptiveWhiteOverlayLow)
            .padding(horizontal = GoaldayDesign.Space2 - 1.dp, vertical = GoaldayDesign.Space1),
    ) {
        Text("%02d".format(index), style = MaterialTheme.typography.labelSmall, color = color.copy(alpha = 0.72f), maxLines = 1)
        Text(text, style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.adaptiveInkSecondary, modifier = Modifier.weight(1f))
    }
}

private fun diaryInBookItemLabel(type: DiaryBlockType): String =
    when (type) {
        DiaryBlockType.IMAGE -> "书内图片条目"
        DiaryBlockType.TEXT -> "书内文字条目"
        DiaryBlockType.TARGET,
        DiaryBlockType.TARGET_IN_BOOK -> "书内目标条目"
        DiaryBlockType.TARGET_CHILD,
        DiaryBlockType.TARGET_CHILD_IN_BOOK -> "书内子目标"
        DiaryBlockType.TOPIC_TARGET,
        DiaryBlockType.TOPIC_TARGET_IN_BOOK -> "书内专题目标"
    }

@Composable
internal fun DiaryImageStrip(
    imageUris: List<String>,
    onRemoveImage: ((String) -> Unit)?,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space1 + 2.dp), modifier = Modifier.fillMaxWidth()) {
        imageUris.take(3).forEach { uri ->
            DiaryImageTile(
                uri = uri,
                onRemove = onRemoveImage?.let { remove -> { remove(uri) } },
                modifier = Modifier.weight(1f),
            )
        }
        repeat((3 - imageUris.take(3).size).coerceAtLeast(0)) {
            Spacer(Modifier.weight(1f))
        }
    }
}

// 对照逆向 item_diary_img.xml：图片块水平内边距 paddingStart/End=5dip，高度自适应保持比例
@Composable
internal fun DiaryImageTile(
    uri: String,
    onRemove: (() -> Unit)?,
    modifier: Modifier = Modifier,
    fixedHeight: Boolean = true,
) {
    val context = LocalContext.current
    val bitmap = remember(uri) {
        runCatching {
            context.contentResolver.openInputStream(Uri.parse(uri))?.use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        }.getOrNull()
    }
    val aspectRatio = remember(bitmap) {
        if (bitmap != null && bitmap.width > 0 && bitmap.height > 0) {
            bitmap.width.toFloat() / bitmap.height.toFloat()
        } else {
            1f
        }
    }
    Box(
        modifier = modifier
            .then(if (fixedHeight) Modifier.height(76.dp) else Modifier.padding(horizontal = 5.dp))
            .then(if (!fixedHeight && bitmap != null) Modifier.aspectRatio(aspectRatio) else Modifier)
            .clip(RoundedCornerShape(GoaldayDesign.RadiusS))
            .background(GoaldayDesign.adaptiveSurfaceSoft)
            .border(GoaldayDesign.Hairline, GoaldayDesign.BorderColor.copy(alpha = 0.5f), RoundedCornerShape(GoaldayDesign.RadiusS)),
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                contentScale = if (fixedHeight) ContentScale.Crop else ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Text(
                "图片不可读",
                style = MaterialTheme.typography.labelSmall,
                color = GoaldayDesign.adaptiveInkMuted,
                modifier = Modifier.align(Alignment.Center),
            )
        }
        onRemove?.let { remove ->
            Text(
                "删除",
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .background(GoaldayDesign.ImageRemoveScrim, RoundedCornerShape(bottomStart = GoaldayDesign.RadiusS))
                    .clickable { remove() }
                    .padding(horizontal = GoaldayDesign.Space1 + 2.dp, vertical = 2.dp),
            )
        }
    }
}

@Composable
private fun DiaryBlock(title: String, content: String) {
    if (content.isBlank()) return
    DiarySticker(title)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(GoaldayDesign.RadiusM))
            .background(
                Brush.verticalGradient(
                    listOf(GoaldayDesign.AiInsightStart, GoaldayDesign.AiInsightMid, GoaldayDesign.AiInsightEnd),
                ),
            )
            .border(GoaldayDesign.Hairline, GoaldayDesign.AiInsightBorder, RoundedCornerShape(GoaldayDesign.RadiusM))
            .padding(horizontal = GoaldayDesign.Space2 + 1.dp, vertical = GoaldayDesign.Space2 - 1.dp),
    ) {
        Text(content, style = MaterialTheme.typography.bodyMedium, color = GoaldayDesign.DiarySectionInk)
    }
}

@Composable
private fun DiaryLine(title: String, content: String) {
    if (content.isBlank()) return
    Text(title, style = MaterialTheme.typography.labelMedium, color = GoaldayDesign.adaptiveInkSecondary)
    content.lines().map(String::trim).filter(String::isNotBlank).take(3).forEachIndexed { index, line ->
        Text("${index + 1}. $line", style = MaterialTheme.typography.bodySmall, color = GoaldayDesign.adaptiveInkPrimary)
    }
}

@Composable
internal fun DiaryEditField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
) {
    Text(label, style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.adaptiveInkSecondary)
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        minLines = 1,
        maxLines = 3,
        shape = RoundedCornerShape(GoaldayDesign.RadiusS),
    )
}

@Composable
private fun DiarySticker(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = GoaldayDesign.adaptiveInkSecondary,
        modifier = Modifier
            .clip(RoundedCornerShape(GoaldayDesign.RadiusS))
            .background(GoaldayDesign.adaptiveSurfaceSoft.copy(alpha = 0.20f))
            .padding(horizontal = GoaldayDesign.Space2, vertical = GoaldayDesign.Space1),
    )
}

@Composable
private fun SectionField(value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        minLines = 2,
        maxLines = 5,
    )
}

@Composable
internal fun PaperNoteCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space1),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(GoaldayDesign.RadiusM))
            .background(GoaldayDesign.adaptiveSurfaceSoft)
            .border(GoaldayDesign.Hairline, GoaldayDesign.BorderColor.copy(alpha = 0.5f), RoundedCornerShape(GoaldayDesign.RadiusM))
            .padding(horizontal = GoaldayDesign.Space2 + 2.dp, vertical = GoaldayDesign.Space2 + 1.dp),
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(GoaldayDesign.adaptiveDivider))
        content()
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(GoaldayDesign.adaptiveDivider))
    }
}

@Composable
private fun PageHeaderLine(
    bookTitle: String,
    subtitle: String,
    tint: Color,
    savedText: String,
    onSavedClick: (() -> Unit)? = null,
) {
    Column(verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space1)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(GoaldayDesign.RadiusPill)).background(tint))
                Spacer(Modifier.width(GoaldayDesign.Space1 + 2.dp))
                Text(bookTitle, style = MaterialTheme.typography.titleSmall, color = GoaldayDesign.adaptiveInkPrimary)
            }
            Text(
                text = savedText,
                style = MaterialTheme.typography.labelSmall,
                color = GoaldayDesign.adaptiveInkMuted,
                modifier = if (onSavedClick != null) Modifier.clickable(onClick = onSavedClick) else Modifier,
            )
        }
        Text(subtitle, style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.adaptiveInkMuted)
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(GoaldayDesign.adaptiveDivider))
    }
}

@Composable
private fun DiaryToolChip(
    label: String,
    onClick: () -> Unit,
) {
    Text(
        text = label,
        color = GoaldayDesign.adaptiveInkSecondary,
        modifier = Modifier
            .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
            .background(GoaldayDesign.adaptiveSurfaceSoft)
            .clickable(onClick = onClick)
            .padding(horizontal = GoaldayDesign.Space3, vertical = GoaldayDesign.Space2),
    )
}
