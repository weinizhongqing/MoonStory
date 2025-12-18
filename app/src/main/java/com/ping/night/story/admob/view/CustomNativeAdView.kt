package com.ping.night.story.admob.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.ping.night.story.R
import com.ping.night.story.databinding.NsNativeAdBigPageBinding
import com.ping.night.story.databinding.NsNativeAdFullScreenPageBinding
import com.ping.night.story.databinding.NsNativeAdSmallPageBinding

class CustomNativeAdView: FrameLayout{

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        attrs?.let {
            val typed = resources.obtainAttributes(it, R.styleable.CustomNativeAdView)
            colorType = typed.getInt(R.styleable.CustomNativeAdView_content_color, 1)
            typed.recycle()
        }
    }

    var colorType = 1

    private var nativeAd: NativeAd? = null

    private var mFullBinding: NsNativeAdFullScreenPageBinding? = null

    fun showFullAd(nativeAd: NativeAd?, close: () -> Unit) {
        if (nativeAd == null) return
        this.nativeAd?.destroy()
        this.nativeAd = nativeAd
        mFullBinding = null
        val bindingView = createViewFull()
        mFullBinding = bindingView
        val view = bindingView.root
        view.bodyView?.isVisible = false
        view.callToActionView?.isVisible = false
        view.iconView?.isVisible = false

        nativeAd.icon?.let {
            view.iconView?.isVisible = true
            (view.iconView as ImageView).setImageDrawable(it.drawable)
        }

        nativeAd.mediaContent?.let {
            view.mediaView?.setMediaContent(it)
        }

        (view.headlineView as TextView).apply {
            this.text = nativeAd.headline
        }
        nativeAd.body?.let {
            view.bodyView?.isVisible = true
            (view.bodyView as TextView).apply {
                this.text = it
            }
        }

        nativeAd.callToAction?.let {
            view.callToActionView?.isVisible = true
            (view.callToActionView as TextView).text = it
        }

        view.setNativeAd(nativeAd)
        removeAllViews()
        addView(view)

        bindingView.imageClose.setOnClickListener {
            close()
        }
    }

    fun updateCountdown(time: Int, complete: Boolean) {
        mFullBinding?.showCountdown?.text = "${time}s"
        if (complete) {
            mFullBinding?.showCountdown?.isVisible = false
            mFullBinding?.imageClose?.isVisible = true
        }

    }


    fun showAdView(nativeAd: NativeAd) {
        this.nativeAd?.destroy()
        this.nativeAd = nativeAd
        val view = getView()

        view.bodyView?.isVisible = false
        view.callToActionView?.isVisible = false
        view.iconView?.isVisible = false

        nativeAd.icon?.let {
            view.iconView?.isVisible = true
            (view.iconView as ImageView).setImageDrawable(it.drawable)
        }

        nativeAd.mediaContent?.let {
            view.mediaView?.setMediaContent(it)
        }

        (view.headlineView as TextView).apply {
            this.text = nativeAd.headline
        }
        nativeAd.body?.let {
            view.bodyView?.isVisible = true
            (view.bodyView as TextView).apply {
                this.text = it
            }
        }

        nativeAd.callToAction?.let {
            view.callToActionView?.isVisible = true
            (view.callToActionView as TextView).text = it
        }

        view.setNativeAd(nativeAd)
        removeAllViews()
        addView(view)
    }


    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        nativeAd?.destroy()
        nativeAd = null
    }

    fun getView(): NativeAdView {
        return if (_root_ide_package_.com.ping.night.story.helper.MMKVHelper.isVip) {
            createViewVIP()
        } else {
            createViewOrdinary()
        }
    }


    private fun createViewOrdinary(): NativeAdView {
        val binding = NsNativeAdSmallPageBinding.inflate(LayoutInflater.from(context))
        val view = binding.root
        view.iconView = binding.nativeLogoImage
        view.mediaView = binding.nativeMediaView
        view.headlineView = binding.nativeTitleText
        view.bodyView = binding.nativeDescText
        view.callToActionView = binding.nativeAdAction
        return view
    }

    private fun createViewVIP(): NativeAdView {
        val binding = NsNativeAdBigPageBinding.inflate(LayoutInflater.from(context))
        val view = binding.root
        view.iconView = binding.nativeLogoImage
        view.mediaView = binding.nativeMediaView
        view.headlineView = binding.nativeTitleText
        view.bodyView = binding.nativeDescText
        view.callToActionView = binding.nativeAdAction
        return view
    }

    private fun createViewFull(): NsNativeAdFullScreenPageBinding {
        val binding = NsNativeAdFullScreenPageBinding.inflate(LayoutInflater.from(context))
        val view = binding.root
        view.iconView = binding.nativeLogoImage
        view.mediaView = binding.nativeMediaView
        view.headlineView = binding.nativeTitleText
        view.bodyView = binding.nativeDescText
        view.callToActionView = binding.nativeAdAction
        return binding
    }


}