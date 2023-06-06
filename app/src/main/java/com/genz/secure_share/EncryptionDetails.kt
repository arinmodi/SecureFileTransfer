package com.genz.secure_share

import android.app.DatePickerDialog
import android.graphics.Color
import android.icu.util.Calendar
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.genz.secure_share.algorithms.AES
import com.genz.secure_share.utils.ButtonClickListener
import com.genz.secure_share.utils.ProgressDialog
import com.genz.secure_share.utils.ResponseDialog
import com.genz.secure_share.viewmodels.EncryptionViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Encryption Details Fragment,
 * user selects the algorithm, expiry and pass
 * and encrypt the file
 *
 * Only Open From Upload Fragment
 */
class EncryptionDetails : Fragment(), AdapterView.OnItemSelectedListener, ButtonClickListener {

    /**
     * View of the screen
     */
    private lateinit var fileName: TextView
    private lateinit var spinner: Spinner
    private lateinit var pass: EditText
    private lateinit var date: TextView
    private lateinit var generate: CardView

    /**
     * Dialogs
     */
    private lateinit var datePickerDialog: DatePickerDialog
    private lateinit var progressDialog: ProgressDialog
    private lateinit var responseDialog: ResponseDialog
    private lateinit var noInternet: NoInternet

    /**
     * Calender Instance
     */
    private val calendar = Calendar.getInstance()

    /**
     * bundle to get parameters from
     * upload fragment
     */
    private var bundle: Bundle? = null

    /**
     * View Modal for observing file uploading
     */
    private lateinit var encryptionViewModel: EncryptionViewModel

    /**
     * Indicates Selected Algorithm By User,
     * value comes from the spinner
     */
    private var algorithmSelected = "AES-128"

    /**
     * Refers to file result of the uploading file,
     * used to open the corresponding fragment
     */
    private var isUploadSuccess = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_encryption_details, container, false)
    }

    /**
     * Invokes when file successfully uploaded,
     * handles the dialog
     */
    private fun uploadImageSuccess() {
        // uploaded successfully, hide progress dialog and show success dialog
        responseDialog.showDialog()
        progressDialog.dismiss()
        responseDialog.setButtonText("OK")
        responseDialog.setMsg("File Encrypted...")
        responseDialog.setImage(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.success
            )
        )
        isUploadSuccess = true
        Toast.makeText(
            requireContext(), "File Uploaded & Stored",
            Toast.LENGTH_LONG
        ).show()
    }

    /**
     * Invokes when file upload fail,
     * handles the dialog
     */
    private fun uploadImageFail() {
        // error occurred, hide progress dialog and show error dialog
        responseDialog.showDialog()
        progressDialog.dismiss()
        responseDialog.setButtonText("Try Again")
        responseDialog.setMsg("Something Bad Happen")
        responseDialog.setImage(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.error
            )
        )
        isUploadSuccess = false
        Toast.makeText(
            requireContext(), "File Not Uploaded. Try Again",
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        encryptionViewModel = EncryptionViewModel(requireActivity().application)
        bundle = arguments

        // expiry/date picker dialog
        datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, monthOfYear, dayOfMonth ->
                calendar.set(year, monthOfYear, dayOfMonth)

                // Display the selected date
                val formattedDate = SimpleDateFormat(
                    "dd-MM-yyyy",
                    Locale.getDefault()
                ).format(calendar.time)
                date.text = formattedDate
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        progressDialog = ProgressDialog(requireActivity())
        responseDialog = ResponseDialog(requireActivity(), this)
        noInternet = NoInternet(requireActivity())

        // ui assignments
        val nextButton = view.findViewById<CardView>(R.id.nextButton)
        fileName = view.findViewById(R.id.selectedFile)
        spinner = view.findViewById(R.id.spinner)
        pass = view.findViewById(R.id.pass)
        date = view.findViewById(R.id.expiry)
        generate = view.findViewById(R.id.generate)


        val displayText = "File : ${bundle?.getString("name")}"

        // algorithm dropdown, spinner
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.algorithms,
            R.layout.algorithm_item
        ).also { adapter ->
            adapter.setDropDownViewResource(R.layout.algorithm_item_unselected)
            spinner.adapter = adapter
        }

        calendar.add(Calendar.YEAR, 1)
        datePickerDialog.datePicker.maxDate = calendar.time.time
        calendar.add(Calendar.YEAR, -1)
        calendar.add(Calendar.DAY_OF_MONTH, 2)
        datePickerDialog.datePicker.minDate = calendar.time.time

        val sd = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val defaultExpiry = sd.format(calendar.timeInMillis)

        fileName.text = displayText
        spinner.onItemSelectedListener = this
        date.text = defaultExpiry

        date.setOnClickListener { openDateDialog() }
        nextButton.setOnClickListener { onNext() }
        generate.setOnClickListener { generatePass() }

        lifecycleScope.launchWhenCreated {
            encryptionViewModel.storeEvent.observe(viewLifecycleOwner) {
                when (it) {
                    is EncryptionViewModel.MainEvent.Success -> {
                        uploadImageSuccess()
                    }

                    is EncryptionViewModel.MainEvent.Failure -> {
                        uploadImageFail()
                    }

                    else -> {
                        Log.i("FIle Uploaded", "Loading...")
                    }
                }
            }

            (requireActivity() as MainActivity).networkState.observe(viewLifecycleOwner) {
                if (it) {
                    noInternet.dismiss()
                } else {
                    noInternet.show()
                }
            }
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
        Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
    }

    /**
     * when date is clicked, open date picker dialog and set buttons color
     */
    private fun openDateDialog() {
        datePickerDialog.show()
        datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(Color.RED)
        datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(Color.GREEN)
    }

    /**
     * Handler For Next Button
     */
    private fun onNext() {
        if (date.text.isEmpty() || pass.text.isEmpty()) {
            showToast("Please Enter Proper Details")
            return
        }

        // currently we support only AES that's why probably not required when statement
        // but in future we many include other algorithms
        when (algorithmSelected) {
            "AES-128", "AES-192", "AES-256" -> {
                encryptionViewModel.algo = algorithmSelected
                encryptionViewModel.pass = pass.text.toString()
                encryptionViewModel.fileName = bundle?.getString("name") ?: ""

                try {
                    // filePath(Uri) from Upload Fragment, it will never null
                    val path = bundle?.getString("path") ?: return

                    val secretKey = AES.stringToSecretKey(pass.text.toString())
                    val requiredSize = (secretKey?.encoded?.size ?: 1) * 8
                    val selectedSize = algorithmSelected.split("-")[1].toInt()

                    if (secretKey == null || requiredSize != selectedSize) {
                        showToast("Algorithm and Pass Size Miss-match")
                        return
                    }

                    val randomIV = AES.generateRandomIV()
                    val encodedFile = AES.encryptFile(
                        context = requireContext(),
                        filePath = Uri.parse(path),
                        secretKey = secretKey,
                        fileName = bundle?.getString("name") ?: "",
                        iv = randomIV
                    )

                    if (encodedFile == null) {
                        showToast("Unsupported file format")
                        return
                    }

                    progressDialog.showDialog()
                    val iv = Base64.getEncoder().encodeToString(randomIV)

                    encryptionViewModel.uploadImage(
                        file = encodedFile,
                        expiry = date.text.toString(),
                        iv = iv
                    )
                } catch (e: Exception) {
                    Log.e("E : ", e.toString())
                    showToast("Something Bad Happen! Try Again...")
                }
            }
        }
    }

    /**
     * Generates Random Correct Sized Pass,
     * internally calling AES class function
     */
    private fun generatePass() {
        // currently we support only AES that's why probably not required when statement
        // but in future we many include other algorithms
        when (algorithmSelected) {
            "AES-128", "AES-192", "AES-256" -> {
                val keySize = (algorithmSelected.split("-")[1]).toInt()
                val key = AES.secretKeyToString(AES.generateSecretKey(keySize))
                if (key != null) {
                    pass.setText(key)
                } else {
                    pass.setText("")
                }
            }
        }
    }

    /**
     * Invokes when Response Button Dialog click
     */
    override fun onResponseDialogButtonClick() {
        if (isUploadSuccess) {
            responseDialog.dismiss()
            // upload success, direct user to Files Fragment
            requireActivity().supportFragmentManager.beginTransaction().remove(this)
                .commit()
            (requireActivity() as MainActivity).loadFragment(FilesFragment())
        } else {
            // upload fail, direct user to upload again
            responseDialog.dismiss()
            requireActivity().supportFragmentManager.beginTransaction().remove(this)
                .commit()
            requireActivity().supportFragmentManager.popBackStack()
            (requireActivity() as MainActivity).loadFragment(UploadFragment())
        }
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {}
}