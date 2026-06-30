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
import com.bf410.goaldaylocal.data.LocalStateStore
import com.bf410.goaldaylocal.data.ScheduleEntry
import com.tencent.mmkv.MMKV
import java.time.LocalDate

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
    val referenceSignal: String,
    val unlockSignal: String,
    val colorSignal: String,
) {
    SCHEDULE_MID(
        title = "日程中号组件",
        subtitle = "适合放在首页，快速查看当前计划。",
        referenceSignal = "schedule_mid_widget",
        unlockSignal = "unlock_schedule_mid_widget",
        colorSignal = "widget_schedule_add_color",
    ),
    SCHEDULE_LARGE(
        title = "日程大号组件",
        subtitle = "显示更多行，适合周计划和手账桌面。",
        referenceSignal = "schedule_larger_widget",
        unlockSignal = "unlock_schedule_larger_widget",
        colorSignal = "widget_schedule_add_color",
    ),
    DIARY_ADD(
        title = "日记添加组件",
        subtitle = "一键进入今日记录，保留同一套颜色风格。",
        referenceSignal = "diary_add_widget_configure",
        unlockSignal = "unlock_diary_add_widget",
        colorSignal = "widget_plan_add_color",
    ),
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
    val previewEntries = remember { LocalStateStore(MMKV.defaultMMKV()).scheduleEntries() }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF8EF))
            .verticalScroll(rememberScrollState())
            .padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text("添加小组件", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = Color(0xFFE88FAE))
            Text(kind.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold, color = Color(0xFF2F2922))
            Text(kind.subtitle, style = MaterialTheme.typography.bodySmall, color = Color(0xFF7A7065))
        }
        WidgetKindSignalStrip(kind = kind)
        WidgetLocalUnlockCard(kind = kind)
        WidgetPreviewCard(kind = kind, config = config, entries = previewEntries)
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
private fun WidgetKindSignalStrip(kind: WidgetConfigureKind) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.66f))
            .border(0.7.dp, Color(0x18B7A893), RoundedCornerShape(16.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            WidgetSignalPill(kind.referenceSignal, Color(0xFFB07A8F), Modifier.weight(1f))
            WidgetSignalPill(kind.colorSignal, Color(0xFFE88FAE), Modifier.weight(1f))
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            WidgetSignalPill(kind.unlockSignal, Color(0xFF6F8E68), Modifier.weight(1f))
            WidgetSignalPill("无 VIP 锁 · 纯本地", Color(0xFF6F8E68), Modifier.weight(1f))
        }
    }
}

@Composable
private fun WidgetSignalPill(
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Text(
        label,
        style = MaterialTheme.typography.labelSmall,
        color = color,
        maxLines = 1,
        textAlign = TextAlign.Center,
        modifier = modifier
            .clip(RoundedCornerShape(99.dp))
            .background(color.copy(alpha = 0.10f))
            .padding(horizontal = 7.dp, vertical = 5.dp),
    )
}

@Composable
private fun WidgetLocalUnlockCard(kind: WidgetConfigureKind) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF2F7EE))
            .border(0.7.dp, Color(0x266F8E68), RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text("本地全解锁", style = MaterialTheme.typography.labelMedium, color = Color(0xFF4E7547), fontWeight = FontWeight.SemiBold)
        Text(
            "${kind.unlockSignal} 在本地版直接可用，不展示 VIP、登录或服务器校验。",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF667660),
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
    entries: List<ScheduleEntry>,
) {
    val style = config.style
    val today = LocalDate.now()
    val scopedEntries = remember(entries, config.scope) {
        entries
            .filter { it.matchesWidgetScopeForPreview(today, config.scope) }
            .sortedWith(compareBy<ScheduleEntry>({ it.completed }, { it.year }, { it.month }, { it.day }, { it.timeText }, { it.title.lowercase() }))
    }
    val maxRows = if (kind == WidgetConfigureKind.SCHEDULE_LARGE) config.density.largeRows else config.density.smallRows
    val previewRows = scopedEntries.take(maxRows)
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
                Text(
                    if (kind == WidgetConfigureKind.DIARY_ADD) {
                        "打开日记页 · 本地保存 · 无 VIP 锁"
                    } else {
                        "${config.scope.label} · ${config.density.label} · ${style.dotResourceName}"
                    },
                    color = Color(style.subtitleColor),
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                )
            }
            Text(
                if (kind == WidgetConfigureKind.DIARY_ADD) "${today.monthValue}/${today.dayOfMonth}" else config.scope.shortLabel,
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
            if (previewRows.isEmpty()) {
                Text(
                    config.scope.previewEmptyText().replace("\n", " · "),
                    color = Color(style.doneTextColor),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(13.dp))
                        .background(Color.White.copy(alpha = 0.50f))
                        .padding(10.dp),
                )
            } else {
                previewRows.forEach { entry ->
                    PreviewScheduleRow(
                        text = entry.previewText(today, config.scope, config.density),
                        style = style,
                        completed = entry.completed,
                    )
                }
            }
        }
    }
}

private fun ScheduleWidgetScope.previewEmptyText(): String =
    when (this) {
        ScheduleWidgetScope.TODAY -> "今天还没有日程\n打开 Goalday，把目标排进今天"
        ScheduleWidgetScope.UPCOMING -> "未来7天还没有待办\n从目标页安排下一步"
        ScheduleWidgetScope.WEEK -> "本周还没有日程\n把本周计划放进手账"
    }

private fun ScheduleEntry.matchesWidgetScopeForPreview(today: LocalDate, scope: ScheduleWidgetScope): Boolean {
    val date = runCatching { LocalDate.of(year, month, day) }.getOrNull() ?: return false
    return when (scope) {
        ScheduleWidgetScope.TODAY -> date == today
        ScheduleWidgetScope.UPCOMING -> !date.isBefore(today) && java.time.temporal.ChronoUnit.DAYS.between(today, date) <= 6
        ScheduleWidgetScope.WEEK -> {
            val start = today.minusDays((today.dayOfWeek.value - 1).toLong())
            val end = start.plusDays(6)
            !date.isBefore(start) && !date.isAfter(end)
        }
    }
}

private fun ScheduleEntry.previewText(
    today: LocalDate,
    scope: ScheduleWidgetScope,
    density: ScheduleWidgetDensity,
): String {
    val datePrefix = if (scope == ScheduleWidgetScope.TODAY) {
        ""
    } else {
        val date = runCatching { LocalDate.of(year, month, day) }.getOrNull()
        when (date?.let { java.time.temporal.ChronoUnit.DAYS.between(today, it).toInt() }) {
            0 -> "今天 "
            1 -> "明天 "
            else -> "${month}/${day} "
        }
    }
    val time = timeText.takeIf { it.isNotBlank() }?.let { "$it " }.orEmpty()
    val noteText = if (density == ScheduleWidgetDensity.DETAILED) note.takeIf { it.isNotBlank() }?.let { " · $it" }.orEmpty() else ""
    return "$datePrefix$time$title$noteText"
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
            Text(if (selected) "已选择" else style.dotResourceName, color = Color(style.subtitleColor), style = MaterialTheme.typography.labelSmall, maxLines = 1)
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
