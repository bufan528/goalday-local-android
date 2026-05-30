package com.bf410.goaldaylocal.ui.book

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

enum class ShellStyle {
    LIGHT,
    BOOK,
}

@Composable
fun BookShell(
    modifier: Modifier = Modifier,
    shellStyle: ShellStyle = ShellStyle.LIGHT,
    canTurnPrevious: Boolean,
    canTurnNext: Boolean,
    turnEnabled: Boolean,
    onTapPrevious: () -> Unit,
    onTapNext: () -> Unit,
    content: @Composable BoxScope.() -> Unit,
) {
    val outerShape = if (shellStyle == ShellStyle.BOOK) RoundedCornerShape(34.dp) else RoundedCornerShape(28.dp)
    val innerShape = if (shellStyle == ShellStyle.BOOK) RoundedCornerShape(26.dp) else RoundedCornerShape(22.dp)
    val outerPaddingH = if (shellStyle == ShellStyle.BOOK) 8.dp else 10.dp
    val outerPaddingV = if (shellStyle == ShellStyle.BOOK) 6.dp else 8.dp
    val edgeZoneWidth = if (shellStyle == ShellStyle.BOOK) 24.dp else 20.dp

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = outerPaddingH, vertical = outerPaddingV)
            .shadow(if (shellStyle == ShellStyle.BOOK) 20.dp else 14.dp, outerShape, clip = false)
            .clip(outerShape)
            .background(
                if (shellStyle == ShellStyle.BOOK) {
                    Brush.linearGradient(
                        listOf(Color(0xFFB9865E), Color(0xFFE4C39F), Color(0xFFAA7750)),
                        start = Offset.Zero,
                        end = Offset(900f, 700f),
                    )
                } else {
                    Brush.verticalGradient(listOf(Color(0xFFFDFDFD), Color(0xFFFDFDFD)))
                },
            ),
    ) {
        if (shellStyle == ShellStyle.BOOK) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(14.dp)
                    .padding(horizontal = 18.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0x55EBD8C6), Color(0x88FFF8EE), Color(0x55EBD8C6)),
                        ),
                    ),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(8.dp)
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0x66EFE3D6), Color(0x99FFF8EE), Color(0x66EFE3D6)),
                        ),
                    ),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(28.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0x26FFFFFF), Color.Transparent),
                        ),
                    ),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .width(18.dp)
                    .fillMaxHeight()
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0x66744B2F), Color(0x448E6141), Color(0x22744B2F), Color.Transparent),
                        ),
                    ),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .width(8.dp)
                    .height(24.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFFC64B5A), Color(0xFF8D2430)),
                        ),
                    ),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(10.dp)
                    .padding(horizontal = 20.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0x338A6547), Color(0x66FFF7ED), Color(0x338A6547)),
                        ),
                    ),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .width(12.dp)
                    .fillMaxHeight()
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0x44E2C9B2), Color(0x88FFF7EC)),
                        ),
                    ),
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        if (shellStyle == ShellStyle.BOOK) {
                            listOf(Color(0x22FFFFFF), Color.Transparent, Color(0x12000000))
                        } else {
                            listOf(Color(0x10FFFFFF), Color.Transparent, Color(0x05000000))
                        },
                    ),
                ),
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = if (shellStyle == ShellStyle.BOOK) 10.dp else 8.dp, vertical = if (shellStyle == ShellStyle.BOOK) 12.dp else 10.dp)
                .clip(innerShape)
                .background(Color(0xFFFFFFFF))
                .then(
                    if (shellStyle == ShellStyle.BOOK) {
                        Modifier.border(1.dp, Color(0x22AA8B70), innerShape)
                    } else {
                        Modifier
                    },
                ),
        )

        if (shellStyle == ShellStyle.BOOK) {
            repeat(4) { layer ->
                val offset = 2 + layer
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(top = (18 + layer).dp, bottom = (20 + layer).dp, end = (offset).dp)
                        .width((3 + layer).dp)
                        .fillMaxHeight()
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0x30A07D5E), Color(0x80FFF8EE)),
                            ),
                        ),
                )
            }
            repeat(3) { layer ->
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(start = (16 + layer).dp, end = (16 + layer).dp, bottom = (2 + layer).dp)
                        .height((2 + layer).dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0x33A07D5E), Color(0x66FFF8EE), Color(0x33A07D5E)),
                            ),
                        ),
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .width(if (shellStyle == ShellStyle.BOOK) 3.dp else 1.dp)
                .fillMaxHeight()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            if (shellStyle == ShellStyle.BOOK) Color(0x335E3E26) else Color.Transparent,
                            if (shellStyle == ShellStyle.BOOK) Color(0x55F2E4D6) else Color(0x14000000),
                            if (shellStyle == ShellStyle.BOOK) Color(0x335E3E26) else Color.Transparent,
                        ),
                    ),
                ),
        )

        content()

        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .width(edgeZoneWidth)
                .fillMaxHeight()
                .clickable(enabled = canTurnPrevious && turnEnabled, onClick = onTapPrevious),
        )

        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .width(edgeZoneWidth)
                .fillMaxHeight()
                .clickable(enabled = canTurnNext && turnEnabled, onClick = onTapNext),
        )
    }
}
