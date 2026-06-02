package com.bf410.goaldaylocal.ui.book

import androidx.compose.ui.graphics.Color

data class InspirationTemplate(
    val title: String,
    val subtitle: String,
    val color: Color,
    val linkToSchedule: Boolean = true,
    val items: List<String>,
)

internal object InspirationTemplates {
    val all = listOf(
        InspirationTemplate(
            title = "2026年愿望清单",
            subtitle = "年度愿望池，可直接排入月计划",
            color = Color(0xFFF2C0A5),
            items = listOf("制定年度计划", "每月复盘一次", "完成一本手账", "为一年后的自己写信", "清理手机相册", "尝试一种新运动"),
        ),
        InspirationTemplate(
            title = "每月幸福小事",
            subtitle = "把轻量体验放进日程",
            color = Color(0xFFA1B774),
            items = listOf("给自己买一束花", "做一顿喜欢的饭", "整理一个角落", "记录今天的小幸福", "散步15分钟", "看一次日落"),
        ),
        InspirationTemplate(
            title = "人生体验清单",
            subtitle = "慢慢完成的 100 件事",
            color = Color(0xFFFFAA5F),
            items = listOf("去周边城市旅行", "看一次现场演出", "做一本相册", "体验一次手作", "和朋友野餐", "写下理想人生清单"),
        ),
        InspirationTemplate(
            title = "主题周计划",
            subtitle = "一周只围绕一个主题推进",
            color = Color(0xFFC9D6C1),
            items = listOf("写下本周主题", "列出任务池", "选出最重要三件", "每天拖入日期", "周末做复盘"),
        ),
        InspirationTemplate(
            title = "解压治愈小手工",
            subtitle = "低压力恢复能量",
            color = Color(0xFFF1A5B6),
            items = listOf("做一枚书签", "整理手账贴纸", "尝试折纸", "做一张拼贴卡片", "给朋友准备小礼物"),
        ),
        InspirationTemplate(
            title = "五年日记",
            subtitle = "一日一问，适合长期记录",
            color = Color(0xFFF1A5B6),
            linkToSchedule = false,
            items = listOf("今天最开心的事", "今天学到的新东西", "想感谢的人", "明天最期待什么", "今天的情绪关键词"),
        ),
        InspirationTemplate(
            title = "缓解焦虑",
            subtitle = "把状态拉回来的小行动",
            color = Color(0xFF9EAADB),
            items = listOf("深呼吸3分钟", "出门散步15分钟", "写下担心清单", "洗热水澡", "减少短视频30分钟"),
        ),
        InspirationTemplate(
            title = "周复盘三步",
            subtitle = "回顾、总结、安排下周",
            color = Color(0xFFF1A5B6),
            linkToSchedule = false,
            items = listOf("回顾本周完成", "找出一个卡点", "保留一个有效方法", "写下下周重点"),
        ),
        InspirationTemplate(
            title = "日复盘问题",
            subtitle = "用问题收束一天",
            color = Color(0xFFF1A5B6),
            linkToSchedule = false,
            items = listOf("今天完成了什么", "哪里消耗最大", "有什么小进步", "明天优先做什么"),
        ),
        InspirationTemplate(
            title = "每周断舍离",
            subtitle = "每周清一个小区域",
            color = Color(0xFFF8D58A),
            linkToSchedule = false,
            items = listOf("整理衣柜一格", "清理过期物品", "删除无用照片", "处理一个闲置", "清空桌面"),
        ),
        InspirationTemplate(
            title = "独处可做的事",
            subtitle = "一个人也能过得丰富",
            color = Color(0xFFBBD1AD),
            items = listOf("看一部电影", "读书30分钟", "做一次冥想", "写一篇日记", "学一道新菜"),
        ),
        InspirationTemplate(
            title = "高质量书单",
            subtitle = "阅读目标模板",
            color = Color(0xFF334F46),
            items = listOf("选定本月书籍", "拆成四次阅读", "记录一句摘抄", "写一段读后感"),
        ),
        InspirationTemplate(
            title = "出行行李清单",
            subtitle = "旅行前检查项",
            color = Color(0xFFF8D58A),
            items = listOf("证件与票据", "衣物与洗护", "充电器和相机", "常用药品", "出门前检查门窗"),
        ),
        InspirationTemplate(
            title = "电影清单",
            subtitle = "把想看的片子变成计划",
            color = Color(0xFF334F46),
            items = listOf("选一部高分电影", "约定观影时间", "记录一句台词", "写下观后感"),
        ),
        InspirationTemplate(
            title = "情侣浪漫小事",
            subtitle = "关系经营的小计划",
            color = Color(0xFFF1A5B6),
            items = listOf("一起散步", "准备一次小惊喜", "认真聊一次近况", "拍一张合照", "计划一次短途旅行"),
        ),
    )
}
