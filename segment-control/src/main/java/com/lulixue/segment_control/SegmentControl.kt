package com.lulixue.segment_control

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class SegmentControl(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    companion object {
        private const val DEFAULT_SELECTED_BG_COLOR = Color.WHITE
        private val DEFAULT_SEPARATOR_COLOR = Color.parseColor("#CBCBCF")
        private val DEFAULT_BACKGROUND_COLOR = Color.parseColor("#EEEEEF")
    }
    private var roundRadius: Float = 5.dp
    private var itemPaddingStart = 10.dp
    private var itemPaddingEnd = 10.dp
    private var itemPaddingTop = 5.dp
    private var itemPaddingBottom = 5.dp
    private var itemTextSize = 13.sp
    private var fixedWidth = false
    private var separatorColor = DEFAULT_SEPARATOR_COLOR
    private var itemBackgroundColor = DEFAULT_BACKGROUND_COLOR
    private var selectedBackgroundColor = DEFAULT_SELECTED_BG_COLOR
    private var selectedRoundPadding: Float = 2.dp
    private val separatorWidth = 1.dp
    private var separatorHeight = 0f
    private var selectedPosition = 0

    private val items = ArrayList<String>().apply {
        add("First")
        add("Second")
        add("Third")
    }

    private var itemWidths = ArrayList<Float>()
    private var bound = Rect()

    private var fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = 1.dp
    }

    private var linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = 1.dp
        strokeCap = Paint.Cap.ROUND
    }

    private var textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = itemTextSize
        textAlign = Paint.Align.CENTER
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        var maxHeight = 0
        var maxWidth = 0
        itemWidths.clear()
        separatorHeight = 0f
        items.forEach {
            textPaint.getTextBounds(it, 0, it.length, bound)
            separatorHeight = max(separatorHeight, bound.height().toFloat())
            itemWidths.add(bound.width().toFloat())
            maxWidth = max(maxWidth, bound.width())
            maxHeight = max(maxHeight, bound.height())
        }
        separatorHeight += 2.dp
        var totalWidth = 0
        if (fixedWidth) {
            totalWidth = items.size * maxWidth
        } else {
            itemWidths.forEach {
                totalWidth += it.toInt()
            }
        }

        totalWidth += (3 * (itemPaddingStart + itemPaddingEnd + 2 * selectedRoundPadding)).toInt()

        val wrapContentHeight = (maxHeight + itemPaddingTop + itemPaddingBottom + 2 * selectedRoundPadding).toInt()
        val destHeight = when(heightMode) {
            MeasureSpec.AT_MOST -> {
                min(wrapContentHeight, heightSize)
            }
            MeasureSpec.EXACTLY -> {
                heightSize
            }
            else -> {
                wrapContentHeight
            }
        }
        val destWidth = when (widthMode) {
            MeasureSpec.AT_MOST -> {
                min(totalWidth, widthSize)
            }
            MeasureSpec.EXACTLY -> {
                widthSize
            }
            else -> {
                totalWidth
            }
        }
        val itemWidth = destWidth - 6 * selectedRoundPadding
        if (fixedWidth) {
            itemWidths.clear()
            repeat(items.size) {
                itemWidths.add(itemWidth / items.size)
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
        fillPaint.color = itemBackgroundColor
        canvas.drawRoundRect(0f, 0f, measuredWidth.toFloat(), measuredHeight.toFloat(),
                            roundRadius,  roundRadius, fillPaint)

        var startX = 0f
        for ((i, text) in items.withIndex()) {

            startX += selectedRoundPadding
            if (selectedPosition == i) {
                fillPaint.color = selectedBackgroundColor
                canvas.drawRoundRect(startX, selectedRoundPadding,
                            startX + itemWidths[i], measuredHeight - selectedRoundPadding,
                                roundRadius,  roundRadius, fillPaint)
                textPaint.typeface = Typeface.DEFAULT_BOLD
            } else {
                textPaint.typeface = Typeface.DEFAULT
            }

            val x = startX + itemWidths[i] / 2
            val y = measuredHeight / 2f - (textPaint.descent() + textPaint.ascent()) / 2
            canvas.drawText(text, x, y, textPaint)

            startX += itemWidths[i]
            startX += selectedRoundPadding
            linePaint.color = separatorColor
            if (i != items.lastIndex) {
                if (abs(i - selectedPosition) > 0) {
                    val startY = (measuredHeight - separatorHeight) / 2
                    canvas.drawRect(
                        startX,
                        startY,
                        startX + separatorWidth,
                        startY + separatorHeight,
                        linePaint
                    )
                }
            }
        }

        canvas.restore()
    }
}