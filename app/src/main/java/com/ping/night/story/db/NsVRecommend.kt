package com.ping.night.story.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.File


@Entity(tableName = "link_v_recommed")
data class NsVRecommend(
    @PrimaryKey(autoGenerate = false) @ColumnInfo(name = "id") var id: Int = 0,
    @ColumnInfo(name = "relation_id") var resId: Int = 0,
    @ColumnInfo(name = "url") var url: String = "",
    @ColumnInfo(name = "name") var name: String = "",
    @ColumnInfo(name = "coverUrl") var cover: String = "",
    @ColumnInfo(name = "filePath") var filePath: String = "",
    @ColumnInfo(name = "updateTime") var updateTime: Long = 0,
) {
    fun canOpen(): Boolean {
        if (filePath.isEmpty()) return false

        return try {
            File(filePath).exists()
        } catch (_: Exception) {
            false
        }
    }
}