package com.example.napmonitor.sleep

enum class SleepState {
    DISCONNECTED,
    BASELINE_BUILDING,
    MONITORING,
    SLEEP_DETECTED,
    ALARM_SCHEDULED,
    ALARM_FIRED,
    AWAKE_AFTER_SLEEP,
    CANCELLED
}
