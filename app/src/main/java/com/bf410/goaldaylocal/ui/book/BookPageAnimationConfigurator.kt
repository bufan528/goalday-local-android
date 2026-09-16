package com.bf410.goaldaylocal.ui.book

import kotlin.math.abs

/**
 * 对照原版 BaseBookViewKt.java + BookPageAnimationConfigurator.java：
 * 手账翻页使用 6 页旋转配置，根据 progress 和 isLeftSlide 计算每一页的 rotationY。
 *
 * 6 页配置：
 * - firstPage:  左/右均为 0°（空闲态 -180°）
 * - pageOne:    左 (-180→-180)，右 (-180→-153.5)（空闲态 -180°）
 * - pageTwo:    左 (-153.5→-180)，右 (-153.5→-26.5)（空闲态 -153.5°）
 * - pageThree:  左 (-26.5→-153.5)，右 (-26.5→0)（空闲态 -26.5°）
 * - pageFour:   左 (0→-26.5)，右 (0→0)（空闲态 0°）
 * - lastPage:   左/右均为 0°（空闲态 0°）
 *
 * isLeftSlide 判定：原版由 progress 位置自动判定（abs(progress-0) ≤ abs(progress-1) → left config），
 * 而非手势方向。最终角度钳制在 [-180, 0]。
 */
class BookPageAnimationConfigurator {

    data class PageConfig(
        val leftSlideStart: Float,
        val leftSlideEnd: Float,
        val rightSlideStart: Float,
        val rightSlideEnd: Float,
    )

    private val pageConfigs: Map<String, PageConfig> = mapOf(
        "firstPage" to PageConfig(0f, 0f, 0f, 0f),
        "pageOne" to PageConfig(-180f, -180f, -180f, -153.5f),
        "pageTwo" to PageConfig(-153.5f, -180f, -153.5f, -26.5f),
        "pageThree" to PageConfig(-26.5f, -153.5f, -26.5f, 0f),
        "pageFour" to PageConfig(0f, -26.5f, 0f, 0f),
        "lastPage" to PageConfig(0f, 0f, 0f, 0f),
    )

    private val idlePageConfigs: Map<String, Float> = mapOf(
        "firstPage" to -180f,
        "pageOne" to -180f,
        "pageTwo" to -153.5f,
        "pageThree" to -26.5f,
        "pageFour" to 0f,
        "lastPage" to 0f,
    )

    private var isStateIdle: Boolean = true
    private var isLeftSlide: Boolean? = null

    /**
     * 进入拖动/动画态。
     * 对照原版 BookPageAnimationConfigurator：不记录方向，方向由 [calculate] 根据 progress 自动判定。
     */
    fun start() {
        isStateIdle = false
        isLeftSlide = null
    }

    /** 进入空闲态。 */
    fun idle() {
        isStateIdle = true
        isLeftSlide = null
    }

    /**
     * 计算指定页面的 rotationY。
     * 对照原版：isLeftSlide 在首次调用时根据 progress 位置自动判定
     *   abs(progress - 0) ≤ abs(progress - 1) → left config
     *   否则 → right config
     * @param pageName firstPage/pageOne/pageTwo/pageThree/pageFour/lastPage
     * @param progress 0..1 的翻页进度
     */
    fun calculate(pageName: String, progress: Float): Float {
        if (isStateIdle) {
            return idlePageConfigs[pageName] ?: 0f
        }
        val clamped = progress.coerceIn(0f, 1f)
        val config = pageConfigs[pageName] ?: return 0f
        if (isLeftSlide == null) {
            // 对照原版：首次计算时根据 progress 与 0/1 的距离判定 left/right
            isLeftSlide = kotlin.math.abs(clamped - 0f) <= kotlin.math.abs(clamped - 1f)
        }
        val value = if (isLeftSlide == true) {
            lerp(config.leftSlideStart, config.leftSlideEnd, clamped)
        } else {
            lerp(config.rightSlideStart, config.rightSlideEnd, 1f - clamped)
        }
        return value.coerceIn(-180f, 0f)
    }

    /**
     * 对照原版 BaseBookViewKt.m31447t(BookOpenableView) + m31479U/V/W/X/Y/Z：
     * 6 页联动旋转一次算出（front/pageOne/pageTwo/pageThree/pageFour/last）。
     *
     * - front(U)：bookIsOpen ? -180 : -180*progress（封面开书翻到左侧后常驻 -180）
     * - pageOne(X)：(hasGesture && bookIsOpen) ? configurator : progress*-180
     * - pageTwo(Z)：(hasGesture && bookIsOpen) ? configurator : progress*(26.5-180)
     * - pageThree(Y)：(hasGesture && bookIsOpen) ? configurator : progress*-26.5
     * - pageFour(W)/lastPage(V)：恒走 configurator（含空闲态 -180/-153.5/-26.5/0 层叠）
     *
     * 注意：[calculate] 首次调用会按 progress 位置锁定 isLeftSlide，
     * 同一帧内 6 次调用 progress 一致，锁定结果一致，与原版逐行计算等价。
     */
    fun fullRotations(progress: Float, bookIsOpen: Boolean, hasGestureStart: Boolean): PageRotations {
        val p = progress.coerceIn(0f, 1f)
        val front = if (bookIsOpen) -180f else -180f * p
        val pageOne = if (hasGestureStart && bookIsOpen) calculate("pageOne", p) else p * -180f
        val pageTwo = if (hasGestureStart && bookIsOpen) calculate("pageTwo", p) else p * (26.5f - 180f)
        val pageThree = if (hasGestureStart && bookIsOpen) calculate("pageThree", p) else p * -26.5f
        val pageFour = calculate("pageFour", p)
        val last = calculate("lastPage", p)
        return PageRotations(
            frontRotation = front,
            pageOneRotation = pageOne,
            pageTwoRotation = pageTwo,
            pageThreeRotation = pageThree,
            pageFourRotation = pageFour,
            lastRotation = last,
        )
    }

    companion object {
        /**
         * 联动纸张跟随系数 = 26.5/180（对照原版 pageThree 空闲角 -26.5°）。
         * 翻页时非主动页以主动页旋转的该比例跟随剥离，既有层叠纸感，
         * 静止时 progress=0 → 跟随角=0（与主动页完全重合，稳态像素零变化）。
         */
        const val FOLLOW_FACTOR = 26.5f / 180f
    }

    /**
     * 翻页联动跟随角：白色衬纸层以主动页旋转的 [FOLLOW_FACTOR] 跟随。
     * max 跟随 ±26.5° 恰为原版 pageThree 空闲层叠角。
     */
    fun followerRotation(activeRotation: Float): Float = activeRotation * FOLLOW_FACTOR

    /**
     * 对照原版 6-page 曲线，映射到单页 0°→±180° 的完整翻转。
     * - NEXT（左滑）使用 pageThree 左滑曲线，progress 0→1。
     * - PREVIOUS（右滑）使用 pageTwo 右滑曲线，并对 progress 做 1-x 反转，
     *   与原版 BaseBookViewKt 在方向 != LEFT 时 f2 = 1 - f2 的行为一致。
     */
    fun handbookPageRotationY(direction: TurnDirection?, progress: Float): Float {
        return when (direction) {
            TurnDirection.NEXT -> {
                // pageThree 左滑：-26.5° → -153.5°
                val r = calculate("pageThree", progress)
                ((r + 26.5f) / -127f).coerceIn(0f, 1f) * -180f
            }
            TurnDirection.PREVIOUS -> {
                // pageTwo 右滑：原 progress 0→1 映射为 1→0，触发 right config
                val reversed = 1f - progress.coerceIn(0f, 1f)
                val r = calculate("pageTwo", reversed)
                ((r + 153.5f) / 127f).coerceIn(0f, 1f) * 180f
            }
            null -> 0f
        }
    }

    private fun lerp(start: Float, end: Float, fraction: Float): Float {
        return start + (end - start) * fraction
    }
}
