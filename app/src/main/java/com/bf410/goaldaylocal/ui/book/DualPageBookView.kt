package com.bf410.goaldaylocal.ui.book

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.input.pointer.util.addPointerInputChange
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.app.Activity
import android.graphics.Rect
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.platform.LocalContext
import android.os.Build
import androidx.compose.ui.layout.positionInWindow
import com.bf410.goaldaylocal.data.BookPage
import com.bf410.goaldaylocal.data.DiaryPage
import com.bf410.goaldaylocal.data.SchedulePage
import com.bf410.goaldaylocal.data.TopicBook
import com.bf410.goaldaylocal.ui.replica.GoaldayDesign
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import kotlin.math.abs

/**
 * 双页展开手账视图。
 *
 * 对照原版 BookViewExampleKt.CalendarBookView + BaseBookViewKt：
 * - 书尺寸 fillMaxSize，不再使用 screenWidth*0.47 的窄条
 * - 左右两页同时可见：左页=日程，右页=日记
 * - 页面背景纯白、左平右圆圆角(10dp)、10dp 阴影(#FFC5BBB6)
 * - 翻页时当前页绕书脊做 180° rotationY，transformOrigin 在书脊侧
 * - 子元素（列表、勾选框）仍可交互，翻页手势在全宽区域检测
 */
/** 书芯按周翻页的最大范围（约 ±5 年） */
private const val MaxWeekOffset = 260

/** 记录 Tab 与书内右页共用的日记存储 bookId */
private const val DiaryStoreBookId = "diary"

@Composable
fun DualPageBookView(
    book: TopicBook,
    currentPage: BookPage,
    uiState: BookUiState,
    viewModel: BookViewModel,
    onBack: () -> Unit = {},
    onOpenDate: (LocalDate, Boolean) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current.density

    // 原版行为：书芯按周翻页（对照 CircularCalendarPageState），默认打开当前周
    val today = LocalDate.now()
    val currentWeekMonday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    var weekOffset by remember { mutableIntStateOf(0) }
    val spreadMonday = currentWeekMonday.plusWeeks(weekOffset.toLong())

    val schedulePage = book.pages.filterIsInstance<SchedulePage>().firstOrNull()
        ?: SchedulePage("日程页", emptyList())
    val diaryPage = book.pages.filterIsInstance<DiaryPage>().firstOrNull()
        ?: DiaryPage("日记页", "写下这一页最重要的记录。")
    val scheduleIndex = book.pages.indexOfFirst { it is SchedulePage }.coerceAtLeast(0)
    val diaryIndex = book.pages.indexOfFirst { it is DiaryPage }.coerceAtLeast(0)

    val weekStartDate = spreadMonday
    val diaryDate = spreadMonday
    val pairMonth = spreadMonday.monthValue

    // 翻页背面：左页背面 = 上一周日程，右页背面 = 下周周一日记
    val prevWeekStartDate = spreadMonday.minusWeeks(1)
    val nextDiaryDate = spreadMonday.plusWeeks(1)

    // 右页日记内容按日期读取（记录 Tab 与书内共用同一存储）
    val diaryStore = remember { com.bf410.goaldaylocal.data.LocalStateStore(com.tencent.mmkv.MMKV.defaultMMKV()) }
    val spreadDiaryDraft = remember(diaryDate) { diaryStore.diaryText(DiaryStoreBookId, diaryDate.toString()) }
    val nextDiaryDraft = remember(nextDiaryDate) { diaryStore.diaryText(DiaryStoreBookId, nextDiaryDate.toString()) }

    var showBookShelf by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val progress = remember { Animatable(0f) }
    var turnDirection by remember { mutableStateOf<TurnDirection?>(null) }
    var isAnimating by remember { mutableStateOf(false) }
    var pageWidthPx by remember { mutableFloatStateOf(1f) }

    // 把书页左右边缘排除在系统返回手势之外，确保全宽翻页热区可用
    val view = LocalView.current
    var bookBounds by remember { mutableStateOf<Rect?>(null) }
    DisposableEffect(bookBounds) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return@DisposableEffect onDispose { }
        }
        val bounds = bookBounds
        val decor = (view.context as? Activity)?.window?.decorView
        if (decor != null && bounds != null && bounds.width() > 0 && bounds.height() > 0) {
            val edgePx = with(view.context.resources.displayMetrics) { (56f * density).toInt() }
            val leftRect = Rect(bounds.left, bounds.top, bounds.left + edgePx, bounds.bottom)
            val rightRect = Rect(bounds.right - edgePx, bounds.top, bounds.right, bounds.bottom)
            decor.systemGestureExclusionRects = listOf(leftRect, rightRect)
        }
        onDispose {
            decor?.systemGestureExclusionRects = emptyList()
        }
    }

    // 对照原版：动画时长自适应，progress>0.5 时 100ms，否则 300ms
    fun settle(complete: Boolean) {
        if (isAnimating) return
        scope.launch {
            isAnimating = true
            val currentProgress = progress.value
            if (complete) {
                progress.animateTo(1f, tween(if (currentProgress > 0.5f) 100 else 300))
                weekOffset = when (turnDirection) {
                    TurnDirection.NEXT -> (weekOffset + 1).coerceAtMost(MaxWeekOffset)
                    TurnDirection.PREVIOUS -> (weekOffset - 1).coerceAtLeast(-MaxWeekOffset)
                    null -> weekOffset
                }
                progress.snapTo(0f)
            } else {
                progress.animateTo(0f, tween(if (currentProgress > 0.5f) 100 else 300))
            }
            turnDirection = null
            // 对照原版：动画结束后 10ms 再重新启用手势
            kotlinx.coroutines.delay(10)
            isAnimating = false
        }
    }

    val headerMonth = "${pairMonth}月"
    val shellColor = GoaldayDesign.BookBoardLight
    val shadowColor = Color(0xFFC5BBB6)
    // 布纹贴图：取原版封面左上干净区域（无书脊线/年份字）
    val context = LocalContext.current
    val fabricImage = remember {
        runCatching {
            val src = android.graphics.BitmapFactory.decodeResource(
                context.resources,
                com.bf410.goaldaylocal.R.drawable.book_cover_fabric,
            )
            android.graphics.Bitmap.createBitmap(
                src,
                (src.width * 0.10f).toInt(),
                (src.height * 0.06f).toInt(),
                (src.width * 0.40f).toInt(),
                (src.height * 0.35f).toInt(),
            ).asImageBitmap()
        }.getOrNull()
    }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        val configuration = LocalConfiguration.current
        val screenWidthDp = configuration.screenWidthDp.dp
        // 对照原版实机测量（1080x2400 模拟器截图）：
        // 摊开书整体宽 ≈ 0.82×屏宽（321dp/393dp），高 ≈ 宽×1.22（392dp），
        // 页面为窄长比例（Hobonichi Weeks 手账），不是方形。
        // 原版书顶 ≈ 屏高 25%（216dp），月份标题中心 ≈ 屏高 17.5%（155dp）。
        val bookWidth = screenWidthDp * 0.82f
        val bookHeight = bookWidth * 1.22f

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // 顶部月份标题：对照原版，标题与书之间留出明显空隙
            Spacer(Modifier.height(128.dp))
            Text(
                text = headerMonth,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = GoaldayDesign.InkPrimary,
            )
            Spacer(Modifier.height(28.dp))

            // 书壳
            Box(
                modifier = Modifier
                    .width(bookWidth)
                    .height(bookHeight)
                    .padding(horizontal = 4.dp, vertical = 4.dp)
                    .onGloballyPositioned {
                        val pos = it.positionInWindow()
                        bookBounds = Rect(
                            pos.x.toInt(),
                            pos.y.toInt(),
                            (pos.x + it.size.width).toInt(),
                            (pos.y + it.size.height).toInt(),
                        )
                    }
                    .shadow(
                        elevation = 10.dp,
                        shape = RoundedCornerShape(0.dp, 10.dp, 10.dp, 0.dp),
                        clip = false,
                        ambientColor = shadowColor,
                        spotColor = shadowColor,
                    )
                    .clip(RoundedCornerShape(0.dp, 10.dp, 10.dp, 0.dp))
                    .drawWithContent {
                        // 书壳布纹：使用原版逆向提取的 book_cover_fabric 贴图平铺（避开左上书脊与年份字）
                        fabricImage?.let { bmp ->
                            val tileW = bmp.width
                            val tileH = bmp.height
                            var y = 0
                            while (y < size.height.toInt()) {
                                var x = 0
                                while (x < size.width.toInt()) {
                                    drawImage(
                                        image = bmp,
                                        srcOffset = IntOffset(0, 0),
                                        srcSize = IntSize(tileW, tileH),
                                        dstOffset = IntOffset(x, y),
                                        dstSize = IntSize(tileW, tileH),
                                    )
                                    x += tileW
                                }
                                y += tileH
                            }
                        } ?: drawRect(shellColor)
                        drawContent()
                    }
                    .drawWithContent {
                        // 右侧书页厚度堆叠效果：模拟一本真实书的页缘
                        val stackWidth = 10.dp.toPx()
                        val stackCount = 6
                        for (i in 0 until stackCount) {
                            val x = size.width - stackWidth + (i * stackWidth / stackCount)
                            val alpha = 0.06f + i * 0.02f
                            drawLine(
                                color = shadowColor.copy(alpha = alpha),
                                start = Offset(x, 0f),
                                end = Offset(x, size.height),
                                strokeWidth = 1.2f,
                            )
                        }
                        drawContent()
                    }
                    .padding(start = 6.dp, end = 8.dp, top = 10.dp, bottom = 6.dp)
                    .pointerInput(weekOffset) {
                        val width = size.width.toFloat()
                        pageWidthPx = width
                        awaitPointerEventScope {
                            while (true) {
                                val down = awaitFirstDown(requireUnconsumed = false)
                                if (isAnimating) {
                                    down.consume()
                                    continue
                                }
                                val startX = down.position.x
                                val velocityTracker = VelocityTracker()
                                velocityTracker.resetTracking()
                                velocityTracker.addPointerInputChange(down)

                                var turnDir: TurnDirection? = null
                                var finished = false
                                while (!finished) {
                                    val event = awaitPointerEvent()
                                    val change = event.changes.firstOrNull { it.id == down.id } ?: continue
                                    if (change.pressed.not()) {
                                        val velocity = velocityTracker.calculateVelocity().x
                                        val complete = when (turnDir) {
                                            TurnDirection.NEXT -> progress.value > 0.25f || velocity < -800
                                            TurnDirection.PREVIOUS -> progress.value > 0.25f || velocity > 800
                                            null -> false
                                        }
                                        settle(complete)
                                        finished = true
                                        break
                                    }
                                    val dx = change.positionChange().x
                                    velocityTracker.addPointerInputChange(change)
                                    if (turnDir == null && abs(dx) > 4f) {
                                        turnDir = if (dx < 0) TurnDirection.NEXT else TurnDirection.PREVIOUS
                                        val can = when (turnDir) {
                                            TurnDirection.NEXT -> weekOffset < MaxWeekOffset
                                            TurnDirection.PREVIOUS -> weekOffset > -MaxWeekOffset
                                            null -> false
                                        }
                                        if (can) {
                                            turnDirection = turnDir
                                            scope.launch { progress.snapTo(0f) }
                                        } else {
                                            finished = true
                                            break
                                        }
                                    }
                                    if (turnDir != null) {
                                        val rawProgress = abs(change.position.x - startX) / (width * 0.45f)
                                        val newProgress = rawProgress.coerceIn(0f, 1f)
                                        scope.launch { progress.snapTo(newProgress) }
                                    }
                                    change.consume()
                                }
                            }
                        }
                    },
            ) {
                // 书页容器：左右页 + 中央书脊阴影
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(0.dp),
                ) {
                    // 左页：日程
                    HandbookPage(
                        modifier = Modifier.weight(1f),
                        isLeft = true,
                        progress = progress.value,
                        direction = turnDirection,
                        onTap = { onOpenDate(weekStartDate, true) },
                        content = {
                            InBookSchedulePreview(
                                modifier = Modifier.fillMaxSize(),
                                page = schedulePage,
                                pageIndex = scheduleIndex,
                                pageCount = book.pages.size,
                                schedulePreviewEntries = uiState.schedulePreviewEntries,
                                isChecked = { pageTitle, title ->
                                    uiState.todayCompletedItems.contains(title)
                                },
                                tint = book.color,
                                turnProgress = progress.value,
                                turnDirection = turnDirection,
                                handbookMode = true,
                                weekStartDate = weekStartDate,
                            )
                        },
                        backContent = {
                            InBookSchedulePreview(
                                modifier = Modifier.fillMaxSize(),
                                page = schedulePage,
                                pageIndex = scheduleIndex,
                                pageCount = book.pages.size,
                                schedulePreviewEntries = uiState.schedulePreviewEntries,
                                isChecked = { pageTitle, title ->
                                    uiState.todayCompletedItems.contains(title)
                                },
                                tint = book.color,
                                turnProgress = progress.value,
                                turnDirection = turnDirection,
                                handbookMode = true,
                                weekStartDate = prevWeekStartDate,
                            )
                        },
                    )

                    // 书脊：对照原版 BaseBookViewKt，中央装订阴影
                    Box(
                        modifier = Modifier
                            .width(8.dp)
                            .fillMaxHeight()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFFE8DED7),
                                        Color(0xFFD8CCC4),
                                        Color(0xFFE8DED7),
                                    ),
                                ),
                            ),
                    )

                    // 右页：日记
                    HandbookPage(
                        modifier = Modifier.weight(1f),
                        isLeft = false,
                        progress = progress.value,
                        direction = turnDirection,
                        onTap = { onOpenDate(diaryDate, false) },
                        content = {
                            InBookDiaryPreview(
                                modifier = Modifier.fillMaxSize(),
                                page = diaryPage,
                                pageIndex = diaryIndex,
                                pageCount = book.pages.size,
                                diaryDraft = spreadDiaryDraft,
                                tint = book.color,
                                turnProgress = progress.value,
                                turnDirection = turnDirection,
                                handbookMode = true,
                                diaryDate = diaryDate,
                                onAddImage = {},
                            )
                        },
                        backContent = {
                            InBookDiaryPreview(
                                modifier = Modifier.fillMaxSize(),
                                page = diaryPage,
                                pageIndex = diaryIndex,
                                pageCount = book.pages.size,
                                diaryDraft = nextDiaryDraft,
                                tint = book.color,
                                turnProgress = progress.value,
                                turnDirection = turnDirection,
                                handbookMode = true,
                                diaryDate = nextDiaryDate,
                                onAddImage = {},
                            )
                        },
                    )
                }
            }
        }

        // 底部栏：对照原版 BookViewExampleKt
        // 左侧导出图标 | 中间 2026 ▼ | 右侧返回
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(52.dp)
                .background(shellColor)
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(40.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "导出",
                    modifier = Modifier.size(22.dp),
                    tint = GoaldayDesign.InkPrimary,
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.clickableNoRipple { showBookShelf = true },
            ) {
                Text(
                    text = "${spreadMonday.year}",
                    fontSize = 16.sp,
                    color = GoaldayDesign.InkPrimary,
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "切换年份",
                    modifier = Modifier.size(18.dp),
                    tint = GoaldayDesign.InkPrimary,
                )
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clickableNoRipple { onBack() },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "返回",
                    fontSize = 14.sp,
                    color = GoaldayDesign.InkPrimary,
                )
            }
        }

        // 编辑本周：书内快捷入口 → 跳主界面周 Tab 定位本周（不用退出再找日期）
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 64.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xB31C1C1C))
                .clickableNoRipple { onOpenDate(weekStartDate, true) }
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Text(
                text = "✎ 编辑本周",
                fontSize = 13.sp,
                color = Color.White,
            )
        }

        // 书架底部弹层（对照原版 BookShelfBottomDialog：横排布纹封面选年份换书）
        if (showBookShelf) {
            BookShelfSheet(
                fabricImage = fabricImage,
                currentYear = spreadMonday.year,
                onPickYear = { year ->
                    val targetMonday = LocalDate.of(year, 1, 1)
                        .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                    weekOffset = java.time.temporal.ChronoUnit.WEEKS
                        .between(currentWeekMonday, targetMonday).toInt()
                    showBookShelf = false
                },
                onDismiss = { showBookShelf = false },
            )
        }
    }
}

/** 书架弹层：横排布纹封面 + 年份，点击切换到对应年份的书（对照原版截图） */
@Composable
private fun BookShelfSheet(
    fabricImage: androidx.compose.ui.graphics.ImageBitmap?,
    currentYear: Int,
    onPickYear: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    androidx.compose.material3.Surface(color = Color.White) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(top = 14.dp, bottom = 26.dp),
        ) {
            Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Text(
                    "书架",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GoaldayDesign.InkPrimary,
                    modifier = Modifier.align(Alignment.Center),
                )
                Text(
                    "取消",
                    fontSize = 15.sp,
                    color = Color(0xFF3875F6),
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .clickableNoRipple { onDismiss() },
                )
            }
            Spacer(Modifier.height(16.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                val years = listOf(currentYear, currentYear - 1, currentYear - 2, currentYear - 3).sortedDescending()
                years.forEach { year ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(width = 58.dp, height = 84.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(GoaldayDesign.BookBoardLight)
                                .clickableNoRipple { onPickYear(year) },
                        ) {
                            fabricImage?.let { bmp ->
                                Image(
                                    bitmap = bmp,
                                    contentDescription = "${year}年封面",
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                    modifier = Modifier.matchParentSize(),
                                )
                            }
                            Text(
                                year.toString(),
                                fontSize = 11.sp,
                                color = Color(0xFF8B4A4A),
                                modifier = Modifier.align(Alignment.Center),
                            )
                        }
                        Text(
                            year.toString(),
                            fontSize = 14.sp,
                            color = GoaldayDesign.InkPrimary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HandbookPage(
    modifier: Modifier,
    isLeft: Boolean,
    progress: Float,
    direction: TurnDirection?,
    content: @Composable () -> Unit,
    backContent: @Composable () -> Unit = {},
    onTap: () -> Unit = {},
) {
    // 左页：左侧平、右侧圆；右页：左侧圆、右侧平
    val pageShape = RoundedCornerShape(
        topStart = if (isLeft) 0.dp else 10.dp,
        topEnd = if (isLeft) 10.dp else 0.dp,
        bottomEnd = if (isLeft) 10.dp else 0.dp,
        bottomStart = if (isLeft) 0.dp else 10.dp,
    )

    // 翻页时当前页绕书脊旋转
    val shouldRotate = when (direction) {
        TurnDirection.NEXT -> !isLeft
        TurnDirection.PREVIOUS -> isLeft
        null -> false
    }
    val rotationY = if (shouldRotate) {
        val sign = if (isLeft) 1f else -1f
        progress * 180f * sign
    } else 0f
    val absRotation = kotlin.math.abs(rotationY)
    // 正面可见条件：rotationY 绝对值 <= 90°；背面可见条件：> 90°
    val frontAlpha = if (absRotation <= 90f) 1f else 0f
    val backAlpha = if (absRotation > 90f) 1f else 0f

    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer {
                this.rotationY = rotationY
                this.cameraDistance = 60f * density
                this.transformOrigin = if (isLeft) {
                    TransformOrigin(1f, 0.5f)
                } else {
                    TransformOrigin(0f, 0.5f)
                }
            }
            // 页面边缘阴影：比书壳更淡，营造纸张厚度
            .shadow(
                elevation = 10.dp,
                shape = pageShape,
                clip = false,
                ambientColor = Color(0xFFC5BBB6),
                spotColor = Color(0xFFC5BBB6),
            )
            .clip(pageShape)
            .background(Color.White)
            .drawWithContent {
                drawContent()
                if (rotationY == 0f) {
                    // 书脊侧弯入阴影：宽 12dp，贴近中缝处最深（对照原版页面弯入装订处）
                    val spineWidth = 12.dp.toPx()
                    drawRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFFC5BBB6).copy(alpha = 0f),
                                Color(0xFFC5BBB6).copy(alpha = 0.14f),
                            ),
                            startX = if (isLeft) size.width - spineWidth else 0f,
                            endX = if (isLeft) size.width else spineWidth,
                        ),
                        size = Size(width = spineWidth, height = size.height),
                        topLeft = Offset(x = if (isLeft) size.width - spineWidth else 0f, y = 0f),
                    )
                    // 外侧书口：多层细线模拟纸页厚度堆叠
                    val stackWidth = 4.dp.toPx()
                    for (i in 0 until 3) {
                        val x = if (isLeft) i * stackWidth / 3 else size.width - stackWidth + (i * stackWidth / 3)
                        drawLine(
                            color = Color(0xFFC5BBB6).copy(alpha = 0.10f + i * 0.04f),
                            start = Offset(x, 0f),
                            end = Offset(x, size.height),
                            strokeWidth = 0.8f,
                        )
                    }
                }
            },
    ) {
        // 正面内容
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = frontAlpha },
        ) {
            content()
            if (onTap != {}) {
                // 原版书页为 NoTouchConstraintLayout：点任意页面区域即跳转主界面
                Box(
                    Modifier
                        .matchParentSize()
                        .clickableNoRipple { onTap() },
                )
            }
        }
        // 背面内容：反方向再旋转 180°，翻到背面时正向可读
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    alpha = backAlpha
                    this.rotationY = 180f
                },
        ) {
            backContent()
        }
    }
}

@Composable
private fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier = this.then(
    clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick,
    )
)
