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
        val startTarget = intent?.getStringExtra(EXTRA_START_TARGET)
        setContent {
            GoaldayApp(startTarget = startTarget)
        }
    }
}
