package com.bf410.goaldaylocal.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView

/**
 * 对照原版全屏沉浸：M3 弹层/对话框自带独立窗口，聚焦时会唤出系统栏；
 * 在弹层内容顶部调用一次，使其窗口同样隐藏系统栏（滑边临时唤出）。
 * 非对话框内调用时无操作。
 */
@Composable
fun KeepImmersiveInDialog() {
    val view = LocalView.current
    DisposableEffect(view) {
        val window = (view.parent as? androidx.compose.ui.window.DialogWindowProvider)?.window
        window?.let {
            androidx.core.view.WindowInsetsControllerCompat(it, it.decorView).apply {
                hide(androidx.core.view.WindowInsetsCompat.Type.systemBars())
                systemBarsBehavior =
                    androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
        onDispose { }
    }
}
