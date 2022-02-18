package com.hola360.backgroundvideorecoder.ui.setting

import androidx.viewpager2.widget.ViewPager2
import com.hola360.backgroundvideorecoder.R
import com.hola360.backgroundvideorecoder.databinding.FragmentSettingBinding
import com.hola360.backgroundvideorecoder.ui.base.basefragment.BaseFragment
import com.hola360.backgroundvideorecoder.ui.record.RecordViewPagerAdapter

class SettingsFragment : BaseFragment<FragmentSettingBinding>() {

    override val layoutId: Int = R.layout.fragment_setting
    override val showToolbar: Boolean = true
    override val toolbarTitle: String? by lazy {
        resources.getString(R.string.setting_title)
    }
    override val menuCode: Int = 0

    override fun initViewModel() {
    }

    override fun initView() {
        binding!!.viewPager.adapter = RecordViewPagerAdapter(childFragmentManager, lifecycle).apply {
            updateFragment(RecordViewPagerAdapter.SETTING_PAGER)
        }
        binding!!.viewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                binding!!.position = position
            }
        })
        binding!!.general.setOnClickListener {
            setSelectionPage(0)
        }
        binding!!.video.setOnClickListener {
            setSelectionPage(1)
        }
        binding!!.audio.setOnClickListener {
            setSelectionPage(2)
        }
        binding!!.about.setOnClickListener {
            setSelectionPage(3)
        }
    }

    private fun setSelectionPage(position: Int) {
        if (binding!!.position != position) {
            binding!!.viewPager.setCurrentItem(position, true)
            binding!!.position = position
        }
    }
}