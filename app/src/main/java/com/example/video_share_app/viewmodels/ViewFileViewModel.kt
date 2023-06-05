package com.example.video_share_app.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.video_share_app.model.FileResponse
import com.example.video_share_app.repostory.ViewRepository
import kotlinx.coroutines.launch

class ViewFileViewModel(application: Application) : AndroidViewModel(application) {
    private var viewRepository : ViewRepository = ViewRepository()

    val fileLive : LiveData<FileResponse?>
    get() = viewRepository.file

     fun getFile(searchKey : String) {
        viewModelScope.launch {
            viewRepository.getFile(searchKey)
        }
    }
}