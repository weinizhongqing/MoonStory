package com.ping.night.story.page

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.ping.night.story.Constant
import com.ping.night.story.exx.registerBroadcast
import com.ping.night.story.helper.LanguageHelper

abstract class BasePage : AppCompatActivity(){

    var isAppRun = true

    open fun onBack() {
        finish()
    }


    private val languageReceiver = object : android.content.BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Constant.LANGUAGE_RADIO_KEY) {
                recreate()
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        LanguageHelper.updateLocale(this)
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBack()
            }
        })
        registerBroadcast(languageReceiver, IntentFilter( Constant.LANGUAGE_RADIO_KEY))
    }


    override fun onResume() {
        super.onResume()
        isAppRun = true
    }


    override fun onPause() {
        super.onPause()
        isAppRun = false
    }


    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(languageReceiver)
    }


}