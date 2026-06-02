package com.bf410.goaldaylocal.data

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BackupSnapshot(
    val name: String,
    val absolutePath: String,
    val modifiedAtMillis: Long,
    val fileCount: Int,
    val sizeBytes: Long,
)

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
        val source = latestBackupFile()
            ?: error("没有可恢复的备份")
        restoreBackupDirectory(source)
    }

    fun restoreBackup(path: String): Result<File> = runCatching {
        val source = File(path)
        require(source.exists() && source.isDirectory) { "备份不存在" }
        restoreBackupDirectory(source)
    }

    fun latestBackupPath(): String =
        latestBackupFile()
            ?.absolutePath
            ?: "暂无备份"

    fun backupRootPath(): String = backupDir.absolutePath

    fun backupSnapshots(): List<BackupSnapshot> =
        backupDir.listFiles()
            ?.filter { it.isDirectory }
            ?.sortedByDescending { it.lastModified() }
            ?.map { dir ->
                val files = dir.walkTopDown().filter { it.isFile }.toList()
                BackupSnapshot(
                    name = dir.name,
                    absolutePath = dir.absolutePath,
                    modifiedAtMillis = dir.lastModified(),
                    fileCount = files.size,
                    sizeBytes = files.sumOf { it.length() },
                )
            }
            ?: emptyList()

    fun storageSummary(): String {
        val snapshots = backupSnapshots()
        val size = snapshots.sumOf { it.sizeBytes }
        return "${snapshots.size} 个备份 · ${formatBytes(size)}"
    }

    private fun latestBackupFile(): File? =
        backupDir.listFiles()
            ?.filter { it.isDirectory }
            ?.maxByOrNull { it.lastModified() }

    private fun restoreBackupDirectory(source: File): File {
        val targetDir = File(context.filesDir.parentFile, "mmkv").apply { mkdirs() }
        source.listFiles()?.forEach { file ->
            file.copyTo(File(targetDir, file.name), overwrite = true)
        }
        return source
    }

    companion object {
        fun formatBytes(bytes: Long): String {
            if (bytes < 1024) return "$bytes B"
            val kb = bytes / 1024.0
            if (kb < 1024) return String.format(Locale.US, "%.1f KB", kb)
            val mb = kb / 1024.0
            return String.format(Locale.US, "%.1f MB", mb)
        }
    }
}
