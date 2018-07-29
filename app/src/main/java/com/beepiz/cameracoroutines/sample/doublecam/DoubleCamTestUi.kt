package com.beepiz.cameracoroutines.sample.doublecam

import android.content.Context
import android.view.Surface
import android.view.SurfaceView
import com.beepiz.cameracoroutines.sample.recording.SurfaceHolderState
import splitties.viewdsl.core.Ui
import splitties.viewdsl.core.add
import splitties.viewdsl.core.lParams
import splitties.viewdsl.core.matchParent
import splitties.viewdsl.core.v
import splitties.viewdsl.core.verticalLayout

class DoubleCamTestUi(override val ctx: Context) : Ui {
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
