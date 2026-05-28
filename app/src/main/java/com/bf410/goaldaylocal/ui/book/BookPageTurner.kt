package com.bf410.goaldaylocal.ui.book

import android.annotation.SuppressLint
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
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
    onRenameCustomItem: (String, String) -> Unit,
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
    var contentMode by remember(pageIndex, bookId) { mutableStateOf<PageContentMode>(PageContentMode.Browsing) }

    val canTurnPrevious = previousPage != null
    val canTurnNext = nextPage != null
    val turnEnabled = canTurnPage(contentMode)
    val dragProgress = progress.value.coerceIn(0f, 1f)
    val visualProgress = visualTurnProgress(dragProgress)
    val draggingToNext = direction == TurnDirection.NEXT
    val draggingToPrevious = direction == TurnDirection.PREVIOUS
    val turnRotation = when (direction) {
        TurnDirection.NEXT -> -108f * visualProgress
        TurnDirection.PREVIOUS -> 108f * visualProgress
        null -> 0f
    }
    val turnShadowWidth = (18f + visualProgress * visualProgress * 92f).dp
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
            .pointerInput(pageIndex, bookId, canTurnNext, canTurnPrevious, pageWidthPx, turnEnabled) {
                if (!turnEnabled) return@pointerInput
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

                        scope.launch { progress.snapTo(adjustedProgress) }
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
                .padding(horizontal = 8.dp, vertical = 6.dp)
                .shadow(34.dp, RoundedCornerShape(42.dp), clip = false)
                .clip(RoundedCornerShape(42.dp))
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFFC79B75), Color(0xFFE7C7A5), Color(0xFFD2A784)),
                        start = Offset(0f, 0f),
                        end = Offset(1300f, 900f),
                    ),
                ),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(Color(0x26FFFFFF), Color.Transparent, Color(0x18000000)))),
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp, vertical = 20.dp)
                    .clip(RoundedCornerShape(36.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFFC89B72), Color(0xFFE5C49F), Color(0xFFBE8F63)),
                            start = Offset(0f, 180f),
                            end = Offset(1200f, 780f),
                        ),
                    ),
            )

            SpineLayer(visualProgress = visualProgress, active = direction != null)

            DestinationPageLayer(
                modifier = Modifier.fillMaxSize().padding(horizontal = 28.dp, vertical = 26.dp),
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
                        .padding(horizontal = 28.dp, vertical = 26.dp)
                        .graphicsLayer {
                            transformOrigin = if (draggingToNext) TransformOrigin(0f, 0.5f) else TransformOrigin(1f, 0.5f)
                            rotationY = turnRotation * 0.92f
                            cameraDistance = 30f * density
                        },
                    tint = tint,
                    progress = visualProgress,
                    direction = direction,
                )
            }

            ActivePageLayer(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 28.dp, vertical = 26.dp)
                    .graphicsLayer {
                        transformOrigin = if (draggingToNext) TransformOrigin(0f, 0.5f) else TransformOrigin(1f, 0.5f)
                        rotationY = turnRotation
                        translationX = when {
                            draggingToNext -> -(visualProgress * 22f + visualProgress * visualProgress * 42f)
                            draggingToPrevious -> visualProgress * 22f + visualProgress * visualProgress * 42f
                            else -> 0f
                        }
                        cameraDistance = 30f * density
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
                onRenameCustomItem = onRenameCustomItem,
                onAddToSchedule = onAddToSchedule,
                pendingCommand = diaryCommand,
                onCommand = { diaryCommand = it },
                contentMode = contentMode,
                onContentModeChange = { contentMode = it },
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
                                        Color.Black.copy(alpha = (0.10f + visualProgress * visualProgress * 0.34f).coerceAtMost(0.50f)),
                                        Color(0x22000000),
                                        Color.Transparent,
                                    ),
                                )
                            } else {
                                Brush.horizontalGradient(
                                    listOf(
                                        Color.Transparent,
                                        Color(0x22000000),
                                        Color.Black.copy(alpha = (0.10f + visualProgress * visualProgress * 0.34f).coerceAtMost(0.50f)),
                                    ),
                                )
                            },
                        ),
                )

                Box(
                    modifier = Modifier
                        .align(if (draggingToNext) Alignment.CenterEnd else Alignment.CenterStart)
                        .width((2f + visualProgress * 6f).dp)
                        .fillMaxHeight()
                        .background(
                            Brush.horizontalGradient(
                                if (draggingToNext) {
                                    listOf(
                                        Color.White.copy(alpha = (0.10f + visualProgress * 0.22f).coerceAtMost(0.28f)),
                                        Color.Black.copy(alpha = (0.06f + visualProgress * 0.16f).coerceAtMost(0.20f)),
                                    )
                                } else {
                                    listOf(
                                        Color.Black.copy(alpha = (0.06f + visualProgress * 0.16f).coerceAtMost(0.20f)),
                                        Color.White.copy(alpha = (0.10f + visualProgress * 0.22f).coerceAtMost(0.28f)),
                                    )
                                },
                            ),
                        ),
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .width(60.dp)
                    .fillMaxHeight()
                    .clickable(enabled = canTurnPrevious && turnEnabled) {
                        direction = TurnDirection.PREVIOUS
                        scope.launch { progress.snapTo(initialEdgeTapProgress()) }
                        settle(TurnReleaseResult.CompletePrevious)
                    },
            )

            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .width(60.dp)
                    .fillMaxHeight()
                    .clickable(enabled = canTurnNext && turnEnabled) {
                        direction = TurnDirection.NEXT
                        scope.launch { progress.snapTo(initialEdgeTapProgress()) }
                        settle(TurnReleaseResult.CompleteNext)
                    },
            )
        }
    }
}

@Composable
private fun BoxScope.SpineLayer(
    visualProgress: Float,
    active: Boolean,
) {
    Box(
        modifier = Modifier
            .align(Alignment.Center)
            .width(18.dp)
            .fillMaxHeight()
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color(0xFF8E6243).copy(alpha = if (active) (0.76f + visualProgress * 0.18f).coerceAtMost(0.95f) else 0.76f),
                        Color(0xFFF4E2CF),
                        Color(0xFF8E6243).copy(alpha = if (active) (0.76f + visualProgress * 0.18f).coerceAtMost(0.95f) else 0.76f),
                    ),
                ),
            ),
    )

    if (active) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .width((14f + visualProgress * 8f).dp)
                .fillMaxHeight()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color.Black.copy(alpha = (0.06f + visualProgress * 0.14f).coerceAtMost(0.18f)),
                            Color.Transparent,
                            Color.Black.copy(alpha = (0.06f + visualProgress * 0.14f).coerceAtMost(0.18f)),
                        ),
                    ),
                ),
        )
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
    val pageSubtitle = destinationPageSubtitle(direction, subtitle)
    val pageNumber = when {
        page == null -> ""
        direction == TurnDirection.NEXT -> "${pageIndex + 2} / $pageCount"
        direction == TurnDirection.PREVIOUS -> "$pageIndex / $pageCount"
        else -> "${pageIndex + 1} / $pageCount"
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp, 28.dp, 22.dp, 22.dp))
            .background(Brush.horizontalGradient(listOf(Color(0xFFF5E9D8), Color(0xFFFFFCF7), Color(0xFFF0DECA))))
            .padding(horizontal = 30.dp, vertical = 28.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color(0x14A07856), Color.Transparent, Color(0x10C9AA87)))),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = (0.06f + visualTurnProgress(revealProgress) * 0.94f).coerceIn(0.06f, 1f) },
        ) {
            PageHeaderLine(bookTitle = bookTitle, subtitle = pageSubtitle, tint = tint.copy(alpha = 0.72f), savedText = pageNumber.ifBlank { "翻页预览" })
            Spacer(Modifier.height(18.dp))
            Text(pageTitle, style = MaterialTheme.typography.headlineSmall, color = Color(0xFF2D261F))
            Spacer(Modifier.height(14.dp))
            Text(
                condensedPreviewText(page?.let(::pagePreviewText) ?: subtitle, maxLength = 92),
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF6A5D4F),
            )
            Spacer(Modifier.weight(1f))
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
            .clip(RoundedCornerShape(24.dp, 24.dp, 28.dp, 28.dp))
            .background(
                Brush.horizontalGradient(
                    if (direction == TurnDirection.NEXT) {
                        listOf(Color(0xFFE0CFB9), Color(0xFFF4EBDF), Color(0xFFFFFCF7))
                    } else {
                        listOf(Color(0xFFFFFCF7), Color(0xFFF4EBDF), Color(0xFFE0CFB9))
                    },
                ),
            )
            .padding(horizontal = 28.dp, vertical = 26.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0x10FFFFFF), Color.Transparent))),
        )

        Column(modifier = Modifier.fillMaxSize()) {
            PageHeaderLine(bookTitle = "纸页背面", subtitle = "翻页进行中", tint = tint.copy(alpha = 0.58f), savedText = "当前进度 ${(progress * 100).toInt()}%")
            Spacer(Modifier.weight(1f))
        }

        Box(
            modifier = Modifier
                .align(if (direction == TurnDirection.NEXT) Alignment.CenterStart else Alignment.CenterEnd)
                .width((12f + progress * 26f).dp)
                .fillMaxHeight()
                .background(
                    Brush.horizontalGradient(
                        if (direction == TurnDirection.NEXT) {
                            listOf(
                                Color.Black.copy(alpha = (0.08f + progress * 0.16f).coerceAtMost(0.22f)),
                                Color.White.copy(alpha = (0.10f + progress * 0.18f).coerceAtMost(0.24f)),
                                Color.Transparent,
                            )
                        } else {
                            listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = (0.10f + progress * 0.18f).coerceAtMost(0.24f)),
                                Color.Black.copy(alpha = (0.08f + progress * 0.16f).coerceAtMost(0.22f)),
                            )
                        },
                    ),
                ),
        )

        Box(
            modifier = Modifier
                .align(if (direction == TurnDirection.NEXT) Alignment.CenterStart else Alignment.CenterEnd)
                .width((3f + progress * 4f).dp)
                .fillMaxHeight()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = (0.12f + progress * 0.20f).coerceAtMost(0.26f)),
                            Color.Transparent,
                            Color.Black.copy(alpha = (0.06f + progress * 0.12f).coerceAtMost(0.16f)),
                        ),
                    ),
                ),
        )
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
    onRenameCustomItem: (String, String) -> Unit,
    onAddToSchedule: (String, Int) -> Unit,
    pendingCommand: RichEditorCommand?,
    onCommand: (RichEditorCommand) -> Unit,
    contentMode: PageContentMode,
    onContentModeChange: (PageContentMode) -> Unit,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(26.dp, 26.dp, 30.dp, 30.dp))
            .background(Brush.horizontalGradient(listOf(Color(0xFFFFFEFB), Color(0xFFFBF4EB), Color(0xFFECD9C0))))
            .padding(horizontal = 28.dp, vertical = 26.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color(0x0D9D7456), Color.Transparent, Color(0x12986B49)))),
        )

        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            PageHeaderLine(
                bookTitle = bookTitle,
                subtitle = subtitle,
                tint = tint,
                savedText = if (isSaved) "已保存到我的书" else "保存为我的书",
                onSavedClick = onToggleSaved,
            )
            Spacer(Modifier.height(14.dp))
            Text(text = page.title, style = MaterialTheme.typography.headlineSmall, color = Color(0xFF2D261F))
            Spacer(Modifier.height(16.dp))
            when (page) {
                is TargetPage -> EditableBulletPage(page.title, page.items, customPageItems, tint, "添加你的目标", isChecked, onToggleChecked, onAddCustomItem, onRemoveCustomItem, onRenameCustomItem, onAddToSchedule, contentMode, onContentModeChange)
                is SchedulePage -> EditableBulletPage(page.title, page.items, customPageItems, tint.copy(alpha = 0.74f), "添加你的日程任务", isChecked, onToggleChecked, onAddCustomItem, onRemoveCustomItem, onRenameCustomItem, onAddToSchedule, contentMode, onContentModeChange)
                is PlanPage -> EditableBulletPage(page.title, page.items, customPageItems, Color(0xFFB88A58), "添加你的计划", isChecked, onToggleChecked, onAddCustomItem, onRemoveCustomItem, onRenameCustomItem, onAddToSchedule, contentMode, onContentModeChange)
                is DiaryPage -> DiarySection(page.title, page.prompt, tint, diaryDraft, pendingCommand, onCommand, onDiaryChange, contentMode, onContentModeChange)
            }
            Spacer(Modifier.height(24.dp))
            Text(text = "${pageIndex + 1} / $pageCount", style = MaterialTheme.typography.labelMedium, color = Color(0xFF7A7065))
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
    onRenameCustomItem: (String, String) -> Unit,
    onAddToSchedule: (String, Int) -> Unit,
    contentMode: PageContentMode,
    onContentModeChange: (PageContentMode) -> Unit,
) {
    var newItem by remember(pageTitle) { mutableStateOf("") }
    var editedBaseItems by remember(pageTitle, baseItems) { mutableStateOf(baseItems) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        editedBaseItems.forEach { item ->
            ActionBulletRow(
                pageTitle = pageTitle,
                item = item,
                tint = tint,
                checked = isChecked(pageTitle, item),
                removable = false,
                onToggleChecked = onToggleChecked,
                onAddToSchedule = onAddToSchedule,
                onRemoveCustomItem = onRemoveCustomItem,
                onRenameCustomItem = onRenameCustomItem,
                contentMode = contentMode,
                onContentModeChange = onContentModeChange,
                onRenameDisplayedItem = { oldItem, replacement ->
                    editedBaseItems = renameDisplayedChecklistItem(editedBaseItems, oldItem, replacement)
                },
            )
        }
        if (customItems.isNotEmpty()) {
            Text("我的内容", style = MaterialTheme.typography.titleSmall, color = Color(0xFF5E4837))
            customItems.forEach { item ->
                ActionBulletRow(
                    pageTitle = pageTitle,
                    item = item,
                    tint = tint,
                    checked = isChecked(pageTitle, item),
                    removable = true,
                    onToggleChecked = onToggleChecked,
                    onAddToSchedule = onAddToSchedule,
                    onRemoveCustomItem = onRemoveCustomItem,
                    onRenameCustomItem = onRenameCustomItem,
                    contentMode = contentMode,
                    onContentModeChange = onContentModeChange,
                    onRenameDisplayedItem = { _, _ -> },
                )
            }
        }
        PaperNoteCard {
            Text("先浏览，再点条目编辑。新增内容仍然在这里完成。", style = MaterialTheme.typography.bodySmall, color = Color(0xFF7B6B5A))
            OutlinedTextField(
                value = newItem,
                onValueChange = { newItem = it },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                label = { Text(inputLabel) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            )
            TextButton(onClick = {
                onAddCustomItem(newItem)
                newItem = ""
            }) {
                Text("保存")
            }
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
    onRenameCustomItem: (String, String) -> Unit,
    contentMode: PageContentMode,
    onContentModeChange: (PageContentMode) -> Unit,
    onRenameDisplayedItem: (String, String) -> Unit,
) {
    val activeEdit = contentMode as? PageContentMode.EditingChecklistItem
    val isEditingThisRow = activeEdit?.title == pageTitle && activeEdit.item == item
    var draft by remember(pageTitle, item) { mutableStateOf(item) }

    PaperNoteCard {
        if (isEditingThisRow) {
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                label = { Text("编辑内容") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TextButton(onClick = {
                    if (removable) onRenameCustomItem(item, draft) else onRenameDisplayedItem(item, draft)
                    onContentModeChange(PageContentMode.Browsing)
                }) { Text("保存") }
                TextButton(onClick = {
                    draft = item
                    onContentModeChange(PageContentMode.Browsing)
                }) { Text("取消") }
                if (removable) {
                    TextButton(onClick = {
                        onRemoveCustomItem(item)
                        onContentModeChange(PageContentMode.Browsing)
                    }) { Text("删除") }
                }
            }
        } else {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier
                        .clickable { onToggleChecked(pageTitle, item) }
                        .fillMaxWidth(0.62f),
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
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF8F684F),
                    modifier = Modifier
                        .clip(RoundedCornerShape(99.dp))
                        .background(Color(0x1A8F684F))
                        .clickable { onAddToSchedule(item, LocalDate.now().dayOfMonth) }
                        .padding(horizontal = 8.dp, vertical = 7.dp),
                )
                Text(
                    text = "编辑",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF8F684F),
                    modifier = Modifier
                        .clickable { onContentModeChange(PageContentMode.EditingChecklistItem(pageTitle, item)) }
                        .padding(top = 7.dp),
                )
            }
        }
    }
}

@Composable
private fun completedTextStyle(completed: Boolean): TextStyle =
    MaterialTheme.typography.bodyMedium.copy(
        textDecoration = if (completed) TextDecoration.LineThrough else TextDecoration.None,
    )

@Composable
private fun DiarySection(
    title: String,
    prompt: String,
    tint: Color,
    diaryDraft: String,
    pendingCommand: RichEditorCommand?,
    onCommand: (RichEditorCommand) -> Unit,
    onDiaryChange: (String) -> Unit,
    contentMode: PageContentMode,
    onContentModeChange: (PageContentMode) -> Unit,
) {
    val editingDiary = contentMode as? PageContentMode.EditingDiary

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(text = prompt, style = MaterialTheme.typography.titleSmall, color = Color(0xFF342C24))
        if (editingDiary?.title == title) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DiaryToolChip("加粗") { onCommand(RichEditorCommand("bold")) }
                DiaryToolChip("标题") { onCommand(RichEditorCommand("formatBlock", "<h2>")) }
                DiaryToolChip("列表") { onCommand(RichEditorCommand("insertUnorderedList")) }
                DiaryToolChip("引用") { onCommand(RichEditorCommand("formatBlock", "<blockquote>")) }
                DiaryToolChip("完成") { onContentModeChange(PageContentMode.Browsing) }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFFFFFBF5))
                    .padding(18.dp),
            ) {
                InlineRichDiaryEditor(
                    html = diaryDraft,
                    placeholder = "写下今天的记录、感受或下一步。",
                    modifier = Modifier.fillMaxSize(),
                    pendingCommand = pendingCommand,
                    onHtmlChange = onDiaryChange,
                )
            }
        } else {
            PaperNoteCard(
                modifier = Modifier.clickable {
                    onContentModeChange(pageContentModeForTap(DiaryPage(title, prompt)))
                },
            ) {
                Text(
                    text = condensedPreviewText(
                        if (diaryDraft.isBlank()) "点击这里开始编辑日记，默认先展示内容。" else diaryDraft,
                        maxLength = 180,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (diaryDraft.isBlank()) Color(0xFF8E806F) else Color(0xFF342C24),
                )
            }
        }
        Text(
            text = "纸页内容会保存在本地，不依赖服务器。",
            style = MaterialTheme.typography.bodySmall,
            color = tint.copy(alpha = 0.72f),
        )
    }
}

@Composable
private fun PaperNoteCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFFFFFCF6), Color(0xFFFAF2E8), Color(0xFFF5E7D6))))
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0x14A17856)))
        content()
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0x0EA17856)))
    }
}

@Composable
private fun PageHeaderLine(
    bookTitle: String,
    subtitle: String,
    tint: Color,
    savedText: String,
    onSavedClick: (() -> Unit)? = null,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(99.dp)).background(tint))
                Spacer(Modifier.width(8.dp))
                Text(bookTitle, style = MaterialTheme.typography.titleMedium, color = Color(0xFF342C24))
            }
            Text(
                text = savedText,
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF8B7660),
                modifier = if (onSavedClick != null) Modifier.clickable(onClick = onSavedClick) else Modifier,
            )
        }
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color(0xFF7B6A59))
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0x1A9C7C5C)))
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

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun InlineRichDiaryEditor(
    html: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    pendingCommand: RichEditorCommand? = null,
    onHtmlChange: (String) -> Unit,
) {
    var initialHtmlLoaded by remember { mutableStateOf(false) }
    var lastAppliedHtml by remember { mutableStateOf<String?>(null) }
    var appliedCommandCount by remember { mutableIntStateOf(0) }
    val bridge = remember {
        object {
            @JavascriptInterface
            fun onChange(value: String) {
                lastAppliedHtml = value
                onHtmlChange(value)
            }
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                isVerticalScrollBarEnabled = false
                addJavascriptInterface(bridge, "AndroidEditor")
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        view?.evaluateJavascript("RE.setPlaceholder(${placeholder.asJsLiteral()});", null)
                        view?.evaluateJavascript("RE.setHtml(${html.asJsLiteral()});", null)
                        lastAppliedHtml = html
                        initialHtmlLoaded = true
                    }
                }
                loadUrl("file:///android_asset/editor.html")
            }
        },
        update = { webView ->
            webView.evaluateJavascript("RE.setPlaceholder(${placeholder.asJsLiteral()});", null)

            if (initialHtmlLoaded && html != lastAppliedHtml) {
                webView.evaluateJavascript("RE.setHtml(${html.asJsLiteral()});", null)
                lastAppliedHtml = html
            }

            if (pendingCommand != null) {
                val commandKey = pendingCommand.hashCode()
                if (appliedCommandCount != commandKey) {
                    val value = pendingCommand.value?.asJsLiteral() ?: "null"
                    webView.evaluateJavascript("RE.command(${pendingCommand.name.asJsLiteral()}, $value);", null)
                    appliedCommandCount = commandKey
                }
            }
        },
    )
}

private fun String.asJsLiteral(): String =
    "'" + replace("\\", "\\\\").replace("'", "\\'").replace("\n", "\\n").replace("\r", "") + "'"
