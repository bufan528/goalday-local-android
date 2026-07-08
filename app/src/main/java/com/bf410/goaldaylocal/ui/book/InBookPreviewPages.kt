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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bf410.goaldaylocal.R
import com.bf410.goaldaylocal.data.DiaryPage
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
// ============================================================

// region 日程页 (fragment_schedule_inbook.xml + item_schedule_item_in_book.xml)
// 结构：RecyclerView 垂直列表，每行一天
// 每天结构：24.5dp日期列 + 2列×3行目标槽(paddingVertical=3.5pt≈4.7dp)
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

    Column(
        modifier = modifier
            .graphicsLayer {
                translationX = shift
                this.alpha = alpha
            }
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        if (groupedByDay.isEmpty()) {
            // 无日程数据时，显示页面标题对应的items
            page.items.take(6).forEach { item ->
                Text(
                    item,
                    fontSize = 9.sp,
                    color = GoaldayDesign.adaptiveInkPrimary,
                    modifier = Modifier.padding(vertical = 2.dp, horizontal = 10.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        } else {
            groupedByDay.forEach { (dayMonth, entries) ->
                val (day, month) = dayMonth
                val date = LocalDate.of(today.year, month, day)
                val weekdayNames = listOf("一", "二", "三", "四", "五", "六", "日")
                val weekday = weekdayNames.getOrElse(date.dayOfWeek.value - 1) { "" }
                InBookScheduleDayRow(
                    day = day,
                    weekday = weekday,
                    entries = entries,
                    pageTitle = page.title,
                    isChecked = isChecked,
                )
            }
        }
        // 底部留白（对照 RecyclerView marginBottom=30dp）
        Box(modifier = Modifier.height(30.dp))
    }
}

// 对照 item_schedule_item_in_book.xml
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
        // 左侧日期列：24.5dp 宽
        // 对照 ConstraintLayout: 9sp日期 + 9sp分隔线 + 6sp周几
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
                    color = GoaldayDesign.adaptiveInkSecondary,
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
        // 对照：2个 LinearLayout(weight=1), paddingVertical=3.5pt≈4.7dp
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
    Column(
        modifier = modifier.padding(vertical = 4.7.dp),
    ) {
        for (i in 0 until 3) {
            val entry = entries.getOrNull(i)
            // 对照 item_schedule_item_in_book.xml: layout_height=12.0pt≈16dp
            Row(
                modifier = Modifier.height(16.dp),
                verticalAlignment = Alignment.Top,
            ) {
                if (entry != null) {
                    val checked = entry.completed || isChecked(pageTitle, entry.title)
                    // 勾选框 9×9dp, marginStart=2dp, marginTop=2dp
                    Box(
                        modifier = Modifier
                            .padding(start = 2.dp, top = 2.dp)
                            .size(9.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (checked) GoaldayDesign.Positive else Color.Transparent)
                            .border(0.5.dp, GoaldayDesign.adaptiveInkMuted, RoundedCornerShape(2.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (checked) {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(7.dp),
                            )
                        }
                    }
                    // 目标文字
                    Text(
                        entry.title,
                        fontSize = 9.sp,
                        color = if (checked) GoaldayDesign.adaptiveInkMuted else GoaldayDesign.adaptiveInkPrimary,
                        textDecoration = if (checked) TextDecoration.LineThrough else TextDecoration.None,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .padding(start = 2.dp)
                            .weight(1f),
                    )
                }
            }
        }
    }
}
// endregion

// region 日记页 (fragment_diary_inbook.xml + item_diary_*.xml)
// 结构：RecyclerView(marginTop=5dp, marginBottom=30dp, marginStart/End=7.5pt≈10dp) + 底部图片栏(23pt≈30.7dp)
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
        // 内容区：对照 fragment_diary_inbook.xml RecyclerView
        // marginTop=5dp, marginBottom=30dp, marginStart/End=7.5pt(10dp)
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(top = 5.dp, bottom = 30.dp, start = 10.dp, end = 10.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(5.dp),  // 对照 marginBottom=5dp
            ) {
                // 对照 item_diary_text.xml：16sp #2C2C2C, lineSpacingExtra=2dp→lineHeight=18sp
                val plainText = diaryDraft.ifBlank { page.prompt }
                plainText.split("\n").filter { it.isNotBlank() }.forEach { line ->
                    Text(
                        line,
                        fontSize = 16.sp,
                        color = GoaldayDesign.DiarySectionInk,
                        lineHeight = 18.sp,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
        // 底部图片栏：对照 fl_bottom_bar: 23pt(30.7dp) 高, 背景@2131100579(白)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.7.dp)
                .background(GoaldayDesign.adaptiveSurface),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // fl_select_pic: 23pt(30.7dp) × 23pt, marginStart=3.75pt(5dp)
            Box(
                modifier = Modifier
                    .padding(start = 5.dp)
                    .size(30.7.dp),
                contentAlignment = Alignment.Center,
            ) {
                // ic_select_pic: 12.5pt(16.7dp) × 12.5pt, marginStart=3.75pt(5dp)
                Image(
                    painter = painterResource(R.drawable.ic_select_pic),
                    contentDescription = "插入图片",
                    modifier = Modifier
                        .size(16.7.dp)
                        .padding(start = 5.dp),
                )
            }
        }
    }
}
// endregion

// region 目标页 (item_target_detail.xml 简化版, 无滑动删除)
// 结构：列表，每项 → 勾选框 + 目标文字(20sp) + 底部分隔线(4dp)
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
) {
    val eased = turnProgress * turnProgress * (3f - 2f * turnProgress)
    val shift = when (turnDirection) {
        TurnDirection.NEXT -> -(eased * 8f)
        TurnDirection.PREVIOUS -> eased * 8f
        null -> 0f
    }
    val alpha = (1f - eased * 0.08f).coerceIn(0.92f, 1f)

    val items = remember(page.items, customPageItems) {
        (page.items + customPageItems).distinct()
    }

    Column(
        modifier = modifier
            .graphicsLayer {
                translationX = shift
                this.alpha = alpha
            }
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        items.forEach { item ->
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

// 对照 item_target_detail.xml（简化，无 SwipeRevealLayout）
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
            // 勾选框（对照原版自定义drawable样式）
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (checked) GoaldayDesign.Positive else Color.Transparent)
                    .border(1.dp, GoaldayDesign.adaptiveInkMuted, RoundedCornerShape(4.dp))
                    .clickable { onToggleChecked() },
                contentAlignment = Alignment.Center,
            ) {
                if (checked) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
            // 目标文字：对照 tv_content textSize=20sp
            Text(
                item,
                fontSize = 20.sp,
                color = if (checked) GoaldayDesign.adaptiveInkMuted else GoaldayDesign.adaptiveInkPrimary,
                textDecoration = if (checked) TextDecoration.LineThrough else TextDecoration.None,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .padding(start = 10.dp)
                    .weight(1f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        // 底部分隔线：对照 view height=4dp, translationY=3dp
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .graphicsLayer { translationY = 3f }
                .background(GoaldayDesign.adaptiveDivider),
        )
    }
}
// endregion
