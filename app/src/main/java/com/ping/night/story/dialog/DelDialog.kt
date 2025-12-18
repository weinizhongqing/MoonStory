package com.ping.night.story.dialog

import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.ping.night.story.R
import com.ping.night.story.databinding.NsDialogDeleteTipsBinding


class DelDialog(
    context: Context,
    val content: String,
    private val callback: () -> Unit
) : BottomSheetDialog(context, R.style.dialog_bottom_sheet_style) {

    private val binding by lazy { NsDialogDeleteTipsBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val layoutParams = window?.attributes
        layoutParams?.width = ViewGroup.LayoutParams.MATCH_PARENT
        layoutParams?.height = ViewGroup.LayoutParams.WRAP_CONTENT
        window?.attributes = layoutParams

        setCanceledOnTouchOutside(false)
        binding.deleteTipsPageDescText.text = content

        binding.deleteTipsPageNoAction.setOnClickListener {
            dismiss()
        }

        binding.deleteTipsPageDeleteAction.setOnClickListener {
            callback()
            dismiss()
        }

    }
}