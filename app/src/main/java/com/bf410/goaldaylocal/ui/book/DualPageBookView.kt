package com.bf410.goaldaylocal.ui.book

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
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
import androidx.compose.ui.res.painterResource
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
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import kotlin.math.abs

/**
 * 双页展开手账视图。
 *
 * 对照原版 BookViewExampleKt.CalendarBookView + BaseBookViewKt + CircularCalendarPageState：
 * - 书芯为按日双面的年历书：spread = [左页 | 右页]，每页 = 一天的日程面或日记面；
 *   中心落在周一时 spread 为 [周一日程 | 周一日记]，其余为 [D-1 日记 | D 日记]；
 * - 翻页步长 NEXT +2 天（中心周日 +1）/ PREV -2 天（中心周一 -1），4 次翻完一周；
 * - 页面背景纯白、左平右圆圆角(10dp)、10dp 阴影(#FFC5BBB6)；
 * - 翻页时当前页绕书脊做 180° rotationY，transformOrigin 在书脊侧；
 * - 子元素（列表、勾选框）仍可交互，翻页手势在全宽区域检测。
 */
/** 书芯年份边界（对照原版 BookPageDateRange.fromYear：整年循环） */
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

    // 原版书芯状态机：按日双面年历书（spread=pages[4]/[5]；中心=周一时 [周一日程|周一日记]，
    // 其余 [D-1 日记|D 日记]；开书对照真机落在本周一）。翻页 NEXT +2 天(周日+1)/PREV -2 天(周一-1)
    val pageState = remember {
        CircularCalendarPageState(
            initialCenterDate = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)),
        )
    }
    var turnCount by remember { mutableIntStateOf(0) }
    val leftPage = pageState.leftPage
    val rightPage = pageState.rightPage
    // 翻页背面落点：NEXT 右页背面=下一 spread 左页；PREV 左页背面=上一 spread 右页
    val nextSpread = pageState.nextSpread()
    val prevSpread = pageState.prevSpread()
    val nextLeftPage = nextSpread.first
    val prevRightPage = prevSpread.second

    val schedulePage = book.pages.filterIsInstance<SchedulePage>().firstOrNull()
        ?: SchedulePage("日程页", emptyList())
    val diaryPage = book.pages.filterIsInstance<DiaryPage>().firstOrNull()
        ?: DiaryPage("日记页", "写下这一页最重要的记录。")

    // 日记内容按日期读取（记录 Tab 与书内共用同一存储）；书内可写态按日期缓存编辑态并即时落盘
    val diaryStore = remember { com.bf410.goaldaylocal.data.LocalStateStore(com.tencent.mmkv.MMKV.defaultMMKV()) }
    val diaryStates = remember { androidx.compose.runtime.mutableStateMapOf<LocalDate, StructuredDiary>() }
    fun diaryStateOf(date: LocalDate): StructuredDiary =
        diaryStates.getOrPut(date) {
            StructuredDiary.fromRaw(diaryStore.diaryText(DiaryStoreBookId, date.toString()))
        }
    fun saveDiaryState(date: LocalDate, state: StructuredDiary) {
        diaryStates[date] = state
        diaryStore.setDiaryText(DiaryStoreBookId, date.toString(), state.toRaw())
    }
    var pendingImageDate by remember { mutableStateOf<LocalDate?>(null) }
    val pickerContext = LocalContext.current
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: android.net.Uri? ->
        val date = pendingImageDate
        if (uri != null && date != null) {
            // 物理复制后存绝对路径（content 直链重启即失效，见 DiaryImageStore）
            val stored = copyDiaryImageToPrivateDir(pickerContext, uri, date.toString()) ?: uri.toString()
            saveDiaryState(date, diaryStateOf(date).withImageUri(stored))
        }
    }

    var showBookShelf by remember { mutableStateOf(false) }
    var showExportSheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val progress = remember { Animatable(0f) }
    var turnDirection by remember { mutableStateOf<TurnDirection?>(null) }
    var isAnimating by remember { mutableStateOf(false) }
    var pageWidthPx by remember { mutableFloatStateOf(1f) }
    // 对照原版 BookPageAnimationConfigurator：6页曲线 + isLeftSlide按progress位置判定
    val flipConfigurator = remember { BookPageAnimationConfigurator() }
    // 单通道drag：避免每move一个launch乱序，合流到最新进度（holder不用state，免每次move重组）
    val dragJobHolder = remember { arrayOfNulls<kotlinx.coroutines.Job>(1) }
    // 对照原版 BaseBookView$2 + performOpenAnimation(m31449r)：
    // 进书 800ms 后播开书动画（封面 frontRotation U = -180*progress 翻到左侧），期间禁用翻页手势；
    // 开书完成后 bookIsOpen=true（翻页阈值由 0.5 降到 0.3，对照 e0），封面常驻 -180 隐藏。
    var bookIsOpen by remember { mutableStateOf(false) }
    val openProgress = remember { Animatable(0f) }
    var isOpening by remember { mutableStateOf(true) }
    // 开书动画可重播：换书/切年时重置封面再走一遍（对照原版切书重调 performOpenAnimation）
    fun replayOpenAnimation() {
        if (isAnimating) return
        scope.launch {
            bookIsOpen = false
            isOpening = true
            openProgress.snapTo(0f)
            kotlinx.coroutines.delay(800)
            openProgress.animateTo(1f, tween(450, easing = LinearEasing))
            bookIsOpen = true
            kotlinx.coroutines.delay(10)
            isOpening = false
        }
    }
    LaunchedEffect(Unit) {
        replayOpenAnimation()
    }
    // 释放阈值双态：闭合→首页 0.5，页→页 0.3（对照原版 e0；开启动画期间手势已锁，闭合态只影响首翻）
    val flipThreshold = if (bookIsOpen) 0.3f else 0.5f

    // 书芯单页渲染：日程面=该页所在周的书内周视图（原版书内嵌 ScheduleFragment，7 行 Mon-Sun），
    // 日记面=可写日记页（原版书内嵌 DiaryFragment，翻到即可书写；改动即时落盘）
    val pageContent: @Composable (DayPage, Boolean) -> Unit = { page, isLeftPage ->
        if (page.isSchedule) {
            InBookSchedulePreview(
                modifier = Modifier.fillMaxSize(),
                page = schedulePage,
                pageIndex = 0,
                pageCount = book.pages.size,
                schedulePreviewEntries = uiState.schedulePreviewEntries,
                isChecked = viewModel::isChecked,
                tint = book.color,
                turnProgress = progress.value,
                turnDirection = turnDirection,
                handbookMode = true,
                weekStartDate = page.date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)),
                // 书内日程面只读预览（不可新增/改名/跳转）
                editable = false,
                onAddEntry = { date, text ->
                    viewModel.addScheduleFromHandbook(text, date.monthValue, date.dayOfMonth)
                },
                onRenameEntry = { entryId, newTitle ->
                    viewModel.updateScheduleTitleFromHandbook(entryId, newTitle)
                },
                onOpenDay = { date -> onOpenDate(date, true) },
            )
        } else {
            InBookDiaryEditorPage(
                modifier = Modifier.fillMaxSize(),
                date = page.date,
                isLeftPage = isLeftPage,
                state = diaryStateOf(page.date),
                onStateChange = { saveDiaryState(page.date, it) },
                onAddImage = {
                    pendingImageDate = page.date
                    imagePicker.launch(arrayOf("image/*"))
                },
                scheduleEntries = uiState.schedulePreviewEntries,
                // 内嵌目标打卡：联动当前书的计划看板（今日待办/已完成）
                planItems = uiState.todayPlanItems,
                donePlanItems = uiState.todayCompletedItems,
                onCompleteItem = viewModel::moveItemToCompleted,
                onUncompleteItem = viewModel::moveItemToToday,
            )
        }
    }

    // 把书页左右边缘排除在系统返回手势之外，确保全宽翻页热区可用
    val view = LocalView.current

    // 全屏沉浸由 MainActivity 统一处理（对照原版各页均无系统栏）；此处不再重复隐藏/恢复，
    // 避免离书时把栏唤出（之前 onDispose 的 show() 会破坏主界面沉浸）。
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

    // 对照原版：动画时长自适应，progress>0.5 时 100ms，否则 300ms，线性 easing；
    // 单手势最多进一位，进位只发生在松手 settle（手势中途不提前进位）
    fun settle(complete: Boolean, alreadyAdvanced: Boolean = false) {
        if (isAnimating) return
        scope.launch {
            isAnimating = true
            val currentProgress = progress.value
            val spec = tween<Float>(if (currentProgress > 0.5f) 100 else 300, easing = LinearEasing)
            if (complete) {
                progress.animateTo(1f, spec)
                if (!alreadyAdvanced) {
                    when (turnDirection) {
                        TurnDirection.NEXT -> pageState.goNextPage()
                        TurnDirection.PREVIOUS -> pageState.goPreviousPage()
                        null -> {}
                    }
                }
                turnCount++
                progress.snapTo(0f)
            } else {
                progress.animateTo(0f, spec)
            }
            turnDirection = null
            flipConfigurator.idle()
            // 对照原版：动画结束后 10ms 再重新启用手势
            kotlinx.coroutines.delay(10)
            isAnimating = false
        }
    }

    val shellColor = GoaldayDesign.BookBoardLight
    val shadowColor = Color(0xFFC5BBB6)
    val context = LocalContext.current
    // 书壳布纹：程序化细颗粒（自绘，避免位图资源依赖）
    val fabricImage: androidx.compose.ui.graphics.ImageBitmap? = null

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Text(
            "${rightPage.date.monthValue}月",
            fontSize = 18.sp,
            color = GoaldayDesign.adaptiveInkPrimary,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 167.dp),
        )
        val configuration = LocalConfiguration.current
        val screenWidthDp = configuration.screenWidthDp.dp
        // 真机dump修正（2026-09-15 BookActivity hierarchy 904x2316）：
        // 白页总宽848px=0.938屏宽、总高785px，单页424x785 h/w=1.851=原版AnimationBookWidth*1.85；
        // 旧1.092整书比偏高18%，白页276dp偏窄，已废弃。白页垂直居中top≈766px。
        // 月标签在页内左上（9月 | 第38周），屏顶不再放居中大月（对照dump index17/18）。
        val shellWidth = screenWidthDp * 0.968f
        val spreadWidth = shellWidth - 10.dp
        val spreadHeight = spreadWidth * 0.926f
        // 壳比白页每边大5dp水平/8dp垂直，露出层叠纸边
        val shellHeight = spreadHeight + 16.dp

        // 书壳（最外层：布纹）。对称内边，白页垂直居中于屏幕（dump top≈766px即居中）
        Box(
            modifier = Modifier
                .align(Alignment.Center)
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
                    // 对照原版 BookFlipConfig corner=10dp、壳阴影 10dp
                    elevation = 10.dp,
                    shape = RoundedCornerShape(10.dp),
                    clip = false,
                    ambientColor = shadowColor,
                    spotColor = shadowColor,
                )
                .clip(RoundedCornerShape(10.dp))
                .drawWithContent {
                    // 书壳底色（纯色书衣）
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
                    } ?: run {
                        drawRect(shellColor)
                        // 程序化布纹：细密经纬线（对照原版封面织物感，自绘无位图依赖）
                        val step = 3.dp.toPx()
                        val warp = Color(0xFF8A7F73).copy(alpha = 0.055f)
                        val weft = Color.White.copy(alpha = 0.06f)
                        var ly = 0f
                        while (ly < size.height) {
                            drawLine(warp, Offset(0f, ly), Offset(size.width, ly), strokeWidth = 1f)
                            drawLine(weft, Offset(0f, ly + step / 2), Offset(size.width, ly + step / 2), strokeWidth = 1f)
                            ly += step
                        }
                        var lx = 0f
                        while (lx < size.width) {
                            drawLine(warp, Offset(lx, 0f), Offset(lx, size.height), strokeWidth = 1f)
                            lx += step
                        }
                    }
                    drawContent()
                }
                    .pointerInput(Unit) {
                        val width = size.width.toFloat()
                        pageWidthPx = width
                        awaitPointerEventScope {
                            while (true) {
                                val down = awaitFirstDown(requireUnconsumed = false)
                                if (isAnimating || isOpening) {
                                    down.consume()
                                    continue
                                }
                                var startX = down.position.x
                                val startY = down.position.y
                                val velocityTracker = VelocityTracker()
                                velocityTracker.resetTracking()
                                velocityTracker.addPointerInputChange(down)

                                var turnDir: TurnDirection? = null
                                var finished = false
                                // 对照原版 ComposeModifiersKt.horizontalSwipeGesture：
                                // detectHorizontalDragGestures 内部走系统 touchSlop + 水平锁定，
                                // progress=abs(totalDistance)/threshold 钳制0..1，
                                // velocity=dx/dt，isFling=abs(v)>minFling，方向首动锁定不再翻转。
                                val touchSlop = viewConfiguration.touchSlop.toFloat()
                                while (!finished) {
                                    val event = awaitPointerEvent()
                                    val change = event.changes.firstOrNull { it.id == down.id } ?: continue
                                    if (change.pressed.not()) {
                                        val velocity = velocityTracker.calculateVelocity().x
                                        // 对照原版 BaseBookViewKt阈值0.3 + fling560 + 反向300回弹
                                        val opposing = when (turnDir) {
                                            TurnDirection.NEXT -> velocity > 300f
                                            TurnDirection.PREVIOUS -> velocity < -300f
                                            null -> false
                                        }
                                        val complete = when (turnDir) {
                                            TurnDirection.NEXT -> !opposing && (progress.value > flipThreshold || velocity < -560f)
                                            TurnDirection.PREVIOUS -> !opposing && (progress.value > flipThreshold || velocity > 560f)
                                            null -> false
                                        }
                                        settle(complete)
                                        finished = true
                                        break
                                    }
                                    val totalDx = change.position.x - startX
                                    val totalDy = change.position.y - startY
                                    velocityTracker.addPointerInputChange(change)
                                    if (turnDir == null && abs(totalDx) > touchSlop && abs(totalDx) > abs(totalDy)) {
                                        turnDir = if (totalDx < 0) TurnDirection.NEXT else TurnDirection.PREVIOUS
                                        val can = when (turnDir) {
                                            TurnDirection.NEXT -> pageState.canGoNext()
                                            TurnDirection.PREVIOUS -> pageState.canGoPrevious()
                                            null -> false
                                        }
                                        if (can) {
                                            turnDirection = turnDir
                                            flipConfigurator.start()
                                            dragJobHolder[0]?.cancel()
                                            dragJobHolder[0] = scope.launch { progress.snapTo(0f) }
                                        } else {
                                            finished = true
                                            break
                                        }
                                    }
                                    if (turnDir != null) {
                                        // 单页宽=整壳宽/2（原版AnimationBookWidth=屏宽*0.47≈单页），
                                        // 之前width*0.45偏敏感9%，改回width/2与原版一致。
                                        // 对照原版：一次手势只判一次、最多进一位（单进位），走满不提前进位，
                                        // 松手 settle 时按阈值/甩速决定进位或回弹。
                                        val singlePage = (width / 2f).coerceAtLeast(1f)
                                        val newProgress = (abs(change.position.x - startX) / singlePage).coerceIn(0f, 1f)
                                        dragJobHolder[0]?.cancel()
                                        dragJobHolder[0] = scope.launch { progress.snapTo(newProgress) }
                                    }
                                    change.consume()
                                }
                            }
                        }
                    },
            ) {
                // 中层：层叠书页灰白边（壳→白总水平10dp/垂直16dp，对照dump白页848x785）
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 2.dp, vertical = 2.dp)
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
                // 对照原版 RenderPages 6 页联动：每侧主动页之后垫一张白色衬纸，
                // 以主动页旋转的 FOLLOW_FACTOR(26.5/180) 跟随剥离；静止 progress=0 时跟随角=0
                // （与主动页完全重合，稳态像素零变化），仅翻页中显出纸张层叠。
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 3.dp, end = 3.dp, top = 6.dp, bottom = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(0.dp),
                ) {
                    // 左页：spread 第4页（周一中心=周一日程面，其余=D-1 日记面）
                    Box(modifier = Modifier.weight(1f)) {
                        val leftActive = handbookActiveRotation(true, progress.value, turnDirection, flipConfigurator)
                        if (turnDirection != null && progress.value > 0.01f && leftActive != 0f) {
                            FollowerPaper(isLeft = true, rotationY = flipConfigurator.followerRotation(leftActive), density = density)
                        }
                        HandbookPage(
                            modifier = Modifier.fillMaxSize(),
                            isLeft = true,
                            progress = progress.value,
                            direction = turnDirection,
                            // 点左页回主界面（日程面→周，日记面→记录）
                            onTap = { onOpenDate(leftPage.date, leftPage.isSchedule) },
                            configurator = flipConfigurator,
                            content = { pageContent(leftPage, true) },
                            backContent = {
                                // PREV 左页向右翻，背面=上一 spread 的右页
                                pageContent(prevRightPage, false)
                            },
                        )
                    }

                    // 右页：spread 第5页（周一中心=周一日记面，其余=D 日记面）
                    Box(modifier = Modifier.weight(1f)) {
                        val rightActive = handbookActiveRotation(false, progress.value, turnDirection, flipConfigurator)
                        if (turnDirection != null && progress.value > 0.01f && rightActive != 0f) {
                            FollowerPaper(isLeft = false, rotationY = flipConfigurator.followerRotation(rightActive), density = density)
                        }
                        HandbookPage(
                            modifier = Modifier.fillMaxSize(),
                            isLeft = false,
                            progress = progress.value,
                            direction = turnDirection,
                            onTap = { onOpenDate(rightPage.date, rightPage.isSchedule) },
                            configurator = flipConfigurator,
                            content = { pageContent(rightPage, false) },
                            backContent = {
                                // NEXT 右页向左翻，背面=下一 spread 的左页
                                pageContent(nextLeftPage, true)
                            },
                        )
                    }
                }
                // 开书封面：对照原版 frontRotation U = bookIsOpened ? -180 : -180*progress，
                // 全幅盖住双页、绕书脊（中线）翻开；过 90° 硬切隐藏，动画结束由 bookIsOpen 摘掉
                if (!bookIsOpen) {
                    val coverRot = -180f * openProgress.value
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .padding(start = 3.dp, end = 3.dp, top = 6.dp, bottom = 6.dp)
                            .graphicsLayer {
                                rotationY = coverRot
                                cameraDistance = 40f * density
                                transformOrigin = TransformOrigin(0.5f, 0.5f)
                                alpha = if (-coverRot <= 90f) 1f else 0f
                            }
                            .shadow(
                                elevation = 10.dp,
                                shape = RoundedCornerShape(10.dp),
                                clip = false,
                                ambientColor = Color(0xFFC5BBB6),
                                spotColor = Color(0xFFC5BBB6),
                            )
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White),
                    ) {
                        // 自绘封面：米色书衣 + 居中年份衬线字
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(yearCoverColor(rightPage.date.year)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                rightPage.date.year.toString(),
                                fontSize = 22.sp,
                                fontFamily = FontFamily.Serif,
                                color = Color(0xFF7A5C44),
                            )
                        }
                    }
                }
                // 开启动画期间吞掉书页一切触摸（对照原版 gestureEnable=false），封面翻完即撤
                if (isOpening) {
                    Box(
                        Modifier
                            .matchParentSize()
                            .pointerInput(Unit) {
                                awaitPointerEventScope {
                                    while (true) {
                                        val down = awaitFirstDown(requireUnconsumed = false)
                                        down.consume()
                                    }
                                }
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
                // 对照dump底栏总高68px=26dp：导出[6,2122][132,2190]/年[341,2116][564,2190]/返回[731,2125][857,2190]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(26.dp)
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clickableNoRipple { showExportSheet = true },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = remember { uploadTrayIcon() },
                            contentDescription = "导出",
                            modifier = Modifier.size(18.dp),
                            tint = GoaldayDesign.InkPrimary,
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier.clickableNoRipple { showBookShelf = true },
                    ) {
                        Text(
                            text = "${rightPage.date.year}",
                            fontSize = 14.sp,
                            color = GoaldayDesign.InkPrimary,
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "切换年份",
                            modifier = Modifier.size(16.dp),
                            tint = GoaldayDesign.InkPrimary,
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clickableNoRipple { onBack() },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "返回",
                            fontSize = 13.sp,
                            color = GoaldayDesign.InkPrimary,
                        )
                    }
                }
                // 下方留给系统手势条（对照原版底栏内容不贴屏幕底）
                Spacer(Modifier.height(24.dp))
            }
        }

        // 书架底部弹层（横排封面选年份换书，背后调光）
        if (showBookShelf) {
            Box(Modifier.fillMaxSize()) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color(0x66000000))
                        .clickableNoRipple { showBookShelf = false },
                )
                Box(Modifier.align(Alignment.BottomCenter)) {
                    BookShelfSheet(
                        currentYear = rightPage.date.year,
                onPickYear = { year ->
                    // 对照原版 BookShelfManager/BookPageDateRange.fromYear：选年换整年书并限定翻页边界
                    val start = LocalDate.of(year, 1, 1)
                    val end = LocalDate.of(year, 12, 31)
                    pageState.setDateRange(
                        BookPageDateRange(
                            start.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                            end.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1,
                        ),
                    )
                    pageState.jumpToDate(start)
                    turnCount++
                    showBookShelf = false
                    // 换书重播开场（对照原版切书重调 performOpenAnimation）
                    replayOpenAnimation()
                },
                onDismiss = { showBookShelf = false },
                    )
                }
            }
        }

        // 导出全屏页（打印PDF分区勾选 + 起止日期 + 预览 + 生成分享）
        if (showExportSheet) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(GoaldayDesign.AppBg),
            ) {
                ExportCenterSheet(
                    diaryTextFor = { date ->
                        diaryStore.diaryText(DiaryStoreBookId, date.toString())
                    },
                    scheduleEntries = uiState.schedulePreviewEntries,
                    weeklyTheme = uiState.weeklyTheme,
                    onDismiss = { showExportSheet = false },
                )
            }
        }
    }
}

/** 书架弹层：横排封面 + 年份，点击切换到对应年份的书 */
@Composable
private fun BookShelfSheet(
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
                // 对照原版 BookConstant：固定 2023–2026 四本年度书，书衣按 BookShelfManager.bookCoverMapping
                val years = listOf(2026, 2025, 2024, 2023)
                years.forEach { year ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(width = 58.dp, height = 84.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(yearCoverColor(year))
                                .clickableNoRipple { onPickYear(year) },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                year.toString(),
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Serif,
                                color = Color(0xFF7A5C44),
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
    onTap: (() -> Unit)? = null,
    configurator: BookPageAnimationConfigurator? = null,
) {
    // 左页：左侧平、右侧圆；右页：左侧圆、右侧平
    val pageShape = handbookPageShape(isLeft)

    // 翻页时当前页绕书脊旋转
    // 对照原版6页曲线：优先用configurator.handbookPageRotationY取非线性幅度，fallback线性progress*180
    val rotationY = handbookActiveRotation(isLeft, progress, direction, configurator)
    val absRotation = kotlin.math.abs(rotationY)
    // 正面可见条件：rotationY 绝对值 <= 90°；背面可见条件：> 90°
    val frontAlpha = if (absRotation <= 90f) 1f else 0f
    val backAlpha = if (absRotation > 90f) 1f else 0f

    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer {
                this.rotationY = rotationY
                // 对照原版 BaseBookViewKt：cameraDistance = 40 × density
                this.cameraDistance = 40f * density
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
                    // 书脊凹槽阴影：左页右缘保持纯白，右页左缘（屏幕中线）最深，向右雾化到全白；
                    // 宽度收至正文起始处（右页正文 start=16dp），避免压住首字
                    if (!isLeft) {
                        val gutterWidth = 16.dp.toPx()
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
            if (onTap != null) {
                // 原版书页为 NoTouchConstraintLayout：点任意页面区域即跳转主界面（仅日程面）
                Box(
                    Modifier
                        .matchParentSize()
                        .clickableNoRipple { onTap() },
                )
            }
        }
        // 背面内容：仅翻页越过 90° 时才组合（静止时若常驻会盖在正面层之上拦截触摸）
        if (backAlpha > 0f) {
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
}

/** 书页圆角：左页左侧平、右侧圆；右页反之（对照原版 cornerRadius=10dp）。 */
private fun handbookPageShape(isLeft: Boolean): RoundedCornerShape = RoundedCornerShape(
    topStart = if (isLeft) 0.dp else 10.dp,
    topEnd = if (isLeft) 10.dp else 0.dp,
    bottomEnd = if (isLeft) 10.dp else 0.dp,
    bottomStart = if (isLeft) 0.dp else 10.dp,
)

/**
 * 主动页旋转角：NEXT 转右页（负角向左翻）、PREVIOUS 转左页（正角向右翻），其余 0。
 * 幅度对照原版 6 页曲线（configurator.handbookPageRotationY），无配置器时 fallback 线性。
 */
private fun handbookActiveRotation(
    isLeft: Boolean,
    progress: Float,
    direction: TurnDirection?,
    configurator: BookPageAnimationConfigurator?,
): Float {
    val shouldRotate = when (direction) {
        TurnDirection.NEXT -> !isLeft
        TurnDirection.PREVIOUS -> isLeft
        null -> false
    }
    if (!shouldRotate) return 0f
    val curveMag = configurator?.let { kotlin.math.abs(it.handbookPageRotationY(direction, progress)) }
        ?: (progress * 180f)
    return curveMag * (if (isLeft) 1f else -1f)
}

/**
 * 联动衬纸：翻页时垫在主动页之下的白色纸层，以 FOLLOW_FACTOR 跟随剥离。
 * 无内容、无手势（对照原版 RenderPages 中间纸层）；静止时不组合，稳态零像素变化。
 */
@Composable
private fun FollowerPaper(isLeft: Boolean, rotationY: Float, density: Float) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                this.rotationY = rotationY
                // 对照原版 BaseBookViewKt：cameraDistance = 40 × density
                this.cameraDistance = 40f * density
                this.transformOrigin = if (isLeft) {
                    TransformOrigin(1f, 0.5f)
                } else {
                    TransformOrigin(0f, 0.5f)
                }
                // 与主动页一致的 Alpha 硬切
                this.alpha = if (kotlin.math.abs(rotationY) <= 90f) 1f else 0f
            }
            .shadow(
                elevation = 10.dp,
                shape = handbookPageShape(isLeft),
                clip = false,
                ambientColor = Color(0xFFC5BBB6),
                spotColor = Color(0xFFC5BBB6),
            )
            .clip(handbookPageShape(isLeft))
            .background(Color.White),
    )
}

/** 年度书封面底色（自绘布纹替代：按年份微调米色，封面年份数字叠在上层）。 */
private fun yearCoverColor(year: Int): androidx.compose.ui.graphics.Color = when (year) {
    2026 -> androidx.compose.ui.graphics.Color(0xFFE9E2D8)
    2025 -> androidx.compose.ui.graphics.Color(0xFFE4DCCF)
    2024 -> androidx.compose.ui.graphics.Color(0xFFDFD5C6)
    else -> androidx.compose.ui.graphics.Color(0xFFD9CFC2)
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
