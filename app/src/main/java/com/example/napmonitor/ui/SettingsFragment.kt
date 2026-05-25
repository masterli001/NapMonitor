package com.example.napmonitor.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import com.example.napmonitor.R
import com.example.napmonitor.preference.PreferencesManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.slider.Slider
import java.util.Locale

class SettingsFragment : Fragment() {

    private lateinit var prefs: PreferencesManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_settings, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        try {
            val mainActivity = requireActivity() as MainActivity
            prefs = mainActivity.getPreferencesManager()

            // Sliders
            val durationSlider = view.findViewById<Slider>(R.id.duration_slider)
            val durationValueText = view.findViewById<TextView>(R.id.duration_value_text)
            val hrSlider = view.findViewById<Slider>(R.id.hr_threshold_slider)
            val hrThresholdText = view.findViewById<TextView>(R.id.hr_threshold_text)
            val alarmDelaySlider = view.findViewById<Slider>(R.id.alarm_delay_slider)
            val alarmDelayText = view.findViewById<TextView>(R.id.alarm_delay_text)
            
            // Switches
            val vibrateSwitch = view.findViewById<SwitchCompat>(R.id.vibrate_switch)
            val soundSwitch = view.findViewById<SwitchCompat>(R.id.sound_switch)
            
            val applyBtn = view.findViewById<MaterialButton>(R.id.apply_btn)

            // 1. Setup Duration Slider
            durationSlider?.value = (prefs.guardHours * 60 + prefs.guardMinutes).toFloat().coerceIn(1f, 240f)
            durationValueText?.text = String.format(Locale.getDefault(), "%d 分钟", durationSlider?.value?.toInt())
            durationSlider?.addOnChangeListener { _, value, _ ->
                durationValueText?.text = String.format(Locale.getDefault(), "%d 分钟", value.toInt())
            }

            // 2. Setup HR Threshold Slider
            hrSlider?.value = prefs.hrThreshold.toFloat().coerceIn(40f, 150f)
            hrThresholdText?.text = String.format(Locale.getDefault(), "%d bpm", hrSlider?.value?.toInt())
            hrSlider?.addOnChangeListener { _, value, _ ->
                hrThresholdText?.text = String.format(Locale.getDefault(), "%d bpm", value.toInt())
            }

            // 3. Setup Alarm Delay Slider
            alarmDelaySlider?.value = prefs.alarmDelayMinutes.toFloat().coerceIn(1f, 5f)
            alarmDelayText?.text = String.format(Locale.getDefault(), "%d 分钟", alarmDelaySlider?.value?.toInt())
            alarmDelaySlider?.addOnChangeListener { _, value, _ ->
                alarmDelayText?.text = String.format(Locale.getDefault(), "%d 分钟", value.toInt())
            }

            // 4. Setup Switches
            vibrateSwitch?.isChecked = prefs.vibrateEnabled
            soundSwitch?.isChecked = prefs.soundEnabled

            // 5. Apply Button Logic
            applyBtn?.setOnClickListener {
                val totalMinutes = durationSlider?.value?.toInt() ?: 30
                prefs.guardHours = totalMinutes / 60
                prefs.guardMinutes = totalMinutes % 60
                
                prefs.hrThreshold = hrSlider?.value?.toInt() ?: 80
                prefs.alarmDelayMinutes = alarmDelaySlider?.value?.toInt() ?: 3
                prefs.vibrateEnabled = vibrateSwitch?.isChecked ?: true
                prefs.soundEnabled = soundSwitch?.isChecked ?: true
                
                Toast.makeText(requireContext(), "守护配置已更新", Toast.LENGTH_SHORT).show()
                mainActivity.findViewById<com.google.android.material.tabs.TabLayout>(R.id.tab_layout)?.getTabAt(0)?.select()
            }
        } catch (e: Exception) {
            Log.e("SettingsError", "Init failed: ${e.message}")
        }
    }
}
