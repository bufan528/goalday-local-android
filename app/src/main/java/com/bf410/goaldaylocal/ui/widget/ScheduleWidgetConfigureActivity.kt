package com.bf410.goaldaylocal.ui.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bf410.goaldaylocal.R

class ScheduleWidgetConfigureActivity : ComponentActivity() {
    private var appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID
    private var configureKind: WidgetConfigureKind = WidgetConfigureKind.SCHEDULE_MID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(Activity.RESULT_CANCELED)
        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID,
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }
        val manager = AppWidgetManager.getInstance(this)
        configureKind = when (manager.getAppWidgetInfo(appWidgetId)?.initialLayout) {
            R.layout.widget_schedule_large -> WidgetConfigureKind.SCHEDULE_LARGE
            R.layout.widget_quick_diary -> WidgetConfigureKind.DIARY_ADD
            else -> WidgetConfigureKind.SCHEDULE_MID
        }
        setContent {
            MaterialTheme {
                ScheduleWidgetConfigureScreen(
                    kind = configureKind,
                    initialConfig = ScheduleWidgetProvider.loadConfig(appWidgetId),
                    onConfirm = { config ->
                        saveAndClose(config)
                    },
                )
            }
        }
    }

    private fun saveAndClose(config: ScheduleWidgetConfig) {
        ScheduleWidgetProvider.saveConfig(appWidgetId, config)
        val manager = AppWidgetManager.getInstance(this)
        val info = manager.getAppWidgetInfo(appWidgetId)
        val views = when (info?.initialLayout) {
            R.layout.widget_schedule_large -> ScheduleWidgetProvider.buildLargeRemoteViews(this, appWidgetId)
            R.layout.widget_quick_diary -> QuickDiaryWidgetProvider.buildRemoteViews(this, appWidgetId)
            else -> ScheduleWidgetProvider.buildRemoteViews(this, appWidgetId)
        }
        manager.updateAppWidget(appWidgetId, views)
        val result = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(Activity.RESULT_OK, result)
        finish()
    }
}

private enum class WidgetConfigureKind(
    val title: String,
    val subtitle: String,
) {
    SCHEDULE_MID("日程中号组件", "适合放在首页，快速查看当前计划。"),
    SCHEDULE_LARGE("日程大号组件", "显示更多行，适合周计划和手账桌面。"),
    DIARY_ADD("日记添加组件", "一键进入今日记录，保留同一套颜色风格。"),
}

@Composable
private fun ScheduleWidgetConfigureScreen(
    kind: WidgetConfigureKind,
    initialConfig: ScheduleWidgetConfig,
    onConfirm: (ScheduleWidgetConfig) -> Unit,
) {
    var selectedStyle by remember { mutableStateOf(initialConfig.style) }
    var selectedScope by remember { mutableStateOf(initialConfig.scope) }
    var selectedDensity by remember { mutableStateOf(initialConfig.density) }
    val config = ScheduleWidgetConfig(selectedStyle, selectedScope, selectedDensity)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF8EF))
            .verticalScroll(rememberScrollState())
            .padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(kind.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold, color = Color(0xFF2F2922))
            Text(kind.subtitle, style = MaterialTheme.typography.bodySmall, color = Color(0xFF7A7065))
        }
        WidgetPreviewCard(kind = kind, config = config)
        ConfigureSectionTitle("颜色")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            ScheduleWidgetStyle.entries.forEach { style ->
                StyleSwatch(
                    style = style,
                    selected = selectedStyle == style,
                    onClick = { selectedStyle = style },
                )
            }
        }
        if (kind != WidgetConfigureKind.DIARY_ADD) {
            ConfigureSectionTitle("显示范围")
            SegmentedPicker(
                items = ScheduleWidgetScope.entries,
                selected = selectedScope,
                label = { it.label },
                onSelect = { selectedScope = it },
            )
            ConfigureSectionTitle("行距密度")
            SegmentedPicker(
                items = ScheduleWidgetDensity.entries,
                selected = selectedDensity,
                label = { it.label },
                onSelect = { selectedDensity = it },
            )
        } else {
            ConfigureSectionTitle("日记入口")
            Text(
                "日记组件使用同一套颜色配置，点击桌面组件会直接进入本地日记页。",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF7A7065),
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.72f))
                    .border(0.7.dp, Color(0x18B7A893), RoundedCornerShape(14.dp))
                    .padding(12.dp),
            )
        }
        Text(
            "添加到桌面",
            style = MaterialTheme.typography.titleSmall,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Color(selectedStyle.accentColor))
                .clickable { onConfirm(config) }
                .padding(vertical = 13.dp),
        )
    }
}

@Composable
private fun ConfigureSectionTitle(title: String) {
    Text(title, style = MaterialTheme.typography.labelMedium, color = Color(0xFF6F5C4C), fontWeight = FontWeight.SemiBold)
}

@Composable
private fun WidgetPreviewCard(
    kind: WidgetConfigureKind,
    config: ScheduleWidgetConfig,
) {
    val style = config.style
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Color(style.backgroundColor))
            .border(1.dp, Color(style.accentColor).copy(alpha = 0.22f), RoundedCornerShape(22.dp))
            .padding(15.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.weight(1f)) {
                Text(if (kind == WidgetConfigureKind.DIARY_ADD) "记录今天" else style.title, color = Color(style.titleColor), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(if (kind == WidgetConfigureKind.DIARY_ADD) "打开日记页 · 本地保存" else "${config.scope.label} · ${config.density.label}", color = Color(style.subtitleColor), style = MaterialTheme.typography.labelSmall)
            }
            Text(
                if (kind == WidgetConfigureKind.DIARY_ADD) "6/4" else config.scope.shortLabel,
                color = Color(style.accentColor),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clip(RoundedCornerShape(99.dp))
                    .background(Color.White.copy(alpha = 0.6f))
                    .padding(horizontal = 9.dp, vertical = 5.dp),
            )
        }
        if (kind == WidgetConfigureKind.DIARY_ADD) {
            Text(
                "补一条文字、目标或图片块",
                color = Color(style.doneTextColor),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(13.dp))
                    .background(Color.White.copy(alpha = 0.55f))
                    .padding(10.dp),
            )
        } else {
            repeat(if (kind == WidgetConfigureKind.SCHEDULE_LARGE) 4 else config.density.smallRows) { index ->
                PreviewScheduleRow(
                    text = listOf("09:30 整理今天目标", "明天 写一页复盘", "6/7 打印手账长图", "本周 清空待办池").getOrElse(index) { "记录本地计划" },
                    style = style,
                    completed = index == 2,
                )
            }
        }
    }
}

@Composable
private fun PreviewScheduleRow(
    text: String,
    style: ScheduleWidgetStyle,
    completed: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(11.dp))
            .background(Color.White.copy(alpha = 0.38f))
            .padding(horizontal = 9.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("●", color = Color(if (completed) style.doneColor else style.accentColor), style = MaterialTheme.typography.labelSmall)
        Text(text, color = Color(if (completed) style.doneTextColor else style.titleColor), style = MaterialTheme.typography.labelSmall, maxLines = 1)
    }
}

@Composable
private fun StyleSwatch(
    style: ScheduleWidgetStyle,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .width(126.dp)
            .height(92.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(style.backgroundColor))
            .border(
                width = if (selected) 2.dp else 0.7.dp,
                color = if (selected) Color(style.accentColor) else Color(0x24B7A893),
                shape = RoundedCornerShape(16.dp),
            )
            .clickable(onClick = onClick)
            .padding(10.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            Box(Modifier.width(18.dp).height(18.dp).clip(RoundedCornerShape(99.dp)).background(Color(style.accentColor)))
            Box(Modifier.width(18.dp).height(18.dp).clip(RoundedCornerShape(99.dp)).background(Color(style.doneColor)))
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(style.label, color = Color(style.titleColor), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            Text(if (selected) "已选择" else style.title, color = Color(style.subtitleColor), style = MaterialTheme.typography.labelSmall, maxLines = 1)
        }
    }
}

@Composable
private fun <T> SegmentedPicker(
    items: List<T>,
    selected: T,
    label: (T) -> String,
    onSelect: (T) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.72f))
            .border(0.7.dp, Color(0x18B7A893), RoundedCornerShape(16.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        items.forEach { item ->
            val active = item == selected
            Text(
                label(item),
                color = if (active) Color.White else Color(0xFF6F5C4C),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (active) Color(0xFFE88FAE) else Color.Transparent)
                    .clickable { onSelect(item) }
                    .padding(vertical = 9.dp),
            )
        }
    }
}
