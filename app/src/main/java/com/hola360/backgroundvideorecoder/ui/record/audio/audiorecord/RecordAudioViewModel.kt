package com.hola360.backgroundvideorecoder.ui.record.audio.audiorecord

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.hola360.backgroundvideorecoder.data.model.audio.AudioMode
import com.hola360.backgroundvideorecoder.data.model.audio.AudioModel
import com.hola360.backgroundvideorecoder.data.model.audio.AudioQuality
import com.hola360.backgroundvideorecoder.utils.SharedPreferenceUtils
import kotlinx.coroutines.launch

class RecordAudioViewModel(val application: Application) : ViewModel() {

    private var audioModel: AudioModel? = null
    val recordAudioLiveData = MutableLiveData<AudioModel>()
    private val dataSharedPreferenceUtil = SharedPreferenceUtils.getInstance(application)

    init {

    }

    fun updateQuality(audioQuality: AudioQuality) {
        viewModelScope.launch {
            audioModel?.quality = audioQuality
            dataSharedPreferenceUtil!!.setAudioConfig(Gson().toJson(audioModel))
            recordAudioLiveData.value = audioModel!!
        }
    }

    fun updateMode(audioMode: AudioMode) {
        viewModelScope.launch {
            audioModel?.mode = audioMode
            dataSharedPreferenceUtil!!.setAudioConfig(Gson().toJson(audioModel))
            recordAudioLiveData.value = audioModel!!
        }
    }

    fun updateDuration(duration: Long) {
        viewModelScope.launch {
            audioModel?.duration = duration
            dataSharedPreferenceUtil!!.setAudioConfig(Gson().toJson(audioModel))
            recordAudioLiveData.value = audioModel!!
        }
    }

    fun updateMuted() {
        viewModelScope.launch {
            audioModel?.isMuted = !audioModel?.isMuted!!
            dataSharedPreferenceUtil!!.setAudioConfig(Gson().toJson(audioModel))
            recordAudioLiveData.value = audioModel!!
        }
    }

    fun getAudioConfig() {
        viewModelScope.launch {
            audioModel = if (!dataSharedPreferenceUtil!!.getAudioConfig().isNullOrEmpty()) {
                Gson().fromJson(dataSharedPreferenceUtil.getAudioConfig(), AudioModel::class.java)
            } else {
                AudioModel()
            }
            recordAudioLiveData.value = audioModel!!
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