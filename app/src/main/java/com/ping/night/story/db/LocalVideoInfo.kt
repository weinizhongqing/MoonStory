package com.ping.night.story.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "local_video_info")
data class LocalVideoInfo (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val parseUrl: String,
    val blogger: String,
    val videoDesc:String,
    var imageUrl: String = "",
    var videoDuration: Long = 0,
    var videoSize: Long = 0,
    var postsDesc: String = "",
    var fileName: String = "",
    var saveTime: Long = System.currentTimeMillis(),
    var filePath: String = "",
    var index: Int = 0,
    var type: Int = 0,
    var likeStatus:Int = 0,
)