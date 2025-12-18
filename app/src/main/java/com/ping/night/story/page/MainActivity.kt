package com.ping.night.story.page

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.ping.night.story.Constant
import com.ping.night.story.NsApp
import com.ping.night.story.R
import com.ping.night.story.admob.position.NsPosition
import com.ping.night.story.admob.view.CustomNativeAdView
import com.ping.night.story.admob.view.NsAdPage
import com.ping.night.story.databinding.NsHomePageBinding
import com.ping.night.story.dialog.NotifyDialog
import com.ping.night.story.dialog.SettingsDialog
import com.ping.night.story.fbase.AffairHelper
import com.ping.night.story.fbase.RemoteConfigHelper
import com.ping.night.story.helper.AppHelper
import com.ping.night.story.helper.MMKVHelper
import com.ping.night.story.helper.ToastHelper
import com.ping.night.story.helper.VideoHelper

class MainActivity : NsAdPage() {
    private val mBinding by lazy { NsHomePageBinding.inflate(layoutInflater) }

    private var complete: () -> Unit = {
        firstGuide()
    }
    private var startTime = 0L
    private var next: () -> Unit = {
        val t2 = System.currentTimeMillis()
        if (t2 - startTime < 500) {
            openNotificationSettingsPage()
        } else {
            complete.invoke()
        }
    }

    private var permission: ActivityResultLauncher<String>? = null
    private var notificationSettingsLauncher: ActivityResultLauncher<Intent>? = null


    override val currentPageNativeAd: String?
        get() = NsPosition.AD_HOME_N

    override val currentPageNativeView: CustomNativeAdView?
        get() = mBinding.homePageNativeAd

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(mBinding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        mBinding.homePageStart.setOnClickListener {
            var url = AppHelper.getClipboardContent().trim()
            if (!MMKVHelper.isVip && (!VideoHelper.isSupportedOrdinarySocialLink(url) || url.isEmpty())) {
                ToastHelper.show(this)
                return@setOnClickListener
            }
            if (url.isEmpty()) {
                url = RemoteConfigHelper.Companion.instance.getDefaultLink()
            }
            showInterAd(NsPosition.AD_CLICK_I) {
                NsAnalyActivity.startAnV(this, url)
            }
        }

        mBinding.homePageDownloading.setOnClickListener {
            showInterAd(NsPosition.AD_CLICK_I) {
                startActivity(Intent(this, NsDVingActivity::class.java))
            }
        }

        mBinding.homePageLocalVideos.setOnClickListener {
            showInterAd(NsPosition.AD_CLICK_I) {
                NsVideosActivity.startPage(this)
            }

        }

        mBinding.homePageFavorite.setOnClickListener {
            showInterAd(NsPosition.AD_CLICK_I) {
                NsFavActivity.startPage(this)
            }

        }

        mBinding.homePageSettings.setOnClickListener {
            SettingsDialog(this).show()
        }

        notifyProcessor(intent)

        permission = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            next.invoke()
        }
        notificationSettingsLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (AppHelper.hasNotifyPermission()) {
                    AffairHelper.Companion.instance.event("notify_yse")
                } else {
                    AffairHelper.Companion.instance.event("notify_no")
                }
                complete.invoke()
            }

        checkPm {
            if (it) {
                getPermission()
            } else {
                complete.invoke()
            }
        }



        mBinding.homePageRecommend.setOnClickListener {
            startActivity(Intent(this, NsRecommendActivity::class.java))
        }


    }


    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        notifyProcessor(intent)
    }


    override fun onResume() {
        super.onResume()
        if (!skipLoadNativeAd) {
            preLoadAd(NsPosition.AD_CLICK_I)
            preLoadAd(NsPosition.AD_FUNC_N)
        }

        mBinding.homePageRecommend.isVisible = MMKVHelper.isVip
    }


    private fun checkPm(callback: (Boolean) -> Unit) {
        val t = System.currentTimeMillis()
        val lastT = MMKVHelper.lastShowPDialogTime
        if (t - lastT > 24 * 60 * 60 * 1000 && !AppHelper.hasNotifyPermission()) {
            MMKVHelper.lastShowPDialogTime = t
            val dialog = NotifyDialog(this) {
                callback.invoke(it)
            }
            dialog.show()
            skipLoadNativeAd = true
        } else {
            callback.invoke(false)
        }
    }

    private fun getPermission() {
        if (AppHelper.hasNotifyPermission()) {
            startTime = 0L
            next.invoke()
            return
        }
        startTime = System.currentTimeMillis()
        permission?.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    private fun openNotificationSettingsPage() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, NsApp.Companion.app.packageName)
                }
                notificationSettingsLauncher?.launch(intent)
            } else {
                complete.invoke()
            }
        } catch (e: Exception) {
            complete.invoke()
        }
    }

    private fun firstGuide() {
        if (MMKVHelper.isVip) {
            if (MMKVHelper.isFirstOpen) {
                skipLoadNativeAd = true
                val url = RemoteConfigHelper.Companion.instance.getDefaultLink()
                NsAnalyActivity.startAnV(this, url)
                MMKVHelper.isFirstOpen = false
            }
        }
    }

    private fun notifyProcessor(intent: Intent?) {
        val status = intent?.getStringExtra(Constant.APP_RUN_STATUS_MODE_KEY) ?: "cold"
        val parseUrl = intent?.getStringExtra(Constant.NOTIFY_FCM_FORM_URL_KEY) ?: ""
        intent?.removeExtra(Constant.APP_RUN_STATUS_MODE_KEY)
        intent?.removeExtra(Constant.NOTIFY_FCM_FORM_URL_KEY)

        when (status) {
            Constant.NOTIFY_FCM_FORM_KEY -> {
                if (parseUrl.isNotEmpty()) {
                    NsAnalyActivity.startAnV(this, parseUrl)
                    skipLoadNativeAd = true
                }
            }

            Constant.DOWNLOAD_MODE_SUCCESS_KEY -> {
                NsVideosActivity.startPage(this)
                skipLoadNativeAd = true
            }

            Constant.DOWNLOAD_MODE_KEY -> {
                NsFavActivity.startPage(this)
                skipLoadNativeAd = true
            }
        }

        val shareText = intent?.getStringExtra("shareText") ?: ""
        if (shareText.isNotEmpty()) {
            skipLoadNativeAd = true
            NsAnalyActivity.startAnV(this, shareText)
        }

    }
}