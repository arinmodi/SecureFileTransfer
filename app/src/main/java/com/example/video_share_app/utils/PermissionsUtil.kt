package com.example.video_share_app.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

/**
 * Checks for Permission
 */
open class PermissionsUtil {
    companion object {
        /**
         * Checks weather read external storage permission granted
         */
        fun isStorageReadPermission(context : Context) : Boolean {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED
        }

        /**
         * Checks weather Write external storage permission granted
         */
        fun isStorageWritePermission(context : Context) : Boolean {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED
        }
    }
}