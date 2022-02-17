package com.hola360.backgroundvideorecoder.ui.setting

import android.view.View
import androidx.navigation.fragment.findNavController
import com.hola360.backgroundvideorecoder.NavMainGraphDirections
import com.hola360.backgroundvideorecoder.R
import com.hola360.backgroundvideorecoder.databinding.LayoutSettingAboutBinding
import com.hola360.backgroundvideorecoder.ui.record.BaseRecordPageFragment

class AboutSetting : BaseRecordPageFragment<LayoutSettingAboutBinding>(), View.OnClickListener {

    override val layoutId: Int
        get() = R.layout.layout_setting_about

    override fun initView() {
        binding!!.btnPrivacyPolicy.setOnClickListener(this)
        binding!!.btnTermOfService.setOnClickListener(this)
        binding!!.btnContactUs.setOnClickListener(this)
        binding!!.btnRateUs.setOnClickListener(this)
        binding!!.btnShareApp.setOnClickListener(this)
    }

    override fun initViewModel() {

    }

    override fun onClick(p0: View?) {
        when (p0) {
            binding!!.btnPrivacyPolicy -> {
                findNavController().navigate(NavMainGraphDirections.actionToNavPrivacy(true))
            }
            binding!!.btnTermOfService -> {
                findNavController().navigate(NavMainGraphDirections.actionToNavPrivacy(false))
            }
            binding!!.btnContactUs -> {

            }
            binding!!.btnRateUs -> {

            }
            binding!!.btnShareApp -> {

            }
        }
    }
}