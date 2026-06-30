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
import com.bf410.goaldaylocal.ui.book.PageTurnStyle
import com.bf410.goaldaylocal.ui.replica.GoaldayDesign
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val KEY_FONT_SIZE = "settings_font_size"
private const val KEY_PAGE_TURN_STYLE = "page_turn_style"
private const val KEY_DARK_MODE = "dark_mode"

private data class FontSizeOption(
    val key: String,
    val label: String,
    val previewSp: Int,
)

@Composable
fun SettingsScreen(
    onShowGuide: () -> Unit = {},
    onFontSizeChange: (String) -> Unit = {},
    onDarkModeChange: (String) -> Unit = {},
) {
    val context = LocalContext.current
    val manager = remember { BackupManager(context) }
    val mmkv = remember { MMKV.defaultMMKV() }
    val scope = rememberCoroutineScope()
    var refreshTick by remember { mutableIntStateOf(0) }
    var selectedFont by remember { mutableStateOf(mmkv.decodeString(KEY_FONT_SIZE, "standard") ?: "standard") }
    var selectedTurnStyle by remember {
        mutableStateOf(
            runCatching {
                PageTurnStyle.valueOf((mmkv.decodeString(KEY_PAGE_TURN_STYLE, "SIMULATION") ?: "SIMULATION").uppercase())
            }.getOrDefault(PageTurnStyle.SIMULATION).name
        )
    }
    val turnStyleOptions = remember {
        listOf(
            PageTurnStyleOption("SIMULATION", "仿真"),
            PageTurnStyleOption("COVER", "覆盖"),
            PageTurnStyleOption("SCROLL", "滚动"),
            PageTurnStyleOption("NONE", "无动画"),
        )
    }
    var selectedDarkMode by remember { mutableStateOf(mmkv.decodeString(KEY_DARK_MODE, "AUTO") ?: "AUTO") }
    val darkModeOptions = remember {
        listOf(
            DarkModeOption("AUTO", "跟随系统"),
            DarkModeOption("LIGHT", "浅色"),
            DarkModeOption("DARK", "深色"),
        )
    }
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
            .background(GoaldayDesign.AppBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = GoaldayDesign.Space4, vertical = GoaldayDesign.Space3),
        verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space3),
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
                meta = "本地",
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
                    onFontSizeChange(option.key)
                    Toast.makeText(context, "字号已设为：${option.label}", Toast.LENGTH_SHORT).show()
                },
            )
            PageTurnStyleMenu(
                options = turnStyleOptions,
                selected = selectedTurnStyle,
                onSelected = { option ->
                    selectedTurnStyle = option.key
                    mmkv.encode(KEY_PAGE_TURN_STYLE, option.key)
                    Toast.makeText(context, "翻页方式已设为：${option.label}", Toast.LENGTH_SHORT).show()
                },
            )
            DarkModeMenu(
                options = darkModeOptions,
                selected = selectedDarkMode,
                onSelected = { option ->
                    selectedDarkMode = option.key
                    mmkv.encode(KEY_DARK_MODE, option.key)
                    onDarkModeChange(option.key)
                    Toast.makeText(context, "外观已设为：${option.label}", Toast.LENGTH_SHORT).show()
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
                    color = GoaldayDesign.adaptiveInkSecondary,
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
                    color = GoaldayDesign.adaptiveInkSecondary,
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
            .shadow(
                elevation = GoaldayDesign.ShadowMedium,
                shape = RoundedCornerShape(GoaldayDesign.Radius2XL)
            )
            .background(GoaldayDesign.PinkSoft, RoundedCornerShape(GoaldayDesign.Radius2XL))
            .border(
                width = GoaldayDesign.Hairline,
                color = GoaldayDesign.Pink.copy(alpha = 0.20f),
                shape = RoundedCornerShape(GoaldayDesign.Radius2XL)
            )
            .padding(GoaldayDesign.Space4),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space2)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space1)) {
                    Text(
                        "本地数据中心",
                        style = MaterialTheme.typography.labelSmall,
                        color = GoaldayDesign.Pink,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "Goalday Local",
                        style = MaterialTheme.typography.titleLarge,
                        color = GoaldayDesign.InkPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    "$backupCount",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .background(GoaldayDesign.Pink, RoundedCornerShape(GoaldayDesign.RadiusM))
                        .padding(horizontal = GoaldayDesign.Space3, vertical = GoaldayDesign.Space2),
                )
            }
            Text(
                "无服务器依赖，不设置付费锁。所有日程、手账、日记和组件数据保存在本机。",
                color = GoaldayDesign.InkSecondary,
                style = MaterialTheme.typography.bodySmall
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space2),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusPill(backupSummary, GoaldayDesign.Pink)
                StatusPill(
                    latestBackup?.let { "最近 ${formatBackupDate(it.modifiedAtMillis)}" } ?: "暂无备份",
                    GoaldayDesign.InkSecondary
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.42f), RoundedCornerShape(GoaldayDesign.RadiusL))
                    .border(
                        width = GoaldayDesign.Hairline,
                        color = Color.White.copy(alpha = 0.46f),
                        shape = RoundedCornerShape(GoaldayDesign.RadiusL)
                    )
                    .padding(horizontal = GoaldayDesign.Space3, vertical = GoaldayDesign.Space2),
                horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space3),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LocalDataMetric("离线", "本机运行", Modifier.weight(1f))
                LocalDataMetric("手账", "本地保存", Modifier.weight(1f))
                LocalDataMetric("备份", "${backupCount} 份", Modifier.weight(1f))
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space2),
                modifier = Modifier.fillMaxWidth()
            ) {
                HeroActionButton("立即备份", GoaldayDesign.InkPrimary, Modifier.weight(1f), onCreateBackup)
                HeroActionButton("恢复最近", GoaldayDesign.Pink, Modifier.weight(1f), onRestoreLatest)
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
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space1)) {
        Text(label, color = GoaldayDesign.Pink, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
        Text(value, color = GoaldayDesign.InkPrimary, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
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
            .shadow(
                elevation = GoaldayDesign.ShadowSoft,
                shape = RoundedCornerShape(GoaldayDesign.RadiusPill)
            )
            .background(color, RoundedCornerShape(GoaldayDesign.RadiusPill))
            .clickable(onClick = onClick)
            .padding(horizontal = GoaldayDesign.Space3),
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
            .shadow(
                elevation = GoaldayDesign.ShadowSoft,
                shape = RoundedCornerShape(GoaldayDesign.RadiusXL)
            )
            .background(GoaldayDesign.Paper, RoundedCornerShape(GoaldayDesign.RadiusL))
            .border(
                width = GoaldayDesign.Hairline,
                color = GoaldayDesign.BorderColor.copy(alpha = 0.13f),
                shape = RoundedCornerShape(GoaldayDesign.RadiusL)
            )
            .padding(horizontal = GoaldayDesign.Space3, vertical = GoaldayDesign.Space3),
        verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space2),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space1),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    "备份操作",
                    style = MaterialTheme.typography.titleSmall,
                    color = GoaldayDesign.InkPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    latestBackup?.let { "最近 ${formatBackupDate(it.modifiedAtMillis)}" } ?: "暂无可恢复备份",
                    color = GoaldayDesign.InkSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            StatusPill("$backupCount 个", GoaldayDesign.Pink)
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space2),
            modifier = Modifier.fillMaxWidth()
        ) {
            BackupActionChip("创建", GoaldayDesign.PrimaryAction, Modifier.weight(1f), onCreate)
            BackupActionChip("恢复最近", GoaldayDesign.Pink, Modifier.weight(1f), onRestoreLatest)
            BackupActionChip("清理旧备份", GoaldayDesign.Danger, Modifier.weight(1f), onCleanup)
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
            .background(color, RoundedCornerShape(GoaldayDesign.RadiusPill))
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
            modifier = Modifier.padding(horizontal = GoaldayDesign.Space1 + 2.dp),
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space2)) {
        Text(
            title,
            style = MaterialTheme.typography.labelMedium,
            color = GoaldayDesign.InkSecondary,
            fontWeight = FontWeight.SemiBold
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = GoaldayDesign.ShadowSoft,
                    shape = RoundedCornerShape(GoaldayDesign.RadiusXL)
                )
                .background(Color.White.copy(alpha = 0.93f), RoundedCornerShape(GoaldayDesign.RadiusXL))
                .border(
                    width = GoaldayDesign.Hairline,
                    color = GoaldayDesign.BorderColor.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(GoaldayDesign.RadiusXL)
                )
                .padding(GoaldayDesign.Space3),
            verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space2),
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
            .shadow(
                elevation = GoaldayDesign.ShadowSoft / 2,
                shape = RoundedCornerShape(GoaldayDesign.RadiusM)
            )
            .background(Color.White, RoundedCornerShape(GoaldayDesign.RadiusM))
            .border(
                width = GoaldayDesign.Hairline,
                color = GoaldayDesign.BorderColor.copy(alpha = 0.08f),
                shape = RoundedCornerShape(GoaldayDesign.RadiusM)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = GoaldayDesign.Space3, vertical = GoaldayDesign.Space3),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space3),
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space1)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                color = GoaldayDesign.InkPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                subtitle,
                color = GoaldayDesign.InkSecondary,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Text(
            meta,
            color = GoaldayDesign.Pink,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold
        )
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
            .shadow(
                elevation = GoaldayDesign.ShadowSoft / 2,
                shape = RoundedCornerShape(GoaldayDesign.RadiusM)
            )
            .background(Color.White, RoundedCornerShape(GoaldayDesign.RadiusM))
            .border(
                width = GoaldayDesign.Hairline,
                color = GoaldayDesign.BorderColor.copy(alpha = 0.08f),
                shape = RoundedCornerShape(GoaldayDesign.RadiusM)
            )
            .padding(horizontal = GoaldayDesign.Space3, vertical = GoaldayDesign.Space3),
        verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space2),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "字号",
                    style = MaterialTheme.typography.titleSmall,
                    color = GoaldayDesign.InkPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "设置会保存在本机偏好中。",
                    color = GoaldayDesign.InkSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                options.firstOrNull { it.key == selected }?.label ?: "标准",
                color = GoaldayDesign.Pink,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space2)) {
            options.forEach { option ->
                val active = option.key == selected
                Text(
                    option.label,
                    color = if (active) Color.White else GoaldayDesign.InkSecondary,
                    fontSize = option.previewSp.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .background(
                            if (active) GoaldayDesign.Pink else GoaldayDesign.SurfaceSoft,
                            RoundedCornerShape(GoaldayDesign.RadiusPill)
                        )
                        .clickable { onSelected(option) }
                        .padding(horizontal = GoaldayDesign.Space3 + 1.dp, vertical = GoaldayDesign.Space1 + 2.dp),
                )
            }
        }
    }
}

private data class PageTurnStyleOption(
    val key: String,
    val label: String,
)

@Composable
private fun PageTurnStyleMenu(
    options: List<PageTurnStyleOption>,
    selected: String,
    onSelected: (PageTurnStyleOption) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = GoaldayDesign.ShadowSoft / 2,
                shape = RoundedCornerShape(GoaldayDesign.RadiusM)
            )
            .background(Color.White, RoundedCornerShape(GoaldayDesign.RadiusM))
            .border(
                width = GoaldayDesign.Hairline,
                color = GoaldayDesign.BorderColor.copy(alpha = 0.08f),
                shape = RoundedCornerShape(GoaldayDesign.RadiusM)
            )
            .padding(horizontal = GoaldayDesign.Space3, vertical = GoaldayDesign.Space3),
        verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space2),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "翻页方式",
                    style = MaterialTheme.typography.titleSmall,
                    color = GoaldayDesign.InkPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "仿真模式最贴近真实书本，覆盖/滚动更轻量。",
                    color = GoaldayDesign.InkSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                options.firstOrNull { it.key == selected }?.label ?: "仿真",
                color = GoaldayDesign.Pink,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space2)) {
            options.forEach { option ->
                val active = option.key == selected
                Text(
                    option.label,
                    color = if (active) Color.White else GoaldayDesign.InkSecondary,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (active) GoaldayDesign.Pink else GoaldayDesign.SurfaceSoft,
                            RoundedCornerShape(GoaldayDesign.RadiusPill)
                        )
                        .clickable { onSelected(option) }
                        .padding(vertical = GoaldayDesign.Space1 + 2.dp),
                )
            }
        }
    }
}

private data class DarkModeOption(
    val key: String,
    val label: String,
)

@Composable
private fun DarkModeMenu(
    options: List<DarkModeOption>,
    selected: String,
    onSelected: (DarkModeOption) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = GoaldayDesign.ShadowSoft / 2,
                shape = RoundedCornerShape(GoaldayDesign.RadiusM)
            )
            .background(Color.White, RoundedCornerShape(GoaldayDesign.RadiusM))
            .border(
                width = GoaldayDesign.Hairline,
                color = GoaldayDesign.BorderColor.copy(alpha = 0.08f),
                shape = RoundedCornerShape(GoaldayDesign.RadiusM)
            )
            .padding(horizontal = GoaldayDesign.Space3, vertical = GoaldayDesign.Space3),
        verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space2),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "深色模式",
                    style = MaterialTheme.typography.titleSmall,
                    color = GoaldayDesign.InkPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "跟随系统将按设备夜间模式自动切换。",
                    color = GoaldayDesign.InkSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                options.firstOrNull { it.key == selected }?.label ?: "跟随系统",
                color = GoaldayDesign.Pink,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space2)) {
            options.forEach { option ->
                val active = option.key == selected
                Text(
                    option.label,
                    color = if (active) Color.White else GoaldayDesign.InkSecondary,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (active) GoaldayDesign.Pink else GoaldayDesign.SurfaceSoft,
                            RoundedCornerShape(GoaldayDesign.RadiusPill)
                        )
                        .clickable { onSelected(option) }
                        .padding(vertical = GoaldayDesign.Space1 + 2.dp),
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
    Column(verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space2)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "备份历史",
                style = MaterialTheme.typography.titleSmall,
                color = GoaldayDesign.InkPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.width(GoaldayDesign.Space2))
            Text(
                "${snapshots.size} 个",
                style = MaterialTheme.typography.labelSmall,
                color = GoaldayDesign.InkSecondary
            )
        }
        if (snapshots.isEmpty()) {
            Text(
                "暂无备份。创建备份后，这里会显示可恢复的历史记录。",
                color = GoaldayDesign.InkSecondary,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GoaldayDesign.Paper, RoundedCornerShape(GoaldayDesign.RadiusM))
                    .border(
                        width = GoaldayDesign.Hairline,
                        color = GoaldayDesign.BorderColor.copy(alpha = 0.09f),
                        shape = RoundedCornerShape(GoaldayDesign.RadiusM)
                    )
                    .padding(GoaldayDesign.Space3),
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
            .shadow(
                elevation = GoaldayDesign.ShadowSoft / 2,
                shape = RoundedCornerShape(GoaldayDesign.RadiusM)
            )
            .background(GoaldayDesign.Paper, RoundedCornerShape(GoaldayDesign.RadiusM))
            .border(
                width = GoaldayDesign.Hairline,
                color = GoaldayDesign.BorderColor.copy(alpha = 0.09f),
                shape = RoundedCornerShape(GoaldayDesign.RadiusM)
            )
            .padding(horizontal = GoaldayDesign.Space3, vertical = GoaldayDesign.Space2),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space3),
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space1)
        ) {
            Text(
                snapshot.name,
                style = MaterialTheme.typography.bodySmall,
                color = GoaldayDesign.InkPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                "${formatBackupDate(snapshot.modifiedAtMillis)} · ${snapshot.fileCount} 文件 · ${BackupManager.formatBytes(snapshot.sizeBytes)}",
                color = GoaldayDesign.InkSecondary,
                style = MaterialTheme.typography.labelSmall,
            )
        }
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space1)
        ) {
            Text(
                "恢复",
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .background(GoaldayDesign.Pink, RoundedCornerShape(GoaldayDesign.RadiusPill))
                    .clickable(onClick = onRestore)
                    .padding(horizontal = GoaldayDesign.Space3, vertical = GoaldayDesign.Space1),
            )
            Text(
                "删除",
                color = GoaldayDesign.Danger,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clickable(onClick = onDelete)
                    .padding(horizontal = GoaldayDesign.Space3, vertical = 3.dp),
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
            .background(Color.White.copy(alpha = 0.72f), RoundedCornerShape(GoaldayDesign.RadiusPill))
            .border(
                width = GoaldayDesign.Hairline,
                color = Color.White.copy(alpha = 0.58f),
                shape = RoundedCornerShape(GoaldayDesign.RadiusPill)
            )
            .padding(horizontal = GoaldayDesign.Space2 + 1.dp, vertical = GoaldayDesign.Space1),
    )
}

private fun formatBackupDate(millis: Long): String =
    SimpleDateFormat("MM月dd日 HH:mm", Locale.CHINA).format(Date(millis))
