package com.example.dashboardwarninglightsapp.objects

import android.net.Uri
import android.util.Log
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class StorageHandler {

    private val storage = Firebase.storage


    interface GetDownloadURLCallBack {
        fun getDownloadURLCallBack(uri: Uri?)

    }


    fun uploadImage(uri: Uri, getDownloadURLCallBack: GetDownloadURLCallBack) {
        val storageRef = storage.reference
        val riversRef = storageRef.child("images/${System.currentTimeMillis()}")
        val uploadTask = riversRef.putFile(uri)

        val urlTask = uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            riversRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result

                getDownloadURLCallBack.getDownloadURLCallBack(downloadUri)

            } else {
                // Handle failures
                // ...
            }

        }


    }


}