package com.hola360.backgroundvideorecoder.service.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.navigation.NavDeepLinkBuilder
import com.hola360.backgroundvideorecoder.MainActivity
import com.hola360.backgroundvideorecoder.R
import com.hola360.backgroundvideorecoder.service.RecordService
import com.hola360.backgroundvideorecoder.utils.Utils


class RecordNotificationManager(private val mService: RecordService) {
    val notificationManager: NotificationManagerCompat by lazy {
        NotificationManagerCompat.from(mService)
    }

    init {
        if (Utils.isAndroidO()) {
            createChannel()
        }
    }

    fun getNotification(
        title: String,
        des: String,
        isVideo: Boolean,
        importance: Int
    ): Notification {
        return buildNotification(title, des, isVideo, importance).build()
    }

    fun notifyNewStatus(notification: Notification) {
        notificationManager.notify(SOUND_NOTIFICATION_ID, notification)
    }

    private fun buildNotification(
        title: String, des: String,
        isVideo: Boolean, importance: Int
    ): NotificationCompat.Builder {
        return if (importance == 0) {
            buildHighChannel(title, des, isVideo)
        } else {
            buildLowChannel(title, des, isVideo)
        }
    }

    private fun buildHighChannel(
        title: String,
        des: String,
        isVideo: Boolean
    ): NotificationCompat.Builder {
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val builder = NotificationCompat.Builder(mService, SOUND_CHANNEL_ID)
        builder.apply {
            setContentIntent(createContentIntent())
            setSmallIcon(R.drawable.ic_video_record)
            setCustomContentView(getRemoteViews(title, des, isVideo))
            priority= NotificationCompat.PRIORITY_DEFAULT
            setOnlyAlertOnce(true)
            setSound(soundUri)
        }
        return builder
    }

    private fun buildLowChannel(
        title: String,
        des: String,
        isVideo: Boolean
    ): NotificationCompat.Builder {
        val builder = NotificationCompat.Builder(mService, SILENT_CHANNEL_ID)
        builder.apply {
            setContentIntent(createContentIntent())
            setSmallIcon(R.drawable.ic_video_record)
            setCustomContentView(getRemoteViews(title, des, isVideo))
            setOnlyAlertOnce(true)
            priority= NotificationCompat.PRIORITY_MIN
        }
        return builder
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel() {
        val soundChannel = NotificationChannel(SOUND_CHANNEL_ID, SOUND_CHANNEL_ID,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        soundChannel.enableLights(false)
        soundChannel.enableVibration(false)

        val silentChannel = NotificationChannel(SILENT_CHANNEL_ID, SILENT_CHANNEL_ID,
            NotificationManager.IMPORTANCE_MIN
        )
        silentChannel.enableLights(false)
        silentChannel.enableVibration(false)
        Log.d("abcVideo", "Create channel")
        notificationManager.createNotificationChannel(soundChannel)
        notificationManager.createNotificationChannel(silentChannel)
    }

    private fun createContentIntent(): PendingIntent {
//        val destination= if(isVideo){
//            R.id.nav_video_record
//        }else{
//            R.id.nav_audio_record
//        }
//        return NavDeepLinkBuilder(context)
//            .setComponentName(MainActivity::class.java)
//            .setGraph(R.navigation.nav_main_graph)
//            .setDestination(destination)
//            .createPendingIntent()
        val openUI = Intent(mService, MainActivity::class.java)
        openUI.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        return PendingIntent.getActivity(
            mService, REQUEST_CODE, openUI,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun getRemoteViews(title: String, des: String, isVideo: Boolean): RemoteViews {
        val drawable: Drawable? = if (isVideo) {
            ContextCompat.getDrawable(mService, R.drawable.ic_video_record)
        } else {
            ContextCompat.getDrawable(mService, R.drawable.ic_micro)
        }
        val titlePrefix = if (isVideo) {
            mService.getString(R.string.video_record_notification_prefix)
        } else {
            mService.getString(R.string.audio_record_notification_prefix)
        }
        val bitmap = drawable?.toBitmap(
            drawable.intrinsicWidth.coerceAtLeast(1),
            drawable.intrinsicHeight.coerceAtLeast(1),
            null
        )
        return RemoteViews(mService.packageName, R.layout.layout_custom_notification).apply {
            setTextViewText(R.id.titlePrefix, titlePrefix)
            setTextViewText(R.id.des, des)
            setTextViewText(R.id.title, title)
            setImageViewBitmap(R.id.icon, bitmap)
        }
    }

    companion object {
        const val ACTION_STOP = "stop_record"
        const val ACTION_RECORD_FROM_SCHEDULE = "Action_schedule"
        const val SOUND_NOTIFICATION_ID = 234
        private const val SOUND_CHANNEL_ID = "Sound_channel"
        private const val SILENT_CHANNEL_ID = "Silent_channel"
        private const val REQUEST_CODE = 1212
    }

    init {
        notificationManager.cancelAll()
    }
}