package com.bf410.goaldaylocal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.bf410.goaldaylocal.ui.GoaldayApp

const val EXTRA_START_TARGET = "goalday_start_target"
const val START_TARGET_DIARY = "diary"
const val START_TARGET_HANDBOOK = "handbook"

class MainActivity : ComponentActivity() {
    // 桌面组件二次跳转：singleTask 复用实例走 onNewIntent，用 state 送进 Compose；
    // 消费后清 null，同 target 再点也能触发
    private var liveTarget by androidx.compose.runtime.mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        hideSystemBars()
        val startTarget = intent?.getStringExtra(EXTRA_START_TARGET)
        liveTarget = startTarget
        setContent {
            GoaldayApp(
                startTarget = startTarget,
                liveTarget = liveTarget,
                onLiveTargetConsumed = { liveTarget = null },
            )
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        liveTarget = intent.getStringExtra(EXTRA_START_TARGET)
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
