package com.bf410.goaldaylocal.data

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BackupManager(
    private val context: Context,
) {
    private val backupDir: File
        get() = File(context.getExternalFilesDir(null), "backups").apply { mkdirs() }

    fun backupMmkv(): Result<File> = runCatching {
        val sourceDir = File(context.filesDir.parentFile, "mmkv")
        require(sourceDir.exists()) { "未找到本地数据目录" }
        val targetDir = File(
            backupDir,
            "goalday-backup-${SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())}",
        ).apply { mkdirs() }
        sourceDir.listFiles()?.forEach { file ->
            file.copyTo(File(targetDir, file.name), overwrite = true)
        }
        targetDir
    }

    fun restoreLatestBackup(): Result<File> = runCatching {
        val source = backupDir.listFiles()
            ?.filter { it.isDirectory }
            ?.maxByOrNull { it.lastModified() }
            ?: error("没有可恢复的备份")
        val targetDir = File(context.filesDir.parentFile, "mmkv").apply { mkdirs() }
        source.listFiles()?.forEach { file ->
            file.copyTo(File(targetDir, file.name), overwrite = true)
        }
        source
    }

    fun latestBackupPath(): String =
        backupDir.listFiles()
            ?.filter { it.isDirectory }
            ?.maxByOrNull { it.lastModified() }
            ?.absolutePath
            ?: "暂无备份"

    fun backupRootPath(): String = backupDir.absolutePath
}
