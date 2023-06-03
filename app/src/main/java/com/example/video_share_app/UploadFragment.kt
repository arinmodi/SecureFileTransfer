package com.example.video_share_app

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.example.video_share_app.utils.PermissionsUtil
import java.io.ByteArrayInputStream
import java.io.File

class UploadFragment : Fragment() {

    // file selection button
    private lateinit var selectButton : CardView

    // selected file name UI
    private lateinit var  fileNameView : TextView

    // next button
    private lateinit var nextButton : CardView

    // theme button
    private lateinit var themeButton : ImageView

    // selected file name
    private var fileName : String = ""

    //selected file path
    private lateinit var filePath : String

    // refers to status of theme
    private var isDarkMode = false


    // permission launcher
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts
        .RequestPermission())
    { isGranted: Boolean ->
        if (isGranted) {
            sendFileSelectionIntent()
        } else {
            Toast.makeText(requireContext(), "Permission Denied", Toast.LENGTH_LONG).show()
        }
    }

    // file selection launcher
    private val requestFileLauncher = registerForActivityResult(ActivityResultContracts
        .StartActivityForResult()) {
        val code = it.resultCode
        val data = it.data

        if (code == Activity.RESULT_OK) {
            if (data != null) {
                val uri : Uri = data.data as Uri
                val cursor = requireContext().contentResolver.query(uri, null, null, null, null)
                val nameIndex = cursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                cursor?.moveToFirst()
                fileName = if (nameIndex == null) {
                    ""
                } else {
                    cursor.getString(nameIndex)
                }
                filePath = uri.toString()
                displayName()
            } else {
                Toast.makeText(requireContext(), "Something Bad Happen!", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(requireContext(), "Something Bad Happen!", Toast.LENGTH_LONG).show()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        selectButton = view.findViewById(R.id.selectButton)
        selectButton.setOnClickListener { fileSelection() }

        fileNameView = view.findViewById(R.id.filename)

        nextButton = view.findViewById(R.id.next)
        nextButton.setOnClickListener { next() }

        themeButton = view.findViewById(R.id.theme)
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        isDarkMode = currentNightMode == Configuration.UI_MODE_NIGHT_YES
        if (isDarkMode) {
            themeButton.setImageResource(R.drawable.light_mode_24)
        } else {
            themeButton.setImageResource(R.drawable.dark_mode)
        }
        themeButton.setOnClickListener { setTheme() }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_upload, container, false)
    }

    // file selection button click handler
    private fun fileSelection() {
        if (PermissionsUtil.isStorageReadPermission(requireContext())) {
            sendFileSelectionIntent()
        } else {
            requestPermissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    // launch storage file pick intent
    private fun sendFileSelectionIntent() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        requestFileLauncher.launch(intent)
    }

    // display filename
    private fun displayName() {
        fileNameView.text = fileName
    }

    // next button click handler
    private fun next() {
        if (fileName.isEmpty()) {
            Toast.makeText(requireContext(), "Please Select A File First!", Toast.LENGTH_LONG).show()
        } else {
            val detailsFrag = EncryptionDetails()
            val fileBundle = Bundle()
            fileBundle.putString("name", fileName)
            fileBundle.putString("path", filePath)
            detailsFrag.arguments = fileBundle
            val activity = activity as MainActivity
            activity.loadFragment(detailsFrag)
        }
    }

    // change the theme
    private fun setTheme() {
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
    }
}