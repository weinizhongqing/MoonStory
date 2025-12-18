package com.ping.night.story.helper

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.core.graphics.createBitmap
import com.bumptech.glide.Glide
import com.ping.night.story.NsApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

object VideoHelper {

    fun isSupportedOrdinarySocialLink(url: String): Boolean {
        val regex = Regex(
            "https?://(www\\.)?(x\\.com)/.*"
        )
        return regex.matches(url)
    }


    fun extractUrl(text: String): String {
        val u = if (text.contains("http://", ignoreCase = true)) {
            text.replaceFirst("http://", "https://")
        } else {
            text
        }

        if (text.contains("xiaohongshu") || text.contains("xhslink")) {
            val regex = Regex("(https?://[\\w\\-\\.]+(?:/[\\w\\-./?&%=]*)?)")
            return regex.find(u)?.value ?: u
        } else {
            return u
        }
    }

    fun shareVideoFile(context: Context, videoFiles: List<File>) {
        if (videoFiles.size == 1) {
            shareVideo(context, videoFiles[0])
        } else {
            shareVideos(context, videoFiles)
        }
    }

    private fun shareVideo(context: Context, videoFile: File) {
        // 获取视频文件的 Uri
        val videoUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider", // FileProvider 的 authority
            videoFile
        )

        // 创建分享 Intent
        val shareIntent = Intent().apply {
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK // 授予临时权限

            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, videoUri)
            type = "video/*" // 设置 MIME 类型
        }

        // 启动分享对话框
        context.startActivity(shareIntent)
    }

    private fun shareVideos(context: Context, videoFiles: List<File>) {
        // 把文件列表转换成 Uri 列表
        val videoUris = ArrayList<Uri>()
        videoFiles.forEach { file ->
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            videoUris.add(uri)
        }

        // 创建分享 Intent
        val shareIntent = Intent().apply {
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
            action = Intent.ACTION_SEND_MULTIPLE
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, videoUris)
            type = "video/*"
        }

        // 启动分享对话框
        context.startActivity(shareIntent)
    }


    fun getVideoThumbnail(videoPath: String): Bitmap? {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(videoPath)
            return retriever.getFrameAtTime(0)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            retriever.release()
        }
        return null
    }

    fun playVideo(context: Context, path: String) {
        // 获取视频文件的 Uri
        val videoUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            File(path)
        )

        // 创建 Intent
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(videoUri, "video/*")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        // 启动播放器
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
        }
    }

    suspend fun getBitmapFromUrl(url: String): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                val drawable = Glide.with(NsApp.app)
                    .asDrawable() // 兼容低版本，返回 Drawable 而非 Bitmap
                    .load(url)
                    .submit()
                    .get()

                when (drawable) {
                    is BitmapDrawable -> drawable.bitmap // ✅ 直接返回静态图片
                    else -> {
                        val bitmap = createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight)
                        val canvas = Canvas(bitmap)
                        drawable.setBounds(0, 0, canvas.width, canvas.height)
                        drawable.draw(canvas)
                        bitmap
                    }
                }
            } catch (e: Exception) {
                null // 捕获异常，防止崩溃
            }
        }
    }


    fun formatSize(bytes: Long): String {
        return if (bytes < 1024) {
            "$bytes B"
        } else if (bytes < 1024 * 1024) {
            String.format(Locale.getDefault(), "%.1f KB", bytes / 1024.0)
        } else if (bytes < 1024 * 1024 * 1024) {
            String.format(Locale.getDefault(), "%.1f MB", bytes / (1024.0 * 1024))
        } else {
            String.format(Locale.getDefault(), "%.1f GB", bytes / (1024.0 * 1024 * 1024))
        }
    }

    fun formatDuration(durationSeconds: Long): String {
        val hours = durationSeconds / 3600
        val minutes = (durationSeconds % 3600) / 60
        val seconds = durationSeconds % 60

        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }

}