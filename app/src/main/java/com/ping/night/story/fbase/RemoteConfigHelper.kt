package com.ping.night.story.fbase

import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.ConfigUpdate
import com.google.firebase.remoteconfig.ConfigUpdateListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException
import com.google.firebase.remoteconfig.remoteConfig
import com.ping.night.story.NsApp
import com.ping.night.story.R
import com.ping.night.story.admob.config.NsAdConfigAnalysis
import com.ping.night.story.exx.decodeBase64
import com.ping.night.story.helper.ColHelper
import com.ping.night.story.helper.MMKVHelper
import kotlinx.coroutines.delay

class RemoteConfigHelper {

    companion object {
        val instance: RemoteConfigHelper by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { RemoteConfigHelper() }
    }

    private val mFireSettings: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()

    private var isComplete = false

    fun init() {
        mFireSettings.setDefaultsAsync(R.xml.fb_default_config)
        mFireSettings.fetchAndActivate().addOnCompleteListener {
            isComplete = true
            if (it.isSuccessful) {
                unComplete()
            }
        }

        mFireSettings.addOnConfigUpdateListener(object : ConfigUpdateListener {
            override fun onUpdate(configUpdate: ConfigUpdate) {
                mFireSettings.activate().addOnCompleteListener {
                    unComplete()
                }
            }

            override fun onError(p0: FirebaseRemoteConfigException) {
            }
        })
    }

    private fun unComplete() {
        AffairHelper.instance.event("ns_remote_config_update")
        NsAdConfigAnalysis.initConfig()
        ColHelper.postColo()
        NsApp.app.initFacebookOrSolar()
    }

    suspend fun awaitComplete() {
        while (!isComplete) {
           delay(100)
        }
    }

    fun getAdConfig() = mFireSettings.getString("ns_ad_config").let {
        it.ifEmpty {
            "eyJuc19hZF9wb3NpdGlvbl9jb25maWciOlt7Im5zX2FkX3Bvc2l0aW9uIjoibnNfc3RhcnQiLCJuc19wb3NpdGlvbl90eXBlIjoibnNfb3BlbiIsIm5zX3Bvc2l0aW9uX3N0YXR1cyI6MH0seyJuc19hZF9wb3NpdGlvbiI6Im5zX2xhbmd1YWdlX2kiLCJuc19wb3NpdGlvbl90eXBlIjoibnNfaW50ZXIiLCJuc19wb3NpdGlvbl9zdGF0dXMiOjF9LHsibnNfYWRfcG9zaXRpb24iOiJuc19jbGlja19pIiwibnNfcG9zaXRpb25fdHlwZSI6Im5zX2ludGVyIiwibnNfcG9zaXRpb25fc3RhdHVzIjoxfSx7Im5zX2FkX3Bvc2l0aW9uIjoibnNfYmFja19pIiwibnNfcG9zaXRpb25fdHlwZSI6Im5zX2ludGVyIiwibnNfcG9zaXRpb25fc3RhdHVzIjoxfSx7Im5zX2FkX3Bvc2l0aW9uIjoibnNfcGFyc2VfaSIsIm5zX3Bvc2l0aW9uX3R5cGUiOiJuc19pbnRlciIsIm5zX3Bvc2l0aW9uX3N0YXR1cyI6MH0seyJuc19hZF9wb3NpdGlvbiI6Im5zX2xhbmd1YWdlX24iLCJuc19wb3NpdGlvbl90eXBlIjoibnNfbmF0aXZlIiwibnNfcG9zaXRpb25fc3RhdHVzIjoxfSx7Im5zX2FkX3Bvc2l0aW9uIjoibnNfaG9tZV9uIiwibnNfcG9zaXRpb25fdHlwZSI6Im5zX25hdGl2ZSIsIm5zX3Bvc2l0aW9uX3N0YXR1cyI6MH0seyJuc19hZF9wb3NpdGlvbiI6Im5zX3BhcnNlX24iLCJuc19wb3NpdGlvbl90eXBlIjoibnNfbmF0aXZlIiwibnNfcG9zaXRpb25fc3RhdHVzIjoxfSx7Im5zX2FkX3Bvc2l0aW9uIjoibnNfZG93bmxvYWRfc3VjX24iLCJuc19wb3NpdGlvbl90eXBlIjoibnNfbmF0aXZlIiwibnNfcG9zaXRpb25fc3RhdHVzIjoxfSx7Im5zX2FkX3Bvc2l0aW9uIjoibnNfZnVuY19uIiwibnNfcG9zaXRpb25fdHlwZSI6Im5zX25hdGl2ZSIsIm5zX3Bvc2l0aW9uX3N0YXR1cyI6MH1dLCJuc19vcGVuIjpbeyJzX3R5cGUiOiJvcGVuIiwiaWQiOiJjYS1hcHAtcHViLTQyNDYyNjMzNzEzNjUxMTkvNDkwNzc0Nzk4OCJ9XSwibnNfbmF0aXZlIjpbeyJzX3R5cGUiOiJuYXRpdmUiLCJpZCI6ImNhLWFwcC1wdWItNDI0NjI2MzM3MTM2NTExOS8zNTk0NjY2MzE5In1dLCJuc19pbnRlciI6W3sic190eXBlIjoiaW50ZXIiLCJpZCI6ImNhLWFwcC1wdWItNDI0NjI2MzM3MTM2NTExOS8zMDI3NjkwMTA4In1dLCJuc19hZF9jYWNoZV9jb3VudCI6eyJuc19uYXRpdmUiOjEsIm5zX2ludGVyIjoxLCJuc19vcGVuIjoxfX0="
        }
    }

    //ewoibnNfZmFjZWJvb2tfaWQiOiI0NzkxMTc0MjQ5MDQxODAiLAoibnNfZmFjZWJvb2tfdG9rZW4iOiJlOWY3MmFkNzdmMWRhYmEwY2Q1NjAzZmE0YjJmYjJkNiIKfQ==
    fun getFacebookConfig() = mFireSettings.getString("ns_facebook_config")
    val updateFacebookAdValue: Float
        get() {
            val value = mFireSettings.getLong("ns_facebook_v_coefficient")
            if (value <= 0) return 1f
            return value / 100f
        }

    fun getFacebookThreshold() = mFireSettings.getDouble("ns_facebook_threshold")
    fun getUseConfig() = mFireSettings.getString("ns_user_config")
    fun getDefaultLink(): String {
        val default = mFireSettings.getString("ns_default_link")
            .ifEmpty { "aHR0cHM6Ly93d3cuaW5zdGFncmFtLmNvbS9yZWVsL0RQdW16d2pENE1SLw==" }
        return default.decodeBase64() ?: ""
    }

    val stateTransitionTime: Long
        get() = mFireSettings.getLong("ns_config_v_hour").takeIf { it > 0 } ?: 48
    val isVip: Boolean
        get() = mFireSettings.getBoolean(
            MMKVHelper.userKey.orEmpty().ifEmpty { "ns_user_ordinary" })
    val clocEnable: Boolean
        get() {
            return Firebase.remoteConfig.getBoolean("ns_cloc")
        }
    val fbValueThreshold: Double
        get() = mFireSettings.getDouble("ns_fb_value_threshold")

    val solarAppKey: String
        get() = mFireSettings.getString("ns_solar_app_key")

    val showNativeCountdown: Long
        get() = mFireSettings.getLong("ns_show_native_countdown")

}