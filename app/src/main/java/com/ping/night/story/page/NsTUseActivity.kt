package com.ping.night.story.page

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.ping.night.story.Constant
import com.ping.night.story.R
import com.ping.night.story.admob.view.NsAdPage
import com.ping.night.story.databinding.NsGuidePageBinding
import com.ping.night.story.fbase.RemoteConfigHelper
import com.ping.night.story.helper.MMKVHelper


class NsTUseActivity : NsAdPage() {
    private val binding by lazy { NsGuidePageBinding.inflate(layoutInflater) }

    private var isFirstOpen = false

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
        binding.headPage.pageClose.setOnClickListener { onBack() }
        binding.headPage.pageTiele.text = getString(R.string.how_to_use)

        binding.guidePageBottomAction.setOnClickListener {
            if (isFirstOpen) {
                startActivity(Intent(this, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    if (MMKVHelper.isVip){
                        putExtra(Constant.APP_RUN_STATUS_MODE_KEY, Constant.NOTIFY_FCM_FORM_KEY)
                        putExtra(Constant.NOTIFY_FCM_FORM_URL_KEY, RemoteConfigHelper.instance.getDefaultLink())
                    }
                })
                isFirstOpen = false
                finish()
            }else{
                isFirstOpen = true
                binding.imgStep2.setImageResource(R.mipmap.img_step_3)
                binding.guidePageBottomActionText.text = getString(R.string.ok)
            }
        }




    }
}