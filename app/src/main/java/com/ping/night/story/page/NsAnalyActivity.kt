package com.ping.night.story.page

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.ping.night.story.Constant
import com.ping.night.story.NsApp
import com.ping.night.story.R
import com.ping.night.story.admob.NsAdHelper
import com.ping.night.story.admob.position.NsPosition
import com.ping.night.story.admob.view.CustomNativeAdView
import com.ping.night.story.admob.view.NsAdPage
import com.ping.night.story.databinding.NsVideoParsePageBinding
import com.ping.night.story.db.DownloadInfo
import com.ping.night.story.download.DownloaderHelper
import com.ping.night.story.fbase.AffairHelper
import com.ping.night.story.fbase.RemoteConfigHelper
import com.ping.night.story.helper.MMKVHelper
import com.ping.night.story.helper.VideoHelper
import com.ping.night.story.utils.AppNetUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import okhttp3.Request
import org.json.JSONObject
import java.util.UUID

class NsAnalyActivity : NsAdPage() {

    companion object {
        fun startAnV(context: Context, url: String) {
            val intent = Intent(context, NsAnalyActivity::class.java)
            intent.putExtra(Constant.VIDEO_PARSE_KEY, url)
            context.startActivity(intent)
        }
    }

    private val mBinding by lazy { NsVideoParsePageBinding.inflate(layoutInflater) }

    override val currentPageNativeAd: String?
        get() = NsPosition.AD_PARSE_N

    override val currentPageNativeView: CustomNativeAdView?
        get() = mBinding.videoParsePageAd

    override val isBackHomePageAdShow: Boolean
        get() = true
    private var downloadInfo: DownloadInfo? = null

    private var currentVideoUrl = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(mBinding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        mBinding.headPage.pageTiele.text = getString(R.string.video_analysis)
        mBinding.headPage.pageClose.setOnClickListener { onBack() }
        val url = intent.getStringExtra(Constant.VIDEO_PARSE_KEY) ?: ""
        currentVideoUrl = VideoHelper.extractUrl(url)

        mBinding.videoParsePageRetryAction.setOnClickListener {
            startVideoParse()
        }

        mBinding.videoParsePageDownloadAction.setOnClickListener {
            if (downloadInfo == null) {
                return@setOnClickListener
            }
            downloadInfo?.let {
                showInterAd(NsPosition.AD_CLICK_I) {
                    DownloaderHelper.insertTaskIfNotExist(it)
                    Log.d("downloadModel", "startDownload6: $it")
                    NsDVingActivity.starPage(this,it)
                    finish()
                }
            }
        }
        startVideoParse()
    }


    private fun startVideoParse() {
        startParseVideoView()
        val isDefaultUrl = if (currentVideoUrl.contains(
                RemoteConfigHelper.instance.getDefaultLink(),
                ignoreCase = true
            )
        ) "default" else "user"
        AffairHelper.instance.event("fsp_parse_start", "status", isDefaultUrl)

        lifecycleScope.launch(Dispatchers.IO) {
            val t1 = System.currentTimeMillis()
            val job1 = async {
                withTimeout(15000) {
                    NsAdHelper.instance.load(
                        NsPosition.AD_PARSE_I)
                }
            }

            val job2 = async {
                withTimeout(15000) {
                    val request = Request.Builder()
                        .url("${Constant.PARSE_URL_KEY}?w01=$currentVideoUrl")
                        .header("VI", NsApp.app.packageName)
                        .build()

                    runCatching {
                        val response = AppNetUtils.instance.client.newCall(request).execute()

                        if (!response.isSuccessful) {
                            AffairHelper.instance.event(
                                "ns_parse_fal",
                                "cate",
                                "${response.code}--${response.message}"
                            )
                            return@runCatching
                        }

                        val body = response.body.string()
                        if (body.isEmpty()) {
                            return@runCatching
                        }

                        val json = JSONObject(body)
                        if (json.optInt("code") != 0) {
                            return@runCatching
                        }

                        Log.d("parseVideo", "json: ${json.optJSONObject("data").optString("w05")}")

                        json.optJSONObject("data")?.let { data ->
                            downloadInfo = DownloadInfo(
                                taskId = "${UUID.randomUUID()}.mp4",
                                sourceUrl = currentVideoUrl,
                                url = data.getString("w01"),
                                title = data.optString("w03", ""),
                                img = data.optString("w05", ""),
                                blogger = data.optString("a03", ""),
                                videoDesc = data.optString("w03", ""),
                                duration = data.optString("w04", "0").toFloatOrNull() ?: 0f,
                                path = "",
                                header = data.optString("w02", "")
                            )
                        }

                    }.onFailure { error ->
                        AffairHelper.instance.event(
                            "ns_parse_fal",
                            "cate",
                            error.message ?: "unknown error"
                        )
                        null
                    }
                }
            }

            job1.await()
            job2.await()

            val t2 = System.currentTimeMillis()
            if (t2 - t1 < 2000) {
                delay(2000 - (t2 - t1))
            }

            withContext(Dispatchers.Main) {
                if (downloadInfo != null) {
                    Glide.with(this@NsAnalyActivity)
                        .load(downloadInfo!!.img)
                        .into(mBinding.videoParsePageVideoImage)
                }
                if (downloadInfo == null) {
                    if (!MMKVHelper.isVip) {
                        parseVideoFiledView()
                    } else {
                        showInterAd(NsPosition.AD_PARSE_I) {
                            parseVideoFiledView()
                        }
                    }
                } else {
                    showInterAd(NsPosition.AD_PARSE_I) {
                        AffairHelper.instance.event("parse_success_show")
                        parseVideoSuccessView()
                    }

                }
            }
        }
    }

    private fun startParseVideoView() {
        mBinding.videoParsePageAnalysisLoading.isVisible = true
        mBinding.videoParsePageLlVideoFiled.isVisible = false
        mBinding.videoParsePageLlVideoImage.isVisible = false
    }


    private fun parseVideoFiledView() {
        mBinding.videoParsePageAnalysisLoading.isVisible = false
        mBinding.videoParsePageLlVideoFiled.isVisible = true
        mBinding.videoParsePageLlVideoImage.isVisible = false
    }


    private fun parseVideoSuccessView() {
        mBinding.videoParsePageAnalysisLoading.isVisible = false
        mBinding.videoParsePageLlVideoFiled.isVisible = false
        mBinding.videoParsePageLlVideoImage.isVisible = true
    }
}