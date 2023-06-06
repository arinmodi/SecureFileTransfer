package com.genz.secure_share.utils

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log


/**
 * Interface for network state change
 * basically act as listener
 */
interface NetworkChangeListener {
    fun onNetWorkChange(state : Boolean)
}

/**
 * BroadCastReceiver for Network change listener
 */
class NetworkChangeReceiver : BroadcastReceiver() {
    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(p0: Context?, p1: Intent?) {
        Log.e("Hello : ", "Notified")
        if (networkChangeListener != null) {
            networkChangeListener!!.onNetWorkChange(isConnected(p0))
        }
    }

    /**
     * Check the status of the internet connectivity
     */
    private fun isConnected(context: Context?) : Boolean {
        Log.e("Hello", "I am Here")
        val conMng = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities: NetworkCapabilities? = conMng.getNetworkCapabilities(
            conMng.activeNetwork
        )

        var isAvailable = false

        if (capabilities != null) {
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                isAvailable = true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                isAvailable = true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                isAvailable = true
            }
        }

        return isAvailable
    }

    companion object {
        var networkChangeListener : NetworkChangeListener?= null
    }
}