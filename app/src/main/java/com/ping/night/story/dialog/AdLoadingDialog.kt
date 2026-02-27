package com.ping.night.story.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import com.ping.night.story.R
import com.ping.night.story.databinding.NsDialogLoadingBinding


class AdLoadingDialog(context: Context) : Dialog(context, R.style.dialogStyle) {

    private val binding by lazy { NsDialogLoadingBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val layoutParams = window?.attributes
        layoutParams?.width = ViewGroup.LayoutParams.MATCH_PARENT
        layoutParams?.height = ViewGroup.LayoutParams.WRAP_CONTENT
        window?.attributes = layoutParams

        setCanceledOnTouchOutside(false)
        setCancelable(false)
    }
}