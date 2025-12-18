package com.ping.night.story.page

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.recyclerview.widget.LinearLayoutManager
import com.ping.night.story.R
import com.ping.night.story.admob.position.NsPosition
import com.ping.night.story.admob.view.CustomNativeAdView
import com.ping.night.story.admob.view.NsAdPage
import com.ping.night.story.databinding.NsLocalVideosPageBinding
import com.ping.night.story.db.LocalVideoInfo
import com.ping.night.story.db.NsDBHelper
import com.ping.night.story.helper.VideoHelper
import com.ping.night.story.page.adapter.VideoAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class NsVideosActivity : NsAdPage() {

    companion object {
        fun startPage(context: Context) {
            val intent = Intent(context, NsVideosActivity::class.java)
            context.startActivity(intent)
        }
    }

    private val binding by lazy { NsLocalVideosPageBinding.inflate(layoutInflater) }

    override val currentPageNativeAd: String?
        get() = NsPosition.AD_FUNC_N

    override val currentPageNativeView: CustomNativeAdView?
        get() = binding.localVideoPageAd

    override val isBackHomePageAdShow: Boolean
        get() = true

    private val mVideoAdapter by lazy {
        VideoAdapter(this, {
            showInterAd(NsPosition.AD_CLICK_I) {
                VideoHelper.playVideo(this, it)
            }
        }, {
            //like
            updateLike(it)
        })
    }

    private var videoList: Flow<PagingData<LocalVideoInfo>> =
        queryDownloadedPager().cachedIn(lifecycleScope).flowOn(Dispatchers.IO)

    private fun queryDownloadedPager(): Flow<PagingData<LocalVideoInfo>> {
        return Pager(
            PagingConfig(pageSize = 50)
        ) {
            NsDBHelper.instance.localVideoDao().getAllDownloadedPaging()
        }.flow
    }


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
        binding.headPage.pageTiele.text = getString(R.string.local_videos)
        binding.localVideoPageRv.layoutManager = LinearLayoutManager(this)
        binding.localVideoPageRv.adapter = mVideoAdapter

        mVideoAdapter.addLoadStateListener {
            when (it.refresh) {
                is LoadState.NotLoading -> {
                    if (mVideoAdapter.itemCount > 0) {
                        binding.localVideoPageMap.isVisible = false
                        binding.localVideoPageRv.isVisible = true

                    } else {
                        binding.localVideoPageMap.isVisible = true
                        binding.localVideoPageRv.isVisible = false
                    }
                }

                is LoadState.Error -> {
                    binding.localVideoPageMap.isVisible = true
                    binding.localVideoPageRv.isVisible = false
                }

                else -> {}
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                videoList.collectLatest {
                    mVideoAdapter.submitData(it)
                }
            }
        }
    }


    private fun updateLike(video: LocalVideoInfo) {
        lifecycleScope.launch(Dispatchers.IO) {
            val status = if (video.likeStatus == 1) {
                0
            } else {
                1
            }
            NsDBHelper.instance.localVideoDao().updateLike(video.id, status)
        }
    }
}