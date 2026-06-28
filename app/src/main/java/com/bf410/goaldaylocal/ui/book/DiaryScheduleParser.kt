package com.bf410.goaldaylocal.ui.book

import java.time.LocalDate

internal data class DiaryScheduleCandidate(
    val title: String,
    val date: LocalDate,
    val completed: Boolean,
    val sourceNote: String,
)

internal fun parseDiaryScheduleCandidates(
    raw: String,
    sourceNote: String,
    today: LocalDate = LocalDate.now(),
): List<DiaryScheduleCandidate> {
    if (raw.isBlank()) return emptyList()
    val date = diarySection(raw, "日期").lineSequence()
        .map(String::trim)
        .firstOrNull(String::isNotBlank)
        ?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        ?: today
    val candidates = mutableListOf<DiaryScheduleCandidate>()
    fun addLines(sectionName: String, completed: Boolean) {
        diarySection(raw, sectionName)
            .lines()
            .map(::normalizeDiaryScheduleTitle)
            .filter(String::isNotBlank)
            .forEach { title ->
                candidates += DiaryScheduleCandidate(title, date, completed, sourceNote)
            }
    }
    addLines("工作任务", completed = false)
    addLines("日程", completed = false)
    addLines("计划", completed = false)
    addLines("待办", completed = false)
    addLines("今日完成", completed = true)
    diarySection(raw, "日记块")
        .lines()
        .mapNotNull(::parseDiaryBlockForSchedule)
        .forEach { (title, completed) ->
            candidates += DiaryScheduleCandidate(title, date, completed, sourceNote)
        }
    return candidates
}

internal fun escapeDiaryScheduleBlockText(text: String): String =
    text.replace("\\", "\\\\").replace("\n", "\\n").replace("|", "\\p")

private fun diarySection(raw: String, sectionName: String): String {
    val marker = "# $sectionName"
    val start = raw.indexOf(marker)
    if (start < 0) return ""
    val bodyStart = raw.indexOf('\n', start).takeIf { it >= 0 }?.plus(1) ?: return ""
    val bodyEnd = raw.indexOf("\n#", bodyStart).takeIf { it >= 0 } ?: raw.length
    return raw.substring(bodyStart, bodyEnd).trim()
}

private fun parseDiaryBlockForSchedule(line: String): Pair<String, Boolean>? {
    val parts = splitEscapedDiaryBlock(line)
    val type = parts.getOrNull(0).orEmpty()
    if (type !in setOf("target", "target_child", "topic_target")) return null
    val text = unescapeDiaryScheduleBlockText(parts.getOrNull(2).orEmpty().ifBlank { parts.getOrNull(1).orEmpty() })
    val completed = text.trimStart().startsWith("✓")
    val title = normalizeDiaryScheduleTitle(text)
    return title.takeIf(String::isNotBlank)?.let { it to completed }
}

private fun splitEscapedDiaryBlock(line: String): List<String> {
    val parts = mutableListOf<String>()
    val current = StringBuilder()
    var escaping = false
    line.forEach { char ->
        when {
            escaping -> {
                current.append('\\').append(char)
                escaping = false
            }
            char == '\\' -> escaping = true
            char == '|' -> {
                parts += current.toString()
                current.clear()
            }
            else -> current.append(char)
        }
    }
    if (escaping) current.append('\\')
    parts += current.toString()
    return parts
}

private fun unescapeDiaryScheduleBlockText(text: String): String {
    val builder = StringBuilder()
    var escaping = false
    text.forEach { char ->
        if (escaping) {
            builder.append(
                when (char) {
                    'n' -> '\n'
                    'p' -> '|'
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

private fun normalizeDiaryScheduleTitle(raw: String): String =
    raw.lines()
        .firstOrNull()
        .orEmpty()
        .trim()
        .removePrefix("-")
        .removePrefix("•")
        .removePrefix("✓")
        .removePrefix("○")
        .removePrefix("[ ]")
        .removePrefix("[x]")
        .removePrefix("[X]")
        .trim()
