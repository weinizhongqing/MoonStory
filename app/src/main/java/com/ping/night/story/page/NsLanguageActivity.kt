package com.ping.night.story.page

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ping.night.story.R
import com.ping.night.story.admob.position.NsPosition
import com.ping.night.story.admob.view.CustomNativeAdView
import com.ping.night.story.admob.view.NsAdPage
import com.ping.night.story.databinding.NsLanguagePageBinding
import com.ping.night.story.exx.dip2px
import com.ping.night.story.helper.LanguageHelper
import com.ping.night.story.helper.MMKVHelper
import com.ping.night.story.page.adapter.LanguageAdapter

class NsLanguageActivity : NsAdPage() {

    private val binding by lazy { NsLanguagePageBinding.inflate(layoutInflater) }

    val isShow = !(MMKVHelper.isShowLanguage && MMKVHelper.isVip)

    private var selectCode = "en"

    private val adapter: LanguageAdapter by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        LanguageAdapter(
            if (!isShow) "" else LanguageHelper.getSelectLanguageCode()
                .orEmpty(), {
                selectCode = it
                binding.languagePageOkAction.isVisible = true
            })
    }


    override val currentPageNativeAd: String?
        get() = NsPosition.AD_LANGUAGE_N

    override val currentPageNativeView: CustomNativeAdView?
        get() = binding.languagePageAd

    override val isBackHomePageAdShow: Boolean
        get() = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.headPage.pageTiele.text = getString(R.string.language)
        binding.headPage.pageClose.setOnClickListener { onBack() }
        binding.headPage.pageClose.isVisible = !MMKVHelper.isShowLanguage
        binding.languagePageOkAction.isVisible = isShow
        val layoutManager = GridLayoutManager(this,2)
        binding.languagePageRv.layoutManager = layoutManager
        binding.languagePageRv.addItemDecoration(object :
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
        binding.languagePageRv.adapter = adapter

        binding.languagePageOkAction.setOnClickListener {
            if (selectCode.isNotEmpty()) {
                showInterAd(NsPosition.AD_LANGUAGE_I) {
                    LanguageHelper.switchLanguage(selectCode)
                    skipNextView()
                }
            }
        }

        if (MMKVHelper.isShowLanguage) {
            preLoadAd(NsPosition.AD_CLICK_I)
            preLoadAd(NsPosition.AD_HOME_N)
        }
    }

    private fun skipNextView() {
        if (MMKVHelper.isShowLanguage) {
            startActivity(Intent(this, MainActivity::class.java).apply {
                intent.extras?.let {
                    putExtras(it)
                }
            })
            MMKVHelper.isShowLanguage = false
            finish()
        } else {
            finish()
        }
    }
}