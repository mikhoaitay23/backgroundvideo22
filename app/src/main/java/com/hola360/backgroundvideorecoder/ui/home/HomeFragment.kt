package com.hola360.backgroundvideorecoder.ui.home

import androidx.navigation.fragment.findNavController
import com.hola360.backgroundvideorecoder.R
import com.hola360.backgroundvideorecoder.databinding.FragmentHomeBinding
import com.hola360.backgroundvideorecoder.ui.base.basefragment.BaseFragment

class HomeFragment: BaseFragment<FragmentHomeBinding>() {

    override val layoutId: Int = R.layout.fragment_home
    override val showToolbar: Boolean= false
    override var toolbarTitle: String? = null
    override val menuCode: Int = 0

    override fun initView() {
        binding!!.camera.setOnClickListener {
            findNavController().navigate(R.id.nav_video_record)
        }
        binding!!.audio.setOnClickListener {
            findNavController().navigate(R.id.nav_audio_record)
        }
    }
}