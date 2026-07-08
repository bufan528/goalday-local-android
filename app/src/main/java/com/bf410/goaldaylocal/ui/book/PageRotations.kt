package com.bf410.goaldaylocal.ui.book

/**
 * 6页旋转角度数据类
 * 用于管理书本翻页时各个页面的3D旋转角度
 * 
 * @param frontRotation 封面旋转角度（-180° 到 0°）
 * @param pageOneRotation 第1页旋转角度
 * @param pageTwoRotation 第2页旋转角度
 * @param pageThreeRotation 第3页旋转角度
 * @param pageFourRotation 第4页旋转角度
 * @param lastRotation 最后一页旋转角度
 */
data class PageRotations(
    val frontRotation: Float = 0f,
    val pageOneRotation: Float = 0f,
    val pageTwoRotation: Float = 0f,
    val pageThreeRotation: Float = 0f,
    val pageFourRotation: Float = 0f,
    val lastRotation: Float = 0f
) {
    companion object {
        /**
         * 默认旋转角度（所有页面都在 0°，即完全展开状态）
         */
        val DEFAULT = PageRotations()
        
        /**
         * 完全闭合状态的旋转角度
         */
        val CLOSED = PageRotations(
            frontRotation = -180f,
            pageOneRotation = -180f,
            pageTwoRotation = -180f,
            pageThreeRotation = -180f,
            pageFourRotation = -180f,
            lastRotation = -180f
        )
    }
}
