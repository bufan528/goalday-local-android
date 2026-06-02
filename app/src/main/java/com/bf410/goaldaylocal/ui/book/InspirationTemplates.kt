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
            title = "1月｜新年启动",
            subtitle = "把年度愿望拆成第一个月",
            color = Color(0xFFF2C0A5),
            items = listOf("写下年度关键词", "整理一个固定工作区", "设定本月三件大事", "安排一次体检或运动", "完成第一次月复盘"),
        ),
        InspirationTemplate(
            title = "2月｜关系与陪伴",
            subtitle = "把重要的人放进日程",
            color = Color(0xFFF1A5B6),
            items = listOf("约一次认真聊天", "给家人打电话", "准备一份小礼物", "整理合照", "计划一次短途见面"),
        ),
        InspirationTemplate(
            title = "3月｜春日整理",
            subtitle = "空间、身体、计划一起变轻",
            color = Color(0xFFC9D6C1),
            items = listOf("清理衣柜一层", "换一套床品", "更新待办池", "每周散步两次", "做一次预算检查"),
        ),
        InspirationTemplate(
            title = "4月｜学习输入",
            subtitle = "给自己安排一段安静成长",
            color = Color(0xFF9EAADB),
            items = listOf("选一门课程", "每天阅读20分钟", "做一页学习笔记", "复盘一次输出", "整理收藏夹"),
        ),
        InspirationTemplate(
            title = "6月｜年中校准",
            subtitle = "把上半年变成可继续的计划",
            color = Color(0xFFF8D58A),
            items = listOf("回顾上半年完成", "删掉三个无效目标", "重排下半年重点", "整理财务记录", "预约一次休息日"),
        ),
        InspirationTemplate(
            title = "7月｜夏日清单",
            subtitle = "轻一点，也要有生活感",
            color = Color(0xFFFFAA5F),
            items = listOf("看一次晚霞", "做一杯冷饮", "整理旅行心愿", "拍一组夏日照片", "尝试早起散步"),
        ),
        InspirationTemplate(
            title = "8月｜能量恢复",
            subtitle = "把休息排进手账",
            color = Color(0xFFBBD1AD),
            items = listOf("安排半天无社交", "做一次深度清洁", "减少熬夜三天", "看完一本轻松的书", "写一页情绪记录"),
        ),
        InspirationTemplate(
            title = "9月｜重新开学",
            subtitle = "适合重启习惯的月份",
            color = Color(0xFF9EAADB),
            items = listOf("重建早晚流程", "设定学习主题", "整理文具和工具", "每周一次复盘", "完成一个小项目"),
        ),
        InspirationTemplate(
            title = "10月｜秋日体验",
            subtitle = "把季节变化变成生活记录",
            color = Color(0xFFD6A06B),
            items = listOf("去公园散步", "拍一张秋天照片", "整理换季衣物", "做一顿热汤", "写下本月想保留的事"),
        ),
        InspirationTemplate(
            title = "11月｜收尾准备",
            subtitle = "提前整理年末事项",
            color = Color(0xFFC6B4A0),
            items = listOf("检查年度目标", "整理文件资料", "列出年末采购", "准备礼物清单", "复盘一个失败经验"),
        ),
        InspirationTemplate(
            title = "12月｜年终复盘",
            subtitle = "给这一年一个完整收束",
            color = Color(0xFFA9B6C8),
            items = listOf("写年度总结", "整理年度照片", "列出感谢的人", "完成一次房间整理", "写给明年的自己"),
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
        InspirationTemplate(
            title = "50部电影计划",
            subtitle = "把观影变成可完成的清单",
            color = Color(0xFF334F46),
            items = listOf("建立想看片单", "每周看一部", "记录一句台词", "写下推荐指数", "整理年度十佳"),
        ),
        InspirationTemplate(
            title = "暑假计划",
            subtitle = "适合长假和空档期",
            color = Color(0xFFFFAA5F),
            items = listOf("安排一次旅行", "完成一个手作", "读完一本书", "整理照片", "学习一个新技能"),
        ),
        InspirationTemplate(
            title = "秋季计划",
            subtitle = "节奏变稳后的生活安排",
            color = Color(0xFFD6A06B),
            items = listOf("换季整理", "建立运动计划", "准备保暖物品", "做一次年度中后段复盘", "安排一次户外活动"),
        ),
        InspirationTemplate(
            title = "下半年目标",
            subtitle = "从现在开始重排优先级",
            color = Color(0xFF9EAADB),
            items = listOf("选出三个核心目标", "拆成月度行动", "标记已放弃事项", "确定每周检查点", "月底复盘一次"),
        ),
        InspirationTemplate(
            title = "2025愿望备忘",
            subtitle = "把愿望先收进一本书",
            color = Color(0xFFF2C0A5),
            items = listOf("写下想去的地方", "写下想学的技能", "写下想改善的习惯", "写下想见的人", "写下想完成的作品"),
        ),
    )
}
