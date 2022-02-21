package com.hola360.backgroundvideorecoder.ui.myfile.video

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hola360.backgroundvideorecoder.ui.myfile.audio.MyAudioFileViewModel

class MyVideoFileViewModel(val application: Application): ViewModel() {



    init {

    }

    class Factory(private val application: Application) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MyAudioFileViewModel::class.java)) {
                return MyAudioFileViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}