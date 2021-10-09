package com.lulixue.segment_control

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.service.autofill.TextValueSanitizer
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView
import androidx.core.view.GestureDetectorCompat
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@SuppressLint("ClickableViewAccessibility")
class SegmentControl2(context: Context, attrs: AttributeSet?) : ViewGroup(context, attrs) {
    companion object {
        private const val DEFAULT_SELECTED_BG_COLOR = Color.WHITE
        private const val DEFAULT_ITEM_TEXT_COLOR = Color.BLACK
        private const val DEFAULT_ITEM_CLICKED_TEXT_COLOR = Color.GRAY
        private val DEFAULT_SEPARATOR_COLOR = Color.parseColor("#CBCBCF")
        private val DEFAULT_BACKGROUND_COLOR = Color.parseColor("#EEEEEF")
    }
    private val segmentGestureDetector = GestureDetectorCompat(context, SegmentGestureListener())
    private var roundRadius: Float = 5.dp
    private var itemPaddingStart = 10.dp
    private var itemPaddingEnd = 10.dp
    private var itemPaddingTop = 10.dp
    private var itemPaddingBottom = 10.dp
    private var itemTextSize = 13.sp
    private var fixedWidth = false
    private var separatorColor = DEFAULT_SEPARATOR_COLOR
    private var itemBackgroundColor = DEFAULT_BACKGROUND_COLOR
    private var selectedBackgroundColor = DEFAULT_SELECTED_BG_COLOR
    private var itemTextColor = DEFAULT_ITEM_TEXT_COLOR
    private var itemFixedPadding: Float = 2.dp
    private val separatorWidth = 1.dp
    private var separatorHeight = 0f
    private var slideSelected = false
    private var touchOnPosition = -1
    private var downX = 0f
    private var selectedWidth: Float = 0f
    private var selectedItemX: Float = 0f
    private var previousText: String? = null
    var selectedPosition = -1
        set(value) {
            if (field != value) {
                if (field != -1) {
                    previousText = items[field]
                }
                field = value
                animateToPosition(value)
            }
        }

    private val items = ArrayList<String>().apply {
        add("First")
        add("Second")
        add("Third")
    }

    private var backgroundView: View = View(context)
    private val textViews = ArrayList<TextView>()

    private val itemWidths = ArrayList<Float>()
    private val itemEndX = ArrayList<Float>()
    private val bound = Rect()

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    }
    private val selectedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        setShadowLayer(3.0f, 0.0f, 2.0f, Color.LTGRAY)
        color = selectedBackgroundColor
    }

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = 1.dp
        strokeCap = Paint.Cap.ROUND
    }

    private val selectedTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = itemTextColor
        textSize = itemTextSize
        textAlign = Paint.Align.CENTER
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = itemTextSize
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT
    }

    init {
        setOnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    touchEnd(event.x)
                }
            }
            segmentGestureDetector.onTouchEvent(event)
        }
        selectedPosition = 0
    }
    private fun animateSelectedText() {
        val animator = ValueAnimator.ofInt(selectedTextPaint.alpha, 255)
        animator.addUpdateListener {
            selectedTextPaint.alpha = it.animatedValue as Int
            invalidate()
        }
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.duration = 200
        animator.start()
    }
    private fun animateToPosition(position: Int) {
        if (itemEndX.isEmpty()) {
            return
        }
        val destItemX = (if (position == 0) 0f else itemEndX[position-1]) + itemFixedPadding
        val destWidth = itemWidths[position]
        val startWidth = selectedWidth
        val animator = ValueAnimator.ofFloat(selectedItemX, destItemX)
        selectedTextPaint.alpha = 255
        animator.addUpdateListener {
            selectedItemX = it.animatedValue as Float
            selectedWidth = startWidth + it.animatedFraction * (destWidth - startWidth)
            if (it.animatedFraction > 0.7f) {
                previousText = items[position]
            }
            invalidate()

            if (it.animatedFraction == 1.0f) {
//                animateSelectedText()
            }
        }
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.duration = 150
        animator.start()
    }

    private fun getTouchPosition(x: Float): Int {
        if (x < 0) {
            return -1
        }
        for ((i, maxX) in itemEndX.withIndex()) {
            if (x < maxX) {
                return i
            }
        }
        return -1
    }

    private fun touchEnd(x: Float) {
        slideSelected = false
        touchOnPosition = -1
        val position = getTouchPosition(x)
        if (position != -1) {
            selectedPosition = position
        }
        invalidate()
    }

    inner class SegmentGestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            val position = getTouchPosition(e.x)
            if (position == selectedPosition) {
                slideSelected = true
            }
            downX = e.x
            touchOnPosition = position
            invalidate()
            return true
        }

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            touchEnd(e.x)
            return false
        }
        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent?,
            distanceX: Float,
            distanceY: Float,
        ): Boolean {
            if (slideSelected) {
                val destItemX = selectedItemX - (downX - e2!!.x)
                selectedItemX = if (destItemX > itemFixedPadding) {
                    max(destItemX, itemEndX.last())
                } else {
                    itemFixedPadding
                }
            }
            touchOnPosition = getTouchPosition(e2!!.x)
            invalidate()
            return false
        }
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

        totalWidth += (3 * (itemPaddingStart + itemPaddingEnd + 2 * itemFixedPadding) + (itemWidths.size-1) * separatorWidth).toInt()

        val wrapContentHeight = (maxHeight + itemPaddingTop + itemPaddingBottom + 2 * itemFixedPadding).toInt()
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
        val itemWidth = destWidth - 6 * itemFixedPadding
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
        itemEndX.clear()
        var startX = 0f
        for (width in itemWidths) {
            itemEndX.add(startX + width + 2*itemFixedPadding)
            startX += itemEndX.last()
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
            startX += itemFixedPadding
            val x = startX + itemWidths[i] / 2
            val y = measuredHeight / 2f - (textPaint.descent() + textPaint.ascent()) / 2

            if (i != 0) {
                linePaint.color = if (abs(i - selectedPosition) > 1) separatorColor else Color.TRANSPARENT
                val startY = (measuredHeight - separatorHeight) / 2
                canvas.drawRect(
                    startX,
                    startY,
                    startX + separatorWidth,
                    startY + separatorHeight,
                    linePaint
                )
            }

            textPaint.color =
                if (touchOnPosition == i) DEFAULT_ITEM_CLICKED_TEXT_COLOR else itemTextColor
            canvas.drawText(text, x, y, textPaint)

            startX += itemWidths[i]
            startX += itemFixedPadding
            linePaint.color = separatorColor
        }

        if (selectedPosition >= 0) {
            val i = selectedPosition

            selectedWidth = if (selectedWidth == 0f) itemWidths[i] else selectedWidth
            selectedItemX = if (selectedItemX == 0f) itemFixedPadding else selectedItemX
            val animateEnd = selectedWidth == itemWidths[i]
            val text = if (previousText == null || animateEnd) items[i] else previousText!!
            val width = selectedWidth
            val selectedStartX = selectedItemX

            canvas.drawRoundRect(
                selectedStartX , itemFixedPadding,
                selectedStartX + width, measuredHeight - itemFixedPadding,
                roundRadius, roundRadius, selectedPaint
            )

            val x = selectedStartX + width / 2
            val y = measuredHeight / 2f - (textPaint.descent() + textPaint.ascent()) / 2
            selectedTextPaint.typeface = if (animateEnd) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            canvas.drawText(text, x, y, selectedTextPaint)
        }
        canvas.restore()
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {

    }
}