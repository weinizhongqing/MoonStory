package com.ping.night.story.admob.config

import com.ping.night.story.admob.type.NsAdInfo
import com.ping.night.story.admob.type.NsAdType

class NsAdConfig(
    val id: MutableList<NsAdInfo>,
    var isEnable: Int,
    val cacheType: NsAdType
)
