package com.bf410.goaldaylocal.ui

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.bf410.goaldaylocal.R

/**
 * 原版交互反馈对齐：
 * - 点击音效：res/raw/ps_click_music.wav（逆向提取自原版，SoundPool 短音）
 * - 触感：一次性短震动，对照原版 VibrationUtils（默认 50ms，API 31 走 VibratorManager）
 */
object InteractionFeedback {
    private var soundPool: SoundPool? = null
    private var clickSoundId: Int = 0
    private var soundReady = false

    fun click(context: Context) {
        runCatching {
            val pool = soundPool ?: SoundPool.Builder()
                .setMaxStreams(2)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build(),
                )
                .build()
                .also { created ->
                    clickSoundId = created.load(context, R.raw.ps_click_music, 1)
                    created.setOnLoadCompleteListener { _, _, status -> soundReady = status == 0 }
                    soundPool = created
                }
            if (soundReady) pool.play(clickSoundId, 0.5f, 0.5f, 1, 0, 1f)
        }
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
