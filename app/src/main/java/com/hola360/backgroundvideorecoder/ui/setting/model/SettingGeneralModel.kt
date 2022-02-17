package com.hola360.backgroundvideorecoder.ui.setting.model

import com.anggrayudi.storage.file.StorageId


class SettingGeneralModel {
    var storageId:String= StorageId.PRIMARY
    var checkStorage:Boolean=false
    var checkBattery:Boolean= false
    var appLock:Boolean= false
    var notificationImportance:Int= 0
}