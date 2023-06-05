package com.example.video_share_app

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
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

/**
 * Upload Fragment, User select the file to share
 */
class UploadFragment : Fragment() {

    /**
     * UI Views
     */
    private lateinit var selectButton: CardView
    private lateinit var fileNameView: TextView
    private lateinit var nextButton: CardView
    private lateinit var themeButton: ImageView

    /**
     * Refers to selected file name
     */
    private var fileName: String = ""

    /**
     * Refers to selected file path
     */
    private lateinit var filePath: String

    /**
     * Refers to mode of the app
     */
    private var isDarkMode = false

    /**
     * Read External Storage Permission Launcher
     */
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts
            .RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            sendFileSelectionIntent()
        } else {
            Toast.makeText(requireContext(), "Permission Denied", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_upload, container, false)
    }

    /**
     * show toast message
     */
    private fun showToast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
    }

    /**
     * File Selection Launcher
     */
    private val requestFileLauncher = registerForActivityResult(
        ActivityResultContracts
            .StartActivityForResult()
    ) {
        val code = it.resultCode
        val data = it.data

        if (code == Activity.RESULT_OK) {
            if (data != null) {
                val uri: Uri = data.data as Uri
                val cursor = requireContext().contentResolver.query(
                    uri, null, null, null, null)
                val nameIndex = cursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor?.getColumnIndex(OpenableColumns.SIZE)
                cursor?.moveToFirst()
                val size = if (sizeIndex == null) { 0 } else { cursor.getLong(sizeIndex) }
                val sizeInMB = size / 1024 / 1024

                fileName = if (nameIndex == null) { "" } else { cursor.getString(nameIndex) }
                filePath = uri.toString()

                if (sizeInMB >= 11) {
                    filePath = ""
                    fileName = ""
                    showToast("Sorry! Currently, we don't support large files")
                }

                displayName()
            } else {
                showToast("Something Bad Happen!")
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isDarkMode = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES

        // ui assignments
        selectButton = view.findViewById(R.id.selectButton)
        fileNameView = view.findViewById(R.id.filename)
        nextButton = view.findViewById(R.id.next)
        themeButton = view.findViewById(R.id.theme)

        if (isDarkMode) {
            themeButton.setImageResource(R.drawable.light_mode_24)
        } else {
            themeButton.setImageResource(R.drawable.dark_mode)
        }

        selectButton.setOnClickListener { fileSelection() }
        nextButton.setOnClickListener { next() }
        themeButton.setOnClickListener { setTheme() }
    }

    /**
     * Invokes when user click on Select File Button
     */
    private fun fileSelection() {
        if (PermissionsUtil.isStorageReadPermission(requireContext())) {
            sendFileSelectionIntent()
        } else {
            requestPermissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    /**
     * Open File Manager To Select File
     */
    private fun sendFileSelectionIntent() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        requestFileLauncher.launch(intent)
    }

    /**
     * Display Selected File Name to View
     */
    private fun displayName() {
        fileNameView.text = fileName
    }

    /**
     * Invokes when user clicks on next button,
     * Open Encryption Detail Fragment
     */
    private fun next() {
        if (fileName.isEmpty()) {
            showToast("Please Select A File First!")
            return
        }

        val detailsFrag = EncryptionDetails()
        val fileBundle = Bundle()
        fileBundle.putString("name", fileName)
        fileBundle.putString("path", filePath)
        detailsFrag.arguments = fileBundle
        val activity = activity as MainActivity
        activity.loadFragment(detailsFrag)
    }

    /**
     * Invokes when user clicks on theme icon
     */
    private fun setTheme() {
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            requireActivity().supportFragmentManager.popBackStack()
            requireActivity().recreate()
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            requireActivity().supportFragmentManager.popBackStack()
            requireActivity().recreate()
        }
    }
}