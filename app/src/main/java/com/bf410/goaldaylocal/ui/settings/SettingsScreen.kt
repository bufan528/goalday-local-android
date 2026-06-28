package com.bf410.goaldaylocal.ui.settings

import android.os.Handler
import android.os.Looper
import android.os.Process
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
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bf410.goaldaylocal.data.BackupManager
import com.bf410.goaldaylocal.data.BackupSnapshot
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    val scope = rememberCoroutineScope()
    var refreshTick by remember { mutableIntStateOf(0) }
    var selectedFont by remember { mutableStateOf(mmkv.decodeString(KEY_FONT_SIZE, "standard") ?: "standard") }
    var pendingRestore by remember { mutableStateOf<BackupSnapshot?>(null) }
    var pendingDelete by remember { mutableStateOf<BackupSnapshot?>(null) }

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

    fun createBackup() {
        scope.launch {
            val result = withContext(Dispatchers.IO) { manager.backupMmkv() }
            result.onSuccess {
                refreshBackups()
                Toast.makeText(context, "备份完成", Toast.LENGTH_SHORT).show()
            }.onFailure {
                Toast.makeText(context, it.message ?: "备份失败", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun restoreLatestBackup() {
        val latest = snapshots.firstOrNull()
        if (latest == null) {
            Toast.makeText(context, "暂无可恢复备份", Toast.LENGTH_SHORT).show()
            return
        }
        pendingRestore = latest
    }

    fun cleanupOldBackups() {
        val result = manager.cleanupOldBackups(keepLatest = 6)
        result.onSuccess { deleted ->
            refreshBackups()
            Toast.makeText(context, if (deleted > 0) "已清理 $deleted 个旧备份" else "没有需要清理的旧备份", Toast.LENGTH_SHORT).show()
        }.onFailure {
            Toast.makeText(context, it.message ?: "清理失败", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAF8F4))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SettingsHeroCard(
            backupSummary = manager.storageSummary(),
            latestBackup = latestBackup,
            backupCount = snapshots.size,
            onCreateBackup = ::createBackup,
            onRestoreLatest = ::restoreLatestBackup,
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
            BackupActionPanel(
                backupCount = snapshots.size,
                latestBackup = latestBackup,
                onCreate = ::createBackup,
                onRestoreLatest = ::restoreLatestBackup,
                onCleanup = ::cleanupOldBackups,
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
                onDelete = { pendingDelete = it },
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
                    val snapshotToRestore = snapshot
                    pendingRestore = null
                    scope.launch {
                        val result = withContext(Dispatchers.IO) { manager.restoreBackup(snapshotToRestore.absolutePath) }
                        result.onSuccess {
                            Toast.makeText(context, "恢复完成，应用将重启", Toast.LENGTH_LONG).show()
                            Handler(Looper.getMainLooper()).postDelayed({
                                MMKV.onExit()
                                Process.killProcess(Process.myPid())
                            }, 800)
                        }.onFailure {
                            Toast.makeText(context, it.message ?: "恢复失败", Toast.LENGTH_SHORT).show()
                        }
                    }
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

    pendingDelete?.let { snapshot ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("删除备份？") },
            text = {
                Text(
                    "${snapshot.name}\n${formatBackupDate(snapshot.modifiedAtMillis)} · ${BackupManager.formatBytes(snapshot.sizeBytes)}\n\n删除后无法从这个备份恢复。",
                    color = Color(0xFF5F564E),
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val result = manager.deleteBackup(snapshot.absolutePath)
                    result.onSuccess {
                        refreshBackups()
                        Toast.makeText(context, "已删除备份", Toast.LENGTH_SHORT).show()
                    }.onFailure {
                        Toast.makeText(context, it.message ?: "删除失败", Toast.LENGTH_SHORT).show()
                    }
                    pendingDelete = null
                }) {
                    Text("确认删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
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
    backupCount: Int,
    onCreateBackup: () -> Unit,
    onRestoreLatest: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(18.dp, RoundedCornerShape(26.dp), clip = false)
            .background(Color(0xFFFFECF3), RoundedCornerShape(26.dp))
            .border(1.dp, Color(0x33E88FAE), RoundedCornerShape(26.dp))
            .padding(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("LOCAL DATA CENTER", style = MaterialTheme.typography.labelSmall, color = Color(0xFFE88FAE), fontWeight = FontWeight.SemiBold)
                    Text("Goalday Local", style = MaterialTheme.typography.titleLarge, color = Color(0xFF2F261D), fontWeight = FontWeight.SemiBold)
                }
                Text(
                    "$backupCount",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .background(Color(0xFFE88FAE), RoundedCornerShape(14.dp))
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                )
            }
            Text("无服务器依赖，不设置付费锁。所有日程、手账、日记和组件数据保存在本机。", color = Color(0xFF6C3F50), style = MaterialTheme.typography.bodySmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                StatusPill(backupSummary, Color(0xFFB45E7A))
                StatusPill(latestBackup?.let { "最近 ${formatBackupDate(it.modifiedAtMillis)}" } ?: "暂无备份", Color(0xFF7A6E66))
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.42f), RoundedCornerShape(16.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.46f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 11.dp, vertical = 9.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LocalDataMetric("离线", "本机运行", Modifier.weight(1f))
                LocalDataMetric("手账", "本地保存", Modifier.weight(1f))
                LocalDataMetric("备份", "${backupCount} 份", Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                HeroActionButton("立即备份", Color(0xFF2F2923), Modifier.weight(1f), onCreateBackup)
                HeroActionButton("恢复最近", Color(0xFFE88FAE), Modifier.weight(1f), onRestoreLatest)
            }
        }
    }
}

@Composable
private fun LocalDataMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, color = Color(0xFFB45E7A), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
        Text(value, color = Color(0xFF4F433A), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun HeroActionButton(
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .height(36.dp)
            .shadow(5.dp, RoundedCornerShape(99.dp), clip = false)
            .background(color, RoundedCornerShape(99.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            color = Color.White,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun BackupActionPanel(
    backupCount: Int,
    latestBackup: BackupSnapshot?,
    onCreate: () -> Unit,
    onRestoreLatest: () -> Unit,
    onCleanup: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(18.dp), clip = false)
            .background(Color(0xFFFFFBF6), RoundedCornerShape(16.dp))
            .border(1.dp, Color(0x22B7A893), RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.weight(1f)) {
                Text("备份操作", style = MaterialTheme.typography.titleSmall, color = Color(0xFF2F261D), fontWeight = FontWeight.SemiBold)
                Text(latestBackup?.let { "最近 ${formatBackupDate(it.modifiedAtMillis)}" } ?: "暂无可恢复备份", color = Color(0xFF6C635A), style = MaterialTheme.typography.bodySmall)
            }
            StatusPill("$backupCount 个", Color(0xFFB45E7A))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(7.dp), modifier = Modifier.fillMaxWidth()) {
            BackupActionChip("创建", Color(0xFF2F2923), Modifier.weight(1f), onCreate)
            BackupActionChip("恢复最近", Color(0xFFE88FAE), Modifier.weight(1f), onRestoreLatest)
            BackupActionChip("清理旧备份", Color(0xFF8F684F), Modifier.weight(1f), onCleanup)
        }
    }
}

@Composable
private fun BackupActionChip(
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .height(34.dp)
            .background(color, RoundedCornerShape(99.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            color = Color.White,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 6.dp),
        )
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
                .shadow(8.dp, RoundedCornerShape(20.dp), clip = false)
                .background(Color(0xEEFFFFFF), RoundedCornerShape(20.dp))
                .border(1.dp, Color(0x24B7A893), RoundedCornerShape(20.dp))
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
            .shadow(3.dp, RoundedCornerShape(14.dp), clip = false)
            .background(Color(0xFFFFFFFF), RoundedCornerShape(14.dp))
            .border(1.dp, Color(0x14B7A893), RoundedCornerShape(14.dp))
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
            .shadow(3.dp, RoundedCornerShape(14.dp), clip = false)
            .background(Color.White, RoundedCornerShape(14.dp))
            .border(1.dp, Color(0x14B7A893), RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("字号", style = MaterialTheme.typography.titleSmall, color = Color(0xFF2F261D), fontWeight = FontWeight.SemiBold)
                Text("设置会保存在本机偏好中。", color = Color(0xFF6C635A), style = MaterialTheme.typography.bodySmall)
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
    onDelete: (BackupSnapshot) -> Unit,
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
                    .background(Color(0xFFFFFBF6), RoundedCornerShape(14.dp))
                    .border(1.dp, Color(0x18B7A893), RoundedCornerShape(14.dp))
                    .padding(12.dp),
            )
            return
        }
        snapshots.take(8).forEach { snapshot ->
            BackupHistoryRow(
                snapshot = snapshot,
                onRestore = { onRestore(snapshot) },
                onDelete = { onDelete(snapshot) },
            )
        }
    }
}

@Composable
private fun BackupHistoryRow(
    snapshot: BackupSnapshot,
    onRestore: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(14.dp), clip = false)
            .background(Color(0xFFFFFBF6), RoundedCornerShape(14.dp))
            .border(1.dp, Color(0x18B7A893), RoundedCornerShape(14.dp))
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
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(5.dp)) {
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
            Text(
                "删除",
                color = Color(0xFFA15E58),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clickable(onClick = onDelete)
                    .padding(horizontal = 10.dp, vertical = 3.dp),
            )
        }
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
            .background(Color.White.copy(alpha = 0.72f), RoundedCornerShape(99.dp))
            .border(1.dp, Color.White.copy(alpha = 0.58f), RoundedCornerShape(99.dp))
            .padding(horizontal = 9.dp, vertical = 4.dp),
    )
}

private fun formatBackupDate(millis: Long): String =
    SimpleDateFormat("MM月dd日 HH:mm", Locale.CHINA).format(Date(millis))
