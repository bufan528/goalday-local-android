package com.bf410.goaldaylocal.ui.book

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

/**
 * 导出计划模型（对照原版 PrintPage）。
 *
 * - 分区勾选：`getDefaultSections()` = [打印PDF(周计划/日记) | 时间(开始/结束) | 预览]；
 *   `CheckableItem("1","周计划")` / `CheckableItem("2","日记")` id 与标题原样保留。
 * - 逐页预览项：`PreviewItem(date, content, PreviewType)`，开发版内容在渲染时组装，
 *   此处只定序：按日期逐天走，周一且勾选周计划→SCHEDULE 页，当天且勾选日记→DIARY 页
 *   （对照 `m31229Z0` 的 RenderMode 分支；空日记也成页，渲染为空白模板页，不跳过）。
 * - 预览截断：`z && size>=30 break`，预览最多 30 页；正式生成不限。
 * - 起止非法（开始>结束）或日期未选→空表（对照 `m31229Z0` 的空返回）。
 * - 任务队列：`ExportPdfQueue` FIFO（对照 `ConcurrentLinkedQueue<PdfTask>`），逐项渲染、
 *   进度可查、可取消（协程 Job）。
 */
internal enum class ExportPreviewType(val label: String) {
    SCHEDULE("周计划"),
    DIARY("日记"),
}

internal data class ExportCheckableItem(
    val id: String,
    val title: String,
)

internal data class ExportPreviewItem(
    val date: LocalDate,
    val type: ExportPreviewType,
) {
    /** 行标题：日记 "9月16日 周三"，周计划 "9月14日那周"。 */
    val title: String get() = when (type) {
        ExportPreviewType.SCHEDULE -> "${date.monthValue}月${date.dayOfMonth}日那周"
        ExportPreviewType.DIARY -> "${date.monthValue}月${date.dayOfMonth}日 ${WEEKDAY_NAMES_CN[date.dayOfWeek.value - 1]}"
    }
}

internal const val EXPORT_PREVIEW_CAP = 30
internal const val EXPORT_SCHEDULE_ID = "1"
internal const val EXPORT_DIARY_ID = "2"

/** 对照 `getDefaultSections()` 的打印 PDF 分区勾选项（默认双勾）。 */
internal fun defaultExportCheckables(): List<ExportCheckableItem> = listOf(
    ExportCheckableItem(EXPORT_SCHEDULE_ID, "周计划"),
    ExportCheckableItem(EXPORT_DIARY_ID, "日记"),
)

/**
 * 组装待办页表。`previewCap=true` 时最多 [EXPORT_PREVIEW_CAP] 页（预览用），
 * 正式生成传 `previewCap=false`（对照 `m31229Z0(z)` 的 z 开关）。
 */
internal fun buildExportItems(
    startDate: LocalDate,
    endDate: LocalDate,
    includeSchedule: Boolean,
    includeDiary: Boolean,
    previewCap: Boolean,
): List<ExportPreviewItem> {
    if (startDate.isAfter(endDate)) return emptyList()
    if (!includeSchedule && !includeDiary) return emptyList()
    val items = ArrayList<ExportPreviewItem>()
    var cursor = startDate
    while (!cursor.isAfter(endDate)) {
        if (includeSchedule && cursor.dayOfWeek == DayOfWeek.MONDAY) {
            items.add(ExportPreviewItem(cursor, ExportPreviewType.SCHEDULE))
        }
        if (includeDiary) {
            items.add(ExportPreviewItem(cursor, ExportPreviewType.DIARY))
        }
        if (previewCap && items.size >= EXPORT_PREVIEW_CAP) break
        cursor = cursor.plusDays(1)
    }
    return items
}

/** 周计划页对应的周一（页内渲染 Mon-Sun 整周，对照书内周 spread）。 */
internal fun ExportPreviewItem.weekMonday(): LocalDate =
    date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

private val WEEKDAY_NAMES_CN = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")

/**
 * PDF 渲染任务队列（对照 `ConcurrentLinkedQueue<PdfTask>` + 分批 `processBatch`）。
 * 调用方按序 `poll()` 渲染、每项完成后记 `markDone()` 报进度；`cancelled` 中断后续。
 */
internal class ExportPdfQueue(items: List<ExportPreviewItem>) {
    private val pending: ArrayDeque<ExportPreviewItem> = ArrayDeque(items)
    val total: Int = items.size
    var doneCount: Int = 0
        private set
    var cancelled: Boolean = false

    val isEmpty: Boolean get() = pending.isEmpty()
    val remaining: Int get() = pending.size

    fun poll(): ExportPreviewItem? {
        if (cancelled) return null
        return pending.removeFirstOrNull()
    }

    fun markDone() {
        doneCount++
    }
}
