package com.ping.night.story.helper

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Build
import android.util.DisplayMetrics
import com.ping.night.story.Constant
import com.ping.night.story.NsApp
import com.ping.night.story.R
import com.ping.night.story.info.LanguageInfo
import java.util.Locale

object LanguageHelper {

    fun getSelectLanguageCode(): String? {
        return MMKVHelper.selectLanCode
    }

    fun getCurrentLan(): Locale {
        val selectCode = getSelectLanguageCode()
        if (selectCode.isNullOrEmpty()) {
            // 加载程序支持的语言
            val lanArray = NsApp.app.resources.getStringArray(R.array.lan_code)
            val sysLanguageList = mutableListOf<Locale>()
            val config = Resources.getSystem().configuration
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                for (i in 0 until config.locales.size()) {
                    sysLanguageList.add(config.locales[i])
                }
            } else {
                @Suppress("DEPRECATION")
                sysLanguageList.add(config.locale)
            }

            for (sysLocale in sysLanguageList) {
                val sysLanguage = sysLocale.language
                val sysCountry = sysLocale.country.lowercase(Locale.US)

                // 找出最相配（语言和国家都相同）和一般相配（只有语言相同）的语言代码
                var first = ""// 最相配语言代码（语言和国家完全匹配）
                var second = ""// 一般相配语言代码（语言匹配，国家不匹配）
                for (appLocale in lanArray) {
                    if (appLocale.startsWith(sysLanguage)) {
                        if (appLocale.endsWith(sysCountry)) {
                            first = appLocale
                            break
                        } else {
                            second = appLocale
                        }
                    }
                }

                // 优先使用语言和国家都完全匹配的语言代码，
                // 其次选择语言匹配的语言代码
                val code = first.ifEmpty {
                    second.ifEmpty {
                        ""
                    }
                }
                if (code.isEmpty()) {
                    continue
                }

                val (language, country) = if (code.contains("_")) {
                    code.split("_".toRegex(), 2).map { it.trim() }
                } else {
                    listOf(code, "")
                }
                MMKVHelper.selectLanCode = code
                // 根据选择出的language和country构建新的Locale
                return Locale(language, country.uppercase(Locale.US))
            }

            if (MMKVHelper.selectLanCode.isNullOrEmpty()){
                MMKVHelper.selectLanCode = "en"
            }
        } else {
            val (language, country) = if (selectCode.contains("_")) {
                selectCode.split("_".toRegex(), 2).map { it.trim() }
            } else {
                listOf(selectCode, "")
            }
            // 根据选择出的language和country构建新的Locale
            return Locale(language, country.uppercase(Locale.US))
        }
        // 如果没有找到匹配的语言则使用英语
        return Locale.ENGLISH
    }

    fun updateLocale(context: Context?) {
        updateLocale(context, getCurrentLan())
    }

    private fun updateLocale(context: Context?, newLocale: Locale): Context? {
        if (context != null) {
            val res = context.resources
            val config = res.configuration
            config.setLocale(newLocale)

            val dm = res.displayMetrics
            val oldValue = DisplayMetrics()
            oldValue.setTo(dm)

            @Suppress("DEPRECATION")
            res.updateConfiguration(config, dm)
            res.displayMetrics.setTo(oldValue)
        }

        return context
    }

    fun switchLanguage(code: String?) {
        if (code.isNullOrEmpty()) return
        val selectCode = getSelectLanguageCode()
        if (selectCode != code) {
            MMKVHelper.selectLanCode = code
            updateLocale(NsApp.app)
            NsApp.app.sendBroadcast(Intent(
                Constant.LANGUAGE_RADIO_KEY
            ).apply {
                `package` = NsApp.app.packageName
            })
        }
    }

    fun getLanguageList(context: Context): MutableList<LanguageInfo> {
        val codes = context.resources.getStringArray(R.array.lan_code)
        val names = context.resources.getStringArray(R.array.lan_name)
        val languages = mutableListOf<LanguageInfo>()
        for (i in codes.indices) {
            languages.add(
                LanguageInfo(
                    i,
                    codes[i],
                    names[i]
                )
            )
        }
        return languages
    }


    fun getCurrentLanguageName(): String{
        val selectCode = getSelectLanguageCode()
        if (selectCode.isNullOrEmpty()) return ""
        val languageList = getLanguageList(NsApp.app)
        for (language in languageList) {
            if (language.code == selectCode) {
                return language.name
            }
        }
        return ""
    }

}