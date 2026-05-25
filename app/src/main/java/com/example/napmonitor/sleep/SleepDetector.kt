package com.example.napmonitor.sleep

import com.example.napmonitor.Constants
import com.example.napmonitor.ble.HeartRateData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.collections.ArrayDeque

data class SleepEvent(
    val state: SleepState,
    val bpm: Int = 0,
    val baseline: Double = Double.NaN
)

class SleepDetector(
    private var baselineOverride: Int?,
    private var confirmationMinutes: Int,
    private var hrThreshold: Int = 80,
    private var alarmDelayMinutes: Int = 3
) {
    private val hrHistory = ArrayDeque<HeartRateData>()
    private var highHrConsecutiveSeconds = 0
    private var monitoringStartTime = 0L

    private val _events = MutableSharedFlow<SleepEvent>(extraBufferCapacity = 32)
    val events: Flow<SleepEvent> = _events

    private val _currentBpm = MutableStateFlow(0)
    val currentBpm: StateFlow<Int> = _currentBpm

    private var lastEmittedState = SleepState.DISCONNECTED

    fun updateConfig(baselineOverride: Int?, confirmationMinutes: Int, hrThreshold: Int, alarmDelayMinutes: Int) {
        this.baselineOverride = baselineOverride
        this.confirmationMinutes = confirmationMinutes
        this.hrThreshold = hrThreshold
        this.alarmDelayMinutes = alarmDelayMinutes
    }

    fun startMonitoring() {
        monitoringStartTime = System.currentTimeMillis()
        highHrConsecutiveSeconds = 0
        emit(SleepState.MONITORING)
    }

    fun onHeartRateData(data: HeartRateData) {
        if (!data.isSensorContactDetected) return
        _currentBpm.value = data.bpm
        hrHistory.addLast(data)

        // Keep 5 mins of history
        val limit = System.currentTimeMillis() - 300_000L
        while (hrHistory.isNotEmpty() && hrHistory.first().timestampMs < limit) {
            hrHistory.removeFirst()
        }

        // Grace period: No HR alarm in the first 2 minutes
        val elapsedMillis = System.currentTimeMillis() - monitoringStartTime
        if (elapsedMillis < 120_000L) return

        // Safety: High Heart Rate Alarm with Confirmation Window
        if (data.bpm >= hrThreshold) {
            highHrConsecutiveSeconds++
            if (highHrConsecutiveSeconds >= alarmDelayMinutes * 60) {
                emit(SleepState.ALARM_FIRED)
                highHrConsecutiveSeconds = 0
            }
        } else {
            highHrConsecutiveSeconds = 0
        }
    }

    fun cancel() {
        monitoringStartTime = 0L
        highHrConsecutiveSeconds = 0
        _currentBpm.value = 0
        hrHistory.clear()
        emit(SleepState.CANCELLED)
    }

    fun isSleepDetected(): Boolean = true // Simplified for fixed-time nap

    private fun emit(state: SleepState) {
        if (state == lastEmittedState) return
        lastEmittedState = state
        _events.tryEmit(SleepEvent(state = state, bpm = _currentBpm.value))
    }
}
