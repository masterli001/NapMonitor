package com.example.napmonitor

import android.os.ParcelUuid
import java.util.UUID

object Constants {
    val HEART_RATE_SERVICE_UUID: ParcelUuid =
        ParcelUuid.fromString("0000180D-0000-1000-8000-00805F9B34FB")
    val HEART_RATE_MEASUREMENT_CHAR_UUID: UUID =
        UUID.fromString("00002A37-0000-1000-8000-00805F9B34FB")
    val CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR: UUID =
        UUID.fromString("00002902-0000-1000-8000-00805F9B34FB")

    const val FOREGROUND_SERVICE_ID = 1001
    const val NOTIFICATION_CHANNEL_ID = "nap_monitor_channel"
    const val ALARM_CHANNEL_ID = "nap_alarm_channel"
    const val ALARM_REQUEST_CODE = 2001

    const val BASELINE_BUILD_SECONDS = 180L
    const val SLEEP_CONFIRMATION_SECONDS = 300L
    const val LOW_VARIABILITY_THRESHOLD_BPM = 5.0
    const val HR_DROP_RATIO = 0.90
    const val HR_WAKE_RATIO = 0.95
    const val WINDOW_SIZE_SECONDS = 60
    const val MIN_HEART_RATE_BPM = 40
    const val MAX_HEART_RATE_BPM = 200
    const val MIN_BASELINE_BPM = 45
    const val MAX_BASELINE_BPM = 100

    const val DEFAULT_NAP_DURATION_MINUTES = 30
    const val DEFAULT_WINDOW_START_HOUR = 12
    const val DEFAULT_WINDOW_START_MINUTE = 0
    const val DEFAULT_WINDOW_END_HOUR = 17
    const val DEFAULT_WINDOW_END_MINUTE = 0
    const val DEFAULT_HR_THRESHOLD = 80

    const val PREFS_NAME = "nap_monitor_prefs"
    const val KEY_WINDOW_START_HOUR = "window_start_hour"
    const val KEY_WINDOW_START_MINUTE = "window_start_minute"
    const val KEY_WINDOW_END_HOUR = "window_end_hour"
    const val KEY_WINDOW_END_MINUTE = "window_end_minute"
    const val KEY_GUARD_HOURS = "guard_hours"
    const val KEY_GUARD_MINUTES = "guard_minutes"
    const val KEY_BASELINE_OVERRIDE = "baseline_hr_override"
    const val KEY_SENSITIVITY = "sensitivity"
    const val KEY_DEVICE_MAC = "device_mac_address"
    const val KEY_DEVICE_NAME = "device_name"
    
    const val KEY_HR_THRESHOLD = "hr_threshold"
    const val KEY_VIBRATE_ENABLED = "vibrate_enabled"
    const val KEY_SOUND_ENABLED = "sound_enabled"
    const val KEY_ALARM_VOLUME = "alarm_volume"
    const val KEY_SMART_WAKE_ENABLED = "smart_wake_enabled"
    const val KEY_ALARM_DELAY = "alarm_delay_minutes"
    
    const val DEFAULT_ALARM_DELAY = 3
}
