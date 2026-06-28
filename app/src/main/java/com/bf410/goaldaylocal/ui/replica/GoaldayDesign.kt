package com.bf410.goaldaylocal.ui.replica

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object GoaldayDesign {
    val AppBg = Color(0xFFFAF8F4)
    val Surface = Color(0xFFFFFEFB)
    val SurfaceSoft = Color(0xFFF7F3EE)
    val InkPrimary = Color(0xFF2F2923)
    val InkSecondary = Color(0xFF7D756B)
    val InkMuted = Color(0xFFA79E92)
    val Accent = Color(0xFFE88FAE)
    val AccentSoft = Color(0xFFFFECF3)
    val Pink = Color(0xFFE88FAE)
    val PinkSoft = Color(0xFFFFECF3)
    val GreenSoft = Color(0xFFEAF4E4)
    val Positive = Color(0xFF769B69)
    val Danger = Color(0xFFA15E58)
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

    val RadiusS = 8.dp
    val RadiusM = 12.dp
    val RadiusL = 16.dp
    val RadiusPill = 99.dp

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
}
