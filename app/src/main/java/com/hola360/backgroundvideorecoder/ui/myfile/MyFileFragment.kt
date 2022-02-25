package com.hola360.backgroundvideorecoder.ui.myfile

import androidx.viewpager2.widget.ViewPager2
import com.hola360.backgroundvideorecoder.R
import com.hola360.backgroundvideorecoder.databinding.FragmentMyFileBinding
import com.hola360.backgroundvideorecoder.ui.base.basefragment.BaseFragment
import com.hola360.backgroundvideorecoder.ui.myfile.adapter.MyFileSectionAdapter
import com.hola360.backgroundvideorecoder.ui.record.RecordViewPagerAdapter

class MyFileFragment : BaseFragment<FragmentMyFileBinding>() {

    override val layoutId: Int = R.layout.fragment_my_file
    override val showToolbar: Boolean = true
    override val toolbarTitle: String? by lazy {
        resources.getString(R.string.my_file_title)
    }
    override val menuCode: Int = 2
    private val adapter: RecordViewPagerAdapter by lazy {
        RecordViewPagerAdapter(childFragmentManager, lifecycle).apply {
            updateFragment(RecordViewPagerAdapter.MY_FILE_PAGER)
        }
    }

    override fun initViewModel() {

    }

    override fun initView() {
        binding!!.viewPager.adapter = adapter
        binding!!.viewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                binding!!.position = position
            }
        })
        binding!!.mLayoutVideo.setOnClickListener {
            if (binding!!.position != 0) {
                binding!!.viewPager.setCurrentItem(0, true)
                binding!!.position = 0
            }
        }
        binding!!.mLayoutAudio.setOnClickListener {
            if (binding!!.position != 1) {
                binding!!.viewPager.setCurrentItem(1, true)
                binding!!.position = 1
            }
        }
    }
}