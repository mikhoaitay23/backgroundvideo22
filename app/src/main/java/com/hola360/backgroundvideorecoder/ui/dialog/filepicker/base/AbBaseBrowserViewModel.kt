package com.hola360.backgroundvideorecoder.ui.dialog.filepicker.base

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.Parcelable
import androidx.lifecycle.*
import com.anggrayudi.storage.file.toRawFile
import com.example.techtallfeb2022.data.model.StorageBrowserModel
import com.hola360.backgroundvideorecoder.ui.dialog.filepicker.data.repository.InternalRepository
import com.hola360.backgroundvideorecoder.ui.dialog.filepicker.data.response.DataResponse
import com.zippro.filemanager.data.response.LoadingStatus
import com.hola360.backgroundvideorecoder.ui.dialog.filepicker.model.ActionModel
import com.hola360.backgroundvideorecoder.app.App
import com.hola360.backgroundvideorecoder.ui.dialog.filepicker.utils.FilePickerUtils
import kotlinx.coroutines.launch
import java.io.File

open class AbBaseBrowserViewModel(
    val app: App
) : AndroidViewModel(app) {
    val allFileLiveData = MutableLiveData<DataResponse<MutableList<File>>>()
    var storageBrowserModel: StorageBrowserModel? = null
    val storageBrowserModelLiveData = MutableLiveData<StorageBrowserModel>()
    val isStackNullLiveData = MutableLiveData(false)
    var files = mutableListOf<File>()
    private val repository = InternalRepository(app)
    private val stateMaps = HashMap<String, Parcelable?>()
    var curState: Parcelable? = null
    val curStateLiveData = MutableLiveData<Parcelable?>()
    val actionModelLiveData = MutableLiveData<MutableList<ActionModel>>()
    private val actionModels = mutableListOf<ActionModel>()

    init {
        allFileLiveData.value = DataResponse.DataIdle()
    }

    private val handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            curStateLiveData.value = curState
        }
    }

    val isEmpty: LiveData<Boolean> = Transformations.map(allFileLiveData) {
        allFileLiveData.value!!.loadingStatus == LoadingStatus.Error
    }

    fun updateCurPosY() {
        handler.sendEmptyMessageDelayed(0, UPDATE_STATE_DELAY)
    }

    fun initStorage() {
        viewModelScope.launch {
            val storageList = repository.getAllStorages()
             storageBrowserModel = StorageBrowserModel(storageList, 0)
            storageBrowserModelLiveData.value = storageBrowserModel
        }

    }

    private fun clearActionModel() {
        actionModels.clear()
        actionModelLiveData.value = actionModels
    }

    fun changeStorage(newStorage: Int) {
        if (newStorage != storageBrowserModel!!.curStorage) {
            storageBrowserModel!!.curStorage = newStorage
            storageBrowserModel!!.curStack.clear()
            curState = null
            stateMaps.clear()
            actionModels.clear()
            clearActionModel()
            storageBrowserModelLiveData.value = storageBrowserModel
        }
    }

    fun pushStack(sub: String, newState: Parcelable?) {
        stateMaps[storageBrowserModel!!.fullPath(app)] = newState
        storageBrowserModel!!.curStack.add(sub)
        curState = null
        storageBrowserModelLiveData.value = storageBrowserModel
    }

    fun popToPosition(position: Int) {
        if (position < storageBrowserModel!!.curStack.size) {
            if (position == 0) {
                storageBrowserModel!!.curStack.clear()
                curState = stateMaps[storageBrowserModel!!.fullPath(app)]
                stateMaps.clear()
            } else {
                val size = storageBrowserModel!!.curStack.size
                for (i in size - 1 downTo position) {
                    storageBrowserModel!!.curStack.removeAt(i)
                    stateMaps.remove(storageBrowserModel!!.fullPath(app))
                }
                curState = stateMaps[storageBrowserModel!!.fullPath(app)]
            }
            storageBrowserModelLiveData.value = storageBrowserModel
        }
    }

    fun popStack() {
        if (storageBrowserModel!!.curStack.isEmpty()) {
            isStackNullLiveData.value = true
        } else {

                stateMaps.remove(storageBrowserModel!!.fullPath(app))
            storageBrowserModel!!.curStack.removeLast()
                curState = stateMaps[storageBrowserModel!!.fullPath(app)]
                storageBrowserModelLiveData.value = storageBrowserModel


        }
    }

    fun fetch(oldState: Parcelable?) {
        if (FilePickerUtils.storagePermissionGrant(app.applicationContext)) {
            if (allFileLiveData.value!!.loadingStatus != LoadingStatus.Loading) {
                if (oldState != null) {
                    curState = oldState
                }
                allFileLiveData.value = DataResponse.DataLoading()
                viewModelScope.launch {
                    val curDoc = storageBrowserModel!!.getCurDocumentationFile(app)
                    if (curDoc != null) {
                        val file = curDoc.toRawFile(app)
                        val list = repository.getInternalFiles(file!!)
                        files.clear()
                        if (!list.isNullOrEmpty()) {
                            allFileLiveData.value = DataResponse.DataSuccess(list.toMutableList())
                        } else {
                            allFileLiveData.value = DataResponse.DataError()
                        }
                    } else {
                        allFileLiveData.value = DataResponse.DataError()
                    }


                }
            }
        } else {
            allFileLiveData.value = DataResponse.DataError()
        }
    }

    companion object {
        const val UPDATE_STATE_DELAY = 50L
    }

}