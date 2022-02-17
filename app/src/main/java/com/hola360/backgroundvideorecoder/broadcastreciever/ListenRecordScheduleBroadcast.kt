package com.hola360.backgroundvideorecoder.broadcastreciever

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.hola360.backgroundvideorecoder.MainActivity
import com.hola360.backgroundvideorecoder.utils.Constants
import com.hola360.backgroundvideorecoder.utils.VideoRecordUtils

class ListenRecordScheduleBroadcast:BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.let {
            if(intent.getBooleanExtra(Constants.SCHEDULE_TYPE, true)){
                context?.let {
                    VideoRecordUtils.startRecordIntent(it, MainActivity.RECORD_VIDEO)
                }
            }
        }
    }
}