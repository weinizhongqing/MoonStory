package com.ping.night.story.page

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.ping.night.story.R
import com.ping.night.story.admob.position.NsPosition
import com.ping.night.story.admob.view.CustomNativeAdView
import com.ping.night.story.admob.view.NsAdPage
import com.ping.night.story.databinding.NsRecommendedPageBinding
import com.ping.night.story.db.NsDBHelper
import com.ping.night.story.db.NsVRecommend
import com.ping.night.story.helper.VideoHelper
import com.ping.night.story.page.adapter.NsRecommendDetailAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class NsRecommendActivity : NsAdPage(), NsRecommendDetailAdapter.Callback {

    private val binding by lazy { NsRecommendedPageBinding.inflate(layoutInflater) }

    private val recommendVideoList: Flow<List<NsVRecommend>> =
        NsDBHelper.Companion.instance.linkVRecommendDao().reVideo()
            .flowOn(Dispatchers.IO) // 指定执行线程

    private val mAdapter by lazy { NsRecommendDetailAdapter() }

    override val currentPageNativeAd: String?
        get() = NsPosition.AD_FUNC_N

    override val currentPageNativeView: CustomNativeAdView?
        get() = binding.recommendedPageAd

    override val isBackHomePageAdShow: Boolean
        get() = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        binding.headPage.pageClose.setOnClickListener { onBack() }
        binding.headPage.pageTiele.text = getString(R.string.recommended)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.recommendedPageRv.layoutManager = GridLayoutManager(this, 2)
        binding.recommendedPageRv.adapter = mAdapter
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                recommendVideoList.collectLatest {
                    mAdapter.submitList(it)
                }
            }
        }
        mAdapter.callback = this
        preLoadAd(NsPosition.AD_CLICK_I)
    }

    override fun onItemClick(video: NsVRecommend) {
        if (video.canOpen()) {
            showInterAd(NsPosition.AD_CLICK_I) {
                VideoHelper.playVideo(this, video.filePath)
            }
        } else {
            NsAnalyActivity.startAnV(this, video.url)
            finish()
        }
    }

}