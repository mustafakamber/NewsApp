package com.mustk.newsapp.ui.view

import android.app.AlertDialog
import android.content.Context
import android.view.KeyEvent
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.mustk.newsapp.R

class CheckConnectionFragment(private val context: Context) {

    private var alertDialogCheckConnection: AlertDialog? = null

    fun showCheckConnectionDialog(onCheckPressed : () -> Unit) {
        val builder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        val dialogLayout =
            inflater.inflate(R.layout.fragment_network, null) as ConstraintLayout
        val titleTextView = dialogLayout.findViewById<TextView>(R.id.networkTitleText)
        val checkButton = dialogLayout.findViewById<Button>(R.id.networkCheckButton)
        val networkTitle = context.getString(R.string.network_error)
        titleTextView.text = networkTitle
        checkButton.text = context.getString(R.string.check_connect)
        builder.setView(dialogLayout)
        alertDialogCheckConnection = builder.create()
        alertDialogCheckConnection?.setOnKeyListener { _, keyCode, _ ->
            keyCode == KeyEvent.KEYCODE_BACK
        }
        alertDialogCheckConnection?.setCanceledOnTouchOutside(false)
        alertDialogCheckConnection?.show()
        checkButton.setOnClickListener {
            onCheckPressed()
        }
    }

    fun dismissDialog(){
        alertDialogCheckConnection?.dismiss()
    }
}