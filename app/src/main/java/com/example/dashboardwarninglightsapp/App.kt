package com.example.dashboardwarninglightsapp

import android.app.Application
import com.example.dashboardwarninglightsapp.objects.VolleyUtils
import com.example.dashboardwarninglightsapp.utils.SystemServiceUtils

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        SystemServiceUtils.init(this)
        VolleyUtils.init(this)
    }
}