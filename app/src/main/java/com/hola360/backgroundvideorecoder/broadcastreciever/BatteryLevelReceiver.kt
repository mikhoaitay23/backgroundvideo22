package com.hola360.backgroundvideorecoder.broadcastreciever

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import com.hola360.backgroundvideorecoder.MainActivity
import com.hola360.backgroundvideorecoder.utils.VideoRecordUtils

class BatteryLevelReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.let {
            if(it.action== Intent.ACTION_BATTERY_LOW && context!= null){
                VideoRecordUtils.startRecordIntent(context, MainActivity.RECORD_VIDEO_LOW_BATTERY)
            }
        }
    }
}