package com.example.dashboardwarninglightsapp.objects

import android.app.Activity
import com.example.dashboardwarninglightsapp.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

class GoogleAuth {


    fun getGoogleSignInClient(activity: Activity): GoogleSignInClient {

        val gso: GoogleSignInOptions?
        val mGoogleSignInClient: GoogleSignInClient?

        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(activity.getString(R.string.google_auth_id_token))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(activity, gso)

        return mGoogleSignInClient
    }


}