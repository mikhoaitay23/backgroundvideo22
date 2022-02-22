package com.hola360.backgroundvideorecoder.ui.myfile.video

import android.app.Application
import androidx.lifecycle.*
import com.anggrayudi.storage.file.toRawFile
import com.hola360.backgroundvideorecoder.data.model.LoadDataStatus
import com.hola360.backgroundvideorecoder.data.repository.BackgroundRecordRepository
import com.hola360.backgroundvideorecoder.data.response.DataResponse
import com.hola360.backgroundvideorecoder.ui.myfile.audio.MyAudioFileViewModel
import com.hola360.backgroundvideorecoder.utils.Configurations
import com.hola360.backgroundvideorecoder.utils.SharedPreferenceUtils
import com.hola360.backgroundvideorecoder.utils.Utils
import kotlinx.coroutines.launch
import java.io.File

class MyVideoFileViewModel(val application: Application): ViewModel() {

    private val repository = BackgroundRecordRepository(application)
    val allFileLiveData = MutableLiveData<DataResponse<MutableList<File>>>()

    init {
        allFileLiveData.value = DataResponse.DataEmptyResponse()
    }

    val isEmpty: LiveData<Boolean> = Transformations.map(allFileLiveData) {
        allFileLiveData.value!!.loadDataStatus == LoadDataStatus.ERROR
    }

    fun fetch() {
        if (allFileLiveData.value!!.loadDataStatus != LoadDataStatus.LOADING) {
            allFileLiveData.value = DataResponse.DataLoadingResponse()
            viewModelScope.launch {
                val curPath =
                    SharedPreferenceUtils.getInstance(application)?.getParentPath().plus("/")
                        .plus(Configurations.RECORD_PATH).plus("/")
                        .plus(Configurations.RECORD_VIDEO_PATH)
                if (curPath != null) {
                    val documentFile = Utils.getDocumentFile(application, curPath)
                    if (documentFile != null) {
                        val file = documentFile.toRawFile(application)
                        val list = repository.getAllFileOnly(file!!)
                        if (!list.isNullOrEmpty()) {
                            allFileLiveData.value =
                                DataResponse.DataSuccessResponse(list.toMutableList())
                        } else {
                            allFileLiveData.value = DataResponse.DataErrorResponse()
                        }
                    } else {
                        allFileLiveData.value = DataResponse.DataErrorResponse()
                    }
                }
            }
        }
    }

    class Factory(private val application: Application) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MyVideoFileViewModel::class.java)) {
                return MyVideoFileViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}