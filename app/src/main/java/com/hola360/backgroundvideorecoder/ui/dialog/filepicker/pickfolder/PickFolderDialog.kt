package com.hola360.backgroundvideorecoder.ui.dialog.filepicker.pickfolder

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView

import com.zippro.filemanager.data.response.LoadingStatus
import com.hola360.backgroundvideorecoder.ui.dialog.filepicker.popup.ActionAdapter
import com.hola360.backgroundvideorecoder.ui.dialog.filepicker.popup.ListActionPopup

import android.view.KeyEvent
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.documentfile.provider.DocumentFile
import com.anggrayudi.storage.file.getAbsolutePath
import com.anggrayudi.storage.file.inSdCardStorage
import com.hola360.backgroundvideorecoder.ui.dialog.filepicker.data.response.DataResponse
import com.hola360.backgroundvideorecoder.ui.dialog.filepicker.base.BaseDialogFragment
import com.hola360.backgroundvideorecoder.ui.dialog.filepicker.pickfolder.adapter.BrowserPathAdapter
import com.hola360.backgroundvideorecoder.ui.dialog.filepicker.pickfolder.adapter.FileAdapter
import com.hola360.backgroundvideorecoder.R
import com.hola360.backgroundvideorecoder.app.App
import com.hola360.backgroundvideorecoder.databinding.LayoutPickFolderDialogBinding
import com.hola360.backgroundvideorecoder.ui.dialog.filepicker.utils.StorageUtils
import com.hola360.backgroundvideorecoder.ui.dialog.filepicker.utils.FilePickerUtils
import com.hola360.backgroundvideorecoder.utils.SharedPreferenceUtils
import java.io.File


class PickFolderDialog : BaseDialogFragment<LayoutPickFolderDialogBinding>(),
    BrowserPathAdapter.OnPathClickListener, ActionAdapter.OnActionClickListener {
    private lateinit var mViewModel: PickFolderViewModel
    private var mAdapter: FileAdapter? = null
    private val browserPathAdapter = BrowserPathAdapter()
    private val listActionPopup by lazy { ListActionPopup(mainActivity) }
    private var smoothScroller: RecyclerView.SmoothScroller? = null
    var mOnPickPathResultListener: OnPickPathResultListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAdapter = FileAdapter(object : FileAdapter.ListenerClickItem {
            override fun onClickFileItem(position: Int, cFile: File) {
                itemClick(position, cFile)
            }

        })

        browserPathAdapter.onPathClickListener = this

    }

    private fun itemClick(position: Int, cFile: File) {
        if (cFile.isDirectory) {
            mViewModel.pushStack(
                cFile.name,
                binding!!.recycleView.layoutManager!!.onSaveInstanceState()
            )
        }
    }

    override fun getLayout(): Int {
        return R.layout.layout_pick_folder_dialog
    }

    override fun initView() {
        binding!!.recycleView.apply {
            setHasFixedSize(true)
            adapter = mAdapter
        }
        binding!!.recycleViewPath.apply {
            setHasFixedSize(true)
            adapter = browserPathAdapter
        }
        mDialog!!.setOnKeyListener { dialog, keyCode, event ->
            if (event.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                if (listActionPopup.isShowing()) {
                    listActionPopup.dismiss()
                } else {
                    mViewModel.popStack()
                }
            }
            true
        }



        binding!!.myButtonCancel.setOnClickListener { dismiss() }
        binding!!.myButtonSelect.setOnClickListener {
            if(getCurDocumentFile() != null){
                val isFromSdCard =
                    getCurDocumentFile()!!
                        .inSdCardStorage(mainActivity)
                if (isFromSdCard && !FilePickerUtils.isGrantAccessSdCard(mainActivity)) {
                    showDialogInform(false)
                } else {
                    dismiss()
                    mOnPickPathResultListener?.onPickPathResult(
                        getCurDocumentFile()!!.getAbsolutePath(mainActivity),
                        mViewModel.storageBrowserModel!!.curStorageModel().storageId
                    )
                }
            }
        }

        mDialog!!.setCancelable(false)
        isCancelable = false
        binding!!.viewModel = mViewModel
        mViewModel.initStorage()
//        reloadData()
    }

    private fun getCurDocumentFile(): DocumentFile? {
        return mViewModel.storageBrowserModel!!.getCurDocumentationFile(mainActivity)
    }

    override fun initViewModel() {

        val factory = PickFolderViewModel.Factory(
            mainActivity.application as App
        )
        mViewModel = ViewModelProvider(this, factory)[PickFolderViewModel::class.java]

        mViewModel.storageBrowserModelLiveData.observe(this) {
            it?.let {
                browserPathAdapter.updatePaths(it.fullPathStack(mainActivity))
                mViewModel.fetch(
                    null
                )
            }
        }
        mViewModel.curStateLiveData.observe(this) {
            if (it != null) {
                binding!!.recycleView.layoutManager!!.onRestoreInstanceState(it)
            } else {
                binding!!.recycleView.layoutManager!!.scrollToPosition(0)
            }
        }
        mViewModel.isStackNullLiveData.observe(this) {
            it?.let {
                if (it) {
                    dismiss()
                }
            }
        }
        mViewModel.allFileLiveData.observe(this) {
            it?.let {
                if (it.loadingStatus == LoadingStatus.Success) {
                    val body = (it as DataResponse.DataSuccess).body
                    mAdapter!!.update(body)
                    mViewModel.updateCurPosY()
                    smoothScroll(browserPathAdapter.itemCount - 1)
                } else if (it.loadingStatus == LoadingStatus.Error) {
                    mAdapter!!.update(null)
                }
            }
        }

        mViewModel.actionModelLiveData.observe(this) {
            it?.let {
                if (it.isNotEmpty()) {
                    listActionPopup.showPopup(
                        binding!!.myImageViewStorage,
                        it,
                        this@PickFolderDialog
                    )
                }
            }
        }
    }

    private fun smoothScroll(position: Int) {
        smoothScroller = object : LinearSmoothScroller(context) {
            override fun getVerticalSnapPreference(): Int {
                return SNAP_TO_START
            }
        }
        smoothScroller!!.targetPosition = position.coerceAtLeast(0)
        binding!!.recycleViewPath.layoutManager!!.startSmoothScroll(smoothScroller);
    }


    companion object {
        fun create(): PickFolderDialog {
            return PickFolderDialog()
        }
    }

    private val grantAccessLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == Activity.RESULT_OK) {
            val contentResolver = mainActivity.contentResolver
            val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            it.data?.data?.also { uri ->
                if (StorageUtils.checkIfSDCardRoot(uri)) {
                    contentResolver.takePersistableUriPermission(uri, takeFlags)
                    SharedPreferenceUtils.getInstance(requireContext())!!.saveUriSdCard(uri.toString())
                    mOnPickPathResultListener?.onPickPathResult(
                        getCurDocumentFile()!!.getAbsolutePath(mainActivity),
                        mViewModel.storageBrowserModel!!.curStorageModel().storageId
                    )
                } else{
                    Toast.makeText(requireContext(), "Unsuccessful", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun launchGrantSdCardPermission() {
        val intent = StorageUtils.getSdCardAccessIntent(mainActivity)
        if (intent != null) {
            grantAccessLauncher.launch(intent)
        } else {
            Toast.makeText(requireContext(), "Unsuccessful", Toast.LENGTH_SHORT).show()
        }

    }

    private fun showDialogInform(mWillDeleteAfterPermissionRequested: Boolean) {
        AlertDialog.Builder(mainActivity).run {
            setTitle(getString(R.string.title_dialog_permission_sdcard))
            setMessage(getString(R.string.msg_giant_sd_card))
            setPositiveButton(getString(R.string.ok)) { _: DialogInterface?, _: Int ->
                launchGrantSdCardPermission()
            }
            setNegativeButton(getString(R.string.cancel)) { dialogInterface: DialogInterface, _: Int ->
                dialogInterface.dismiss()
            }
            create()
            show()
        }
    }

    private fun reloadData() {
        mViewModel.fetch(
            binding!!.recycleView.layoutManager!!.onSaveInstanceState()
        )
    }

    override fun onItemActionClick(position: Int) {
        mViewModel.changeStorage(position)
    }

    override fun onItemClick(position: Int) {
        mViewModel.popToPosition(position)
    }

    interface OnPickPathResultListener {
        fun onPickPathResult(path: String?, storageId:String)
    }
}