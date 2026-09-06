package com.bf410.goaldaylocal.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import com.bf410.goaldaylocal.ui.main.OriginalMainScreen
import com.bf410.goaldaylocal.ui.replica.GoaldayDesign
import com.bf410.goaldaylocal.ui.replica.LocalGoaldayDarkMode
import com.bf410.goaldaylocal.ui.settings.SettingsScreen
import com.bf410.goaldaylocal.START_TARGET_DIARY
import com.bf410.goaldaylocal.START_TARGET_HANDBOOK
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
    // MAIN = 原版主界面（周|记录|清单 + 底部三图标导航），对照 com.first.goalday 1:1
    MAIN("记录", Icons.Filled.Adjust),
    BOOK("手账", Icons.AutoMirrored.Filled.MenuBook),
    CALENDAR("日历", Icons.Filled.CalendarMonth),
    SETTINGS("设置", Icons.Filled.Settings),
}

private data class TabConfig(
    val tab: RootTab,
    val visible: Boolean = true,
)

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoaldayApp(startTarget: String? = null) {
    var tab by rememberSaveable(startTarget) {
        mutableStateOf(
            if (startTarget == START_TARGET_DIARY || startTarget == START_TARGET_HANDBOOK) {
                RootTab.BOOK
            } else {
                RootTab.MAIN
            }
        )
    }
    var bookSurface by rememberSaveable(startTarget) {
        mutableStateOf(
            if (startTarget == START_TARGET_DIARY || startTarget == START_TARGET_HANDBOOK) {
                BookRootSurface.BOOK
            } else {
                BookRootSurface.BOOK
            }
        )
    }
    var bookEntryMode by rememberSaveable(startTarget) {
        mutableStateOf(
            when (startTarget) {
                START_TARGET_DIARY -> BookEntryMode.DIARY
                START_TARGET_HANDBOOK -> BookEntryMode.HANDBOOK
                else -> BookEntryMode.HANDBOOK
            }
        )
    }
    var calendarFocusDay by rememberSaveable { mutableStateOf<Int?>(null) }
    // 主界面周选择器触发器：底部日历图标在 MAIN 下递增
    var mainWeekPickerTick by remember { mutableIntStateOf(0) }
    val bookViewModel: BookViewModel = viewModel(factory = BookViewModel.Factory)
    val calendarViewModel: CalendarViewModel = viewModel(factory = CalendarViewModel.Factory)
    val bookUiState by bookViewModel.uiState.collectAsState()
    val mmkv = remember { MMKV.defaultMMKV() }
    // Tab 管理：读取保存的顺序与显隐配置
    val savedTabOrder = remember { mmkv.decodeString(KEY_TAB_ORDER, null) }
    val savedTabVisible = remember { mmkv.decodeString(KEY_TAB_VISIBLE, null) }
    var tabConfigs by remember {
        val default = RootTab.entries.map { TabConfig(it, true) }
        val order = savedTabOrder?.split(",")?.mapNotNull { name ->
            RootTab.entries.find { it.name == name }
        }
        val visible = savedTabVisible?.split(",")?.map { it.toBooleanStrictOrNull() }
        val configs = if (order != null && order.size == RootTab.entries.size) {
            order.mapIndexed { index, tab ->
                TabConfig(tab, visible?.getOrNull(index) ?: true)
            }
        } else default
        mutableStateOf(configs)
    }
    LaunchedEffect(tabConfigs) {
        mmkv.encode(KEY_TAB_ORDER, tabConfigs.joinToString(",") { it.tab.name })
        mmkv.encode(KEY_TAB_VISIBLE, tabConfigs.joinToString(",") { it.visible.toString() })
    }
    var showTabManager by remember { mutableStateOf(false) }
    var darkModePref by remember { mutableStateOf(mmkv.decodeString("dark_mode", "AUTO") ?: "AUTO") }
    val systemDark = isSystemInDarkTheme()
    val isDark = when (darkModePref) {
        "DARK" -> true
        "LIGHT" -> false
        else -> systemDark
    }
    // 通过 deep-link 入口（handbook/diary）启动时直接跳过引导，方便验证手账页
    var showGuide by remember(startTarget) { mutableStateOf(startTarget == null && !mmkv.decodeBool(KEY_GUIDE_SEEN, false)) }

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
            topBar = {
                // 原版化导航：MAIN 自带 周|记录|清单 顶栏；其余界面保留旧顶部 Tab 作为次级导航
                val immersiveBook = tab == RootTab.BOOK && bookSurface == BookRootSurface.BOOK && bookEntryMode != BookEntryMode.PLANNER
                if (!immersiveBook && tab != RootTab.MAIN) {
                    val visibleTabs = tabConfigs
                        .filter { it.visible && it.tab != RootTab.MAIN }
                        .map { it.tab }
                    GoaldayTopTabBar(
                        selectedTab = tab,
                        tabs = visibleTabs,
                        onSelect = { item ->
                            if (tab != item) {
                                tab = item
                                when (item) {
                                    RootTab.BOOK -> {
                                        bookSurface = BookRootSurface.HOME
                                        bookEntryMode = BookEntryMode.PLANNER
                                    }
                                    RootTab.CALENDAR, RootTab.SETTINGS -> Unit
                                    RootTab.MAIN -> Unit
                                }
                            }
                        },
                        onManage = { showTabManager = true },
                    )
                }
            },
            bottomBar = {
                // 原版底部三图标导航：● 记录主页 / 日历 / 书本（沉浸式手账时隐藏）
                val immersiveBook = tab == RootTab.BOOK && bookSurface == BookRootSurface.BOOK && bookEntryMode != BookEntryMode.PLANNER
                if (!immersiveBook) {
                    GoaldayBottomNavOriginal(
                        selected = tab,
                        onSelect = { item ->
                            if (item == RootTab.CALENDAR && tab == RootTab.MAIN) {
                                // 原版行为：主界面点日历图标 = 弹出周选择器，不切页
                                mainWeekPickerTick++
                            } else {
                                tab = item
                                when (item) {
                                    RootTab.BOOK -> {
                                        bookSurface = BookRootSurface.BOOK
                                        bookEntryMode = BookEntryMode.HANDBOOK
                                    }
                                    else -> Unit
                                }
                            }
                        },
                    )
                }
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
                        RootTab.MAIN -> OriginalMainScreen(
                            bookViewModel = bookViewModel,
                            openWeekPickerTick = mainWeekPickerTick,
                            onOpenBook = {
                                tab = RootTab.BOOK
                                bookSurface = BookRootSurface.BOOK
                                bookEntryMode = BookEntryMode.HANDBOOK
                            },
                            onOpenInspiration = {
                                tab = RootTab.BOOK
                                bookSurface = BookRootSurface.INSPIRATION
                                bookEntryMode = BookEntryMode.PLANNER
                            },
                            onOpenSettings = { tab = RootTab.SETTINGS },
                        )
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
                if (showTabManager) {
                    TabManagementSheet(
                        configs = tabConfigs,
                        onConfigsChange = { tabConfigs = it },
                        onDismiss = { showTabManager = false },
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
                color = if (isDark) GoaldayDesign.DarkInkPrimary else GoaldayDesign.adaptiveInkPrimary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
            )
            Text(
                subtitle,
                color = if (isDark) GoaldayDesign.DarkInkSecondary else GoaldayDesign.adaptiveInkMuted,
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
                label = "首页",
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
                selected = (surface == BookRootSurface.BOOK && entryMode == BookEntryMode.HANDBOOK) || (surface == BookRootSurface.BOOK && entryMode == BookEntryMode.DIARY),
                onClick = onOpenHandbook,
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
            color = if (selected) Color.White else if (isDark) GoaldayDesign.DarkInkSecondary else GoaldayDesign.adaptiveInkSecondary,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1,
        )
    }
}

// 原版底部三图标导航（对照原版主界面：● 记录 / 日历 / 书本，bg #E5DAD4，高 56dp）
@Composable
private fun GoaldayBottomNavOriginal(
    selected: RootTab,
    onSelect: (RootTab) -> Unit,
) {
    val items = listOf(
        RootTab.MAIN to Icons.Filled.Adjust,
        RootTab.CALENDAR to Icons.Filled.CalendarMonth,
        RootTab.BOOK to Icons.AutoMirrored.Filled.MenuBook,
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (LocalGoaldayDarkMode.current) GoaldayDesign.DarkSurface else GoaldayDesign.TabBarBg)
            .navigationBarsPadding()
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items.forEach { (item, icon) ->
            val active = selected == item
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onSelect(item) },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = item.label,
                    tint = if (active) GoaldayDesign.adaptiveInkPrimary else GoaldayDesign.adaptiveInkMuted,
                    modifier = Modifier.size(26.dp),
                )
            }
        }
    }
}

// P1-1：顶部文字 Tab（对照原版 FlexibleTabLayout: minHeight=49dp, paddingBottom=5dp, 纯文字 18sp bold）
// 选中=#252525 加粗，未选中=#9E9E9E 常规，背景=#E5DAD4
@Composable
private fun GoaldayTopTabBar(
    selectedTab: RootTab,
    tabs: List<RootTab>,
    onSelect: (RootTab) -> Unit,
    onManage: () -> Unit,
) {
    val isDark = LocalGoaldayDarkMode.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(49.dp)
            .background(if (isDark) GoaldayDesign.DarkTabBarBg else GoaldayDesign.TabBarBg)
            .padding(bottom = 5.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = GoaldayDesign.Space3)
                .align(Alignment.Center),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            tabs.forEach { tab ->
                Text(
                    text = tab.label,
                    fontSize = 18.sp,
                    fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal,
                    color = if (selectedTab == tab) GoaldayDesign.adaptiveInkPrimary else GoaldayDesign.adaptiveInkMuted,
                    maxLines = 1,
                    modifier = Modifier.clickable { onSelect(tab) },
                )
            }
        }
        Text(
            text = "管理",
            fontSize = 12.sp,
            color = GoaldayDesign.adaptiveInkMuted,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = GoaldayDesign.Space2)
                .clickable { onManage() },
        )
    }
}

private const val KEY_TAB_ORDER = "tab_order_v1"
private const val KEY_TAB_VISIBLE = "tab_visible_v1"

/**
 * Tab 管理底部弹窗：支持拖拽排序与显隐切换。
 * 对齐原版首页顶部 Tab 长按/管理入口。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TabManagementSheet(
    configs: List<TabConfig>,
    onConfigsChange: (List<TabConfig>) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var localConfigs by remember(configs) { mutableStateOf(configs) }
    var draggedIndex by remember { mutableStateOf<Int?>(null) }
    var draggedOffset by remember { mutableStateOf(0f) }
    val itemHeightPx = with(LocalDensity.current) { 52.dp.toPx() }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = GoaldayDesign.Space3)
                .padding(bottom = GoaldayDesign.Space4),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "标签管理",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = GoaldayDesign.adaptiveInkPrimary,
                )
                TextButton(
                    onClick = {
                        onConfigsChange(localConfigs)
                        onDismiss()
                    },
                ) {
                    Text("完成", color = GoaldayDesign.PrimaryAction)
                }
            }
            Spacer(Modifier.height(GoaldayDesign.Space2))
            localConfigs.forEachIndexed { index, config ->
                val isDragging = draggedIndex == index
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .graphicsLayer {
                            translationY = if (isDragging) draggedOffset else 0f
                        }
                        .clip(RoundedCornerShape(GoaldayDesign.RadiusM))
                        .background(if (isDragging) GoaldayDesign.SurfaceSoft else GoaldayDesign.adaptiveSurface)
                        .border(
                            GoaldayDesign.Hairline,
                            GoaldayDesign.BorderColor.copy(alpha = 0.1f),
                            RoundedCornerShape(GoaldayDesign.RadiusM),
                        )
                        .padding(horizontal = GoaldayDesign.Space3),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space2),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Default.DragHandle,
                            contentDescription = "拖动排序",
                            tint = GoaldayDesign.adaptiveInkMuted,
                            modifier = Modifier
                                .size(24.dp)
                                .pointerInput(Unit) {
                                    detectDragGesturesAfterLongPress(
                                        onDragStart = { draggedIndex = index },
                                        onDragEnd = {
                                            draggedIndex = null
                                            draggedOffset = 0f
                                        },
                                        onDragCancel = {
                                            draggedIndex = null
                                            draggedOffset = 0f
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            draggedOffset += dragAmount.y
                                            val current = draggedIndex ?: return@detectDragGesturesAfterLongPress
                                            val moveItems = (draggedOffset / itemHeightPx).toInt()
                                            val target = (current + moveItems)
                                                .coerceIn(0, localConfigs.lastIndex)
                                            if (target != current) {
                                                localConfigs = localConfigs.toMutableList().apply {
                                                    val item = removeAt(current)
                                                    add(target, item)
                                                }
                                                draggedIndex = target
                                                draggedOffset -= moveItems * itemHeightPx
                                            }
                                        },
                                    )
                                },
                        )
                        Icon(config.tab.icon, contentDescription = null, tint = GoaldayDesign.adaptiveInkPrimary)
                        Text(
                            config.tab.label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = GoaldayDesign.adaptiveInkPrimary,
                        )
                    }
                    IconButton(
                        onClick = {
                            localConfigs = localConfigs.mapIndexed { i, c ->
                                if (i == index) c.copy(visible = !c.visible) else c
                            }
                        },
                    ) {
                        Icon(
                            imageVector = if (config.visible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (config.visible) "隐藏" else "显示",
                            tint = if (config.visible) GoaldayDesign.adaptiveInkPrimary else GoaldayDesign.adaptiveInkMuted,
                        )
                    }
                }
                if (index < localConfigs.lastIndex) {
                    Spacer(Modifier.height(GoaldayDesign.Space1))
                }
            }
        }
    }
}

