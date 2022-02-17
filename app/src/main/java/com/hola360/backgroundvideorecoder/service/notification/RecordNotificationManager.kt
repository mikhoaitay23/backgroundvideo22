package com.hola360.backgroundvideorecoder.service.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.hola360.backgroundvideorecoder.MainActivity
import com.hola360.backgroundvideorecoder.R
import com.hola360.backgroundvideorecoder.service.RecordService
import com.hola360.backgroundvideorecoder.utils.Utils


class RecordNotificationManager(private val mService: RecordService) {
    val notificationManager: NotificationManagerCompat by lazy {
        NotificationManagerCompat.from(mService)
    }

    fun getNotification(title: String, des: String): Notification {
        val builder = buildNotification(title, des)
        return builder.build()
    }

    fun notifyNewStatus(notification: Notification){
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun buildNotification(
        title: String,
        des: String
    ): NotificationCompat.Builder {
        if (Utils.isAndroidO()) {
            createChannel()
        }
        val builder = NotificationCompat.Builder(mService, CHANNEL_ID)
        builder.apply {
            setContentIntent(createContentIntent())
            setSmallIcon(R.drawable.ic_abort)
            setContentTitle(title)
            setContentText(des)
            setOnlyAlertOnce(true)
            setOngoing(true)
            setChannelId(CHANNEL_ID).addAction(
                R.drawable.ic_stop_24,
                mService.getString(R.string.stop), createStopIntent()
            )
            color = ContextCompat.getColor(mService, R.color.colorAccent)
        }
        return builder
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel() {
        val mChannel =
            NotificationChannel(CHANNEL_ID, CHANNEL_ID, NotificationManager.IMPORTANCE_LOW)
        mService.getString(R.string.app_name)
        mChannel.enableLights(false)
        mChannel.enableVibration(false)
        notificationManager.createNotificationChannel(mChannel)
    }

    private fun createContentIntent(): PendingIntent {
        val openUI = Intent(mService, MainActivity::class.java)
        openUI.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        return PendingIntent.getActivity(
            mService, REQUEST_CODE, openUI,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    companion object {
        const val ACTION_STOP = "stop_record"
        const val ACTION_RECORD_FROM_SCHEDULE = "Action_schedule"
        const val ACTION_RECORD_VIDEO_SCHEDULE = "Video_schedule"
        const val NOTIFICATION_ID = 234
        private const val CHANNEL_ID = "RecordBg"
        private const val REQUEST_CODE = 1212
    }

    private fun createStopIntent(): PendingIntent {
        val stopIntent = Intent(ACTION_STOP)
        return PendingIntent.getBroadcast(
            mService, 0,
            stopIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }


    init {
        notificationManager.cancelAll()
    }
}