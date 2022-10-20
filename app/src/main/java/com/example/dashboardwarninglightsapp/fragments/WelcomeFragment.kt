package com.example.dashboardwarninglightsapp.fragments

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import com.example.dashboardwarninglightsapp.R
import com.example.dashboardwarninglightsapp.activity.MainActivity
import com.example.dashboardwarninglightsapp.databinding.FragmentWelcomeBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class WelcomeFragment : Fragment() {
    private lateinit var binding: FragmentWelcomeBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWelcomeBinding.inflate(inflater, container, false)
        clickListeners()
        isUserLogOut()

        return binding.root
    }

    private fun isUserLogOut() {
        val user = Firebase.auth.currentUser
        if (user != null) {
            openMainActivity()
        } else {
            // No user is signed in
        }

    }

    private fun clickListeners() {
        binding.welcomeBTNRegister.setOnClickListener(signUpClick)
        binding.welcomeBTNLogIn.setOnClickListener(logInClick)
    }


    private val signUpClick: View.OnClickListener = View.OnClickListener { view ->
        view.findNavController().navigate(R.id.signUpFragment3)

    }

    private val logInClick: View.OnClickListener = View.OnClickListener { view ->
        view.findNavController().navigate(R.id.logInFragment)

    }

    private fun openMainActivity() {
        val intent = Intent(activity, MainActivity::class.java)
        startActivity(intent)
        activity?.finish()
    }

}