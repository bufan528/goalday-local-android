package com.bf410.goaldaylocal.ui.book

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.bf410.goaldaylocal.data.BookPage
import com.bf410.goaldaylocal.data.DiaryPage
import com.bf410.goaldaylocal.data.PlanPage
import com.bf410.goaldaylocal.data.SchedulePage
import com.bf410.goaldaylocal.data.TargetPage
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.math.abs

@Composable
fun BookPageTurner(
    bookId: String,
    bookTitle: String,
    subtitle: String,
    page: BookPage,
    previousPage: BookPage?,
    nextPage: BookPage?,
    pageIndex: Int,
    pageCount: Int,
    tint: Color,
    isSaved: Boolean,
    diaryDraft: String,
    customPageItems: List<String>,
    onToggleSaved: () -> Unit,
    isChecked: (String, String) -> Boolean,
    onToggleChecked: (String, String) -> Unit,
    onDiaryChange: (String) -> Unit,
    onAddCustomItem: (String) -> Unit,
    onRemoveCustomItem: (String) -> Unit,
    onAddToSchedule: (String, Int) -> Unit,
    onFlipNext: () -> Unit,
    onFlipPrevious: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val progress = remember(pageIndex, bookId) { Animatable(0f) }
    var direction by remember(pageIndex, bookId) { mutableStateOf<TurnDirection?>(null) }
    var pageWidthPx by remember { mutableFloatStateOf(1f) }
    var lastVelocityPxPerSecond by remember { mutableFloatStateOf(0f) }
    var lastEventTimeMs by remember { mutableStateOf(0L) }
    var diaryCommand by remember(pageIndex) { mutableStateOf<RichEditorCommand?>(null) }

    val canTurnPrevious = previousPage != null
    val canTurnNext = nextPage != null
    val dragProgress = progress.value.coerceIn(0f, 1f)
    val draggingToNext = direction == TurnDirection.NEXT
    val draggingToPrevious = direction == TurnDirection.PREVIOUS
    val turnRotation = when (direction) {
        TurnDirection.NEXT -> -100f * dragProgress
        TurnDirection.PREVIOUS -> 100f * dragProgress
        null -> 0f
    }
    val turnShadowWidth = (18f + dragProgress * dragProgress * 86f).dp
    val destinationPage = when (direction) {
        TurnDirection.NEXT -> nextPage
        TurnDirection.PREVIOUS -> previousPage
        null -> null
    }

    fun settle(result: TurnReleaseResult) {
        scope.launch {
            when (result) {
                TurnReleaseResult.CompleteNext -> {
                    progress.animateTo(1f, animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing))
                    onFlipNext()
                }
                TurnReleaseResult.CompletePrevious -> {
                    progress.animateTo(1f, animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing))
                    onFlipPrevious()
                }
                TurnReleaseResult.SnapBack -> {
                    progress.animateTo(0f, animationSpec = tween(durationMillis = 280, easing = LinearOutSlowInEasing))
                }
            }
            progress.snapTo(0f)
            direction = null
            lastVelocityPxPerSecond = 0f
            lastEventTimeMs = 0L
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .onSizeChanged { pageWidthPx = it.width.toFloat().coerceAtLeast(1f) }
            .pointerInput(pageIndex, bookId, canTurnNext, canTurnPrevious, pageWidthPx) {
                detectHorizontalDragGestures(
                    onDragStart = {
                        lastVelocityPxPerSecond = 0f
                        lastEventTimeMs = 0L
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        val resolvedDirection = when {
                            dragAmount < 0f -> TurnDirection.NEXT
                            dragAmount > 0f -> TurnDirection.PREVIOUS
                            else -> direction
                        } ?: return@detectHorizontalDragGestures

                        direction = resolvedDirection
                        val canTurn = when (resolvedDirection) {
                            TurnDirection.NEXT -> canTurnNext
                            TurnDirection.PREVIOUS -> canTurnPrevious
                        }
                        val deltaProgress = abs(dragAmount) / pageWidthPx
                        val rawProgress = progress.value + deltaProgress
                        val adjustedProgress = applyBoundaryResistance(rawProgress, canTurn)

                        val nowMs = change.uptimeMillis
                        val deltaMs = (nowMs - lastEventTimeMs).coerceAtLeast(1L)
                        lastVelocityPxPerSecond = (dragAmount / deltaMs) * 1000f
                        lastEventTimeMs = nowMs

                        scope.launch {
                            progress.snapTo(adjustedProgress)
                        }
                    },
                    onDragCancel = { settle(TurnReleaseResult.SnapBack) },
                    onDragEnd = {
                        val activeDirection = direction ?: return@detectHorizontalDragGestures
                        settle(
                            resolvePageTurnRelease(
                                direction = activeDirection,
                                progress = dragProgress,
                                velocity = lastVelocityPxPerSecond,
                                hasPreviousPage = canTurnPrevious,
                                hasNextPage = canTurnNext,
                            ),
                        )
                    },
                )
            },
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp)
                .shadow(28.dp, RoundedCornerShape(30.dp), clip = false)
                .clip(RoundedCornerShape(30.dp))
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFFE7D4BE), Color(0xFFF7F0E6), Color(0xFFE6D2BC)),
                        start = Offset.Zero,
                        end = Offset(1100f, 1500f),
                    ),
                ),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0x22A18362), Color.Transparent, Color(0x22A18362)),
                        ),
                    ),
            )

            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .width(12.dp)
                    .fillMaxHeight()
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFFB79E83), Color(0xFFF0E1D1), Color(0xFFB79E83)),
                        ),
                    ),
            )

            DestinationPageLayer(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                bookTitle = bookTitle,
                subtitle = subtitle,
                page = destinationPage,
                pageIndex = pageIndex,
                pageCount = pageCount,
                tint = tint,
                revealProgress = dragProgress,
                direction = direction,
            )

            if (direction != null && dragProgress > 0.01f) {
                PageBackLayer(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                        .graphicsLayer {
                            transformOrigin = if (draggingToNext) TransformOrigin(0f, 0.5f) else TransformOrigin(1f, 0.5f)
                            rotationY = turnRotation * 0.96f
                            cameraDistance = 34f * density
                        },
                    tint = tint,
                    progress = dragProgress,
                    direction = direction,
                )
            }

            ActivePageLayer(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .graphicsLayer {
                        transformOrigin = if (draggingToNext) TransformOrigin(0f, 0.5f) else TransformOrigin(1f, 0.5f)
                        rotationY = turnRotation
                        translationX = when {
                            draggingToNext -> -(dragProgress * 26f + dragProgress * dragProgress * 28f)
                            draggingToPrevious -> dragProgress * 26f + dragProgress * dragProgress * 28f
                            else -> 0f
                        }
                        cameraDistance = 34f * density
                        shadowElevation = 24f
                    },
                page = page,
                pageIndex = pageIndex,
                pageCount = pageCount,
                bookTitle = bookTitle,
                subtitle = subtitle,
                tint = tint,
                isSaved = isSaved,
                diaryDraft = diaryDraft,
                customPageItems = customPageItems,
                onToggleSaved = onToggleSaved,
                isChecked = isChecked,
                onToggleChecked = onToggleChecked,
                onDiaryChange = onDiaryChange,
                onAddCustomItem = onAddCustomItem,
                onRemoveCustomItem = onRemoveCustomItem,
                onAddToSchedule = onAddToSchedule,
                pendingCommand = diaryCommand,
                onCommand = { diaryCommand = it },
            )

            if (direction != null && dragProgress > 0.01f) {
                Box(
                    modifier = Modifier
                        .align(if (draggingToNext) Alignment.CenterStart else Alignment.CenterEnd)
                        .fillMaxHeight()
                        .width(turnShadowWidth)
                        .background(
                            if (draggingToNext) {
                                Brush.horizontalGradient(
                                    listOf(
                                        Color.Black.copy(alpha = (0.10f + dragProgress * dragProgress * 0.34f).coerceAtMost(0.50f)),
                                        Color(0x22000000),
                                        Color.Transparent,
                                    ),
                                )
                            } else {
                                Brush.horizontalGradient(
                                    listOf(
                                        Color.Transparent,
                                        Color(0x22000000),
                                        Color.Black.copy(alpha = (0.10f + dragProgress * dragProgress * 0.34f).coerceAtMost(0.50f)),
                                    ),
                                )
                            },
                        ),
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .width(52.dp)
                    .fillMaxHeight()
                    .clickable(enabled = canTurnPrevious) {
                        direction = TurnDirection.PREVIOUS
                        scope.launch {
                            progress.snapTo(initialEdgeTapProgress())
                        }
                        settle(TurnReleaseResult.CompletePrevious)
                    },
            )

            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .width(52.dp)
                    .fillMaxHeight()
                    .clickable(enabled = canTurnNext) {
                        direction = TurnDirection.NEXT
                        scope.launch {
                            progress.snapTo(initialEdgeTapProgress())
                        }
                        settle(TurnReleaseResult.CompleteNext)
                    },
            )
        }
    }
}

@Composable
private fun DestinationPageLayer(
    modifier: Modifier,
    bookTitle: String,
    subtitle: String,
    page: BookPage?,
    pageIndex: Int,
    pageCount: Int,
    tint: Color,
    revealProgress: Float,
    direction: TurnDirection?,
) {
    val pageTitle = page?.title ?: "封面"
    val pageSubtitle = when (direction) {
        TurnDirection.NEXT -> "下一页"
        TurnDirection.PREVIOUS -> "上一页"
        null -> subtitle
    }
    val pageNumber = when {
        page == null -> ""
        direction == TurnDirection.NEXT -> "${pageIndex + 2} / $pageCount"
        direction == TurnDirection.PREVIOUS -> "$pageIndex / $pageCount"
        else -> "${pageIndex + 1} / $pageCount"
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFFF1E0CA), Color(0xFFFFFBF5), Color(0xFFEFDFC9)),
                ),
            )
            .padding(26.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = (0.08f + revealProgress * revealProgress * 0.92f).coerceIn(0.08f, 1f) },
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(99.dp)).background(tint.copy(alpha = 0.78f)))
                Spacer(Modifier.width(8.dp))
                Text(bookTitle, style = MaterialTheme.typography.titleMedium, color = Color(0xFF2D261F))
            }
            Spacer(Modifier.height(18.dp))
            Text(pageTitle, style = MaterialTheme.typography.headlineMedium, color = Color(0xFF2D261F))
            Text(pageSubtitle, style = MaterialTheme.typography.bodySmall, color = Color(0xFF887A6D), modifier = Modifier.padding(top = 4.dp))
            Spacer(Modifier.height(20.dp))
            Text(page?.let(::pagePreviewText) ?: subtitle, style = MaterialTheme.typography.bodyLarge, color = Color(0xFF5F564C))
            Spacer(Modifier.weight(1f))
            if (pageNumber.isNotBlank()) {
                Text(pageNumber, style = MaterialTheme.typography.labelLarge, color = Color(0xFF887A6D))
            }
        }
    }
}

@Composable
private fun PageBackLayer(
    modifier: Modifier,
    tint: Color,
    progress: Float,
    direction: TurnDirection?,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.horizontalGradient(
                    if (direction == TurnDirection.NEXT) {
                        listOf(Color(0xFFE5D5C0), Color(0xFFF8F1E7), Color(0xFFFFFCF7))
                    } else {
                        listOf(Color(0xFFFFFCF7), Color(0xFFF8F1E7), Color(0xFFE5D5C0))
                    },
                ),
            )
            .padding(26.dp),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(99.dp)).background(tint.copy(alpha = 0.64f)))
                Spacer(Modifier.width(8.dp))
                Text("纸页背面", style = MaterialTheme.typography.titleMedium, color = Color(0xFF6D6257))
            }
            Spacer(Modifier.height(18.dp))
            Text("翻页进行中", style = MaterialTheme.typography.bodyLarge, color = Color(0xFF85786B))
            Spacer(Modifier.height(10.dp))
            Text("当前进度 ${(progress * 100).toInt()}%", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF9C8F81))
            Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
private fun ActivePageLayer(
    modifier: Modifier,
    page: BookPage,
    pageIndex: Int,
    pageCount: Int,
    bookTitle: String,
    subtitle: String,
    tint: Color,
    isSaved: Boolean,
    diaryDraft: String,
    customPageItems: List<String>,
    onToggleSaved: () -> Unit,
    isChecked: (String, String) -> Boolean,
    onToggleChecked: (String, String) -> Unit,
    onDiaryChange: (String) -> Unit,
    onAddCustomItem: (String) -> Unit,
    onRemoveCustomItem: (String) -> Unit,
    onAddToSchedule: (String, Int) -> Unit,
    pendingCommand: RichEditorCommand?,
    onCommand: (RichEditorCommand) -> Unit,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFFFFFEFB), Color(0xFFF9F2E9), Color(0xFFEBDAC4)),
                ),
            )
            .padding(26.dp),
    ) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(99.dp)).background(tint))
                Spacer(Modifier.width(8.dp))
                Text(bookTitle, style = MaterialTheme.typography.titleLarge)
            }
            Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF6C635A), modifier = Modifier.padding(top = 4.dp))
            TextButton(onClick = onToggleSaved, modifier = Modifier.padding(top = 2.dp)) {
                Text(if (isSaved) "已保存到我的书" else "保存为我的书")
            }
            Spacer(Modifier.height(10.dp))
            Text(text = page.title, style = MaterialTheme.typography.headlineMedium, color = Color(0xFF2D261F))
            Spacer(Modifier.height(18.dp))
            when (page) {
                is TargetPage -> EditableBulletPage(page.title, page.items, customPageItems, tint, "添加你的目标", isChecked, onToggleChecked, onAddCustomItem, onRemoveCustomItem, onAddToSchedule)
                is SchedulePage -> EditableBulletPage(page.title, page.items, customPageItems, tint.copy(alpha = 0.74f), "添加你的日程任务", isChecked, onToggleChecked, onAddCustomItem, onRemoveCustomItem, onAddToSchedule)
                is PlanPage -> EditableBulletPage(page.title, page.items, customPageItems, Color(0xFFB88A58), "添加你的计划", isChecked, onToggleChecked, onAddCustomItem, onRemoveCustomItem, onAddToSchedule)
                is DiaryPage -> DiarySection(
                    prompt = page.prompt,
                    tint = tint,
                    diaryDraft = diaryDraft,
                    pendingCommand = pendingCommand,
                    onCommand = onCommand,
                    onDiaryChange = onDiaryChange,
                )
            }
            Spacer(Modifier.height(28.dp))
            Text(text = "${pageIndex + 1} / $pageCount", style = MaterialTheme.typography.labelLarge, color = Color(0xFF7A7065))
        }
    }
}

private fun pagePreviewText(page: BookPage): String =
    when (page) {
        is TargetPage -> page.items.take(3).joinToString("\n") { "• $it" }.ifBlank { "这一页还没有内容" }
        is PlanPage -> page.items.take(3).joinToString("\n") { "• $it" }.ifBlank { "这一页还没有内容" }
        is SchedulePage -> page.items.take(3).joinToString("\n") { "• $it" }.ifBlank { "这一页还没有内容" }
        is DiaryPage -> page.prompt
    }

@Composable
private fun EditableBulletPage(
    pageTitle: String,
    baseItems: List<String>,
    customItems: List<String>,
    tint: Color,
    inputLabel: String,
    isChecked: (String, String) -> Boolean,
    onToggleChecked: (String, String) -> Unit,
    onAddCustomItem: (String) -> Unit,
    onRemoveCustomItem: (String) -> Unit,
    onAddToSchedule: (String, Int) -> Unit,
) {
    var newItem by remember(pageTitle) { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        baseItems.forEach { item ->
            ActionBulletRow(pageTitle, item, tint, isChecked(pageTitle, item), false, onToggleChecked, onAddToSchedule, onRemoveCustomItem)
        }
        if (customItems.isNotEmpty()) {
            Text("我的内容", style = MaterialTheme.typography.titleMedium, color = Color(0xFF5E4837))
            customItems.forEach { item ->
                ActionBulletRow(pageTitle, item, tint, isChecked(pageTitle, item), true, onToggleChecked, onAddToSchedule, onRemoveCustomItem)
            }
        }
        OutlinedTextField(
            value = newItem,
            onValueChange = { newItem = it },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            label = { Text(inputLabel) },
            singleLine = true,
        )
        TextButton(onClick = {
            onAddCustomItem(newItem)
            newItem = ""
        }) {
            Text("保存")
        }
    }
}

@Composable
private fun ActionBulletRow(
    pageTitle: String,
    item: String,
    tint: Color,
    checked: Boolean,
    removable: Boolean,
    onToggleChecked: (String, String) -> Unit,
    onAddToSchedule: (String, Int) -> Unit,
    onRemoveCustomItem: (String) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier
                .clickable { onToggleChecked(pageTitle, item) }
                .fillMaxWidth(0.66f),
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 6.dp)
                    .size(18.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .background(if (checked) tint else Color(0xFFE5DBCD)),
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = item,
                style = completedTextStyle(checked),
                color = if (checked) Color(0xFF8B847D) else Color(0xFF342C24),
            )
        }
        Text(
            text = "加入日历",
            style = MaterialTheme.typography.labelLarge,
            color = Color(0xFF8F684F),
            modifier = Modifier
                .clip(RoundedCornerShape(99.dp))
                .background(Color(0x1A8F684F))
                .clickable { onAddToSchedule(item, LocalDate.now().dayOfMonth) }
                .padding(horizontal = 10.dp, vertical = 8.dp),
        )
        if (removable) {
            Text(
                text = "删除",
                style = MaterialTheme.typography.labelLarge,
                color = Color(0xFF9C5A52),
                modifier = Modifier
                    .clickable { onRemoveCustomItem(item) }
                    .padding(top = 8.dp),
            )
        }
    }
}

@Composable
private fun completedTextStyle(completed: Boolean): TextStyle =
    MaterialTheme.typography.bodyLarge.copy(
        textDecoration = if (completed) TextDecoration.LineThrough else TextDecoration.None,
    )

@Composable
private fun DiarySection(
    prompt: String,
    tint: Color,
    diaryDraft: String,
    pendingCommand: RichEditorCommand?,
    onCommand: (RichEditorCommand) -> Unit,
    onDiaryChange: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(text = prompt, style = MaterialTheme.typography.titleMedium, color = Color(0xFF342C24))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            DiaryToolChip("加粗") { onCommand(RichEditorCommand("bold")) }
            DiaryToolChip("标题") { onCommand(RichEditorCommand("formatBlock", "<h2>")) }
            DiaryToolChip("列表") { onCommand(RichEditorCommand("insertUnorderedList")) }
            DiaryToolChip("引用") { onCommand(RichEditorCommand("formatBlock", "<blockquote>")) }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFFFFFBF5))
                .padding(18.dp),
        ) {
            RichDiaryEditor(
                html = diaryDraft,
                placeholder = "写下今天的记录、感受或下一步。",
                modifier = Modifier.fillMaxSize(),
                pendingCommand = pendingCommand,
                onHtmlChange = onDiaryChange,
            )
        }
        Text(
            text = "纸页内容会保存在本地，不依赖服务器。",
            style = MaterialTheme.typography.bodySmall,
            color = tint.copy(alpha = 0.72f),
        )
    }
}

@Composable
private fun DiaryToolChip(
    label: String,
    onClick: () -> Unit,
) {
    Text(
        text = label,
        color = Color(0xFF8F684F),
        modifier = Modifier
            .clip(RoundedCornerShape(99.dp))
            .background(Color(0x1A8F684F))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    )
}
