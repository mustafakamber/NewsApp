package com.mustk.newsapp.ui.view

import android.app.AlertDialog
import android.content.Context
import android.view.KeyEvent
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.mustk.newsapp.R

class LoadingFragment(private val context : Context) {

    private var alertDialogLoading : AlertDialog? = null

    fun showLoadingDialog() {
        val builder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        val dialogLayout =
            inflater.inflate(R.layout.fragment_loading,null) as ConstraintLayout
        builder.setView(dialogLayout)
        alertDialogLoading = builder.create()
        alertDialogLoading?.setOnKeyListener { _, keyCode, _ ->
            keyCode == KeyEvent.KEYCODE_BACK
        }
        alertDialogLoading?.setCanceledOnTouchOutside(false)
        alertDialogLoading?.show()
    }

    fun dismissDialog(){
        alertDialogLoading?.dismiss()
    }
}