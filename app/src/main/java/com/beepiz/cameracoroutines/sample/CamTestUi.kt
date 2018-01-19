package com.beepiz.cameracoroutines.sample

import android.app.Activity
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ProgressBar
import com.beepiz.cameracoroutines.sample.viewdsl.Ui
import com.beepiz.cameracoroutines.sample.viewdsl.add
import com.beepiz.cameracoroutines.sample.viewdsl.lParams
import com.beepiz.cameracoroutines.sample.viewdsl.v

class CamTestUi(override val ctx: Activity) : Ui {

    override val root = v(::FrameLayout) {
        add(::ProgressBar, lParams(gravity = Gravity.CENTER))
    }
}
