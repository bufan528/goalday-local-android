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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bf410.goaldaylocal.data.BackupManager
import com.bf410.goaldaylocal.data.BackupSnapshot
import com.bf410.goaldaylocal.ui.replica.GoaldayDesign
import com.bf410.goaldaylocal.ui.replica.LocalGoaldayDarkMode
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// 对照原版 activity_setting.xml 的 MMKV 键
// 字体大小：保持 "settings_font_size" 以兼容 GoaldayApp 读取（值：compact/standard/large）
private const val KEY_FONT_SIZE = "settings_font_size"
// 暗色模式：保持 "dark_mode" 以兼容 GoaldayApp 读取（值：AUTO/LIGHT/DARK）
private const val KEY_DARK_MODE = "dark_mode"
// 日记图片尺寸：对照原版 "diary_image_size"（值：small/large）
private const val KEY_DIARY_IMAGE_SIZE = "diary_image_size"

private data class FontSizeOption(
    val key: String,
    val label: String,
    val previewSp: Int,
)

private data class DarkModeOption(
    val key: String,
    val label: String,
)

@Composable
fun SettingsScreen(
    onShowGuide: () -> Unit = {},
    onFontSizeChange: (String) -> Unit = {},
    onDarkModeChange: (String) -> Unit = {},
    onBack: () -> Unit = {},
    onOpenCalendar: () -> Unit = {},
) {
    val context = LocalContext.current
    val manager = remember { BackupManager(context) }
    val mmkv = remember { MMKV.defaultMMKV() }
    val scope = rememberCoroutineScope()
    var refreshTick by remember { mutableIntStateOf(0) }
    var selectedFont by remember { mutableStateOf(mmkv.decodeString(KEY_FONT_SIZE, "standard") ?: "standard") }
    var selectedDarkMode by remember { mutableStateOf(mmkv.decodeString(KEY_DARK_MODE, "AUTO") ?: "AUTO") }
    var selectedDiaryImageSize by remember {
        val raw = mmkv.decodeString(KEY_DIARY_IMAGE_SIZE, "large") ?: "large"
        val normalized = if (raw == "small") "small" else "large"
        if (raw != normalized) {
            mmkv.encode(KEY_DIARY_IMAGE_SIZE, normalized)
        }
        mutableStateOf(normalized)
    }
    var pendingRestore by remember { mutableStateOf<BackupSnapshot?>(null) }
    var pendingDelete by remember { mutableStateOf<BackupSnapshot?>(null) }
    var showBackupDialog by remember { mutableStateOf(false) }
    // 恢复后重启遮罩：直接杀进程像崩溃，先盖层说明再走
    var restarting by remember { mutableStateOf(false) }

    // 备份列表走 IO 线程读目录：remember 里直接调会卡主线程
    var snapshots by remember { mutableStateOf(listOf<BackupSnapshot>()) }
    LaunchedEffect(refreshTick) {
        snapshots = withContext(Dispatchers.IO) { manager.backupSnapshots() }
    }

    val fontOptions = remember {
        listOf(
            FontSizeOption("compact", "小", 14),
            FontSizeOption("standard", "中", 16),
            FontSizeOption("large", "大", 18),
        )
    }
    val imageSizeOptions = remember {
        listOf(
            FontSizeOption("small", "小", 14),
            FontSizeOption("large", "大", 16),
        )
    }
    val darkModeOptions = remember {
        listOf(
            DarkModeOption("AUTO", "跟随系统"),
            DarkModeOption("LIGHT", "浅色"),
            DarkModeOption("DARK", "深色"),
        )
    }

    val versionName = remember {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrDefault("1.0") ?: "1.0"
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

    fun cleanupOldBackups() {
        // 遍历删除跑 IO 线程，主线程直接调会卡死
        scope.launch {
            val result = withContext(Dispatchers.IO) { manager.cleanupOldBackups(keepLatest = 6) }
            result.onSuccess { deleted ->
                refreshBackups()
                Toast.makeText(context, if (deleted > 0) "已清理 $deleted 个旧备份" else "没有需要清理的旧备份", Toast.LENGTH_SHORT).show()
            }.onFailure {
                Toast.makeText(context, it.message ?: "清理失败", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 对照 activity_setting.xml：顶部 toolbar_normal + ScrollView(padding=20dp) 内容区
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GoaldayDesign.adaptiveAppBg),
    ) {
        // 顶部 toolbar_normal：返回箭头 + 标题（18sp 加粗）
        SettingsToolbar(onBack = onBack)

        // ScrollView 内容区，padding=20dp
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(13.dp),
        ) {
            // VIP 卡片图片位置：本地离线版无 VIP，替换为应用信息横幅
            AppInfoBanner(versionName = versionName, backupCount = snapshots.size)

            // 本地离线版无账号体系：账号分组（含用户信息）隐藏，不留死按钮

            // "通用" 分组标题
            SettingsGroupTitle("通用")
            // 通用分组卡片
            SettingsCard {
                // 语言行（默认隐藏，对照原版 visibility="gone"）
                // 字体大小行：标题"字体大小" + 三按钮组（小/中/大，38dp 宽高）
                FontSizeToggleRow(
                    options = fontOptions,
                    selected = selectedFont,
                    onSelected = { option ->
                        selectedFont = option.key
                        mmkv.encode(KEY_FONT_SIZE, option.key)
                        onFontSizeChange(option.key)
                        Toast.makeText(context, "字号已设为：${option.label}", Toast.LENGTH_SHORT).show()
                    },
                )
                SettingsDivider()
                // 数据迁移行
                SettingsNavRow(
                    title = "数据迁移",
                    onClick = { showBackupDialog = true },
                )
                SettingsDivider()
                // 导入日历行：跳到日历页做系统日历导入（对照原版 CalendarImport）
                SettingsNavRow(
                    title = "导入日历",
                    onClick = onOpenCalendar,
                )
                SettingsDivider()
                // 深色模式行（保留现有功能逻辑）
                DarkModeToggleRow(
                    options = darkModeOptions,
                    selected = selectedDarkMode,
                    onSelected = { option ->
                        selectedDarkMode = option.key
                        mmkv.encode(KEY_DARK_MODE, option.key)
                        onDarkModeChange(option.key)
                        Toast.makeText(context, "外观已设为：${option.label}", Toast.LENGTH_SHORT).show()
                    },
                )
                SettingsDivider()
                // 新手引导行（保留现有功能逻辑）
                SettingsNavRow(
                    title = "新手引导",
                    onClick = onShowGuide,
                )
            }

            // "日程" 分组标题（默认隐藏，对照原版 visibility="gone"）

            // "日记" 分组标题
            SettingsGroupTitle("日记")
            // 日记分组卡片
            SettingsCard {
                // 图片尺寸行：标题"图片尺寸" + 两按钮组（小/大，57dp 宽，35dp 高）
                ImageSizeToggleRow(
                    options = imageSizeOptions,
                    selected = selectedDiaryImageSize,
                    onSelected = { option ->
                        selectedDiaryImageSize = option.key
                        mmkv.encode(KEY_DIARY_IMAGE_SIZE, option.key)
                        Toast.makeText(context, "图片尺寸已设为：${option.label}", Toast.LENGTH_SHORT).show()
                    },
                )
            }

            // "联系我们" 分组标题
            SettingsGroupTitle("联系我们")
            // 联系我们分组卡片
            SettingsCard {
                // 本地离线版无反馈通道：意见反馈行隐藏，不留死按钮
                // 小红书行
                SettingsNavRow(
                    title = "小红书",
                    onClick = {
                        Toast.makeText(context, "请到小红书搜索 Goalday", Toast.LENGTH_SHORT).show()
                    },
                )
                // 邮件行（隐藏，对照原版 visibility="gone"）
                // 好评行（隐藏，对照原版 visibility="gone"）
                SettingsDivider()
                // 版本信息行：标题 + 版本号（#9E9E9E）
                SettingsInfoRow(title = "版本信息", info = versionName)
                SettingsDivider()
                // 软件更新行：本地离线版点按即告知版本状态（无远端可查，不留“点了没反应”）
                SettingsNavRow(
                    title = "软件更新",
                    onClick = {
                        Toast.makeText(context, "当前已是最新版本（$versionName）", Toast.LENGTH_SHORT).show()
                    },
                )
                // 本地离线版无内嵌协议文档：隐私政策/用户条款隐藏，不留死按钮
            }

            // 底部 IPC 备案信息（12sp，居中，marginBottom=6dp）
            IpcInfoText()
        }
    }

    // 数据迁移对话框（保留备份/恢复/清理功能）
    if (showBackupDialog) {
        BackupMigrationDialog(
            snapshots = snapshots,
            onDismiss = { showBackupDialog = false },
            onCreate = ::createBackup,
            onRestore = { pendingRestore = it; showBackupDialog = false },
            onDelete = { pendingDelete = it; showBackupDialog = false },
            onCleanup = ::cleanupOldBackups,
        )
    }

    // 恢复备份确认对话框
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
                            // 先刷桌面组件再重启：否则组件长期残留旧数据
                            runCatching {
                                withContext(Dispatchers.IO) {
                                    com.bf410.goaldaylocal.ui.widget.WidgetRefresh.refreshScheduleWidgets(context)
                                }
                            }
                            restarting = true
                            kotlinx.coroutines.delay(900)
                            MMKV.onExit()
                            // 定时拉起再退出：回到应用而不是停在桌面，用户不以为崩溃
                            runCatching {
                                val alarm = context.getSystemService(android.content.Context.ALARM_SERVICE) as android.app.AlarmManager
                                val relaunch = android.app.PendingIntent.getActivity(
                                    context,
                                    99,
                                    android.content.Intent(context, com.bf410.goaldaylocal.MainActivity::class.java),
                                    android.app.PendingIntent.FLAG_IMMUTABLE,
                                )
                                alarm.set(android.app.AlarmManager.RTC, System.currentTimeMillis() + 400, relaunch)
                            }
                            Process.killProcess(Process.myPid())
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

    // 恢复后重启遮罩：点不掉，盖住“假死”期
    if (restarting) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("已恢复备份") },
            text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.5.dp,
                        color = GoaldayDesign.adaptiveInkPrimary,
                    )
                    Spacer(Modifier.width(12.dp))
                    Text("正在重启应用…", color = GoaldayDesign.adaptiveInkSecondary)
                }
            },
            confirmButton = {},
        )
    }

    // 删除备份确认对话框
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
                    scope.launch {
                        val result = withContext(Dispatchers.IO) { manager.deleteBackup(snapshot.absolutePath) }
                        result.onSuccess {
                            refreshBackups()
                            Toast.makeText(context, "已删除备份", Toast.LENGTH_SHORT).show()
                        }.onFailure {
                            Toast.makeText(context, it.message ?: "删除失败", Toast.LENGTH_SHORT).show()
                        }
                        pendingDelete = null
                    }
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

// 顶部 toolbar_normal：返回箭头 + 居中标题（18sp 加粗，对照原版标题居中）
@Composable
private fun SettingsToolbar(onBack: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(GoaldayDesign.adaptiveAppBg)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "返回",
            tint = GoaldayDesign.adaptiveInkPrimary,
            modifier = Modifier
                .size(24.dp)
                .align(Alignment.CenterStart)
                .clickable(onClick = onBack),
        )
        Text(
            "设置",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = GoaldayDesign.adaptiveInkPrimary,
            modifier = Modifier.align(Alignment.Center),
        )
    }
}

// VIP 卡片位置替换：应用信息横幅（本地离线版无 VIP 推广图）
@Composable
private fun AppInfoBanner(
    versionName: String,
    backupCount: Int,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = GoaldayDesign.ShadowSoft,
                shape = RoundedCornerShape(GoaldayDesign.RadiusL),
            )
            .background(GoaldayDesign.PinkSoft, RoundedCornerShape(GoaldayDesign.RadiusL))
            .border(
                width = GoaldayDesign.Hairline,
                color = GoaldayDesign.Pink.copy(alpha = 0.20f),
                shape = RoundedCornerShape(GoaldayDesign.RadiusL),
            )
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    "Goalday Local",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoaldayDesign.adaptiveInkPrimary,
                )
                Text(
                    "本地离线版 · v$versionName",
                    fontSize = 13.sp,
                    color = GoaldayDesign.adaptiveInkSecondary,
                )
            }
            Text(
                "$backupCount 份备份",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = GoaldayDesign.Pink,
                modifier = Modifier
                    .background(GoaldayDesign.adaptiveWhiteOverlayMedium, RoundedCornerShape(GoaldayDesign.RadiusPill))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            )
        }
    }
}

// 分组标题（16sp #9E9E9E marginLeft=15dp marginBottom=13dp）
@Composable
private fun SettingsGroupTitle(title: String) {
    Text(
        title,
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        color = GoaldayDesign.adaptiveInkMuted,
        modifier = Modifier.padding(start = 15.dp),
    )
}

// 圆角卡片容器（对照原版 bg_setting_item 背景，marginBottom=13dp 由父 spacedBy 处理）
@Composable
private fun SettingsCard(
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                GoaldayDesign.adaptiveSurface,
                RoundedCornerShape(GoaldayDesign.RadiusL),
            )
            .border(
                width = GoaldayDesign.Hairline,
                color = GoaldayDesign.BorderColor.copy(alpha = 0.14f),
                shape = RoundedCornerShape(GoaldayDesign.RadiusL),
            ),
        content = content,
    )
}

// 卡片内导航行：标题(16sp #252525 加粗) + 右侧箭头（padding=15dp）
@Composable
private fun SettingsNavRow(
    title: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(15.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            title,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = GoaldayDesign.adaptiveInkPrimary,
            modifier = Modifier.weight(1f),
        )
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = GoaldayDesign.adaptiveInkMuted,
            modifier = Modifier.size(20.dp),
        )
    }
}

// 卡片内信息行：标题(16sp #252525 加粗) + 右侧信息(#9E9E9E)（padding=15dp）
@Composable
private fun SettingsInfoRow(
    title: String,
    info: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(15.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            title,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = GoaldayDesign.adaptiveInkPrimary,
            modifier = Modifier.weight(1f),
        )
        Text(
            info,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = GoaldayDesign.adaptiveInkMuted,
        )
    }
}

// 分隔线（1dp，对照原版 ?android:windowBackground）
@Composable
private fun SettingsDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(GoaldayDesign.adaptiveDivider),
    )
}

// 分段控件配色：浅色白底+米色选中黑字，深色沿用现有暗色+品牌橙
private val SegmentGroupBg: Color
    @Composable get() = if (LocalGoaldayDarkMode.current) GoaldayDesign.adaptiveSurfaceSoft else Color.White
private val SegmentSelectedBg: Color
    @Composable get() = if (LocalGoaldayDarkMode.current) GoaldayDesign.Pink else GoaldayDesign.MorandiBeige
private val SegmentSelectedFg: Color
    @Composable get() = if (LocalGoaldayDarkMode.current) Color.White else Color.Black

// 字体大小切换行：标题"字体大小" + 三按钮组（小/中/大，38x38dp，bg_setting_fontsize_menu 背景）
@Composable
private fun FontSizeToggleRow(
    options: List<FontSizeOption>,
    selected: String,
    onSelected: (FontSizeOption) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "字体大小",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = GoaldayDesign.adaptiveInkPrimary,
            modifier = Modifier.weight(1f),
        )
        // bg_setting_fontsize_menu 背景容器：白底细边框 + 段间分隔线（对照原版三段式）
        Row(
            modifier = Modifier
                .border(
                    width = GoaldayDesign.Hairline,
                    color = GoaldayDesign.adaptiveDivider,
                    shape = RoundedCornerShape(GoaldayDesign.RadiusS),
                )
                .background(
                    SegmentGroupBg,
                    RoundedCornerShape(GoaldayDesign.RadiusS),
                )
                .clip(RoundedCornerShape(GoaldayDesign.RadiusS)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            options.forEachIndexed { index, option ->
                val active = option.key == selected
                if (index != 0) {
                    Box(
                        modifier = Modifier
                            .width(GoaldayDesign.Hairline)
                            .height(38.dp)
                            .background(GoaldayDesign.adaptiveDivider),
                    )
                }
                Box(
                    modifier = Modifier
                        .size(width = 38.dp, height = 38.dp)
                        .background(
                            if (active) SegmentSelectedBg else Color.Transparent,
                        )
                        .clickable { onSelected(option) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        option.label,
                        fontSize = option.previewSp.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (active) SegmentSelectedFg else GoaldayDesign.adaptiveInkPrimary,
                    )
                }
            }
        }
    }
}

// 图片尺寸切换行：标题"图片尺寸" + 两按钮组（小/大，57x35dp，bg_setting_fontsize_menu 背景）
@Composable
private fun ImageSizeToggleRow(
    options: List<FontSizeOption>,
    selected: String,
    onSelected: (FontSizeOption) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "图片尺寸",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = GoaldayDesign.adaptiveInkPrimary,
            modifier = Modifier.weight(1f),
        )
        Row(
            modifier = Modifier
                .border(
                    width = GoaldayDesign.Hairline,
                    color = GoaldayDesign.adaptiveDivider,
                    shape = RoundedCornerShape(GoaldayDesign.RadiusS),
                )
                .background(
                    SegmentGroupBg,
                    RoundedCornerShape(GoaldayDesign.RadiusS),
                )
                .clip(RoundedCornerShape(GoaldayDesign.RadiusS)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            options.forEachIndexed { index, option ->
                val active = option.key == selected
                if (index != 0) {
                    Box(
                        modifier = Modifier
                            .width(GoaldayDesign.Hairline)
                            .height(35.dp)
                            .background(GoaldayDesign.adaptiveDivider),
                    )
                }
                Box(
                    modifier = Modifier
                        .size(width = 57.dp, height = 35.dp)
                        .background(
                            if (active) SegmentSelectedBg else Color.Transparent,
                        )
                        .clickable { onSelected(option) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        option.label,
                        fontSize = option.previewSp.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (active) SegmentSelectedFg else GoaldayDesign.adaptiveInkPrimary,
                    )
                }
            }
        }
    }
}

// 深色模式切换行：标题 + 三按钮组（跟随系统/浅色/深色）
@Composable
private fun DarkModeToggleRow(
    options: List<DarkModeOption>,
    selected: String,
    onSelected: (DarkModeOption) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "深色模式",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = GoaldayDesign.adaptiveInkPrimary,
            modifier = Modifier.weight(1f),
        )
        Row(
            modifier = Modifier
                .border(
                    width = GoaldayDesign.Hairline,
                    color = GoaldayDesign.adaptiveDivider,
                    shape = RoundedCornerShape(GoaldayDesign.RadiusS),
                )
                .background(
                    SegmentGroupBg,
                    RoundedCornerShape(GoaldayDesign.RadiusS),
                )
                .clip(RoundedCornerShape(GoaldayDesign.RadiusS)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            options.forEachIndexed { index, option ->
                val active = option.key == selected
                if (index != 0) {
                    Box(
                        modifier = Modifier
                            .width(GoaldayDesign.Hairline)
                            .height(35.dp)
                            .background(GoaldayDesign.adaptiveDivider),
                    )
                }
                Box(
                    modifier = Modifier
                        .size(width = 57.dp, height = 35.dp)
                        .background(
                            if (active) SegmentSelectedBg else Color.Transparent,
                        )
                        .clickable { onSelected(option) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        option.label,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (active) SegmentSelectedFg else GoaldayDesign.adaptiveInkPrimary,
                        maxLines = 1,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

// 底部 IPC 备案信息（12sp，居中，marginBottom=6dp）
@Composable
private fun IpcInfoText() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp, bottom = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            "Goalday Local · 本地离线版",
            fontSize = 12.sp,
            color = GoaldayDesign.adaptiveInkMuted,
            textAlign = TextAlign.Center,
        )
    }
}

// 数据迁移对话框（保留备份/恢复/清理功能）
@Composable
private fun BackupMigrationDialog(
    snapshots: List<BackupSnapshot>,
    onDismiss: () -> Unit,
    onCreate: () -> Unit,
    onRestore: (BackupSnapshot) -> Unit,
    onDelete: (BackupSnapshot) -> Unit,
    onCleanup: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("数据迁移") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "本地备份与迁移：所有数据保存在本机，无服务器依赖。",
                    fontSize = 13.sp,
                    color = GoaldayDesign.adaptiveInkSecondary,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    BackupDialogButton(
                        text = "创建备份",
                        color = GoaldayDesign.PrimaryAction,
                        modifier = Modifier.weight(1f),
                        onClick = onCreate,
                    )
                    BackupDialogButton(
                        text = "清理旧备份",
                        color = GoaldayDesign.Danger,
                        modifier = Modifier.weight(1f),
                        onClick = onCleanup,
                    )
                }
                Text(
                    "备份历史（${snapshots.size} 个）",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GoaldayDesign.adaptiveInkPrimary,
                )
                if (snapshots.isEmpty()) {
                    Text(
                        "暂无备份。点击「创建备份」即可生成本地数据快照。",
                        fontSize = 13.sp,
                        color = GoaldayDesign.adaptiveInkMuted,
                    )
                } else {
                    snapshots.take(8).forEach { snapshot ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    snapshot.name,
                                    fontSize = 13.sp,
                                    color = GoaldayDesign.adaptiveInkPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Text(
                                    "${formatBackupDate(snapshot.modifiedAtMillis)} · ${snapshot.fileCount} 文件 · ${BackupManager.formatBytes(snapshot.sizeBytes)}",
                                    fontSize = 11.sp,
                                    color = GoaldayDesign.adaptiveInkMuted,
                                )
                            }
                            Text(
                                "恢复",
                                color = GoaldayDesign.Pink,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier
                                    .clickable { onRestore(snapshot) }
                                    .padding(4.dp),
                            )
                            Text(
                                "删除",
                                color = GoaldayDesign.Danger,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier
                                    .clickable { onDelete(snapshot) }
                                    .padding(4.dp),
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("关闭") }
        },
    )
}

@Composable
private fun BackupDialogButton(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .background(color, RoundedCornerShape(GoaldayDesign.RadiusPill))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            textAlign = TextAlign.Center,
        )
    }
}

private fun formatBackupDate(millis: Long): String =
    SimpleDateFormat("MM月dd日 HH:mm", Locale.CHINA).format(Date(millis))
