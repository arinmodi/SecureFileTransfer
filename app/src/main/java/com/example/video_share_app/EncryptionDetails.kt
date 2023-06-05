package com.example.video_share_app

import android.app.DatePickerDialog
import android.graphics.Color
import android.icu.util.Calendar
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.example.video_share_app.algorithms.AES
import com.example.video_share_app.viewmodels.EncryptionViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.lifecycle.lifecycleScope
import com.example.video_share_app.utils.ProgressDialog
import com.example.video_share_app.utils.ResponseDialog
import java.util.Base64

class EncryptionDetails : Fragment(), AdapterView.OnItemSelectedListener {

    // file name view
    private lateinit var fileName : TextView

    // dropdown view
    private lateinit var spinner: Spinner

    // pass view
    private lateinit var pass : EditText

    // date text view(for expiry)
    private lateinit var date: TextView

    private val calendar = Calendar.getInstance()

    private var bundle: Bundle? = null

    // date Picker view
    private lateinit var datePickerDialog : DatePickerDialog

    // generate pass view
    private lateinit var generate : CardView

    // progress dialog view
    private lateinit var progressDialog: ProgressDialog

    // response dialog
    private lateinit var responseDialog : ResponseDialog

    private lateinit var encryptionViewModel : EncryptionViewModel

    private var algorithmSelected = "AES-128"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        encryptionViewModel = EncryptionViewModel(requireActivity().application)

        bundle = arguments

        // filename view related
        fileName = view.findViewById(R.id.selectedFile)
        val displayText = "File : ${bundle?.getString("name")}"
        fileName.text = displayText

        // algorithm dropdown related
        spinner = view.findViewById(R.id.spinner)
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.algorithms,
            R.layout.algorithm_item
        ).also { adapter ->
            adapter.setDropDownViewResource(R.layout.algorithm_item_unselected)
            spinner.adapter = adapter
        }
        spinner.onItemSelectedListener = this

        // pass view related
        pass = view.findViewById(R.id.pass)

        // expiry/date picker related
        datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, monthOfYear, dayOfMonth ->
                calendar.set(year, monthOfYear, dayOfMonth)

                // Display the selected date
                val formattedDate = SimpleDateFormat("dd-MM-yyyy",
                    Locale.getDefault()).format(calendar.time)
                date.text = formattedDate
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        date = view.findViewById(R.id.expiry)
        calendar.add(Calendar.YEAR, 1)
        datePickerDialog.datePicker.maxDate = calendar.time.time
        calendar.add(Calendar.YEAR, -1)
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        datePickerDialog.datePicker.minDate = calendar.time.time
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        calendar.add(Calendar.MONTH, 1)

        val year = calendar.get(Calendar.YEAR).toString()
        val month = calendar.get(Calendar.MONTH).toString()
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH).toString()

        val defaultExpiry = "$dayOfMonth-$month-$year"
        date.text = defaultExpiry
        date.setOnClickListener { openDateDialog() }

        // next button related
        view.findViewById<CardView>(R.id.nextButton).setOnClickListener {
            validation()
        }

        // generate button related
        generate = view.findViewById(R.id.generate)
        generate.setOnClickListener {
            generatePass()
        }

        // progress dialog related
        progressDialog = ProgressDialog(requireActivity())

        // response dialog related
        responseDialog = ResponseDialog(requireActivity())

        lifecycleScope.launchWhenCreated {
            encryptionViewModel.storeEvent.observe(viewLifecycleOwner) {
                when (it) {
                    is EncryptionViewModel.MainEvent.Success -> {
                        // file uploaded and stored, hide progress dialog and show success dialog
                        responseDialog.showDialog()
                        progressDialog.dismiss()
                        responseDialog.setButtonText("OK")
                        responseDialog.setMsg("File Encrypted...")
                        responseDialog.setImage(ContextCompat.getDrawable(requireContext(),
                            R.drawable.success))
                        Toast.makeText(requireContext(), "File Uploaded & Stored",
                            Toast.LENGTH_LONG).show()
                    }

                    is EncryptionViewModel.MainEvent.Failure -> {
                        // error occurred, hide progress dialog and show error dialog
                        responseDialog.showDialog()
                        progressDialog.dismiss()
                        responseDialog.setButtonText("Try Again")
                        responseDialog.setMsg("Something Bad Happen")
                        responseDialog.setImage(ContextCompat.getDrawable(requireContext(),
                            R.drawable.error))
                        Toast.makeText(requireContext(), "File Not Uploaded. Try Again",
                            Toast.LENGTH_LONG).show()
                    }

                    else -> {
                        Log.i("FIle Uploaded", "Loading...")
                    }
                }
            }
        }
    }

    private fun openDateDialog() {
        datePickerDialog.show()
        datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(Color.RED)
        datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(Color.GREEN)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_encryption_details, container, false)
    }

    /* validate input */
    private fun validation() {
        if (date.text.isNotEmpty() &&  pass.text.isNotEmpty())  {
            if (algorithmSelected.contains("AES")) {
                encryptionViewModel.algo = algorithmSelected
                encryptionViewModel.pass = pass.text.toString()
                encryptionViewModel.fileName = bundle?.getString("name")?: ""
                try {
                    val path = bundle?.getString("path")
                    if (path != null) {
                        val secretKey = AES.stringToSecretKey(pass.text.toString())
                        val randomIV = AES.generateRandomIV()
                        val encodedFile =
                            AES.encryptFile(
                                context = requireContext(),
                                filePath = Uri.parse(path),
                                secretKey = secretKey,
                                fileName = bundle?.getString("name") ?: "",
                                iv = randomIV
                            )
                        progressDialog.showDialog()
                        val iv = Base64.getEncoder().encodeToString(randomIV)
                        encryptionViewModel.uploadImage(encodedFile, date.text.toString(), iv)
                    }
                } catch (e: Exception) {
                    Log.e("E : ", e.toString())
                    Toast.makeText(
                        requireContext(), "Something Bad Happen! Try Again...",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        } else {
            Toast.makeText(requireContext(), "Please Enter Proper Details",
                Toast.LENGTH_LONG).show()
        }
    }

    /* generate key according to algorithm */
    private fun generatePass() {
        if (algorithmSelected.contains("AES")) {
            val keySize = (algorithmSelected.split("-")[1]).toInt()
            val key = AES.secretKeyToString(AES.generateSecretKey(keySize))
            pass.setText(key)
        }
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        algorithmSelected = resources.getStringArray(R.array.algorithms)[p2]
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {}
}