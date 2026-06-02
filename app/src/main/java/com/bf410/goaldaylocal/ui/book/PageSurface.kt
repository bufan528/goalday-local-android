package com.bf410.goaldaylocal.ui.book

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import com.bf410.goaldaylocal.data.BookPage
import com.bf410.goaldaylocal.data.DiaryPage
import com.bf410.goaldaylocal.data.PlanPage
import com.bf410.goaldaylocal.data.ScheduleEntry
import com.bf410.goaldaylocal.data.SchedulePage
import com.bf410.goaldaylocal.data.TargetPage
import com.bf410.goaldaylocal.ui.replica.BoardTask
import com.bf410.goaldaylocal.ui.replica.DualLaneExecutionBoard
import com.bf410.goaldaylocal.ui.replica.ExecutionBoardHeader
import com.bf410.goaldaylocal.ui.replica.GoaldayDesign
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.delay
import java.io.File
import java.io.FileOutputStream

@Composable
fun BoxScope.SpineLayer(
    visualProgress: Float,
    active: Boolean,
    profile: TurnProfile = TurnProfile.DEFAULT,
) {
    val baseWidth = if (profile == TurnProfile.HANDBOOK) 28.dp else 20.dp
    Box(
        modifier = Modifier
            .align(Alignment.Center)
            .width(baseWidth)
            .fillMaxHeight()
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color(0xFF6E4229).copy(alpha = if (active) 0.92f else 0.78f),
                        Color(0xFFF8E8D5),
                        Color(0xFF6E4229).copy(alpha = if (active) 0.92f else 0.78f),
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
                            Color(0x44FFF0DF).copy(alpha = (0.24f + visualProgress * 0.24f).coerceAtMost(0.46f)),
                            Color(0x11000000),
                            Color(0x44FFF0DF).copy(alpha = (0.24f + visualProgress * 0.24f).coerceAtMost(0.46f)),
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
            .clip(RoundedCornerShape(28.dp, 28.dp, 32.dp, 32.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFFFFDF9),
                        Color(0xFFFFFBF6),
                        Color(0xFFFFF8EF),
                    ),
                ),
            )
            .padding(horizontal = 18.dp, vertical = 16.dp),
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .alpha(0.08f)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Transparent,
                            GoaldayDesign.Accent.copy(alpha = 0.06f),
                            Color.Transparent,
                            GoaldayDesign.Accent.copy(alpha = 0.05f),
                            Color.Transparent,
                        ),
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .width(10.dp)
                .fillMaxHeight()
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0x1F8C6A4B), Color(0x0EA07E5D), Color.Transparent),
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .width(8.dp)
                .fillMaxHeight()
                .background(
                    Brush.horizontalGradient(
                        listOf(Color.Transparent, Color(0x0C8D7A66), Color(0x14806A54)),
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .width(2.dp)
                .fillMaxHeight()
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0x12000000), Color.Transparent))),
        )
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .width(8.dp)
                .fillMaxHeight()
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0x12000000), Color(0x10F7E9D7), Color(0x12000000)),
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .width(14.dp)
                .fillMaxHeight()
                .background(
                    Brush.horizontalGradient(
                        listOf(Color.Transparent, Color(0x10A88A6C), Color.Transparent),
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0x12A48A70)),
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .alpha(0.12f)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0x10C7B39C),
                            Color.Transparent,
                            Color(0x08C7B39C),
                            Color.Transparent,
                            Color(0x0CC7B39C),
                        ),
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .width(4.dp)
                .fillMaxHeight()
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0x12000000), Color.Transparent),
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .width(20.dp)
                .height(20.dp)
                .background(
                    Brush.linearGradient(
                        listOf(Color.Transparent, Color(0x12C2AE95)),
                        start = Offset(0f, 20f),
                        end = Offset(20f, 0f),
                    ),
                ),
        )
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 14.dp, top = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            repeat(8) {
                Box(
                    modifier = Modifier
                        .width(6.dp)
                        .height(1.dp)
                        .background(Color(0x2A95785E)),
                )
            }
        }
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 2.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.End,
        ) {
            listOf(Color(0x33E693B1), Color(0x33F0C187), Color(0x3394C8E8), Color(0x339FD39B)).forEach { c ->
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(16.dp)
                        .clip(RoundedCornerShape(99.dp))
                        .background(c),
                )
            }
        }

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
            Spacer(Modifier.height(6.dp))
            Text(text = title, style = MaterialTheme.typography.titleMedium, color = Color(0xFF26211C))
            Spacer(Modifier.height(8.dp))
            body()
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(10.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color(0x1A000000)),
                    ),
                ),
        )
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
            color = Color(0xFF6A5D4F).copy(alpha = destinationRevealAlpha(revealProgress)),
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
    val curlAlignTop = anchorY < 0.46f
    val anchorVerticalOffset = ((anchorY - 0.5f) * 2f).coerceIn(-1f, 1f)
    val anchorTopBias = (-anchorVerticalOffset).coerceIn(0f, 1f)
    val anchorBottomBias = anchorVerticalOffset.coerceIn(0f, 1f)
    val easedCurl = progress * progress * (3f - 2f * progress)
    val curlStrength = (0.12f + easedCurl * 0.76f).coerceIn(0f, 0.88f)
    val stackShadow = (0.07f + progress * 0.18f).coerceAtMost(0.30f)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp, 24.dp, 30.dp, 30.dp))
            .background(
                Brush.horizontalGradient(
                    if (direction == TurnDirection.NEXT) {
                        listOf(Color(0xFFDCCAB4), Color(0xFFF4E9DD), Color(0xFFFFFCF8))
                    } else {
                        listOf(Color(0xFFFFFCF8), Color(0xFFF4E9DD), Color(0xFFDCCAB4))
                    },
                ),
            )
            .padding(horizontal = 28.dp, vertical = 26.dp),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .width((4f + progress * 10f).dp)
                .fillMaxHeight()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color.Black.copy(alpha = stackShadow),
                            Color(0x33A5876A).copy(alpha = (0.10f + progress * 0.14f).coerceAtMost(0.24f)),
                            Color.Transparent,
                        ),
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .width((3f + progress * 8f).dp)
                .fillMaxHeight()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = (0.10f + progress * 0.18f).coerceAtMost(0.28f)),
                            Color.Black.copy(alpha = (0.05f + progress * 0.12f).coerceAtMost(0.20f)),
                        ),
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color(0x12FFFFFF), Color.Transparent),
                    ),
                ),
        )

        Box(
            modifier = Modifier
                .align(if (direction == TurnDirection.NEXT) Alignment.CenterStart else Alignment.CenterEnd)
                .width((12f + progress * 24f).dp)
                .fillMaxHeight()
                .background(
                    Brush.horizontalGradient(
                        if (direction == TurnDirection.NEXT) {
                            listOf(
                                Color.Black.copy(alpha = (0.10f + progress * 0.18f).coerceAtMost(0.24f)),
                                Color.White.copy(alpha = (0.14f + progress * 0.22f).coerceAtMost(0.30f)),
                                Color.Transparent,
                            )
                        } else {
                            listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = (0.14f + progress * 0.22f).coerceAtMost(0.30f)),
                                Color.Black.copy(alpha = (0.10f + progress * 0.18f).coerceAtMost(0.24f)),
                            )
                        },
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .align(if (direction == TurnDirection.NEXT) Alignment.TopEnd else Alignment.TopStart)
                .width((20f + progress * 44f).dp)
                .height((16f + progress * 34f).dp)
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color.White.copy(alpha = (0.08f + curlStrength * 0.20f).coerceAtMost(0.34f)),
                            Color(0x18C4A98E),
                            Color.Transparent,
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(120f, 100f),
                    ),
                ),
        )

        Box(
            modifier = Modifier
                .align(
                    when {
                        direction == TurnDirection.NEXT && curlAlignTop -> Alignment.TopStart
                        direction == TurnDirection.PREVIOUS && curlAlignTop -> Alignment.TopEnd
                        direction == TurnDirection.NEXT -> Alignment.BottomStart
                        else -> Alignment.BottomEnd
                    },
                )
                .width((28f + progress * 66f).dp)
                .height((30f + progress * 70f).dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(
                                alpha = (
                                    0.18f +
                                        curlStrength * 0.36f +
                                        anchorTopBias * 0.10f +
                                        anchorBottomBias * 0.10f
                                    ).coerceAtMost(0.66f),
                            ),
                            Color(0x22A48A70).copy(alpha = (0.18f + curlStrength * 0.24f).coerceAtMost(0.46f)),
                            Color.Transparent,
                        ),
                        radius = 210f,
                    ),
                ),
        )

        Box(
            modifier = Modifier
                .align(
                    when {
                        direction == TurnDirection.NEXT && curlAlignTop -> Alignment.TopEnd
                        direction == TurnDirection.PREVIOUS && curlAlignTop -> Alignment.TopStart
                        direction == TurnDirection.NEXT -> Alignment.BottomEnd
                        else -> Alignment.BottomStart
                    },
                )
                .width((14f + progress * 30f).dp)
                .height((24f + progress * 40f).dp)
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color.Black.copy(
                                alpha = (
                                    0.06f + curlStrength * 0.24f +
                                        anchorTopBias * 0.06f +
                                        anchorBottomBias * 0.06f
                                    ).coerceAtMost(0.40f),
                            ),
                            Color.Transparent,
                        ),
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .align(
                    when {
                        direction == TurnDirection.NEXT && curlAlignTop -> Alignment.TopEnd
                        direction == TurnDirection.PREVIOUS && curlAlignTop -> Alignment.TopStart
                        direction == TurnDirection.NEXT -> Alignment.BottomEnd
                        else -> Alignment.BottomStart
                    },
                )
                .width((18f + progress * 42f).dp)
                .height((8f + progress * 20f).dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color.White.copy(
                                alpha = (
                                    0.08f + curlStrength * 0.24f +
                                        anchorTopBias * 0.08f +
                                        anchorBottomBias * 0.08f
                                    ).coerceAtMost(0.42f),
                            ),
                            Color(0x229C8167),
                            Color.Transparent,
                        ),
                    ),
                ),
        )
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
                .width((10f + progress * 26f).dp)
                .height((16f + progress * 26f).dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = (0.02f + curlStrength * 0.08f).coerceAtMost(0.12f)),
                            Color.Transparent,
                        ),
                        radius = 100f,
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
    onAddScheduleFromHandbook: (String, Int, Int) -> Unit,
    onWeeklyThemeChange: (String) -> Unit,
    onMoveItemToToday: (String) -> Unit,
    onMoveItemToCompleted: (String) -> Unit,
    onRestoreItemFromToday: (String) -> Unit,
    onRestoreItemFromCompleted: (String) -> Unit,
    onUpdateScheduleTitle: (String, String) -> Unit,
    onMoveScheduleDay: (String, Int, Int) -> Unit,
    onToggleScheduleCompleted: (String) -> Unit,
    pendingCommand: RichEditorCommand?,
    onCommand: (RichEditorCommand) -> Unit,
    contentMode: PageContentMode,
    onContentModeChange: (PageContentMode) -> Unit,
    handbookMode: Boolean = false,
    turnProgress: Float = 0f,
    turnDirection: TurnDirection? = null,
) {
    if (handbookMode) {
        HandbookReplicaPage(
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
        return
    }
    val easedShift = turnProgress * turnProgress
    val contentShift = when (turnDirection) {
        TurnDirection.NEXT -> -(easedShift * 22f + turnProgress * 5f)
        TurnDirection.PREVIOUS -> easedShift * 22f + turnProgress * 5f
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
                            Color(0x18B59072),
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
                is TargetPage -> TargetDetailReplicaPage(page.title, page.items, customPageItems, tint, isChecked, onToggleChecked, onAddCustomItem, onRemoveCustomItem, onRenameCustomItem, onAddToSchedule)
                is SchedulePage -> EditableBulletPage(page.title, page.items, customPageItems, tint.copy(alpha = 0.74f), BookStrings.addSchedule, true, isChecked, onToggleChecked, onAddCustomItem, onAddCustomItemWithDeadline, onRemoveCustomItem, onRenameCustomItem, onAddToSchedule, weeklyTheme, todayPlanItems, todayCompletedItems, schedulePreviewEntries, onWeeklyThemeChange, onMoveItemToToday, onMoveItemToCompleted, onRestoreItemFromToday, onRestoreItemFromCompleted, contentMode, onContentModeChange)
                is PlanPage -> EditableBulletPage(page.title, page.items, customPageItems, Color(0xFFB88A58), BookStrings.addPlan, false, isChecked, onToggleChecked, onAddCustomItem, onAddCustomItemWithDeadline, onRemoveCustomItem, onRenameCustomItem, onAddToSchedule, weeklyTheme, todayPlanItems, todayCompletedItems, schedulePreviewEntries, onWeeklyThemeChange, onMoveItemToToday, onMoveItemToCompleted, onRestoreItemFromToday, onRestoreItemFromCompleted, contentMode, onContentModeChange)
                is DiaryPage -> DiarySection(page.title, page.prompt, tint, diaryDraft, todayPlanItems, todayCompletedItems, pendingCommand, onCommand, onDiaryChange, contentMode, onContentModeChange)
            }
        }
        Spacer(Modifier.height(24.dp))
        Text(text = "${pageIndex + 1} / $pageCount", style = MaterialTheme.typography.labelMedium, color = Color(0xFF7A7065))
    }
}

@Composable
private fun HandbookReplicaPage(
    modifier: Modifier,
    page: BookPage,
    pageIndex: Int,
    pageCount: Int,
    todayPlanItems: List<String>,
    todayCompletedItems: List<String>,
    schedulePreviewEntries: List<ScheduleEntry>,
    weeklyTheme: String,
    onAddPoolItem: (String) -> Unit,
    onRemovePoolItem: (String) -> Unit,
    onAddSchedule: (String, Int, Int) -> Unit,
    onWeeklyThemeChange: (String) -> Unit,
    onUpdateScheduleTitle: (String, String) -> Unit,
    onMoveScheduleDay: (String, Int, Int) -> Unit,
    onToggleScheduleCompleted: (String) -> Unit,
    turnProgress: Float,
    turnDirection: TurnDirection?,
) {
    data class DaySpreadBlock(
        val day: Int,
        val done: List<ScheduleEntry>,
        val todo: List<ScheduleEntry>,
    )
    val eased = turnProgress * turnProgress * (3f - 2f * turnProgress)
    val contentShift = when (turnDirection) {
        TurnDirection.NEXT -> -(eased * 8f)
        TurnDirection.PREVIOUS -> eased * 8f
        null -> 0f
    }
    val alpha = (1f - eased * 0.08f).coerceIn(0.92f, 1f)
    val today = LocalDate.now()
    val pageMonth = Regex("(\\d{1,2})月")
        .find(page.title)
        ?.groupValues
        ?.getOrNull(1)
        ?.toIntOrNull()
        ?.coerceIn(1, 12)
    val anchorYear = schedulePreviewEntries.firstOrNull()?.year ?: today.year
    val anchorMonth = pageMonth ?: schedulePreviewEntries.firstOrNull()?.month ?: today.monthValue
    val sorted = schedulePreviewEntries
        .filter { it.month == anchorMonth }
        .sortedWith(compareBy<ScheduleEntry>({ it.day }, { it.completed }, { it.title.lowercase() }, { it.id }))
    val monthLength = YearMonth.of(anchorYear, anchorMonth).lengthOfMonth()
    val defaultStart = if (anchorYear == today.year && anchorMonth == today.monthValue) {
        (today.dayOfMonth - 1).coerceIn(0, monthLength - 1)
    } else {
        0
    }
    val maxStart = (monthLength - 3).coerceAtLeast(0)
    var windowStart by remember(page.title, anchorYear, anchorMonth) { mutableStateOf(defaultStart.coerceIn(0, maxStart)) }
    val start = windowStart.coerceIn(0, maxStart)
    LaunchedEffect(page.title, anchorYear, anchorMonth, defaultStart, maxStart) {
        windowStart = defaultStart.coerceIn(0, maxStart)
    }
    val dayBlocks = List(3) { offset ->
        val day = start + offset + 1
        val dayEntries = sorted.filter { it.day == day }
        DaySpreadBlock(
            day = day,
            done = dayEntries.filter { it.completed }.take(5),
            todo = dayEntries.filterNot { it.completed }.take(5),
        )
    }
    val leftBlocks = dayBlocks
    val rightBlocks = dayBlocks
    val visibleDays = rightBlocks.map { it.day }
    val visibleRangeLabel = "${dayBlocks.first().day}-${dayBlocks.last().day}日"
    val fallbackLeftDone = todayCompletedItems.take(3)
    val fallbackRightTodo = todayPlanItems.take(3)
    val scheduledTitles = sorted.map { it.title }.toSet()
    val visiblePoolItems = todayPlanItems.filterNot { it in scheduledTitles }.take(6)
    var draftText by remember(page.title) { mutableStateOf("") }
    var draftDay by remember(page.title) { mutableStateOf(rightBlocks.firstOrNull()?.day ?: 1) }
    LaunchedEffect(visibleDays) {
        if (draftDay !in visibleDays) {
            draftDay = visibleDays.firstOrNull() ?: 1
        }
    }
    val selectedDraftDay = if (draftDay in visibleDays) draftDay else visibleDays.firstOrNull() ?: 1
    var editingId by remember(pageIndex) { mutableStateOf<String?>(null) }
    var editingText by remember(pageIndex) { mutableStateOf("") }
    var saveHint by remember(pageIndex) { mutableStateOf("") }
    var spreadOrigin by remember(pageIndex) { mutableStateOf(Offset.Zero) }
    val todoDropBounds = remember(pageIndex) { mutableMapOf<Int, Rect>() }
    val doneDropBounds = remember(pageIndex) { mutableMapOf<Int, Rect>() }
    var draggingPoolItem by remember(pageIndex) { mutableStateOf<String?>(null) }
    var draggingTodoEntry by remember(pageIndex) { mutableStateOf<ScheduleEntry?>(null) }
    var dragPosition by remember(pageIndex) { mutableStateOf(Offset.Zero) }
    var activePoolDropDay by remember(pageIndex) { mutableStateOf<Int?>(null) }
    var activeDoneDropDay by remember(pageIndex) { mutableStateOf<Int?>(null) }
    val context = LocalContext.current
    var exportHint by remember(pageIndex) { mutableStateOf("") }
    fun clearPoolDrag() {
        draggingPoolItem = null
        activePoolDropDay = null
        dragPosition = Offset.Zero
    }
    fun clearTodoDrag() {
        draggingTodoEntry = null
        activePoolDropDay = null
        activeDoneDropDay = null
        dragPosition = Offset.Zero
    }
    LaunchedEffect(saveHint) {
        if (saveHint.isBlank()) return@LaunchedEffect
        delay(1200)
        saveHint = ""
    }
    LaunchedEffect(exportHint) {
        if (exportHint.isBlank()) return@LaunchedEffect
        delay(1400)
        exportHint = ""
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(GoaldayDesign.RadiusL))
            .background(GoaldayDesign.Surface)
            .border(0.5.dp, Color(0x10000000), RoundedCornerShape(GoaldayDesign.RadiusL))
            .graphicsLayer {
                translationX = contentShift
                this.alpha = alpha
            }
            .onGloballyPositioned { coordinates ->
                spreadOrigin = coordinates.boundsInRoot().topLeft
            }
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        HandbookPaperRuling()
        HandbookMonthHeader(
            year = anchorYear,
            month = anchorMonth,
            pageIndex = pageIndex,
            pageCount = pageCount,
            weeklyTheme = weeklyTheme,
            onWeeklyThemeChange = onWeeklyThemeChange,
            rangeLabel = visibleRangeLabel,
            visibleDays = visibleDays,
            canShiftPrevious = start > 0,
            canShiftNext = start < maxStart,
            onPreviousRange = { windowStart = (start - 3).coerceAtLeast(0) },
            onNextRange = { windowStart = (start + 3).coerceAtMost(maxStart) },
            onSelectMonthDay = { day ->
                windowStart = (day - 1).coerceIn(0, maxStart)
                draftDay = day
            },
        )
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 33.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "导出",
                color = Color(0xFF8B6F78),
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier
                    .clip(RoundedCornerShape(99.dp))
                    .background(Color(0x18E88FAE))
                    .clickable {
                        val uri = exportHandbookScheduleLongImage(context, anchorYear, anchorMonth, visibleDays, sorted, weeklyTheme)
                        exportHint = if (uri != null) "已导出" else "导出失败"
                    }
                    .padding(horizontal = 7.dp, vertical = 3.dp),
            )
            Text(
                "分享",
                color = Color(0xFF8B6F78),
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier
                    .clip(RoundedCornerShape(99.dp))
                    .background(Color(0x18E88FAE))
                    .clickable {
                        val uri = exportHandbookScheduleLongImage(context, anchorYear, anchorMonth, visibleDays, sorted, weeklyTheme)
                        exportHint = if (uri != null && shareLongImage(context, uri)) "已打开分享" else "分享失败"
                    }
                    .padding(horizontal = 7.dp, vertical = 3.dp),
            )
            if (exportHint.isNotBlank()) {
                Text(exportHint, style = MaterialTheme.typography.labelSmall, color = Color(0xFF7A7065))
            }
        }
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 58.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        "done",
                        style = MaterialTheme.typography.labelMedium,
                        color = GoaldayDesign.InkPrimary,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.alpha(0.92f),
                    )
                    Text("已执行", style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.InkMuted, modifier = Modifier.alpha(0.85f))
                }
                leftBlocks.forEachIndexed { idx, block ->
                    DaySpreadSection(
                        day = block.day,
                        done = if (idx == 0 && block.done.isEmpty()) fallbackLeftDone else block.done.map { it.title },
                        todoCount = block.todo.size,
                        accent = GoaldayDesign.Positive,
                        activeDrop = activeDoneDropDay == block.day,
                        onBounds = { rect -> doneDropBounds[block.day] = rect },
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        "todo",
                        style = MaterialTheme.typography.labelMedium,
                        color = GoaldayDesign.InkPrimary,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.alpha(0.92f),
                    )
                    Text("待计划", style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.InkMuted, modifier = Modifier.alpha(0.85f))
                }
                HandbookQuickAddRow(
                    value = draftText,
                    onValueChange = { draftText = it },
                    days = rightBlocks.map { it.day },
                    selectedDay = selectedDraftDay,
                    onSelectDay = { draftDay = it },
                    poolItems = visiblePoolItems,
                    onAddPoolItem = { text ->
                        onAddPoolItem(text)
                        draftText = ""
                        saveHint = "已加入待安排"
                    },
                    onRemovePoolItem = { text ->
                        onRemovePoolItem(text)
                        saveHint = "已从待安排移除"
                    },
                    onPickPoolItem = { text ->
                        onAddSchedule(text, anchorMonth, selectedDraftDay)
                        saveHint = "已放入${selectedDraftDay}日"
                    },
                    onPoolDragStart = { text, position ->
                        draggingPoolItem = text
                        dragPosition = position
                        activePoolDropDay = todoDropBounds.entries.firstOrNull { it.value.contains(position) }?.key
                    },
                    onPoolDrag = { delta ->
                        dragPosition += delta
                        activePoolDropDay = todoDropBounds.entries.firstOrNull { it.value.contains(dragPosition) }?.key
                    },
                    onPoolDragEnd = {
                        val text = draggingPoolItem
                        val targetDay = activePoolDropDay
                        if (text != null && targetDay != null) {
                            onAddSchedule(text, anchorMonth, targetDay)
                            draftDay = targetDay
                            saveHint = "已拖入${targetDay}日"
                        } else if (text != null) {
                            saveHint = "未命中日期"
                        }
                        clearPoolDrag()
                    },
                    onPoolDragCancel = {
                        saveHint = "已取消拖放"
                        clearPoolDrag()
                    },
                    onDone = {
                        val text = draftText.trim()
                        if (text.isNotBlank()) {
                            onAddSchedule(text, anchorMonth, selectedDraftDay)
                            draftText = ""
                            saveHint = "已加入${selectedDraftDay}日"
                        }
                    },
                )
                rightBlocks.forEachIndexed { idx, block ->
                    DaySpreadEditableSection(
                        day = block.day,
                        entries = if (idx == 0 && block.todo.isEmpty()) {
                            fallbackRightTodo.mapIndexed { i, text ->
                                ScheduleEntry(id = "fallback_$i", title = text, day = block.day, month = anchorMonth, year = anchorYear, completed = false, note = "")
                            }
                        } else {
                            block.todo
                        },
                        editingId = editingId,
                        editingText = editingText,
                        activeDrop = activePoolDropDay == block.day,
                        onBounds = { rect -> todoDropBounds[block.day] = rect },
                        onStartEdit = { entry ->
                            if (!entry.id.startsWith("fallback_")) {
                                editingId = entry.id
                                editingText = entry.title
                            } else {
                                onAddSchedule(entry.title, anchorMonth, entry.day)
                                saveHint = "已放入${entry.day}日"
                            }
                        },
                        onTextChange = { editingText = it },
                        onCommit = { entry ->
                            if (!entry.id.startsWith("fallback_")) {
                                onUpdateScheduleTitle(entry.id, editingText)
                                editingId = null
                                saveHint = "已保存"
                            }
                        },
                        onToggleCompleted = { entry ->
                            if (!entry.id.startsWith("fallback_")) {
                                onToggleScheduleCompleted(entry.id)
                            } else {
                                onAddSchedule(entry.title, anchorMonth, entry.day)
                                saveHint = "已放入${entry.day}日"
                            }
                        },
                        onEntryDragStart = { entry, position ->
                            if (!entry.id.startsWith("fallback_")) {
                                draggingTodoEntry = entry
                                dragPosition = position
                                activePoolDropDay = todoDropBounds.entries.firstOrNull { it.value.contains(position) }?.key
                                activeDoneDropDay = doneDropBounds.entries.firstOrNull { it.value.contains(position) }?.key
                            }
                        },
                        onEntryDrag = { delta ->
                            dragPosition += delta
                            activePoolDropDay = todoDropBounds.entries.firstOrNull { it.value.contains(dragPosition) }?.key
                            activeDoneDropDay = doneDropBounds.entries.firstOrNull { it.value.contains(dragPosition) }?.key
                        },
                        onEntryDragEnd = {
                            val entry = draggingTodoEntry
                            val doneTargetDay = activeDoneDropDay
                            val todoTargetDay = activePoolDropDay
                            when {
                                entry != null && doneTargetDay == entry.day -> {
                                    onToggleScheduleCompleted(entry.id)
                                    saveHint = "已放入${doneTargetDay}日 done"
                                }
                                entry != null && todoTargetDay != null && todoTargetDay != entry.day -> {
                                    onMoveScheduleDay(entry.id, anchorMonth, todoTargetDay)
                                    draftDay = todoTargetDay
                                    saveHint = "已拖到${todoTargetDay}日"
                                }
                                entry != null && doneTargetDay != null -> saveHint = "请拖到同日期 done"
                                entry != null -> saveHint = "未命中日期或 done"
                            }
                            clearTodoDrag()
                        },
                        onEntryDragCancel = {
                            saveHint = "已取消拖放"
                            clearTodoDrag()
                        },
                    )
                }
            }
        }

        (draggingPoolItem ?: draggingTodoEntry?.title)?.let { text ->
            val localX = (dragPosition.x - spreadOrigin.x).toInt()
            val localY = (dragPosition.y - spreadOrigin.y).toInt()
            Text(
                text,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White,
                maxLines = 1,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset { IntOffset(localX, localY) }
                    .clip(RoundedCornerShape(GoaldayDesign.RadiusS))
                    .background(GoaldayDesign.Pink)
                    .border(0.8.dp, Color.White, RoundedCornerShape(GoaldayDesign.RadiusS))
                    .padding(horizontal = 7.dp, vertical = 4.dp),
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .width(1.5.dp)
                .fillMaxHeight()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0x0E000000),
                            Color(0x20000000),
                            Color(0x0E000000),
                        ),
                    ),
                ),
        )

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
                .background(Color(0x11A68B71))
                .border(0.5.dp, Color(0x1EA68B71), RoundedCornerShape(GoaldayDesign.RadiusPill))
                .padding(horizontal = 9.dp, vertical = 1.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = page.title.ifBlank { "手账" },
                style = MaterialTheme.typography.labelSmall,
                color = GoaldayDesign.InkSecondary,
                modifier = Modifier.alpha(0.86f),
                textAlign = TextAlign.Center,
            )
            Text(
                text = "${pageIndex + 1}/$pageCount",
                style = MaterialTheme.typography.labelSmall,
                color = GoaldayDesign.InkSecondary,
                modifier = Modifier.alpha(0.78f),
                textAlign = TextAlign.Center,
            )
        }
        if (saveHint.isNotBlank()) {
            Text(
                saveHint,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF7A7269),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 4.dp, bottom = 2.dp),
            )
        }
    }
}

@Composable
private fun BoxScope.HandbookPaperRuling() {
    repeat(9) { index ->
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = (54 + index * 24).dp)
                .fillMaxWidth()
                .height(0.45.dp)
                .background(Color(0x09000000)),
        )
    }
    repeat(2) { index ->
        Box(
            modifier = Modifier
                .align(if (index == 0) Alignment.CenterStart else Alignment.CenterEnd)
                .padding(horizontal = 8.dp)
                .width(0.6.dp)
                .fillMaxHeight()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color(0x10E88FAE), Color.Transparent),
                    ),
                ),
        )
    }
}

@Composable
private fun BoxScope.HandbookMonthHeader(
    year: Int,
    month: Int,
    pageIndex: Int,
    pageCount: Int,
    weeklyTheme: String,
    onWeeklyThemeChange: (String) -> Unit,
    rangeLabel: String,
    visibleDays: List<Int>,
    canShiftPrevious: Boolean,
    canShiftNext: Boolean,
    onPreviousRange: () -> Unit,
    onNextRange: () -> Unit,
    onSelectMonthDay: (Int) -> Unit,
) {
    val monthModel = remember(year, month) { YearMonth.of(year, month) }
    val today = LocalDate.now()
    val visibleDaySet = remember(visibleDays) { visibleDays.toSet() }
    Column(
        modifier = Modifier
            .align(Alignment.TopCenter)
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(0.88f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    "$year GOALDAY",
                    style = MaterialTheme.typography.labelSmall,
                    color = GoaldayDesign.InkMuted,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.alpha(0.86f),
                )
                Text(
                    "${month}月计划",
                    style = MaterialTheme.typography.titleSmall,
                    color = GoaldayDesign.InkPrimary,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "‹",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (canShiftPrevious) GoaldayDesign.Pink else GoaldayDesign.InkMuted,
                        modifier = Modifier
                            .alpha(if (canShiftPrevious) 1f else 0.35f)
                            .clickable(enabled = canShiftPrevious, onClick = onPreviousRange),
                    )
                    Text(rangeLabel, style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.InkSecondary)
                    Text(
                        "›",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (canShiftNext) GoaldayDesign.Pink else GoaldayDesign.InkMuted,
                        modifier = Modifier
                            .alpha(if (canShiftNext) 1f else 0.35f)
                            .clickable(enabled = canShiftNext, onClick = onNextRange),
                    )
                }
            }
            Column(
                modifier = Modifier
                    .weight(1.12f)
                    .clip(RoundedCornerShape(GoaldayDesign.RadiusS))
                    .background(Color(0x08E88FAE))
                    .border(0.35.dp, Color(0x10E88FAE), RoundedCornerShape(GoaldayDesign.RadiusS))
                    .padding(horizontal = 4.dp, vertical = 3.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(1.dp), verticalAlignment = Alignment.CenterVertically) {
                    (1..monthModel.lengthOfMonth()).forEach { day ->
                        val visible = day in visibleDaySet
                        val isToday = today.year == year && today.monthValue == month && today.dayOfMonth == day
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(if (visible) 9.dp else 5.dp)
                                .clip(RoundedCornerShape(99.dp))
                                .clickable { onSelectMonthDay(day) }
                                .background(
                                    when {
                                        visible -> GoaldayDesign.Pink
                                        isToday -> GoaldayDesign.InkPrimary.copy(alpha = 0.45f)
                                        else -> Color(0x1A000000)
                                    },
                                ),
                        )
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("1", style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.InkMuted)
                    Text(monthModel.lengthOfMonth().toString(), style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.InkMuted)
                }
            }
            Text(
                "${pageIndex + 1}/$pageCount",
                style = MaterialTheme.typography.labelSmall,
                color = GoaldayDesign.InkMuted,
                modifier = Modifier
                    .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
                    .background(Color(0x0D000000))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
            )
        }
        BasicTextField(
            value = weeklyTheme,
            onValueChange = onWeeklyThemeChange,
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(GoaldayDesign.RadiusS))
                .background(GoaldayDesign.PinkSoft)
                .border(0.35.dp, Color(0x18E88FAE), RoundedCornerShape(GoaldayDesign.RadiusS))
                .padding(horizontal = 7.dp, vertical = 4.dp),
            textStyle = MaterialTheme.typography.bodySmall.copy(color = GoaldayDesign.InkPrimary),
            decorationBox = { inner ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text("本月重点", style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.Pink, fontWeight = FontWeight.SemiBold)
                    Box(Modifier.weight(1f)) {
                        if (weeklyTheme.isBlank()) {
                            Text("写下最重要的目标", style = MaterialTheme.typography.bodySmall, color = GoaldayDesign.InkMuted)
                        }
                        inner()
                    }
                }
            },
        )
    }
}

@Composable
private fun DaySpreadSection(
    day: Int,
    done: List<String>,
    todoCount: Int,
    accent: Color,
    activeDrop: Boolean,
    onBounds: (Rect) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (activeDrop) GoaldayDesign.GreenSoft else Color.Transparent, RoundedCornerShape(GoaldayDesign.RadiusS))
            .border(if (activeDrop) 0.9.dp else 0.35.dp, if (activeDrop) GoaldayDesign.Positive else Color(0x0A000000), RoundedCornerShape(GoaldayDesign.RadiusS))
            .onGloballyPositioned { coordinates -> onBounds(coordinates.boundsInRoot()) }
            .padding(horizontal = 5.dp, vertical = 3.dp),
        verticalArrangement = Arrangement.spacedBy(1.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("${day}日", style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.InkSecondary)
            Text("done ${done.size}", style = MaterialTheme.typography.labelSmall, color = accent.copy(alpha = 0.88f))
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("done", style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.InkMuted)
            Text("todo $todoCount", style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.InkMuted)
        }
        done.take(2).forEach { line ->
            Text("✓ $line", style = MaterialTheme.typography.bodySmall, color = GoaldayDesign.InkSecondary, textDecoration = TextDecoration.LineThrough, maxLines = 1)
        }
        repeat((3 - done.take(2).size).coerceAtLeast(0)) { index ->
            EmptyHandbookSlot(
                label = when {
                    activeDrop && index == 0 -> "释放放入 done"
                    done.isEmpty() && index == 0 -> "○"
                    else -> ""
                },
                highlight = activeDrop && index == 0,
            )
        }
    }
}

@Composable
private fun HandbookQuickAddRow(
    value: String,
    onValueChange: (String) -> Unit,
    days: List<Int>,
    selectedDay: Int,
    onSelectDay: (Int) -> Unit,
    poolItems: List<String>,
    onAddPoolItem: (String) -> Unit,
    onRemovePoolItem: (String) -> Unit,
    onPickPoolItem: (String) -> Unit,
    onPoolDragStart: (String, Offset) -> Unit,
    onPoolDrag: (Offset) -> Unit,
    onPoolDragEnd: () -> Unit,
    onPoolDragCancel: () -> Unit,
    onDone: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    fun submitAndKeepFocus() {
        onDone()
        focusRequester.requestFocus()
    }
    fun addToPoolAndKeepFocus() {
        onAddPoolItem(value)
        focusRequester.requestFocus()
    }

    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            textStyle = MaterialTheme.typography.bodySmall.copy(color = GoaldayDesign.InkPrimary),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { submitAndKeepFocus() }),
            decorationBox = { inner ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(0.35.dp, Color(0x12000000), RoundedCornerShape(GoaldayDesign.RadiusS))
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("＋", style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.Pink)
                    Box(Modifier.weight(1f)) {
                        if (value.isBlank()) {
                            Text("写入计划", style = MaterialTheme.typography.bodySmall, color = GoaldayDesign.InkMuted)
                        }
                        inner()
                    }
                    Text("入池", style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.InkSecondary, modifier = Modifier.clickable(onClick = ::addToPoolAndKeepFocus))
                    Text("加入", style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.Pink, modifier = Modifier.clickable(onClick = ::submitAndKeepFocus))
                }
            },
        )
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
            days.forEach { day ->
                Text(
                    "${day}日",
                    color = if (day == selectedDay) Color.White else GoaldayDesign.InkSecondary,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                        .background(if (day == selectedDay) GoaldayDesign.Pink else Color(0x0F000000), RoundedCornerShape(GoaldayDesign.RadiusPill))
                        .clickable { onSelectDay(day) }
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                )
            }
        }
        if (poolItems.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("待安排 · 长按拖入日期", style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.InkMuted)
                poolItems.forEach { item ->
                    var rowOrigin by remember(item) { mutableStateOf(Offset.Zero) }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(GoaldayDesign.RadiusS))
                            .background(Color(0x08E88FAE))
                            .onGloballyPositioned { coordinates ->
                                rowOrigin = coordinates.boundsInRoot().topLeft
                            }
                            .pointerInput(item) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = { start -> onPoolDragStart(item, rowOrigin + start) },
                                    onDrag = { _, dragAmount -> onPoolDrag(dragAmount) },
                                    onDragEnd = onPoolDragEnd,
                                    onDragCancel = onPoolDragCancel,
                                )
                            }
                            .clickable { onPickPoolItem(item) }
                            .padding(horizontal = 5.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("□", style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.InkMuted)
                        Text(item, style = MaterialTheme.typography.bodySmall, color = GoaldayDesign.InkPrimary, maxLines = 1, modifier = Modifier.weight(1f))
                        Text("×", style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.InkMuted, modifier = Modifier.clickable { onRemovePoolItem(item) })
                    }
                }
            }
        }
    }
}

@Composable
private fun DaySpreadEditableSection(
    day: Int,
    entries: List<ScheduleEntry>,
    editingId: String?,
    editingText: String,
    activeDrop: Boolean,
    onBounds: (Rect) -> Unit,
    onStartEdit: (ScheduleEntry) -> Unit,
    onTextChange: (String) -> Unit,
    onCommit: (ScheduleEntry) -> Unit,
    onToggleCompleted: (ScheduleEntry) -> Unit,
    onEntryDragStart: (ScheduleEntry, Offset) -> Unit,
    onEntryDrag: (Offset) -> Unit,
    onEntryDragEnd: () -> Unit,
    onEntryDragCancel: () -> Unit,
) {
    val doneCount = entries.count { it.completed }
    val todoCount = entries.count { !it.completed }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (activeDrop) GoaldayDesign.PinkSoft else Color.Transparent, RoundedCornerShape(GoaldayDesign.RadiusS))
            .border(if (activeDrop) 0.9.dp else 0.35.dp, if (activeDrop) GoaldayDesign.Pink else Color(0x0A000000), RoundedCornerShape(GoaldayDesign.RadiusS))
            .onGloballyPositioned { coordinates -> onBounds(coordinates.boundsInRoot()) }
            .padding(horizontal = 5.dp, vertical = 3.dp),
        verticalArrangement = Arrangement.spacedBy(1.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("${day}日", style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.InkSecondary)
            Text("d${doneCount}/t${todoCount}", style = MaterialTheme.typography.labelSmall, color = Color(0xFFB07A8F))
        }
        repeat(4) { idx ->
            val entry = entries.getOrNull(idx)
            if (entry == null) {
                EmptyHandbookSlot(
                    label = if (activeDrop && idx == entries.size.coerceAtMost(3)) "释放到${day}日" else "",
                    highlight = activeDrop && idx == entries.size.coerceAtMost(3),
                )
            } else {
                HandbookEntryLine(
                    slotLabel = "${day}",
                    entry = entry,
                    editingId = editingId,
                    editingText = editingText,
                    onStartEdit = { onStartEdit(entry) },
                    onTextChange = onTextChange,
                    onCommit = { onCommit(entry) },
                    onToggleCompleted = { onToggleCompleted(entry) },
                    onDragStart = { position -> onEntryDragStart(entry, position) },
                    onDrag = onEntryDrag,
                    onDragEnd = onEntryDragEnd,
                    onDragCancel = onEntryDragCancel,
                )
            }
        }
    }
}

@Composable
private fun EmptyHandbookSlot(
    label: String,
    highlight: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            label.ifBlank { " " },
            style = MaterialTheme.typography.labelSmall,
            color = if (highlight) GoaldayDesign.Pink else GoaldayDesign.InkMuted,
            maxLines = 1,
            modifier = Modifier.width(54.dp),
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(if (highlight) 1.2.dp else 0.6.dp)
                .background(if (highlight) GoaldayDesign.Pink else Color(0x16000000)),
        )
    }
}

private fun pagePreviewText(page: BookPage): String =
    when (page) {
        is TargetPage -> page.items.take(3).joinToString("\n") { "• $it" }.ifBlank { BookStrings.contentEmpty }
        is PlanPage -> page.items.take(3).joinToString("\n") { "• $it" }.ifBlank { BookStrings.contentEmpty }
        is SchedulePage -> page.items.take(3).joinToString("\n") { "• $it" }.ifBlank { BookStrings.contentEmpty }
        is DiaryPage -> page.prompt
    }


@Composable
private fun HandbookEntryLine(
    slotLabel: String,
    entry: ScheduleEntry,
    editingId: String?,
    editingText: String,
    onStartEdit: () -> Unit,
    onTextChange: (String) -> Unit,
    onCommit: () -> Unit,
    onToggleCompleted: () -> Unit,
    onDragStart: (Offset) -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val rowEditorFocus = remember(entry.id) { FocusRequester() }
    var rowOrigin by remember(entry.id) { mutableStateOf(Offset.Zero) }
    LaunchedEffect(editingId) {
        if (editingId == entry.id) {
            rowEditorFocus.requestFocus()
        }
    }
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(16.dp)
            .onGloballyPositioned { coordinates ->
                rowOrigin = coordinates.boundsInRoot().topLeft
            }
            .pointerInput(entry.id, editingId) {
                if (editingId == entry.id || entry.id.startsWith("fallback_")) return@pointerInput
                detectDragGesturesAfterLongPress(
                    onDragStart = { start -> onDragStart(rowOrigin + start) },
                    onDrag = { _, dragAmount -> onDrag(dragAmount) },
                    onDragEnd = onDragEnd,
                    onDragCancel = onDragCancel,
                )
            },
    ) {
        Text(
            slotLabel,
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF9A958D),
            modifier = Modifier
                .width(18.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color(0x14000000))
                .padding(horizontal = 1.dp, vertical = 0.dp),
            textAlign = TextAlign.Center,
        )
        Text(
            if (entry.completed) "✓" else "○",
            style = MaterialTheme.typography.labelSmall,
            color = if (entry.completed) Color(0xFF7A9D73) else Color(0xFF9A958D),
            modifier = Modifier.padding(top = 1.dp).clickable { onToggleCompleted() },
        )
        if (editingId == entry.id) {
            BasicTextField(
                value = editingText,
                onValueChange = onTextChange,
                textStyle = TextStyle(color = Color(0xFF2F2E2C)),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    onCommit()
                    focusManager.clearFocus(force = true)
                }),
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(rowEditorFocus)
                    .background(Color(0x09000000), RoundedCornerShape(4.dp))
                    .padding(horizontal = 4.dp, vertical = 1.dp),
            )
            Text("Done", style = MaterialTheme.typography.labelSmall, color = Color(0xFFE18DA9), modifier = Modifier.clickable {
                onCommit()
                focusManager.clearFocus(force = true)
            })
        } else {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(4.dp))
                    .clickable { onStartEdit() }
                    .padding(horizontal = 2.dp, vertical = 0.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    entry.title,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (entry.completed) Color(0xFF7A746E) else Color(0xFF2F2E2C),
                    textDecoration = if (entry.completed) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = 1,
                    modifier = Modifier.weight(1f).padding(top = 1.dp),
                    textAlign = TextAlign.Start,
                )
                Text("✎", style = MaterialTheme.typography.labelSmall, color = Color(0xFFAAA39A), modifier = Modifier.padding(top = 1.dp))
            }
        }
    }
}

@Composable
private fun TargetDetailReplicaPage(
    pageTitle: String,
    baseItems: List<String>,
    customItems: List<String>,
    tint: Color,
    isChecked: (String, String) -> Boolean,
    onToggleChecked: (String, String) -> Unit,
    onAddCustomItem: (String) -> Unit,
    onRemoveCustomItem: (String) -> Unit,
    onRenameCustomItem: (String, String) -> Unit,
    onAddToSchedule: (String, Int) -> Unit,
) {
    val items = remember(baseItems, customItems) { (baseItems + customItems).distinct() }
    var draft by remember(pageTitle) { mutableStateOf("") }
    var editingItem by remember(pageTitle) { mutableStateOf<String?>(null) }
    var editingText by remember(pageTitle) { mutableStateOf("") }
    val todayDay = LocalDate.now().dayOfMonth

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
                Text("目标详情 · 勾选完成 · 一键排入今日日程", style = MaterialTheme.typography.bodySmall, color = GoaldayDesign.InkSecondary)
            }
        }

        items.chunked(2).forEachIndexed { rowIndex, rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                rowItems.forEachIndexed { columnIndex, item ->
                    val index = rowIndex * 2 + columnIndex
                    val checked = isChecked(pageTitle, item)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(GoaldayDesign.RadiusS))
                            .background(if (checked) GoaldayDesign.GreenSoft else Color(0xFFFFFEFC))
                            .border(0.6.dp, if (checked) GoaldayDesign.Positive.copy(alpha = 0.35f) else Color(0x12000000), RoundedCornerShape(GoaldayDesign.RadiusS))
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(if (checked) "✓" else "□", color = if (checked) GoaldayDesign.Positive else GoaldayDesign.InkMuted, modifier = Modifier.clickable { onToggleChecked(pageTitle, item) })
                                Text("目标 ${index + 1}", color = GoaldayDesign.InkMuted, style = MaterialTheme.typography.labelSmall)
                            }
                            Text("排入", color = GoaldayDesign.Pink, style = MaterialTheme.typography.labelSmall, modifier = Modifier.clickable { onAddToSchedule(item, todayDay) })
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
                                    .background(Color(0x08000000), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 5.dp, vertical = 3.dp),
                            )
                            Text("保存", color = GoaldayDesign.Pink, style = MaterialTheme.typography.labelSmall, modifier = Modifier.clickable {
                                if (item in customItems) onRenameCustomItem(item, editingText)
                                editingItem = null
                            })
                        } else {
                            Text(
                                item,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (checked) GoaldayDesign.InkSecondary else GoaldayDesign.InkPrimary,
                                textDecoration = if (checked) TextDecoration.LineThrough else TextDecoration.None,
                                maxLines = 2,
                                modifier = Modifier.clickable {
                                    if (item in customItems) {
                                        editingItem = item
                                        editingText = item
                                    }
                                },
                            )
                            if (item in customItems) {
                                Text("删除", color = GoaldayDesign.Danger, style = MaterialTheme.typography.labelSmall, modifier = Modifier.clickable { onRemoveCustomItem(item) })
                            }
                        }
                    }
                }
                if (rowItems.size == 1) Spacer(Modifier.weight(1f))
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
) {
    val stagedItems = remember(todayPlanItems, todayCompletedItems) { (todayPlanItems + todayCompletedItems).toSet() }
    val sourceBaseItems = remember(baseItems, stagedItems) { baseItems.filterNot { it in stagedItems } }
    val sourceCustomItems = remember(customItems, stagedItems) { customItems.filterNot { it in stagedItems } }
    val sourceItems = sourceBaseItems + sourceCustomItems
    val listNames = remember { listOf("Todo", "未来的自己", "奖励清单", "电影清单") }
    var selectedListIndex by remember(pageTitle) { mutableStateOf(0) }
    val shownSourceItems = remember(sourceItems, selectedListIndex) {
        when (selectedListIndex) {
            1 -> sourceItems.filter { it.contains("目标") || it.contains("学习") || it.contains("计划") || it.contains("未来") }
            2 -> sourceItems.filter { it.contains("奖励") || it.contains("完成") || it.contains("复盘") || it.contains("打卡") }
            3 -> sourceItems.filter { it.contains("电影") || it.contains("读书") || it.contains("阅读") || it.contains("TED") }
            else -> sourceItems
        }.ifEmpty { sourceItems }
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        ExecutionBoardHeader(
            title = if (isSchedulePage) "日程执行板" else "任务执行板",
        )
    ReferencePlannerBoard(
            sourceItems = shownSourceItems,
            todayItems = todayPlanItems,
            doneItems = todayCompletedItems,
            schedulePreviewEntries = schedulePreviewEntries,
            selectedListName = listNames[selectedListIndex],
            onSwitchList = { selectedListIndex = (selectedListIndex + 1) % listNames.size },
            onMoveItemToToday = onMoveItemToToday,
            onMoveItemToCompleted = onMoveItemToCompleted,
            onRestoreItemFromDone = onRestoreItemFromCompleted,
        )
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
) {
    val weekDates = (0..6).map { LocalDate.now().plusDays(it.toLong()) }
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
        when {
            entries.isEmpty() -> ""
            entries.first().completed -> "✓ ${entries.first().title}"
            else -> "· ${entries.first().title}"
        }
    }
    val todayPool = todayItems.distinct().take(6).map { BoardTask(id = "today_$it", title = it) }
    val poolSource = sourceItems.filterNot { it in todayItems }.distinct().take(8).map { BoardTask(id = "pool_$it", title = it) }
    val donePreview = doneItems.take(3).map { BoardTask(id = "done_$it", title = it, completed = true) }
    val allRight = (todayPool + poolSource + donePreview)
    var selectedId by remember(allRight) { mutableStateOf(allRight.firstOrNull()?.id) }

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
        topActions = {
            Text("切换", modifier = Modifier.clickable(onClick = onSwitchList), color = Color(0xFF6F675D), style = MaterialTheme.typography.labelSmall)
            Text("↺ 回收", modifier = Modifier.clickable { allRight.firstOrNull { it.id == selectedId }?.let { onRestoreItemFromDone(it.title) } }, color = Color(0xFF8B7E71), style = MaterialTheme.typography.labelSmall)
            Text(
                "✓ 完成",
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier
                    .clip(RoundedCornerShape(99.dp))
                    .background(Color(0xFF222222))
                    .clickable { allRight.firstOrNull { it.id == selectedId }?.let { onMoveItemToCompleted(it.title) } }
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            )
        },
    )
}

@Composable
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
) {
    val editingDiary = contentMode as? PageContentMode.EditingDiary
    var structured by remember(title, diaryDraft) { mutableStateOf(StructuredDiary.fromRaw(diaryDraft)) }
    var exportHint by remember(title) { mutableStateOf("") }
    val context = LocalContext.current
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
    fun applyLinkedTarget(item: String, completed: Boolean) {
        structured = if (completed) structured.withCompletedTarget(item) else structured.withWorkTarget(item)
        onDiaryChange(structured.toRaw())
        onContentModeChange(PageContentMode.EditingDiary(title))
    }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            listOf("📎 记忆", "🌿 心情", "📸 片段").forEach { sticker ->
                Text(
                    sticker,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF7B6A5A),
                    modifier = Modifier
                        .clip(RoundedCornerShape(99.dp))
                        .background(Color(0x12B59072))
                        .border(1.dp, Color(0x1EB59072), RoundedCornerShape(99.dp))
                        .padding(horizontal = 7.dp, vertical = 3.dp),
                )
            }
        }
        Text(text = prompt, style = MaterialTheme.typography.labelSmall, color = Color(0xFF6E665D))
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
                onAddImage = { imagePicker.launch(arrayOf("image/*")) },
                onRemoveImage = { uri ->
                    structured = structured.withoutImageUri(uri)
                    onDiaryChange(structured.toRaw())
                },
                onDone = {
                    onDiaryChange(structured.toRaw())
                    onContentModeChange(PageContentMode.Browsing)
                },
            )
        } else {
            PaperNoteCard(
                modifier = Modifier.clickable {
                    onContentModeChange(pageContentModeForTap(DiaryPage(title, prompt)))
                },
            ) {
                StructuredDiaryPreview(
                    state = StructuredDiary.fromRaw(diaryDraft),
                    onAddImage = {
                        onContentModeChange(PageContentMode.EditingDiary(title))
                        imagePicker.launch(arrayOf("image/*"))
                    },
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = {
                val uri = exportDiaryLongImage(context, title, currentDiaryState())
                exportHint = if (uri != null) "已导出长图" else "导出失败"
            }) { Text("导出长图") }
            TextButton(onClick = {
                val uri = exportDiaryLongImage(context, title, currentDiaryState())
                exportHint = if (uri != null && shareLongImage(context, uri)) "已打开分享" else "分享失败"
            }) { Text("分享长图") }
            if (exportHint.isNotBlank()) {
                Text(exportHint, style = MaterialTheme.typography.labelSmall, color = Color(0xFF7A7065))
            }
        }
        Text(text = BookStrings.diaryLocalOnly, style = MaterialTheme.typography.labelSmall, color = tint.copy(alpha = 0.62f))
    }
}

private data class StructuredDiary(
    val moodTags: String,
    val todayDone: String,
    val workTasks: String,
    val smallJoy: String,
    val canImprove: String,
    val photoNotes: String,
) {
    val imageUris: List<String>
        get() = photoNotes.lines()
            .map(String::trim)
            .filter { it.startsWith(DIARY_IMAGE_PREFIX) }
            .map { it.removePrefix(DIARY_IMAGE_PREFIX).trim() }
            .filter(String::isNotBlank)

    val photoText: String
        get() = photoNotes.lines()
            .map(String::trim)
            .filter { it.isNotBlank() && !it.startsWith(DIARY_IMAGE_PREFIX) }
            .joinToString("\n")

    fun withPhotoText(text: String): StructuredDiary =
        copy(photoNotes = mergeDiaryPhotoNotes(text, imageUris))

    fun withImageUri(uri: String): StructuredDiary =
        copy(photoNotes = mergeDiaryPhotoNotes(photoText, (imageUris + uri).distinct()))

    fun withoutImageUri(uri: String): StructuredDiary =
        copy(photoNotes = mergeDiaryPhotoNotes(photoText, imageUris.filterNot { it == uri }))

    fun withCompletedTarget(item: String): StructuredDiary =
        copy(todayDone = appendUniqueDiaryLine(todayDone, item))

    fun withWorkTarget(item: String): StructuredDiary =
        copy(workTasks = appendUniqueDiaryLine(workTasks, item))

    fun toRaw(): String = buildString {
        appendLine("# 心情标签")
        appendLine(moodTags.trim())
        appendLine("# 今日完成")
        appendLine(todayDone.trim())
        appendLine("# 工作任务")
        appendLine(workTasks.trim())
        appendLine("# 小幸福")
        appendLine(smallJoy.trim())
        appendLine("# 可改进")
        appendLine(canImprove.trim())
        appendLine("# 图片")
        append(photoNotes.trim())
    }

    companion object {
        fun fromRaw(raw: String): StructuredDiary {
            if (raw.isBlank()) return StructuredDiary("", "", "", "", "", "")
            fun section(name: String, next: String?): String {
                val start = raw.indexOf("# $name")
                if (start < 0) return ""
                val bodyStart = raw.indexOf('\n', start).takeIf { it >= 0 }?.plus(1) ?: return ""
                val bodyEnd = next?.let { marker ->
                    raw.indexOf("# $marker", bodyStart).takeIf { it >= 0 }
                } ?: raw.length
                return raw.substring(bodyStart, bodyEnd).trim()
            }
            return StructuredDiary(
                moodTags = section("心情标签", "今日完成"),
                todayDone = section("今日完成", "工作任务"),
                workTasks = section("工作任务", "小幸福"),
                smallJoy = section("小幸福", "可改进"),
                canImprove = section("可改进", "图片"),
                photoNotes = section("图片", null),
            )
        }
    }
}

private const val DIARY_IMAGE_PREFIX = "image:"

private fun mergeDiaryPhotoNotes(text: String, imageUris: List<String>): String =
    buildList {
        text.lines().map(String::trim).filter(String::isNotBlank).forEach(::add)
        imageUris.distinct().forEach { uri -> add("$DIARY_IMAGE_PREFIX$uri") }
    }.joinToString("\n")

private fun appendUniqueDiaryLine(raw: String, item: String): String {
    val normalized = item.trim()
    if (normalized.isBlank()) return raw
    val lines = raw.lines().map(String::trim).filter(String::isNotBlank)
    if (normalized in lines) return raw
    return (lines + normalized).joinToString("\n")
}

private fun diaryDateLabel(date: LocalDate): String {
    val weekday = when (date.dayOfWeek) {
        DayOfWeek.MONDAY -> "周一"
        DayOfWeek.TUESDAY -> "周二"
        DayOfWeek.WEDNESDAY -> "周三"
        DayOfWeek.THURSDAY -> "周四"
        DayOfWeek.FRIDAY -> "周五"
        DayOfWeek.SATURDAY -> "周六"
        DayOfWeek.SUNDAY -> "周日"
    }
    return "${date.monthValue}月${date.dayOfMonth}日 · $weekday"
}

private fun exportHandbookScheduleLongImage(
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

private fun renderHandbookScheduleLongImage(
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
    canvas.drawColor(0xFFFFFBF6.toInt())
    val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF2F2922.toInt()
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
            appendLine("done")
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
    val estimatedHeight = 1500 + state.imageUris.take(3).size * 300 + state.toRaw().length.coerceAtMost(1600)
    val scratch = Bitmap.createBitmap(width, estimatedHeight.coerceAtLeast(2200), Bitmap.Config.ARGB_8888)
    val canvas = Canvas(scratch)
    canvas.drawColor(0xFFFFFBF6.toInt())
    val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF2F2922.toInt()
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
    canvas.drawText(diaryDateLabel(LocalDate.now()), padding, y, subtitlePaint)
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
    state.imageUris.take(3).forEachIndexed { index, uri ->
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
    val done = doneItems.map(String::trim).filter(String::isNotBlank).distinct().take(3)
    val todo = todoItems.map(String::trim).filter(String::isNotBlank).filterNot { it in done }.distinct().take(3)
    if (done.isEmpty() && todo.isEmpty()) return
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0x10E88FAE))
            .border(0.7.dp, Color(0x20E88FAE), RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Text("关联目标", style = MaterialTheme.typography.labelSmall, color = Color(0xFF7A6B5F), fontWeight = FontWeight.Medium)
        if (done.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.fillMaxWidth()) {
                done.forEach { item ->
                    DiaryLinkedTargetChip("✓ $item", GoaldayDesign.Positive, Modifier.weight(1f)) { onPickDone(item) }
                }
            }
        }
        if (todo.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.fillMaxWidth()) {
                todo.forEach { item ->
                    DiaryLinkedTargetChip("○ $item", Color(0xFFB07A8F), Modifier.weight(1f)) { onPickTodo(item) }
                }
            }
        }
    }
}

@Composable
private fun DiaryLinkedTargetChip(
    text: String,
    color: Color,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    Text(
        text,
        color = color,
        style = MaterialTheme.typography.labelSmall,
        maxLines = 1,
        modifier = modifier
            .clip(RoundedCornerShape(99.dp))
            .background(Color.White.copy(alpha = 0.72f))
            .clickable { onClick() }
            .padding(horizontal = 6.dp, vertical = 4.dp),
    )
}

@Composable
private fun StructuredDiaryEditor(
    state: StructuredDiary,
    onStateChange: (StructuredDiary) -> Unit,
    onAddImage: () -> Unit,
    onRemoveImage: (String) -> Unit,
    onDone: () -> Unit,
) {
    val dateLabel = remember { diaryDateLabel(LocalDate.now()) }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(dateLabel, style = MaterialTheme.typography.labelLarge, color = Color(0xFF3A342E), modifier = Modifier.align(Alignment.CenterHorizontally))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                OutlinedTextField(
                    value = state.moodTags,
                    onValueChange = { onStateChange(state.copy(moodTags = it)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
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
                    .background(Color(0x18B7A893)),
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                DiaryEditField("🍀 小幸福", state.smallJoy) { onStateChange(state.copy(smallJoy = it)) }
                DiaryEditField("📝 可改进", state.canImprove) { onStateChange(state.copy(canImprove = it)) }
                DiaryEditField("📷 图片描述", state.photoText) { onStateChange(state.withPhotoText(it)) }
            }
        }
        if (state.imageUris.isNotEmpty()) {
            DiaryImageStrip(
                imageUris = state.imageUris,
                onRemoveImage = onRemoveImage,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onAddImage) { Text("添加图片") }
            TextButton(onClick = onDone) { Text("完成") }
        }
    }
}

@Composable
private fun StructuredDiaryPreview(
    state: StructuredDiary,
    onAddImage: () -> Unit,
) {
    val dateLabel = remember { diaryDateLabel(LocalDate.now()) }
    val moodItems = remember(state.moodTags) {
        state.moodTags.split(',', '，', ' ').map(String::trim).filter(String::isNotBlank).take(6)
    }
    val photos = state.photoText.lines().map(String::trim).filter(String::isNotBlank)
    Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
        Text(dateLabel, style = MaterialTheme.typography.labelLarge, color = Color(0xFF3A342E), modifier = Modifier.align(Alignment.CenterHorizontally))
        if (moodItems.isNotEmpty()) {
            Text(
                moodItems.joinToString("  ") { "#$it" },
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF8B7A68),
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        }
        if (photos.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.fillMaxWidth()) {
                photos.take(3).forEach { note ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF2EFE9))
                            .border(1.dp, Color(0xFFE6DED2), RoundedCornerShape(8.dp))
                            .padding(5.dp),
                    ) {
                        Text(note, style = MaterialTheme.typography.labelSmall, color = Color(0xFF6B6258))
                    }
                }
            }
        }
        if (state.imageUris.isNotEmpty()) {
            DiaryImageStrip(imageUris = state.imageUris, onRemoveImage = null)
        } else if (photos.isEmpty()) {
            Text(
                "＋ 添加图片",
                color = Color(0xFFB07A8F),
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier
                    .align(Alignment.End)
                    .clickable { onAddImage() }
                    .padding(horizontal = 4.dp, vertical = 2.dp),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0x14EFE3D4))
                        .border(1.dp, Color(0x26CCB79F), RoundedCornerShape(10.dp))
                        .padding(horizontal = 7.dp, vertical = 6.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        DiaryLine("☀️ 今日完成", state.todayDone)
                        DiaryLine("📚 工作任务", state.workTasks)
                    }
                }
            }
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(Color(0x18B7A893)),
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0x11F5E9DB))
                        .border(1.dp, Color(0x22C5AC8F), RoundedCornerShape(10.dp))
                        .padding(horizontal = 7.dp, vertical = 6.dp),
                ) {
                    DiaryLine("🍀 小幸福", state.smallJoy)
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0x14E9AFC0))
                        .border(1.dp, Color(0x2AE9AFC0), RoundedCornerShape(8.dp))
                        .padding(horizontal = 6.dp, vertical = 5.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text("📝 可改进", style = MaterialTheme.typography.labelSmall, color = Color(0xFF5A4A3B))
                        Text(
                            state.canImprove.ifBlank { "记录今天想优化的一件小事" },
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF2F2922),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DiaryImageStrip(
    imageUris: List<String>,
    onRemoveImage: ((String) -> Unit)?,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
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

@Composable
private fun DiaryImageTile(
    uri: String,
    onRemove: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val bitmap = remember(uri) {
        runCatching {
            context.contentResolver.openInputStream(Uri.parse(uri))?.use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        }.getOrNull()
    }
    Box(
        modifier = modifier
            .height(76.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(Color(0xFFF2EFE9))
            .border(1.dp, Color(0xFFE6DED2), RoundedCornerShape(9.dp)),
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Text(
                "图片不可读",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF8B7A68),
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
                    .background(Color(0xAA1F1B17), RoundedCornerShape(bottomStart = 8.dp))
                    .clickable { remove() }
                    .padding(horizontal = 6.dp, vertical = 2.dp),
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
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0x1AF4DABB), Color(0x0EF4DABB), Color(0x14F6E8D3)),
                ),
            )
            .border(1.dp, Color(0x22C8AF91), RoundedCornerShape(12.dp))
            .padding(horizontal = 9.dp, vertical = 7.dp),
    ) {
        Text(content, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF342C24))
    }
}

@Composable
private fun DiaryLine(title: String, content: String) {
    if (content.isBlank()) return
    Text(title, style = MaterialTheme.typography.labelMedium, color = Color(0xFF5A4A3B))
    content.lines().map(String::trim).filter(String::isNotBlank).take(3).forEachIndexed { index, line ->
        Text("${index + 1}. $line", style = MaterialTheme.typography.bodySmall, color = Color(0xFF2F2922))
    }
}

@Composable
private fun DiaryEditField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
) {
    Text(label, style = MaterialTheme.typography.labelSmall, color = Color(0xFF5A4A3B))
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        minLines = 1,
        maxLines = 3,
        shape = RoundedCornerShape(8.dp),
    )
}

@Composable
private fun NotebookSpread(content: @Composable ColumnScope.() -> Unit) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFFFFFEFB), Color(0xFFFFFCF7), Color(0xFFFEF8EF)),
                ),
            )
            .border(1.dp, Color(0xFFE8DFD3), RoundedCornerShape(18.dp))
            .padding(12.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(Brush.horizontalGradient(listOf(Color(0x22C6B8A5), Color.Transparent, Color(0x22C6B8A5)))),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Brush.horizontalGradient(listOf(Color.Transparent, Color(0x18BFA991), Color.Transparent))),
        )
        content()
    }
}

@Composable
private fun DiarySticker(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = Color(0xFF5B4A3C),
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0x33E9D6BC))
            .padding(horizontal = 8.dp, vertical = 4.dp),
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
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF8F8F6))
            .border(1.dp, Color(0xFFE9E6E1), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 9.dp),
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0x0F9D958C)))
        content()
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0x0A9D958C)))
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
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(99.dp)).background(tint))
                Spacer(Modifier.width(6.dp))
                Text(bookTitle, style = MaterialTheme.typography.titleSmall, color = Color(0xFF342C24))
            }
            Text(
                text = savedText,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF8B7660),
                modifier = if (onSavedClick != null) Modifier.clickable(onClick = onSavedClick) else Modifier,
            )
        }
        Text(subtitle, style = MaterialTheme.typography.labelSmall, color = Color(0xFF7B6A59))
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0x1A9C7C5C)))
    }
}

@Composable
private fun DiaryToolChip(
    label: String,
    onClick: () -> Unit,
) {
    Text(
        text = label,
        color = Color(0xFF8F684F),
        modifier = Modifier
            .clip(RoundedCornerShape(99.dp))
            .background(Color(0x1A8F684F))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    )
}
