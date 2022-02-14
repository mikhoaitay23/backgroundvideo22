package com.hola360.backgroundvideorecoder.ui.setting

import android.app.NotificationManager
import android.os.Build
import android.view.View
import com.google.gson.Gson
import com.hola360.backgroundvideorecoder.MainActivity
import com.hola360.backgroundvideorecoder.R
import com.hola360.backgroundvideorecoder.databinding.LayoutSettingGeneralBinding
import com.hola360.backgroundvideorecoder.ui.dialog.OnDialogDismiss
import com.hola360.backgroundvideorecoder.ui.dialog.listdialog.ListSelectionAdapter
import com.hola360.backgroundvideorecoder.ui.dialog.listdialog.ListSelectionBotDialog
import com.hola360.backgroundvideorecoder.ui.record.BaseRecordPageFragment
import com.hola360.backgroundvideorecoder.ui.record.video.base.BaseRecordVideoFragment
import com.hola360.backgroundvideorecoder.ui.setting.model.SettingGeneralModel

class GeneralSetting: BaseRecordPageFragment<LayoutSettingGeneralBinding>(), View.OnClickListener {

    override val layoutId: Int
        get() = R.layout.layout_setting_general
    private val generalSetting: SettingGeneralModel by lazy {
        getDataPrefGeneralSetting()
    }
    private val notificationImportanceDialog: ListSelectionBotDialog by lazy {
        val title = resources.getString(R.string.setting_general_notification_title)
        val itemList = resources.getStringArray(R.array.notification_importance).toMutableList()
        ListSelectionBotDialog(title, itemList, object : ListSelectionAdapter.OnItemListSelection {
            override fun onSelection(position: Int) {
                generalSetting.notificationImportance= position
                updateDataAndUI()
                notificationImportanceDialog.dialog?.dismiss()
            }
        }, dismissCallback)
    }
    private var showDialog= false
    private val dismissCallback= object :OnDialogDismiss{
        override fun onDismiss() {
            showDialog= false
        }
    }

    override fun initView() {
        binding!!.setting= generalSetting
        binding!!.freeStorage.setOnClickListener(this)
        binding!!.batteryLevel.setOnClickListener(this)
        binding!!.appLock.setOnClickListener(this)
        binding!!.storagePath.setOnClickListener(this)
        binding!!.notificationLevel.setOnClickListener(this)
        binding!!.storageSwitch.isEnabled=false
        binding!!.batterySwitch.isEnabled=false
        binding!!.appLockSwitch.isEnabled= false
    }

    override fun initViewModel() {

    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.freeStorage->{
                binding!!.storageSwitch.isChecked= !binding!!.storageSwitch.isChecked
                generalSetting.checkStorage= !generalSetting.checkStorage
                updateDataAndUI()
            }
            R.id.batteryLevel->{
                binding!!.batterySwitch.isChecked= !binding!!.batterySwitch.isChecked
                generalSetting.checkBattery= !generalSetting.checkBattery
                updateDataAndUI()
            }
            R.id.appLock->{
                binding!!.appLockSwitch.isChecked= !binding!!.appLockSwitch.isChecked
                generalSetting.appLock= !generalSetting.appLock
                updateDataAndUI()
            }
            R.id.storagePath->{

            }
            R.id.notificationLevel->{
                if(!showDialog){
                    showDialog=true
                    notificationImportanceDialog.setSelectionPos(generalSetting.notificationImportance)
                    notificationImportanceDialog.show((requireActivity() as MainActivity).supportFragmentManager, "Notification_level")
                }
            }
        }
    }

    private fun getDataPrefGeneralSetting():SettingGeneralModel{
        val value= dataPref!!.getGeneralSetting()
        value?.let {
             return if("" == it){
                 SettingGeneralModel()
            }else{
                Gson().fromJson(it, SettingGeneralModel::class.java)
            }
        }
        return SettingGeneralModel()
    }

    private fun updateDataAndUI(){
        dataPref!!.putGeneralSetting(Gson().toJson(generalSetting))
        binding!!.setting= generalSetting
    }
}