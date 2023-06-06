package com.genz.secure_share.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.genz.secure_share.model.FileResponse
import com.genz.secure_share.repostory.ViewRepository
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