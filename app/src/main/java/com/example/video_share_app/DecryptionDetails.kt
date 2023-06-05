package com.example.video_share_app

import android.content.ActivityNotFoundException
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.*
import android.util.Log
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.video_share_app.algorithms.AES
import com.example.video_share_app.utils.ButtonClickListener
import com.example.video_share_app.utils.PermissionsUtil
import com.example.video_share_app.utils.ProgressDialog
import com.example.video_share_app.utils.ResponseDialog
import java.io.File
import java.util.*
import java.util.concurrent.Executors
import javax.crypto.SecretKey

/**
 * Decryption Details Fragment,
 * user selects the algorithm and pass
 * and decrypt the file
 *
 * Only Open From ViewFile Fragment
 */
class DecryptionDetails : AppCompatActivity(), OnItemSelectedListener, ButtonClickListener {

    /**
     * views of the screen
     */
    private lateinit var fileName: TextView
    private lateinit var algorithm: Spinner
    private lateinit var pass: EditText
    private lateinit var pasteButton: CardView
    private lateinit var pasteButtonText: TextView
    private lateinit var viewFileButton: CardView
    private lateinit var viewFileButtonText: TextView

    /**
     * Dialogs
     */
    private lateinit var progressDialog: ProgressDialog
    private lateinit var responseDialog: ResponseDialog

    /**
     * Indicates Selected Algorithm By User,
     * value comes from the spinner
     */
    private var algorithmSelected = "AES-128"

    /**
     * Refers to file saved to external storage,
     * after decryption.
     *
     * used to open the file using intent
     */
    private lateinit var savedFile: File

    /**
     * Read and Write External Storage Permission Launcher
     */
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    )
    { isGranted: Boolean ->
        if (isGranted) {
            showToast("Permission Grated, Thank You!")
        } else {
            showToast("Permission Denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_decryption_details)

        // get values from intent
        val name = "File : " + intent.getStringExtra("name")
        val url = intent.getStringExtra("url").toString()
        val iv = intent.getStringExtra("iv").toString()
        Log.i("IV : ", iv)

        val clipBoardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager

        // ui assignments
        progressDialog = ProgressDialog(this)
        responseDialog = ResponseDialog(this, this)
        fileName = findViewById(R.id.selectedFile)
        pass = findViewById(R.id.pass)
        algorithm = findViewById(R.id.spinner)
        pasteButton = findViewById(R.id.generate)
        pasteButtonText = findViewById(R.id.btnText)
        viewFileButton = findViewById(R.id.viewFileButton)
        viewFileButtonText = findViewById(R.id.nextButtonText)

        fileName.text = name
        pasteButtonText.text = resources.getString(R.string.paste)
        viewFileButtonText.text = resources.getString(R.string.view_file)

        // for spinner
        ArrayAdapter.createFromResource(
            this,
            R.array.algorithms,
            R.layout.algorithm_item
        ).also { adapter ->
            adapter.setDropDownViewResource(R.layout.algorithm_item_unselected)
            algorithm.adapter = adapter
        }
        algorithm.onItemSelectedListener = this


        pasteButton.setCardBackgroundColor(
            ContextCompat.getColor(
                this,
                R.color.buttonColor2
            )
        )

        viewFileButton.setCardBackgroundColor(
            ContextCompat.getColor(
                this,
                R.color.buttonColor1
            )
        )

        pasteButtonText.setOnClickListener {
            pass.setText(clipBoardManager.primaryClip?.getItemAt(0)?.text?.toString())
        }

        viewFileButton.setOnClickListener {
            onViewFile(intent.getStringExtra("name").toString(), url, iv)
        }
    }

    /**
     * invokes when item selected in the spinner(dropdown)
     */
    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        algorithmSelected = resources.getStringArray(R.array.algorithms)[p2]
    }

    /**
     * show toast message
     */
    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }

    /**
     * Handler For View File Button
     */
    private fun onViewFile(fileName: String, url: String, iv: String) {
        if (pass.text.isEmpty()) {
            showToast("Please Enter Proper Details")
            return
        }

        // currently we support only AES that's why probably not required when
        // but in future we many include other algorithms
        when (algorithmSelected) {
            "AES-128", "AES-192", "AES-256" -> {
                val secretKey = AES.stringToSecretKey(pass.text.toString())

                val requiredSize = (secretKey?.encoded?.size ?: 1) * 8
                val selectedSize = algorithmSelected.split("-")[1].toInt()

                if (secretKey == null || requiredSize != selectedSize) {
                    showToast("Bad Pass")
                    return
                }

                // check for permission, read and write
                if (!PermissionsUtil.isStorageReadPermission(this)) {
                    requestPermissionLauncher.launch(
                        android.Manifest.permission
                            .READ_EXTERNAL_STORAGE
                    )
                    return
                }

                if (!PermissionsUtil.isStorageWritePermission(this)) {
                    requestPermissionLauncher.launch(
                        android.Manifest.permission
                            .WRITE_EXTERNAL_STORAGE
                    )
                    return
                }

                try {
                    progressDialog.showDialog()
                    progressDialog.setMessage("unlocking the file...")
                    NetworkTask().doInBackground(this, secretKey, url, fileName, iv)
                } catch (e: Exception) {
                    Log.e("Decryption Error : ", e.toString())
                    progressDialog.dismiss()
                }
            }
        }
    }

    /**
     * Call DecryptFile function of AES,
     * which internally reading data from url,
     * that's has to be the background task
     */
    inner class NetworkTask {
        private val executor = Executors.newSingleThreadExecutor()
        private val handler = Handler(Looper.getMainLooper())

        fun doInBackground(context: Context, secretKey: SecretKey, vararg params: String) {
            executor.execute {
                try {
                    val url = params[0]
                    val name = params[1]
                    val iv = params[2]

                    // decrypt file
                    val file = AES.decryptFile(
                        context = context,
                        url = url,
                        secretKey = secretKey,
                        fileName = name,
                        iv = Base64.getDecoder().decode(iv)
                    )

                    handler.post {
                        postDecryptionActions(file)
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Invalid Details", Toast.LENGTH_LONG).show()
                }
            }

        }
    }

    /**
     * once file deprecated,
     * copy internal storage file to external storage
     */
    private fun postDecryptionActions(file: File?) {
        if (file == null) {
            showToast("Invalid Details")
            progressDialog.dismiss()
            return
        }

        val destination: File = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            File(
                Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOCUMENTS
                ).toString() + "/" +
                        getString(R.string.app_name)
            )
        } else {
            File(
                Environment.getExternalStorageDirectory().toString() + "/" +
                        getString(R.string.app_name)
            )
        }

        val targetFile = File(destination, file.name)

        try {
            if (!destination.exists()) {
                val response = destination.mkdir()
                if (!response) {
                    progressDialog.dismiss()
                    showToast("We are not able to save file to storage")
                    return
                }
            }

            // save file
            if (!targetFile.exists()) {
                file.copyTo(targetFile)
            }

            // show response on the screen
            showToast("File Saved!")
            progressDialog.dismiss()
            responseDialog.showDialog()
            responseDialog.setImage(ContextCompat.getDrawable(this, R.drawable.unlock))
            responseDialog.setMsg("File Unlocked, Open the file")
            responseDialog.setButtonText("Open")
            savedFile = targetFile
        } catch (e: Exception) {
            showToast("Something Bad Happen")
            Log.e("Storage Error : ", "Saving File...")
            e.printStackTrace()
            progressDialog.dismiss()
        }
    }

    /**
     * Invokes when Response Button Dialog click
     */
    override fun onResponseDialogButtonClick() {
        responseDialog.dismiss()
        // open the file
        val myMime = MimeTypeMap.getSingleton()
        val newIntent = Intent(Intent.ACTION_VIEW)
        val extension = fileName.text.substring(fileName.text.lastIndexOf("."))
        Log.e("Ext : ", extension)
        val mimeType = myMime.getMimeTypeFromExtension(extension)
            ?.substring(1)
        newIntent.setDataAndType(FileProvider.getUriForFile(this,
            BuildConfig.APPLICATION_ID + ".provider",savedFile), mimeType)
        newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        newIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        try {
            startActivity(newIntent)
        } catch (e: ActivityNotFoundException) {
            showToast("No handler for this type of file")
        }
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {}
}