package com.hola360.backgroundvideorecoder.ui.record.audio.audiorecord

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hola360.backgroundvideorecoder.data.model.audio.AudioMode
import com.hola360.backgroundvideorecoder.data.model.audio.AudioModel
import com.hola360.backgroundvideorecoder.data.model.audio.AudioQuality
import kotlinx.coroutines.launch

class RecordAudioViewModel(val application: Application) : ViewModel() {

    var audioModel: AudioModel? = null
    val recordAudioLiveData = MutableLiveData<AudioModel>()

    init {

    }

    fun updateQuality(audioQuality: AudioQuality) {
        viewModelScope.launch {
            audioModel?.quality = audioQuality
            recordAudioLiveData.value = audioModel
        }
    }

    fun updateMode(audioMode: AudioMode) {
        viewModelScope.launch {
            audioModel?.mode = audioMode
            recordAudioLiveData.value = audioModel
        }
    }

    fun updateDuration(duration: Long) {
        viewModelScope.launch {
            audioModel?.duration = duration
            recordAudioLiveData.value = audioModel
        }
    }

    fun updateMuted() {
        viewModelScope.launch {
            audioModel?.isMuted = !audioModel?.isMuted!!
            recordAudioLiveData.value = audioModel
        }
    }

    fun getAudioConfig(){
        viewModelScope.launch {
            audioModel = AudioModel()
            recordAudioLiveData.value = audioModel
        }
    }

    class Factory(private val application: Application) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RecordAudioViewModel::class.java)) {
                return RecordAudioViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}