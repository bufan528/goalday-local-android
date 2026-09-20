package com.bf410.goaldaylocal.data

import androidx.compose.ui.graphics.Color

object SampleLibrary {
    // 首屏种子对照原版开库两专题（日程指南 F1A5B6 / 清单指南 334F46，条目逐字照搬）。
    // 注意：book id 与 TargetPage 名保持不变，存量勾选/隐藏/排序按 (bookId, pageTitle) 存取，改名会孤儿化用户数据。
    val books = listOf(
        TopicBook(
            id = "goalday-2026",
            title = "日程指南",
            subtitle = "使用指南",
            color = Color(0xFFF1A5B6),
            pages = listOf(
                TargetPage(
                    "年度目标",
                    listOf(
                        "左侧周计划：点击任意空白处开始编辑",
                        "右侧清单：点击“·标题”切换清单列表",
                        "长按拖动：可以将事件从右侧清单拖放到左侧日程",
                    ),
                ),
                PlanPage("年度计划", listOf("每月第一天复盘上月完成情况", "每周日晚上规划下周日程", "每季度更新目标进度")),
            ) + (1..12).flatMap { month ->
                listOf(
                    SchedulePage(
                        title = "${month}月日程",
                        items = listOf(
                            "列出本月要做的所有事",
                            "标出每周最重要的目标",
                            "把当天计划拖入待办/已完成",
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
            title = "清单指南",
            subtitle = "使用指南",
            color = Color(0xFF334F46),
            pages = listOf(
                TargetPage(
                    "回顾页",
                    listOf(
                        "🟠清单列表指南🟠",
                        "创建新列表：点击“➕”新建空白清单，点击“💡”导入清单模板",
                        "更改信息/删除列表：长按列表弹出选择框",
                        "关联到日程：打开时，清单中“含时间的事件”会自动同步到日程中，修改清单或日程中的内容双向同步；关闭后，事件不再同步到日程中，在日程页面拖动时，清单中的事件会被删除",
                        "🟡清单内容指南🟡",
                        "创建新事件：点击空白处或键盘上的“换行”",
                        "删除事件：点击键盘上方的“🗑️”",
                        "置顶事件：点击键盘上方的“️⬆️”",
                        "完成或取消完成事件：点击事件前或键盘上方的“☑️”",
                        "事件的时间：点击完成时会自动记录时间，并且在日记中生成卡片，点击下方的时间戳，会直接跳转到日记页",
                        "更改时间：点击键盘上方的“时间”，灰色代表未来未完成的，有颜色的代表过去已完成的",
                        "更多设置：点击右上角“···”",
                    ),
                ),
                SchedulePage("下周页", listOf("周日晚上 20:30 写好下周计划", "把要完成的计划拖进日程", "留出 1 个恢复能量的空白时段")),
                DiaryPage("记录页", "这周最值得保留的方法是什么？"),
            ),
        ),
    )
}
