package com.bf410.goaldaylocal.data

import android.content.Context
import com.tencent.mmkv.MMKV
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
    // 外置目录可能返回 null（旧机/权限）：回退应用私有目录，绝不写相对路径
    private val backupDir: File
        get() {
            val base = context.getExternalFilesDir(null) ?: context.filesDir
            return File(base, "backups").apply { mkdirs() }
        }

    fun backupMmkv(): Result<File> = runCatching {
        MMKV.defaultMMKV().sync()
        val sourceDir = File(context.filesDir.parentFile, "mmkv")
        require(sourceDir.exists()) { "未找到本地数据目录" }
        val targetDir = File(
            backupDir,
            // 毫秒时间戳：同秒连点两次不再合并覆盖
            "goalday-backup-${SimpleDateFormat("yyyyMMdd-HHmmss-SSS", Locale.US).format(Date())}",
        ).apply { mkdirs() }
        try {
            copyMmkvFiles(sourceDir, targetDir)
        } catch (e: Exception) {
            // 拷贝失败不留空目录：否则列表出现 0 文件备份，恢复它即清库
            runCatching { targetDir.deleteRecursively() }
            throw e
        }
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
        // 空目录/手工丢入的目录直接恢复=清库：必须含有效数据文件
        require(source.listFiles()?.any { it.isFile && isSafeMmkvFileName(it.name) } == true) {
            "该备份是空的，没有可恢复的数据"
        }
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
            ?.filter { it.isDirectory && !it.name.startsWith(".") }
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
            ?.filter { it.isDirectory && !it.name.startsWith(".") }
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
            ?.filter { it.isDirectory && !it.name.startsWith(".") }
            ?.maxByOrNull { it.lastModified() }

    private fun requireBackupChild(path: String): File {
        val root = backupDir.canonicalFile
        // 相对路径按备份根解析，不跟随进程工作目录
        val target = (if (File(path).isAbsolute) File(path) else File(root, path)).canonicalFile
        require(target.parentFile?.canonicalPath == root.canonicalPath) { "只能操作备份目录内的数据" }
        return target
    }

    private fun restoreBackupDirectory(source: File): File {
        val targetDir = File(context.filesDir.parentFile, "mmkv").apply { mkdirs() }
        // 先给当前数据做临时快照：恢复中途失败可回滚，不会半清空丢库
        val rollbackDir = File(backupDir, ".tmp-restore-${System.currentTimeMillis()}").apply { mkdirs() }
        runCatching { copyMmkvFiles(targetDir, rollbackDir) }
        try {
            clearRestoreTarget(targetDir)
            copyMmkvFiles(source, targetDir)
        } catch (e: Exception) {
            runCatching {
                clearRestoreTarget(targetDir)
                copyMmkvFiles(rollbackDir, targetDir)
            }
            throw e
        } finally {
            runCatching { rollbackDir.deleteRecursively() }
        }
        return source
    }

    private fun clearRestoreTarget(targetDir: File) {
        // 全量清（含点文件/tmp 残留）：只删安全名会新旧混合出幽灵勾选
        targetDir.listFiles()
            ?.filter { it.isFile }
            ?.forEach { file ->
                check(file.delete()) { "清理旧数据失败：${file.name}" }
            }
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
        if (!isSafeMmkvFileName(file.name)) return false
        if (file.length() > MAX_BACKUP_SINGLE_FILE_BYTES) return false
        return true
    }

    private fun isSafeMmkvFileName(name: String): Boolean =
        name.isNotBlank() && !name.startsWith(".") && File.separatorChar !in name

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
