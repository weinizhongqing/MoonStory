package com.ping.night.story.db.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ping.night.story.db.LocalVideoInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface LocalVideoDao {

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    fun insertModel(info: LocalVideoInfo)

    @Query("SELECT * FROM local_video_info WHERE parseUrl =:url")
    fun queryByUrl(url: String): LocalVideoInfo?

    @Query("SELECT * FROM local_video_info WHERE filePath != '' ORDER BY saveTime DESC")
    fun queryVideoModel(): MutableList<LocalVideoInfo>

    @Query("SELECT * FROM local_video_info WHERE filePath != '' ORDER BY saveTime DESC")
    fun getAllDownloadedPaging(): PagingSource<Int, LocalVideoInfo>


    @Query("SELECT * FROM local_video_info WHERE filePath != '' and likeStatus = 1 ORDER BY saveTime DESC")
    fun getAllLikePaging(): PagingSource<Int, LocalVideoInfo>

    @Query("UPDATE local_video_info SET likeStatus =:status WHERE  id = :id")
    fun updateLike(id:Int,status: Int)

    @Delete
    fun deleteVideo(info: LocalVideoInfo)

    @Query("SELECT COUNT(*) FROM local_video_info WHERE likeStatus=1")
    fun favCount():Flow<Int>

    @Query("SELECT COUNT(*) FROM local_video_info")
    fun allVideoCount():Flow<Int>
}