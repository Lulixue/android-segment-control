package com.lulixue.segment_control

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import kotlin.math.max
import kotlin.math.min

class SegmentControl(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private var roundRadius: Float = 5.dp
    private var roundStrokeWidth: Float = 0f
    private var roundStrokeColor = Color.GRAY
    private var itemPaddingStart = 5.dp
    private var itemPaddingRight = 5.dp
    private var itemPaddingTop = 3.dp
    private var itemPaddingBottom = 3.dp
    private var itemTextSize = 13.sp
    private var fixedWidth = true
    private var items = ArrayList<String>().apply {
        add("First")
        add("Second")
        add("Third")
    }
    private var itemWidths = ArrayList<Float>()
    private var bound = Rect()

    private var separatorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = 1.dp
        strokeCap = Paint.Cap.ROUND
        color = Color.GRAY
    }

    private var textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = itemTextSize
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSpec = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSpec = MeasureSpec.getSize(heightMeasureSpec)

        var maxHeight = 0
        var maxWidth = 0
        itemWidths.clear()
        items.forEach {
            textPaint.getTextBounds(it, 0, it.length, bound)
            itemWidths.add(bound.width().toFloat())
            maxWidth = max(maxWidth, bound.width())
            maxHeight = max(maxHeight, bound.height())
        }
        var totalWidth = 0
        if (fixedWidth) {
            totalWidth = items.size * maxWidth
        } else {
            itemWidths.forEach {
                totalWidth += it.toInt()
            }
        }

        totalWidth += (3 * (itemPaddingStart + itemPaddingRight)).toInt()

        val wrapContentHeight =  (maxHeight + itemPaddingTop + itemPaddingBottom).toInt()
        val destHeight = when(heightMode) {
            MeasureSpec.AT_MOST -> {
                min(wrapContentHeight, heightSpec)
            }
            MeasureSpec.EXACTLY -> {
                heightSpec
            }
            else -> {
                wrapContentHeight
            }
        }
        val destWidth = when (widthMode) {
            MeasureSpec.AT_MOST -> {
                min(totalWidth, widthSpec)
            }
            MeasureSpec.EXACTLY -> {
                widthSpec
            }
            else -> {
                totalWidth
            }
        }
        val itemWidth = (destWidth - 3 * (itemPaddingStart + itemPaddingRight)) / items.size
        if (fixedWidth) {
            itemWidths.clear()
            repeat(items.size) {
                itemWidths.add(itemWidth)
            }
        } else {
            var totalItemWidth = 0f
            itemWidths.forEach {
                totalItemWidth += it
            }
            for (i in 0 until items.size) {
                val newWidth = (itemWidths[0] / totalItemWidth) * itemWidth
                itemWidths.removeAt(0)
                itemWidths.add(newWidth)
            }
        }
        setMeasuredDimension(destWidth, destHeight)
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        canvas.save()
        separatorPaint.color = Color.GRAY
        canvas.drawRoundRect(0f, 0f, measuredWidth.toFloat(), measuredHeight.toFloat(), roundRadius,  roundRadius, separatorPaint)

        var textX = itemPaddingStart
        for ((i, text) in items.withIndex()) {
            canvas.drawText(text, textX, measuredHeight/2f, textPaint)
            textX += itemWidths[i]
            textX += itemPaddingRight
            separatorPaint.color = Color.DKGRAY
            canvas.drawRect(textX, itemPaddingTop,textX+1.5f, measuredHeight-itemPaddingBottom,  separatorPaint)
            textX += itemPaddingStart
        }

        canvas.restore()
    }
}