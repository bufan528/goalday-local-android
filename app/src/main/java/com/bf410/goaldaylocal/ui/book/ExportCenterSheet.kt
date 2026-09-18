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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import java.time.LocalDate

/**
 * 导出中心底部弹层（对照原版 PrintPage `getDefaultSections` 三分区：打印PDF + 时间 + 预览）。
 * - 打印PDF：勾选项 周计划(id=1)/日记(id=2)，默认双勾（对照 CheckableItem）；
 * - 时间：起止日期（对照 DATE 分区 开始/结束）；
 * - 预览：待办页表（对照 PreviewItem(date, type)，周一出周计划页、每天出日记页，空日记也成页；
 *   预览截断 30 页，正式生成不限，对照 `m31229Z0` 的 z 开关）；
 * - 生成走 FIFO 任务队列逐项渲染并报进度（对照 ConcurrentLinkedQueue<PdfTask> + processBatch），
 *   离开弹层取消剩余任务。
 * - 日记每天一页（复用 renderDiaryLongImage 纸张风格排版，空日记为空白模板页）；
 * - 日程逢周一出一页（对照书内周日程 spread，Mon-Sun 七行）；
 * - 双勾时按日期交错（周一先周计划后日记，对照原版逐天走表的顺序）。
 */
@Composable
internal fun ExportCenterSheet(
    diaryTextFor: (LocalDate) -> String,
    scheduleEntries: List<ScheduleEntry>,
    weeklyTheme: String,
    onDismiss: () -> Unit,
    fullscreen: Boolean = false,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val now = LocalDate.now()
    var includeSchedule by remember { mutableStateOf(true) }
    var includeDiary by remember { mutableStateOf(true) }
    var startDate by remember { mutableStateOf<LocalDate?>(null) }
    var endDate by remember { mutableStateOf<LocalDate?>(null) }
    var generating by remember { mutableStateOf(false) }
    var progressDone by remember { mutableStateOf(0) }
    var progressTotal by remember { mutableStateOf(0) }
    var resultUri by remember { mutableStateOf<Uri?>(null) }
    var runJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }
    // 离开弹层取消未做完的渲染任务（对照队列取消）
    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose { runJob?.cancel() }
    }
    val rangeValid = startDate != null && endDate != null && !startDate!!.isAfter(endDate)
    // 预览页表（截断 30，正式生成用不限长的同一顺序）
    val previewItems = remember(startDate, endDate, includeSchedule, includeDiary) {
        val s = startDate
        val e = endDate
        if (s == null || e == null || s.isAfter(e)) emptyList()
        else buildExportItems(s, e, includeSchedule, includeDiary, previewCap = true)
    }
    val fullCount = remember(startDate, endDate, includeSchedule, includeDiary) {
        val s = startDate
        val e = endDate
        if (s == null || e == null || s.isAfter(e)) 0
        else buildExportItems(s, e, includeSchedule, includeDiary, previewCap = false).size
    }
    val canGenerate = !generating && rangeValid && (includeSchedule || includeDiary) && fullCount > 0

    fun pickDate(current: LocalDate?, onPicked: (LocalDate) -> Unit) {
        val base = current ?: LocalDate.now()
        DatePickerDialog(
            context,
            { _, year, month, day -> onPicked(LocalDate.of(year, month + 1, day)) },
            base.year,
            base.monthValue - 1,
            base.dayOfMonth,
        ).show()
    }

    fun startGenerate() {
        val s = startDate
        val e = endDate
        if (!canGenerate || s == null || e == null) return
        runJob = scope.launch {
            generating = true
            progressDone = 0
            resultUri = withContext(Dispatchers.Default) {
                runCatching {
                    generateExportPdfQueued(
                        context = context,
                        startDate = s,
                        endDate = e,
                        includeSchedule = includeSchedule,
                        includeDiary = includeDiary,
                        diaryTextFor = diaryTextFor,
                        scheduleEntries = scheduleEntries,
                        weeklyTheme = weeklyTheme,
                        onProgress = { done, total ->
                            progressDone = done
                            progressTotal = total
                        },
                    )
                }.getOrNull()
            }
            generating = false
        }
    }

    Surface(color = GoaldayDesign.AppBg) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            Box(Modifier.fillMaxWidth()) {
                Text(
                    "取消",
                    fontSize = 16.sp,
                    color = Color_Blue,
                    modifier = Modifier.align(Alignment.CenterStart).clickable { onDismiss() },
                )
                Text(
                    "设置选项",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoaldayDesign.InkPrimary,
                    modifier = Modifier.align(Alignment.Center),
                )
                Text(
                    if (generating) "生成中…" else "确定",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GoaldayDesign.MorandiCoral,
                    modifier = Modifier.align(Alignment.CenterEnd).clickable { startGenerate() },
                )
            }
            Spacer(Modifier.height(16.dp))

            Text("打印PDF", fontSize = 13.sp, color = GoaldayDesign.InkMuted)
            Spacer(Modifier.height(8.dp))
            val checkables = defaultExportCheckables()
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color_White),
            ) {
                checkables.forEachIndexed { ci, item ->
                    val checked = when (item.id) {
                        EXPORT_SCHEDULE_ID -> includeSchedule
                        else -> includeDiary
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (item.id == EXPORT_SCHEDULE_ID) includeSchedule = !includeSchedule
                                else includeDiary = !includeDiary
                            }
                            .padding(horizontal = 14.dp, vertical = 13.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(item.title, fontSize = 16.sp, color = GoaldayDesign.InkPrimary)
                        if (checked) {
                            Text("✓", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }
                    if (ci < checkables.lastIndex) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp)
                                .height(0.7.dp)
                                .background(GoaldayDesign.InkMuted.copy(alpha = 0.25f)),
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))

            Text("时间", fontSize = 13.sp, color = GoaldayDesign.InkMuted)
            Spacer(Modifier.height(8.dp))

            @Composable
            fun DateRow(label: String, date: LocalDate?, onPick: (LocalDate) -> Unit) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { pickDate(date, onPick) }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(label, fontSize = 16.sp, color = GoaldayDesign.InkPrimary)
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(GoaldayDesign.InkMuted.copy(alpha = 0.55f))
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                    ) {
                        Text(
                            date?.toString() ?: "选择日期",
                            fontSize = 14.sp,
                            color = Color.White,
                        )
                    }
                }
            }
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color_White),
            ) {
                DateRow("开始", startDate) { startDate = it }
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp)
                        .height(0.7.dp)
                        .background(GoaldayDesign.InkMuted.copy(alpha = 0.25f)),
                )
                DateRow("结束", endDate) { endDate = it }
            }
            Spacer(Modifier.height(16.dp))

            // 预览分区
            Text("预览", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = GoaldayDesign.InkPrimary)
            Spacer(Modifier.height(8.dp))
            if (previewItems.isEmpty()) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color_White),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "请选择时间范围和内容类型",
                        fontSize = 14.sp,
                        color = GoaldayDesign.adaptiveInkMuted,
                    )
                }
            } else {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color_White)
                        .padding(vertical = 6.dp),
                ) {
                    previewItems.forEachIndexed { index, item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 9.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                "${index + 1}. ${item.title}",
                                fontSize = 13.sp,
                                color = GoaldayDesign.InkPrimary,
                            )
                            Text(
                                item.type.label,
                                fontSize = 11.sp,
                                color = GoaldayDesign.adaptiveInkMuted,
                            )
                        }
                    }
                }
                if (fullCount > previewItems.size) {
                    Text(
                        "页面较多，仅显示前 ${previewItems.size} 页",
                        fontSize = 11.sp,
                        color = GoaldayDesign.adaptiveInkMuted,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
            }
            Spacer(Modifier.height(16.dp))

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

/** 组装 PDF：FIFO 任务队列逐项渲染（对照 PdfTask 队列 + processBatch 顺序），空日记也成页 */
private fun generateExportPdfQueued(
    context: Context,
    startDate: LocalDate,
    endDate: LocalDate,
    includeSchedule: Boolean,
    includeDiary: Boolean,
    diaryTextFor: (LocalDate) -> String,
    scheduleEntries: List<ScheduleEntry>,
    weeklyTheme: String,
    onProgress: (done: Int, total: Int) -> Unit,
): Uri? {
    val queue = ExportPdfQueue(
        buildExportItems(startDate, endDate, includeSchedule, includeDiary, previewCap = false),
    )
    if (queue.total == 0) return null
    val document = PdfDocument()
    try {
        onProgress(0, queue.total)
        while (true) {
            val item = queue.poll() ?: break
            val bitmap = when (item.type) {
                ExportPreviewType.SCHEDULE -> {
                    val monday = item.weekMonday()
                    val days = (0..6).map { monday.plusDays(it.toLong()) }
                    renderHandbookScheduleLongImage(
                        year = monday.year,
                        month = monday.monthValue,
                        days = days.map { it.dayOfMonth },
                        entries = scheduleEntries,
                        weeklyTheme = weeklyTheme,
                    )
                }
                ExportPreviewType.DIARY -> {
                    val day = item.date
                    renderDiaryLongImage(
                        context = context,
                        title = "${day.monthValue}月${day.dayOfMonth}日 · ${day.year}",
                        state = StructuredDiary.fromRaw(diaryTextFor(day)),
                    )
                }
            }
            appendPdfPage(document, bitmap)
            queue.markDone()
            onProgress(queue.doneCount, queue.total)
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