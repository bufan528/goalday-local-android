package com.bf410.goaldaylocal.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bf410.goaldaylocal.ui.book.BookEntryMode
import com.bf410.goaldaylocal.ui.book.BookHomeScreen
import com.bf410.goaldaylocal.ui.book.BookViewModel
import com.bf410.goaldaylocal.ui.calendar.CalendarScreen
import com.bf410.goaldaylocal.ui.calendar.CalendarViewModel
import com.bf410.goaldaylocal.ui.replica.GoaldayDesign
import com.bf410.goaldaylocal.ui.settings.SettingsScreen
import com.bf410.goaldaylocal.START_TARGET_DIARY
import com.tencent.mmkv.MMKV

private enum class RootTab(val label: String, val iconText: String) {
    BOOK("手账", "▣"),
    CALENDAR("日历", "□"),
    SETTINGS("设置", "○"),
}

@Composable
fun GoaldayApp(startTarget: String? = null) {
    var tab by rememberSaveable { mutableStateOf(RootTab.BOOK) }
    var bookEntryMode by rememberSaveable(startTarget) {
        mutableStateOf(if (startTarget == START_TARGET_DIARY) BookEntryMode.DIARY else BookEntryMode.PLANNER)
    }
    val bookViewModel: BookViewModel = viewModel(factory = BookViewModel.Factory)
    val calendarViewModel: CalendarViewModel = viewModel(factory = CalendarViewModel.Factory)
    val bookUiState by bookViewModel.uiState.collectAsState()
    val mmkv = remember { MMKV.defaultMMKV() }
    var showGuide by remember { mutableStateOf(!mmkv.decodeBool(KEY_GUIDE_SEEN, false)) }

    val canGoBackInsideApp = !bookUiState.inLibraryMode || tab != RootTab.BOOK
    val allowEdgeBackSwipe = canGoBackInsideApp
    val density = LocalDensity.current
    val edgeWidthPx = with(density) { 28.dp.toPx() }
    val triggerDistancePx = with(density) { 72.dp.toPx() }
    var backSwipeStartX by remember { mutableFloatStateOf(0f) }
    var backSwipeTravel by remember { mutableFloatStateOf(0f) }

    fun navigateBackInsideApp() {
        when {
            !bookUiState.inLibraryMode -> bookViewModel.openLibrary()
            tab != RootTab.BOOK -> tab = RootTab.BOOK
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
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 0.dp,
                ) {
                    RootTab.entries.forEach { item ->
                        val selected = tab == item
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                tab = item
                                if (item == RootTab.BOOK) {
                                    bookEntryMode = BookEntryMode.PLANNER
                                }
                            },
                            icon = { Text(item.iconText, color = if (selected) GoaldayDesign.Pink else Color(0xFF9E958A)) },
                            label = { Text(item.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = GoaldayDesign.Pink,
                                selectedTextColor = GoaldayDesign.Pink,
                                unselectedIconColor = Color(0xFF9E958A),
                                unselectedTextColor = Color(0xFF9E958A),
                                indicatorColor = GoaldayDesign.PinkSoft,
                            ),
                        )
                    }
                }
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
                        RootTab.CALENDAR -> CalendarScreen(
                            viewModel = calendarViewModel,
                            focusDay = null,
                            onFocusConsumed = {},
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
