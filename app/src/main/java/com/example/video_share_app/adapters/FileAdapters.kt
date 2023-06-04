package com.example.video_share_app.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.video_share_app.R
import com.example.video_share_app.room.File

interface DeleteClickListener {
    fun onDeleteClick(file : File)
}

interface CopyClickListener {
    fun onCopyClick(file : File)
}

class FileAdapters(
    val context : Context,
    private val deleteClickListener: DeleteClickListener,
    private val copyClickListener: CopyClickListener
) : RecyclerView.Adapter<FileAdapters.ViewHolder>(){

    private val allFiles = ArrayList<File>()

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val fileNameView: TextView = itemView.findViewById(R.id.fileName)
        val searchKeyView: TextView = itemView.findViewById(R.id.searchKey)
        val algorithmView: TextView = itemView.findViewById(R.id.algorithm)
        val expiryView: TextView = itemView.findViewById(R.id.expiry)
        val passView: TextView = itemView.findViewById(R.id.pass)

        val deleteButton : CardView = itemView.findViewById(R.id.delete)
        val copyButton : CardView = itemView.findViewById(R.id.copy)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // inflating our layout file for each item of recycler view.
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.file_item,
            parent, false
        )
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // on below line we are setting data to item of recycler view.
        val fileName = "File : " + allFiles[position].name
        holder.fileNameView.text = fileName
        val searchKey = "Search Key : " + allFiles[position].searchKey
        holder.searchKeyView.text = searchKey
        val date = allFiles[position].expiry.split("-")
        val finalDate = "Expiry : ${date[2]}-${date[1]}-${date[0]}"
        holder.expiryView.text = finalDate
        val pass = "Pass : " + allFiles[position].pass
        holder.passView.text = pass
        val algo = "Algorithm : " + allFiles[position].algo
        holder.algorithmView.text = algo

        holder.deleteButton.setOnClickListener {
            deleteClickListener.onDeleteClick(allFiles[position])
        }

        holder.copyButton.setOnClickListener {
            copyClickListener.onCopyClick(allFiles[position])
        }
    }

    override fun getItemCount(): Int {
        return allFiles.size
    }

    // below method is use to update our list of notes.
    fun updateList(newList: List<File>) {
        allFiles.clear()
        allFiles.addAll(newList)
        notifyDataSetChanged()
    }

}