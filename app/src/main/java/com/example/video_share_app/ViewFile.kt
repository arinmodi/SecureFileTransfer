package com.example.video_share_app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.video_share_app.model.FileResponse
import com.example.video_share_app.utils.ProgressDialog
import com.example.video_share_app.viewmodels.ViewFileViewModel

class ViewFile : Fragment() {

    // search key edit text
    private lateinit var searchInput : EditText

    // search button
    private lateinit var searchButton : CardView

    // message, search result
    private lateinit var message : TextView

    // view file button
    private lateinit var viewFile : CardView

    private lateinit var viewFileViewModel : ViewFileViewModel

    private var fr : FileResponse? = null

    private lateinit var progressDialog: ProgressDialog

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchInput = view.findViewById(R.id.searchKey)
        searchButton = view.findViewById(R.id.search)
        message = view.findViewById(R.id.message)
        viewFile = view.findViewById(R.id.viewFileButton)

        message.text = getString(R.string.no_files_found)
        viewFile.visibility = View.INVISIBLE

        progressDialog = ProgressDialog(requireActivity())

        viewFileViewModel = ViewFileViewModel(requireActivity().application)

        searchButton.setOnClickListener {
            Log.e("Button Pressed", "true")
            if (searchInput.text.length == 20) {
                progressDialog.showDialog()
                progressDialog.setMessage("file is searching...")
                viewFileViewModel.getFile(searchKey = searchInput.text.toString())
            } else {
                Toast.makeText(context, "Invalid Key", Toast.LENGTH_LONG).show()
            }
        }

        lifecycleScope.launchWhenCreated {
            viewFileViewModel.fileLive.observe(viewLifecycleOwner) {
                if (it == null) {
                    fr = null
                    message.text = getString(R.string.no_files_found)
                    viewFile.visibility = View.INVISIBLE
                    progressDialog.dismiss()
                } else {
                    progressDialog.dismiss()
                    fr = it
                    val msg = it.name + " found"
                    message.text = msg
                    viewFile.visibility = View.VISIBLE
                }
            }
        }

        viewFile.setOnClickListener {
            onViewFile()
        }
    }

    private fun onViewFile() {
        if (fr != null) {
            val intent = Intent(context, DecryptionDetails::class.java)
            intent.putExtra("name", fr!!.name)
            intent.putExtra("url", fr!!.url)
            intent.putExtra("iv", fr!!.iv)
            startActivity(intent)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_view_file, container, false)
    }
}