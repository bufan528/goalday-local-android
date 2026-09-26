package com.bf410.goaldaylocal.ui.book

internal object HandbookLabels {
    const val plan = "计划"
    const val schedule = "日程"
    const val diary = "日记"
    const val topics = "主题"
    const val settings = "设置"
    const val monthPicker = "月份"
    const val bookOverview = "总览"
    const val bookShelf = "书架"
    const val export = "导出"
}

/** 手账分段（原 LocalHandbookWorkspace.kt 已删，枚举有单测 pin，迁到此处保留契约） */
internal enum class LocalHandbookSegment(val label: String) {
    PLAN(HandbookLabels.plan),
    SCHEDULE(HandbookLabels.schedule),
    DIARY(HandbookLabels.diary),
    TOPICS(HandbookLabels.topics),
}
