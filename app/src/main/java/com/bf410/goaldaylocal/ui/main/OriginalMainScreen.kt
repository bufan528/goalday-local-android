package com.bf410.goaldaylocal.ui.main

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.isUnspecified
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bf410.goaldaylocal.ui.InteractionFeedback
import com.bf410.goaldaylocal.ui.KeepImmersiveInDialog
import com.bf410.goaldaylocal.ui.KeepImmersiveInPopup
import com.bf410.goaldaylocal.data.LocalStateStore
import com.bf410.goaldaylocal.data.ScheduleEntry
import com.bf410.goaldaylocal.data.TargetPage
import com.bf410.goaldaylocal.data.TopicBook
import com.bf410.goaldaylocal.ui.book.BookUiState
import com.bf410.goaldaylocal.ui.book.BookViewModel
import com.bf410.goaldaylocal.ui.calendar.dayEntryTimeRank
import com.bf410.goaldaylocal.ui.book.diaryPromptOffsetKey
import com.bf410.goaldaylocal.ui.book.journalPromptFor
import com.bf410.goaldaylocal.ui.book.randomPromptOffset
import com.bf410.goaldaylocal.ui.replica.GoaldayDesign
import com.bf410.goaldaylocal.ui.replica.LocalGoaldayDarkMode
import com.tencent.mmkv.MMKV
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.WeekFields
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.runtime.saveable.Saver
import java.util.Locale

/** 跨午夜自动刷新的"今天"（对照原版行为：日期过期后界面自动切到新的一天，无需重启进程） */
@Composable
private fun rememberToday(): LocalDate {
    var today by remember { mutableStateOf(LocalDate.now()) }
    LaunchedEffect(today) {
        val now = java.time.LocalDateTime.now()
        val next = now.toLocalDate().plusDays(1).atStartOfDay()
        kotlinx.coroutines.delay(java.time.Duration.between(now, next).toMillis() + 300)
        today = LocalDate.now()
    }
    return today
}

/**
 * 原版主界面 1:1 复刻（对照原版真机截图与布局）。
 *
 * 信息架构：
 * - 顶部文字 Tab："N周 ▼"(周选择器，弹周历) | "记录"(选中时显示"M月D日") | "清单"，bg #E5DAD4
 *   行内编辑时顶栏变为右上角黑色「完成」按钮（对照原版截图）
 * - 周 Tab：左侧周日期列（今日黑底圆角高亮），任意一天点空白即可行内新增条目；
 *   条目 = 圆形勾选框(○/黑底白勾☑) + 文字（完成加删除线）；右侧任务池
 * - 记录 Tab：一日一问提示语 + 日记编辑（非编辑态无底栏，对照原版）；
 *   内容以结构化格式存储，自动把当日已完成日程写入「今日完成」供书内渲染卡片
 * - 清单 Tab：专题卡片（色块 + 名称 + 进度 x/y）+ FAB（+ 与 💡 只在本 Tab，对照原版）
 */
// 深色自适应：主界面所有颜色接 LocalGoaldayDarkMode（修复硬编码浅色）
private val MainTabBarBg: Color @Composable get() =
    if (LocalGoaldayDarkMode.current) Color(0xFF35312B) else Color(0xFFE5DAD4)
private val MainContentBg: Color @Composable get() =
    if (LocalGoaldayDarkMode.current) Color(0xFF221E1A) else Color(0xFFFDFAF6)
private val MainTabDivider: Color @Composable get() =
    if (LocalGoaldayDarkMode.current) Color(0xFF4A443D) else Color(0xFFC5BBB6)
private val WeekBandBg: Color @Composable get() =
    if (LocalGoaldayDarkMode.current) Color(0xFF3A2C26) else Color(0xFFFEECEC)
private val TodayCoral = Color(0xFFF66061)
private val TodayBlack: Color @Composable get() =
    if (LocalGoaldayDarkMode.current) Color(0xFFF66061) else Color(0xFF1E1E1E)
// 对照原版周视图右侧任务池：统一粉橙小方块（主品牌色 #F79941）
private val PoolBullet = Color(0xFFF79941)
private val EntryCircle: Color @Composable get() =
    if (LocalGoaldayDarkMode.current) Color(0xFFB9B1A7) else Color(0xFF3A3A3A)
private val RowCardBg: Color @Composable get() =
    if (LocalGoaldayDarkMode.current) Color(0xFF2C2722) else Color(0xFFFBF7F1)
private val FabLight: Color @Composable get() =
    if (LocalGoaldayDarkMode.current) Color(0xFF4A443C) else Color(0xFFEFE7DC)

internal enum class MainSubTab(val label: String) {
    WEEK("周"),
    MONTH("月"),
    RECORD("记录"),
    LIST("清单"),
}

private const val DIARY_BOOK_ID = "diary"

/**
 * 跨界面导航桥：书内点页 → 跳回主界面并选中对应日期/Tab
 * （对照原版 EventBus ScheduleDateSelectedEvent/DiaryDateSelectedEvent + onFinish）
 */
object MainUiBridge {
    internal var tick by androidx.compose.runtime.mutableIntStateOf(0)
    internal var date: LocalDate? = null
    internal var targetTab: MainSubTab? = null
    internal var directEdit: Boolean = false

    internal fun go(date: LocalDate, tab: MainSubTab, directEdit: Boolean = false) {
        this.date = date
        this.targetTab = tab
        this.directEdit = directEdit
        tick++
    }

    fun consume() {
        date = null
        targetTab = null
        directEdit = false
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun OriginalMainScreen(
    bookViewModel: BookViewModel,
    openWeekPickerTick: Int = 0,
    onOpenBook: () -> Unit,
    onOpenInspiration: () -> Unit,
    onOpenSettings: () -> Unit,
    onListDetailExpandedChange: (Boolean) -> Unit = {},
) {
    val uiState by bookViewModel.uiState.collectAsState()
    var subTabIndex by rememberSaveable { mutableIntStateOf(MainSubTab.WEEK.ordinal) }
    // 程序化导航目标（bridge/点选）：飞行中落定收集器见到旧落定页不回跳，到达即清
    var pagerTargetTab by remember { mutableStateOf<MainSubTab?>(null) }
    // LocalDate 默认 saver 旋转重建会闪退：经 ISO 字符串持久化
    var selectedDate by rememberSaveable(
        stateSaver = Saver(save = { it.toString() }, restore = { LocalDate.parse(it) }),
    ) { mutableStateOf(LocalDate.now()) }
    var showWeekPicker by remember { mutableStateOf(false) }
    // 周视图模式（对照原版 MainViewModel.isCurrentScheduleViewExpanded：再点周Tab在固定2×3与自适应流式间切换）
    var scheduleAdaptive by rememberSaveable { mutableStateOf(false) }
    // 行内编辑中的日期（任意一天可编辑，对照原版）；非 null 时顶栏显示「完成」
    var editingDate by remember { mutableStateOf<LocalDate?>(null) }
    // 清单详情（提升到主界面层以便系统返回拦截）
    var expandedBookId by rememberSaveable { mutableStateOf<String?>(null) }
    // 改期作用域确认（对照原版 pop_repeat：命中重复序列先选仅此/未来）
    var repeatScopeRequest by remember { mutableStateOf<BookViewModel.RepeatScopeRequest?>(null) }
    fun executeRepeatScope(request: BookViewModel.RepeatScopeRequest, scope: BookViewModel.RepeatScope) {
        if (request.isMove) {
            val id = request.targetIds.firstOrNull()
            if (id != null) bookViewModel.moveScheduleDayWithScope(id, request.moveYear, request.moveMonth, request.moveDay, scope)
        } else {
            bookViewModel.deleteScheduleWithScope(request.targetIds, scope)
        }
        repeatScopeRequest = null
    }
    // 日程条目编辑弹层（点/长按条目唤出：编辑标题/时间/移动/删除，对照原版 item_target_detail）
    var editingEntry by remember { mutableStateOf<ScheduleEntry?>(null) }
    // Tab 显隐（对照原版长按顶栏的 dialog_tab_manage，月默认隐藏）
    val mmkvStore = remember { MMKV.defaultMMKV() }
    var tabVisibility by remember {
        mutableStateOf(
            MainSubTab.entries.associateWith {
                mmkvStore.decodeBool("main_tab_visible_" + it.name, it != MainSubTab.MONTH)
            },
        )
    }
    // Tab 顺序（对照原版 dialog_tab_manage「按住拖动调整页面顺序」）
    var tabOrder by remember {
        val parsed = mmkvStore.decodeString("main_tab_order", null)
            ?.split(",")?.mapNotNull { name -> MainSubTab.entries.firstOrNull { it.name == name.trim() } }
        val valid = parsed
            ?.takeIf { it.size == MainSubTab.entries.size && it.distinct().size == it.size }
            ?: MainSubTab.entries.toList()
        mutableStateOf(valid)
    }
    var showTabManage by remember { mutableStateOf(false) }
    var currentSubTab = MainSubTab.entries[subTabIndex.coerceIn(0, MainSubTab.entries.lastIndex)]
    if (tabVisibility[currentSubTab] == false) {
        // 回退到第一个可见 Tab（当前 Tab 被隐藏时，按用户自定顺序）
        val firstVisible = tabOrder.firstOrNull { tabVisibility[it] == true } ?: MainSubTab.WEEK
        currentSubTab = firstVisible
        subTabIndex = firstVisible.ordinal
    }
    // 可见 Tab 页（对照原版 ViewPager2 页组；顺序/显隐可配；前移以便 bridge 导航直接驱 pager）
    val visibleTabs = remember(tabOrder, tabVisibility) {
        tabOrder.filter { tabVisibility[it] == true }.ifEmpty { listOf(MainSubTab.WEEK) }
    }
    var poolDragging by remember { mutableStateOf(false) }
    // 注意：initialPage 只在首次组合生效；bridge 跨 surface 跳转靠下面 effect 滑过去
    val mainPagerState = androidx.compose.foundation.pager.rememberPagerState(
        initialPage = visibleTabs.indexOf(currentSubTab).coerceAtLeast(0),
        pageCount = { visibleTabs.size },
    )

    // 系统返回：清单详情/行内编辑/条目编辑/作用域框优先返回上一级，其余交给应用级返回
    androidx.activity.compose.BackHandler(
        enabled = editingDate != null || expandedBookId != null || editingEntry != null ||
            repeatScopeRequest != null,
    ) {
        when {
            repeatScopeRequest != null -> repeatScopeRequest = null
            editingEntry != null -> editingEntry = null
            editingDate != null -> editingDate = null
            else -> expandedBookId = null
        }
    }

    LaunchedEffect(Unit) { bookViewModel.refreshSchedulePreview() }
    LaunchedEffect(openWeekPickerTick) {
        if (openWeekPickerTick > 0) showWeekPicker = true
    }
    // 书内点页 → 跳到对应日期与 Tab（对照原版 EventBus 行为）；书内日记直达编辑态（无提问、有输入 hint）
    var diaryDirectEdit by remember { mutableStateOf(false) }
    LaunchedEffect(MainUiBridge.tick) {
        val target = MainUiBridge.date
        val tab = MainUiBridge.targetTab
        if (target != null && tab != null) {
            // bridge 导航：先把页滑到位再切选中——落定收集器全程只看到一致态，
            // 不存在“已切选中但页未到”的回跳窗口；取消（离页）则不消费，回来重播。
            selectedDate = target
            diaryDirectEdit = MainUiBridge.directEdit
            pagerTargetTab = tab
            val idx = visibleTabs.indexOf(tab).coerceAtLeast(0)
            // 隐藏当前 Tab 后目标索引可能越界：失败不消费，回来重播
            val navigated = if (idx != mainPagerState.currentPage) {
                runCatching {
                    mainPagerState.animateScrollToPage(idx, animationSpec = tween(durationMillis = 300))
                }.isSuccess
            } else {
                true
            }
            subTabIndex = tab.ordinal
            if (navigated) MainUiBridge.consume()
        }
    }

    // 对照原版清单展开态（o_listexp）：详情是全屏页，不带主 Tab 栏；
    // 展开时藏起顶部 Tab 栏，详情自带顶栏（TopicDetailSimple）。
    val listDetailExpanded = currentSubTab == MainSubTab.LIST && expandedBookId != null
    // 清单详情全屏时通知应用层藏起底部导航（对照原版详情页无底栏）
    LaunchedEffect(listDetailExpanded) { onListDetailExpandedChange(listDetailExpanded) }

    // 对照原版全屏沉浸：系统栏由 MainActivity 统一隐藏（滑边临时唤出），此处不再染色状态栏。

    // 点选 → 滑到对应页（对照原版 ViewPager2 smoothScroll，有滑感而非瞬切）
    LaunchedEffect(subTabIndex, visibleTabs) {
        val target = visibleTabs.indexOf(currentSubTab).coerceAtLeast(0)
        if (target != mainPagerState.currentPage && visibleTabs.getOrNull(target) == currentSubTab) {
            pagerTargetTab = currentSubTab
            runCatching {
                mainPagerState.animateScrollToPage(
                    target,
                    animationSpec = tween(durationMillis = 300),
                )
            }
        } else {
            pagerTargetTab = null
        }
    }
    // 横滑落定 → 切换选中（同点选收尾：退行内编辑、退出直编态）
    // key 带 visibleTabs：开关 Tab 后映射会变，不带会用旧表把落定页判到错 Tab
    LaunchedEffect(mainPagerState, visibleTabs) {
            snapshotFlow { mainPagerState.settledPage }.collect { settled ->
            val tab = visibleTabs.getOrNull(settled) ?: return@collect
            if (tab.ordinal != subTabIndex) {
                // 非静息态一律不回跳：bridge 飞行守卫 / 正在滚动 / 当前页与落定页不一致
                // （否则 fresh composition 初次落定 emission 会把 bridge 刚设好的 Tab 打回旧页）
                if (pagerTargetTab != null || mainPagerState.isScrollInProgress ||
                    mainPagerState.currentPage != settled
                ) {
                    return@collect
                }
                editingDate = null
                diaryDirectEdit = false
                subTabIndex = tab.ordinal
            } else {
                pagerTargetTab = null
            }
        }
    }

    Column(Modifier.fillMaxSize().background(MainContentBg)) {
        if (!listDetailExpanded) {
            OriginalTopTabBar(
                selected = currentSubTab,
                selectedDate = selectedDate,
                editing = editingDate != null,
                onDone = { editingDate = null },
                onWeekClick = { scheduleAdaptive = !scheduleAdaptive },
                weekAdaptive = scheduleAdaptive,
                onSelect = {
                    editingDate = null
                    diaryDirectEdit = false
                    subTabIndex = it.ordinal
                },
                onManageTabs = { showTabManage = true },
                onOpenSettings = onOpenSettings,
                tabVisibility = tabVisibility,
                tabOrder = tabOrder,
            )
        }
        // 主子 Tab 横滑切换（对照原版 ViewPager2；池拖拽时禁滑）
        androidx.compose.foundation.pager.HorizontalPager(
            state = mainPagerState,
            userScrollEnabled = !poolDragging,
            modifier = Modifier.fillMaxSize().clipToBounds(),
            key = { visibleTabs[it] },
        ) { pageIndex ->
            when (val pageTab = visibleTabs.getOrElse(pageIndex) { MainSubTab.WEEK }) {
                MainSubTab.WEEK -> WeekScheduleView(
                    uiState = uiState,
                    viewModel = bookViewModel,
                    selectedDate = selectedDate,
                    editingDate = editingDate,
                    adaptiveMode = scheduleAdaptive,
                    onStartEdit = { editingDate = it },
                    onFinishEdit = { editingDate = null },
                    onSelectDate = { selectedDate = it },
                    onEditEntry = { editingEntry = it },
                    onPoolDragging = { poolDragging = it },
                    onExpandAll = { scheduleAdaptive = true },
                )
                MainSubTab.MONTH -> MonthScheduleView(
                    uiState = uiState,
                    viewModel = bookViewModel,
                    selectedDate = selectedDate,
                    onPickDay = { picked ->
                        selectedDate = picked
                        editingDate = null
                        subTabIndex = MainSubTab.WEEK.ordinal
                    },
                    onEditEntry = { editingEntry = it },
                )
                MainSubTab.RECORD -> RecordDiaryPager(
                    selectedDate = selectedDate,
                    entries = uiState.schedulePreviewEntries,
                    onSelectDate = { selectedDate = it },
                    directEdit = diaryDirectEdit,
                )
                MainSubTab.LIST -> TopicListView(
                    uiState = uiState,
                    viewModel = bookViewModel,
                    expandedBookId = expandedBookId,
                    onExpandBook = { expandedBookId = it },
                    onOpenBookShelf = onOpenBook,
                    onOpenInspiration = onOpenInspiration,
                    onOpenSettings = onOpenSettings,
                )
            }
        }
    }

    if (showTabManage) {
        TabManageSheet(
            visibility = tabVisibility,
            order = tabOrder,
            onToggle = { tab, visible ->
                // 至少保留一个可见 Tab
                if (visible || tabVisibility.count { it.value } > 1) {
                    tabVisibility = tabVisibility.toMutableMap().apply { put(tab, visible) }
                    mmkvStore.encode("main_tab_visible_" + tab.name, visible)
                }
            },
            onReorder = { newOrder ->
                tabOrder = newOrder
                mmkvStore.encode("main_tab_order", newOrder.joinToString(",") { it.name })
            },
            onDismiss = { showTabManage = false },
        )
    }
    if (showWeekPicker) {
        WeekPickerSheet(
            selectedDate = selectedDate,
            onPick = {
                selectedDate = it
                subTabIndex = MainSubTab.WEEK.ordinal
            },
            onDismiss = { showWeekPicker = false },
        )
    }
    editingEntry?.let { entry ->
        EntryEditSheet(
            entry = entry,
            viewModel = bookViewModel,
            allEntries = uiState.schedulePreviewEntries,
            onDismiss = { editingEntry = null },
            onRequestRepeatScope = { request -> repeatScopeRequest = request },
        )
    }
    repeatScopeRequest?.let { request ->
        RepeatScopeSheet(
            isMove = request.isMove,
            onPick = { scope ->
                executeRepeatScope(request, scope)
                if (request.isMove) editingEntry = null
            },
            onDismiss = { repeatScopeRequest = null },
        )
    }
}

/** 改期作用域确认（对照原版 pop_repeat：命中重复序列时选仅此/未来；行高 44dp） */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RepeatScopeSheet(
    isMove: Boolean,
    onPick: (BookViewModel.RepeatScope) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = null,
        containerColor = if (LocalGoaldayDarkMode.current) Color(0xFF2C2722) else Color.White,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
    ) {
        Column(
            modifier = Modifier.padding(start = 15.dp, end = 15.dp, top = 8.dp, bottom = 28.dp),
        ) {
            KeepImmersiveInDialog()
            @Composable
            fun ScopeRow(label: String, scope: BookViewModel.RepeatScope) {
                Text(
                    label,
                    fontSize = 16.sp,
                    color = GoaldayDesign.adaptiveInkPrimary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPick(scope) }
                        .padding(vertical = 14.dp),
                )
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(0.7.dp)
                        .background(GoaldayDesign.adaptiveDivider.copy(alpha = 0.6f)),
                )
            }
            ScopeRow("仅此日程", BookViewModel.RepeatScope.ONLY_THIS)
            if (isMove) {
                ScopeRow("更改所有未来日程的时间", BookViewModel.RepeatScope.ALL_FUTURE)
            } else {
                ScopeRow("删除所有未来日程", BookViewModel.RepeatScope.ALL_FUTURE)
            }
        }
    }
}

/** 周底色板（对照原版周底栏选色：行内编辑与池拖放时浮于底部，点选新条目颜色） */
@Composable
private fun WeekColorBar(
    modifier: Modifier = Modifier,
    bookColor: Color,
    activeArgb: Int?,
    onPick: (Int?) -> Unit,
) {
    val defaultArgb = bookColor.toArgb()
    val barContext = LocalContext.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(if (LocalGoaldayDarkMode.current) Color(0xFF2C2722) else Color.White)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        EntryColorChoices.forEach { argb ->
            val resolved = argb ?: defaultArgb
            val isSelected = (activeArgb ?: defaultArgb) == resolved
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(Color(resolved))
                    .border(
                        width = if (isSelected) 2.dp else 0.dp,
                        color = if (isSelected) GoaldayDesign.adaptiveInkPrimary else Color.Transparent,
                        shape = CircleShape,
                    )
                    .clickable {
                        InteractionFeedback.click(barContext)
                        onPick(argb)
                    },
                contentAlignment = Alignment.Center,
            ) {
                if (argb == null) {
                    Box(
                        Modifier
                            .size(22.dp)
                            .border(1.2.dp, Color.White.copy(alpha = 0.85f), CircleShape),
                    )
                }
            }
        }
    }
}

/** 顶部文字 Tab：N周 ▼ | 记录 | 清单；行内编辑时只显示右上角「完成」 */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun OriginalTopTabBar(
    selected: MainSubTab,
    selectedDate: LocalDate,
    editing: Boolean,
    onDone: () -> Unit,
    onWeekClick: () -> Unit,
    weekAdaptive: Boolean,
    onSelect: (MainSubTab) -> Unit,
    onManageTabs: () -> Unit,
    onOpenSettings: () -> Unit,
    tabVisibility: Map<MainSubTab, Boolean>,
    tabOrder: List<MainSubTab> = MainSubTab.entries.toList(),
) {
    // 对照原版 ll_tab：三个固定宽度 Tab（95/95/85dp）左对齐，文字在各自格内居中，
    // 高 49dp，格间 1px×15dp 分隔线；顶栏无设置入口（设置走底栏再点首页图标）
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MainTabBarBg)
            // 状态栏 inset 由外层容器统一处理，这里再加会双重下移（对照原版 Tab 文字中心≈屏y152px）
            .height(49.dp)
            .pointerInput(Unit) {
                detectTapGestures(onLongPress = { onManageTabs() })
            },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (editing) {
            Spacer(Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .background(TodayBlack)
                    .clickable { onDone() }
                    .padding(horizontal = 22.dp, vertical = 8.dp),
            ) {
                Text("完成", fontSize = 15.sp, color = Color.White)
            }
            Spacer(Modifier.width(20.dp))
            return@Row
        }
        val visible = tabOrder.filter { tabVisibility[it] == true }
        visible.forEachIndexed { index, tabItem ->
            if (index > 0) TabDividerText()
            val slotWidth = if (tabItem == MainSubTab.LIST) 85.dp else 95.dp
            // 整格可点（对照原版容器分发：点格内空白同样切换，之前只有文字裸区可点）
            val slotClick: () -> Unit = {
                // 对照原版 FlexibleTabContainer.selectTab：首次点=选中周 Tab，再点=展开/切换视图
                if (tabItem == MainSubTab.WEEK && selected == MainSubTab.WEEK) onWeekClick() else onSelect(tabItem)
            }
            Box(
                modifier = Modifier.width(slotWidth).fillMaxHeight()
                    .combinedClickable(onClick = slotClick, onLongClick = onManageTabs),
                contentAlignment = Alignment.Center,
            ) {
                when (tabItem) {
                    MainSubTab.WEEK -> Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val weekNum = selectedDate.get(WeekFields.ISO.weekOfWeekBasedYear())
                        // 对照原版tab_main：18sp恒粗体；选中黑、未选中#36000000（color_tab_main选择器）
                        val weekLabel = if (selected == MainSubTab.WEEK) "${weekNum}周" else "周"
                        Text(
                            weekLabel,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selected == MainSubTab.WEEK) {
                                if (LocalGoaldayDarkMode.current) GoaldayDesign.adaptiveInkPrimary else Color.Black
                            } else if (LocalGoaldayDarkMode.current) {
                                GoaldayDesign.adaptiveInkMuted
                            } else {
                                Color(0x36000000)
                            },
                        )
                        if (selected == MainSubTab.WEEK) {
                            Spacer(Modifier.width(4.dp))
                            // 对照原版真机：实心黑三角（收起=▼，展开=▲），非细 chevron
                            WeekTriangle(expanded = weekAdaptive)
                        }
                    }
                    MainSubTab.MONTH -> TabLabel("月", selected == MainSubTab.MONTH)
                    MainSubTab.RECORD -> TabLabel(
                        text = if (selected == MainSubTab.RECORD) "${selectedDate.monthValue}月${selectedDate.dayOfMonth}日" else "记录",
                        selected = selected == MainSubTab.RECORD,
                    )
                    MainSubTab.LIST -> TabLabel("清单", selected == MainSubTab.LIST)
                }
            }
        }
        Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun TabDividerText() {
    Text("｜", fontSize = 15.sp, color = MainTabDivider)
}

/** 周 Tab 旁的实心三角指示器：对照原版真机为填充▼/▲，而非线性 chevron */
@Composable
private fun WeekTriangle(expanded: Boolean) {
    val color = if (LocalGoaldayDarkMode.current) GoaldayDesign.adaptiveInkPrimary else Color.Black
    Canvas(Modifier.size(16.dp, 10.dp)) {
        val path = androidx.compose.ui.graphics.Path().apply {
            if (expanded) {
                moveTo(0f, size.height)
                lineTo(size.width, size.height)
                lineTo(size.width / 2f, 0f)
            } else {
                moveTo(0f, 0f)
                lineTo(size.width, 0f)
                lineTo(size.width / 2f, size.height)
            }
            close()
        }
        drawPath(path, color)
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun TabLabel(text: String, selected: Boolean) {
    // 纯展示文字，点击由外层整格接管（对照原版容器分发）
    Text(
        text,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = if (selected) {
            if (LocalGoaldayDarkMode.current) GoaldayDesign.adaptiveInkPrimary else Color.Black
        } else if (LocalGoaldayDarkMode.current) {
            GoaldayDesign.adaptiveInkMuted
        } else {
            // 对照原版color_tab_main未选中态#36000000
            Color(0x36000000)
        },
    )
}

/** 左滑操作项：图标 + 底色（对照原版 SwipeRevealLayout 的编辑黑层/删除红层，按钮内图宽50dp） */
data class SwipeAction(
    val label: String,
    val bg: Color,
    val icon: ImageVector,
    val onAction: () -> Unit,
)

/** 自绘描线图标（对照原版位图箭头/灯泡/操作栏图标：2dp 圆头描边，自绘无位图依赖）。 */
@Composable
private fun ChevronGlyph(
    mirrored: Boolean,
    color: Color,
    size: androidx.compose.ui.unit.Dp = 17.dp,
) {
    Canvas(Modifier.size(size)) {
        val s = size.toPx()
        val sw = 2.dp.toPx()
        val round = androidx.compose.ui.graphics.drawscope.Stroke(
            width = sw,
            cap = androidx.compose.ui.graphics.StrokeCap.Round,
            join = androidx.compose.ui.graphics.StrokeJoin.Round,
        )
        val x0 = if (mirrored) s * 0.66f else s * 0.34f
        val x1 = if (mirrored) s * 0.34f else s * 0.66f
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(x0, s * 0.24f)
            lineTo(x1, s * 0.5f)
            lineTo(x0, s * 0.76f)
        }
        drawPath(path, color, style = round)
    }
}

@Composable
private fun OutlineBulbGlyph(
    tint: Color,
    size: androidx.compose.ui.unit.Dp = 20.dp,
) {
    Canvas(Modifier.size(size)) {
        val s = size.toPx()
        val u = s / 20f
        val sw = 1.8.dp.toPx()
        val round = androidx.compose.ui.graphics.drawscope.Stroke(width = sw, cap = androidx.compose.ui.graphics.StrokeCap.Round)
        // 灯头圆
        drawCircle(tint, radius = 5.4f * u, center = Offset(10f * u, 7.6f * u), style = round)
        // 灯颈与底座线
        drawLine(tint, Offset(7.6f * u, 12.2f * u), Offset(7.6f * u, 14.4f * u), sw, androidx.compose.ui.graphics.StrokeCap.Round)
        drawLine(tint, Offset(12.4f * u, 12.2f * u), Offset(12.4f * u, 14.4f * u), sw, androidx.compose.ui.graphics.StrokeCap.Round)
        drawLine(tint, Offset(8.2f * u, 15.4f * u), Offset(11.8f * u, 15.4f * u), sw, androidx.compose.ui.graphics.StrokeCap.Round)
        drawLine(tint, Offset(8.8f * u, 17f * u), Offset(11.2f * u, 17f * u), sw, androidx.compose.ui.graphics.StrokeCap.Round)
        // 放射线（左上/上/右上）
        drawLine(tint, Offset(4.8f * u, 5f * u), Offset(3.4f * u, 3.6f * u), sw, androidx.compose.ui.graphics.StrokeCap.Round)
        drawLine(tint, Offset(10f * u, 1.8f * u), Offset(10f * u, 0.8f * u), sw, androidx.compose.ui.graphics.StrokeCap.Round)
        drawLine(tint, Offset(15.2f * u, 5f * u), Offset(16.6f * u, 3.6f * u), sw, androidx.compose.ui.graphics.StrokeCap.Round)
    }
}

@Composable
private fun OutlineTrashGlyph(
    tint: Color,
    size: androidx.compose.ui.unit.Dp = 22.dp,
) {
    Canvas(Modifier.size(size)) {
        val s = size.toPx()
        val u = s / 22f
        val sw = 1.9.dp.toPx()
        val cap = androidx.compose.ui.graphics.StrokeCap.Round
        // 盖 + 提手
        drawLine(tint, Offset(6.5f * u, 5f * u), Offset(15.5f * u, 5f * u), sw, cap)
        drawLine(tint, Offset(9.5f * u, 5f * u), Offset(9.5f * u, 3.2f * u), sw, cap)
        drawLine(tint, Offset(12.5f * u, 5f * u), Offset(12.5f * u, 3.2f * u), sw, cap)
        drawLine(tint, Offset(9.5f * u, 3.2f * u), Offset(12.5f * u, 3.2f * u), sw, cap)
        // 筐体
        drawLine(tint, Offset(7f * u, 5f * u), Offset(8f * u, 18.5f * u), sw, cap)
        drawLine(tint, Offset(15f * u, 5f * u), Offset(14f * u, 18.5f * u), sw, cap)
        drawLine(tint, Offset(8f * u, 18.5f * u), Offset(14f * u, 18.5f * u), sw, cap)
        // 筐内竖线
        drawLine(tint, Offset(10.2f * u, 8f * u), Offset(10.2f * u, 16f * u), sw * 0.8f, cap)
        drawLine(tint, Offset(11.8f * u, 8f * u), Offset(11.8f * u, 16f * u), sw * 0.8f, cap)
    }
}

@Composable
private fun OutlineUpGlyph(
    tint: Color,
    size: androidx.compose.ui.unit.Dp = 22.dp,
) {
    Canvas(Modifier.size(size)) {
        val s = size.toPx()
        val sw = 2.dp.toPx()
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(s * 0.3f, s * 0.64f)
            lineTo(s * 0.5f, s * 0.36f)
            lineTo(s * 0.7f, s * 0.64f)
        }
        drawPath(
            path,
            tint,
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = sw,
                cap = androidx.compose.ui.graphics.StrokeCap.Round,
                join = androidx.compose.ui.graphics.StrokeJoin.Round,
            ),
        )
    }
}

@Composable
private fun OutlineCheckGlyph(
    tint: Color,
    size: androidx.compose.ui.unit.Dp = 22.dp,
) {
    Canvas(Modifier.size(size)) {
        val s = size.toPx()
        val sw = 2.2.dp.toPx()
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(s * 0.26f, s * 0.54f)
            lineTo(s * 0.45f, s * 0.72f)
            lineTo(s * 0.76f, s * 0.3f)
        }
        drawPath(
            path,
            tint,
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = sw,
                cap = androidx.compose.ui.graphics.StrokeCap.Round,
                join = androidx.compose.ui.graphics.StrokeJoin.Round,
            ),
        )
    }
}

/** 日程条目选色（对照原版周底栏色板；null=默认色） */
private val EntryColorChoices: List<Int?> = listOf(
    null,
    0xFFF79941.toInt(),
    0xFFF66061.toInt(),
    0xFFBBD1AD.toInt(),
    0xFF8FA8F0.toInt(),
    0xFFC9A8F0.toInt(),
)

/**
 * 行左滑露出操作层（对照原版 SwipeRevealLayout，item_target_detail / item_plan_item）：
 * 内容行向左拖动露出右侧操作按钮，超过一半松手保持展开，否则弹回。
 */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun SwipeableActionsRow(
    actions: List<SwipeAction>,
    onContentClick: (() -> Unit)? = null,
    onContentLongClick: (() -> Unit)? = null,
    content: @Composable RowScope.() -> Unit,
) {
    val density = LocalDensity.current
    // 对照原版：按钮内 ImageView 宽50dp居中，每操作占50dp
    val maxRevealPx = with(density) { (50.dp * actions.size).toPx() }
    val reveal = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    Box(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .height(IntrinsicSize.Min),
        ) {
            actions.forEach { action ->
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(50.dp)
                        .background(action.bg)
                        .clickable {
                            scope.launch { reveal.snapTo(0f) }
                            action.onAction()
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = action.icon,
                        contentDescription = action.label,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(-reveal.value.roundToInt(), 0) }
                .background(MainContentBg)
                .pointerInput(actions.size) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            scope.launch {
                                reveal.snapTo((reveal.value - dragAmount).coerceIn(0f, maxRevealPx))
                            }
                        },
                        onDragEnd = {
                            scope.launch {
                                // 对照原版 SwipeRevealLayout 线性跟手：tween 线性回弹，无 spring 超调
                                val spec = tween<Float>(durationMillis = 180)
                                if (reveal.value > maxRevealPx / 2) reveal.animateTo(maxRevealPx, spec) else reveal.animateTo(0f, spec)
                            }
                        },
                        onDragCancel = {
                            scope.launch { reveal.animateTo(0f, tween(durationMillis = 180)) }
                        },
                    )
                }
                .then(
                    if (onContentClick != null) {
                        Modifier.combinedClickable(
                            onClick = onContentClick,
                            onLongClick = onContentLongClick ?: onContentClick,
                        )
                    } else {
                        Modifier
                    },
                ),
            verticalAlignment = Alignment.CenterVertically,
            content = content,
        )
    }
}

// region 周 Tab —— 左侧周日期列（任意天行内编辑）+ 右侧任务池

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun WeekScheduleView(
    uiState: BookUiState,
    viewModel: BookViewModel,
    selectedDate: LocalDate,
    editingDate: LocalDate?,
    adaptiveMode: Boolean,
    onStartEdit: (LocalDate) -> Unit,
    onFinishEdit: () -> Unit,
    onSelectDate: (LocalDate) -> Unit,
    onEditEntry: (ScheduleEntry) -> Unit = {},
    onPoolDragging: (Boolean) -> Unit = {},
    onExpandAll: () -> Unit = {},
) {
    val today = rememberToday()
    val context = LocalContext.current
    val monday = selectedDate.with(DayOfWeek.MONDAY)
    val weekDays = remember(monday) { (0..6).map { monday.plusDays(it.toLong()) } }
    var quickInput by remember(editingDate) { mutableStateOf("") }
    val dividerColor = MainTabDivider
    val diaryStore = remember { LocalStateStore(MMKV.defaultMMKV()) }
    // 长按拖拽：池条目 → 日期行排期（拖拽时上报告知外层禁掉横滑切页）
    var draggingItem by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(draggingItem) { onPoolDragging(draggingItem != null) }
    var dropTarget by remember { mutableStateOf<LocalDate?>(null) }
    val rowBounds = remember { androidx.compose.runtime.mutableStateMapOf<Long, Rect>() }
    // 池容器在窗口中的原点（把条目局部坐标换算为窗口坐标）
    var poolOrigin by remember { mutableStateOf(Offset.Zero) }
    // 跟手浮层：手指窗口坐标 + 外层容器原点（浮层偏移=手指窗口-容器窗口）
    var dragFingerWindow by remember { mutableStateOf(Offset.Zero) }
    var weekRootOrigin by remember { mutableStateOf(Offset.Zero) }
    val poolItemOrigins = remember { androidx.compose.runtime.mutableStateMapOf<String, Offset>() }
    // 右侧任务池折叠开关（对照原版 fragment_schedule 的 bg_arrow 圆钮）
    var poolCollapsed by rememberSaveable { mutableStateOf(false) }
    // 自适应态给左侧让宽：点周标题展开看全量时自动收起任务池（左列 108dp→全宽，少换行）；回固定态保持收起，点 > 再展开池
    LaunchedEffect(adaptiveMode) {
        if (adaptiveMode) poolCollapsed = true
    }
    // 右池行内改名：点条目聚焦改名（清空失焦=删除），长按拖拽排期
    var poolEditingItem by remember { mutableStateOf<String?>(null) }
    // 新增/拖放条目颜色（对照原版周底栏选色；null=清单默认色）
    var newEntryColorArgb by remember { mutableStateOf<Int?>(null) }
    var dropColorArgb by remember { mutableStateOf<Int?>(null) }
    var poolEditValue by remember { mutableStateOf(TextFieldValue()) }
    val poolEditFocus = remember { FocusRequester() }
    val poolKeyboard = LocalSoftwareKeyboardController.current
    fun commitPoolEdit() {
        val original = poolEditingItem ?: return
        val text = poolEditValue.text.trim()
        poolEditingItem = null
        if (text.isBlank()) {
            viewModel.removeListPageItem(original)
        } else if (text != original) {
            viewModel.renameListPageItem(original, text)
        }
        poolKeyboard?.hide()
    }
    LaunchedEffect(poolEditingItem) {
        if (poolEditingItem != null) runCatching { poolEditFocus.requestFocus() }
    }
    // 池空区点按建空条（对照原版池空区点按建空条）：新建行聚焦输入，空白提交=丢弃，有字=新增
    var poolCreatingNew by remember { mutableStateOf(false) }
    var poolNewValue by remember { mutableStateOf(TextFieldValue()) }
    var poolNewHadFocus by remember { mutableStateOf(false) }
    val poolNewFocus = remember { FocusRequester() }
    fun commitPoolNew() {
        if (!poolCreatingNew) return
        val text = poolNewValue.text.trim()
        poolCreatingNew = false
        poolNewHadFocus = false
        if (text.isNotBlank()) {
            InteractionFeedback.click(context)
            viewModel.addListPageItem(text)
        }
        poolKeyboard?.hide()
    }
    LaunchedEffect(poolCreatingNew) {
        if (poolCreatingNew) runCatching { poolNewFocus.requestFocus() }
    }
    val listState = rememberLazyListState()
    val focusRequester = remember { FocusRequester() }

    // 对照原版：周视图始终从周一（列表首项）开始展示完整一周，不自动滚到今天
    LaunchedEffect(monday) {
        runCatching { listState.scrollToItem(0) }
    }
    LaunchedEffect(editingDate) {
        if (editingDate != null) runCatching { focusRequester.requestFocus() }
    }

    // 外层Box承载跟手浮层（对照原版TargetDragShadowBuilder系统阴影跟手）
    Box(
        Modifier
            .fillMaxSize()
            .onGloballyPositioned { weekRootOrigin = it.boundsInWindow().topLeft },
    ) {
    Row(Modifier.fillMaxSize()) {
        // 左侧：周日期列（今日黑底圆角白字；任意一天点空白进入行内新增）
        // 固定态 7 行等高铺满（行高=可用高/7），未超限不可滑；超限当天行增高+整周可滑；自适应态内容撑高可滑
        BoxWithConstraints(
            modifier = Modifier
                // 右侧池固定 177dp（对照 fragment_schedule.xml 池面板宽），左侧占剩余
                .weight(1f)
                .fillMaxHeight(),
        ) {
            val dayH = (maxHeight - 18.dp) / 7
            // 任务区定宽：左栏宽 - 日期列 43dp - 间距 4dp - 右边距 12dp；
            // 条目全宽纵排，不用 weight（wrap 容器里 weight 会塌成内容宽、字被竖排截断）
            val taskAreaWidth = (maxWidth - 43.dp - 4.dp - 12.dp).coerceAtLeast(0.dp)
            // 固定态截断上限（展开 3 / 收起 6）；有任何一天超限则整周允许纵滑，否则保持固定等高不可滑
            val fixedCap = if (poolCollapsed) 6 else 3
            val weekHasOverflow = weekDays.any { d ->
                uiState.schedulePreviewEntries.count {
                    it.year == d.year && it.month == d.monthValue && it.day == d.dayOfMonth
                } > fixedCap
            }
        LazyColumn(
            state = listState,
            // 收起态双列多行撑高后也要能滑，否则长标题的后几行看不到
            userScrollEnabled = adaptiveMode || weekHasOverflow || poolCollapsed,
            // 对照原版真机：左列表首行距顶栏约 21dp（原版 14 文本 y284 = 顶栏底216 + 行内13 + 顶隙55）
            contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 18.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(weekDays, key = { it.toEpochDay() }) { date ->
                // 稳定排序只到时间：同键保持入库顺序（id 是随机串，排进去展示顺序随机跳变）；
                // 时间按数值排（09:30 不会掉到 10:00 后面），无时间仍置顶（与旧字符串排序一致）
                val entries = uiState.schedulePreviewEntries
                    .filter { it.year == date.year && it.month == date.monthValue && it.day == date.dayOfMonth }
                    .sortedWith(compareBy({ !it.pinned }, { if (it.timeText.isBlank()) -1 else dayEntryTimeRank(it.timeText, it.note) }))
                val isToday = date == today
                val isEditing = editingDate == date
                // 超限当天行增高（min 撑开，footer 可见）+ 整周可滑；未超限保持等高不可滑；
                // 收起态一律增高：双列窄格单行省略看不清标题，改多行全文展示
                val dayTotalCount = entries.size + (if (isEditing) 1 else 0)
                val dayOverflow = dayTotalCount > fixedCap
                val allowGrow = adaptiveMode || dayOverflow || poolCollapsed
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(if (allowGrow) Modifier.heightIn(min = dayH) else Modifier.height(dayH))
                        .onGloballyPositioned { rowBounds[date.toEpochDay()] = it.boundsInWindow() }
                        .background(
                            if (dropTarget == date) WeekBandBg else Color.Transparent,
                        )
                        .clickable {
                            onSelectDate(date)
                            onStartEdit(date)
                        }
                        .drawBehind {
                            val stroke = 0.6.dp.toPx()
                            drawLine(
                                color = dividerColor.copy(alpha = 0.4f),
                                start = Offset(0f, size.height - stroke / 2),
                                end = Offset(size.width, size.height - stroke / 2),
                                strokeWidth = stroke,
                            )
                        }
                        .padding(start = 0.dp, top = 5.dp, end = 12.dp, bottom = 5.dp),
                ) {
                    Row(
                        // 整行占满列宽，否则内部 weight 列塌成内容宽、格子不等宽
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                    ) {
                        // 日期列：对照原版真机 43dp 宽（fl_day 113px），内容居中→数字中心≈21.5dp、
                        // 今天胶囊33dp居中溢出列两侧（原版[14,100]px）；非今天不占固定高度
                        Column(
                            modifier = Modifier.width(43.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Column(
                                modifier = if (isToday) {
                                    Modifier
                                        .width(33.dp)
                                        .height(75.dp)
                                        .background(TodayBlack, RoundedCornerShape(8.dp))
                                } else {
                                    Modifier.width(33.dp)
                                },
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                            ) {
                                Text(
                                    date.dayOfMonth.toString(),
                                    fontSize = 18.sp,
                                    lineHeight = 21.sp,
                                    fontWeight = if (isToday) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isToday) Color(0xFFFDFBF7) else if (LocalGoaldayDarkMode.current) GoaldayDesign.adaptiveInkPrimary else Color.Black,
                                )
                                Spacer(Modifier.height(4.dp))
                                Box(
                                    Modifier
                                        .width(15.dp)
                                        .height(1.dp)
                                        .background(if (isToday) Color(0xFFFDFBF7) else if (LocalGoaldayDarkMode.current) GoaldayDesign.adaptiveInkMuted else Color.Black),
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    weekdayName(date),
                                    fontSize = 12.sp,
                                    lineHeight = 14.sp,
                                    fontWeight = if (isToday) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isToday) Color(0xFFFDFBF7) else if (LocalGoaldayDarkMode.current) GoaldayDesign.adaptiveInkMuted else Color.Black,
                                )
                            }
                        }
                        Spacer(Modifier.width(4.dp))
                        // 日程区：固定/自适应都是条目全宽纵排（对照布局与真机）
                        val editingSlot = if (isEditing) entries.size else -1
                        val renderCell: @Composable (Int, Boolean) -> Unit = { slotIndex, fixed ->
                            val entry = entries.getOrNull(slotIndex)
                            Box(
                                // 格子占满列宽；固定态单槽最小 31dp（对照 cW 行高）
                                modifier = Modifier.fillMaxWidth().then(if (fixed) Modifier.heightIn(min = 31.dp) else Modifier.heightIn(min = 33.dp)),
                                contentAlignment = Alignment.CenterStart,
                            ) {
                                when {
                                    entry != null -> {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            // 展开态条目可多行：勾选框顶对齐首行（中心≈首行字心）
                                            verticalAlignment = if (fixed) Alignment.CenterVertically else Alignment.Top,
                                        ) {
                                            // 勾选框：独立clickable,不与文本区抢事件
                                            // 专题关联条目圆角方+专题色，普通条目圆环（环色默认#333333，深色下跟随自适应墨色保证可见）
                                            val isDarkRow = LocalGoaldayDarkMode.current
                                            val entryTint = entry.colorArgb?.let { Color(it) }
                                                ?: if (isDarkRow) GoaldayDesign.adaptiveInkPrimary else Color(0xFF333333)
                                            val entryBoxShape = if (entry.colorArgb != null) RoundedCornerShape(4.dp) else CircleShape
                                            Box(
                                                modifier = Modifier
                                                    .padding(top = if (fixed) 0.dp else 2.dp)
                                                    .size(18.dp)
                                                    .border(
                                                        1.5.dp,
                                                        if (entry.completed) {
                                                            Color.Transparent
                                                        } else if (LocalGoaldayDarkMode.current) {
                                                            EntryCircle
                                                        } else {
                                                            entryTint
                                                        },
                                                        entryBoxShape,
                                                    )
                                                    .background(
                                                        if (entry.completed) entryTint else Color.Transparent,
                                                        entryBoxShape,
                                                    )
                                                    .clickable {
                                                        InteractionFeedback.click(context)
                                                        InteractionFeedback.haptic(context, 30L)
                                                        viewModel.toggleScheduleCompletedFromHandbook(entry.id)
                                                        val flipped = entries.map {
                                                            if (it.id == entry.id) it.copy(completed = !it.completed) else it
                                                        }
                                                        diaryStore.setDiaryText(
                                                            DIARY_BOOK_ID,
                                                            date.toString(),
                                                            buildStructuredDiary(
                                                                date,
                                                                flipped,
                                                                diaryUserText(diaryStore, date),
                                                                diaryImagePaths(diaryStore, date),
                                                            ),
                                                        )
                                                    },
                                                contentAlignment = Alignment.Center,
                                            ) {
                                                if (entry.completed) {
                                                    Text("✓", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                }
                                            }
                                            Spacer(Modifier.width(10.dp))
                                            Box(Modifier.weight(1f).clickable { onEditEntry(entry) }) {
                                                // 固定单行省略；自适应多行换行、不限槽数展示全部（对照原版 20dip 黑色 END 省略）
                                                Text(
                                                    (if (entry.timeText.isNotBlank()) entry.timeText + "  " else "") + entry.title,
                                                    fontSize = 20.sp,
                                                    lineHeight = 26.sp,
                                                    color = if (LocalGoaldayDarkMode.current) {
                                                        if (entry.completed) GoaldayDesign.adaptiveInkMuted else GoaldayDesign.adaptiveInkPrimary
                                                    } else {
                                                        Color.Black
                                                    },
                                                    textDecoration = if (entry.completed) TextDecoration.LineThrough else TextDecoration.None,
                                                    maxLines = if (fixed) 1 else Int.MAX_VALUE,
                                                    overflow = TextOverflow.Ellipsis,
                                                )
                                            }
                                        }
                                    }
                                    // 行内新增：输入框出现在下一个空槽位（对照原版槽位 EditText）
                                    isEditing && slotIndex == editingSlot -> {
                                        BasicTextField(
                                            value = quickInput,
                                            onValueChange = { quickInput = it },
                                            singleLine = true,
                                            textStyle = TextStyle(fontSize = 20.sp, lineHeight = 26.sp, color = GoaldayDesign.adaptiveInkPrimary),
                                            cursorBrush = SolidColor(TodayCoral),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .focusRequester(focusRequester),
                                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                            keyboardActions = KeyboardActions(
                                                onDone = {
                                                    if (quickInput.isNotBlank()) {
                                                        InteractionFeedback.click(context)
                                                        viewModel.addScheduleFromHandbook(
                                                            quickInput,
                                                            date.monthValue,
                                                            date.dayOfMonth,
                                                            year = date.year,
                                                            colorArgb = newEntryColorArgb,
                                                        )
                                                    }
                                                    quickInput = ""
                                                },
                                            ),
                                            decorationBox = { inner ->
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                     Box(
                                                        modifier = Modifier
                                                            .size(18.dp)
                                                            .border(1.8.dp, EntryCircle, CircleShape),
                                                    )
                                                    Spacer(Modifier.width(10.dp))
                                                    Box {
                                                        if (quickInput.isEmpty()) {
                                                            Text(
                                                                "写下你现在最想完成的",
                                                                fontSize = 20.sp,
                                                                lineHeight = 26.sp,
                                                                color = GoaldayDesign.adaptiveInkMuted.copy(alpha = 0.75f),
                                                                maxLines = 1,
                                                            )
                                                        }
                                                        inner()
                                                    }
                                                }
                                            },
                                        )
                                    }
                                }
                            }
                        }
                        // 对照原版：今天空行提示横跨任务区整宽、单行截断（非编辑态才展示；编辑走槽位输入框）
                        if (isToday && entries.isEmpty() && !isEditing) {
                            val todayHint = remember {
                                val arr = context.resources.getStringArray(
                                    com.bf410.goaldaylocal.R.array.schedule_empty_hints,
                                )
                                if (arr.isNotEmpty()) arr[kotlin.random.Random.nextInt(arr.size)] else ""
                            }
                            if (todayHint.isNotEmpty()) {
                                Text(
                                    todayHint,
                                    fontSize = 20.sp,
                                    lineHeight = 26.sp,
                                    color = GoaldayDesign.adaptiveInkMuted,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.fillMaxWidth().padding(start = 28.dp),
                                )
                            }
                        } else if (adaptiveMode) {
                            // 自适应模式：条目全宽纵排、格 minHeight 33dp
                            val cellCount = maxOf(
                                entries.size,
                                if (isEditing) entries.size + 1 else -1,
                                if (isToday && entries.isEmpty()) 1 else -1,
                            ).coerceAtLeast(1)
                            // 对照原版 item_schedule_item_adaptive：单垂直容器纵排、展开态行 minHeight（真机空行254px）
                            Column(Modifier.width(taskAreaWidth).heightIn(min = 87.dp)) {
                                repeat(cellCount) { index -> renderCell(index, false) }
                            }
                        } else {
                            // 固定模式（对照原版真机：复选+单行正文）；
                            // 池展开时左半只显示 3 条（右 3 槽被池浮层盖住），单行省略；
                            // 池收起时左右两列各 3 槽全显（对照原版收起态左右 EditText），窄格改多行全文展示
                            if (!poolCollapsed) {
                                Column(Modifier.width(taskAreaWidth).heightIn(min = 93.dp)) {
                                    // 固定展开态只显 3 条：编辑时预留一槽给输入框，避免第 4 格被定高裁掉看不见
                                    val shownCount = if (isEditing) 2 else 3
                                    entries.take(shownCount).forEachIndexed { index, _ -> renderCell(index, true) }
                                    // 行内新增输入框跟在末条之后（对照原版槽位 EditText）
                                    if (isEditing) renderCell(entries.size, true)
                                    val hiddenCount = entries.size - shownCount
                                    if (hiddenCount > 0) {
                                        Text(
                                            "+还有${hiddenCount}项·点标题展开",
                                            fontSize = 12.sp,
                                            lineHeight = 16.sp,
                                            color = GoaldayDesign.adaptiveInkMuted,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.fillMaxWidth().padding(start = 28.dp, top = 2.dp).clickable { onExpandAll() },
                                        )
                                    }
                                }
                            } else {
                                Column(Modifier.fillMaxWidth().heightIn(min = 93.dp)) {
                                    // 收起态左右双列 6 槽：窄格单行省略看不清标题，一律多行全文展示；
                                    // 编辑时只占 5 槽+输入框共 6 格，保证输入框可见
                                    val leftRange = if (isEditing && entries.size >= 6) (0..1) else (0..2)
                                    val rightRange = if (isEditing && entries.size >= 6) (2..4) else (3..5)
                                    Row(Modifier.fillMaxWidth()) {
                                        // 右列无内容时不占半宽，左列独占整宽少换行（4+ 条才需右列）
                                        val showRightColumn = entries.size > 3
                                        Column(Modifier.weight(1f)) {
                                            leftRange.forEach { index -> renderCell(index, false) }
                                        }
                                        if (showRightColumn) {
                                            Column(Modifier.weight(1f)) {
                                                rightRange.forEach { index -> renderCell(index, false) }
                                            }
                                        }
                                    }
                                    // 行内新增输入框：6 槽未满落在格内对应空槽，占满时已腾槽显示
                                    if (isEditing) renderCell(entries.size, true)
                                    val visibleCount = if (isEditing && entries.size >= 6) 5 else 6
                                    val hiddenCount = entries.size - visibleCount
                                    if (hiddenCount > 0) {
                                        Text(
                                            "+还有${hiddenCount}项·点标题展开",
                                            fontSize = 12.sp,
                                            lineHeight = 16.sp,
                                            color = GoaldayDesign.adaptiveInkMuted,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.fillMaxWidth().padding(start = 28.dp, top = 2.dp).clickable { onExpandAll() },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (adaptiveMode) item { Spacer(Modifier.height(90.dp)) }
        }
        }

        if (!poolCollapsed) {
        // 中缝分隔线（对照原版 #C5BBB6 细线）
        Box(
            Modifier
                .width(0.7.dp)
                .fillMaxHeight()
                .background(dividerColor.copy(alpha = 0.5f)),
        )

        // 右侧：任务池（专题 chip + 橙色方块条目）
        Column(
            modifier = Modifier
                .width(177.dp)
                .fillMaxHeight()
                .padding(top = 10.dp)
                .onGloballyPositioned { poolOrigin = it.boundsInWindow().topLeft },
        ) {
            val currentBook = uiState.books.getOrNull(uiState.selectedBookIndex)
            // 顶卡通栏居中：圆点 10dp + 标题 20sp + 下拉箭头；点按弹出清单下拉（当前项打勾）
            var showTopicPopup by remember { mutableStateOf(false) }
            Box(Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp, end = 10.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (LocalGoaldayDarkMode.current) Color(0xFF2C2722) else Color.White)
                    .border(0.7.dp, MainTabDivider.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .clickable { showTopicPopup = true }
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                // 对照原版真机：标题圆点 10dp 跟清单颜色（v_dot 26px）
                Box(
                    Modifier
                        .size(10.dp)
                        .background(currentBook?.color ?: PoolBullet, CircleShape),
                )
                Spacer(Modifier.width(8.dp))
                    Text(
                        currentBook?.title ?: "选择清单",
                        fontSize = 20.sp,
                        lineHeight = 26.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GoaldayDesign.adaptiveInkPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                Spacer(Modifier.width(12.dp))
                Icon(
                    Icons.Filled.ExpandMore,
                    contentDescription = "切换专题",
                    tint = GoaldayDesign.adaptiveInkMuted,
                    modifier = Modifier.size(18.dp),
                )
            }
            DropdownMenu(
                expanded = showTopicPopup,
                onDismissRequest = { showTopicPopup = false },
                containerColor = if (LocalGoaldayDarkMode.current) Color(0xFF2C2722) else Color.White,
                modifier = Modifier
                    .width(220.dp)
                    .heightIn(max = 400.dp),
            ) {
                KeepImmersiveInPopup()
                uiState.books.forEachIndexed { index, book ->
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    Modifier
                                        .size(10.dp)
                                        .background(book.color, CircleShape),
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    book.title,
                                    fontSize = 16.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f),
                                )
                                if (index == uiState.selectedBookIndex) {
                                    Text("✓", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = GoaldayDesign.adaptiveInkPrimary)
                                }
                            }
                        },
                        onClick = {
                            viewModel.selectBook(index)
                            poolCollapsed = false
                            showTopicPopup = false
                            val guideKey = "pool_drag_guide_shown"
                            if (!MMKV.defaultMMKV().decodeBool(guideKey, false)) {
                                MMKV.defaultMMKV().encode(guideKey, true)
                                android.widget.Toast.makeText(context, "长按右侧清单事件，可以拖动到左侧日程哦。", android.widget.Toast.LENGTH_LONG).show()
                            }
                        },
                    )
                }
            }
            }
            Spacer(Modifier.height(10.dp))
            LazyColumn(Modifier.weight(1f)) {
                // 右栏 = 当前清单条目全集；点按行内改名（清空失焦即删除），长按拖拽排入左侧选中日期
                val targetPage = currentBook?.pages?.filterIsInstance<TargetPage>()?.firstOrNull()
                val listItems = if (targetPage != null && currentBook != null) {
                    val hidden = diaryStore.hiddenPageItems(currentBook.id, targetPage.title)
                    val merged = ((targetPage.items - hidden) + diaryStore.customPageItems(currentBook.id, targetPage.title)).distinct()
                    // 置顶顺序与详情页一致
                    diaryStore.applyPageItemOrder(currentBook.id, targetPage.title, merged)
                } else {
                    uiState.todayPlanItems
                }
                // key 带序号：同名条目跨源并存时不撞 key、不串行
                itemsIndexed(listItems, key = { index, item -> "$index:$item" }) { _, poolItem ->
                        val itemChecked = targetPage != null && viewModel.isChecked(targetPage.title, poolItem)
                        // 初挂载会先回调一次未聚焦：得过焦点之后才允许失焦提交
                        var poolRowHadFocus by remember(poolItem) { mutableStateOf(false) }
                        val poolInner: @Composable RowScope.() -> Unit = {
                            Box(
                                modifier = Modifier
                                    .padding(top = 10.dp)
                                    .size(5.dp)
                                    .background(currentBook?.color ?: PoolBullet, CircleShape),
                            )
                            Spacer(Modifier.width(6.dp))
                            // 右池：未完成黑字，完成22%黑、无删除线；最多3行自适应（固定态也不再强制单行，保证长标题可读）
                            Text(
                                poolItem,
                                fontSize = 20.sp,
                                lineHeight = 26.sp,
                                color = if (LocalGoaldayDarkMode.current) {
                                    if (itemChecked) GoaldayDesign.adaptiveInkMuted else GoaldayDesign.adaptiveInkPrimary
                                } else {
                                    if (itemChecked) Color(0x36000000) else Color.Black
                                },
                                textDecoration = TextDecoration.None,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        // 条目行：点按行内改名（清空失焦即删除）+ 长按拖拽排期
                        val dragContext = context
                        val dragEnable = true
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .alpha(if (draggingItem == poolItem) 0.35f else 1f)
                                // 对照原版 item_schedule_target：上下内距 7dp；多行时圆点顶对齐首行
                                .padding(start = 17.dp, end = 14.dp, top = 7.dp, bottom = 7.dp),
                            verticalAlignment = Alignment.Top,
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .onGloballyPositioned { poolItemOrigins[poolItem] = it.boundsInWindow().topLeft }
                                    .pointerInput(dragEnable) {
                                        detectDragGesturesAfterLongPress(
                                                onDragStart = { touch ->
                                                    if (poolEditingItem == poolItem) commitPoolEdit()
                                                    if (poolCreatingNew) commitPoolNew()
                                                    draggingItem = poolItem
                                                val origin = poolItemOrigins[poolItem] ?: poolOrigin
                                                dragFingerWindow = Offset(origin.x + touch.x, origin.y + touch.y)
                                                InteractionFeedback.haptic(dragContext)
                                            },
                                            onDrag = { change, _ ->
                                                change.consume()
                                                // 用绝对坐标（Box 原点+指针位置）反推窗口坐标；
                                                // 不能在 consume() 之后读 positionChange()（会恒为 Zero，浮条不跟手）
                                                val origin = poolItemOrigins[poolItem] ?: poolOrigin
                                                dragFingerWindow = Offset(origin.x + change.position.x, origin.y + change.position.y)
                                                dropTarget = rowBounds.entries
                                                    .firstOrNull { it.value.contains(dragFingerWindow) }
                                                    ?.let { LocalDate.ofEpochDay(it.key) }
                                            },
                                            onDragEnd = {
                                                val target = dropTarget
                                                val item = draggingItem
                                                if (target != null && item != null) {
                                                    InteractionFeedback.click(dragContext)
                                                    viewModel.addScheduleFromHandbook(item, target.monthValue, target.dayOfMonth, year = target.year, colorArgb = dropColorArgb ?: currentBook?.color?.toArgb())
                                                }
                                                draggingItem = null
                                                dropTarget = null
                                                dropColorArgb = null
                                            },
                                            onDragCancel = {
                                                draggingItem = null
                                                dropTarget = null
                                                dropColorArgb = null
                                            },
                                        )
                                    }
                            ) {
                                if (poolEditingItem == poolItem) {
                                    BasicTextField(
                                        value = poolEditValue,
                                        onValueChange = { poolEditValue = it },
                                        // 多行编辑态全文可见（对照原版池点按编辑全文换行展示；Done 照常落盘）
                                        singleLine = false,
                                        maxLines = 5,
                                        textStyle = TextStyle(fontSize = 20.sp, lineHeight = 26.sp, color = GoaldayDesign.adaptiveInkPrimary),
                                        cursorBrush = SolidColor(TodayCoral),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .focusRequester(poolEditFocus)
                                            .onFocusChanged {
                                                if (it.isFocused) {
                                                    poolRowHadFocus = true
                                                } else if (poolRowHadFocus && poolEditingItem == poolItem) {
                                                    poolRowHadFocus = false
                                                    commitPoolEdit()
                                                }
                                            },
                                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                        keyboardActions = KeyboardActions(onDone = { commitPoolEdit() }),
                                    )
                                } else {
                                    // 点按行内改名（外层只处理长按拖拽）；Top 对齐使圆点落在首行字心
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                if (poolCreatingNew) commitPoolNew()
                                                if (poolEditingItem != poolItem) {
                                                    poolEditingItem = poolItem
                                                    poolEditValue = TextFieldValue(poolItem, TextRange(poolItem.length))
                                                }
                                            },
                                        verticalAlignment = Alignment.Top,
                                    ) {
                                        poolInner()
                                    }
                                }
                            }
                        }
                    }
                if (listItems.isEmpty()) {
                    item {
                        Text(
                            "长按右侧清单条目，可以拖动到左侧选中日期",
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            color = GoaldayDesign.adaptiveInkMuted,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        )
                    }
                }
                // 空区点按建空条（对照原版池空区点按）：新建行聚焦输入，空白提交丢弃，有字新增
                if (poolCreatingNew) {
                    item(key = "pool_new_row") {
                        BasicTextField(
                            value = poolNewValue,
                            onValueChange = { poolNewValue = it },
                            singleLine = true,
                            textStyle = TextStyle(fontSize = 20.sp, lineHeight = 26.sp, color = GoaldayDesign.adaptiveInkPrimary),
                            cursorBrush = SolidColor(TodayCoral),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 17.dp, end = 14.dp, top = 7.dp, bottom = 7.dp)
                                .focusRequester(poolNewFocus)
                                .onFocusChanged {
                                    if (it.isFocused) {
                                        poolNewHadFocus = true
                                    } else if (poolNewHadFocus && poolCreatingNew) {
                                        commitPoolNew()
                                    }
                                },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { commitPoolNew() }),
                        )
                    }
                }
                item(key = "pool_empty_tap") {
                    // 池空区接住点按建空条；至少 120dp 保证短池也有可点空区
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .heightIn(min = 120.dp)
                            .clickable {
                                if (poolEditingItem != null) commitPoolEdit()
                                if (!poolCreatingNew) {
                                    poolNewValue = TextFieldValue("", TextRange.Zero)
                                    poolNewHadFocus = false
                                    poolCreatingNew = true
                                }
                            },
                    )
                }
                item { Spacer(Modifier.height(90.dp)) }
            }

            // 收起/展开按钮：43×43dp 圆形 #E5DAD4（对照原版 bg_arrow，margin 33dp）
            Box(Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 33.dp, bottom = 20.dp)
                        .size(43.dp)
                        .background(MainTabBarBg, CircleShape)
                        .clickable { poolCollapsed = !poolCollapsed },
                    contentAlignment = Alignment.Center,
                ) {
                    // 自绘描线箭头（对照原版 bg_arrow 内 8×15dp 位图箭头）
                    ChevronGlyph(mirrored = poolCollapsed, color = GoaldayDesign.adaptiveInkPrimary)
                }
            }
        }
        }

        // 池折叠时：右下角展开按钮
        if (poolCollapsed) {
            Box(Modifier.fillMaxHeight()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 33.dp, bottom = 40.dp)
                        .size(43.dp)
                        .background(MainTabBarBg, CircleShape)
                        .clickable { poolCollapsed = false },
                    contentAlignment = Alignment.Center,
                ) {
                    ChevronGlyph(mirrored = true, color = GoaldayDesign.adaptiveInkPrimary)
                }
            }
        }
    } // Row
    // 跟手浮层：对照原版1.2倍灰影，对应 dccccc 灰底
    draggingItem?.let { label ->
        val local = dragFingerWindow - weekRootOrigin
        Box(
            Modifier
                .align(Alignment.TopStart)
                .offset { IntOffset(local.x.roundToInt(), local.y.roundToInt()) }
                .width(120.dp)
                .background(Color(0xFFCCCCCC), RoundedCornerShape(6.dp))
                .padding(horizontal = 10.dp, vertical = 8.dp),
        ) {
            Text(label, fontSize = 15.sp, color = GoaldayDesign.adaptiveInkPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
    // 底色板：行内编辑或池拖放时浮于底部（对照原版周底栏选色；顶起键盘）
    if (editingDate != null || draggingItem != null) {
        val currentArgb = if (draggingItem != null) dropColorArgb else newEntryColorArgb
        WeekColorBar(
            modifier = Modifier.align(Alignment.BottomCenter).imePadding(),
            bookColor = uiState.books.getOrNull(uiState.selectedBookIndex)?.color ?: PoolBullet,
            activeArgb = currentArgb,
            onPick = { picked ->
                if (draggingItem != null) dropColorArgb = picked else newEntryColorArgb = picked
            },
        )
    }
    } // Box
}

private fun weekdayName(date: LocalDate): String = when (date.dayOfWeek) {
    DayOfWeek.MONDAY -> "周一"
    DayOfWeek.TUESDAY -> "周二"
    DayOfWeek.WEDNESDAY -> "周三"
    DayOfWeek.THURSDAY -> "周四"
    DayOfWeek.FRIDAY -> "周五"
    DayOfWeek.SATURDAY -> "周六"
    DayOfWeek.SUNDAY -> "周日"
}

// endregion

// region 记录 Tab —— 一日一问 + 日记编辑（结构化存储，供书内渲染今日完成卡片）

/** 历史日记分页窗口：对照原版 DiaryScrollAdapter.getItemCount=5，中心页=2（开屏即当天）。 */
internal const val DIARY_PAGER_SIZE = 5
internal const val DIARY_PAGER_CENTER = 2

/**
 * 分页窗口页对应的日期：date = anchor + (page - center)。
 * 对照原版 DiaryScrollAdapter.m31003a：calendar = centerCalendar + (position - 2) 天。
 */
internal fun diaryPagerDate(anchor: LocalDate, page: Int, center: Int = DIARY_PAGER_CENTER): LocalDate =
    anchor.plusDays((page - center).toLong())

/**
 * 历史日记纵向分页浏览。
 *
 * 对照原版 DiaryScrollFragment（纵向 ViewPager2 + DiaryScrollAdapter 5 页窗口）：
 * - 纵滑逐天浏览历史日记，每页即当日完整编辑器（对照 createFragment 按日期 new DiaryFragment）；
 * - 落定后锚点跟随到落定日并无动画回正到中心页，再同步选中日期
 *  （对照 onPageSelected 500ms 后 setCurrentItem(2,false) + 日期事件；collectLatest + delay 500ms
 *  合并快速连滑，与原版一致）；
 * - 外部跳日期（周表选日/书内跳转）时锚点重置并回正（对照 m31002b + ConstantViewModel 日期事件）；
 * - 编辑态（键盘工具栏出现）锁定纵滑翻页，阅读滚动不受影响（对照原版编辑态 setUserInputEnabled(false)）。
 * 注：原版 paging/DiaryPagingSource 经查无任何调用（死代码），日期按日历算术生成即可，无需 Paging3。
 */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun RecordDiaryPager(
    selectedDate: LocalDate,
    entries: List<ScheduleEntry>,
    onSelectDate: (LocalDate) -> Unit,
    directEdit: Boolean = false,
) {
    var anchorDate by remember { mutableStateOf(selectedDate) }
    val pagerState = rememberPagerState(initialPage = DIARY_PAGER_CENTER, pageCount = { DIARY_PAGER_SIZE })
    var editorFocused by remember { mutableStateOf(false) }
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }.collectLatest { settled ->
            if (settled != DIARY_PAGER_CENTER) {
                // 对照原版落定 500ms 后回正：快滑合并，只处理最后一次落定
                delay(500)
                // 先算新锚点再发布：连滑时闭包里的 anchorDate 是旧值，分两行会跳错一天
                val newAnchor = diaryPagerDate(anchorDate, settled)
                anchorDate = newAnchor
                onSelectDate(newAnchor)
                pagerState.scrollToPage(DIARY_PAGER_CENTER)
            }
        }
    }
    LaunchedEffect(selectedDate) {
        if (selectedDate != anchorDate) {
            anchorDate = selectedDate
            pagerState.scrollToPage(DIARY_PAGER_CENTER)
        }
    }
    VerticalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize(),
        userScrollEnabled = !editorFocused,
    ) { page ->
        RecordDiaryView(
            selectedDate = diaryPagerDate(anchorDate, page),
            entries = entries,
            onEditorFocusChanged = { editorFocused = it },
            directEdit = directEdit,
        )
    }
}

@Composable
private fun RecordDiaryView(
    selectedDate: LocalDate,
    entries: List<ScheduleEntry>,
    onEditorFocusChanged: (Boolean) -> Unit = {},
    directEdit: Boolean = false,
) {
    val store = remember { LocalStateStore(MMKV.defaultMMKV()) }
    // 提示语轮换：点提示语切下一条，按天持久化（书内页同步读取同一偏移，同一天同一条）；
    // 无手动轮换记录时随机初始偏移，对照原版每次绑定随机一条（remember 保证重组不重抽、不闪变）
    var promptOffset by remember(selectedDate) {
        val key = diaryPromptOffsetKey(selectedDate)
        val mmkv = MMKV.defaultMMKV()
        mutableStateOf(if (mmkv.containsKey(key)) mmkv.decodeInt(key, 0) else randomPromptOffset())
    }
    val prompt = remember(selectedDate, promptOffset) { journalPromptFor(selectedDate, promptOffset) }
    // 编辑器只展示用户正文；「今日完成」等结构化段落由系统维护
    var text by remember(selectedDate) {
        mutableStateOf(diaryUserText(store, selectedDate))
    }
    // 键盘工具栏显隐（对照原版：底栏仅编辑时出现）
    var editorFocused by remember { mutableStateOf(false) }
    // 对照原版 DiaryScrollFragment 返回键回调：编辑态下返回先退出编辑（解锁纵滑），而非退出页面
    val focusManager = LocalFocusManager.current
    androidx.activity.compose.BackHandler(enabled = editorFocused) {
        focusManager.clearFocus()
    }
    val context = androidx.compose.ui.platform.LocalContext.current
    var imagePaths by remember(selectedDate) {
        mutableStateOf(diaryImagePaths(store, selectedDate))
    }

    fun saveAll() {
        store.setDiaryText(DIARY_BOOK_ID, selectedDate.toString(), buildStructuredDiary(selectedDate, entries, text, imagePaths))
    }

    val ioScope = rememberCoroutineScope()
    val imagePicker = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) {
            // 复制进应用私有目录，保证长期可读（对照原版本地图片方案）；4K 图拷贝走 IO 线程防卡死
            ioScope.launch {
                val absPath = withContext(Dispatchers.IO) {
                    runCatching {
                        val dir = java.io.File(context.filesDir, "diary_images").apply { mkdirs() }
                        val file = java.io.File(dir, "d" + selectedDate.toEpochDay() + "_" + System.currentTimeMillis() + ".jpg")
                        context.contentResolver.openInputStream(uri)?.use { input ->
                            file.outputStream().use { output -> input.copyTo(output) }
                        }
                        file.absolutePath.takeIf { file.exists() && file.length() > 0 }
                    }.getOrNull()
                }
                if (absPath != null) {
                    imagePaths = imagePaths + absPath
                    saveAll()
                }
            }
        }
    }

    Column(Modifier.fillMaxSize().imePadding()) {
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            // 今日完成卡片（对照原版记录 Tab：橙红渐变卡片 + @来源清单）
            val completedToday = entries.filter {
                it.completed &&
                    it.year == selectedDate.year &&
                    it.month == selectedDate.monthValue &&
                    it.day == selectedDate.dayOfMonth
            }
            completedToday.forEach { doneEntry ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Brush.verticalGradient(listOf(GoaldayDesign.Pink, TodayCoral)))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                ) {
                    Column {
                        Text(
                            doneEntry.title,
                            fontSize = 15.sp,
                            lineHeight = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                        )
                        if (doneEntry.note.isNotBlank()) {
                            Text(
                                "@" + doneEntry.note,
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                color = Color.White.copy(alpha = 0.75f),
                            )
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
            }
            if (prompt.isNotBlank() && !directEdit) {
                Text(
                    prompt,
                    fontSize = 16.sp,
                    lineHeight = 22.sp,
                    color = GoaldayDesign.adaptiveInkMuted,
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) {
                        // 点提示语轮换下一条（对照原版随机取法的"换一条"直觉；按天持久化）
                        val next = promptOffset + 1
                        promptOffset = next
                        MMKV.defaultMMKV().encode(diaryPromptOffsetKey(selectedDate), next)
                    },
                )
                Spacer(Modifier.height(14.dp))
            }
            BasicTextField(
                value = text,
                onValueChange = {
                    text = it
                    saveAll()
                },
                textStyle = TextStyle(fontSize = 16.sp, lineHeight = 24.sp, color = GoaldayDesign.adaptiveInkPrimary),
                cursorBrush = SolidColor(TodayCoral),
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged {
                        editorFocused = it.isFocused
                        onEditorFocusChanged(it.isFocused)
                    },
                decorationBox = { inner ->
                    Box {
                        if (text.isEmpty()) {
                            Text(
                                "点击输入",
                                fontSize = 16.sp,
                                lineHeight = 24.sp,
                                color = GoaldayDesign.adaptiveInkMuted,
                            )
                        }
                        inner()
                    }
                },
            )
            // 已插入的图片
            imagePaths.forEach { path ->
                Spacer(Modifier.height(10.dp))
                DiaryImageThumb(path = path, onRemove = {
                    imagePaths = imagePaths - path
                    java.io.File(path).delete()
                    saveAll()
                })
            }
            Spacer(Modifier.height(120.dp))
        }
        // 键盘工具栏（对照原版 fragment_diary 底栏：bg #E5DAD4 高约 46dp，插图/键盘双 25dp 图标，仅编辑时出现）
        if (editorFocused) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MainTabBarBg)
                    .padding(start = 8.dp, end = 8.dp, top = 10.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Filled.Image,
                    contentDescription = "插入图片",
                    tint = GoaldayDesign.adaptiveInkPrimary,
                    modifier = Modifier
                        .size(25.dp)
                        .clickable {
                            imagePicker.launch(
                                androidx.activity.result.PickVisualMediaRequest(
                                    androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly,
                                ),
                            )
                        },
                )
                Spacer(Modifier.width(16.dp))
                Icon(
                    Icons.Filled.Keyboard,
                    contentDescription = "收起键盘",
                    tint = GoaldayDesign.adaptiveInkPrimary,
                    modifier = Modifier
                        .size(25.dp)
                        .clickable { focusManager.clearFocus() },
                )
            }
        }
    }
}

/** 记录页图片缩略图（含移除） */
@Composable
private fun DiaryImageThumb(path: String, onRemove: () -> Unit) {
    val bitmap = remember(path) {
        runCatching {
            val f = java.io.File(path)
            if (f.exists()) {
                val opts = android.graphics.BitmapFactory.Options().apply { inJustDecodeBounds = true }
                android.graphics.BitmapFactory.decodeFile(path, opts)
                var sample = 1
                while (opts.outWidth / sample > 1080 * 2) sample *= 2
                android.graphics.BitmapFactory.decodeFile(path, android.graphics.BitmapFactory.Options().apply { inSampleSize = sample })
            } else null
        }.getOrNull()
    }
    if (bitmap != null) {
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp)),
        ) {
            androidx.compose.foundation.Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "日记图片",
                contentScale = androidx.compose.ui.layout.ContentScale.FillWidth,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                "×",
                fontSize = 14.sp,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .background(Color(0x66000000), CircleShape)
                    .padding(horizontal = 7.dp, vertical = 1.dp)
                    .clickable { onRemove() },
            )
        }
    }
}

/** 解析已存图片路径列表 */
private fun diaryImagePaths(store: LocalStateStore, date: LocalDate): List<String> {
    val raw = store.diaryText(DIARY_BOOK_ID, date.toString())
    val start = raw.indexOf("# 图片")
    if (start < 0) return emptyList()
    val bodyStart = raw.indexOf('\n', start).takeIf { it >= 0 }?.plus(1) ?: return emptyList()
    val next = raw.indexOf("# ", bodyStart).takeIf { it >= 0 } ?: raw.length
    return raw.substring(bodyStart, next).lines().map(String::trim).filter { it.isNotBlank() }
}

/** 解析出用户正文（富文本段），用于编辑器回显 */
private fun diaryUserText(store: LocalStateStore, date: LocalDate): String {
    val raw = store.diaryText(DIARY_BOOK_ID, date.toString())
    if (raw.isBlank()) return ""
    if (!raw.contains("# ")) return raw
    val start = raw.indexOf("# 富文本")
    if (start < 0) return ""
    val bodyStart = raw.indexOf('\n', start).takeIf { it >= 0 }?.plus(1) ?: return ""
    val next = raw.indexOf("# ", bodyStart).takeIf { it >= 0 } ?: raw.length
    return raw.substring(bodyStart, next).trim()
}

/** 组装结构化日记：日期 + 今日完成（自动同步已完成日程）+ 用户正文 */
private fun buildStructuredDiary(date: LocalDate, entries: List<ScheduleEntry>, userText: String, imagePaths: List<String> = emptyList()): String {
    val completed = entries
        .filter {
            it.completed && it.year == date.year && it.month == date.monthValue && it.day == date.dayOfMonth
        }
        .map { it.title }
    return buildString {
        appendLine("# 日期")
        appendLine(date.toString())
        if (completed.isNotEmpty()) {
            appendLine("# 今日完成")
            completed.forEach { appendLine(it) }
        }
        if (imagePaths.isNotEmpty()) {
            appendLine("# 图片")
            imagePaths.forEach { appendLine(it) }
        }
        if (userText.isNotBlank()) {
            appendLine("# 富文本")
            append(userText.trim())
        }
    }.trimEnd()
}

// endregion

// region 清单 Tab —— 专题卡片列表 + FAB（仅本 Tab 显示，对照原版）

@Composable
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
private fun TopicListView(
    uiState: BookUiState,
    viewModel: BookViewModel,
    expandedBookId: String?,
    onExpandBook: (String?) -> Unit,
    onOpenBookShelf: () -> Unit,
    onOpenInspiration: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val store = remember { LocalStateStore(MMKV.defaultMMKV()) }
    var revision by remember { mutableIntStateOf(0) }
    val listContext = LocalContext.current
    var pendingDeleteBook by remember { mutableStateOf<TopicBook?>(null) }
    // 长按卡片/详情更多的操作表（对照原版长按列表弹出选择框：打开/改名/删除）
    var sheetBookId by remember { mutableStateOf<String?>(null) }
    var renameBookId by remember { mutableStateOf<String?>(null) }
    // 新建清单弹层（对照原版 +FAB 的 PlanAddBottomDialog：名称+颜色+完成）
    var showAddSheet by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        if (expandedBookId == null) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                // 对照原版 rv_plan：左右 20dp、顶部 11dp，卡片间距 4dp
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 11.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items(uiState.books, key = { it.id }) { book ->
                    val page = book.pages.filterIsInstance<TargetPage>().firstOrNull()
                    // 与详情页同口径：模板去隐藏 + 自定义追加，否则新加的条不计入 x/y
                    val cardItems = page?.let { viewModel.detailBaseItems(book, it.title) } ?: emptyList()
                    val done = cardItems.count { store.isChecked(book.id, page?.title.orEmpty(), it) }
                    val total = cardItems.size
                    // 左滑操作层（对照原版清单卡片左滑：黑色信息 + 红色删除）
                    SwipeableActionsRow(
                        actions = buildList {
                            add(
                                SwipeAction("打开", Color(0xFF252525), Icons.Filled.Info) {
                                    onExpandBook(book.id)
                                },
                            )
                            if (book.id.startsWith("custom_")) {
                                add(
                                    SwipeAction("删除", Color(0xFFED8888), Icons.Filled.Delete) {
                                        pendingDeleteBook = book
                                    },
                                )
                            }
                        },
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 49.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(GoaldayDesign.adaptiveSurface)
                                .combinedClickable(
                                    onClick = { onExpandBook(book.id) },
                                    onLongClick = {
                                        InteractionFeedback.haptic(listContext)
                                        sheetBookId = book.id
                                    },
                                )
                                .padding(start = 13.dp, end = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                Modifier
                                    .size(10.dp)
                                    .background(
                                        book.color,
                                        CircleShape,
                                    ),
                            )
                            Spacer(Modifier.width(16.dp))
                            Text(
                                book.title,
                                fontSize = 16.sp,
                                color = GoaldayDesign.adaptiveInkPrimary,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                "$done/$total",
                                fontSize = 14.sp,
                                color = GoaldayDesign.adaptiveInkPrimary,
                            )
                        }
                    }
                }
            }
        } else {
            val book = uiState.books.firstOrNull { it.id == expandedBookId }
            if (book == null) {
                onExpandBook(null)
            } else {
                TopicDetailSimple(
                    book = book,
                    store = store,
                    viewModel = viewModel,
                    revision = revision,
                    onToggle = { revision++ },
                    onBack = { onExpandBook(null) },
                )
            }
        }

        // FAB：+ 与 💡，只在清单 Tab 显示（对照原版截图）
        if (expandedBookId == null) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    // 对照原版 iv_add/iv_tip：43dp 圆钮，右 20dp、下 32dp、相互间距 18dp
                    .padding(end = 20.dp, bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(43.dp)
                        .background(FabLight, CircleShape)
                        .clickable { showAddSheet = true },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "新建清单",
                        tint = GoaldayDesign.adaptiveInkPrimary,
                        modifier = Modifier.size(22.dp),
                    )
                }
                Box(
                    modifier = Modifier
                        .size(43.dp)
                        .background(TodayBlack, CircleShape)
                        .clickable { onOpenInspiration() },
                    contentAlignment = Alignment.Center,
                ) {
                    // 自绘描线灯泡（对照原版 iv_tip 描线灯泡）
                    OutlineBulbGlyph(tint = Color.White)
                }
            }
        }

        pendingDeleteBook?.let { book ->
            AlertDialog(
                onDismissRequest = { pendingDeleteBook = null },
                title = { Text("删除清单", fontSize = 17.sp, fontWeight = FontWeight.SemiBold) },
                text = {
                    KeepImmersiveInDialog()
                    Text("确定删除「${book.title}」吗？其中的条目与记录会一并删除，无法恢复。")
                },
                confirmButton = {
                    Text(
                        "删除",
                        color = TodayCoral,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clickable {
                                InteractionFeedback.haptic(listContext)
                                viewModel.removeCustomBookById(book.id)
                                pendingDeleteBook = null
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                    )
                },
                dismissButton = {
                    Text(
                        "取消",
                        modifier = Modifier
                            .clickable { pendingDeleteBook = null }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                    )
                },
            )
        }

        // 长按/更多的操作表：打开 + 重命名/删除（自建清单；对照原版长按选择框）
        val sheetBook = uiState.books.firstOrNull { it.id == sheetBookId }
        if (sheetBook != null) {
            ModalBottomSheet(
                onDismissRequest = { sheetBookId = null },
                dragHandle = null,
                containerColor = if (LocalGoaldayDarkMode.current) Color(0xFF2C2722) else Color.White,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            ) {
                Column(
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 28.dp),
                ) {
                    KeepImmersiveInDialog()
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(10.dp).background(sheetBook.color, CircleShape))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            sheetBook.title,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GoaldayDesign.adaptiveInkPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f),
                                )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "打开",
                        fontSize = 16.sp,
                        color = GoaldayDesign.adaptiveInkPrimary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onExpandBook(sheetBook.id)
                                sheetBookId = null
                            }
                            .padding(vertical = 12.dp),
                    )
                    if (sheetBook.id.startsWith("custom_")) {
                        Text(
                            "重命名",
                            fontSize = 16.sp,
                            color = GoaldayDesign.adaptiveInkPrimary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    renameBookId = sheetBook.id
                                    sheetBookId = null
                                }
                                .padding(vertical = 12.dp),
                        )
                        Text(
                            "删除",
                            fontSize = 16.sp,
                            color = TodayCoral,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    pendingDeleteBook = sheetBook
                                    sheetBookId = null
                                }
                                .padding(vertical = 12.dp),
                        )
                    }
                }
            }
        }

        // 重命名弹层（仅自建清单；预设示例不可改名）
        val renameBook = uiState.books.firstOrNull { it.id == renameBookId }
        if (renameBook != null) {
            var renameText by remember(renameBook.id) { mutableStateOf(renameBook.title) }
            AlertDialog(
                onDismissRequest = { renameBookId = null },
                title = { Text("重命名清单", fontSize = 17.sp, fontWeight = FontWeight.SemiBold) },
                text = {
                    KeepImmersiveInDialog()
                    BasicTextField(
                        value = renameText,
                        onValueChange = { renameText = it },
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 17.sp, color = GoaldayDesign.adaptiveInkPrimary),
                        cursorBrush = SolidColor(TodayCoral),
                        modifier = Modifier.fillMaxWidth(),
                    )
                },
                confirmButton = {
                    Text(
                        "保存",
                        color = GoaldayDesign.adaptiveInkPrimary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clickable {
                                InteractionFeedback.click(listContext)
                                viewModel.renameListBook(renameBook.id, renameText)
                                renameBookId = null
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                    )
                },
                dismissButton = {
                    Text(
                        "取消",
                        modifier = Modifier
                            .clickable { renameBookId = null }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                    )
                },
            )
        }

        if (showAddSheet) {
            TopicAddSheet(
                onCreate = { title, color, linkedToSchedule ->
                    InteractionFeedback.click(listContext)
                    viewModel.createCustomBook(title, "", color, linkedToSchedule)
                    showAddSheet = false
                },
                onDismiss = { showAddSheet = false },
            )
        }
    }
}

/** 清单详情：返回 + 色点 + 标题 + 更多；编号 + 圆形勾选 + 虚线分隔（对照原版详情页取证） */
@Composable
private fun TopicDetailSimple(
    book: TopicBook,
    store: LocalStateStore,
    viewModel: BookViewModel,
    revision: Int,
    onToggle: () -> Unit,
    onBack: () -> Unit,
) {
    val page = book.pages.filterIsInstance<TargetPage>().firstOrNull()
    val dividerColor = MainTabDivider
    val detailContext = LocalContext.current
    // 勾选切换（对照原版点勾选框切换；正文行内直接编辑，整行点按保持可切换）
    fun toggleItem(item: String, pageTitle: String, checked: Boolean) {
        InteractionFeedback.click(detailContext)
        InteractionFeedback.haptic(detailContext, 30L)
        store.setChecked(book.id, pageTitle, item, !checked)
        // 完成日期戳（对照原版勾选后行下显示的日期章）
        store.setCheckedDate(
            book.id,
            pageTitle,
            item,
            if (!checked) LocalDate.now().toString() else "",
        )
        // 首次完成提示（对照种子第13条“点击下方的时间戳，会直接跳转到日记页”，每书一次）
        if (!checked) {
            val toastKey = "detail_complete_hint_shown_" + book.id
            val mmkv = MMKV.defaultMMKV()
            if (!mmkv.decodeBool(toastKey, false)) {
                mmkv.encode(toastKey, true)
                android.widget.Toast.makeText(
                    detailContext,
                    "点击下方的时间戳，会直接跳转到日记页",
                    android.widget.Toast.LENGTH_LONG,
                ).show()
            }
        }
        // 联动任务池：勾选进池（可拖去排期），取消勾选移出（同一 (bookId, 页题) 存储）
        if (page != null) {
            val pool = store.todayPlanItems(book.id, pageTitle)
            store.saveTodayPlanItems(
                book.id,
                pageTitle,
                if (!checked) (pool + item).distinct() else pool.filterNot { it == item },
            )
            viewModel.refreshSchedulePreview()
        }
        onToggle()
    }
    // 更多菜单：对照原版 target_detail_options（显示已完成/序号/完成时间），存书级偏好
    var showOptionsMenu by remember { mutableStateOf(false) }
    var optionsTick by remember { mutableIntStateOf(0) }
    optionsTick.let { }
    val showCompleted = store.detailShowCompleted(book.id)
    val showNumbers = store.detailShowNumbers(book.id)
    val showDates = store.detailShowDates(book.id)
    val pageTitle = page?.title ?: ""
    // 底部操作栏选中态：长按行选中，再次长按取消（对照原版选中后出底栏）
    var selectedDetailItem by remember { mutableStateOf<String?>(null) }
    var renameDetailItem by remember { mutableStateOf<String?>(null) }
    fun selectDetailItem(item: String) {
        InteractionFeedback.haptic(detailContext)
        selectedDetailItem = if (selectedDetailItem == item) null else item
    }
    // 选中态下返回先取消选中，再按一次才退详情
    androidx.activity.compose.BackHandler(enabled = selectedDetailItem != null) {
        selectedDetailItem = null
    }
    // 日期改期（对照原版底栏日期钮：选定日期记为完成时间并勾选）
    fun assignDetailItemDate(item: String, dateText: String) {
        store.setChecked(book.id, pageTitle, item, true)
        store.setCheckedDate(book.id, pageTitle, item, dateText)
        val pool = store.todayPlanItems(book.id, pageTitle)
        store.saveTodayPlanItems(book.id, pageTitle, (pool + item).distinct())
        viewModel.refreshSchedulePreview()
        onToggle()
    }
    fun pickDetailItemDate(item: String) {
        val init = runCatching {
            store.checkedDate(book.id, pageTitle, item).ifBlank { LocalDate.now().toString() }.let(LocalDate::parse)
        }.getOrElse { LocalDate.now() }
        android.app.DatePickerDialog(
            detailContext,
            { _, year, month, day -> assignDetailItemDate(item, LocalDate.of(year, month + 1, day).toString()) },
            init.year,
            init.monthValue - 1,
            init.dayOfMonth,
        ).show()
    }
    Column(Modifier.fillMaxSize()) {
        // 对照原版展开态顶栏：全屏页无主 Tab 栏，顶栏为内容底色；
        // 返回 chevron + 本书色圆点 + 20sp 标题 + 右侧更多。
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 14.dp, end = 16.dp, top = 14.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 返回触区放大到 48dp（对照原版 toolbar_normal 大手势区），字形不变
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clickable { onBack() },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "‹",
                    fontSize = 22.sp,
                    color = GoaldayDesign.adaptiveInkPrimary,
                )
            }
            Box(
                Modifier
                    .size(10.dp)
                    .background(book.color, CircleShape),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                book.title,
                fontSize = 20.sp,
                lineHeight = 26.sp,
                fontWeight = FontWeight.SemiBold,
                color = GoaldayDesign.adaptiveInkPrimary,
                modifier = Modifier.weight(1f),
                maxLines = 1,
            )
            Box {
                // 更多触区放大到 48dp，字形不变
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clickable { showOptionsMenu = true },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "···",
                        fontSize = 16.sp,
                        color = GoaldayDesign.adaptiveInkPrimary,
                    )
                }
                DropdownMenu(
                    expanded = showOptionsMenu,
                    onDismissRequest = { showOptionsMenu = false },
                    containerColor = if (LocalGoaldayDarkMode.current) Color(0xFF2C2722) else Color.White,
                    modifier = Modifier.width(220.dp),
                ) {
                    KeepImmersiveInPopup()
                    @Composable
                    fun DetailOptionRow(label: String, enabled: Boolean, onToggleOption: () -> Unit) {
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        label,
                                        fontSize = 16.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f),
                                    )
                                    if (enabled) {
                                        Text("✓", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = GoaldayDesign.adaptiveInkPrimary)
                                    }
                                }
                            },
                            onClick = {
                                InteractionFeedback.click(detailContext)
                                onToggleOption()
                                optionsTick++
                                showOptionsMenu = false
                            },
                        )
                    }
                    DetailOptionRow("显示已完成", showCompleted) {
                        store.setDetailShowCompleted(book.id, !showCompleted)
                    }
                    DetailOptionRow("显示序号", showNumbers) {
                        store.setDetailShowNumbers(book.id, !showNumbers)
                    }
                    DetailOptionRow("显示完成时间和日记", showDates) {
                        store.setDetailShowDates(book.id, !showDates)
                    }
                }
            }
        }
        LazyColumn(
            modifier = Modifier.weight(1f),
            // 行自带左右边距（对照原版勾选框起 27dp、内容尾 27dp、分隔线边距 20dp）
            contentPadding = PaddingValues(vertical = 0.dp),
        ) {
            // key 带上 revision：勾选写入的是 MMKV（非 Compose 观测状态），
            // revision 变化时换 key 强制重建 item，重读 isChecked 刷新勾选框；
            // 显示选项同样带进 key，否则 key 命中会跳过重组、开关看着没反应；
            // 关掉“显示已完成”时过滤掉已勾选项
            val baseItems = viewModel.detailBaseItems(book, pageTitle)
            val orderedItems = store.applyPageItemOrder(book.id, pageTitle, baseItems)
            val visibleItems = orderedItems.filter { item ->
                showCompleted || !store.isChecked(book.id, pageTitle, item)
            }
            val displayFlags = "${if (showCompleted) 1 else 0}${if (showNumbers) 1 else 0}${if (showDates) 1 else 0}"
            itemsIndexed(visibleItems, key = { _, item -> "$revision-$displayFlags-$item" }) { index, item ->
                val checked = store.isChecked(book.id, pageTitle, item)
                val checkedDateText = if (checked) store.checkedDate(book.id, pageTitle, item) else ""
                // 行内直接改名（对照原版行内 EditText）：左滑编辑进行内态，Done/失焦落盘（清空=删）
                val isEditingDetail = (renameDetailItem == item)
                // 左滑露出编辑/删除（对照原版 SwipeRevealLayout 黑编辑+红删除）；点按切换勾选，长按选中出底栏
                SwipeableActionsRow(
                    actions = listOf(
                        SwipeAction("编辑", Color(0xFF252525), Icons.Filled.Edit) {
                            selectedDetailItem = null
                            renameDetailItem = item
                        },
                        SwipeAction("删除", Color(0xFFED8888), Icons.Filled.Delete) {
                            InteractionFeedback.haptic(detailContext)
                            viewModel.removeListPageItemIn(book, pageTitle, item)
                            if (selectedDetailItem == item) selectedDetailItem = null
                            if (renameDetailItem == item) renameDetailItem = null
                            onToggle()
                        },
                    ),
                    onContentClick = if (isEditingDetail) null else ({ toggleItem(item, pageTitle, checked) }),
                    onContentLongClick = if (isEditingDetail) null else ({ selectDetailItem(item) }),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 27.dp, end = 27.dp, top = 20.dp, bottom = 20.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        // 对照原版勾选框：圆形，空心书色环；完成后书色填充 + 白勾
                        Box(
                            modifier = Modifier
                                .padding(top = 3.dp)
                                .size(20.dp)
                                .border(1.5.dp, book.color, CircleShape)
                                .background(if (checked) book.color else Color.Transparent, CircleShape)
                                .then(
                                    if (isEditingDetail) {
                                        Modifier.clickable { toggleItem(item, pageTitle, checked) }
                                    } else {
                                        Modifier
                                    },
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (checked) {
                                Text("✓", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                        Spacer(Modifier.width(10.dp))
                        if (showNumbers) {
                            Text(
                                "${index + 1}",
                                fontSize = 20.sp,
                                lineHeight = 28.sp,
                                color = if (checked) GoaldayDesign.adaptiveInkMuted else GoaldayDesign.adaptiveInkPrimary,
                            )
                            Spacer(Modifier.width(8.dp))
                        }
                        Column(Modifier.weight(1f)) {
                            if (isEditingDetail) {
                                val detailFocusManager = LocalFocusManager.current
                                val detailKeyboard = LocalSoftwareKeyboardController.current
                                var detailField by remember(item) {
                                    mutableStateOf(TextFieldValue(item, TextRange(item.length)))
                                }
                                var detailCommitted by remember(item) { mutableStateOf(false) }
                                var detailHadFocus by remember(item) { mutableStateOf(false) }
                                val detailFocusRequester = remember(item) { FocusRequester() }
                                fun commitDetailRename() {
                                    if (detailCommitted) return
                                    detailCommitted = true
                                    InteractionFeedback.click(detailContext)
                                    viewModel.renameListPageItemIn(book, pageTitle, item, detailField.text)
                                    val detailTrimmed = detailField.text.trim()
                                    if (selectedDetailItem == item && detailTrimmed.isNotBlank()) {
                                        selectedDetailItem = detailTrimmed
                                    }
                                    if (detailTrimmed.isBlank() && selectedDetailItem == item) {
                                        selectedDetailItem = null
                                    }
                                    renameDetailItem = null
                                    detailFocusManager.clearFocus()
                                    detailKeyboard?.hide()
                                    onToggle()
                                }
                                BasicTextField(
                                    value = detailField,
                                    onValueChange = { detailField = it },
                                    singleLine = true,
                                    textStyle = TextStyle(
                                        fontSize = 20.sp,
                                        lineHeight = 28.sp,
                                        color = if (checked) GoaldayDesign.adaptiveInkMuted else GoaldayDesign.adaptiveInkPrimary,
                                    ),
                                    cursorBrush = SolidColor(book.color),
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                    keyboardActions = KeyboardActions(onDone = { commitDetailRename() }),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .focusRequester(detailFocusRequester)
                                        .onFocusChanged { focusState ->
                                            if (focusState.isFocused) {
                                                detailHadFocus = true
                                            } else if (detailHadFocus && renameDetailItem == item) {
                                                commitDetailRename()
                                            }
                                        },
                                )
                                LaunchedEffect(item) {
                                    detailFocusRequester.requestFocus()
                                    detailKeyboard?.show()
                                }
                            } else {
                                Text(
                                    item,
                                    fontSize = 20.sp,
                                    lineHeight = 28.sp,
                                    color = if (checked) GoaldayDesign.adaptiveInkMuted else GoaldayDesign.adaptiveInkPrimary,
                                )
                            }
                            if (showDates && checked && checkedDateText.isNotBlank()) {
                                // 完成日期章：对照原版勾选后行下弹出的书色圆角日期，点章跳当日日记
                                Box(
                                    modifier = Modifier
                                        .padding(top = 8.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(book.color)
                                        .clickable {
                                            InteractionFeedback.click(detailContext)
                                            runCatching { LocalDate.parse(checkedDateText) }.getOrNull()?.let { diaryDate ->
                                                MainUiBridge.go(diaryDate, MainSubTab.RECORD)
                                                onBack()
                                            }
                                        }
                                        .padding(horizontal = 10.dp, vertical = 4.dp),
                                ) {
                                    Text(
                                        checkedDateText,
                                        fontSize = 13.sp,
                                        color = Color.White,
                                    )
                                }
                            }
                        }
                    }
                }
                Box(
                    Modifier
                        .padding(horizontal = 20.dp)
                        .fillMaxWidth()
                        .height(1.dp)
                        .drawBehind {
                            drawLine(
                                color = dividerColor.copy(alpha = 0.55f),
                                start = Offset(0f, size.height / 2),
                                end = Offset(size.width, size.height / 2),
                                strokeWidth = 1.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 5.dp.toPx())),
                            )
                        },
                )
            }
        }
        // 底部选中操作栏（对照原版详情底栏 46dp：日期改期 | 删除/置顶/完成）
        val selectedItem = selectedDetailItem
        if (selectedItem != null) {
            val selChecked = store.isChecked(book.id, pageTitle, selectedItem)
            val selDateText = store.checkedDate(book.id, pageTitle, selectedItem).ifBlank { LocalDate.now().toString() }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .background(if (LocalGoaldayDarkMode.current) Color(0xFF2C2722) else Color.White)
                    .drawBehind {
                        drawLine(
                            color = dividerColor.copy(alpha = 0.5f),
                            start = Offset(0f, 0f),
                            end = Offset(size.width, 0f),
                            strokeWidth = 0.7.dp.toPx(),
                        )
                    }
                    .padding(start = 16.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    selDateText,
                    fontSize = 17.sp,
                    color = GoaldayDesign.adaptiveInkPrimary,
                    maxLines = 1,
                    modifier = Modifier.clickable { pickDetailItemDate(selectedItem) },
                )
                Spacer(Modifier.weight(1f))
                Box(
                    Modifier
                        .width(1.dp)
                        .height(22.dp)
                        .background(Color(0xFFD3CDC6)),
                )
                @Composable
                fun BoardIcon(
                    label: String,
                    tint: Color = GoaldayDesign.adaptiveInkPrimary,
                    onTap: () -> Unit,
                    icon: @Composable () -> Unit,
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 56.dp, height = 46.dp)
                            .clickable {
                                InteractionFeedback.click(detailContext)
                                onTap()
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        icon()
                    }
                }
                // 自绘描线图标（对照原版详情底栏 ic_trash/ic_top/ic_complete 位图）
                BoardIcon("删除", onTap = {
                    InteractionFeedback.haptic(detailContext)
                    viewModel.removeListPageItemIn(book, pageTitle, selectedItem)
                    selectedDetailItem = null
                    onToggle()
                }) {
                    OutlineTrashGlyph(tint = GoaldayDesign.adaptiveInkPrimary)
                }
                BoardIcon("置顶", onTap = {
                    InteractionFeedback.haptic(detailContext)
                    viewModel.moveDetailPageItemToTop(book, pageTitle, selectedItem)
                    onToggle()
                }) {
                    OutlineUpGlyph(tint = GoaldayDesign.adaptiveInkPrimary)
                }
                BoardIcon("完成", onTap = {
                    toggleItem(selectedItem, pageTitle, selChecked)
                }) {
                    OutlineCheckGlyph(tint = GoaldayDesign.adaptiveInkPrimary)
                }
            }
        }
        // 行内改名已移到行内 BasicTextField（对照原版行内 EditText），此处无弹层
    }
}

// endregion

// region 新建清单弹层（对照原版 PlanAddBottomDialog：名称 + 颜色选择 + 取消/完成）

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopicAddSheet(
    onCreate: (String, Color, Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    val dark = LocalGoaldayDarkMode.current
    val fieldBg = if (dark) Color(0xFF35312B) else Color(0xFFFBF7F1)
    val palette = listOf(
        Color(0xFFF79941),
        Color(0xFFF66061),
        Color(0xFFBBD1AD),
        Color(0xFF8FA8F0),
        Color(0xFFC9A8F0),
        Color(0xFFF5D88B),
    )
    var name by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf(palette.first()) }
    // 对照原版新建清单弹层：关联到日程开关
    var linked by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null,
        containerColor = if (dark) Color(0xFF2C2722) else Color.White,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
    ) {
        // 键盘弹起时把内容顶上去，不盖住完成行
        Column(Modifier.fillMaxWidth().imePadding().padding(horizontal = 20.dp)) {
            KeepImmersiveInDialog()
            Text(
                "新建清单",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = GoaldayDesign.adaptiveInkPrimary,
            )
            Spacer(Modifier.height(12.dp))
            BasicTextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                textStyle = TextStyle(fontSize = 16.sp, color = GoaldayDesign.adaptiveInkPrimary),
                cursorBrush = SolidColor(TodayCoral),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(fieldBg)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                decorationBox = { inner ->
                    Box {
                        if (name.isEmpty()) {
                            Text(
                                "清单名称",
                                fontSize = 16.sp,
                                color = GoaldayDesign.adaptiveInkMuted.copy(alpha = 0.7f),
                            )
                        }
                        inner()
                    }
                },
            )
            Spacer(Modifier.height(14.dp))
            Text("选择颜色", fontSize = 13.sp, color = GoaldayDesign.adaptiveInkMuted)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                palette.forEach { colorItem ->
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(colorItem)
                            .border(
                                width = if (selected == colorItem) 2.5.dp else 0.dp,
                                color = if (selected == colorItem) GoaldayDesign.adaptiveInkPrimary else Color.Transparent,
                                shape = CircleShape,
                            )
                            .clickable { selected = colorItem },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (selected == colorItem) {
                            Text("✓", fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            Spacer(Modifier.height(18.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "关联到日程",
                    fontSize = 15.sp,
                    color = GoaldayDesign.adaptiveInkPrimary,
                    modifier = Modifier.weight(1f),
                )
                Switch(
                    checked = linked,
                    onCheckedChange = { linked = it },
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "取消",
                    fontSize = 15.sp,
                    color = GoaldayDesign.adaptiveInkMuted,
                    modifier = Modifier
                        .clickable { onDismiss() }
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                )
                Spacer(Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (name.trim().isBlank()) GoaldayDesign.adaptiveInkMuted.copy(alpha = 0.4f) else TodayBlack)
                        .clickable(enabled = name.trim().isNotBlank()) { onCreate(name.trim(), selected, linked) }
                        .padding(horizontal = 22.dp, vertical = 9.dp),
                ) {
                    Text("完成", fontSize = 14.sp, color = Color.White)
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

// endregion

// region 日程条目编辑弹层（对照原版点条目的编辑底栏：标题/时间/状态/移动/删除）

/** 条目编辑弹层重复行文案（对照原版 fl_repeat：不重复/每天/每周/每月）。 */
private fun repeatSheetLabel(rule: String): String =
    when (rule) {
        "daily" -> "每天"
        "weekly" -> "每周"
        "monthly" -> "每月"
        else -> "不重复"
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EntryEditSheet(
    entry: ScheduleEntry,
    viewModel: BookViewModel,
    allEntries: List<ScheduleEntry>,
    onDismiss: () -> Unit,
    onRequestRepeatScope: (BookViewModel.RepeatScopeRequest) -> Unit = {},
) {
    val entryDate = remember(entry.id) { LocalDate.of(entry.year, entry.month, entry.day) }
    val weekDays = remember(entry.id) {
        (0..6).map { entryDate.with(DayOfWeek.MONDAY).plusDays(it.toLong()) }
    }
    var title by remember(entry.id) { mutableStateOf(entry.title) }
    var timeText by remember(entry.id) { mutableStateOf(entry.timeText) }
    // 删改的作用域判定必须读最新快照：弹层内改了重复后 entry 参数已过期（对照作用域 dormant 根因）
    val liveEntry = allEntries.firstOrNull { it.id == entry.id } ?: entry
    val dark = LocalGoaldayDarkMode.current
    val sheetContext = LocalContext.current
    val fieldBg = if (dark) Color(0xFF35312B) else Color(0xFFFBF7F1)
    val sheetState = rememberModalBottomSheetState()
    val diaryStore = remember { LocalStateStore(MMKV.defaultMMKV()) }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null,
        containerColor = if (dark) Color(0xFF2C2722) else Color.White,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
    ) {
        // 键盘弹起时把内容顶上去，不盖住保存行
        Column(Modifier.fillMaxWidth().imePadding().padding(horizontal = 20.dp)) {
            KeepImmersiveInDialog()
            Text(
                "${entryDate.monthValue}月${entryDate.dayOfMonth}日 · ${weekdayName(entryDate)}",
                fontSize = 13.sp,
                color = GoaldayDesign.adaptiveInkMuted,
            )
            Spacer(Modifier.height(10.dp))
            // 标题
            BasicTextField(
                value = title,
                onValueChange = { title = it },
                singleLine = true,
                textStyle = TextStyle(fontSize = 16.sp, color = GoaldayDesign.adaptiveInkPrimary),
                cursorBrush = SolidColor(TodayCoral),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(fieldBg)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            )
            Spacer(Modifier.height(8.dp))
            // 时间（可选，如 09:30）
            BasicTextField(
                value = timeText,
                onValueChange = { timeText = it },
                singleLine = true,
                textStyle = TextStyle(fontSize = 15.sp, color = GoaldayDesign.adaptiveInkPrimary),
                cursorBrush = SolidColor(TodayCoral),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(fieldBg)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                decorationBox = { inner ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "⏰",
                            fontSize = 13.sp,
                            modifier = Modifier.padding(end = 8.dp),
                        )
                        Box {
                            if (timeText.isEmpty()) {
                                Text(
                                    "时间（可选，如 09:30）",
                                    fontSize = 15.sp,
                                    color = GoaldayDesign.adaptiveInkMuted.copy(alpha = 0.7f),
                                )
                            }
                            inner()
                        }
                    }
                },
            )
            Spacer(Modifier.height(8.dp))
            // 重复（对照原版 fl_repeat：不重复/每天/每周/每月；落盘展开后续事项，删改走作用域确认）
            var repeatNow by remember(entry.id) { mutableStateOf(entry.repeatRule) }
            var showRepeatMenu by remember { mutableStateOf(false) }
            Box {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(fieldBg)
                        .clickable { showRepeatMenu = true }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "重复",
                        fontSize = 15.sp,
                        color = GoaldayDesign.adaptiveInkPrimary,
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        repeatSheetLabel(repeatNow),
                        fontSize = 15.sp,
                        color = GoaldayDesign.adaptiveInkMuted,
                    )
                }
                DropdownMenu(
                    expanded = showRepeatMenu,
                    onDismissRequest = { showRepeatMenu = false },
                    containerColor = if (dark) Color(0xFF2C2722) else Color.White,
                ) {
                    KeepImmersiveInPopup()
                    listOf("" to "不重复", "daily" to "每天", "weekly" to "每周", "monthly" to "每月").forEach { (rule, label) ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        label,
                                        fontSize = 16.sp,
                                        maxLines = 1,
                                        color = GoaldayDesign.adaptiveInkPrimary,
                                        modifier = Modifier.weight(1f),
                                    )
                                    if (rule == repeatNow) {
                                        Text("✓", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = GoaldayDesign.adaptiveInkPrimary)
                                    }
                                }
                            },
                            onClick = {
                                showRepeatMenu = false
                                if (rule != repeatNow) {
                                    repeatNow = rule
                                    viewModel.setScheduleRepeatFromHandbook(entry.id, rule)
                                }
                            },
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            // 颜色与置顶（对照原版周底栏选色/置顶：专题色，无值=默认；置顶排本日最前）
            var pinnedNow by remember(entry.id) { mutableStateOf(entry.pinned) }
            var colorNow by remember(entry.id) { mutableStateOf(entry.colorArgb) }
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                EntryColorChoices.forEach { argb ->
                    val isSelected = colorNow == argb
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(argb?.let { Color(it) } ?: Color.Transparent)
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) GoaldayDesign.adaptiveInkPrimary else GoaldayDesign.adaptiveDivider,
                                shape = CircleShape,
                            )
                            .clickable {
                                colorNow = argb
                                viewModel.updateScheduleColorFromHandbook(entry.id, argb)
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (argb == null) {
                            Box(
                                Modifier
                                    .size(20.dp)
                                    .border(1.2.dp, GoaldayDesign.adaptiveInkMuted, CircleShape),
                            )
                        }
                    }
                }
                Spacer(Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (pinnedNow) TodayBlack else fieldBg)
                        .clickable {
                            pinnedNow = !pinnedNow
                            viewModel.pinScheduleEntries(setOf(entry.id), pinnedNow)
                        }
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                ) {
                    Text(
                        if (pinnedNow) "取消置顶" else "置顶",
                        fontSize = 13.sp,
                        color = if (pinnedNow) Color.White else GoaldayDesign.adaptiveInkPrimary,
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            // 移动到本周其他天
            Text(
                "移动到",
                fontSize = 13.sp,
                color = GoaldayDesign.adaptiveInkMuted,
            )
            Spacer(Modifier.height(6.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                weekDays.forEach { day ->
                    val isCurrent = day == entryDate
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isCurrent) TodayBlack else fieldBg,
                            )
                            .clickable(enabled = !isCurrent) {
                                if (viewModel.isRepeatingEntry(liveEntry)) {
                                    onDismiss()
                                    onRequestRepeatScope(
                                        BookViewModel.RepeatScopeRequest(
                                            targetIds = setOf(entry.id),
                                            isMove = true,
                                            moveMonth = day.monthValue,
                                            moveDay = day.dayOfMonth,
                                            moveYear = day.year,
                                        ),
                                    )
                                } else {
                                    viewModel.moveScheduleDayFromHandbook(entry.id, day.monthValue, day.dayOfMonth, year = day.year)
                                    onDismiss()
                                }
                            }
                            .padding(vertical = 9.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            weekdayName(day).removePrefix("周"),
                            fontSize = 13.sp,
                            color = if (isCurrent) Color.White else GoaldayDesign.adaptiveInkPrimary,
                        )
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // 状态切换
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(fieldBg)
                        .clickable {
                            viewModel.toggleScheduleCompletedFromHandbook(entry.id)
                            onDismiss()
                        }
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                ) {
                    Text(
                        if (entry.completed) "标记未完成" else "标记完成",
                        fontSize = 13.sp,
                        color = GoaldayDesign.adaptiveInkPrimary,
                    )
                }
                Spacer(Modifier.weight(1f))
                // 删除（红字，对照原版危险操作；重复条目先选作用域）
                Text(
                    "删除",
                    fontSize = 15.sp,
                    color = TodayCoral,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clickable {
                            InteractionFeedback.click(sheetContext)
                            InteractionFeedback.haptic(sheetContext)
                            if (viewModel.isRepeatingEntry(liveEntry)) {
                                onDismiss()
                                onRequestRepeatScope(
                                    BookViewModel.RepeatScopeRequest(
                                        targetIds = setOf(entry.id),
                                        isMove = false,
                                    ),
                                )
                            } else {
                                viewModel.deleteScheduleFromHandbook(entry.id)
                                // 同步清理该日日记「今日完成」段（与勾选联动共用同一存储）
                            val remaining = allEntries.filter {
                                it.id != entry.id &&
                                    it.year == entry.year && it.month == entry.month && it.day == entry.day
                            }
                            diaryStore.setDiaryText(
                                DIARY_BOOK_ID,
                                entryDate.toString(),
                                buildStructuredDiary(
                                    entryDate,
                                    remaining,
                                    diaryUserText(diaryStore, entryDate),
                                    diaryImagePaths(diaryStore, entryDate),
                                ),
                            )
                            onDismiss()
                            }
                        }
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                )
                Spacer(Modifier.width(8.dp))
                // 保存
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(TodayBlack)
                        .clickable {
                            if (title.trim().isNotBlank()) {
                                viewModel.updateScheduleTitleFromHandbook(entry.id, title)
                            }
                            viewModel.updateScheduleTimeFromHandbook(entry.id, timeText)
                            onDismiss()
                        }
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                ) {
                    Text("保存", fontSize = 14.sp, color = Color.White)
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

// endregion

// region Tab 管理弹层（对照原版 dialog_tab_manage）

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TabManageSheet(
    visibility: Map<MainSubTab, Boolean>,
    order: List<MainSubTab>,
    onToggle: (MainSubTab, Boolean) -> Unit,
    onReorder: (List<MainSubTab>) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        // 对照原版 bg_tab_manage_sheet：#FDFAF6 + 顶部35dp圆角；BottomSheetDialog强制展开+跳过折叠态；
        // 原版无系统拖拽手柄（仅自定义浅灰小条），故 dragHandle=null
        dragHandle = null,
        containerColor = if (LocalGoaldayDarkMode.current) Color(0xFF2C2722) else Color(0xFFFDFAF6),
        shape = RoundedCornerShape(topStart = 35.dp, topEnd = 35.dp),
    ) {
        Column(Modifier.fillMaxWidth()) {
            KeepImmersiveInDialog()
            // 拖拽手柄：对照原版59×8dp/marginTop12/#F0F0F0/6dp圆角
            Box(
                Modifier
                    .padding(top = 12.dp)
                    .size(width = 59.dp, height = 8.dp)
                    .align(Alignment.CenterHorizontally)
                    .background(Color(0xFFF0F0F0), RoundedCornerShape(6.dp)),
            )
            Text(
                "按住拖动调整页面顺序",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = GoaldayDesign.adaptiveInkMuted,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 12.dp),
            )
            var localOrder by remember { mutableStateOf(order) }
            var draggingTab by remember { mutableStateOf<MainSubTab?>(null) }
            val rowBounds = remember { androidx.compose.runtime.mutableStateMapOf<MainSubTab, Rect>() }
            Column(Modifier.padding(horizontal = 16.dp)) {
            localOrder.forEach { tab ->
                val visible = visibility[tab] == true
                // 对照原版 MainTab.canHide：周（SCHEDULE）不可隐藏，眼镜置灰0.6+点按吐司
                val canHideTab = tab != MainSubTab.WEEK
                val cantHideHint = {
                    InteractionFeedback.click(context)
                    android.widget.Toast.makeText(context, "该页面不支持隐藏", android.widget.Toast.LENGTH_SHORT).show()
                }
                // key 按身份跟踪行：拖拽换位时 pointerInput 不因位置重组而销毁，
                // 否则手势以 cancel 收场、onDragEnd 的保存不会执行
                key(tab) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .onGloballyPositioned { rowBounds[tab] = it.boundsInWindow() }
                        .alpha(if (draggingTab == tab) 0.4f else 1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (LocalGoaldayDarkMode.current) Color(0xFF35312B) else Color.White)
                        .pointerInput(tab) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = {
                                    draggingTab = tab
                                    InteractionFeedback.haptic(context)
                                },
                                onDrag = { change, _ ->
                                    change.consume()
                                    val from = localOrder.indexOf(tab)
                                    if (from < 0) return@detectDragGesturesAfterLongPress
                                    val bounds = rowBounds[tab] ?: return@detectDragGesturesAfterLongPress
                                    val pt = bounds.topLeft + change.position
                                    val hit = rowBounds.entries
                                        .firstOrNull { it.value.contains(pt) }?.key
                                        ?: return@detectDragGesturesAfterLongPress
                                    if (hit != tab) {
                                        val to = localOrder.indexOf(hit)
                                        if (to >= 0) {
                                            localOrder = localOrder.toMutableList().apply {
                                                add(to, removeAt(from))
                                            }
                                        }
                                    }
                                },
                                onDragEnd = {
                                    draggingTab = null
                                    onReorder(localOrder)
                                },
                                onDragCancel = {
                                    // 行重排可能中断手势：cancel 时同样落盘，避免顺序丢失
                                    draggingTab = null
                                    onReorder(localOrder)
                                },
                            )
                        }
                        // 对照原版：行体只有长按（拖拽），点击只在眼睛上；行体点击无操作
                        .height(52.dp)
                        .padding(start = 20.dp, end = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        tab.label,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoaldayDesign.adaptiveInkPrimary,
                    )
                    Spacer(Modifier.weight(1f))
                    Icon(
                        // 对照原版 ic_eye_visible/ic_eye_hidden（#252525矢量）；不可隐藏的周恒显+0.6灰
                        imageVector = if (visible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = if (visible) "隐藏" else "显示",
                        tint = GoaldayDesign.adaptiveInkPrimary,
                        modifier = Modifier
                            .size(24.dp)
                            .alpha(if (canHideTab) 1f else 0.6f)
                            .clickable {
                                if (canHideTab) onToggle(tab, !visible) else cantHideHint()
                            },
                    )
                }
                }
            }
            }
            // 对照原版 dialog_tab_manage paddingBottom=224dip
            Spacer(Modifier.height(224.dp))
        }
    }
}

// endregion

// region 月视图（原版月 Tab：整月日程滚动列表）

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun MonthScheduleView(
    uiState: BookUiState,
    viewModel: BookViewModel,
    selectedDate: LocalDate,
    onPickDay: (LocalDate) -> Unit = {},
    onEditEntry: (ScheduleEntry) -> Unit = {},
) {
    val today = rememberToday()
    val monthDays = remember(selectedDate.withDayOfMonth(1)) {
        val first = selectedDate.withDayOfMonth(1)
        val len = YearMonth.of(first.year, first.monthValue).lengthOfMonth()
        (0 until len).map { first.plusDays(it.toLong()) }
    }
    val dividerColor = MainTabDivider
    val listState = rememberLazyListState()
    val monthContext = LocalContext.current
    val monthStore = remember { LocalStateStore(MMKV.defaultMMKV()) }
    // 右侧清单侧栏折叠（对照 fragment_monthly_schedule 的 bg_arrow 圆钮）
    var monthPoolCollapsed by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(selectedDate) {
        runCatching { listState.animateScrollToItem((selectedDate.dayOfMonth - 1).coerceAtLeast(0)) }
    }
    Box(Modifier.fillMaxSize()) {
    Row(Modifier.fillMaxSize()) {
    LazyColumn(
        state = listState,
        modifier = Modifier
            .weight(if (monthPoolCollapsed) 1f else 1.15f)
            .fillMaxHeight(),
        contentPadding = PaddingValues(bottom = 90.dp),
    ) {
        items(monthDays, key = { it.toEpochDay() }) { date ->
            // 稳定排序只到时间：同键保持入库顺序（与周视图/书内一致；时间按数值排，无时间置顶）
            val entries = uiState.schedulePreviewEntries
                .filter { it.year == date.year && it.month == date.monthValue && it.day == date.dayOfMonth }
                .sortedWith(compareBy({ !it.pinned }, { if (it.timeText.isBlank()) -1 else dayEntryTimeRank(it.timeText, it.note) }))
            val isToday = date == today
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onPickDay(date) }
                    .drawBehind {
                        val stroke = 0.6.dp.toPx()
                        drawLine(
                            color = dividerColor.copy(alpha = 0.4f),
                            start = Offset(0f, size.height - stroke / 2),
                            end = Offset(size.width, size.height - stroke / 2),
                            strokeWidth = stroke,
                        )
                    }
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = if (isToday) {
                        Modifier.width(52.dp).background(TodayBlack, RoundedCornerShape(8.dp)).padding(vertical = 5.dp)
                    } else {
                        Modifier.width(52.dp).padding(vertical = 5.dp)
                    },
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        date.dayOfMonth.toString(),
                        fontSize = 15.sp,
                        lineHeight = 17.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isToday) Color.White else GoaldayDesign.adaptiveInkPrimary,
                    )
                    Text(
                        weekdayName(date),
                        fontSize = 9.sp,
                        lineHeight = 10.sp,
                        color = if (isToday) Color.White.copy(alpha = 0.85f) else GoaldayDesign.adaptiveInkMuted,
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    if (entries.isEmpty() && isToday) {
                        Text(
                            "今天还没有安排",
                            fontSize = 13.sp,
                            color = GoaldayDesign.adaptiveInkMuted.copy(alpha = 0.7f),
                        )
                    }
                    entries.forEach { entry ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    // 月视图条目同样进编辑弹层（快速勾选走周视图圆形框）
                                    onClick = { onEditEntry(entry) },
                                    onLongClick = { onEditEntry(entry) },
                                )
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(PoolBullet),
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                (if (entry.timeText.isNotBlank()) entry.timeText + "  " else "") + entry.title,
                                fontSize = 13.sp,
                                lineHeight = 16.sp,
                                color = GoaldayDesign.adaptiveInkPrimary,
                                textDecoration = if (entry.completed) TextDecoration.LineThrough else TextDecoration.None,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }
    }

        if (!monthPoolCollapsed) {
            // 中缝分隔线
            Box(
                Modifier
                    .width(0.7.dp)
                    .fillMaxHeight()
                    .background(dividerColor.copy(alpha = 0.5f)),
            )
            // 右侧清单侧栏（对照 fragment_monthly_schedule：选择清单标题栏 + 条目列表）
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(top = 10.dp),
            ) {
                val currentBook = uiState.books.getOrNull(uiState.selectedBookIndex)
                // 月池顶卡点按弹清单下拉（对照原版与周池同行为；之前是直接轮切，不可定向选）
                var showMonthTopicPopup by remember { mutableStateOf(false) }
                Box(Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (LocalGoaldayDarkMode.current) Color(0xFF2C2722) else Color.White)
                        .border(0.7.dp, MainTabDivider.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .clickable { showMonthTopicPopup = true }
                        .padding(horizontal = 10.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        Modifier
                            .size(10.dp)
                            .background(currentBook?.color ?: PoolBullet, CircleShape),
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        currentBook?.title ?: "选择清单",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GoaldayDesign.adaptiveInkPrimary,
                        modifier = Modifier.weight(1f, fill = false),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.width(10.dp))
                    Icon(
                        Icons.Filled.ExpandMore,
                        contentDescription = "切换清单",
                        tint = GoaldayDesign.adaptiveInkMuted,
                        modifier = Modifier.size(18.dp),
                    )
                }
                DropdownMenu(
                    expanded = showMonthTopicPopup,
                    onDismissRequest = { showMonthTopicPopup = false },
                    containerColor = if (LocalGoaldayDarkMode.current) Color(0xFF2C2722) else Color.White,
                    modifier = Modifier
                        .width(220.dp)
                        .heightIn(max = 400.dp),
                ) {
                    KeepImmersiveInPopup()
                    uiState.books.forEachIndexed { index, book ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        Modifier
                                            .size(10.dp)
                                            .background(book.color, CircleShape),
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        book.title,
                                        fontSize = 16.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f),
                                    )
                                    if (index == uiState.selectedBookIndex) {
                                        Text("✓", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = GoaldayDesign.adaptiveInkPrimary)
                                    }
                                }
                            },
                            onClick = {
                                viewModel.selectBook(index)
                                monthPoolCollapsed = false
                                showMonthTopicPopup = false
                            },
                        )
                    }
                }
                }
                Spacer(Modifier.height(10.dp))
                LazyColumn(Modifier.weight(1f)) {
                    val targetPage = currentBook?.pages?.filterIsInstance<TargetPage>()?.firstOrNull()
                    // 与周池/详情页同口径：滤隐藏 + 置顶排序，否则删掉/隐藏的条目在月池阴魂不散
                    val listItems = if (targetPage != null && currentBook != null) {
                        val monthHidden = monthStore.hiddenPageItems(currentBook.id, targetPage.title)
                        val monthMerged = ((targetPage.items - monthHidden) + monthStore.customPageItems(currentBook.id, targetPage.title)).distinct()
                        monthStore.applyPageItemOrder(currentBook.id, targetPage.title, monthMerged)
                    } else {
                        emptyList()
                    }
                    itemsIndexed(listItems, key = { index, item -> "$index:$item" }) { _, item ->
                        val itemChecked = targetPage != null && viewModel.isChecked(targetPage.title, item)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    InteractionFeedback.click(monthContext)
                                    viewModel.addScheduleFromHandbook(
                                        item,
                                        selectedDate.monthValue,
                                        selectedDate.dayOfMonth,
                                        year = selectedDate.year,
                                    )
                                }
                                .padding(horizontal = 14.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.Top,
                        ) {
                            if (itemChecked) {
                                Box(
                                    modifier = Modifier
                                        .size(17.dp)
                                        .border(1.6.dp, Color.Transparent, RoundedCornerShape(4.dp))
                                        .background(currentBook?.color ?: GoaldayDesign.Pink, RoundedCornerShape(4.dp)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text("✓", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            } else {
                                Box(
                                    Modifier
                                        .padding(top = 5.dp)
                                        .size(7.dp)
                                        .background(PoolBullet),
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            // 月池条目与周池统一：20sp，完成22%黑、无删除线（对照原版池完成态）
                            Text(
                                item,
                                fontSize = 20.sp,
                                lineHeight = 26.sp,
                                color = if (LocalGoaldayDarkMode.current) {
                                    if (itemChecked) GoaldayDesign.adaptiveInkMuted else GoaldayDesign.adaptiveInkPrimary
                                } else {
                                    if (itemChecked) Color(0x36000000) else Color.Black
                                },
                                textDecoration = TextDecoration.None,
                            )
                        }
                    }
                    if (listItems.isEmpty()) {
                        item {
                            Text(
                                "点右上角切换清单，点条目即可排入选中日期",
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                                color = GoaldayDesign.adaptiveInkMuted,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            )
                        }
                    }
                    item { Spacer(Modifier.height(90.dp)) }
                }
                // 收起圆钮（对照 bg_arrow 43×43dip）
                Box(Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 33.dp, bottom = 20.dp)
                            .size(43.dp)
                            .background(MainTabBarBg, CircleShape)
                            .clickable { monthPoolCollapsed = true },
                        contentAlignment = Alignment.Center,
                    ) {
                        // 与周池统一用自绘箭头（对照原版 bg_arrow）
                        ChevronGlyph(mirrored = false, color = GoaldayDesign.adaptiveInkPrimary)
                    }
                }
            }
        }
        }

        // 折叠态：右下角展开钮
        if (monthPoolCollapsed) {
            Box(Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 33.dp, bottom = 40.dp)
                        .size(43.dp)
                        .background(MainTabBarBg, CircleShape)
                        .clickable { monthPoolCollapsed = false },
                        contentAlignment = Alignment.Center,
                    ) {
                        ChevronGlyph(mirrored = true, color = GoaldayDesign.adaptiveInkPrimary)
                    }
            }
        }
    }
}

// endregion

// region 周选择器底部弹层（对照原版 dialog_calendar：周号列 + 周高亮带 + 今日珊瑚红）

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WeekPickerSheet(
    selectedDate: LocalDate,
    onPick: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    // 对照原版真机：无拖拽手柄、深色遮罩（约60%黑）、月标题常规字重+右箭头、中文周标题、
    // 周数列带竖分隔线、无当前周高亮、非本月日期留空、日期浅灰
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null,
        containerColor = if (LocalGoaldayDarkMode.current) Color(0xFF2C2722) else Color.White,
        scrimColor = Color.Black.copy(alpha = 0.58f),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
    ) {
        val today = rememberToday()
        var monthAnchor by remember { mutableStateOf(YearMonth.from(selectedDate)) }
        val weekFields = WeekFields.ISO

        KeepImmersiveInDialog()
        Column(Modifier.padding(horizontal = 16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "上个月",
                    tint = GoaldayDesign.adaptiveInkPrimary,
                    modifier = Modifier
                        .size(26.dp)
                        .clickable {
                            monthAnchor = if (monthAnchor.monthValue == 1) {
                                YearMonth.of(monthAnchor.year - 1, 12)
                            } else {
                                monthAnchor.minusMonths(1)
                            }
                        },
                )
                Text(
                    "${monthAnchor.monthValue}月 ${monthAnchor.year}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Normal,
                    color = GoaldayDesign.adaptiveInkPrimary,
                )
                Icon(
                    Icons.Filled.KeyboardArrowRight,
                    contentDescription = "下个月",
                    tint = GoaldayDesign.adaptiveInkPrimary,
                    modifier = Modifier
                        .size(22.dp)
                        .clickable {
                            monthAnchor = if (monthAnchor.monthValue == 12) {
                                YearMonth.of(monthAnchor.year + 1, 1)
                            } else {
                                monthAnchor.plusMonths(1)
                            }
                        },
                )
                Spacer(Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(TodayBlack)
                        .clickable {
                            onPick(today)
                            onDismiss()
                        }
                        .padding(horizontal = 18.dp, vertical = 8.dp),
                ) {
                    Text("今天", fontSize = 14.sp, color = Color.White)
                }
            }
            Spacer(Modifier.height(24.dp))
            Row(Modifier.fillMaxWidth()) {
                Spacer(Modifier.width(46.dp))
                listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日").forEach {
                    Text(
                        it,
                        fontSize = 14.sp,
                        color = if (LocalGoaldayDarkMode.current) GoaldayDesign.adaptiveInkMuted else Color(0xFFC9C9C9),
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            val first = monthAnchor.atDay(1)
            val firstMonday = first.with(DayOfWeek.MONDAY).let {
                if (it.isAfter(first)) it.minusWeeks(1) else it
            }
            Row(Modifier.fillMaxWidth()) {
                // 周数列 + 竖分隔线（对照原版日历面板）
                Column(Modifier.width(46.dp)) {
                    (0..5).map { firstMonday.plusWeeks(it.toLong()) }.forEach { weekStart ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(51.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                weekStart.get(weekFields.weekOfWeekBasedYear()).toString(),
                                fontSize = 14.sp,
                                color = if (LocalGoaldayDarkMode.current) GoaldayDesign.adaptiveInkMuted else Color(0xFFC9C9C9),
                            )
                        }
                    }
                }
                Box(
                    Modifier
                        .width(1.dp)
                        .height(51.dp * 6)
                        .background(
                            if (LocalGoaldayDarkMode.current) GoaldayDesign.adaptiveInkMuted.copy(alpha = 0.45f)
                            else Color(0xFFE3E3E3),
                        ),
                )
                Column(Modifier.weight(1f)) {
                    (0..5).map { firstMonday.plusWeeks(it.toLong()) }.forEach { weekStart ->
                        val days = (0..6).map { weekStart.plusDays(it.toLong()) }
                        Row(Modifier.fillMaxWidth().height(51.dp)) {
                            days.forEach { date ->
                                val inMonth = date.monthValue == monthAnchor.monthValue
                                val isToday = date == today
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clickable(enabled = inMonth) {
                                            onPick(date)
                                            onDismiss()
                                        },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    if (inMonth) {
                                        Box(
                                            modifier = Modifier
                                                .size(30.dp)
                                                .background(
                                                    if (isToday) TodayCoral else Color.Transparent,
                                                    RoundedCornerShape(8.dp),
                                                ),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Text(
                                                date.dayOfMonth.toString(),
                                                fontSize = 16.sp,
                                                fontWeight = if (isToday) FontWeight.SemiBold else FontWeight.Normal,
                                                color = if (isToday) Color.White
                                                else if (LocalGoaldayDarkMode.current) GoaldayDesign.adaptiveInkMuted
                                                else Color(0xFFBDBDBD),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

// endregion
