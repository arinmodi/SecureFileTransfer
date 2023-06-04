package com.example.video_share_app.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import com.example.video_share_app.R

class ProgressDialog(activity : Activity) {
    private var activity : Activity
    private var alertDialog: AlertDialog? = null

    init {
        this.activity = activity
    }

    @SuppressLint("InflateParams")
    fun showDialog() {
        val builder = AlertDialog.Builder(activity)
        val inflater = activity.layoutInflater
        builder.setView(inflater.inflate(R.layout.progress_dialog, null))
        builder.setCancelable(false)

        alertDialog = builder.create()
        alertDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog?.show()
    }

    fun dismiss() {
        alertDialog?.dismiss()
    }
}