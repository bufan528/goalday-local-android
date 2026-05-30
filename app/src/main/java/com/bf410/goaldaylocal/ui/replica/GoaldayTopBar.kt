package com.bf410.goaldaylocal.ui.replica

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun GoaldayTopBar(
    leftTitle: String = "14周",
    rightPrimaryText: String = "完成",
    onRightPrimaryClick: () -> Unit,
    rightSecondary: (@Composable RowScope.() -> Unit)? = null,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 2.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(leftTitle, style = MaterialTheme.typography.labelMedium, color = Color(0xFF7E776E))
            Text("|", style = MaterialTheme.typography.labelMedium, color = Color(0xFFD2CBC1))
            Text("Goalday", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = Color(0xFF2B2824))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            rightSecondary?.invoke(this)
            Text(
                rightPrimaryText,
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier
                    .background(Color(0xFF222222), RoundedCornerShape(99.dp))
                    .clickable(onClick = onRightPrimaryClick)
                    .padding(horizontal = 9.dp, vertical = 4.dp),
            )
        }
    }
}
