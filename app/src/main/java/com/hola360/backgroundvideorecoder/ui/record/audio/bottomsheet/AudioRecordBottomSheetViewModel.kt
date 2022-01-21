package com.hola360.backgroundvideorecoder.ui.record.audio.bottomsheet

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class AudioRecordBottomSheetViewModel(val application: Application) : ViewModel() {

    init {

    }

    class Factory(private val application: Application) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AudioRecordBottomSheetViewModel::class.java)) {
                return AudioRecordBottomSheetViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}