package com.example.napmonitor.ble

data class HeartRateData(
    val bpm: Int,
    val timestampMs: Long,
    val isSensorContactDetected: Boolean,
    val energyExpended: Int?,
    val rrIntervals: List<Float>
)
