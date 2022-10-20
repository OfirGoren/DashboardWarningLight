package com.example.dashboardwarninglightsapp.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import com.example.dashboardwarninglightsapp.R
import com.example.dashboardwarninglightsapp.activity.ConnectActivity
import com.example.dashboardwarninglightsapp.activity.MainActivity
import com.example.dashboardwarninglightsapp.databinding.FragmentProfileBinding
import com.example.dashboardwarninglightsapp.objects.FireStoreHandler
import com.example.dashboardwarninglightsapp.objects.GoogleAuth
import com.example.dashboardwarninglightsapp.objects.StorageHandler
import com.example.dashboardwarninglightsapp.objects.User
import com.example.dashboardwarninglightsapp.utils.ToastyUtils
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import es.dmoral.toasty.Toasty


class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private val fireStoreHandler = FireStoreHandler()
    private val storageHandler = StorageHandler()
    private var photoUri: Uri? = null
    private val auth: FirebaseAuth = Firebase.auth
    private var toastyMsg: ToastyUtils? = null
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private var userDetail = User()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        toastyMsg = ToastyUtils(requireContext().applicationContext)
        initAllListeners()
        initValues()
        initListeners()
        initListItems()
        initGoogleAuth()
        locationToastyMsg()
        getUserProfileInfo()

        // hideSystemUI()

        return binding.root


    }


    private fun initListeners() {

        //  binding.profileAutoCompleteCompanies.onItemClickListener = itemCompanyClick
        //  binding.signUpAutoCompleteCompanies.onItemClickListener = itemCompanyClick


    }

    private fun initListItems() {

        setListItems(R.array.car_companies, binding.profileAutoCompleteCompanies)
        setListItems(R.array.car_models, binding.profileAutoCompleteModel)

    }

    private fun setListItems(list: Int, view: AutoCompleteTextView) {

        val itemsList = resources.getStringArray(list)
        val adapter =
            ArrayAdapter(requireContext(), R.layout.list_item_companies_car, itemsList)
        (view as? AutoCompleteTextView)?.setAdapter(adapter)


    }

    private fun initAllListeners() {
        takePhotoListener()
        saveChangesListener()
        logOutListener()
    }

    private fun initValues() {
        mGoogleSignInClient = GoogleAuth().getGoogleSignInClient(requireActivity())

    }

    private fun initGoogleAuth() {

    }

    private fun saveChangesListener() {
        binding.profileBTNSaveChanges.setOnClickListener { view ->
            uploadNewUserProfile()

        }
    }


    private fun logOutListener() {
        binding.profileTXTLogOut.setOnClickListener {
            openConnectActivity()
            Firebase.auth.signOut()
            mGoogleSignInClient.signOut()
        }

    }


    private fun uploadNewUserProfile() {
        uploadImageStorageDB()
    }

    private fun getUserProfileInfo() {
        fireStoreHandler.getUserDetail(object : FireStoreHandler.GetUserDetail {
            override fun getUserDetailCallBack(user: User) {

                userDetail = user
                binding.profileTXTEmail.text = user.email
                binding.profileEDTName.setText(user.name)

                binding.profileTextInputCompanies.editText?.setText(user.companyCar)
                binding.profileTextInputModel.editText?.setText(user.modelCar)
                if (user.imageUri?.isEmpty() == false) {
                    Picasso.get()
                        .load(Uri.parse(user.imageUri))
                        .into(binding.profileIMG)

                }

            }
        })
    }

    private fun takePhotoListener() {
        binding.profileIMG.setOnClickListener {
            ImagePicker.with(this@ProfileFragment)
                .crop(16f, 16f)
                .compress(1024)
                //Final image size will be less than 1 MB(Optional)
                .maxResultSize(
                    620,
                    620
                )  //Final image resolution will be less than 1080 x 1080(Optional)
                .createIntent { intent ->
                    startForProfileImageResult.launch(intent)
                }


        }
    }

    private fun getCurrentProfileUser(): User {
        val name = binding.profileEDTName.text.toString()
        val email = binding.profileTXTEmail.text.toString()
        val company = binding.profileTextInputCompanies.editText?.text.toString()
        val model = binding.profileTextInputModel.editText?.text.toString()

        return User(name, email, company, model, id = auth.currentUser?.uid)

    }

    private val startForProfileImageResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val resultCode = result.resultCode
            val data = result.data

            when (resultCode) {
                Activity.RESULT_OK -> {
                    //Image Uri will not be null for RESULT_OK
                    photoUri = data?.data

                    binding.profileIMG.setImageURI(photoUri)


                }
                ImagePicker.RESULT_ERROR -> {
                    Toast.makeText(context, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
                }
                else -> {
                    Toast.makeText(context, "Task Cancelled", Toast.LENGTH_SHORT).show()
                }
            }
        }

    private fun uploadImageStorageDB() {
        val currentUser = getCurrentProfileUser()
        if (photoUri != null) {
            storageHandler.uploadImage(photoUri!!, object : StorageHandler.GetDownloadURLCallBack {
                override fun getDownloadURLCallBack(uri: Uri?) {

                    val photoUriStorage = uri.toString()
                    currentUser.imageUri = photoUriStorage
                    saveUserInDB(currentUser)

                }

            })
        } else {
            currentUser.imageUri = userDetail.imageUri
            saveUserInDB(currentUser)
        }
    }


    private fun locationToastyMsg() {
        val location = IntArray(2)
        binding.profileBTNSaveChanges.getLocationOnScreen(location)

        val yOffset = location[1] + 85
        toastyMsg?.setLocationToasty(-1, -1, yOffset)


    }

    fun saveUserInDB(currentUser: User) {


        fireStoreHandler.saveUser(currentUser, object : FireStoreHandler.UserInfoCallBack {
            override fun isSucceeded() {
                toastyMsg?.toastySuccess("Success!")
            }

            override fun isFailed(cause: Throwable?) {
                toastyMsg?.toastyError("Update Failed , Please Check Internet Connection")

            }
        })


    }


    @SuppressLint("SourceLockedOrientationActivity")
    override fun onStart() {
        super.onStart()
        showSystemUI()
//        @RequiresApi(Build.VERSION_CODES.R)
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//            activity?.window?.attributes?.layoutInDisplayCutoutMode =
//                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
//        }

        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT


    }

    private fun showSystemUI() {


        activity?.window?.let { WindowCompat.setDecorFitsSystemWindows(it, false) }
        activity?.window?.let {
            WindowInsetsControllerCompat(
                it,
                binding.root
            ).show(WindowInsetsCompat.Type.systemBars())
        }

    }

    private fun openConnectActivity() {
        val intent = Intent(activity, ConnectActivity::class.java)
        startActivity(intent)
        activity?.finish()
    }

}