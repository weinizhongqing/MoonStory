package com.ping.night.story.download

import android.graphics.Bitmap
import android.util.Log
import com.ping.night.story.NsApp
import com.ping.night.story.admob.NsAdHelper
import com.ping.night.story.admob.position.NsPosition
import com.ping.night.story.db.DownloadInfo
import com.ping.night.story.helper.VideoHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile

class DownloadHandle (
    private val okHttpClient: OkHttpClient,
    private val downloadCache: DownloadCache,
    private val message: DownloadMessage
) {
    private val downloadCalls = mutableMapOf<String, Call>()

    fun startDownload(downloadInfo: DownloadInfo, onUpdate: () -> Unit, callback: DownloaderHelper.DownloadCallback?, onComplete: (DownloadInfo, Bitmap?) -> Unit) {
        if (downloadInfo.isCompleted || downloadCache.getTask(downloadInfo.taskId)?.isPaused == false) return
        downloadCache.updateTask(downloadInfo.apply { isPaused = false })
        onUpdate()
        CoroutineScope(Dispatchers.IO).launch {
            delay(200)
            val fileSize = getFileSize(downloadInfo)
            if (fileSize > 0L && fileSize >= downloadInfo.downloaded) {
                Log.d("DownloadHandle", "startDownload: fileSize = $fileSize, downloaded = ${downloadInfo.downloaded}")
                downloadInfo.total = fileSize
                downloadCache.updateTask(downloadInfo)
            }
            val file = File(NsApp.app.getExternalFilesDir(null), "downloads/${downloadInfo.taskId}")
            file.parentFile?.mkdirs()
            val output = RandomAccessFile(file, "rw")
            output.seek(downloadInfo.downloaded)

            val call = buildRequest(downloadInfo)
            downloadCalls[downloadInfo.taskId] = call

            val bitmap = VideoHelper.getBitmapFromUrl(downloadInfo.img)

            call.enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    downloadInfo.isPaused = true
                    downloadCache.updateTask(downloadInfo)
                    onUpdate()
                }

                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        downloadInfo.isPaused = true
                        downloadCache.updateTask(downloadInfo)
                        onUpdate()
                        return
                    }
                    launch {
                        NsAdHelper.instance.preLoad(
                            NsPosition.AD_DOWNLOAD_SUC_N)
                    }
                    val contentRange = response.header("Content-Range")
                    if (contentRange == null && downloadInfo.downloaded > 0) {
                        downloadInfo.downloaded = 0
                        output.seek(0)
                    }

                    response.body.byteStream().use { input ->
                        val buffer = ByteArray(8192)
                        var bytesRead: Int
                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            output.write(buffer, 0, bytesRead)
                            downloadInfo.downloaded += bytesRead
                            message.notifyProgress(downloadInfo, bitmap, callback)
                        }
                        downloadInfo.isCompleted = true
                        downloadInfo.path = file.absolutePath
                        if (downloadInfo.total == 0L || downloadInfo.total < downloadInfo.downloaded) {
                            downloadInfo.total = downloadInfo.downloaded
                        }
                        onComplete(downloadInfo, bitmap)
                    }
                }
            })
        }
    }

    fun pauseDownload(taskId: String, onUpdate: () -> Unit) {
        Log.d("DownloadHandle", "pauseDownload: $taskId")
        downloadCalls[taskId]?.cancel()
        downloadCache.getTask(taskId)?.apply {
            isPaused = true
            downloadCache.updateTask(this)
            onUpdate()
        }
    }

    fun resumeDownload(taskId: String, onUpdate: () -> Unit, callback: DownloaderHelper.DownloadCallback?, onComplete: (DownloadInfo, Bitmap?) -> Unit) {
        downloadCache.getTask(taskId)?.takeIf { it.isPaused && !it.isCompleted }?.let {
            startDownload(it, onUpdate, callback,onComplete)
        }
    }

    fun cancelDownload(taskId: String) {
        downloadCalls[taskId]?.cancel()
        downloadCalls.remove(taskId)
    }

    private fun buildRequest(downloadModel: DownloadInfo): Call {
        val builder = Request.Builder().url(downloadModel.url)
        builder.addHeader("Range", "bytes=${downloadModel.downloaded}-")
        try {
            JSONObject(downloadModel.header).keys().forEach {
                builder.addHeader(it, JSONObject(downloadModel.header).getString(it))
            }
        } catch (_: Exception) {}
        return okHttpClient.newCall(builder.build())
    }

    private fun getFileSize(downloadModel: DownloadInfo): Long {
        val builder = Request.Builder().url(downloadModel.url).get()
        try {
            JSONObject(downloadModel.header).keys().forEach {
                builder.addHeader(it, JSONObject(downloadModel.header).getString(it))
            }
        } catch (_: Exception) {}

        try {
            okHttpClient.newCall(builder.build()).execute().use { response ->
                if (response.isSuccessful) {
                    val contentLength = response.header("Content-Length")?.toLongOrNull()
                    if (contentLength != null && contentLength > 0) {
                        return contentLength
                    }
                    val inputStream = response.body?.byteStream()
                    inputStream?.use {
                        val buffer = ByteArray(2048)
                        val bytesRead = it.read(buffer)
                        return if (bytesRead > 0) bytesRead.toLong() else 0L
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return 0L
    }

}