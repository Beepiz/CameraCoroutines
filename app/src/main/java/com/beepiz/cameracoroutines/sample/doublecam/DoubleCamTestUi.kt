package com.beepiz.cameracoroutines.sample.doublecam

import android.content.Context
import android.view.Surface
import android.view.SurfaceView
import com.beepiz.cameracoroutines.sample.recording.SurfaceHolderState
import splitties.views.dsl.core.*

class DoubleCamTestUi(override val ctx: Context) : Ui {
    private val surfaceView1 = view(::SurfaceView)

    val surfaceHolderState1 = SurfaceHolderState(surfaceView1.holder)
    val previewSurface1: Surface get() = surfaceView1.holder.surface

    private val surfaceView2 = view(::SurfaceView)

    val surfaceHolderState2 = SurfaceHolderState(surfaceView2.holder)
    val previewSurface2: Surface get() = surfaceView2.holder.surface

    override val root = verticalLayout {
        add(surfaceView1, lParams(width = matchParent, height = 0) {
            weight = 1f
        })
        add(surfaceView2, lParams(width = matchParent, height = 0) {
            weight = 1f
        })
    }
}
