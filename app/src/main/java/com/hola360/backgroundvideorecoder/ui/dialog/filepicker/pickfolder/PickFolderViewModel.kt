package com.hola360.backgroundvideorecoder.ui.dialog.filepicker.pickfolder

import androidx.lifecycle.*
import com.hola360.backgroundvideorecoder.app.App
import com.hola360.backgroundvideorecoder.ui.dialog.filepicker.model.ActionModel
import com.hola360.backgroundvideorecoder.ui.dialog.filepicker.base.AbBaseBrowserViewModel
import com.hola360.backgroundvideorecoder.ui.dialog.filepicker.utils.FilePickerUtils
import kotlinx.coroutines.launch


class PickFolderViewModel(
    application: App
) : AbBaseBrowserViewModel(application) {
    private val actionModels = mutableListOf<ActionModel>()

    fun clickOnMemory() {
        viewModelScope.launch {
            actionModels.clear()
            storageBrowserModel!!.storageModels.forEachIndexed { index, storageModel ->
                val actionModel =
                    ActionModel(
                        FilePickerUtils.getStorageIcon(storageModel),
                        FilePickerUtils.getStorageName(app, storageModel)
                    )
                actionModel.isSelected = index == storageBrowserModel!!.curStorage
                actionModel.isSelectable = true
                actionModels.add(actionModel)
            }
            actionModelLiveData.value = actionModels
        }
    }

    class Factory(
        private val application: App
    ) :
        ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PickFolderViewModel::class.java)) {
                return PickFolderViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

}