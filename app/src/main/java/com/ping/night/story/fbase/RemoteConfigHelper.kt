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
           delay(80)
        }
    }

    fun getAdConfig() = mFireSettings.getString("ns_ad_config").let {
        it.ifEmpty {
            "ewogICJuc19hZF9wb3NpdGlvbl9jb25maWciOiBbCiAgICB7CiAgICAgICJuc19hZF9wb3NpdGlvbiI6ICJuc19zdGFydCIsCiAgICAgICJuc19wb3NpdGlvbl90eXBlIjogIm5zX29wZW4iLAogICAgICAibnNfcG9zaXRpb25fc3RhdHVzIjogMAogICAgfSwKICAgIHsKICAgICAgIm5zX2FkX3Bvc2l0aW9uIjogIm5zX2xhbmd1YWdlX2kiLAogICAgICAibnNfcG9zaXRpb25fdHlwZSI6ICJuc19pbnRlciIsCiAgICAgICJuc19wb3NpdGlvbl9zdGF0dXMiOiAxCiAgICB9LAogICAgewogICAgICAibnNfYWRfcG9zaXRpb24iOiAibnNfY2xpY2tfaSIsCiAgICAgICJuc19wb3NpdGlvbl90eXBlIjogIm5zX2ludGVyIiwKICAgICAgIm5zX3Bvc2l0aW9uX3N0YXR1cyI6IDEKICAgIH0sCiAgICB7CiAgICAgICJuc19hZF9wb3NpdGlvbiI6ICJuc19iYWNrX2kiLAogICAgICAibnNfcG9zaXRpb25fdHlwZSI6ICJuc19pbnRlciIsCiAgICAgICJuc19wb3NpdGlvbl9zdGF0dXMiOiAxCiAgICB9LAogICAgewogICAgICAibnNfYWRfcG9zaXRpb24iOiAibnNfcGFyc2VfaSIsCiAgICAgICJuc19wb3NpdGlvbl90eXBlIjogIm5zX2ludGVyIiwKICAgICAgIm5zX3Bvc2l0aW9uX3N0YXR1cyI6IDAKICAgIH0sCiAgICB7CiAgICAgICJuc19hZF9wb3NpdGlvbiI6ICJuc19sYW5ndWFnZV9uIiwKICAgICAgIm5zX3Bvc2l0aW9uX3R5cGUiOiAibnNfbmF0aXZlIiwKICAgICAgIm5zX3Bvc2l0aW9uX3N0YXR1cyI6IDEKICAgIH0sCiAgICB7CiAgICAgICJuc19hZF9wb3NpdGlvbiI6ICJuc19ob21lX24iLAogICAgICAibnNfcG9zaXRpb25fdHlwZSI6ICJuc19uYXRpdmUiLAogICAgICAibnNfcG9zaXRpb25fc3RhdHVzIjogMAogICAgfSwKICAgIHsKICAgICAgIm5zX2FkX3Bvc2l0aW9uIjogIm5zX3BhcnNlX24iLAogICAgICAibnNfcG9zaXRpb25fdHlwZSI6ICJuc19uYXRpdmUiLAogICAgICAibnNfcG9zaXRpb25fc3RhdHVzIjogMQogICAgfSwKICAgIHsKICAgICAgIm5zX2FkX3Bvc2l0aW9uIjogIm5zX2Rvd25sb2FkX3N1Y19uIiwKICAgICAgIm5zX3Bvc2l0aW9uX3R5cGUiOiAibnNfbmF0aXZlIiwKICAgICAgIm5zX3Bvc2l0aW9uX3N0YXR1cyI6IDEKICAgIH0sCiAgICB7CiAgICAgICJuc19hZF9wb3NpdGlvbiI6ICJuc19mdW5jX24iLAogICAgICAibnNfcG9zaXRpb25fdHlwZSI6ICJuc19uYXRpdmUiLAogICAgICAibnNfcG9zaXRpb25fc3RhdHVzIjogMAogICAgfQogIF0sCiAgIm5zX29wZW4iOiBbCiAgICB7CiAgICAgICJzX3R5cGUiOiJvcGVuIiwKICAgICAgImlkIjoiY2EtYXBwLXB1Yi00MjQ2MjYzMzcxMzY1MTE5LzUxOTMxNTQyNTMiCiAgICB9LAogICAgewogICAgICAic190eXBlIjoib3BlbiIsCiAgICAgICJpZCI6ImNhLWFwcC1wdWItNDI0NjI2MzM3MTM2NTExOS8xMzI5OTMwODYzIgogICAgfSwKICAgIHsKICAgICAgInNfdHlwZSI6Im9wZW4iLAogICAgICAiaWQiOiJjYS1hcHAtcHViLTQyNDYyNjMzNzEzNjUxMTkvNzcwMzc2NzUyMSIKICAgIH0KICBdLAoKICAibnNfbmF0aXZlIjogWwogICAgewogICAgICAic190eXBlIjoibmF0aXZlIiwKICAgICAgImlkIjoiY2EtYXBwLXB1Yi00MjQ2MjYzMzcxMzY1MTE5LzM2MDQ3NDA0MTciCiAgICB9LAogICAgewogICAgICAic190eXBlIjoibmF0aXZlIiwKICAgICAgImlkIjoiY2EtYXBwLXB1Yi00MjQ2MjYzMzcxMzY1MTE5LzczNDQwMTgzMzgiCiAgICB9LAogICAgewogICAgICAic190eXBlIjoibmF0aXZlIiwKICAgICAgImlkIjoiY2EtYXBwLXB1Yi00MjQ2MjYzMzcxMzY1MTE5LzQ3MTc4NTQ5OTYiCiAgICB9CiAgXSwKICAibnNfaW50ZXIiOiBbCiAgICB7CiAgICAgICJzX3R5cGUiOiJpbnRlciIsCiAgICAgICJpZCI6ImNhLWFwcC1wdWItNDI0NjI2MzM3MTM2NTExOS8zNzY0NTIyNTEwIgogICAgfSwKICAgIHsKICAgICAgInNfdHlwZSI6ImludGVyIiwKICAgICAgImlkIjoiY2EtYXBwLXB1Yi00MjQ2MjYzMzcxMzY1MTE5LzYyMzA5MDM3NTAiCiAgICB9LAogICAgewogICAgICAic190eXBlIjoibmF0aXZlIiwKICAgICAgImlkIjoiY2EtYXBwLXB1Yi00MjQ2MjYzMzcxMzY1MTE5LzQ5MTc4MjIwODkiCiAgICB9CiAgXSwKCiAgIm5zX2FkX2NhY2hlX2NvdW50IjogewogICAgIm5zX25hdGl2ZSI6IDEsCiAgICAibnNfaW50ZXIiOiAxLAogICAgIm5zX29wZW4iOiAxCiAgfQp9"
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