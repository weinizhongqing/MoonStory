package com.ping.night.story.download

import androidx.recyclerview.widget.DiffUtil
import com.ping.night.story.db.DownloadInfo

class DownloadDiffUtil : DiffUtil.ItemCallback<DownloadInfo>() {

    override fun areItemsTheSame(oldItem: DownloadInfo, newItem: DownloadInfo): Boolean {
        return oldItem.taskId == newItem.taskId
    }

    override fun areContentsTheSame(oldItem: DownloadInfo, newItem: DownloadInfo): Boolean {
        return oldItem == newItem

    }
}