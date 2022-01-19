package com.hola360.backgroundvideorecoder.ui.privacy

import com.hola360.backgroundvideorecoder.R
import com.hola360.backgroundvideorecoder.databinding.FragmentPrivacyBinding
import com.hola360.backgroundvideorecoder.ui.base.basefragment.BaseFragment

class PrivacyFragment :BaseFragment<FragmentPrivacyBinding>() {

    override val layoutId: Int = R.layout.fragment_privacy
    override val showToolbar: Boolean = true
    override var toolbarTitle: String?= null
    override val menuCode: Int = 0
    private val args: PrivacyFragmentArgs by lazy {
        PrivacyFragmentArgs.fromBundle(requireArguments())
    }

    override fun initView() {
        toolbarTitle= if(args.isPrivacy){
             "Privacy Policy"
        }else{
            "Term of Service"
        }
    }
}