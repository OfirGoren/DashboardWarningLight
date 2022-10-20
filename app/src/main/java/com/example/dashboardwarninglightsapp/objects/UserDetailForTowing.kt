package com.example.dashboardwarninglightsapp.objects


data class UserDetailForTowing(
    var name: String? = "",
    var imageUri: String? = "",
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var companyCar: String? = "",
    var modeCar: String? = "",
    var Userid: String? = null,
    var requestId: String? = null
)