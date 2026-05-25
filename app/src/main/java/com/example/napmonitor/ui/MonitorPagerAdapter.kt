package com.example.napmonitor.ui

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class MonitorPagerAdapter(activity: MainActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> MonitorFragment()
        1 -> SettingsFragment()
        else -> throw IllegalStateException()
    }
}
