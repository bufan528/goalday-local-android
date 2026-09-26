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
        // 每日提醒闹钟随重装/升级丢失：启动时按开关重排（幂等）。
        runCatching {
            if (com.bf410.goaldaylocal.ui.reminder.ReminderScheduler.isEnabled()) {
                com.bf410.goaldaylocal.ui.reminder.ReminderScheduler.schedule(this)
            }
        }
    }

    companion object {
        var appContext: Context? = null
            private set
    }
}
