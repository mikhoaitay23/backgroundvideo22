package com.hola360.backgroundvideorecoder.ui.myfile.audio

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hola360.backgroundvideorecoder.data.model.LoadDataStatus
import com.hola360.backgroundvideorecoder.data.model.media.AudioFile
import com.hola360.backgroundvideorecoder.data.repository.BackgroundRecordRepository
import com.hola360.backgroundvideorecoder.data.response.DataResponse
import com.hola360.backgroundvideorecoder.utils.SharedPreferenceUtils
import kotlinx.coroutines.launch
import java.io.File

class MyAudioFileViewModel(val application: Application) : ViewModel() {

    private val repository = BackgroundRecordRepository(application)
    private val audioFiles = mutableListOf<AudioFile>()
    val mediaFileLiveData = MutableLiveData<DataResponse<MutableList<AudioFile>>>()

    init {
        mediaFileLiveData.value = DataResponse.DataEmptyResponse()
    }

    fun loadAudios() {
        if (mediaFileLiveData.value!!.loadDataStatus != LoadDataStatus.LOADING) {
            mediaFileLiveData.value = DataResponse.DataLoadingResponse()
            viewModelScope.launch {
                val file = File(
                    SharedPreferenceUtils.getInstance(
                        application
                    )?.getParentPath()!!
                )
                val uri = Uri.fromFile(file)
                audioFiles.clear()
                audioFiles.add(AudioFile(null, null, null, 0, 0))
                audioFiles.addAll(repository.getAllAudio())
                if (audioFiles.isNotEmpty()) {
                    mediaFileLiveData.value = DataResponse.DataSuccessResponse(audioFiles)
                } else {
                    mediaFileLiveData.value = DataResponse.DataErrorResponse()
                }
            }
        }

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