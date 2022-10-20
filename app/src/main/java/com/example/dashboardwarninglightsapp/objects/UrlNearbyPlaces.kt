package com.example.dashboardwarninglightsapp.objects

class UrlNearbyPlaces {


    companion object {

        private const val KEY = "AIzaSyCk3Ryy634kGv3s5kiyw4hSqYgm0QRts40"

        fun getUrlStrNearbyGarages(lat: Double, lng: Double): String {

            return "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                    "?location=" + lat + ", " + lng + // current location
                    "&radius=10000" +  // Nearby radios
                    "&keyword=מוסך טויוטה" +
                    "&sensor=true" +
                    "&key=" + KEY

        }


        fun getUrlStrDetailGarage(id: String): String {
            return "https://maps.googleapis.com/maps/api/place/details/json" +
                    "?fields=name%2Crating%2Cformatted_phone_number" +
                    "&place_id=" + id +
                    "&key=" + KEY

        }


    }

}