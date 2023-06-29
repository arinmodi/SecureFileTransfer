package com.genz.secure_share.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.genz.secure_share.model.FileResponse
import com.genz.secure_share.repostory.ViewRepository
import kotlinx.coroutines.launch

open class ViewFileViewModel(private val viewRepository: ViewRepository) : ViewModel() {

    val fileLive : LiveData<FileResponse?>
    get() = viewRepository.file

     fun getFile(searchKey : String) {
        viewModelScope.launch {
            viewRepository.getFile(searchKey)
        }
    }
}