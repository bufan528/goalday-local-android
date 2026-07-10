package com.bf410.goaldaylocal.ui.replica

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GoaldayTopBar(
    leftTitle: String = "14周",
    rightPrimaryText: String = "完成",
    onRightPrimaryClick: () -> Unit,
    rightSecondary: (@Composable RowScope.() -> Unit)? = null,
    onBackClick: (() -> Unit)? = null,
) {
    // 对照 toolbar_normal.xml：back(30dip h) + 标题居中(18dip, fontWeight=600) + more(27dip h) + 完成
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(top = GoaldayDesign.Space1 + 2.dp)
            .fillMaxWidth(),
    ) {
        // 左侧：可选返回按钮（padding 20dp v / 30dp h 在 30dip 高区域内）
        if (onBackClick != null) {
            Box(
                modifier = Modifier
                    .height(30.dp)
                    .padding(vertical = 0.dp, horizontal = 30.dp)
                    .clickable(onClick = onBackClick),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "返回",
                    tint = GoaldayDesign.adaptiveInkPrimary,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
        // 中间：标题居中 18sp fontWeight=600
        Text(
            leftTitle,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = GoaldayDesign.adaptiveInkPrimary,
            maxLines = 1,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f),
        )
        // 右侧：more + 完成
        Row(horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space2), verticalAlignment = Alignment.CenterVertically) {
            rightSecondary?.invoke(this)
            // 完成按钮：bg_toolbar_complete（主色橙黄），padding 5dp v / 25dp h
            Text(
                rightPrimaryText,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                modifier = Modifier
                    .background(GoaldayDesign.MorandiOrange, RoundedCornerShape(GoaldayDesign.RadiusPill))
                    .clickable(onClick = onRightPrimaryClick)
                    .padding(horizontal = 25.dp, vertical = 5.dp),
            )
        }
    }
}
