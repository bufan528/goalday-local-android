package com.bf410.goaldaylocal.ui.book

/**
 * 翻页配置类
 * 用于控制翻页动画的各种参数
 */
data class BookFlipConfig(
    val animationDuration: Long = 300L,
    val shadowEnabled: Boolean = true,
    val shadowAlpha: Float = 0.3f,
    val cornerRadius: Float = 8f,
    val pageSpacing: Float = 2f,
    val enableHapticFeedback: Boolean = true,
    val flipSensitivity: Float = 1.0f
) {
    companion object {
        val DEFAULT = BookFlipConfig()
        
        val FAST = BookFlipConfig(
            animationDuration = 200L,
            flipSensitivity = 1.2f
        )
        
        val SLOW = BookFlipConfig(
            animationDuration = 500L,
            flipSensitivity = 0.8f
        )
    }
}
