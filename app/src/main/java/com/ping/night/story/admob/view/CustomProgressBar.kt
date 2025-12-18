package com.ping.night.story.admob.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import com.ping.night.story.R

class CustomProgressBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : android.view.View(context, attrs, defStyle) {

    private var progress = 0 // 0 - 100
    private val paintBackground = Paint().apply {
        color = ContextCompat.getColor(context, R.color.col_00000000)
    }
    private val paintProgress = Paint().apply {
        color = ContextCompat.getColor(context, R.color.col_4D000000)
    }

    fun setProgress(value: Int) {
        progress = value.coerceIn(0, 100)
        invalidate() // 触发重新绘制
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()

        // 绘制背景
        canvas.drawRect(0f, 0f, width, height, paintBackground)

        // 根据进度计算高度（从底部往上填充）
        val progressHeight = height * progress / 100
        val top = height - progressHeight

        // 绘制进度
        canvas.drawRect(0f, top, width, height, paintProgress)
    }
}