package com.hola360.backgroundvideorecoder.ui.record.video

import android.app.Application
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hola360.backgroundvideorecoder.ui.record.video.model.CameraCapability
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class RecordVideoViewModel(val application: Application): ViewModel() {

    private val cameraCapabilities= MutableLiveData<MutableList<CameraCapability>>()

    init {

    }

}