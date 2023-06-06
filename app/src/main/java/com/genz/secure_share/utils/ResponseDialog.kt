package com.genz.secure_share.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.genz.secure_share.R

interface ButtonClickListener {
    fun onResponseDialogButtonClick()
}

class ResponseDialog(activity: Activity, buttonClickListener: ButtonClickListener) {
    private var activity : Activity
    private var alertDialog: AlertDialog? = null
    private var buttonClickListener : ButtonClickListener? = null

    private var image : ImageView? = null
    private var message : TextView? = null
    private var buttonText : TextView? = null

    init {
        this.activity = activity
        this.buttonClickListener = buttonClickListener
    }

    @SuppressLint("InflateParams")
    fun showDialog() {
        val builder = AlertDialog.Builder(activity)
        val inflater = activity.layoutInflater
        builder.setView(inflater.inflate(R.layout.response_dialog, null))
        builder.setCancelable(false)

        alertDialog = builder.create()
        alertDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog?.show()

        image = alertDialog?.findViewById(R.id.img)
        message = alertDialog?.findViewById(R.id.text)
        buttonText = alertDialog?.findViewById(R.id.buttonText)

        alertDialog?.findViewById<CardView>(R.id.button)?.setOnClickListener {
            buttonClickListener?.onResponseDialogButtonClick()
        }

    }

    fun setImage(resource : Drawable?) {
        image?.setImageDrawable(resource)
    }

    fun setButtonText(msg : String) {
        buttonText?.text = msg
    }

    fun setMsg(msg : String) {
        message?.text = msg
    }

    fun dismiss() {
        alertDialog?.dismiss()
    }
}