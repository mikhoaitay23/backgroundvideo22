package com.hola360.backgroundvideorecoder.ui.record.video

import androidx.lifecycle.MutableLiveData
import androidx.viewpager2.widget.ViewPager2
import com.hola360.backgroundvideorecoder.R
import com.hola360.backgroundvideorecoder.databinding.FragmentVideoRecordBinding
import com.hola360.backgroundvideorecoder.ui.base.basefragment.BaseFragment
import com.hola360.backgroundvideorecoder.ui.record.RecordSchedule
import com.hola360.backgroundvideorecoder.ui.record.RecordViewPagerAdapter
import com.hola360.backgroundvideorecoder.ui.record.video.model.VideoRecordConfiguration

class VideoRecordFragment: BaseFragment<FragmentVideoRecordBinding>() {

    override val layoutId: Int= R.layout.fragment_video_record
    override val showToolbar: Boolean = true
    override val toolbarTitle: String? by lazy {
        resources.getString(R.string.video_record_title)
    }
    override val menuCode: Int = 0
    private val adapter: RecordViewPagerAdapter by lazy {
        RecordViewPagerAdapter(childFragmentManager, lifecycle).apply {
            setVideoRecordFragment(this@VideoRecordFragment)
            updateFragment(true)
        }
    }
    var videoRecordConfiguration= MutableLiveData<VideoRecordConfiguration>()
    var recordSchedule= MutableLiveData<RecordSchedule>()

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