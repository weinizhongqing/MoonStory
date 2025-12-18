package com.ping.night.story.page.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.ping.night.story.NsApp
import com.ping.night.story.R
import com.ping.night.story.databinding.NsItemVideoPageBinding
import com.ping.night.story.db.LocalVideoInfo
import com.ping.night.story.db.NsDBHelper
import com.ping.night.story.dialog.DelDialog
import com.ping.night.story.helper.VideoHelper
import kotlinx.coroutines.launch
import java.io.File


class VideoAdapter(
    private val context: Context,
    private val callback: (String) -> Unit,
    private val likeCallback: (LocalVideoInfo) -> Unit
) : PagingDataAdapter<LocalVideoInfo, VideoAdapter.ViewHolder>(diffUtil) {
    companion object {
        @JvmStatic
        internal val diffUtil
            get() = object : DiffUtil.ItemCallback<LocalVideoInfo>() {
                override fun areItemsTheSame(
                    oldItem: LocalVideoInfo,
                    newItem: LocalVideoInfo
                ): Boolean {
                    return oldItem.id == newItem.id
                }

                override fun areContentsTheSame(
                    oldItem: LocalVideoInfo,
                    newItem: LocalVideoInfo
                ): Boolean {
                    return oldItem == newItem
                }
            }
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = NsItemVideoPageBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val data = getItem(position) ?: return
        holder.binding.ivLocalVideoCover.setImageBitmap(VideoHelper.getVideoThumbnail(data.filePath))
        holder.binding.tvLocalVideoTitle.text = data.fileName
        holder.binding.tvLocalVideoDesc.text = data.videoDesc

        val drawable = AppCompatResources.getDrawable(
            context,
            if (data.likeStatus == 1) R.mipmap.img_item_video_liked else R.mipmap.img_item_video_like // 你的 mipmap 图片
        )

        holder.binding.tvLocalVideoAddLike.setCompoundDrawablesRelativeWithIntrinsicBounds(drawable,null,null,null)

        holder.binding.tvLocalVideoAddLike.setOnClickListener {
            likeCallback.invoke(data)
        }

        holder.binding.tvLocalVideoShare.setOnClickListener {
            VideoHelper.shareVideoFile(context, arrayListOf(File(data.filePath)))
        }


        holder.binding.tvLocalVideoDelete.setOnClickListener {
            DelDialog(
                context,
                context.getString(R.string.del_downloaded_content)
            ) {
                NsApp.scope.launch {
                    try {
                        NsDBHelper.instance.localVideoDao()
                            .deleteVideo(data)
                        File(data.filePath).delete()
                    } catch (_: Exception) {
                    }
                }
            }.show()
        }


        holder.binding.root.setOnClickListener {
            callback.invoke(data.filePath)
        }
    }


    inner class ViewHolder(val binding: NsItemVideoPageBinding) : RecyclerView.ViewHolder(binding.root)
}