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
