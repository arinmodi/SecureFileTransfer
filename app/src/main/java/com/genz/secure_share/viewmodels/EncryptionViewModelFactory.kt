package com.genz.secure_share.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.genz.secure_share.repostory.FileRepository

class EncryptionViewModelFactory(private val fileRepository: FileRepository) :  ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>) : T {
        return EncryptionViewModel(fileRepository) as T
    }
}