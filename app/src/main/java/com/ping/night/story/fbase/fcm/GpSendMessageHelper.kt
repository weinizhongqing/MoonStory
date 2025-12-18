package com.ping.night.story.fbase.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.RECEIVER_EXPORTED
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.ping.night.story.Constant
import com.ping.night.story.NsApp
import com.ping.night.story.R
import com.ping.night.story.exx.dip2px
import com.ping.night.story.fbase.AffairHelper
import com.ping.night.story.helper.AppHelper
import com.ping.night.story.helper.MMKVHelper
import com.ping.night.story.page.NsOpenActivity
import jp.wasabeef.glide.transformations.RoundedCornersTransformation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.min

class GpSendMessageHelper {

    companion object{
        val instance : GpSendMessageHelper by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { GpSendMessageHelper() }
    }
    var dataList: List<Pair<String, String>>? = null
    private val fcmIdList = arrayOf(128312, 128313, 128314, 128315, 128316, 128317)
    private var notificationManager: NotificationManager? = null

    init {
        val filter = IntentFilter(Intent.ACTION_USER_PRESENT)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            NsApp.app.registerReceiver(GpScreenOnReceiver(), filter, RECEIVER_EXPORTED)
        } else {
            NsApp.app.registerReceiver(GpScreenOnReceiver(), filter)
        }
        notificationManager =  NsApp.app.getSystemService<NotificationManager>()
        val push = NotificationChannel(
            "ns_message", "Night Story MESSAGE", NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "NOTIFICATIONS FOR FRM MESSAGE"
        }
        notificationManager?.createNotificationChannels(
            arrayListOf(
                push
            )
        )
    }
    fun sendMessage(dataList:List<Pair<String, String>>){
        if (!AppHelper.isScreenOn()) {
            this.dataList = dataList
        } else {
            AffairHelper.instance.event("send_fcm_message")
            Log.d("GpMessageService_log", "sendMessage: ${dataList.size}")
            showFcmNotify(dataList)
        }
    }
    private fun showFcmNotify(list: List<Pair<String, String>>){
        NsApp.scope.launch {
            list.forEach {
                val bitmap = getBitMap(it.second)
                val fcmId = createPushConf()
                val contentList = NsApp.app.resources.getStringArray(R.array.ns_message_send_title)
                val actionList = NsApp.app.resources.getStringArray(R.array.ns_message_send_action)
                val size = min(contentList.size, actionList.size)
                val contentIndex = (0..<size).random()
                withContext(Dispatchers.Main) {
                    showPushMsg(
                        fcmId,
                        bitmap,
                        contentList[contentIndex],
                        actionList[contentIndex],
                        it.first,
                        contentIndex
                    )
                }
                delay(1200)
            }
        }
    }
    private fun showPushMsg(id: Int, bitmap: Bitmap?, content: String, action: String, pUrl: String, cIndex: Int){
        val remoteView = RemoteViews(NsApp.app.packageName, R.layout.ns_fcm_notify_min_page)
        val remoteViewBig = RemoteViews(NsApp.app.packageName, R.layout.ns_fcm_notify_max_page)
        val builder = NotificationCompat.Builder(NsApp.app, "ns_message")
        builder.setOngoing(true)
        builder.setGroupSummary(false)
        builder.setGroup(id.hashCode().toString())
        builder.setAutoCancel(true)
        builder.setSmallIcon(R.mipmap.ic_launcher)
        builder.priority = NotificationManager.IMPORTANCE_HIGH
        remoteView.setTextViewText(R.id.tv_title, content)
        if (bitmap == null) {
            remoteView.setImageViewResource(R.id.fcm_img, R.drawable.shape_default_img)
            remoteViewBig.setImageViewResource(R.id.fcm_img, R.drawable.shape_default_img)
        } else {
            remoteView.setImageViewBitmap(R.id.fcm_img, bitmap)
            remoteViewBig.setImageViewBitmap(R.id.fcm_img, bitmap)
        }
        remoteViewBig.setTextViewText(R.id.btn_action, action)
        remoteViewBig.setTextViewText(R.id.tv_title, content)
        builder.setContentIntent(pIntentAction(NsApp.app, pUrl, id))
        builder.setContent(remoteView)
        builder.setCustomBigContentView(remoteViewBig)
        builder.setStyle(NotificationCompat.DecoratedCustomViewStyle())
        notificationManager?.notify(id, builder.build())
    }
    private fun pIntentAction(context: Context, url: String, id: Int): PendingIntent {
        val intent = Intent(context, NsOpenActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            setPackage(context.packageName)
            putExtra(
                Constant.APP_RUN_STATUS_MODE_KEY,
                Constant.NOTIFY_FCM_FORM_KEY)
            putExtra(Constant.NOTIFY_FCM_FORM_URL_KEY, url)
        }
        return PendingIntent.getActivity(
            context, id, intent,  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        )
    }
    private suspend fun getBitMap(url: String?): Bitmap? {
        return withContext(Dispatchers.IO) {
            if (url.isNullOrEmpty()) return@withContext null
            try {
                Glide.with(NsApp.app.applicationContext).asBitmap().load(url)
                    .apply(
                        RequestOptions
                            .bitmapTransform(RoundedCornersTransformation(NsApp.app.dip2px(16),0)))
                    .submit().get()
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    private fun createPushConf(): Int {
        val indexLast = MMKVHelper.notifyIdIndex
        val idIndex = (indexLast + 1) % fcmIdList.size
        MMKVHelper.notifyIdIndex = idIndex
        return fcmIdList[idIndex]
    }

}