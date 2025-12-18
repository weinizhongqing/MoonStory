package com.ping.night.story.download

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.ping.night.story.Constant
import com.ping.night.story.NsApp
import com.ping.night.story.R
import com.ping.night.story.page.NsOpenActivity

class DownloadPush {


    companion object {
        private const val CHANNEL_ID = "ns_channel"
        private const val CHANNEL_NAME = "Night Story Notifications"
    }

    private val notificationManager = NsApp.app.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "NOTIFICATIONS FOR DOWNLOAD"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showProgressNotification(downloadInfo: com.ping.night.story.db.DownloadInfo, progress: Int, bitmap: Bitmap?) {
        val remoteViews = RemoteViews(NsApp.app.packageName, R.layout.ns_download_natify).apply {
            setTextViewText(R.id.tv_title, downloadInfo.title)
            setTextViewText(R.id.tv_progress, "$progress%")
            setProgressBar(R.id.progress,100,progress,false)
            bitmap?.applyToRemoteViews(this, R.id.down_img)
        }

        val notification = buildNotification(remoteViews, Constant.DOWNLOAD_MODE_KEY, downloadInfo.taskId.hashCode())
            .setOngoing(true)
            .build()

        notificationManager.notify(downloadInfo.taskId.hashCode(), notification)
    }

    fun showCompletionNotification(downloadInfo: com.ping.night.story.db.DownloadInfo, bitmap: Bitmap?) {
        val remoteViews = RemoteViews(NsApp.app.packageName, R.layout.ns_download_natify_suc).apply {
            setTextViewText(R.id.tv_title, downloadInfo.title)
            bitmap?.applyToRemoteViews(this, R.id.down_img)
        }

        val notification = buildNotification(remoteViews, Constant.DOWNLOAD_MODE_SUCCESS_KEY, downloadInfo.taskId.hashCode())
            .setOngoing(false)
            .build()

        notificationManager.notify(downloadInfo.taskId.hashCode(), notification)
    }

    private fun buildNotification(
        remoteViews: RemoteViews,
        fromType: String,
        notificationId: Int
    ): NotificationCompat.Builder {
        return NotificationCompat.Builder(NsApp.app, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(buildContentIntent(fromType, notificationId))
            .setCustomContentView(remoteViews)
            .setCustomBigContentView(remoteViews)
            .setCustomHeadsUpContentView(remoteViews)
            .setPriority(NotificationCompat.PRIORITY_LOW)
    }

    private fun buildContentIntent(type: String, notifyId: Int): PendingIntent {
        val intent = Intent(NsApp.app, NsOpenActivity::class.java).apply {
            `package` = NsApp.app.packageName
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            )
            putExtra(Constant.APP_RUN_STATUS_MODE_KEY, type)
            putExtra(Constant.NOTIFICATION_ID_KEY, notifyId)
        }

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        // 用 notifyId 作为 requestCode，避免重复触发问题
        return PendingIntent.getActivity( NsApp.app, notifyId, intent, flags)
    }

    // 扩展函数：简化 bitmap 设置
    private fun Bitmap.applyToRemoteViews(remoteViews: RemoteViews, viewId: Int) {
        remoteViews.setImageViewBitmap(viewId, this)
    }

}