package com.hola360.backgroundvideorecoder.ui.setting

import android.content.Intent
import android.net.Uri
import android.view.View
import androidx.navigation.fragment.findNavController
import com.hola360.backgroundvideorecoder.MainActivity
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
                contactUs()
            }
            binding!!.btnRateUs -> {
                (requireActivity() as MainActivity).showToast(getString(R.string.coming_soon))
            }
            binding!!.btnShareApp -> {
                (requireActivity() as MainActivity).showToast(getString(R.string.coming_soon))
            }
        }
    }

    private fun contactUs() {
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("mailto:")
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.extra_email)))
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.extra_subject))
        intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.extra_text))
        startActivity(Intent.createChooser(intent, "Send Email using: "))
    }
}