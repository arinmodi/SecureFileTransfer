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
import com.example.video_share_app.algorithms.AES
import java.text.SimpleDateFormat
import java.util.Locale
import javax.crypto.spec.SecretKeySpec

class EncryptionDetails : Fragment() {

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

    // date Picker
    private lateinit var datePickerDialog : DatePickerDialog

    // generate pass view
    private lateinit var generate : CardView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

        // pass view related
        pass = view.findViewById(R.id.pass)

        // expiry/date picker related
        datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, monthOfYear, dayOfMonth ->
                calendar.set(year, monthOfYear, dayOfMonth)

                // Display the selected date
                val formattedDate = SimpleDateFormat("dd/MM/yyyy",
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
            try {
                val path = bundle?.getString("path")
                if (path != null) {
                    val secretKey = AES.stringToSecretKey(pass.text.toString())
                    val encodedFile =
                        AES.encryptFile(
                            context=requireContext(),
                            filePath = Uri.parse(path),
                            secretKey = secretKey,
                            fileName = bundle?.getString("name")?:""
                        )
                }
            } catch (e : Exception) {
                Log.e("E : ", e.toString())
                Toast.makeText(requireContext(), "Something Bad Happen! Try Again...",
                    Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(requireContext(), "Please Enter Proper Details",
                Toast.LENGTH_LONG).show()
        }
    }

    /* generate key according to algorithm */
    private fun generatePass() {
        val key = AES.secretKeyToString(AES.generateSecretKey(128))
        pass.setText(key)
    }
}