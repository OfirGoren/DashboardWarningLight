package com.example.dashboardwarninglightsapp.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.dashboardwarninglightsapp.R
import com.example.dashboardwarninglightsapp.databinding.FragmentInviteTowingBinding
import com.example.dashboardwarninglightsapp.databinding.TowingDetailBinding
import com.example.dashboardwarninglightsapp.interfaces.GetLastLocationCallBack
import com.example.dashboardwarninglightsapp.objects.*
import com.example.dashboardwarninglightsapp.utils.DialogUtils
import com.google.android.gms.maps.SupportMapFragment
import com.squareup.picasso.Picasso


class InviteTowingFragment : Fragment() {

    private lateinit var binding: FragmentInviteTowingBinding
    private val fireStoreHandler = FireStoreHandler()
    private lateinit var fusedLocationHandler: FusedLocationHandler
    private lateinit var dialogUtils: DialogUtils
    private lateinit var towingMap: TowingMap


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentInviteTowingBinding.inflate(inflater, container, false)
        binding.towingDetailPRG.visibility = View.INVISIBLE
        binding.inviteTowingBTNInvite.visibility = View.VISIBLE
        initValues()
        initMap()
        getRealTimeTowing()
        inviteTowingListener()
        showSystemBars()




        return binding.root
    }

    private fun inviteTowingListener() {
        binding.inviteTowingBTNInvite.setOnClickListener {
            checkPermission()


        }
    }

    private fun initMap() {
        val towingMap = childFragmentManager
            .findFragmentById(binding.inviteTowingMap.id) as SupportMapFragment

        initMapTowing(towingMap)
    }

    private fun initMapTowing(towingMap: SupportMapFragment) {
        this.towingMap = TowingMap(towingMap, requireContext().applicationContext)
    }

    private fun getRealTimeTowing() {
        fireStoreHandler.getRealTimeTowing(object : FireStoreHandler.TowingDetailCallBack {
            override fun towingDetailCallBack(towingDetail: UserDetailForTowing) {
                binding.towingDetailPRG.visibility = View.INVISIBLE
                binding.inviteTowingBTNInvite.visibility = View.GONE






                towingMap.setLocationOnMap(towingDetail.latitude, towingDetail.longitude)
            }


        })
    }


    private fun sendRequestGetNearbyTowing(user: User, latitude: Double, longitude: Double) {
        val userDetailForTowing = UserDetailForTowing(
            user.name,
            user.imageUri,
            latitude,
            longitude,
            user.companyCar,
            user.modelCar,
            user.id
        )
        fireStoreHandler.sendRequestForTowing(userDetailForTowing);


    }

    private fun initValues() {
        dialogUtils = DialogUtils()
        fusedLocationHandler = FusedLocationHandler(requireContext())

    }

    override fun onStart() {
        super.onStart()
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
    }

    private fun showSystemBars() {

        activity?.let { WindowCompat.setDecorFitsSystemWindows(it.window, false) }
        activity?.let {
            WindowInsetsControllerCompat(
                it.window,
                binding.root
            ).show(WindowInsetsCompat.Type.systemBars())
        }
    }


    private fun checkPermission() {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) -> {
                startRequestToTowing()
                // You can use the API that requires the permission.
            }
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) -> {
                startRequestToTowing()

            }
            else -> {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                startLauncherLocation()

            }
        }

    }

    private fun startRequestToTowing() {
        binding.towingDetailPRG.visibility = View.VISIBLE
        binding.inviteTowingBTNInvite.visibility = View.INVISIBLE
        fireStoreHandler.getUserDetail(object : FireStoreHandler.GetUserDetail {
            override fun getUserDetailCallBack(user: User) {

                fusedLocationHandler.initGetLastLocationCallBack(object :
                    GetLastLocationCallBack {

                    override fun getLastLocationCallBack(latitude: Double, longitude: Double) {


                        sendRequestGetNearbyTowing(user, latitude, longitude);


                    }

                });
                fusedLocationHandler.getLastLocation()
            }

        });

    }

    private fun startLauncherLocation() {
        requestPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),


            )

    }

    @RequiresApi(Build.VERSION_CODES.N)
    val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                startRequestToTowing()
                // Precise location access granted.
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                startRequestToTowing()
                // Only approximate location access granted.
            }
            else -> {
                permissionDenied()
                // No location access granted.
            }
        }
    }

    private fun permissionDenied() {

        // It is still possible to view the confirmation request message
        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {

            dialogUtils.showDialog(requireContext(), getString(R.string.msgAgreeLocation),
                R.style.MyThemeOverlay_MaterialAlertDialog,
                object : DialogUtils.ResponseCallBack {
                    override fun whichButtonIsPressed(btn: DialogUtils.ButtonPressed) {
                        if (DialogUtils.ButtonPressed.Continue == btn) {
                            startLauncherLocation()

                        }
                    }

                })
            // After twice the system has presented to the user
        } else {
            // make toast with msg that help the user to accept permission prom settings

            dialogUtils.showDialog(requireContext(), getString(R.string.msgAgreeMoveToSetting),
                R.style.MyThemeOverlay_MaterialAlertDialog,
                object : DialogUtils.ResponseCallBack {
                    override fun whichButtonIsPressed(btn: DialogUtils.ButtonPressed) {
                        if (DialogUtils.ButtonPressed.Continue == btn) {
                            openPermissionSettings()

                        }
                    }


                })
        }

    }

    private fun openPermissionSettings() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", activity?.packageName, null)
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)


    }

    override fun onStop() {
        super.onStop()
        dialogUtils.dialog?.dismiss()

    }
}