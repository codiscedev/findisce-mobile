package com.findisce.mobile

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment

class MainActivity : AppCompatActivity() {

    lateinit var dashboardTab: RelativeLayout
    lateinit var wealthTab: RelativeLayout
    lateinit var healthTab: RelativeLayout
    lateinit var settingsTab: RelativeLayout
    lateinit var dashboardTabTitle: TextView
    lateinit var wealthTabTitle: TextView
    lateinit var healthTabTitle: TextView
    lateinit var settingsTabTitle: TextView
    lateinit var dashboardTabIcon: ImageView
    lateinit var wealthTabIcon: ImageView
    lateinit var healthTabIcon: ImageView
    lateinit var settingsTabIcon: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        dashboardTab = findViewById(R.id.dashboard)
        wealthTab = findViewById(R.id.wealth)
        healthTab = findViewById(R.id.health)
        settingsTab = findViewById(R.id.settings)

        dashboardTabTitle = findViewById(R.id.dashboard_text)
        wealthTabTitle = findViewById(R.id.wealth_text)
        healthTabTitle = findViewById(R.id.health_text)
        settingsTabTitle = findViewById(R.id.settings_text)

        dashboardTabIcon = findViewById(R.id.dashboard_icon)
        wealthTabIcon = findViewById(R.id.wealth_icon)
        healthTabIcon = findViewById(R.id.health_icon)
        settingsTabIcon = findViewById(R.id.settings_icon)

        openFragment(DashboardFragment())

        dashboardTab.setOnClickListener {
            dashboardTab.setBackgroundResource(R.drawable.dashboard_tab_bg)
            dashboardTabIcon.setColorFilter(
                ContextCompat.getColor(
                    this,
                    R.color.white
                )
            )
            dashboardTabTitle.visibility = View.VISIBLE

            wealthTab.setBackgroundResource(R.drawable.inactive_tab_bg)
            wealthTabIcon.setColorFilter(
                ContextCompat.getColor(
                    this,
                    R.color.unSelectedTab
                )
            )
            wealthTabTitle.visibility = View.GONE

            healthTab.setBackgroundResource(R.drawable.inactive_tab_bg)
            healthTabIcon.setColorFilter(
                ContextCompat.getColor(
                    this,
                    R.color.unSelectedTab
                )
            )
            healthTabTitle.visibility = View.GONE

            settingsTab.setBackgroundResource(R.drawable.inactive_tab_bg)
            settingsTabIcon.setColorFilter(
                ContextCompat.getColor(
                    this,
                    R.color.unSelectedTab
                )
            )
            settingsTabTitle.visibility = View.GONE

            openFragment(DashboardFragment())
        }

        wealthTab.setOnClickListener {
            dashboardTab.setBackgroundResource(R.drawable.inactive_tab_bg)
            dashboardTabIcon.setColorFilter(
                ContextCompat.getColor(
                    this,
                    R.color.unSelectedTab
                )
            )
            dashboardTabTitle.visibility = View.GONE

            wealthTab.setBackgroundResource(R.drawable.downloads_tab_bg)
            wealthTabIcon.setColorFilter(
                ContextCompat.getColor(
                    this,
                    R.color.white
                )
            )
            wealthTabTitle.visibility = View.VISIBLE

            healthTab.setBackgroundResource(R.drawable.inactive_tab_bg)
            healthTabIcon.setColorFilter(
                ContextCompat.getColor(
                    this,
                    R.color.unSelectedTab
                )
            )
            healthTabTitle.visibility = View.GONE

            settingsTab.setBackgroundResource(R.drawable.inactive_tab_bg)
            settingsTabIcon.setColorFilter(
                ContextCompat.getColor(
                    this,
                    R.color.unSelectedTab
                )
            )
            settingsTabTitle.visibility = View.GONE
            openFragment(WealthFragment())
        }

        healthTab.setOnClickListener {
            dashboardTab.setBackgroundResource(R.drawable.inactive_tab_bg)
            dashboardTabIcon.setColorFilter(
                ContextCompat.getColor(
                    this,
                    R.color.unSelectedTab
                )
            )
            dashboardTabTitle.visibility = View.GONE

            wealthTab.setBackgroundResource(R.drawable.inactive_tab_bg)
            wealthTabIcon.setColorFilter(
                ContextCompat.getColor(
                    this,
                    R.color.unSelectedTab
                )
            )
            wealthTabTitle.visibility = View.GONE

            healthTab.setBackgroundResource(R.drawable.reports_tab_bg)
            healthTabIcon.setColorFilter(
                ContextCompat.getColor(
                    this,
                    R.color.white
                )
            )
            healthTabTitle.visibility = View.VISIBLE

            settingsTab.setBackgroundResource(R.drawable.inactive_tab_bg)
            settingsTabIcon.setColorFilter(
                ContextCompat.getColor(
                    this,
                    R.color.unSelectedTab
                )
            )
            settingsTabTitle.visibility = View.GONE
            openFragment(HealthFragment())
        }

        settingsTab.setOnClickListener {
            dashboardTab.setBackgroundResource(R.drawable.inactive_tab_bg)
            dashboardTabIcon.setColorFilter(
                ContextCompat.getColor(
                    this,
                    R.color.unSelectedTab
                )
            )
            dashboardTabTitle.visibility = View.GONE

            wealthTab.setBackgroundResource(R.drawable.inactive_tab_bg)
            wealthTabIcon.setColorFilter(
                ContextCompat.getColor(
                    this,
                    R.color.unSelectedTab
                )
            )
            wealthTabTitle.visibility = View.GONE

            healthTab.setBackgroundResource(R.drawable.inactive_tab_bg)
            healthTabIcon.setColorFilter(
                ContextCompat.getColor(
                    this,
                    R.color.unSelectedTab
                )
            )
            healthTabTitle.visibility = View.GONE

            settingsTab.setBackgroundResource(R.drawable.settings_tab_bg)
            settingsTabIcon.setColorFilter(
                ContextCompat.getColor(
                    this,
                    R.color.white
                )
            )
            settingsTabTitle.visibility = View.VISIBLE
            openFragment(SettingsFragment())
        }
    }

    private fun openFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .commit()
    }
}
