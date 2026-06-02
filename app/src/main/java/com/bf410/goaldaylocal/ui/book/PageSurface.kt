package com.bf410.goaldaylocal.ui.book

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
    onAddScheduleFromHandbook: (String, Int, Int) -> Unit,
    onWeeklyThemeChange: (String) -> Unit,
    onMoveItemToToday: (String) -> Unit,
    onMoveItemToCompleted: (String) -> Unit,
    onRestoreItemFromToday: (String) -> Unit,
    onRestoreItemFromCompleted: (String) -> Unit,
    onUpdateScheduleTitle: (String, String) -> Unit,
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
            onAddSchedule = onAddScheduleFromHandbook,
            onWeeklyThemeChange = onWeeklyThemeChange,
            onUpdateScheduleTitle = onUpdateScheduleTitle,
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
                is TargetPage -> EditableBulletPage(page.title, page.items, customPageItems, tint, BookStrings.addTarget, false, isChecked, onToggleChecked, onAddCustomItem, onAddCustomItemWithDeadline, onRemoveCustomItem, onRenameCustomItem, onAddToSchedule, weeklyTheme, todayPlanItems, todayCompletedItems, schedulePreviewEntries, onWeeklyThemeChange, onMoveItemToToday, onMoveItemToCompleted, onRestoreItemFromToday, onRestoreItemFromCompleted, contentMode, onContentModeChange)
                is SchedulePage -> EditableBulletPage(page.title, page.items, customPageItems, tint.copy(alpha = 0.74f), BookStrings.addSchedule, true, isChecked, onToggleChecked, onAddCustomItem, onAddCustomItemWithDeadline, onRemoveCustomItem, onRenameCustomItem, onAddToSchedule, weeklyTheme, todayPlanItems, todayCompletedItems, schedulePreviewEntries, onWeeklyThemeChange, onMoveItemToToday, onMoveItemToCompleted, onRestoreItemFromToday, onRestoreItemFromCompleted, contentMode, onContentModeChange)
                is PlanPage -> EditableBulletPage(page.title, page.items, customPageItems, Color(0xFFB88A58), BookStrings.addPlan, false, isChecked, onToggleChecked, onAddCustomItem, onAddCustomItemWithDeadline, onRemoveCustomItem, onRenameCustomItem, onAddToSchedule, weeklyTheme, todayPlanItems, todayCompletedItems, schedulePreviewEntries, onWeeklyThemeChange, onMoveItemToToday, onMoveItemToCompleted, onRestoreItemFromToday, onRestoreItemFromCompleted, contentMode, onContentModeChange)
                is DiaryPage -> DiarySection(page.title, page.prompt, tint, diaryDraft, pendingCommand, onCommand, onDiaryChange, contentMode, onContentModeChange)
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
    onAddSchedule: (String, Int, Int) -> Unit,
    onWeeklyThemeChange: (String) -> Unit,
    onUpdateScheduleTitle: (String, String) -> Unit,
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
    val start = if (anchorYear == today.year && anchorMonth == today.monthValue) {
        (today.dayOfMonth - 1).coerceIn(0, monthLength - 1)
    } else {
        0
    }
    val dayBlocks = List(3) { offset ->
        val day = ((start + offset) % monthLength) + 1
        val dayEntries = sorted.filter { it.day == day }
        DaySpreadBlock(
            day = day,
            done = dayEntries.filter { it.completed }.take(3),
            todo = dayEntries.filterNot { it.completed }.take(3),
        )
    }
    val leftBlocks = dayBlocks
    val rightBlocks = dayBlocks
    val fallbackLeftDone = todayCompletedItems.take(3)
    val fallbackRightTodo = todayPlanItems.take(3)
    var draftText by remember(page.title) { mutableStateOf("") }
    var draftDay by remember(page.title) { mutableStateOf(rightBlocks.firstOrNull()?.day ?: 1) }
    val selectedDraftDay = draftDay.coerceIn(1, monthLength)
    var editingId by remember(pageIndex) { mutableStateOf<String?>(null) }
    var editingText by remember(pageIndex) { mutableStateOf("") }
    var saveHint by remember(pageIndex) { mutableStateOf("") }
    LaunchedEffect(saveHint) {
        if (saveHint.isBlank()) return@LaunchedEffect
        delay(1200)
        saveHint = ""
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
        )
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
                    )
                }
            }
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
) {
    val monthModel = remember(year, month) { YearMonth.of(year, month) }
    val weekdays = listOf("M", "T", "W", "T", "F", "S", "S")
    val firstWeekDays = remember(monthModel) {
        List(7) { offset ->
            monthModel.atDay((offset + 1).coerceAtMost(monthModel.lengthOfMonth()))
        }
    }
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
            }
            Row(
                modifier = Modifier
                    .weight(1.12f)
                    .clip(RoundedCornerShape(GoaldayDesign.RadiusS))
                    .background(Color(0x08E88FAE))
                    .border(0.35.dp, Color(0x10E88FAE), RoundedCornerShape(GoaldayDesign.RadiusS))
                    .padding(horizontal = 4.dp, vertical = 3.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                firstWeekDays.forEachIndexed { index, date ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(weekdays[index], style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.InkMuted)
                        Text(
                            date.dayOfMonth.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (date.dayOfMonth == LocalDate.now().dayOfMonth && date.monthValue == LocalDate.now().monthValue) {
                                GoaldayDesign.Pink
                            } else {
                                GoaldayDesign.InkSecondary
                            },
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
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
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(0.35.dp, Color(0x0A000000), RoundedCornerShape(GoaldayDesign.RadiusS))
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
        if (done.isEmpty()) {
            Text("○", style = MaterialTheme.typography.bodySmall, color = GoaldayDesign.InkMuted)
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
    onDone: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    fun submitAndKeepFocus() {
        onDone()
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
    }
}

@Composable
private fun DaySpreadEditableSection(
    day: Int,
    entries: List<ScheduleEntry>,
    editingId: String?,
    editingText: String,
    onStartEdit: (ScheduleEntry) -> Unit,
    onTextChange: (String) -> Unit,
    onCommit: (ScheduleEntry) -> Unit,
    onToggleCompleted: (ScheduleEntry) -> Unit,
) {
    val doneCount = entries.count { it.completed }
    val todoCount = entries.count { !it.completed }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(0.35.dp, Color(0x0A000000), RoundedCornerShape(GoaldayDesign.RadiusS))
            .padding(horizontal = 5.dp, vertical = 3.dp),
        verticalArrangement = Arrangement.spacedBy(1.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("${day}日", style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.InkSecondary)
            Text("d${doneCount}/t${todoCount}", style = MaterialTheme.typography.labelSmall, color = Color(0xFFB07A8F))
        }
        repeat(3) { idx ->
            val entry = entries.getOrNull(idx)
            if (entry == null) {
                Text("○", style = MaterialTheme.typography.bodySmall, color = GoaldayDesign.InkMuted)
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
                )
            }
        }
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
) {
    val focusManager = LocalFocusManager.current
    val rowEditorFocus = remember(entry.id) { FocusRequester() }
    LaunchedEffect(editingId) {
        if (editingId == entry.id) {
            rowEditorFocus.requestFocus()
        }
    }
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        modifier = Modifier.fillMaxWidth().height(16.dp),
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
    pendingCommand: RichEditorCommand?,
    onCommand: (RichEditorCommand) -> Unit,
    onDiaryChange: (String) -> Unit,
    contentMode: PageContentMode,
    onContentModeChange: (PageContentMode) -> Unit,
) {
    val editingDiary = contentMode as? PageContentMode.EditingDiary
    var structured by remember(title, diaryDraft) { mutableStateOf(StructuredDiary.fromRaw(diaryDraft)) }

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
        if (editingDiary?.title == title) {
            StructuredDiaryEditor(
                state = structured,
                onStateChange = { structured = it },
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
                StructuredDiaryPreview(state = StructuredDiary.fromRaw(diaryDraft))
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

@Composable
private fun StructuredDiaryEditor(
    state: StructuredDiary,
    onStateChange: (StructuredDiary) -> Unit,
    onDone: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("4月4日 · 周四", style = MaterialTheme.typography.labelLarge, color = Color(0xFF3A342E), modifier = Modifier.align(Alignment.CenterHorizontally))
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
                DiaryEditField("📷 图片描述", state.photoNotes) { onStateChange(state.copy(photoNotes = it)) }
            }
        }
        TextButton(onClick = onDone) { Text("完成") }
    }
}

@Composable
private fun StructuredDiaryPreview(state: StructuredDiary) {
    val moodItems = remember(state.moodTags) {
        state.moodTags.split(',', '，', ' ').map(String::trim).filter(String::isNotBlank).take(6)
    }
    val photos = state.photoNotes.lines().map(String::trim).filter(String::isNotBlank)
    Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
        Text("4月4日 · 周四", style = MaterialTheme.typography.labelLarge, color = Color(0xFF3A342E), modifier = Modifier.align(Alignment.CenterHorizontally))
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
