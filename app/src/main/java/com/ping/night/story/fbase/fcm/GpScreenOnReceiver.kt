package com.ping.night.story.fbase.fcm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ping.night.story.NsApp
import com.ping.night.story.fbase.AffairHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GpScreenOnReceiver : BroadcastReceiver(){

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_USER_PRESENT) {
            NsApp.scope.launch {
                delay(1000)
                AffairHelper.instance.event("send_receiver_fcm_message")
                GpSendMessageHelper.instance.dataList?.let {
                    GpSendMessageHelper.instance.sendMessage(it)
                    GpSendMessageHelper.instance.dataList = null
                }
            }
        }
    }
}