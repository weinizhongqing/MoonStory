package com.ping.night.story.fbase.fcm

import android.util.Base64
import android.util.Log
import com.ping.night.story.fbase.AffairHelper
import com.ping.night.story.fbase.RemoteConfigHelper
import com.ping.night.story.helper.AppHelper
import com.ping.night.story.helper.MMKVHelper
import com.ping.night.story.helper.NsRecommendHelper
import org.json.JSONArray
import java.util.Calendar

class GpMessageHelper {

    companion object{
        val instance : GpMessageHelper by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { GpMessageHelper() }
        const val DATA_RANGE_KEY = "date_range"
        const val VIDEO_INFO_KEY = "video_info"
        const val TS_KEY = "ts"
        const val OR_URL_KEY = "or_url"
        const val COVER_KEY = "cover"
    }
    fun handleTime(tsString: String){
        val ts = tsString.toLongOrNull() ?: return
        if (MMKVHelper.firstFcmTime == -1L) {
            MMKVHelper.firstFcmTime = ts
        } else {
            val elapsed = ts - MMKVHelper.firstFcmTime
            val vipLimitSeconds = RemoteConfigHelper.instance.stateTransitionTime * 3600
            if (!MMKVHelper.isVip && elapsed > vipLimitSeconds) {
                MMKVHelper.isAutoVip = true
            }
        }
    }
    fun handleMessage(data: Map<String, String>){
        if (data.isEmpty()) return
        val rangeString = data[DATA_RANGE_KEY]
        if (!rangeString.isNullOrBlank()) {
            val range = parseRange(rangeString)
            if (range != null && !isCurrentHourInRange(range)) {
                return
            }
        }
        val videoInfo = data[VIDEO_INFO_KEY]
        if (videoInfo.isNullOrEmpty()){
            return
        }

        if (AppHelper.hasNotifyPermission()) {
            AffairHelper.instance.event("ns_get_fcm_has_permission")
            if (!MMKVHelper.isVip){
                return
            }
            try {
                val jsonStr = String(Base64.decode(videoInfo, Base64.NO_WRAP))
                Log.d("GpMessageService_log", "handleMessage: $jsonStr")

                if (jsonStr.isEmpty()) return
                val listFcm = parseJsonArray(jsonStr)
                Log.d("GpMessageService_log", "listFcm: ${listFcm.size}")
                NsRecommendHelper.insertList(listFcm)
                GpSendMessageHelper.instance.sendMessage(listFcm)
            } catch (e: Exception) {
                AffairHelper.instance.event(
                    "ns_get_fcm_parse_json_error",
                    "msg",e.message.toString()
                )
            }
        }
    }
    private fun parseRange(input: String): IntRange? {
        val nums = Regex("\\d+").findAll(input).map { it.value.toInt() }.toList()
        return if (nums.size == 2) nums[0] until nums[1] else null
    }
    private fun isCurrentHourInRange(range: IntRange): Boolean {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return hour in range
    }
    private fun parseJsonArray(jsonArrayString: String): List<Pair<String, String>> {
        val result = mutableListOf<Pair<String, String>>()
        try {
            val jsonArray = JSONArray(jsonArrayString)
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val key = jsonObject.getString(OR_URL_KEY)
                val value = jsonObject.getString(COVER_KEY)
                result.add(key to value)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

}