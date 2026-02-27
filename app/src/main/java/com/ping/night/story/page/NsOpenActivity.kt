package com.ping.night.story.page

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.SystemClock
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.ping.night.story.Constant
import com.ping.night.story.NsApp
import com.ping.night.story.NsAppLifecycle
import com.ping.night.story.R
import com.ping.night.story.admob.NsAd
import com.ping.night.story.admob.NsAdHelper
import com.ping.night.story.admob.position.NsPosition
import com.ping.night.story.admob.position.NsPosition.isEnable
import com.ping.night.story.admob.view.NsAdPage
import com.ping.night.story.databinding.NsStartPageBinding
import com.ping.night.story.db.DownloadInfo
import com.ping.night.story.fbase.AffairHelper
import com.ping.night.story.fbase.RemoteConfigHelper
import com.ping.night.story.helper.MMKVHelper
import com.ping.night.story.utils.AppNetUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.Request
import org.json.JSONObject
import java.util.UUID

class NsOpenActivity : NsAdPage() {

    private val binding: NsStartPageBinding by lazy {
        NsStartPageBinding.inflate(layoutInflater)
    }


    private var isLenOrRe: Boolean = false
    private var status = "cold"

    private var shareText = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        notifyProcessor(intent = intent)
        getNotifyPM {
            umpRequest {
                loadOpenAd()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        notifyProcessor(intent = intent)
    }

    private fun notifyProcessor(intent: Intent?){
        isLenOrRe = intent?.getBooleanExtra(Constant.APP_RUN_STATUS_KEY,false) ?: false
        status = intent?.getStringExtra(Constant.APP_RUN_STATUS_MODE_KEY)?: "cold"
        val notifyId = intent?.getIntExtra(Constant.NOTIFICATION_ID_KEY, -1) ?: -1
        val notificationManager = NotificationManagerCompat.from(this)
        when (status) {
            Constant.NOTIFY_FCM_FORM_KEY -> {
                notificationManager.cancel(notifyId)
                AffairHelper.instance.event("click_fcm_message_notify", "msg", notifyId.toString())
            }

            Constant.DOWNLOAD_MODE_SUCCESS_KEY -> {
                AffairHelper.instance.event("click_download_suc_notify")
                notificationManager.cancel(notifyId)
            }

            Constant.DOWNLOAD_MODE_KEY -> {
                AffairHelper.instance.event("click_downloading_notify")
                notificationManager.cancel(notifyId)
            }
        }

        if (Intent.ACTION_SEND == intent?.action && intent.type != null) {
            shareText = intent.getStringExtra(Intent.EXTRA_TEXT) ?: ""
        }

        AffairHelper.instance.event("run_app_status", "msg", if (isLenOrRe) "hot" else "cold")
    }

    private fun getNotifyPM(complete: () -> Unit) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        ) {
            complete.invoke()
            return
        }
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            complete.invoke()
        }.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    private fun umpRequest(complete: () -> Unit){
        if (NsApp.app.obtainGoogleUpm) {
            complete.invoke()
            return
        }
        val builder = ConsentRequestParameters.Builder()
        val params = builder.build()
        val consentInformation = UserMessagingPlatform.getConsentInformation(this)
        consentInformation.requestConsentInfoUpdate(this, params, {
            UserMessagingPlatform.loadAndShowConsentFormIfRequired(this) { _ ->
                complete.invoke()
                NsApp.app.obtainGoogleUpm = true
                if (consentInformation.canRequestAds()) {
                    try {
                        MobileAds.initialize(NsApp.app)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }, {
            complete.invoke()
        })
    }


    private fun loadOpenAd(){
        lifecycleScope.launch{
            val t1 = System.currentTimeMillis()
            withTimeoutOrNull(3000){
                RemoteConfigHelper.instance.awaitComplete()
            }
            val t2 = System.currentTimeMillis()

            if (t2 - t1 < 1000){
                delay(1000 - (t2 - t1))
            }
            if (NsPosition.AD_START.isEnable()) {
                AffairHelper.instance.event("arrive_ad_${NsPosition.AD_START}")
            }
            preLoadNextViewAd()
            launch {
                val ad = NsAdHelper.instance.get(NsPosition.AD_START)
                if (ad != null) {
                    showOpenAd(ad)
                } else {
                    val loadStart = System.currentTimeMillis()
                    withTimeoutOrNull(12000) {
                        NsAdHelper.instance.preLoad(NsPosition.AD_START)
                    }
                    val loadTime = (System.currentTimeMillis() - loadStart) / 1000
                    AffairHelper.instance.event("start_load_t", "msg", loadTime.toString())
                    val fallbackAd = NsAdHelper.instance.get(NsPosition.AD_START)
                    showOpenAd(fallbackAd)
                }
            }
        }
    }


    private fun preLoadNextViewAd(){
        if (MMKVHelper.isShowLanguage){
            preLoadAd(NsPosition.AD_LANGUAGE_I)
            preLoadAd(NsPosition.AD_LANGUAGE_N)
        }else{
            preLoadAd(NsPosition.AD_CLICK_I)
            preLoadAd(NsPosition.AD_HOME_N)
        }
    }

    private fun showOpenAd(openAd: NsAd?){
        Log.d("NsOpenActivity", "showOpenAd: ")
        if (!isAppRun) {
            finish()
            return
        }
        val adPosition = NsPosition.AD_START
        if (openAd == null){
            skipNextView()
            return
        }
        openAd.apply {
            AffairHelper.instance.event("fill_ad_$adPosition")
            this.onClose { skipNextView() }
            this.onClick { AffairHelper.instance.event("click_ad_$adPosition") }
            this.onShow {
                AffairHelper.instance.event("show_ad_$adPosition")
                lifecycleScope.launch {
                    NsAdHelper.instance.preLoad(
                        NsPosition.AD_START)
                }
            }
            this.show(adPosition, this@NsOpenActivity)
        }
    }




    private fun skipNextView(){
        if (!isLenOrRe && NsAppLifecycle.startedAct > 0) {
            if (MMKVHelper.isShowLanguage){
                startActivity(Intent(this, NsLanguageActivity::class.java))
            }else{
                startActivity( Intent(this, MainActivity::class.java).apply {
                    intent?.extras?.let { putExtras(it) }
                    if (shareText.isNotEmpty()) {
                        putExtra("shareText", shareText)
                    }
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                })
            }
        }
        finish()
    }


    override fun onDestroy() {
        super.onDestroy()
    }

}