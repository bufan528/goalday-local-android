package com.bf410.goaldaylocal.ui.book

/**
 * 精确删除：删掉所有与目标 trim 相等的条目（含重名全部，前后空格不影响匹配）。
 * 调用方传未 trim 的值也安全。
 */
internal fun removeExactItem(items: List<String>, item: String): List<String> {
    val normalized = item.trim()
    if (normalized.isBlank()) return items
    return items.filterNot { it.trim() == normalized }
}

/**
 * 精确改名：trim 后比对；新名空白直接返回原样（不删）；改到已存在的名字上则合并去重。
 */
internal fun renameExactItemDistinct(items: List<String>, oldItem: String, newItem: String): List<String> {
    val normalizedOld = oldItem.trim()
    val normalizedNew = newItem.trim()
    if (normalizedOld.isBlank() || normalizedNew.isBlank()) return items
    return items.map { item -> if (item.trim() == normalizedOld) normalizedNew else item }.distinct()
}
