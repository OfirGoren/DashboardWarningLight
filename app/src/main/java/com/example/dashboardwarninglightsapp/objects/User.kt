package com.example.dashboardwarninglightsapp.objects


data class User(
    val name: String? = "",
    val email: String? = "",
    val companyCar: String? = "",
    val modelCar: String? = "",
    var imageUri: String? = "",
    var id: String? = null
)
