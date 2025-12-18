package com.ping.night.story.page

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ping.night.story.Constant
import com.ping.night.story.R
import com.ping.night.story.admob.position.NsPosition
import com.ping.night.story.admob.view.CustomNativeAdView
import com.ping.night.story.admob.view.NsAdPage
import com.ping.night.story.databinding.NsDownloadingPageBinding
import com.ping.night.story.db.DownloadInfo
import com.ping.night.story.download.DownloaderHelper
import com.ping.night.story.exx.dip2px
import com.ping.night.story.page.adapter.DownloadingAdapter

class NsDVingActivity : NsAdPage(), DownloaderHelper.DownloadCallback {

    companion object {
        fun starPage(context: Context, downloadInfo: DownloadInfo? = null) {
            context.startActivity(Intent(context, NsDVingActivity::class.java).apply {
                putExtra(Constant.DOWNLOAD_MODEL_KEY, downloadInfo)
            })
        }
    }


    private val binding: NsDownloadingPageBinding by lazy {
        NsDownloadingPageBinding.inflate(layoutInflater)
    }

    override val currentPageNativeAd: String?
        get() = NsPosition.AD_FUNC_N

    override val currentPageNativeView: CustomNativeAdView?
        get() = binding.downloadingPageAd

    override val isBackHomePageAdShow: Boolean
        get() = true

    private var adapter: DownloadingAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.headPage.pageClose.setOnClickListener { onBack() }
        binding.headPage.pageTiele.text = getString(R.string.downloading)
        adapter = DownloadingAdapter(this)
        val layoutManager = GridLayoutManager(this,2)
        binding.downloadingPageRv.layoutManager = layoutManager
        binding.downloadingPageRv.adapter = adapter
        binding.downloadingPageRv.addItemDecoration(object :
            RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: android.graphics.Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                val spacing = dip2px(8)
                outRect.left = spacing
                outRect.right = spacing
                outRect.top = spacing
                outRect.bottom = spacing
            }
        })

        DownloaderHelper.callback = this

        DownloaderHelper.liveData.observe(this) {
            updateData(it)
        }

        DownloaderHelper.init()

        val downloadModel = intent.getParcelableExtra<DownloadInfo>(
            Constant.DOWNLOAD_MODEL_KEY)
        if (downloadModel != null) {
            Log.d("downloadModel", "startDownload5: $downloadModel")
            DownloaderHelper.start(downloadModel)
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun updateData(data: List<DownloadInfo>) {
        adapter?.submitList(data)
        adapter?.notifyDataSetChanged()
        binding.downloadingPageMap.isVisible = data.isEmpty()
        binding.downloadingPageRv.isVisible = data.isNotEmpty()
    }

    override fun onDownloadChanged(info: DownloadInfo) {
        Log.d("DownloadMessage", "onDownloadChanged: refreshData")
        adapter?.refreshData(info)
    }

    override fun onDestroy() {
        super.onDestroy()
        DownloaderHelper.callback = null
    }


}