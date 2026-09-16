package com.bf410.goaldaylocal.ui.book

import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bf410.goaldaylocal.data.ScheduleEntry
import com.bf410.goaldaylocal.ui.replica.GoaldayDesign
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

/** 导出内容模式（对照原版 PrintPage.RenderMode：ONLY_DIARY / ONLY_SCHEDULE / BOTH） */
internal enum class ExportContentMode(val label: String) {
    BOTH("日记+日程"),
    DIARY("仅日记"),
    SCHEDULE("仅日程"),
}

/**
 * 导出中心底部弹层（对照原版 PrintPage：内容筛选 + 起止日期 + 真 PDF 生成 + 系统分享）。
 * - 日记：范围内每天一页（复用 renderDiaryLongImage 纸张风格排版）；
 * - 日程：范围内每周一页（对照书内周日程 spread，Mon-Sun 七行）；
 * - 两者：每周 [周日程页 + 7 张日记页]。
 */
@Composable
internal fun ExportCenterSheet(
    diaryTextFor: (LocalDate) -> String,
    scheduleEntries: List<ScheduleEntry>,
    weeklyTheme: String,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val now = LocalDate.now()
    var mode by remember { mutableStateOf(ExportContentMode.BOTH) }
    var startDate by remember { mutableStateOf(LocalDate.of(now.year, 1, 1)) }
    var endDate by remember { mutableStateOf(LocalDate.of(now.year, 12, 31)) }
    var generating by remember { mutableStateOf(false) }
    var resultUri by remember { mutableStateOf<Uri?>(null) }

    fun pickDate(current: LocalDate, onPicked: (LocalDate) -> Unit) {
        DatePickerDialog(
            context,
            { _, year, month, day -> onPicked(LocalDate.of(year, month + 1, day)) },
            current.year,
            current.monthValue - 1,
            current.dayOfMonth,
        ).show()
    }

    Surface(color = Color_White) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp)) {
            Box(Modifier.fillMaxWidth()) {
                Text(
                    "导出中心",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GoaldayDesign.InkPrimary,
                    modifier = Modifier.align(Alignment.Center),
                )
                Text(
                    "取消",
                    fontSize = 15.sp,
                    color = Color_Blue,
                    modifier = Modifier.align(Alignment.CenterEnd).clickable { onDismiss() },
                )
            }
            Spacer(Modifier.height(16.dp))

            Text("内容", fontSize = 13.sp, color = GoaldayDesign.InkMuted)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ExportContentMode.entries.forEach { candidate ->
                    val active = mode == candidate
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (active) GoaldayDesign.PinkSoft else Color_SurfaceSoft)
                            .clickable { mode = candidate }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                    ) {
                        Text(
                            candidate.label,
                            fontSize = 14.sp,
                            color = if (active) GoaldayDesign.Pink else GoaldayDesign.adaptiveInkPrimary,
                            fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                        )
                    }
                }
            }
            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color_SurfaceSoft)
                    .clickable { pickDate(startDate) { startDate = it } }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("起始日期", fontSize = 14.sp, color = GoaldayDesign.InkPrimary)
                Text(startDate.toString(), fontSize = 14.sp, color = GoaldayDesign.adaptiveInkMuted)
            }
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color_SurfaceSoft)
                    .clickable { pickDate(endDate) { endDate = it } }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("结束日期", fontSize = 14.sp, color = GoaldayDesign.InkPrimary)
                Text(endDate.toString(), fontSize = 14.sp, color = GoaldayDesign.adaptiveInkMuted)
            }
            Spacer(Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(GoaldayDesign.InkPrimary)
                    .clickable(enabled = !generating) {
                        scope.launch {
                            if (startDate.isAfter(endDate)) return@launch
                            generating = true
                            resultUri = withContext(Dispatchers.Default) {
                                runCatching {
                                    generateExportPdf(
                                        context = context,
                                        startDate = startDate,
                                        endDate = endDate,
                                        mode = mode,
                                        diaryTextFor = diaryTextFor,
                                        scheduleEntries = scheduleEntries,
                                        weeklyTheme = weeklyTheme,
                                    )
                                }.getOrNull()
                            }
                            generating = false
                        }
                    }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    if (generating) "生成中…" else "生成 PDF",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                )
            }

            if (resultUri != null) {
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color_SurfaceSoft)
                            .clickable { sharePdf(context, resultUri!!) }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                    ) {
                        Text("分享 PDF", fontSize = 14.sp, color = GoaldayDesign.InkPrimary)
                    }
                    Text(
                        "已保存到 Download/Goalday",
                        fontSize = 12.sp,
                        color = GoaldayDesign.adaptiveInkMuted,
                        modifier = Modifier.padding(vertical = 12.dp),
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

/** 组装 PDF：日程按周成页、日记按天成页（空日记跳过） */
private fun generateExportPdf(
    context: Context,
    startDate: LocalDate,
    endDate: LocalDate,
    mode: ExportContentMode,
    diaryTextFor: (LocalDate) -> String,
    scheduleEntries: List<ScheduleEntry>,
    weeklyTheme: String,
): Uri? {
    val document = PdfDocument()
    try {
        if (mode != ExportContentMode.DIARY) {
            var weekCursor = startDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            while (!weekCursor.isAfter(endDate)) {
                val days = (0..6).map { weekCursor.plusDays(it.toLong()) }
                val bitmap = renderHandbookScheduleLongImage(
                    year = weekCursor.year,
                    month = weekCursor.monthValue,
                    days = days.map { it.dayOfMonth },
                    entries = scheduleEntries,
                    weeklyTheme = weeklyTheme,
                )
                appendPdfPage(document, bitmap)
                weekCursor = weekCursor.plusWeeks(1)
            }
        }
        if (mode != ExportContentMode.SCHEDULE) {
            var dayCursor = startDate
            while (!dayCursor.isAfter(endDate)) {
                val state = StructuredDiary.fromRaw(diaryTextFor(dayCursor))
                if (state.isNotEmpty()) {
                    val bitmap = renderDiaryLongImage(
                        context = context,
                        title = "${dayCursor.monthValue}月${dayCursor.dayOfMonth}日 · ${dayCursor.year}",
                        state = state,
                    )
                    appendPdfPage(document, bitmap)
                }
                dayCursor = dayCursor.plusDays(1)
            }
        }
        // 范围内无内容时放一张空白页，保证产出有效文件
        if (document.pages.size == 0) {
            val scratch = Bitmap.createBitmap(1080, 1440, Bitmap.Config.ARGB_8888)
            scratch.eraseColor(GoaldayDesign.ExportCanvasPaper.toArgb())
            appendPdfPage(document, scratch)
        }
        return savePdfToDownloads(
            context = context,
            document = document,
            fileName = "Goalday_导出_${startDate}_${endDate}_${System.currentTimeMillis()}.pdf",
        )
    } finally {
        document.close()
    }
}

private fun StructuredDiary.isNotEmpty(): Boolean =
    blocks.isNotEmpty() || legacyImageUris.isNotEmpty() ||
        photoText.isNotBlank() || richHtml.isNotBlank() ||
        todayDone.isNotBlank() || workTasks.isNotBlank() ||
        smallJoy.isNotBlank() || canImprove.isNotBlank()

private fun appendPdfPage(document: PdfDocument, bitmap: Bitmap) {
    val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, document.pages.size + 1).create()
    val page = document.startPage(pageInfo)
    page.canvas.drawBitmap(bitmap, 0f, 0f, null)
    document.finishPage(page)
}

private fun savePdfToDownloads(context: Context, document: PdfDocument, fileName: String): Uri? =
    runCatching {
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
            put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/Goalday")
            put(MediaStore.Downloads.IS_PENDING, 1)
        }
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values) ?: return null
        resolver.openOutputStream(uri)?.use { output -> document.writeTo(output) }
        values.clear()
        values.put(MediaStore.Downloads.IS_PENDING, 0)
        resolver.update(uri, values, null, null)
        uri
    }.getOrNull()

private fun sharePdf(context: Context, uri: Uri) {
    runCatching {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "分享 Goalday PDF"))
    }
}

private val Color_White = androidx.compose.ui.graphics.Color.White
private val Color_Blue = androidx.compose.ui.graphics.Color(0xFF3875F6)
private val Color_SurfaceSoft = androidx.compose.ui.graphics.Color(0xFFF3EEE9)