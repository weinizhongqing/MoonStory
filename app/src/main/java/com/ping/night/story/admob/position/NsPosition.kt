package com.ping.night.story.admob.position

import com.ping.night.story.admob.cache.NsAdCache
import com.ping.night.story.helper.MMKVHelper

object NsPosition {

    const val AD_START = "ns_start"
    const val AD_LANGUAGE_I = "ns_language_i"
    const val AD_LANGUAGE_N = "ns_language_n"
    const val AD_HOME_N: String = "ns_home_n"
    const val AD_CLICK_I: String = "ns_click_i"
    const val AD_BACK_I: String = "ns_back_i"
    const val AD_PARSE_I: String = "ns_parse_i"
    const val AD_PARSE_N: String = "ns_parse_n"
    const val AD_FUNC_N = "ns_func_n"
    const val AD_DOWNLOAD_SUC_N: String = "ns_download_suc_n"


    fun String.isEnable(): Boolean {
        val t = NsAdCache.instance.getAu(this) ?: return false
        return when (t.isEnable) {
            0 -> {
                true
            }
            1 -> {
                MMKVHelper.isVip
            }
            else -> {
                false
            }
        }
    }

}