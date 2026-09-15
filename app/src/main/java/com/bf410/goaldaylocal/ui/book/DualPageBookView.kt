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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path as vectorPath
import androidx.compose.material.icons.filled.KeyboardArrowDown
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
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
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
    // 对照原版实机：左页=当天（15|周二），右页=次日（16|周三），翻页按周平移
    val leftDiaryDate = today.plusWeeks(weekOffset.toLong())
    val diaryDate = leftDiaryDate.plusDays(1)
    val pairMonth = diaryDate.monthValue

    // 翻页背面：左页背面 = 上周当天，右页背面 = 下周次日
    val prevWeekStartDate = spreadMonday.minusWeeks(1)
    val nextDiaryDate = diaryDate.plusWeeks(1)

    // 日记内容按日期读取（记录 Tab 与书内共用同一存储）
    val diaryStore = remember { com.bf410.goaldaylocal.data.LocalStateStore(com.tencent.mmkv.MMKV.defaultMMKV()) }
    val spreadDiaryDraft = remember(diaryDate) { diaryStore.diaryText(DiaryStoreBookId, diaryDate.toString()) }
    val nextDiaryDraft = remember(nextDiaryDate) { diaryStore.diaryText(DiaryStoreBookId, nextDiaryDate.toString()) }
    val leftDiaryDraft = remember(leftDiaryDate) { diaryStore.diaryText(DiaryStoreBookId, leftDiaryDate.toString()) }
    val prevLeftDiaryDate = leftDiaryDate.minusWeeks(1)
    val prevLeftDiaryDraft = remember(prevLeftDiaryDate) { diaryStore.diaryText(DiaryStoreBookId, prevLeftDiaryDate.toString()) }

    var showBookShelf by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val progress = remember { Animatable(0f) }
    var turnDirection by remember { mutableStateOf<TurnDirection?>(null) }
    var isAnimating by remember { mutableStateOf(false) }
    var pageWidthPx by remember { mutableFloatStateOf(1f) }

    // 把书页左右边缘排除在系统返回手势之外，确保全宽翻页热区可用
    val view = LocalView.current

    // 书页全屏沉浸：对照原版 BookActivity，阅读态隐藏状态栏与导航栏，滑边临时唤出，离开书页恢复
    DisposableEffect(Unit) {
        val window = (view.context as? Activity)?.window
        val insetsController = window?.let { WindowInsetsControllerCompat(it, it.decorView) }
        insetsController?.hide(WindowInsetsCompat.Type.systemBars())
        insetsController?.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        onDispose {
            insetsController?.show(WindowInsetsCompat.Type.systemBars())
        }
    }
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
        val screenDensity = LocalDensity.current
        // 像素测量原版实机截图（904x2316, density 2.625）三层结构：
        // 布纹书壳(最外,宽≈0.937屏宽) → 层叠灰白书页边(中) → 当前纯白双页(最内)；
        // 当前双页白宽 = 书壳-46dp、双页高/白宽≈1.092；书垂直中心在屏幕 50%；标题中心在屏高 20.6%。
        val shellWidth = screenWidthDp * 0.937f
        val spreadWidth = shellWidth - 46.dp
        val spreadHeight = spreadWidth * 1.092f
        // 像素实测原版：白页上方书壳边 14.5dp、下方 24.5dp（下宽上窄的层叠纸边）
        val shellHeight = spreadHeight + 39.dp

        // 顶部月份标题：中心对齐原版屏高 20.6%（18sp 文字半高约 5dp）
        val titleTop = with(screenDensity) { configuration.screenHeightDp.dp * 0.206f - 5.dp }
        Text(
            text = headerMonth,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = GoaldayDesign.InkPrimary,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = titleTop),
        )

        // 书壳（最外层：布纹）。下内边比上内边宽 10dp，故整体下移 4.5dp 让内部白页仍垂直居中于屏幕
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = 4.5.dp)
                .width(shellWidth)
                .height(shellHeight)
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
                    elevation = 12.dp,
                    shape = RoundedCornerShape(14.dp),
                    clip = false,
                    ambientColor = shadowColor,
                    spotColor = shadowColor,
                )
                .clip(RoundedCornerShape(14.dp))
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
                // 中层：层叠书页灰白边（布纹内缩，模拟后面叠着的纸页）
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFFAF9F7)),
                ) {
                // 后页层：当前双页后方叠着的带横格线日程纸，被白页盖住中间、仅在内缩露出的四周可见（对照原版层叠页）
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .drawWithContent {
                            drawRect(Color(0xFFFAF9F7))
                            // 仅左页后方是带横格的日程页，右页后方是无横格日记页（对照原版：横格只在左侧露条可见）
                            val gap = 38.dp.toPx()
                            var yLine = 15.dp.toPx()
                            while (yLine < size.height) {
                                drawLine(
                                    color = Color(0xFFC5BBB6).copy(alpha = 0.13f),
                                    start = Offset(0f, yLine),
                                    end = Offset(size.width / 2f, yLine),
                                    strokeWidth = 1f,
                                )
                                yLine += gap
                            }
                        },
                )
                // 书页容器：左右页（当前纯白双页，内缩露出后方横格层叠页）
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 13.dp, end = 13.dp, top = 11.dp, bottom = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(0.dp),
                ) {
                    // 左页：日程
                    HandbookPage(
                        modifier = Modifier.weight(1f),
                        isLeft = true,
                        progress = progress.value,
                        direction = turnDirection,
                        onTap = { onOpenDate(leftDiaryDate, false) },
                        content = {
                            InBookDiaryPreview(
                                modifier = Modifier.fillMaxSize(),
                                page = diaryPage,
                                pageIndex = 0,
                                pageCount = book.pages.size,
                                diaryDraft = leftDiaryDraft,
                                tint = book.color,
                                turnProgress = progress.value,
                                turnDirection = turnDirection,
                                handbookMode = true,
                                diaryDate = leftDiaryDate,
                                onAddImage = {},
                                scheduleEntries = uiState.schedulePreviewEntries,
                            )
                        },
                        backContent = {
                            InBookDiaryPreview(
                                modifier = Modifier.fillMaxSize(),
                                page = diaryPage,
                                pageIndex = 0,
                                pageCount = book.pages.size,
                                diaryDraft = prevLeftDiaryDraft,
                                tint = book.color,
                                turnProgress = progress.value,
                                turnDirection = turnDirection,
                                handbookMode = true,
                                diaryDate = prevLeftDiaryDate,
                                onAddImage = {},
                                scheduleEntries = uiState.schedulePreviewEntries,
                            )
                        },
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
                                pageIndex = 1,
                                pageCount = book.pages.size,
                                diaryDraft = spreadDiaryDraft,
                                tint = book.color,
                                turnProgress = progress.value,
                                turnDirection = turnDirection,
                                handbookMode = true,
                                diaryDate = diaryDate,
                                onAddImage = {},
                                scheduleEntries = uiState.schedulePreviewEntries,
                            )
                        },
                        backContent = {
                            InBookDiaryPreview(
                                modifier = Modifier.fillMaxSize(),
                                page = diaryPage,
                                pageIndex = 1,
                                pageCount = book.pages.size,
                                diaryDraft = nextDiaryDraft,
                                tint = book.color,
                                turnProgress = progress.value,
                                turnDirection = turnDirection,
                                handbookMode = true,
                                diaryDate = nextDiaryDate,
                                onAddImage = {},
                                scheduleEntries = uiState.schedulePreviewEntries,
                            )
                        },
                    )
                }
            }
        }

        // 底部栏：对照原版 BookViewExampleKt
        // 左侧导出图标 | 中间 2026 ▼ | 右侧返回；背景铺到屏幕底，内容固定在上 52dp，下方 24dp 留给系统手势条
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(GoaldayDesign.TabBarBg),
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier.size(40.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = remember { uploadTrayIcon() },
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
                Spacer(Modifier.height(24.dp))
            }
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
                    // 书脊凹槽阴影：对照原版实机像素，左页右缘保持纯白，右页左缘（屏幕中线）最深，向右约18dp雾化到全白
                    if (!isLeft) {
                        val gutterWidth = 20.dp.toPx()
                        drawRect(
                            brush = Brush.horizontalGradient(
                                0f to Color(0xFFE0DEDB),
                                0.28f to Color(0xFFEFEDEA),
                                0.65f to Color(0xFFF8F7F5),
                                1f to Color.White,
                                startX = 0f,
                                endX = gutterWidth,
                            ),
                            size = Size(width = gutterWidth, height = size.height),
                            topLeft = Offset.Zero,
                        )
                    }
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

/**
 * 导出图标：对照原版实机像素自绘——下方 U 形开口托盘（两竖边+底横），中央悬空实心向上箭头。
 * Compose 核心图标库的 FileUpload/Upload 只有一条横线、Publish 横线在箭头上方，均不匹配。
 */
private fun uploadTrayIcon(): ImageVector =
    ImageVector.Builder(
        name = "BookExportUpload",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).vectorPath(fill = SolidColor(Color.Black)) {
        // 实心箭头：三角箭头 + 矩形箭杆（箭杆底端悬空，不接触托盘）
        moveTo(12f, 3f)
        lineTo(7.2f, 9f)
        lineTo(10.6f, 9f)
        lineTo(10.6f, 15f)
        lineTo(13.4f, 15f)
        lineTo(13.4f, 9f)
        lineTo(16.8f, 9f)
        close()
    }.vectorPath(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Butt,
        strokeLineJoin = StrokeJoin.Miter,
    ) {
        // U 形托盘：左竖边 → 底横 → 右竖边
        moveTo(4f, 13.5f)
        lineTo(4f, 20f)
        lineTo(20f, 20f)
        lineTo(20f, 13.5f)
    }.build()
