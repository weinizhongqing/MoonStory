package com.ping.night.story.helper

import android.Manifest
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import androidx.core.content.ContextCompat
import com.ping.night.story.NsApp

object AppHelper {

    fun isScreenOn(): Boolean {
        return (NsApp.app.getSystemService(Context.POWER_SERVICE) as PowerManager).isInteractive
    }

    fun getClipboardContent(): String {
        val clipboard =
            NsApp.app.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        // 检查剪切板中是否有内容
        if (clipboard.hasPrimaryClip()) {
            val clip = clipboard.primaryClip
            val item = clip?.getItemAt(0)  // 获取剪切板中的第一个项

            return item?.text.toString()
        }
        return ""
    }

    fun hasNotifyPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            NsApp.app.applicationContext,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

}