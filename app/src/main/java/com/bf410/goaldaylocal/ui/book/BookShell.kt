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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bf410.goaldaylocal.ui.replica.GoaldayDesign

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
    val outerShape = if (shellStyle == ShellStyle.BOOK) RoundedCornerShape(40.dp) else RoundedCornerShape(28.dp)
    val innerShape = if (shellStyle == ShellStyle.BOOK) RoundedCornerShape(30.dp) else RoundedCornerShape(22.dp)
    val outerPaddingH = if (shellStyle == ShellStyle.BOOK) 4.dp else 10.dp
    val outerPaddingV = if (shellStyle == ShellStyle.BOOK) 2.dp else 8.dp
    val edgeZoneWidth = if (shellStyle == ShellStyle.BOOK) 30.dp else 20.dp

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = outerPaddingH, vertical = outerPaddingV)
            .shadow(if (shellStyle == ShellStyle.BOOK) 30.dp else 14.dp, outerShape, clip = false)
            .clip(outerShape)
            .background(
                if (shellStyle == ShellStyle.BOOK) {
                    Brush.linearGradient(
                        listOf(Color(0xFFBF8797), Color(0xFFFFF2F6), Color(0xFFD8A2AE), Color(0xFF9F6674)),
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
                    .align(Alignment.CenterStart)
                    .width(26.dp)
                    .fillMaxHeight()
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xC55A3440), Color(0x9A8A5361), Color(0x70C58E9E), Color.Transparent),
                        ),
                    ),
            )
            Text(
                "GOALDAY",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFFDECF1),
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .graphicsLayer { rotationZ = -90f }
                    .padding(bottom = 2.dp),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 6.dp, top = 24.dp, bottom = 24.dp)
                    .width(2.dp)
                    .fillMaxHeight()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0x66FFF7FA), Color(0x55F5C8D5), Color(0x66FFF7FA)),
                        ),
                    ),
            )
            repeat(7) { index ->
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 12.dp, top = (42 + index * 38).dp)
                        .width(5.dp)
                        .height(10.dp)
                        .clip(RoundedCornerShape(99.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0x554C2630), Color(0x66FFF8FB), Color(0x334C2630)),
                            ),
                        ),
                )
            }
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(14.dp)
                    .padding(horizontal = 18.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0x55E8B8C4), Color(0xAAFFF8FA), Color(0x55E8B8C4)),
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
                            listOf(Color(0x66F3C8D2), Color(0xCCFFF9FB), Color(0x66F3C8D2)),
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
                    .width(22.dp)
                    .fillMaxHeight()
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0x765A3440), Color(0x4C8A5361), Color(0x2DC58E9E), Color.Transparent),
                        ),
                    ),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .width(8.dp)
                    .height(28.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFFE88FAE), Color(0xFFA94F6C)),
                        ),
                    ),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(12.dp)
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
                    .width(20.dp)
                    .fillMaxHeight()
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0x40A68466), Color(0x66EED8C4), Color(0xAAFFF7EC)),
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
                .padding(horizontal = if (shellStyle == ShellStyle.BOOK) 14.dp else 8.dp, vertical = if (shellStyle == ShellStyle.BOOK) 15.dp else 10.dp)
                .clip(innerShape)
                .background(Color(0xFFFFFFFF))
                .then(
                    if (shellStyle == ShellStyle.BOOK) {
                        Modifier.border(1.5.dp, Color(0x40A7896E), innerShape)
                    } else {
                        Modifier
                    },
                ),
        )

        if (shellStyle == ShellStyle.BOOK) {
            repeat(5) { layer ->
                val offset = 2 + layer
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(top = (14 + layer).dp, bottom = (16 + layer).dp, end = (offset).dp)
                        .width((4 + layer).dp)
                        .fillMaxHeight()
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0x66A07D5E), Color(0xDDFFF8EE)),
                            ),
                        ),
                )
            }
            repeat(3) { layer ->
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(start = (14 + layer).dp, end = (14 + layer).dp, bottom = (2 + layer).dp)
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
                .width(if (shellStyle == ShellStyle.BOOK) 4.dp else 1.dp)
                .fillMaxHeight()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            if (shellStyle == ShellStyle.BOOK) Color(0x4A5E3E26) else Color.Transparent,
                            if (shellStyle == ShellStyle.BOOK) Color(0x66F2E4D6) else Color(0x14000000),
                            if (shellStyle == ShellStyle.BOOK) Color(0x4A5E3E26) else Color.Transparent,
                        ),
                    ),
                ),
        )

        if (shellStyle == ShellStyle.BOOK) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(34.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.White.copy(alpha = 0.22f), Color.Transparent),
                        ),
                    ),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(20.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, GoaldayDesign.PrimaryAction.copy(alpha = 0.12f)),
                        ),
                    ),
            )
        }

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
