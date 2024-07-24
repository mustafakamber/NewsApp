package com.mustk.newsapp.ui.view

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.mustk.newsapp.R

class ConfirmationFragment(private val context: Context, private val confirmationTitle: Int) {

    private var alertDialogConfirmation: AlertDialog? = null

    fun showConfirmationDialog(onBackPressed: () -> Unit) {
        val builder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        val dialogLayout =
            inflater.inflate(R.layout.fragment_confirmation, null) as ConstraintLayout
        val titleTextView = dialogLayout.findViewById<TextView>(R.id.dialogTitleText)
        val positiveButton = dialogLayout.findViewById<Button>(R.id.dialogPositiveButton)
        val negativeButton = dialogLayout.findViewById<TextView>(R.id.dialogNegativeButton)
        titleTextView.text = context.getString(confirmationTitle)
        positiveButton.text = context.getString(R.string.yes)
        negativeButton.text = context.getString(R.string.no)
        builder.setView(dialogLayout)
        alertDialogConfirmation = builder.create()
        alertDialogConfirmation?.show()
        positiveButton.setOnClickListener {
            alertDialogConfirmation?.dismiss()
            onBackPressed()
        }
        negativeButton.setOnClickListener {
            alertDialogConfirmation?.dismiss()
        }
    }
}