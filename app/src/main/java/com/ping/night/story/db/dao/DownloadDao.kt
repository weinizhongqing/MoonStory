package com.ping.night.story.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ping.night.story.db.DownloadInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDownloadTask(downloadInfo: DownloadInfo)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateDownloadTask(downloadInfo: DownloadInfo): Int

    @Delete
    fun deleteDownloadTask(downloadInfo: DownloadInfo)

    @Query("SELECT * FROM download_info")
    fun getAllDownloadTasks(): List<DownloadInfo>

    @Query("SELECT * FROM download_info WHERE taskId = :taskId")
    fun getDownloadTaskById(taskId: String): DownloadInfo?

    @Query("SELECT COUNT(*) FROM download_info WHERE isCompleted=0")
    fun downloadCount(): Flow<Int>
}