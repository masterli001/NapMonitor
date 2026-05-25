package com.example.napmonitor.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmDismissReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        nm.cancel(2001)
        val serviceIntent = Intent(context, NapMonitorService::class.java).apply {
            action = "STOP"
        }
        context.stopService(serviceIntent)
    }
}
