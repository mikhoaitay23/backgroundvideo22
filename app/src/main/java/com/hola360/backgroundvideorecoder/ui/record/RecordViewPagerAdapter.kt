package com.hola360.backgroundvideorecoder.ui.record

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.hola360.backgroundvideorecoder.ui.record.audio.RecordAudio
import com.hola360.backgroundvideorecoder.ui.record.audio.ScheduleAudio
import com.hola360.backgroundvideorecoder.ui.record.video.RecordVideo
import com.hola360.backgroundvideorecoder.ui.record.video.ScheduleVideo

class RecordViewPagerAdapter(private val fragmentManager: FragmentManager, private val lifecycle: Lifecycle): FragmentStateAdapter(fragmentManager, lifecycle) {

    private val fragments= mutableListOf<Fragment>()

    fun updateFragment(recordVideo:Boolean){
        fragments.clear()
        if(recordVideo){
            fragments.add(RecordVideo())
            fragments.add(ScheduleVideo())
        }else{
            fragments.add(RecordAudio())
            fragments.add(ScheduleAudio())
        }
    }

    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }
}