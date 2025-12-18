package com.ping.night.story.download

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ping.night.story.BuildConfig
import com.ping.night.story.NsAppLifecycle
import com.ping.night.story.db.DownloadInfo
import com.ping.night.story.db.LocalVideoInfo
import com.ping.night.story.db.NsDBHelper
import com.ping.night.story.fbase.AffairHelper
import okhttp3.OkHttpClient
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object DownloaderHelper {

    private val okHttpClient: OkHttpClient by lazy {
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            @SuppressLint("TrustAllX509TrustManager")
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
            }

            @SuppressLint("TrustAllX509TrustManager")
            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
            }

            override fun getAcceptedIssuers() = arrayOf<X509Certificate>()
        })
        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, SecureRandom())
        val sslSocketFactory = sslContext.socketFactory
        OkHttpClient.Builder()
            .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
            .hostnameVerifier { _, _ -> true }.build()
    }

    private val downloadCache = DownloadCache()
    private val message = DownloadMessage()
    private val dHandle = DownloadHandle(okHttpClient, downloadCache, message)

    interface DownloadCallback {
        fun onDownloadChanged(info: DownloadInfo)
    }

    var callback: DownloadCallback? = null

    private val _liveData = MutableLiveData<List<DownloadInfo>>()
    val liveData: LiveData<List<DownloadInfo>> = _liveData

    fun init() {
        downloadCache.loadAllTasks()
        postUpdate()
    }

    fun insertTaskIfNotExist(task: DownloadInfo) {
        val existingTask = downloadCache.getTask(task.taskId)
        if (existingTask == null) {
            AffairHelper.instance.event("ns_start_download")
            AffairHelper.instance.event("ns_send_savenoti")
            message.notifyProgress(task, null, callback)
            downloadCache.insertTask(task)
            postUpdate()
        }
    }

    fun start(task: DownloadInfo) {
        dHandle.startDownload(task, ::postUpdate, callback, ::completeTask)
    }

    fun pause(taskId: String) {
        dHandle.pauseDownload(taskId, ::postUpdate)
    }

    fun resume(taskId: String) {
        dHandle.resumeDownload(taskId, ::postUpdate, callback, ::completeTask)
    }

    fun delete(taskId: String) {
        dHandle.cancelDownload(taskId)
        downloadCache.deleteTask(taskId)
        postUpdate()
    }

    private fun completeTask(task: DownloadInfo, bitmap: Bitmap?) {
        val videoInfo = NsDBHelper.instance.localVideoDao().queryByUrl(task.taskId)
        if (videoInfo == null) {
            NsDBHelper.instance.localVideoDao().insertModel(
                LocalVideoInfo(
                    parseUrl = task.sourceUrl,
                    imageUrl = task.img,
                    fileName = task.title,
                    videoDuration = task.duration.toLong(),
                    blogger = task.blogger,
                    videoDesc = task.videoDesc,
                    videoSize = task.total,
                    postsDesc = task.title,
                    filePath = task.path
                )
            )
        }

        downloadCache.getTask(task.taskId)?.let {
            downloadCache.updateTask(
                it.apply {
                    downloaded = task.downloaded
                    total = task.total
                    isCompleted = task.isCompleted
                    path = task.path
                }
            )
        }

        if (BuildConfig.DEBUG) Log.e("ns_Download", "completeTask:$task")
        message.notifyCompletion(task, bitmap) {
            AffairHelper.instance.event("ns_download_suc")
            AffairHelper.instance.event("ns_send_saved_noti")
            NsAppLifecycle.gotoDownloadSuccess(task)
            postUpdate()
        }

        delete(task.taskId)

    }

    private fun postUpdate() {
        _liveData.postValue(downloadCache.getAllTasksSorted())
    }

}