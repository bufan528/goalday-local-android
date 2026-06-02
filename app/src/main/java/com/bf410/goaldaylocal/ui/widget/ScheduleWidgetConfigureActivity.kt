package com.bf410.goaldaylocal.ui.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bf410.goaldaylocal.R
import com.tencent.mmkv.MMKV

class ScheduleWidgetConfigureActivity : ComponentActivity() {
    private var appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID

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
        setContent {
            MaterialTheme {
                ScheduleWidgetConfigureScreen(
                    onPick = { style ->
                        saveAndClose(style)
                    },
                )
            }
        }
    }

    private fun saveAndClose(style: ScheduleWidgetStyle) {
        MMKV.defaultMMKV().encode("${ScheduleWidgetProvider.KEY_WIDGET_STYLE_PREFIX}$appWidgetId", style.raw)
        val manager = AppWidgetManager.getInstance(this)
        val info = manager.getAppWidgetInfo(appWidgetId)
        val views = if (info?.initialLayout == R.layout.widget_schedule_large) {
            ScheduleWidgetProvider.buildLargeRemoteViews(this, appWidgetId)
        } else {
            ScheduleWidgetProvider.buildRemoteViews(this, appWidgetId)
        }
        manager.updateAppWidget(appWidgetId, views)
        val result = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(Activity.RESULT_OK, result)
        finish()
    }
}

@Composable
private fun ScheduleWidgetConfigureScreen(
    onPick: (ScheduleWidgetStyle) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF8EF))
            .padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text("选择日程小组件样式", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold, color = Color(0xFF2F2922))
        Text("添加到桌面后仍会自动读取今天的日程。", style = MaterialTheme.typography.bodySmall, color = Color(0xFF7A7065))
        ScheduleWidgetStyle.entries.forEach { style ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(style.backgroundColor))
                    .clickable { onPick(style) }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
                    Text(style.label, style = MaterialTheme.typography.titleMedium, color = Color(style.titleColor), fontWeight = FontWeight.SemiBold)
                    Text("${style.title} · 待办 3 · 完成 1", style = MaterialTheme.typography.bodySmall, color = Color(style.subtitleColor))
                }
                Text("选择", style = MaterialTheme.typography.labelMedium, color = Color(style.accentColor), fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
