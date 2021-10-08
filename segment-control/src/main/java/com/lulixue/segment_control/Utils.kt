package com.lulixue.segment_control

import android.content.res.Resources

val Float.dp: Float
    get() = Resources.getSystem().displayMetrics.density * this + 0.5f

val Int.dp: Float
    get() = this.toFloat().dp

val Float.sp: Float
    get() = Resources.getSystem().displayMetrics.scaledDensity * this + 0.5f

val Int.sp: Float
    get() = toFloat().sp