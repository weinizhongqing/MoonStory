package com.ping.night.story.download

import com.ping.night.story.db.DownloadInfo
import com.ping.night.story.db.NsDBHelper

class DownloadCache {

    private val downloads = mutableMapOf<String, DownloadInfo>()

    fun loadAllTasks(): Map<String, DownloadInfo> {
        downloads.clear()
        val dbTasks = NsDBHelper.instance.downloadDao().getAllDownloadTasks()
        dbTasks.forEach { downloads[it.taskId] = it }
        return downloads
    }

    fun getTask(taskId: String): DownloadInfo? = downloads[taskId]

    fun insertTask(task: DownloadInfo) {
        NsDBHelper.instance.downloadDao().insertDownloadTask(task)
        downloads[task.taskId] = task
    }

    fun updateTask(task: DownloadInfo) {
        NsDBHelper.instance.downloadDao().updateDownloadTask(task)
        downloads[task.taskId] = task
    }

    fun deleteTask(taskId: String) {
        NsDBHelper.instance.downloadDao().getDownloadTaskById(taskId)?.let {
            NsDBHelper.instance.downloadDao().deleteDownloadTask(it)
        }
        downloads.remove(taskId)
    }

    fun getAllTasksSorted() = downloads.values.sortedByDescending { it.time }

}