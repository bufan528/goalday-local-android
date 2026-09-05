package com.bf410.goaldaylocal.ui.replica

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.bf410.goaldaylocal.R

// 暗色模式开关：由 GoaldayApp 顶层根据 MMKV "dark_mode" (AUTO/LIGHT/DARK) + 系统主题决定后提供
val LocalGoaldayDarkMode = compositionLocalOf { false }

object GoaldayDesign {
    // 对照原版 colors.xml：主背景 #FDFAF6（暖奶白），Tab 背景 #E5DAD4（浅褐米色）
    val AppBg = Color(0xFFFDFAF6)
    val Surface = Color(0xFFFFFEFB)
    val SurfaceSoft = Color(0xFFF7F3EE)
    // 原版 color_252525=#252525（主文字），color_727272=#727272（次要），color_9E9E9E=#9E9E9E（弱化）
    val InkPrimary = Color(0xFF252525)
    val InkSecondary = Color(0xFF727272)
    val InkMuted = Color(0xFF9E9E9E)
    // 原版 Tab 背景 color_E5DAD4
    val TabBarBg = Color(0xFFE5DAD4)
    // 原版蓝色 color_3875F6（链接/强调按钮）
    val ActionBlue = Color(0xFF3875F6)
    // 主品牌色：对齐原版 color_F79941 橙黄（保留 Pink/PinkSoft 名称避免 165 处重命名）
    val Pink = Color(0xFFF79941)
    val PinkSoft = Color(0xFFFDE3D6)
    // PinkTint：主色薄叠层（alpha 0.18），用于 chip 背景、高亮底色等场景
    val PinkTint: Color
        get() = Pink.copy(alpha = 0.18f)
    val GreenSoft = Color(0xFFEAF4E4)
    val Positive = Color(0xFF769B69)
    // 原版删除色 #ED8888（item_plan_item fl_delete）
    val Danger = Color(0xFFED8888)
    // DangerInk：危险操作文字色（深红），用于删除/重置等警示标签
    val DangerInk = Color(0xFF7A2F2F)
    // DangerTint：危险操作浅底色（alpha 0.10），用于警示 chip 背景
    val DangerTint: Color
        get() = Color(0xFFD17878).copy(alpha = 0.10f)
    val PrimaryAction = Color(0xFF221F1B)
    val Paper = Color(0xFFFFFCF6)
    val PaperWarm = Color(0xFFFFF6E8)
    val PaperAged = Color(0xFFF3E1CD)
    val PaperLine = Color(0x33B99A7D)
    // 对齐原 APK 手账书壳：暖灰米色硬壳，带一点褐/黄，让书壳更像真实布面精装本
    val BookBoard = Color(0xFFD9CFC2)
    val BookBoardLight = Color(0xFFE9E2D8)
    val BookBoardDark = Color(0xFF7D7369)
    // 书脊：原 APK 为暖米色圆柱，保留适度红/黄分量模拟光照
    val BookSpine = Color(0xFFBCB4AB)
    val BookSpineLight = Color(0xFFDED7CC)
    // DeskTop 与 AppBg 对齐原版 #FDFAF6，桌面渐变从主背景色平滑过渡到深木色
    val DeskTop = Color(0xFFFDFAF6)
    val DeskMid = Color(0xFFF5E4D2)
    val DeskBottom = Color(0xFFE4C7AE)
    val ShelfWood = Color(0xFFD3A681)
    val ShelfWoodDark = Color(0xFF8F6042)
    // 对照原版 GoalDayColors：5 个主题色 #334F46 #9EAADB #BBD1AD #F1A5B6 #F8D58A
    // 路由色对齐原版主题色，让手账各入口与原版视觉一致
    // Route变量定义移至 Topic变量之后（见下方），避免前向引用

    // 语义强调色（仿 Things 3：颜色仅承担语义，95% 中性 + 5% 语义）
    // today=今日暖琥珀 / deadline=截止柔和红 / evening=晚间靛 / someday=某天中性灰 / habit=习惯柔和绿
    val Today = Color(0xFFE8A33D)
    val Deadline = Color(0xFFC75B5B)
    val Evening = Color(0xFF6B7AA8)
    val Someday = Color(0xFF8A8175)
    val Habit = Color(0xFF6B8E5A)

    val RadiusS = 8.dp
    val RadiusM = 12.dp
    val RadiusL = 16.dp
    val RadiusXL = 20.dp
    val Radius2XL = 24.dp
    val Radius3XL = 28.dp
    val RadiusPill = 99.dp

    // Spacing system (呼吸感)
    val Space1 = 4.dp
    val Space2 = 8.dp
    val Space3 = 12.dp
    val Space4 = 16.dp
    val Space5 = 20.dp
    val Space6 = 24.dp
    val Space8 = 32.dp
    val Space10 = 40.dp
    val Space12 = 48.dp

    // Soft shadows (柔和阴影)
    val ShadowSoft = 8.dp
    val ShadowMedium = 12.dp
    val ShadowLarge = 16.dp

    // Hairline borders (超薄边框)
    val Hairline = 0.7.dp
    val HairlineAlpha = 0.10f
    val BorderColor = Color(0xFFA88966)

    // 灵感页/封面叠层色：统一 InspirationScreen 中散落的硬编码半透明色
    val PaperGlass = Color(0x80FFFDF8)
    val PaperGlassMedium = Color(0x88FFFDF8)
    val PaperGlassStrong = Color(0xEFFFFDF8)
    val WhiteOverlayBorder = Color(0x44FFFFFF)
    val WhiteOverlayHigh = Color(0xDEFFFFFF)
    val WhiteOverlayMedium = Color(0x66FFFFFF)
    val WhiteOverlayLow = Color(0x30FFFFFF)
    val CoverWhiteOverlay72 = Color(0xB8FFFFFF)
    val CoverWhiteOverlay60 = Color(0x99FFFFFF)
    val CoverWhiteOverlay55 = Color(0x8CFFFFFF)
    val CoverWhiteOverlay50 = Color(0x80FFFFFF)
    val CoverWhiteOverlay38 = Color(0x61FFFFFF)
    val DarkOverlay = Color(0xAA1F1712)
    val DarkOverlaySoft = Color(0x991F1712)
    val BlackOverlaySoft = Color(0x26000000)
    // BookShell 书脊/纸张阴影叠层
    val BlackOverlayLight = Color(0x18000000)
    // 对齐原版 #36000000（22% 透明黑），用于卡片投影/翻页阴影
    val BlackOverlayMedium = Color(0x36000000)
    val BlackOverlayStrong = Color(0x42000000)
    val BlackOverlayLine = Color(0x44000000)
    val BlackOverlayHairline = Color(0x14000000)
    val WhiteHighlight = Color.White.copy(alpha = 0.27f)

    // 通用柔和色与编辑器/Widget 强调色
    val DividerMuted = Color(0xFFD2CBC1)
    val Clay = Color(0xFFE0D7CD)
    val Sand = Color(0xFFD8CFC5)
    val AccentMauve = Color(0xFFB07A8F)
    val AccentSage = Color(0xFF6F8E68)
    val AccentTerracotta = Color(0xFF8F684F)
    val AccentPeriwinkle = Color(0xFF9EAADB)
    val AccentMintSurface = Color(0xFFF2F7EE)

    // Hero 渐变（Home/Calendar 头部卡片）
    val HomeHeroStart = Color(0xFFFFF4EA)
    val HomeHeroMid = Color(0xFFFFE4EC)
    val HomeHeroEnd = Color(0xFFF2CFB3)
    val HomeHeroOverlayStart = Color(0xEFFFF4EA)
    val HomeHeroOverlayEnd = Color(0x88FFF4EA)
    val CalendarHeroStart = Color(0xFFFFF8F1)
    val CalendarHeroMid = Color(0xFFFFEAF1)
    val CalendarHeroEnd = Color(0xFFEBD0BA)
    val EmptyBookCover = Color(0xFFB88A58)
    // 书库/封面通用叠层色（BookHomeScreen 中大量硬编码半透明色收敛）
    val CoverWhiteOverlayHigh = Color(0xCCFFFFFF)
    val CoverWhiteOverlayMedium = Color(0xAAFFFFFF)
    val CoverWhiteOverlayLow = Color(0x66FFFFFF)
    val CoverWhiteOverlaySubtle = Color(0x33FFFFFF)
    val CoverWhiteOverlayBorder = Color(0x38FFFFFF)
    val CoverWhiteOverlayHairline = Color(0x22FFFFFF)
    val CoverDarkSpine = Color(0x332F261D)
    val CoverDarkSpineStrong = Color(0x662F261D)
    val CoverPageEdgeWarm = Color(0xCCFFF9F0)

    // 目标详情页背景渐变
    val TargetDetailGradientEnd = Color(0xFFF1D9C4)
    val TargetDetailCardEnd = Color(0xFFFFF3D7)

    // 专题/灵感封面 fallback 深色（TopicCoverArt fallbackDeep）
    val TopicDeepOlive = Color(0xFF5F6F3D)
    val TopicDeepPlum = Color(0xFF754E5E)
    val TopicDeepBrown = Color(0xFF6F523D)
    val TopicDeepSteel = Color(0xFF445A72)
    val TopicDeepMoss = Color(0xFF566B5B)
    val TopicDeepPurple = Color(0xFF574B6B)

    // 灵感专题模板主色（InspirationTemplates 封面主色）
    val TopicPeach = Color(0xFFF2C0A5)
    val TopicRose = Color(0xFFF1A5B6)
    val TopicAmber = Color(0xFFFFAA5F)
    val TopicSage = Color(0xFFC9D6C1)
    val TopicPeriwinkle = Color(0xFF9EAADB)
    val TopicDaffodil = Color(0xFFF8D58A)
    val TopicMoss = Color(0xFFBBD1AD)
    val TopicForest = Color(0xFF334F46)
    val TopicTerracotta = Color(0xFFD6A06B)
    val TopicOlive = Color(0xFFA1B774)
    val TopicStone = Color(0xFFC6B4A0)
    val TopicSlate = Color(0xFFA9B6C8)
    val TopicPine = Color(0xFF6D8B7E)
    val TopicSpruce = Color(0xFF51675F)

    // 路由色（在 Topic 变量之后定义，避免前向引用）
    val RouteSchedule = TopicForest      // 森林绿
    val RouteDiary = TopicRose           // 玫瑰粉
    val RouteTarget = TopicMoss          // 苔藓绿
    val RouteOverview = TopicPeriwinkle  // 长春花蓝

    // 专题分类封面色板 mid/deep/ink（TopicCoverArt 按分类固定渐变与文字色）
    val TopicPeachMid = Color(0xFFD88F74)
    val TopicPeachDeep = Color(0xFF6E4638)
    val TopicPeachInk = Color(0xFFFFF4E8)
    val TopicMonthMid = Color(0xFFE7B28E)
    val TopicMonthDeep = Color(0xFF785740)
    val TopicMonthInk = Color(0xFFFFF7EC)
    val TopicAmberMid = Color(0xFFD4814E)
    val TopicAmberDeep = Color(0xFF5F4939)
    val TopicAmberInk = Color(0xFFFFF7EA)
    val TopicSageMid = Color(0xFF8FA77D)
    val TopicSageDeep = Color(0xFF3E594A)
    val TopicSageInk = Color(0xFFF7FFF1)
    val TopicRoseMid = Color(0xFFC97991)
    val TopicRoseDeep = Color(0xFF704559)
    val TopicRoseInk = Color(0xFFFFEEF4)
    val TopicPeriwinkleMid = Color(0xFF7C8BC8)
    val TopicPeriwinkleDeep = Color(0xFF3F496B)
    val TopicPeriwinkleInk = Color(0xFFF4F6FF)
    val TopicReviewMid = Color(0xFFC98C9B)
    val TopicReviewDeep = Color(0xFF5D4A58)
    val TopicReviewInk = Color(0xFFFFF2F5)
    val TopicDaffodilMid = Color(0xFFD0A45A)
    val TopicDaffodilDeep = Color(0xFF665335)
    val TopicDaffodilInk = Color(0xFFFFF8E6)
    val TopicPineMid = Color(0xFF4E6B60)
    val TopicPineDeep = Color(0xFF263D36)
    val TopicPineInk = Color(0xFFF1FFF8)
    val TopicTravelMid = Color(0xFFDFB460)
    val TopicTravelDeep = Color(0xFF6A5637)
    val TopicTravelInk = Color(0xFFFFF8E1)
    val TopicSpruceMid = Color(0xFF334F46)
    val TopicSpruceDeep = Color(0xFF1E2F2A)
    val TopicSpruceInk = Color(0xFFEFFFF8)
    val TopicBondMid = Color(0xFFD7859E)
    val TopicBondDeep = Color(0xFF6D4053)
    val TopicBondInk = Color(0xFFFFEFF4)
    val TopicCraftMid = Color(0xFFE1A15F)
    val TopicCraftDeep = Color(0xFF6B4E3A)
    val TopicCraftInk = Color(0xFFFFF2EB)

    // 原版 Goalday Morandi 调色板（对齐 aapt2 提取的 color_* 资源，用于封面/标签/心情/分类色）
    // 已存在的等价色：color_lightGreen=#BBD1AD=TopicMoss、color_orange=#F2C0A5=TopicPeach
    val MorandiBrownLight = Color(0xFFE5DAD4)   // color_E5DAD4 浅褐
    val MorandiKhaki = Color(0xFFE9D2B5)        // color_E9D2B5 浅卡其
    val MorandiCoral = Color(0xFFF56968)        // color_F56968 珊瑚红
    val MorandiApricot = Color(0xFFF5BD9F)       // color_F5BD9F 杏色
    val MorandiYellow = Color(0xFFF5D88B)        // color_F5D88B 浅黄
    val MorandiBeige = Color(0xFFF6EBDD)         // color_F6EBDD 米色
    val MorandiOrange = Color(0xFFF79941)        // color_F79941 橘黄
    val MorandiPeach = Color(0xFFFDE3D6)         // color_FDE3D6 浅桃
    val MorandiCream = Color(0xFFFDF5EB)         // color_FDF5EB 奶白
    val MorandiOffWhite = Color(0xFFFDFBF7)      // color_FDFBF7 雪白
    val MorandiPinkSoft = Color(0xFFFEECEC)      // color_FEECEC 浅粉
    val MorandiPinkRed = Color(0xFFFF98AF)       // color_FF98AF 粉红
    val MorandiDivider = Color(0xFFC5BBB6)       // color_tab_divider 分隔灰

    // 书库桌面渐变
    val LibraryDeskTop = Color(0xFFF8EFE5)
    val LibraryDeskGradient: Brush
        get() = Brush.verticalGradient(listOf(LibraryDeskTop, DeskMid, DeskBottom))

    // 书籍封面调色板（统一 CreateBookDialog/EditBookDialog/封面渐变）
    val BookCoverPalette = listOf(
        TopicPeach,
        TopicRose,
        TopicAmber,
        TopicMoss,
        TopicPeriwinkle,
    )

    // PageSurface 页面翻转/导出/AI洞察卡片
    val PageTurnEdgeStart = Color(0xFFDCCAB4)
    val PageTurnEdgeMid = Color(0xFFF4E9DD)
    val DiaryPromptGradientStart = Color(0xFFFFF9F4)
    val ExportPaperWarm = Color(0xFFF4DDC6)
    val ExportCanvasPaper = Color(0xFFFFFBF6)
    val ExportInkPrimary = Color(0xFF2F2922)
    val TagMauve = Color(0xFF8B5E6D)
    val ImageRemoveScrim = Color(0xAA1F1B17)
    val AiInsightStart = Color(0x1AF4DABB)
    val AiInsightMid = Color(0x0EF4DABB)
    val AiInsightEnd = Color(0x14F6E8D3)
    val AiInsightBorder = Color(0x22C8AF91)

    val DeskGradient: Brush
        get() = Brush.verticalGradient(listOf(DeskTop, DeskMid, DeskBottom))

    val PaperGradient: Brush
        get() = Brush.linearGradient(
            listOf(Paper, PaperWarm, PaperAged),
            start = Offset.Zero,
            end = Offset(680f, 420f),
        )

    val BookBoardGradient: Brush
        get() = Brush.horizontalGradient(
            listOf(BookBoardDark, BookBoard, Paper, PaperWarm, BookBoard),
        )

    val SpineGradient: Brush
        get() = Brush.verticalGradient(listOf(BookSpine, BookSpineLight, BookSpine))

    val CardPaperGradient: Brush
        get() = Brush.verticalGradient(listOf(Color(0xFFFFFEFA), Paper, Color(0xFFFFF2E1)))

    // 暗色色板储备（夜间纸张色，非纯黑，保留手账暖意）
    val DarkAppBg = Color(0xFF221E1A)
    val DarkSurface = Color(0xFF2C2722)
    val DarkTabBarBg = Color(0xFF3A332D)
    val DarkSurfaceSoft = Color(0xFF332D27)
    val DarkInkPrimary = Color(0xFFF0E9DD)
    val DarkInkSecondary = Color(0xFFB8AE9F)
    val DarkInkMuted = Color(0xFF8A8175)
    val DarkPaper = Color(0xFF2A2620)
    val DarkPaperWarm = Color(0xFF2F2A22)
    val DarkDivider = Color(0x14FFFFFF)

    // 暗色自适应颜色（@Composable getter，自动按 LocalGoaldayDarkMode.current 切换亮/暗）
    // screen 层改用这些属性即可获得暗色模式支持，无需在每个引用处写 if-else
    val adaptiveAppBg: Color @Composable get() = if (LocalGoaldayDarkMode.current) DarkAppBg else AppBg
    val adaptiveSurface: Color @Composable get() = if (LocalGoaldayDarkMode.current) DarkSurface else Surface
    val adaptiveSurfaceSoft: Color @Composable get() = if (LocalGoaldayDarkMode.current) DarkSurfaceSoft else SurfaceSoft
    val adaptiveInkPrimary: Color @Composable get() = if (LocalGoaldayDarkMode.current) DarkInkPrimary else InkPrimary
    val adaptiveInkSecondary: Color @Composable get() = if (LocalGoaldayDarkMode.current) DarkInkSecondary else InkSecondary
    val adaptiveInkMuted: Color @Composable get() = if (LocalGoaldayDarkMode.current) DarkInkMuted else InkMuted
    val adaptivePaper: Color @Composable get() = if (LocalGoaldayDarkMode.current) DarkPaper else Paper
    val adaptivePaperWarm: Color @Composable get() = if (LocalGoaldayDarkMode.current) DarkPaperWarm else PaperWarm
    val adaptiveDivider: Color @Composable get() = if (LocalGoaldayDarkMode.current) DarkDivider else Color(0x14000000)
    val adaptiveBorderColor: Color @Composable get() = if (LocalGoaldayDarkMode.current) Color(0xFF5A4E42) else BorderColor

    // 自适应白色叠层（夜间模式反转为深色叠层）
    val adaptiveWhiteOverlay: Color @Composable get() = if (LocalGoaldayDarkMode.current) Color(0x22FFFFFF) else Color(0xDEFFFFFF)
    val adaptiveWhiteOverlayMedium: Color @Composable get() = if (LocalGoaldayDarkMode.current) Color(0x18FFFFFF) else Color(0xB8FFFFFF)
    val adaptiveWhiteOverlayLow: Color @Composable get() = if (LocalGoaldayDarkMode.current) Color(0x10FFFFFF) else Color(0x72FFFFFF)
    val adaptiveScrim: Color @Composable get() = if (LocalGoaldayDarkMode.current) Color(0xAA000000) else Color(0x26000000)

    // 暗色自适应纸张渐变（书页背景）
    val adaptivePaperGradient: Brush @Composable get() =
        if (LocalGoaldayDarkMode.current) Brush.linearGradient(
            listOf(DarkPaper, DarkPaperWarm, DarkPaper),
            start = Offset.Zero,
            end = Offset(680f, 420f),
        ) else PaperGradient

    // 日记模块专用自适应色（对照逆向硬编码色值）
    val DiarySectionInk: Color @Composable get() = if (LocalGoaldayDarkMode.current) Color(0xFFD4C4B0) else Color(0xFF2C2C2C)
    val DiaryTimeInk: Color @Composable get() = if (LocalGoaldayDarkMode.current) Color(0xFF9E9E9E) else Color(0xFFCBCBCB)
    val DiaryTargetBackground: Color @Composable get() = if (LocalGoaldayDarkMode.current) Color(0xFF3D342C) else Color.White
    val DiaryTargetChildBackground: Color @Composable get() = if (LocalGoaldayDarkMode.current) Color(0xFF332D27) else Color(0xFFF6EBDD)
    val DiaryTargetChildDot: Color @Composable get() = if (LocalGoaldayDarkMode.current) Color(0xFF8A8175) else Color(0xFFC5BBB6)
    val ScheduleDateColumnSeparator: Color @Composable get() = if (LocalGoaldayDarkMode.current) Color(0xFF5A4E42) else Color(0xFFC5BBB6)

    // 字体族 token：封面/大标题用衬线（宋体感）建立手账氛围，正文用苹方（对齐原版 PingFang SC）
    val DisplayFontFamily: FontFamily get() = FontFamily.Serif
    val BodyFontFamily: FontFamily get() = FontFamily(Font(R.font.pingfang_sc))
}
