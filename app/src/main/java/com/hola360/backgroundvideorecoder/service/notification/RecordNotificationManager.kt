package com.hola360.backgroundvideorecoder.service.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.widget.RemoteViews
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

    fun getNotification(title: String, des: String, isVideo:Boolean, importance:Int): Notification {
        val builder = buildNotification(title, des, isVideo, importance)
        return builder.build()
    }

    fun notifyNewStatus(notification: Notification){
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun buildNotification(
        title: String, des: String,
        isVideo: Boolean, importance: Int
    ): NotificationCompat.Builder {
        if (Utils.isAndroidO()) {
            createChannel(importance)
        }
        val builder = NotificationCompat.Builder(mService, CHANNEL_ID)
        builder.apply {
            setContentIntent(createContentIntent())
            setSmallIcon(R.drawable.ic_abort)
            setStyle(NotificationCompat.DecoratedCustomViewStyle())
            setCustomContentView(getRemoteViews(title, des, isVideo))
            setOnlyAlertOnce(true)
        }
        return builder
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel(importance: Int) {
        val importanceLevel= if(importance==0){
            NotificationManager.IMPORTANCE_DEFAULT
        }else{
            NotificationManager.IMPORTANCE_LOW
        }
        val mChannel =
            NotificationChannel(CHANNEL_ID, CHANNEL_ID, importanceLevel)
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

    private fun getRemoteViews(title: String, des: String, isVideo: Boolean):RemoteViews{
        val bitmap= if(isVideo){
            BitmapFactory.decodeResource(mService.resources, R.drawable.ic_video_record)
        }else{
            BitmapFactory.decodeResource(mService.resources, R.drawable.ic_micro)
        }
        return RemoteViews(mService.packageName, R.layout.layout_custom_notification).apply {
            setTextViewText(R.id.des, des)
            setTextViewText(R.id.title, title)
            setImageViewBitmap(R.id.icon, bitmap)
        }
    }

    companion object {
        const val ACTION_STOP = "stop_record"
        const val ACTION_RECORD_FROM_SCHEDULE = "Action_schedule"
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