package com.example.napmonitor.ui

git initgit initgit remote add origin https://github.com/masterli001/NapMonitor.gitgit add .git commit -m "First release of NapMonitor"import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.napmonitor.R
import com.example.napmonitor.service.NapMonitorService
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale

class MonitorFragment : Fragment() {

    private lateinit var hrText: TextView
    private lateinit var countdownText: TextView
    private lateinit var deviceStatusChip: TextView
    private lateinit var startStopBtn: MaterialButton
    private lateinit var monitoringIndicator: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_monitor, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        hrText = view.findViewById(R.id.hr_text)
        countdownText = view.findViewById(R.id.countdown_text)
        deviceStatusChip = view.findViewById(R.id.device_status_chip)
        startStopBtn = view.findViewById(R.id.start_stop_btn)
        monitoringIndicator = view.findViewById(R.id.monitoring_indicator)

        startStopBtn.setOnClickListener {
            val activity = requireActivity() as MainActivity
            val service = activity.boundService
            
            if (service != null && service.getBleManager().isMonitoring) {
                activity.startService(Intent(activity, NapMonitorService::class.java).apply { action = "STOP" })
            } else {
                if (!activity.isBluetoothEnabled()) {
                    startActivity(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
                    return@setOnClickListener
                }
                activity.startService(Intent(activity, NapMonitorService::class.java).apply { action = "START" })
            }
        }
        
        observeServiceData()
    }

    private fun observeServiceData() {
        val activity = requireActivity() as MainActivity
        viewLifecycleOwner.lifecycleScope.launch {
            while (activity.boundService == null) { kotlinx.coroutines.delay(200) }
            
            val service = activity.boundService!!
            val bleManager = service.getBleManager()

            // State sync
            launch {
                bleManager.connectionState.collect { _ ->
                    updateUiState(bleManager.isMonitoring)
                }
            }

            // Device Name sync
            launch {
                bleManager.connectedDeviceName.collectLatest { name ->
                    deviceStatusChip.text = name?.uppercase() ?: "未连接设备"
                }
            }

            // Countdown Sync
            launch {
                service.remainingTimeSeconds.collect { remaining ->
                    if (remaining > 0) {
                        val hours = remaining / 3600
                        val min = (remaining % 3600) / 60
                        val sec = remaining % 60
                        countdownText.text = String.format(Locale.getDefault(), "倒计时剩余：%02d:%02d:%02d", hours, min, sec)
                    } else {
                        countdownText.text = "倒计时已结束"
                    }
                }
            }

            // Heart Rate Sync
            launch {
                bleManager.heartRateData.collect { data ->
                    hrText.text = data.bpm.toString()
                }
            }
            
            updateUiState(bleManager.isMonitoring)
        }
    }

    private fun updateUiState(isMonitoring: Boolean) {
        if (isMonitoring) {
            startStopBtn.text = "停止守护"
            monitoringIndicator.visibility = View.VISIBLE
        } else {
            startStopBtn.text = "开始守护"
            hrText.text = "--"
            monitoringIndicator.visibility = View.INVISIBLE
            countdownText.text = "等待开始..."
        }
    }
}
