package com.hola360.backgroundvideorecoder.broadcastreciever

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.hola360.backgroundvideorecoder.MainActivity
import com.hola360.backgroundvideorecoder.service.RecordService
import com.hola360.backgroundvideorecoder.ui.record.video.ScheduleVideo

class ListenRecordScheduleBroadcast:BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.let {
            if(intent.getBooleanExtra(ScheduleVideo.SCHEDULE_VIDEO, true)){
                context?.let {
                    val intent= Intent(context, RecordService::class.java)
                    intent.putExtra("Video_status", MainActivity.RECORD_VIDEO)
                    it.startService(intent)
                }
            }
        }
    }
}