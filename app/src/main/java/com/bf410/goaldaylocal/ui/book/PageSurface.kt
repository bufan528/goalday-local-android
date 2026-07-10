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
    // spine：颜色 token 化，与深棕书皮（BookSpine）统一，原硬编码 0xFF6E4229/0xFFF8E8D5 收敛
    val baseWidth = if (profile == TurnProfile.HANDBOOK) 28.dp else 20.dp
    Box(
        modifier = Modifier
            .align(Alignment.Center)
            .width(baseWidth)
            .fillMaxHeight()
            .background(
                Brush.horizontalGradient(
                    listOf(
                        GoaldayDesign.BookSpine.copy(alpha = if (active) 0.92f else 0.78f),
                        GoaldayDesign.PaperWarm,
                        GoaldayDesign.BookSpine.copy(alpha = if (active) 0.92f else 0.78f),
                    ),
                ),
            ),
    )

    if (active) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .width((if (profile == TurnProfile.HANDBOOK) 22f else 16f + visualProgress * 10f).dp)
                .fillMaxHeight()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color.Black.copy(alpha = (0.10f + visualProgress * 0.22f).coerceAtMost(0.3f)),
                            Color.Transparent,
                            Color.Black.copy(alpha = (0.10f + visualProgress * 0.22f).coerceAtMost(0.3f)),
                        ),
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .width((4f + visualProgress * if (profile == TurnProfile.HANDBOOK) 11f else 7f).dp)
                .fillMaxHeight()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            GoaldayDesign.PaperWarm.copy(alpha = (0.24f + visualProgress * 0.24f).coerceAtMost(0.46f)),
                            GoaldayDesign.adaptiveDivider,
                            GoaldayDesign.PaperWarm.copy(alpha = (0.24f + visualProgress * 0.24f).coerceAtMost(0.46f)),
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
    // P1-6 大修：原 8 层装饰 Box（边缘高光、角部 radial、linear 高光等）造成"花斑纸"视觉过载
    // 精简为 3 层核心：基底纸渐变 + 中央折痕阴影 + 单一角部柔和阴影
    val curlAlignTop = anchorY < 0.46f
    val easedCurl = progress * progress * (3f - 2f * progress)
    val curlStrength = (0.12f + easedCurl * 0.76f).coerceIn(0f, 0.88f)
    val stackShadow = (0.07f + progress * 0.18f).coerceAtMost(0.30f)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(GoaldayDesign.Radius2XL, GoaldayDesign.Radius2XL, GoaldayDesign.Radius3XL, GoaldayDesign.Radius3XL))
            .background(
                Brush.horizontalGradient(
                    if (direction == TurnDirection.NEXT) {
                        listOf(GoaldayDesign.PageTurnEdgeStart, GoaldayDesign.PageTurnEdgeMid, GoaldayDesign.adaptiveSurface)
                    } else {
                        listOf(GoaldayDesign.adaptiveSurface, GoaldayDesign.PageTurnEdgeMid, GoaldayDesign.PageTurnEdgeStart)
                    },
                ),
            )
            .padding(horizontal = GoaldayDesign.Space6 + 4.dp, vertical = GoaldayDesign.Space6 + 2.dp),
    ) {
        // 层 1：中央折痕阴影（翻页时纸张弯折的暗带，方向跟随翻页方向）
        Box(
            modifier = Modifier
                .align(if (direction == TurnDirection.NEXT) Alignment.CenterStart else Alignment.CenterEnd)
                .width((12f + progress * 24f).dp)
                .fillMaxHeight()
                .background(
                    Brush.horizontalGradient(
                        if (direction == TurnDirection.NEXT) {
                            listOf(
                                Color.Black.copy(alpha = stackShadow),
                                Color.White.copy(alpha = (0.10f + progress * 0.14f).coerceAtMost(0.22f)),
                                Color.Transparent,
                            )
                        } else {
                            listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = (0.10f + progress * 0.14f).coerceAtMost(0.22f)),
                                Color.Black.copy(alpha = stackShadow),
                            )
                        },
                    ),
                ),
        )
        // 层 2：单一角部柔和阴影（合并原 4 层角部装饰），模拟纸张翻起的背光
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
                            Color.Black.copy(alpha = (curlStrength * 0.20f).coerceAtMost(0.28f)),
                            Color.Transparent,
                        ),
                        radius = 260f,
                    ),
                ),
        )

        Column(modifier = Modifier.fillMaxSize()) {
            PageHeaderLine(
                bookTitle = BookStrings.pageBack,
                subtitle = BookStrings.pageTurning,
                tint = tint.copy(alpha = 0.56f),
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
) {
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
        // 底部图片按钮栏（对照 fl_bottom_bar: 23pt=51.11dp 白底，fl_select_pic 23pt=51.11dp，aapt2 验证为 pt）
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(51.11.dp)
                .background(GoaldayDesign.adaptiveSurface),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .padding(start = 8.33.dp)  // 3.75pt=8.33dp（aapt2 验证为 pt）
                    .size(51.11.dp)
                    .clickable { onContentModeChange(PageContentMode.EditingDiary(title)) },
                contentAlignment = Alignment.Center,
            ) {
                // 对照 ic_select_pic：图片选择图标 12.5pt=27.78dp（aapt2 验证为 pt）
                Image(
                    painter = painterResource(R.drawable.ic_select_pic),
                    contentDescription = "插入图片",
                    modifier = Modifier.size(27.78.dp),
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun EditableBulletPage(
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

// 对照逆向 fragment_plan.xml：右下角独立浮动按钮，添加按钮在上(marginBottom=93dp)，提示按钮在下(marginBottom=32dp)
@Composable
private fun PlannerFloatingActionStrip(
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
private fun PlannerLedgerSummary(
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
private fun ReferencePlannerBoard(
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

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun DiarySection(
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
    // 对照 fragment_diary.xml：底栏 bg=#E5DAD4，高 46pt=102.22dp
    // 书内日记页 fragment_diary_inbook.xml：底栏 23pt=51.11dp，仅 ic_select_pic 6.25pt=13.89dp
    val toolbarHeight = if (inBook) 51.11.dp else 102.22.dp
    val iconSize = if (inBook) 13.89.dp else 55.56.dp
    val contentPadding = if (inBook) 4.17.dp else 8.33.dp
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

internal fun exportHandbookScheduleLongImage(
    context: Context,
    year: Int,
    month: Int,
    days: List<Int>,
    entries: List<ScheduleEntry>,
    weeklyTheme: String,
): Uri? = runCatching {
    val bitmap = renderHandbookScheduleLongImage(year, month, days, entries, weeklyTheme)
    saveBitmapToPictures(context, bitmap, "Goalday_schedule_${System.currentTimeMillis()}.png")
}.getOrNull()

internal data class LongImagePreview(
    val title: String,
    val subtitle: String,
    val filePrefix: String,
    val bitmap: Bitmap,
)

private data class LongImageExportHistoryItem(
    val action: String,
    val title: String,
    val preset: String,
    val detail: String,
    val createdAtMillis: Long,
)

private fun saveLongImagePreview(context: Context, preview: LongImagePreview): Uri? =
    saveBitmapToPictures(context, preview.bitmap, "${preview.filePrefix}_${System.currentTimeMillis()}.png")

private fun shareLongImagePreview(context: Context, preview: LongImagePreview): Boolean {
    val uri = saveLongImagePreview(context, preview) ?: return false
    return shareLongImage(context, uri)
}

private enum class LongImageExportPreset(
    val label: String,
    val description: String,
    val paperLabel: String,
    val mediaSize: PrintAttributes.MediaSize,
    val previewInset: Int,
) {
    LONG("长图", "原始比例 · 适合保存分享", "长图", PrintAttributes.MediaSize.UNKNOWN_PORTRAIT, 0),
    PHONE("手机", "9:16 预览 · 适合发到社交软件", "手机", PrintAttributes.MediaSize.NA_LETTER, 10),
    PRINT("打印", "A4 PDF · 适合纸质手账", "A4", PrintAttributes.MediaSize.ISO_A4, 22),
}

private enum class LongImageShortcutMode(
    val raw: String,
    val label: String,
    val description: String,
    val preset: LongImageExportPreset?,
) {
    DISABLED("disabled", "关闭", "shortcut_print_export_disabled", null),
    LONG("long", "长图", "shortcut_print_export_long", LongImageExportPreset.LONG),
    SHORT("short", "短图", "shortcut_print_export_short", LongImageExportPreset.PHONE),
    SHORT_1("short_1", "短图 1", "shortcut_print_export_short_1", LongImageExportPreset.PHONE),
    SHORT_2("short_2", "短图 2", "shortcut_print_export_short_2", LongImageExportPreset.PRINT),
}

private const val KEY_LONG_IMAGE_EXPORT_HISTORY = "long_image_export_history"
private const val KEY_LONG_IMAGE_SHORTCUT_MODE = "long_image_shortcut_mode"
private val longImageHistoryFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MM-dd HH:mm")

private fun loadLongImageShortcutMode(): LongImageShortcutMode {
    val raw = MMKV.defaultMMKV().decodeString(KEY_LONG_IMAGE_SHORTCUT_MODE, LongImageShortcutMode.LONG.raw)
    return LongImageShortcutMode.entries.firstOrNull { it.raw == raw } ?: LongImageShortcutMode.LONG
}

private fun saveLongImageShortcutMode(mode: LongImageShortcutMode) {
    MMKV.defaultMMKV().encode(KEY_LONG_IMAGE_SHORTCUT_MODE, mode.raw)
}

private fun loadLongImageExportHistory(): List<LongImageExportHistoryItem> {
    val raw = MMKV.defaultMMKV().decodeString(KEY_LONG_IMAGE_EXPORT_HISTORY, "[]") ?: "[]"
    val array = runCatching { JSONArray(raw) }.getOrElse { JSONArray() }
    return buildList {
        repeat(array.length()) { index ->
            val json = array.optJSONObject(index) ?: return@repeat
            add(
                LongImageExportHistoryItem(
                    action = json.optString("action").ifBlank { "导出" },
                    title = json.optString("title").ifBlank { "Goalday 长图" },
                    preset = json.optString("preset").ifBlank { "长图" },
                    detail = json.optString("detail"),
                    createdAtMillis = json.optLong("createdAtMillis", 0L),
                ),
            )
        }
    }
}

private fun appendLongImageExportHistory(
    preview: LongImagePreview,
    preset: LongImageExportPreset,
    action: String,
    detail: String = "",
) {
    val updated = (
        listOf(
            LongImageExportHistoryItem(
                action = action,
                title = preview.title,
                preset = preset.label,
                detail = detail,
                createdAtMillis = System.currentTimeMillis(),
            ),
        ) + loadLongImageExportHistory()
    ).take(12)
    val array = JSONArray()
    updated.forEach { item ->
        array.put(
            JSONObject()
                .put("action", item.action)
                .put("title", item.title)
                .put("preset", item.preset)
                .put("detail", item.detail)
                .put("createdAtMillis", item.createdAtMillis),
        )
    }
    MMKV.defaultMMKV().encode(KEY_LONG_IMAGE_EXPORT_HISTORY, array.toString())
}

private fun LongImageExportHistoryItem.displayTime(): String {
    if (createdAtMillis <= 0L) return "刚刚"
    return Instant.ofEpochMilli(createdAtMillis)
        .atZone(ZoneId.systemDefault())
        .format(longImageHistoryFormatter)
}

private fun printLongImagePreview(context: Context, preview: LongImagePreview, preset: LongImageExportPreset): Boolean =
    runCatching {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
        printManager.print(
            preview.title.ifBlank { "Goalday 长图" },
            BitmapPrintDocumentAdapter(preview.title, preview.bitmap),
            PrintAttributes.Builder()
                .setMediaSize(preset.mediaSize)
                .setColorMode(PrintAttributes.COLOR_MODE_COLOR)
                .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                .build(),
        )
        true
    }.getOrDefault(false)

private class BitmapPrintDocumentAdapter(
    private val title: String,
    private val bitmap: Bitmap,
) : PrintDocumentAdapter() {
    override fun onLayout(
        oldAttributes: PrintAttributes?,
        newAttributes: PrintAttributes?,
        cancellationSignal: CancellationSignal?,
        callback: LayoutResultCallback,
        extras: android.os.Bundle?,
    ) {
        if (cancellationSignal?.isCanceled == true) {
            callback.onLayoutCancelled()
            return
        }
        callback.onLayoutFinished(
            PrintDocumentInfo.Builder("${title.ifBlank { "Goalday" }}.pdf")
                .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                .setPageCount(1)
                .build(),
            true,
        )
    }

    override fun onWrite(
        pages: Array<out PageRange>?,
        destination: ParcelFileDescriptor?,
        cancellationSignal: CancellationSignal?,
        callback: WriteResultCallback,
    ) {
        if (destination == null || cancellationSignal?.isCanceled == true) {
            callback.onWriteCancelled()
            return
        }
        runCatching {
            val document = PdfDocument()
            try {
                val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create()
                val page = document.startPage(pageInfo)
                page.canvas.drawBitmap(bitmap, 0f, 0f, null)
                document.finishPage(page)
                FileOutputStream(destination.fileDescriptor).use { output ->
                    document.writeTo(output)
                }
            } finally {
                document.close()
            }
            callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
        }.onFailure {
            callback.onWriteFailed(it.message ?: "打印失败")
        }
    }
}

@Composable
internal fun LongImagePreviewDialog(
    preview: LongImagePreview,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    var actionHint by remember(preview) { mutableStateOf("") }
    var selectedPreset by remember(preview) { mutableStateOf(LongImageExportPreset.LONG) }
    var shortcutMode by remember(preview) { mutableStateOf(loadLongImageShortcutMode()) }
    var exportHistory by remember(preview) { mutableStateOf(loadLongImageExportHistory()) }
    fun recordAction(message: String, action: String, detail: String = "") {
        actionHint = message
        appendLongImageExportHistory(preview, selectedPreset, action, detail)
        exportHistory = loadLongImageExportHistory()
    }
    LaunchedEffect(actionHint) {
        if (actionHint.isNotBlank()) {
            delay(1400)
            actionHint = ""
        }
    }
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(GoaldayDesign.adaptiveSurface, GoaldayDesign.adaptivePaperWarm, GoaldayDesign.ExportPaperWarm),
                    ),
                )
                .padding(horizontal = GoaldayDesign.Space3, vertical = GoaldayDesign.Space3 + 2.dp),
            verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space2 + 2.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GoaldayDesign.adaptiveSurface.copy(alpha = 0.87f))
                    .border(GoaldayDesign.Hairline, GoaldayDesign.BorderColor.copy(alpha = 0.09f), RoundedCornerShape(bottomStart = GoaldayDesign.Radius2XL, bottomEnd = GoaldayDesign.Radius2XL))
                    .padding(horizontal = GoaldayDesign.Space3 + 2.dp, vertical = GoaldayDesign.Space3 - 1.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "‹ 返回",
                    style = MaterialTheme.typography.labelSmall,
                    color = GoaldayDesign.adaptiveInkSecondary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
                        .background(GoaldayDesign.BorderColor.copy(alpha = 0.06f))
                        .clickable { onDismiss() }
                        .padding(horizontal = GoaldayDesign.Space2 + 2.dp, vertical = GoaldayDesign.Space1 + 2.dp),
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space1 / 2), modifier = Modifier.weight(1f)) {
                    Text("长图预览", style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.adaptiveInkSecondary, fontWeight = FontWeight.SemiBold)
                    Text(preview.title, style = MaterialTheme.typography.titleMedium, color = GoaldayDesign.adaptiveInkPrimary, fontWeight = FontWeight.SemiBold)
                    Text(preview.subtitle, style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.adaptiveInkMuted)
                }
                Text(
                    "打印预设",
                    style = MaterialTheme.typography.labelSmall,
                    color = GoaldayDesign.adaptiveInkMuted,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space2),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LongImageInfoPill("长图", "${preview.bitmap.width}×${preview.bitmap.height}")
                LongImageInfoPill("格式", "PNG")
                LongImageInfoPill("预设", selectedPreset.label)
                LongImageInfoPill("快捷", shortcutMode.label)
                LongImageInfoPill("记录", "${exportHistory.size}条")
            }
            LongImagePrintPanel(
                preset = selectedPreset,
                preview = preview,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space2),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LongImageExportPreset.entries.forEach { preset ->
                    LongImagePresetChip(
                        preset = preset,
                        selected = preset == selectedPreset,
                        onClick = { selectedPreset = preset },
                    )
                }
            }
            LongImageShortcutPanel(
                mode = shortcutMode,
                onSelect = { mode ->
                    shortcutMode = mode
                    saveLongImageShortcutMode(mode)
                    mode.preset?.let { selectedPreset = it }
                    actionHint = if (mode == LongImageShortcutMode.DISABLED) "已关闭快捷导出" else "已设置快捷导出：${mode.label}"
                },
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(GoaldayDesign.RadiusXL))
                    .background(GoaldayDesign.adaptiveSurfaceSoft)
                    .border(GoaldayDesign.Hairline, GoaldayDesign.BorderColor.copy(alpha = 0.13f), RoundedCornerShape(GoaldayDesign.RadiusXL))
                    .verticalScroll(rememberScrollState()),
            ) {
                // P1-3 修复：长图预览滚动卡顿
                // 原因：超大 bitmap（高度可能超 8000px）直接 asImageBitmap() 渲染，超出 GPU 纹理上限
                // （多数设备 4096px）触发软件渲染；且每次重组重建 ImageBitmap 包装
                // 修复：1) remember 缓存 ImageBitmap 避免重组重建
                //       2) 等比缩小到预览安全高度（保留长宽比，保存/导出仍用原图全分辨率）
                val previewImageBitmap = remember(preview.bitmap) {
                    val maxPreviewHeight = 4096
                    val src = preview.bitmap
                    if (src.height > maxPreviewHeight) {
                        val scale = maxPreviewHeight.toFloat() / src.height
                        Bitmap.createScaledBitmap(
                            src,
                            (src.width * scale).toInt().coerceAtLeast(1),
                            maxPreviewHeight,
                            true,
                        ).asImageBitmap()
                    } else {
                        src.asImageBitmap()
                    }
                }
                Image(
                    bitmap = previewImageBitmap,
                    contentDescription = null,
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(selectedPreset.previewInset.dp),
                )
            }
            if (exportHistory.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(GoaldayDesign.RadiusL))
                        .background(GoaldayDesign.adaptiveWhiteOverlayMedium)
                        .border(GoaldayDesign.Hairline, GoaldayDesign.BorderColor.copy(alpha = 0.09f), RoundedCornerShape(GoaldayDesign.RadiusL))
                        .padding(GoaldayDesign.Space2 + 1.dp),
                    verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space1 + 2.dp),
                ) {
                    Text("最近导出", style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.adaptiveInkMuted, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space1 + 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        exportHistory.take(6).forEach { item ->
                            LongImageHistoryChip(item)
                        }
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(GoaldayDesign.Radius2XL))
                    .background(GoaldayDesign.adaptiveWhiteOverlay)
                    .border(GoaldayDesign.Hairline, GoaldayDesign.BorderColor.copy(alpha = 0.13f), RoundedCornerShape(GoaldayDesign.Radius2XL))
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = GoaldayDesign.Space2 + 2.dp, vertical = GoaldayDesign.Space2 + 2.dp),
                horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space2 - 1.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LongImageActionChip("保存", GoaldayDesign.Positive, Modifier.width(86.dp)) {
                    val uri = saveLongImagePreview(context, preview)
                    if (uri != null) {
                        recordAction("已保存到相册", "保存", uri.lastPathSegment.orEmpty())
                    } else {
                        actionHint = "保存失败"
                    }
                }
                LongImageActionChip("分享", GoaldayDesign.RouteDiary, Modifier.width(86.dp)) {
                    if (shareLongImagePreview(context, preview)) {
                        recordAction("已打开分享", "分享", selectedPreset.description)
                    } else {
                        actionHint = "分享失败"
                    }
                }
                LongImageActionChip("打印", GoaldayDesign.adaptiveInkSecondary, Modifier.width(86.dp)) {
                    if (printLongImagePreview(context, preview, selectedPreset)) {
                        recordAction("已打开${selectedPreset.label}打印", "打印", selectedPreset.description)
                    } else {
                        actionHint = "打印失败"
                    }
                }
                if (shortcutMode != LongImageShortcutMode.DISABLED) {
                    LongImageActionChip("快捷", GoaldayDesign.Pink, Modifier.width(86.dp)) {
                        val shortcutPreset = shortcutMode.preset ?: selectedPreset
                        selectedPreset = shortcutPreset
                        val uri = saveLongImagePreview(context, preview)
                        if (uri != null) {
                            recordAction("已按${shortcutMode.label}快捷保存", "快捷", shortcutMode.description)
                        } else {
                            actionHint = "快捷导出失败"
                        }
                    }
                }
                if (actionHint.isNotBlank()) {
                    Text(actionHint, style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.adaptiveInkSecondary, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun LongImagePrintPanel(
    preset: LongImageExportPreset,
    preview: LongImagePreview,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(GoaldayDesign.RadiusL))
            .background(GoaldayDesign.adaptiveWhiteOverlayMedium)
            .border(GoaldayDesign.Hairline, GoaldayDesign.BorderColor.copy(alpha = 0.09f), RoundedCornerShape(GoaldayDesign.RadiusL))
            .padding(horizontal = GoaldayDesign.Space2 + 2.dp, vertical = GoaldayDesign.Space2),
        horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space2),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space1 / 2)) {
            Text("导出预设", style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.adaptiveInkSecondary, fontWeight = FontWeight.SemiBold)
            Text(preset.description, style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.adaptiveInkSecondary, maxLines = 1)
        }
        LongImageInfoPill("纸张", preset.paperLabel)
        LongImageInfoPill("比例", if (preset == LongImageExportPreset.PHONE) "9:16" else "${preview.bitmap.width}:${preview.bitmap.height}")
    }
}

@Composable
private fun LongImageShortcutPanel(
    mode: LongImageShortcutMode,
    onSelect: (LongImageShortcutMode) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(GoaldayDesign.RadiusL))
            .background(GoaldayDesign.adaptiveWhiteOverlayMedium)
            .border(GoaldayDesign.Hairline, GoaldayDesign.BorderColor.copy(alpha = 0.09f), RoundedCornerShape(GoaldayDesign.RadiusL))
            .padding(horizontal = GoaldayDesign.Space2 + 2.dp, vertical = GoaldayDesign.Space2),
        verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space2 - 1.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text("快捷导出", style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.adaptiveInkSecondary, fontWeight = FontWeight.SemiBold)
                Text(mode.description, style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.adaptiveInkMuted, maxLines = 1)
            }
            Text(mode.label, style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.adaptiveInkSecondary, fontWeight = FontWeight.SemiBold)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space1 + 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LongImageShortcutMode.entries.forEach { item ->
                LongImageShortcutChip(
                    mode = item,
                    selected = item == mode,
                    onClick = { onSelect(item) },
                )
            }
        }
    }
}

@Composable
private fun LongImageShortcutChip(
    mode: LongImageShortcutMode,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Text(
        mode.label,
        style = MaterialTheme.typography.labelSmall,
        color = if (selected) Color.White else GoaldayDesign.adaptiveInkSecondary,
        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        maxLines = 1,
        modifier = Modifier
            .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
            .background(if (selected) GoaldayDesign.Pink else GoaldayDesign.adaptiveWhiteOverlayMedium)
            .border(GoaldayDesign.Hairline, if (selected) GoaldayDesign.Pink.copy(alpha = 0.32f) else GoaldayDesign.BorderColor.copy(alpha = 0.09f), RoundedCornerShape(GoaldayDesign.RadiusPill))
            .clickable(onClick = onClick)
            .padding(horizontal = GoaldayDesign.Space2 + 2.dp, vertical = GoaldayDesign.Space1 + 1.dp),
    )
}

@Composable
private fun LongImageHistoryChip(item: LongImageExportHistoryItem) {
    Column(
        modifier = Modifier
            .width(142.dp)
            .clip(RoundedCornerShape(GoaldayDesign.RadiusM))
            .background(GoaldayDesign.adaptiveSurface)
            .border(GoaldayDesign.Hairline, GoaldayDesign.BorderColor.copy(alpha = 0.09f), RoundedCornerShape(GoaldayDesign.RadiusM))
            .padding(horizontal = GoaldayDesign.Space2 + 1.dp, vertical = GoaldayDesign.Space2 - 1.dp),
        verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space1 / 2),
    ) {
        Text("${item.action} · ${item.preset}", style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.adaptiveInkPrimary, fontWeight = FontWeight.SemiBold, maxLines = 1)
        Text(item.displayTime(), style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.adaptiveInkMuted, maxLines = 1)
        Text(item.title, style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.adaptiveInkSecondary, maxLines = 1)
    }
}

@Composable
private fun LongImagePresetChip(
    preset: LongImageExportPreset,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .width(138.dp)
            .clip(RoundedCornerShape(GoaldayDesign.RadiusL))
            .background(if (selected) GoaldayDesign.PinkTint else GoaldayDesign.adaptiveWhiteOverlayMedium)
            .border(
                width = if (selected) 1.dp else 0.6.dp,
                color = if (selected) GoaldayDesign.Pink.copy(alpha = 0.36f) else GoaldayDesign.BorderColor.copy(alpha = 0.09f),
                shape = RoundedCornerShape(GoaldayDesign.RadiusL),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = GoaldayDesign.Space2 + 2.dp, vertical = GoaldayDesign.Space2),
        verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space1 / 2),
    ) {
        Text(preset.label, style = MaterialTheme.typography.labelMedium, color = if (selected) GoaldayDesign.Pink else GoaldayDesign.adaptiveInkPrimary, fontWeight = FontWeight.SemiBold)
        Text(preset.description, style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.adaptiveInkMuted, maxLines = 2)
    }
}

@Composable
private fun LongImageInfoPill(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
            .background(GoaldayDesign.adaptiveWhiteOverlayMedium)
            .border(GoaldayDesign.Hairline, GoaldayDesign.BorderColor.copy(alpha = 0.09f), RoundedCornerShape(GoaldayDesign.RadiusPill))
            .padding(horizontal = GoaldayDesign.Space2 + 2.dp, vertical = GoaldayDesign.Space1 + 1.dp),
        horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space1 + 1.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.adaptiveInkMuted)
        Text(value, style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.adaptiveInkPrimary, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun LongImageActionChip(
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Text(
        label,
        style = MaterialTheme.typography.labelSmall,
        color = Color.White,
        fontWeight = FontWeight.SemiBold,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .then(modifier)
            .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
            .background(color)
            .clickable { onClick() }
            .padding(horizontal = GoaldayDesign.Space3 - 1.dp, vertical = GoaldayDesign.Space1 + 2.dp),
    )
}

internal fun renderHandbookScheduleLongImage(
    year: Int,
    month: Int,
    days: List<Int>,
    entries: List<ScheduleEntry>,
    weeklyTheme: String,
): Bitmap {
    val width = 1080
    val padding = 72f
    val contentWidth = width - padding * 2
    val estimatedHeight = 820 + days.size * 420
    val bitmap = Bitmap.createBitmap(width, estimatedHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawColor(GoaldayDesign.ExportCanvasPaper.toArgb())
    val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = GoaldayDesign.ExportInkPrimary.toArgb()
        textSize = 48f
        isFakeBoldText = true
    }
    val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF8B7A68.toInt()
        textSize = 28f
    }
    val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFB07A8F.toInt()
        textSize = 30f
        isFakeBoldText = true
    }
    val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF3A342E.toInt()
        textSize = 30f
    }
    val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFF7EFE6.toInt()
    }
    var y = 86f
    canvas.drawText("Goalday 日程手账", padding, y, titlePaint)
    y += 48f
    val range = days.firstOrNull()?.let { first ->
        val last = days.lastOrNull() ?: first
        "$year 年 $month 月 $first-$last 日"
    } ?: "$year 年 $month 月"
    canvas.drawText(range, padding, y, subtitlePaint)
    y += 54f
    if (weeklyTheme.isNotBlank()) {
        y = drawExportSection(canvas, "本周主题", weeklyTheme, padding, y, contentWidth, labelPaint, bodyPaint, cardPaint)
    }
    days.forEach { day ->
        val dayEntries = entries.filter { it.day == day }
        val todo = dayEntries.filterNot { it.completed }
        val done = dayEntries.filter { it.completed }
        val body = buildString {
            appendLine("todo")
            if (todo.isEmpty()) {
                appendLine("○ 暂无待办")
            } else {
                todo.take(8).forEach { entry ->
                    val time = entry.timeText.takeIf { it.isNotBlank() }?.let { "$it " }.orEmpty()
                    appendLine("○ $time${entry.title}")
                }
            }
            appendLine()
            appendLine("已完成")
            if (done.isEmpty()) {
                appendLine("✓ 暂无完成")
            } else {
                done.take(8).forEach { entry ->
                    val time = entry.timeText.takeIf { it.isNotBlank() }?.let { "$it " }.orEmpty()
                    appendLine("✓ $time${entry.title}")
                }
            }
        }
        y = drawExportSection(canvas, "${month}月${day}日", body.trim(), padding, y, contentWidth, labelPaint, bodyPaint, cardPaint)
    }
    val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFB7A893.toInt()
        textSize = 24f
    }
    y += 42f
    canvas.drawText("Goalday Local", padding, y, footerPaint)
    return Bitmap.createBitmap(bitmap, 0, 0, width, (y + 72f).toInt().coerceAtMost(bitmap.height))
}

private fun exportDiaryLongImage(
    context: Context,
    title: String,
    state: StructuredDiary,
): Uri? = runCatching {
    val bitmap = renderDiaryLongImage(context, title, state)
    saveBitmapToPictures(context, bitmap, "Goalday_${System.currentTimeMillis()}.png")
}.getOrNull()

private fun shareLongImage(context: Context, uri: Uri): Boolean =
    runCatching {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "分享 Goalday 长图"))
        true
    }.getOrDefault(false)

private fun renderDiaryLongImage(
    context: Context,
    title: String,
    state: StructuredDiary,
): Bitmap {
    val width = 1080
    val padding = 72f
    val contentWidth = width - padding * 2
    val exportImageUris = (state.imageBlockUris + state.legacyImageUris).distinct()
    val estimatedHeight = 1600 + exportImageUris.take(6).size * 360 + state.toRaw().length.coerceAtMost(2200)
    val scratch = Bitmap.createBitmap(width, estimatedHeight.coerceAtLeast(2200), Bitmap.Config.ARGB_8888)
    val canvas = Canvas(scratch)
    canvas.drawColor(GoaldayDesign.ExportCanvasPaper.toArgb())
    val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = GoaldayDesign.ExportInkPrimary.toArgb()
        textSize = 48f
        isFakeBoldText = true
    }
    val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF8B7A68.toInt()
        textSize = 28f
    }
    val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFB07A8F.toInt()
        textSize = 30f
        isFakeBoldText = true
    }
    val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF3A342E.toInt()
        textSize = 30f
    }
    val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFF7EFE6.toInt()
    }
    var y = 86f
    canvas.drawText(title.ifBlank { "Goalday 日记" }, padding, y, titlePaint)
    y += 48f
    canvas.drawText(diaryDateLabel(state.date), padding, y, subtitlePaint)
    y += 54f
    if (state.moodTags.isNotBlank()) {
        y = drawExportSection(canvas, "心情标签", state.moodTags, padding, y, contentWidth, labelPaint, bodyPaint, cardPaint)
    }
    y = drawExportSection(canvas, "今日完成", state.todayDone.ifBlank { "今天完成了什么？" }, padding, y, contentWidth, labelPaint, bodyPaint, cardPaint)
    y = drawExportSection(canvas, "工作任务", state.workTasks.ifBlank { "记录待推进的任务。" }, padding, y, contentWidth, labelPaint, bodyPaint, cardPaint)
    y = drawExportSection(canvas, "小幸福", state.smallJoy.ifBlank { "记录今天值得保留的一刻。" }, padding, y, contentWidth, labelPaint, bodyPaint, cardPaint)
    y = drawExportSection(canvas, "可改进", state.canImprove.ifBlank { "记录下一次可以优化的地方。" }, padding, y, contentWidth, labelPaint, bodyPaint, cardPaint)
    if (state.photoText.isNotBlank()) {
        y = drawExportSection(canvas, "图片描述", state.photoText, padding, y, contentWidth, labelPaint, bodyPaint, cardPaint)
    }
    if (state.richHtml.isNotBlank()) {
        y = drawExportSection(canvas, "富文本记录", plainTextFromHtml(state.richHtml), padding, y, contentWidth, labelPaint, bodyPaint, cardPaint)
    }
    state.blocks.take(8).forEach { block ->
        if (block.type == DiaryBlockType.IMAGE) {
            y += 12f
            canvas.drawText("图片记录", padding, y + 32f, labelPaint)
            y += 52f
            y = drawExportImage(context, canvas, block.text, padding, y, contentWidth, cardPaint)
        } else {
            val body = buildString {
                append(block.mainText.ifBlank { "空内容" })
                block.childLines.forEach { child ->
                    appendLine()
                    append("  - ")
                    append(child)
                }
            }
            y = drawExportSection(canvas, "${block.type.label} · ${block.style.label}", body, padding, y, contentWidth, labelPaint, bodyPaint, cardPaint)
        }
    }
    state.legacyImageUris.take(3).forEachIndexed { index, uri ->
        y += 12f
        canvas.drawText("图片 ${index + 1}", padding, y + 32f, labelPaint)
        y += 52f
        y = drawExportImage(context, canvas, uri, padding, y, contentWidth, cardPaint)
    }
    val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFB7A893.toInt()
        textSize = 24f
    }
    y += 48f
    canvas.drawText("Goalday Local", padding, y, footerPaint)
    val finalHeight = (y + 72f).toInt().coerceAtMost(scratch.height)
    return Bitmap.createBitmap(scratch, 0, 0, width, finalHeight)
}

private fun drawExportSection(
    canvas: Canvas,
    label: String,
    body: String,
    x: Float,
    y: Float,
    width: Float,
    labelPaint: Paint,
    bodyPaint: Paint,
    cardPaint: Paint,
): Float {
    val lines = wrapExportText(body, bodyPaint, width - 44f).ifEmpty { listOf(" ") }
    val height = 72f + lines.size * 40f
    val rect = RectF(x, y, x + width, y + height)
    canvas.drawRoundRect(rect, 22f, 22f, cardPaint)
    canvas.drawText(label, x + 22f, y + 40f, labelPaint)
    var lineY = y + 84f
    lines.forEach { line ->
        canvas.drawText(line, x + 22f, lineY, bodyPaint)
        lineY += 40f
    }
    return y + height + 24f
}

private fun drawExportImage(
    context: Context,
    canvas: Canvas,
    uri: String,
    x: Float,
    y: Float,
    width: Float,
    fallbackPaint: Paint,
): Float {
    val source = runCatching {
        context.contentResolver.openInputStream(Uri.parse(uri))?.use { stream ->
            BitmapFactory.decodeStream(stream)
        }
    }.getOrNull()
    val maxHeight = 320f
    val rect = RectF(x, y, x + width, y + maxHeight)
    canvas.drawRoundRect(rect, 22f, 22f, fallbackPaint)
    if (source != null) {
        val ratio = minOf(width / source.width, maxHeight / source.height)
        val drawWidth = source.width * ratio
        val drawHeight = source.height * ratio
        val dest = RectF(x + (width - drawWidth) / 2f, y + (maxHeight - drawHeight) / 2f, x + (width + drawWidth) / 2f, y + (maxHeight + drawHeight) / 2f)
        canvas.drawBitmap(source, null, dest, null)
    }
    return y + maxHeight + 24f
}

private fun wrapExportText(text: String, paint: Paint, maxWidth: Float): List<String> {
    val result = mutableListOf<String>()
    text.lines().forEach { paragraph ->
        var current = ""
        paragraph.forEach { char ->
            val next = current + char
            if (paint.measureText(next) > maxWidth && current.isNotBlank()) {
                result += current
                current = char.toString()
            } else {
                current = next
            }
        }
        if (current.isNotBlank()) result += current
    }
    return result.take(24)
}

private fun saveBitmapToPictures(context: Context, bitmap: Bitmap, fileName: String): Uri? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Goalday")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return null
        resolver.openOutputStream(uri)?.use { output ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
        }
        values.clear()
        values.put(MediaStore.Images.Media.IS_PENDING, 0)
        resolver.update(uri, values, null, null)
        uri
    } else {
        val dir = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Goalday").apply { mkdirs() }
        val file = File(dir, fileName)
        FileOutputStream(file).use { output ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
        }
        Uri.fromFile(file)
    }
}

@Composable
private fun DiaryLinkedTargetStrip(
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

// 对照逆向 item_diary_target_child.xml（aapt2 验证）：5pt=11.11dp 圆点, paddingStart=10pt=22.22dp, marginStart/End=8dp
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
            .padding(start = 22.22.dp),
    ) {
        Box(
            modifier = Modifier
                .size(11.11.dp)
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
        DiaryBlockType.TARGET -> Icons.Filled.Check
        DiaryBlockType.TARGET_CHILD -> Icons.Filled.SubdirectoryArrowRight
        DiaryBlockType.TOPIC_TARGET -> Icons.Filled.RadioButtonChecked
    }

internal fun diaryBlockDisplayTitle(type: DiaryBlockType): String =
    when (type) {
        DiaryBlockType.IMAGE -> "图片记录"
        DiaryBlockType.TEXT -> "文字记录"
        DiaryBlockType.TARGET -> "关联目标"
        DiaryBlockType.TARGET_CHILD -> "目标子项"
        DiaryBlockType.TOPIC_TARGET -> "专题目标"
    }

internal fun diaryBlockDisplaySubtitle(type: DiaryBlockType): String =
    when (type) {
        DiaryBlockType.IMAGE -> "照片 / 截图 / 本地图片"
        DiaryBlockType.TEXT -> "自由文字 / 摘要"
        DiaryBlockType.TARGET -> "完成项 / 工作项"
        DiaryBlockType.TARGET_CHILD -> "下一步 / 子任务"
        DiaryBlockType.TOPIC_TARGET -> "来自灵感主题"
    }

@Composable
internal fun diaryBlockTypeColor(type: DiaryBlockType): Color =
    when (type) {
        DiaryBlockType.IMAGE -> GoaldayDesign.RouteDiary
        DiaryBlockType.TEXT -> GoaldayDesign.adaptiveInkSecondary
        DiaryBlockType.TARGET -> GoaldayDesign.Positive
        DiaryBlockType.TARGET_CHILD -> GoaldayDesign.RouteTarget
        DiaryBlockType.TOPIC_TARGET -> GoaldayDesign.RouteDiary
    }

@Composable
internal fun diaryBlockTypeBackground(type: DiaryBlockType): Color =
    when (type) {
        DiaryBlockType.IMAGE -> GoaldayDesign.Pink.copy(alpha = 0.09f)
        DiaryBlockType.TEXT -> GoaldayDesign.adaptiveSurfaceSoft.copy(alpha = 0.08f)
        DiaryBlockType.TARGET -> GoaldayDesign.Positive.copy(alpha = 0.08f)
        DiaryBlockType.TARGET_CHILD -> GoaldayDesign.Positive.copy(alpha = 0.07f)
        DiaryBlockType.TOPIC_TARGET -> GoaldayDesign.Pink.copy(alpha = 0.09f)
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
private fun StructuredDiaryPreview(
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
private fun DiaryTypedBlockPreview(
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
// aapt2 验证：paddingBottom=4.5pt=10dp, marginTop=4.5pt=10dp, marginBottom=1.5pt=3.33dp, marginStart=8.0pt=17.78dp, layout_marginBottom=5dp
@Composable
private fun DiaryTargetBlockPreview(block: DiaryEntryBlock) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 5.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(GoaldayDesign.DiaryTargetBackground)
            .border(0.5.dp, Color(0x4D000000), RoundedCornerShape(8.dp))
            .padding(bottom = 10.dp),
    ) {
        Row(
            modifier = Modifier.padding(start = 17.78.dp, top = 10.dp, bottom = 3.33.dp),
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
                modifier = Modifier.padding(start = 11.dp, bottom = 4.dp),
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

// 对照 item_diary_target_child_inbook.xml：12dp 高，2.5pt=5.56dp 圆点 + 文字
// aapt2 验证：paddingStart=5.0pt=11.11dp, dot=2.5pt=5.56dp, marginStart/End=4dp
@Composable
private fun DiaryTargetChildRow(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(12.dp)
            .padding(start = 11.11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .padding(start = 4.dp, end = 4.dp)
                .size(5.56.dp)
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

// 对照 item_diary_topic_target_inbook.xml：bg_diary_topic_target 深色背景，8sp 白色标题 + 9sp 副标题
// aapt2 验证：paddingStart/End=5.0pt=11.11dp, marginTop=5.0pt=11.11dp, marginBottom=3.5pt=7.78dp, layout_marginBottom=5dp
@Composable
private fun DiaryTopicTargetBlockPreview(block: DiaryEntryBlock) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 5.dp)
            .clip(RoundedCornerShape(GoaldayDesign.RadiusS))
            .background(GoaldayDesign.adaptiveInkSecondary)
            .padding(horizontal = 11.11.dp)
            .padding(bottom = 11.11.dp),
    ) {
        Text(
            block.mainText.ifBlank { "专题目标" },
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 1,
            modifier = Modifier.padding(top = 11.11.dp, bottom = 7.78.dp),
        )
        if (block.childLines.isNotEmpty()) {
            Text(
                block.childLines.first(),
                fontSize = 9.sp,
                color = Color(0x9CFFFFFF),
                maxLines = 1,
                modifier = Modifier.padding(bottom = 7.78.dp),
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
        DiaryBlockType.TARGET -> "书内目标条目"
        DiaryBlockType.TARGET_CHILD -> "书内子目标"
        DiaryBlockType.TOPIC_TARGET -> "书内专题目标"
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

// 对照逆向 item_diary_img.xml（aapt2 验证）：图片块水平内边距 paddingStart/End=5pt=11.11dp，高度自适应保持比例
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
            .then(if (fixedHeight) Modifier.height(76.dp) else Modifier.padding(horizontal = 11.11.dp))
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
private fun PaperNoteCard(
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
