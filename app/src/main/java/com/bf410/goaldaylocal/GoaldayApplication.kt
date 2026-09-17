package com.bf410.goaldaylocal

import android.app.Application
import android.content.Context
import com.tencent.mmkv.MMKV

class GoaldayApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        MMKV.initialize(this)
        // 字号档位由 Compose 自研体系管理；旧 FontUtils 初始化调用已移除。
    }

    companion object {
        var appContext: Context? = null
            private set
    }
}
