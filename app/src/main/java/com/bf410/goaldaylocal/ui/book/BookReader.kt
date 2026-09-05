package com.bf410.goaldaylocal.ui.book

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.bf410.goaldaylocal.data.BookPage
import com.bf410.goaldaylocal.data.DiaryPage
import com.bf410.goaldaylocal.data.ScheduleEntry
import com.bf410.goaldaylocal.data.TargetItemMeta
import com.tencent.mmkv.MMKV

/**
 * 对照原版 BaseBookViewKt.java L494-514：
 * 每一层书页都是白色背景、左平右圆圆角(10dp)、10dp 阴影、阴影色 #FFC5BBB6。
 */
@Composable
fun HandbookPageCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val pageShape = RoundedCornerShape(
        topStart = 0.dp,
        topEnd = 10.dp,
        bottomEnd = 10.dp,
        bottomStart = 0.dp,
    )
    val shadowColor = Color(0xFFC5BBB6)
    Box(
        modifier = modifier
            .fillMaxSize()
            // 对照原版 BaseBookViewKt.java L725：padding(end = 10dp) 模拟书页右侧页缘
            .padding(end = 10.dp)
            .shadow(
                elevation = 10.dp,
                shape = pageShape,
                clip = false,
                ambientColor = shadowColor,
                spotColor = shadowColor,
            )
            .clip(pageShape)
            .background(Color.White),
    ) {
        content()
    }
}

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
    startInMonthBoard: Boolean = false,
    pages: List<BookPage> = emptyList(),
    currentPageIndex: Int = pageIndex,
    onFlipNext: () -> Unit,
    onFlipPrevious: () -> Unit,
) {
    var diaryCommand by remember(pageIndex, bookId) { mutableStateOf<RichEditorCommand?>(null) }
    var contentMode by remember(pageIndex, bookId) { mutableStateOf<PageContentMode>(PageContentMode.Browsing) }
    val turnEnabled = canTurnPage(contentMode)
    val turnProfile = if (handbookMode || page is DiaryPage) TurnProfile.HANDBOOK else TurnProfile.DEFAULT
    // 暴露 PageTurnEngine 的 TurnController，以便在 lambda 中获取 6-page rotation configurator。
    val turnControllerRef = remember { mutableStateOf<TurnController?>(null) }
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
            startInMonthBoard = startInMonthBoard,
        )
    }

    @Composable
    fun renderPageByIndex(modifier: Modifier, targetPage: BookPage, targetIndex: Int) {
        ActivePageLayer(
            modifier = modifier,
            page = targetPage,
            pageIndex = targetIndex,
            pageCount = pageCount,
            bookTitle = bookTitle,
            subtitle = if (targetIndex == pageIndex) subtitle else destinationPageSubtitle(
                if (targetIndex > pageIndex) TurnDirection.NEXT else TurnDirection.PREVIOUS,
                subtitle
            ),
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
            contentMode = if (targetIndex == pageIndex) contentMode else PageContentMode.Browsing,
            onContentModeChange = if (targetIndex == pageIndex) { { contentMode = it } } else { { } },
            handbookMode = handbookMode,
            turnProgress = 0f,
            turnDirection = null,
            startInMonthBoard = startInMonthBoard,
        )
    }

    // 目标页（正面/背面）统一渲染：destination 与 pageBack 共享此 helper，避免 40+ 参数列表重复
    @Composable
    fun renderTargetPageLayer(
        targetPage: BookPage,
        targetIndex: Int,
        subtitle: String,
        pageAlpha: Float,
        rotationY: Float,
        cardModifier: Modifier,
    ) {
        HandbookPageCard(modifier = cardModifier) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        this.alpha = pageAlpha
                        if (rotationY != 0f) this.rotationY = rotationY
                    },
            ) {
                ActivePageLayer(
                    modifier = Modifier.fillMaxSize(),
                    page = targetPage,
                    pageIndex = targetIndex,
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
                    contentMode = PageContentMode.Browsing,
                    onContentModeChange = { },
                    handbookMode = true,
                    turnProgress = 0f,
                    turnDirection = null,
                )
            }
        }
    }

    if (turnStyle == PageTurnStyle.SIMULATION) {
    PageTurnEngine(
        canTurnPrevious = previousPage != null,
        canTurnNext = nextPage != null,
        turnEnabled = turnEnabled,
        onFlipNext = onFlipNext,
        onFlipPrevious = onFlipPrevious,
        profile = turnProfile,
        // 对照原版 BaseBookViewKt.java L232-253：
        // 手账书 pageIndex>0 表示已翻开过（非封面/首页），用打开态阈值 0.3；
        // 首页/封面用闭合态阈值 0.5。非手账模式 useDualStateThreshold=false 忽略此值。
        bookIsOpen = handbookMode && pageIndex > 0,
        controllerRef = turnControllerRef,
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
            val configurator = turnControllerRef.value?.configurator
            if (handbookMode && destinationPage != null) {
                // 对照原版 BaseBookViewKt：目标页正面，alpha 硬切（正面 rotation <=90° 可见）
                val frontRotation = if (configurator != null) {
                    kotlin.math.abs(configurator.handbookPageRotationY(direction, progress))
                } else {
                    180f * progress.coerceIn(0f, 1f)
                }
                val alpha = if (frontRotation <= 90f) 1f else 0f
                renderTargetPageLayer(
                    targetPage = destinationPage,
                    targetIndex = destinationIndex,
                    subtitle = destinationPageSubtitle(direction, subtitle),
                    pageAlpha = alpha,
                    rotationY = 0f,
                    cardModifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = pagePaddingH, vertical = pagePaddingV),
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
            val destinationPage = when (direction) {
                TurnDirection.NEXT -> nextPage
                TurnDirection.PREVIOUS -> previousPage
                null -> null
            }
            val configurator = turnControllerRef.value?.configurator
            if (handbookMode && destinationPage != null) {
                // 对照原版 BaseBookViewKt：背面 = 目标页内容 + rotationY(180°)
                // 背面在正面 rotation > 90° 时可见
                val frontRotation = if (configurator != null) {
                    kotlin.math.abs(configurator.handbookPageRotationY(direction, progress))
                } else {
                    180f * progress.coerceIn(0f, 1f)
                }
                val alpha = if (frontRotation > 90f) 1f else 0f
                val backIndex = when (direction) {
                    TurnDirection.NEXT -> pageIndex + 1
                    TurnDirection.PREVIOUS -> pageIndex - 1
                    null -> pageIndex
                }.coerceIn(0, (pageCount - 1).coerceAtLeast(0))
                renderTargetPageLayer(
                    targetPage = destinationPage,
                    targetIndex = backIndex,
                    subtitle = destinationPageSubtitle(direction, subtitle),
                    pageAlpha = alpha,
                    // 对照原版 BaseBookViewKt L682：背面内层固定 rotationY=180°，
                    // 与外层 pageBackTransform 叠加后背面内容保持正向可读。
                    rotationY = 180f,
                    cardModifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = pagePaddingH, vertical = pagePaddingV)
                        .pageBackTransform(direction, progress, anchorY, turnProfile, configurator),
                )
            } else {
                PageBackLayer(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = pagePaddingH, vertical = pagePaddingV)
                        .pageBackTransform(direction, progress, anchorY, turnProfile, configurator),
                    tint = tint,
                    progress = progress,
                    direction = direction,
                )
            }
        },
        activePage = { progress, direction, anchorY ->
            // 使用单页绕书脊旋转：HANDBOOK 模式下 rotationY 最大 180°、alpha 90° 硬切，
            // 配合 destination/pageBack 显示目标页，避免多层 DEBUG 导致页面空白。
            val configurator = turnControllerRef.value?.configurator
            renderActivePage(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = pagePaddingH, vertical = pagePaddingV)
                    // 对照原版 BaseBookViewKt：HANDBOOK 翻页不叠加额外边缘渐变阴影，
                    // 仅依赖 HandbookPageCard 的 10dp 阴影（颜色 #FFC5BBB6）还原轻薄纸感。
                    .turningPageTransform(direction, progress, anchorY, turnProfile, configurator),
                progress = progress,
                direction = direction,
            )
        },
        spine = { visualProgress, active ->
            // 对照原版 BaseBookViewKt + 项目硬约束：HANDBOOK 页面背景纯白，
            // 不叠加中央书脊阴影/高光；仅非手账模式保留 SpineLayer。
            if (turnProfile != TurnProfile.HANDBOOK) {
                SpineLayer(visualProgress = visualProgress, active = active, profile = turnProfile)
            }
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
