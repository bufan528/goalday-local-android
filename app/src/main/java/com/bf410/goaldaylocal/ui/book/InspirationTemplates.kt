package com.bf410.goaldaylocal.ui.book

data class InspirationTemplate(
    val title: String,
    val subtitle: String,
    val items: List<String>,
)

internal object InspirationTemplates {
    val all = listOf(
        InspirationTemplate(
            title = "每日微小事",
            subtitle = "适合稳定推进的一天",
            items = listOf("学习一项新技能", "列出4月愿望清单", "读10分钟", "复盘今天"),
        ),
        InspirationTemplate(
            title = "年度愿望清单",
            subtitle = "把愿望拆成可执行步骤",
            items = listOf("整理愿望池", "选出本月3件", "写第一步行动", "设置提醒"),
        ),
        InspirationTemplate(
            title = "工作任务流",
            subtitle = "按优先级推进",
            items = listOf("列今天最重要3件", "拆分为30分钟动作", "完成后打卡", "结束前复盘"),
        ),
        InspirationTemplate(
            title = "小幸福日记",
            subtitle = "记录今日亮点",
            items = listOf("今天完成", "工作任务", "小幸福", "可改进点"),
        ),
        InspirationTemplate(
            title = "手账整理",
            subtitle = "页面内容更清晰",
            items = listOf("按日期排序", "补充标签", "添加总结", "保留一条鼓励语"),
        ),
    )
}
