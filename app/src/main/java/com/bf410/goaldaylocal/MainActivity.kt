package com.bf410.goaldaylocal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.bf410.goaldaylocal.ui.GoaldayApp

const val EXTRA_START_TARGET = "goalday_start_target"
const val START_TARGET_DIARY = "diary"
const val START_TARGET_HANDBOOK = "handbook"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        hideSystemBars()
        val startTarget = intent?.getStringExtra(EXTRA_START_TARGET)
        setContent {
            GoaldayApp(startTarget = startTarget)
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        // 对照原版全屏沉浸：弹层/弹窗/选择器等焦点窗会唤出系统栏，焦点回来即重藏
        if (hasFocus) hideSystemBars()
    }

    // 对照原版全屏沉浸（真机各页均无状态栏/导航条）：隐藏系统栏，滑边临时唤出
    private fun hideSystemBars() {
        androidx.core.view.WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(androidx.core.view.WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior =
                androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}
