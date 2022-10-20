package com.example.dashboardwarninglightsapp.objects

import android.content.Context
import android.util.Log
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class TowingMap(mapFragment: SupportMapFragment, val context: Context) : OnMapReadyCallback {

    private var mMap: GoogleMap? = null


    init {
        mapFragment.getMapAsync(this)
    }


    override fun onMapReady(p0: GoogleMap) {

        mMap = p0

    }

    private fun updateCurrentLocationOnMap(latitude: Double, longitude: Double) {
        val sydney = LatLng(latitude, longitude)
        mMap?.clear()
        mMap?.addMarker(
            MarkerOptions()
                .position(sydney)
                .title("")
        )

        mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(sydney, 18.0f))
    }

    fun setLocationOnMap(latitude: Double, longitude: Double) {

        updateCurrentLocationOnMap(latitude, longitude)

    }


}