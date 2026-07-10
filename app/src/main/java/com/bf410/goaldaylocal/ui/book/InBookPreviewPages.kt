package com.bf410.goaldaylocal.ui.book

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.Text
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
// 每天结构：24.5dp日期列 + 2列×3行目标槽(paddingVertical=3.5dp)
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
    val eased = turnProgress * turnProgress * (3f - 2f * turnProgress)
    val shift = when (turnDirection) {
        TurnDirection.NEXT -> -(eased * 8f)
        TurnDirection.PREVIOUS -> eased * 8f
        null -> 0f
    }
    val alpha = (1f - eased * 0.08f).coerceIn(0.92f, 1f)

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

    // 书页模式保留 LazyColumn 避免一次性渲染 31 天导致 ANR，但禁用用户滚动，让翻页手势优先。
    LazyColumn(
        modifier = baseModifier,
        userScrollEnabled = !handbookMode,
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${page.title} 计划 (${pageIndex + 1}/$pageCount)",
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
                    .padding(horizontal = 12.dp)
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
//   - FrameLayout(12.25dip) 日期列
//   - 2个 LinearLayout(weight=1, paddingVertical=1.75pt)，各含3行(12pt高)
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
        // 左侧日期列：12.25dip 宽（aapt2 验证为 dip）
        // tv_day_1: textSize=9dp, marginBottom=2dp, 上半区
        // divider_line: textSize=9dp "—", color_tab_divider(#C5BBB6), 居中
        // tv_day_2: textSize=6dp, marginTop=2dp, 下半区
        Box(
            modifier = Modifier.width(12.25.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    day.toString(),
                    fontSize = 9.sp,
                    color = GoaldayDesign.InkPrimary,
                    modifier = Modifier.padding(bottom = 2.dp),
                )
                Text(
                    "—",
                    fontSize = 9.sp,
                    color = GoaldayDesign.MorandiDivider,
                )
                Text(
                    weekday,
                    fontSize = 6.sp,
                    color = GoaldayDesign.InkMuted,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
        // 右侧 2 列 × 3 行目标槽
        // 对照：2个 LinearLayout(weight=1), paddingVertical=1.75pt=3.89dp（aapt2 验证为 pt）
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
    // 对照：paddingVertical=1.75pt=3.89dp（aapt2 验证为 pt）
    Column(
        modifier = modifier.padding(vertical = 3.89.dp),
    ) {
        for (i in 0 until 3) {
            val entry = entries.getOrNull(i)
            // 对照 item_schedule_item_in_book.xml: layout_height=12.0pt=26.67dp（aapt2 验证为 pt）
            // cb_target visibility=gone → 不渲染勾选框
            // et_target: style_schedule_day_form textSize=20dip，预览用10sp
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
                        fontSize = 10.sp,
                        color = if (checked) GoaldayDesign.InkMuted else GoaldayDesign.InkPrimary,
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
    tint: Color,
    turnProgress: Float,
    turnDirection: TurnDirection?,
    handbookMode: Boolean = false,
) {
    val eased = turnProgress * turnProgress * (3f - 2f * turnProgress)
    val shift = when (turnDirection) {
        TurnDirection.NEXT -> -(eased * 8f)
        TurnDirection.PREVIOUS -> eased * 8f
        null -> 0f
    }
    val alpha = (1f - eased * 0.08f).coerceIn(0.92f, 1f)

    val planItems = remember(page.items, customPageItems) {
        (page.items + customPageItems).distinct()
    }

    LazyColumn(
        modifier = modifier
            .graphicsLayer {
                translationX = shift
                this.alpha = alpha
            }
            .fillMaxSize()
            // 对照 fragment_plan.xml: RecyclerView paddingTop=11dp, paddingStart/End=20dp
            .padding(top = 11.dp, start = 20.dp, end = 20.dp),
    ) {
        items(planItems, key = { it }) { item ->
            InBookPlanRow(
                item = item,
                checked = isChecked(page.title, item),
                onToggleChecked = { onToggleChecked(page.title, item) },
                onDelete = { onDeleteItem(item) },
            )
        }
    }
}

@Composable
private fun InBookPlanRow(
    item: String,
    checked: Boolean,
    onToggleChecked: () -> Unit,
    onDelete: () -> Unit = {},
) {
    // 对照 item_plan_item.xml（aapt2 验证：全部值为 pt，1pt=2.222dp）:
    // SwipeRevealLayout marginBottom=2pt=4.44dp
    // cl_content minHeight=49pt=108.89dp, white background
    // 右侧滑出：fl_info(黑色编辑) + fl_delete(#ed8888删除)，各50pt=111.11dp宽
    val swipeOffset = remember { androidx.compose.animation.core.Animatable(0f) }
    val scope = rememberCoroutineScope()
    var swipeRevealed by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 108.89.dp)
            .padding(bottom = 4.44.dp),
    ) {
        // 右侧滑动操作按钮（编辑+删除）
        if (swipeRevealed) {
            Row(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight(),
            ) {
                // 编辑按钮：黑色背景
                Box(
                    modifier = Modifier
                        .width(111.11.dp)
                        .fillMaxHeight()
                        .background(Color.Black)
                        .clickable { /* 编辑暂不实现 */ },
                    contentAlignment = Alignment.Center,
                ) {
                    Text("编辑", color = Color.White, fontSize = 14.sp)
                }
                // 删除按钮：#ed8888 红色背景
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

        // 内容层：可滑动
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 108.89.dp)
                .graphicsLayer {
                    translationX = swipeOffset.value
                }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { _, dragAmount ->
                            val newX = (swipeOffset.value + dragAmount).coerceIn(-222.22f, 0f)
                            scope.launch { swipeOffset.snapTo(newX) }
                        },
                        onDragEnd = {
                            if (swipeOffset.value < -111.11f) {
                                // 滑出超过一半，保持展开
                                scope.launch { swipeOffset.animateTo(-222.22f) }
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
                },
            verticalAlignment = Alignment.Top,
        ) {
            // 对照 v_dot: 10pt=22.22dp, marginTop=19.5pt=43.33dp, marginStart=15pt=33.33dp
            // bg_toolbar_plan_dot: 黑色圆形(radius=90dp) when not selected
            Box(
                modifier = Modifier
                    .padding(start = 33.33.dp, top = 43.33.dp)
                    .size(22.22.dp)
                    .background(GoaldayDesign.InkPrimary, shape = RoundedCornerShape(90.dp))
                    .clickable { onToggleChecked() },
            )
            // 对照 tv_content: textSize=16pt=35.56sp, width=266pt=591.11dp, paddingTop/Bottom=14pt=31.11dp, marginStart=16pt=35.56dp
            Text(
                item,
                fontSize = 35.56.sp,
                color = if (checked) GoaldayDesign.InkMuted else GoaldayDesign.InkPrimary,
                textDecoration = if (checked) TextDecoration.LineThrough else TextDecoration.None,
                modifier = Modifier
                    .padding(start = 35.56.dp, top = 31.11.dp, bottom = 31.11.dp)
                    .weight(1f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            // 对照 tv_count: textSize=14pt=31.11sp, paddingTop=14pt=31.11dp, marginEnd=14pt=31.11dp
            // TimeTextView 显示次数/时间，预览暂留空占位
            Spacer(Modifier.width(31.11.dp))
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
) {
    val eased = turnProgress * turnProgress * (3f - 2f * turnProgress)
    val shift = when (turnDirection) {
        TurnDirection.NEXT -> -(eased * 8f)
        TurnDirection.PREVIOUS -> eased * 8f
        null -> 0f
    }
    val alpha = (1f - eased * 0.08f).coerceIn(0.92f, 1f)

    Column(
        modifier = modifier
            .graphicsLayer {
                translationX = shift
                this.alpha = alpha
            }
            .fillMaxSize(),
    ) {
        // 内容区：对照 fragment_diary_inbook.xml RecyclerView（aapt2 验证：marginStart/End 为 pt）
        // marginTop=5dp, marginBottom=30dp, marginStart/End=7.5pt=16.67dp
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(top = 5.dp, bottom = 30.dp, start = 16.67.dp, end = 16.67.dp),
        ) {
            val contentScroll = if (handbookMode) Modifier else Modifier.verticalScroll(rememberScrollState())
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .then(contentScroll),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                // 对照 item_diary_text.xml：16sp #2C2C2C, lineSpacingExtra=2dp
                // 16sp 字体默认行高约 19.2sp + 2dp 间距 ≈ 21sp
                val plainText = diaryDraft.ifBlank { page.prompt }
                plainText.split("\n").filter { it.isNotBlank() }.forEach { line ->
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
        // 底部图片栏：对照 fl_bottom_bar: 23pt=51.11dp 高（aapt2 验证为 pt）, 白色背景
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(51.11.dp)
                .background(GoaldayDesign.Surface),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // fl_select_pic: 23pt=51.11dp × 51.11dp, marginStart=3.75pt=8.33dp（aapt2 验证为 pt）
            Box(
                modifier = Modifier
                    .padding(start = 8.33.dp)
                    .size(51.11.dp),
                contentAlignment = Alignment.Center,
            ) {
                // ic_select_pic: 12.5pt=27.78dp × 27.78dp（aapt2 验证为 pt）
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

// region 目标页 (item_target_detail.xml 简化版, 无滑动删除)
// 结构：列表，每项 → 勾选框(20dp) + 目标文字(20sp) + 底部分隔线(4dp)
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
    tint: Color,
    turnProgress: Float,
    turnDirection: TurnDirection?,
    handbookMode: Boolean = false,
) {
    val eased = turnProgress * turnProgress * (3f - 2f * turnProgress)
    val shift = when (turnDirection) {
        TurnDirection.NEXT -> -(eased * 8f)
        TurnDirection.PREVIOUS -> eased * 8f
        null -> 0f
    }
    val alpha = (1f - eased * 0.08f).coerceIn(0.92f, 1f)

    val targetItems = remember(page.items, customPageItems) {
        (page.items + customPageItems).distinct()
    }

    LazyColumn(
        modifier = modifier
            .graphicsLayer {
                translationX = shift
                this.alpha = alpha
            }
            .fillMaxSize(),
        userScrollEnabled = !handbookMode,
    ) {
        items(targetItems, key = { it }) { item ->
            val checked = isChecked(page.title, item)
            InBookTargetRow(
                item = item,
                checked = checked,
                onToggleChecked = { onToggleChecked(page.title, item) },
                onOpenDetail = { onOpenTargetDetail(item) },
            )
        }
    }
}

// 对照 item_target_detail.xml
// fl_check: paddingTop=21dp, paddingBottom=20dp, paddingStart/End=27dp
// iv_check: wrap_content (ic_box_full 背景 + ic_box_select 前景, 约20dp)
// tv_content: textSize=20dip=20sp, marginStart=10dp, marginEnd=27dp, paddingBottom=12dp
// view2: bg_topic_detail_dot(虚线), height=4dp, translationY=3dp
@Composable
private fun InBookTargetRow(
    item: String,
    checked: Boolean,
    onToggleChecked: () -> Unit,
    onOpenDetail: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenDetail() },
    ) {
        // 内容区：对照 fl_check paddingTop=21dp, paddingBottom=20dp, paddingStart/End=27dp
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 27.dp, top = 21.dp, end = 27.dp, bottom = 20.dp),
            verticalAlignment = Alignment.Top,
        ) {
            // 勾选框：原版 wrap_content (ic_box_full + ic_box_select)，约20dp
            InBookCheckbox(
                checked = checked,
                onToggle = onToggleChecked,
                modifier = Modifier.padding(top = 2.dp),
                size = 20,
            )
            // 目标文字：对照 tv_content textSize=20dip=20sp, marginStart=10dp, paddingBottom=12dp
            Text(
                item,
                fontSize = 20.sp,
                color = if (checked) GoaldayDesign.InkMuted else GoaldayDesign.InkPrimary,
                textDecoration = if (checked) TextDecoration.LineThrough else TextDecoration.None,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .padding(start = 10.dp, bottom = 12.dp)
                    .weight(1f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
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
// endregion
