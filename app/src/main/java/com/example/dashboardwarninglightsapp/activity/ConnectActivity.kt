package com.example.dashboardwarninglightsapp.activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.dashboardwarninglightsapp.databinding.ActivityConnectBinding


class ConnectActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConnectBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConnectBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)


    }
}