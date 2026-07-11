package com.bf410.goaldaylocal.ui.book

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.graphicsLayer
import com.bf410.goaldaylocal.data.BookPage
import com.bf410.goaldaylocal.data.DiaryPage
import com.bf410.goaldaylocal.data.ScheduleEntry
import com.bf410.goaldaylocal.data.TargetItemMeta
import com.tencent.mmkv.MMKV

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
    shellStyle: ShellStyle = ShellStyle.BOOK,
    handbookMode: Boolean = false,
    onFlipNext: () -> Unit,
    onFlipPrevious: () -> Unit,
) {
    var diaryCommand by remember(pageIndex, bookId) { mutableStateOf<RichEditorCommand?>(null) }
    var contentMode by remember(pageIndex, bookId) { mutableStateOf<PageContentMode>(PageContentMode.Browsing) }
    val turnEnabled = canTurnPage(contentMode)
    val turnProfile = if (handbookMode || page is DiaryPage) TurnProfile.HANDBOOK else TurnProfile.DEFAULT
    // 书页内边距：HANDBOOK 模式下 BookShell 已提供书页边距，这里不再额外留白，
    // 避免版心过窄；非 HANDBOOK 模式保留原卡片式边距。
    val pagePaddingH = if (turnProfile == TurnProfile.HANDBOOK) 0.dp else 28.dp
    val pagePaddingV = if (turnProfile == TurnProfile.HANDBOOK) 0.dp else 26.dp

    // 手账页强制使用 SIMULATION 仿真翻页，还原真实书本翻页效果；
    // 非手账模块保留 COVER，兼顾性能。
    // 关键修复：remember 必须跟随 turnProfile，否则 HANDBOOK 首次进入时可能仍用 COVER。
    val turnStyle = remember(turnProfile) {
        val default = if (turnProfile == TurnProfile.HANDBOOK) PageTurnStyle.SIMULATION else PageTurnStyle.COVER
        val raw = MMKV.defaultMMKV().decodeString("page_turn_style", default.name)
        val parsed = runCatching { PageTurnStyle.valueOf((raw ?: default.name).uppercase()) }.getOrDefault(default)
        if (turnProfile == TurnProfile.HANDBOOK) PageTurnStyle.SIMULATION else parsed
    }

    @Composable
    fun renderActivePage(modifier: Modifier, progress: Float, direction: TurnDirection?) {
        ActivePageLayer(
            modifier = modifier,
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
    }

    if (turnStyle == PageTurnStyle.SIMULATION) {
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
                        .graphicsLayer { alpha = destinationRevealAlpha(progress, turnProfile) },
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
            val draggingToNext = direction == TurnDirection.NEXT
            val draggingToPrevious = direction == TurnDirection.PREVIOUS
            renderActivePage(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = pagePaddingH, vertical = pagePaddingV)
                    // 页缘卷曲光影：画在 active page 上，随页面一起转动
.drawBehind {
                        if (direction == null || progress <= 0.01f) return@drawBehind
                        val edgeWidth = (16f + progress * 42f).coerceAtMost(80f)
                        // 外侧受光、内侧背光：页缘边界高亮，向页面内部渐变为阴影
                        if (draggingToNext) {
                            // 右页外翻：左边界是页缘（亮）→ 向右变暗
                            drawRect(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = (0.04f + progress * 0.14f).coerceAtMost(0.20f)),
                                        Color.Black.copy(alpha = (0.06f + progress * 0.16f).coerceAtMost(0.22f)),
                                        Color.Transparent,
                                    ),
                                ),
                                topLeft = Offset(size.width - edgeWidth, 0f),
                                size = Size(edgeWidth, size.height),
                            )
                        } else if (draggingToPrevious) {
                            // 左页外翻：右边界是页缘（亮）→ 向左变暗
                            drawRect(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = (0.06f + progress * 0.16f).coerceAtMost(0.22f)),
                                        Color.White.copy(alpha = (0.04f + progress * 0.14f).coerceAtMost(0.20f)),
                                    ),
                                ),
                                topLeft = Offset(0f, 0f),
                                size = Size(edgeWidth, size.height),
                            )
                        }
                    }
                    .turningPageTransform(direction, progress, anchorY, turnProfile),
                progress = progress,
                direction = direction,
            )
        },
        spine = { visualProgress, active ->
            SpineLayer(visualProgress = visualProgress, active = active, profile = turnProfile)
        },
    )
    } else {
        // 非仿真翻页：覆盖/滚动/无动画，复用 BookShell 外壳 + AnimatedContent 切换
        SimplePageTurner(
            pageKey = pageIndex,
            canTurnPrevious = previousPage != null,
            canTurnNext = nextPage != null,
            turnEnabled = turnEnabled,
            onFlipNext = onFlipNext,
            onFlipPrevious = onFlipPrevious,
            shellStyle = shellStyle,
            style = turnStyle,
        ) {
            renderActivePage(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = pagePaddingH, vertical = pagePaddingV),
                progress = 0f,
                direction = null,
            )
        }
    }
}
