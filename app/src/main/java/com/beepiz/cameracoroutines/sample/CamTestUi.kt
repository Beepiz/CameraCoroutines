package com.beepiz.cameracoroutines.sample

import android.app.Activity
import android.support.constraint.ConstraintLayout
import android.view.Surface
import android.view.SurfaceView
import com.beepiz.cameracoroutines.sample.viewdsl.Ui
import com.beepiz.cameracoroutines.sample.viewdsl.add
import com.beepiz.cameracoroutines.sample.viewdsl.lParams
import com.beepiz.cameracoroutines.sample.viewdsl.matchParent
import com.beepiz.cameracoroutines.sample.viewdsl.v

class CamTestUi(override val ctx: Activity) : Ui {

    private val surfaceView = v(::SurfaceView)

    val surfaceHolderState = SurfaceHolderState(surfaceView.holder)
    val previewSurface: Surface get() = surfaceView.holder.surface

    override val root = v(::ConstraintLayout) {
        add(surfaceView, lParams(width = matchParent, height = matchParent))
    }
}
