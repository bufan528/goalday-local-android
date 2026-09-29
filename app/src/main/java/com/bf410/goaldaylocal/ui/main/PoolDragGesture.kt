package com.bf410.goaldaylocal.ui.main

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed

/**
 * 只负责"按下"的即时反馈，不消费任何事件。
 *
 * 背景：`detectDragGesturesAfterLongPress` 在长按成立前完全没有反馈，
 * 而系统长按阈值是 500ms。这 500ms 里界面纹丝不动，用户主观就是
 * "按下去没反应 / 很生硬"，于是反复重试。
 *
 * 本检测器挂在同一个 Box 上与拖拽检测器并行：按下立刻回调 [onPress]，
 * 抬手或取消时回调 [onRelease]。它**不调用 consume**，所以不会与拖拽检测器抢事件。
 */
suspend fun PointerInputScope.detectPressFeedback(
    onPress: (Offset) -> Unit,
    onRelease: () -> Unit,
) {
    awaitEachGesture {
        awaitFirstDown(requireUnconsumed = false)
        onPress(Offset.Zero)
        // 等手指抬起/取消，把"按住了"的视觉与触感状态收回去
        var released = false
        while (!released) {
            val event = awaitPointerEvent()
            val change = event.changes.firstOrNull { it.pressed }
            released = change == null || change.changedToUpIgnoreConsumed()
        }
        onRelease()
    }
}
