package com.example.napmonitor

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class NapMonitorApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)

            val serviceChannel = NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_ID,
                "Nap Monitor",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Background monitoring status"
                setShowBadge(false)
            }
            nm.createNotificationChannel(serviceChannel)

            val alarmChannel = NotificationChannel(
                Constants.ALARM_CHANNEL_ID,
                "Nap Alarm",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Nap wake-up alarm"
                setSound(null, null)
            }
            nm.createNotificationChannel(alarmChannel)
        }
    }
}
