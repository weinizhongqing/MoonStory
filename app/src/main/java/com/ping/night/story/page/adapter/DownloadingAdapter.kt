package com.ping.night.story.page.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ping.night.story.R
import com.ping.night.story.admob.view.CustomProgressBar
import com.ping.night.story.databinding.NsItemDownloadingPageBinding
import com.ping.night.story.db.DownloadInfo
import com.ping.night.story.dialog.DelDialog
import com.ping.night.story.download.DownloadDiffUtil
import com.ping.night.story.download.DownloaderHelper

class DownloadingAdapter(private val context: Context) :
    ListAdapter<DownloadInfo, DownloadingAdapter.ViewHolder>(
        DownloadDiffUtil()
)  {

    private var recyclerView: RecyclerView? = null

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        this.recyclerView = null
    }

    fun refreshData(data: DownloadInfo?) {
        if (data == null) return
        val tvStatus = recyclerView?.findViewWithTag<TextView>("${data.id}&tvStatus")
        val progressBar = recyclerView?.findViewWithTag<CustomProgressBar>("${data.id}&Progress")
        val imStatus = recyclerView?.findViewWithTag<ImageView>("${data.id}&imstatus")
        var p = ((data.downloaded.toFloat() / data.total.toFloat()) * 100).toInt()
        if (p > 100) {
            p = 100
        }
        progressBar?.setProgress(p)
        tvStatus?.text =
            if (data.isPaused) {
                context.getString(R.string.pause)
            } else context.getString(R.string.downloading)

        if (data.isPaused) {
            imStatus?.setImageResource(R.mipmap.img_item_downloading_page_stop)
        } else {
            imStatus?.setImageResource(R.mipmap.img_item_downloading_page_start)
        }
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = NsItemDownloadingPageBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = getItem(position)
        holder.apply {
            Glide.with(context)
                .load(data.img)
                .into(binding.itemDownloadingPageIcon)
            binding.itemDownloadingPageName.text = data.title
            var p = ((data.downloaded.toFloat() / data.total.toFloat()) * 100).toInt()
            if (p > 100) {
                p = 100
            }
            binding.itemDownloadingPageProgress.setProgress(p)
            binding.itemDownloadingPageStatus.setImageResource(
                if (data.isPaused) {
                    R.mipmap.img_item_downloading_page_stop
                }  else R.mipmap.img_item_downloading_page_start
            )
            binding.itemDownloadingPageStatusTv.text =
                if (data.isPaused) {
                    context.getString(R.string.pause)
                } else context.getString(R.string.downloading)

            binding.itemDownloadingPageProgress.tag = "${data.id}&Progress"
            binding.itemDownloadingPageStatus.tag = "${data.id}&imstatus"
            binding.itemDownloadingPageStatusTv.tag = "${data.id}&tvStatus"


            binding.root.setOnClickListener {

                if (data.isPaused) {
                    DownloaderHelper.resume(data.taskId)
                } else {
                    DownloaderHelper.pause(data.taskId)
                }
            }

            binding.itemDownloadingPageStatus.setOnClickListener {
                if (data.isPaused) {
                    DownloaderHelper.resume(data.taskId)
                } else {
                    DownloaderHelper.pause(data.taskId)
                }
            }

            binding.itemDownloadingPageClose.setOnClickListener {
                DelDialog(
                    context,
                    context.getString(R.string.del_downloading_content)
                ) {
                    DownloaderHelper.delete(data.taskId)
                }.show()
            }
        }
    }


    inner class ViewHolder(val binding: NsItemDownloadingPageBinding) : RecyclerView.ViewHolder(binding.root)
}