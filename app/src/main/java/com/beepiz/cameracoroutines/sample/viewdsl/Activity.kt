@file:Suppress("NOTHING_TO_INLINE")

package com.beepiz.cameracoroutines.sample.viewdsl

import android.app.Activity
import android.view.View

inline var Activity.contentView: View
    @Deprecated(NO_GETTER, level = DeprecationLevel.HIDDEN) get() = noGetter()
    set(value) = setContentView(value)

inline fun Activity.setContentView(ui: Ui) = setContentView(ui.root)
