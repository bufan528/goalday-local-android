package com.bf410.goaldaylocal.ui.book

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.layout.ContentScale
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bf410.goaldaylocal.R
import com.bf410.goaldaylocal.data.DiaryPage
import com.bf410.goaldaylocal.data.PlanItem
import com.bf410.goaldaylocal.data.PlanPage
import com.bf410.goaldaylocal.data.ScheduleEntry
import com.bf410.goaldaylocal.data.SchedulePage
import com.bf410.goaldaylocal.data.TargetItemMeta
import com.bf410.goaldaylocal.data.TargetPage
import com.bf410.goaldaylocal.ui.replica.GoaldayDesign
import java.time.LocalDate
import java.time.YearMonth

// 逆向 item_schedule_item_in_book.xml：pt 是解码器对 dip 的误标，直接使用原始 dip 值
private val InBookScheduleColumnPaddingVertical = 3.5.dp
private val InBookScheduleSlotHeight = 12.dp

// ============================================================
// 书内静态预览页面 — 对照逆向 fragment_*_inbook.xml + item_*_inbook.xml
// 原版书内页面使用 NoTouchConstraintLayout (clickable=false)，
// 大部分控件 visibility=GONE，只保留最简内容列表。
//
// 重要：aapt2 dump 验证（T5.xml = item_schedule_item_in_book.xml）：
// paddingVertical=3.5pt、layout_height=12.0pt 中的 pt 是解码器对 dip 的误标
// layout_width=24.5dp、textSize=9dp/6dp 为 dp（非 pt）
// 所有 pt 值直接作为 dp 使用，不做 ×2.222 换算。
// ============================================================

// 勾选框：使用逆向资源中的原版图标（ic_box_empty / ic_box_full）
// 对照 item_schedule_item_in_book.xml: 9dp 勾选框
// 对照 item_target_detail.xml: wrap_content (约 20dp)
@Composable
private fun InBookCheckbox(
    checked: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    size: Int = 9,
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .pointerInput(Unit) {
                detectTapGestures(onTap = { onToggle() })
            },
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(if (checked) R.drawable.ic_box_full else R.drawable.ic_box_empty),
            contentDescription = if (checked) "已完成" else "未完成",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit,
        )
    }
}

// region 日程页 (fragment_schedule_inbook.xml + item_schedule_item_in_book.xml)
// 结构：RecyclerView 垂直列表，每行一天
// 每天结构：24.5dp 日期列 + 2列×3行目标槽
// 日期列：9sp日期 + 9sp分隔线"—" + 6sp周几
// 目标槽：style_schedule_day_form 文字（20dip），槽高 12dip，列上下内边距 3.5dip
// 所有 pt 值直接作为 dp 使用，dip/dp 保持 1:1。
@Composable
internal fun InBookSchedulePreview(
    modifier: Modifier,
    page: SchedulePage,
    pageIndex: Int,
    pageCount: Int,
    schedulePreviewEntries: List<ScheduleEntry>,
    isChecked: (String, String) -> Boolean,
    tint: Color,
    turnProgress: Float,
    turnDirection: TurnDirection?,
    handbookMode: Boolean = false,
    // 书内双页展开模式：传入当周周一，则渲染 Mon-Sun 共 7 行
    weekStartDate: java.time.LocalDate? = null,
) {
    // HANDBOOK 模式下外部已应用 turningPageTransform 3D 翻页，内容应贴在页面上随页转动，
    // 不再额外做水平视差，避免双重位移导致页面晃动/残影。
    val eased = turnProgress * turnProgress * (3f - 2f * turnProgress)
    val shift = if (handbookMode) 0f else when (turnDirection) {
        TurnDirection.NEXT -> -(eased * 4f)
        TurnDirection.PREVIOUS -> eased * 4f
        null -> 0f
    }
    val alpha = if (handbookMode) 1f else (1f - eased * 0.04f).coerceIn(0.96f, 1f)

    // 对照原版 BookViewExampleKt FragmentPage:
    // 左页(i%2==0): TopStart对齐, padding(start=10dp, top=10dp)
    // 右页(i%2!=0): TopEnd对齐, padding(top=10dp, end=10dp)
    // 主文字12sp, 副文字10sp, 颜色color_tab_divider(#FFC5BBB6)
    val isLeftPage = pageIndex % 2 == 0
    val tabDividerColor = Color(0xFFC5BBB6)

    // 书内周视图：7 天 Mon-Sun，每行等高
    val weekDays = remember(weekStartDate, schedulePreviewEntries) {
        if (weekStartDate == null) return@remember emptyList<ScheduleWeekDay>()
        val weekdayNames = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
        (0..6).map { offset ->
            val date = weekStartDate.plusDays(offset.toLong())
            val entries = schedulePreviewEntries.filter {
                it.year == date.year && it.month == date.monthValue && it.day == date.dayOfMonth
            }.sortedWith(compareBy({ it.timeText }))
            ScheduleWeekDay(
                day = date.dayOfMonth,
                weekday = weekdayNames.getOrElse(date.dayOfWeek.value - 1) { "" },
                entries = entries,
            )
        }
    }

    // 日期标签：周视图基于 weekStartDate；月视图基于今天
    val labelDate = weekStartDate ?: LocalDate.now()
    val dateLabelMain = "${labelDate.monthValue}月"
    val dateLabelSub = try {
        val weekFields = java.time.temporal.WeekFields.of(java.util.Locale.CHINA)
        "第${labelDate.get(weekFields.weekOfWeekBasedYear())}周"
    } catch (_: Exception) {
        ""
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                translationX = shift
                this.alpha = alpha
            }
            .fillMaxSize(),
    ) {
        // 顶部日期标签：对照 fragment_schedule_inbook.xml
        // 左页(i%2==0): 11sp主 + 9sp副, marginStart=10dp, marginTop=10dp
        // 右页(i%2!=0): 12sp主 + 10sp副, marginEnd=10dp, marginTop=10dp
        val headerPadding = 10.dp
        val headerMainSize = if (isLeftPage) 11.sp else 12.sp
        val headerSubSize = if (isLeftPage) 9.sp else 10.sp
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = if (isLeftPage) headerPadding else 0.dp,
                    top = headerPadding,
                    end = if (isLeftPage) 0.dp else headerPadding,
                ),
            contentAlignment = if (isLeftPage) Alignment.TopStart else Alignment.TopEnd,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = dateLabelMain,
                    fontSize = headerMainSize,
                    lineHeight = headerMainSize,
                    color = tabDividerColor,
                    fontFamily = GoaldayDesign.BodyFontFamily,
                )
                Text(
                    text = " | $dateLabelSub",
                    fontSize = headerSubSize,
                    lineHeight = headerMainSize,
                    color = tabDividerColor,
                    fontFamily = GoaldayDesign.BodyFontFamily,
                )
            }
        }
        // 0.5dp 分割线（仅 schedule 页有，对照 BookViewExampleKt L774-L776）
        // 位置在页眉文字下缘（原版线在 header 下方，不能穿过文字）
        Spacer(
            modifier = Modifier
                .padding(top = 28.dp)
                .fillMaxWidth()
                .height(0.5.dp)
                .background(tabDividerColor.copy(alpha = 0.6f)),
        )
        // 书内周视图：7 行等高，与原版 RecyclerView 每项占 parent/7 一致
        if (weekStartDate != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 28.dp + 0.5.dp + 3.dp),
            ) {
                weekDays.forEach { weekDay ->
                    InBookScheduleDayRow(
                        modifier = Modifier.weight(1f),
                        day = weekDay.day,
                        weekday = weekDay.weekday,
                        entries = weekDay.entries,
                        pageTitle = page.title,
                        isChecked = isChecked,
                    )
                }
            }
        } else {
            // 非书内/单页预览：保留原整月滚动列表
            val today = LocalDate.now()
            val scheduleMonth = today.monthValue
            val groupedByDay = remember(schedulePreviewEntries, scheduleMonth) {
                schedulePreviewEntries
                    .filter { it.year == today.year && it.month == scheduleMonth }
                    .sortedWith(compareBy({ it.day }, { it.timeText }))
                    .groupBy { it.day to it.month }
                    .toList()
            }
            val allDays = remember(groupedByDay, scheduleMonth, today) {
                val yearMonth = YearMonth.of(today.year, scheduleMonth)
                val weekdayNames = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
                val entriesByDay = groupedByDay.toMap()
                (1..yearMonth.lengthOfMonth()).map { day ->
                    val date = LocalDate.of(today.year, scheduleMonth, day)
                    val weekday = weekdayNames.getOrElse(date.dayOfWeek.value - 1) { "" }
                    val entries = entriesByDay[day to scheduleMonth] ?: emptyList()
                    (day to weekday) to entries
                }
            }
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 25.dp + 0.5.dp + 5.dp),
            ) {
                items(allDays, key = { it.first.first }) { pair ->
                    val (day, weekday) = pair.first
                    val entries = pair.second
                    InBookScheduleDayRow(
                        day = day,
                        weekday = weekday,
                        entries = entries,
                        pageTitle = page.title,
                        isChecked = isChecked,
                    )
                }
                item {
                    Box(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

private data class ScheduleWeekDay(
    val day: Int,
    val weekday: String,
    val entries: List<ScheduleEntry>,
)

// 对照 item_schedule_item_in_book.xml
// 结构：横向 LinearLayout
//   - FrameLayout(24.5dip) 日期列
//   - 2个 LinearLayout(weight=1, paddingVertical=3.5pt)，各含3行(12pt高)
@Composable
private fun InBookScheduleDayRow(
    day: Int,
    weekday: String,
    entries: List<ScheduleEntry>,
    pageTitle: String,
    isChecked: (String, String) -> Boolean,
    modifier: Modifier = Modifier,
) {
    val dateColumnColor = GoaldayDesign.ScheduleDateColumnSeparator
    Row(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                // 每天底部一条分隔线，对照原版截图：线从页面最左缘贯穿到右缘
                // 实测原版线色 ≈ #D9D2CF（C5BBB6 于白底上 alpha≈0.7 的效果）
                val strokeWidth = 0.5.dp.toPx()
                drawLine(
                    color = dateColumnColor.copy(alpha = 0.7f),
                    start = Offset(x = 0f, y = size.height - strokeWidth / 2),
                    end = Offset(x = size.width, y = size.height - strokeWidth / 2),
                    strokeWidth = strokeWidth,
                )
            },
    ) {
        // 左侧日期列：layout_width=24.5dip = 24.5dp
        // tv_day_1: textSize=9dp, marginBottom=2dp, 上半区；includeFontPadding=false
        // divider_line: textSize=9dp "—", color_tab_divider(#C5BBB6), 居中于 50% guideline
        // tv_day_2: textSize=6dp, marginTop=2dp, 下半区
        // 注意：必须显式收紧 lineHeight——Material3 默认 bodyLarge lineHeight=24sp
        // 会把 9sp 小字的行框撑到 24sp，导致日期/横线/周几被摊满整行且相互重叠
        Box(
            modifier = Modifier.width(24.5.dp).fillMaxHeight(),
            contentAlignment = Alignment.Center,
        ) {
            // 日期列紧凑居中：数字 + 分隔线 + 周几
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    day.toString().padStart(2, '0'),
                    fontSize = 9.sp,
                    lineHeight = 9.sp,
                    color = dateColumnColor,
                    modifier = Modifier.padding(bottom = 2.dp),
                )
                Text(
                    "—",
                    fontSize = 9.sp,
                    lineHeight = 9.sp,
                    color = dateColumnColor,
                )
                Text(
                    weekday,
                    fontSize = 6.sp,
                    lineHeight = 6.sp,
                    color = dateColumnColor,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
        // 右侧 2 列 × 3 行目标槽
        // 对照 item_schedule_item_in_book.xml: 左列填 1/3/5，右列填 2/4/6（奇偶分列）
        val leftEntries = entries.filterIndexed { i, _ -> i % 2 == 0 }.take(3)
        val rightEntries = entries.filterIndexed { i, _ -> i % 2 == 1 }.take(3)
        InBookScheduleTargetColumn(
            entries = leftEntries,
            pageTitle = pageTitle,
            isChecked = isChecked,
            modifier = Modifier.weight(1f),
        )
        InBookScheduleTargetColumn(
            entries = rightEntries,
            pageTitle = pageTitle,
            isChecked = isChecked,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun InBookScheduleTargetColumn(
    entries: List<ScheduleEntry>,
    pageTitle: String,
    isChecked: (String, String) -> Boolean,
    modifier: Modifier = Modifier,
) {
    // 对照 item_schedule_item_in_book.xml: 2个 LinearLayout(weight=1), paddingVertical=3.5dip
    // 行高 12dip（pt 是解码器对 dip 的误标，直接使用原始值）
    // et_target 使用 ContentTextView + FontUtils contentSize；默认 mode=1 时 contentSize=16dp，isInBook 减半为 8sp
    Column(
        modifier = modifier.padding(vertical = InBookScheduleColumnPaddingVertical),
    ) {
        for (i in 0 until 3) {
            val entry = entries.getOrNull(i)
            // cb_target visibility=gone → 不渲染勾选框
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(InBookScheduleSlotHeight)
                    .clipToBounds(),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (entry != null) {
                    val checked = entry.completed || isChecked(pageTitle, entry.title)
                    Text(
                        entry.title,
                        fontSize = 8.sp,
                        // 槽高仅 12dp，必须收紧 lineHeight（默认 24sp 会把文字挤出槽位）
                        lineHeight = 9.sp,
                        color = if (checked) GoaldayDesign.InkMuted else GoaldayDesign.InkPrimary,
                        textDecoration = if (checked) TextDecoration.LineThrough else TextDecoration.None,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(start = 2.dp, end = 2.dp),
                    )
                }
            }
        }
    }
}
// endregion

// region 计划页 (fragment_plan.xml + item_plan_item.xml)
// 结构：RecyclerView(paddingTop=11dp, paddingStart/End=20dp) + 浮动按钮(GONE)
// 每项：黑色圆点(10pt) + 内容文字(16pt) + 右侧时间文字(14pt)，最小高度 49pt
// 滑动删除：对照 item_plan_item.xml 使用 SwipeRevealLayout，右侧滑出编辑(黑色)+删除(#ed8888)
@Composable
internal fun InBookPlanPreview(
    modifier: Modifier,
    page: PlanPage,
    pageIndex: Int,
    pageCount: Int,
    customPageItems: List<String>,
    isChecked: (String, String) -> Boolean,
    onToggleChecked: (String, String) -> Unit,
    onDeleteItem: (String) -> Unit = {},
    onEditItem: (String, String) -> Unit = { _, _ -> },
    onAddItem: (String) -> Unit = {},
    tint: Color,
    turnProgress: Float,
    turnDirection: TurnDirection?,
    handbookMode: Boolean = false,
) {
    // HANDBOOK 模式下外部已应用 turningPageTransform 3D 翻页，内容应贴在页面上随页转动。
    val eased = turnProgress * turnProgress * (3f - 2f * turnProgress)
    val shift = if (handbookMode) 0f else when (turnDirection) {
        TurnDirection.NEXT -> -(eased * 8f)
        TurnDirection.PREVIOUS -> eased * 8f
        null -> 0f
    }
    val alpha = if (handbookMode) 1f else (1f - eased * 0.08f).coerceIn(0.92f, 1f)

    val planItems = remember(page.planItems, customPageItems) {
        val base = page.planItems.takeIf { it.isNotEmpty() } ?: page.items.map { PlanItem(it) }
        val custom = customPageItems.map { PlanItem(it) }
        (base + custom).distinctBy { it.title }
    }

    var editingItem by remember { mutableStateOf<String?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var addDraft by remember { mutableStateOf("") }
    var showTipDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .graphicsLayer {
                translationX = shift
                this.alpha = alpha
            }
            .fillMaxSize(),
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = if (handbookMode) 11.dp else 0.dp,
                    start = 20.dp,
                    end = 20.dp,
                ),
        ) {
            // 手账 NoTouch 阅读态下不显示标题/页码/分隔线，与 fragment_plan.xml 保持一致
            if (!handbookMode) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 11.dp, bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = page.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GoaldayDesign.InkPrimary,
                        )
                        Text(
                            text = "${pageIndex + 1}/$pageCount",
                            fontSize = 10.sp,
                            color = GoaldayDesign.InkMuted,
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(0.7.dp)
                            .background(GoaldayDesign.InkMuted.copy(alpha = 0.15f)),
                    )
                    Spacer(Modifier.height(6.dp))
                }
            }
            items(planItems.withIndex().toList(), key = { it.value.title }) { (index, item) ->
                InBookPlanRow(
                    item = item,
                    index = index,
                    checked = isChecked(page.title, item.title),
                    onToggleChecked = { onToggleChecked(page.title, item.title) },
                    onDelete = { onDeleteItem(item.title) },
                    onEdit = { editingItem = item.title },
                    handbookMode = handbookMode,
                )
            }
        }

        // 浮动按钮：对照 fragment_plan.xml 的 iv_add 和 iv_tip
        // 手账 NoTouch 阅读态下不显示，避免破坏书页沉浸感。
        if (!handbookMode) {
            // 提示按钮 (黑色背景，白色图标) - marginBottom=32dp
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 32.dp)
                    .size(43.dp)
                    .clip(RoundedCornerShape(90.dp))
                    .background(Color.Black)
                    .clickable { showTipDialog = true },
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.plan_tip),
                    contentDescription = "提示",
                    modifier = Modifier.size(24.dp),
                    colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color.White),
                )
            }

            // 添加按钮 (mi色背景 #E5DAD4) - marginBottom=93dp
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 93.dp)
                    .size(43.dp)
                    .clip(RoundedCornerShape(90.dp))
                    .background(Color(0xFFE5DAD4))
                    .clickable { showAddDialog = true },
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.plan_add),
                    contentDescription = "添加计划",
                    modifier = Modifier.size(24.dp),
                )
            }
        }
    }

    // 添加对话框
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("添加计划") },
            text = {
                OutlinedTextField(
                    value = addDraft,
                    onValueChange = { addDraft = it },
                    label = { Text("输入计划内容") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (addDraft.isNotBlank()) {
                        onAddItem(addDraft.trim())
                        addDraft = ""
                    }
                    showAddDialog = false
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = {
                    addDraft = ""
                    showAddDialog = false
                }) { Text("取消") }
            },
        )
    }

    // 编辑对话框
    editingItem?.let { currentItem ->
        var editText by remember(currentItem) { mutableStateOf(currentItem) }
        AlertDialog(
            onDismissRequest = { editingItem = null },
            title = { Text("编辑计划") },
            text = {
                OutlinedTextField(
                    value = editText,
                    onValueChange = { editText = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (editText.isNotBlank() && editText != currentItem) {
                        onEditItem(currentItem, editText)
                    }
                    editingItem = null
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { editingItem = null }) { Text("取消") }
            },
        )
    }

    // 提示对话框 — 对照原版 GuideTipDialog
    if (showTipDialog) {
        AlertDialog(
            onDismissRequest = { showTipDialog = false },
            title = { Text("小贴士") },
            text = {
                Text(
                    "把想做的事写下来，就成功了一半。\n" +
                        "计划要具体：用「动词 + 内容 + 时间」写，比如「早7点跑步30分钟」。\n" +
                        "目标要可衡量：用数字判断是否完成，比「多读书」更好的是「每月读2本」。",
                )
            },
            confirmButton = {
                TextButton(onClick = { showTipDialog = false }) { Text("知道了") }
            },
        )
    }
}

@Composable
private fun InBookPlanRow(
    item: PlanItem,
    index: Int,
    checked: Boolean,
    onToggleChecked: () -> Unit,
    onDelete: () -> Unit = {},
    onEdit: () -> Unit = {},
    handbookMode: Boolean = false,
) {
    // 对照 item_plan_item.xml（apktool 将 dip 误标为 pt，以下使用原始 dip 值）：
    // SwipeRevealLayout marginBottom=2dip
    // cl_content minHeight=49dip, white background
    // 右侧滑出：fl_info(黑色编辑) + fl_delete(#ed8888删除)，各50dip宽
    // HANDBOOK 模式下对齐原版 NoTouch 书页，禁用滑动删除，避免与全宽翻页手势冲突。
    val swipeOffset = remember { androidx.compose.animation.core.Animatable(0f) }
    val scope = rememberCoroutineScope()
    var swipeRevealed by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 49.dp) // cl_content minHeight=49dip
            .padding(bottom = 2.dp), // SwipeRevealLayout marginBottom=2dip
    ) {
        // 右侧滑动操作按钮（编辑+删除）仅在非 HANDBOOK 模式显示
        if (!handbookMode && swipeRevealed) {
            Row(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight(),
            ) {
                // 编辑按钮：黑色背景，50dip宽
                Box(
                    modifier = Modifier
                        .width(50.dp)
                        .fillMaxHeight()
                        .background(Color.Black)
                        .clickable {
                            onEdit()
                            swipeRevealed = false
                            scope.launch { swipeOffset.animateTo(0f) }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text("编辑", color = Color.White, fontSize = 14.sp)
                }
                // 删除按钮：#ed8888 红色背景，50dip宽
                Box(
                    modifier = Modifier
                        .width(50.dp)
                        .fillMaxHeight()
                        .background(Color(0xFFed8888))
                        .clickable {
                            onDelete()
                            swipeRevealed = false
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text("删除", color = Color.White, fontSize = 14.sp)
                }
            }
        }

        // 内容层：HANDBOOK 模式下禁止滑动，仅显示内容
        val rowModifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 49.dp) // cl_content minHeight=49dip
            .graphicsLayer {
                translationX = if (handbookMode) 0f else swipeOffset.value
            }
            .background(Color.White)
        val gestureModifier = if (handbookMode) {
            Modifier
        } else {
            Modifier.pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        val newX = (swipeOffset.value + dragAmount).coerceIn(-100f, 0f)
                        scope.launch { swipeOffset.snapTo(newX) }
                    },
                    onDragEnd = {
                        if (swipeOffset.value < -50f) {
                            // 滑出超过一半，保持展开
                            scope.launch { swipeOffset.animateTo(-100f) }
                            swipeRevealed = true
                        } else {
                            // 回弹
                            scope.launch { swipeOffset.animateTo(0f) }
                            swipeRevealed = false
                        }
                    },
                    onDragCancel = {
                        scope.launch { swipeOffset.animateTo(0f) }
                        swipeRevealed = false
                    },
                )
            }
        }
        Row(
            modifier = rowModifier.then(gestureModifier),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 黑色圆点：10dip，marginStart=15dip
            Box(
                modifier = Modifier
                    .padding(start = 15.dp)
                    .size(10.dp)
                    .background(GoaldayDesign.InkPrimary, shape = RoundedCornerShape(90.dp))
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = { onToggleChecked() })
                    },
            )
            // 内容文字：textSize=16dip, paddingVertical=14dip, marginStart=16dip
            Text(
                item.title,
                fontSize = 16.sp,
                color = if (checked) GoaldayDesign.InkMuted else GoaldayDesign.InkPrimary,
                textDecoration = if (checked) TextDecoration.LineThrough else TextDecoration.None,
                modifier = Modifier
                    .padding(start = 16.dp, top = 14.dp, bottom = 14.dp)
                    .weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            // 右侧时间文字：对照 tv_count textSize=14dip, paddingTop=14dip, marginEnd=14dip
            // 没有时间数据时保持空白，不显示序号
            Text(
                text = item.timeText,
                fontSize = 14.sp,
                color = GoaldayDesign.InkMuted,
                modifier = Modifier
                    .padding(end = 14.dp, top = 14.dp, bottom = 14.dp),
            )
        }
    }
}
// endregion

// region 日记页 (fragment_diary_inbook.xml + item_diary_*.xml)
// 结构：RecyclerView(marginTop=5dip, marginBottom=30dip, marginStart/End=7.5dip) + 底部图片栏(23dip)
@Composable
internal fun InBookDiaryPreview(
    modifier: Modifier,
    page: DiaryPage,
    pageIndex: Int,
    pageCount: Int,
    diaryDraft: String,
    tint: Color,
    turnProgress: Float,
    turnDirection: TurnDirection?,
    handbookMode: Boolean = false,
    // 书内双页展开模式：右页顶部显示 day | weekday
    diaryDate: java.time.LocalDate? = null,
    onAddImage: () -> Unit = {},
    // 当天日程（用于右页顶部渲染今日完成橙卡片，对照原版书右页）
    scheduleEntries: List<ScheduleEntry> = emptyList(),
) {
    // HANDBOOK 模式下外部已应用 turningPageTransform 3D 翻页，内容应贴在页面上随页转动。
    val eased = turnProgress * turnProgress * (3f - 2f * turnProgress)
    val shift = if (handbookMode) 0f else when (turnDirection) {
        TurnDirection.NEXT -> -(eased * 8f)
        TurnDirection.PREVIOUS -> eased * 8f
        null -> 0f
    }
    val alpha = if (handbookMode) 1f else (1f - eased * 0.08f).coerceIn(0.92f, 1f)

    val diary = remember(diaryDraft) { StructuredDiary.fromRaw(diaryDraft) }

    val diaryIsLeftPage = pageIndex % 2 == 0
    val diaryTabDividerColor = Color(0xFFC5BBB6)
    val diaryDateLabelMain = diaryDate?.dayOfMonth?.toString() ?: ""
    val diaryDateLabelSub = diaryDate?.let {
        listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日").getOrElse(it.dayOfWeek.value - 1) { "" }
    } ?: ""

    Box(
        modifier = modifier
            .graphicsLayer {
                translationX = shift
                this.alpha = alpha
            }
            .fillMaxSize(),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            // 手账 NoTouch 阅读态下不显示标题/页码，与 fragment_diary_inbook.xml 保持一致
            if (!handbookMode) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 11.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = diaryDateLabel(diary.date),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GoaldayDesign.InkPrimary,
                )
                Text(
                    text = "${pageIndex + 1}/$pageCount",
                    fontSize = 10.sp,
                    color = GoaldayDesign.InkMuted,
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(0.7.dp)
                    .background(GoaldayDesign.InkMuted.copy(alpha = 0.15f)),
            )
            Spacer(Modifier.height(6.dp))
        }
        // 内容区：对照 fragment_diary_inbook.xml RecyclerView
        // marginTop=5dip, marginBottom=30dip, marginStart/End=7.5dip
        val diaryMarginH = 7.5.dp
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(top = 5.dp, bottom = 30.dp, start = diaryMarginH, end = diaryMarginH),
        ) {
            val contentScroll = if (handbookMode) Modifier else Modifier.verticalScroll(rememberScrollState())
            val moodItems = remember(diary.moodTags) {
                diary.moodTags.split(',', '，', ' ').map(String::trim).filter(String::isNotBlank)
            }
            // 构建统一有序的行列表：块按原顺序（含图片）+ 摘要分区
            val diaryRows = remember(diary) {
                buildList {
                    // 1) 普通块：在书内渲染时把 target/topic_target/target_child 映射为书内变体
                    diary.blocks.forEach { block ->
                        add(block.toInBookBlock())
                    }
                    // 2) 遗留图片（无块顺序信息）追加到末尾
                    diary.legacyImageUris.forEach { uri ->
                        add(DiaryEntryBlock(DiaryBlockType.IMAGE, uri))
                    }
                    // 3) 富文本/图片描述转为文字行
                    if (diary.richHtml.isNotBlank()) {
                        add(DiaryEntryBlock(DiaryBlockType.TEXT, plainTextFromHtml(diary.richHtml)))
                    }
                    if (diary.photoText.isNotBlank()) {
                        add(DiaryEntryBlock(DiaryBlockType.TEXT, diary.photoText))
                    }
                    // 4) 摘要分区转换为书内目标/文字行
                    if (diary.todayDone.isNotBlank()) {
                        add(summaryTargetInBookBlock("今日完成", diary.todayDone))
                    }
                    if (diary.workTasks.isNotBlank()) {
                        add(summaryTargetInBookBlock("工作任务", diary.workTasks))
                    }
                    if (diary.smallJoy.isNotBlank()) {
                        add(DiaryEntryBlock(DiaryBlockType.TEXT, diary.smallJoy))
                    }
                    if (diary.canImprove.isNotBlank()) {
                        add(DiaryEntryBlock(DiaryBlockType.TEXT, diary.canImprove))
                    }
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .then(contentScroll),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                // 今日完成卡片（对照原版书右页：当天完成的日程以橙红渐变卡片置顶）
                val doneEntries = if (diaryDate != null) {
                    scheduleEntries.filter {
                        it.completed &&
                            it.year == diaryDate.year &&
                            it.month == diaryDate.monthValue &&
                            it.day == diaryDate.dayOfMonth
                    }
                } else {
                    emptyList()
                }
                doneEntries.forEach { doneEntry ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(GoaldayDesign.Pink, Color(0xFFF66061)),
                                ),
                            )
                            .padding(horizontal = 11.dp, vertical = 9.dp),
                    ) {
                        Column {
                            Text(
                                doneEntry.title,
                                fontSize = 13.sp,
                                lineHeight = 17.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                            )
                            if (doneEntry.note.isNotBlank()) {
                                Text(
                                    "@" + doneEntry.note,
                                    fontSize = 10.sp,
                                    lineHeight = 13.sp,
                                    color = Color.White.copy(alpha = 0.75f),
                                )
                            }
                        }
                    }
                }
                // 心情标签：对照 fragment_diary_inbook.xml 顶部标签行
                if (moodItems.isNotEmpty()) {
                    Text(
                        text = moodItems.joinToString("  "),
                        fontSize = 12.sp,
                        color = GoaldayDesign.InkMuted,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                // 对照 fragment_diary_inbook.xml RecyclerView：统一列表渲染所有行类型
                if (diaryRows.isNotEmpty()) {
                    DiaryTypedBlockPreview(blocks = diaryRows)
                } else if (moodItems.isEmpty()) {
                    // 空日记显示提示语
                    Text(
                        page.prompt,
                        fontSize = 16.sp,
                        color = GoaldayDesign.DiarySectionInk,
                        lineHeight = 21.sp,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
        // 底部图片栏：对照 fragment_diary_inbook.xml fl_bottom_bar: 23dip 高, 白色背景
        // 书内阅读态下原版 setVisibility(GONE)，这里同样隐藏
        if (!handbookMode) {
        val bottomBarHeight = 23.dp
        val selectPicSize = 23.dp
        val selectPicMarginStart = 3.75.dp
        val selectPicIconSize = 12.5.dp
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(bottomBarHeight)
                .background(Color.White),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // fl_select_pic: 23dip × 23dip, marginStart=3.75dip
            Box(
                modifier = Modifier
                    .padding(start = selectPicMarginStart)
                    .size(selectPicSize)
                    .clickable { onAddImage() },
                contentAlignment = Alignment.Center,
            ) {
                // ic_select_pic: 12.5dip × 12.5dip
                Image(
                    painter = painterResource(R.drawable.ic_select_pic),
                    contentDescription = "插入图片",
                    modifier = Modifier.size(selectPicIconSize),
                )
            }
        }
        }
        // 书内日期标签：对照 fragment_diary_inbook.xml
        // 左页: 11sp主 + 9sp副, marginStart=10dp, marginTop=10dp
        // 右页: 12sp主 + 10sp副, marginEnd=10dp, marginTop=10dp
        if (handbookMode && diaryDate != null) {
            val diaryHeaderPadding = 10.dp
            val diaryHeaderMainSize = if (diaryIsLeftPage) 11.sp else 12.sp
            val diaryHeaderSubSize = if (diaryIsLeftPage) 9.sp else 10.sp
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = if (diaryIsLeftPage) diaryHeaderPadding else 0.dp,
                        top = diaryHeaderPadding,
                        end = if (diaryIsLeftPage) 0.dp else diaryHeaderPadding,
                    ),
                contentAlignment = if (diaryIsLeftPage) Alignment.TopStart else Alignment.TopEnd,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = diaryDateLabelMain,
                        fontSize = diaryHeaderMainSize,
                        lineHeight = diaryHeaderMainSize,
                        color = diaryTabDividerColor,
                        fontFamily = GoaldayDesign.BodyFontFamily,
                    )
                    Text(
                        text = " | $diaryDateLabelSub",
                        fontSize = diaryHeaderSubSize,
                        lineHeight = diaryHeaderMainSize,
                        color = diaryTabDividerColor,
                        fontFamily = GoaldayDesign.BodyFontFamily,
                    )
                }
            }
        }
    }
}
}

// 将普通日记块映射为书内渲染变体，确保在 InBookDiaryPreview 中使用 item_diary_*_inbook 风格。
private fun DiaryEntryBlock.toInBookBlock(): DiaryEntryBlock = when (type) {
    DiaryBlockType.TARGET -> copy(type = DiaryBlockType.TARGET_IN_BOOK)
    DiaryBlockType.TARGET_CHILD -> copy(type = DiaryBlockType.TARGET_CHILD_IN_BOOK)
    DiaryBlockType.TOPIC_TARGET -> copy(type = DiaryBlockType.TOPIC_TARGET_IN_BOOK)
    else -> this
}

// 把摘要分区（今日完成/工作任务等）转成书内目标块：标题作为块主文本，内容行作为子项。
private fun summaryTargetInBookBlock(title: String, content: String): DiaryEntryBlock {
    val lines = content.lines().map(String::trim).filter(String::isNotBlank)
    val body = buildString {
        appendLine(title)
        lines.forEach { appendLine(it) }
    }.trimEnd()
    return DiaryEntryBlock(DiaryBlockType.TARGET_IN_BOOK, body, DiaryBlockStyle.BODY)
}
// endregion

// region 目标页 (item_target_detail.xml 简化版, 支持滑动删除)
// 结构：列表，每项 → 勾选框(20dp) + 目标文字(20sp) + 底部分隔线(4dp)
// 滑动操作：对照 item_target_detail.xml 使用 SwipeRevealLayout，右侧滑出编辑(黑色)+删除(#ed8888)
@Composable
internal fun InBookTargetPreview(
    modifier: Modifier,
    page: TargetPage,
    pageIndex: Int,
    pageCount: Int,
    customPageItems: List<String>,
    targetItemMeta: Map<String, TargetItemMeta>,
    isChecked: (String, String) -> Boolean,
    onToggleChecked: (String, String) -> Unit,
    onOpenTargetDetail: (String) -> Unit,
    onDeleteItem: (String) -> Unit = {},
    onEditItem: (String, String) -> Unit = { _, _ -> },
    onAddItem: (String) -> Unit = {},
    tint: Color,
    turnProgress: Float,
    turnDirection: TurnDirection?,
    handbookMode: Boolean = false,
) {
    // HANDBOOK 模式下外部已应用 turningPageTransform 3D 翻页，内容应贴在页面上随页转动。
    val eased = turnProgress * turnProgress * (3f - 2f * turnProgress)
    val shift = if (handbookMode) 0f else when (turnDirection) {
        TurnDirection.NEXT -> -(eased * 8f)
        TurnDirection.PREVIOUS -> eased * 8f
        null -> 0f
    }
    val alpha = if (handbookMode) 1f else (1f - eased * 0.08f).coerceIn(0.92f, 1f)

    val targetItems = remember(page.items, customPageItems) {
        (page.items + customPageItems).distinct()
    }

    var editingItem by remember { mutableStateOf<String?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var addDraft by remember { mutableStateOf("") }
    var showTipDialog by remember { mutableStateOf(false) }

    // 计算完成进度 — 不能用 remember 包 isChecked，lambda 引用不变但底层状态会变
    val completedCount = targetItems.count { isChecked(page.title, it) }
    val totalCount = targetItems.size
    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f

    Column(
        modifier = modifier
            .graphicsLayer {
                translationX = shift
                this.alpha = alpha
            }
            .fillMaxSize(),
    ) {
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = if (handbookMode) 11.dp else 0.dp),
                userScrollEnabled = !handbookMode,
            ) {
                // 页眉：标题 + 页码（与计划页、日程页、日记页保持一致）
                // HANDBOOK NoTouch 阅读态下隐藏，避免破坏书页沉浸感。
                if (!handbookMode) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 20.dp, end = 20.dp, top = 11.dp, bottom = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = page.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = GoaldayDesign.InkPrimary,
                            )
                            Text(
                                text = "${pageIndex + 1}/$pageCount",
                                fontSize = 10.sp,
                                color = GoaldayDesign.InkMuted,
                            )
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                                .height(0.7.dp)
                                .background(GoaldayDesign.InkMuted.copy(alpha = 0.15f)),
                        )
                        Spacer(Modifier.height(6.dp))
                    }
                }
                items(targetItems.withIndex().toList(), key = { it.value }) { (index, item) ->
                    val checked = isChecked(page.title, item)
                    val meta = targetItemMeta[item]
                    InBookTargetRow(
                        item = item,
                        index = index,
                        checked = checked,
                        onToggleChecked = { onToggleChecked(page.title, item) },
                        onOpenDetail = { onOpenTargetDetail(item) },
                        onDelete = { onDeleteItem(item) },
                        onEdit = { editingItem = item },
                        meta = meta,
                        handbookMode = handbookMode,
                    )
                }
            }
            
            // 浮动按钮：对照 fragment_target.xml 的 iv_add 和 iv_tip
            // 手账 NoTouch 阅读态下不显示。
            if (!handbookMode) {
                // 提示按钮 (黑色背景，白色图标) - marginBottom=32dp
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 20.dp, bottom = 32.dp)
                        .size(43.dp)
                        .clip(RoundedCornerShape(90.dp))
                        .background(Color.Black)
                        .clickable { showTipDialog = true },
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        painter = painterResource(R.drawable.plan_tip),
                        contentDescription = "提示",
                        modifier = Modifier.size(24.dp),
                        colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color.White),
                    )
                }

                // 添加按钮 (mi色背景 #E5DAD4) - marginBottom=93dp
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 20.dp, bottom = 93.dp)
                        .size(43.dp)
                        .clip(RoundedCornerShape(90.dp))
                        .background(Color(0xFFE5DAD4))
                        .clickable { showAddDialog = true },
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        painter = painterResource(R.drawable.plan_add),
                        contentDescription = "添加目标",
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
        }

        // 底部栏：完成数 + 进度条 + 编辑按钮（对照原版目标详情页底部 46dip=46dp）
        // 手账 NoTouch 阅读态下不显示底部操作栏。
        if (!handbookMode) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .background(GoaldayDesign.adaptiveSurface)
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
            // 完成数
            Text(
                text = "$completedCount/$totalCount",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = GoaldayDesign.InkSecondary,
            )
            Spacer(Modifier.width(12.dp))
            // 进度条
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(GoaldayDesign.adaptiveDivider),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress)
                        .clip(RoundedCornerShape(2.dp))
                        .background(GoaldayDesign.Pink),
                )
            }
            Spacer(Modifier.width(16.dp))
            // 编辑按钮
            Text(
                text = "编辑",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = GoaldayDesign.Pink,
                modifier = Modifier.clickable {
                    if (targetItems.isNotEmpty()) {
                        onOpenTargetDetail(targetItems.first())
                    }
                },
            )
        }
        }
    }

    // 添加对话框
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("添加目标") },
            text = {
                OutlinedTextField(
                    value = addDraft,
                    onValueChange = { addDraft = it },
                    label = { Text("输入目标内容") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (addDraft.isNotBlank()) {
                        onAddItem(addDraft.trim())
                        addDraft = ""
                    }
                    showAddDialog = false
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = {
                    addDraft = ""
                    showAddDialog = false
                }) { Text("取消") }
            },
        )
    }

    // 编辑对话框
    editingItem?.let { currentItem ->
        var editText by remember(currentItem) { mutableStateOf(currentItem) }
        AlertDialog(
            onDismissRequest = { editingItem = null },
            title = { Text("编辑目标") },
            text = {
                OutlinedTextField(
                    value = editText,
                    onValueChange = { editText = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (editText.isNotBlank() && editText != currentItem) {
                        onEditItem(currentItem, editText)
                    }
                    editingItem = null
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { editingItem = null }) { Text("取消") }
            },
        )
    }

    // 提示对话框 — 对照原版 GuideTipDialog
    if (showTipDialog) {
        AlertDialog(
            onDismissRequest = { showTipDialog = false },
            title = { Text("小贴士") },
            text = {
                Text(
                    "目标要具体可衡量，有截止日期。\n" +
                        "把大目标拆成小步骤，每完成一步都值得庆祝。\n" +
                        "定期复盘：每周回看进度，调整下一步行动。",
                )
            },
            confirmButton = {
                TextButton(onClick = { showTipDialog = false }) { Text("知道了") }
            },
        )
    }
}

// 对照 item_target_detail.xml（apktool 将 dip 误标为 pt，以下使用原始 dip 值）
// SwipeRevealLayout dragEdge=right，右侧滑出编辑(黑色)+删除(#ed8888)
// fl_check: paddingTop=21dip, paddingBottom=20dip, paddingStart/End=27dip
// iv_check: wrap_content (ic_box_full 背景 + ic_box_select 前景, 约20dp)
// tv_no: textSize=20dip, marginStart=56dip, 序号（TimeTextView）
// tv_content: textSize=20dip, marginStart=10dip, marginEnd=27dip, paddingBottom=12dip
// tv_date: bg_target_detail_date, paddingStart/End=10dip, marginBottom=13dip, 日期标签
// view2: bg_topic_detail_dot(虚线), height=4dp, translationY=3dp
@Composable
private fun InBookTargetRow(
    item: String,
    index: Int,
    checked: Boolean,
    onToggleChecked: () -> Unit,
    onOpenDetail: () -> Unit,
    onDelete: () -> Unit = {},
    onEdit: () -> Unit = {},
    meta: TargetItemMeta? = null,
    handbookMode: Boolean = false,
) {
    // HANDBOOK 模式下对齐原版 NoTouch 书页，禁用滑动删除，避免与全宽翻页手势冲突。
    val swipeOffset = remember { androidx.compose.animation.core.Animatable(0f) }
    val scope = rememberCoroutineScope()
    var swipeRevealed by remember { mutableStateOf(false) }
    val revealWidth = 100.dp  // 编辑+删除各 50dip，总 100dip

    Box(
        modifier = Modifier
            .fillMaxWidth(),
    ) {
        // 右侧滑动操作按钮（编辑+删除）仅在非 HANDBOOK 模式显示
        if (!handbookMode && swipeRevealed) {
            Row(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight(),
            ) {
                // 编辑按钮：黑色背景，50dip宽
                Box(
                    modifier = Modifier
                        .width(50.dp)
                        .fillMaxHeight()
                        .background(Color.Black)
                        .clickable {
                            onEdit()
                            swipeRevealed = false
                            scope.launch { swipeOffset.animateTo(0f) }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text("编辑", color = Color.White, fontSize = 14.sp)
                }
                // 删除按钮：#ed8888 红色背景，50dip宽
                Box(
                    modifier = Modifier
                        .width(50.dp)
                        .fillMaxHeight()
                        .background(Color(0xFFed8888))
                        .clickable {
                            onDelete()
                            swipeRevealed = false
                            scope.launch { swipeOffset.animateTo(0f) }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text("删除", color = Color.White, fontSize = 14.sp)
                }
            }
        }

        // 内容层：HANDBOOK 模式下禁止滑动，仅显示内容
        val columnModifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { translationX = if (handbookMode) 0f else swipeOffset.value }
        val gestureModifier = if (handbookMode) {
            Modifier
        } else {
            Modifier.pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        val newX = (swipeOffset.value + dragAmount).coerceIn(-revealWidth.value, 0f)
                        scope.launch { swipeOffset.snapTo(newX) }
                    },
                    onDragEnd = {
                        if (swipeOffset.value < -revealWidth.value / 2) {
                            scope.launch { swipeOffset.animateTo(-revealWidth.value) }
                            swipeRevealed = true
                        } else {
                            scope.launch { swipeOffset.animateTo(0f) }
                            swipeRevealed = false
                        }
                    },
                    onDragCancel = {
                        scope.launch { swipeOffset.animateTo(0f) }
                        swipeRevealed = false
                    },
                )
            }
        }
        Column(
            modifier = columnModifier.then(gestureModifier)
                .clickable { onOpenDetail() },
        ) {
            // 内容区：对照 item_target_detail.xml
            // fl_check: paddingStart=27dip, paddingTop=21dip, paddingBottom=20dip, paddingEnd=27dip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 27.dp, top = 21.dp, end = 27.dp, bottom = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // 勾选框：原版 wrap_content (ic_box_full + ic_box_select)，约20dp
                InBookCheckbox(
                    checked = checked,
                    onToggle = onToggleChecked,
                    size = 20,
                )
                // 序号：对照 tv_no textSize=20dip, marginStart=56dip (从 parent start)
                // Row padding=27dp + checkbox=20dp + padding=9dp = 56dp
                Text(
                    text = "${index + 1}",
                    fontSize = 20.sp,
                    color = GoaldayDesign.InkMuted,
                    modifier = Modifier.padding(start = 9.dp),
                )
                // 目标文字：对照 tv_content textSize=20dip, marginStart=10dip, marginEnd=27dip, paddingBottom=12dip
                Text(
                    item,
                    fontSize = 20.sp,
                    color = if (checked) GoaldayDesign.InkMuted else GoaldayDesign.InkPrimary,
                    textDecoration = if (checked) TextDecoration.LineThrough else TextDecoration.None,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .padding(start = 10.dp, end = 27.dp, bottom = 12.dp)
                        .weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            // 日期标签：对照 tv_date bg_target_detail_date, paddingStart/End=10dip, marginBottom=13dip
            // tv_date 约束 start_toStart_of tv_content，这里用 56dip 近似内容起始位置
            if (meta?.deadlineDay != null) {
                Box(
                    modifier = Modifier
                        .padding(start = 56.dp, bottom = 13.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(GoaldayDesign.Pink.copy(alpha = 0.1f))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = "${meta.deadlineDay}日",
                        fontSize = 12.sp,
                        color = GoaldayDesign.Pink,
                    )
                }
            }
            // 底部分隔线：对照 view2 bg_topic_detail_dot(虚线2dp), height=4dp, translationY=3dp
            // bg_topic_detail_dot.xml: stroke 2dp, color=#ffdfdfdf, dashWidth=2dp, dashGap=2dp
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .graphicsLayer { translationY = 3f }
                    .drawWithContent {
                        val dashWidth = 2.dp.toPx()
                        val dashGap = 2.dp.toPx()
                        val pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashWidth, dashGap), 0f)
                        drawLine(
                            color = Color(0xFFDFDFDF),
                            start = androidx.compose.ui.geometry.Offset(0f, size.height / 2),
                            end = androidx.compose.ui.geometry.Offset(size.width, size.height / 2),
                            strokeWidth = 2.dp.toPx(),
                            pathEffect = pathEffect,
                        )
                    },
            )
        }
    }
}
// endregion
