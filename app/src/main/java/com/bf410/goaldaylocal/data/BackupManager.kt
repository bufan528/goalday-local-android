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
        copyMmkvFiles(sourceDir, targetDir)
        targetDir
    }

    fun restoreLatestBackup(): Result<File> = runCatching {
        val source = latestBackupFile()
            ?: error("没有可恢复的备份")
        restoreBackupDirectory(source)
    }

    fun restoreBackup(path: String): Result<File> = runCatching {
        val source = requireBackupChild(path)
        require(source.exists() && source.isDirectory) { "备份不存在" }
        restoreBackupDirectory(source)
    }

    fun deleteBackup(path: String): Result<Boolean> = runCatching {
        val target = requireBackupChild(path)
        require(target.exists() && target.isDirectory) { "备份不存在" }
        check(target.deleteRecursively()) { "删除备份失败" }
        true
    }

    fun cleanupOldBackups(keepLatest: Int = 6): Result<Int> = runCatching {
        val safeKeep = keepLatest.coerceAtLeast(1)
        val oldBackups = backupDir.listFiles()
            ?.filter { it.isDirectory }
            ?.sortedByDescending { it.lastModified() }
            ?.drop(safeKeep)
            ?: emptyList()
        oldBackups.forEach { backup ->
            check(backup.deleteRecursively()) { "清理备份失败：${backup.name}" }
        }
        oldBackups.size
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

    private fun requireBackupChild(path: String): File {
        val root = backupDir.canonicalFile
        val target = File(path).canonicalFile
        require(target.parentFile?.canonicalPath == root.canonicalPath) { "只能操作备份目录内的数据" }
        return target
    }

    private fun restoreBackupDirectory(source: File): File {
        val targetDir = File(context.filesDir.parentFile, "mmkv").apply { mkdirs() }
        copyMmkvFiles(source, targetDir)
        return source
    }

    private fun copyMmkvFiles(sourceDir: File, targetDir: File) {
        val files = sourceDir.listFiles()
            ?.filter { it.isFile && isSafeMmkvBackupFile(it) }
            ?: emptyList()
        require(files.size <= MAX_BACKUP_FILE_COUNT) { "备份文件数量异常" }
        require(files.sumOf { it.length() } <= MAX_BACKUP_TOTAL_BYTES) { "备份文件过大" }
        files.forEach { file ->
            file.copyTo(File(targetDir, file.name), overwrite = true)
        }
    }

    private fun isSafeMmkvBackupFile(file: File): Boolean {
        val name = file.name
        if (name.isBlank() || name.startsWith(".") || File.separatorChar in name) return false
        if (file.length() > MAX_BACKUP_SINGLE_FILE_BYTES) return false
        return true
    }

    companion object {
        private const val MAX_BACKUP_FILE_COUNT = 32
        private const val MAX_BACKUP_SINGLE_FILE_BYTES = 10L * 1024L * 1024L
        private const val MAX_BACKUP_TOTAL_BYTES = 50L * 1024L * 1024L

        fun formatBytes(bytes: Long): String {
            if (bytes < 1024) return "$bytes B"
            val kb = bytes / 1024.0
            if (kb < 1024) return String.format(Locale.US, "%.1f KB", kb)
            val mb = kb / 1024.0
            return String.format(Locale.US, "%.1f MB", mb)
        }
    }
}
