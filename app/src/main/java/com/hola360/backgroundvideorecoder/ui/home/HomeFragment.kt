package com.hola360.backgroundvideorecoder.ui.home

import com.hola360.backgroundvideorecoder.R
import com.hola360.backgroundvideorecoder.databinding.FragmentHomeBinding
import com.hola360.backgroundvideorecoder.ui.base.basefragment.BaseFragment

class HomeFragment: BaseFragment<FragmentHomeBinding>() {

    override val layoutId: Int = R.layout.fragment_home
    override val showToolbar: Boolean= false
    override var toolbarTitle: String? = null
    override val menuCode: Int = 0

    override fun initView() {

    }
}