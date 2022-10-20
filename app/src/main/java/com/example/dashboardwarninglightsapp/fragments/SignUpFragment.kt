package com.example.dashboardwarninglightsapp.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.dashboardwarninglightsapp.R
import com.example.dashboardwarninglightsapp.activity.MainActivity
import com.example.dashboardwarninglightsapp.databinding.FragmentSignUpBinding
import com.example.dashboardwarninglightsapp.objects.AuthSignUp
import com.example.dashboardwarninglightsapp.objects.FireStoreHandler
import com.example.dashboardwarninglightsapp.objects.User
import com.example.dashboardwarninglightsapp.utils.DialogUtils
import com.example.dashboardwarninglightsapp.utils.ToastyUtils
import com.example.dashboardwarninglightsapp.utils.WindowUtils
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class SignUpFragment : Fragment() {

    private lateinit var binding: FragmentSignUpBinding
    private lateinit var authSignUp: AuthSignUp
    private lateinit var fireStoreHandler: FireStoreHandler
    private lateinit var gso: GoogleSignInOptions
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private val auth: FirebaseAuth = Firebase.auth
    private var toastyMsg: ToastyUtils? = null
    private var newUser = User()

    companion object {
        const val REGISTER = "Register"
        const val VERIFY = "Verify"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignUpBinding.inflate(inflater, container, false)
        initListItems()
        initValues()
        initListeners()
        initGoogleAuth()
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        locationToastyMsg()
    }

    private fun initValues() {
        toastyMsg = ToastyUtils(requireContext().applicationContext)
        authSignUp = AuthSignUp()
        fireStoreHandler = FireStoreHandler()

    }

    private fun initListeners() {
        binding.signUpBtn.setOnClickListener(registerBtn)
        binding.signUpAutoCompleteModel.onItemClickListener = itemCompanyClick
        binding.signUpAutoCompleteCompanies.onItemClickListener = itemCompanyClick


    }

    private fun initListItems() {

        setListItems(R.array.car_companies, binding.signUpAutoCompleteCompanies)
        setListItems(R.array.car_models, binding.signUpAutoCompleteModel)

    }

    private fun setListItems(list: Int, view: AutoCompleteTextView) {

        val itemsList = resources.getStringArray(list)
        val adapter =
            ArrayAdapter(requireContext(), R.layout.list_item_companies_car, itemsList)
        (view as? AutoCompleteTextView)?.setAdapter(adapter)


    }

    private fun initGoogleAuth() {
        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.google_auth_id_token))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

    }


    private val itemCompanyClick =
        AdapterView.OnItemClickListener { parent, view, position, id ->

            WindowUtils.closeKeyBoardView(requireActivity(), view)


        }


    private val registerBtn = View.OnClickListener { view ->
        // show error to user (when he doesn't filled all the fields)
        errorsHandler()
        val btnText = binding.signUpTXTOnBtn.text.toString()
        visibilityGoneBtn(View.GONE, View.GONE)
        // when all fields inside (by the user)
        if (!isErrorsMsgPerform()) {
            binding.signUpPRB.visibility = View.VISIBLE
            if (btnText == REGISTER) {

                signUpUser()

            } else if (btnText == VERIFY) {

                handlerEmailVerification()


            }
        } else {
            binding.signUpPRB.visibility = View.GONE
            visibilityGoneBtn(View.VISIBLE, View.VISIBLE)
        }


    }

    private fun visibilityGoneBtn(visibilityBtn: Int, visibilityTextBtn: Int) {
        binding.signUpBtn.visibility = visibilityBtn
        binding.signUpTXTOnBtn.visibility = visibilityTextBtn
    }


    private fun handlerEmailVerification() {
        authSignUp.isEmailVerified(object : AuthSignUp.LogInCallBack {
            override fun isSucceeded() {
                openMainActivity()
                fireStoreHandler.createCollectionTowing()

            }

            override fun isFailed() {
                binding.signUpPRB.visibility = View.INVISIBLE
                visibilityGoneBtn(View.VISIBLE, View.VISIBLE)
                toastyMsg?.toastyInfo("You Didn't Verify Your Account")

            }

        })
    }

    private fun signUpUser() {

        val email = binding.signUpEmail.editText?.text.toString()
        val password = binding.signUpEDTPassword.editText?.text.toString()
        // create new account
        authSignUp.registerNewUser(
            requireActivity(), email, password, registerNewUserCallBack
        )
    }

    private fun getUserDetails(): User {
        val name = binding.signUpFullName.editText?.text.toString()
        val email = binding.signUpEmail.editText?.text.toString()
        val companyCar = binding.signUpTextInputCompanies.editText?.text.toString()
        val modelCar = binding.signUpTextInputModel.editText?.text.toString()
        val id = auth.currentUser?.uid
        return User(name, email, companyCar, modelCar, "", id)


    }


    private val registerNewUserCallBack = object : AuthSignUp.RegisterNewUserCallBack {

        override fun isSucceeded(isNewUser: Boolean?) {

            visibilityGoneBtn(View.VISIBLE, View.VISIBLE)
            // succeed sign up
            if (isNewUser == true) {
                // openMainActivity()
                newUser = getUserDetails()
                fireStoreHandler.saveUser(newUser, object : FireStoreHandler.UserInfoCallBack {
                    override fun isSucceeded() {}
                    override fun isFailed(cause: Throwable?) {}
                })

                binding.signUpTXTOnBtn.text = VERIFY
                binding.signUpPRB.visibility = View.INVISIBLE
                toastyMsg?.toastyInfo("Please verify Your Email")

                //save user in FireStore
                //  fireStoreHandler.saveUser(getUserDetails())


                // there is email Exists in auth
            } else if (isNewUser == false) {
                // show msg to user
                // updateEmailErrorMsg()

                showMsgDialogToUser()
                // stop progress bar

                binding.signUpPRB.visibility = View.INVISIBLE
            }
        }

        override fun isFailed() {

        }


    }

    private fun showMsgDialogToUser() {
        val dialogUtils = DialogUtils()
        dialogUtils.showDialog(
            requireActivity(),
            getString(R.string.emailAlreadyRegisteredWithGoogle),
            R.style.MyThemeOverlay_MaterialAlertDialog,
            object : DialogUtils.ResponseCallBack {
                override fun whichButtonIsPressed(btn: DialogUtils.ButtonPressed) {
                    handlerRequestUserToAddedProvider(btn)


                }


            })
    }

    private fun handlerRequestUserToAddedProvider(btn: DialogUtils.ButtonPressed) {


        if (btn == DialogUtils.ButtonPressed.Continue) {

            signInGoogle.launch(mGoogleSignInClient.signInIntent)
        }

    }

    private val signInGoogle =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->

            if (result.resultCode == Activity.RESULT_OK) {
                val intent = result.data
                val task = GoogleSignIn.getSignedInAccountFromIntent(intent)
                try {
                    // Google Sign In was successful, authenticate with Firebase
                    val account = task.getResult(ApiException::class.java)!!

                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)

                    signInWithCredential(credential)

                } catch (e: ApiException) {
                    // Google Sign In failed, update UI appropriately


                }

            }
        }

    private fun signInWithCredential(credential: AuthCredential) {


        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                closeKeyboard()
                if (task.isSuccessful) {

                    linkWithCredential(getEmailPasswordCredential())
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

    private fun linkWithCredential(credential: AuthCredential) {

        auth.currentUser?.linkWithCredential(credential)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    openMainActivity()

                    val user = task.result?.user
                    //updateUI(user)
                } else {
                    if (task.exception.toString().contains(getString(R.string.UserAlreadyLinked))) {
                        Toast.makeText(
                            context,
                            getString(R.string.UserAlreadySignUpWithThisEmail),
                            Toast.LENGTH_LONG
                        ).show()
                    }
//                    Toast.makeText(baseContext, "Authentication failed.",
//                        Toast.LENGTH_SHORT).show()
                    //     updateUI(null)
                }
            }
    }

    private fun getEmailPasswordCredential(): AuthCredential {
        return EmailAuthProvider.getCredential(
            binding.signUpEmail.editText?.text.toString(),
            binding.signUpEDTPassword.editText?.text.toString()
        )
    }

    private fun updateEmailErrorMsg() {

        binding.signUpEmail.editText?.setText("")
        errorMsgHandler(binding.signUpEmail, "Email Is Exsists")


    }

    private fun openMainActivity() {
        val intent = Intent(activity, MainActivity::class.java)
        startActivity(intent)
        activity?.finish()
    }

    private fun isErrorsMsgPerform(): Boolean {

        when {
            binding.signUpFullName.isErrorEnabled -> {
                return true
            }
            binding.signUpEmail.isErrorEnabled -> {
                return true
            }
            binding.signUpEDTPassword.isErrorEnabled -> {
                return true
            }
            binding.signUpTextInputCompanies.isErrorEnabled -> {
                return true
            }
            binding.signUpTextInputModel.isErrorEnabled -> {
                return true
            }
            else -> return false
        }
    }

    private fun errorsHandler() {
        errorMsgHandler(binding.signUpFullName, getString(R.string.require_name))
        errorMsgHandler(binding.signUpEmail, getString(R.string.email_required))
        errorMsgHandlerPassword(binding.signUpEDTPassword, getString(R.string.password_required))
        errorMsgHandler(binding.signUpTextInputCompanies, getString(R.string.company_required))
        errorMsgHandler(binding.signUpTextInputModel, getString(R.string.model_required))


    }

    private fun errorMsgHandlerPassword(
        signUpEDTPassword: TextInputLayout,
        string: String
    ): Boolean {
        if (!errorMsgHandler(signUpEDTPassword, string)) {
            if (signUpEDTPassword.editText?.text.toString().length < 6) {
                signUpEDTPassword.isErrorEnabled = true
                signUpEDTPassword.error = "Must have at least a \n6-character password"
                return true
            }
        }
        return false
    }

    private fun errorMsgHandler(textInput: TextInputLayout, str: String): Boolean {
        if (textInput.editText?.text?.isEmpty() == true) {
            textInput.isErrorEnabled = true
            textInput.error = str
            return true
        } else {
            textInput.isErrorEnabled = false
        }
        return false
    }

    // v is the Button view that you want the Toast to appear above
    // and messageId is the id of your string resource for the message


    private fun locationToastyMsg() {


        toastyMsg?.setLocationToasty(-1, -1, 30)


    }


    private fun closeKeyboard() {
        activity?.currentFocus.let { view ->

            val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(view?.windowToken, 0)

        }

    }

}