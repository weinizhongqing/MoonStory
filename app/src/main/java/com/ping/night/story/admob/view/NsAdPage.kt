package com.ping.night.story.admob.view

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.ping.night.story.admob.NsAd
import com.ping.night.story.admob.NsAdHelper
import com.ping.night.story.admob.position.NsPosition
import com.ping.night.story.admob.position.NsPosition.isEnable
import com.ping.night.story.dialog.AdLoadingDialog
import com.ping.night.story.fbase.AffairHelper
import com.ping.night.story.page.BasePage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

open class NsAdPage : BasePage(){

    open val isBackHomePageAdShow : Boolean = false

    open val currentPageNativeAd : String? = null

    open val currentPageNativeView : CustomNativeAdView? = null

    open var skipLoadNativeAd : Boolean = false

    private val adLoadingDialog : AdLoadingDialog by lazy {
        AdLoadingDialog(
            this
        )
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isBackHomePageAdShow){
            preLoadAd(NsPosition.AD_BACK_I)
        }
    }

    override fun onBack() {
        if (isBackHomePageAdShow){
            showInterAd(NsPosition.AD_BACK_I){
                super.onBack()
            }
        }else{
            super.onBack()
        }
    }

    protected fun preLoadAd(position: String) {
        if (position.isEnable()) {
            lifecycleScope.launch {
                NsAdHelper.instance.preLoad(position)
            }
        }
    }

    protected fun showInterAd(position: String, call: () -> Unit) {
        if (isAppRun) {
            if (position.isEnable()) {
                AffairHelper.instance.event("arrive_ad_$position")
                val ad = NsAdHelper.instance.get(position)
                adLoadingDialog.show()
                if (ad != null) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        delay(500)
                        adLoadingDialog.dismiss()
                        showInterAd(ad, position, call)
                    }
                } else {
                    lifecycleScope.launch {
                        preLoadAd(position)
                        withContext(Dispatchers.Main) {
                            object : android.os.CountDownTimer(5000, 100) {
                                override fun onTick(millisUntilFinished: Long) {
                                    val onTickAD =
                                        NsAdHelper.instance.get(position)
                                    if (onTickAD != null) {
                                        adLoadingDialog.dismiss()
                                        cancel()
                                        showInterAd(onTickAD, position, call)
                                    }
                                }

                                override fun onFinish() {
                                    val onFinishAd = NsAdHelper.instance.get(position)
                                    if (onFinishAd != null) {
                                        adLoadingDialog.dismiss()
                                        showInterAd(onFinishAd, position, call)
                                    } else {
                                        adLoadingDialog.dismiss()
                                        call.invoke()
                                    }
                                }
                            }.start()
                        }
                    }
                }
            } else {
                call.invoke()
            }
        } else {
            call.invoke()
        }
    }


    private fun showInterAd(ad: NsAd, position: String, call: () -> Unit) {
        AffairHelper.instance.event("fill_ad_$position")
        ad.onClick {
            AffairHelper.instance.event("click_ad_$position")
        }.onShow {
            AffairHelper.instance.event("show_ad_$position")
            lifecycleScope.launch {
                NsAdHelper.instance.preLoad(position)
            }
        }.onClose {
            call.invoke()
        }.show(position, this@NsAdPage)
    }



    override fun onResume() {
        super.onResume()
        if (currentPageNativeAd == null && currentPageNativeView == null) {
            return
        }
        if (skipLoadNativeAd) {
            skipLoadNativeAd = false
        } else {
            showNativeAd(currentPageNativeAd!!,currentPageNativeView!!)
        }
    }

    private fun showNativeAd(position: String, customNativeAdView: CustomNativeAdView) {
        lifecycleScope.launch {
            val t1 = System.currentTimeMillis()
            NsAdHelper.instance.load(position)
            val t2 = System.currentTimeMillis()
            if (t2 - t1 < 390) {
                delay(390 - (t2 - t1))
            }
            withContext(Dispatchers.Main) {
                if (isAppRun) {
                    if (position.isEnable()) {
                        AffairHelper.instance.event("arrive_ad_${position}")
                        val ad = NsAdHelper.instance.get(position)
                        if (ad != null) {
                            AffairHelper.instance.event("fill_ad_${position}")
                            ad.onShow {
                                AffairHelper.instance.event("show_ad_${position}")
                            }.onClick {
                                AffairHelper.instance.event("click_ad_${position}")
                            }.showNav(position, customNativeAdView)
                        }
                    }
                }
            }
        }
    }
    
}