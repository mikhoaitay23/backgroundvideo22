package com.hola360.backgroundvideorecoder.ui.myfile.audio

import android.util.Log
import androidx.lifecycle.ViewModelProvider
import com.hola360.backgroundvideorecoder.R
import com.hola360.backgroundvideorecoder.data.model.LoadDataStatus
import com.hola360.backgroundvideorecoder.data.response.DataResponse
import com.hola360.backgroundvideorecoder.databinding.LayoutMyAudioFileBinding
import com.hola360.backgroundvideorecoder.ui.record.BaseRecordPageFragment
import com.hola360.backgroundvideorecoder.ui.record.audio.audiorecord.RecordAudioViewModel

class MyAudioFileFragment : BaseRecordPageFragment<LayoutMyAudioFileBinding>() {

    private lateinit var viewModel: MyAudioFileViewModel

    override val layoutId = R.layout.layout_my_audio_file

    override fun initView() {

    }

    override fun initViewModel() {
        val factory = MyAudioFileViewModel.Factory(requireActivity().application)
        viewModel = ViewModelProvider(this, factory)[MyAudioFileViewModel::class.java]

        viewModel.loadAudios()
        viewModel.mediaFileLiveData.observe(this) {
            if (it.loadDataStatus == LoadDataStatus.SUCCESS) {
                val allMedia = (it as DataResponse.DataSuccessResponse).body
                Log.d("TAG", "initViewModel: $allMedia")

            } else if (it.loadDataStatus == LoadDataStatus.ERROR) {

            }
        }
    }
}