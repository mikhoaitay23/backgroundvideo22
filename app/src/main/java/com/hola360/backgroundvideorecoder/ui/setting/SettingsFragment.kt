package com.hola360.backgroundvideorecoder.ui.setting

import com.hola360.backgroundvideorecoder.R
import com.hola360.backgroundvideorecoder.databinding.FragmentSettingBinding
import com.hola360.backgroundvideorecoder.ui.base.basefragment.BaseFragment

class SettingsFragment: BaseFragment<FragmentSettingBinding>() {

    override val layoutId: Int = R.layout.fragment_setting
    override val showToolbar: Boolean = true
    override val toolbarTitle: String? by lazy {
        resources.getString(R.string.setting_title)
    }
    override val menuCode: Int = 0

    override fun initViewModel() {

    }

    override fun initView() {

    }
}