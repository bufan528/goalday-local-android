package com.bf410.goaldaylocal.ui.book

import android.content.Context
import androidx.compose.ui.graphics.Color

data class InspirationTemplate(
    val id: String,
    val title: String,
    val subtitle: String,
    val color: Color,
    val coverKey: String,
    val targetKey: String,
    val category: String,
    val catalogPath: String,
    val coverAssetPath: String,
    val targetAssetPath: String,
    val linkToSchedule: Boolean = true,
    val items: List<String>,
) {
    val targetCount: Int get() = items.size
}

internal object InspirationTemplates {
    val all: List<InspirationTemplate> =
        listOf(
            topic(
                id = "wish_2026",
                title = "2026年愿望清单",
                subtitle = "年度愿望池，可直接排入月计划",
                color = Color(0xFFF2C0A5),
                coverKey = "2026",
                category = "年度",
                items = annualWishItems(),
            ),
            topic(
                id = "year_next_half",
                title = "下半年目标",
                subtitle = "从现在开始重排优先级",
                color = Color(0xFF9EAADB),
                coverKey = "2025goalfornexthalfyear",
                category = "年度",
                items = listOf(
                    "选出三个下半年核心目标",
                    "给每个目标写一个可衡量结果",
                    "删掉三个不再重要的愿望",
                    "把年度目标拆成月度行动",
                    "安排一次财务复盘",
                    "整理学习/工作工具",
                    "预约一次体检或运动评估",
                    "确定每周固定复盘时间",
                    "为最重要目标设置截止日",
                    "列出需要求助的人或资源",
                    "保存一页下半年愿望备忘",
                    "月底复盘一次完成率",
                ),
            ),
            topic(
                id = "life_list",
                title = "人生体验清单",
                subtitle = "慢慢完成的体验型目标",
                color = Color(0xFFFFAA5F),
                coverKey = "life",
                category = "体验",
                items = experienceItems(),
            ),
            topic(
                id = "topic_week",
                title = "主题周计划",
                subtitle = "一周只围绕一个主题推进",
                color = Color(0xFFC9D6C1),
                coverKey = "topicweek",
                category = "周计划",
                items = themedWeekItems(),
            ),
            topic(
                id = "five_year_diary",
                title = "五年日记",
                subtitle = "一日一问，适合长期记录",
                color = Color(0xFFF1A5B6),
                coverKey = "five_year_diary",
                category = "日记",
                linkToSchedule = false,
                items = fiveYearDiaryPrompts(),
            ),
            topic(
                id = "anxiety_relief",
                title = "缓解焦虑",
                subtitle = "把状态拉回来的小行动",
                color = Color(0xFF9EAADB),
                coverKey = "anxiety_relief",
                category = "疗愈",
                items = listOf(
                    "做一次 3 分钟呼吸练习",
                    "把担心清单写出来并分级",
                    "出门散步 15 分钟",
                    "洗一个热水澡",
                    "把明天必须做的事减到三件",
                    "整理桌面上的一个小区域",
                    "暂停一个让你焦虑的通知源",
                    "写下今天已经完成的一件事",
                    "给身体做一次伸展",
                    "睡前半小时不刷短视频",
                    "喝一杯热饮并记录当下感受",
                    "找一个可信任的人说出压力",
                ),
            ),
            topic(
                id = "weekly_review",
                title = "周复盘三步",
                subtitle = "回顾、总结、安排下周",
                color = Color(0xFFF1A5B6),
                coverKey = "weekly_review",
                category = "复盘",
                linkToSchedule = false,
                items = listOf(
                    "回顾本周完成的目标",
                    "标记未完成但仍重要的事项",
                    "找出一个真正卡住的原因",
                    "写下一个有效方法",
                    "删掉一个低价值待办",
                    "记录一个本周高能量时段",
                    "记录一个本周低能量时段",
                    "选出下周最重要三件事",
                    "给下周安排一次休息",
                    "把复盘结论写进日记",
                ),
            ),
            topic(
                id = "daily_review",
                title = "日复盘问题",
                subtitle = "用问题收束一天",
                color = Color(0xFFF1A5B6),
                coverKey = "daily_review",
                category = "复盘",
                linkToSchedule = false,
                items = listOf(
                    "今天完成了什么",
                    "今天最消耗我的事情是什么",
                    "今天最值得保留的方法是什么",
                    "今天有没有一个小进步",
                    "今天最想感谢谁",
                    "明天最优先的一件事是什么",
                    "哪些事情可以不再纠结",
                    "今天的情绪关键词是什么",
                    "我为身体做了什么",
                    "睡前要清空的一个念头是什么",
                ),
            ),
            topic(
                id = "weekly_cleanup",
                title = "每周断舍离",
                subtitle = "每周清一个小区域",
                color = Color(0xFFF8D58A),
                coverKey = "weekly_cleanup",
                category = "整理",
                linkToSchedule = false,
                items = cleanupItems(),
            ),
            topic(
                id = "solo_activities",
                title = "独处可做的事",
                subtitle = "一个人也能过得丰富",
                color = Color(0xFFBBD1AD),
                coverKey = "solo_activities",
                category = "体验",
                items = soloItems(),
            ),
            topic(
                id = "quality_books",
                title = "高质量书单",
                subtitle = "阅读目标模板",
                color = Color(0xFF334F46),
                coverKey = "quality_books",
                category = "阅读",
                items = listOf(
                    "选定本月主读书",
                    "把全书拆成四次阅读",
                    "为每次阅读写一句摘抄",
                    "记录一个新观点",
                    "把一个观点用到生活里",
                    "写一段读后感",
                    "整理书中提到的延伸书目",
                    "和朋友聊一次读书收获",
                    "月底选出本月最佳段落",
                    "保存一页年度书单",
                ),
            ),
            topic(
                id = "travel_checklist",
                title = "出行行李清单",
                subtitle = "旅行前检查项",
                color = Color(0xFFF8D58A),
                coverKey = "travel_checklist",
                category = "旅行",
                items = travelItems(),
            ),
            topic(
                id = "douban_movies",
                title = "电影清单",
                subtitle = "把想看的片子变成计划",
                color = Color(0xFF334F46),
                coverKey = "douban_movies",
                category = "观影",
                items = listOf(
                    "建立想看片单",
                    "每周看一部电影",
                    "记录一句台词",
                    "写下观后感",
                    "整理一个导演专题",
                    "和朋友约一次线下观影",
                    "看一部旧电影",
                    "看一部纪录片",
                    "给看过的电影打分",
                    "整理年度十佳候选",
                ),
            ),
            topic(
                id = "fifty_films",
                title = "50部电影计划",
                subtitle = "用片单填满观影手账",
                color = Color(0xFF334F46),
                coverKey = "50films",
                category = "观影",
                items = listOf(
                    "挑选 10 部一直想看的电影",
                    "每周安排一次观影时间",
                    "看完后记录一句台词",
                    "写 3 句话短评",
                    "整理一个导演专题",
                    "做一页年度观影片单",
                ),
            ),
            topic(
                id = "couple_activities",
                title = "情侣浪漫小事",
                subtitle = "关系经营的小计划",
                color = Color(0xFFF1A5B6),
                coverKey = "couple_activities",
                category = "关系",
                items = listOf(
                    "一起散步 30 分钟",
                    "准备一次小惊喜",
                    "认真聊一次近况",
                    "拍一张合照",
                    "计划一次短途旅行",
                    "一起做一顿饭",
                    "一起整理共同相册",
                    "写一张感谢卡片",
                    "约定一次无手机晚餐",
                    "一起完成一个小挑战",
                ),
            ),
            topic(
                id = "healing_crafts",
                title = "解压治愈小手工",
                subtitle = "低压力恢复能量",
                color = Color(0xFFF1A5B6),
                coverKey = "healing_crafts",
                category = "手作",
                items = listOf(
                    "做一枚书签",
                    "整理手账贴纸",
                    "尝试折纸",
                    "做一张拼贴卡片",
                    "给朋友准备小礼物",
                    "画一张小卡片",
                    "做一次旧物改造",
                    "整理手作工具盒",
                    "拍一组作品照片",
                    "写下手作过程感受",
                ),
            ),
            topic(
                id = "autumn",
                title = "秋日体验清单",
                subtitle = "把秋天写进手账",
                color = Color(0xFFD6A06B),
                coverKey = "autumn",
                category = "季节",
                items = listOf(
                    "整理秋季衣物",
                    "安排一次秋日散步",
                    "拍一组秋天照片",
                    "做一次房间换季清洁",
                    "计划一次近郊短途",
                    "写一页秋日愿望",
                ),
            ),
            topic(
                id = "january_happiness",
                title = "1月幸福小事",
                subtitle = "新年开始记录开心瞬间",
                color = Color(0xFFF2C0A5),
                coverKey = "january_happiness",
                category = "月份",
                items = listOf(
                    "写下 1 月最期待的三件事",
                    "每天记录一件小幸福",
                    "整理新年愿望清单",
                    "写一封给今年自己的信",
                    "制定一个轻量习惯",
                    "月底选出最幸福的一天",
                ),
            ),
            topic(
                id = "stage_review",
                title = "阶段复盘",
                subtitle = "把最近一段时间重新整理",
                color = Color(0xFFF1A5B6),
                coverKey = "review",
                category = "复盘",
                linkToSchedule = false,
                items = listOf(
                    "回顾最近一个阶段的完成事项",
                    "写下三个有效做法",
                    "标记一个拖延最久的问题",
                    "给重要目标更新下一步",
                    "把复盘结论写进日记",
                    "保存一页阶段总结",
                ),
            ),
            topic(
                id = "summer_vacation",
                title = "暑假计划",
                subtitle = "把假期变成可执行清单",
                color = Color(0xFFFFAA5F),
                coverKey = "summer_vacation",
                category = "季节",
                items = listOf(
                    "列出暑假最想做的 10 件事",
                    "安排一次短途旅行",
                    "完成一本书",
                    "学习一个轻量技能",
                    "拍一组夏日照片",
                    "假期结束前写一页总结",
                ),
            ),
            topic(
                id = "wish_list_2025",
                title = "2025愿望清单",
                subtitle = "补齐还没完成的年度愿望",
                color = Color(0xFFF2C0A5),
                coverKey = "wish_list_2025",
                category = "年度",
                items = listOf(
                    "整理 2025 年还想完成的愿望",
                    "选出三个最重要目标",
                    "给每个愿望写下一步",
                    "把一个愿望排进本周",
                    "记录一个已经实现的愿望",
                    "写下给年底自己的提醒",
                ),
            ),
        ) + monthlyTopics()

    private fun topic(
        id: String,
        title: String,
        subtitle: String,
        color: Color,
        coverKey: String,
        category: String,
        linkToSchedule: Boolean = true,
        items: List<String>,
    ): InspirationTemplate =
        InspirationTemplate(
            id = id,
            title = title,
            subtitle = subtitle,
            color = color,
            coverKey = coverKey,
            targetKey = "${coverKey}_target",
            category = category,
            catalogPath = "assets/topic_center_config.json",
            coverAssetPath = "compose/cover/$coverKey",
            targetAssetPath = "assets/topictarget/${coverKey}_target.txt",
            linkToSchedule = linkToSchedule,
            items = items.distinct(),
        )

    private fun monthlyTopics(): List<InspirationTemplate> {
        val monthData = listOf(
            Triple("jan", "1月｜新年启动", Color(0xFFF2C0A5)),
            Triple("feb", "2月｜关系与陪伴", Color(0xFFF1A5B6)),
            Triple("march", "3月｜春日整理", Color(0xFFC9D6C1)),
            Triple("april", "4月｜学习输入", Color(0xFF9EAADB)),
            Triple("may", "5月｜每天一件幸福小事", Color(0xFFA1B774)),
            Triple("june", "6月｜年中校准", Color(0xFFF8D58A)),
            Triple("july", "7月｜夏日清单", Color(0xFFFFAA5F)),
            Triple("august", "8月｜能量恢复", Color(0xFFBBD1AD)),
            Triple("september", "9月｜重新开学", Color(0xFF9EAADB)),
            Triple("october", "10月｜秋日体验", Color(0xFFD6A06B)),
            Triple("november", "11月｜收尾准备", Color(0xFFC6B4A0)),
            Triple("december", "12月｜年终复盘", Color(0xFFA9B6C8)),
        )
        return monthData.mapIndexed { index, (key, title, color) ->
            topic(
                id = "month_${index + 1}",
                title = title,
                subtitle = if (index == 4) "把轻量体验放进日程" else "月度手账主题与行动清单",
                color = color,
                coverKey = key,
                category = "月份",
                items = monthlyBaseItems(index + 1),
            )
        }
    }

    private fun annualWishItems(): List<String> =
        listOf(
            "制定年度计划",
            "坚持早睡早起 7 天",
            "每月复盘一次",
            "每月存一笔钱",
            "制定年度预算计划",
            "减少外卖次数",
            "定期问候长辈",
            "看完十本书",
            "学习一项新技能",
            "完成一本手账",
            "坚持运动 30 天",
            "学习缓解压力的方法",
            "看 10 部高分电影",
            "学习一个新语言",
            "每天记录一件幸福小事",
            "学习基础剪辑",
            "考取一个行业相关证书",
            "挑战一个月不网购",
            "改变房间布局",
            "尝试一种新运动",
            "户外野餐一次",
            "为一年后的自己写信",
            "清理手机相册",
            "为自己买一束花",
        )

    private fun experienceItems(): List<String> =
        listOf(
            "去周边城市旅行",
            "海边看一次日出",
            "户外露营",
            "做一本旅行手账",
            "现场看一次演出",
            "体验一次非遗手作",
            "做个人自媒体",
            "看一次艺术展",
            "换个城市短住",
            "为自己买一束花",
            "做一本相册",
            "学会开车或骑行",
            "种一棵植物",
            "不玩手机一天",
            "记录生活 vlog",
            "连续早起 7 天",
            "坚持一件小事 30 天",
            "拒绝一次不想做的事",
            "为自己争取一次机会",
            "尝试一种从没吃过的食物",
            "坐火车去一个远方城市",
            "亲手做一次蛋糕",
            "看一次音乐节",
            "发表一篇长文",
        )

    private fun themedWeekItems(): List<String> =
        listOf(
            "年度规划周｜写清楚今年最重要的 3 件事",
            "空间清理周｜整理房间的一个角落",
            "时间盘点周｜记录一周时间流向",
            "公园周｜每天去附近公园散步",
            "早睡周｜每天 23:00 前准备入睡",
            "轻运动周｜每天运动 10 分钟",
            "喝水养成周｜记录每天喝水情况",
            "万步周｜每天步数达到 10000",
            "好好吃饭周｜吃饭时不玩手机",
            "感恩周｜每天记录 3 件感谢的小事",
            "情绪记录周｜每天给情绪取一个名字",
            "相册清理周｜整理手机照片",
            "边界练习周｜拒绝 1 件不想做的事",
            "深度倾听周｜沟通中练习不打断",
            "烹饪学习周｜学做一道感兴趣的菜",
            "语言学习周｜每天练习 30 分钟",
            "纪录片周｜看完一部纪录片",
            "断舍离周｜每天整理一个小区域",
            "预算周｜完成一次个人预算",
            "年度复盘周｜复盘今年关键事件",
        )

    private fun fiveYearDiaryPrompts(): List<String> =
        listOf(
            "新的一年，我最希望改变自己的哪个方面？",
            "今年最重要的目标是什么？",
            "什么事情让我感到有动力？",
            "今年的关键词是什么？",
            "最近的生活是否规律？",
            "我有哪些未完成的心愿？",
            "在时间管理上，我的方法是什么？",
            "今年最想学会的技能是什么？",
            "做什么事情让我感到焦虑？",
            "哪些小事能让我真正放松？",
            "我如何定义幸福？",
            "最近自己的状态如何？",
            "我对孤独的感受是什么？",
            "哪些事情让我感到快乐？",
            "我最喜欢自己的哪一点？",
            "我在生活中最重视什么？",
            "想对十年后的自己说什么？",
            "最近可以改变哪个小习惯？",
            "我是否经常拖延重要的事情？",
            "五年后的理想生活是什么样？",
            "最近一次生气的原因是什么？",
            "哪些事情让我感到满足？",
            "我是否尊重自己的时间和精力？",
            "最近让我幸福的瞬间是什么？",
        )

    private fun cleanupItems(): List<String> =
        listOf(
            "整理衣柜一格",
            "处理变形或不舒服的鞋",
            "清理过期调料",
            "整理冰箱冷冻区",
            "清理不会回购的小样",
            "处理干掉的化妆品",
            "整理数据线和充电器",
            "清理不常用 App",
            "删除无意义截图",
            "整理聊天记录收藏",
            "清理网盘文件夹",
            "更新音乐歌单",
            "处理一个闲置物品",
            "整理书桌抽屉",
            "清空一个收纳盒",
            "给常用物品固定位置",
        )

    private fun soloItems(): List<String> =
        listOf(
            "看一部一直想看的电影",
            "读书 30 分钟",
            "做一次冥想",
            "写一篇日记",
            "学一道新菜",
            "整理照片并做相册页",
            "去附近咖啡店坐一小时",
            "完成一次 citywalk",
            "给自己写一封信",
            "做一次房间香氛整理",
            "学习一个小技能",
            "做一张拼贴手账",
            "听一期播客并记录观点",
            "给未来一周做计划",
        )

    private fun travelItems(): List<String> =
        listOf(
            "准备证件与票据",
            "确认酒店和交通",
            "整理衣物搭配",
            "准备洗护用品",
            "检查充电器和相机",
            "带好常用药品",
            "列出当地想吃的店",
            "安排一个空白休息时段",
            "准备随身小包",
            "出门前检查门窗水电",
            "保存紧急联系人",
            "整理旅行后照片",
        )

    private fun monthlyBaseItems(month: Int): List<String> {
        val seasonal = when (month) {
            1 -> listOf("写下年度关键词", "整理固定工作区", "设定本月三件大事", "完成第一次月复盘")
            2 -> listOf("给家人打电话", "约一次认真聊天", "准备一份小礼物", "整理合照")
            3 -> listOf("清理衣柜一层", "换一套床品", "更新待办池", "做一次预算检查")
            4 -> listOf("选一门课程", "每天阅读 20 分钟", "做一页学习笔记", "整理收藏夹")
            5 -> listOf("给自己买一束花", "做一顿喜欢的饭", "记录今天的小幸福", "看一次日落")
            6 -> listOf("回顾上半年完成", "删掉三个无效目标", "重排下半年重点", "预约一次休息日")
            7 -> listOf("看一次晚霞", "做一杯冷饮", "整理旅行心愿", "拍一组夏日照片")
            8 -> listOf("安排半天无社交", "做一次深度清洁", "减少熬夜三天", "写一页情绪记录")
            9 -> listOf("重建早晚流程", "设定学习主题", "整理文具和工具", "完成一个小项目")
            10 -> listOf("去公园散步", "拍一张秋天照片", "整理换季衣物", "做一顿热汤")
            11 -> listOf("检查年度目标", "整理文件资料", "列出年末采购", "复盘一个失败经验")
            else -> listOf("写年度总结", "整理年度照片", "列出感谢的人", "写给明年的自己")
        }
        return seasonal + listOf(
            "列出本月任务池",
            "每周选出最重要三件事",
            "安排一次身体恢复时间",
            "月底做一次完成率复盘",
            "把一个目标保存成日记记录",
            "整理本月照片或截图",
        )
    }
}

internal fun loadTargetAssetItems(context: Context, path: String): List<String> =
    runCatching {
        val assetName = path.removePrefix("assets/")
        context.assets.open(assetName).bufferedReader().useLines { lines ->
            lines.map(String::trim)
                .filter { it.isNotBlank() && !it.startsWith("#") }
                .distinct()
                .toList()
        }
    }.getOrDefault(emptyList())
