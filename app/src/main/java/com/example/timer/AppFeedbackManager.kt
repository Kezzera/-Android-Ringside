package com.example.timer

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class AppFeedbackManager(context: Context) {
    // Low latency tone generator via ALARM stream for maximum volume & priority
    private val toneGen: ToneGenerator? = try {
        ToneGenerator(AudioManager.STREAM_ALARM, 100)
    } catch (e: Exception) {
        null
    }
    
    private val vibrator: Vibrator? = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager?
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?
        }
    } catch (e: Exception) {
        null
    }

    fun playRoundStartSignal() {
        try {
            // High pitched beep for start
            toneGen?.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT, 500)
            vibrate(longArrayOf(0, 500))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playRoundEndSignal() {
        try {
            // Lower, longer beeps for end
            toneGen?.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 800)
            vibrate(longArrayOf(0, 300, 200, 300))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playWarningSignal() {
        try {
            // Short double beep
            toneGen?.startTone(ToneGenerator.TONE_CDMA_NETWORK_BUSY, 300)
            vibrate(longArrayOf(0, 150, 150, 150))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playRestEndWarningSignal() {
        try {
            // Prepare beep
            toneGen?.startTone(ToneGenerator.TONE_DTMF_D, 200)
            vibrate(longArrayOf(0, 200))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun vibrate(pattern: LongArray) {
        try {
            if (vibrator?.hasVibrator() == true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(pattern, -1)
                }
            }
        } catch (e: Exception) {
            // Ignore vibrate exceptions if permissions are missing or hardware fails
        }
    }
    
    fun release() {
        try {
            toneGen?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
