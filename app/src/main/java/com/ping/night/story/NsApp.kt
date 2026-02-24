package com.ping.night.story

import android.os.Bundle
import android.util.Base64
import android.util.Log
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.android.installreferrer.api.ReferrerDetails
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsConstants
import com.facebook.appevents.AppEventsLogger
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.ResponseInfo
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.crashlytics
import com.ping.night.story.admob.NsAdHelper
import com.ping.night.story.admob.type.NsAdType
import com.ping.night.story.db.NsDBHelper
import com.ping.night.story.db.SolarDataInfo
import com.ping.night.story.fbase.AffairHelper
import com.ping.night.story.fbase.RemoteConfigHelper
import com.ping.night.story.fbase.fcm.GpMessageUploadToken
import com.ping.night.story.helper.MMKVHelper
import com.ping.night.story.helper.NsRecommendHelper
import com.reyun.solar.engine.OnAttributionListener
import com.reyun.solar.engine.SolarEngineConfig
import com.reyun.solar.engine.SolarEngineManager
import com.reyun.solar.engine.infos.SEAdImpEventModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.json.JSONObject
import java.math.BigDecimal
import java.util.Currency

class NsApp : android.app.Application() {

    companion object {
        lateinit var app: NsApp
            private set
        var ignoreOpen = false
        val scope by lazy {
            CoroutineScope(SupervisorJob() + Dispatchers.IO + CoroutineExceptionHandler { _, throwable ->
                Firebase.crashlytics.recordException(throwable)
            })
        }
        var lastFacebookId = ""
    }

    var obtainGoogleUpm: Boolean = false
    override fun onCreate() {
        super.onCreate()
        app = this
        MMKVHelper.initMMKV()
        try {
            FirebaseApp.initializeApp(this)
            RemoteConfigHelper.instance.init()
            NsAdHelper.instance.initialize(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        initGpUserStatus()
        initFacebookOrSolar()
        GpMessageUploadToken.upLoadToken()
        registerActivityLifecycleCallbacks(NsAppLifecycle())
        NsRecommendHelper.init()
        AffairHelper.instance.event("ns_app_start")
    }


    private fun initGpUserStatus() {
        if (MMKVHelper.plyRefStr.isNullOrEmpty()) {
            AffairHelper.Companion.instance.event("ns_gp_attr_start")
            val client = InstallReferrerClient.newBuilder(this).build()
            client.startConnection(object : InstallReferrerStateListener {
                override fun onInstallReferrerSetupFinished(responseCode: Int) {
                    if (responseCode == InstallReferrerClient.InstallReferrerResponse.OK) {
                        try {
                            if (client.isReady) {
                                client.let { client ->
                                    val response: ReferrerDetails = client.installReferrer
                                    val referrerUrl: String = response.installReferrer
                                    if (referrerUrl.isNotEmpty()) {
                                       if(BuildConfig.DEBUG) Log.d("ns_APP", "initGpUserStatus: $referrerUrl")
                                        MMKVHelper.plyRefStr = referrerUrl
                                        if (MMKVHelper.isM(referrerUrl)) {
                                            MMKVHelper.userKey = "ns_user_buy"
                                            AffairHelper.instance.event("ns_gp_user_buy")
                                            AffairHelper.instance.init()
                                        } else {
                                            AffairHelper.instance.event("ns_gp_user_ordinary")
                                        }
                                    }
                                }
                            }
                            client.endConnection()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

                override fun onInstallReferrerServiceDisconnected() {
                }
            })
        }
    }

    fun initFacebookOrSolar() {
        runCatching {
            val config = RemoteConfigHelper.instance.getFacebookConfig()
            if (config.isEmpty()) return@runCatching
            val json = JSONObject(String(Base64.decode(config, Base64.NO_WRAP)))
            val facebookId = json.optString("ns_facebook_id")
            val facebookToken = json.optString("ns_facebook_token")
            FacebookSdk.setClientToken(facebookToken)
            FacebookSdk.setApplicationId(facebookId)
            FacebookSdk.sdkInitialize(this@NsApp)
            AppEventsLogger.activateApp(this@NsApp)
            initSolar(facebookId)
        }.onFailure { error ->
            AffairHelper.instance.event("ns_fb_init_error")
        }
    }

    private fun initSolar(facebookId: String) {
        if (facebookId.isEmpty() || (lastFacebookId == facebookId && SolarEngineManager.getInstance().initialized.get())) return
        lastFacebookId = facebookId
        val solarKey = String(Base64.decode(RemoteConfigHelper.instance.solarAppKey, Base64.NO_WRAP))
        SolarEngineManager.getInstance().preInit(this, solarKey)
        val config = SolarEngineConfig.Builder().setFbAppID(facebookId).build()
        config.setOnAttributionListener(object : OnAttributionListener {
            override fun onAttributionSuccess(attribution: JSONObject) {
                val channel = attribution.optString("channel_id")
                if (channel.isNotEmpty()) {
                    MMKVHelper.reyunRefStr = channel
                    if (MMKVHelper.reyunMl(channel)) {
                        MMKVHelper.userKey = "ns_user_buy"
                        AffairHelper.instance.event("ns_solar_user_buy")
                        AffairHelper.instance.init()
                    } else {
                        AffairHelper.instance.event("ns_solar_user_ordinary")
                    }
                }
            }

            override fun onAttributionFail(errorCode: Int) {
                AffairHelper.instance.event("ns_solar_init_fail", "msg", errorCode.toString())
            }
        })

        SolarEngineManager.getInstance().initialize(this, solarKey, config) { code ->
            if (code == 0) {
                reportUninitializedSolarEvents()
                AffairHelper.instance.event("ns_solar_init_suc")
            } else {
                AffairHelper.instance.event("ns_solar_init_fail", "msg", code.toString())
            }
        }
    }

    private fun reportUninitializedSolarEvents() {
        val events = NsDBHelper.instance.solarDataDao().getAlls()
        if (events.isEmpty()) return
        val manager = SolarEngineManager.getInstance()
        for (entity in events) {
            try {
                val model = SEAdImpEventModel(
                    entity.source,
                    entity.platform,
                    entity.adType,
                    entity.adFormat,
                    entity.adUnitId,
                    entity.value,
                    entity.currency,
                    entity.isPrecache,
                    JSONObject(entity.customData)
                )
                manager.trackAdImpression(model)
                NsDBHelper.instance.solarDataDao().delete(entity)
                AffairHelper.instance.event("ns_solar_value_report")
            } catch (e: Exception) {
                AffairHelper.instance.event(
                    "ns_solar_value_report_fail",
                    "msg",
                    e.message.toString()
                )
            }
        }
    }

    fun logEvent(
        key: String,
        adType: NsAdType,
        adValue: AdValue,
        responseInfo: ResponseInfo?,
        id: String
    ) {
        dealValue(adValue, responseInfo?.mediationAdapterClassName ?: "", id)
        dealSolarValue(adValue, responseInfo, id, adType)

        val va = (adValue.valueMicros).toDouble() / 1000000.0
        AffairHelper.instance.event("ns_ad_revalue", Bundle().apply {
            putDouble(FirebaseAnalytics.Param.VALUE, va)
            putString(FirebaseAnalytics.Param.CURRENCY, "USD")
            putString("precision", adValue.precisionType.toString())
            putString("network", responseInfo?.mediationAdapterClassName ?: "")
            putString("adunitid", id)
        })

        AffairHelper.instance.event("ns_rev_${key}", Bundle().apply {
            putDouble(FirebaseAnalytics.Param.VALUE, va)
            putString(FirebaseAnalytics.Param.CURRENCY, "USD")
            putString("precision", adValue.precisionType.toString())
            putString("network", responseInfo?.mediationAdapterClassName ?: "")
            putString("adunitid", id)
        })
    }


    private fun dealValue(adValue: AdValue, network: String, unitId: String) {
        val v = (adValue.valueMicros).toDouble() / 1000000.0
        if (v < RemoteConfigHelper.instance.fbValueThreshold) return

        MMKVHelper.setDate()
        MMKVHelper.setToEveryDayRevenue(v)
        MMKVHelper.addTotalRevenue(v)

        MMKVHelper.addTotalRevenue2(v)
        MMKVHelper.addTotalRevenue2C(v)

        val total = MMKVHelper.getTotalRevenue()
        if (total > 0.01) {
            MMKVHelper.clearTotalRevenue()
            AffairHelper.instance.event("ns_rev_t_001", Bundle().apply {
                putDouble(FirebaseAnalytics.Param.VALUE, total)
                putString(FirebaseAnalytics.Param.CURRENCY, "USD")
                putString("precision", adValue.precisionType.toString())
                putString("network", network)
                putString("adunitid", unitId)
            })
        }

        dealValueImpressionFb(adValue)
    }


    private fun dealValueImpressionFb(adValue: AdValue) {
        if (FacebookSdk.isInitialized()) {
            val va = MMKVHelper.getTotalRevenue2()
            if (va > 0.0) {
                AppEventsLogger.newLogger(this)
                    .logPurchase(
                        BigDecimal.valueOf(va * RemoteConfigHelper.instance.updateFacebookAdValue),
                        Currency.getInstance(adValue.currencyCode)
                    )

                AppEventsLogger.newLogger(this)
                    .logEvent(
                        AppEventsConstants.EVENT_NAME_AD_IMPRESSION,
                        va * RemoteConfigHelper.instance.updateFacebookAdValue,
                        Bundle().apply {
                            putString(
                                AppEventsConstants.EVENT_PARAM_CURRENCY,
                                adValue.currencyCode
                            )
                        })
                MMKVHelper.clearTotalRevenue2()
            } else {
                AffairHelper.instance.event("ns_fb_rep_is_0")
            }
            val cfgV = RemoteConfigHelper.instance.getFacebookThreshold()
            if (cfgV > 0.0 && !MMKVHelper.isReportFbTotalValue2) {
                val total2c = MMKVHelper.getTotalRevenue2C()
                if (total2c > cfgV) {
                    AppEventsLogger.newLogger(this).logEvent(
                        "ns_fb_cs_max",
                        total2c,
                        Bundle().apply {
                            putString(
                                AppEventsConstants.EVENT_PARAM_CURRENCY,
                                adValue.currencyCode
                            )
                        }
                    )
                    MMKVHelper.isReportFbTotalValue2 = true
                    MMKVHelper.clearTotalRevenue2C()
                }
            }
        }
    }

    fun dealValueClickFb(adValue: AdValue?) {
        if (adValue == null) return
        val v = (adValue.valueMicros).toDouble() / 1000000.0
        MMKVHelper.addTotalRevenue3(v)
        if (FacebookSdk.isInitialized()) {
            val va = MMKVHelper.getTotalRevenue3()
            AppEventsLogger.newLogger(this)
                .logEvent(
                    AppEventsConstants.EVENT_NAME_AD_CLICK,
                    va,
                    Bundle().apply {
                        putString(AppEventsConstants.EVENT_PARAM_CURRENCY, adValue.currencyCode)
                    })
            MMKVHelper.clearTotalRevenue3()
        }
    }


    private fun dealSolarValue(
        adValue: AdValue,
        responseInfo: ResponseInfo?,
        id: String,
        adType: NsAdType
    ) {
        val va = (adValue.valueMicros).toDouble() / 1000000.0
        val t = when (adType) {
            NsAdType.INTERSTITIAL -> 3
            NsAdType.NATIVE -> 6
            NsAdType.OPEN -> 2
        }

        if (SolarEngineManager.getInstance().initialized.get()) {
            val seAdImpEventModel = SEAdImpEventModel(
                responseInfo?.loadedAdapterResponseInfo?.adSourceName,
                "admob",
                t,
                "",
                id,
                va * 1000.0,
                "USD",
                true,
                JSONObject()
            )
            SolarEngineManager.getInstance().trackAdImpression(seAdImpEventModel)
        } else {
            val solarDataModel = SolarDataInfo(
                source = responseInfo?.loadedAdapterResponseInfo?.adSourceName ?: "",
                platform = "admob",
                adType = t,
                adFormat = "",
                adUnitId = id,
                value = va * 1000.0,
                currency = "USD",
                isPrecache = true,
                customData = JSONObject().toString()
            )
            AffairHelper.instance.event("ns_rey_v_record")
            NsDBHelper.instance.solarDataDao().insertEvent(solarDataModel)
        }
    }

}