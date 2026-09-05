package com.bf410.goaldaylocal.ui.book

import java.time.DayOfWeek
import java.time.LocalDate

internal data class StructuredDiary(
    val dateIso: String,
    val moodTags: String,
    val todayDone: String,
    val workTasks: String,
    val smallJoy: String,
    val canImprove: String,
    val photoNotes: String,
    val richHtml: String,
    val blocksRaw: String,
) {
    val imageUris: List<String>
        get() = photoNotes.lines()
            .map(String::trim)
            .filter { it.startsWith(DIARY_IMAGE_PREFIX) }
            .map { it.removePrefix(DIARY_IMAGE_PREFIX).trim() }
            .filter(String::isNotBlank)

    val photoText: String
        get() = photoNotes.lines()
            .map(String::trim)
            .filter { it.isNotBlank() && !it.startsWith(DIARY_IMAGE_PREFIX) }
            .joinToString("\n")

    val date: LocalDate
        get() = runCatching { LocalDate.parse(dateIso) }.getOrElse { LocalDate.now() }

    val blocks: List<DiaryEntryBlock>
        get() = blocksRaw.lines()
            .mapNotNull(DiaryEntryBlock::fromRawLine)

    val imageBlockUris: List<String>
        get() = blocks
            .filter { it.type == DiaryBlockType.IMAGE }
            .map { it.text.trim() }
            .filter(String::isNotBlank)

    val legacyImageUris: List<String>
        get() = imageUris.filterNot { it in imageBlockUris }

    val hasUserContent: Boolean
        get() = moodTags.isNotBlank() ||
            todayDone.isNotBlank() ||
            workTasks.isNotBlank() ||
            smallJoy.isNotBlank() ||
            canImprove.isNotBlank() ||
            photoText.isNotBlank() ||
            richHtml.isNotBlank() ||
            blocks.isNotEmpty() ||
            imageUris.isNotEmpty()

    fun withDate(date: LocalDate): StructuredDiary =
        copy(dateIso = date.toString())

    fun withPhotoText(text: String): StructuredDiary =
        copy(photoNotes = mergeDiaryPhotoNotes(text, imageUris))

    fun withRichHtml(html: String): StructuredDiary =
        copy(richHtml = html)

    fun withImageUri(uri: String): StructuredDiary {
        val normalized = uri.trim()
        if (normalized.isBlank()) return this
        val next = copy(photoNotes = mergeDiaryPhotoNotes(photoText, (imageUris + normalized).distinct()))
        return if (normalized in next.imageBlockUris) {
            next
        } else {
            next.withBlocks(next.blocks + DiaryEntryBlock(DiaryBlockType.IMAGE, normalized))
        }
    }

    fun withoutImageUri(uri: String): StructuredDiary =
        copy(photoNotes = mergeDiaryPhotoNotes(photoText, imageUris.filterNot { it == uri }))
            .withBlocks(blocks.filterNot { it.type == DiaryBlockType.IMAGE && it.text.trim() == uri })

    fun withCompletedTarget(item: String): StructuredDiary =
        copy(todayDone = appendUniqueDiaryLine(todayDone, item))

    fun withWorkTarget(item: String): StructuredDiary =
        copy(workTasks = appendUniqueDiaryLine(workTasks, item))

    fun withTextBlock(text: String = "写下这一刻"): StructuredDiary =
        withBlocks(blocks + DiaryEntryBlock(DiaryBlockType.TEXT, text))

    fun withTargetBlock(item: String, completed: Boolean): StructuredDiary =
        withBlocks(blocks + DiaryEntryBlock(DiaryBlockType.TARGET, item, if (completed) DiaryBlockStyle.CHECK else DiaryBlockStyle.BODY))

    fun withTopicTargetBlock(text: String = "专题目标 · 今天推进一步"): StructuredDiary =
        withBlocks(blocks + DiaryEntryBlock(DiaryBlockType.TOPIC_TARGET, text))

    fun withTargetChildBlock(text: String = "下一步行动"): StructuredDiary =
        withBlocks(blocks + DiaryEntryBlock(DiaryBlockType.TARGET_CHILD, text, DiaryBlockStyle.CHECK))

    fun withInsertedBlockAfter(index: Int, type: DiaryBlockType): StructuredDiary {
        val insertIndex = (index + 1).coerceIn(0, blocks.size)
        val next = blocks.toMutableList().apply {
            add(insertIndex, defaultDiaryBlock(type))
        }
        return withBlocks(next)
    }

    fun withBlockText(index: Int, text: String): StructuredDiary {
        val oldBlock = blocks.getOrNull(index)
        val next = withBlocks(blocks.mapIndexed { blockIndex, block ->
            if (blockIndex == index) block.copy(text = text) else block
        })
        return if (oldBlock?.type == DiaryBlockType.IMAGE) {
            val oldUri = oldBlock.text.trim()
            val nextUri = text.trim()
            next.copy(
                photoNotes = mergeDiaryPhotoNotes(
                    next.photoText,
                    next.imageUris.map { uri -> if (uri == oldUri) nextUri else uri }.filter(String::isNotBlank).distinct(),
                ),
            )
        } else {
            next
        }
    }

    fun withBlockStyle(index: Int, style: DiaryBlockStyle): StructuredDiary =
        withBlocks(blocks.mapIndexed { blockIndex, block ->
            if (blockIndex == index) block.copy(style = style) else block
        })

    fun withBlockChild(index: Int): StructuredDiary {
        val parent = blocks.getOrNull(index) ?: return this
        if (parent.type == DiaryBlockType.TARGET || parent.type == DiaryBlockType.TOPIC_TARGET ||
            parent.type == DiaryBlockType.TARGET_IN_BOOK || parent.type == DiaryBlockType.TOPIC_TARGET_IN_BOOK
        ) {
            val childType = if (parent.type == DiaryBlockType.TARGET_IN_BOOK || parent.type == DiaryBlockType.TOPIC_TARGET_IN_BOOK) {
                DiaryBlockType.TARGET_CHILD_IN_BOOK
            } else {
                DiaryBlockType.TARGET_CHILD
            }
            val next = blocks.toMutableList().apply {
                add(index + 1, DiaryEntryBlock(childType, "下一步行动", DiaryBlockStyle.CHECK))
            }
            return withBlocks(next)
        }
        return withBlocks(blocks.mapIndexed { blockIndex, block ->
            if (blockIndex == index) block.withChildLine() else block
        })
    }

    fun withMovedBlock(index: Int, direction: Int): StructuredDiary {
        val targetIndex = (index + direction).coerceIn(0, blocks.lastIndex)
        if (index !in blocks.indices || targetIndex == index) return this
        val next = blocks.toMutableList()
        val item = next.removeAt(index)
        next.add(targetIndex, item)
        return withBlocks(next)
    }

    fun withoutBlock(index: Int): StructuredDiary =
        withBlocks(blocks.filterIndexed { blockIndex, _ -> blockIndex != index })

    private fun withBlocks(nextBlocks: List<DiaryEntryBlock>): StructuredDiary =
        copy(blocksRaw = nextBlocks.joinToString("\n") { it.toRawLine() })

    fun toRaw(): String = buildString {
        appendLine("# 日期")
        appendLine(dateIso)
        appendLine("# 心情标签")
        appendLine(moodTags.trim())
        appendLine("# 今日完成")
        appendLine(todayDone.trim())
        appendLine("# 工作任务")
        appendLine(workTasks.trim())
        appendLine("# 小幸福")
        appendLine(smallJoy.trim())
        appendLine("# 可改进")
        appendLine(canImprove.trim())
        appendLine("# 图片")
        append(photoNotes.trim())
        if (richHtml.isNotBlank()) {
            appendLine()
            appendLine("# 富文本")
            append(escapeDiaryBlockText(richHtml.trim()))
        }
        if (blocksRaw.isNotBlank()) {
            appendLine()
            appendLine("# 日记块")
            append(blocksRaw.trim())
        }
    }

    companion object {
        fun fromRaw(raw: String): StructuredDiary {
            if (raw.isBlank()) return StructuredDiary(LocalDate.now().toString(), "", "", "", "", "", "", "", "")
            fun section(name: String, nextMarkers: List<String> = emptyList()): String {
                val start = raw.indexOf("# $name")
                if (start < 0) return ""
                val bodyStart = raw.indexOf('\n', start).takeIf { it >= 0 }?.plus(1) ?: return ""
                val bodyEnd = nextMarkers
                    .mapNotNull { marker -> raw.indexOf("# $marker", bodyStart).takeIf { it >= 0 } }
                    .minOrNull()
                    ?: raw.length
                return raw.substring(bodyStart, bodyEnd).trim()
            }
            return StructuredDiary(
                dateIso = section("日期", listOf("心情标签")).ifBlank { LocalDate.now().toString() },
                moodTags = section("心情标签", listOf("今日完成")),
                todayDone = section("今日完成", listOf("工作任务")),
                workTasks = section("工作任务", listOf("小幸福")),
                smallJoy = section("小幸福", listOf("可改进")),
                canImprove = section("可改进", listOf("图片")),
                photoNotes = section("图片", listOf("富文本", "日记块")),
                richHtml = unescapeDiaryBlockText(section("富文本", listOf("日记块"))),
                blocksRaw = section("日记块"),
            )
        }
    }
}

internal const val DIARY_IMAGE_PREFIX = "image:"
private const val DIARY_BLOCK_SEPARATOR = "|"

internal enum class DiaryBlockType(val raw: String, val label: String) {
    IMAGE("image", "图片"),
    TEXT("text", "文字"),
    TARGET("target", "目标"),
    TARGET_CHILD("target_child", "子目标"),
    TOPIC_TARGET("topic_target", "专题目标"),
    TARGET_IN_BOOK("target_in_book", "书内目标"),
    TARGET_CHILD_IN_BOOK("target_child_inbook", "书内子目标"),
    TOPIC_TARGET_IN_BOOK("topic_target_inbook", "书内专题目标"),
}

internal enum class DiaryBlockStyle(val raw: String, val label: String) {
    BODY("body", "正文"),
    BOLD("bold", "加粗"),
    QUOTE("quote", "引用"),
    CHECK("check", "清单"),
}

internal data class DiaryEntryBlock(
    val type: DiaryBlockType,
    val text: String,
    val style: DiaryBlockStyle = DiaryBlockStyle.BODY,
) {
    val mainText: String
        get() = text.lines().firstOrNull().orEmpty()

    val childLines: List<String>
        get() = text.lines()
            .drop(1)
            .map { it.trim().removePrefix("-").trim() }
            .filter(String::isNotBlank)

    fun withChildLine(): DiaryEntryBlock =
        copy(text = listOf(text.trimEnd(), "- 下一步行动").filter(String::isNotBlank).joinToString("\n"))

    fun toRawLine(): String =
        "${type.raw}$DIARY_BLOCK_SEPARATOR${style.raw}$DIARY_BLOCK_SEPARATOR${escapeDiaryBlockText(text)}"

    companion object {
        fun fromRawLine(line: String): DiaryEntryBlock? {
            val trimmed = line.trim()
            if (trimmed.isBlank()) return null
            val typeRaw = trimmed.substringBefore(DIARY_BLOCK_SEPARATOR, missingDelimiterValue = DiaryBlockType.TEXT.raw)
            val afterType = trimmed.substringAfter(DIARY_BLOCK_SEPARATOR, trimmed)
            val type = DiaryBlockType.entries.firstOrNull { it.raw == typeRaw } ?: DiaryBlockType.TEXT
            val styleRaw = afterType.substringBefore(DIARY_BLOCK_SEPARATOR, missingDelimiterValue = "")
            val style = DiaryBlockStyle.entries.firstOrNull { it.raw == styleRaw }
            val contentRaw = if (style == null) afterType else afterType.substringAfter(DIARY_BLOCK_SEPARATOR, "")
            return DiaryEntryBlock(type, unescapeDiaryBlockText(contentRaw), style ?: DiaryBlockStyle.BODY)
        }
    }
}

internal fun defaultDiaryBlock(type: DiaryBlockType): DiaryEntryBlock =
    when (type) {
        DiaryBlockType.IMAGE -> DiaryEntryBlock(DiaryBlockType.IMAGE, "")
        DiaryBlockType.TEXT -> DiaryEntryBlock(DiaryBlockType.TEXT, "写下这一刻")
        DiaryBlockType.TARGET -> DiaryEntryBlock(DiaryBlockType.TARGET, "关联一个目标")
        DiaryBlockType.TARGET_CHILD -> DiaryEntryBlock(DiaryBlockType.TARGET_CHILD, "下一步行动", DiaryBlockStyle.CHECK)
        DiaryBlockType.TOPIC_TARGET -> DiaryEntryBlock(DiaryBlockType.TOPIC_TARGET, "专题目标 · 今天推进一步")
        DiaryBlockType.TARGET_IN_BOOK -> DiaryEntryBlock(DiaryBlockType.TARGET_IN_BOOK, "今日完成", DiaryBlockStyle.CHECK)
        DiaryBlockType.TARGET_CHILD_IN_BOOK -> DiaryEntryBlock(DiaryBlockType.TARGET_CHILD_IN_BOOK, "下一步行动", DiaryBlockStyle.CHECK)
        DiaryBlockType.TOPIC_TARGET_IN_BOOK -> DiaryEntryBlock(DiaryBlockType.TOPIC_TARGET_IN_BOOK, "专题目标 · 今天推进一步")
    }

internal fun escapeDiaryBlockText(text: String): String =
    text.replace("\\", "\\\\").replace("\n", "\\n").replace(DIARY_BLOCK_SEPARATOR, "\\p")

internal fun unescapeDiaryBlockText(text: String): String {
    val builder = StringBuilder()
    var escaping = false
    text.forEach { char ->
        if (escaping) {
            builder.append(
                when (char) {
                    'n' -> '\n'
                    'p' -> DIARY_BLOCK_SEPARATOR
                    '\\' -> '\\'
                    else -> char
                },
            )
            escaping = false
        } else if (char == '\\') {
            escaping = true
        } else {
            builder.append(char)
        }
    }
    if (escaping) builder.append('\\')
    return builder.toString()
}

internal fun mergeDiaryPhotoNotes(text: String, imageUris: List<String>): String =
    buildList {
        text.lines().map(String::trim).filter(String::isNotBlank).forEach(::add)
        imageUris.distinct().forEach { uri -> add("$DIARY_IMAGE_PREFIX$uri") }
    }.joinToString("\n")

internal fun appendUniqueDiaryLine(raw: String, item: String): String {
    val normalized = item.trim()
    if (normalized.isBlank()) return raw
    val lines = raw.lines().map(String::trim).filter(String::isNotBlank)
    if (normalized in lines) return raw
    return (lines + normalized).joinToString("\n")
}

internal fun diaryDateLabel(date: LocalDate): String {
    val weekday = when (date.dayOfWeek) {
        DayOfWeek.MONDAY -> "周一"
        DayOfWeek.TUESDAY -> "周二"
        DayOfWeek.WEDNESDAY -> "周三"
        DayOfWeek.THURSDAY -> "周四"
        DayOfWeek.FRIDAY -> "周五"
        DayOfWeek.SATURDAY -> "周六"
        DayOfWeek.SUNDAY -> "周日"
    }
    return "${date.monthValue}月${date.dayOfMonth}日 · $weekday"
}
