package com.bf410.goaldaylocal.data

import androidx.compose.ui.graphics.Color
import com.bf410.goaldaylocal.ui.replica.GoaldayDesign
import java.time.LocalDate

object SampleLibrary {
    private val currentYear: Int = LocalDate.now().year

    val books = listOf(
        TopicBook(
            id = "goalday-2026",
            title = "$currentYear GOALDAY",
            subtitle = "月度日程与日记手账",
            color = GoaldayDesign.TopicMoss,
            pages = (1..12).flatMap { month ->
                listOf(
                    SchedulePage(
                        title = "${month}月日程",
                        items = listOf(
                            "列出本月要做的所有事",
                            "标出每周最重要的目标",
                            "把当天计划拖入 todo/done",
                        ),
                    ),
                    DiaryPage(
                        title = "${month}月日记",
                        prompt = "把${month}月的日程、完成项和今天的感受写进这一页。",
                    ),
                )
            },
        ),
        TopicBook(
            id = "weekly-review",
            title = "周复盘",
            subtitle = "极简三步法",
            color = GoaldayDesign.TopicRose,
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
            color = GoaldayDesign.TopicAmber,
            pages = listOf(
                TargetPage("体验页", listOf("看一次极光", "海边看一次日出", "做一本相册", "记录生活 vlog", "参加一次音乐节")),
                PlanPage("拆解页", listOf("选一个 30 天内可完成的体验", "列出预算、时间、同行人", "把准备动作放进本月计划")),
                DiaryPage("感受页", "如果今年只完成一件人生体验，它会是什么？"),
            ),
        ),
    )
}
