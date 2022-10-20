package com.example.dashboardwarninglightsapp.utils

import android.annotation.SuppressLint
import android.content.Context

import android.os.VibrationEffect

import android.os.Build

import androidx.core.content.ContextCompat.getSystemService

import android.os.Vibrator


class SystemServiceUtils private constructor(val context: Context) {

    companion object {
        //use Always Application Context
        @SuppressLint("StaticFieldLeak")
        private var INSTANCE: SystemServiceUtils? = null


        @JvmStatic
        fun init(context: Context) {
            INSTANCE = INSTANCE ?: SystemServiceUtils(context)


        }

        fun getInstance(): SystemServiceUtils? {
            return INSTANCE
        }
    }


    fun playVibration(milliseconds: Long) {
        val vibrator = getSystemService(context, Vibrator::class.java)
        vibrator?.let {
            if (Build.VERSION.SDK_INT >= 26) {
                it.vibrate(
                    VibrationEffect.createOneShot(
                        milliseconds,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                it.vibrate(milliseconds)
            }
        }
    }
}