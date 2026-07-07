package com.bf410.goaldaylocal.ui.book

/**
 * 页面配置：定义每个页面的旋转范围
 */
data class PageConfig(
    val leftSlideStart: Float,   // 向左滑动起始角度
    val leftSlideEnd: Float,     // 向左滑动结束角度
    val rightSlideStart: Float,  // 向右滑动起始角度
    val rightSlideEnd: Float     // 向右滑动结束角度
)

/**
 * 书本页面动画配置器
 * 负责根据滑动进度计算每个页面的3D旋转角度
 * 
 * 核心设计：
 * - 6个页面：封面、第1-4页、最后一页
 * - 每个页面有独立的旋转配置
 * - 支持左右滑动，自动判断滑动方向
 * - 旋转角度范围：-180° 到 0°
 */
class BookPageAnimationConfigurator {
    
    // 页面配置表：定义每个页面的旋转范围
    private val pageConfigs = mapOf(
        "firstPage" to PageConfig(0f, 0f, 0f, 0f),           // 封面：不旋转
        "pageOne" to PageConfig(-180f, -180f, -180f, -153.5f), // 第1页
        "pageTwo" to PageConfig(-153.5f, -180f, -153.5f, -26.5f), // 第2页
        "pageThree" to PageConfig(-26.5f, -153.5f, -26.5f, 0f),   // 第3页
        "pageFour" to PageConfig(0f, -26.5f, 0f, 0f),             // 第4页
        "lastPage" to PageConfig(0f, 0f, 0f, 0f)                  // 最后一页：不旋转
    )
    
    // 固定旋转角度表：用于特殊状态
    private val fixedRotations = mapOf(
        "firstPage" to -180f,
        "pageOne" to -180f,
        "pageTwo" to -153.5f,
        "pageThree" to -26.5f,
        "pageFour" to 0f,
        "lastPage" to 0f
    )
    
    // 动画状态
    private var isIdle = true
    private var slideDirection: SlideDirection? = null
    
    /**
     * 滑动方向枚举
     */
    enum class SlideDirection {
        LEFT,   // 向左滑动（翻到下一页）
        RIGHT   // 向右滑动（翻到上一页）
    }
    
    /**
     * 根据页面名称和滑动进度计算旋转角度
     * 
     * @param pageName 页面名称（firstPage, pageOne, pageTwo, pageThree, pageFour, lastPage）
     * @param progress 滑动进度（0.0 到 1.0）
     * @return 旋转角度（-180° 到 0°）
     */
    fun calculateRotation(pageName: String, progress: Float): Float {
        // 如果处于空闲状态，返回固定角度
        if (isIdle) {
            return fixedRotations[pageName] ?: 0f
        }
        
        // 限制进度范围
        val clampedProgress = progress.coerceIn(0f, 1f)
        
        // 获取页面配置
        val config = pageConfigs[pageName] ?: return 0f
        
        // 首次调用时判断滑动方向
        if (slideDirection == null) {
            val distToLeft = kotlin.math.abs(clampedProgress - 0f)
            val distToRight = kotlin.math.abs(clampedProgress - 1f)
            slideDirection = if (distToLeft <= distToRight) {
                SlideDirection.LEFT
            } else {
                SlideDirection.RIGHT
            }
        }
        
        // 根据滑动方向计算旋转角度
        val rotation = when (slideDirection) {
            SlideDirection.LEFT -> {
                lerp(config.leftSlideStart, config.leftSlideEnd, clampedProgress)
            }
            SlideDirection.RIGHT -> {
                lerp(config.rightSlideStart, config.rightSlideEnd, 1f - clampedProgress)
            }
            null -> 0f
        }
        
        // 限制旋转角度范围：-180° 到 0°
        return rotation.coerceIn(-180f, 0f)
    }
    
    /**
     * 计算所有页面的旋转角度
     * 
     * @param progress 滑动进度（0.0 到 1.0）
     * @return PageRotations 包含所有页面的旋转角度
     */
    fun calculateAllRotations(progress: Float): PageRotations {
        return PageRotations(
            frontRotation = calculateRotation("firstPage", progress),
            pageOneRotation = calculateRotation("pageOne", progress),
            pageTwoRotation = calculateRotation("pageTwo", progress),
            pageThreeRotation = calculateRotation("pageThree", progress),
            pageFourRotation = calculateRotation("pageFour", progress),
            lastRotation = calculateRotation("lastPage", progress)
        )
    }
    
    /**
     * 线性插值
     */
    private fun lerp(start: Float, end: Float, progress: Float): Float {
        return start + (end - start) * progress
    }
    
    /**
     * 设置为空闲状态（动画结束）
     */
    fun setIdle() {
        isIdle = true
        slideDirection = null
    }
    
    /**
     * 开始动画（重置状态）
     */
    fun startAnimation() {
        isIdle = false
        slideDirection = null
    }
    
    /**
     * 检查是否处于空闲状态
     */
    fun isIdle(): Boolean = isIdle
    
    /**
     * 获取当前滑动方向
     */
    fun getSlideDirection(): SlideDirection? = slideDirection
}
