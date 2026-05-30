package com.bf410.goaldaylocal.ui.home

import androidx.compose.foundation.background
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF8F5EE), RoundedCornerShape(14.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "今天",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2E2A26),
                )
                Text(
                    text = "${today.monthValue}月${today.dayOfMonth}日",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF8A847B),
                )
            }
            Text(
                text = "左 Done · 右 Todo",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF6F675F),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            QuickEntry("日历", "查看整月安排", onOpenCalendar, Modifier.weight(1f))
            QuickEntry("灵感", "把想法变成行动", onOpenInspiration, Modifier.weight(1f))
            QuickEntry("手账", "翻页记录与回顾", onOpenHandbook, Modifier.weight(1f))
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF5EFE5), RoundedCornerShape(16.dp))
                .clickable(onClick = onOpenHandbook)
                .padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("打开手账本", color = Color(0xFF3A3027), fontWeight = FontWeight.SemiBold)
                Text("像翻书一样回顾与记录今天", color = Color(0xFF7F7468), style = MaterialTheme.typography.bodySmall)
            }
            Text("▦", color = Color(0xFF6D5642), style = MaterialTheme.typography.titleLarge)
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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            TaskColumn(
                title = "Done",
                accent = Color(0xFFA8B79B),
                items = uiState.todayCompletedItems,
                onItemClick = viewModel::restoreItemFromCompleted,
                modifier = Modifier.weight(1f),
            )
            TaskColumn(
                title = "Todo",
                accent = Color(0xFFE6B28D),
                items = uiState.todayPlanItems,
                onItemClick = viewModel::moveItemToCompleted,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun QuickEntry(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(Color(0xFFF8F5F0), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(title, fontWeight = FontWeight.SemiBold, color = Color(0xFF3B342C))
        Text(subtitle, style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B847C))
    }
}

@Composable
private fun TaskColumn(
    title: String,
    accent: Color,
    items: List<String>,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(Color(0xFFF8F8F6), RoundedCornerShape(18.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(accent, RoundedCornerShape(99.dp)),
            )
            Spacer(modifier = Modifier.size(6.dp))
            Text(title, fontWeight = FontWeight.SemiBold, color = Color(0xFF352F29))
        }
        if (items.isEmpty()) {
            Text(
                "暂无内容",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFAAA49B),
            )
        } else {
            items.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .clickable { onItemClick(item) }
                        .padding(horizontal = 10.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(accent, RoundedCornerShape(99.dp)),
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = item,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF4A443D),
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
    }
}
