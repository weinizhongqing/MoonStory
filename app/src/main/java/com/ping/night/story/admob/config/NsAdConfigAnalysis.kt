package com.ping.night.story.admob.config

import android.util.Base64
import com.ping.night.story.admob.cache.NsAdCache
import com.ping.night.story.admob.type.NsAdInfo
import com.ping.night.story.admob.type.NsAdType
import com.ping.night.story.fbase.RemoteConfigHelper
import org.json.JSONArray
import org.json.JSONObject

object NsAdConfigAnalysis {

    const val KEY_FSP_AD_POSITION_CONFIG = "ns_ad_position_config"
    const val KEY_FSP_AD_CACHE_COUNT = "ns_ad_cache_count"
    const val KEY_FSP_AD_POSITION = "ns_ad_position"
    const val KEY_FSP_AD_POSITION_STATUS = "ns_position_status"
    const val KEY_FSP_AD_POSITION_TYPE = "ns_position_type"


    fun initConfig() {
        try {
            val json = getAdJson()
            val jsonArray = json.getJSONArray(KEY_FSP_AD_POSITION_CONFIG)
            val cacheCount = json.getJSONObject(KEY_FSP_AD_CACHE_COUNT)

            val openAdIdList = json.getJSONArray(NsAdType.OPEN.type)
            val idNatives = json.getJSONArray(NsAdType.NATIVE.type)
            val idIters = json.getJSONArray(NsAdType.INTERSTITIAL.type)
            parseAdCacheCount(cacheCount)
            parseAdPosition(jsonArray, idNatives, idIters, openAdIdList)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun parseAdPosition(
        position: JSONArray,
        idNatives: JSONArray,
        idIters: JSONArray,
        openAdIdList: JSONArray
    ) {
        for (i in 0 until position.length()) {
            val obj: JSONObject = position.getJSONObject(i)
            val key = obj.getString(KEY_FSP_AD_POSITION)

            val isEnable = obj.optInt(KEY_FSP_AD_POSITION_STATUS, -1)
            val id = mutableListOf<NsAdInfo>()
            val t = when (obj.optString(KEY_FSP_AD_POSITION_TYPE)) {
                NsAdType.NATIVE.type -> {
                    id.addAll(parseAdIdList(idNatives))
                    NsAdType.NATIVE
                }

                NsAdType.INTERSTITIAL.type -> {
                    id.addAll(parseAdIdList(idIters))
                    NsAdType.INTERSTITIAL
                }

                NsAdType.OPEN.type -> {
                    id.addAll(parseAdIdList(openAdIdList))
                    NsAdType.OPEN
                }

                else -> null
            }
            if (t != null) {
                val au = NsAdConfig(id, isEnable, t)
                NsAdCache.instance.cfgList[key] = au
            }
        }

    }

    private fun parseAdCacheCount(json: JSONObject) {
        NsAdCache.cacheInterCount = json.optInt(
            NsAdType.INTERSTITIAL.type, 1)
        NsAdCache.cacheNativeCount = json.optInt(
            NsAdType.NATIVE.type, 1)
        NsAdCache.cacheOpenCount = json.optInt(
            NsAdType.OPEN.type, 1)
    }

    private fun parseAdIdList(json: JSONArray): List<NsAdInfo> {
        val nativeAdIdList = arrayListOf<NsAdInfo>()
        for (i in 0 until json.length()) {
            val sType = json.getJSONObject(i).getString("s_type")
            val id = json.getJSONObject(i).getString("id")

            nativeAdIdList.add(
                NsAdInfo(
                    id,
                    sType
                )
            )
        }
        return nativeAdIdList
    }


    private fun getAdJson(): JSONObject {
        val s = RemoteConfigHelper.instance.getAdConfig()
        return JSONObject(String(Base64.decode(s.toByteArray(), Base64.DEFAULT)))
    }


}