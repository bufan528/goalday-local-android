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
import androidx.compose.ui.draw.drawWithContent
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
import com.bf410.goaldaylocal.data.PlanPage
import com.bf410.goaldaylocal.data.ScheduleEntry
import com.bf410.goaldaylocal.data.SchedulePage
import com.bf410.goaldaylocal.data.TargetItemMeta
import com.bf410.goaldaylocal.data.TargetPage
import com.bf410.goaldaylocal.ui.replica.GoaldayDesign
import java.time.LocalDate
import java.time.YearMonth

// ============================================================
// 书内静态预览页面 — 对照逆向 fragment_*_inbook.xml + item_*_inbook.xml
// 原版书内页面使用 NoTouchConstraintLayout (clickable=false)，
// 大部分控件 visibility=GONE，只保留最简内容列表。
//
// 重要：aapt2 dump 验证（T5.xml = item_schedule_item_in_book.xml）：
// paddingVertical=3.5pt、layout_height=12.0pt 为真实 pt（1pt = 160/72 dp ≈ 2.222dp）
// layout_width=24.5dp、textSize=9dp/6dp 为 dp（非 pt）
// pt 按 1pt = 2.222dp 换算，dp 保持 1:1。
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
            .clickable { onToggle() },
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
// 每天结构：24.5dp日期列 + 2列×3行目标槽(paddingVertical=3.5pt=7.78dp)
// 日期列：9sp日期 + 9sp分隔线"—" + 6sp周几
// 目标槽：9dp勾选框 + style_schedule_day_form文字
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

    // 按天分组日程数据
    val today = LocalDate.now()
    val groupedByDay = remember(schedulePreviewEntries, page.title) {
        schedulePreviewEntries
            .filter { it.year == today.year }
            .sortedWith(compareBy({ it.month }, { it.day }, { it.timeText }))
            .groupBy { it.day to it.month }
            .toList()
    }

    val monthLabel = remember(page.title) {
        Regex("(\\d+)月").find(page.title)?.groupValues?.get(0) ?: page.title
    }
    // 预计算所有天数据，避免在 LazyColumn item 中重复计算
    val allDays = remember(groupedByDay, page.title, page.items, today) {
        if (groupedByDay.isNotEmpty()) {
            groupedByDay.map { (dayMonth, entries) ->
                val (day, month) = dayMonth
                val date = LocalDate.of(today.year, month, day)
                val weekdayNames = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
                val weekday = weekdayNames.getOrElse(date.dayOfWeek.value - 1) { "" }
                day to weekday to entries
            }
        } else {
            val items = page.items
            val monthNum = Regex("(\\d+)月").find(page.title)?.groupValues?.get(1)?.toIntOrNull()
            val scheduleMonth = monthNum ?: today.monthValue
            val yearMonth = YearMonth.of(today.year, scheduleMonth)
            val weekdayNames = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
            (1..yearMonth.lengthOfMonth()).map { day ->
                val date = LocalDate.of(today.year, scheduleMonth, day)
                val weekday = weekdayNames.getOrElse(date.dayOfWeek.value - 1) { "" }
                val dayEntries = (0 until 6).mapNotNull { slot ->
                    if (items.isEmpty()) return@mapNotNull null
                    val itemIndex = ((day - 1) * 6 + slot) % items.size
                    val title = items.getOrNull(itemIndex) ?: ""
                    if (title.isBlank()) return@mapNotNull null
                    ScheduleEntry(
                        id = "preview_${day}_${slot}",
                        title = title,
                        year = today.year,
                        month = scheduleMonth,
                        day = day,
                        timeText = "",
                        completed = false,
                    )
                }
                (day to weekday) to dayEntries
            }
        }
    }
    val baseModifier = modifier
        .graphicsLayer {
            translationX = shift
            this.alpha = alpha
        }
        .fillMaxSize()
        .handbookPaperRuling(null)

    // 书页模式保留 LazyColumn 避免一次性渲染 31 天导致 ANR，但禁用用户滚动，让翻页手势优先。
    LazyColumn(
        modifier = baseModifier,
        userScrollEnabled = !handbookMode,
    ) {
        // 页眉：标题 + 页码（与计划页、目标页、日记页保持一致）
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
                    color = GoaldayDesign.adaptiveInkPrimary,
                )
                Text(
                    text = "${pageIndex + 1}/$pageCount",
                    fontSize = 10.sp,
                    color = GoaldayDesign.adaptiveInkMuted,
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(0.7.dp)
                    .background(GoaldayDesign.adaptiveInkMuted.copy(alpha = 0.15f)),
            )
            Spacer(Modifier.height(6.dp))
        }
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

// 对照 item_schedule_item_in_book.xml
// 结构：横向 LinearLayout
//   - FrameLayout(24.5dp) 日期列
//   - 2个 LinearLayout(weight=1, paddingVertical=3.5pt=7.78dp)，各含3行(12pt=26.67dp高)
@Composable
private fun InBookScheduleDayRow(
    day: Int,
    weekday: String,
    entries: List<ScheduleEntry>,
    pageTitle: String,
    isChecked: (String, String) -> Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
    ) {
        // 左侧日期列：24.5dip = 24.5dp（apktool 验证 item_schedule_item_in_book.xml）
        // tv_day_1: textSize=9dp, marginBottom=2dp, 上半区
        // divider_line: textSize=9dp "—", color_tab_divider(#C5BBB6), 居中
        // tv_day_2: textSize=6dp, marginTop=2dp, 下半区
        Box(
            modifier = Modifier.width(24.5.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    day.toString(),
                    fontSize = 9.sp,
                    color = GoaldayDesign.adaptiveInkPrimary,
                    modifier = Modifier.padding(bottom = 2.dp),
                )
                Text(
                    "—",
                    fontSize = 9.sp,
                    color = GoaldayDesign.adaptiveInkMuted,
                )
                Text(
                    weekday,
                    fontSize = 6.sp,
                    color = GoaldayDesign.adaptiveInkMuted,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
        // 右侧 2 列 × 3 行目标槽
        // 对照：2个 LinearLayout(weight=1), paddingVertical=3.5pt=7.78dp（apktool 验证为 pt）
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
    // 对照：paddingVertical=3.5pt=7.78dp（apktool 验证为 pt，1pt = 2.222dp）
    Column(
        modifier = modifier.padding(vertical = 7.78.dp),
    ) {
        for (i in 0 until 3) {
            val entry = entries.getOrNull(i)
            // 对照 item_schedule_item_in_book.xml: layout_height=12.0pt=26.67dp
            // cb_target visibility=gone → 不渲染勾选框
            // et_target: style_schedule_day_form textSize=20dip
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(26.67.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (entry != null) {
                    val checked = entry.completed || isChecked(pageTitle, entry.title)
                    Text(
                        entry.title,
                        fontSize = 20.sp,
                        color = if (checked) GoaldayDesign.adaptiveInkMuted else GoaldayDesign.adaptiveInkPrimary,
                        textDecoration = if (checked) TextDecoration.LineThrough else TextDecoration.None,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
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

    val planItems = remember(page.items, customPageItems) {
        (page.items + customPageItems).distinct()
    }

    var editingItem by remember { mutableStateOf<String?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var addDraft by remember { mutableStateOf("") }

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
                .handbookPaperRuling(null),
        ) {
            // 页眉：标题 + 页码（与目标页、日程页保持一致）
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
                        color = GoaldayDesign.adaptiveInkPrimary,
                    )
                    Text(
                        text = "${pageIndex + 1}/$pageCount",
                        fontSize = 10.sp,
                        color = GoaldayDesign.adaptiveInkMuted,
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .height(0.7.dp)
                        .background(GoaldayDesign.adaptiveInkMuted.copy(alpha = 0.15f)),
                )
                Spacer(Modifier.height(6.dp))
            }
            items(planItems.withIndex().toList(), key = { it.value }) { (index, item) ->
                InBookPlanRow(
                    item = item,
                    index = index,
                    checked = isChecked(page.title, item),
                    onToggleChecked = { onToggleChecked(page.title, item) },
                    onDelete = { onDeleteItem(item) },
                    onEdit = { editingItem = item },
                    handbookMode = handbookMode,
                )
            }
        }

        // 浮动按钮：对照 fragment_plan.xml 的 iv_add 和 iv_tip
        // iv_add: bg_plan_menu(白色圆角90dp, 43dp) + plan_add图标, marginEnd=20dp, marginBottom=93dp
        // iv_tip: bg_plan_menu(黑色圆角90dp, 43dp) + plan_tip图标(白色), marginEnd=20dp, marginBottom=32dp
        
        // 提示按钮 (黑色背景，白色图标) - marginBottom=32dp
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 32.dp)
                .size(43.dp)
                .clip(RoundedCornerShape(90.dp))
                .background(Color.Black)
                .clickable { /* TODO: 提示功能 */ },
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.plan_tip),
                contentDescription = "提示",
                modifier = Modifier.size(24.dp),
                colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color.White),
            )
        }
        
        // 添加按钮 (白色背景) - marginBottom=93dp
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 93.dp)
                .size(43.dp)
                .clip(RoundedCornerShape(90.dp))
                .background(Color.White)
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
}

@Composable
private fun InBookPlanRow(
    item: String,
    index: Int,
    checked: Boolean,
    onToggleChecked: () -> Unit,
    onDelete: () -> Unit = {},
    onEdit: () -> Unit = {},
    handbookMode: Boolean = false,
) {
    // 对照 item_plan_item.xml:
    // SwipeRevealLayout marginBottom=2pt=4.44dp
    // cl_content minHeight=49pt=108.89dp, white background
    // 右侧滑出：fl_info(黑色编辑) + fl_delete(#ed8888删除)，各50pt=111.1dp宽
    // HANDBOOK 模式下对齐原版 NoTouch 书页，禁用滑动删除，避免与全宽翻页手势冲突。
    val swipeOffset = remember { androidx.compose.animation.core.Animatable(0f) }
    val scope = rememberCoroutineScope()
    var swipeRevealed by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 108.89.dp) // cl_content minHeight=49pt=108.89dp
            .padding(bottom = 4.44.dp), // SwipeRevealLayout marginBottom=2pt=4.44dp
    ) {
        // 右侧滑动操作按钮（编辑+删除）仅在非 HANDBOOK 模式显示
        if (!handbookMode && swipeRevealed) {
            Row(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight(),
            ) {
                // 编辑按钮：黑色背景，50pt=111.11dp宽
                Box(
                    modifier = Modifier
                        .width(111.11.dp)
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
                // 删除按钮：#ed8888 红色背景，50pt=111.11dp宽
                Box(
                    modifier = Modifier
                        .width(111.11.dp)
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
            .heightIn(min = 108.89.dp) // cl_content minHeight=49pt=108.89dp
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
            // 黑色圆点：10pt=22.22dp，marginStart=15pt=33.33dp
            Box(
                modifier = Modifier
                    .padding(start = 33.33.dp)
                    .size(22.22.dp)
                    .background(GoaldayDesign.adaptiveInkPrimary, shape = RoundedCornerShape(90.dp))
                    .clickable { onToggleChecked() },
            )
            // 内容文字：textSize=16pt=35.56sp, paddingVertical=14pt=31.11dp, marginStart=16pt=35.56dp
            Text(
                item,
                fontSize = 35.56.sp,
                color = if (checked) GoaldayDesign.adaptiveInkMuted else GoaldayDesign.adaptiveInkPrimary,
                textDecoration = if (checked) TextDecoration.LineThrough else TextDecoration.None,
                modifier = Modifier
                    .padding(start = 35.56.dp, top = 31.11.dp, bottom = 31.11.dp)
                    .weight(1f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            // 右侧时间文字：对照 tv_count textSize=14pt=31.11sp, paddingTop=14pt=31.11dp, marginEnd=14pt=31.11dp
            // 由于 PlanPage 数据模型无时间信息，显示序号作为占位
            Text(
                text = "${index + 1}",
                fontSize = 31.11.sp,
                color = GoaldayDesign.adaptiveInkMuted,
                modifier = Modifier
                    .padding(end = 31.11.dp, top = 31.11.dp, bottom = 31.11.dp),
            )
        }
    }
}
// endregion

// region 日记页 (fragment_diary_inbook.xml + item_diary_*.xml)
// 结构：RecyclerView(marginTop=5dp, marginBottom=30dp, marginStart/End=7.5pt=16.67dp) + 底部图片栏(23pt=51.11dp)
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
    onAddImage: () -> Unit = {},
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

    Column(
        modifier = modifier
            .graphicsLayer {
                translationX = shift
                this.alpha = alpha
            }
            .fillMaxSize(),
    ) {
        // 页眉：日期 + 页码（日记页使用日期标题，对齐原版 fragment_diary_inbook.xml）
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
                color = GoaldayDesign.adaptiveInkPrimary,
            )
            Text(
                text = "${pageIndex + 1}/$pageCount",
                fontSize = 10.sp,
                color = GoaldayDesign.adaptiveInkMuted,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(0.7.dp)
                .background(GoaldayDesign.adaptiveInkMuted.copy(alpha = 0.15f)),
        )
        Spacer(Modifier.height(6.dp))
        // 内容区：对照 fragment_diary_inbook.xml RecyclerView
        // marginTop=5dip=5dp, marginBottom=30dip=30dp, marginStart/End=7.5pt=16.67dp
        // 添加纸张横线效果，模拟真实笔记本（横线固定在背景上，内容在上面滚动）
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(top = 5.dp, bottom = 30.dp, start = 16.67.dp, end = 16.67.dp)
                .handbookPaperRuling(null),
        ) {
            val contentScroll = if (handbookMode) Modifier else Modifier.verticalScroll(rememberScrollState())
            // 复用外部解析的 diary，避免重复解析
            val imageUris = remember(diary) { (diary.imageBlockUris + diary.legacyImageUris).distinct() }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .then(contentScroll),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                // 图片区：对照 item_diary_img.xml
                if (imageUris.isNotEmpty()) {
                    DiaryImageStrip(imageUris = imageUris, onRemoveImage = null)
                }
                // 对照 item_diary_text.xml：16sp #2C2C2C, lineSpacingExtra=2dp
                // 16sp 字体默认行高约 19.2sp + 2dp 间距 ≈ 21sp
                if (diary.hasUserContent) {
                    // 渲染所有非图片块（图片已在上方渲染）：TARGET/TOPIC_TARGET/TARGET_CHILD/TEXT
                    val nonImageBlocks = diary.blocks.filter { it.type != DiaryBlockType.IMAGE }
                    DiaryTypedBlockPreview(nonImageBlocks)
                    // 渲染摘要文本（今日完成/工作任务/小幸福/可改进），使用卡片式布局
                    // 对照 item_diary_target_in_book.xml：白色背景+0.5pt边框+8pt圆角
                    val sections = listOf(
                        "今日完成" to diary.todayDone,
                        "工作任务" to diary.workTasks,
                        "小幸福" to diary.smallJoy,
                        "可改进" to diary.canImprove,
                    )
                    sections.forEach { (title, content) ->
                        if (content.isNotBlank()) {
                            // 卡片容器：白色背景+边框+圆角
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White)
                                    .border(
                                        width = 0.5.dp,
                                        color = Color.Black.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(16.dp),
                            ) {
                                Column {
                                    // 分区标题：带 ic_reward 图标
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    ) {
                                        Image(
                                            painter = painterResource(R.drawable.ic_reward),
                                            contentDescription = title,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            title,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF503311),
                                        )
                                    }
                                    // 分区内容
                                    content.split("\n").filter { it.isNotBlank() }.forEach { line ->
                                        Text(
                                            line,
                                            fontSize = 16.sp,
                                            color = GoaldayDesign.DiarySectionInk,
                                            lineHeight = 21.sp,
                                            modifier = Modifier.fillMaxWidth(),
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
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
        // 底部图片栏：对照 fragment_diary_inbook.xml fl_bottom_bar: 23pt=51.11dp 高, 白色背景
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(51.11.dp)
                .background(Color.White),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // fl_select_pic: 23pt=51.11dp × 51.11dp, marginStart=3.75pt=8.33dp
            Box(
                modifier = Modifier
                    .padding(start = 8.33.dp)
                    .size(51.11.dp)
                    .clickable { onAddImage() },
                contentAlignment = Alignment.Center,
            ) {
                // ic_select_pic: 12.5pt=27.78dp × 27.78dp
                Image(
                    painter = painterResource(R.drawable.ic_select_pic),
                    contentDescription = "插入图片",
                    modifier = Modifier.size(27.78.dp),
                )
            }
        }
    }
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
                    .handbookPaperRuling(null),
                userScrollEnabled = !handbookMode,
            ) {
                // 页眉：标题 + 页码（与计划页、日程页、日记页保持一致）
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
                            color = GoaldayDesign.adaptiveInkPrimary,
                        )
                        Text(
                            text = "${pageIndex + 1}/$pageCount",
                            fontSize = 10.sp,
                            color = GoaldayDesign.adaptiveInkMuted,
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .height(0.7.dp)
                            .background(GoaldayDesign.adaptiveInkMuted.copy(alpha = 0.15f)),
                    )
                    Spacer(Modifier.height(6.dp))
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
            // 提示按钮 (黑色背景，白色图标) - marginBottom=32dp
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 32.dp)
                    .size(43.dp)
                    .clip(RoundedCornerShape(90.dp))
                    .background(Color.Black)
                    .clickable { /* TODO: 提示功能 */ },
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.plan_tip),
                    contentDescription = "提示",
                    modifier = Modifier.size(24.dp),
                    colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color.White),
                )
            }
            
            // 添加按钮 (白色背景) - marginBottom=93dp
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 93.dp)
                    .size(43.dp)
                    .clip(RoundedCornerShape(90.dp))
                    .background(Color.White)
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
        
        // 底部栏：完成数 + 进度条 + 编辑按钮（对照原版目标详情页底部）
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(GoaldayDesign.adaptiveSurface)
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 完成数
            Text(
                text = "$completedCount/$totalCount",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = GoaldayDesign.adaptiveInkSecondary,
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
}

// 对照 item_target_detail.xml
// SwipeRevealLayout dragEdge=right，右侧滑出编辑(黑色)+删除(#ed8888)
// fl_check: paddingTop=21dp, paddingBottom=20dp, paddingStart/End=27dp
// iv_check: wrap_content (ic_box_full 背景 + ic_box_select 前景, 约20dp)
// tv_no: textSize=20dp, marginStart=56dp, 序号（TimeTextView）
// tv_content: textSize=20dip=20sp, marginStart=10dp, marginEnd=27dp, paddingBottom=12dp
// tv_date: bg_target_detail_date, paddingStart/End=10dp, marginBottom=13dp, 日期标签
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
    val revealWidth = 222.22.dp  // 编辑+删除各 50pt=111.11dp，总 222.22dp

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
                // 编辑按钮：黑色背景，50pt=111.11dp宽
                Box(
                    modifier = Modifier
                        .width(111.11.dp)
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
                // 删除按钮：#ed8888 红色背景，50pt=111.11dp宽
                Box(
                    modifier = Modifier
                        .width(111.11.dp)
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
                    color = GoaldayDesign.adaptiveInkMuted,
                    modifier = Modifier.padding(start = 9.dp),
                )
                // 目标文字：对照 tv_content marginStart=10dip, marginEnd=27dip, paddingBottom=12dip
                Text(
                    item,
                    fontSize = 20.sp,
                    color = if (checked) GoaldayDesign.adaptiveInkMuted else GoaldayDesign.adaptiveInkPrimary,
                    textDecoration = if (checked) TextDecoration.LineThrough else TextDecoration.None,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .padding(start = 10.dp, bottom = 12.dp)
                        .weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            // 日期标签：对照 tv_date bg_target_detail_date, paddingStart/End=10dip, marginBottom=13dip
            // tv_date 约束 start_toStart_of tv_content，这里用 56dp 近似内容起始位置
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
