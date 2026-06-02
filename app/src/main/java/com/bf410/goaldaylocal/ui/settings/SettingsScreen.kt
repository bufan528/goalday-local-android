package com.bf410.goaldaylocal.ui.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.bf410.goaldaylocal.data.BackupManager

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val manager = remember { BackupManager(context) }
    var latestBackup by remember { mutableStateOf(manager.latestBackupPath()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text("本地设置", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(top = 18.dp))
        SettingCard(
            title = "立即备份",
            subtitle = "把当前本地数据复制到：${manager.backupRootPath()}",
            onClick = {
                val result = manager.backupMmkv()
                result.onSuccess {
                    latestBackup = it.absolutePath
                    Toast.makeText(context, "备份完成", Toast.LENGTH_SHORT).show()
                }.onFailure {
                    Toast.makeText(context, it.message ?: "备份失败", Toast.LENGTH_SHORT).show()
                }
            },
        )
        SettingCard(
            title = "恢复最近一次备份",
            subtitle = "最近备份：$latestBackup",
            onClick = {
                val result = manager.restoreLatestBackup()
                result.onSuccess {
                    latestBackup = it.absolutePath
                    Toast.makeText(context, "恢复完成，请重启应用查看", Toast.LENGTH_LONG).show()
                }.onFailure {
                    Toast.makeText(context, it.message ?: "恢复失败", Toast.LENGTH_SHORT).show()
                }
            },
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0x55FFFFFF), RoundedCornerShape(24.dp))
                .padding(18.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("备份目录", style = MaterialTheme.typography.titleMedium)
                Text(manager.backupRootPath(), color = Color(0xFF6C635A))
                Text("后续这里还会继续补字号、导出长图、PDF 和桌面组件设置。", color = Color(0xFF6C635A))
            }
        }
    }
}

@Composable
private fun SettingCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0x66FFFFFF), RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .padding(18.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = Color(0xFF2F261D))
            Text(subtitle, color = Color(0xFF6C635A))
        }
    }
}
