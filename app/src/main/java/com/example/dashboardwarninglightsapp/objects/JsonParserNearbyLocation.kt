package com.example.dashboardwarninglightsapp.objects

import android.util.Log
import org.json.JSONArray
import org.json.JSONObject


class JsonParserNearbyLocation {



    companion object {
        const val NAME = "name"
        const val LATITUDE = "latitude"
        const val LONGITUDE = "longitude"
        const val PLACES_ID = "place_id"
        const val PHONE_NUMBER = "formatted_phone_number"
        const val RATING = "rating"
        private const val LOCATION = "location"
        private const val   LAT = "lat"
        private const val LNG = "lng"
        private const val GEOMETRY = "geometry"

    }


    private fun parseJsonObject(obj: JSONObject): HashMap<String, String> {
        val dataList: HashMap<String, String> = HashMap()


        val name = obj.getString(NAME)
        val latitude = obj.getJSONObject(GEOMETRY).getJSONObject(LOCATION).getString(LAT)
        val longitude = obj.getJSONObject(GEOMETRY).getJSONObject(LOCATION).getString(LNG)
        val placesId = obj.getString(PLACES_ID)


        dataList[NAME] = name
        dataList[LATITUDE] = latitude
        dataList[LONGITUDE] = longitude
        dataList[PLACES_ID] = placesId



        return dataList
    }


    private fun personJsonArray(jsonArray: JSONArray): ArrayList<HashMap<String, String>> {

        val dataList: ArrayList<HashMap<String, String>> = ArrayList()
        for (i in 0 until jsonArray.length()) {
            val data = parseJsonObject(jsonArray.get(i) as JSONObject)
            dataList.add(data)
        }

        return dataList
    }


    fun parseResult(obj: JSONObject): ArrayList<HashMap<String, String>> {


       val jsonArray = obj.getJSONArray("results")


        return personJsonArray(jsonArray)
    }

     fun getDetailsOnGarages(obj: JSONObject):String {
         var phoneNumber = ""

         val jsonObj = obj.getJSONObject("result")

         if(jsonObj.has(PHONE_NUMBER)) {
             phoneNumber = jsonObj.getString(PHONE_NUMBER)

         }

        return  phoneNumber

    }



}