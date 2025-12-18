package com.ping.night.story.exx

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.os.Build
import android.util.Base64
import android.util.TypedValue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Long.getDate(): String {
    val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return format.format(Date(this))
}

fun Context.dip2px(dp: Int): Int =
    TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp.toFloat(), this.resources.displayMetrics
    ).toInt()

@SuppressLint("UnspecifiedRegisterReceiverFlag")
fun Context.registerBroadcast(receiver: BroadcastReceiver, intentFilter: IntentFilter) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        registerReceiver(receiver, intentFilter, Context.RECEIVER_NOT_EXPORTED)
    } else {
        registerReceiver(receiver, intentFilter)
    }
}

fun Context.getStatusBarHeight(): Int {
    var result = 0
    val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
    if (resourceId > 0) {
        result = resources.getDimensionPixelSize(resourceId)
    }
    return result
}

fun String?.decodeBase64(): String? {
    if (this.isNullOrEmpty()) return null
    return try {
        String(Base64.decode(this, Base64.NO_WRAP))
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}