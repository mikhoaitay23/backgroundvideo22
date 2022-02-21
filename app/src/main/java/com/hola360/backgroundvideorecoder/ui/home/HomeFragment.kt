package com.hola360.backgroundvideorecoder.ui.home

import android.Manifest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import com.hola360.backgroundvideorecoder.R
import com.hola360.backgroundvideorecoder.databinding.FragmentHomeBinding
import com.hola360.backgroundvideorecoder.ui.base.basefragment.BaseFragment
import com.hola360.backgroundvideorecoder.utils.Constants
import com.hola360.backgroundvideorecoder.utils.SystemUtils
import com.hola360.backgroundvideorecoder.utils.Utils

class HomeFragment: BaseFragment<FragmentHomeBinding>() {

    override val layoutId: Int = R.layout.fragment_home
    override val showToolbar: Boolean= false
    override val toolbarTitle: String? = null
    override val menuCode: Int = 0

    override fun initView() {
        binding!!.setting.setOnClickListener {
            findNavController().navigate(R.id.nav_setting)
        }
        binding!!.myFile.setOnClickListener {
            goToMyFile()
        }
        binding!!.share.setOnClickListener {

        }
        binding!!.rateApp.setOnClickListener {

        }
        binding!!.camera.setOnClickListener {
            findNavController().navigate(R.id.nav_video_record)
        }
        binding!!.audio.setOnClickListener {
            findNavController().navigate(R.id.nav_audio_record)
        }
    }

    override fun initViewModel() {

    }

    private fun goToMyFile() {
        if (SystemUtils.hasPermissions(mainActivity, *Utils.getStoragePermissions())) {
            findNavController().navigate(R.id.nav_my_file)
        } else {
            resultLauncher.launch(
                Utils.getStoragePermissions()
            )
        }
    }

    private val resultLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {
            it?.let {
                if (SystemUtils.hasPermissions(mainActivity, *Utils.getStoragePermissions())) {
                    findNavController().navigate(R.id.nav_my_file)
                } else {
                    mainActivity.showAlertPermissionNotGrant()
                }
            }
        }
}