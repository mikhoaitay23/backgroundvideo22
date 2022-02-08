package com.hola360.backgroundvideorecoder.ui.record.audio.audioschedule

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hola360.backgroundvideorecoder.data.model.audio.AudioMode
import com.hola360.backgroundvideorecoder.data.model.audio.AudioModel
import com.hola360.backgroundvideorecoder.data.model.audio.AudioQuality
import com.hola360.backgroundvideorecoder.ui.record.RecordSchedule
import kotlinx.coroutines.launch
import java.util.*

class ScheduleAudioViewModel(val application: Application) : ViewModel() {

    var audioModel: AudioModel? = null
    var recordSchedule: RecordSchedule? = null
    val recordAudioLiveData = MutableLiveData<AudioModel>()
    val recordScheduleLiveData = MutableLiveData<RecordSchedule>()
    val isRecordScheduleLiveData = MutableLiveData<Boolean>()
    val saveRecordScheduleLiveData = MutableLiveData<ValidateType>()

    init {
        isRecordScheduleLiveData.value = false
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

    fun updateDate(date: Long) {
        viewModelScope.launch {
            if (recordSchedule == null) {
                recordSchedule = RecordSchedule()
            }
            recordSchedule?.scheduleTime = date
            recordScheduleLiveData.value = recordSchedule
        }
    }

    fun updateTime(time: Long) {
        viewModelScope.launch {
            if (recordSchedule == null) {
                recordSchedule = RecordSchedule()
            }
            recordSchedule?.scheduleTime = time
            recordScheduleLiveData.value = recordSchedule
        }
    }

    fun getAudioConfig() {
        viewModelScope.launch {
            audioModel = AudioModel()
            recordAudioLiveData.value = audioModel
        }
    }

    fun setSchedule() {
        viewModelScope.launch {
            if (recordSchedule == null) {
                recordSchedule = RecordSchedule()
                recordSchedule!!.scheduleTime = Calendar.getInstance().timeInMillis
                recordSchedule!!.isVideo = false
            }
            val validateType = validateSchedule()
            if (validateType == ValidateType.ValidateDone) {
                isRecordScheduleLiveData.value = true
            }
            saveRecordScheduleLiveData.value = validateType
        }
    }

    fun cancelSchedule() {
        viewModelScope.launch {
            isRecordScheduleLiveData.value = false
        }
    }

    private fun validateSchedule(): ValidateType {
        return if (Calendar.getInstance().timeInMillis == recordSchedule!!.scheduleTime) {
            ValidateType.InvalidTime
        } else {
            ValidateType.ValidateDone
        }
    }

    enum class ValidateType {
        ValidateDone, InvalidTime
    }

    class Factory(private val application: Application) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ScheduleAudioViewModel::class.java)) {
                return ScheduleAudioViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}