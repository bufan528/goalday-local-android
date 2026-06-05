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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.RoundedCornerShape
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
    HOME("今日", "今"),
    BOOK("手账", "账"),
    INSPIRATION("灵感", "灵"),
    CALENDAR("日历", "历"),
    SETTINGS("设置", "设"),
}

@Composable
fun GoaldayApp(startTarget: String? = null) {
    var tab by rememberSaveable(startTarget) {
        mutableStateOf(if (startTarget == START_TARGET_DIARY) RootTab.BOOK else RootTab.HOME)
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

    val canGoBackInsideApp = tab != RootTab.HOME || !bookUiState.inLibraryMode
    val allowEdgeBackSwipe = canGoBackInsideApp
    val density = LocalDensity.current
    val edgeWidthPx = with(density) { 28.dp.toPx() }
    val triggerDistancePx = with(density) { 72.dp.toPx() }
    var backSwipeStartX by remember { mutableFloatStateOf(0f) }
    var backSwipeTravel by remember { mutableFloatStateOf(0f) }

    fun navigateBackInsideApp() {
        when {
            tab != RootTab.HOME -> {
                tab = RootTab.HOME
                bookEntryMode = BookEntryMode.PLANNER
            }
            !bookUiState.inLibraryMode -> bookViewModel.openLibrary()
        }
    }

    BackHandler(enabled = canGoBackInsideApp) {
        navigateBackInsideApp()
    }

    LaunchedEffect(tab) {
        if (tab == RootTab.BOOK) bookViewModel.refreshSchedulePreview()
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
                            RootTab.BOOK -> bookEntryMode = BookEntryMode.PLANNER
                            RootTab.HOME, RootTab.INSPIRATION, RootTab.CALENDAR, RootTab.SETTINGS -> Unit
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
                AnimatedContent(
                    targetState = tab,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "root-tab-switch",
                ) { currentTab ->
                    when (currentTab) {
                        RootTab.HOME -> HomeScreen(
                            calendarViewModel = calendarViewModel,
                            onOpenCalendar = {
                                calendarFocusDay = null
                                tab = RootTab.CALENDAR
                            },
                            onOpenCalendarForDay = { day ->
                                calendarFocusDay = day
                                tab = RootTab.CALENDAR
                            },
                            onOpenHandbook = {
                                bookEntryMode = BookEntryMode.HANDBOOK
                                tab = RootTab.BOOK
                            },
                            onOpenDiary = {
                                bookEntryMode = BookEntryMode.DIARY
                                tab = RootTab.BOOK
                            },
                            onOpenInspiration = {
                                tab = RootTab.INSPIRATION
                            },
                        )
                        RootTab.INSPIRATION -> InspirationScreen(
                            viewModel = bookViewModel,
                            onOpenHandbook = {
                                bookEntryMode = BookEntryMode.HANDBOOK
                                tab = RootTab.BOOK
                            },
                        )
                        RootTab.CALENDAR -> CalendarScreen(
                            viewModel = calendarViewModel,
                            focusDay = calendarFocusDay,
                            onFocusConsumed = { calendarFocusDay = null },
                        )
                        RootTab.BOOK -> BookHomeScreen(viewModel = bookViewModel, entryMode = bookEntryMode)
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
