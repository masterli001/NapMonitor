package com.example.napmonitor.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import android.os.ParcelUuid
import android.util.Log
import android.widget.Toast
import com.example.napmonitor.Constants
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow

enum class BleConnectionState {
    DISCONNECTED, SCANNING, CONNECTING, DISCOVERING, CONNECTED
}

class BleHeartRateManager(private val context: Context) {

    private val bluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val adapter: BluetoothAdapter = bluetoothManager.adapter

    private val _connectionState = MutableStateFlow(BleConnectionState.DISCONNECTED)
    val connectionState: StateFlow<BleConnectionState> = _connectionState

    private val _foundDevices = MutableStateFlow<Set<String>>(emptySet())
    val foundDevices: StateFlow<Set<String>> = _foundDevices

    private val _connectedDeviceName = MutableStateFlow<String?>(null)
    val connectedDeviceName: StateFlow<String?> = _connectedDeviceName

    private val _heartRateFlow = MutableSharedFlow<HeartRateData>(replay = 0, extraBufferCapacity = 64)
    val heartRateData: Flow<HeartRateData> = _heartRateFlow

    private var gatt: BluetoothGatt? = null
    private val bgThread: HandlerThread = HandlerThread("BleThread").apply { start() }
    private val handler = Handler(bgThread.looper)

    private var reconnectAttempt = 0
    private var targetMac: String? = null
    @Volatile
    var isMonitoring = false
    private var isFirstHeartRateToastShown = false

    @SuppressLint("MissingPermission")
    fun connectToSavedDevice(mac: String, name: String?) {
        targetMac = mac
        if (isMonitoring) return
        isMonitoring = true
        isFirstHeartRateToastShown = false
        reconnectAttempt = 0
        connectDirect(mac)
    }

    @SuppressLint("MissingPermission")
    fun disconnect() {
        isMonitoring = false
        isFirstHeartRateToastShown = false
        handler.removeCallbacksAndMessages(null)
        gatt?.let { g ->
            g.disconnect()
            g.close()
        }
        gatt = null
        _connectionState.value = BleConnectionState.DISCONNECTED
        // Don't quit the thread — may reconnect later
    }

    @SuppressLint("MissingPermission")
    fun findDevice(callback: (BluetoothDevice) -> Unit) {
        val scanner = adapter.bluetoothLeScanner ?: return

        val filter = ScanFilter.Builder()
            .setServiceUuid(Constants.HEART_RATE_SERVICE_UUID)
            .build()

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        scanner.startScan(listOf(filter), settings, object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val device = result.device
                val name = device.name ?: return
                if (name.contains("vivo", ignoreCase = true) || name.contains("WATCH", ignoreCase = true)) {
                    targetMac = device.address
                    scanner.stopScan(this)
                    callback(device)
                }
            }

            override fun onScanFailed(errorCode: Int) {
                scanner.stopScan(this)
            }
        })
    }

    @SuppressLint("MissingPermission")
    private fun connectDirect(mac: String) {
        // GUARD: If already connected or connecting, do nothing
        if (_connectionState.value == BleConnectionState.CONNECTED || 
            _connectionState.value == BleConnectionState.CONNECTING ||
            _connectionState.value == BleConnectionState.DISCOVERING) {
            Log.d("BleManager", "connectDirect ignored: already in state ${_connectionState.value}")
            return
        }

        handler.post { Toast.makeText(context, "发现设备，正在连接...", Toast.LENGTH_SHORT).show() }
        _connectionState.value = BleConnectionState.CONNECTING
        val device = adapter.getRemoteDevice(mac)
        gatt = device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE, BluetoothDevice.PHY_LE_1M_MASK, handler)
    }

    @SuppressLint("MissingPermission")
    fun scanAndConnect() {
        // GUARD: Prevent starting scan if already monitoring or connected
        if (isMonitoring || _connectionState.value != BleConnectionState.DISCONNECTED) {
            Log.d("BleManager", "scanAndConnect ignored: isMonitoring=$isMonitoring, state=${_connectionState.value}")
            return
        }

        isMonitoring = true // Set early to sync UI
        val scanner = adapter.bluetoothLeScanner ?: return
        _connectionState.value = BleConnectionState.SCANNING
        _foundDevices.value = emptySet()
        isFirstHeartRateToastShown = false

        // Debug: Completely empty settings and null filters to see everything
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        val scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val device = result.device
                val deviceName = device.name ?: "Unknown"
                val deviceInfo = "$deviceName [${device.address}]"
                
                // Update debug list
                if (!foundDevices.value.contains(deviceInfo)) {
                    _foundDevices.value = foundDevices.value + deviceInfo
                }

                Log.d("BleDebug", "发现设备: $deviceInfo")

                val lowerName = deviceName.lowercase()
                // Targeted matching for vivo WATCH 3 (3EB suffix)
                if (lowerName.contains("vivo") || lowerName.contains("3eb")) {
                    Log.d("BleDebug", "命中精准关键字 (vivo/3EB)，尝试连接: $deviceName")
                    handler.removeCallbacksAndMessages("scan_timeout")
                    scanner.stopScan(this)
                    targetMac = device.address
                    connectDirect(device.address)
                }
            }

            override fun onScanFailed(errorCode: Int) {
                Log.e("BleDebug", "扫描失败: $errorCode")
                handler.removeCallbacksAndMessages("scan_timeout")
                scanner.stopScan(this)
                _connectionState.value = BleConnectionState.DISCONNECTED
            }
        }

        // Pass null for filters to see EVERYTHING
        scanner.startScan(null, settings, scanCallback)

        // 15 second timeout for debugging
        handler.postAtTime({
            scanner.stopScan(scanCallback)
            if (_connectionState.value == BleConnectionState.SCANNING) {
                _connectionState.value = BleConnectionState.DISCONNECTED
                Toast.makeText(context, "未发现心率信号，请确认手表已开启心率广播", Toast.LENGTH_LONG).show()
            }
        }, "scan_timeout", android.os.SystemClock.uptimeMillis() + 15000)
    }

    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        reconnectAttempt = 0
                        _connectionState.value = BleConnectionState.DISCOVERING
                        val name = gatt.device.name ?: "vivo WATCH 3"
                        _connectedDeviceName.value = name
                        Log.d("BleManager", "GATT Connected: $name. Discovering services...")
                        handler.post { Toast.makeText(context, "已连接到: $name", Toast.LENGTH_SHORT).show() }
                        gatt.discoverServices()
                    } else {
                        Log.e("BleManager", "GATT Connect failed with status: $status")
                        handleDisconnect()
                    }
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.d("BleManager", "GATT Disconnected")
                    _connectedDeviceName.value = null
                    handleDisconnect()
                }
            }
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            Log.d("BleManager", "onServicesDiscovered status: $status")
            if (status != BluetoothGatt.GATT_SUCCESS) return

            val service = gatt.getService(Constants.HEART_RATE_SERVICE_UUID.uuid)
            if (service == null) {
                Log.e("BleManager", "Heart Rate Service NOT found!")
                return
            }
            
            val characteristic = service.getCharacteristic(Constants.HEART_RATE_MEASUREMENT_CHAR_UUID)
            if (characteristic == null) {
                Log.e("BleManager", "Heart Rate Measurement Char NOT found!")
                return
            }

            val cccd = characteristic.getDescriptor(Constants.CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR)
            if (cccd != null) {
                Log.d("BleManager", "Enabling notifications for 2A37")
                gatt.setCharacteristicNotification(characteristic, true)
                cccd.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                gatt.writeDescriptor(cccd)
            } else {
                Log.e("BleManager", "CCCD Descriptor 2902 NOT found!")
            }
            _connectionState.value = BleConnectionState.CONNECTED
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            if (characteristic.uuid == Constants.HEART_RATE_MEASUREMENT_CHAR_UUID) {
                val data = parseHeartRate(characteristic.value)
                if (data != null) {
                    if (!isFirstHeartRateToastShown) {
                        isFirstHeartRateToastShown = true
                        handler.post { Toast.makeText(context, "监测已开始", Toast.LENGTH_SHORT).show() }
                    }
                    _heartRateFlow.tryEmit(data)
                }
            }
        }
    }

    private fun handleDisconnect() {
        _connectionState.value = BleConnectionState.DISCONNECTED
        gatt?.close()
        gatt = null

        if (!isMonitoring) return

        val delayMs = when (reconnectAttempt) {
            0 -> 1000L
            1 -> 2000L
            2 -> 4000L
            3 -> 8000L
            4 -> 16000L
            else -> 30000L
        }
        reconnectAttempt++
        handler.postDelayed({
            if (isMonitoring && targetMac != null) {
                connectDirect(targetMac!!)
            }
        }, delayMs)
    }

    private fun parseHeartRate(value: ByteArray): HeartRateData? {
        if (value.isEmpty()) return null

        var offset = 0
        val flags = value[offset++].toInt() and 0xFF
        val isUint16 = (flags and 0x01) != 0
        val contactSupported = (flags and 0x04) != 0
        val contactDetected = (flags and 0x02) != 0
        val energyPresent = (flags and 0x08) != 0
        val rrPresent = (flags and 0x10) != 0

        val bpm = if (isUint16) {
            if (offset + 1 >= value.size) return null
            val v = ((value[offset].toInt() and 0xFF) or
                    ((value[offset + 1].toInt() and 0xFF) shl 8))
            offset += 2
            v
        } else {
            value[offset++].toInt() and 0xFF
        }

        if (bpm < Constants.MIN_HEART_RATE_BPM || bpm > Constants.MAX_HEART_RATE_BPM) return null

        var energy: Int? = null
        if (energyPresent && offset + 1 < value.size) {
            energy = ((value[offset].toInt() and 0xFF) or
                    ((value[offset + 1].toInt() and 0xFF) shl 8))
            offset += 2
        }

        val rrList = mutableListOf<Float>()
        if (rrPresent) {
            while (offset + 1 < value.size) {
                val rr = ((value[offset].toInt() and 0xFF) or
                        ((value[offset + 1].toInt() and 0xFF) shl 8)) / 1024.0f
                rrList.add(rr)
                offset += 2
            }
        }

        return HeartRateData(
            bpm = bpm,
            timestampMs = System.currentTimeMillis(),
            isSensorContactDetected = !contactSupported || contactDetected,
            energyExpended = energy,
            rrIntervals = rrList
        )
    }
}
