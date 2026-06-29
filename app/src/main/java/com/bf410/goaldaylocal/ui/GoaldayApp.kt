package com.bf410.goaldaylocal.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bf410.goaldaylocal.ui.book.BookEntryMode
import com.bf410.goaldaylocal.ui.book.BookHomeScreen
import com.bf410.goaldaylocal.ui.book.BookViewModel
import com.bf410.goaldaylocal.ui.calendar.CalendarScreen
import com.bf410.goaldaylocal.ui.calendar.CalendarViewModel
import com.bf410.goaldaylocal.ui.home.HomeScreen
import com.bf410.goaldaylocal.ui.inspiration.InspirationScreen
import com.bf410.goaldaylocal.ui.replica.GoaldayDesign
import com.bf410.goaldaylocal.ui.settings.SettingsScreen
import com.bf410.goaldaylocal.START_TARGET_DIARY
import com.tencent.mmkv.MMKV

private val goaldayColorScheme = lightColorScheme(
    primary = GoaldayDesign.Pink,
    onPrimary = Color.White,
    primaryContainer = GoaldayDesign.PinkSoft,
    onPrimaryContainer = GoaldayDesign.InkPrimary,
    secondary = GoaldayDesign.RouteDiary,
    onSecondary = Color.White,
    surface = GoaldayDesign.Surface,
    onSurface = GoaldayDesign.InkPrimary,
    surfaceVariant = GoaldayDesign.SurfaceSoft,
    onSurfaceVariant = GoaldayDesign.InkSecondary,
    background = GoaldayDesign.AppBg,
    onBackground = GoaldayDesign.InkPrimary,
    outline = GoaldayDesign.InkMuted,
)

private enum class RootTab(val label: String, val iconText: String) {
    BOOK("手账", "账"),
    CALENDAR("日历", "历"),
    SETTINGS("设置", "设"),
}

private enum class BookRootSurface {
    HOME,
    BOOK,
    INSPIRATION,
}

private data class AppRoute(
    val tab: RootTab,
    val bookSurface: BookRootSurface,
    val bookEntryMode: BookEntryMode,
)

@Composable
fun GoaldayApp(startTarget: String? = null) {
    var tab by rememberSaveable(startTarget) {
        mutableStateOf(RootTab.BOOK)
    }
    var bookSurface by rememberSaveable(startTarget) {
        mutableStateOf(if (startTarget == START_TARGET_DIARY) BookRootSurface.BOOK else BookRootSurface.HOME)
    }
    var bookEntryMode by rememberSaveable(startTarget) {
        mutableStateOf(if (startTarget == START_TARGET_DIARY) BookEntryMode.DIARY else BookEntryMode.PLANNER)
    }
    var calendarFocusDay by rememberSaveable { mutableStateOf<Int?>(null) }
    val bookViewModel: BookViewModel = viewModel(factory = BookViewModel.Factory)
    val calendarViewModel: CalendarViewModel = viewModel(factory = CalendarViewModel.Factory)
    val bookUiState by bookViewModel.uiState.collectAsState()
    val mmkv = remember { MMKV.defaultMMKV() }
    var showGuide by remember { mutableStateOf(!mmkv.decodeBool(KEY_GUIDE_SEEN, false)) }

    val canGoBackInsideApp = tab != RootTab.BOOK ||
        bookSurface != BookRootSurface.HOME ||
        bookSurface == BookRootSurface.BOOK && bookEntryMode == BookEntryMode.PLANNER && !bookUiState.inLibraryMode
    val allowEdgeBackSwipe = canGoBackInsideApp
    val density = LocalDensity.current
    val edgeWidthPx = with(density) { 28.dp.toPx() }
    val triggerDistancePx = with(density) { 72.dp.toPx() }
    var backSwipeStartX by remember { mutableFloatStateOf(0f) }
    var backSwipeTravel by remember { mutableFloatStateOf(0f) }

    fun closeGuide() {
        mmkv.encode(KEY_GUIDE_SEEN, true)
        showGuide = false
    }

    fun openGuideTarget(target: GuideTarget) {
        tab = RootTab.BOOK
        when (target) {
            GuideTarget.INSPIRATION -> {
                bookSurface = BookRootSurface.INSPIRATION
                bookEntryMode = BookEntryMode.PLANNER
            }
            GuideTarget.HANDBOOK -> {
                bookSurface = BookRootSurface.BOOK
                bookEntryMode = BookEntryMode.HANDBOOK
                bookViewModel.openBook(0)
            }
            GuideTarget.DIARY -> {
                bookSurface = BookRootSurface.BOOK
                bookEntryMode = BookEntryMode.DIARY
                bookViewModel.openBook(0)
            }
            GuideTarget.HOME -> {
                bookSurface = BookRootSurface.HOME
                bookEntryMode = BookEntryMode.PLANNER
            }
        }
    }

    fun navigateBackInsideApp() {
        when {
            tab == RootTab.BOOK &&
                bookSurface == BookRootSurface.BOOK &&
                bookEntryMode == BookEntryMode.PLANNER &&
                !bookUiState.inLibraryMode -> {
                bookViewModel.openLibrary()
            }
            tab == RootTab.BOOK && bookSurface != BookRootSurface.HOME -> {
                bookSurface = BookRootSurface.HOME
                bookEntryMode = BookEntryMode.PLANNER
            }
            tab != RootTab.BOOK -> {
                tab = RootTab.BOOK
                bookSurface = BookRootSurface.HOME
                bookEntryMode = BookEntryMode.PLANNER
            }
        }
    }

    BackHandler(enabled = canGoBackInsideApp) {
        navigateBackInsideApp()
    }

    LaunchedEffect(tab, bookSurface) {
        if (tab == RootTab.BOOK && bookSurface == BookRootSurface.BOOK) {
            bookViewModel.refreshSchedulePreview()
        }
    }

    MaterialTheme(colorScheme = goaldayColorScheme) {
        Scaffold(
            containerColor = GoaldayDesign.AppBg,
            bottomBar = {
                GoaldayBottomDock(
                    selectedTab = tab,
                    onSelect = { item ->
                        tab = item
                        when (item) {
                            RootTab.BOOK -> {
                                bookSurface = BookRootSurface.HOME
                                bookEntryMode = BookEntryMode.PLANNER
                            }
                            RootTab.CALENDAR, RootTab.SETTINGS -> Unit
                        }
                    },
                )
            },
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(GoaldayDesign.AppBg)
                    .padding(padding)
                    .pointerInput(allowEdgeBackSwipe, edgeWidthPx, triggerDistancePx) {
                        if (!allowEdgeBackSwipe) return@pointerInput
                        detectHorizontalDragGestures(
                            onDragStart = { start ->
                                backSwipeStartX = start.x
                                backSwipeTravel = 0f
                            },
                            onHorizontalDrag = { _, dragAmount ->
                                if (backSwipeStartX <= edgeWidthPx && dragAmount > 0f) {
                                    backSwipeTravel += dragAmount
                                }
                            },
                            onDragEnd = {
                                if (backSwipeStartX <= edgeWidthPx && backSwipeTravel >= triggerDistancePx) {
                                    navigateBackInsideApp()
                                }
                                backSwipeTravel = 0f
                                backSwipeStartX = 0f
                            },
                            onDragCancel = {
                                backSwipeTravel = 0f
                                backSwipeStartX = 0f
                            },
                        )
                    },
            ) {
                val route = AppRoute(tab, bookSurface, bookEntryMode)
                AnimatedContent(
                    targetState = route,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "root-tab-switch",
                ) { currentRoute ->
                    when (currentRoute.tab) {
                        RootTab.BOOK -> {
                            fun openBookHome() {
                                bookSurface = BookRootSurface.HOME
                                bookEntryMode = BookEntryMode.PLANNER
                                tab = RootTab.BOOK
                            }
                            fun openBookLibrary() {
                                bookViewModel.openLibrary()
                                bookEntryMode = BookEntryMode.PLANNER
                                bookSurface = BookRootSurface.BOOK
                                tab = RootTab.BOOK
                            }
                            fun openBookMode(mode: BookEntryMode) {
                                bookEntryMode = mode
                                bookSurface = BookRootSurface.BOOK
                                tab = RootTab.BOOK
                            }
                            fun openInspiration() {
                                bookSurface = BookRootSurface.INSPIRATION
                                tab = RootTab.BOOK
                            }
                            BookRootScaffold(
                                surface = currentRoute.bookSurface,
                                entryMode = currentRoute.bookEntryMode,
                                onOpenHome = { openBookHome() },
                                onOpenLibrary = { openBookLibrary() },
                                onOpenHandbook = { openBookMode(BookEntryMode.HANDBOOK) },
                                onOpenDiary = { openBookMode(BookEntryMode.DIARY) },
                                onOpenInspiration = { openInspiration() },
                            ) {
                                when (currentRoute.bookSurface) {
                                    BookRootSurface.HOME -> HomeScreen(
                                        calendarViewModel = calendarViewModel,
                                        onOpenCalendar = {
                                            calendarFocusDay = null
                                            tab = RootTab.CALENDAR
                                        },
                                        onOpenCalendarForDay = { day ->
                                            calendarFocusDay = day
                                            tab = RootTab.CALENDAR
                                        },
                                        onOpenBook = { openBookLibrary() },
                                        onOpenHandbook = { openBookMode(BookEntryMode.HANDBOOK) },
                                        onOpenDiary = { openBookMode(BookEntryMode.DIARY) },
                                        onOpenInspiration = { openInspiration() },
                                    )
                                    BookRootSurface.INSPIRATION -> InspirationScreen(
                                        viewModel = bookViewModel,
                                        onOpenHandbook = { openBookMode(BookEntryMode.HANDBOOK) },
                                    )
                                    BookRootSurface.BOOK -> BookHomeScreen(viewModel = bookViewModel, entryMode = currentRoute.bookEntryMode)
                                }
                            }
                        }
                        RootTab.CALENDAR -> CalendarScreen(
                            viewModel = calendarViewModel,
                            focusDay = calendarFocusDay,
                            onFocusConsumed = { calendarFocusDay = null },
                        )
                        RootTab.SETTINGS -> SettingsScreen(
                            onShowGuide = { showGuide = true },
                        )
                    }
                }
                if (showGuide) {
                    GuideOverlay(
                        onClose = ::closeGuide,
                        onOpenTarget = ::openGuideTarget,
                    )
                }
            }
        }
    }
}

@Composable
private fun BookRootScaffold(
    surface: BookRootSurface,
    entryMode: BookEntryMode,
    onOpenHome: () -> Unit,
    onOpenLibrary: () -> Unit,
    onOpenHandbook: () -> Unit,
    onOpenDiary: () -> Unit,
    onOpenInspiration: () -> Unit,
    content: @Composable () -> Unit,
) {
    val showRootHeader = surface != BookRootSurface.BOOK || entryMode == BookEntryMode.PLANNER
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GoaldayDesign.AppBg),
    ) {
        if (showRootHeader) {
            BookRootHeader(
                surface = surface,
                entryMode = entryMode,
                onOpenHome = onOpenHome,
                onOpenLibrary = onOpenLibrary,
                onOpenHandbook = onOpenHandbook,
                onOpenDiary = onOpenDiary,
                onOpenInspiration = onOpenInspiration,
            )
        }
        Box(modifier = Modifier.weight(1f)) {
            content()
        }
    }
}

@Composable
private fun BookRootHeader(
    surface: BookRootSurface,
    entryMode: BookEntryMode,
    onOpenHome: () -> Unit,
    onOpenLibrary: () -> Unit,
    onOpenHandbook: () -> Unit,
    onOpenDiary: () -> Unit,
    onOpenInspiration: () -> Unit,
) {
    val routeLabel = when {
        surface == BookRootSurface.HOME -> "fragment_schedule"
        surface == BookRootSurface.INSPIRATION -> "TopicCenter"
        entryMode == BookEntryMode.DIARY -> "DiaryActivity"
        entryMode == BookEntryMode.HANDBOOK -> "BookActivity"
        else -> "BookLibrary"
    }
    val subtitle = when {
        surface == BookRootSurface.HOME -> "今日计划、拖拽日程和桌面组件都在本地运行"
        surface == BookRootSurface.INSPIRATION -> "专题目标、导入任务和保存手账本"
        entryMode == BookEntryMode.DIARY -> "日记条目、图片、目标块和长图导出"
        entryMode == BookEntryMode.HANDBOOK -> "翻页手账、日程页、目标页和日记页"
        else -> "书库、本子封面和本地模板"
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(GoaldayDesign.Paper)
            .shadow(GoaldayDesign.ShadowSoft, RoundedCornerShape(bottomStart = GoaldayDesign.Radius2XL, bottomEnd = GoaldayDesign.Radius2XL))
            .border(GoaldayDesign.Hairline, GoaldayDesign.BorderColor.copy(alpha = GoaldayDesign.HairlineAlpha), RoundedCornerShape(bottomStart = GoaldayDesign.Radius2XL, bottomEnd = GoaldayDesign.Radius2XL))
            .padding(horizontal = GoaldayDesign.Space4, vertical = GoaldayDesign.Space3),
        verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space2),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(1.dp), modifier = Modifier.weight(1f)) {
                Text(
                    "Goalday 手账",
                    color = GoaldayDesign.InkPrimary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                )
                Text(
                    subtitle,
                    color = GoaldayDesign.InkMuted,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                )
            }
            Text(
                routeLabel,
                color = GoaldayDesign.Pink,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                modifier = Modifier
                    .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
                    .background(GoaldayDesign.PinkSoft)
                    .border(GoaldayDesign.Hairline, GoaldayDesign.Pink.copy(alpha = 0.22f), RoundedCornerShape(GoaldayDesign.RadiusPill))
                    .padding(horizontal = GoaldayDesign.Space2, vertical = GoaldayDesign.Space1),
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BookRootSegmentChip(
                label = "今日",
                code = "今",
                selected = surface == BookRootSurface.HOME,
                onClick = onOpenHome,
            )
            BookRootSegmentChip(
                label = "书库",
                code = "书",
                selected = surface == BookRootSurface.BOOK && entryMode == BookEntryMode.PLANNER,
                onClick = onOpenLibrary,
            )
            BookRootSegmentChip(
                label = "手账",
                code = "账",
                selected = surface == BookRootSurface.BOOK && entryMode == BookEntryMode.HANDBOOK,
                onClick = onOpenHandbook,
            )
            BookRootSegmentChip(
                label = "日记",
                code = "记",
                selected = surface == BookRootSurface.BOOK && entryMode == BookEntryMode.DIARY,
                onClick = onOpenDiary,
            )
            BookRootSegmentChip(
                label = "灵感",
                code = "灵",
                selected = surface == BookRootSurface.INSPIRATION,
                onClick = onOpenInspiration,
            )
        }
    }
}

@Composable
private fun BookRootSegmentChip(
    label: String,
    code: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .height(34.dp)
            .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
            .background(if (selected) GoaldayDesign.Pink else Color.White.copy(alpha = 0.78f))
            .border(
                width = GoaldayDesign.Hairline,
                color = if (selected) GoaldayDesign.Pink.copy(alpha = 0.38f) else GoaldayDesign.BorderColor.copy(alpha = 0.08f),
                shape = RoundedCornerShape(GoaldayDesign.RadiusPill),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = GoaldayDesign.Space2, vertical = GoaldayDesign.Space1),
        horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space1),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            code,
            color = if (selected) Color.White else GoaldayDesign.Pink,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
                .background(if (selected) Color.White.copy(alpha = 0.20f) else GoaldayDesign.PinkSoft)
                .padding(horizontal = GoaldayDesign.Space1 + 2.dp, vertical = 2.dp),
        )
        Text(
            label,
            color = if (selected) Color.White else GoaldayDesign.InkSecondary,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1,
        )
    }
}

@Composable
private fun GoaldayBottomDock(
    selectedTab: RootTab,
    onSelect: (RootTab) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = GoaldayDesign.ShadowMedium,
                shape = RoundedCornerShape(topStart = GoaldayDesign.Radius2XL, topEnd = GoaldayDesign.Radius2XL)
            )
            .clip(RoundedCornerShape(topStart = GoaldayDesign.Radius2XL, topEnd = GoaldayDesign.Radius2XL))
            .background(GoaldayDesign.Paper)
            .border(
                width = GoaldayDesign.Hairline,
                color = GoaldayDesign.BorderColor.copy(alpha = 0.08f),
                shape = RoundedCornerShape(topStart = GoaldayDesign.Radius2XL, topEnd = GoaldayDesign.Radius2XL)
            )
            .padding(horizontal = GoaldayDesign.Space3, vertical = GoaldayDesign.Space2),
        horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space2),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RootTab.entries.forEach { tab ->
            GoaldayBottomDockItem(
                tab = tab,
                selected = selectedTab == tab,
                onClick = { onSelect(tab) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun GoaldayBottomDockItem(
    tab: RootTab,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(GoaldayDesign.RadiusXL))
            .background(
                if (selected) GoaldayDesign.PinkSoft
                else Color.Transparent
            )
            .border(
                width = if (selected) GoaldayDesign.Hairline else 0.dp,
                color = if (selected) GoaldayDesign.Pink.copy(alpha = 0.30f) else Color.Transparent,
                shape = RoundedCornerShape(GoaldayDesign.RadiusXL)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = GoaldayDesign.Space1, vertical = GoaldayDesign.Space2),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = tab.iconText,
            color = if (selected) Color.White else GoaldayDesign.InkMuted,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier = Modifier
                .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
                .background(
                    if (selected) GoaldayDesign.Pink
                    else GoaldayDesign.BorderColor.copy(alpha = 0.06f)
                )
                .padding(horizontal = GoaldayDesign.Space2, vertical = 3.dp),
        )
        Text(
            text = tab.label,
            color = if (selected) GoaldayDesign.InkPrimary else GoaldayDesign.InkMuted,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1,
        )
    }
}
