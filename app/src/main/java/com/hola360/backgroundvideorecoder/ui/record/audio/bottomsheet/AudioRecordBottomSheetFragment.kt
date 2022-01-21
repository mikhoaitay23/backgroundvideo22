package com.hola360.backgroundvideorecoder.ui.record.audio.bottomsheet

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.hola360.backgroundvideorecoder.R
import com.hola360.backgroundvideorecoder.data.model.audio.AudioModel
import com.hola360.backgroundvideorecoder.databinding.FragmentBottomSheetRecordAudioBinding
import com.hola360.backgroundvideorecoder.ui.base.basedialog.BaseBottomSheetDialog
import com.hola360.backgroundvideorecoder.ui.record.audio.utils.AudioRecordUtils

class AudioRecordBottomSheetFragment :
    BaseBottomSheetDialog<FragmentBottomSheetRecordAudioBinding>(), View.OnClickListener {

    private lateinit var viewModel: AudioRecordBottomSheetViewModel
    private var audioRecordUtils: AudioRecordUtils? = null
    private var audioModel: AudioModel? = null

    override fun getLayout() = R.layout.fragment_bottom_sheet_record_audio

    override fun initView() {

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val factory = AudioRecordBottomSheetViewModel.Factory(requireActivity().application)
        viewModel =
            ViewModelProvider(this, factory)[AudioRecordBottomSheetViewModel::class.java]
        audioRecordUtils = AudioRecordUtils()
        audioModel = AudioModel()

        initClick()

        audioRecordUtils!!.onStartRecording(audioModel!!)
    }

    override fun onClick(view: View?) {
        when (view) {
            binding!!.btnPause -> {
                when {
                    audioRecordUtils!!.isPaused() -> audioRecordUtils!!.onResumeRecording()
                    audioRecordUtils!!.isRecording() -> audioRecordUtils!!.onPauseRecording()
                }
            }
            binding!!.btnAbort -> {

            }
            binding!!.btnSave -> {

            }
        }
    }

    private fun initClick() {
        binding!!.btnPause.setOnClickListener(this)
        binding!!.btnAbort.setOnClickListener(this)
        binding!!.btnSave.setOnClickListener(this)
    }
}