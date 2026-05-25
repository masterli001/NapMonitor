# NapMonitor - Smart Nap Guardian for Android

An Android application that monitors your heart rate during naps via BLE smartwatches/bands, providing real-time safety protection and smart wake-up alarms.

## Features

- **BLE Real-time Heart Rate Monitoring** -- Connects to standard BLE heart rate service (0x180D), parses HR measurement characteristics (0x2A37) with full UINT8/UINT16, RR-interval, and energy expenditure support
- **7-State Sleep Detection Engine** -- DISCONNECTED -> BASELINE_BUILDING -> MONITORING -> SLEEP_DETECTED -> ALARM_SCHEDULED -> ALARM_FIRED -> AWAKE_AFTER_SLEEP
- **Safety Heart Rate Alarm** -- Triggers emergency wake-up when HR exceeds configurable threshold (default 80bpm) for a confirmation window (1-5 min)
- **Countdown Alarm System** -- Configurable guard duration (1-240 min) with precise countdown, full-screen alarm + vibration + notification
- **Foreground Service with WakeLock** -- Persistent background monitoring even when screen is off
- **Auto-Reconnect** -- Exponential backoff strategy (1s -> 2s -> 4s -> 8s -> 16s -> 30s) on BLE disconnection
- **Reactive Preferences** -- SharedPreferences + Flow callbackFlow for real-time config sync

## Tech Stack

- **Language**: Kotlin
- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 34 (Android 14)
- **Architecture**: Android Jetpack (ViewPager2, ViewModel, Coroutines, Flow)
- **BLE**: Android BLE GATT protocol stack
- **Build**: Gradle KTS + Kotlin DSL

## Supported Devices

Currently tested with **vivo WATCH 3**. Should work with any device broadcasting the standard BLE Heart Rate Service (UUID: 0x180D).

## Building

```bash
./gradlew assembleDebug
```

## Usage

1. Grant Bluetooth, Notification, and Exact Alarm permissions
2. Ensure your smartwatch/fitness band is broadcasting heart rate data
3. Configure guard duration and heart rate threshold in Settings tab
4. Tap "Start Guard" to begin monitoring
5. The app will wake you up when the timer expires or if abnormal heart rate is detected

## AI-Assisted Development

This project was built from scratch using AI-assisted coding (Claude Code), covering architecture design, BLE protocol implementation, state machine design, exception handling, and UI development. We're migrating to MiMo-V2.5-Pro to continue feature development.

## License

MIT
