package com.bf410.goaldaylocal.data

import androidx.compose.ui.graphics.Color

object SampleLibrary {
    val books = listOf(
        TopicBook(
            id = "goalday-2026",
            title = "2026 GOALDAY",
            subtitle = "月度日程本",
            color = Color(0xFFBBD1AD),
            pages = (1..12).map { month ->
                SchedulePage(
                    title = "${month}月",
                    items = listOf(
                        "列出本月要做的所有事",
                        "标出每周最重要的目标",
                        "把当天计划拖入 todo/done",
                    ),
                )
            },
        ),
        TopicBook(
            id = "weekly-review",
            title = "周复盘",
            subtitle = "极简三步法",
            color = Color(0xFFF1A5B6),
            pages = listOf(
                TargetPage("回顾页", listOf("给完成和未完成的目标打分", "总结效果、效率和产出", "找到真正影响结果的原因")),
                SchedulePage("下周页", listOf("周日晚上 20:30 写好下周计划", "把要完成的计划拖进日程", "留出 1 个恢复能量的空白时段")),
                DiaryPage("记录页", "这周最值得保留的方法是什么？"),
            ),
        ),
        TopicBook(
            id = "life-list",
            title = "人生体验清单",
            subtitle = "慢慢完成的 100 件事",
            color = Color(0xFFFFAA5F),
            pages = listOf(
                TargetPage("体验页", listOf("看一次极光", "海边看一次日出", "做一本相册", "记录生活 vlog", "参加一次音乐节")),
                PlanPage("拆解页", listOf("选一个 30 天内可完成的体验", "列出预算、时间、同行人", "把准备动作放进本月计划")),
                DiaryPage("感受页", "如果今年只完成一件人生体验，它会是什么？"),
            ),
        ),
    )
}
