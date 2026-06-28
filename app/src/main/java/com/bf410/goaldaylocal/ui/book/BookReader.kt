package com.bf410.goaldaylocal.ui.book

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.graphicsLayer
import com.bf410.goaldaylocal.data.BookPage
import com.bf410.goaldaylocal.data.DiaryPage
import com.bf410.goaldaylocal.data.ScheduleEntry
import com.bf410.goaldaylocal.data.TargetItemMeta

@Composable
fun BookReader(
    bookId: String,
    bookTitle: String,
    subtitle: String,
    page: BookPage,
    previousPage: BookPage?,
    nextPage: BookPage?,
    pageIndex: Int,
    pageCount: Int,
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
    onAddScheduleFromHandbook: (String, Int, Int) -> Unit,
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
    shellStyle: ShellStyle = ShellStyle.LIGHT,
    handbookMode: Boolean = false,
    onFlipNext: () -> Unit,
    onFlipPrevious: () -> Unit,
) {
    var diaryCommand by remember(pageIndex, bookId) { mutableStateOf<RichEditorCommand?>(null) }
    var contentMode by remember(pageIndex, bookId) { mutableStateOf<PageContentMode>(PageContentMode.Browsing) }
    val turnEnabled = canTurnPage(contentMode)
    val turnProfile = if (handbookMode || page is DiaryPage || shellStyle == ShellStyle.BOOK) TurnProfile.HANDBOOK else TurnProfile.DEFAULT
    val pagePaddingH = if (turnProfile == TurnProfile.HANDBOOK) 8.dp else 28.dp
    val pagePaddingV = if (turnProfile == TurnProfile.HANDBOOK) 8.dp else 26.dp

    PageTurnEngine(
        canTurnPrevious = previousPage != null,
        canTurnNext = nextPage != null,
        turnEnabled = turnEnabled,
        onFlipNext = onFlipNext,
        onFlipPrevious = onFlipPrevious,
        profile = turnProfile,
        shell = { canPrev, canNext, enabled, tapPrev, tapNext, content ->
            BookShell(
                shellStyle = shellStyle,
                canTurnPrevious = canPrev,
                canTurnNext = canNext,
                turnEnabled = enabled,
                onTapPrevious = tapPrev,
                onTapNext = tapNext,
                content = content,
            )
        },
        destination = { progress, direction ->
            val destinationPage = when (direction) {
                TurnDirection.NEXT -> nextPage
                TurnDirection.PREVIOUS -> previousPage
                null -> null
            }
            val destinationIndex = when (direction) {
                TurnDirection.NEXT -> pageIndex + 1
                TurnDirection.PREVIOUS -> pageIndex - 1
                null -> pageIndex
            }.coerceIn(0, (pageCount - 1).coerceAtLeast(0))
            if (handbookMode && destinationPage != null) {
                ActivePageLayer(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = pagePaddingH, vertical = pagePaddingV)
                        .graphicsLayer { alpha = destinationRevealAlpha(progress) },
                    page = destinationPage,
                    pageIndex = destinationIndex,
                    pageCount = pageCount,
                    bookTitle = bookTitle,
                    subtitle = destinationPageSubtitle(direction, subtitle),
                    tint = tint,
                    isSaved = isSaved,
                    diaryDraft = diaryDraft,
                    customPageItems = customPageItems,
                    weeklyTheme = weeklyTheme,
                    todayPlanItems = todayPlanItems,
                    todayCompletedItems = todayCompletedItems,
                    schedulePreviewEntries = schedulePreviewEntries,
                    targetItemMeta = targetItemMeta,
                    onToggleSaved = onToggleSaved,
                    isChecked = isChecked,
                    onToggleChecked = onToggleChecked,
                    onDiaryChange = onDiaryChange,
                    onAddCustomItem = onAddCustomItem,
                    onAddCustomItemWithDeadline = onAddCustomItemWithDeadline,
                    onRemoveCustomItem = onRemoveCustomItem,
                    onRenameCustomItem = onRenameCustomItem,
                    onAddToSchedule = onAddToSchedule,
                    onAddHandbookPoolItem = onAddHandbookPoolItem,
                    onRemoveHandbookPoolItem = onRemoveHandbookPoolItem,
                    onAddScheduleFromHandbook = onAddScheduleFromHandbook,
                    onWeeklyThemeChange = onWeeklyThemeChange,
                    onMoveItemToToday = onMoveItemToToday,
                    onMoveItemToCompleted = onMoveItemToCompleted,
                    onRestoreItemFromToday = onRestoreItemFromToday,
                    onRestoreItemFromCompleted = onRestoreItemFromCompleted,
                    onUpdateScheduleTitle = onUpdateScheduleTitle,
                    onMoveScheduleDay = onMoveScheduleDay,
                    onToggleScheduleCompleted = onToggleScheduleCompleted,
                    onUpdateTargetNote = onUpdateTargetNote,
                    onUpdateTargetDeadline = onUpdateTargetDeadline,
                    onOpenTargetDetail = onOpenTargetDetail,
                    pendingCommand = diaryCommand,
                    onCommand = { diaryCommand = it },
                    contentMode = PageContentMode.Browsing,
                    onContentModeChange = { },
                    handbookMode = true,
                    turnProgress = 0f,
                    turnDirection = null,
                )
            } else {
                DestinationPageLayer(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = pagePaddingH, vertical = pagePaddingV),
                    bookTitle = bookTitle,
                    subtitle = subtitle,
                    page = destinationPage,
                    pageIndex = pageIndex,
                    pageCount = pageCount,
                    tint = tint,
                    revealProgress = progress,
                    direction = direction,
                )
            }
        },
        pageBack = { progress, direction, anchorY ->
            PageBackLayer(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = pagePaddingH, vertical = pagePaddingV)
                    .pageBackTransform(direction, progress, anchorY, turnProfile),
                tint = tint,
                progress = progress,
                direction = direction,
            )
        },
        activePage = { progress, direction, anchorY ->
            ActivePageLayer(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = pagePaddingH, vertical = pagePaddingV)
                    .turningPageTransform(direction, progress, anchorY, turnProfile),
                page = page,
                pageIndex = pageIndex,
                pageCount = pageCount,
                bookTitle = bookTitle,
                subtitle = subtitle,
                tint = tint,
                isSaved = isSaved,
                diaryDraft = diaryDraft,
                customPageItems = customPageItems,
                weeklyTheme = weeklyTheme,
                todayPlanItems = todayPlanItems,
                todayCompletedItems = todayCompletedItems,
                schedulePreviewEntries = schedulePreviewEntries,
                targetItemMeta = targetItemMeta,
                onToggleSaved = onToggleSaved,
                isChecked = isChecked,
                onToggleChecked = onToggleChecked,
                onDiaryChange = onDiaryChange,
                onAddCustomItem = onAddCustomItem,
                onAddCustomItemWithDeadline = onAddCustomItemWithDeadline,
                onRemoveCustomItem = onRemoveCustomItem,
                onRenameCustomItem = onRenameCustomItem,
                onAddToSchedule = onAddToSchedule,
                onAddHandbookPoolItem = onAddHandbookPoolItem,
                onRemoveHandbookPoolItem = onRemoveHandbookPoolItem,
                onAddScheduleFromHandbook = onAddScheduleFromHandbook,
                onWeeklyThemeChange = onWeeklyThemeChange,
                onMoveItemToToday = onMoveItemToToday,
                onMoveItemToCompleted = onMoveItemToCompleted,
                onRestoreItemFromToday = onRestoreItemFromToday,
                onRestoreItemFromCompleted = onRestoreItemFromCompleted,
                onUpdateScheduleTitle = onUpdateScheduleTitle,
                onMoveScheduleDay = onMoveScheduleDay,
                onToggleScheduleCompleted = onToggleScheduleCompleted,
                onUpdateTargetNote = onUpdateTargetNote,
                onUpdateTargetDeadline = onUpdateTargetDeadline,
                onOpenTargetDetail = onOpenTargetDetail,
                pendingCommand = diaryCommand,
                onCommand = { diaryCommand = it },
                contentMode = contentMode,
                onContentModeChange = { contentMode = it },
                handbookMode = handbookMode,
                turnProgress = progress,
                turnDirection = direction,
            )
        },
        spine = { visualProgress, active ->
            SpineLayer(visualProgress = visualProgress, active = active, profile = turnProfile)
        },
    )
}
