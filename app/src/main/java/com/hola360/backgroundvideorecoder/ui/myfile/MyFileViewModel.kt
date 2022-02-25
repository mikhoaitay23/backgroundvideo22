package com.hola360.backgroundvideorecoder.ui.myfile

import android.app.Application
import androidx.lifecycle.*
import com.anggrayudi.storage.file.toRawFile
import com.hola360.backgroundvideorecoder.data.model.LoadDataStatus
import com.hola360.backgroundvideorecoder.data.model.mediafile.MediaFile
import com.hola360.backgroundvideorecoder.data.repository.BackgroundRecordRepository
import com.hola360.backgroundvideorecoder.data.response.DataResponse
import com.hola360.backgroundvideorecoder.utils.SharedPreferenceUtils
import com.hola360.backgroundvideorecoder.utils.Utils
import kotlinx.coroutines.launch

class MyFileViewModel(val application: Application) : ViewModel() {

    private val repository = BackgroundRecordRepository(application)
    val allFileLiveData = MutableLiveData<DataResponse<MutableList<MediaFile>>>()
    private val listMediaFile = mutableListOf<MediaFile>()
    val filterList= mutableListOf<MediaFile>()

    init {
        allFileLiveData.value = DataResponse.DataEmptyResponse()
    }

    val isEmpty: LiveData<Boolean> = Transformations.map(allFileLiveData) {
        allFileLiveData.value!!.loadDataStatus == LoadDataStatus.ERROR
    }

    fun fetch(curPath:String)= viewModelScope.launch {
        if (allFileLiveData.value!!.loadDataStatus != LoadDataStatus.LOADING) {
            allFileLiveData.value = DataResponse.DataLoadingResponse()
            val documentFile = Utils.getDocumentFile(application, curPath)
            if (documentFile != null) {
                val file = documentFile.toRawFile(application)
                val list = repository.getAllFileOnly(file!!)
                if (!list.isNullOrEmpty()) {
                    listMediaFile.clear()
                    filterList.clear()
                    for (i in list) {
                        listMediaFile.add(MediaFile(i))
                        filterList.add(MediaFile(i))
                    }
                    allFileLiveData.value =
                        DataResponse.DataSuccessResponse(filterList)
                } else {
                    allFileLiveData.value = DataResponse.DataErrorResponse()
                }
            } else {
                allFileLiveData.value = DataResponse.DataErrorResponse()
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
            if (modelClass.isAssignableFrom(MyFileViewModel::class.java)) {
                return MyFileViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}