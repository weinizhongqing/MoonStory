package com.ping.night.story.admob.cache

import com.ping.night.story.admob.NsAd
import com.ping.night.story.admob.NsAdHelper
import com.ping.night.story.admob.config.NsAdConfig
import com.ping.night.story.admob.config.NsAdConfigAnalysis
import com.ping.night.story.admob.type.NsAdType


class NsAdCache {

    companion object{
        val instance : NsAdCache by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            NsAdCache()
        }
        var cacheOpenCount = 1
        var cacheInterCount = 1
        var cacheNativeCount = 1
    }

    private val cacheList = HashMap<NsAdType, MutableList<NsAd>>()

    val cfgList = HashMap<String, NsAdConfig>()


    fun getCacheSize(type: NsAdType) = cacheList[type]?.size

    fun hasCache(key: String): Boolean {
        val t = getAu(key)
        synchronized(cacheList) {
            if (t == null) return false
            if (cacheList.isEmpty()) return false
            val cache = cacheList[t.cacheType] ?: return false
            val ret = cache.firstOrNull { it.isAva() }
            return ret != null
        }
    }

    fun addCache(type: NsAdType, ad: NsAd) {
        synchronized(cacheList) {
            val cacheL = cacheList[type]
            if (cacheL == null) {
                val newL = mutableListOf<NsAd>()
                newL.add(ad)
                cacheList[type] = newL
            } else {
                cacheL.add(ad)
            }
        }
    }

    fun checkCache(t: NsAdConfig, key: String): Boolean {
        synchronized(cacheList) {
            val cache = cacheList[t.cacheType] ?: return true
            val filteredCache = mutableListOf<NsAd>()
            cache.forEach {
                if (it.isAva()) {
                    filteredCache.add(it)
                }
            }
            cacheList[t.cacheType] = filteredCache
            val size = filteredCache.size
            NsAdHelper.instance.adLog( "[$key]-[${t.cacheType}] current cache size=$size")
            val cacheCount = when (t.cacheType) {
                NsAdType.INTERSTITIAL -> cacheInterCount
                NsAdType.NATIVE -> cacheNativeCount
                NsAdType.OPEN -> cacheOpenCount
            }
            if (size >= cacheCount) {
                NsAdHelper.instance.adLog(
                    "[$key]-[${t.cacheType}] check cache, current cache size=$size >= configSize $cacheCount"
                )
                return false
            } else {
                return true
            }
        }
    }

    fun getCache(key: String): NsAd?{
        val t = getAu(key)
        synchronized(cacheList) {
            if (t == null) return null
            val cache = cacheList[t.cacheType] ?: return null
            NsAdHelper.instance.adLog( "[$key] get cache cache= $cache")
            val a = cache.firstOrNull { it.isAva() }
            NsAdHelper.instance.adLog( "[$key] get cache $a")
            return a
        }
    }


    fun getAu(sk: String): NsAdConfig? {
        val s = cfgList[sk]
        if (s == null) {
            NsAdHelper.instance.adLog( "[$sk] cfg is null , init cfg...")
            NsAdConfigAnalysis.initConfig()
        }
        return cfgList[sk]
    }
    
}