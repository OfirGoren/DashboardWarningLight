package com.example.dashboardwarninglightsapp.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.example.dashboardwarninglightsapp.R
import com.example.dashboardwarninglightsapp.databinding.FragmentMenuBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class MenuFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentMenuBinding
    private var menuBtnPressedCallBack: BtnPressedCallBack? = null

    interface BtnPressedCallBack {
        fun btnPressedFromBottomSheet(btnId: Int)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //  setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme);
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (dialog as? BottomSheetDialog)?.behavior?.state = STATE_EXPANDED
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment


        binding = FragmentMenuBinding.inflate(inflater, container, false)
        setClickListeners()

        return binding.root
    }

    private fun setClickListeners() {
        binding.bottomNavigation.setOnItemSelectedListener { menuIdPressed ->

            this.dismiss()
            menuBtnPressedCallBack?.btnPressedFromBottomSheet(menuIdPressed.itemId)
            return@setOnItemSelectedListener true
        }

    }


    fun initBtnCallBack(btnMenuPressedCallBack: BtnPressedCallBack) {

        this.menuBtnPressedCallBack = btnMenuPressedCallBack


    }

    private val nearbyGaragesPressed = View.OnClickListener { view ->
        this.dismiss()
        //  menuBtnPressedCallBack?.btnPressedFromBottomSheet()


//
//        navController.clearBackStack(R.id.toyota_c_hr_dash_fragment2)

    }


}