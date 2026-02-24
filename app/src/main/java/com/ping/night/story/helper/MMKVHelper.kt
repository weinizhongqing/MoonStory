package com.ping.night.story.helper

import android.util.Base64
import com.ping.night.story.BuildConfig
import com.ping.night.story.NsApp
import com.ping.night.story.exx.getDate
import com.ping.night.story.fbase.RemoteConfigHelper
import com.reyun.solar.engine.SolarEngineManager
import com.tencent.mmkv.MMKV
import org.json.JSONArray

object MMKVHelper {


    private val mmkv by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { MMKV.defaultMMKV() }

    fun initMMKV() {
        MMKV.initialize(NsApp.app)
        if (appFirstOpenTime == -1L) {
            appFirstOpenTime = System.currentTimeMillis()
        }
    }

    var clockFinish: Boolean
        get() {
            return mmkv.decodeBool("ns_clo", false)
        }
        set(value) {
            mmkv.encode("ns_clo", value)
        }

    val isVip: Boolean
        get() {
            if (BuildConfig.DEBUG) {
                return true
            }
            return RemoteConfigHelper.instance.isVip || isAutoVip
        }

    var appFirstOpenTime: Long
        get() {
            return mmkv.decodeLong("ns_app_first_opn_time", -1)
        }
        set(value) {
            mmkv.encode("ns_app_first_opn_time", value)
        }

    var isShowLanguage: Boolean
        get() {
            return mmkv.decodeBool("ns_is_show_language", true)
        }
        set(value) {
            mmkv.encode("ns_is_show_language", value)
        }

    var selectLanCode: String?
        get() {
            return mmkv.decodeString("ns_select_lan_code")
        }
        set(value) {
            mmkv.encode("ns_select_lan_code", value)
        }
    var plyRefStr: String?
        get() {
            return mmkv.decodeString("ns_ply_ref_str")
        }
        set(value) {
            mmkv.encode("ns_ply_ref_str", value)
        }

    var reyunRefStr: String?
        get() {
            return mmkv.decodeString("ns_re_yun_ref_str")
        }
        set(value) {
            mmkv.encode("ns_re_yun_ref_str", value)
        }


    var notifyIdIndex: Int
        get() {
            return mmkv.decodeInt("ns_notify_id_index", 0)
        }
        set(value) {
            mmkv.encode("ns_notify_id_index", value)
        }

    fun getInstallDay(): String {
        val installTime = appFirstOpenTime
        val currentTime = System.currentTimeMillis()

        val dayOff = (currentTime - installTime) / (24 * 60 * 60 * 1000)

        return if (dayOff < 0) {
            "-1"
        } else {
            "${if (dayOff in 0..14) dayOff else 15}"
        }
    }

    var userKey: String?
        get() = mmkv.decodeString("ns_user_key")
        set(value) {
            mmkv.encode("ns_user_key", value)
        }


    fun isM(newPlyRefStr: String?=null): Boolean {
        val rs = newPlyRefStr?:plyRefStr
        if (rs.isNullOrEmpty()) return false
        return rs.contains("facebook", true)
                || rs.contains("fb4a", true)
                || rs.contains("instagram", true)
                || rs.contains("fb", true)
                || rs.contains("ig4a", true)
                || rs.contains("gclid", true)
                || rs.contains("youtubeads", true)
                || checkUsermmkv(rs)
    }


    private fun checkUsermmkv(attr: String): Boolean {
        val mmkv = RemoteConfigHelper.instance.getUseConfig()
        if (mmkv.isEmpty()) return false
        try {
            JSONArray(String(Base64.decode(mmkv, Base64.NO_WRAP))).run {
                for (i in 0 until length()) {
                    if (attr.contains(getString(i), true)) return true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }


    fun reyunMl(network: String? = null): Boolean {
        var ref = network
        if (ref.isNullOrEmpty()) {
            ref = getReyunNetwork()
            if (ref.isNullOrEmpty()) {
                ref = reyunRefStr
            }
        }
        return !ref.isNullOrEmpty() && ref != "-1"
    }


    private fun getReyunNetwork(): String? {
        return try {
            SolarEngineManager.getInstance().attribution?.optString("channel_id")
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    fun setRpFcmDally() {
        val time = System.currentTimeMillis()
        val timeString = time.getDate()
        mmkv.encode("${timeString}_fcm_dally", true)
    }

    fun getRpFcmDally(): Boolean {
        val time = System.currentTimeMillis()
        val timeString = time.getDate()
        return mmkv.decodeBool("${timeString}_fcm_dally", false)
    }

    fun setToken(token: String) {
        mmkv.encode("ns_f_token", token)
    }

    fun getToken(): String {
        return mmkv.decodeString("ns_f_token", "") ?: ""
    }

    fun setGaid(id: String) {
        mmkv.encode("ns_gaid", id)
    }

    fun getGaid(): String {
        return mmkv.decodeString("ns_gaid", "") ?: ""
    }


    fun setDate() {
        val time = System.currentTimeMillis()
        val timeString = time.getDate()
        val cur = getDate()
        if (timeString != cur) {
            mmkv.encode("ns_current_time_string", timeString)
            setToEveryDayRevenue(0.0)
        }
    }

    private fun getDate() = mmkv.decodeString("ns_current_time_string", "")

    fun setToEveryDayRevenue(d: Double) {
        val total = getEveryDayRevenue()
        mmkv.encode("ns_everyday_revenue", d + total)
    }

    fun getEveryDayRevenue() = mmkv.decodeDouble("ns_everyday_revenue", 0.0)


    fun addTotalRevenue(d: Double) {
        val total = getTotalRevenue()
        mmkv.encode("ns_total_revenue", d + total)
    }

    fun clearTotalRevenue() {
        mmkv.encode("ns_total_revenue", 0.0)
    }

    fun getTotalRevenue() = mmkv.decodeDouble("ns_total_revenue", 0.0)


    var isReportFbTotalValue2: Boolean
        get() {
            return mmkv.decodeBool("ns_is_fb_report_v", false)
        }
        set(value) {
            mmkv.encode("ns_is_fb_report_v", value)
        }

    //展示价值
    fun addTotalRevenue2(d: Double) {
        val total = getTotalRevenue2()
        mmkv.encode("ns_total_revenue_2", d + total)
    }

    fun clearTotalRevenue2() {
        mmkv.encode("ns_total_revenue_2", 0.0)
    }

    fun getTotalRevenue2() = mmkv.decodeDouble("ns_total_revenue_2", 0.0)

    //展示价值
    fun addTotalRevenue2C(d: Double) {
        val total = getTotalRevenue2C()
        mmkv.encode("ns_total_revenue_2c", d + total)
    }

    fun clearTotalRevenue2C() {
        mmkv.encode("ns_total_revenue_2c", 0.0)
    }

    fun getTotalRevenue2C() = mmkv.decodeDouble("ns_total_revenue_2c", 0.0)


    //点击价值
    fun addTotalRevenue3(d: Double) {
        val total = getTotalRevenue3()
        mmkv.encode("ns_total_revenue_3", d + total)
    }

    fun clearTotalRevenue3() {
        mmkv.encode("ns_total_revenue_3", 0.0)
    }

    fun getTotalRevenue3() = mmkv.decodeDouble("ns_total_revenue_3", 0.0)

    var firstFcmTime: Long
        get() = mmkv.decodeLong("ns_fcm_lst_time", -1L)
        set(value) {
            mmkv.encode("ns_fcm_lst_time", value)
        }

    var isAutoVip: Boolean
        get() = mmkv.decodeBool("ns_change_v", false)
        set(value) {
            mmkv.encode("ns_change_v", value)
        }


    var isFirstOpen: Boolean
        get() = mmkv.decodeBool("ns_is_first_open", true)
        set(value) {
            mmkv.encode("ns_is_first_open", value)
        }

    var lastShowPDialogTime: Long
        get() = mmkv.decodeLong("ns_last_spd_t", -1)
        set(value) {
            mmkv.encode("ns_last_spd_t", value)
        }

    var initExampleData: Boolean
        get() {
            return mmkv.decodeBool("ns_example_data", false)
        }
        set(value) {
            mmkv.encode("ns_example_data", value)
        }

    var recommendDataIndex: Int
        get() {
            return mmkv.decodeInt("ns_example_data_index", 6)
        }
        set(value) {
            mmkv.encode("ns_example_data_index", value)
        }
}