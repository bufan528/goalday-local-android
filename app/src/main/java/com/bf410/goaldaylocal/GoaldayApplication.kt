package com.bf410.goaldaylocal

import android.app.Application
import com.tencent.mmkv.MMKV

class GoaldayApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        MMKV.initialize(this)
    }
}
