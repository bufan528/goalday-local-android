package com.bf410.goaldaylocal.ui.book

import android.content.Context
import android.net.Uri

/**
 * 日记图片统一落盘：复制到应用私有 diary_images/ 后存绝对路径（对照记录页同名逻辑）。
 *
 * 书侧之前直存 content://，权限丢失/重启后即不可读；改走物理复制后，书内展示、
 * 记录页回显与长图导出统一走 File 解码，不再依赖外部 uri 权限。
 */
internal fun copyDiaryImageToPrivateDir(context: Context, uri: Uri, dateIso: String): String? {
    return runCatching {
        val dir = java.io.File(context.filesDir, "diary_images").apply { mkdirs() }
        val stamp = dateIso.replace("-", "").ifBlank { "nodate" }
        val file = java.io.File(dir, "b" + stamp + "_" + System.currentTimeMillis() + ".jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output -> input.copyTo(output) }
        }
        if (file.exists() && file.length() > 0) file.absolutePath else null
    }.getOrNull()
}
