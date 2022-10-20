package com.example.dashboardwarninglightsapp.objects

import android.annotation.SuppressLint
import android.content.Context
import android.content.IntentSender
import android.util.Log
import com.example.dashboardwarninglightsapp.interfaces.GetLastLocationCallBack
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task


class FusedLocationHandler(val context: Context) {

    private var fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    private lateinit var getLastLocationCallBack: GetLastLocationCallBack


    fun initGetLastLocationCallBack(getLastLocationCallBack: GetLastLocationCallBack) {
        this.getLastLocationCallBack = getLastLocationCallBack

    }

    @SuppressLint("MissingPermission")
    fun getLastLocation() {
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(createLocationRequest())

        val client: SettingsClient = LocationServices.getSettingsClient(context)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())


        task.addOnSuccessListener {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {

                    getLastLocationCallBack.getLastLocationCallBack(
                        location.latitude,
                        location.longitude
                    )
                }
            }


        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().

                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }

    }


    private fun createLocationRequest(): LocationRequest {
        val locationRequest = LocationRequest.create().apply {
            interval = 1000
            fastestInterval = 1000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        return locationRequest

    }


}