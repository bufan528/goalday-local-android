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
import com.bf410.goaldaylocal.ui.inspiration.InspirationScreen

private enum class RootTab(val label: String, val icon: String) {
    HOME("日程", "◍"),
    CALENDAR("日历", "◌"),
    INSPIRATION("灵感", "◎"),
    HANDBOOK("手账", "▦"),
}

@Composable
fun GoaldayApp() {
    var tab by rememberSaveable { mutableStateOf(RootTab.HOME) }
    val bookViewModel: BookViewModel = viewModel(factory = BookViewModel.Factory)
    val calendarViewModel: CalendarViewModel = viewModel(factory = CalendarViewModel.Factory)
    val bookUiState by bookViewModel.uiState.collectAsState()

    val canGoBackInsideApp = !bookUiState.inLibraryMode || tab != RootTab.HOME
    val density = LocalDensity.current
    val edgeWidthPx = with(density) { 28.dp.toPx() }
    val triggerDistancePx = with(density) { 72.dp.toPx() }
    var backSwipeStartX by remember { mutableFloatStateOf(0f) }
    var backSwipeTravel by remember { mutableFloatStateOf(0f) }

    fun navigateBackInsideApp() {
        when {
            !bookUiState.inLibraryMode -> bookViewModel.openLibrary()
            tab != RootTab.HOME -> tab = RootTab.HOME
        }
    }

    BackHandler(enabled = canGoBackInsideApp) {
        navigateBackInsideApp()
    }

    MaterialTheme {
        Scaffold(
            containerColor = Color(0xFFF7F6F3),
            bottomBar = {
                NavigationBar(
                    containerColor = Color(0xFFF4F1EB),
                    tonalElevation = 0.dp,
                ) {
                    RootTab.entries.forEach { item ->
                        val selected = tab == item
                        NavigationBarItem(
                            selected = selected,
                            onClick = { tab = item },
                            icon = { Text(item.icon) },
                            label = { Text(item.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF2F2A25),
                                selectedTextColor = Color(0xFF2F2A25),
                                unselectedIconColor = Color(0xFF9E978D),
                                unselectedTextColor = Color(0xFF9E978D),
                                indicatorColor = Color(0xFFEDE6DC),
                            ),
                        )
                    }
                }
            },
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF7F6F3))
                    .padding(padding)
                    .padding(horizontal = 10.dp)
                    .pointerInput(canGoBackInsideApp, edgeWidthPx, triggerDistancePx) {
                        if (!canGoBackInsideApp) return@pointerInput
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
                        RootTab.HOME -> BookHomeScreen(viewModel = bookViewModel, entryMode = BookEntryMode.PLANNER)
                        RootTab.CALENDAR -> CalendarScreen(viewModel = calendarViewModel)
                        RootTab.INSPIRATION -> InspirationScreen(viewModel = bookViewModel)
                        RootTab.HANDBOOK -> BookHomeScreen(viewModel = bookViewModel, entryMode = BookEntryMode.HANDBOOK)
                    }
                }
            }
        }
    }
}
