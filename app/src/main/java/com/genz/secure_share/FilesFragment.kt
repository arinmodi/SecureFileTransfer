package com.genz.secure_share

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
import com.genz.secure_share.adapters.FileClickListener
import com.genz.secure_share.adapters.FileAdapters
import com.genz.secure_share.room.File
import com.genz.secure_share.utils.ProgressDialog
import com.genz.secure_share.viewmodels.EncryptionViewModel

/**
 * List Of Files, User Can View, Delete and Copy Also
 * Kind of History
 */
class FilesFragment : Fragment(), FileClickListener {

    /**
     * Views on the screen
     */
    private lateinit var listView: RecyclerView
    private lateinit var noData: LinearLayout

    /**
     * No Internet Dialog
     */
    private lateinit var noInternet: NoInternet

    /**
     * Progress Dialogs
     */
    private lateinit var progressDialog: ProgressDialog

    /**
     * Encryption view Model for observing files available in
     * room(SQLite DB) and perform operations
     */
    private lateinit var fileViewModel: EncryptionViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_files, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fileAdapters = FileAdapters(requireContext(), this)

        fileViewModel = EncryptionViewModel(requireActivity().application)

        noInternet = NoInternet(requireActivity())
        progressDialog = ProgressDialog(requireActivity())

        listView = view.findViewById(R.id.filesList)
        noData = view.findViewById(R.id.no_data)
        listView.layoutManager = LinearLayoutManager(context)

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
            // observer delete event
            fileViewModel.storeEvent.observe(viewLifecycleOwner) {
                when (it) {
                    is EncryptionViewModel.MainEvent.Success -> {
                        progressDialog.dismiss()
                        Toast.makeText(context, "File Deleted!", Toast.LENGTH_LONG).show()
                    }

                    is EncryptionViewModel.MainEvent.Failure -> {
                        progressDialog.dismiss()
                        Toast.makeText(
                            context, "Something Bad HappenTry Again",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    else -> {
                        Log.i("Status : ", "Loading")
                    }
                }
            }

            // observe network state chnage
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
     * Invokes when user clicks delete button
     */
    override fun onDeleteClick(file: File) {
        progressDialog.showDialog()
        progressDialog.setMessage("Deleting a file...")
        fileViewModel.deleteFile(file.searchKey, file)
    }

    /**
     * Invokes when user clicks copy button
     */
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