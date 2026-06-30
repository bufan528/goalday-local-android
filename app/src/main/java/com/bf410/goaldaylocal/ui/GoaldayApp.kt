package com.bf410.goaldaylocal.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.Typography
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Today
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bf410.goaldaylocal.ui.book.BookEntryMode
import com.bf410.goaldaylocal.ui.book.BookHomeScreen
import com.bf410.goaldaylocal.ui.book.BookViewModel
import com.bf410.goaldaylocal.ui.calendar.CalendarScreen
import com.bf410.goaldaylocal.ui.calendar.CalendarViewModel
import com.bf410.goaldaylocal.ui.home.HomeScreen
import com.bf410.goaldaylocal.ui.inspiration.InspirationScreen
import com.bf410.goaldaylocal.ui.replica.GoaldayDesign
import com.bf410.goaldaylocal.ui.replica.LocalGoaldayDarkMode
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

private val goaldayDarkColorScheme = darkColorScheme(
    primary = GoaldayDesign.Pink,
    onPrimary = Color.White,
    primaryContainer = GoaldayDesign.PinkSoft,
    onPrimaryContainer = GoaldayDesign.DarkInkPrimary,
    secondary = GoaldayDesign.RouteDiary,
    onSecondary = Color.White,
    surface = GoaldayDesign.DarkSurface,
    onSurface = GoaldayDesign.DarkInkPrimary,
    surfaceVariant = GoaldayDesign.DarkSurfaceSoft,
    onSurfaceVariant = GoaldayDesign.DarkInkSecondary,
    background = GoaldayDesign.DarkAppBg,
    onBackground = GoaldayDesign.DarkInkPrimary,
    outline = GoaldayDesign.DarkInkMuted,
)

private enum class RootTab(val label: String, val icon: ImageVector) {
    BOOK("手账", Icons.AutoMirrored.Filled.MenuBook),
    CALENDAR("日历", Icons.Filled.CalendarMonth),
    SETTINGS("设置", Icons.Filled.Settings),
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
    var darkModePref by remember { mutableStateOf(mmkv.decodeString("dark_mode", "AUTO") ?: "AUTO") }
    val systemDark = isSystemInDarkTheme()
    val isDark = when (darkModePref) {
        "DARK" -> true
        "LIGHT" -> false
        else -> systemDark
    }
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

    val goaldayMmkv = remember { MMKV.defaultMMKV() }
    var fontSizeKey by remember { mutableStateOf(goaldayMmkv.decodeString("settings_font_size", "standard") ?: "standard") }

    val goaldayTypography = remember(fontSizeKey) {
        val scale = when (fontSizeKey) {
            "compact" -> 0.88f
            "large" -> 1.12f
            else -> 1.0f
        }
        Typography().run {
            copy(
                titleLarge = titleLarge.copy(
                    fontFamily = GoaldayDesign.DisplayFontFamily,
                    fontSize = (24 * scale).sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = (32 * scale).sp,
                ),
                titleMedium = titleMedium.copy(
                    fontSize = (17 * scale).sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = (24 * scale).sp,
                ),
                bodyLarge = bodyLarge.copy(
                    fontSize = (16 * scale).sp,
                    lineHeight = (24 * scale).sp,
                ),
                bodyMedium = bodyMedium.copy(
                    fontSize = (14 * scale).sp,
                    lineHeight = (20 * scale).sp,
                ),
                bodySmall = bodySmall.copy(fontSize = (12 * scale).sp),
                labelLarge = labelLarge.copy(fontSize = (14 * scale).sp),
                labelMedium = labelMedium.copy(fontSize = (12 * scale).sp),
                labelSmall = labelSmall.copy(fontSize = (11 * scale).sp),
            )
        }
    }

    MaterialTheme(colorScheme = if (isDark) goaldayDarkColorScheme else goaldayColorScheme, typography = goaldayTypography) {
        CompositionLocalProvider(LocalGoaldayDarkMode provides isDark) {
        Scaffold(
            containerColor = if (isDark) GoaldayDesign.DarkAppBg else GoaldayDesign.AppBg,
            bottomBar = {
                GoaldayBottomDock(
                    selectedTab = tab,
                    onSelect = { item ->
                        if (tab != item) {
                            tab = item
                            when (item) {
                                RootTab.BOOK -> {
                                    bookSurface = BookRootSurface.HOME
                                    bookEntryMode = BookEntryMode.PLANNER
                                }
                                RootTab.CALENDAR, RootTab.SETTINGS -> Unit
                            }
                        }
                    },
                )
            },
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(if (isDark) GoaldayDesign.DarkAppBg else GoaldayDesign.AppBg)
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
                                    BookRootSurface.BOOK -> BookHomeScreen(viewModel = bookViewModel, entryMode = currentRoute.bookEntryMode, onBack = { navigateBackInsideApp() })
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
                            onFontSizeChange = { fontSizeKey = it },
                            onDarkModeChange = { darkModePref = it },
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
    val isDark = LocalGoaldayDarkMode.current
    val showRootHeader = surface != BookRootSurface.BOOK || entryMode == BookEntryMode.PLANNER
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) GoaldayDesign.DarkAppBg else GoaldayDesign.AppBg),
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
    val subtitle = when {
        surface == BookRootSurface.HOME -> "今日计划、拖拽日程和桌面组件都在本地运行"
        surface == BookRootSurface.INSPIRATION -> "专题目标、导入任务和保存手账本"
        entryMode == BookEntryMode.DIARY -> "日记条目、图片、目标块和长图导出"
        entryMode == BookEntryMode.HANDBOOK -> "翻页手账、日程页、目标页和日记页"
        else -> "书库、本子封面和本地模板"
    }
    val isDark = LocalGoaldayDarkMode.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isDark) GoaldayDesign.DarkSurface else GoaldayDesign.Paper)
            .shadow(GoaldayDesign.ShadowSoft, RoundedCornerShape(bottomStart = GoaldayDesign.Radius2XL, bottomEnd = GoaldayDesign.Radius2XL))
            .border(GoaldayDesign.Hairline, GoaldayDesign.BorderColor.copy(alpha = GoaldayDesign.HairlineAlpha), RoundedCornerShape(bottomStart = GoaldayDesign.Radius2XL, bottomEnd = GoaldayDesign.Radius2XL))
            .padding(horizontal = GoaldayDesign.Space4, vertical = GoaldayDesign.Space3),
        verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space2),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(1.dp), modifier = Modifier.fillMaxWidth()) {
            Text(
                "Goalday 手账",
                color = if (isDark) GoaldayDesign.DarkInkPrimary else GoaldayDesign.InkPrimary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
            )
            Text(
                subtitle,
                color = if (isDark) GoaldayDesign.DarkInkSecondary else GoaldayDesign.InkMuted,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
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
                icon = Icons.Filled.Today,
                selected = surface == BookRootSurface.HOME,
                onClick = onOpenHome,
            )
            BookRootSegmentChip(
                label = "书库",
                icon = Icons.AutoMirrored.Filled.LibraryBooks,
                selected = surface == BookRootSurface.BOOK && entryMode == BookEntryMode.PLANNER,
                onClick = onOpenLibrary,
            )
            BookRootSegmentChip(
                label = "手账",
                icon = Icons.AutoMirrored.Filled.MenuBook,
                selected = surface == BookRootSurface.BOOK && entryMode == BookEntryMode.HANDBOOK,
                onClick = onOpenHandbook,
            )
            BookRootSegmentChip(
                label = "日记",
                icon = Icons.Filled.EditNote,
                selected = surface == BookRootSurface.BOOK && entryMode == BookEntryMode.DIARY,
                onClick = onOpenDiary,
            )
            BookRootSegmentChip(
                label = "灵感",
                icon = Icons.Filled.Lightbulb,
                selected = surface == BookRootSurface.INSPIRATION,
                onClick = onOpenInspiration,
            )
        }
    }
}

@Composable
private fun BookRootSegmentChip(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val isDark = LocalGoaldayDarkMode.current
    Row(
        modifier = Modifier
            .height(34.dp)
            .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
            .background(if (selected) GoaldayDesign.Pink else if (isDark) GoaldayDesign.DarkSurfaceSoft else Color.White.copy(alpha = 0.78f))
            .border(
                width = GoaldayDesign.Hairline,
                color = if (selected) GoaldayDesign.Pink.copy(alpha = 0.38f) else GoaldayDesign.BorderColor.copy(alpha = 0.08f),
                shape = RoundedCornerShape(GoaldayDesign.RadiusPill),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = GoaldayDesign.Space3, vertical = GoaldayDesign.Space1),
        horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space1),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (selected) Color.White else GoaldayDesign.Pink,
            modifier = Modifier.size(16.dp),
        )
        Text(
            label,
            color = if (selected) Color.White else if (isDark) GoaldayDesign.DarkInkSecondary else GoaldayDesign.InkSecondary,
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
    val isDark = LocalGoaldayDarkMode.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = GoaldayDesign.ShadowMedium,
                shape = RoundedCornerShape(topStart = GoaldayDesign.Radius2XL, topEnd = GoaldayDesign.Radius2XL)
            )
            .clip(RoundedCornerShape(topStart = GoaldayDesign.Radius2XL, topEnd = GoaldayDesign.Radius2XL))
            .background(if (isDark) GoaldayDesign.DarkSurface else GoaldayDesign.Paper)
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
    val isDark = LocalGoaldayDarkMode.current
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
        Icon(
            imageVector = tab.icon,
            contentDescription = tab.label,
            tint = if (selected) GoaldayDesign.Pink else if (isDark) GoaldayDesign.DarkInkSecondary else GoaldayDesign.InkMuted,
            modifier = Modifier.size(22.dp),
        )
        Text(
            text = tab.label,
            color = if (selected) (if (isDark) GoaldayDesign.DarkInkPrimary else GoaldayDesign.InkPrimary) else if (isDark) GoaldayDesign.DarkInkSecondary else GoaldayDesign.InkMuted,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1,
        )
    }
}
