package com.bf410.goaldaylocal.ui.book

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bf410.goaldaylocal.data.ScheduleEntry
import java.time.LocalDate
import java.time.temporal.WeekFields
import java.util.Locale

private val PageCornerRadius = 10.dp
private val PageShadowElevation = 10.dp
private val PageShadowColor = Color(0xFFC5BBB6)
private val BookSpineWidth = 2.dp
private val TabDividerColor = Color(0xFFC5BBB6)

private val FabricBackground = Brush.verticalGradient(
    listOf(
        Color(0xFFEDE4DA),
        Color(0xFFE5DAD0),
        Color(0xFFE0D5CB),
    )
)

@Composable
fun OriginalHandbookView(
    scheduleEntries: List<ScheduleEntry> = emptyList(),
    onToggleCompleted: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var currentDate by remember { mutableStateOf(LocalDate.now()) }

    LaunchedEffect(Unit) {
        val monday = currentDate.with(WeekFields.of(Locale.CHINA).dayOfWeek(), 1)
        currentDate = monday
    }

    val weekStartDate = currentDate.with(WeekFields.of(Locale.CHINA).dayOfWeek(), 1)
    val monthLabel = "${weekStartDate.monthValue}月"
    val weekOfYear = weekStartDate.get(WeekFields.of(Locale.CHINA).weekOfWeekBasedYear())

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(FabricBackground),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(32.dp))
            Text(
                text = monthLabel,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF3A3A3A),
            )
            Spacer(Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center,
            ) {
                BookSpread(
                    weekStartDate = weekStartDate,
                    weekOfYear = weekOfYear,
                    scheduleEntries = scheduleEntries,
                    onToggleCompleted = onToggleCompleted,
                )
            }

            Spacer(Modifier.height(40.dp))
        }

        BottomBar()
    }
}

@Composable
private fun BookSpread(
    weekStartDate: LocalDate,
    weekOfYear: Int,
    scheduleEntries: List<ScheduleEntry>,
    onToggleCompleted: (String) -> Unit,
) {
    val pageShape = RoundedCornerShape(
        topStart = 0.dp,
        topEnd = PageCornerRadius,
        bottomEnd = PageCornerRadius,
        bottomStart = 0.dp,
    )

    val leftPageShape = RoundedCornerShape(
        topStart = PageCornerRadius,
        topEnd = 0.dp,
        bottomEnd = 0.dp,
        bottomStart = PageCornerRadius,
    )

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        LeftPageStack()
        RightPageStack()

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .shadow(
                        elevation = PageShadowElevation,
                        shape = leftPageShape,
                        clip = false,
                        ambientColor = PageShadowColor,
                        spotColor = PageShadowColor,
                    )
                    .clip(leftPageShape)
                    .background(Color.White)
                    .padding(start = 10.dp),
            ) {
                LeftPageContent(
                    weekStartDate = weekStartDate,
                    weekOfYear = weekOfYear,
                    scheduleEntries = scheduleEntries,
                    onToggleCompleted = onToggleCompleted,
                )
            }

            Box(
                modifier = Modifier
                    .width(BookSpineWidth)
                    .fillMaxHeight()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFFB8A89A),
                                Color(0xFF9A8B7D),
                                Color(0xFFB8A89A),
                            )
                        )
                    )
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .shadow(
                        elevation = PageShadowElevation,
                        shape = pageShape,
                        clip = false,
                        ambientColor = PageShadowColor,
                        spotColor = PageShadowColor,
                    )
                    .clip(pageShape)
                    .background(Color.White)
                    .padding(end = 10.dp),
            ) {
                RightPageContent(date = weekStartDate)
            }
        }
    }
}

@Composable
private fun LeftPageStack() {
    Box(Modifier.fillMaxWidth()) {
        repeat(3) { layer ->
            val offsetX = (layer + 1).dp * 3f
            val offsetY = (layer + 1).dp * 1f
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.48f)
                    .fillMaxHeight()
                    .offset(x = offsetX, y = offsetY)
                    .clip(
                        RoundedCornerShape(
                            topStart = PageCornerRadius,
                            topEnd = 0.dp,
                            bottomEnd = 0.dp,
                            bottomStart = PageCornerRadius,
                        )
                    )
                    .background(Color.White.copy(alpha = 0.9f - layer * 0.2f)),
            )
        }
    }
}

@Composable
private fun RightPageStack() {
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
        repeat(3) { layer ->
            val offsetX = -((layer + 1).dp * 3f)
            val offsetY = (layer + 1).dp * 1f
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.48f)
                    .fillMaxHeight()
                    .offset(x = offsetX, y = offsetY)
                    .clip(
                        RoundedCornerShape(
                            topStart = 0.dp,
                            topEnd = PageCornerRadius,
                            bottomEnd = PageCornerRadius,
                            bottomStart = 0.dp,
                        )
                    )
                    .background(Color.White.copy(alpha = 0.9f - layer * 0.2f)),
            )
        }
    }
}

@Composable
private fun LeftPageContent(
    weekStartDate: LocalDate,
    weekOfYear: Int,
    scheduleEntries: List<ScheduleEntry>,
    onToggleCompleted: (String) -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        Text(
            text = "${weekStartDate.monthValue}月 | 第${weekOfYear}周",
            fontSize = 12.sp,
            color = TabDividerColor,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 10.dp, top = 10.dp),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 35.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(TabDividerColor)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 5.dp)
            ) {
                for (i in 0 until 7) {
                    val date = weekStartDate.plusDays(i.toLong())
                    WeekScheduleRow(
                        date = date,
                        entries = scheduleEntries,
                        onToggleCompleted = onToggleCompleted,
                    )
                }
            }
        }
    }
}

@Composable
private fun WeekScheduleRow(
    date: LocalDate,
    entries: List<ScheduleEntry>,
    onToggleCompleted: (String) -> Unit,
) {
    val dayOfWeek = when (date.dayOfWeek.value) {
        1 -> "周一"
        2 -> "周二"
        3 -> "周三"
        4 -> "周四"
        5 -> "周五"
        6 -> "周六"
        else -> "周日"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.width(48.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            Text(
                text = "${date.dayOfMonth}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF333333),
            )
            Text(
                text = dayOfWeek,
                fontSize = 10.sp,
                color = Color(0xFF999999),
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .height(0.5.dp)
                .background(TabDividerColor.copy(alpha = 0.5f))
        )
    }
}

@Composable
private fun RightPageContent(date: LocalDate) {
    val dayOfWeek = when (date.dayOfWeek.value) {
        1 -> "周一"
        2 -> "周二"
        3 -> "周三"
        4 -> "周四"
        5 -> "周五"
        6 -> "周六"
        else -> "周日"
    }

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        Text(
            text = "${date.dayOfMonth} | $dayOfWeek",
            fontSize = 12.sp,
            color = TabDividerColor,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 10.dp, end = 10.dp),
        )
    }
}

@Composable
private fun BottomBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(Color(0xFFE5DAD0))
            .padding(horizontal = 20.dp),
    ) {
        Text(
            text = "⬆",
            fontSize = 20.sp,
            color = Color(0xFF5A5A5A),
            modifier = Modifier.align(Alignment.CenterStart),
        )
        Row(
            modifier = Modifier.align(Alignment.Center),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "2026",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF3A3A3A),
            )
            Text(
                text = " ▾",
                fontSize = 14.sp,
                color = Color(0xFF5A5A5A),
            )
        }
        Text(
            text = "返回",
            fontSize = 16.sp,
            color = Color(0xFF3A3A3A),
            modifier = Modifier.align(Alignment.CenterEnd),
        )
    }
}
