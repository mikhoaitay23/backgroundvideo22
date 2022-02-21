package com.hola360.backgroundvideorecoder.ui.setting

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.provider.Settings
import android.view.View
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.hola360.backgroundvideorecoder.MainActivity
import com.hola360.backgroundvideorecoder.NavMainGraphDirections
import com.hola360.backgroundvideorecoder.R
import com.hola360.backgroundvideorecoder.databinding.LayoutSettingGeneralBinding
import com.hola360.backgroundvideorecoder.ui.dialog.OnDialogDismiss
import com.hola360.backgroundvideorecoder.ui.dialog.filepicker.pickfolder.PickFolderDialog
import com.hola360.backgroundvideorecoder.ui.dialog.filepicker.utils.FilePickerUtils
import com.hola360.backgroundvideorecoder.ui.dialog.listdialog.ListSelectionAdapter
import com.hola360.backgroundvideorecoder.ui.dialog.listdialog.ListSelectionBotDialog
import com.hola360.backgroundvideorecoder.ui.record.BaseRecordPageFragment
import com.hola360.backgroundvideorecoder.ui.setting.applock.AppLockFragment
import com.hola360.backgroundvideorecoder.ui.setting.model.SettingGeneralModel
import com.hola360.backgroundvideorecoder.utils.SystemUtils
import com.hola360.backgroundvideorecoder.utils.Utils

class GeneralSetting: BaseRecordPageFragment<LayoutSettingGeneralBinding>(), View.OnClickListener {

    override val layoutId: Int
        get() = R.layout.layout_setting_general
    private val generalSetting: SettingGeneralModel by lazy {
        Utils.getDataPrefGeneralSetting(dataPref!!)
    }
    private val parentPath: String by lazy {
        dataPref!!.getParentPath()!!
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
    private var passcode:String= ""

    override fun initView() {
        binding!!.setting= generalSetting
        binding!!.path= parentPath
        binding!!.freeStorage.setOnClickListener(this)
        binding!!.batteryLevel.setOnClickListener(this)
        binding!!.appLock.setOnClickListener(this)
        binding!!.storagePath.setOnClickListener(this)
        binding!!.notificationLevel.setOnClickListener(this)
    }

    override fun initViewModel() {
    }

    override fun onResume() {
        super.onResume()
        binding!!.appLockSwitch.isChecked= hasPasscode()
    }

    private fun hasPasscode():Boolean{
        passcode= dataPref!!.getPasscode() ?: ""
        return passcode != ""
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
                if(binding!!.appLockSwitch.isChecked){
                    findNavController().navigate(NavMainGraphDirections.actionToNavAppLock(AppLockFragment.DELETE_MODE))
                }else{
                    findNavController().navigate(NavMainGraphDirections.actionToNavAppLock(AppLockFragment.CREATE_MODE))
                }
//                binding!!.appLockSwitch.isChecked= !binding!!.appLockSwitch.isChecked
//                generalSetting.appLock= !generalSetting.appLock
//                updateDataAndUI()
            }
            R.id.storagePath->{
                if (FilePickerUtils.storagePermissionGrant(requireContext())) {
                    startPickFolder()
                } else {
                    requestPermission()
                }
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

    private fun updateDataAndUI(){
        dataPref!!.putGeneralSetting(Gson().toJson(generalSetting))
        binding!!.setting= generalSetting
    }

    private fun startPickFolder() {
        val pickFolderDialog = PickFolderDialog.create()
        pickFolderDialog.mOnPickPathResultListener= object :PickFolderDialog.OnPickPathResultListener{
            override fun onPickPathResult(path: String?, storageId:String) {
                generalSetting.storageId= storageId
                binding!!.path= path!!
                dataPref!!.setParentPath(path)
                updateDataAndUI()
            }
        }
        pickFolderDialog.show(requireActivity().supportFragmentManager, "PickFolder")
    }

    private val customContract = object : ActivityResultContract<String, Boolean>() {
        override fun createIntent(context: Context, input: String): Intent {
            val intent = Intent(input)
            intent.addCategory("android.intent.category.DEFAULT")
            intent.data = Uri.parse(
                String.format(
                    "package:%s",
                    requireActivity().applicationContext.packageName
                )
            )
            return intent
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Boolean =
            if (SystemUtils.isAndroidR()) {
                Environment.isExternalStorageManager()
            } else {
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            }

    }

    private val activityResultLauncher = registerForActivityResult(customContract) {
        if (it) {
            setupWhenPermissionGranted()
        } else {
            SystemUtils.showAlertPermissionNotGrant(binding!!, requireActivity())
        }
    }

    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            if (FilePickerUtils.storagePermissionGrant(requireContext())
            ) {
                setupWhenPermissionGranted()
            } else {
                SystemUtils.showAlertPermissionNotGrant(binding!!, requireActivity())
            }
        }

    private fun setupWhenPermissionGranted(){}

    private fun requestPermission() {
        if (SystemUtils.isAndroidR()) {
            activityResultLauncher.launch(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
        } else resultLauncher.launch(
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        )
    }


}