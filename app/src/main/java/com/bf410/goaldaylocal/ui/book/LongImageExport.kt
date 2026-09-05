package com.bf410.goaldaylocal.ui.book

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.CancellationSignal
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.print.PageRange
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SubdirectoryArrowRight
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bf410.goaldaylocal.R
import com.bf410.goaldaylocal.data.BookPage
import com.bf410.goaldaylocal.data.DiaryPage
import com.bf410.goaldaylocal.data.PlanPage
import com.bf410.goaldaylocal.data.ScheduleEntry
import com.bf410.goaldaylocal.data.SchedulePage
import com.bf410.goaldaylocal.data.TargetItemMeta
import com.bf410.goaldaylocal.data.TargetPage
import com.bf410.goaldaylocal.ui.replica.BoardTask
import com.bf410.goaldaylocal.ui.replica.DualLaneExecutionBoard
import com.bf410.goaldaylocal.ui.replica.ExecutionBoardHeader
import com.bf410.goaldaylocal.ui.replica.GoaldayDesign
import com.bf410.goaldaylocal.ui.replica.TimelineTask
import com.tencent.mmkv.MMKV
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.delay
import java.io.File
import java.io.FileOutputStream

internal fun exportHandbookScheduleLongImage(
    context: Context,
    year: Int,
    month: Int,
    days: List<Int>,
    entries: List<ScheduleEntry>,
    weeklyTheme: String,
): Uri? = runCatching {
    val bitmap = renderHandbookScheduleLongImage(year, month, days, entries, weeklyTheme)
    saveBitmapToPictures(context, bitmap, "Goalday_schedule_${System.currentTimeMillis()}.png")
}.getOrNull()

internal data class LongImagePreview(
    val title: String,
    val subtitle: String,
    val filePrefix: String,
    val bitmap: Bitmap,
)

private data class LongImageExportHistoryItem(
    val action: String,
    val title: String,
    val preset: String,
    val detail: String,
    val createdAtMillis: Long,
)

private fun saveLongImagePreview(context: Context, preview: LongImagePreview): Uri? =
    saveBitmapToPictures(context, preview.bitmap, "${preview.filePrefix}_${System.currentTimeMillis()}.png")

private fun shareLongImagePreview(context: Context, preview: LongImagePreview): Boolean {
    val uri = saveLongImagePreview(context, preview) ?: return false
    return shareLongImage(context, uri)
}

private enum class LongImageExportPreset(
    val label: String,
    val description: String,
    val paperLabel: String,
    val mediaSize: PrintAttributes.MediaSize,
    val previewInset: Int,
) {
    LONG("长图", "原始比例 · 适合保存分享", "长图", PrintAttributes.MediaSize.UNKNOWN_PORTRAIT, 0),
    PHONE("手机", "9:16 预览 · 适合发到社交软件", "手机", PrintAttributes.MediaSize.NA_LETTER, 10),
    PRINT("打印", "A4 PDF · 适合纸质手账", "A4", PrintAttributes.MediaSize.ISO_A4, 22),
}

private enum class LongImageShortcutMode(
    val raw: String,
    val label: String,
    val description: String,
    val preset: LongImageExportPreset?,
) {
    DISABLED("disabled", "关闭", "shortcut_print_export_disabled", null),
    LONG("long", "长图", "shortcut_print_export_long", LongImageExportPreset.LONG),
    SHORT("short", "短图", "shortcut_print_export_short", LongImageExportPreset.PHONE),
    SHORT_1("short_1", "短图 1", "shortcut_print_export_short_1", LongImageExportPreset.PHONE),
    SHORT_2("short_2", "短图 2", "shortcut_print_export_short_2", LongImageExportPreset.PRINT),
}

private const val KEY_LONG_IMAGE_EXPORT_HISTORY = "long_image_export_history"
private const val KEY_LONG_IMAGE_SHORTCUT_MODE = "long_image_shortcut_mode"
private val longImageHistoryFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MM-dd HH:mm")

private fun loadLongImageShortcutMode(): LongImageShortcutMode {
    val raw = MMKV.defaultMMKV().decodeString(KEY_LONG_IMAGE_SHORTCUT_MODE, LongImageShortcutMode.LONG.raw)
    return LongImageShortcutMode.entries.firstOrNull { it.raw == raw } ?: LongImageShortcutMode.LONG
}

private fun saveLongImageShortcutMode(mode: LongImageShortcutMode) {
    MMKV.defaultMMKV().encode(KEY_LONG_IMAGE_SHORTCUT_MODE, mode.raw)
}

private fun loadLongImageExportHistory(): List<LongImageExportHistoryItem> {
    val raw = MMKV.defaultMMKV().decodeString(KEY_LONG_IMAGE_EXPORT_HISTORY, "[]") ?: "[]"
    val array = runCatching { JSONArray(raw) }.getOrElse { JSONArray() }
    return buildList {
        repeat(array.length()) { index ->
            val json = array.optJSONObject(index) ?: return@repeat
            add(
                LongImageExportHistoryItem(
                    action = json.optString("action").ifBlank { "导出" },
                    title = json.optString("title").ifBlank { "Goalday 长图" },
                    preset = json.optString("preset").ifBlank { "长图" },
                    detail = json.optString("detail"),
                    createdAtMillis = json.optLong("createdAtMillis", 0L),
                ),
            )
        }
    }
}

private fun appendLongImageExportHistory(
    preview: LongImagePreview,
    preset: LongImageExportPreset,
    action: String,
    detail: String = "",
) {
    val updated = (
        listOf(
            LongImageExportHistoryItem(
                action = action,
                title = preview.title,
                preset = preset.label,
                detail = detail,
                createdAtMillis = System.currentTimeMillis(),
            ),
        ) + loadLongImageExportHistory()
    ).take(12)
    val array = JSONArray()
    updated.forEach { item ->
        array.put(
            JSONObject()
                .put("action", item.action)
                .put("title", item.title)
                .put("preset", item.preset)
                .put("detail", item.detail)
                .put("createdAtMillis", item.createdAtMillis),
        )
    }
    MMKV.defaultMMKV().encode(KEY_LONG_IMAGE_EXPORT_HISTORY, array.toString())
}

private fun LongImageExportHistoryItem.displayTime(): String {
    if (createdAtMillis <= 0L) return "刚刚"
    return Instant.ofEpochMilli(createdAtMillis)
        .atZone(ZoneId.systemDefault())
        .format(longImageHistoryFormatter)
}

private fun printLongImagePreview(context: Context, preview: LongImagePreview, preset: LongImageExportPreset): Boolean =
    runCatching {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
        printManager.print(
            preview.title.ifBlank { "Goalday 长图" },
            BitmapPrintDocumentAdapter(preview.title, preview.bitmap),
            PrintAttributes.Builder()
                .setMediaSize(preset.mediaSize)
                .setColorMode(PrintAttributes.COLOR_MODE_COLOR)
                .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                .build(),
        )
        true
    }.getOrDefault(false)

private class BitmapPrintDocumentAdapter(
    private val title: String,
    private val bitmap: Bitmap,
) : PrintDocumentAdapter() {
    override fun onLayout(
        oldAttributes: PrintAttributes?,
        newAttributes: PrintAttributes?,
        cancellationSignal: CancellationSignal?,
        callback: LayoutResultCallback,
        extras: android.os.Bundle?,
    ) {
        if (cancellationSignal?.isCanceled == true) {
            callback.onLayoutCancelled()
            return
        }
        callback.onLayoutFinished(
            PrintDocumentInfo.Builder("${title.ifBlank { "Goalday" }}.pdf")
                .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                .setPageCount(1)
                .build(),
            true,
        )
    }

    override fun onWrite(
        pages: Array<out PageRange>?,
        destination: ParcelFileDescriptor?,
        cancellationSignal: CancellationSignal?,
        callback: WriteResultCallback,
    ) {
        if (destination == null || cancellationSignal?.isCanceled == true) {
            callback.onWriteCancelled()
            return
        }
        runCatching {
            val document = PdfDocument()
            try {
                val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create()
                val page = document.startPage(pageInfo)
                page.canvas.drawBitmap(bitmap, 0f, 0f, null)
                document.finishPage(page)
                FileOutputStream(destination.fileDescriptor).use { output ->
                    document.writeTo(output)
                }
            } finally {
                document.close()
            }
            callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
        }.onFailure {
            callback.onWriteFailed(it.message ?: "打印失败")
        }
    }
}

@Composable
internal fun LongImagePreviewDialog(
    preview: LongImagePreview,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    var actionHint by remember(preview) { mutableStateOf("") }
    var selectedPreset by remember(preview) { mutableStateOf(LongImageExportPreset.LONG) }
    var shortcutMode by remember(preview) { mutableStateOf(loadLongImageShortcutMode()) }
    var exportHistory by remember(preview) { mutableStateOf(loadLongImageExportHistory()) }
    fun recordAction(message: String, action: String, detail: String = "") {
        actionHint = message
        appendLongImageExportHistory(preview, selectedPreset, action, detail)
        exportHistory = loadLongImageExportHistory()
    }
    LaunchedEffect(actionHint) {
        if (actionHint.isNotBlank()) {
            delay(1400)
            actionHint = ""
        }
    }
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(GoaldayDesign.adaptiveSurface, GoaldayDesign.adaptivePaperWarm, GoaldayDesign.ExportPaperWarm),
                    ),
                )
                .padding(horizontal = GoaldayDesign.Space3, vertical = GoaldayDesign.Space3 + 2.dp),
            verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space2 + 2.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GoaldayDesign.adaptiveSurface.copy(alpha = 0.87f))
                    .border(GoaldayDesign.Hairline, GoaldayDesign.BorderColor.copy(alpha = 0.09f), RoundedCornerShape(bottomStart = GoaldayDesign.Radius2XL, bottomEnd = GoaldayDesign.Radius2XL))
                    .padding(horizontal = GoaldayDesign.Space3 + 2.dp, vertical = GoaldayDesign.Space3 - 1.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "‹ 返回",
                    style = MaterialTheme.typography.labelSmall,
                    color = GoaldayDesign.adaptiveInkSecondary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
                        .background(GoaldayDesign.BorderColor.copy(alpha = 0.06f))
                        .clickable { onDismiss() }
                        .padding(horizontal = GoaldayDesign.Space2 + 2.dp, vertical = GoaldayDesign.Space1 + 2.dp),
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space1 / 2), modifier = Modifier.weight(1f)) {
                    Text("长图预览", style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.adaptiveInkSecondary, fontWeight = FontWeight.SemiBold)
                    Text(preview.title, style = MaterialTheme.typography.titleMedium, color = GoaldayDesign.adaptiveInkPrimary, fontWeight = FontWeight.SemiBold)
                    Text(preview.subtitle, style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.adaptiveInkMuted)
                }
                Text(
                    "打印预设",
                    style = MaterialTheme.typography.labelSmall,
                    color = GoaldayDesign.adaptiveInkMuted,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space2),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LongImageInfoPill("长图", "${preview.bitmap.width}×${preview.bitmap.height}")
                LongImageInfoPill("格式", "PNG")
                LongImageInfoPill("预设", selectedPreset.label)
                LongImageInfoPill("快捷", shortcutMode.label)
                LongImageInfoPill("记录", "${exportHistory.size}条")
            }
            LongImagePrintPanel(
                preset = selectedPreset,
                preview = preview,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space2),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LongImageExportPreset.entries.forEach { preset ->
                    LongImagePresetChip(
                        preset = preset,
                        selected = preset == selectedPreset,
                        onClick = { selectedPreset = preset },
                    )
                }
            }
            LongImageShortcutPanel(
                mode = shortcutMode,
                onSelect = { mode ->
                    shortcutMode = mode
                    saveLongImageShortcutMode(mode)
                    mode.preset?.let { selectedPreset = it }
                    actionHint = if (mode == LongImageShortcutMode.DISABLED) "已关闭快捷导出" else "已设置快捷导出：${mode.label}"
                },
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(GoaldayDesign.RadiusXL))
                    .background(GoaldayDesign.adaptiveSurfaceSoft)
                    .border(GoaldayDesign.Hairline, GoaldayDesign.BorderColor.copy(alpha = 0.13f), RoundedCornerShape(GoaldayDesign.RadiusXL))
                    .verticalScroll(rememberScrollState()),
            ) {
                // P1-3 修复：长图预览滚动卡顿
                // 原因：超大 bitmap（高度可能超 8000px）直接 asImageBitmap() 渲染，超出 GPU 纹理上限
                // （多数设备 4096px）触发软件渲染；且每次重组重建 ImageBitmap 包装
                // 修复：1) remember 缓存 ImageBitmap 避免重组重建
                //       2) 等比缩小到预览安全高度（保留长宽比，保存/导出仍用原图全分辨率）
                val previewImageBitmap = remember(preview.bitmap) {
                    val maxPreviewHeight = 4096
                    val src = preview.bitmap
                    if (src.height > maxPreviewHeight) {
                        val scale = maxPreviewHeight.toFloat() / src.height
                        Bitmap.createScaledBitmap(
                            src,
                            (src.width * scale).toInt().coerceAtLeast(1),
                            maxPreviewHeight,
                            true,
                        ).asImageBitmap()
                    } else {
                        src.asImageBitmap()
                    }
                }
                Image(
                    bitmap = previewImageBitmap,
                    contentDescription = null,
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(selectedPreset.previewInset.dp),
                )
            }
            if (exportHistory.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(GoaldayDesign.RadiusL))
                        .background(GoaldayDesign.adaptiveWhiteOverlayMedium)
                        .border(GoaldayDesign.Hairline, GoaldayDesign.BorderColor.copy(alpha = 0.09f), RoundedCornerShape(GoaldayDesign.RadiusL))
                        .padding(GoaldayDesign.Space2 + 1.dp),
                    verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space1 + 2.dp),
                ) {
                    Text("最近导出", style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.adaptiveInkMuted, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space1 + 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        exportHistory.take(6).forEach { item ->
                            LongImageHistoryChip(item)
                        }
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(GoaldayDesign.Radius2XL))
                    .background(GoaldayDesign.adaptiveWhiteOverlay)
                    .border(GoaldayDesign.Hairline, GoaldayDesign.BorderColor.copy(alpha = 0.13f), RoundedCornerShape(GoaldayDesign.Radius2XL))
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = GoaldayDesign.Space2 + 2.dp, vertical = GoaldayDesign.Space2 + 2.dp),
                horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space2 - 1.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LongImageActionChip("保存", GoaldayDesign.Positive, Modifier.width(86.dp)) {
                    val uri = saveLongImagePreview(context, preview)
                    if (uri != null) {
                        recordAction("已保存到相册", "保存", uri.lastPathSegment.orEmpty())
                    } else {
                        actionHint = "保存失败"
                    }
                }
                LongImageActionChip("分享", GoaldayDesign.RouteDiary, Modifier.width(86.dp)) {
                    if (shareLongImagePreview(context, preview)) {
                        recordAction("已打开分享", "分享", selectedPreset.description)
                    } else {
                        actionHint = "分享失败"
                    }
                }
                LongImageActionChip("打印", GoaldayDesign.adaptiveInkSecondary, Modifier.width(86.dp)) {
                    if (printLongImagePreview(context, preview, selectedPreset)) {
                        recordAction("已打开${selectedPreset.label}打印", "打印", selectedPreset.description)
                    } else {
                        actionHint = "打印失败"
                    }
                }
                if (shortcutMode != LongImageShortcutMode.DISABLED) {
                    LongImageActionChip("快捷", GoaldayDesign.Pink, Modifier.width(86.dp)) {
                        val shortcutPreset = shortcutMode.preset ?: selectedPreset
                        selectedPreset = shortcutPreset
                        val uri = saveLongImagePreview(context, preview)
                        if (uri != null) {
                            recordAction("已按${shortcutMode.label}快捷保存", "快捷", shortcutMode.description)
                        } else {
                            actionHint = "快捷导出失败"
                        }
                    }
                }
                if (actionHint.isNotBlank()) {
                    Text(actionHint, style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.adaptiveInkSecondary, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun LongImagePrintPanel(
    preset: LongImageExportPreset,
    preview: LongImagePreview,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(GoaldayDesign.RadiusL))
            .background(GoaldayDesign.adaptiveWhiteOverlayMedium)
            .border(GoaldayDesign.Hairline, GoaldayDesign.BorderColor.copy(alpha = 0.09f), RoundedCornerShape(GoaldayDesign.RadiusL))
            .padding(horizontal = GoaldayDesign.Space2 + 2.dp, vertical = GoaldayDesign.Space2),
        horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space2),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space1 / 2)) {
            Text("导出预设", style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.adaptiveInkSecondary, fontWeight = FontWeight.SemiBold)
            Text(preset.description, style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.adaptiveInkSecondary, maxLines = 1)
        }
        LongImageInfoPill("纸张", preset.paperLabel)
        LongImageInfoPill("比例", if (preset == LongImageExportPreset.PHONE) "9:16" else "${preview.bitmap.width}:${preview.bitmap.height}")
    }
}

@Composable
private fun LongImageShortcutPanel(
    mode: LongImageShortcutMode,
    onSelect: (LongImageShortcutMode) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(GoaldayDesign.RadiusL))
            .background(GoaldayDesign.adaptiveWhiteOverlayMedium)
            .border(GoaldayDesign.Hairline, GoaldayDesign.BorderColor.copy(alpha = 0.09f), RoundedCornerShape(GoaldayDesign.RadiusL))
            .padding(horizontal = GoaldayDesign.Space2 + 2.dp, vertical = GoaldayDesign.Space2),
        verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space2 - 1.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text("快捷导出", style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.adaptiveInkSecondary, fontWeight = FontWeight.SemiBold)
                Text(mode.description, style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.adaptiveInkMuted, maxLines = 1)
            }
            Text(mode.label, style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.adaptiveInkSecondary, fontWeight = FontWeight.SemiBold)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space1 + 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LongImageShortcutMode.entries.forEach { item ->
                LongImageShortcutChip(
                    mode = item,
                    selected = item == mode,
                    onClick = { onSelect(item) },
                )
            }
        }
    }
}

@Composable
private fun LongImageShortcutChip(
    mode: LongImageShortcutMode,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Text(
        mode.label,
        style = MaterialTheme.typography.labelSmall,
        color = if (selected) Color.White else GoaldayDesign.adaptiveInkSecondary,
        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        maxLines = 1,
        modifier = Modifier
            .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
            .background(if (selected) GoaldayDesign.Pink else GoaldayDesign.adaptiveWhiteOverlayMedium)
            .border(GoaldayDesign.Hairline, if (selected) GoaldayDesign.Pink.copy(alpha = 0.32f) else GoaldayDesign.BorderColor.copy(alpha = 0.09f), RoundedCornerShape(GoaldayDesign.RadiusPill))
            .clickable(onClick = onClick)
            .padding(horizontal = GoaldayDesign.Space2 + 2.dp, vertical = GoaldayDesign.Space1 + 1.dp),
    )
}

@Composable
private fun LongImageHistoryChip(item: LongImageExportHistoryItem) {
    Column(
        modifier = Modifier
            .width(142.dp)
            .clip(RoundedCornerShape(GoaldayDesign.RadiusM))
            .background(GoaldayDesign.adaptiveSurface)
            .border(GoaldayDesign.Hairline, GoaldayDesign.BorderColor.copy(alpha = 0.09f), RoundedCornerShape(GoaldayDesign.RadiusM))
            .padding(horizontal = GoaldayDesign.Space2 + 1.dp, vertical = GoaldayDesign.Space2 - 1.dp),
        verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space1 / 2),
    ) {
        Text("${item.action} · ${item.preset}", style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.adaptiveInkPrimary, fontWeight = FontWeight.SemiBold, maxLines = 1)
        Text(item.displayTime(), style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.adaptiveInkMuted, maxLines = 1)
        Text(item.title, style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.adaptiveInkSecondary, maxLines = 1)
    }
}

@Composable
private fun LongImagePresetChip(
    preset: LongImageExportPreset,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .width(138.dp)
            .clip(RoundedCornerShape(GoaldayDesign.RadiusL))
            .background(if (selected) GoaldayDesign.PinkTint else GoaldayDesign.adaptiveWhiteOverlayMedium)
            .border(
                width = if (selected) 1.dp else 0.6.dp,
                color = if (selected) GoaldayDesign.Pink.copy(alpha = 0.36f) else GoaldayDesign.BorderColor.copy(alpha = 0.09f),
                shape = RoundedCornerShape(GoaldayDesign.RadiusL),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = GoaldayDesign.Space2 + 2.dp, vertical = GoaldayDesign.Space2),
        verticalArrangement = Arrangement.spacedBy(GoaldayDesign.Space1 / 2),
    ) {
        Text(preset.label, style = MaterialTheme.typography.labelMedium, color = if (selected) GoaldayDesign.Pink else GoaldayDesign.adaptiveInkPrimary, fontWeight = FontWeight.SemiBold)
        Text(preset.description, style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.adaptiveInkMuted, maxLines = 2)
    }
}

@Composable
private fun LongImageInfoPill(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
            .background(GoaldayDesign.adaptiveWhiteOverlayMedium)
            .border(GoaldayDesign.Hairline, GoaldayDesign.BorderColor.copy(alpha = 0.09f), RoundedCornerShape(GoaldayDesign.RadiusPill))
            .padding(horizontal = GoaldayDesign.Space2 + 2.dp, vertical = GoaldayDesign.Space1 + 1.dp),
        horizontalArrangement = Arrangement.spacedBy(GoaldayDesign.Space1 + 1.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.adaptiveInkMuted)
        Text(value, style = MaterialTheme.typography.labelSmall, color = GoaldayDesign.adaptiveInkPrimary, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun LongImageActionChip(
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Text(
        label,
        style = MaterialTheme.typography.labelSmall,
        color = Color.White,
        fontWeight = FontWeight.SemiBold,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .then(modifier)
            .clip(RoundedCornerShape(GoaldayDesign.RadiusPill))
            .background(color)
            .clickable { onClick() }
            .padding(horizontal = GoaldayDesign.Space3 - 1.dp, vertical = GoaldayDesign.Space1 + 2.dp),
    )
}

internal fun renderHandbookScheduleLongImage(
    year: Int,
    month: Int,
    days: List<Int>,
    entries: List<ScheduleEntry>,
    weeklyTheme: String,
): Bitmap {
    val width = 1080
    val padding = 72f
    val contentWidth = width - padding * 2
    val estimatedHeight = 820 + days.size * 420
    val bitmap = Bitmap.createBitmap(width, estimatedHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawColor(GoaldayDesign.ExportCanvasPaper.toArgb())
    val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = GoaldayDesign.ExportInkPrimary.toArgb()
        textSize = 48f
        isFakeBoldText = true
    }
    val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF8B7A68.toInt()
        textSize = 28f
    }
    val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFB07A8F.toInt()
        textSize = 30f
        isFakeBoldText = true
    }
    val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF3A342E.toInt()
        textSize = 30f
    }
    val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFF7EFE6.toInt()
    }
    var y = 86f
    canvas.drawText("Goalday 日程手账", padding, y, titlePaint)
    y += 48f
    val range = days.firstOrNull()?.let { first ->
        val last = days.lastOrNull() ?: first
        "$year 年 $month 月 $first-$last 日"
    } ?: "$year 年 $month 月"
    canvas.drawText(range, padding, y, subtitlePaint)
    y += 54f
    if (weeklyTheme.isNotBlank()) {
        y = drawExportSection(canvas, "本周主题", weeklyTheme, padding, y, contentWidth, labelPaint, bodyPaint, cardPaint)
    }
    days.forEach { day ->
        val dayEntries = entries.filter { it.day == day }
        val todo = dayEntries.filterNot { it.completed }
        val done = dayEntries.filter { it.completed }
        val body = buildString {
            appendLine("todo")
            if (todo.isEmpty()) {
                appendLine("○ 暂无待办")
            } else {
                todo.take(8).forEach { entry ->
                    val time = entry.timeText.takeIf { it.isNotBlank() }?.let { "$it " }.orEmpty()
                    appendLine("○ $time${entry.title}")
                }
            }
            appendLine()
            appendLine("已完成")
            if (done.isEmpty()) {
                appendLine("✓ 暂无完成")
            } else {
                done.take(8).forEach { entry ->
                    val time = entry.timeText.takeIf { it.isNotBlank() }?.let { "$it " }.orEmpty()
                    appendLine("✓ $time${entry.title}")
                }
            }
        }
        y = drawExportSection(canvas, "${month}月${day}日", body.trim(), padding, y, contentWidth, labelPaint, bodyPaint, cardPaint)
    }
    val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFB7A893.toInt()
        textSize = 24f
    }
    y += 42f
    canvas.drawText("Goalday Local", padding, y, footerPaint)
    return Bitmap.createBitmap(bitmap, 0, 0, width, (y + 72f).toInt().coerceAtMost(bitmap.height))
}

internal fun exportDiaryLongImage(
    context: Context,
    title: String,
    state: StructuredDiary,
): Uri? = runCatching {
    val bitmap = renderDiaryLongImage(context, title, state)
    saveBitmapToPictures(context, bitmap, "Goalday_${System.currentTimeMillis()}.png")
}.getOrNull()

private fun shareLongImage(context: Context, uri: Uri): Boolean =
    runCatching {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "分享 Goalday 长图"))
        true
    }.getOrDefault(false)

internal fun renderDiaryLongImage(
    context: Context,
    title: String,
    state: StructuredDiary,
): Bitmap {
    val width = 1080
    val padding = 72f
    val contentWidth = width - padding * 2
    val exportImageUris = (state.imageBlockUris + state.legacyImageUris).distinct()
    val estimatedHeight = 1600 + exportImageUris.take(6).size * 360 + state.toRaw().length.coerceAtMost(2200)
    val scratch = Bitmap.createBitmap(width, estimatedHeight.coerceAtLeast(2200), Bitmap.Config.ARGB_8888)
    val canvas = Canvas(scratch)
    canvas.drawColor(GoaldayDesign.ExportCanvasPaper.toArgb())
    val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = GoaldayDesign.ExportInkPrimary.toArgb()
        textSize = 48f
        isFakeBoldText = true
    }
    val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF8B7A68.toInt()
        textSize = 28f
    }
    val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFB07A8F.toInt()
        textSize = 30f
        isFakeBoldText = true
    }
    val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF3A342E.toInt()
        textSize = 30f
    }
    val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFF7EFE6.toInt()
    }
    var y = 86f
    canvas.drawText(title.ifBlank { "Goalday 日记" }, padding, y, titlePaint)
    y += 48f
    canvas.drawText(diaryDateLabel(state.date), padding, y, subtitlePaint)
    y += 54f
    if (state.moodTags.isNotBlank()) {
        y = drawExportSection(canvas, "心情标签", state.moodTags, padding, y, contentWidth, labelPaint, bodyPaint, cardPaint)
    }
    y = drawExportSection(canvas, "今日完成", state.todayDone.ifBlank { "今天完成了什么？" }, padding, y, contentWidth, labelPaint, bodyPaint, cardPaint)
    y = drawExportSection(canvas, "工作任务", state.workTasks.ifBlank { "记录待推进的任务。" }, padding, y, contentWidth, labelPaint, bodyPaint, cardPaint)
    y = drawExportSection(canvas, "小幸福", state.smallJoy.ifBlank { "记录今天值得保留的一刻。" }, padding, y, contentWidth, labelPaint, bodyPaint, cardPaint)
    y = drawExportSection(canvas, "可改进", state.canImprove.ifBlank { "记录下一次可以优化的地方。" }, padding, y, contentWidth, labelPaint, bodyPaint, cardPaint)
    if (state.photoText.isNotBlank()) {
        y = drawExportSection(canvas, "图片描述", state.photoText, padding, y, contentWidth, labelPaint, bodyPaint, cardPaint)
    }
    if (state.richHtml.isNotBlank()) {
        y = drawExportSection(canvas, "富文本记录", plainTextFromHtml(state.richHtml), padding, y, contentWidth, labelPaint, bodyPaint, cardPaint)
    }
    state.blocks.take(8).forEach { block ->
        if (block.type == DiaryBlockType.IMAGE) {
            y += 12f
            canvas.drawText("图片记录", padding, y + 32f, labelPaint)
            y += 52f
            y = drawExportImage(context, canvas, block.text, padding, y, contentWidth, cardPaint)
        } else {
            val body = buildString {
                append(block.mainText.ifBlank { "空内容" })
                block.childLines.forEach { child ->
                    appendLine()
                    append("  - ")
                    append(child)
                }
            }
            y = drawExportSection(canvas, "${block.type.label} · ${block.style.label}", body, padding, y, contentWidth, labelPaint, bodyPaint, cardPaint)
        }
    }
    state.legacyImageUris.take(3).forEachIndexed { index, uri ->
        y += 12f
        canvas.drawText("图片 ${index + 1}", padding, y + 32f, labelPaint)
        y += 52f
        y = drawExportImage(context, canvas, uri, padding, y, contentWidth, cardPaint)
    }
    val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFB7A893.toInt()
        textSize = 24f
    }
    y += 48f
    canvas.drawText("Goalday Local", padding, y, footerPaint)
    val finalHeight = (y + 72f).toInt().coerceAtMost(scratch.height)
    return Bitmap.createBitmap(scratch, 0, 0, width, finalHeight)
}

private fun drawExportSection(
    canvas: Canvas,
    label: String,
    body: String,
    x: Float,
    y: Float,
    width: Float,
    labelPaint: Paint,
    bodyPaint: Paint,
    cardPaint: Paint,
): Float {
    val lines = wrapExportText(body, bodyPaint, width - 44f).ifEmpty { listOf(" ") }
    val height = 72f + lines.size * 40f
    val rect = RectF(x, y, x + width, y + height)
    canvas.drawRoundRect(rect, 22f, 22f, cardPaint)
    canvas.drawText(label, x + 22f, y + 40f, labelPaint)
    var lineY = y + 84f
    lines.forEach { line ->
        canvas.drawText(line, x + 22f, lineY, bodyPaint)
        lineY += 40f
    }
    return y + height + 24f
}

private fun drawExportImage(
    context: Context,
    canvas: Canvas,
    uri: String,
    x: Float,
    y: Float,
    width: Float,
    fallbackPaint: Paint,
): Float {
    val source = runCatching {
        context.contentResolver.openInputStream(Uri.parse(uri))?.use { stream ->
            BitmapFactory.decodeStream(stream)
        }
    }.getOrNull()
    val maxHeight = 320f
    val rect = RectF(x, y, x + width, y + maxHeight)
    canvas.drawRoundRect(rect, 22f, 22f, fallbackPaint)
    if (source != null) {
        val ratio = minOf(width / source.width, maxHeight / source.height)
        val drawWidth = source.width * ratio
        val drawHeight = source.height * ratio
        val dest = RectF(x + (width - drawWidth) / 2f, y + (maxHeight - drawHeight) / 2f, x + (width + drawWidth) / 2f, y + (maxHeight + drawHeight) / 2f)
        canvas.drawBitmap(source, null, dest, null)
    }
    return y + maxHeight + 24f
}

private fun wrapExportText(text: String, paint: Paint, maxWidth: Float): List<String> {
    val result = mutableListOf<String>()
    text.lines().forEach { paragraph ->
        var current = ""
        paragraph.forEach { char ->
            val next = current + char
            if (paint.measureText(next) > maxWidth && current.isNotBlank()) {
                result += current
                current = char.toString()
            } else {
                current = next
            }
        }
        if (current.isNotBlank()) result += current
    }
    return result.take(24)
}

private fun saveBitmapToPictures(context: Context, bitmap: Bitmap, fileName: String): Uri? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Goalday")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return null
        resolver.openOutputStream(uri)?.use { output ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
        }
        values.clear()
        values.put(MediaStore.Images.Media.IS_PENDING, 0)
        resolver.update(uri, values, null, null)
        uri
    } else {
        val dir = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Goalday").apply { mkdirs() }
        val file = File(dir, fileName)
        FileOutputStream(file).use { output ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
        }
        Uri.fromFile(file)
    }
}
