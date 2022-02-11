package com.hola360.backgroundvideorecoder.ui.record

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.hola360.backgroundvideorecoder.ui.record.audio.audiorecord.RecordAudio
import com.hola360.backgroundvideorecoder.ui.record.audio.audioschedule.ScheduleAudio
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

    fun updateRecordingTime(time:Long){
        if(fragments.size>0 && fragments[0] is RecordVideo){
            (fragments[0] as RecordVideo).updateRecordingTime(time)
        }
    }

    fun onRecordingComplete(){
        if(fragments.size>0 && fragments[0] is RecordVideo){
            (fragments[0] as RecordVideo).onRecordCompleted()
        }
        if(fragments.size>1 && fragments[1] is ScheduleVideo){
            (fragments[1] as ScheduleVideo).checkScheduleWhenRecordStop()
        }
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }
}