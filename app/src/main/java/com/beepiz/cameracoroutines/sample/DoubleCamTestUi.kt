package com.beepiz.cameracoroutines.sample

import android.app.Activity
import android.view.Surface
import android.view.SurfaceView
import com.beepiz.cameracoroutines.sample.recording.SurfaceHolderState
import com.beepiz.cameracoroutines.sample.viewdsl.Ui
import com.beepiz.cameracoroutines.sample.viewdsl.add
import com.beepiz.cameracoroutines.sample.viewdsl.lParams
import com.beepiz.cameracoroutines.sample.viewdsl.matchParent
import com.beepiz.cameracoroutines.sample.viewdsl.v
import com.beepiz.cameracoroutines.sample.viewdsl.verticalLayout

class DoubleCamTestUi(override val ctx: Activity) : Ui {

    private val surfaceView1 = v(::SurfaceView)

    val surfaceHolderState1 = SurfaceHolderState(surfaceView1.holder)
    val previewSurface1: Surface get() = surfaceView1.holder.surface

    private val surfaceView2 = v(::SurfaceView)

    val surfaceHolderState2 = SurfaceHolderState(surfaceView2.holder)
    val previewSurface2: Surface get() = surfaceView2.holder.surface

    override val root = v(::verticalLayout) {
        add(surfaceView1, lParams(width = matchParent, height = 0) {
            weight = 1f
        })
        add(surfaceView2, lParams(width = matchParent, height = 0) {
            weight = 1f
        })
    }
}
