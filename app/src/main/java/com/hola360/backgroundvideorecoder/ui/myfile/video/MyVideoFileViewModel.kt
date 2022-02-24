package com.hola360.backgroundvideorecoder.ui.myfile.video

import android.app.Application
import androidx.lifecycle.*
import com.anggrayudi.storage.file.toRawFile
import com.hola360.backgroundvideorecoder.data.model.LoadDataStatus
import com.hola360.backgroundvideorecoder.data.model.mediafile.MediaFile
import com.hola360.backgroundvideorecoder.data.repository.BackgroundRecordRepository
import com.hola360.backgroundvideorecoder.data.response.DataResponse
import com.hola360.backgroundvideorecoder.ui.myfile.audio.MyAudioFileViewModel
import com.hola360.backgroundvideorecoder.utils.Configurations
import com.hola360.backgroundvideorecoder.utils.SharedPreferenceUtils
import com.hola360.backgroundvideorecoder.utils.Utils
import kotlinx.coroutines.launch
import java.io.File

class MyVideoFileViewModel(val application: Application) : ViewModel() {

    private val repository = BackgroundRecordRepository(application)
    val allFileLiveData = MutableLiveData<DataResponse<MutableList<MediaFile>>>()
    val listMediaFile = mutableListOf<MediaFile>()

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
                            for (i in list) {
                                listMediaFile.add(MediaFile(i))
                            }
                            allFileLiveData.value =
                                DataResponse.DataSuccessResponse(listMediaFile)
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

    private fun MutableList<MediaFile>.sort() {
        if (SharedPreferenceUtils.getInstance(application)?.getSortByDate() == true) {
            if (SharedPreferenceUtils.getInstance(application)?.getSortByASC() == true) {
                this.sortBy {
                    it.file.lastModified()
                }
            } else {
                this.sortByDescending {
                    it.file.lastModified()
                }
            }
        } else if (SharedPreferenceUtils.getInstance(application)?.getSortBySize() == true) {
            if (SharedPreferenceUtils.getInstance(application)?.getSortByASC() == true) {
                this.sortBy {
                    it.file.length()
                }
            } else {
                this.sortByDescending {
                    it.file.length()
                }
            }
        } else {
            if (SharedPreferenceUtils.getInstance(application)?.getSortByASC() == true) {
                this.sortBy {
                    it.file.name
                }
            } else {
                this.sortByDescending {
                    it.file.name
                }
            }
        }
    }

    fun applyNewSort() {
        if (listMediaFile.isNotEmpty()) {
            listMediaFile.sort()
            allFileLiveData.value = DataResponse.DataSuccessResponse(listMediaFile)
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