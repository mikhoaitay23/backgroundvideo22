package com.hola360.backgroundvideorecoder.ui.record.audio.audiorecord

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import com.hola360.backgroundvideorecoder.MainActivity
import com.hola360.backgroundvideorecoder.R
import com.hola360.backgroundvideorecoder.data.model.audio.AudioMode
import com.hola360.backgroundvideorecoder.data.model.audio.AudioModel
import com.hola360.backgroundvideorecoder.data.model.audio.AudioQuality
import com.hola360.backgroundvideorecoder.databinding.LayoutRecordAudioBinding
import com.hola360.backgroundvideorecoder.ui.dialog.OnDialogDismiss
import com.hola360.backgroundvideorecoder.ui.dialog.listdialog.ListSelectionAdapter
import com.hola360.backgroundvideorecoder.ui.dialog.listdialog.ListSelectionBotDialog
import com.hola360.backgroundvideorecoder.ui.record.BaseRecordPageFragment
import com.hola360.backgroundvideorecoder.ui.record.audio.bottomsheet.AudioRecordBottomSheetFragment
import com.hola360.backgroundvideorecoder.ui.record.audio.utils.AudioRecordUtils
import com.hola360.backgroundvideorecoder.utils.Constants
import com.hola360.backgroundvideorecoder.utils.SystemUtils
import kotlinx.android.synthetic.main.layout_record_audio.*

class RecordAudio : BaseRecordPageFragment<LayoutRecordAudioBinding>(), View.OnClickListener {

    override val layoutId: Int = R.layout.layout_record_audio
    private var audioModel: AudioModel? = null
    private var audioRecordUtils: AudioRecordUtils? = null
    private var audioRecordBottomSheetFragment: AudioRecordBottomSheetFragment? = null
    private lateinit var mainActivity: MainActivity
    private var listSelectionBottomSheet: ListSelectionBotDialog? = null
    private lateinit var viewModel: RecordAudioViewModel

    override fun initViewModel() {
        val factory = RecordAudioViewModel.Factory(requireActivity().application)
        viewModel = ViewModelProvider(this, factory)[RecordAudioViewModel::class.java]

        viewModel.recordAudioLiveData.observe(this, {
            Log.d("TAG", "initViewModel: $it")
        })
    }

    override fun initView() {
        audioRecordUtils = AudioRecordUtils()
        mainActivity = activity as MainActivity

        binding!!.btnQuality.setOnClickListener(this)
        binding!!.btnMode.setOnClickListener(this)
        binding!!.btnDuration.setOnClickListener(this)

        audioModel = AudioModel()
        audioRecordBottomSheetFragment = AudioRecordBottomSheetFragment()
        imgRecord.setOnClickListener {
            if (SystemUtils.hasPermissions(requireContext(), Constants.RECORD_AUDIO_PERMISSION)) {
                audioRecordBottomSheetFragment!!.show(
                    requireActivity().supportFragmentManager,
                    "bottomSheetAudioRecord"
                )
            } else {
                resultLauncher.launch(Constants.RECORD_AUDIO_PERMISSION)
            }
//            mainActivity.intentService = Intent(requireActivity(), RecordService::class.java)
//            mainActivity.intentService!!.putExtra("Audio_status", 0)
//            mainActivity.intentService!!.putExtra("Audio_configuration", "fakeVideoConfiguration")
//            requireActivity().startService(mainActivity.intentService)
//            requireActivity().bindService(
//                mainActivity.intentService!!,
//                mainActivity.mConnection,
//                Context.BIND_AUTO_CREATE
//            )
        }

        binding!!.lifecycleOwner = this
        binding!!.viewModel = viewModel
        viewModel.getAudioConfig()
    }

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
            if (result) {
                audioRecordBottomSheetFragment!!.show(
                    requireActivity().supportFragmentManager,
                    "bottomSheetAudioRecord"
                )
            } else {
                SystemUtils.showAlertPermissionNotGrant(binding!!, requireActivity())
            }
        }

    override fun onClick(p0: View?) {
        when (p0) {
            binding!!.btnQuality -> {
                onQualityBottomSheet()
            }
            binding!!.btnMode -> {
                onModeBottomSheet()
            }
            binding!!.btnDuration -> {
            }
        }
    }

    private fun onQualityBottomSheet() {
        val listSelection = resources.getStringArray(R.array.record_quality).toMutableList()
        listSelectionBottomSheet = ListSelectionBotDialog(
            getString(R.string.record_quality),
            listSelection,
            object : ListSelectionAdapter.OnItemListSelection {
                override fun onSelection(position: Int) {
                    listSelectionBottomSheet!!.setSelectionPos(position)
                    viewModel.updateQuality(AudioQuality.getByInt(position))
                    listSelectionBottomSheet!!.dialog!!.dismiss()
                }

            }, object : OnDialogDismiss{
                override fun onDismiss() {

                }

            })
        listSelectionBottomSheet!!.show(
            requireActivity().supportFragmentManager,
            "bottomSheetAudioRecordQuality"
        )
    }

    private fun onModeBottomSheet() {
        val listSelection = resources.getStringArray(R.array.record_mode).toMutableList()
        listSelectionBottomSheet = ListSelectionBotDialog(
            getString(R.string.record_mode),
            listSelection,
            object : ListSelectionAdapter.OnItemListSelection {
                override fun onSelection(position: Int) {
                    listSelectionBottomSheet!!.setSelectionPos(position)
                    viewModel.updateMode(AudioMode.getByInt(position))
                    listSelectionBottomSheet!!.dialog!!.dismiss()
                }

            }, object : OnDialogDismiss{
                override fun onDismiss() {

                }

            })
        listSelectionBottomSheet!!.show(
            requireActivity().supportFragmentManager,
            "bottomSheetAudioRecordMode"
        )
    }

}