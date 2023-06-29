package com.genz.secure_share.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.genz.secure_share.repostory.ViewRepository

class ViewFileViewModelFactory(private val viewRepository: ViewRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>) : T {
        return ViewFileViewModel(viewRepository) as T
    }
}