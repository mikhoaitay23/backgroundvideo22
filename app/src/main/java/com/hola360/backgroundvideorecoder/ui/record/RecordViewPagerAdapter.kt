package com.hola360.backgroundvideorecoder.ui.record

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.hola360.backgroundvideorecoder.ui.record.audio.audiorecord.RecordAudio
import com.hola360.backgroundvideorecoder.ui.record.audio.audioschedule.ScheduleAudio
import com.hola360.backgroundvideorecoder.ui.record.video.RecordVideo
import com.hola360.backgroundvideorecoder.ui.record.video.ScheduleVideo
import com.hola360.backgroundvideorecoder.ui.setting.AboutSetting
import com.hola360.backgroundvideorecoder.ui.setting.AudioSetting
import com.hola360.backgroundvideorecoder.ui.setting.GeneralSetting
import com.hola360.backgroundvideorecoder.ui.setting.VideoSetting

class RecordViewPagerAdapter(private val fragmentManager: FragmentManager, private val lifecycle: Lifecycle): FragmentStateAdapter(fragmentManager, lifecycle) {

    private val fragments= mutableListOf<Fragment>()
    private var type:Int=0

    fun updateFragment(type:Int){
        this.type= type
        fragments.clear()
        when(type){
            RECORD_VIDEO_PAGER->{
                fragments.add(RecordVideo())
                fragments.add(ScheduleVideo())
            }
            RECORD_AUDIO_PAGER->{
                fragments.add(RecordAudio())
                fragments.add(ScheduleAudio())
            }
            SETTING_PAGER->{
                fragments.add(GeneralSetting())
                fragments.add(VideoSetting())
                fragments.add(AudioSetting())
                fragments.add(AboutSetting())
            }
            MY_FILE_PAGER->{

            }
        }
    }

    override fun getItemCount(): Int {
        return if(type!= SETTING_PAGER){
            2
        }else{
            4
        }
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }

    companion object{
        const val RECORD_VIDEO_PAGER=0
        const val RECORD_AUDIO_PAGER=1
        const val SETTING_PAGER= 2
        const val MY_FILE_PAGER=3
    }
}