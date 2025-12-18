package com.ping.night.story.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "solar_data_info")
data class SolarDataInfo (
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val source: String,
    val platform: String,
    val adType: Int,
    val adFormat: String,
    val adUnitId: String,
    val value: Double,
    val currency: String,
    val isPrecache: Boolean,
    val customData: String
)