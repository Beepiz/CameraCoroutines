package com.beepiz.cameracoroutines.sample

import android.app.Activity
import android.view.Gravity
import android.widget.FrameLayout
import splitties.viewdsl.appcompat.styles.progressBar
import splitties.viewdsl.core.Ui
import splitties.viewdsl.core.add
import splitties.viewdsl.core.lParams
import splitties.viewdsl.core.v

class CamTestUi(override val ctx: Activity) : Ui {

    override val root = v(::FrameLayout) {
        add(v(::progressBar), lParams(gravity = Gravity.CENTER))
    }
}
