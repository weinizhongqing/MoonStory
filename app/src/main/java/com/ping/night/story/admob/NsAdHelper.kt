package com.ping.night.story.admob

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.nativead.NativeAd
import com.ping.night.story.BuildConfig
import com.ping.night.story.admob.cache.NsAdCache
import com.ping.night.story.admob.config.NsAdConfigAnalysis
import com.ping.night.story.admob.loader.NsAdLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NsAdHelper {

    companion object {
        val instance: NsAdHelper by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { NsAdHelper() }
        const val TAG_A = "NS_AD_LOG"
    }

    private lateinit var context: Context
    private lateinit var load: NsAdLoader

    var tempNativeFAd: NativeAd? = null
    fun initialize(context: Context):NsAdHelper {
        this.context = context
        try {
            MobileAds.initialize(context) {
            }
            //初始化配置开关
            NsAdConfigAnalysis.initConfig()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        load = NsAdLoader(
            NsAdLoader.FSPAdLoader2(context)
        )
        return this
    }

    suspend fun load(
        key: String,
        waitTime: Int = 25,
        activity: Activity? = null,
    ): NsAd? =
        withContext(Dispatchers.Main) {
            val au = NsAdCache.instance.getAu(key)
            var ret: NsAd?
            if (au != null) {
                if (au.id.isNotEmpty()) {
                    for (id in au.id) {
                        ret = load.lAd(key, au, id, waitTime, activity)
                        if (ret != null) {
                            return@withContext ret
                        }
                    }
                }
            } else {
                adLog( "[$key] au is null")
            }
            return@withContext null
        }

    suspend fun preLoad(key: String, activity: Activity? = null) =
        withContext(Dispatchers.Main) {
            val au = NsAdCache.instance.getAu(key)
            var ret: NsAd?
            if (au != null) {
                if (au.id.isNotEmpty()) {
                    for (id in au.id) {
                        ret = load.preAd(key, au, id, activity)
                        if (ret != null) {
                            return@withContext
                        }
                    }
                }
            }
        }

    fun adLog(msg: String){
        if (BuildConfig.DEBUG)
            Log.d(TAG_A, msg)
    }

    fun get(key: String): NsAd? {
        return NsAdCache.instance.getCache(key)
    }

}