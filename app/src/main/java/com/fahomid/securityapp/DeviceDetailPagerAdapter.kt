package com.fahomid.securityapp

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

// Adapter for managing the fragments in the ViewPager2 of DeviceDetailActivity
class DeviceDetailPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    // Return the number of fragments
    override fun getItemCount(): Int = 2

    // Create and return the fragment for the given position
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> LiveFeedFragment()       // Return LiveFeedFragment for the first tab
            1 -> EventClipsFragment()     // Return EventClipsFragment for the second tab
            else -> throw IllegalStateException("Unexpected position $position")
        }
    }
}
