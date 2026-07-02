package com.bf410.goaldaylocal.ui.replica

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun GoaldaySegmentBar(
    items: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(GoaldayDesign.adaptiveSurfaceSoft, RoundedCornerShape(GoaldayDesign.RadiusM))
            .padding(horizontal = GoaldayDesign.Space2 + 2.dp, vertical = GoaldayDesign.Space2 + 1.dp),
        horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space2),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items.forEachIndexed { index, label ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(
                        if (index == selectedIndex) Color.White else Color.Transparent,
                        RoundedCornerShape(GoaldayDesign.RadiusS),
                    )
                    .clickable { onSelect(index) }
                    .padding(vertical = GoaldayDesign.Space1 + 2.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    color = if (index == selectedIndex) GoaldayDesign.adaptiveInkPrimary else GoaldayDesign.adaptiveInkMuted,
                    fontWeight = if (index == selectedIndex) FontWeight.SemiBold else FontWeight.Normal,
                )
            }
        }
    }
}
