package com.bf410.goaldaylocal.ui.replica

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp

// 暗色模式开关：由 GoaldayApp 顶层根据 MMKV "dark_mode" (AUTO/LIGHT/DARK) + 系统主题决定后提供
val LocalGoaldayDarkMode = compositionLocalOf { false }

object GoaldayDesign {
    val AppBg = Color(0xFFFAF8F4)
    val Surface = Color(0xFFFFFEFB)
    val SurfaceSoft = Color(0xFFF7F3EE)
    val InkPrimary = Color(0xFF2F2923)
    val InkSecondary = Color(0xFF7D756B)
    val InkMuted = Color(0xFF766B60)
    // 主品牌色：统一用 Pink/PinkSoft 语义（Accent/AccentSoft 别名已合并去重）
    val Pink = Color(0xFFE88FAE)
    val PinkSoft = Color(0xFFFFECF3)
    // PinkTint：粉色薄叠层（alpha 0.18），用于 chip 背景、高亮底色等场景，替代散落的 Color(0x18E88FAE) 与 Pink.copy(alpha = 0.18f)
    val PinkTint: Color
        get() = Pink.copy(alpha = 0.18f)
    val GreenSoft = Color(0xFFEAF4E4)
    val Positive = Color(0xFF769B69)
    val Danger = Color(0xFFA15E58)
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
    val BookBoard = Color(0xFFE0B895)
    val BookBoardDark = Color(0xFF6C4A39)
    val BookSpine = Color(0xFF5A3B2E)
    val BookSpineLight = Color(0xFFB47C62)
    val DeskTop = Color(0xFFFFFBF6)
    val DeskMid = Color(0xFFF5E4D2)
    val DeskBottom = Color(0xFFE4C7AE)
    val ShelfWood = Color(0xFFD3A681)
    val ShelfWoodDark = Color(0xFF8F6042)
    val RouteSchedule = Pink
    val RouteDiary = Color(0xFFB07A8F)
    val RouteTarget = Color(0xFF6F8E68)
    val RouteOverview = Color(0xFF8F684F)

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
    val WhiteOverlaySubtle = Color(0x33FFFFFF)
    val DarkOverlay = Color(0xAA1F1712)
    val DarkOverlaySoft = Color(0x991F1712)
    val BlackOverlaySoft = Color(0x26000000)

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

    // 暗色自适应纸张渐变（书页背景）
    val adaptivePaperGradient: Brush @Composable get() =
        if (LocalGoaldayDarkMode.current) Brush.linearGradient(
            listOf(DarkPaper, DarkPaperWarm, DarkPaper),
            start = Offset.Zero,
            end = Offset(680f, 420f),
        ) else PaperGradient

    // 字体族 token：封面/大标题用衬线（宋体感）建立手账氛围，UI 正文用默认无衬线保可读性
    val DisplayFontFamily: FontFamily get() = FontFamily.Serif
    val BodyFontFamily: FontFamily get() = FontFamily.Default
}
