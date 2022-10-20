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
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.registerForActivityResult
import androidx.fragment.app.Fragment
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.dashboardwarninglightsapp.R
import com.example.dashboardwarninglightsapp.databinding.FragmentMapBinding
import com.example.dashboardwarninglightsapp.interfaces.IOnFocusListenable
import com.example.dashboardwarninglightsapp.objects.MapGarages
import com.example.dashboardwarninglightsapp.utils.DialogUtils

import com.google.android.gms.maps.SupportMapFragment


class MapFragment : Fragment(), IOnFocusListenable {

    private lateinit var binding: FragmentMapBinding
    private lateinit var mapGarages: MapGarages
    private lateinit var dialogUtils: DialogUtils


    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMapBinding.inflate(inflater, container, false)

        //  activity?.theme?.applyStyle(R.style.myThemeActivity, true);
        initValues()
        initCallBacks()

        val mapFragment = childFragmentManager
            .findFragmentById(binding.map.id) as SupportMapFragment

        initMapGarages(mapFragment)
        //  mapGarages.updateMapWithCurrentLocation()
        showSystemBars()

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
        checkPermission()


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

    private fun initValues() {
        dialogUtils = DialogUtils()
    }


    private fun initCallBacks() {


    }

    private fun initMapGarages(mapFragment: SupportMapFragment) {
        mapGarages = MapGarages(mapFragment, requireContext().applicationContext)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                mapGarages.updateMapWithCurrentLocation()
                // Precise location access granted.
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                mapGarages.updateMapWithCurrentLocation()
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

            dialogUtils.showDialog(requireContext(), getString(R.string.msgAgreeYourLocation),
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

    private fun startLauncherLocation() {
        requestPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),


            )

    }

    private fun checkPermission() {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) -> {
                mapGarages.updateMapWithCurrentLocation()
                // You can use the API that requires the permission.
            }
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) -> {

                mapGarages.updateMapWithCurrentLocation()
            }
            else -> {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                startLauncherLocation()

            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {

    }

    override fun onStop() {
        super.onStop()
        dialogUtils.dialog?.dismiss()

    }

}




