package com.bf410.goaldaylocal.ui.inspiration

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
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
            .padding(top = 14.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "灵感中心",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF2E2A26),
        )
        InspirationTemplates.all.forEachIndexed { index, template ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(98.dp)
                    .background(
                        Brush.horizontalGradient(
                            when (index % 4) {
                                0 -> listOf(Color(0xFFCEC8BE), Color(0xFFA9A49D))
                                1 -> listOf(Color(0xFF8B7165), Color(0xFFB08E7D))
                                2 -> listOf(Color(0xFF5D4C3A), Color(0xFF8A745F))
                                else -> listOf(Color(0xFF8A869F), Color(0xFFA6A2BA))
                            },
                        ),
                        RoundedCornerShape(14.dp),
                    )
                    .border(1.dp, Color(0x24FFFFFF), RoundedCornerShape(14.dp))
                    .clickable { viewModel.applyInspirationToToday(template.items) }
                    .padding(horizontal = 14.dp, vertical = 10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(template.title, fontWeight = FontWeight.SemiBold, color = Color.White)
                        Text(template.subtitle, style = MaterialTheme.typography.bodySmall, color = Color(0xEFFFFFFF))
                    }
                    Text("添加", color = Color.White, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}
