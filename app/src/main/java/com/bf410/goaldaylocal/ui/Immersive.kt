package com.bf410.goaldaylocal.ui

import android.app.Activity
import android.content.ContextWrapper
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

/**
 * DropdownMenu（Popup 窗）对照沉浸：Popup 不是 Dialog，拿不到独立 window，
 * 开着时系统导航条会被唤出。这里按内容 view 自身控制器藏（作用到 Popup 窗），
 * 再按宿主 Activity 窗补藏一次；关闭时再藏一次（MainActivity 焦点重藏是第三道）。
 * 在每个 DropdownMenu 内容顶部调用一次。
 */
@Composable
fun KeepImmersiveInPopup() {
    val view = LocalView.current
    DisposableEffect(view) {
        var ctx: android.content.Context? = view.context
        var activity: Activity? = null
        while (ctx is ContextWrapper) {
            if (ctx is Activity) {
                activity = ctx
                break
            }
            ctx = ctx.baseContext
        }
        fun hide() {
            // Popup 内容挂在 Popup 自有窗口上：按内容 view 自身取控制器藏（作用到 Popup 窗），
            // 之前只藏 Activity 窗压不住（真机 dd_open 仍露导航条）。
            androidx.core.view.ViewCompat.getWindowInsetsController(view)?.apply {
                hide(androidx.core.view.WindowInsetsCompat.Type.systemBars())
                systemBarsBehavior =
                    androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
            activity?.window?.let {
                androidx.core.view.WindowInsetsControllerCompat(it, it.decorView).apply {
                    hide(androidx.core.view.WindowInsetsCompat.Type.systemBars())
                    systemBarsBehavior =
                        androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            }
        }
        hide()
        onDispose { hide() }
    }
}
