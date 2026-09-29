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
 *
 * 关键：震动依赖 manifest 的 `android.permission.VIBRATE`（普通权限，装机即授予）。
 * 漏声明时 vibrate() 抛 SecurityException，会被这里的 runCatching 静默吞掉，
 * 现场表现是"代码明明调了 haptic 却一点反馈都没有"。所以下面只在无权限时降级不报错。
 */
object InteractionFeedback {
    fun click(context: Context) {
        haptic(context, 15L)
    }

    /** 拖拽落位/确认等稍重的正反馈 */
    fun confirm(context: Context) {
        haptic(context, 28L)
    }

    /**
     * 拿起来（长按进入拖拽）：比 [click] 更长更实，明确告诉用户"已经拿起了"。
     * 用 EFFECT_HEAVY_CLICK 之外的显式波形，避免各厂商对 predefined 效果差异过大。
     */
    fun lift(context: Context) {
        val vibrator = resolveVibrator(context) ?: return
        if (!vibrator.hasVibrator()) return
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(28L, VibrationEffect.DEFAULT_AMPLITUDE),
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(28L)
            }
        }
    }

    fun haptic(context: Context, durationMs: Long = 50L) {
        val vibrator = resolveVibrator(context) ?: return
        if (!vibrator.hasVibrator()) return
        runCatching {
            vibrator.vibrate(
                VibrationEffect.createOneShot(
                    durationMs,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        VibrationEffect.DEFAULT_AMPLITUDE
                    } else {
                        0
                    },
                ),
            )
        }
    }

    private fun resolveVibrator(context: Context): Vibrator? = runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }.getOrNull()
}
