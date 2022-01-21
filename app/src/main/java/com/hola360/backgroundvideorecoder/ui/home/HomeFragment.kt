package com.hola360.backgroundvideorecoder.ui.home

import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import com.hola360.backgroundvideorecoder.R
import com.hola360.backgroundvideorecoder.databinding.FragmentHomeBinding
import com.hola360.backgroundvideorecoder.ui.base.basefragment.BaseFragment
import com.hola360.backgroundvideorecoder.utils.Constants
import com.hola360.backgroundvideorecoder.utils.SystemUtils
import java.util.jar.Manifest

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
            findNavController().navigate(R.id.nav_my_file)
        }
        binding!!.share.setOnClickListener {

        }
        binding!!.rateApp.setOnClickListener {

        }
        binding!!.camera.setOnClickListener {
            if(SystemUtils.hasPermissions(requireContext(), *Constants.CAMERA_RECORD_PERMISSION)){
                findNavController().navigate(R.id.nav_video_record)
            }else{
                getCameraPermission.launch(Constants.CAMERA_RECORD_PERMISSION)
            }
        }
        binding!!.audio.setOnClickListener {
            findNavController().navigate(R.id.nav_audio_record)
        }
    }

    override fun initViewModel() {

    }

    private val getCameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result: Map<String?, Boolean?>? ->
        if (SystemUtils.hasPermissions(requireContext(), *Constants.CAMERA_RECORD_PERMISSION)) {
            findNavController().navigate(R.id.nav_video_record)
        } else {
            SystemUtils.showAlertPermissionNotGrant(binding!!, requireActivity())
        }
    }
}