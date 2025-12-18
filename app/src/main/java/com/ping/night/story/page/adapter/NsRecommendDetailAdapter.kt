package com.ping.night.story.page.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.request.RequestOptions
import com.ping.night.story.R
import com.ping.night.story.databinding.NsItemRecommendedPageBinding
import com.ping.night.story.db.NsVRecommend

class NsRecommendDetailAdapter : ListAdapter<NsVRecommend, NsRecommendDetailAdapter.ViewHolder>(diffUtil) {

    interface Callback {
        fun onItemClick(video: NsVRecommend)
    }

    class ViewHolder(val binding: NsItemRecommendedPageBinding) :
        RecyclerView.ViewHolder(binding.root)

    var callback: Callback? = null

    companion object {
        @JvmStatic
        internal val diffUtil
            get() = object : DiffUtil.ItemCallback<NsVRecommend>() {
                override fun areItemsTheSame(
                    oldItem: NsVRecommend,
                    newItem: NsVRecommend
                ): Boolean {
                    return oldItem.id == newItem.id
                }

                override fun areContentsTheSame(
                    oldItem: NsVRecommend,
                    newItem: NsVRecommend
                ): Boolean {
                    return oldItem == newItem
                }
            }
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = getItem(position) ?: return
        val resId = when (data.resId) {
            1 -> R.mipmap.img_ns_1
            2 -> R.mipmap.img_ns_2
            3 -> R.mipmap.img_ns_3
            4 -> R.mipmap.img_ns_4
            5 -> R.mipmap.img_ns_5
            6 -> R.mipmap.img_ns_6
            else -> null
        }
        val options =
            RequestOptions()
                .format(DecodeFormat.PREFER_ARGB_8888)
        if (resId == null) {

            Glide.with(holder.itemView.context).load(data.cover)
                .apply(options)
                .placeholder(R.drawable.shape_default_img)
                .into(holder.binding.itemRecommendedPageIcon)
        } else {
            Glide.with(holder.itemView.context).load(resId)
                .apply(options)
                .into(holder.binding.itemRecommendedPageIcon)
        }

//        holder.binding.ivStartInfo.isVisible = false
        holder.itemView.setOnClickListener {
            callback?.onItemClick(data)
        }
//
//        holder.binding.ivMenu.setOnClickListener {
//            callback?.onItemMenu(it, data)
//        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            NsItemRecommendedPageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }


}