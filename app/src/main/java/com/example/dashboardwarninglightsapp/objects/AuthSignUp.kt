package com.example.dashboardwarninglightsapp.objects

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class AuthSignUp {

    private val auth: FirebaseAuth = Firebase.auth


    interface RegisterNewUserCallBack {
        fun isSucceeded(isNewUser: Boolean?)
        fun isFailed()

    }

    interface LogInCallBack {
        fun isSucceeded()
        fun isFailed()

    }

    interface ExistOrNot {
        fun isExist(provider: String)
        fun noExist()

    }

    /**
    sign up new user with email and password
    return from callBack if the action was Successful or failed
     **/
    fun registerNewUser(
        activity: Activity,
        email: String,
        password: String,
        newUserCallBack: RegisterNewUserCallBack
    ) {

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(activity) { task ->
                if (!task.isSuccessful) {

                    try {
                        throw task.exception!!
                    } catch (weakPassword: FirebaseAuthWeakPasswordException) {

                    } // if user enters wrong password.
                    catch (malformedEmail: FirebaseAuthInvalidCredentialsException) {

                    } catch (existEmail: FirebaseAuthUserCollisionException) {

                        newUserCallBack.isSucceeded(false)

                    } catch (e: Exception) {
                    }
                } else {

                    sendEmailVerification(newUserCallBack)

                }

            }

    }

    fun sendEmailVerification(newUserCallBack: RegisterNewUserCallBack) {
        auth.currentUser?.sendEmailVerification()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                newUserCallBack.isSucceeded(true)
            }
        }


    }

    /**
     * when user want to log in with email and password
     * callback if log in successful or failed
     */
    fun logInUser(
        activity: Activity,
        email: String,
        password: String,
        LogInCallBack: LogInCallBack
    ) {

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(activity) { task ->
                if (task.isSuccessful) {
                    LogInCallBack.isSucceeded()


                } else {
                    // If sign in fails, display a message to the user.
                    LogInCallBack.isFailed()

                }
            }
    }


    fun checkEmailAlreadyExistOrNot(email: String, existOrNot: ExistOrNot) {
        auth.fetchSignInMethodsForEmail(email)
            .addOnCompleteListener { task ->
                val isNewUser = task.result.signInMethods?.isEmpty() ?: false


                if (isNewUser) {
                    existOrNot.noExist()
                } else {
                    val position = task.result.signInMethods?.size?.minus(1)
                    val provider = position?.let { task.result.signInMethods?.get(it).toString() }

                    existOrNot.isExist(provider ?: "")
                }

            }


    }


    fun getCurrentProvider(): String? {
        val position = auth.currentUser?.providerData?.size?.minus(1)
        return position?.let { auth.currentUser?.providerData?.get(it)?.providerId }

    }


    fun isEmailVerified(logInCallBack: LogInCallBack) {
        auth.currentUser?.reload()?.addOnSuccessListener {
            if (auth.currentUser?.isEmailVerified == true) {
                logInCallBack.isSucceeded()
            } else {
                logInCallBack.isFailed()
            }
        }


    }


}