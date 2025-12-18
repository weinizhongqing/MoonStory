package com.ping.night.story.fbase

import android.os.Bundle
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics
import com.ping.night.story.NsApp
import com.ping.night.story.helper.MMKVHelper

class AffairHelper {

    companion object{
        val instance : AffairHelper by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {  AffairHelper() }
        const val TAG = "ns_analytics"
    }

    private val analytics = FirebaseAnalytics.getInstance(NsApp.app)

    fun init() {
        analytics.setUserProperty("ns_user", if (MMKVHelper.isVip) "ins" else "noins")
        analytics.setUserProperty("ns_day", MMKVHelper.getInstallDay())
    }

    fun event(action: String, key: String, value: String) {
        val bundle = Bundle()
        bundle.putString(key, value)
        analytics.logEvent(action, bundle)
        Log.i(TAG, "logEvent->${action} [$key=$value]")
    }

    fun event(action: String, bundle: Bundle? = null) {
        analytics.logEvent(action, bundle)
        Log.i(TAG, "logEvent->${action},${bundle ?: ""}")
    }

}