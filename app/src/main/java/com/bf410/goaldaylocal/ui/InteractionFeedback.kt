package com.bf410.goaldaylocal.ui

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * 交互反馈：
 * - 点击：系统触感短震动（无音频资源依赖）
 * - 触感：一次性短震动（默认 50ms，API 31 走 VibratorManager）
 */
object InteractionFeedback {
    fun click(context: Context) {
        haptic(context, 15L)
    }

    fun haptic(context: Context, durationMs: Long = 50L) {
        runCatching {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }
}
