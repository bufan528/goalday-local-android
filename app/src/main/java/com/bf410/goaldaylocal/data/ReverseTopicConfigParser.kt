package com.bf410.goaldaylocal.data

import org.json.JSONArray
import org.json.JSONObject

data class ReverseTopic(
    val id: String,
    val title: String,
    val colorHex: String,
    val coverKey: String,
    val targetKey: String,
    val linkToSchedule: Boolean,
    val targets: List<String>,
)

object ReverseTopicConfigParser {
    fun parse(raw: String): List<ReverseTopic> {
        // 手工改坏/BOM/顶层数组一律回空列表，不抛（调用方直接展示）
        val trimmed = raw.trim().removePrefix("\uFEFF")
        if (trimmed.isBlank()) return emptyList()
        val root = runCatching { JSONObject(trimmed) }.getOrNull() ?: return emptyList()
        return when {
            root.optJSONArray("cn") != null || root.optJSONArray("en") != null -> {
                parseArray(root.optJSONArray("cn") ?: JSONArray(), includeEnglish = root.optJSONArray("en"))
            }
            root.optJSONObject("roots") != null -> {
                val roots = root.getJSONObject("roots")
                val cnTopics = roots.optJSONObject("cn")?.optJSONArray("topics") ?: JSONArray()
                val enTopics = roots.optJSONObject("en")?.optJSONArray("topics")
                parseArray(cnTopics, includeEnglish = enTopics, cleanRoomShape = true)
            }
            else -> emptyList()
        }
    }

    private fun parseArray(
        array: JSONArray,
        includeEnglish: JSONArray? = null,
        cleanRoomShape: Boolean = false,
    ): List<ReverseTopic> {
        // 坏元素只跳过该元素：getJSONObject 会抛，整批失败
        val merged = buildList {
            repeat(array.length()) { array.optJSONObject(it)?.let(::add) }
            if (array.length() == 0 && includeEnglish != null) {
                repeat(includeEnglish.length()) { includeEnglish.optJSONObject(it)?.let(::add) }
            }
        }
        val usedIds = mutableSetOf<String>()
        return merged.mapIndexedNotNull { index, item ->
            val title = item.optString(if (cleanRoomShape) "title" else "name")
                .ifBlank { item.optString("title") }
                .ifBlank { return@mapIndexedNotNull null }
            // cn/en 合并时 id 可能重复：重复的加后缀，不丢条目
            val baseId = item.optString("id").ifBlank { "topic_$index" }
            var id = baseId
            var suffix = 2
            while (!usedIds.add(id)) {
                id = "${baseId}_$suffix"
                suffix++
            }
            val cover = item.optString("cover").removeSuffix(".png")
            val target = item.optString("target").removeSuffix(".txt")
            ReverseTopic(
                id = id,
                title = title,
                colorHex = normalizeColor(item.optString("color")),
                coverKey = cover,
                targetKey = target,
                linkToSchedule = if (item.has("linkToSchedule")) {
                    item.optBoolean("linkToSchedule")
                } else {
                    item.optBoolean("schedule")
                },
                targets = item.optJSONArray("targets").toStringList(),
            )
        }
    }

    private fun normalizeColor(raw: String): String {
        val hex = raw.trim().removePrefix("#")
        // 3 位展开，8 位取后 6 位，非法才回退默认
        val six = when {
            hex.matches(Regex("[0-9a-fA-F]{6}")) -> hex
            hex.matches(Regex("[0-9a-fA-F]{3}")) -> hex.flatMap { listOf(it, it) }.joinToString("")
            hex.matches(Regex("[0-9a-fA-F]{8}")) -> hex.substring(2)
            else -> return "F2C0A5"
        }
        return six.uppercase()
    }

    private fun JSONArray?.toStringList(): List<String> {
        if (this == null) return emptyList()
        return buildList {
            // 只要纯字符串：数字/对象不再 coerce 成 "123"/"{}" 脏条目
            repeat(length()) { index ->
                (opt(index) as? String)?.takeIf { it.isNotBlank() }?.let(::add)
            }
        }
    }
}
