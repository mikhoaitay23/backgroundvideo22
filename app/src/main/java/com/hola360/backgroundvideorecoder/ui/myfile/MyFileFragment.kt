package com.hola360.backgroundvideorecoder.ui.myfile

import com.hola360.backgroundvideorecoder.R
import com.hola360.backgroundvideorecoder.databinding.FragmentMyFileBinding
import com.hola360.backgroundvideorecoder.ui.base.basefragment.BaseFragment

class MyFileFragment: BaseFragment<FragmentMyFileBinding>() {

    override val layoutId: Int = R.layout.fragment_my_file
    override val showToolbar: Boolean = true
    override val toolbarTitle: String? by lazy {
        resources.getString(R.string.my_file_title)
    }
    override val menuCode: Int = 0

    override fun initViewModel() {

    }

    override fun initView() {

    }
}