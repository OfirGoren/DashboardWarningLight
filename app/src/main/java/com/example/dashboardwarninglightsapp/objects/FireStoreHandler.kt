package com.example.dashboardwarninglightsapp.objects

import android.util.Log
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.SignInMethodQueryResult
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase


class FireStoreHandler {

    private val db = Firebase.firestore
    private val auth: FirebaseAuth = Firebase.auth

    companion object {
        private const val USERS = "users"
        private const val WARNING_LIGHTS = "WarningLights"
        const val INFO = "info"
        const val INSTRUCTION = "instruction"
        const val TITLE = "title"
        const val TOWING = "Towing"
        const val REQUEST_TOWING = "RequestTowing"
    }


    interface GetWarningLightInfo {
        fun isSucceeded(warningLightInfo: WarningLight)
        fun isFailed(exception: Exception)


    }

    interface GetUserDetail {
        fun getUserDetailCallBack(user: User)

    }

    interface UserInfoCallBack {
        fun isSucceeded()
        fun isFailed(cause: Throwable?)
    }

    interface TowingDetailCallBack {
        fun towingDetailCallBack(towingDetail: UserDetailForTowing)
    }


    fun saveUser(user: User, userInfoCallBack: UserInfoCallBack) {


        user.id?.let {
            db.collection(USERS).document(it)
                .set(user)
                .addOnSuccessListener {
                    userInfoCallBack.isSucceeded()
                }
                .addOnFailureListener { e ->
                    userInfoCallBack.isFailed(e.cause)

                }
        }


    }

    fun getUserDetail(getUserDetail: GetUserDetail) {
        auth.currentUser?.let {
            db.collection(USERS).document(it.uid)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    val user = documentSnapshot.toObject<User>()

                    if (user != null) {
                        getUserDetail.getUserDetailCallBack(user)
                    }

                }


        }

    }


    fun getInfoWarningLight(warningLight: String, getWarningLightInfo: GetWarningLightInfo) {

        val docRef = db.collection(WARNING_LIGHTS).document(warningLight)
        docRef.get()
            .addOnSuccessListener { document ->
                Thread {
                    if (document != null) {
                        val map = document.data

                        val warningLightChosen = WarningLight.mapToWarningLightObj(map)

                        getWarningLightInfo.isSucceeded(warningLightChosen)

                    } else {

                    }
                }.start()
            }
            .addOnFailureListener { exception ->
                getWarningLightInfo.isFailed(exception)

            }


    }


    fun getRealTimeTowing(towingDetailCallBack: TowingDetailCallBack) {


        val dateMill = System.currentTimeMillis()

        val docRef =
            auth.currentUser?.uid?.let { db.collection(USERS).document(it).collection(TOWING) }
        docRef?.addSnapshotListener { snapshot, e ->
            if (e != null) {
                //  Log.w(TAG, "Listen failed.", e)
                //  return@addSnapshotListener
            }

            if (snapshot != null) {
                val userDetailForTowing = UserDetailForTowing()
                for (snap in snapshot.documentChanges) {

                    val userDetail = snap.document.toObject(UserDetailForTowing::class.java);
                    userDetail.requestId?.toLong()?.let { requestId ->

                        userDetailForTowing.name = userDetail.name
                        userDetailForTowing.imageUri = userDetail.imageUri
                        userDetailForTowing.latitude = userDetail.latitude
                        userDetailForTowing.longitude = userDetail.longitude
                        userDetailForTowing.Userid = userDetail.Userid
                        userDetailForTowing.requestId = userDetail.requestId
                        towingDetailCallBack.towingDetailCallBack(userDetailForTowing)

                    }


                }

                //     Log.d("FGDdg", "Current data: ${snapshot.data}")
            } else {
                //   Log.d("GFDdgf", "Current data: null")
            }
        }


    }

    fun createCollectionTowing() {
        val num = hashMapOf(
            "0" to "0",
        )
        val docRef = auth.currentUser?.uid?.let {
            db.collection(USERS).document(it).collection(TOWING).document("0")
        }
        docRef?.set(num)?.addOnSuccessListener { document ->

        }


    }

    fun sendRequestForTowing(userDetailForTowing: UserDetailForTowing) {

        val mill = System.currentTimeMillis().toString()
        userDetailForTowing.requestId = mill
        db.collection(REQUEST_TOWING).document(mill)
            .set(userDetailForTowing)
            .addOnSuccessListener { documentSnapshot ->


            }

    }


    fun checkIfEmailAlreadyExistOrNot(
        email: String,
        isEmailAlreadyExist: (input: Boolean) -> Unit
    ) {

        auth.fetchSignInMethodsForEmail(email)
            .addOnCompleteListener { task ->
                if (task.result?.signInMethods?.size == 0) {

                    isEmailAlreadyExist(false)
                } else {

                    // email existed
                    isEmailAlreadyExist(true)
                }
            }.addOnFailureListener { e -> e.printStackTrace() }

    }

}