package com.ping.night.story.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ping.night.story.db.NsVRecommend
import kotlinx.coroutines.flow.Flow


@Dao
interface NsVRecommendDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(model: NsVRecommend): Long


    @Query("SELECT * FROM link_v_recommed ORDER BY updateTime DESC Limit 4")
    fun viExLimit(): Flow<List<NsVRecommend>>

    @Query("SELECT * FROM link_v_recommed ORDER BY updateTime DESC Limit 20")
    fun reVideo(): Flow<List<NsVRecommend>>

    @Query("UPDATE link_v_recommed SET filePath =:filePath WHERE id =:id")
    suspend fun updateMediaPath(id: Int, filePath: String)

    // 获取总数据量
    @Query("SELECT COUNT(*) FROM link_v_recommed")
    suspend fun getCount(): Long

    // 删除超过7天的数据
    @Query(
        """
        DELETE FROM link_v_recommed 
        WHERE updateTime < :sevenDaysAgo 
        AND (SELECT COUNT(*) FROM link_v_recommed) > 6
        AND id NOT IN (
            SELECT id FROM link_v_recommed 
            ORDER BY updateTime DESC 
            LIMIT 6
        )
    """
    )
    suspend fun deleteOlderThan(sevenDaysAgo: Long): Unit


    @Delete
    suspend fun delete(recommend: NsVRecommend): Int
}