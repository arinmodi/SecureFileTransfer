package com.example.video_share_app

import android.R.attr.label
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.video_share_app.adapters.CopyClickListener
import com.example.video_share_app.adapters.DeleteClickListener
import com.example.video_share_app.adapters.FileAdapters
import com.example.video_share_app.room.File
import com.example.video_share_app.utils.ProgressDialog
import com.example.video_share_app.viewmodels.EncryptionViewModel


class FilesFragment : Fragment(), DeleteClickListener, CopyClickListener {

    // Recycler View
    private lateinit var listView : RecyclerView

    // no data found
    private lateinit var noData : LinearLayout

    private lateinit var fileViewModel : EncryptionViewModel

    // progress dialog
    private lateinit var progressDialog: ProgressDialog

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fileViewModel = EncryptionViewModel(requireActivity().application)

        progressDialog = ProgressDialog(requireActivity())

        listView = view.findViewById(R.id.filesList)
        noData = view.findViewById(R.id.no_data)

        listView.layoutManager = LinearLayoutManager(context)

        val fileAdapters = FileAdapters(requireContext(), this, this)
        listView.adapter = fileAdapters

        fileViewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
        )[EncryptionViewModel::class.java]

        fileViewModel.allFiles.observe(viewLifecycleOwner) { list ->
            list?.let {
                if (it.isEmpty()) {
                    listView.visibility = View.GONE
                    noData.visibility = View.VISIBLE
                } else {
                    listView.visibility = View.VISIBLE
                    noData.visibility = View.GONE
                    fileAdapters.updateList(it)
                }
            }
        }

        lifecycleScope.launchWhenCreated {
            fileViewModel.storeEvent.observe(viewLifecycleOwner) {
                when(it) {
                    is EncryptionViewModel.MainEvent.Success -> {
                        progressDialog.dismiss()
                        Toast.makeText(context, "File Deleted!", Toast.LENGTH_LONG).show()
                    }

                    is EncryptionViewModel.MainEvent.Failure -> {
                        progressDialog.dismiss()
                        Toast.makeText(context, "Something Bad HappenTry Again",
                            Toast.LENGTH_LONG).show()
                    }

                    else -> {
                        Log.i("Status : ", "Loading")
                    }

                }
            }
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_files, container, false)
    }

    private fun deleteFile(file : File) {
        progressDialog.showDialog()
        progressDialog.setMessage("Deleting a file...")
        fileViewModel.deleteFile(file.searchKey, file)
    }

    override fun onDeleteClick(file: File) {
        deleteFile(file)
    }

    override fun onCopyClick(file: File) {
        var data = ""
        data += "File : ${file.name}\n"
        data += "Algorithm : ${file.algo}\n"
        data += "Expiry : ${file.expiry}\n"
        data += "Pass : ${file.pass}\n"
        data += "Search Key : ${file.searchKey}\n"

        val clipBoard = context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("data", data)
        clipBoard.setPrimaryClip(clip)

        Toast.makeText(context, "Details Copied", Toast.LENGTH_LONG).show()
    }
}