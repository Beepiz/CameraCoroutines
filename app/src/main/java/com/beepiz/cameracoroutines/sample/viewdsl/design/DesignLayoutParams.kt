@file:Suppress("NOTHING_TO_INLINE")

package com.beepiz.cameracoroutines.sample.viewdsl.design

import android.support.design.widget.AppBarLayout
import android.support.design.widget.CoordinatorLayout
import android.view.Gravity
import com.beepiz.cameracoroutines.sample.viewdsl.matchParent
import com.beepiz.cameracoroutines.sample.viewdsl.wrapContent

inline fun CoordinatorLayout.lParams(width: Int = wrapContent, height: Int = wrapContent,
                                     gravity: Int = Gravity.NO_GRAVITY,
                                     initParams: CoordinatorLayout.LayoutParams.() -> Unit) =
        CoordinatorLayout.LayoutParams(width, height).also { it.gravity = gravity }.apply(initParams)

inline fun CoordinatorLayout.lParams(width: Int = wrapContent, height: Int = wrapContent,
                                     gravity: Int = Gravity.NO_GRAVITY) =
        CoordinatorLayout.LayoutParams(width, height).also { it.gravity = gravity }

inline fun CoordinatorLayout.lParams(width: Int = wrapContent, height: Int = wrapContent) =
        CoordinatorLayout.LayoutParams(width, height)

inline fun AppBarLayout.lParams(width: Int = matchParent, height: Int = wrapContent,
                                initParams: AppBarLayout.LayoutParams.() -> Unit) =
        AppBarLayout.LayoutParams(width, height).apply(initParams)

inline fun AppBarLayout.lParams(width: Int = matchParent, height: Int = wrapContent) =
        AppBarLayout.LayoutParams(width, height)
