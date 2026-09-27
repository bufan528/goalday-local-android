package com.bf410.goaldaylocal.ui.book

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 日记图片统一异步解码：IO 线程做 bounds + 采样 + 解码，主线程只包一层 ImageBitmap。
 *
 * 之前三处（记录页缩略图/书内图片块/图片 Tile）都在 remember 里主线程同步解码，
 * 4K 大图滚到即掉帧；Tile 甚至无采样直接 decodeStream，全尺寸进内存即 OOM。
 */

/** 采样比纯函数（可单测）：最长边压到 maxSidePx 以内。 */
internal fun sampleSizeForBounds(outWidth: Int, outHeight: Int, maxSidePx: Int): Int {
    if (outWidth <= 0 || outHeight <= 0 || maxSidePx <= 0) return 1
    var sample = 1
    while (outWidth / sample > maxSidePx || outHeight / sample > maxSidePx) sample *= 2
    return sample
}

internal fun decodeSampledDiaryImage(context: Context, uri: String, maxSidePx: Int): Bitmap? = runCatching {
    val bare = uri.trim().removePrefix("file://")
    val file = File(bare)
    if (file.exists() && file.isFile) {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return@runCatching null
        val sample = sampleSizeForBounds(bounds.outWidth, bounds.outHeight, maxSidePx)
        BitmapFactory.decodeFile(file.absolutePath, BitmapFactory.Options().apply { inSampleSize = sample })
    } else {
        // content 链：流不可复位，先读字节再两次解码（与长图导出同路）
        context.contentResolver.openInputStream(Uri.parse(uri))?.use { stream ->
            val bytes = stream.readBytes()
            if (bytes.isEmpty()) return@use null
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return@use null
            val sample = sampleSizeForBounds(bounds.outWidth, bounds.outHeight, maxSidePx)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, BitmapFactory.Options().apply { inSampleSize = sample })
        }
    }
}.getOrNull()

/** 异步版本：占位先行（null），解完重组；path 变化自动重解。 */
@Composable
internal fun rememberDiaryImageBitmap(uri: String, maxSidePx: Int): ImageBitmap? {
    val context = LocalContext.current
    var bitmap by remember(uri) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(uri) {
        bitmap = withContext(Dispatchers.IO) {
            decodeSampledDiaryImage(context, uri, maxSidePx)?.asImageBitmap()
        }
    }
    return bitmap
}
