package com.ping.night.story.helper

import com.ping.night.story.BuildConfig
import com.ping.night.story.NsApp
import com.ping.night.story.fbase.RemoteConfigHelper
import com.ping.night.story.utils.AppNetUtils
import kotlinx.coroutines.launch
import okhttp3.Request

object ColHelper {

    private var uploadFlag = false

    fun postColo() {
        if (!RemoteConfigHelper.instance.clocEnable) return
        if (MMKVHelper.clockFinish) return
        if (uploadFlag) return
        uploadFlag = true
        NsApp.scope.launch {
            try {
                val request = Request.Builder()
                    .url("https://api.twelvedl.xyz/van/ka/")
                    .header("VI", NsApp.app.packageName)
                    .header("VU", BuildConfig.VERSION_NAME)
                    .build()
                val response = AppNetUtils.instance.client.newCall(request).execute()
                if (response.isSuccessful) {
                    val success = response.code == 200
                    if (success) {
                        MMKVHelper.clockFinish = true
                    }
                }
            } catch (_: Exception) {

            }
            uploadFlag = false
        }
    }

}