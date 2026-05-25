package com.example.napmonitor.ui

import android.content.Context
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.napmonitor.R
import com.example.napmonitor.preference.PreferencesManager

class AlarmActivity : AppCompatActivity() {

    private var ringtone: Ringtone? = null
    private var vibrator: Vibrator? = null
    private lateinit var prefs: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Show over lock screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }
        
        setContentView(R.layout.activity_alarm)
        prefs = PreferencesManager(this)

        findViewById<TextView>(R.id.alarm_title)?.text = "守护警报"
        findViewById<TextView>(R.id.alarm_subtitle)?.text = "倒计时结束或心率异常，请确认安全"

        findViewById<Button>(R.id.dismiss_btn).setOnClickListener {
            stopAlarm()
            finish()
        }

        startAlarm()
    }

    private fun startAlarm() {
        Log.d("AlarmActivity", "Starting sound and vibration")
        
        // 1. Sound
        if (prefs.soundEnabled) {
            try {
                val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM) 
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                ringtone = RingtoneManager.getRingtone(applicationContext, uri)
                ringtone?.let {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        it.isLooping = true
                    }
                    it.play()
                }
            } catch (e: Exception) {
                Log.e("AlarmActivity", "Error playing ringtone: ${e.message}")
            }
        }

        // 2. Vibration
        if (prefs.vibrateEnabled) {
            try {
                vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                    vibratorManager.defaultVibrator
                } else {
                    @Suppress("DEPRECATION")
                    getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                }

                vibrator?.let { v ->
                    if (v.hasVibrator()) {
                        val pattern = longArrayOf(0, 500, 500)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            v.vibrate(VibrationEffect.createWaveform(pattern, 0))
                        } else {
                            @Suppress("DEPRECATION")
                            v.vibrate(pattern, 0)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("AlarmActivity", "Error starting vibration: ${e.message}")
            }
        }
    }

    private fun stopAlarm() {
        try {
            ringtone?.stop()
            vibrator?.cancel()
        } catch (e: Exception) {
            Log.e("AlarmActivity", "Error stopping alarm: ${e.message}")
        }
    }

    override fun onDestroy() {
        stopAlarm()
        super.onDestroy()
    }
}
