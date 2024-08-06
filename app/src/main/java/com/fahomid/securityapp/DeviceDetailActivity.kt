package com.fahomid.securityapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import androidx.viewpager2.widget.ViewPager2

// Activity to display detailed information about a device
class DeviceDetailActivity : AppCompatActivity() {

    // ViewPager2 to handle swiping between tabs
    private lateinit var viewPager: ViewPager2
    // TabLayout to display the tabs
    private lateinit var tabLayout: TabLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_detail)

        // Initialize ViewPager2 and TabLayout
        viewPager = findViewById(R.id.view_pager)
        tabLayout = findViewById(R.id.tab_layout)

        // Set up the ViewPager2 with an adapter
        val adapter = DeviceDetailPagerAdapter(this)
        viewPager.adapter = adapter

        // Attach the TabLayout to the ViewPager2 and set the tab titles
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Live Feed"          // Title for the first tab
                1 -> "Recorded Clips"     // Title for the second tab
                else -> null
            }
        }.attach()
    }

    // Function to get the live feed URL passed through the intent
    fun getVideoFeed(): String? {
        return intent.getStringExtra("LIVE_FEED_URL")
    }

    // Function to get the clips URL passed through the intent
    fun getClips(): String? {
        return intent.getStringExtra("CLIPS_URL")
    }
}
