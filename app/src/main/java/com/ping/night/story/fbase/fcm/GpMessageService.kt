package com.ping.night.story.fbase.fcm

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.ping.night.story.NsApp
import com.ping.night.story.fbase.AffairHelper
import com.ping.night.story.helper.LanguageHelper
import com.ping.night.story.helper.MMKVHelper

class GpMessageService : FirebaseMessagingService(){

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d("GpMessageService_log", "onMessageReceived: ${message.data}")
        AffairHelper.instance.event("ns_fcm_message")
        LanguageHelper.updateLocale(
            NsApp.app)
        val times = message.data[GpMessageHelper.TS_KEY]
        times?.let {
            GpMessageHelper.instance.handleTime(it)
        }
        GpMessageHelper.instance.handleMessage(message.data)
    }


    override fun onNewToken(token: String) {
        super.onNewToken(token)
        if (token != MMKVHelper.getToken()) {
            GpMessageUploadToken.upLoadToken()
        }
    }

}