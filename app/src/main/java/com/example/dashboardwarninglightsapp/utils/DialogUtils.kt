package com.example.dashboardwarninglightsapp.utils

import android.app.Activity
import android.content.Context
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.getSystemService
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class DialogUtils {


    var dialog: AlertDialog? = null

    enum class ButtonPressed {
        Cancel, Continue
    }

    interface ResponseCallBack {
        fun whichButtonIsPressed(btn: ButtonPressed)
    }

    fun showDialog(
        context: Context,
        msg: String,
        styleDialogId: Int,
        responseCallBack: ResponseCallBack
    ) {
        dialog = MaterialAlertDialogBuilder(context, styleDialogId)
            .setMessage(msg)
            .setNegativeButton(ButtonPressed.Cancel.name) { dialog, which ->
                responseCallBack.whichButtonIsPressed(ButtonPressed.Cancel)

            }
            .setPositiveButton(ButtonPressed.Continue.name) { dialog, which ->
                responseCallBack.whichButtonIsPressed(ButtonPressed.Continue)


            }.show()


    }


}