package com.example.dashboardwarninglightsapp.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.dashboardwarninglightsapp.R
import com.example.dashboardwarninglightsapp.activity.MainActivity
import com.example.dashboardwarninglightsapp.databinding.FragmentLogInBinding
import com.example.dashboardwarninglightsapp.objects.*
import com.example.dashboardwarninglightsapp.utils.WindowUtils
import com.facebook.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class LogInFragment : Fragment() {

    private lateinit var binding: FragmentLogInBinding
    private lateinit var authSignUp: AuthSignUp

    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private val auth: FirebaseAuth = Firebase.auth
    private var callbackManager: CallbackManager? = null
    private var errorCredential: AuthCredential? = null
    private val googleAuth = GoogleAuth()
    private var googleAccount: GoogleSignInAccount? = null
    private val fireStoreHandler = FireStoreHandler()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment

        binding = FragmentLogInBinding.inflate(inflater, container, false)

        initValues()
        initGoogleAuth()
        initListeners()






        return binding.root
    }


    private fun initGoogleAuth() {

        mGoogleSignInClient = googleAuth.getGoogleSignInClient(requireActivity())

    }


    private fun initValues() {
        authSignUp = AuthSignUp()
        callbackManager = CallbackManager.Factory.create()


    }

    private fun initListeners() {
        binding.logInBtn.setOnClickListener(logInClick)
        binding.logInGoogle.setOnClickListener(googleLogIn)


    }


    /*
     ---------------------------- Listeners ---------------------------
     */

    private val logInClick = View.OnClickListener { view ->
        WindowUtils.closeKeyBoardView(requireActivity(), view)
        logInClickWithEmailHandler()


    }

    private val googleLogIn = View.OnClickListener {
        startGoogleLauncher(signInGoogle, mGoogleSignInClient.signInIntent)


    }


    /*
    ---------------------------- LogIn With Email And Password ---------------------------
    */

    private fun logInClickWithEmailHandler() {
        val email = binding.logInEmail.editText?.text.toString()
        val password = binding.logInPassword.editText?.text.toString()
        if (email.isEmpty() || password.isEmpty()) {

            binding.logInTEVErrorMsg.text = getString(R.string.log_is_failed_try_again)
            setVisibilityProgressAndErrorText(View.INVISIBLE, View.VISIBLE)
            //exit from Method
            return
        }
        // handle progress bar and error msg display
        setVisibilityProgressAndErrorText(View.VISIBLE, View.INVISIBLE)
        // log In to account
        authSignUp.logInUser(requireActivity(), email, password, logInCallBack)

    }


    // callback On login mode
    private val logInCallBack = object : AuthSignUp.LogInCallBack {
        //when we log in to user account is succeeded
        override fun isSucceeded() {
            //check if the email was verified
            authSignUp.isEmailVerified(object : AuthSignUp.LogInCallBack {
                // when email was verified
                override fun isSucceeded() {

                    // enter the user
                    openMainActivity()
                }

                //when email wasn't verified
                override fun isFailed() {
                    binding.logInTEVErrorMsg.text = getString(R.string.needToVerify)
                    setVisibilityProgressAndErrorText(View.INVISIBLE, View.VISIBLE)

                }
            })
        }

        // when log in failed
        override fun isFailed() {
            // show msg to user
            binding.logInTEVErrorMsg.text = getString(R.string.log_is_failed_try_again)
            // show error msg to user , and close the progressBar
            setVisibilityProgressAndErrorText(View.INVISIBLE, View.VISIBLE)


        }

    }


    /*
     ---------------------------- authentication With Google ---------------------------
     */

    private fun startGoogleLauncher(
        startForResult: ActivityResultLauncher<Intent>, signInIntent: Intent
    ) {

        startForResult.launch(signInIntent)


    }

    private val signInGoogle =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->

            if (result.resultCode == Activity.RESULT_OK) {
                val intent = result.data
                val task = GoogleSignIn.getSignedInAccountFromIntent(intent)
                try {
                    // Google Sign In was successful, authenticate with Firebase
                    googleAccount = task.getResult(ApiException::class.java)!!


                    googleAccount?.email?.let {
                        fireStoreHandler.checkIfEmailAlreadyExistOrNot(
                            it,
                            this::isEmailAlreadyExistCallBack
                        )
                    }
                    // check there is already provider , if not create
                    //otherwise


                } catch (e: ApiException) {
                    // Google Sign In failed, update UI appropriately


                }

            }
        }


    private fun isEmailAlreadyExistCallBack(emailExist: Boolean) {
        val credential = GoogleAuthProvider.getCredential(googleAccount?.idToken, null)
        googleAccount?.let { accu ->
            signInWithCredential(
                credential,
                this::saveUserDb,
                accu,
                emailExist
            )
        }


    }

    private fun checkIfUserAlreadySignUp(
        account: GoogleSignInAccount
    ) {
        val email = account.email ?: ""
        auth.fetchSignInMethodsForEmail(email)
            .addOnCompleteListener { task ->

                val alreadyEmailProvider =
                    task.result.signInMethods?.contains(EmailAuthProvider.PROVIDER_ID)
                val alreadyGoogleProvider =
                    task.result.signInMethods?.contains(GoogleAuthProvider.PROVIDER_ID)

                if (alreadyEmailProvider == false && alreadyGoogleProvider == true) {


                    saveUserDb(account.email, account.photoUrl, account.displayName)

                }


            }
    }


    // get the token and make credential to added authentication google firebase
    private fun signInWithCredential(
        credential: AuthCredential,
        saveUserDb: (email: String?, photoUrl: Uri?, displayName: String?) -> Unit,
        account: GoogleSignInAccount,
        emailExist: Boolean
    ) {


        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->

                if (task.isSuccessful) {
                    if (!emailExist) {
                        checkIfUserAlreadySignUp(account)
                    }
                    openMainActivity()

                } else {
                    // If sign in fails, display a message to the user.
                    //  Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(
                        context, "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()

                }
            }
    }

    private fun saveUserDb(email: String?, photoUrl: Uri?, displayName: String?) {
        val user =
            User(displayName, email, imageUri = photoUrl.toString(), id = auth.currentUser?.uid)
        fireStoreHandler.saveUser(user, object : FireStoreHandler.UserInfoCallBack {
            override fun isSucceeded() {
                fireStoreHandler.createCollectionTowing()

            }

            override fun isFailed(cause: Throwable?) {}

        })

    }


    private fun setVisibilityProgressAndErrorText(
        visibilityProgressBar: Int,
        visibilityErrorMsg: Int
    ) {
        binding.logInPRB.visibility = visibilityProgressBar
        binding.logInTEVErrorMsg.visibility = visibilityErrorMsg

    }

    private fun openMainActivity() {
        val intent = Intent(activity, MainActivity::class.java)
        startActivity(intent)
        activity?.finish()
    }


    override fun onDestroy() {
        super.onDestroy()


    }

}