package com.ping.night.story.admob.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.ping.night.story.R
import com.ping.night.story.admob.NsAdHelper
import com.ping.night.story.databinding.NsFullScreenPageBinding
import com.ping.night.story.fbase.RemoteConfigHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean

class NsFullNativeAdActivity : androidx.appcompat.app.AppCompatActivity() {

    companion object {
        var adClose: (() -> Unit)? = null
        fun show(activity: Activity, close: () -> Unit) {
            adClose = close
            val intent = Intent(activity, NsFullNativeAdActivity::class.java)
            activity.startActivity(intent)
        }
    }

    private lateinit var mBindingView: NsFullScreenPageBinding

    private val mCountdown = Countdown()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        mBindingView = NsFullScreenPageBinding.inflate(layoutInflater)
        setContentView(mBindingView.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )
            insets
        }

        mBindingView.adContainerView.showFullAd(NsAdHelper.instance.tempNativeFAd!!) {
            if (mCountdown.isRunning()) {
                return@showFullAd
            }
            finish()
            adClose?.invoke()
        }

        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
            }
        })
        startAdCountdown()
    }

    private fun startAdCountdown() {
        if (RemoteConfigHelper.instance.showNativeCountdown<=0){
            mBindingView.adContainerView.updateCountdown(0, true)
        }else{
            mCountdown.start(RemoteConfigHelper.instance.showNativeCountdown, {
                mBindingView.adContainerView.updateCountdown(it.toInt(), false)
            }) {
                mBindingView.adContainerView.updateCountdown(0, true)
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        NsAdHelper.instance.tempNativeFAd = null
        mCountdown.stop()
    }

    inner class Countdown {
        private var job: Job? = null
        private val isRunning = AtomicBoolean(false)

        /**
         * 开始倒计时
         * @param totalSeconds 总秒数
         * @param onTick 每秒回调，参数为剩余秒数
         * @param onFinish 倒计时结束回调
         */
        fun start(
            totalSeconds: Long, onTick: (Long) -> Unit = {}, onFinish: () -> Unit = {}
        ) {
            if (isRunning.get()) return

            isRunning.set(true)
            job = CoroutineScope(Dispatchers.Default).launch {
                var remainingSeconds = totalSeconds

                while (remainingSeconds > 0 && isRunning.get()) {
                    withContext(Dispatchers.Main) {
                        onTick(remainingSeconds)
                    }
                    delay(1000)
                    remainingSeconds--
                }

                if (isRunning.get()) {
                    withContext(Dispatchers.Main) {
                        onFinish()
                    }
                }
                isRunning.set(false)
            }
        }

        /**
         * 停止倒计时
         */
        fun stop() {
            isRunning.set(false)
            job?.cancel()
            job = null
        }

        /**
         * 检查是否正在运行
         */
        fun isRunning(): Boolean = isRunning.get()
    }
}