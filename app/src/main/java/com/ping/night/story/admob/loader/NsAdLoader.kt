package com.ping.night.story.admob.loader

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.ping.night.story.NsApp
import com.ping.night.story.admob.NsAd
import com.ping.night.story.admob.NsAdHelper
import com.ping.night.story.admob.cache.NsAdCache
import com.ping.night.story.admob.config.NsAdConfig
import com.ping.night.story.admob.position.NsPosition.isEnable
import com.ping.night.story.admob.type.NsAdInfo
import com.ping.night.story.admob.type.NsAdSType
import com.ping.night.story.admob.type.NsAdType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class NsAdLoader(val load: FSPAdLoader2) {

    private var isLoad = hashMapOf<NsAdType, AtomicBoolean>()
    private var isPreLoad = hashMapOf<NsAdType, AtomicBoolean>()

    suspend fun lAd(
        key: String,
        au: NsAdConfig,
        adUnit: NsAdInfo,
        waitTime: Int,
        activity: Activity? = null
    ): NsAd? =
        withContext(Dispatchers.Main) {
            if (!key.isEnable()) {
                NsAdHelper.instance.adLog("[$key] load stop, is not enable")
                return@withContext null
            }

            if (isLoad[au.cacheType]?.get() == true) {
                NsAdHelper.instance.adLog("[$key] is loading... wait")
                var flag = false
                repeat(waitTime) { // 重复执行
                    if (!flag) {
                        if (NsAdCache.instance.hasCache(key)) {
                            flag = true
                            return@repeat
                        } else {
                            delay(200) // 等待200毫秒
                        }
                    }

                }
                return@withContext NsAdHelper.instance.get(key)
            }
            if (NsAdCache.instance.hasCache(key)) {
                val abpA = NsAdHelper.instance.get(key)
                if (abpA != null) {
                    NsAdHelper.instance.adLog("[$key] load from cache")
                    return@withContext abpA
                }
            }
            isLoad[au.cacheType] = AtomicBoolean(true)
            val resetJob = NsApp.scope.launch {
                delay(30000)
                if (isLoad[au.cacheType]?.get() == true) isLoad[au.cacheType]?.set(false)
            }
            NsAdHelper.instance.adLog("[$key] start load ")
            val ret = runCatching {
                when (adUnit.type) {
                    NsAdSType.NATIVE.type -> load.lnative(key, adUnit.id, au.cacheType)
                    NsAdSType.INTERSTITIAL.type -> load.linterstitial(
                        key,
                        adUnit.id,
                        au.cacheType
                    )

                    NsAdSType.OPEN.type -> load.lopen(key, adUnit.id, au.cacheType)
                    else -> {
                        return@withContext null
                    }
                }
            }
            isLoad[au.cacheType]?.set(false)
            resetJob.cancel()

            if (ret.isSuccess) {
                val ad = ret.getOrNull()
                if (ad != null) {
                    return@withContext ad
                }
            } else {
                NsAdHelper.instance.adLog("[$key]-[${au.cacheType}] load fail")
            }
            return@withContext null
        }


    suspend fun preAd(
        key: String,
        adConfig: NsAdConfig,
        adUnit: NsAdInfo,
        activity: Activity? = null
    ): NsAd? = withContext(Dispatchers.Main) {

        if (!key.isEnable()) {
            NsAdHelper.instance.adLog("[$key]-[${adUnit.type}] load stop, is not enable")
            return@withContext null
        }

        if (!NsAdCache.instance.checkCache(adConfig, key)) {
            //不需要加载
            return@withContext null
        }

        if (isPreLoad[adConfig.cacheType]?.get() == true) {
            NsAdHelper.instance.adLog("[$key]-[${adConfig.cacheType}] pre load is loading... return")
            return@withContext null
        }

        NsAdHelper.instance.adLog("[$key]-[${adConfig.cacheType}] pre load next")
        isPreLoad[adConfig.cacheType] = AtomicBoolean(true)
        val resetJob = NsApp.scope.launch {
            delay(30000)
            if (isPreLoad[adConfig.cacheType]?.get() == true) isPreLoad[adConfig.cacheType]?.set(
                false
            )
        }
        val ret = runCatching {
            when (adUnit.type) {
                NsAdSType.NATIVE.type -> load.lnative(key, adUnit.id, adConfig.cacheType)
                NsAdSType.INTERSTITIAL.type -> load.linterstitial(
                    key,
                    adUnit.id,
                    adConfig.cacheType
                )

                NsAdSType.OPEN.type -> load.lopen(key, adUnit.id, adConfig.cacheType)
                else -> {
                    null
                }
            }
        }

        isPreLoad[adConfig.cacheType]?.set(false)
        resetJob.cancel()

        if (ret.isSuccess) {
            val ad = ret.getOrNull()
            if (ad != null) {
//                Log.d(TAGA, "[$key]-[${abpAu.type}] pre load success. ad=$ad")
                NsAdHelper.instance.adLog(
                    "[$key]-[${adConfig.cacheType}] pre load success. cache.size=${
                        NsAdCache.instance.getCacheSize(adConfig.cacheType)
                    }"
                )

                return@withContext ad
            }
        } else {
            NsAdHelper.instance.adLog("[$key]-[${adConfig.cacheType}] pre load fail")
        }
        return@withContext null
    }


    class FSPAdLoader2(val context: Context) {
        suspend fun lnative(key: String, id: String, cacheType: NsAdType): NsAd {
            return loadNativeAd(key, id, cacheType)
        }

        suspend fun linterstitial(key: String, id: String, cacheType: NsAdType): NsAd {
            return loadInterstitialAd(key, id, cacheType)
        }

        suspend fun lopen(key: String, id: String, cacheType: NsAdType): NsAd {
            return loadOpenAd(key, id, cacheType)
        }


        // 加载 Native 广告
        private suspend fun loadNativeAd(key: String, id: String, type: NsAdType): NsAd {
            return suspendCancellableCoroutine { continuation ->
                val nativeAd = NsAd()
                val adLoader = AdLoader.Builder(context, id)
                    .forNativeAd { abpa ->
                        NsAdHelper.instance.adLog("[$key] load success")
                        nativeAd.setNative(type,abpa, id)
                        NsAdCache.instance.addCache(type, nativeAd)
                        continuation.resume(nativeAd)
                    }
                    .withAdListener(nativeAd.getNativeListener { p0 ->
                        NsAdHelper.instance.adLog("[$key] load error:${p0.message}")
                        continuation.resumeWithException(Exception(p0.code.toString()))
                    })
                    .withNativeAdOptions(
                        NativeAdOptions.Builder().build()
                    )
                    .build()
                adLoader.loadAd(AdRequest.Builder().build())
            }
        }

        // 加载 Interstitial 广告
        private suspend fun loadInterstitialAd(
            key: String,
            id: String,
            cacheType: NsAdType
        ): NsAd {
            return suspendCancellableCoroutine { continuation ->
                val adRequest = AdRequest.Builder().build()
                InterstitialAd.load(context, id, adRequest, object : InterstitialAdLoadCallback() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        NsAdHelper.instance.adLog("[$key] load error:${adError.message}")
                        continuation.resumeWithException(Exception(adError.code.toString()))
                    }

                    override fun onAdLoaded(interstitialAd: InterstitialAd) {
                        NsAdHelper.instance.adLog("[$key] load success")
                        val abpa = NsAd(
                            cacheType,
                            interstitialAd
                        )
                        NsAdCache.instance.addCache(cacheType, abpa)
                        continuation.resume(abpa)
                    }
                })
            }
        }

        // 加载 Open 广告
        private suspend fun loadOpenAd(key: String, id: String, cacheType: NsAdType): NsAd {
            return suspendCancellableCoroutine { continuation ->
                AppOpenAd.load(
                    context,
                    id,
                    AdRequest.Builder().build(),
                    object : AppOpenAd.AppOpenAdLoadCallback() {
                        override fun onAdFailedToLoad(adError: LoadAdError) {
                            NsAdHelper.instance.adLog("[$key] load error:${adError.message}")
                            continuation.resumeWithException(Exception(adError.code.toString()))
                        }

                        override fun onAdLoaded(appOpenAd: AppOpenAd) {
                            NsAdHelper.instance.adLog("[$key] load success")
                            val abpa = NsAd(
                                cacheType,
                                appOpenAd
                            )
                            NsAdCache.Companion.instance.addCache(cacheType, abpa)
                            continuation.resume(abpa)
                        }
                    })
            }
        }
    }

}