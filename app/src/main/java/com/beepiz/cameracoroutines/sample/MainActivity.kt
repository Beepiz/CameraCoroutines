package com.beepiz.cameracoroutines.sample

import android.Manifest
import android.annotation.SuppressLint
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.M
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import com.beepiz.cameracoroutines.sample.doublecam.DoubleCamTestActivity
import splitties.activities.start
import splitties.viewdsl.appcompat.button
import splitties.viewdsl.core.add
import splitties.viewdsl.core.contentView
import splitties.viewdsl.core.lParams
import splitties.viewdsl.core.v
import splitties.viewdsl.core.verticalLayout
import splitties.views.onClick

class MainActivity : AppCompatActivity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        contentView = v(::verticalLayout) {
            val lp = lParams(gravity = Gravity.CENTER_HORIZONTAL)
            if (SDK_INT >= M) add(v(::button) {
                text = "Request camera permission"
                onClick {
                    requestPermissions(arrayOf(Manifest.permission.CAMERA), 0)
                }
            }, lp)
            if (SDK_INT >= M) add(v(::button) {
                text = "Request microphone permission"
                onClick {
                    requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 1)
                }
            }, lp)
            add(v(::button) {
                text = "Open Cam test"
                onClick { start<CamTestActivity>() }
            }, lp)
            add(v(::button) {
                text = "Double cam test"
                onClick { start<DoubleCamTestActivity>() }
            }, lp)
            add(v(::button) {
                text = "Launch extensions approach Activity"
                onClick { start<ExtensionsApproachCamActivity>() }
            }, lp)
        }
    }
}
