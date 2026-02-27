package com.ping.night.story

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import com.google.android.gms.ads.AdActivity
import com.ping.night.story.admob.NsAdHelper
import com.ping.night.story.admob.position.NsPosition
import com.ping.night.story.admob.view.NsFullNativeAdActivity
import com.ping.night.story.db.DownloadInfo
import com.ping.night.story.dialog.DownloadSuccessDialog
import com.ping.night.story.helper.MMKVHelper
import com.ping.night.story.page.BasePage
import com.ping.night.story.page.MainActivity
import com.ping.night.story.page.NsOpenActivity
import kotlinx.coroutines.launch

class NsAppLifecycle : Application.ActivityLifecycleCallbacks {

    companion object {
        var startedAct = 0
            private set(value) {
                field = value
            }
        var mainIsStarted = false
        private val activeActivities = mutableListOf<Activity>()
        private fun getTopActivity(): Activity? {
            return activeActivities.lastOrNull()
        }

        fun gotoDownloadSuccess(downloadModel: DownloadInfo) {
            getTopActivity()?.let { activity ->
                if (activity is BasePage) {
                    activity.runOnUiThread {
                        DownloadSuccessDialog(
                            activity,
                            downloadModel
                        ).show()
                    }
                }
            } ?: run {
            }
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        if (activity is MainActivity) {
            mainIsStarted = true
        }
        if (NsApp.isClickNative) return
        activeActivities.add(activity)
    }

    override fun onActivityStarted(activity: Activity) {
        if (startedAct == 0) {
            val checkAc = activeActivities.last()
            if (checkAc !is NsOpenActivity && checkAc is BasePage && MMKVHelper.isVip && !hasAdActivity())
                activity.startActivity(Intent(activity, NsOpenActivity::class.java).apply {
                    putExtra(Constant.APP_RUN_STATUS_KEY, true)
                    putExtra(Constant.APP_RUN_STATUS_MODE_KEY, "hot")
                })
            NsApp.ignoreOpen = false
            NsApp.isClickNative = false
        }
        startedAct++
    }

    override fun onActivityResumed(activity: Activity) {
    }

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {
        startedAct--
        if (startedAct == 0) {
            NsApp.scope.launch {
                NsAdHelper.instance.preLoad(
                    NsPosition.AD_START)
            }
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {
        if (activity is MainActivity) {
            mainIsStarted = false
        }
        activeActivities.remove(activity)
    }


    private fun hasAdActivity(): Boolean {
        if (NsApp.isClickNative) {
            return false
        }
        for (activity in activeActivities) {
            if (activity is AdActivity || activity is NsFullNativeAdActivity) {
                return true
            }
        }
        return false
    }
}