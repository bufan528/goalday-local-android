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
        val trimmed = raw.trim()
        if (trimmed.isBlank()) return emptyList()
        val root = JSONObject(trimmed)
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
        val merged = buildList {
            repeat(array.length()) { add(array.getJSONObject(it)) }
            if (array.length() == 0 && includeEnglish != null) {
                repeat(includeEnglish.length()) { add(includeEnglish.getJSONObject(it)) }
            }
        }
        return merged.mapIndexedNotNull { index, item ->
            val title = item.optString(if (cleanRoomShape) "title" else "name")
                .ifBlank { item.optString("title") }
                .ifBlank { return@mapIndexedNotNull null }
            val id = item.optString("id").ifBlank { "topic_$index" }
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
        return if (hex.matches(Regex("[0-9a-fA-F]{6}"))) hex.uppercase() else "F2C0A5"
    }

    private fun JSONArray?.toStringList(): List<String> {
        if (this == null) return emptyList()
        return buildList {
            repeat(length()) { index ->
                optString(index).takeIf { it.isNotBlank() }?.let(::add)
            }
        }
    }
}
