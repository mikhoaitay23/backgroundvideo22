package com.hola360.backgroundvideorecoder.ui.record.video

import androidx.viewpager2.widget.ViewPager2
import com.hola360.backgroundvideorecoder.R
import com.hola360.backgroundvideorecoder.databinding.FragmentVideoRecordBinding
import com.hola360.backgroundvideorecoder.ui.base.basefragment.BaseFragment
import com.hola360.backgroundvideorecoder.ui.record.RecordViewPagerAdapter

class VideoRecordFragment: BaseFragment<FragmentVideoRecordBinding>() {

    override val layoutId: Int= R.layout.fragment_video_record
    override val showToolbar: Boolean = true
    override var toolbarTitle: String? = "Video Record"
    override val menuCode: Int = 0
    private val adapter: RecordViewPagerAdapter by lazy {
        RecordViewPagerAdapter(childFragmentManager, lifecycle).apply {
            updateFragment(true)
        }
    }
    private var curPagerPage= 0

    override fun initView() {
        binding!!.viewPager.adapter= adapter
        binding!!.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                curPagerPage= position
            }
        })
        binding!!.record.setOnClickListener {
            if(curPagerPage != 0){
                binding!!.viewPager.setCurrentItem(0, true)
            }
        }
        binding!!.schedule.setOnClickListener {
            if(curPagerPage != 1){
                binding!!.viewPager.setCurrentItem(1, true)
            }
        }
    }
}