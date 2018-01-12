package com.beepiz.cameracoroutines.sample.extensions

import android.os.Build
import android.view.View

inline fun View.onClick(noinline l: (v: View) -> Unit) = setOnClickListener(l)

inline val View.isLtr get() = Build.VERSION.SDK_INT < 17 || layoutDirection == View.LAYOUT_DIRECTION_LTR
inline val View.isRtl get() = !isLtr
