package com.bf410.goaldaylocal.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.activity.compose.BackHandler
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bf410.goaldaylocal.ui.book.BookStrings
import com.bf410.goaldaylocal.ui.book.BookHomeScreen
import com.bf410.goaldaylocal.ui.book.BookViewModel
import com.bf410.goaldaylocal.ui.calendar.CalendarScreen
import com.bf410.goaldaylocal.ui.calendar.CalendarViewModel
import com.bf410.goaldaylocal.ui.settings.SettingsScreen

private enum class RootTab(val label: String) {
    BOOK(BookStrings.tabBook),
    CALENDAR(BookStrings.tabCalendar),
    SETTINGS(BookStrings.tabSettings),
}

@Composable
fun GoaldayApp() {
    var tab by rememberSaveable { mutableStateOf(RootTab.BOOK) }
    val bookViewModel: BookViewModel = viewModel(factory = BookViewModel.Factory)
    val calendarViewModel: CalendarViewModel = viewModel(factory = CalendarViewModel.Factory)
    val bookUiState by bookViewModel.uiState.collectAsState()

    val canGoBackInsideApp = !bookUiState.inLibraryMode || tab != RootTab.BOOK
    BackHandler(enabled = canGoBackInsideApp) {
        when {
            !bookUiState.inLibraryMode -> bookViewModel.openLibrary()
            tab != RootTab.BOOK -> tab = RootTab.BOOK
        }
    }

    MaterialTheme {
        Scaffold(
            bottomBar = {
                NavigationBar {
                    RootTab.entries.forEach { item ->
                        NavigationBarItem(
                            selected = tab == item,
                            onClick = { tab = item },
                            icon = {},
                            label = { Text(item.label) },
                        )
                    }
                }
            },
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFFF8F4EE), Color(0xFFEDE6D9)),
                        ),
                    )
                    .padding(padding)
                    .padding(horizontal = 14.dp),
            ) {
                when (tab) {
                    RootTab.BOOK -> BookHomeScreen(viewModel = bookViewModel)
                    RootTab.CALENDAR -> CalendarScreen(viewModel = calendarViewModel)
                    RootTab.SETTINGS -> SettingsScreen()
                }
            }
        }
    }
}
