package com.example.napmonitor.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.napmonitor.Constants
import com.example.napmonitor.ui.MainActivity

class NotificationHelper(private val context: Context) {

    fun buildServiceNotification(stateText: String, bpm: Int, countdownText: String?): Notification {
        val openIntent = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val hrText = if (bpm > 0) "心率: $bpm bpm" else ""
        val content = buildString {
            append(stateText)
            if (hrText.isNotEmpty()) {
                append(" · ")
                append(hrText)
            }
            if (countdownText != null) {
                append(" · ")
                append(countdownText)
            }
        }

        return NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Smart Guard 实时守护中")
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(openIntent)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    fun buildAlarmNotification(): Notification {
        val dismissIntent = PendingIntent.getBroadcast(
            context, 100,
            Intent(context, AlarmDismissReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val fullScreenIntent = PendingIntent.getActivity(
            context, 200,
            Intent(context, com.example.napmonitor.ui.AlarmActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, Constants.ALARM_CHANNEL_ID)
            .setContentTitle("守护提醒")
            .setContentText("预设时间已到或心率异常，请检查")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenIntent, true)
            .setAutoCancel(true)
            .addAction(android.R.drawable.ic_media_pause, "关闭", dismissIntent)
            .build()
    }
}
