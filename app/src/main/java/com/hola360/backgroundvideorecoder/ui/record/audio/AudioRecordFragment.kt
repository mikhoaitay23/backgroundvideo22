package com.hola360.backgroundvideorecoder.ui.record.audio

import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.hola360.backgroundvideorecoder.R
import com.hola360.backgroundvideorecoder.databinding.FragmentAudioRecordBinding
import com.hola360.backgroundvideorecoder.ui.base.basefragment.BaseFragment
import com.hola360.backgroundvideorecoder.ui.record.RecordViewPagerAdapter

class AudioRecordFragment : BaseFragment<FragmentAudioRecordBinding>() {

    override val layoutId: Int = R.layout.fragment_audio_record
    override val showToolbar: Boolean = true
    override val toolbarTitle: String? by lazy {
        resources.getString(R.string.audio_record_title)
    }
    override val menuCode: Int = R.drawable.ic_home_setting
    private val adapter: RecordViewPagerAdapter by lazy {
        RecordViewPagerAdapter(childFragmentManager, lifecycle).apply {
            updateFragment(false)
        }
    }
    private var curPagerPage = 0

    override fun initViewModel() {

    }

    override fun initView() {
        binding!!.viewPager.adapter = adapter
        binding!!.viewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                curPagerPage = position
                if (curPagerPage == 0) {
                    binding!!.imgRecord.setColorFilter(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.md_white_1000
                        ), android.graphics.PorterDuff.Mode.SRC_IN
                    )
                    binding!!.tvRecord.setTextColor(resources.getColor(R.color.md_white_1000))

                    binding!!.imgSchedule.setColorFilter(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.bg_page_un_select
                        ), android.graphics.PorterDuff.Mode.SRC_IN
                    )
                    binding!!.tvSchedule.setTextColor(resources.getColor(R.color.bg_page_un_select))
                } else {
                    binding!!.imgRecord.setColorFilter(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.bg_page_un_select
                        ), android.graphics.PorterDuff.Mode.SRC_IN
                    )
                    binding!!.tvRecord.setTextColor(resources.getColor(R.color.bg_page_un_select))

                    binding!!.imgSchedule.setColorFilter(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.md_white_1000
                        ), android.graphics.PorterDuff.Mode.SRC_IN
                    )
                    binding!!.tvSchedule.setTextColor(resources.getColor(R.color.md_white_1000))
                }
            }
        })

        binding!!.mLayoutRecord.setOnClickListener {
            if (curPagerPage != 0) {
                binding!!.viewPager.setCurrentItem(0, true)
            }
        }
        binding!!.mLayoutSchedule.setOnClickListener {
            if (curPagerPage != 1) {
                binding!!.viewPager.setCurrentItem(1, true)
            }
        }
    }
}