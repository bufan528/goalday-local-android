package com.bf410.goaldaylocal.ui.inspiration

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bf410.goaldaylocal.ui.book.BookViewModel
import com.bf410.goaldaylocal.ui.book.InspirationTemplates

@Composable
fun InspirationScreen(
    viewModel: BookViewModel,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = "灵感中心",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF2E2A26),
        )
        Text(
            text = "一键加入今日 Todo",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF8A847B),
        )
        InspirationTemplates.all.forEach { template ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8F5EF), RoundedCornerShape(16.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(template.title, fontWeight = FontWeight.SemiBold, color = Color(0xFF332D27))
                Text(template.subtitle, style = MaterialTheme.typography.bodySmall, color = Color(0xFF877E74))
                template.items.take(3).forEach {
                    Text("• $it", style = MaterialTheme.typography.bodySmall, color = Color(0xFF5A524A))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    Text(
                        text = "加入今日",
                        color = Color(0xFFE07E95),
                        modifier = Modifier
                            .background(Color(0x1AE07E95), RoundedCornerShape(99.dp))
                            .clickable { viewModel.applyInspirationToToday(template.items) }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                    )
                }
            }
        }
    }
}
