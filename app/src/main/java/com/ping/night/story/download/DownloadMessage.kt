package com.ping.night.story.download

import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.ping.night.story.db.DownloadInfo

class DownloadMessage {

    private val handler = Handler(Looper.getMainLooper())
    private val downloadPush = DownloadPush()
    private var lastNotifyTime = 0L

    fun notifyProgress(downloadInfo: DownloadInfo, bitmap: Bitmap?, callback: DownloaderHelper.DownloadCallback?) {
        val now = System.currentTimeMillis()
        if (now - lastNotifyTime >= 100 || (downloadInfo.downloaded >= downloadInfo.total && downloadInfo.total > 0)) {
            lastNotifyTime = now
            handler.post {
                val progress = if (downloadInfo.total == 0L) 0 else (downloadInfo.downloaded * 100 / downloadInfo.total).toInt()
                downloadPush.showProgressNotification(downloadInfo, progress, bitmap)
                Log.d("DownloadMessage", "notifyProgress: $downloadInfo")
                callback?.onDownloadChanged(downloadInfo)
            }
        }
    }

    fun notifyCompletion(downloadInfo: DownloadInfo, bitmap: Bitmap?, onUpdate: () -> Unit) {
        handler.post {
            downloadPush.showCompletionNotification(downloadInfo, bitmap)
            onUpdate()
        }
    }
}