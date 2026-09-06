package com.bf410.goaldaylocal.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bf410.goaldaylocal.data.LocalStateStore
import com.bf410.goaldaylocal.data.ScheduleEntry
import com.bf410.goaldaylocal.data.TargetPage
import com.bf410.goaldaylocal.data.TopicBook
import com.bf410.goaldaylocal.ui.book.BookUiState
import com.bf410.goaldaylocal.ui.book.BookViewModel
import com.bf410.goaldaylocal.ui.replica.GoaldayDesign
import com.tencent.mmkv.MMKV
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.WeekFields
import java.util.Locale

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
private val MainTabBarBg = Color(0xFFE5DAD4)
private val MainContentBg = Color(0xFFFDFAF6)
private val MainTabDivider = Color(0xFFC5BBB6)
private val WeekBandBg = Color(0xFFFEECEC)
private val TodayCoral = Color(0xFFF66061)
private val TodayBlack = Color(0xFF1E1E1E)
private val PoolBullet = Color(0xFFF2C0A5)
private val EntryCircle = Color(0xFF3A3A3A)

private enum class MainSubTab(val label: String) {
    WEEK("周"),
    RECORD("记录"),
    LIST("清单"),
}

private const val DIARY_BOOK_ID = "diary"

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
    onOpenBook: () -> Unit,
    onOpenInspiration: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val uiState by bookViewModel.uiState.collectAsState()
    var subTabIndex by rememberSaveable { mutableIntStateOf(MainSubTab.WEEK.ordinal) }
    var selectedDate by rememberSaveable { mutableStateOf(LocalDate.now()) }
    var showWeekPicker by remember { mutableStateOf(false) }
    // 行内编辑中的日期（任意一天可编辑，对照原版）；非 null 时顶栏显示「完成」
    var editingDate by remember { mutableStateOf<LocalDate?>(null) }

    LaunchedEffect(Unit) { bookViewModel.refreshSchedulePreview() }

    val currentSubTab = MainSubTab.entries[subTabIndex.coerceIn(0, MainSubTab.entries.lastIndex)]

    Column(Modifier.fillMaxSize().background(MainContentBg)) {
        OriginalTopTabBar(
            selected = currentSubTab,
            selectedDate = selectedDate,
            editing = editingDate != null,
            onDone = { editingDate = null },
            onWeekClick = { showWeekPicker = true },
            onSelect = {
                editingDate = null
                subTabIndex = it.ordinal
            },
        )
        Box(Modifier.fillMaxSize()) {
            when (currentSubTab) {
                MainSubTab.WEEK -> WeekScheduleView(
                    uiState = uiState,
                    viewModel = bookViewModel,
                    selectedDate = selectedDate,
                    editingDate = editingDate,
                    onStartEdit = { editingDate = it },
                    onFinishEdit = { editingDate = null },
                    onSelectDate = { selectedDate = it },
                )
                MainSubTab.RECORD -> RecordDiaryView(
                    selectedDate = selectedDate,
                    entries = uiState.schedulePreviewEntries,
                )
                MainSubTab.LIST -> TopicListView(
                    uiState = uiState,
                    onOpenBookShelf = onOpenBook,
                    onOpenInspiration = onOpenInspiration,
                    onOpenSettings = onOpenSettings,
                )
            }
        }
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
}

/** 顶部文字 Tab：N周 ▼ | 记录 | 清单；行内编辑时只显示右上角「完成」 */
@Composable
private fun OriginalTopTabBar(
    selected: MainSubTab,
    selectedDate: LocalDate,
    editing: Boolean,
    onDone: () -> Unit,
    onWeekClick: () -> Unit,
    onSelect: (MainSubTab) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MainTabBarBg)
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
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
            return@Row
        }
        val weekNum = selectedDate.get(WeekFields.ISO.weekOfWeekBasedYear())
        Row(
            modifier = Modifier.clickable { onWeekClick() },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                "${weekNum}周",
                fontSize = 18.sp,
                fontWeight = if (selected == MainSubTab.WEEK) FontWeight.Bold else FontWeight.Normal,
                color = if (selected == MainSubTab.WEEK) GoaldayDesign.InkPrimary else GoaldayDesign.InkMuted,
            )
            Icon(
                Icons.Filled.KeyboardArrowDown,
                contentDescription = "选择周",
                tint = GoaldayDesign.InkPrimary,
                modifier = Modifier.size(20.dp),
            )
        }
        TabDividerText()
        TabLabel(
            text = if (selected == MainSubTab.RECORD) "${selectedDate.monthValue}月${selectedDate.dayOfMonth}日" else "记录",
            selected = selected == MainSubTab.RECORD,
        ) { onSelect(MainSubTab.RECORD) }
        TabDividerText()
        TabLabel("清单", selected == MainSubTab.LIST) { onSelect(MainSubTab.LIST) }
    }
}

@Composable
private fun TabDividerText() {
    Text("｜", fontSize = 15.sp, color = MainTabDivider)
}

@Composable
private fun TabLabel(text: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        text,
        fontSize = 18.sp,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        color = if (selected) GoaldayDesign.InkPrimary else GoaldayDesign.InkMuted,
        modifier = Modifier.clickable(onClick = onClick),
    )
}

// region 周 Tab —— 左侧周日期列（任意天行内编辑）+ 右侧任务池

@Composable
private fun WeekScheduleView(
    uiState: BookUiState,
    viewModel: BookViewModel,
    selectedDate: LocalDate,
    editingDate: LocalDate?,
    onStartEdit: (LocalDate) -> Unit,
    onFinishEdit: () -> Unit,
    onSelectDate: (LocalDate) -> Unit,
) {
    val today = LocalDate.now()
    val monday = selectedDate.with(DayOfWeek.MONDAY)
    val weekDays = remember(monday) { (0..6).map { monday.plusDays(it.toLong()) } }
    var quickInput by remember(editingDate) { mutableStateOf("") }
    val listState = rememberLazyListState()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(selectedDate) {
        val idx = weekDays.indexOfFirst { it == selectedDate }.coerceAtLeast(0)
        runCatching { listState.animateScrollToItem(idx) }
    }
    LaunchedEffect(editingDate) {
        if (editingDate != null) runCatching { focusRequester.requestFocus() }
    }

    Row(Modifier.fillMaxSize()) {
        // 左侧：周日期列（今日黑底圆角白字；任意一天点空白进入行内新增）
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1.15f)
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
                        .clickable {
                            onSelectDate(date)
                            onStartEdit(date)
                        }
                        .drawBehind {
                            val stroke = 0.6.dp.toPx()
                            drawLine(
                                color = MainTabDivider.copy(alpha = 0.4f),
                                start = Offset(0f, size.height - stroke / 2),
                                end = Offset(size.width, size.height - stroke / 2),
                                strokeWidth = stroke,
                            )
                        }
                        .padding(horizontal = 10.dp, vertical = 10.dp),
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Column(
                            modifier = if (isToday) {
                                Modifier
                                    .width(62.dp)
                                    .background(TodayBlack, RoundedCornerShape(10.dp))
                                    .padding(vertical = 8.dp)
                            } else {
                                Modifier.width(62.dp).padding(vertical = 8.dp)
                            },
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                date.dayOfMonth.toString(),
                                fontSize = 20.sp,
                                lineHeight = 22.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isToday) Color.White else GoaldayDesign.InkPrimary,
                            )
                            Text(
                                "—",
                                fontSize = 11.sp,
                                lineHeight = 12.sp,
                                color = if (isToday) Color.White.copy(alpha = 0.7f) else MainTabDivider,
                            )
                            Text(
                                weekdayName(date),
                                fontSize = 11.sp,
                                lineHeight = 12.sp,
                                color = if (isToday) Color.White.copy(alpha = 0.85f) else GoaldayDesign.InkMuted,
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            entries.forEach { entry ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    // 圆形勾选框：○ 未完成 / 黑底白勾 完成（对照原版）
                                    Box(
                                        modifier = Modifier
                                            .size(19.dp)
                                            .border(
                                                1.8.dp,
                                                if (entry.completed) Color.Transparent else EntryCircle,
                                                CircleShape,
                                            )
                                            .background(
                                                if (entry.completed) EntryCircle else Color.Transparent,
                                                CircleShape,
                                            )
                                            .clickable {
                                                viewModel.toggleScheduleCompletedFromHandbook(entry.id)
                                            },
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        if (entry.completed) {
                                            Text("✓", fontSize = 11.sp, color = Color.White)
                                        }
                                    }
                                    Spacer(Modifier.width(10.dp))
                                    Text(
                                        entry.title,
                                        fontSize = 15.sp,
                                        lineHeight = 19.sp,
                                        color = GoaldayDesign.InkPrimary,
                                        textDecoration = if (entry.completed) TextDecoration.LineThrough else TextDecoration.None,
                                        maxLines = 2,
                                    )
                                }
                            }
                            if (isEditing) {
                                BasicTextField(
                                    value = quickInput,
                                    onValueChange = { quickInput = it },
                                    singleLine = true,
                                    textStyle = TextStyle(fontSize = 15.sp, color = GoaldayDesign.InkPrimary),
                                    cursorBrush = SolidColor(TodayCoral),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp)
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
                                                        color = GoaldayDesign.InkMuted.copy(alpha = 0.75f),
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
            }
            item { Spacer(Modifier.height(90.dp)) }
        }

        // 中缝分隔线（对照原版 #C5BBB6 细线）
        Box(
            Modifier
                .width(0.7.dp)
                .fillMaxHeight()
                .background(MainTabDivider.copy(alpha = 0.5f)),
        )

        // 右侧：任务池（专题 chip + 橙色方块条目）
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
                    .background(Color(0xFFFBF7F1))
                    .border(0.7.dp, MainTabDivider.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
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
                        .background(currentBook?.color ?: PoolBullet, RoundedCornerShape(3.dp)),
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    currentBook?.title ?: "选择清单",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GoaldayDesign.InkPrimary,
                    maxLines = 1,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    Icons.Filled.ExpandMore,
                    contentDescription = "切换专题",
                    tint = GoaldayDesign.InkMuted,
                    modifier = Modifier.size(18.dp),
                )
            }
            Spacer(Modifier.height(10.dp))
            LazyColumn(Modifier.weight(1f)) {
                items(uiState.todayPlanItems, key = { it }) { poolItem ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.addScheduleFromHandbook(
                                    poolItem,
                                    selectedDate.monthValue,
                                    selectedDate.dayOfMonth,
                                )
                            }
                            .padding(horizontal = 14.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Box(
                            Modifier
                                .padding(top = 5.dp)
                                .size(7.dp)
                                .background(PoolBullet),
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            poolItem,
                            fontSize = 16.sp,
                            lineHeight = 20.sp,
                            color = GoaldayDesign.InkPrimary,
                        )
                    }
                }
                if (uiState.todayPlanItems.isEmpty()) {
                    item {
                        Text(
                            "点击右上专题切换清单，点条目即可排入左侧选中日期",
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            color = GoaldayDesign.InkMuted,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        )
                    }
                }
                item { Spacer(Modifier.height(90.dp)) }
            }
        }
    }
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

@Composable
private fun RecordDiaryView(
    selectedDate: LocalDate,
    entries: List<ScheduleEntry>,
) {
    val store = remember { LocalStateStore(MMKV.defaultMMKV()) }
    val prompt = remember(selectedDate) {
        if (JOURNAL_PROMPTS.isEmpty()) "" else JOURNAL_PROMPTS[Math.floorMod(selectedDate.toEpochDay().toInt(), JOURNAL_PROMPTS.size)]
    }
    // 编辑器只展示用户正文；「今日完成」等结构化段落由系统维护
    var text by remember(selectedDate) {
        mutableStateOf(diaryUserText(store, selectedDate))
    }

    Column(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            if (prompt.isNotBlank()) {
                Text(
                    prompt,
                    fontSize = 16.sp,
                    lineHeight = 22.sp,
                    color = GoaldayDesign.InkMuted,
                )
                Spacer(Modifier.height(14.dp))
            }
            BasicTextField(
                value = text,
                onValueChange = {
                    text = it
                    store.setDiaryText(DIARY_BOOK_ID, selectedDate.toString(), buildStructuredDiary(selectedDate, entries, it))
                },
                textStyle = TextStyle(fontSize = 16.sp, lineHeight = 24.sp, color = GoaldayDesign.InkPrimary),
                cursorBrush = SolidColor(TodayCoral),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(120.dp))
        }
    }
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
private fun buildStructuredDiary(date: LocalDate, entries: List<ScheduleEntry>, userText: String): String {
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
    onOpenBookShelf: () -> Unit,
    onOpenInspiration: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val store = remember { LocalStateStore(MMKV.defaultMMKV()) }
    var expandedBookId by rememberSaveable { mutableStateOf<String?>(null) }
    var revision by remember { mutableIntStateOf(0) }

    Box(Modifier.fillMaxSize()) {
        if (expandedBookId == null) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        Icon(
                            Icons.Filled.Settings,
                            contentDescription = "设置",
                            tint = GoaldayDesign.InkMuted,
                            modifier = Modifier
                                .size(22.dp)
                                .clickable { onOpenSettings() },
                        )
                    }
                }
                items(uiState.books, key = { it.id }) { book ->
                    val page = book.pages.filterIsInstance<TargetPage>().firstOrNull()
                    val done = page?.items?.count { store.isChecked(book.id, page.title, it) } ?: 0
                    val total = page?.items?.size ?: 0
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFFBF7F1))
                            .clickable { expandedBookId = book.id }
                            .padding(horizontal = 14.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            Modifier
                                .size(12.dp)
                                .background(book.color, RoundedCornerShape(3.dp)),
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            book.title,
                            fontSize = 16.sp,
                            color = GoaldayDesign.InkPrimary,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                        )
                        Text(
                            "$done/$total",
                            fontSize = 14.sp,
                            color = GoaldayDesign.InkMuted,
                        )
                    }
                }
            }
        } else {
            val book = uiState.books.firstOrNull { it.id == expandedBookId }
            if (book == null) {
                expandedBookId = null
            } else {
                TopicDetailSimple(
                    book = book,
                    store = store,
                    revision = revision,
                    onToggle = { revision++ },
                    onBack = { expandedBookId = null },
                )
            }
        }

        // FAB：+ 与 💡，只在清单 Tab 显示（对照原版截图）
        if (expandedBookId == null) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(Color(0xFFEFE7DC), CircleShape)
                        .clickable { onOpenBookShelf() },
                    contentAlignment = Alignment.Center,
                ) {
                    Text("+", fontSize = 26.sp, color = GoaldayDesign.InkPrimary, fontWeight = FontWeight.Light)
                }
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(TodayBlack, CircleShape)
                        .clickable { onOpenInspiration() },
                    contentAlignment = Alignment.Center,
                ) {
                    Text("💡", fontSize = 20.sp)
                }
            }
        }
    }
}

/** 清单详情：‹ 返回 + 色块 + 标题 + ···；编号条目 + 珊瑚描边方形勾选框 + 虚线分隔（对照原版截图） */
@Composable
private fun TopicDetailSimple(
    book: TopicBook,
    store: LocalStateStore,
    revision: Int,
    onToggle: () -> Unit,
    onBack: () -> Unit,
) {
    val page = book.pages.filterIsInstance<TargetPage>().firstOrNull()
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
                color = GoaldayDesign.InkPrimary,
                modifier = Modifier
                    .clickable { onBack() }
                    .padding(end = 14.dp),
            )
            Box(
                Modifier
                    .size(11.dp)
                    .background(book.color, RoundedCornerShape(2.dp)),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                book.title,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = GoaldayDesign.InkPrimary,
                modifier = Modifier.weight(1f),
                maxLines = 1,
            )
            Text("···", fontSize = 16.sp, color = GoaldayDesign.InkPrimary)
        }
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
        ) {
            itemsIndexed(page?.items ?: emptyList(), key = { _, item -> item }) { index, item ->
                val checked = store.isChecked(book.id, page?.title ?: "", item)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            store.setChecked(book.id, page?.title ?: "", item, !checked)
                            onToggle()
                        }
                        .padding(vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(17.dp)
                            .border(1.6.dp, PoolBullet, RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (checked) {
                            Text("✓", fontSize = 11.sp, color = PoolBullet)
                        }
                    }
                    Spacer(Modifier.width(14.dp))
                    Text(
                        "${index + 1}  $item",
                        fontSize = 15.sp,
                        lineHeight = 20.sp,
                        color = GoaldayDesign.InkPrimary,
                        textDecoration = if (checked) TextDecoration.LineThrough else TextDecoration.None,
                    )
                }
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .drawBehind {
                            drawLine(
                                color = MainTabDivider.copy(alpha = 0.55f),
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

// region 周选择器底部弹层（对照原版 dialog_calendar：周号列 + 周高亮带 + 今日珊瑚红）

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WeekPickerSheet(
    selectedDate: LocalDate,
    onPick: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
    ) {
        val today = LocalDate.now()
        var monthAnchor by remember { mutableStateOf(YearMonth.from(selectedDate)) }
        val weekFields = WeekFields.ISO

        Column(Modifier.padding(horizontal = 16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "${monthAnchor.monthValue}月 ${monthAnchor.year}",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GoaldayDesign.InkPrimary,
                )
                Icon(
                    Icons.Filled.KeyboardArrowDown,
                    contentDescription = "下个月",
                    tint = GoaldayDesign.InkPrimary,
                    modifier = Modifier
                        .size(24.dp)
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
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth()) {
                Spacer(Modifier.width(46.dp))
                listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach {
                    Text(
                        it,
                        fontSize = 13.sp,
                        color = GoaldayDesign.InkMuted,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            val first = monthAnchor.atDay(1)
            val firstMonday = first.with(DayOfWeek.MONDAY).let {
                if (it.isAfter(first)) it.minusWeeks(1) else it
            }
            (0..5).map { firstMonday.plusWeeks(it.toLong()) }.forEach { weekStart ->
                val days = (0..6).map { weekStart.plusDays(it.toLong()) }
                val isCurrentWeek =
                    !selectedDate.isBefore(weekStart) && selectedDate.isBefore(weekStart.plusWeeks(1))
                val weekNum = weekStart.get(weekFields.weekOfWeekBasedYear())
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isCurrentWeek) WeekBandBg else Color.Transparent),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier.width(46.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            weekNum.toString(),
                            fontSize = 15.sp,
                            fontWeight = if (isCurrentWeek) FontWeight.Bold else FontWeight.Normal,
                            color = if (isCurrentWeek) GoaldayDesign.InkPrimary else GoaldayDesign.InkMuted,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isCurrentWeek) Color.White else Color.Transparent)
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                        )
                    }
                    days.forEach { date ->
                        val inMonth = date.monthValue == monthAnchor.monthValue
                        val isToday = date == today
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    onPick(date)
                                    onDismiss()
                                }
                                .padding(vertical = 5.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(
                                        if (isToday) TodayCoral else Color.Transparent,
                                        RoundedCornerShape(10.dp),
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    date.dayOfMonth.toString(),
                                    fontSize = 16.sp,
                                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                    color = when {
                                        isToday -> Color.White
                                        !inMonth -> GoaldayDesign.InkMuted.copy(alpha = 0.55f)
                                        else -> GoaldayDesign.InkPrimary
                                    },
                                )
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
