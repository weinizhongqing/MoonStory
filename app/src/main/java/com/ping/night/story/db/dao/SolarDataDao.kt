package com.ping.night.story.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ping.night.story.db.SolarDataInfo

@Dao
interface SolarDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertEvent(event: SolarDataInfo)

    @Query("SELECT * FROM solar_data_info")
    fun getAlls(): List<SolarDataInfo>

    @Delete
    fun delete(event: SolarDataInfo)

}