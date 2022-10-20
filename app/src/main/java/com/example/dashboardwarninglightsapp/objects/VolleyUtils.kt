package com.example.dashboardwarninglightsapp.objects

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley

class VolleyUtils private constructor(val context: Context) {


    interface NearbyGaragesCallBack {
        fun getNearbyGaragesCallBack(
            nearbyGarages: ArrayList<HashMap<String, String>>
        )

    }

    interface DetailGarageCallBack {
        fun getDetailGarageCallBack(detailGarage: HashMap<String, String>)
    }

    companion object {
        //use Always Application Context
        @SuppressLint("StaticFieldLeak")
        private var INSTANCE: VolleyUtils? = null


        @JvmStatic
        fun init(context: Context) {
            INSTANCE = INSTANCE ?: VolleyUtils(context)

        }

        fun getInstance(): VolleyUtils? {
            return INSTANCE
        }
    }


    fun downloadNearbyPlaces(urlStr: String, nearbyGarages: NearbyGaragesCallBack) {


        val queue = Volley.newRequestQueue(context)

        val requestNearbyCarRepair = JsonObjectRequest(
            Request.Method.GET, urlStr, null,
            { response ->

                val jsonParser = JsonParserNearbyLocation()
                val garagesList = jsonParser.parseResult(response)



                nearbyGarages.getNearbyGaragesCallBack(garagesList)


            }

        ) {

        }
        queue.add(requestNearbyCarRepair)
// Access the RequestQueue through your singleton class.
        //    MySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }

    fun downloadDetailGarage(
        url: String,
        nearbyGarage: HashMap<String, String>,
        detailGarageCallBack: DetailGarageCallBack
    ) {

        val queue = Volley.newRequestQueue(context)

        val requestNearbyCarRepair = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->

                val jsonParser = JsonParserNearbyLocation()
                val phoneNumber = jsonParser.getDetailsOnGarages(response)
                nearbyGarage[JsonParserNearbyLocation.PHONE_NUMBER] = phoneNumber

                detailGarageCallBack.getDetailGarageCallBack(nearbyGarage)


            }

        ) {

        }
        queue.add(requestNearbyCarRepair)


    }


}



