package com.example.napmonitor.service

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import com.example.napmonitor.Constants
import com.example.napmonitor.alarm.AlarmScheduler
import com.example.napmonitor.ble.BleHeartRateManager
import com.example.napmonitor.preference.PreferencesManager
import com.example.napmonitor.sleep.SleepDetector
import com.example.napmonitor.sleep.SleepState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class NapMonitorService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var bleManager: BleHeartRateManager
    private lateinit var sleepDetector: SleepDetector
    private lateinit var alarmScheduler: AlarmScheduler
    private lateinit var notificationHelper: NotificationHelper
    private lateinit var preferencesManager: PreferencesManager
    
    private var wakeLock: PowerManager.WakeLock? = null

    private val _remainingTimeSeconds = MutableStateFlow(0L)
    val remainingTimeSeconds: StateFlow<Long> = _remainingTimeSeconds

    private var collectJob: Job? = null
    private var sleepEventJob: Job? = null
    private var countdownJob: Job? = null
    private var alarmTriggerMs: Long = 0L

    inner class LocalBinder : android.os.Binder() {
        fun getService(): NapMonitorService = this@NapMonitorService
    }
    private val binder = LocalBinder()
    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        notificationHelper = NotificationHelper(this)
        preferencesManager = PreferencesManager(this)
        bleManager = BleHeartRateManager(this)
        alarmScheduler = AlarmScheduler(this)

        sleepDetector = SleepDetector(
            baselineOverride = preferencesManager.baselineOverride,
            confirmationMinutes = preferencesManager.confirmationMinutes,
            hrThreshold = preferencesManager.hrThreshold,
            alarmDelayMinutes = preferencesManager.alarmDelayMinutes
        )

        startForegroundWithNotification("智能守护仪已就绪")
        observeSleepEvents()
        observePreferenceChanges()
    }

    private fun startForegroundWithNotification(stateText: String) {
        val notification = notificationHelper.buildServiceNotification(
            stateText,
            if (::sleepDetector.isInitialized) sleepDetector.currentBpm.value else 0,
            null
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(Constants.FOREGROUND_SERVICE_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC or ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH)
        } else {
            startForeground(Constants.FOREGROUND_SERVICE_ID, notification)
        }
    }

    private fun observePreferenceChanges() {
        scope.launch {
            preferencesManager.onChange().collectLatest { key ->
                if (key == Constants.KEY_SENSITIVITY || key == Constants.KEY_HR_THRESHOLD || key == Constants.KEY_ALARM_DELAY) {
                    sleepDetector.updateConfig(
                        baselineOverride = preferencesManager.baselineOverride,
                        confirmationMinutes = preferencesManager.confirmationMinutes,
                        hrThreshold = preferencesManager.hrThreshold,
                        alarmDelayMinutes = preferencesManager.alarmDelayMinutes
                    )
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "START" -> startMonitoring()
            "STOP" -> stopMonitoring()
        }
        return START_STICKY
    }

    fun getBleManager() = bleManager
    fun getSleepDetector() = sleepDetector

    private fun startMonitoring() {
        if (bleManager.isMonitoring) return
        
        // Acquire WakeLock
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SmartGuard::MonitoringLock").apply {
            acquire(24 * 60 * 60 * 1000L) // Max 24h
        }

        val mac = preferencesManager.deviceMac
        if (mac != null) {
            bleManager.connectToSavedDevice(mac, preferencesManager.deviceName)
        } else {
            bleManager.scanAndConnect()
        }

        sleepDetector.startMonitoring()
        
        collectJob?.cancel()
        collectJob = scope.launch {
            bleManager.heartRateData.collect { hrData ->
                sleepDetector.onHeartRateData(hrData)
                updateNotification("正在实时监测中")
            }
        }

        startNapCountdown()
    }

    private fun startNapCountdown() {
        countdownJob?.cancel()
        val totalSec = (preferencesManager.guardHours * 3600L) + (preferencesManager.guardMinutes * 60L)
        alarmTriggerMs = System.currentTimeMillis() + (totalSec * 1000L)
        
        Log.d("NapTimer", "Timer Started: Target=$totalSec sec")
        
        countdownJob = scope.launch {
            while (System.currentTimeMillis() < alarmTriggerMs) {
                val remaining = (alarmTriggerMs - System.currentTimeMillis()) / 1000
                _remainingTimeSeconds.value = remaining
                
                val hours = remaining / 3600
                val min = (remaining % 3600) / 60
                val sec = remaining % 60
                
                val countdownText = if (hours > 0) String.format("%02d:%02d:%02d", hours, min, sec) else String.format("%02d:%02d", min, sec)
                updateNotification("守护中", "剩余 $countdownText")
                delay(1000)
            }
            _remainingTimeSeconds.value = 0
            triggerAlarm("守护倒计时结束")
        }
    }

    private fun triggerAlarm(reason: String) {
        Log.d("NapService", "Triggering alarm: $reason")
        val alarmIntent = Intent(this, com.example.napmonitor.ui.AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        startActivity(alarmIntent)
        
        val nm = getSystemService(NotificationManager::class.java)
        nm.notify(Constants.ALARM_REQUEST_CODE, notificationHelper.buildAlarmNotification())
        
        stopMonitoring()
    }

    private fun observeSleepEvents() {
        sleepEventJob?.cancel()
        sleepEventJob = scope.launch {
            sleepDetector.events.collect { event ->
                if (event.state == SleepState.ALARM_FIRED) {
                    triggerAlarm("心率异常安全报警")
                }
            }
        }
    }

    private fun updateNotification(stateText: String, countdownText: String? = null) {
        val nm = getSystemService(NotificationManager::class.java)
        nm.notify(
            Constants.FOREGROUND_SERVICE_ID,
            notificationHelper.buildServiceNotification(stateText, sleepDetector.currentBpm.value, countdownText)
        )
    }

    private fun stopMonitoring() {
        bleManager.disconnect()
        alarmScheduler.cancelAlarm()
        sleepDetector.cancel()
        collectJob?.cancel()
        countdownJob?.cancel()
        alarmTriggerMs = 0L
        _remainingTimeSeconds.value = 0
        
        wakeLock?.let { if (it.isHeld) it.release() }
        wakeLock = null

        updateNotification("守护已停止")
    }

    override fun onDestroy() {
        stopMonitoring()
        scope.cancel()
        super.onDestroy()
    }
}
