package com.ping.night.story.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ping.night.story.NsApp
import com.ping.night.story.db.dao.DownloadDao
import com.ping.night.story.db.dao.LocalVideoDao
import com.ping.night.story.db.dao.NsVRecommendDao
import com.ping.night.story.db.dao.SolarDataDao


@Database(
    entities = [SolarDataInfo::class, LocalVideoInfo::class, DownloadInfo::class, NsVRecommend::class],
    version = 1,
    exportSchema = false
)
abstract class NsDBHelper : RoomDatabase(){

    companion object {
        val instance: NsDBHelper by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            Room.databaseBuilder(NsApp.app, NsDBHelper::class.java, "ns_db_data")
                .allowMainThreadQueries()
                .setJournalMode(JournalMode.AUTOMATIC)
                .fallbackToDestructiveMigration()
                .build()
        }
    }


    abstract fun solarDataDao(): SolarDataDao

    abstract fun localVideoDao(): LocalVideoDao

    abstract fun downloadDao(): DownloadDao

    abstract fun linkVRecommendDao(): NsVRecommendDao

}