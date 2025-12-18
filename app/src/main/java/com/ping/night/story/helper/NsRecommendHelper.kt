package com.ping.night.story.helper

import com.ping.night.story.NsApp
import com.ping.night.story.db.NsDBHelper
import com.ping.night.story.db.NsVRecommend
import com.ping.night.story.utils.Utils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

object NsRecommendHelper {

    private val defaultExUrls = arrayListOf(
        "https://www.instagram.com/reel/DSUIEAkEfod/",
        "https://www.instagram.com/reel/DSNSlkUCYnG/",
        "https://www.instagram.com/reel/DSVDGsKjT2N/",
        "https://www.instagram.com/reel/DMwUKFxvia8/",
        "https://www.instagram.com/reel/DR_5ep0EhJa/",
        "https://www.instagram.com/reel/DMI77l4SIlx/"
    )






    fun init() {
        if (MMKVHelper.initExampleData) {
            clearOldHistory()
            return
        }
        MMKVHelper.initExampleData = true

        val models = defaultExUrls.mapIndexed { index, url ->
            NsVRecommend(
                id = Utils.getUniqueId(url),
                resId = index + 1,
                url = url,
                name = "Recommend ${(index + 1)}",
                cover = "",
                updateTime = System.currentTimeMillis()
            )
        }
        insertModels(models)
    }

    private fun insertModels(models: List<NsVRecommend>) {
        NsApp.scope.launch {
            models.forEach {
                NsDBHelper.instance.linkVRecommendDao().insert(it)
            }

        }
    }

    fun insertList(list: List<Pair<String, String>>) {
        list.forEach {
            insertNew(it.second, it.first)
        }
    }

    private fun clearOldHistory() {
        NsApp.scope.launch {
            delay(1000 * 20)
            val count = NsDBHelper.instance.linkVRecommendDao().getCount()
            if (count > 9) {
                val sevenDaysAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(3)
                NsDBHelper.instance.linkVRecommendDao().deleteOlderThan(sevenDaysAgo)
            }
        }
    }

    private fun insertNew(cover: String, url: String) {
        val lastIndex = MMKVHelper.recommendDataIndex
        val newIndex = lastIndex + 1
        val model = NsVRecommend(
            id = Utils.getUniqueId(url),
            resId = -1,
            url = url,
            name = "Recommend $newIndex",
            cover = cover,
            updateTime = System.currentTimeMillis()
        )
        MMKVHelper.recommendDataIndex = newIndex
        NsApp.scope.launch {
            NsDBHelper.instance.linkVRecommendDao().insert(model)
        }
    }
}