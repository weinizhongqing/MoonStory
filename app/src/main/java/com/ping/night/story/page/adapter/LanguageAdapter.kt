package com.ping.night.story.page.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ping.night.story.NsApp
import com.ping.night.story.R
import com.ping.night.story.databinding.NsItemLanguagePageBinding
import com.ping.night.story.helper.LanguageHelper

class LanguageAdapter(
    defaultSelectCode: String,
    private val selectItem: (String) -> Unit
) : RecyclerView.Adapter<LanguageAdapter.LanguageViewHolder>() {

    private val dataList = LanguageHelper.getLanguageList(NsApp.app)

    var selectCode = defaultSelectCode
        private set

    class LanguageViewHolder(val binding: NsItemLanguagePageBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageViewHolder {
        val view = NsItemLanguagePageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LanguageViewHolder(view)
    }

    override fun onBindViewHolder(holder: LanguageViewHolder, position: Int) {
        val item = dataList[position]
        val isSelectLang = selectCode == item.code
        holder.binding.apply {

            itemLanguagePageText.text = item.name
            itemLanguagePageText.setTextColor(ContextCompat.getColor(holder.binding.root.context,if (isSelectLang) R.color.white else R.color.col_333333))
            itemLanguagePage.setBackgroundResource(if (isSelectLang) R.drawable.shape_languag_item_selected else R.drawable.shape_languag_item_select)
            root.setOnClickListener {
                if (!isSelectLang) {
                    selectCode = item.code
                    selectItem.invoke(item.code)
                    notifyDataSetChanged()
                }
            }
        }
        holder.binding.root.isSelected = isSelectLang
    }

    override fun getItemCount(): Int = dataList.size
}