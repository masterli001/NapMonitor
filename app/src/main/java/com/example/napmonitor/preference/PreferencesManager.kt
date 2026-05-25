package com.example.napmonitor.preference

import android.content.Context
import android.content.SharedPreferences
import com.example.napmonitor.Constants
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)

    var windowStartHour: Int
        get() = prefs.getInt(Constants.KEY_WINDOW_START_HOUR, Constants.DEFAULT_WINDOW_START_HOUR)
        set(value) = prefs.edit().putInt(Constants.KEY_WINDOW_START_HOUR, value).apply()

    var windowStartMinute: Int
        get() = prefs.getInt(Constants.KEY_WINDOW_START_MINUTE, Constants.DEFAULT_WINDOW_START_MINUTE)
        set(value) = prefs.edit().putInt(Constants.KEY_WINDOW_START_MINUTE, value).apply()

    var windowEndHour: Int
        get() = prefs.getInt(Constants.KEY_WINDOW_END_HOUR, Constants.DEFAULT_WINDOW_END_HOUR)
        set(value) = prefs.edit().putInt(Constants.KEY_WINDOW_END_HOUR, value).apply()

    var windowEndMinute: Int
        get() = prefs.getInt(Constants.KEY_WINDOW_END_MINUTE, Constants.DEFAULT_WINDOW_END_MINUTE)
        set(value) = prefs.edit().putInt(Constants.KEY_WINDOW_END_MINUTE, value).apply()

    var guardHours: Int
        get() = prefs.getInt(Constants.KEY_GUARD_HOURS, 0)
        set(value) = prefs.edit().putInt(Constants.KEY_GUARD_HOURS, value).apply()

    var guardMinutes: Int
        get() = prefs.getInt(Constants.KEY_GUARD_MINUTES, 30)
        set(value) = prefs.edit().putInt(Constants.KEY_GUARD_MINUTES, value).apply()

    var baselineOverride: Int?
        get() {
            val v = prefs.getInt(Constants.KEY_BASELINE_OVERRIDE, -1)
            return if (v == -1) null else v
        }
        set(value) {
            if (value != null) {
                prefs.edit().putInt(Constants.KEY_BASELINE_OVERRIDE, value).apply()
            } else {
                prefs.edit().remove(Constants.KEY_BASELINE_OVERRIDE).apply()
            }
        }

    var sensitivity: Int
        get() = prefs.getInt(Constants.KEY_SENSITIVITY, 1)
        set(value) = prefs.edit().putInt(Constants.KEY_SENSITIVITY, value).apply()

    val confirmationMinutes: Int
        get() = when (sensitivity) {
            0 -> 7
            1 -> 5
            2 -> 3
            else -> 5
        }

    var deviceMac: String?
        get() = prefs.getString(Constants.KEY_DEVICE_MAC, null)
        set(value) = prefs.edit().putString(Constants.KEY_DEVICE_MAC, value).apply()

    var deviceName: String?
        get() = prefs.getString(Constants.KEY_DEVICE_NAME, null)
        set(value) = prefs.edit().putString(Constants.KEY_DEVICE_NAME, value).apply()

    var hrThreshold: Int
        get() = prefs.getInt(Constants.KEY_HR_THRESHOLD, Constants.DEFAULT_HR_THRESHOLD)
        set(value) = prefs.edit().putInt(Constants.KEY_HR_THRESHOLD, value).apply()

    var vibrateEnabled: Boolean
        get() = prefs.getBoolean(Constants.KEY_VIBRATE_ENABLED, true)
        set(value) = prefs.edit().putBoolean(Constants.KEY_VIBRATE_ENABLED, value).apply()

    var soundEnabled: Boolean
        get() = prefs.getBoolean(Constants.KEY_SOUND_ENABLED, true)
        set(value) = prefs.edit().putBoolean(Constants.KEY_SOUND_ENABLED, value).apply()

    var alarmVolume: Int
        get() = prefs.getInt(Constants.KEY_ALARM_VOLUME, 70)
        set(value) = prefs.edit().putInt(Constants.KEY_ALARM_VOLUME, value).apply()

    var smartWakeEnabled: Boolean
        get() = prefs.getBoolean(Constants.KEY_SMART_WAKE_ENABLED, true)
        set(value) = prefs.edit().putBoolean(Constants.KEY_SMART_WAKE_ENABLED, value).apply()

    var alarmDelayMinutes: Int
        get() = prefs.getInt(Constants.KEY_ALARM_DELAY, Constants.DEFAULT_ALARM_DELAY)
        set(value) = prefs.edit().putInt(Constants.KEY_ALARM_DELAY, value).apply()

    fun isInNapWindow(): Boolean {
        val now = java.util.Calendar.getInstance()
        val currentMin = now.get(java.util.Calendar.HOUR_OF_DAY) * 60 + now.get(java.util.Calendar.MINUTE)
        val startMin = windowStartHour * 60 + windowStartMinute
        val endMin = windowEndHour * 60 + windowEndMinute
        return currentMin in startMin..endMin
    }

    fun onChange(): Flow<String> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key != null) {
                trySend(key)
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }
}
