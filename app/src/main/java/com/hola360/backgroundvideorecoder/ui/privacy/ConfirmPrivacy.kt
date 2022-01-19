package com.hola360.backgroundvideorecoder.ui.privacy

import androidx.navigation.fragment.findNavController
import com.hola360.backgroundvideorecoder.MainActivity
import com.hola360.backgroundvideorecoder.NavMainGraphDirections
import com.hola360.backgroundvideorecoder.R
import com.hola360.backgroundvideorecoder.databinding.FragmentConfirmPrivacyBinding
import com.hola360.backgroundvideorecoder.ui.base.basefragment.BaseFragment

class ConfirmPrivacy: BaseFragment<FragmentConfirmPrivacyBinding>() {

    override val layoutId: Int = R.layout.fragment_confirm_privacy
    override val showToolbar: Boolean = false
    override var toolbarTitle: String? = null
    override val menuCode: Int = 0

    override fun initView() {
        binding!!.termOfService.setOnClickListener {
            findNavController().navigate(NavMainGraphDirections.actionToNavPrivacy(false))
        }
        binding!!.privacyPolicy.setOnClickListener {
            findNavController().navigate(NavMainGraphDirections.actionToNavPrivacy(true))
        }
        binding!!.agree.setOnClickListener {
            dataPref!!.putBooleanValue(MainActivity.PRIVACY, true)
            findNavController().popBackStack()
        }
    }
}