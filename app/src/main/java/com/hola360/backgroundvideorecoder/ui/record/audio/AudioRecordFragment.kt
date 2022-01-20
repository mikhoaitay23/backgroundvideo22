package com.hola360.backgroundvideorecoder.ui.record.audio

import androidx.viewpager2.widget.ViewPager2
import com.hola360.backgroundvideorecoder.R
import com.hola360.backgroundvideorecoder.databinding.FragmentAudioRecordBinding
import com.hola360.backgroundvideorecoder.ui.base.basefragment.BaseFragment
import com.hola360.backgroundvideorecoder.ui.record.RecordViewPagerAdapter

class AudioRecordFragment: BaseFragment<FragmentAudioRecordBinding>(){

    override val layoutId: Int = R.layout.fragment_audio_record
    override val showToolbar: Boolean = true
    override val toolbarTitle: String? by lazy {
        resources.getString(R.string.audio_record_title)
    }
    override val menuCode: Int= 0
    private val adapter: RecordViewPagerAdapter by lazy {
        RecordViewPagerAdapter(childFragmentManager, lifecycle).apply {
            updateFragment(false)
        }
    }
    private var curPagerPage= 0

    override fun initViewModel() {

    }

    override fun initView() {
        binding!!.viewPager.adapter= adapter
        binding!!.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                curPagerPage= position
            }
        })
    }
}