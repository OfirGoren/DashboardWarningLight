package com.example.dashboardwarninglightsapp.utils

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

class WindowUtils {

    companion object {

        fun closeKeyBoardView(activity: Activity, view: View?) {
            val imm =
                activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view?.applicationWindowToken, 0)

        }

    }
}