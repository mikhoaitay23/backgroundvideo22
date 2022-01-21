package com.hola360.backgroundvideorecoder.ui.record.audio

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import com.hola360.backgroundvideorecoder.R
import com.hola360.backgroundvideorecoder.data.model.audio.AudioModel
import com.hola360.backgroundvideorecoder.databinding.LayoutRecordAudioBinding
import com.hola360.backgroundvideorecoder.ui.record.BaseRecordPageFragment
import com.hola360.backgroundvideorecoder.ui.record.audio.bottomsheet.AudioRecordBottomSheetFragment
import com.hola360.backgroundvideorecoder.ui.record.audio.utils.AudioRecordUtils
import com.hola360.backgroundvideorecoder.utils.Constants
import com.hola360.backgroundvideorecoder.utils.SystemUtils
import kotlinx.android.synthetic.main.layout_record_audio.*

class RecordAudio : BaseRecordPageFragment<LayoutRecordAudioBinding>() {

    override val layoutId: Int = R.layout.layout_record_audio
    private var audioModel: AudioModel? = null
    private var audioRecordUtils: AudioRecordUtils? = null
    private var audioRecordBottomSheetFragment: AudioRecordBottomSheetFragment? = null

    override fun initViewModel() {

    }

    override fun initView() {
        audioRecordUtils = AudioRecordUtils()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        audioModel = AudioModel()
        audioRecordBottomSheetFragment = AudioRecordBottomSheetFragment()
        imgRecord.setOnClickListener {
            if (SystemUtils.hasPermissions(requireContext(), Constants.RECORD_AUDIO_PERMISSION)) {
//                when {
                audioRecordBottomSheetFragment!!.show(
                    requireActivity().supportFragmentManager,
                    "bottomSheetAudioRecord"
                )
//                    isPaused -> audioRecordUtils?.onResumeRecording()
//                    isRecording -> audioRecordUtils?.onPauseRecording()
//                    else -> audioRecordUtils?.onStartRecording(audioModel!!)
//                }
            } else {
                resultLauncher.launch(Constants.RECORD_AUDIO_PERMISSION)
            }
        }
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

}