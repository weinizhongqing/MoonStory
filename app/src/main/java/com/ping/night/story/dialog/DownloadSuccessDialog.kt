package com.ping.night.story.dialog

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.ping.night.story.R
import com.ping.night.story.admob.NsAdHelper
import com.ping.night.story.admob.position.NsPosition
import com.ping.night.story.admob.position.NsPosition.isEnable
import com.ping.night.story.admob.view.CustomNativeAdView
import com.ping.night.story.databinding.NsDialogDownloadSucBinding
import com.ping.night.story.db.DownloadInfo
import com.ping.night.story.fbase.AffairHelper
import com.ping.night.story.helper.VideoHelper
import com.ping.night.story.page.BasePage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DownloadSuccessDialog(val activity: BasePage, private val downloadInfo: DownloadInfo) :
    com.google.android.material.bottomsheet.BottomSheetDialog(activity, R.style.dialog_bottom_sheet_style) {
    private val binding by lazy { NsDialogDownloadSucBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        Glide.with(activity)
            .load(downloadInfo.img)
            .into(binding.videoParsePageVideoImage)
        binding.videoParsePageVideoImage.setOnClickListener {
            AffairHelper.instance.event("down_suc_dialog_click")
            dismiss()
            VideoHelper.playVideo(context, downloadInfo.path)
        }

        binding.videoParsePagePlayAction.setOnClickListener {
            AffairHelper.Companion.instance.event("down_suc_dialog_click")
            dismiss()
            VideoHelper.playVideo(context, downloadInfo.path)
        }
        showNativeAd(NsPosition.AD_DOWNLOAD_SUC_N,binding.dialogDownloadSucNativeAd)
    }


    private fun showNativeAd(position: String, customNativeAdView: CustomNativeAdView) {
        if (!position.isEnable()) {
            return
        }
        activity.lifecycleScope.launch {
            if (NsAdHelper.instance.get(position)==null){
                val t1 = System.currentTimeMillis()
                NsAdHelper.instance.load(position)
                val t2 = System.currentTimeMillis()
                if (t2 - t1 < 480) {
                    delay((480 - (t2 - t1)))
                }
            }
            withContext(Dispatchers.Main) {
                if (activity.isAppRun) {
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

    override fun onStart() {
        super.onStart()
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }



}