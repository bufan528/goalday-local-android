package com.bf410.goaldaylocal.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bf410.goaldaylocal.ui.book.BookViewModel
import java.time.LocalDate

@Composable
fun HomeScreen(
    viewModel: BookViewModel,
    onOpenCalendar: () -> Unit,
    onOpenInspiration: () -> Unit,
    onOpenHandbook: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val today = LocalDate.now()
    var quickInput by remember { mutableStateOf("") }
    val weekDays = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
    val weekNumbers = (0..6).map { today.plusDays(it.toLong()).dayOfMonth }
    val leftItems = (uiState.todayCompletedItems + uiState.todayPlanItems).take(7)
    val rightItems = (uiState.todayPlanItems + uiState.todayCompletedItems).distinct().take(10)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF4EEEC), RoundedCornerShape(10.dp))
                .border(1.dp, Color(0x16000000), RoundedCornerShape(10.dp))
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SegChip("日程", true, Modifier.weight(1f))
            SegChip("${today.monthValue}月${today.dayOfMonth}日", false, Modifier.weight(1f))
            SegChip("清单", false, Modifier.weight(1f))
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            Column(
                modifier = Modifier
                    .weight(1.15f)
                    .fillMaxSize()
                    .border(1.dp, Color(0x18000000)),
            ) {
                weekDays.forEachIndexed { index, day ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .border(0.5.dp, Color(0x12000000))
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Column(modifier = Modifier.width(34.dp)) {
                            Text(weekNumbers[index].toString(), fontWeight = FontWeight.SemiBold, color = Color(0xFF26221D))
                            Text(day, style = MaterialTheme.typography.labelSmall, color = Color(0xFF7B756E))
                        }
                        Text(
                            text = leftItems.getOrNull(index) ?: "",
                            color = Color(0xFF37312A),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .border(1.dp, Color(0x18000000)),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "To do  ˅",
                        modifier = Modifier
                            .background(Color(0xFFF9F9F7), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0x16000000), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                    Text("完成", modifier = Modifier.clickable { rightItems.firstOrNull()?.let(viewModel::moveItemToCompleted) })
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    rightItems.forEach { item ->
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("·", color = Color(0xFFD8CFC5))
                            Text(item, modifier = Modifier.clickable { viewModel.moveItemToCompleted(item) })
                        }
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(12.dp)
                        .size(30.dp)
                        .background(Color(0x12000000), RoundedCornerShape(99.dp))
                        .clickable(onClick = onOpenHandbook),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("›", color = Color(0xFF5F5850))
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = quickInput,
                onValueChange = { quickInput = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("快速新增 Todo") },
                singleLine = true,
            )
            TextButton(
                onClick = {
                    viewModel.addQuickTodo(quickInput)
                    quickInput = ""
                },
            ) { Text("添加") }
        }
    }
}

@Composable
private fun SegChip(
    text: String,
    active: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(if (active) Color.White else Color.Transparent, RoundedCornerShape(8.dp))
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = if (active) Color(0xFF2D2823) else Color(0xFF9D958B), fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal)
    }
}
