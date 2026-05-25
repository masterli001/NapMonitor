package com.example.napmonitor.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.napmonitor.Constants

class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleAlarm(triggerTimeMs: Long) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("trigger_time", triggerTimeMs)
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        val pendingIntent = PendingIntent.getBroadcast(context, Constants.ALARM_REQUEST_CODE, intent, flags)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeMs, pendingIntent)
        } else {
            val alarmInfo = AlarmManager.AlarmClockInfo(triggerTimeMs, pendingIntent)
            alarmManager.setAlarmClock(alarmInfo, pendingIntent)
        }
    }

    fun cancelAlarm() {
        val intent = Intent(context, AlarmReceiver::class.java)
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        val pendingIntent = PendingIntent.getBroadcast(context, Constants.ALARM_REQUEST_CODE, intent, flags)
        alarmManager.cancel(pendingIntent)
    }
}
