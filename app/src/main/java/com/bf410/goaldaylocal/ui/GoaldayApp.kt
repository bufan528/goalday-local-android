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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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

    MaterialTheme {
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
                        onClose = {
                            mmkv.encode(KEY_GUIDE_SEEN, true)
                            showGuide = false
                        },
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
            .background(Color(0xFFFFFCF7))
            .border(0.7.dp, Color(0x18A88966), RoundedCornerShape(bottomStart = 22.dp, bottomEnd = 22.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
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
                    .clip(RoundedCornerShape(99.dp))
                    .background(Color(0xFFFFEDF4))
                    .border(0.6.dp, GoaldayDesign.Pink.copy(alpha = 0.22f), RoundedCornerShape(99.dp))
                    .padding(horizontal = 9.dp, vertical = 5.dp),
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
            .clip(RoundedCornerShape(99.dp))
            .background(if (selected) GoaldayDesign.Pink else Color.White.copy(alpha = 0.78f))
            .border(
                width = 0.7.dp,
                color = if (selected) GoaldayDesign.Pink.copy(alpha = 0.38f) else Color(0x16A88966),
                shape = RoundedCornerShape(99.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 9.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            code,
            color = if (selected) Color.White else GoaldayDesign.Pink,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .clip(RoundedCornerShape(99.dp))
                .background(if (selected) Color.White.copy(alpha = 0.20f) else GoaldayDesign.PinkSoft)
                .padding(horizontal = 6.dp, vertical = 2.dp),
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
            .background(Color(0xFFFFFCF7))
            .border(0.7.dp, Color(0x18A88966), RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RootTab.entries.forEach { item ->
            val selected = selectedTab == item
            GoaldayBottomDockItem(
                tab = item,
                selected = selected,
                modifier = Modifier.weight(1f),
                onClick = { onSelect(item) },
            )
        }
    }
}

@Composable
private fun GoaldayBottomDockItem(
    tab: RootTab,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .background(
                if (selected) GoaldayDesign.PinkSoft else Color.Transparent,
                RoundedCornerShape(16.dp),
            )
            .border(
                0.7.dp,
                if (selected) GoaldayDesign.Pink.copy(alpha = 0.26f) else Color.Transparent,
                RoundedCornerShape(16.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 5.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            tab.iconText,
            color = if (selected) GoaldayDesign.Pink else GoaldayDesign.InkMuted,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
        )
        Text(
            tab.label,
            color = if (selected) GoaldayDesign.InkPrimary else GoaldayDesign.InkMuted,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1,
        )
    }
}
