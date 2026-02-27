package com.ping.night.story.admob

import android.app.Activity
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.nativead.NativeAd
import com.ping.night.story.NsApp
import com.ping.night.story.NsAppLifecycle
import com.ping.night.story.admob.type.NsAdSType
import com.ping.night.story.admob.type.NsAdType
import com.ping.night.story.admob.view.CustomNativeAdView
import com.ping.night.story.admob.view.NsFullNativeAdActivity
import kotlinx.coroutines.launch

class NsAd() {

    private var exTime: Long = 0
    private var nav: NativeAd? = null
    private var inter: InterstitialAd? = null
    private var open: AppOpenAd? = null
    private var adValue: AdValue? = null
    private var closeListener: (() -> Unit)? = null
    var clickListener: (() -> Unit)? = null
    var showListener: (() -> Unit)? = null
    var onNativeLoadFailed: ((LoadAdError) -> Unit)? = null
    private var isAva = true
    var stype: NsAdSType = NsAdSType.NATIVE
    var type: NsAdType = NsAdType.NATIVE

    var key: String = ""
    private var id: String = ""


    constructor(type: NsAdType, inter: InterstitialAd) : this() {
        this.type = type
        this.stype = NsAdSType.INTERSTITIAL
        this.inter = inter
        exTime = System.currentTimeMillis() + 60 * 60 * 1000
    }

    constructor(type: NsAdType, open: AppOpenAd) : this() {
        this.type = type
        this.stype = NsAdSType.OPEN
        this.open = open
        exTime = System.currentTimeMillis() + 1 * 60 * 60 * 1000
    }


    constructor(nat: NativeAd) : this() {
        this.type = NsAdType.NATIVE
        this.nav = nat
        exTime = System.currentTimeMillis() + 60 * 60 * 1000
    }


    fun getNativeListener(l: ((LoadAdError) -> Unit)): AdListener {
        this.onNativeLoadFailed = l

        return mNativeListener
    }

    fun setNative(type: NsAdType, nat: NativeAd, id: String) {
        this.id = id
        this.type = type
        this.stype =NsAdSType.NATIVE
        this.nav = nat
        exTime = System.currentTimeMillis() + 60 * 60 * 1000
    }

    private var mNativeListener = object : AdListener() {
        override fun onAdClicked() {
            super.onAdClicked()
            NsAdHelper.instance.adLog("[${key}] onAdClicked")
            NsApp.isClickNative = true
            clickListener?.invoke()
            NsApp.app.dealValueClickFb(adValue)
        }

        override fun onAdImpression() {
            super.onAdImpression()
            NsAdHelper.instance.adLog("[${key}] onAdImpression")
            showListener?.invoke()
            nav = null
        }

        override fun onAdFailedToLoad(p0: LoadAdError) {
            super.onAdFailedToLoad(p0)
            onNativeLoadFailed?.invoke(p0)
        }
    }


    private var mFullScreenContentCallback: FullScreenContentCallback =
        object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                closeListener?.invoke()
                onDestroy()
                NsApp.scope.launch {
                    NsAdHelper.instance.preLoad(key)
                }
            }

            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                closeListener?.invoke()
                onDestroy()
                NsApp.scope.launch {
                    NsAdHelper.instance.preLoad(key)
                }
            }

            override fun onAdShowedFullScreenContent() {
                NsAdHelper.instance.adLog("[${this@NsAd.key}] onAdImpression")
                inter = null
                open = null
                showListener?.invoke()
            }

            override fun onAdClicked() {
                NsAdHelper.instance.adLog("[${this@NsAd.key}] onAdClicked")
                clickListener?.invoke()
                NsApp.app.dealValueClickFb(adValue)
            }

        }

    /**
     * 广告关闭时回调
     */
    fun onClose(callBack: () -> Unit) = apply {
        this.closeListener = callBack
    }


    /**
     * 广告关闭时回调
     */
    fun onClick(callBack: () -> Unit) = apply {
        this.clickListener = callBack
    }

    /**
     * 广告关闭时回调
     */
    fun onShow(callBack: () -> Unit) = apply {
        this.showListener = callBack
    }

    fun showNav(key: String, customNativeAdView: CustomNativeAdView) {
        this.key = key
        Log.i(NsAdHelper.TAG_A, "[$key] show ad ${this.nav}")
        this.nav.let {
            it?.let { ad ->
                ad.setOnPaidEventListener { adValue ->
                    NsApp.app.logEvent(key, type, adValue, ad.responseInfo, id)
                    this.adValue = adValue
                }
                customNativeAdView.showAdView(ad)
            }
        }
        isAva = false
    }


    fun showNativeFullAd(key: String, activity: Activity) {
        this.key = key
        Log.i(NsAdHelper.TAG_A, "[$key] show ad ${this.nav}")
        if (this.nav == null) {
            closeListener?.invoke()
            return
        }
        this.nav!!.let {
            it.setOnPaidEventListener { adValue ->
                NsApp.app.logEvent(key, type, adValue, it.responseInfo, id)
                this.adValue = adValue
            }
            NsAdHelper.instance.tempNativeFAd = it
            NsFullNativeAdActivity.show(activity) {
                closeListener?.invoke()
            }
        }
        isAva = false
    }


    fun show(key: String, activity: Activity) {
        this.key = key
        Log.i(NsAdHelper.TAG_A, "[$key] show ad")
        try {

            if (type == NsAdType.OPEN) {
                when (stype) {
                    NsAdSType.INTERSTITIAL -> {
                        if (inter == null) {
                            closeListener?.invoke()
                        }

                        inter?.let { ad ->
                            ad.setOnPaidEventListener { adValue ->
                                NsApp.app.logEvent(key, type, adValue, ad.responseInfo, ad.adUnitId)
                                this.adValue = adValue
                            }
                        }
                        inter?.fullScreenContentCallback = mFullScreenContentCallback
                        inter?.show(activity)
                    }
                    NsAdSType.NATIVE -> {
                        showNativeFullAd(key,activity)
                    }
                    else -> {
                        if (open == null) {
                            closeListener?.invoke()
                        }

                        open?.let { openAd ->
                            openAd.setOnPaidEventListener { adValue ->
                                NsApp.app.logEvent(
                                    key,
                                    type,
                                    adValue,
                                    openAd.responseInfo,
                                    openAd.adUnitId
                                )
                                this.adValue = adValue
                            }
                        }
                        open?.fullScreenContentCallback = mFullScreenContentCallback
                        open?.show(activity)
                    }
                }
            } else if (type == NsAdType.INTERSTITIAL) {
                if(stype== NsAdSType.NATIVE){
                    showNativeFullAd(key,activity)
                }else{
                    if (inter == null) {
                        closeListener?.invoke()
                    }

                    inter?.let { ad ->
                        ad.setOnPaidEventListener { adValue ->
                            NsApp.app.logEvent(key, type, adValue, ad.responseInfo, ad.adUnitId)
                            this.adValue = adValue
                        }
                    }
                    inter?.fullScreenContentCallback = mFullScreenContentCallback
                    inter?.show(activity)
                }
            }
        } catch (_: Exception) {
            closeListener?.invoke()
        }
    }

    /**
     * 广告是否可用，广告展示之后调用onDestroy置为不可用
     */
    fun isAva(): Boolean {
        when (stype) {
            NsAdSType.NATIVE -> if (nav == null) {
                return false
            }

            NsAdSType.INTERSTITIAL -> if (inter == null) {
                return false
            }

            NsAdSType.OPEN -> if (open == null) {
                return false
            }

            else -> {
                return false
            }
        }
        return isAva && (System.currentTimeMillis() <= exTime)
    }

    fun onDestroy() {
        isAva = false
        nav?.destroy()
        inter = null
        nav = null
        open = null
    }

}