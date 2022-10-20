package com.example.dashboardwarninglightsapp.objects

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.example.dashboardwarninglightsapp.R
import com.example.dashboardwarninglightsapp.interfaces.GetLastLocationCallBack
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

class MapGarages(mapFragment: SupportMapFragment, val context: Context) : OnMapReadyCallback,
    GetLastLocationCallBack, VolleyUtils.NearbyGaragesCallBack {
    private lateinit var mMap: GoogleMap
    private var marker: ArrayList<Marker>
    private var fusedLocationHandler: FusedLocationHandler
    private var currentLat: Double? = null
    private var currentLng: Double? = null


    init {
        mapFragment.getMapAsync(this)
        fusedLocationHandler = FusedLocationHandler(context)
        fusedLocationHandler.initGetLastLocationCallBack(this)
        marker = ArrayList()

    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap


        // Add a marker in Sydney and move the camera
//        val sydney = LatLng(-34.0, 151.0)
//        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(sydney ,18.0f))
    }

    private fun updateCurrentLocationOnMap(latitude: Double, longitude: Double) {
        val sydney = LatLng(latitude, longitude)


        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(sydney, 14.0f))
    }

    @SuppressLint("MissingPermission")
    override fun getLastLocationCallBack(latitude: Double, longitude: Double) {
        currentLat = latitude
        currentLng = longitude
        updateCurrentLocationOnMap(latitude, longitude)
        showNearbyGaragesOnMap(latitude, longitude)
        mMap.isMyLocationEnabled = true
    }


    private fun showNearbyGaragesOnMap(latitude: Double, longitude: Double) {
        val urlNearbyGarages = UrlNearbyPlaces.getUrlStrNearbyGarages(latitude, longitude)

        VolleyUtils.getInstance()?.downloadNearbyPlaces(urlNearbyGarages, this)


    }

    fun updateMapWithCurrentLocation() {
        fusedLocationHandler.getLastLocation()

    }

    override fun getNearbyGaragesCallBack(
        nearbyGarages: ArrayList<HashMap<String, String>>) {
        addPhoneNumberToDetailGarages(nearbyGarages)
        

    }

    private fun addPhoneNumberToDetailGarages(garages:ArrayList<HashMap<String, String>>) {
        for(nearbyGarages in garages ) {
             nearbyGarages[JsonParserNearbyLocation.PLACES_ID]?.let {placeId ->
                val urlDetailGarages =  UrlNearbyPlaces.getUrlStrDetailGarage(placeId)
                VolleyUtils.getInstance()?.downloadDetailGarage(urlDetailGarages,nearbyGarages,
                    object : VolleyUtils.DetailGarageCallBack {
                        override fun getDetailGarageCallBack(detailGarage: HashMap<String, String>) {
                            putMarkersOnMap(nearbyGarages)
                        }

                    })
            }


        }

    }

    private fun putMarkersOnMap(garagesDetails: HashMap<String, String>) {
        val latLng: LatLng

        val lat: Double = garagesDetails[JsonParserNearbyLocation.LATITUDE]?.toDouble() ?: 0.0
        val lng: Double = garagesDetails[JsonParserNearbyLocation.LONGITUDE]?.toDouble() ?: 0.0
        val name: String = garagesDetails[JsonParserNearbyLocation.NAME].toString()
        val phoneNumber =  garagesDetails[JsonParserNearbyLocation.PHONE_NUMBER].toString()

            latLng = LatLng(lat, lng)
            mMap.addMarker(
                MarkerOptions().position(latLng).title(name).snippet(phoneNumber).snippet(phoneNumber).icon(
                    BitmapDescriptorFactory.fromResource(
                        R.drawable.marker_yellow
                    )
                )
            )?.let { marker.add(it) }





    }
}