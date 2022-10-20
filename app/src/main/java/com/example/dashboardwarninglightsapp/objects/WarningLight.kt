package com.example.dashboardwarninglightsapp.objects

class WarningLight(var info: String = "", var instruction: String = "", var title: String = "") {


    /**
     * ------------------------ static method ---------------------------------
     */
    companion object {
        fun mapToWarningLightObj(map: MutableMap<String, Any>?): WarningLight {
            val warningList = WarningLight()
            if (map != null) {
                for (value in map) {
                    val keyValue = value.key
                    val mapValue = value.value.toString().replace("$", "\n")

                    when (keyValue) {
                        FireStoreHandler.TITLE -> {
                            warningList.title = mapValue
                        }
                        FireStoreHandler.INFO -> {
                            warningList.info = mapValue
                        }
                        FireStoreHandler.INSTRUCTION -> {
                            warningList.instruction = mapValue
                        }
                    }


                }
            }
            return warningList
        }

        fun getNamesWarningLightGarages(): Array<String> {


            return arrayOf(
                "toyota_Chr_Rcta",
                "toyota_Chr_High_Coolant",
                "toyota_Chr_IMG_Check_Engine",
                "toyota_Chr_BrakeElectric",
                "toyota_Chr_Abs",
                "toyota_Chr_Slip",
                "toyota_Chr_TirePressure"
            )

        }

        fun getNamesWarningLightTowing(): Array<String> {

            return arrayOf("toyota_Chr_Brake_Usa", "toyota_Chr_Steering", "toyota_Chr_Srs")


        }


    }


}