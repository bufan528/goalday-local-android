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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.runtime.SideEffect
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bf410.goaldaylocal.ui.InteractionFeedback
import com.bf410.goaldaylocal.data.LocalStateStore
import com.bf410.goaldaylocal.data.ScheduleEntry
import com.bf410.goaldaylocal.data.TargetPage
import com.bf410.goaldaylocal.data.TopicBook
import com.bf410.goaldaylocal.ui.book.BookUiState
import com.bf410.goaldaylocal.ui.book.BookViewModel
import com.bf410.goaldaylocal.ui.replica.GoaldayDesign
import com.bf410.goaldaylocal.ui.replica.LocalGoaldayDarkMode
import com.tencent.mmkv.MMKV
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.WeekFields
import kotlin.math.roundToInt
import kotlinx.coroutines.launch
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
 * 原版主界面 1:1 复刻（对照 com.first.goalday v2.5.7 实机截图与逆向布局）。
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

    internal fun go(date: LocalDate, tab: MainSubTab) {
        this.date = date
        this.targetTab = tab
        tick++
    }

    fun consume() {
        date = null
        targetTab = null
    }
}

/** 原版 JournalPrompts 的 41 条一日一问（jadx 反编译 JournalPrompts.java 全量搬运） */
private val JOURNAL_PROMPTS = listOf(
    "描述一周中最值得记住的时刻。",
    "今天的天气如何？它影响了你的心情吗？",
    "醒来后第一个念头是什么？",
    "今天穿的衣服是什么颜色/风格？为什么选这套？",
    "早餐/今天的第一口食物是什么？味道如何？",
    "路上看到最有趣的事物是什么？（比如一只猫、一朵云、一个招牌）",
    "手机相册里今天的第一张照片拍的是什么？",
    "今天听到最印象深刻的一句话（或歌词）是什么？",
    "手边常用的物品（比如水杯、笔）今天有什么特别之处吗？",
    "今天是否闻到了某种特别的气味？让你联想到了什么？",
    "天空的颜色在一天中有变化吗？哪个时刻最美？",
    "用三个词形容今天的整体情绪。",
    "今天什么时候笑了？为什么？",
    "是否有瞬间感到焦虑或不安？当时在做什么？",
    "今天最让自己感到骄傲的一件小事是什么？",
    "如果今天有一种颜色，它会是什么？为什么？",
    "今天是否错过了什么？心里有什么感觉？",
    "谁或什么事让你今天感到温暖？",
    "今天是否做了决定？是轻松还是艰难的选择？",
    "睡前此刻的心情是怎样的？",
    "今天是否有什么意外惊喜？",
    "今天和谁聊天最愉快？聊了什么？",
    "是否帮助了别人或被别人帮助？细节是什么？",
    "今天听到最有趣的八卦或故事是什么？",
    "如果有人给你今天的社交状态拍张照，会是什么画面？",
    "是否遇到新面孔？TA给你什么印象？",
    "今天是否说了\"谢谢\"或收到感谢？因为什么？",
    "是否和某人产生分歧？后来如何了？",
    "今天最想分享给朋友的事是什么？",
    "如果给家人发今天的一条总结短信，你会写什么？",
    "今天是否想到某个远方的人？为什么？",
    "如果今天是一部电影，它的名字会叫什么？",
    "今天有什么瞬间想按下\"暂停键\"重复体验？",
    "如果让今天的你给十年后的自己写句话，会写什么？",
    "今天是否有一个\"啊哈！\"的灵感时刻？",
    "如果今天是一种食物，它会是什么味道？",
    "今天的衣服风格像哪种动物或植物？",
    "今天是否听到一段音乐或声音让你浮想联翩？",
    "如果今天是一个梦境，你会如何解释它？",
    "给今天的关键词画个简单的符号（描述出来即可）。",
    "平行世界的另一个你今天可能在做什么？",
)

@Composable
fun OriginalMainScreen(
    bookViewModel: BookViewModel,
    openWeekPickerTick: Int = 0,
    onOpenBook: () -> Unit,
    onOpenInspiration: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val uiState by bookViewModel.uiState.collectAsState()
    var subTabIndex by rememberSaveable { mutableIntStateOf(MainSubTab.WEEK.ordinal) }
    var selectedDate by rememberSaveable { mutableStateOf(LocalDate.now()) }
    var showWeekPicker by remember { mutableStateOf(false) }
    // 周视图模式（对照原版 MainViewModel.isCurrentScheduleViewExpanded：再点周Tab在固定2×3与自适应流式间切换）
    var scheduleAdaptive by rememberSaveable { mutableStateOf(false) }
    // 行内编辑中的日期（任意一天可编辑，对照原版）；非 null 时顶栏显示「完成」
    var editingDate by remember { mutableStateOf<LocalDate?>(null) }
    // 清单详情（提升到主界面层以便系统返回拦截）
    var expandedBookId by rememberSaveable { mutableStateOf<String?>(null) }
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

    // 系统返回：清单详情/行内编辑/条目编辑优先返回上一级，其余交给应用级返回
    androidx.activity.compose.BackHandler(
        enabled = editingDate != null || expandedBookId != null || editingEntry != null,
    ) {
        editingDate = null
        expandedBookId = null
        editingEntry = null
    }

    LaunchedEffect(Unit) { bookViewModel.refreshSchedulePreview() }
    LaunchedEffect(openWeekPickerTick) {
        if (openWeekPickerTick > 0) showWeekPicker = true
    }
    // 书内点页 → 跳到对应日期与 Tab（对照原版 EventBus 行为）
    LaunchedEffect(MainUiBridge.tick) {
        val target = MainUiBridge.date
        val tab = MainUiBridge.targetTab
        if (target != null && tab != null) {
            selectedDate = target
            subTabIndex = tab.ordinal
            MainUiBridge.consume()
        }
    }

    var currentSubTab = MainSubTab.entries[subTabIndex.coerceIn(0, MainSubTab.entries.lastIndex)]
    if (tabVisibility[currentSubTab] == false) {
        // 回退到第一个可见 Tab（当前 Tab 被隐藏时，按用户自定顺序）
        val firstVisible = tabOrder.firstOrNull { tabVisibility[it] == true } ?: MainSubTab.WEEK
        currentSubTab = firstVisible
        subTabIndex = firstVisible.ordinal
    }

    // 对照原版真机：状态栏与顶栏同色（米色延伸到状态栏区）+ 黑色状态栏图标
    val statusBarColor = MainTabBarBg
    val mainWindow = (LocalContext.current as? android.app.Activity)?.window
    SideEffect {
        mainWindow?.let { w ->
            w.statusBarColor = statusBarColor.toArgb()
            androidx.core.view.WindowInsetsControllerCompat(w, w.decorView).isAppearanceLightStatusBars = true
        }
    }

    Column(Modifier.fillMaxSize().background(MainContentBg)) {
        OriginalTopTabBar(
            selected = currentSubTab,
            selectedDate = selectedDate,
            editing = editingDate != null,
            onDone = { editingDate = null },
            onWeekClick = { scheduleAdaptive = !scheduleAdaptive },
            weekAdaptive = scheduleAdaptive,
            onSelect = {
                editingDate = null
                subTabIndex = it.ordinal
            },
            onManageTabs = { showTabManage = true },
            onOpenSettings = onOpenSettings,
            tabVisibility = tabVisibility,
            tabOrder = tabOrder,
        )
        // Tab 切换方向滑动（对照原版 ViewPager2 滑动切换的直觉：往左切页从右滑入）
        // 退出页整屏滑出，避免残留纹理在屏幕边缘露出；clipToBounds 兜底裁剪
        AnimatedContent(
            targetState = currentSubTab,
            modifier = Modifier.fillMaxSize().clipToBounds(),
            transitionSpec = {
                val forward = targetState.ordinal >= initialState.ordinal
                (slideInHorizontally(tween(240)) { full -> if (forward) full else -full } + fadeIn(tween(200))) togetherWith
                    (slideOutHorizontally(tween(240)) { full -> if (forward) -full else full } + fadeOut(tween(180)))
            },
            label = "mainTabContent",
        ) { pageTab ->
            when (pageTab) {
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
        )
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
            Box(
                modifier = Modifier.width(slotWidth).fillMaxHeight(),
                contentAlignment = Alignment.Center,
            ) {
                when (tabItem) {
                    MainSubTab.WEEK -> Row(
                        modifier = Modifier.combinedClickable(
                            onClick = {
                                // 对照原版 FlexibleTabContainer.selectTab：首次点=选中周 Tab，再点=展开/切换视图
                                if (selected == MainSubTab.WEEK) onWeekClick() else onSelect(MainSubTab.WEEK)
                            },
                            onLongClick = onManageTabs,
                        ),
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
                    MainSubTab.MONTH -> TabLabel("月", selected == MainSubTab.MONTH, onLongPress = onManageTabs) { onSelect(MainSubTab.MONTH) }
                    MainSubTab.RECORD -> TabLabel(
                        text = if (selected == MainSubTab.RECORD) "${selectedDate.monthValue}月${selectedDate.dayOfMonth}日" else "记录",
                        selected = selected == MainSubTab.RECORD,
                        onLongPress = onManageTabs,
                    ) { onSelect(MainSubTab.RECORD) }
                    MainSubTab.LIST -> TabLabel("清单", selected == MainSubTab.LIST, onLongPress = onManageTabs) { onSelect(MainSubTab.LIST) }
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
private fun TabLabel(text: String, selected: Boolean, onLongPress: () -> Unit = {}, onClick: () -> Unit) {
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
        modifier = Modifier.combinedClickable(onClick = onClick, onLongClick = onLongPress),
    )
}

/** 左滑操作项：图标 + 底色（对照原版 SwipeRevealLayout 的编辑黑层/删除红层，按钮内图宽50dp） */
data class SwipeAction(
    val label: String,
    val bg: Color,
    val icon: ImageVector,
    // 原版 plan_edit/plan_trash PNG（白图，只能站深底）；有值时优先用图（放onAction前以保trailing写法）
    val iconRes: Int? = null,
    val onAction: () -> Unit,
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
                    if (action.iconRes != null) {
                        Image(
                            painter = painterResource(action.iconRes),
                            contentDescription = action.label,
                            modifier = Modifier.width(50.dp),
                            contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                        )
                    } else {
                        Icon(
                            imageVector = action.icon,
                            contentDescription = action.label,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp),
                        )
                    }
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
                                if (reveal.value > maxRevealPx / 2) reveal.animateTo(maxRevealPx) else reveal.animateTo(0f)
                            }
                        },
                        onDragCancel = {
                            scope.launch { reveal.animateTo(0f) }
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
) {
    val today = rememberToday()
    val context = LocalContext.current
    val monday = selectedDate.with(DayOfWeek.MONDAY)
    val weekDays = remember(monday) { (0..6).map { monday.plusDays(it.toLong()) } }
    var quickInput by remember(editingDate) { mutableStateOf("") }
    val dividerColor = MainTabDivider
    val diaryStore = remember { LocalStateStore(MMKV.defaultMMKV()) }
    // 长按拖拽：池条目 → 日期行排期
    var draggingItem by remember { mutableStateOf<String?>(null) }
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
        LazyColumn(
            state = listState,
            // 对照原版真机：左列表首行距顶栏约 21dp（原版 14 文本 y284 = 顶栏底216 + 行内13 + 顶隙55）
            contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 18.dp),
            modifier = Modifier
                .weight(if (poolCollapsed) 1f else 1.15f)
                .fillMaxHeight(),
        ) {
            items(weekDays, key = { it.toEpochDay() }) { date ->
                val entries = uiState.schedulePreviewEntries
                    .filter { it.year == date.year && it.month == date.monthValue && it.day == date.dayOfMonth }
                    .sortedWith(compareBy({ it.timeText }, { it.id }))
                val isToday = date == today
                val isEditing = editingDate == date
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
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
                    Row(verticalAlignment = Alignment.Top) {
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
                                    color = if (isToday) Color.White else GoaldayDesign.adaptiveInkPrimary,
                                )
                                Spacer(Modifier.height(4.dp))
                                Box(
                                    Modifier
                                        .width(15.dp)
                                        .height(1.dp)
                                        .background(if (isToday) Color.White else Color(0xFFB3000000)),
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    weekdayName(date),
                                    fontSize = 12.sp,
                                    lineHeight = 14.sp,
                                    fontWeight = if (isToday) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isToday) Color.White else GoaldayDesign.adaptiveInkMuted,
                                )
                            }
                        }
                        Spacer(Modifier.width(4.dp))
                        // 日程区：固定模式=原版 2×3 槽（槽高 31dp，真机行高 271px=103dp=3×31+上下5）；自适应模式=行数随内容增长
                        val editingSlot = if (isEditing) entries.size else -1
                        val renderCell: @Composable (Int, Boolean) -> Unit = { slotIndex, fixed ->
                            val entry = entries.getOrNull(slotIndex)
                            Box(
                                modifier = if (fixed) Modifier.height(31.dp) else Modifier.heightIn(min = 33.dp),
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
                                            Box(
                                                modifier = Modifier
                                                    .padding(top = if (fixed) 0.dp else 2.dp)
                                                    .size(19.dp)
                                                    .border(
                                                        1.8.dp,
                                                        if (entry.completed) Color.Transparent else EntryCircle,
                                                        CircleShape,
                                                    )
                                                    .background(
                                                        if (entry.completed) GoaldayDesign.Pink else Color.Transparent,
                                                        CircleShape,
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
                                                // 对照原版：固定2×3槽单行截断；展开(自适应)态多行换行不截断
                                                Text(
                                                    (if (entry.timeText.isNotBlank()) entry.timeText + "  " else "") + entry.title,
                                                    fontSize = 17.sp,
                                                    lineHeight = 23.sp,
                                                    color = if (entry.completed) GoaldayDesign.adaptiveInkMuted else GoaldayDesign.adaptiveInkPrimary,
                                                    textDecoration = if (entry.completed) TextDecoration.LineThrough else TextDecoration.None,
                                                    maxLines = if (fixed) 1 else Int.MAX_VALUE,
                                                    overflow = TextOverflow.Clip,
                                                )
                                            }
                                        }
                                    }
                                    // 对照原版：今天第一个空槽显示随机引导提示语（schedule_empty_hints）
                                    slotIndex == 0 && isToday && entries.isEmpty() -> {
                                        val hint = remember {
                                            val arr = context.resources.getStringArray(
                                                com.bf410.goaldaylocal.R.array.schedule_empty_hints,
                                            )
                                            if (arr.isNotEmpty()) arr[kotlin.random.Random.nextInt(arr.size)] else ""
                                        }
                                        if (hint.isNotEmpty()) {
                                            Text(
                                                hint,
                                                fontSize = 17.sp,
                                                lineHeight = 23.sp,
                                                color = GoaldayDesign.adaptiveInkMuted,
                                                maxLines = if (fixed) 1 else 2,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                        }
                                    }
                                    // 行内新增：输入框出现在下一个空槽位（对照原版槽位 EditText）
                                    isEditing && slotIndex == editingSlot -> {
                                        BasicTextField(
                                            value = quickInput,
                                            onValueChange = { quickInput = it },
                                            singleLine = true,
                                            textStyle = TextStyle(fontSize = 15.sp, color = GoaldayDesign.adaptiveInkPrimary),
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
                                                        )
                                                    }
                                                    quickInput = ""
                                                },
                                            ),
                                            decorationBox = { inner ->
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(19.dp)
                                                            .border(1.8.dp, EntryCircle, CircleShape),
                                                    )
                                                    Spacer(Modifier.width(10.dp))
                                                    Box {
                                                        if (quickInput.isEmpty()) {
                                                            Text(
                                                                "写下你现在最想完成的",
                                                                fontSize = 15.sp,
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
                        if (adaptiveMode) {
                            // 自适应模式（对照原版 item_schedule_item_adaptive）：每行 2 格、格 minHeight 33dp、无 6 槽上限
                            val cellCount = maxOf(
                                entries.size,
                                if (isEditing) entries.size + 1 else -1,
                                if (isToday && entries.isEmpty()) 1 else -1,
                            ).coerceAtLeast(1)
                            // 对照原版 item_schedule_item_adaptive：展开态行 minHeight（真机空行254px）
                            Column(Modifier.heightIn(min = 87.dp)) {
                                repeat((cellCount + 1) / 2) { row ->
                                    Row {
                                        Box(Modifier.weight(1f)) { renderCell(row * 2, false) }
                                        Box(Modifier.weight(1f)) { renderCell(row * 2 + 1, false) }
                                    }
                                }
                            }
                        } else {
                            // 固定模式内容区=3×31dp 槽（原版真机行高 271px=103dp=内容93+上下padding10）
                            Row(Modifier.heightIn(min = 93.dp)) {
                                Column(Modifier.weight(1f)) {
                                    repeat(3) { renderCell(it, true) }
                                }
                                Column(Modifier.weight(1f)) {
                                    repeat(3) { renderCell(it + 3, true) }
                                }
                            }
                            // 槽位已满时的兜底输入行（原版 6 槽满后新增走详情页）
                            if (isEditing && editingSlot >= 6) {
                            BasicTextField(
                                value = quickInput,
                                onValueChange = { quickInput = it },
                                singleLine = true,
                                textStyle = TextStyle(fontSize = 15.sp, color = GoaldayDesign.adaptiveInkPrimary),
                                cursorBrush = SolidColor(TodayCoral),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 2.dp)
                                    .focusRequester(focusRequester),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        if (quickInput.isNotBlank()) {
                                            viewModel.addScheduleFromHandbook(
                                                quickInput,
                                                date.monthValue,
                                                date.dayOfMonth,
                                            )
                                        }
                                        quickInput = ""
                                    },
                                ),
                            )
                            }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(90.dp)) }
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
                .weight(1f)
                .fillMaxHeight()
                .padding(top = 10.dp)
                .onGloballyPositioned { poolOrigin = it.boundsInWindow().topLeft },
        ) {
            val currentBook = uiState.books.getOrNull(uiState.selectedBookIndex)
            // 对照原版真机：chip 为包裹宽度、右对齐（右边距18dp），非通栏
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
            Row(
                modifier = Modifier
                    .padding(end = 18.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (LocalGoaldayDarkMode.current) Color(0xFF2C2722) else Color.White)
                    .border(0.7.dp, MainTabDivider.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .clickable {
                        val next = (uiState.selectedBookIndex + 1) % uiState.books.size.coerceAtLeast(1)
                        viewModel.openBook(next)
                    }
                    .padding(horizontal = 10.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // 对照原版真机：标题圆点 10dp 跟清单颜色（v_dot 26px）
                Box(
                    Modifier
                        .size(10.dp)
                        .background(currentBook?.color ?: PoolBullet, RoundedCornerShape(2.dp)),
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    currentBook?.title ?: "选择清单",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GoaldayDesign.adaptiveInkPrimary,
                    maxLines = 1,
                    modifier = Modifier.widthIn(max = 200.dp),
                )
                Icon(
                    Icons.Filled.ExpandMore,
                    contentDescription = "切换专题",
                    tint = GoaldayDesign.adaptiveInkMuted,
                    modifier = Modifier.size(18.dp),
                )
            }
            }
            Spacer(Modifier.height(10.dp))
            LazyColumn(Modifier.weight(1f)) {
                // 右栏 = 当前清单条目全集（对照原版周 Tab：未完成=橙方块，
                // 完成=橙勾框+灰字删除线；点击/拖拽条目 → 排入左侧选中日期）
                val targetPage = currentBook?.pages?.filterIsInstance<TargetPage>()?.firstOrNull()
                val listItems = if (targetPage != null && currentBook != null) {
                    (targetPage.items + diaryStore.customPageItems(currentBook.id, targetPage.title)).distinct()
                } else {
                    uiState.todayPlanItems
                }
                items(listItems, key = { it }) { poolItem ->
                        val itemChecked = targetPage != null && viewModel.isChecked(targetPage.title, poolItem)
                        val isCustomPoolItem = targetPage != null && currentBook != null &&
                            diaryStore.customPageItems(currentBook.id, targetPage.title).contains(poolItem)
                        val poolInner: @Composable RowScope.() -> Unit = {
                            if (itemChecked) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 3.dp)
                                        .size(17.dp)
                                        .border(1.6.dp, Color.Transparent, RoundedCornerShape(4.dp))
                                        .background(GoaldayDesign.Pink, RoundedCornerShape(4.dp)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text("✓", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 9.dp)
                                        .size(5.dp)
                                        .background(currentBook?.color ?: PoolBullet),
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            // 对照原版周视图：条目多行换行（17sp 字/23sp 行高，条目间距 7dp）
                            Text(
                                poolItem,
                                fontSize = 17.sp,
                                lineHeight = 23.sp,
                                color = if (itemChecked) GoaldayDesign.adaptiveInkMuted else GoaldayDesign.adaptiveInkPrimary,
                                textDecoration = if (itemChecked) TextDecoration.LineThrough else TextDecoration.None,
                            )
                        }
                        val addPoolToSchedule = {
                            InteractionFeedback.click(context)
                            viewModel.addScheduleFromHandbook(
                                poolItem,
                                selectedDate.monthValue,
                                selectedDate.dayOfMonth,
                            )
                        }
                        // 拖拽+点击手势与删除按钮完全分离：
                        // - 左侧内容区域：combinedClickable处理点击(排期) + pointerInput处理长按拖拽
                        // - 右侧删除按钮：独立clickable，不被父容器拦截
                        val dragContext = context
                        val dragEnable = true
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .alpha(if (draggingItem == poolItem) 0.35f else 1f)
                                // 对照原版真机：条目间距=行高23sp+上下6.5dp（单行条目间距95px、两行154px）
                                .padding(horizontal = 14.dp, vertical = 6.5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .onGloballyPositioned { poolItemOrigins[poolItem] = it.boundsInWindow().topLeft }
                                    .pointerInput(dragEnable) {
                                        detectDragGesturesAfterLongPress(
                                            onDragStart = { touch ->
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
                                                    viewModel.addScheduleFromHandbook(item, target.monthValue, target.dayOfMonth)
                                                }
                                                draggingItem = null
                                                dropTarget = null
                                            },
                                            onDragCancel = {
                                                draggingItem = null
                                                dropTarget = null
                                            },
                                        )
                                    }
                                    .clickable(onClick = addPoolToSchedule),
                            ) {
                                // Top 对齐+padding 使圆点/勾选中心落在首行字心（行高23sp）
                                Row(verticalAlignment = Alignment.Top) {
                                    poolInner()
                                }
                            }
                            if (isCustomPoolItem) {
                                Spacer(Modifier.width(8.dp))
                                Box(
                                    Modifier
                                        .size(36.dp)
                                        .background(Color(0xFFED8888), RoundedCornerShape(8.dp))
                                        .clickable {
                                            InteractionFeedback.click(dragContext)
                                            viewModel.removeListPageItem(poolItem)
                                        },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Image(
                                        painter = painterResource(com.bf410.goaldaylocal.R.drawable.plan_trash),
                                        contentDescription = "删除",
                                        modifier = Modifier.width(20.dp),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                                    )
                                }
                            }
                        }
                    }
                if (listItems.isEmpty()) {
                    item {
                        Text(
                            "点击右上专题切换清单，点条目即可排入左侧选中日期",
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            color = GoaldayDesign.adaptiveInkMuted,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        )
                    }
                }
                // 尾部新增条目（对照原版：清单尾部直接输入添加）
                item {
                    var addItemText by remember { mutableStateOf("") }
                    BasicTextField(
                        value = addItemText,
                        onValueChange = { addItemText = it },
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 16.sp, color = GoaldayDesign.adaptiveInkPrimary),
                        cursorBrush = SolidColor(TodayCoral),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 9.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (addItemText.isNotBlank()) {
                                    InteractionFeedback.click(context)
                                    viewModel.addListPageItem(addItemText)
                                }
                                addItemText = ""
                            },
                        ),
                        decorationBox = { inner ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "+",
                                    fontSize = 16.sp,
                                    color = GoaldayDesign.adaptiveInkMuted,
                                    modifier = Modifier.padding(end = 10.dp),
                                )
                                Box {
                                    if (addItemText.isEmpty()) {
                                        Text(
                                            "添加条目到当前清单",
                                            fontSize = 15.sp,
                                            color = GoaldayDesign.adaptiveInkMuted.copy(alpha = 0.7f),
                                            maxLines = 1,
                                        )
                                    }
                                    inner()
                                }
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
                    Text(
                        if (poolCollapsed) "‹" else "›",
                        fontSize = 17.sp,
                        color = GoaldayDesign.adaptiveInkPrimary,
                    )
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
                    Text("‹", fontSize = 17.sp, color = GoaldayDesign.adaptiveInkPrimary)
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
 *  （对照 onPageSelected 500ms 后 setCurrentItem(2,false) + 日期事件；Compose 落定即完成，无需延迟）；
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
) {
    var anchorDate by remember { mutableStateOf(selectedDate) }
    val pagerState = rememberPagerState(initialPage = DIARY_PAGER_CENTER, pageCount = { DIARY_PAGER_SIZE })
    var editorFocused by remember { mutableStateOf(false) }
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }.collect { settled ->
            if (settled != DIARY_PAGER_CENTER) {
                anchorDate = diaryPagerDate(anchorDate, settled)
                onSelectDate(anchorDate)
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
        )
    }
}

@Composable
private fun RecordDiaryView(
    selectedDate: LocalDate,
    entries: List<ScheduleEntry>,
    onEditorFocusChanged: (Boolean) -> Unit = {},
) {
    val store = remember { LocalStateStore(MMKV.defaultMMKV()) }
    val prompt = remember(selectedDate) {
        if (JOURNAL_PROMPTS.isEmpty()) "" else JOURNAL_PROMPTS[Math.floorMod(selectedDate.toEpochDay().toInt(), JOURNAL_PROMPTS.size)]
    }
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

    val imagePicker = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) {
            // 复制进应用私有目录，保证长期可读（对照原版本地图片方案）
            runCatching {
                val dir = java.io.File(context.filesDir, "diary_images").apply { mkdirs() }
                val file = java.io.File(dir, "d" + selectedDate.toEpochDay() + "_" + System.currentTimeMillis() + ".jpg")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    file.outputStream().use { output -> input.copyTo(output) }
                }
                if (file.exists() && file.length() > 0) {
                    imagePaths = imagePaths + file.absolutePath
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
            if (prompt.isNotBlank()) {
                Text(
                    prompt,
                    fontSize = 16.sp,
                    lineHeight = 22.sp,
                    color = GoaldayDesign.adaptiveInkMuted,
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
        // 键盘工具栏（对照原版 fragment_diary 底栏：bg #E5DAD4 高约 46dp，插图图标 25dp，仅编辑时出现）
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
                    val done = page?.items?.count { store.isChecked(book.id, page.title, it) } ?: 0
                    val total = page?.items?.size ?: 0
                    // 对照原版真机：当前选中清单的圆点为方块，其余为圆点
                    val isSelectedBook = uiState.books.getOrNull(uiState.selectedBookIndex)?.id == book.id
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
                                    SwipeAction("删除", Color(0xFFED8888), Icons.Filled.Delete, iconRes = com.bf410.goaldaylocal.R.drawable.plan_trash) {
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
                                .clickable { onExpandBook(book.id) }
                                .padding(start = 13.dp, end = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                Modifier
                                    .size(10.dp)
                                    .background(
                                        book.color,
                                        if (isSelectedBook) RoundedCornerShape(2.dp) else CircleShape,
                                    ),
                            )
                            Spacer(Modifier.width(16.dp))
                            Text(
                                book.title,
                                fontSize = 16.sp,
                                color = GoaldayDesign.adaptiveInkPrimary,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
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
                    // 对照原版 iv_add：src=plan_add 原图
                    Image(
                        painter = androidx.compose.ui.res.painterResource(com.bf410.goaldaylocal.R.drawable.plan_add),
                        contentDescription = "新建清单",
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
                    // 对照原版 iv_tip：src=plan_tip 原图 tint 白
                    Image(
                        painter = androidx.compose.ui.res.painterResource(com.bf410.goaldaylocal.R.drawable.plan_tip),
                        contentDescription = "灵感",
                        colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color.White),
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }

        pendingDeleteBook?.let { book ->
            AlertDialog(
                onDismissRequest = { pendingDeleteBook = null },
                title = { Text("删除清单", fontSize = 17.sp, fontWeight = FontWeight.SemiBold) },
                text = { Text("确定删除「${book.title}」吗？其中的条目与记录会一并删除，无法恢复。") },
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

        if (showAddSheet) {
            TopicAddSheet(
                onCreate = { title, color ->
                    InteractionFeedback.click(listContext)
                    viewModel.createCustomBook(title, "", color)
                    showAddSheet = false
                },
                onDismiss = { showAddSheet = false },
            )
        }
    }
}

/** 清单详情：‹ 返回 + 色块 + 标题 + ···；编号条目 + 珊瑚描边方形勾选框 + 虚线分隔（对照原版截图） */
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
    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MainTabBarBg)
                .statusBarsPadding()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "‹",
                fontSize = 22.sp,
                color = GoaldayDesign.adaptiveInkPrimary,
                modifier = Modifier
                    .clickable { onBack() }
                    .padding(end = 14.dp),
            )
            Box(
                Modifier
                    .size(11.dp)
                    .background(PoolBullet, RoundedCornerShape(2.dp)),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                book.title,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = GoaldayDesign.adaptiveInkPrimary,
                modifier = Modifier.weight(1f),
                maxLines = 1,
            )
            Text("···", fontSize = 16.sp, color = GoaldayDesign.adaptiveInkPrimary)
        }
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
        ) {
            // key 带上 revision：勾选写入的是 MMKV（非 Compose 观测状态），
            // revision 变化时换 key 强制重建 item，重读 isChecked 刷新勾选框
            itemsIndexed(page?.items ?: emptyList(), key = { _, item -> "$revision-$item" }) { index, item ->
                val detailContext = LocalContext.current
                val checked = store.isChecked(book.id, page?.title ?: "", item)
                val checkedDateText = if (checked) store.checkedDate(book.id, page?.title ?: "", item) else ""
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                InteractionFeedback.click(detailContext)
                                InteractionFeedback.haptic(detailContext, 30L)
                                store.setChecked(book.id, page?.title ?: "", item, !checked)
                                // 完成日期戳（对照原版勾选后行下显示的日期 chip）
                                store.setCheckedDate(
                                    book.id,
                                    page?.title ?: "",
                                    item,
                                    if (!checked) LocalDate.now().toString() else "",
                                )
                                // 联动任务池：勾选进池（可拖去排期），取消勾选移出（同一 (bookId, 页题) 存储）
                                if (page != null) {
                                    val pool = store.todayPlanItems(book.id, page.title)
                                    store.saveTodayPlanItems(
                                        book.id,
                                        page.title,
                                        if (!checked) (pool + item).distinct() else pool.filterNot { it == item },
                                    )
                                    viewModel.refreshSchedulePreview()
                                }
                                onToggle()
                            }
                            .padding(vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(17.dp)
                                .border(1.6.dp, if (checked) Color.Transparent else PoolBullet, RoundedCornerShape(4.dp))
                                .background(if (checked) GoaldayDesign.Pink else Color.Transparent, RoundedCornerShape(4.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (checked) {
                                Text("✓", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                        Spacer(Modifier.width(14.dp))
                        Text(
                            "${index + 1}  $item",
                            fontSize = 15.sp,
                            lineHeight = 20.sp,
                            color = if (checked) GoaldayDesign.adaptiveInkMuted else GoaldayDesign.adaptiveInkPrimary,
                        )
                    }
                    if (checked && checkedDateText.isNotBlank()) {
                        // 完成日期戳：原版勾选后行下弹出的橙色圆角日期 chip
                        Box(
                            modifier = Modifier
                                .padding(start = 31.dp, bottom = 8.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(GoaldayDesign.Pink.copy(alpha = 0.75f))
                                .padding(horizontal = 10.dp, vertical = 3.dp),
                        ) {
                            Text(
                                checkedDateText,
                                fontSize = 12.sp,
                                color = Color.White,
                            )
                        }
                    }
                }
                Box(
                    Modifier
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
    }
}

// endregion

// region 新建清单弹层（对照原版 PlanAddBottomDialog：名称 + 颜色选择 + 取消/完成）

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopicAddSheet(
    onCreate: (String, Color) -> Unit,
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
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null,
        containerColor = if (dark) Color(0xFF2C2722) else Color.White,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
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
                        .clickable(enabled = name.trim().isNotBlank()) { onCreate(name.trim(), selected) }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EntryEditSheet(
    entry: ScheduleEntry,
    viewModel: BookViewModel,
    allEntries: List<ScheduleEntry>,
    onDismiss: () -> Unit,
) {
    val entryDate = remember(entry.id) { LocalDate.of(entry.year, entry.month, entry.day) }
    val weekDays = remember(entry.id) {
        (0..6).map { entryDate.with(DayOfWeek.MONDAY).plusDays(it.toLong()) }
    }
    var title by remember(entry.id) { mutableStateOf(entry.title) }
    var timeText by remember(entry.id) { mutableStateOf(entry.timeText) }
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
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
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
                                viewModel.moveScheduleDayFromHandbook(entry.id, day.monthValue, day.dayOfMonth)
                                onDismiss()
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
                // 删除（红字，对照原版危险操作）
                Text(
                    "删除",
                    fontSize = 15.sp,
                    color = TodayCoral,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clickable {
                            InteractionFeedback.click(sheetContext)
                            InteractionFeedback.haptic(sheetContext)
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
            val entries = uiState.schedulePreviewEntries
                .filter { it.year == date.year && it.month == date.monthValue && it.day == date.dayOfMonth }
                .sortedWith(compareBy({ it.timeText }, { it.id }))
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
                Row(
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (LocalGoaldayDarkMode.current) Color(0xFF2C2722) else Color.White)
                        .border(0.7.dp, MainTabDivider.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .clickable {
                            val next = (uiState.selectedBookIndex + 1) % uiState.books.size.coerceAtLeast(1)
                            viewModel.openBook(next)
                        }
                        .padding(horizontal = 10.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        Modifier
                            .size(12.dp)
                            .background(PoolBullet, RoundedCornerShape(3.dp)),
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "选择清单",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GoaldayDesign.adaptiveInkPrimary,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                    )
                    Icon(
                        Icons.Filled.ExpandMore,
                        contentDescription = "切换清单",
                        tint = GoaldayDesign.adaptiveInkMuted,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Spacer(Modifier.height(10.dp))
                LazyColumn(Modifier.weight(1f)) {
                    val targetPage = currentBook?.pages?.filterIsInstance<TargetPage>()?.firstOrNull()
                    val listItems = if (targetPage != null && currentBook != null) {
                        (targetPage.items + monthStore.customPageItems(currentBook.id, targetPage.title)).distinct()
                    } else {
                        emptyList()
                    }
                    items(listItems, key = { it }) { item ->
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
                                        .background(GoaldayDesign.Pink, RoundedCornerShape(4.dp)),
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
                            Text(
                                item,
                                fontSize = 16.sp,
                                lineHeight = 20.sp,
                                color = if (itemChecked) GoaldayDesign.adaptiveInkMuted else GoaldayDesign.adaptiveInkPrimary,
                                textDecoration = if (itemChecked) TextDecoration.LineThrough else TextDecoration.None,
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
                        Text("›", fontSize = 17.sp, color = GoaldayDesign.adaptiveInkPrimary)
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
                    Text("‹", fontSize = 17.sp, color = GoaldayDesign.adaptiveInkPrimary)
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
        containerColor = Color.White,
        scrimColor = Color.Black.copy(alpha = 0.58f),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
    ) {
        val today = rememberToday()
        var monthAnchor by remember { mutableStateOf(YearMonth.from(selectedDate)) }
        val weekFields = WeekFields.ISO

        Column(Modifier.padding(horizontal = 16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
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
                        color = Color(0xFFC9C9C9),
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
                                color = Color(0xFFC9C9C9),
                            )
                        }
                    }
                }
                Box(
                    Modifier
                        .width(1.dp)
                        .height(51.dp * 6)
                        .background(Color(0xFFE3E3E3)),
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
                                                color = if (isToday) Color.White else Color(0xFFBDBDBD),
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
