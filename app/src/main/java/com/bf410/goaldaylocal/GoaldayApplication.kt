package com.bf410.goaldaylocal

import android.app.Application
import android.content.Context
import com.tencent.mmkv.MMKV

class GoaldayApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        MMKV.initialize(this)
    }

    companion object {
        var appContext: Context? = null
            private set
    }
}
