package com.bf410.goaldaylocal.ui.book

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun BookShell(
    modifier: Modifier = Modifier,
    canTurnPrevious: Boolean,
    canTurnNext: Boolean,
    turnEnabled: Boolean,
    onTapPrevious: () -> Unit,
    onTapNext: () -> Unit,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 6.dp, vertical = 4.dp)
            .shadow(42.dp, RoundedCornerShape(44.dp), clip = false)
            .clip(RoundedCornerShape(44.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFFB78055), Color(0xFFE4C19B), Color(0xFFC28D60)),
                    start = Offset.Zero,
                    end = Offset(1300f, 900f),
                ),
            ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color(0x30FFFFFF), Color.Transparent, Color(0x22000000)))),
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 18.dp)
                .clip(RoundedCornerShape(38.dp))
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFFBC8B63), Color(0xFFE8CCAE), Color(0xFFB07E55)),
                        start = Offset(0f, 120f),
                        end = Offset(1280f, 900f),
                    ),
                ),
        )

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .width(30.dp)
                .fillMaxHeight()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFF885A38).copy(alpha = 0.52f),
                            Color(0xFFF3DFC8).copy(alpha = 0.88f),
                            Color(0xFF885A38).copy(alpha = 0.52f),
                        ),
                    ),
                ),
        )

        content()

        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .width(60.dp)
                .fillMaxHeight()
                .clickable(enabled = canTurnPrevious && turnEnabled, onClick = onTapPrevious),
        )

        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .width(60.dp)
                .fillMaxHeight()
                .clickable(enabled = canTurnNext && turnEnabled, onClick = onTapNext),
        )
    }
}
