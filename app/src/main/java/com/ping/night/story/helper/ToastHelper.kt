package com.ping.night.story.helper

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.Toast
import com.ping.night.story.databinding.NsToastTipsBinding

object ToastHelper {

    fun show(context: Context, duration: Int = Toast.LENGTH_SHORT, gravity: Int = Gravity.CENTER) {
        val inflater = LayoutInflater.from(context)
        val binding: NsToastTipsBinding = NsToastTipsBinding.inflate(inflater)
        Toast(context).apply {
            setView(binding.root)
            setDuration(duration)
            setGravity(gravity, 0, 0)
            show()
        }
    }

}