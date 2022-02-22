package com.hola360.backgroundvideorecoder.service

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hola360.backgroundvideorecoder.R
import com.hola360.backgroundvideorecoder.app.App
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.IllegalArgumentException

class FileTransferViewModel(app: App):ViewModel() {

    fun createSDCardFile()= viewModelScope.launch {
        withContext(Dispatchers.IO){

        }
    }

    class Factory(private val application: App) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(FileTransferViewModel::class.java)) {
                return FileTransferViewModel(application) as T
            }
            throw  IllegalArgumentException(application.resources.getString(R.string.unable_create_viewmodel))
        }
    }
}