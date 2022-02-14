package com.hola360.backgroundvideorecoder.ui.record.video

import androidx.viewpager2.widget.ViewPager2
import com.hola360.backgroundvideorecoder.R
import com.hola360.backgroundvideorecoder.databinding.FragmentVideoRecordBinding
import com.hola360.backgroundvideorecoder.ui.base.basefragment.BaseFragment
import com.hola360.backgroundvideorecoder.ui.record.RecordViewPagerAdapter

class VideoRecordFragment: BaseFragment<FragmentVideoRecordBinding>() {

    override val layoutId: Int= R.layout.fragment_video_record
    override val showToolbar: Boolean = true
    override val toolbarTitle: String? by lazy {
        resources.getString(R.string.video_record_title)
    }
    override val menuCode: Int = 0
    private val adapter: RecordViewPagerAdapter by lazy {
        RecordViewPagerAdapter(childFragmentManager, lifecycle).apply {
            updateFragment(RecordViewPagerAdapter.RECORD_VIDEO_PAGER)
        }
    }

    override fun initViewModel() {
    }

    override fun initView() {
        binding!!.viewPager.adapter= adapter
        binding!!.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                binding!!.position= position
            }
        })
        binding!!.record.setOnClickListener {
            if(binding!!.position != 0){
                binding!!.viewPager.setCurrentItem(0, true)
                binding!!.position=0
            }
        }
        binding!!.schedule.setOnClickListener {
            if(binding!!.position != 1){
                binding!!.viewPager.setCurrentItem(1, true)
                binding!!.position=1
            }
        }
    }
}