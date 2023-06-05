package com.example.video_share_app

import android.annotation.SuppressLint
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.video_share_app.algorithms.AES
import com.example.video_share_app.model.FileResponse
import com.example.video_share_app.utils.PermissionsUtil
import java.io.File
import java.util.Base64
import java.util.concurrent.Executors
import javax.crypto.SecretKey

class DecryptionDetails : AppCompatActivity(), OnItemSelectedListener {

    private lateinit var fileName : TextView
    private lateinit var algorithm : Spinner
    private lateinit var pass : EditText
    private lateinit var pasteButton : CardView
    private lateinit var pasteButtonText : TextView
    private lateinit var viewFileButton : CardView
    private lateinit var viewFileButtonText : TextView

    private var algorithmSelected = "AES-128"

    // permission launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts
        .RequestPermission())
    { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this, "Permission Grated, Thank You!", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "Permission Denied", Toast.LENGTH_LONG).show()
        }
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_decryption_details)

        val name = "File : " + intent.getStringExtra("name")
        val url = intent.getStringExtra("url").toString()
        val iv = intent.getStringExtra("iv").toString()

        Log.i("IV : ", iv)

        val clipBoardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager

        fileName = findViewById(R.id.selectedFile)
        fileName.text = name

        algorithm = findViewById(R.id.spinner)
        ArrayAdapter.createFromResource(
            this,
            R.array.algorithms,
            R.layout.algorithm_item
        ).also { adapter ->
            adapter.setDropDownViewResource(R.layout.algorithm_item_unselected)
            algorithm.adapter = adapter
        }
        algorithm.onItemSelectedListener = this

        pass = findViewById(R.id.pass)

        pasteButton = findViewById(R.id.generate)
        pasteButton.setCardBackgroundColor(ContextCompat.getColor(this,
            R.color.buttonColor2))
        pasteButtonText = findViewById(R.id.btnText)
        pasteButtonText.text = resources.getString(R.string.paste)
        pasteButtonText.setOnClickListener {
            pass.setText(clipBoardManager.primaryClip?.getItemAt(0)?.text?.toString())
        }

        viewFileButton = findViewById(R.id.viewFileButton)
        viewFileButton.setCardBackgroundColor(ContextCompat.getColor(this,
            R.color.buttonColor1))
        viewFileButtonText = findViewById(R.id.nextButtonText)
        viewFileButtonText.text = resources.getString(R.string.view_file)
        viewFileButton.setOnClickListener {
            onViewFile(intent.getStringExtra("name").toString(), url, iv)
        }
    }

    private fun onViewFile(fileName : String, url : String, iv : String) {
        if (pass.text.isNotEmpty()) {
            if (algorithmSelected.contains("AES")) {
                if (PermissionsUtil.isStorageWritePermission(this)) {
                    val secretKey = AES.stringToSecretKey(pass.text.toString())
                    try {
                        NetworkTask().doInBackground(this, secretKey, url, fileName, iv)
                    } catch (e: Exception) {
                        Log.e("Decryption Error : ", e.toString())
                    }
                } else {
                    requestPermissionLauncher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }
        } else {
            Toast.makeText(this, "Please Enter Proper Details",
                Toast.LENGTH_LONG).show()
        }
    }

    inner class NetworkTask {
        private val executor = Executors.newSingleThreadExecutor()
        private val handler = Handler(Looper.getMainLooper())

        fun doInBackground(context : Context, secretKey : SecretKey, vararg params : String?) {
            executor.execute {
                try {
                    val file = params[0]?.let {
                        params[1]?.let { it1 ->
                            params[2]?.let { it2 ->
                                AES.decryptFile(
                                    context = context,
                                    it, secretKey, it1, Base64.getDecoder().decode(it2)
                                )
                            }
                        }
                    }

                    handler.post {
                        if (file != null) {
                            var destination : File? = null

                            destination = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                File(Environment.getExternalStoragePublicDirectory(
                                    Environment.DIRECTORY_DOCUMENTS).toString() + "/" +
                                        getString(R.string.app_name)
                                )
                            } else {
                                File(Environment.getExternalStorageDirectory().toString() + "/" +
                                        getString(R.string.app_name)
                                )
                            }

                            val targetFile = File(destination, file.name)

                            if (!destination.exists()) {
                                if (destination.mkdirs()) {
                                    file.copyTo(targetFile)
                                    Toast.makeText(context, "File Saved!", Toast.LENGTH_LONG)
                                        .show()
                                }
                            }else {
                                file.copyTo(targetFile)
                                Toast.makeText(context, "File Saved!", Toast.LENGTH_LONG)
                                    .show()
                            }
                        }
                    }
                } catch (e : Exception) {
                    Toast.makeText(context, "Invalid Details", Toast.LENGTH_LONG).show()
                }
            }

        }
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        algorithmSelected = resources.getStringArray(R.array.algorithms)[p2]
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {}
}