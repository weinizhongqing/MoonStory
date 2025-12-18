package com.ping.night.story.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import com.ping.night.story.R
import com.ping.night.story.databinding.NsDialogNatifyPBinding


class NotifyDialog(context: Context,private val callback:(Boolean) -> Unit) : Dialog(context, R.style.dialogStyle) {

    private val binding by lazy { NsDialogNatifyPBinding.inflate(layoutInflater) }
    private var isConfirm = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val layoutParams = window?.attributes
        layoutParams?.width = ViewGroup.LayoutParams.MATCH_PARENT
        layoutParams?.height = ViewGroup.LayoutParams.WRAP_CONTENT
        layoutParams?.gravity = Gravity.BOTTOM
        window?.attributes = layoutParams

        setCanceledOnTouchOutside(false)

        binding.notifyPmsitionPageAllowAction.setOnClickListener {
            isConfirm = true
            dismiss()
        }

        binding.notifyPmsitionPageClose.setOnClickListener {
            dismiss()
        }

        setOnDismissListener {
            callback.invoke(isConfirm)
        }
    }
}