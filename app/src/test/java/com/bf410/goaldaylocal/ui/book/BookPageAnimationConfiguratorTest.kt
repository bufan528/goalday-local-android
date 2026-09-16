package com.bf410.goaldaylocal.ui.book

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * 对照原版 BaseBookViewKt.m31447t + m31479U/V/W/X/Y/Z，
 * 以及 BookPageAnimationConfigurator idle/6-page 配置。
 *
 * 浮点断言一律带 delta（避免 -0.0f 与连乘舍入问题）。
 */
class BookPageAnimationConfiguratorTest {

    private val delta = 1e-3f

    @Test
    fun idle_returns_stacked_rotations() {
        val configurator = BookPageAnimationConfigurator()
        // 空闲态直接返回 idlePageConfigs，与 progress 无关
        assertEquals(-180f, configurator.calculate("firstPage", 0.5f), delta)
        assertEquals(-180f, configurator.calculate("pageOne", 0.5f), delta)
        assertEquals(-153.5f, configurator.calculate("pageTwo", 0.5f), delta)
        assertEquals(-26.5f, configurator.calculate("pageThree", 0.5f), delta)
        assertEquals(0f, configurator.calculate("pageFour", 0.5f), delta)
        assertEquals(0f, configurator.calculate("lastPage", 0.5f), delta)
    }

    @Test
    fun frontRotation_follows_open_progress_until_open() {
        val configurator = BookPageAnimationConfigurator()
        // bookIsOpen=false（闭合）：front = -180*progress（对照 U）
        assertEquals(0f, configurator.fullRotations(0f, bookIsOpen = false, hasGestureStart = false).frontRotation, delta)
        assertEquals(-90f, configurator.fullRotations(0.5f, bookIsOpen = false, hasGestureStart = false).frontRotation, delta)
        // bookIsOpen=true：front 常驻 -180
        assertEquals(-180f, configurator.fullRotations(0f, bookIsOpen = true, hasGestureStart = false).frontRotation, delta)
        assertEquals(-180f, configurator.fullRotations(1f, bookIsOpen = true, hasGestureStart = true).frontRotation, delta)
    }

    @Test
    fun without_gesture_falls_back_to_linear_formulas() {
        val configurator = BookPageAnimationConfigurator()
        // 无手势时 X/Y/Z 走线性 fallback（对照 m31476X/Y/Z 的 else 分支）
        val r = configurator.fullRotations(0.5f, bookIsOpen = true, hasGestureStart = false)
        assertEquals(-90f, r.pageOneRotation, delta)
        assertEquals(0.5f * (26.5f - 180f), r.pageTwoRotation, delta)
        assertEquals(0.5f * -26.5f, r.pageThreeRotation, delta)
    }

    @Test
    fun drag_start_locks_left_slide_configs() {
        val configurator = BookPageAnimationConfigurator()
        configurator.start()
        // progress=0 首次计算锁定 isLeftSlide=true（abs(0-0)<=abs(0-1)）
        val r = configurator.fullRotations(0f, bookIsOpen = true, hasGestureStart = true)
        assertEquals(-180f, r.pageOneRotation, delta)
        assertEquals(-153.5f, r.pageTwoRotation, delta)
        assertEquals(-26.5f, r.pageThreeRotation, delta)
    }

    @Test
    fun follower_rotation_tracks_active_page_at_paper_ratio() {
        val configurator = BookPageAnimationConfigurator()
        assertEquals(0f, configurator.followerRotation(0f), delta)
        assertEquals(26.5f, configurator.followerRotation(180f), delta)
        assertEquals(-26.5f, configurator.followerRotation(-180f), delta)
    }
}
