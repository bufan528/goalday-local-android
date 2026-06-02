package com.bf410.goaldaylocal.ui.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bf410.goaldaylocal.data.BackupManager
import com.bf410.goaldaylocal.data.BackupSnapshot
import com.tencent.mmkv.MMKV
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val KEY_FONT_SIZE = "settings_font_size"

private data class FontSizeOption(
    val key: String,
    val label: String,
    val previewSp: Int,
)

@Composable
fun SettingsScreen(
    onShowGuide: () -> Unit = {},
) {
    val context = LocalContext.current
    val manager = remember { BackupManager(context) }
    val mmkv = remember { MMKV.defaultMMKV() }
    var refreshTick by remember { mutableIntStateOf(0) }
    var selectedFont by remember { mutableStateOf(mmkv.decodeString(KEY_FONT_SIZE, "standard") ?: "standard") }
    var pendingRestore by remember { mutableStateOf<BackupSnapshot?>(null) }

    val snapshots = remember(refreshTick) { manager.backupSnapshots() }
    val latestBackup = snapshots.firstOrNull()
    val fontOptions = remember {
        listOf(
            FontSizeOption("compact", "小", 13),
            FontSizeOption("standard", "标准", 15),
            FontSizeOption("large", "大", 17),
        )
    }

    fun refreshBackups() {
        refreshTick += 1
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("本地设置", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(top = 18.dp))
        SettingsHeroCard(
            backupSummary = manager.storageSummary(),
            latestBackup = latestBackup,
        )
        SettingsSection(title = "偏好") {
            SettingRow(
                title = "离线功能",
                subtitle = "主题中心、任务池、手账翻页、日程、日记、组件、备份均本地运行。",
                meta = "LOCAL",
                onClick = {
                    Toast.makeText(context, "当前版本已按本地离线模式运行", Toast.LENGTH_SHORT).show()
                },
            )
            SettingRow(
                title = "新手引导",
                subtitle = "重新查看目标、日程、日记、导出四步引导。",
                meta = "打开",
                onClick = onShowGuide,
            )
            FontSizeMenu(
                options = fontOptions,
                selected = selectedFont,
                onSelected = { option ->
                    selectedFont = option.key
                    mmkv.encode(KEY_FONT_SIZE, option.key)
                    Toast.makeText(context, "字号已设为：${option.label}", Toast.LENGTH_SHORT).show()
                },
            )
        }
        SettingsSection(title = "备份") {
            SettingRow(
                title = "立即备份",
                subtitle = "把当前 MMKV 本地数据复制到备份目录。",
                meta = "创建",
                onClick = {
                    val result = manager.backupMmkv()
                    result.onSuccess {
                        refreshBackups()
                        Toast.makeText(context, "备份完成", Toast.LENGTH_SHORT).show()
                    }.onFailure {
                        Toast.makeText(context, it.message ?: "备份失败", Toast.LENGTH_SHORT).show()
                    }
                },
            )
            SettingRow(
                title = "备份目录",
                subtitle = manager.backupRootPath(),
                meta = manager.storageSummary(),
                onClick = {
                    Toast.makeText(context, manager.backupRootPath(), Toast.LENGTH_LONG).show()
                },
            )
            BackupHistoryList(
                snapshots = snapshots,
                onRestore = { pendingRestore = it },
            )
        }
    }

    pendingRestore?.let { snapshot ->
        AlertDialog(
            onDismissRequest = { pendingRestore = null },
            title = { Text("恢复备份？") },
            text = {
                Text(
                    "${snapshot.name}\n${formatBackupDate(snapshot.modifiedAtMillis)} · ${BackupManager.formatBytes(snapshot.sizeBytes)}\n\n恢复会覆盖当前本地 MMKV 数据，完成后建议重启应用查看。",
                    color = Color(0xFF5F564E),
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val result = manager.restoreBackup(snapshot.absolutePath)
                    result.onSuccess {
                        refreshBackups()
                        Toast.makeText(context, "恢复完成，请重启应用查看", Toast.LENGTH_LONG).show()
                    }.onFailure {
                        Toast.makeText(context, it.message ?: "恢复失败", Toast.LENGTH_SHORT).show()
                    }
                    pendingRestore = null
                }) {
                    Text("确认恢复")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingRestore = null }) {
                    Text("取消")
                }
            },
        )
    }
}

@Composable
private fun SettingsHeroCard(
    backupSummary: String,
    latestBackup: BackupSnapshot?,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFFECF3), RoundedCornerShape(22.dp))
            .border(1.dp, Color(0x22E88FAE), RoundedCornerShape(22.dp))
            .padding(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Text("Goalday Local", style = MaterialTheme.typography.titleMedium, color = Color(0xFF2F261D), fontWeight = FontWeight.SemiBold)
            Text("无服务器依赖，不设置付费锁。所有日程、手账、日记和组件数据保存在本机。", color = Color(0xFF6C3F50))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                StatusPill(backupSummary, Color(0xFFB45E7A))
                StatusPill(latestBackup?.let { "最近 ${formatBackupDate(it.modifiedAtMillis)}" } ?: "暂无备份", Color(0xFF7A6E66))
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.labelMedium, color = Color(0xFF8B7A68), fontWeight = FontWeight.SemiBold)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0x66FFFFFF), RoundedCornerShape(18.dp))
                .border(1.dp, Color(0x18B7A893), RoundedCornerShape(18.dp))
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = content,
        )
    }
}

@Composable
private fun SettingRow(
    title: String,
    subtitle: String,
    meta: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0x73FFFFFF), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = Color(0xFF2F261D), fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = Color(0xFF6C635A), style = MaterialTheme.typography.bodySmall)
        }
        Text(meta, color = Color(0xFFB45E7A), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun FontSizeMenu(
    options: List<FontSizeOption>,
    selected: String,
    onSelected: (FontSizeOption) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0x73FFFFFF), RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("字号", style = MaterialTheme.typography.titleSmall, color = Color(0xFF2F261D), fontWeight = FontWeight.SemiBold)
                Text("设置已保存，后续可接入全局排版。", color = Color(0xFF6C635A), style = MaterialTheme.typography.bodySmall)
            }
            Text(
                options.firstOrNull { it.key == selected }?.label ?: "标准",
                color = Color(0xFFB45E7A),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            options.forEach { option ->
                val active = option.key == selected
                Text(
                    option.label,
                    color = if (active) Color.White else Color(0xFF6C635A),
                    fontSize = option.previewSp.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .background(if (active) Color(0xFFE88FAE) else Color(0xFFF7EFE6), RoundedCornerShape(99.dp))
                        .clickable { onSelected(option) }
                        .padding(horizontal = 13.dp, vertical = 6.dp),
                )
            }
        }
    }
}

@Composable
private fun BackupHistoryList(
    snapshots: List<BackupSnapshot>,
    onRestore: (BackupSnapshot) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("备份历史", style = MaterialTheme.typography.titleSmall, color = Color(0xFF2F261D), fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.width(8.dp))
            Text("${snapshots.size} 个", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B7A68))
        }
        if (snapshots.isEmpty()) {
            Text(
                "暂无备份。创建备份后，这里会显示可恢复的历史记录。",
                color = Color(0xFF7A6E66),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0x55FFFFFF), RoundedCornerShape(12.dp))
                    .padding(12.dp),
            )
            return
        }
        snapshots.take(6).forEach { snapshot ->
            BackupHistoryRow(snapshot = snapshot, onRestore = { onRestore(snapshot) })
        }
    }
}

@Composable
private fun BackupHistoryRow(
    snapshot: BackupSnapshot,
    onRestore: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0x55FFFFFF), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(snapshot.name, style = MaterialTheme.typography.bodySmall, color = Color(0xFF2F261D), fontWeight = FontWeight.SemiBold)
            Text(
                "${formatBackupDate(snapshot.modifiedAtMillis)} · ${snapshot.fileCount} 文件 · ${BackupManager.formatBytes(snapshot.sizeBytes)}",
                color = Color(0xFF7A6E66),
                style = MaterialTheme.typography.labelSmall,
            )
        }
        Text(
            "恢复",
            color = Color.White,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .background(Color(0xFFE88FAE), RoundedCornerShape(99.dp))
                .clickable(onClick = onRestore)
                .padding(horizontal = 10.dp, vertical = 5.dp),
        )
    }
}

@Composable
private fun StatusPill(
    text: String,
    color: Color,
) {
    Text(
        text,
        color = color,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .background(Color.White.copy(alpha = 0.62f), RoundedCornerShape(99.dp))
            .padding(horizontal = 9.dp, vertical = 4.dp),
    )
}

private fun formatBackupDate(millis: Long): String =
    SimpleDateFormat("MM月dd日 HH:mm", Locale.CHINA).format(Date(millis))
